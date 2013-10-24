package com.autoradio.push.test.client;

import javax.annotation.Resource;

import org.junit.Test;

import com.autoradio.push.pojo.Message;
import com.autoradio.push.ws.PushWebService;
import com.autoradio.push.test.BaseTest;

public class PushClient extends BaseTest {

	@Resource(name = "pushClient", type = PushWebService.class)
	private PushWebService pushClient;

	@Test
	public void testPush() {

		Message message = new Message();
		message.setMsgNo("1001");
		message.setMsgPlatform(1);
		message.setMsgTitle("推送测试ios");
		message.setMsgContent("共产党要灭亡了");
		System.out.println(pushClient.push(message));
	}

}
