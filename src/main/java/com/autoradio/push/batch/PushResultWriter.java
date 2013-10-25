package com.autoradio.push.batch;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.autoradio.push.service.PushService;
import com.autoradio.push.pojo.PushRecord;

@Component(value = "pushResultWriter")
@Scope(value = "step")
public class PushResultWriter implements ItemWriter<PushRecord> {

	private final Logger logger = Logger.getLogger(getClass());

	@Resource(name = "pushService", type = PushService.class)
	private PushService pushService;

	@Value(value = "${sendThread.suspend.second}")
	private int suspendTime = 1;

	private String msgNo;

	private String sendOvertimeRule = "1";

	private String sendStartTime;

	private String sendEndTime;

	private String msgPlatform;

	@BeforeStep
	public void setMsgNo(StepExecution stepExecution) {

		this.msgNo = stepExecution.getJobParameters().getString("msgNo");
		this.sendOvertimeRule = stepExecution.getJobParameters().getString("sendOvertimeRule");
		this.sendStartTime = stepExecution.getJobParameters().getString("sendStartTime");
		this.sendEndTime = stepExecution.getJobParameters().getString("sendEndTime");
		this.msgPlatform = stepExecution.getJobParameters().getString("msgPlatform");
		logger.info("msgNo:" + msgNo);
		logger.info("sendStartTime:" + sendStartTime);
		logger.info("sendEndTime:" + sendEndTime);
		logger.info("msgPlatform:" + msgPlatform);
	}

	@Override
	public void write(List<? extends PushRecord> items) throws Exception {

		logger.info("PushResultWriter.write run...");
		Time now = Time.valueOf(new SimpleDateFormat("HH:mm:ss").format(new Date()));
		while (now.before(Time.valueOf(sendStartTime)) || now.after(Time.valueOf(sendEndTime))) {
			logger.info("发送时间不在允许发送时间段范围");
			if ("0".equals(sendOvertimeRule)) {
				return;
			} else if ("1".equals(sendOvertimeRule)) {
				TimeUnit.MINUTES.sleep(suspendTime);
			} else if ("2".equals(sendOvertimeRule)) {
				break;
			}
		}
		// 更新表中该条信息的待发送记录
		pushService.updateMsgSendTimes(msgNo, items.size());
		// 调用发送端接口,需要区分ios和android接口
		switch (msgPlatform) {
		case "0":
			// 只发送ios
			break;
		case "1":
			// 只发送android
			break;
		case "2":
			// ios和android都发送
			break;
		default:
			// 默认都发送
		}
		// 发送完成
		pushService.updateMsgReceiveTimes(msgNo, items.size());
	}
}
