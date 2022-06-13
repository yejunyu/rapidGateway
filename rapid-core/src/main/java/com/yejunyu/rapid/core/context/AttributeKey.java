package com.yejunyu.rapid.core.context;

import java.util.Set;

/**
 * @author : YeJunyu
 * @description : context属性 key
 * @email : yyyejunyu@gmail.com
 * @date : 2022/6/5
 */
public abstract class AttributeKey<T> {
    /**
     * 到负载均衡之前,通过具体的服务,获取对应的服务实例列表
     */
    public static final AttributeKey<Set<String>> MATCH_ADDRESS = create(Set.class);

    /**
     * 对象转成对应的 class 类
     *
     * @param value
     * @return
     */
    public abstract T cast(Object value);

    /**
     * 创建一个 simpleAttributeKey
     *
     * @param valueClass
     * @param <T>
     * @return
     */
    public static <T> AttributeKey<T> create(final Class<? super T> valueClass) {
        return new SimpleAttributeKey(valueClass);
    }

    public static class SimpleAttributeKey<T> extends AttributeKey<T> {

        private final Class<T> valueClass;

        public SimpleAttributeKey(final Class<T> valueClass) {
            this.valueClass = valueClass;
        }

        @Override
        public T cast(Object value) {
            return valueClass.cast(value);
        }

        @Override
        public String toString() {
            if (valueClass != null) {
                return getClass().getName() + "<" +
                        valueClass.getName() +
                        ">";
            }
            return super.toString();
        }
    }
}
