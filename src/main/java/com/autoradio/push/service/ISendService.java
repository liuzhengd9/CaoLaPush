package com.autoradio.push.service;

import java.util.List;

import com.autoradio.push.pojo.Message;
import com.autoradio.push.pojo.PushRecord;

public interface ISendService {

	/**
	 * @param record
	 *            发送记录
	 * @param platform
	 *            0为ios,1为android,2为全都发送
	 * @return 返回发送成功条数
	 */
	public int send(Message message, PushRecord record, int platform);

	/**
	 * @param records
	 *            发送记录
	 * @param platform
	 *            0为ios,1为android,2为全都发送
	 * @return 返回发送成功条数
	 */
	public int batchSend(Message message, List<? extends PushRecord> records, int platform);
}
