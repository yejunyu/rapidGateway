package com.yejunyu.rapid.core.helpers;

import org.asynchttpclient.*;

import java.util.concurrent.CompletableFuture;

/**
 * @author : YeJunyu
 * @description : asyncHttpClint 执行辅助类
 * @email : yyyejunyu@gmail.com
 * @date : 2022/5/16
 */
public class AsyncHttpHelper {

    private AsyncHttpHelper() {
    }

    private static final class SingletonHolder {
        private static final AsyncHttpHelper INSTANCE = new AsyncHttpHelper();
    }

    public static AsyncHttpHelper getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private AsyncHttpClient asyncHttpClient;

    public void init(AsyncHttpClient asyncHttpClient) {
        this.asyncHttpClient = asyncHttpClient;
    }

    public CompletableFuture<Response> executeRequest(Request request) {
        final ListenableFuture<Response> future = asyncHttpClient.executeRequest(request);
        return future.toCompletableFuture();
    }

    public <T> CompletableFuture<T> executeRequest(Request request, AsyncHandler<T> handler) {
        final ListenableFuture<T> future = asyncHttpClient.executeRequest(request, handler);
        return future.toCompletableFuture();
    }

}
