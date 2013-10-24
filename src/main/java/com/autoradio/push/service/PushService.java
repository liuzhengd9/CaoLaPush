package com.autoradio.push.service;

import com.autoradio.push.pojo.Message;

public interface PushService {

	public String push(final Message message);

	public void createPushRecord(Object msgNo);

	public void importMongoData2MySql(Object msgNo);

	public void dropTable(Object msgNo);
}
