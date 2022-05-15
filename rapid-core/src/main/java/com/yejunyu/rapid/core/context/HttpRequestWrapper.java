package com.yejunyu.rapid.core.context;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.Data;

/**
 * @author : YeJunyu
 * @description :
 * @email : yyyejunyu@gmail.com
 * @date : 2022/5/15
 */
@Data
public class HttpRequestWrapper {

    private FullHttpRequest fullHttpRequest;

    private ChannelHandlerContext ctx;
}
