package com.autoradio.push.batch;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import com.autoradio.push.service.IPushService;

@Component(value = "createPushRecordTasklet")
public class CreatePushRecordTasklet implements Tasklet {

	private final Logger logger = Logger.getLogger(getClass());

	@Resource(name = "pushService", type = IPushService.class)
	private IPushService pushService;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

		logger.info("CreatePushRecordTasklet.execute...");
		pushService.createPushRecord(chunkContext.getStepContext().getJobParameters().get("msgNo"));
		return RepeatStatus.FINISHED;
	}
}
