package com.autoradio.push.test.batch;

import javax.annotation.Resource;

import org.junit.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;

import com.autoradio.push.test.BaseTest;

public class BatchTest extends BaseTest {

	@Resource(name = "jobLauncher", type = JobLauncher.class)
	private JobLauncher jobLauncher;

	@Resource(name = "caoLaPushJob", type = Job.class)
	private Job caoLaPushJob;

	@Test
	public void push() {

		try {
			jobLauncher.run(caoLaPushJob, new JobParametersBuilder().addString("no", "1111111").toJobParameters());
		} catch (JobExecutionAlreadyRunningException e) {
			e.printStackTrace();
		} catch (JobRestartException e) {
			e.printStackTrace();
		} catch (JobInstanceAlreadyCompleteException e) {
			e.printStackTrace();
		} catch (JobParametersInvalidException e) {
			e.printStackTrace();
		}
	}

}
