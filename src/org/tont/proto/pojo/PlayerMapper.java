package org.tont.proto.pojo;


public interface PlayerMapper {
	
	void addPlayer(Player player);
	void banByPid(int pid);
	void updatePlayer(Player player);
	Player findByPid(int pid);
	Player findByAccount(String account);
	Player findByNickname(String nickname);
	Player findByPlayer(Player player);
}
