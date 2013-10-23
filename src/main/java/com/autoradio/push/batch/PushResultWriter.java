package com.autoradio.push.batch;

import java.util.List;

import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import com.autoradio.push.service.PushRecord;

@Component(value = "pushResultWriter")
public class PushResultWriter implements ItemWriter<PushRecord> {

	@Override
	public void write(List<? extends PushRecord> items) throws Exception {

	}
}
