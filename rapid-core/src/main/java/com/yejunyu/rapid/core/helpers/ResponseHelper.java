package com.yejunyu.rapid.core.helpers;

import com.yejunyu.rapid.common.enums.ResponseCode;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;

import java.nio.charset.StandardCharsets;

/**
 * @author : YeJunyu
 * @description : 响应对象
 * @email : yyyejunyu@gmail.com
 * @date : 2022/5/31
 */
public class ResponseHelper {

    public static FullHttpResponse getHttpResponse(ResponseCode responseCode) {
        DefaultFullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR,
                Unpooled.wrappedBuffer(responseCode.getMessage().getBytes(StandardCharsets.UTF_8)));
        httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON + ";charset=utf-8");
        httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, responseCode.getMessage().length());
        return httpResponse;
    }
}
