package com.autoradio.push.service;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService(endpointInterface = "com.autoradio.push.service.PushService")
@SOAPBinding(style=SOAPBinding.Style.RPC)
public class PushServiceImpl implements PushService {

	@Override
	public int push() {

		System.out.println("调用成功");
		return 0;
	}

}
