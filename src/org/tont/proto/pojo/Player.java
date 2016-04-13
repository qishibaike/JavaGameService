package org.tont.proto.pojo;

import java.sql.Timestamp;

public class Player {

	private int pid;
	private String account;
	private String password;
	private String nickname;
	private Timestamp createTime;
	private Timestamp lastLogin;
	private boolean isBaned;
	private int gold;
	private short cur_Scene;
	private short cur_Pos_X;
	private short cur_Pos_Y;
	private int hp;
	
	public int getPid() {
		return pid;
	}
	
	public void setPid(int pid) {
		this.pid = pid;
	}
	
	public String getAccount() {
		return account;
	}
	
	public void setAccount(String account) {
		this.account = account;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getNickname() {
		return nickname;
	}
	
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	
	public Timestamp getCreateTime() {
		return createTime;
	}
	
	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}
	
	public Timestamp getLastLogin() {
		return lastLogin;
	}
	
	public void setLastLogin(Timestamp lastLogin) {
		this.lastLogin = lastLogin;
	}
	
	public boolean isBaned() {
		return isBaned;
	}

	public void setBaned(boolean isBaned) {
		this.isBaned = isBaned;
	}
	
	public int getGold() {
		return gold;
	}
	
	public void setGold(int gold) {
		this.gold = gold;
	}
	
	public short getCurScene() {
		return cur_Scene;
	}
	
	public void setCurScene(short curScene) {
		this.cur_Scene = curScene;
	}
	
	public short getCurPosX() {
		return cur_Pos_X;
	}
	
	public void setCurPosX(short curPosX) {
		this.cur_Pos_X = curPosX;
	}
	
	public short getCurPosY() {
		return cur_Pos_Y;
	}
	
	public void setCurPosY(short curPosY) {
		this.cur_Pos_Y = curPosY;
	}
	
	public int getHp() {
		return hp;
	}
	
	public void setHp(int hp) {
		this.hp = hp;
	}
	
}
