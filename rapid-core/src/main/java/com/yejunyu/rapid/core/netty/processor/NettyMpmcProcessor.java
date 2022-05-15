package com.yejunyu.rapid.core.netty.processor;

import com.yejunyu.rapid.core.RapidConfig;
import com.yejunyu.rapid.core.context.HttpRequestWrapper;

/**
 * @author : YeJunyu
 * @description :
 * @email : yyyejunyu@gmail.com
 * @date : 2022/5/15
 */
public class NettyMpmcProcessor implements Processor {

    private RapidConfig rapidConfig;

    private NettyCoreProcessor nettyCoreProcessor;

    public NettyMpmcProcessor(RapidConfig rapidConfig, NettyCoreProcessor nettyCoreProcessor) {
        this.rapidConfig = rapidConfig;
        this.nettyCoreProcessor = nettyCoreProcessor;
    }

    @Override
    public void process(HttpRequestWrapper httpRequestWrapper) {

    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }
}
