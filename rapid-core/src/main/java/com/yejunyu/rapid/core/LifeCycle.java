package com.yejunyu.rapid.core;

/**
 * @author : YeJunyu
 * @description : 容器生命周期
 * @email : yyyejunyu@gmail.com
 * @date : 2022/5/9
 */
public interface LifeCycle {
    /**
     * 容器初始化
     */
    void init();

    /**
     * 容器启动
     */
    void start();

    /**
     * 容器关闭
     */
    void shutdown();
}
