package com.autoradio.push.ws;

import javax.jws.WebService;

import com.autoradio.push.pojo.Message;

@WebService
public interface PushWebService {

	public String push(final Message message);

	public Message getMessage(String msgNo);
}
