package com.yejunyu.rapid.common.exception;

import com.yejunyu.rapid.common.enums.ResponseCode;

/**
 * Created by @author yejunyu on 2022/6/23
 *
 * @email : yyyejunyu@gmail.com
 */
public class RapidPathNotMatchException extends RapidBaseException {

    public RapidPathNotMatchException() {
        this(ResponseCode.PATH_NO_MATCHED);
    }

    public RapidPathNotMatchException(ResponseCode code) {
        super(code.getMessage(), code);
    }

    public RapidPathNotMatchException(Throwable throwable, ResponseCode code) {
        super(code.getMessage(), throwable, code);
    }
}
