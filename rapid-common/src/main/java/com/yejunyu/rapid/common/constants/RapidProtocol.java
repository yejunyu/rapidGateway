package com.yejunyu.rapid.common.constants;

/**
 * Created by @author yejunyu on 2022/6/17
 * 协议定义
 *
 * @email : yyyejunyu@gmail.com
 */
public interface RapidProtocol {

    String HTTP = "http";

    String DUBBO = "dubbo";

    static boolean isHttp(String protocol) {
        return HTTP.equalsIgnoreCase(protocol);
    }

    static boolean isDubbo(String protocol) {
        return DUBBO.equalsIgnoreCase(protocol);
    }
}
