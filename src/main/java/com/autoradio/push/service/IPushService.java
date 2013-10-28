package com.autoradio.push.service;

import java.math.BigDecimal;
import java.util.List;

import com.autoradio.push.pojo.Message;
import com.autoradio.push.pojo.PushRecord;

public interface IPushService {

	public String push(Message message);

	public void createPushRecord(Object msgNo);

	public void importMongoData2MySql(String msgNo, BigDecimal sendRate);

	public void send(List<? extends PushRecord> items, String msgNo);

	public void dropTable(String msgNo);

	public void updateMsgSendTimes(String msgNo, int times);

	public void updateMsgReceiveTimes(String msgNo, int times);

	public void updateMsgReceiveRate(String msgNo);

	public void updateMsgReadTimes(String msgNo, int times);

	public void updateMsgReadRate(String msgNo);

	public Message getMessageByMsgNo(String msgNo);

}
