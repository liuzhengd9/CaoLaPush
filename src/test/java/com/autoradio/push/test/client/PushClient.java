package com.autoradio.push.test.client;

import javax.annotation.Resource;

import org.junit.Test;

import com.autoradio.push.service.Message;
import com.autoradio.push.service.PushService;
import com.autoradio.push.test.BaseTest;

public class PushClient extends BaseTest {

	@Resource(name = "pushClient", type = PushService.class)
	private PushService pushService;

	@Test
	public void testPush() {

		Message message = new Message();
		message.setMsgNo("1001");
		message.setMsgPlatform(2);
		message.setMsgTitle("推送测试");
		message.setMsgContent("今年国家颁布下发了关于农村小额贷款的政策的整改通知");
		System.out.println(pushService.push(message));
	}

}
