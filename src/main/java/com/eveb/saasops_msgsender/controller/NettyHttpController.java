package com.eveb.saasops_msgsender.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author William
 * @createTime 18-5-31:下午9:10
 * @Description: netty结合spring Http(用一句话描述该文件做什么)
 */
@RequestMapping("/http/spring")
@Controller
public class NettyHttpController {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value="/test", produces = "text/html; charset=utf-8")
    @ResponseBody
    public String test(){
        return "spring集成完成";
    }
}
