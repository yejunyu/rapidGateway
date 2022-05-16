package com.yejunyu.rapid.core;

import com.yejunyu.rapid.common.constants.RapidBufferHelper;
import com.yejunyu.rapid.core.netty.NettyHttpClient;
import com.yejunyu.rapid.core.netty.NettyHttpServer;
import com.yejunyu.rapid.core.netty.processor.NettyBatchProcessor;
import com.yejunyu.rapid.core.netty.processor.NettyCoreProcessor;
import com.yejunyu.rapid.core.netty.processor.NettyMpmcProcessor;
import com.yejunyu.rapid.core.netty.processor.Processor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author : YeJunyu
 * @description : 主启动容器
 * 所有依赖的其他容器一起初始化,启动和关闭
 * @email : yyyejunyu@gmail.com
 * @date : 2022/5/9
 */
@Slf4j
public class RapidContainer implements LifeCycle {

    private final RapidConfig rapidConfig;

    private Processor processor;

    private NettyHttpServer nettyHttpServer;

    private NettyHttpClient nettyHttpClient;

    public RapidContainer(RapidConfig rapidConfig) {
        this.rapidConfig = rapidConfig;
        init();
    }

    @Override
    public void init() {
        // 1. 构建核心处理器
        NettyCoreProcessor nettyCoreProcessor = new NettyCoreProcessor();
        // 2. 是否开启缓冲
        final String bufferType = rapidConfig.getBufferType();
        if (RapidBufferHelper.isFlusher(bufferType)) {
            processor = new NettyBatchProcessor(rapidConfig, nettyCoreProcessor);
        } else if (RapidBufferHelper.isMpmc(bufferType)) {
            processor = new NettyMpmcProcessor(rapidConfig, nettyCoreProcessor);
        } else {
            this.processor = nettyCoreProcessor;
        }
        // 3. 创建 httpserver
        nettyHttpServer = new NettyHttpServer(rapidConfig, processor);
        nettyHttpClient = new NettyHttpClient(rapidConfig, nettyHttpServer.getBossEventLoopGroup());
    }

    @Override
    public void start() {
        processor.start();
        nettyHttpServer.start();
        nettyHttpClient.start();
        log.info("RapidContainer start!");
    }

    @Override
    public void shutdown() {
        nettyHttpClient.shutdown();
        nettyHttpServer.shutdown();
        processor.shutdown();
        log.info("RapidContainer shutdown!");
    }
}
