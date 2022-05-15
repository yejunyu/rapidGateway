package com.yejunyu.rapid.core.netty.handlers;

import com.yejunyu.rapid.core.context.HttpRequestWrapper;
import com.yejunyu.rapid.core.netty.processor.Processor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author : YeJunyu
 * @description : http 业务核心处理器
 * @email : yyyejunyu@gmail.com
 * @date : 2022/5/13
 */
@Slf4j
public class NettyHttpServerHandler extends ChannelInboundHandlerAdapter {

    private Processor processor;

    public NettyHttpServerHandler(Processor processor) {
        this.processor = processor;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            FullHttpRequest request = (FullHttpRequest) msg;
            HttpRequestWrapper httpRequestWrapper = new HttpRequestWrapper();
            httpRequestWrapper.setFullHttpRequest(request);
            httpRequestWrapper.setCtx(ctx);

            // processor
            processor.process(httpRequestWrapper);
        } else {
            log.error("NettyHttpServerHandler#channelRead message type is not HttpRequest {}", msg);
            final boolean isRelease = ReferenceCountUtil.release(msg);
            if (!isRelease) {
                log.error("NettyHttpServerHandler#channelRead release fail 资源释放失败!");
            }
        }
    }
}
