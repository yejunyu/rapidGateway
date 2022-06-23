package com.yejunyu.rapid.common.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * Created by @author yejunyu on 2022/6/16
 * 资源服务定义: 下游服务需要实现接口完成服务注册
 *
 * @email : yyyejunyu@gmail.com
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceDefinition implements Serializable {

    /**
     * serviceId:version
     */
    private String uniqueId;

    private String serviceId;

    private String version;
    /**
     * 具体协议:http dubbo grpc
     */
    private String protocol;
    /**
     * 路径匹配规则: 访问只是 ant 表达式:定义具体的服务路径匹配规则
     */
    private String patternPath;

    private String envType;
    /**
     * 服务是否开启, 切流量上线可用
     */
    private boolean enable = true;
    /**
     * 服务列表信息
     * key: path
     */
    private Map<String, ServiceInvoker> invokerMap;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceDefinition that = (ServiceDefinition) o;
        return Objects.equals(uniqueId, that.uniqueId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uniqueId);
    }
}
