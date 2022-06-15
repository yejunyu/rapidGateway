package com.yejunyu.rapid.core.context;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yejunyu.rapid.common.enums.ResponseCode;
import com.yejunyu.rapid.common.utils.JSONUtil;
import io.netty.handler.codec.http.*;
import lombok.Data;
import org.asynchttpclient.Response;

/**
 * Created by @author yejunyu on 2022/6/14
 * 构建下游响应返回值
 *
 * @email : yyyejunyu@gmail.com
 */
@Data
public class RapidResponse {

    private HttpHeaders responseHeaders = new DefaultHttpHeaders();
    /**
     * 额外的响应结果
     */
    private final HttpHeaders extResponseHeaders = new DefaultHttpHeaders();

    private String content;

    private HttpResponseStatus httpResponseStatus;

    private Response futureResponse;

    private RapidResponse() {
    }

    /**
     * 设置响应头
     *
     * @param key
     * @param value
     */
    public void addHeader(CharSequence key, CharSequence value) {
        responseHeaders.add(key, value);
    }

    /**
     * 构建网关响应对象
     *
     * @param futureResponse 异步回调返回值
     * @return 网关响应对象
     */
    public static RapidResponse buildRapidResponse(Response futureResponse) {
        final RapidResponse rapidResponse = new RapidResponse();
        rapidResponse.setFutureResponse(futureResponse);
        rapidResponse.setHttpResponseStatus(HttpResponseStatus.valueOf(futureResponse.getStatusCode()));
        return rapidResponse;
    }

    /**
     * 构建一个 json 返回对象, 失败时用
     *
     * @param responseCode 错误码
     * @param objects
     * @return
     */
    public static RapidResponse buildRapidResponse(ResponseCode responseCode, Object... objects) {
        ObjectNode node = JSONUtil.createObjectNode();
        node.put(JSONUtil.STATUS, responseCode.getStatus().code());
        node.put(JSONUtil.CODE, responseCode.getCode());
        node.put(JSONUtil.MESSAGE, responseCode.getMessage());
        final RapidResponse rapidResponse = new RapidResponse();
        rapidResponse.setHttpResponseStatus(responseCode.getStatus());
        rapidResponse.addHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON + ";charset=utf-8");
        rapidResponse.setContent(JSONUtil.toJSONString(node));
        return rapidResponse;
    }

    /**
     * 构建一个 json 返回对象, 成功时用
     *
     * @param data
     * @return
     */
    public static RapidResponse buildRapidResponse(Object data) {
        ObjectNode node = JSONUtil.createObjectNode();
        node.put(JSONUtil.STATUS, ResponseCode.SUCCESS.getStatus().code());
        node.put(JSONUtil.CODE, ResponseCode.SUCCESS.getCode());
        node.putPOJO(JSONUtil.DATA, data);
        final RapidResponse rapidResponse = new RapidResponse();
        rapidResponse.setHttpResponseStatus(ResponseCode.SUCCESS.getStatus());
        rapidResponse.addHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON + ";charset=utf-8");
        rapidResponse.setContent(JSONUtil.toJSONString(node));
        return rapidResponse;
    }
}
