package com.yejunyu.rapid.core.context;

import com.alibaba.fastjson.JSONPath;
import com.google.common.collect.Lists;
import com.yejunyu.rapid.common.constants.BaseConst;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;

import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by @author yejunyu on 2022/6/13
 *
 * @email : yyyejunyu@gmail.com
 */
@Slf4j
public class RapidRequest implements RequestMutable {
    /**
     * FullHttpRequest: 在header里面必须要有该属性：uniqueId
     * 表示服务的唯一性ID: serviceId:version
     */
    @Getter
    private final String uniqueId;
    /**
     * 请求开始的时间戳
     */
    @Getter
    private final long beginTime;

    @Getter
    private final Charset charset;

    /**
     * 客户端 ip: 流控,黑白名单
     */
    @Getter
    private final String clientIp;
    /**
     * 请求地址: ip:port
     */
    @Getter
    private final String host;

    /**
     * 请求路径: /xxx/xx/xxx
     */
    @Getter
    private final String path;
    /**
     * uri: /xxx/xx/xxx?attr1=value&attr2=value2
     */
    @Getter
    private final String uri;
    /**
     * get/post/put...
     */
    @Getter
    private final HttpMethod method;
    /**
     * 请求格式: application/json;
     */
    @Getter
    private final String contentType;

    @Getter
    private final HttpHeaders headers;

    /**
     * 参数解析器
     */
    @Getter
    private final QueryStringDecoder queryStringDecoder;
    /**
     * fullHttpRequest
     */
    @Getter
    private final FullHttpRequest httpRequest;
    /**
     * 请求体
     */
    private String body;

    /**
     * 请求对象里的 cookie
     */
    private Map<String, Cookie> cookieMap;
    /**
     * 请求是定义的 post 参数集合
     */
    private Map<String, List<String>> postParameters;

    /********************** 下面是可修改的请求变量 **************/
    /**
     * 默认为 http://
     */
    private String modifyScheme;
    /**
     * 可修改的 host, 原 host 记录着
     */
    private String modifyHost;
    /**
     * 可修改的path, 原 path 记录着
     */
    private String modifyPath;
    /**
     * 构建下游请求的 http 请求构建器
     */
    private final RequestBuilder requestBuilder;

    public RapidRequest(String uniqueId, Charset charset, String clientIp, String host, String uri, HttpMethod method,
                        String contentType, HttpHeaders headers, FullHttpRequest fullHttpRequest) {
        this.uniqueId = uniqueId;
        this.beginTime = System.currentTimeMillis();
        this.charset = charset;
        this.clientIp = clientIp;
        this.host = host;
        this.method = method;
        this.contentType = contentType;
        this.headers = headers;
        this.uri = uri;
        this.queryStringDecoder = new QueryStringDecoder(uri, charset);
        this.path = queryStringDecoder.path();
        this.httpRequest = fullHttpRequest;
        this.modifyHost = host;
        this.modifyPath = path;
        this.modifyScheme = BaseConst.HTTP_PREFIX_SEPARATOR;
        this.requestBuilder = new RequestBuilder();
        this.requestBuilder.setMethod(getMethod().name())
                .setHeaders(getHeaders())
                .setQueryParams(queryStringDecoder.parameters());
        ByteBuf contentBuf = fullHttpRequest.content();
        if (Objects.nonNull(contentBuf)) {
            this.requestBuilder.setBody(contentBuf.nioBuffer());
        }
    }

    /**
     * 获取 body
     *
     * @return 请求文本
     */
    public String getBody() {
        if (StringUtils.isEmpty(body)) {
            body = httpRequest.content().toString(charset);
        }
        return body;
    }

    public Cookie getCookie(String name) {
        if (cookieMap == null) {
            cookieMap = new HashMap<>();
            final String cookieStr = getHeaders().get(HttpHeaderNames.COOKIE);
            final Set<Cookie> cookies = ServerCookieDecoder.STRICT.decode(cookieStr);
            for (Cookie cookie : cookies) {
                cookieMap.put(name, cookie);
            }

        }
        return cookieMap.get(name);
    }

    /**
     * 获取指定名称的参数值
     *
     * @param name
     * @return
     */
    public List<String> getQueryParametersMultiple(String name) {
        return queryStringDecoder.parameters().get(name);
    }

    public List<String> getPostParametersMultiple(String name) {
        final String body = getBody();
        if (isFormPost()) {
            if (postParameters == null) {
                QueryStringDecoder paramDecoder = new QueryStringDecoder(body, false);
                postParameters = paramDecoder.parameters();
            }
            if (CollectionUtils.isEmptyMap(postParameters)) {
                return null;
            } else {
                return postParameters.get(name);
            }
        } else if (isJsonPost()) {
            try {
                return Lists.newArrayList(JSONPath.read(body, name).toString());
            } catch (Exception e) {
                log.error("RapidRequest#getPostParametersMultiple jsonpath解析失败 jsonpath:{}, body:{} ", name, body, e);
            }
        } else {
            log.error("RapidRequest#getPostParametersMultiple unknown request form");
        }
        return null;
    }

    @Override
    public void setModifyHost(String host) {
        this.modifyHost = host;
    }

    @Override
    public String getModifyHost() {
        return modifyHost;
    }

    @Override
    public void setModifyPath(String path) {
        this.modifyPath = path;
    }

    @Override
    public String getModifyPath() {
        return modifyPath;
    }

    @Override
    public void addHeader(CharSequence name, String value) {
        requestBuilder.addHeader(name, value);
    }

    @Override
    public void setHeader(CharSequence name, String value) {
        requestBuilder.setHeader(name, value);
    }

    @Override
    public void addQueryParam(String name, String value) {
        requestBuilder.addQueryParam(name, value);
    }

    @Override
    public void addOrReplaceCookie(org.asynchttpclient.cookie.Cookie cookie) {
        requestBuilder.addOrReplaceCookie(cookie);
    }

    @Override
    public void addFormParam(String name, String value) {
        if (isFormPost()) {
            requestBuilder.addFormParam(name, value);
        }
    }

    @Override
    public void setRequestTimeout(int timeout) {
        requestBuilder.setRequestTimeout(timeout);
    }

    @Override
    public Request build() {
        requestBuilder.setUrl(getFinalUrl());
        return requestBuilder.build();
    }

    @Override
    public String getFinalUrl() {
        return modifyScheme + modifyHost + modifyPath;
    }

    /**
     * 是否是 form 形式的 post
     *
     * @return
     */
    public boolean isFormPost() {
        return HttpMethod.POST.equals(method) &&
                (contentType.startsWith(HttpHeaderValues.FORM_DATA.toString()) ||
                        contentType.startsWith(HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString()));
    }

    public boolean isJsonPost() {
        return HttpMethod.POST.equals(method) &&
                contentType.startsWith(HttpHeaderValues.APPLICATION_JSON.toString());
    }
}
