package com.autoradio.push.batch;

import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import com.autoradio.push.service.IPushService;
import com.autoradio.push.pojo.PushRecord;

@Component(value = "pushResultWriter")
public class PushResultWriter implements ItemWriter<PushRecord> {

	private final Logger logger = Logger.getLogger(getClass());

	@Resource(name = "pushService", type = IPushService.class)
	private IPushService pushService;

	@Override
	public void write(List<? extends PushRecord> items) throws Exception {

		logger.info("PushResultWriter.write run...");
		pushService.send(items, items.get(0).getMsgNo());
	}
}
