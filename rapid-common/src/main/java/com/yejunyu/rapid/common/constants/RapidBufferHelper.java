package com.yejunyu.rapid.common.constants;

/**
 * @author : YeJunyu
 * @description : 缓冲区辅助类
 * @email : yyyejunyu@gmail.com
 * @date : 2021/12/30
 */
public interface RapidBufferHelper {

    String FLUSHER = "FLUSHER";

    String MPMC = "MPMC";

    static boolean isMpmc(String bufferType) {
        return MPMC.equals(bufferType);
    }

    static boolean isFlusher(String bufferType) {
        return FLUSHER.equals(bufferType);
    }
}
