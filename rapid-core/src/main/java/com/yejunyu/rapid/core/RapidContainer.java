package com.yejunyu.rapid.core;

import com.yejunyu.rapid.common.constants.RapidBufferHelper;
import com.yejunyu.rapid.core.netty.NettyHttpClient;
import com.yejunyu.rapid.core.netty.NettyHttpServer;
import com.yejunyu.rapid.core.netty.processor.NettyBatchProcessor;
import com.yejunyu.rapid.core.netty.processor.NettyCoreProcessor;
import com.yejunyu.rapid.core.netty.processor.NettyMpmcProcessor;
import com.yejunyu.rapid.core.netty.processor.Processor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : YeJunyu
 * @description : 主启动容器
 * 所有依赖的其他容器一起初始化,启动和关闭
 * @email : yyyejunyu@gmail.com
 * @date : 2022/5/9
 */
@Slf4j
public class RapidContainer implements LifeCycle {

    /**
     * 核心配置
     */
    private final RapidConfig rapidConfig;
    /**
     * 消息核心处理器
     */
    private Processor processor;
    /**
     * 接收 http 请求的 server
     */
    private NettyHttpServer nettyHttpServer;
    /**
     * http 请求转发核心类
     */
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
        // 3. 创建 http server
        nettyHttpServer = new NettyHttpServer(rapidConfig, processor);
        // 4. 创建 http client
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
