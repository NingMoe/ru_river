package com.biner.ru.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.biner.ru.common.Constants;
import com.biner.ru.common.MapResult;
import com.biner.ru.mapper.ScoreMapper;
import com.biner.ru.model.Score;
import com.biner.ru.util.AESUtils;
import com.biner.ru.util.CodeMsg;
import com.biner.ru.util.HttpUtils;
import com.biner.ru.webSocket.SpringWebSocketHandler;

@Service
public class CommonService {

	private Logger logger = Logger.getLogger(this.getClass());
	// 将排名缓存在内存中，避免每次查询数据库
	private static Vector<Score> scores = new Vector<Score>();
	
	private final static int SCORE_PAGE_SIZE = 5;
	
	@Autowired
	private ScoreMapper scoreMapper;
	
	@Bean//这个注解会从Spring容器拿出Bean
    public SpringWebSocketHandler webSocketHandler() {
        return new SpringWebSocketHandler();
    }
	
	public MapResult getHomeInfo(String ip) {
		MapResult result = null;
		String ipUrl = Constants.FREEGEOIP_URL + ip;
		String location = "亲爱";
		Map<String, Object> data = new HashMap<String, Object>();
		try {
//			String resultStr = HttpUtils.sendGet(ipUrl);
//			JSONObject resultJson = JSONObject.parseObject(resultStr);
//			if ("CN".equals(resultJson.getString("country_code"))) {
//				String priCode = resultJson.getString("region_code");
//				location = Constants.PROVINCE_CODE.get(priCode);
//			}
		} catch (Exception e) {
			logger.error("Request Freegeoip has error:::", e);
			result = new MapResult(CodeMsg.SERVER_EXCEPTION);
		}
		if (scores.size() == 0) {
			Score sc = new Score();
			sc.setType("1");
			sc.setPageNo(0);
			sc.setPageSize(SCORE_PAGE_SIZE);
			try {
				scores = scoreMapper.getAllRank(sc);
			} catch (Exception e) {
				logger.error("Get all ranks has error:::", e);
			}
		}
		result = new MapResult();
		data.put("province", location);
		data.put("scores", scores);
		result.setData(data);
		return result;
	}

	public String getVerifyMailContent(String mail, String username) throws Exception {
		String token = AESUtils.encrypt(mail + "_" + System.currentTimeMillis(), Constants.SECRET_KEY);
		String url = Constants.HOST_SERVER_BASE_URL + "#/authMail/" + token;
		return String.format(Constants.VERIFY_MAIL_CONTENT, username, mail, url);
	}
	
	public MapResult recordScore(String score, String userId) {
		int scInt = Integer.parseInt(score);
		Score sc = new Score();
		sc.setUserId(Long.parseLong(userId));
		sc.setScore(scInt);
		sc.setType("1");
		scoreMapper.save(sc);
		if (scInt > scores.get(scores.size() - 1).getScore()) {// 推送给前端刷新
			//TODO 直接操作vector，不从数据库拿
			sc.setPageNo(0);
			sc.setPageSize(SCORE_PAGE_SIZE);
			scores = scoreMapper.getAllRank(sc);
			webSocketHandler().sendMessageToAll(JSONObject.toJSONString(scores));
		}
		return new MapResult();
	}
}
