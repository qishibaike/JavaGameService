package org.tont.proto.pojo;

public class Ship {
	
	private int pid;
	private int shipId;
	private int level;
	private int exp;
	private String name;
	private String detail;
	
	
	public int getExp() {
		return exp;
	}

	public void setExp(int exp) {
		this.exp = exp;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public int getShipId() {
		return shipId;
	}
	
	public void setShipId(int shipId) {
		this.shipId = shipId;
	}
	
	public int getLevel() {
		return level;
	}
	
	public void setLevel(int level) {
		this.level = level;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDetail() {
		return detail;
	}
	
	public void setDetail(String detail) {
		this.detail = detail;
	}
	
}
