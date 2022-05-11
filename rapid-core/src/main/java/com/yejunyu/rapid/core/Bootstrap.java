package com.yejunyu.rapid.core;

/**
 * @author : YeJunyu
 * @description : 网关主入口
 * @email : yyyejunyu@gmail.com
 * @date : 2021/12/29
 */
public class Bootstrap {
    public static void main(String[] args) {
        // 1. 加载网关配置信息
        RapidConfig rapidConfig = RapidConfigLoader.getInstance().load(args);
        // 2. 插件初始化

        // 3. 初始化服务注册管理中心, 监听动态配置的变更

        // 4. 容器启动
    }
}
