package com.autoradio.push.service;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

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
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.stereotype.Service;

import com.autoradio.push.pojo.Message;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;

@Service(value = "pushService")
public class PushServiceImpl implements PushService {

	private final Logger logger = Logger.getLogger(getClass());

	@Resource(name = "jobLauncher", type = JobLauncher.class)
	private JobLauncher jobLauncher;

	@Resource(name = "caoLaPushJob", type = Job.class)
	private Job caoLaPushJob;

	@Resource(name = "jdbcTemplate", type = JdbcTemplate.class)
	private JdbcTemplate jdbcTemplate;

	@Resource(name = "mongoTemplate", type = MongoTemplate.class)
	private MongoTemplate mongoTemplate;

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
		jdbcTemplate
				.update("insert into push_message(msg_no,msg_platform,msg_title,msg_content,send_state,send_start_time,send_end_time,send_overtime_rule) values(?,?,?,?,0,?,?,?) on duplicate key update msg_platform=?,msg_title=?,msg_content=?,send_state=0,send_start_time=?,send_end_time=?,send_overtime_rule=?",
						new PreparedStatementSetter() {

							@Override
							public void setValues(PreparedStatement ps) throws SQLException {

								ps.setString(1, message.getMsgNo());
								ps.setInt(2, message.getMsgPlatform());
								ps.setString(3, message.getMsgTitle());
								ps.setString(4, message.getMsgContent());
								ps.setString(5, message.getSendStartTime());
								ps.setString(6, message.getSendEndTime());
								ps.setInt(7, message.getSendOvertimeRule());
								// 出现重复key的时候
								ps.setInt(8, message.getMsgPlatform());
								ps.setString(9, message.getMsgTitle());
								ps.setString(10, message.getMsgContent());
								ps.setString(11, message.getSendStartTime());
								ps.setString(12, message.getSendEndTime());
								ps.setInt(13, message.getSendOvertimeRule());
							}
						});

		try {
			jobLauncher.run(caoLaPushJob,
					new JobParametersBuilder().addString("msgNo", message.getMsgNo()).addString("sendStartTime", message.getSendStartTime()).addString("sendEndTime", message.getSendEndTime())
							.addString("sendOvertimeRule", String.valueOf(message.getSendOvertimeRule())).toJobParameters());
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

	@Override
	public void createPushRecord(Object msgNo) {

		String[] sqls = new String[] { MessageFormat.format("drop table if exists push_record_{0}", msgNo),
				MessageFormat.format("create table push_record_{0}(id int not null auto_increment,msg_no varchar(10) not null,receive_info_table_name varchar(20),primary key (id));", msgNo) };
		jdbcTemplate.batchUpdate(sqls);
	}

	@Override
	public void importMongoData2MySql(Object msgNo) {

		List<Object[]> params = new ArrayList<Object[]>();
		for (int i = 0; i < 100; i++) {
			params.add(new Object[] { msgNo });
		}
		DBCollection coll = mongoTemplate.getCollection("");
		DBCursor cursor = coll.find();
		while (cursor.hasNext()) {
			// 操作cursor

			if (params.size() == 100000) {
				jdbcTemplate.batchUpdate(MessageFormat.format("insert into push_record_{0}(msg_no) values(?)", msgNo), params);
				params.clear();
			}
		}
		if (params.size() > 0) {
			jdbcTemplate.batchUpdate(MessageFormat.format("insert into push_record_{0}(msg_no) values(?)", msgNo), params);
		}

	}

	@Override
	public void dropTable(Object msgNo) {

		logger.info("drop table push_record_" + msgNo);
		jdbcTemplate.update(MessageFormat.format("drop table push_record_{0}", msgNo));
	}
}
