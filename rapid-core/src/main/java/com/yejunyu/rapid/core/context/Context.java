package com.yejunyu.rapid.core.context;

import com.yejunyu.rapid.common.config.Rule;
import io.netty.channel.ChannelHandlerContext;

import java.util.function.Consumer;

/**
 * @author : YeJunyu
 * @description : 网关请求上下文定义接口
 * @email : yyyejunyu@gmail.com
 * @date : 2022/6/5
 */
public interface Context {
    /**
     * 一个请求正在进行中
     */
    int RUNNING = -1;
    /**
     * 写回响应标记, 标记当前请求需要写回
     */
    int WRITTEN = 0;
    /**
     * 当写回成功后,设置改标记" ctx.writeAndFlush
     */
    int COMPLETED = 1;
    /**
     * 表示整个流程完毕, completed 并不是最终结束, 因为有一些后置处理器
     * 比如 统计请求个数, 响应时间之类的
     */
    int TERMINATED = 2;

    /**
     * 设置状态到 running
     */
    void running();

    /**
     * 设置状态到 written
     */
    void written();

    /**
     * 设置状态到 completed
     */
    void completed();

    /**
     * 设置状态到 terminated
     */
    void terminated();

    /**
     * 判断状态是否是 running
     */
    boolean isRunning();

    /**
     * 判断状态是否是 written
     */
    boolean isWritten();

    /**
     * 判断状态是否是 completed
     */
    boolean isCompleted();

    /**
     * 判断状态是否是 terminated
     */
    boolean isTerminated();

    /**
     * 获取请求转换协议
     *
     * @return 协议
     */
    String getProtocol();

    /**
     * 获取 规则
     *
     * @return 规则对象
     */
    Rule getRule();

    /**
     * 上下文中的请求对象
     *
     * @return 请求
     */
    Object getRequest();

    /**
     * 上下文中的返回对象
     *
     * @return 返回对象
     */
    Object getResponse();

    /**
     * 设置返回值
     *
     * @param response
     */
    void setResponse(Object response);

    /**
     * 设置异常
     *
     * @param throwable
     */
    void setThrowable(Throwable throwable);

    /**
     * 获取异常
     *
     * @return
     */
    Throwable getThrowable();

    /**
     * 获取上下文参数
     *
     * @param key
     * @param <T>
     * @return
     */
    <T> T getAttribute(Object key);

    /**
     * 保存上下文参数
     *
     * @param key
     * @param value
     * @param <T>
     * @return
     */
    <T> T putAttribute(AttributeKey<T> key, T value);

    /**
     * 获取 netty 的 ctx
     *
     * @return
     */
    ChannelHandlerContext getNettyCtx();

    /**
     * 是否保持了连接
     *
     * @return bool
     */
    boolean isKeepAlive();

    /**
     * 释放请求资源
     */
    boolean releaseRequest();

    /**
     * 写回接收回调函数的设置
     */
    void completedCallback(Consumer<Context> consumer);

    /**
     * 回调函数的执行
     */
    void invokeCompletedCallback();
}
