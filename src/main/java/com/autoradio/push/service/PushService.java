package com.autoradio.push.service;

import javax.jws.WebService;

@WebService
public interface PushService {

	public String push(final Message message);
}
