package com.eveb.saasops_msgsender.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * spring context 的配置文件
 * <p>
 * Created by Green Lei on 2015/10/20 10:07.
 */
@Configuration
@EnableWebMvc
@ImportResource({"classpath*:/SaasopsApplicationContext.xml"})
public class AppConfig {
}
