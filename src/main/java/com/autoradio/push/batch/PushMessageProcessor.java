package com.autoradio.push.batch;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.autoradio.push.service.PushService;
import com.autoradio.push.pojo.PushRecord;

@Component(value = "pushMessageProcessor")
public class PushMessageProcessor implements ItemProcessor<PushRecord, PushRecord> {

	private final Logger logger = Logger.getLogger(getClass());

	@Resource(name = "pushService", type = PushService.class)
	private PushService pushService;

	@Override
	public PushRecord process(PushRecord item) throws Exception {

		logger.info("PushMessageProcessor.process run...");
		logger.info("msgNo:" + item.getMsgNo() + ",udid:" + item.getUdid());
		return item;
	}

}
