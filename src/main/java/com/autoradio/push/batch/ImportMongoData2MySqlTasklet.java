package com.autoradio.push.batch;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Component(value="importMongoData2MySqlTasklet")
public class ImportMongoData2MySqlTasklet implements Tasklet {

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		System.out.println(chunkContext.getStepContext().getJobParameters().get("msgNo"));
		System.out.println("CopyDataTasklet execute...");
		return RepeatStatus.FINISHED;
	}

}