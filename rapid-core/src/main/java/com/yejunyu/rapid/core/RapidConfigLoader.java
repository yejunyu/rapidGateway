package com.yejunyu.rapid.core;

import com.yejunyu.rapid.common.utils.PropertiesUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * @author : YeJunyu
 * @description : 网关配置加载类
 * 网关配置加载规则: 高优先级覆盖低优先级
 * 运行参数 -> jvm 参数 -> 环境变量 -> 配置文件 -> 内部 rapidConfig 对象默认值
 * @email : yyyejunyu@gmail.com
 * @date : 2022/5/9
 */
@Slf4j
public class RapidConfigLoader {

    private final static String CONFIG_FILE = "rapid.properties";

    private final static String CONFIG_ENV_PREFIX = "rapid_";
    private final static String CONFIG_JVM_PREFIX = "rapid.";

    // 单例
    private final static RapidConfigLoader INSTANCE = new RapidConfigLoader();

    private final RapidConfig rapidConfig = new RapidConfig();

    private RapidConfigLoader() {
    }

    public static RapidConfigLoader getInstance() {
        return INSTANCE;
    }

    public static RapidConfig getRapidConfig() {
        return INSTANCE.rapidConfig;
    }

    // load 返回 rapidConfig
    public RapidConfig load(String[] args) {
        // 加载逻辑
        // 1. 配置文件
        {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(CONFIG_FILE);
            if (is != null) {
                Properties properties = new Properties();
                try {
                    properties.load(is);
                    PropertiesUtils.properties2Object(properties, rapidConfig);
                } catch (IOException e) {
                    log.error("RapidConfigLoader#load is error", e);
                } finally {
                    try {
                        is.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }
        }
        // 2. 环境变量
        {
            Map<String, String> env = System.getenv();
            Properties properties = new Properties();
            properties.putAll(env);
            PropertiesUtils.properties2Object(properties, rapidConfig, CONFIG_ENV_PREFIX);
        }
        // 3. jvm 参数
        {
            Properties properties = System.getProperties();
            PropertiesUtils.properties2Object(properties, rapidConfig, CONFIG_JVM_PREFIX);
        }
        // 4. 运行参数 --xxx=xxx --port=1234
        {
            if (args != null && args.length > 0) {
                Properties properties = new Properties();
                for (String arg : args) {
                    if (arg.startsWith("--") && arg.contains("=")) {
                        properties.put(arg.substring(2, arg.indexOf("=")), arg.substring(arg.indexOf("=") + 1));
                    }
                }
                PropertiesUtils.properties2Object(properties, rapidConfig);
            }
        }
        return rapidConfig;
    }
}
