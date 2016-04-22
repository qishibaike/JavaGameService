package org.tont.core.scene;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.tont.core.GlobalTime;
import org.tont.core.netty.ServerChannelManager;
import org.tont.proto.GameMsgEntity;
import org.tont.proto.MoveBroadcast.MoveBroadcastPacket;
import org.tont.proto.MoveBroadcast.MoveEntity;
import org.tont.util.ConstantUtil;


public class SceneChildNode {
	
	private final String GATEWAY = ConstantUtil.GATEWAY;

	private int nodeId;
	private SceneNodeBorder border;
	private SceneChildNode [] neighbouringNodes;

	private Map<Integer,MoveAction> playerMap = new HashMap<Integer,MoveAction>();
	
	
	public SceneChildNode(int nodeId, SceneNodeBorder border) {
		this.nodeId = nodeId;
		this.border = border;
		this.neighbouringNodes = new SceneChildNode[9];
	}
	
	
	/**
	 * 处理玩家移动消息
	 **/
	public void handleMove(GameMsgEntity msg, MoveEntity moveEntity) {
		
		MoveAction action = new MoveAction(moveEntity, GlobalTime.getCurTime());
		//验证移动数据合法性
		if (!validateMove(action)) {
			//非法移动，记录日志
			//断开该玩家的连接
			msg.setMsgCode((short) 99);
			msg.setData(null);
			ServerChannelManager.getChannel(GATEWAY).writeAndFlush(msg);
			return;
		}
		int pid = moveEntity.getPid();
		
		//判断是否还在本节点管理范围内
		int relativePos = isInNode(moveEntity.getPosCurrentX(), moveEntity.getPosCurrentY());
		if (relativePos != 4) {
			//该位置已不在本节点，将该玩家交由其他节点管理
			neighbouringNodes[relativePos].handleMove(msg, moveEntity);
			playerMap.remove(pid);
		}
		
		playerMap.put(pid, action);
		
		//创建广播数据包
		MoveBroadcastPacket packet = getBroadcastPacket(moveEntity);
		
		msg.setMsgCode((short) 420);
		msg.setData(packet.toByteArray());
		ServerChannelManager.getChannel(GATEWAY).writeAndFlush(msg);
	}
	
	
	/**
	 * 验证移动的合法性 
	 **/
	private boolean validateMove(MoveAction MoveAction) {
		return true;
	}
	
	
	/**
	 *按九宫格排列，返回0~8表示该位置相对本节点的位置
	 *如返回4，表示该位置在本节点内 
	 *以左下角为坐标系原点
	 *左下方节点为0
	 **/
	private int isInNode(int x, int y) {
		
		if (x < border.leftBorder) {
			if (y < border.bottomBorder) {
				return 0;
			} else if (y > border.topBorder) {
				return 6;
			} else {
				return 3;
			}
		}
		
		if (x > border.rightBorder) {
			if (y < border.bottomBorder) {
				return 2;
			} else if (y > border.topBorder) {
				return 8;
			} else {
				return 5;
			}
		}
		
		if (y > border.topBorder) {
			return 7;
		} else if (y < border.bottomBorder) {
			return 1;
		} else {
			return 4;
		}
		
	}
	
	
	/***
	 * 设置相邻节点的引用
	 * 以左下角为坐标系原点
	 * @param leftTop
	 * @param top
	 * @param rightTop
	 * @param left
	 * @param right
	 * @param leftBottom
	 * @param bottom
	 * @param rightBottom
	 */
	public void setNeighbouringNodes (SceneChildNode leftBottom
										, SceneChildNode bottom
										, SceneChildNode rightBottom
										, SceneChildNode left
										, SceneChildNode right
										, SceneChildNode leftTop
										, SceneChildNode top
										, SceneChildNode rightTop) {
		neighbouringNodes[0] = leftBottom;
		neighbouringNodes[1] = bottom;
		neighbouringNodes[2] = rightBottom;
		neighbouringNodes[3] = left;
		neighbouringNodes[4] = this;
		neighbouringNodes[5] = right;
		neighbouringNodes[6] = leftTop;
		neighbouringNodes[7] = top;
		neighbouringNodes[8] = rightTop;
	}
	
	
	/***
	 * 玩家刚进入场景，向该场景子节点添加玩家
	 * @param msg
	 * @param moveEntity
	 */
	public void addPlayer(GameMsgEntity msg, MoveEntity moveEntity) {
		playerMap.put(msg.getPid(), new MoveAction(moveEntity, GlobalTime.getCurTime()));
		
		//创建广播数据包
		MoveBroadcastPacket packet = getBroadcastPacket(moveEntity);
		
		msg.setMsgCode((short) 420);
		msg.setData(packet.toByteArray());
		ServerChannelManager.getChannel(GATEWAY).writeAndFlush(msg);
	}
	
	
	/***
	 * 玩家离开该场景
	 * @param msg
	 * @param moveEntity
	 */
	public void removePlayer(GameMsgEntity msg, MoveEntity moveEntity) {
		playerMap.remove(msg.getPid());
		
		//创建广播数据包
		MoveBroadcastPacket packet = getBroadcastPacket(moveEntity);
		
		msg.setMsgCode((short) 420);
		msg.setData(packet.toByteArray());
		ServerChannelManager.getChannel(GATEWAY).writeAndFlush(msg);
	}
	
	//创建广播数据包
	private MoveBroadcastPacket getBroadcastPacket (MoveEntity moveEntity) {
		//创建广播数据包
		MoveBroadcastPacket.Builder packetBuilder = MoveBroadcastPacket.newBuilder();
		packetBuilder.setMove(moveEntity);
		
		//遍历相邻九个节点的玩家，填充广播数据包
		int k = -1;
		for (int i = 0; i < 9 ; i++) {
			if (neighbouringNodes[i] != null) {
				Iterator<Entry<Integer, MoveAction>> iter = neighbouringNodes[i].playerMap.entrySet().iterator();
				while (iter.hasNext()) {
					k++;
					Map.Entry<Integer,MoveAction> entry = (Map.Entry<Integer,MoveAction>) iter.next();
					int recPid = entry.getKey();
					packetBuilder.setRecId(k, recPid);
				}
			}
		}
		
		MoveBroadcastPacket packet = packetBuilder.build();
		
		return packet;
	}
	
	//Getter and Setter
	
	public int getSceneId() {
		return nodeId;
	}
	
	public void setSceneId(int sceneId) {
		this.nodeId = sceneId;
	}
	
	public Map<Integer, MoveAction> getPlayerMap() {
		return playerMap;
	}
	
	public void setPlayerMap(Map<Integer, MoveAction> playerMap) {
		this.playerMap = playerMap;
	}
	
	public void setBorder(SceneNodeBorder border) {
		this.border = border;
	}
	
	public SceneNodeBorder getBorder() {
		return border;
	}
	
}
