package com.yejunyu.rapid.core.netty.processor;

import com.yejunyu.rapid.core.context.HttpRequestWrapper;
import com.yejunyu.rapid.core.context.RapidContext;
import com.yejunyu.rapid.core.helpers.RequestHelper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * @author : YeJunyu
 * @description :
 * @email : yyyejunyu@gmail.com
 * @date : 2022/5/15
 */
@Slf4j
public class NettyCoreProcessor implements Processor {

    @Override
    public void process(HttpRequestWrapper event) {
        final ChannelHandlerContext ctx = event.getCtx();
        final FullHttpRequest request = event.getFullHttpRequest();
        try {
            // 1. 解析 httpRequest 转成内部对象 Context
            final RapidContext rapidContext = RequestHelper.assembleContext(request, ctx);
            // 2. 执行过滤器逻辑 FilterChain

            // 3.
        } catch (Exception e) {

        } finally {

        }

    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }
}
