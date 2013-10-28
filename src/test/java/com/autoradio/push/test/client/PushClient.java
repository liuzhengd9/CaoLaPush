package com.autoradio.push.test.client;

import java.math.BigDecimal;

import javax.annotation.Resource;

import org.junit.Test;
import org.springframework.util.Assert;

import com.autoradio.push.pojo.Message;
import com.autoradio.push.ws.PushWebService;
import com.autoradio.push.test.BaseTest;

public class PushClient extends BaseTest {

	@Resource(name = "pushClient", type = PushWebService.class)
	private PushWebService pushClient;

//	@Test
	public void testPush() {

		Message message = new Message();
		message.setMsgNo("1000");
		message.setMsgPlatform(2);
		message.setMsgTitle("推送测试ios");
		message.setMsgContent("共产党要灭亡了");
		message.setSendStartTime("09:00:00");
		message.setSendEndTime("21:00:00");
		message.setSendRate(new BigDecimal(1.00));
		System.out.println(pushClient.push(message));
	}

	 @Test
	public void testBatchPush() {

		for (int i = 1; i <= 100; i++) {
			Message message = new Message();
			message.setMsgNo(i + "");
			message.setMsgPlatform(2);
			message.setMsgTitle("推送测试ios");
			message.setMsgContent("共产党要灭亡了");
			message.setSendStartTime("09:00:00");
			message.setSendEndTime("21:00:00");
			message.setSendRate(new BigDecimal(1.00));
			System.out.println(pushClient.push(message));
		}
	}

	// @Test
	public void testGetResult() {

		Message message = pushClient.getMessage("1000");

		Assert.notNull(message);

		System.out.println(message.getMsgContent());
	}
}
