package com.autoradio.push.service;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import com.autoradio.push.pojo.Message;
import com.autoradio.push.pojo.PushRecord;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

@Service(value = "pushService")
public class PushServiceImpl implements IPushService {

	private final Logger logger = Logger.getLogger(getClass());

	@Resource(name = "jobLauncher", type = JobLauncher.class)
	private JobLauncher jobLauncher;

	@Resource(name = "kaoLaPushJob", type = Job.class)
	private Job kaoLaPushJob;

	@Resource(name = "jdbcTemplate", type = JdbcTemplate.class)
	private JdbcTemplate jdbcTemplate;

	@Resource(name = "mongoTemplate", type = MongoTemplate.class)
	private MongoTemplate mongoTemplate;

	@Resource(name = "jiguangSendService2", type = ISendService.class)
	private ISendService sendService;

	@Value(value = "${execute.pushService.threadPool.size}")
	private int poolSize = 10;

	@Value(value = "${mongo2mysql.batch.records.size}")
	private int batchIntoMySqlSize = 100000;

	@Value(value = "${sendThread.suspend.second}")
	private int suspendTime = 1;

	private Executor executor;

	@PostConstruct
	public void initThreadPool() {

		logger.info("execute.pushService.threadPool.size:" + this.poolSize);
		executor = Executors.newFixedThreadPool(this.poolSize);
	}

	/**
	 * 0代表失败 1代表成功
	 */
	@Override
	public String push(Message message) {

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
		if (message.getSendOvertimeRule() == 1 && Time.valueOf(message.getSendStartTime()).after(Time.valueOf(message.getSendEndTime()))) {
			return JSON.toString(createResult(0, "消息发送开始时间必须在截止时间之后"));
		}
		if (message.getSendRate().doubleValue() <= 0D || message.getSendRate().doubleValue() > 1D) {
			return JSON.toString(createResult(0, "消息发送比例必须大于0且小于等于1"));
		}
		// 保存消息
		saveMessage(message);
		// 启动发送线程
		startPushJob(message);
		return JSON.toString(createResult(1, "任务发送成功"));
	}

