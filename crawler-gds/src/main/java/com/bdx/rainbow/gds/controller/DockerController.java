package com.bdx.rainbow.gds.controller;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class DockerController {
	
    public final Logger logger = LoggerFactory.getLogger(DockerController.class);

    @RequestMapping(value = {"/rest"}, produces = "text/html;charset=UTF-8")
    @ResponseBody
    public String getJsonRsp(String msg, HttpServletRequest request) throws Exception {

    	logger.info("============================================================");
    	logger.info("request msg = "+msg);
    	logger.info("============================================================");
    	
        return "request msg = ["+msg+"]";
    }

}
