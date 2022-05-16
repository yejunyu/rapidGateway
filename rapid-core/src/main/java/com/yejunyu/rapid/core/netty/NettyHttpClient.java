package com.yejunyu.rapid.core.netty;

import com.yejunyu.rapid.core.LifeCycle;
import com.yejunyu.rapid.core.RapidConfig;
import com.yejunyu.rapid.core.helpers.AsyncHttpHelper;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.EventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;

import java.io.IOException;

/**
 * @author : YeJunyu
 * @description : http 转 http 下游服务的 client
 * @email : yyyejunyu@gmail.com
 * @date : 2022/5/9
 */
@Slf4j
public class NettyHttpClient implements LifeCycle {

    private AsyncHttpClient asyncHttpClient;

    private DefaultAsyncHttpClientConfig.Builder clientBuilder;

    private final RapidConfig rapidConfig;

    private final EventLoopGroup workEventLoopGroup;

    public NettyHttpClient(RapidConfig rapidConfig, EventLoopGroup eventLoopGroup) {
        this.rapidConfig = rapidConfig;
        this.workEventLoopGroup = eventLoopGroup;
        init();
    }

    @Override
    public void init() {
        this.clientBuilder = new DefaultAsyncHttpClientConfig.Builder()
                .setFollowRedirect(false)
                .setEventLoopGroup(workEventLoopGroup)
                .setAllocator(PooledByteBufAllocator.DEFAULT)
                .setCompressionEnforced(true)
                .setConnectTimeout(rapidConfig.getHttpConnectTimeout())
                .setRequestTimeout(rapidConfig.getHttpRequestTimeout())
                .setMaxRequestRetry(rapidConfig.getHttpMaxRequestRetryTimes())
                .setMaxConnections(rapidConfig.getHttpMaxConnections())
                .setMaxConnectionsPerHost(rapidConfig.getHttpConnectionsPerHost())
                .setPooledConnectionIdleTimeout(rapidConfig.getHttpPooledConnectionIdleTimeout());

    }

    @Override
    public void start() {
        this.asyncHttpClient = new DefaultAsyncHttpClient(clientBuilder.build());
        AsyncHttpHelper.getInstance().init(asyncHttpClient);
    }

    @Override
    public void shutdown() {
        if (asyncHttpClient != null) {
            try {
                this.asyncHttpClient.close();
            } catch (IOException e) {
                log.error("NettyHttpClient#shutdown error ", e);
            }
        }
    }
}
