package org.tont.proto.pojo;

import java.sql.Timestamp;

public class MarketResEntity {
	
	private int itemId;
	private int pid;
	private int rid;
	private int number;
	private int unit_Price;
	private Timestamp createTime;

	
	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public int getPid() {
		return pid;
	}
	
	public void setPid(int pid) {
		this.pid = pid;
	}
	
	public int getRid() {
		return rid;
	}
	
	public void setRid(int rid) {
		this.rid = rid;
	}
	
	public int getNumber() {
		return number;
	}
	
	public void setNumber(int number) {
		this.number = number;
	}
	
	public int getUnit_Price() {
		return unit_Price;
	}
	
	public void setUnit_Price(int unit_Price) {
		this.unit_Price = unit_Price;
	}
	
	public Timestamp getCreateTime() {
		return createTime;
	}
	
	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}
	
	@Override
	public String toString() {
		String print = "{ itemid = " + this.itemId + " : "
				+ "pid = " + this.pid + " : "
				+ "rid = " + this.rid + " : "
				+ "number = " + this.number + " : "
				+ "unit_Price = " + this.unit_Price + " } ";
		return print;
	}
	
}