	private void saveMessage(final Message message) {

		// 向数据库中添加待发送记录
		jdbcTemplate
				.update("insert into push_message(msg_no,msg_platform,msg_title,msg_content,send_state,send_start_time,send_end_time,send_overtime_rule,send_rate) values(?,?,?,?,0,?,?,?,?) on duplicate key update msg_platform=?,msg_title=?,msg_content=?,msg_send_times=0,msg_receive_times=0,msg_receive_rate=0,msg_read_times=0,msg_read_rate=0,send_state=0,send_start_time=?,send_end_time=?,send_overtime_rule=?,send_rate=?",
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
								ps.setBigDecimal(8, message.getSendRate());
								// 出现重复key的时候
								ps.setInt(9, message.getMsgPlatform());
								ps.setString(10, message.getMsgTitle());
								ps.setString(11, message.getMsgContent());
								ps.setString(12, message.getSendStartTime());
								ps.setString(13, message.getSendEndTime());
								ps.setInt(14, message.getSendOvertimeRule());
								ps.setBigDecimal(15, message.getSendRate());
							}
						});
	}

	public void startPushJob(final Message message) {

		executor.execute(new Thread() {

			public void run() {

				try {
					jobLauncher.run(
							kaoLaPushJob,
							new JobParametersBuilder().addString("msgNo", message.getMsgNo()).addString("sendStartTime", message.getSendStartTime()).addString("sendEndTime", message.getSendEndTime())
									.addString("sendOvertimeRule", String.valueOf(message.getSendOvertimeRule()))
									.addString("sendRate", message.getSendRate().setScale(2, BigDecimal.ROUND_HALF_UP).toString()).addString("msgPlatform", String.valueOf(message.getMsgPlatform()))
									.toJobParameters());

				} catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException | JobParametersInvalidException e) {
					logger.error(e.getMessage(), e);
				}
			}
		});
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
				MessageFormat.format("create table push_record_{0}(id int not null auto_increment,msg_no varchar(10) not null,udid varchar(100),primary key (id))", msgNo),
				"update push_message set send_state=1 where msg_no='" + msgNo + "'" };
		jdbcTemplate.batchUpdate(sqls);
	}

	@Override
	public void importMongoData2MySql(String msgNo, BigDecimal sendRate) {

		List<Object[]> params = new ArrayList<Object[]>();

		DBCollection coll = mongoTemplate.getCollection("basis_channelSource");
		DBObject obj = new BasicDBObject();
		obj.put("udid", new BasicDBObject("$exists", true));
		long count = new BigDecimal(coll.count(obj)).multiply(sendRate).longValue();
		try (DBCursor cursor = coll.find(obj)) {

			while (cursor.hasNext() && count-- > 0) {
				// 操作cursor
				params.add(new Object[] { msgNo, cursor.next().get("udid") });
				if (params.size() == batchIntoMySqlSize) {
					jdbcTemplate.batchUpdate(MessageFormat.format("insert into push_record_{0}(msg_no,udid) values(?,?)", msgNo), params);
					params.clear();
				}
			}
			if (params.size() > 0) {
				jdbcTemplate.batchUpdate(MessageFormat.format("insert into push_record_{0}(msg_no,udid) values(?,?)", msgNo), params);
				params.clear();
			}
		}
	}

	@Override
	public void dropTable(String msgNo) {

		logger.info("drop table push_record_" + msgNo);
		String[] sqls = new String[] { MessageFormat.format("drop table push_record_{0}", msgNo),
				"update push_message set msg_receive_rate=msg_receive_times/msg_send_times where msg_no='" + msgNo + "'", "update push_message set send_state=2 where msg_no='" + msgNo + "'" };
		jdbcTemplate.batchUpdate(sqls);
	}

	@Override
	public void updateMsgSendTimes(String msgNo, int times) {

		jdbcTemplate.update("update push_message set msg_send_times=msg_send_times+? where msg_no=?", times, msgNo);

	}

	@Override
	public void updateMsgReceiveTimes(String msgNo, int times) {

		jdbcTemplate.update("update push_message set msg_receive_times=msg_receive_times+? where msg_no=?", times, msgNo);
	}

	@Override
	public void updateMsgReadTimes(String msgNo, int times) {

		jdbcTemplate.update("update push_message set msg_read_times=msg_read_times+? where msg_no=?", times, msgNo);
	}

	@Override
	public void updateMsgReceiveRate(String msgNo) {

		jdbcTemplate.update("update push_message set msg_receive_rate=msg_receive_times/msg_send_times where msg_no=?", msgNo);
	}

	@Override
	public void updateMsgReadRate(String msgNo) {

		jdbcTemplate.update("update push_message set msg_receive_rate=msg_read_times/msg_send_times where msg_no=?", msgNo);
	}

	@Override
	public void send(List<? extends PushRecord> records, String msgNo) {

		logger.info("items size:" + records.size());
		long start = System.currentTimeMillis();
		Message message = jdbcTemplate.queryForObject("select msg_no,msg_platform,msg_title,msg_content,send_start_time,send_end_time,send_overtime_rule from push_message where msg_no=?",
				new Object[] { msgNo }, new RowMapper<Message>() {

					@Override
					public Message mapRow(ResultSet rs, int rowNum) throws SQLException {

						Message message = new Message();
						message.setMsgNo(rs.getString("msg_no"));
						message.setMsgPlatform(rs.getInt("msg_platform"));
						message.setMsgTitle(rs.getString("msg_title"));
						message.setMsgContent(rs.getString("msg_content"));
						message.setSendStartTime(rs.getString("send_start_time"));
						message.setSendEndTime(rs.getString("send_end_time"));
						message.setSendOvertimeRule(rs.getInt("send_overtime_rule"));
						return message;
					}

				});
		if (message.getSendOvertimeRule() == 0) {
			return;
		} else if (message.getSendOvertimeRule() == 1) {
			Time now = Time.valueOf(new SimpleDateFormat("HH:mm:ss").format(new Date()));
			while (now.before(Time.valueOf(message.getSendStartTime())) || now.after(Time.valueOf(message.getSendEndTime()))) {
				logger.info("发送时间不在允许发送时间段范围");
				try {
					TimeUnit.MINUTES.sleep(suspendTime);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
				now = Time.valueOf(new SimpleDateFormat("HH:mm:ss").format(new Date()));
			}
		} else if ("2".equals(message.getSendOvertimeRule())) {
			// 强制发送,直接执行下面的代码
		}
		logger.info("**************use time 1 in milliseconds:" + (System.currentTimeMillis() - start));
		start = System.currentTimeMillis();
		// 更新表中该条信息的待发送记录
		this.updateMsgSendTimes(msgNo, records.size());
		logger.info("**************use time 2 in milliseconds:" + (System.currentTimeMillis() - start));
		start = System.currentTimeMillis();
		// 成功发送的消息条数
		int sentSize = 0;
		// 调用发送端接口,需要区分ios和android接口
		switch (message.getMsgPlatform()) {
		case 0:
			// 只发送android
			sentSize = sendService.batchSend(message, records, 0);
			break;
		case 1:
			// 只发送ios
			sentSize = sendService.batchSend(message, records, 1);
			break;
		case 2:
			// ios和android都发送
			sentSize = sendService.batchSend(message, records, 2);
			break;
		default:
			// 默认都发送
			sentSize = sendService.batchSend(message, records, 2);
		}
		logger.info("**************use time 3 in milliseconds:" + (System.currentTimeMillis() - start));
		start = System.currentTimeMillis();
		if (sentSize > 0) {
			// 发送完成
			this.updateMsgReceiveTimes(msgNo, sentSize);
		}
		logger.info("**************use time 4 in milliseconds:" + (System.currentTimeMillis() - start));
	}

	@Override
	public Message getMessageByMsgNo(String msgNo) {

		List<Message> list = jdbcTemplate
				.query("select msg_no,msg_platform,msg_title,msg_content,msg_send_times,msg_receive_times,msg_receive_rate,msg_read_times,msg_read_rate,send_state,send_start_time,send_end_time,send_overtime_rule,send_rate from push_message where msg_no=?",
						new Object[] { msgNo }, new RowMapper<Message>() {

							@Override
							public Message mapRow(ResultSet rs, int rowNum) throws SQLException {

								Message message = new Message();
								message.setMsgNo(rs.getString("msg_no"));
								message.setMsgPlatform(rs.getInt("msg_platform"));
								message.setMsgTitle(rs.getString("msg_title"));
								message.setMsgContent(rs.getString("msg_content"));
								message.setMsgSendTimes(rs.getInt("msg_send_times"));
								message.setMsgReceiveTimes(rs.getInt("msg_receive_times"));
								message.setMsgReceiveRate(rs.getBigDecimal("msg_receive_rate"));
								message.setMsgReadTimes(rs.getInt("msg_read_times"));
								message.setMsgReadRate(rs.getBigDecimal("msg_read_rate"));
								message.setSendState(rs.getInt("send_state"));
								message.setSendStartTime(rs.getString("send_start_time"));
								message.setSendEndTime(rs.getString("send_end_time"));
								message.setSendOvertimeRule(rs.getInt("send_overtime_rule"));
								message.setSendRate(rs.getBigDecimal("send_rate"));
								return message;
							}

						});
		return list.size() == 0 ? null : list.get(0);
	}
}
