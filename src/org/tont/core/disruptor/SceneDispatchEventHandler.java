package org.tont.core.disruptor;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.tont.core.cache.ServerCache;
import org.tont.core.netty.ServerChannelManager;
import org.tont.proto.GameMsgEntity;
import org.tont.proto.PlayerOwnedRes;
import org.tont.proto.PlayerOwnedShip;
import org.tont.util.ConstantUtil;

import redis.clients.jedis.Jedis;

import com.google.protobuf.InvalidProtocolBufferException;
import com.lmax.disruptor.EventHandler;

public class SceneDispatchEventHandler  implements EventHandler<DispatchEvent> {

	private final String GATEWAY = ConstantUtil.GATEWAY;
	private final String MARKET = ConstantUtil.MARKET;
	private final String BATTLE = ConstantUtil.BATTLE;
	private final String SCENE = ConstantUtil.SCENE;
	
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
	 * @param msg
	 */
	public void queryOwnRes(GameMsgEntity msg) {
		Jedis jedis = ServerCache.getJedis();
		try {
			Map<String, String> res = jedis.hgetAll("OwnRes:" + msg.getPid());
			if (res == null) {
				//缓存中没有，从数据库加载
			} else {
				//从缓存中找到数据，经由网关返回结果给客户端
				PlayerOwnedRes.Resources.Builder builder = PlayerOwnedRes.Resources.newBuilder();
				builder.putAllResMap(res);
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
