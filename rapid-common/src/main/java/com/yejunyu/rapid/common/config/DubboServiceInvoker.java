package com.yejunyu.rapid.common.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by @author yejunyu on 2022/6/16
 *
 * @email : yyyejunyu@gmail.com
 */
@Getter
@Setter
public class DubboServiceInvoker extends BaseServiceInvoker {

    private String registerAddress;

    private String interfaceClass;

    private String methodName;

    private String[] parameterTypes;

    private String version;
}
