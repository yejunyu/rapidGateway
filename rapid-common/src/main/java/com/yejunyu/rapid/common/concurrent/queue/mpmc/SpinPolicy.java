package com.yejunyu.rapid.common.concurrent.queue.mpmc;

/**
 * @author : YeJunyu
 * @description :
 * @email : yyyejunyu@gmail.com
 * @date : 2022/6/6
 */
public enum SpinPolicy {
    WAITING,
    BLOCKING,
    SPINNING,
    ;
}
