package com.yejunyu.rapid.common.config;

/**
 * Created by @author yejunyu on 2022/6/16
 *
 * @email : yyyejunyu@gmail.com
 */
public interface ServiceInvoker {
    /**
     * 获取真正的服务调用的全路径
     *
     * @return 服务调用全路径
     */
    String getInvokerPath();

    /**
     * 设置服务调用全路径
     *
     * @param invokerPath 服务调用全路径
     */
    void setInvokerPath(String invokerPath);

    /**
     * 获取指定服务调用绑定的唯一规则
     *
     * @return rule id
     */
    String getRuleId();

    /**
     * rule id

     * @param ruleId
     */
    void setRuleId(String ruleId);

    /**
     * 获取服务超时时间
     *
     * @return
     */
    int getTimeout();

    /**
     * 设置服务调用超时时间
     *
     * @param timeout
     */
    void setTimeout(int timeout);
}
