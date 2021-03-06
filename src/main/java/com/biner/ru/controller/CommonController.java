package com.biner.ru.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.biner.ru.common.MapResult;
import com.biner.ru.common.ParamThreadLocal;
import com.biner.ru.util.CodeMsg;
import com.biner.ru.util.IpUtils;
import com.biner.ru.util.ResponseUtil;
import com.biner.ru.service.CommonService;

@Controller
@RequestMapping("/common")
public class CommonController {

	@Autowired
	private CommonService commonService;

	private Logger logger = Logger.getLogger(CommonController.class);

	@RequestMapping("/token/getHomeInfo")
	public void getIpLocation(HttpServletRequest request, HttpServletResponse response) {
		MapResult result = null;
		String ip = IpUtils.getIpAddr(request);
		if(StringUtils.isEmpty(ip)) {
			result = new MapResult(CodeMsg.SERVER_EXCEPTION, "获取IP地址异常");
		} else {
			result = commonService.getHomeInfo(ip);
		}
		ResponseUtil.outputJSONResponseEncrypt(request, response, result);
	}

	@RequestMapping("/recordScore")
	public void recordScore(HttpServletRequest request, HttpServletResponse response) {
		MapResult result = null;
		try {
			Map<String, String> params = ParamThreadLocal.get();
			String score = params.get("score");
			String userId = params.get("userId");
			result = commonService.recordScore(score, userId);
		} catch (Exception e) {
			logger.error("Record score has error:", e);
			result = new MapResult(CodeMsg.SERVER_EXCEPTION, "请求异常");
		}
		ResponseUtil.outputJSONResponseEncrypt(request, response, result);
	}
}
