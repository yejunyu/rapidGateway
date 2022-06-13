package com.yejunyu.rapid.core.netty.processor;

import com.yejunyu.rapid.common.concurrent.queue.mpmc.MpmcBlockingQueue;
import com.yejunyu.rapid.common.enums.ResponseCode;
import com.yejunyu.rapid.core.RapidConfig;
import com.yejunyu.rapid.core.context.HttpRequestWrapper;
import com.yejunyu.rapid.core.helpers.ResponseHelper;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author : YeJunyu
 * @description :
 * @email : yyyejunyu@gmail.com
 * @date : 2022/5/15
 */
@Slf4j
public class NettyMpmcProcessor implements Processor {

    private RapidConfig rapidConfig;

    private NettyCoreProcessor nettyCoreProcessor;

    private MpmcBlockingQueue<HttpRequestWrapper> mpmcBlockingQueue;

    private ExecutorService executorService;

    private volatile boolean isRunning = false;

    private Thread consumerProcessorThread;

    public NettyMpmcProcessor(RapidConfig rapidConfig, NettyCoreProcessor nettyCoreProcessor) {
        this.rapidConfig = rapidConfig;
        this.nettyCoreProcessor = nettyCoreProcessor;
        this.mpmcBlockingQueue = new MpmcBlockingQueue<>(rapidConfig.getBufferSize());
    }

    @Override
    public void process(HttpRequestWrapper httpRequestWrapper) throws Exception {
        log.info("NettyMpmcProcessor add!");
        this.mpmcBlockingQueue.put(httpRequestWrapper);
    }

    @Override
    public void start() {
        this.isRunning = true;
        this.nettyCoreProcessor.start();
        this.executorService = Executors.newFixedThreadPool(rapidConfig.getProcessThread());
        for (int i = 0; i < rapidConfig.getProcessThread(); i++) {
            this.executorService.submit(new ConsumerProcessor());
        }
    }

    @Override
    public void shutdown() {
        this.isRunning = false;
        this.nettyCoreProcessor.shutdown();
        this.executorService.shutdown();
    }

    /**
     * 消费者核心实现类
     */
    public class ConsumerProcessor implements Runnable {

        @Override
        public void run() {
            while (isRunning) {
                HttpRequestWrapper event = null;
                try {
                    event = mpmcBlockingQueue.take();
                    nettyCoreProcessor.process(event);
                } catch (Exception e) {
                    if (event != null) {
                        final HttpRequest request = event.getFullHttpRequest();
                        final ChannelHandlerContext ctx = event.getCtx();
                        try {
                            log.error("ConsumerProcessor#onException 请求处理失败, request: {}.", request, e);
                            // 构建响应对象
                            final FullHttpResponse httpResponse = ResponseHelper.getHttpResponse(ResponseCode.INTERNAL_ERROR);
                            if (!HttpUtil.isKeepAlive(request)) {
                                ctx.writeAndFlush(httpResponse).addListener(ChannelFutureListener.CLOSE);
                            } else {
                                // 如果连接还在, 设置一下连接, 重发一下消息
                                httpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                                ctx.writeAndFlush(httpResponse);
                            }
                        } catch (Exception t) {
                            log.error("ConsumerProcessor#onException 请求回写失败, request: {}.", request, t);
                        }
                    } else {
                        log.error("ConsumerProcessor# onException event is Empty ", e);
                    }
                }
            }
        }
    }
}
