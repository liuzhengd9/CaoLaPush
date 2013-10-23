package com.autoradio.push.batch;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

public class SaveMessageTasklet implements Tasklet {

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

		System.out.println(chunkContext.getStepContext().getJobParameters().get("msgNo"));
		chunkContext.getStepContext().getJobParameters().put("name", "刘正");
		System.out.println("SaveMessageTasklet execute...");
		return RepeatStatus.FINISHED;
	}

}
