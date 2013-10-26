package com.autoradio.push.batch;

import java.math.BigDecimal;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import com.autoradio.push.service.IPushService;

@Component(value = "importMongoData2MySqlTasklet")
public class ImportMongoData2MySqlTasklet implements Tasklet {

	private final Logger logger = Logger.getLogger(getClass());

	@Resource(name = "pushService", type = IPushService.class)
	private IPushService pushService;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

		logger.info("ImportMongoData2MySqlTasklet.execute...");
		pushService.importMongoData2MySql((String)chunkContext.getStepContext().getJobParameters().get("msgNo"),new BigDecimal((String)chunkContext.getStepContext().getJobParameters().get("sendRate")));
		return RepeatStatus.FINISHED;
	}

}