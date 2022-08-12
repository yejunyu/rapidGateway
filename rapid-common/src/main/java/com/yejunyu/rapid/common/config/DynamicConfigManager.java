package com.yejunyu.rapid.common.config;

import com.google.common.collect.Sets;
import lombok.Getter;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by @author yejunyu on 2022/6/17
 * 动态服务换成配置管理
 *
 * @email : yyyejunyu@gmail.com
 */
@Getter
public class DynamicConfigManager {
    /**
     * 服务定义 map:
     * key: uniqueId
     */
    private final ConcurrentHashMap<String, ServiceDefinition> serviceDefinitionMap = new ConcurrentHashMap<>();
    /**
     * 服务实例集合map:
     * key:uniqueId
     */
    private final ConcurrentHashMap<String, Set<ServiceInstance>> serviceInstanceMap = new ConcurrentHashMap<>();
    /**
     * 规则集合:
     * key: ruleId
     */
    private final ConcurrentHashMap<String, Rule> ruleMap = new ConcurrentHashMap<>();

    private DynamicConfigManager() {
    }

    private static class SingletonHolder {
        private static final DynamicConfigManager INSTANCE = new DynamicConfigManager();
    }

    /************** 对服务定义缓存的操作方法 ***************/

    public static DynamicConfigManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void putServiceDefinition(String uniqueId, ServiceDefinition serviceDefinition) {
        serviceDefinitionMap.put(uniqueId, serviceDefinition);
    }

    public ServiceDefinition getServiceDefinition(String uniqueId) {
        return serviceDefinitionMap.get(uniqueId);
    }

    public void removeServiceDefinition(String uniqueId) {
        serviceDefinitionMap.remove(uniqueId);
    }

    /************** 对服务实例缓存的操作方法 ***************/
    /**
     * 添加 serviceInstance 实例
     *
     * @param uniqueId
     * @param serviceInstance
     */
    public void addServiceInstance(String uniqueId, ServiceInstance serviceInstance) {
        serviceInstanceMap.computeIfAbsent(uniqueId, (x) -> {
            if (serviceInstanceMap.containsKey(uniqueId)) {
                serviceInstanceMap.get(uniqueId).add(serviceInstance);
            } else {
                serviceInstanceMap.put(uniqueId, Sets.newHashSet(serviceInstance));
            }
            return serviceInstanceMap.get(uniqueId);
        });
    }

    /**
     * 更新和添加 serviceInstance
     *
     * @param uniqueId
     * @param serviceInstance
     */
    public void updateServiceInstance(String uniqueId, ServiceInstance serviceInstance) {
        removeServiceInstance(uniqueId, serviceInstance.getServiceInstanceId());
        addServiceInstance(uniqueId, serviceInstance);
    }

    public void removeServiceInstance(String uniqueId, String serviceInstanceId) {
        final Set<ServiceInstance> serviceInstances = serviceInstanceMap.get(uniqueId);
        final Iterator<ServiceInstance> iterator = serviceInstances.iterator();
        while (iterator.hasNext()) {
            final ServiceInstance instance = iterator.next();
            if (instance.getServiceInstanceId().equalsIgnoreCase(serviceInstanceId)) {
                iterator.remove();
                break;
            }
        }
    }

    /**
     * remove 整个服务
     *
     * @param uniqueId
     */
    public void removeServiceInstancesByUniqueId(String uniqueId) {
        serviceInstanceMap.remove(uniqueId);
    }

    /************** 对规则缓存的操作方法 ***************/

    public void putRule(String ruleId, Rule rule) {
        ruleMap.put(ruleId, rule);
    }

    public Rule getRule(String ruleId) {
        return ruleMap.get(ruleId);
    }

    public void removeRule(String ruleId) {
        ruleMap.remove(ruleId);
    }
}
