package com.autoradio.push.batch;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.autoradio.push.service.pojo.PushRecord;

@Component(value = "pushMessageProcessor")
public class PushMessageProcessor implements ItemProcessor<PushRecord, PushRecord> {

	@Override
	public PushRecord process(PushRecord item) throws Exception {

		System.out.println(item.getMsgNo());
		return item;
	}

}
