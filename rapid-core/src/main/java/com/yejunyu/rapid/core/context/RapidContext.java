package com.yejunyu.rapid.core.context;

import com.google.common.base.Preconditions;
import com.yejunyu.rapid.common.config.Rule;
import com.yejunyu.rapid.common.utils.AssertUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

/**
 * Created by @author yejunyu on 2022/6/13
 *
 * @email : yyyejunyu@gmail.com
 */
public class RapidContext extends BaseContext {

    private final RapidRequest request;
    private RapidResponse response;

    private final Rule rule;

    public RapidContext(String protocol, ChannelHandlerContext nettyCtx, boolean keepAlive, RapidRequest rapidRequest, Rule rule) {
        super(protocol, nettyCtx, keepAlive);
        this.request = rapidRequest;
        this.rule = rule;
    }

    /**
     * rapid 上下文 builder
     */
    public static class ContextBuilder {
        private String protocol;
        private ChannelHandlerContext nettyCtx;
        private RapidRequest rapidRequest;
        private Rule rule;
        private boolean keepalive;

        public ContextBuilder() {
        }

        public ContextBuilder setProtocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public ContextBuilder setNettyCtx(ChannelHandlerContext nettyCtx) {
            this.nettyCtx = nettyCtx;
            return this;
        }

        public ContextBuilder setRapidRequest(RapidRequest rapidRequest) {
            this.rapidRequest = rapidRequest;
            return this;
        }

        public ContextBuilder setRule(Rule rule) {
            this.rule = rule;
            return this;
        }

        public ContextBuilder setKeepalive(boolean keepalive) {
            this.keepalive = keepalive;
            return this;
        }

        public RapidContext build() {
            Preconditions.checkNotNull(protocol, "protocol 不能为空");
            Preconditions.checkNotNull(nettyCtx, "nettyCtx 不能为空");
            Preconditions.checkNotNull(rapidRequest, "rapidRequest 不能为空");
            Preconditions.checkNotNull(rule, "rule 不能为空");
            return new RapidContext(protocol, nettyCtx, keepalive, rapidRequest, rule);
        }
    }

    /**
     * 获取必要的上下文参数, 没有则抛出异常
     *
     * @param key
     * @param <T>
     * @return
     */
    public <T> T getRequiredAttribute(AttributeKey<T> key) {
        final T value = getAttribute(key);
        AssertUtil.notNull(value, "required attribute '" + key + "' is missing !");
        return value;
    }

    /**
     * 获取上下文参数, 没有则用默认值替代
     *
     * @param key
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttributeOrDefault(AttributeKey<T> key, T defaultValue) {
        return (T) attributes.getOrDefault(key, defaultValue);
    }

    /**
     * 根据过滤器获取过滤器配置
     *
     * @param filterId
     * @return
     */
    public Rule.FilterConfig getFilterConfig(String filterId) {
        return rule.getFilterConfig(filterId);
    }

    /**
     * 获取上下文中唯一 uniqueId
     *
     * @return
     */
    public String getUniqueId() {
        return request.getUniqueId();
    }

    @Override
    public boolean releaseRequest() {
        if (super.releaseRequest()) {
            ReferenceCountUtil.release(request.getHttpRequest());
            return true;
        }
        return false;
    }

    @Override
    public Rule getRule() {
        return rule;
    }

    @Override
    public Object getRequest() {
        return request;
    }

    @Override
    public Object getResponse() {
        return response;
    }

    @Override
    public void setResponse(Object response) {
        this.response = (RapidResponse) response;
    }

    /**
     * 获取原始请求对象
     *
     * @return
     */
    public RapidRequest getOriginRequest() {
        return request;
    }

    /**
     * 获取可修改的请求对象
     *
     * @return
     */
    public RapidRequest getMutableRequest() {
        return request;
    }
}
