package com.yejunyu.rapid.common.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by @author yejunyu on 2022/6/16
 *
 * @email : yyyejunyu@gmail.com
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ServiceInstance implements Serializable {
    /**
     * 服务实例 id: ip:port
     */
    private String serviceInstanceId;
    /**
     * 服务唯一 id: serviceId:version
     */
    private String uniqueId;
    /**
     * 服务地址: ip:port
     */
    private String address;
    /**
     * 标签信息
     */
    private String tags;
    /**
     * 服务权重
     */
    private Integer weight;
    /**
     * 服务注册的时间戳: 负载均衡,warmup 预热
     */
    private long registerTimestamp;
    /**
     * 服务实例是否开启
     */
    private boolean enable = true;
    /**
     * 服务版本号
     */
    private String version;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceInstance that = (ServiceInstance) o;
        return Objects.equals(serviceInstanceId, that.serviceInstanceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceInstanceId);
    }
}
