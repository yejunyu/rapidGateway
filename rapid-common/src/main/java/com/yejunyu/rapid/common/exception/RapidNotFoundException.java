package com.yejunyu.rapid.common.exception;

import com.yejunyu.rapid.common.enums.ResponseCode;

/**
 * Created by @author yejunyu on 2022/6/23
 *
 * @email : yyyejunyu@gmail.com
 */
public class RapidNotFoundException extends RapidBaseException {

    public RapidNotFoundException(ResponseCode code) {
        super(code.getMessage(), code);
    }

    public RapidNotFoundException(Throwable throwable, ResponseCode code) {
        super(code.getMessage(), throwable, code);
    }
}
