package com.yejunyu.rapid.common.config;

import lombok.Data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by @author yejunyu on 2022/6/13
 * 规则模型
 *
 * @email : yyyejunyu@gmail.com
 */
@Data
public class Rule implements Comparable<Rule>, Serializable {

    /**
     * 规则 id 全局唯一
     */
    private String id;

    private String name;

    private String protocol;

    private Integer order;
    /**
     * 规则集合定义
     */
    private Set<Rule.FilterConfig> filterConfigSet = new HashSet<>();

    /**
     * 向规则里添加指定的过滤器
     *
     * @param filterConfig
     * @return
     */
    public boolean addFilterConfig(FilterConfig filterConfig) {
        return filterConfigSet.add(filterConfig);
    }

    /**
     * 根据过滤器 id 获取 filterConfig
     *
     * @param filterId
     * @return
     */
    public FilterConfig getFilterConfig(String filterId) {
        for (FilterConfig filterConfig : filterConfigSet) {
            if (filterConfig.getId().equalsIgnoreCase(filterId)) {
                return filterConfig;
            }
        }
        return null;
    }

    /**
     * 根据 filterId 判断当前 Rule 是否存在
     *
     * @param filterId
     * @return
     */
    public boolean hasId(String filterId) {
        for (FilterConfig filterConfig : filterConfigSet) {
            if (filterConfig.getId().equalsIgnoreCase(filterId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int compareTo(Rule o) {
        final int compare = Integer.compare(getOrder(), o.getOrder());
        if (compare == 0) {
            return getId().compareTo(o.getId());
        }
        return compare;
    }

    /**
     * 过滤器的配置类
     */
    @Data
    public static class FilterConfig {
        /**
         * 过滤器唯一 id
         */
        private String id;
        /**
         * 过滤器配置描述信息: json string
         */
        private String config;

    }
}
