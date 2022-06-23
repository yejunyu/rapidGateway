package com.yejunyu.rapid.core.helpers;

import com.yejunyu.rapid.common.config.DynamicConfigManager;
import com.yejunyu.rapid.common.config.Rule;
import com.yejunyu.rapid.common.config.ServiceDefinition;
import com.yejunyu.rapid.common.config.ServiceInvoker;
import com.yejunyu.rapid.common.constants.BaseConst;
import com.yejunyu.rapid.common.constants.RapidConst;
import com.yejunyu.rapid.common.constants.RapidProtocol;
import com.yejunyu.rapid.common.enums.ResponseCode;
import com.yejunyu.rapid.common.exception.RapidNotFoundException;
import com.yejunyu.rapid.common.exception.RapidPathNotMatchException;
import com.yejunyu.rapid.common.exception.RapidResponseException;
import com.yejunyu.rapid.common.utils.AntPathMatcher;
import com.yejunyu.rapid.core.context.AttributeKey;
import com.yejunyu.rapid.core.context.RapidContext;
import com.yejunyu.rapid.core.context.RapidRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by @author yejunyu on 2022/6/15
 * 请求对象辅助类
 *
 * @email : yyyejunyu@gmail.com
 */
public class RequestHelper {

    private static final AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();

    /**
     * 解析 fullhttprequest 构建 rapidContext 核心构造方法
     *
     * @param request
     * @param ctx
     * @return
     */
    public static RapidContext assembleContext(FullHttpRequest request, ChannelHandlerContext ctx) {
        // 1. 构建RapidRequest请求对象
        RapidRequest rapidRequest = assembleRequest(request, ctx);

        // 2. 根据请求对象里的 uniqueId 获取资源服务信息
        ServiceDefinition serviceDefinition = getServiceDefinition(rapidRequest);

        // 3. 路径匹配
        if (!ANT_PATH_MATCHER.match(serviceDefinition.getPatternPath(), rapidRequest.getPath())) {
            throw new RapidPathNotMatchException();
        }

        // 4. 根据请求对象获取服务定义对应的方法调用, 然后获取对应规则
        ServiceInvoker serviceInvoker = getServiceInvoker(rapidRequest, serviceDefinition);
        final String ruleId = serviceInvoker.getRuleId();
        final Rule rule = DynamicConfigManager.getInstance().getRule(ruleId);

        // 5. 构建 RapidContext 对象
        final RapidContext rapidContext = new RapidContext.ContextBuilder()
                .setProtocol(serviceDefinition.getProtocol())
                .setKeepalive(HttpUtil.isKeepAlive(request))
                .setRapidRequest(rapidRequest)
                .setNettyCtx(ctx)
                .setRule(rule)
                .build();

        // 6. 设置一些必要的上下文参数
        putContext(rapidContext, serviceInvoker);
        return rapidContext;
    }

    /**
     * 往 context 中设置一些必要的上下文参数
     *
     * @param rapidContext   请求上下文
     * @param serviceInvoker 服务调用信息
     */
    private static void putContext(RapidContext rapidContext, ServiceInvoker serviceInvoker) {
        switch (rapidContext.getProtocol()) {
            case RapidProtocol.HTTP:
                rapidContext.putAttribute(AttributeKey.HTTP_INVOKER, serviceInvoker);
                break;
            case RapidProtocol.DUBBO:
                rapidContext.putAttribute(AttributeKey.DUBBO_INVOKER, serviceInvoker);
                break;
            default:
                break;

        }
    }

    /**
     * 根据请求对象获取服务定义方法调用
     *
     * @param rapidRequest      请求对象
     * @param serviceDefinition 服务定义
     * @return 服务调用对象
     */
    private static ServiceInvoker getServiceInvoker(RapidRequest rapidRequest, ServiceDefinition serviceDefinition) {
        final Map<String, ServiceInvoker> invokerMap = serviceDefinition.getInvokerMap();
        final ServiceInvoker serviceInvoker = invokerMap.get(rapidRequest.getPath());
        if (serviceInvoker == null) {
            throw new RapidNotFoundException(ResponseCode.SERVICE_INVOKER_NOT_FOUND);
        }
        return serviceInvoker;
    }

    /**
     * 通过 uniqueId 获取服务定义信息
     *
     * @param rapidRequest 上游请求
     * @return 服务定义信息
     */
    private static ServiceDefinition getServiceDefinition(RapidRequest rapidRequest) {
        // 网关初始化的时候从缓存信息获取 serviceDefinition
        final ServiceDefinition serviceDefinition = DynamicConfigManager.getInstance().getServiceDefinition(rapidRequest.getUniqueId());
        if (serviceDefinition == null) {
            throw new RapidNotFoundException(ResponseCode.SERVICE_DEFINITION_NOT_FOUND);
        }
        return serviceDefinition;
    }

    private static RapidRequest assembleRequest(FullHttpRequest request, ChannelHandlerContext ctx) {
        final HttpHeaders headers = request.headers();
        // header 头必须有关键属性 uniqueId
        final String uniqueId = headers.get(RapidConst.UNIQUE_ID);
        if (StringUtils.isBlank(uniqueId)) {
            throw new RapidResponseException(ResponseCode.REQUEST_PARSE_ERROR_NO_UNIQUEID);
        }
        final String host = headers.get(HttpHeaderNames.HOST);
        final HttpMethod method = request.method();
        final String uri = request.uri();
        String clientIp = getClientIp(request, ctx);
        String contentType = HttpUtil.getMimeType(request) == null ? "" : HttpUtil.getMimeType(request).toString();
        final Charset charset = HttpUtil.getCharset(request, StandardCharsets.UTF_8);

        return new RapidRequest(uniqueId, charset, clientIp, host, uri, method, contentType, headers, request);
    }

    /**
     * 获取客户端ip
     *
     * @param request
     * @param ctx
     * @return
     */
    private static String getClientIp(FullHttpRequest request, ChannelHandlerContext ctx) {
        final String xForwardedValue = request.headers().get(BaseConst.HTTP_FORWARD_SEPARATOR);
        String clientIp = null;
        if (StringUtils.isNoneBlank(xForwardedValue)) {
            final List<String> values = Arrays.asList(xForwardedValue.split(","));
            if (values.size() >= 1 && StringUtils.isNotBlank(values.get(0))) {
                clientIp = values.get(0).trim();
            }
        }
        if (clientIp == null) {
            // 假如请求头中没有就用 netty channel 获取remoteAddress
            final InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
            clientIp = socketAddress.getAddress().getHostAddress();
        }
        return clientIp;
    }
}
