package com.autoradio.push.service;

import java.math.BigDecimal;

import com.autoradio.push.pojo.Message;

public interface PushService {

	public String push(Message message);

	public void createPushRecord(Object msgNo);

	public void importMongoData2MySql(String msgNo, BigDecimal sendRate);

	public void dropTable(Object msgNo);

	public void updateMsgSendTimes(String msgNo, int times);

	public void updateMsgReceiveTimes(String msgNo, int times);

	public void updateMsgReceiveRate(String msgNo);

	public void updateMsgReadTimes(String msgNo, int times);

	public void updateMsgReadRate(String msgNo);

}
