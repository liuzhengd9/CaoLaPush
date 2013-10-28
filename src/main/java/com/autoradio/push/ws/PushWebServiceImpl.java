package com.autoradio.push.ws;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import com.autoradio.push.service.IPushService;
import com.autoradio.push.pojo.Message;

@WebService(endpointInterface = "com.autoradio.push.ws.PushWebService")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class PushWebServiceImpl implements PushWebService {

	@Resource(name = "pushService", type = IPushService.class)
	private IPushService pushService;

	@Override
	public String push(Message message) {

		return pushService.push(message);
	}

	@Override
	public Message getMessage(String msgNo) {

		return pushService.getMessageByMsgNo(msgNo);
	}

}
