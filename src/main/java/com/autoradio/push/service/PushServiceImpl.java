package com.autoradio.push.service;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.apache.cxf.common.util.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jetty.util.ajax.JSON;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;

@WebService(endpointInterface = "com.autoradio.push.service.PushService")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class PushServiceImpl implements PushService {

	private final Logger logger = Logger.getLogger(getClass());

	@Resource(name = "jobLauncher", type = JobLauncher.class)
	private JobLauncher jobLauncher;

	@Resource(name = "caoLaPushJob", type = Job.class)
	private Job caoLaPushJob;

	@Resource(name = "jdbcTemplate", type = JdbcTemplate.class)
	private JdbcTemplate jdbcTemplate;

	/**
	 * 0代表失败 1代表成功
	 */
	@Override
	public String push(final Message message) {

		// 检查参数
		if (message == null) {
			return JSON.toString(createResult(0, "参数不能为空"));
		}
		if (StringUtils.isEmpty(message.getMsgNo())) {
			return JSON.toString(createResult(0, "消息唯一编号不能为空"));
		}
		if (message.getMsgPlatform() != 0 && message.getMsgPlatform() != 1 && message.getMsgPlatform() != 2) {
			return JSON.toString(createResult(0, "消息推送平台只能为0、1、2中的一个,0代表ios,1代表android,2代表全部"));
		}
		if (StringUtils.isEmpty(message.getMsgTitle())) {
			return JSON.toString(createResult(0, "消息标题不能为空"));
		}
		if (StringUtils.isEmpty(message.getMsgContent())) {
			return JSON.toString(createResult(0, "消息内容不能为空"));
		}
		if (message.getSendState() != 0) {
			return JSON.toString(createResult(0, "消息推送发送状态只能为0,代表未发送"));
		}
		if (message.getSendOvertimeRule() != 0 && message.getSendOvertimeRule() != 1 && message.getSendOvertimeRule() != 2) {
			return JSON.toString(createResult(0, "消息发送超过发送时间段后采取的策略只能为0、1、2中的一个,0代表放弃,1代表等待,2代表强制发送"));
		}
		// 向数据库中添加待发送记录
		jdbcTemplate.update("insert into push_message(msg_no,msg_platform,msg_title,msg_content,send_state,send_start_time,send_end_time,send_overtime_rule) value(?,?,?,?,?,?,?,?)",
				new PreparedStatementSetter() {

					@Override
					public void setValues(PreparedStatement ps) throws SQLException {

						ps.setString(1, message.getMsgNo());
						ps.setInt(2, message.getMsgPlatform());
						ps.setString(3, message.getMsgTitle());
						ps.setString(4, message.getMsgContent());
						ps.setInt(5, 0);
						ps.setString(6, message.getSendStartTime());
						ps.setString(7, message.getSendEndTime());
						ps.setInt(8, message.getSendOvertimeRule());
					}
				});

		try {
			jobLauncher.run(caoLaPushJob, new JobParametersBuilder().addString("msgNo", message.getMsgNo()).toJobParameters());
			return JSON.toString(createResult(1, "成功"));
		} catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException | JobParametersInvalidException e) {
			logger.error(e.getMessage(), e);
			return JSON.toString(createResult(2, e.getMessage()));
		}
	}

	/**
	 * @param status
	 *            状态参数,0代表参数问题,1代表成果,2代表异常
	 * @param message
	 * @return
	 */
	private Map<String, Object> createResult(int status, String message) {

		Map<String, Object> result = new HashMap<String, Object>();
		result.put("status", status);
		result.put("message", message);
		return result;
	}
}
