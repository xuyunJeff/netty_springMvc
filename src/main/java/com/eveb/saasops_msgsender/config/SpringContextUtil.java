package com.eveb.saasops_msgsender.config;


import lombok.Data;
import lombok.Getter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;


/**
 * @author William
 * @createTime 18-5-31:下午11:15
 * @Description: 获取spring所有的uri(用一句话描述该文件做什么)
 */
@Component
public class SpringContextUtil implements ApplicationContextAware,ServletContextAware {

    private static ApplicationContext applicationContext;

    private static ServletContext servletContext;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextUtil.applicationContext =applicationContext;
    }

    public static Object getBean(String beanName){
        return applicationContext.getBean(beanName);
    }


    @Override
    public void setServletContext(ServletContext servletContext) {
        SpringContextUtil.servletContext =servletContext;
    }

    public static ServletContext getServletContext(){
        return servletContext;
    }
}
