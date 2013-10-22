package com.autoradio.push.test.client;

import javax.annotation.Resource;

import org.junit.Test;

import com.autoradio.push.service.PushService;
import com.autoradio.push.test.BaseTest;

public class PushClient extends BaseTest {

	@Resource(name = "pushClient", type = PushService.class)
	private PushService pushService;

	@Test
	public void push() {

		pushService.push();
	}

}
