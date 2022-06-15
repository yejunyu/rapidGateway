package com.yejunyu.rapid.core.context;

import org.asynchttpclient.Request;
import org.asynchttpclient.cookie.Cookie;

/**
 * Created by @author yejunyu on 2022/6/13
 * 请求参数接口
 *
 * @email : yyyejunyu@gmail.com
 */
public interface RequestMutable {
    /**
     * 设置请求 host
     *
     * @param host
     */
    void setModifyHost(String host);

    /**
     * 获取 host
     *
     * @return host
     */
    String getModifyHost();

    /**
     * 设置请求路径
     *
     * @param path
     */
    void setModifyPath(String path);

    /**
     * 获取请求路径
     *
     * @return path
     */
    String getModifyPath();

    /**
     * 添加请求头信息
     *
     * @param name  头名字
     * @param value 请求头的值
     */
    void addHeader(CharSequence name, String value);

    /**
     * 设置请求头信息
     *
     * @param name  请求头名字
     * @param value 请求头的值
     */
    void setHeader(CharSequence name, String value);

    /**
     * 添加请求查询参数
     *
     * @param name  请求头名
     * @param value 请求头的值
     */
    void addQueryParam(String name, String value);

    /**
     * 添加或替换 cookie
     *
     * @param cookie cookie
     */
    void addOrReplaceCookie(Cookie cookie);

    /**
     * 添加 form 表单参数
     *
     * @param name
     * @param value
     */
    void addFormParam(String name, String value);

    /**
     * 设置请求超时时间(秒)
     *
     * @param timeout
     */
    void setRequestTimeout(int timeout);

    /**
     * 构建转发请求的请求对象
     *
     * @return Request
     */
    Request build();

    /**
     * 获取最终路由路径
     *
     * @return 路由路径
     */
    String getFinalUrl();
}
