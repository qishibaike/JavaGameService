package org.tont.core.disruptor;

import org.apache.ibatis.session.SqlSession;
import org.tont.core.netty.gateway.Gateway;
import org.tont.core.session.SessionEntity;
import org.tont.proto.GameMsgEntity;
import org.tont.proto.LoginRequest;
import org.tont.proto.LoginResponse;
import org.tont.proto.RegisterRequest;
import org.tont.proto.RegisterResponse;
import org.tont.proto.pojo.Player;
import org.tont.util.MyBatisUtil;
import org.tont.util.TokenHelper;

import com.google.protobuf.InvalidProtocolBufferException;
import com.lmax.disruptor.EventHandler;

public class GatewayDispatchEventHandler implements EventHandler<DispatchEvent> {
	
	private final String ADD_PLAYER = "org.tont.proto.pojo.PlayerMapper.addPlayer";
	private final String FIND_BY_ACCOUNT = "org.tont.proto.pojo.PlayerMapper.findByAccount";
	private final String FIND_BY_NICKNAME = "org.tont.proto.pojo.PlayerMapper.findByNickname";
	private final String FIND_BY_PLAYER = "org.tont.proto.pojo.PlayerMapper.findByPlayer";

	@Override
	public void onEvent(DispatchEvent event, long sequence, boolean endOfBatch)
			throws Exception {
		//处理请求
		GameMsgEntity msg = event.getMsgEntity();
		
		switch(msg.getMsgCode()) {
			case 100:
				login(msg);
				break;
				
			case 110:
				register(msg);
				break;
		}
	}
	
	public void register(GameMsgEntity msg) {
		try {
			RegisterRequest.RegisterRequestEntity regReq = RegisterRequest.RegisterRequestEntity.parseFrom(msg.getData());
			String message = null;
			
			//校验数据格式
			if (!validateRegister(regReq)) {
				//数据格式校验未通过
				message = "格式不正确";
				RegisterResponse.RegisterResponseEntity.Builder regResBuilder = RegisterResponse.RegisterResponseEntity.newBuilder();
				regResBuilder.setSuccess(false);
				regResBuilder.setMessage(message);
				msg.setData(regResBuilder.build().toByteArray());
				msg.getChannel().writeAndFlush(msg);
				return;
			}
			
			//注册玩家账号
			boolean isSuccess = false;
			SqlSession sqlSession = null;
			Player player = null;
			try {
				sqlSession = MyBatisUtil.getSqlSession(true);
				
				//检查是否已有重复账户名
				player = sqlSession.selectOne(FIND_BY_ACCOUNT,regReq.getAccount());
				if (player == null) {
					
					//检查是否已有重复昵称
					player = sqlSession.selectOne(FIND_BY_NICKNAME,regReq.getNickname());
					if (player == null) {
						
						//插入玩家账号信息
						player = new Player();
						player.setAccount(regReq.getAccount());
						player.setPassword(regReq.getPassword());
						player.setNickname(regReq.getNickname());
						if (sqlSession.insert(ADD_PLAYER, player) >= 1) {
							isSuccess = true;
							message = "SUCCESS!";
						}
						
					} else {
						message = "昵称重复";
					}
				} else {
					message = "账户名重复";
				}
			} finally {
				if (sqlSession != null) {
					sqlSession.close();
				}
			}
			
			//返回注册结果
			RegisterResponse.RegisterResponseEntity.Builder regResBuilder = RegisterResponse.RegisterResponseEntity.newBuilder();
			regResBuilder.setSuccess(isSuccess);
			regResBuilder.setMessage(message);
			if (isSuccess) {
				regResBuilder.setPid(player.getPid());
			}
			msg.setData(regResBuilder.build().toByteArray());
			msg.getChannel().writeAndFlush(msg);
		} catch (InvalidProtocolBufferException e) {
			//非法数据，记录日志
		} finally {
		}
	}
	
	public boolean validateRegister(RegisterRequest.RegisterRequestEntity regReq) {
		return true;
	}
	
	public void login(GameMsgEntity msg) {
		
		boolean isSuccess = false;
		SqlSession sqlSession = null;
		
		try {
			LoginRequest.LoginRequestEntity loginReq = LoginRequest.LoginRequestEntity.parseFrom(msg.getData());
			String message = null;
			
			//格式校验
			if (!validateLogin(loginReq)) {
				return;
			}
			
			Player player = new Player();
			player.setAccount(loginReq.getAccount());
			player.setPassword(loginReq.getPassword());
			
			sqlSession = MyBatisUtil.getSqlSession(true);
			
			//校验账号密码
			Player result = sqlSession.selectOne(FIND_BY_PLAYER,player);
			if (result == null||result.isBaned()) {
				if (result == null) {
					message = "该用户不存在或密码错误";
				} else {
					message = "该用户已被禁止登陆";
				}
				LoginResponse.LoginResponseEntity.Builder responseBuilder = LoginResponse.LoginResponseEntity.newBuilder();
				responseBuilder.setSuccess(isSuccess);
				responseBuilder.setMessage(message);
				msg.setData(responseBuilder.build().toByteArray());
				msg.getChannel().writeAndFlush(msg);
				return;
			}
			
			//分发token
			String token = TokenHelper.makeToken();
			
			//加入session池
			SessionEntity session = new SessionEntity(result.getPid(), token, msg.getChannel());
			Gateway.getSessionPool().setSession(result.getPid(), session);
			isSuccess = true;
			message = "登录成功";
			
			//回复响应消息,数据从缓存或数据库提取
			LoginResponse.LoginResponseEntity.Builder responseBuilder = LoginResponse.LoginResponseEntity.newBuilder();
			responseBuilder.setSuccess(isSuccess);
			responseBuilder.setMessage(message);
			responseBuilder.setPid(result.getPid());
			responseBuilder.setToken(token);
			responseBuilder.setNickname(result.getNickname());
			responseBuilder.setGold(result.getGold());
			responseBuilder.setCurScene(result.getCurScene());
			responseBuilder.setCurPosX(result.getCurPosX());
			responseBuilder.setCurPosY(result.getCurPosY());
			msg.setData(responseBuilder.build().toByteArray());
			msg.getChannel().writeAndFlush(msg);
			
			//检查缓存中是否有玩家数据，如果没有，则从数据库加载到缓存中
			//Jedis jedis = ServerCache.getJedis();
			
			
		} catch (InvalidProtocolBufferException e) {
			//非法数据，记录日志
		} finally {
			if (sqlSession != null) {
				sqlSession.close();
			}
		}
	}
	
	public boolean validateLogin(LoginRequest.LoginRequestEntity loginReq) {
		return true;
	}

}
