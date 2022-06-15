package com.yejunyu.rapid.core.context;

import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Created by @author yejunyu on 2022/6/13
 *
 * @email : yyyejunyu@gmail.com
 */
public abstract class BaseContext implements Context {

    protected final String protocol;

    protected final ChannelHandlerContext nettyCtx;

    protected final boolean keepAlive;

    protected volatile int status = Context.RUNNING;
    /**
     * 保存所有上下文参数集合
     */
    protected final Map<AttributeKey<?>, Object> attributes = new HashMap<>();
    /**
     * 请求过程中的异常对象
     */
    protected Throwable throwable;
    /**
     * 定义是都已经释放请求资源
     */
    protected final AtomicBoolean requestReleased = new AtomicBoolean(false);
    /**
     * 存放回调的集合
     */
    protected List<Consumer<Context>> completedCallbacks;

    public BaseContext(String protocol, ChannelHandlerContext nettyCtx, boolean keepAlive) {
        this.protocol = protocol;
        this.nettyCtx = nettyCtx;
        this.keepAlive = keepAlive;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public ChannelHandlerContext getNettyCtx() {
        return nettyCtx;
    }

    @Override
    public boolean isKeepAlive() {
        return keepAlive;
    }

    @Override
    public void running() {
        status = Context.RUNNING;
    }

    @Override
    public void written() {
        status = Context.WRITTEN;
    }

    @Override
    public void completed() {
        status = Context.COMPLETED;
    }

    @Override
    public void terminated() {
        status = Context.TERMINATED;
    }

    @Override
    public boolean isRunning() {
        return status == Context.RUNNING;
    }

    @Override
    public boolean isWritten() {
        return status == Context.WRITTEN;
    }

    @Override
    public boolean isCompleted() {
        return status == Context.COMPLETED;
    }

    @Override
    public boolean isTerminated() {
        return status == Context.TERMINATED;
    }

    @Override
    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    @Override
    public Throwable getThrowable() {
        return this.throwable;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAttribute(Object key) {
        return (T) attributes.get(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T putAttribute(AttributeKey<T> key, T value) {
        return (T) attributes.put(key, value);
    }

    @Override
    public boolean releaseRequest() {
        return this.requestReleased.compareAndSet(false, true);
    }

    @Override
    public void completedCallback(Consumer<Context> consumer) {
        if (completedCallbacks == null) {
            completedCallbacks = new ArrayList<>();
        }
        completedCallbacks.add(consumer);
    }

    @Override
    public void invokeCompletedCallback() {
        if (completedCallbacks != null) {
            completedCallbacks.forEach(call -> call.accept(this));
        }
    }
}
