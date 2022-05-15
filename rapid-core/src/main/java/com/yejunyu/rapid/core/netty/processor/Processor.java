package com.yejunyu.rapid.core.netty.processor;

import com.yejunyu.rapid.core.context.HttpRequestWrapper;

/**
 * @author : YeJunyu
 * @description : netty 核心逻辑执行器接口
 * @email : yyyejunyu@gmail.com
 * @date : 2022/5/15
 */
public interface Processor {

    /**
     * 执行器执行
     *
     * @param httpRequestWrapper
     */
    void process(HttpRequestWrapper httpRequestWrapper);

    /**
     * 执行器启动
     */
    void start();

    /**
     * 执行器关闭
     */
    void shutdown();
}
