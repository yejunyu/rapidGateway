package com.yejunyu.rapid.core.helpers;

import com.yejunyu.rapid.common.utils.AntPathMatcher;
import com.yejunyu.rapid.core.context.RapidContext;
import com.yejunyu.rapid.core.context.RapidRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
    public static RapidContext doContext(FullHttpRequest request, ChannelHandlerContext ctx) {
        // 1. 构建RapidRequest请求对象
        RapidRequest rapidRequest = buildRequest(request, ctx);

        // 2. 根据请求对象里的 uniqueId 获取资源服务信息
        return null;
    }

    private static RapidRequest buildRequest(FullHttpRequest request, ChannelHandlerContext ctx) {
        return null;
    }
}
