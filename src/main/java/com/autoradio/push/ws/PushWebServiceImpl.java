package com.autoradio.push.ws;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import com.autoradio.push.service.PushService;
import com.autoradio.push.pojo.Message;

@WebService(endpointInterface = "com.autoradio.push.ws.PushWebService")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class PushWebServiceImpl implements PushWebService {

	@Resource(name = "pushService", type = PushService.class)
	private PushService pushService;

	@Override
	public String push(Message message) {

		return pushService.push(message);
	}

}
