package org.tont.core.disruptor;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.tont.core.cache.ServerCache;
import org.tont.core.netty.ServerChannelManager;
import org.tont.proto.GameMsgEntity;
import org.tont.proto.PlayerOwnedRes;
import org.tont.proto.PlayerOwnedShip;
import org.tont.proto.pojo.PlayerResource;
import org.tont.util.ConstantUtil;
import org.tont.util.MyBatisUtil;

import redis.clients.jedis.Jedis;

import com.google.protobuf.InvalidProtocolBufferException;
import com.lmax.disruptor.EventHandler;

public class SceneDispatchEventHandler  implements EventHandler<DispatchEvent> {

	private final String GATEWAY = ConstantUtil.GATEWAY;
	private final String GET_ALL_BY_PID = "org.tont.proto.pojo.PlayerResourceMapper.getAllByPid";
	
	@Override
	public void onEvent(DispatchEvent event, long sequence, boolean endOfBatch)
			throws Exception {
		//处理请求
		GameMsgEntity msg = event.getMsgEntity();
		switch(msg.getMsgCode()) {
			case 540:
				queryOwnRes(msg);
				break;
			case 550:
				queryOwnShips(msg);
				break;
		}
	}
	
	
	/**
	 * 查询玩家拥有的资源物品列表
	 * 以键值对<RID,NUM>的形式存储在一个map里
	 * @param msg
	 */
	public void queryOwnRes(GameMsgEntity msg) {
		
		Jedis jedis = null;
		
		try {
			jedis = ServerCache.getJedis();
			Map<String, String> resMap = null;
			
			if (jedis != null) {
				resMap = jedis.hgetAll("OwnRes:" + msg.getPid());
			} else {
				//无法连接至缓存服务器
				System.out.println("无法连接至缓存服务器");
				return;
			}
			
			if (resMap == null) {
				//缓存中没有，从数据库加载
				SqlSession sqlSession = null;
				
				try {
					//从数据库抓取数据并进行组装
					sqlSession = MyBatisUtil.getSqlSession();
					List<PlayerResource> resList = sqlSession.selectList(GET_ALL_BY_PID, msg.getPid());
					resMap = new HashMap<String,String>();
					for (int i = 0; i < resList.size() ; i++) {
						PlayerResource resItem = resList.get(i);
						resMap.put(resItem.getRid()+"", resItem.getNumber()+"");
					}
					
					//将数据载入缓存
					jedis.hmset("OwnRes:" + msg.getPid(), resMap);
					
					//经由网关返回结果给客户端
					PlayerOwnedRes.Resources.Builder builder = PlayerOwnedRes.Resources.newBuilder();
					builder.putAllResMap(resMap);
					PlayerOwnedRes.Resources resources = builder.build();
					msg.setData(resources.toByteArray());
					ServerChannelManager.getChannel(GATEWAY).writeAndFlush(msg);
					
				} finally {
					if (sqlSession != null) {
						sqlSession.close();
					}
				}
				
			} else {
				//从缓存中找到数据，经由网关返回结果给客户端
				PlayerOwnedRes.Resources.Builder builder = PlayerOwnedRes.Resources.newBuilder();
				builder.putAllResMap(resMap);
				PlayerOwnedRes.Resources resources = builder.build();
				msg.setData(resources.toByteArray());
				ServerChannelManager.getChannel(GATEWAY).writeAndFlush(msg);
			}
		} finally {
			if (jedis != null) {
				jedis.close();
			}
		}
	}
	
	
	/**
	 * 查询玩家拥有的舰船列表
	 * @param msg
	 */
	public void queryOwnShips(GameMsgEntity msg) {
		Jedis jedis = ServerCache.getJedis();
		try {
			String key = "OwnShip" + msg.getPid();
			List<byte[]> shipList = jedis.lrange(key.getBytes("UTF-8"), 0, -1);
			if (shipList == null) {
				//缓存中没有，从数据库加载
			} else {
				//从缓存中找到数据，经由网关返回结果给客户端
				PlayerOwnedShip.Ships.Builder builder = PlayerOwnedShip.Ships.newBuilder();
				int num = shipList.size();
				for (int i = 0 ; i < num ; i ++) {
					PlayerOwnedShip.ShipEntity ship = PlayerOwnedShip.ShipEntity.parseFrom(shipList.get(i));
					builder.setShip(i, ship);
				}
				PlayerOwnedShip.Ships ships = builder.build();
				msg.setData(ships.toByteArray());
				ServerChannelManager.getChannel(GATEWAY).writeAndFlush(msg);
			}
		} catch (InvalidProtocolBufferException e) {
			//问题数据，记录日志
			return;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} finally {
			if (jedis != null) {
				jedis.close();
			}
		}
	}
	
}
