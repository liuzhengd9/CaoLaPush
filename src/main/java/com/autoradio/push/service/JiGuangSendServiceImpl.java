package com.autoradio.push.service;

import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import cn.jpush.api.DeviceEnum;
import cn.jpush.api.ErrorCodeEnum;
import cn.jpush.api.JPushClient;
import cn.jpush.api.MessageResult;

import com.autoradio.push.pojo.Message;
import com.autoradio.push.pojo.PushRecord;

@Service(value = "jiguangSendService")
public class JiGuangSendServiceImpl implements ISendService {

	private final Logger logger = Logger.getLogger(getClass());

	@Value(value = "${jpush.masterSecret}")
	private String masterSecret;

	@Value(value = "${jpush.message.timeToLive}")
	private int timeToLive;

	@Resource(name = "jdbcTemplate", type = JdbcTemplate.class)
	private JdbcTemplate jdbcTemplate;

	@Override
	public int send(Message message, PushRecord record, int platform) {

		MessageResult msgResult = null;
		try {
			switch (platform) {
			case 0:
				msgResult = new JPushClient(masterSecret, record.getUdid(), timeToLive, DeviceEnum.Android).sendCustomMessageWithAppKey(record.getId(), message.getMsgTitle(), message.getMsgContent());
				break;
			case 1:
				msgResult = new JPushClient(masterSecret, record.getUdid(), timeToLive, DeviceEnum.IOS).sendCustomMessageWithAppKey(record.getId(), message.getMsgTitle(), message.getMsgContent());
				break;
			default:
				msgResult = new JPushClient(masterSecret, record.getUdid(), timeToLive).sendCustomMessageWithAppKey(record.getId(), message.getMsgTitle(), message.getMsgContent());
			}
			if (msgResult != null) {
				if (msgResult.getErrcode() == ErrorCodeEnum.NOERROR.value()) {
					return 1;
				} else {
					jdbcTemplate.update("insert into push_log(msg_no,push_record_table,send_no,app_key,error_code,error_msg,send_time) values(?,?,?,?,?,?,sysdate())", message.getMsgNo(),
							"push_record_" + message.getMsgNo(), record.getId(), record.getUdid(), msgResult.getErrcode(), msgResult.getErrmsg());
					return 0;
				}
			} else {
				return 0;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			logger.info("*******************************************************************************");
			jdbcTemplate.update("insert into push_log(msg_no,push_record_table,send_no,app_key,error_code,error_msg,send_time) values(?,?,?,?,?,?,sysdate())", message.getMsgNo(), "push_record_"
					+ message.getMsgNo(), record.getId(), record.getUdid(), msgResult.getErrcode(), msgResult.getErrmsg());
			return 0;
		}
	}

	@Override
	public int batchSend(Message message, List<? extends PushRecord> records, int platform) {

		int sentSize = 0;
		for (PushRecord record : records) {
			sentSize += send(message, record, platform);
		}
		return sentSize;
	}

}