package com.yejunyu.rapid.core.netty.processor;

import com.lmax.disruptor.dsl.ProducerType;
import com.yejunyu.rapid.common.concurrent.queue.flusher.ParallelFlusher;
import com.yejunyu.rapid.common.enums.ResponseCode;
import com.yejunyu.rapid.core.RapidConfig;
import com.yejunyu.rapid.core.context.HttpRequestWrapper;
import com.yejunyu.rapid.core.helpers.ResponseHelper;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;

/**
 * @author : YeJunyu
 * @description :
 * @email : yyyejunyu@gmail.com
 * @date : 2022/5/15
 */
@Slf4j
public class NettyBatchProcessor implements Processor {

    private static final String THREAD_NAME_PREFIX = "rapid-flusher-";

    private final NettyCoreProcessor nettyCoreProcessor;

    private final ParallelFlusher<HttpRequestWrapper> parallelFlusher;

    public NettyBatchProcessor(RapidConfig rapidConfig, NettyCoreProcessor nettyCoreProcessor) {
        this.nettyCoreProcessor = nettyCoreProcessor;
        final ParallelFlusher.Builder<HttpRequestWrapper> builder = new ParallelFlusher.Builder<HttpRequestWrapper>()
                .setBufferSize(rapidConfig.getBufferSize())
                .setThreads(rapidConfig.getProcessThread())
                .setProducerType(ProducerType.MULTI)
                .setNamePrefix(THREAD_NAME_PREFIX)
                .setWaitStrategy(rapidConfig.getWaitStrategy());
        final BatchEventProcessorListener batchEventProcessorListener = new BatchEventProcessorListener();
        builder.setListener(batchEventProcessorListener);
        this.parallelFlusher = builder.build();
    }

    @Override
    public void process(HttpRequestWrapper httpRequestWrapper) {
        this.parallelFlusher.add(httpRequestWrapper);
    }

    @Override
    public void start() {
        this.nettyCoreProcessor.start();
        this.parallelFlusher.start();
    }

    @Override
    public void shutdown() {
        this.nettyCoreProcessor.shutdown();
        this.parallelFlusher.shutDown();
    }

    /**
     * 监听事件的核心处理逻辑
     */
    public class BatchEventProcessorListener implements ParallelFlusher.EventListener<HttpRequestWrapper> {

        @Override
        public void onEvent(HttpRequestWrapper event) throws Exception {
            nettyCoreProcessor.process(event);
        }

        @Override
        public void onException(Throwable t, long sequence, HttpRequestWrapper event) {
            final FullHttpRequest fullHttpRequest = event.getFullHttpRequest();
            final ChannelHandlerContext ctx = event.getCtx();
            try {
                log.error("BatchEventProcessorListener#onException 请求处理失败, request: {}.", fullHttpRequest, t);
                // 构建响应对象
                final FullHttpResponse httpResponse = ResponseHelper.getHttpResponse(ResponseCode.INTERNAL_ERROR);
                if (!HttpUtil.isKeepAlive(fullHttpRequest)) {
                    ctx.writeAndFlush(httpResponse).addListener(ChannelFutureListener.CLOSE);
                } else {
                    // 如果连接还在, 设置一下连接, 重发一下消息
                    httpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                    ctx.writeAndFlush(httpResponse);
                }
            } catch (Exception e) {
                log.error("BatchEventProcessorListener#onException 请求回写失败, request: {}.", fullHttpRequest, t);
            }
        }
    }
}
