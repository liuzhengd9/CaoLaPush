package com.autoradio.push.pojo;

import java.io.Serializable;

public class PushRecord implements Serializable {

	private static final long serialVersionUID = 8644569319338099246L;

	private int id;

	private String msgNo;

	private String receiveInfoTableName;

	public int getId() {

		return id;
	}

	public void setId(int id) {

		this.id = id;
	}

	public String getMsgNo() {

		return msgNo;
	}

	public void setMsgNo(String msgNo) {

		this.msgNo = msgNo;
	}

	public String getReceiveInfoTableName() {

		return receiveInfoTableName;
	}

	public void setReceiveInfoTableName(String receiveInfoTableName) {

		this.receiveInfoTableName = receiveInfoTableName;
	}
}
