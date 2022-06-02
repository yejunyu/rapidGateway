package com.yejunyu.rapid.core;

import com.lmax.disruptor.*;
import com.yejunyu.rapid.common.constants.BaseConst;
import com.yejunyu.rapid.common.constants.RapidBufferHelper;
import com.yejunyu.rapid.common.utils.NetUtils;

/**
 * @author : YeJunyu
 * @description : 网关配置
 * @email : yyyejunyu@gmail.com
 * @date : 2021/12/30
 */
public class RapidConfig {

    private int port = 8888;
    // 网关唯一 id 192.168.1.1:8888
    private String rapidId = NetUtils.getLocalIp() + BaseConst.COLON_SEPARATOR + port;
    // 网关注册中心地址, etcd
    private String registerAddr = "http://127.0.0.1:2379";
    // 网关命名空间 dev test prod
    private String nameSpace = "rapid-dev";
    // 网关线程数
    private int processThread = Runtime.getRuntime().availableProcessors();
    // netty boss 线程数
    private int eventLoopBossNum = 1;
    // netty work 线程数
    private int eventLoopWorkNum = processThread;
    // 是否开启 epoll
    private boolean useEpoll = true;
    // 是否开启 netty 内存分配机制
    private boolean nettyAllocator = true;
    // http body 报文最大大小
    private int maxContentLength = 64 * 1024 * 1024;
    // dubbo 开启连接数
    private int dubboConnections = processThread;
    // 设置响应模式, 默认单异步: CompletableFuture 回调
    private boolean whenCompleteAsync = false;

    // 网关队列: 缓冲模式
    private String bufferType = RapidBufferHelper.FLUSHER;
    // 网关队列: 内存队列大小
    private int bufferSize = 1024 * 1024;
    // 网关队列: 阻塞/等待策略 block yield
    private String waitStrategy = "blocking";

    // http Async 参数
    // 连接超时时间 30s
    private int httpConnectTimeout = 30 * 1000;
    // 请求超时时间
    private int httpRequestTimeout = 30 * 1000;
    // 客户端重试次数
    private int httpMaxRequestRetryTimes = 2;
    // 客户端请求最大连接数
    private int httpMaxConnections = 10_000;
    // 客户端每个地址支持最大连接数
    private int httpConnectionsPerHost = 8000;
    // 客户端空闲连接超时时间, 60s
    private int httpPooledConnectionIdleTimeout = 60 * 1000;

    public int getPort() {
        return port;
    }

    public String getRapidId() {
        return rapidId;
    }

    public String getRegisterAddr() {
        return registerAddr;
    }

    public String getNameSpace() {
        return nameSpace;
    }

    public int getProcessThread() {
        return processThread;
    }

    public int getEventLoopBossNum() {
        return eventLoopBossNum;
    }

    public int getEventLoopWorkNum() {
        return eventLoopWorkNum;
    }

    public boolean isUseEpoll() {
        return useEpoll;
    }

    public boolean isNettyAllocator() {
        return nettyAllocator;
    }

    public int getMaxContentLength() {
        return maxContentLength;
    }

    public int getDubboConnections() {
        return dubboConnections;
    }

    public boolean isWhenCompleteAsync() {
        return whenCompleteAsync;
    }

    public String getBufferType() {
        return bufferType;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public WaitStrategy getWaitStrategy() {
        switch (waitStrategy){
            case "yielding":
                return new YieldingWaitStrategy();
            case "busySpin":
                return new BusySpinWaitStrategy();
            case "sleeping":
                return new SleepingWaitStrategy();
            case "blocking":
            default:
                return new BlockingWaitStrategy();
        }
    }

    public int getHttpConnectTimeout() {
        return httpConnectTimeout;
    }

    public int getHttpRequestTimeout() {
        return httpRequestTimeout;
    }

    public int getHttpMaxRequestRetryTimes() {
        return httpMaxRequestRetryTimes;
    }

    public int getHttpMaxConnections() {
        return httpMaxConnections;
    }

    public int getHttpConnectionsPerHost() {
        return httpConnectionsPerHost;
    }

    public int getHttpPooledConnectionIdleTimeout() {
        return httpPooledConnectionIdleTimeout;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setRapidId(String rapidId) {
        this.rapidId = rapidId;
    }

    public void setRegisterAddr(String registerAddr) {
        this.registerAddr = registerAddr;
    }

    public void setNameSpace(String nameSpace) {
        this.nameSpace = nameSpace;
    }

    public void setProcessThread(int processThread) {
        this.processThread = processThread;
    }

    public void setEventLoopBossNum(int eventLoopBossNum) {
        this.eventLoopBossNum = eventLoopBossNum;
    }

    public void setEventLoopWorkNum(int eventLoopWorkNum) {
        this.eventLoopWorkNum = eventLoopWorkNum;
    }

    public void setUseEpoll(boolean useEpoll) {
        this.useEpoll = useEpoll;
    }

    public void setNettyAllocator(boolean nettyAllocator) {
        this.nettyAllocator = nettyAllocator;
    }

    public void setMaxContentLength(int maxContentLength) {
        this.maxContentLength = maxContentLength;
    }

    public void setDubboConnections(int dubboConnections) {
        this.dubboConnections = dubboConnections;
    }

    public void setWhenCompleteAsync(boolean whenCompleteAsync) {
        this.whenCompleteAsync = whenCompleteAsync;
    }

    public void setBufferType(String bufferType) {
        this.bufferType = bufferType;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public void setWaitStrategy(String waitStrategy) {
        this.waitStrategy = waitStrategy;
    }

    public void setHttpConnectTimeout(int httpConnectTimeout) {
        this.httpConnectTimeout = httpConnectTimeout;
    }

    public void setHttpRequestTimeout(int httpRequestTimeout) {
        this.httpRequestTimeout = httpRequestTimeout;
    }

    public void setHttpMaxRequestRetryTimes(int httpMaxRequestRetryTimes) {
        this.httpMaxRequestRetryTimes = httpMaxRequestRetryTimes;
    }

    public void setHttpMaxConnections(int httpMaxConnections) {
        this.httpMaxConnections = httpMaxConnections;
    }

    public void setHttpConnectionsPerHost(int httpConnectionsPerHost) {
        this.httpConnectionsPerHost = httpConnectionsPerHost;
    }

    public void setHttpPooledConnectionIdleTimeout(int httpPooledConnectionIdleTimeout) {
        this.httpPooledConnectionIdleTimeout = httpPooledConnectionIdleTimeout;
    }
}
