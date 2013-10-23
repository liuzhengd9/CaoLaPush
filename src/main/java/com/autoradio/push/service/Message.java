package com.autoradio.push.service;

import java.io.Serializable;
import java.math.BigDecimal;

public class Message implements Serializable {

	private static final long serialVersionUID = -8282626441151762171L;

	// 消息唯一编码，异步系统间可以通过该编码获取相应的记录
	private String msgNo;

	// 消息发送平台:0代表anroid,1代表ios,2全部
	private int msgPlatform = 2;

	// 消息标题
	private String msgTitle;

	// 消息内容
	private String msgContent;

	// 消息发送次数
	private int msgSendTimes;

	// 消息送达次数
	private int msgReceiveTimes;

	// 消息送达率
	private BigDecimal msgReceiveRate;

	// 消息开启次数
	private int msgReadTimes;

	// 消息开启率
	private BigDecimal msgReadRate;

	// 发送状态:0未发送,1发送中,2发送完成
	private int sendState = 0;

	// 消息发送开始时间
	private String sendStartTime;

	// 消息发送截止时间
	private String sendEndTime;

	// 息发送超过发送时间段后采取的策略:0放弃,1等待(默认),2强制发送
	private int sendOvertimeRule = 1;

	public String getMsgNo() {

		return msgNo;
	}

	public void setMsgNo(String msgNo) {

		this.msgNo = msgNo;
	}

	public int getMsgPlatform() {

		return msgPlatform;
	}

	public void setMsgPlatform(int msgPlatform) {

		this.msgPlatform = msgPlatform;
	}

	public String getMsgTitle() {

		return msgTitle;
	}

	public void setMsgTitle(String msgTitle) {

		this.msgTitle = msgTitle;
	}

	public String getMsgContent() {

		return msgContent;
	}

	public void setMsgContent(String msgContent) {

		this.msgContent = msgContent;
	}

	public int getMsgSendTimes() {

		return msgSendTimes;
	}

	public void setMsgSendTimes(int msgSendTimes) {

		this.msgSendTimes = msgSendTimes;
	}

	public int getMsgReceiveTimes() {

		return msgReceiveTimes;
	}

	public void setMsgReceiveTimes(int msgReceiveTimes) {

		this.msgReceiveTimes = msgReceiveTimes;
	}

	public BigDecimal getMsgReceiveRate() {

		return msgReceiveRate;
	}

	public void setMsgReceiveRate(BigDecimal msgReceiveRate) {

		this.msgReceiveRate = msgReceiveRate;
	}

	public int getMsgReadTimes() {

		return msgReadTimes;
	}

	public void setMsgReadTimes(int msgReadTimes) {

		this.msgReadTimes = msgReadTimes;
	}

	public BigDecimal getMsgReadRate() {

		return msgReadRate;
	}

	public void setMsgReadRate(BigDecimal msgReadRate) {

		this.msgReadRate = msgReadRate;
	}

	public int getSendState() {

		return sendState;
	}

	public void setSendState(int sendState) {

		this.sendState = sendState;
	}

	public String getSendStartTime() {

		return sendStartTime;
	}

	public void setSendStartTime(String sendStartTime) {

		this.sendStartTime = sendStartTime;
	}

	public String getSendEndTime() {

		return sendEndTime;
	}

	public void setSendEndTime(String sendEndTime) {

		this.sendEndTime = sendEndTime;
	}

	public int getSendOvertimeRule() {

		return sendOvertimeRule;
	}

	public void setSendOvertimeRule(int sendOvertimeRule) {

		this.sendOvertimeRule = sendOvertimeRule;
	}

}
