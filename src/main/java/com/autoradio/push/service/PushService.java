package com.autoradio.push.service;

import javax.jws.WebService;

import com.autoradio.push.service.pojo.Message;

@WebService
public interface PushService {

	public String push(final Message message);
}
