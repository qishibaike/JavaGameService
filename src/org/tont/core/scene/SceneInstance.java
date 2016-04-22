package org.tont.core.scene;

import java.util.HashMap;
import java.util.Map;

import org.tont.proto.GameMsgEntity;
import org.tont.proto.MoveBroadcast.MoveEntity;
import org.tont.proto.MoveBroadcast.SwitchScene;

import com.google.protobuf.InvalidProtocolBufferException;

public class SceneInstance implements InOrOutSceneValidate {
	
	private int sceneId;
	private int sceneType;	//1=normal,2=personal
	private SceneNodeBorder border;
	private Position initPos;
	
	private int cellWidth;
	private int cellHeight;
	private int childNodeNumX;
	private int childNodeNumY;
	
	private SceneChildNode [] nodes;
	private Map<Integer,Integer> playerNodeMap = new HashMap<Integer,Integer>();	//<pid,nodeid>
	
	public SceneInstance (int sceneId, int sceneType, int childNodeNumX, int childNodeNumY, SceneNodeBorder border) {
		this(sceneId, sceneType, childNodeNumX, childNodeNumY, border, new Position(0, 0));
	}
	
	public SceneInstance (int sceneId, int sceneType, int childNodeNumX, int childNodeNumY, SceneNodeBorder border, Position initPosition) {
		
		this.sceneId = sceneId;
		this.sceneType = sceneType;
		this.border = border;
		this.initPos = initPosition;
		
		this.nodes = new SceneChildNode[childNodeNumX * childNodeNumY];
		
		int cellWidth = (border.rightBorder - border.leftBorder) / childNodeNumX;
		int cellHeight = (border.topBorder - border.bottomBorder) / childNodeNumY;
		this.childNodeNumX = childNodeNumX;
		this.childNodeNumY = childNodeNumY;
		
		//以左下角为坐标系原点，创建场景子节点实例矩阵
		for (int nodeX = 0 ; nodeX < childNodeNumX ; nodeX++) {
			for (int nodeY = 0 ; nodeY < childNodeNumY ;nodeY++) {
				
				SceneNodeBorder childBorder = new SceneNodeBorder(border.leftBorder + cellWidth * nodeX
						, border.leftBorder + cellWidth * (nodeX + 1)
						, border.bottomBorder + cellHeight * (nodeY + 1)
						, border.bottomBorder + cellHeight * nodeY);
				int nodeId = nodeY * childNodeNumX + nodeX;
				nodes[nodeId] = new SceneChildNode( nodeId , childBorder);
			}
		}
		
		//向子节点填充相邻子节点的引用
		for (int nodeX = 0 ; nodeX < childNodeNumX ; nodeX++) {
			for (int nodeY = 0 ; nodeY < childNodeNumY ;nodeY++) {
				int nodeId = nodeY * childNodeNumX + nodeX;
				SceneChildNode node = nodes[nodeId];
				node.setNeighbouringNodes(nodeId % childNodeNumX == 0 ? null : (nodeId / childNodeNumX == 0 ? null : nodes[nodeId - childNodeNumX - 1])
						, nodeId / childNodeNumX == 0 ? null : nodes[nodeId - childNodeNumX]
						, nodeId % childNodeNumX == childNodeNumX-1 ? null : (nodeId / childNodeNumX == 0 ? null : nodes[nodeId - childNodeNumX + 1])
						, nodeId % childNodeNumX == 0 ? null : nodes[nodeId - 1]
						, nodeId % childNodeNumX == childNodeNumX-1 ? null : nodes[nodeId + 1]
						, nodeId % childNodeNumX == 0 ? null : (nodeId / childNodeNumX == childNodeNumY - 1 ? null : nodes[nodeId + childNodeNumX -1])
						, nodeId / childNodeNumX == childNodeNumY - 1 ? null : nodes[nodeId + childNodeNumX]
						, nodeId % childNodeNumX == childNodeNumX-1 ? null : (nodeId / childNodeNumX == childNodeNumY - 1 ? null : nodes[nodeId + childNodeNumX -1]));
			}
		}
		
		//加载完成
		System.out.println("已加载场景实例：SceneID=" + sceneId + " ；场景子节点数：" + childNodeNumX * childNodeNumY);
	}
	
	
	/**
	 * 向该场景添加一名玩家
	 **/
	public void addPlayer(GameMsgEntity msg, Position pos) {
		
		int nodeId = getNodeIdByPos(pos);
		
		if (nodeId == -1) {
			return;
		}
		
		this.playerNodeMap.put(msg.getPid(), nodeId);
		MoveEntity moveEntity = null;
		try {
			moveEntity = MoveEntity.parseFrom(msg.getData());
		} catch (InvalidProtocolBufferException e) {
			//
		}
		this.nodes[nodeId].addPlayer(msg, moveEntity);
	}
	
	public void addPlayer(GameMsgEntity msg) {
		addPlayer(msg, initPos);
	}
	
	
	/**
	 * 从该场景移除一名玩家
	 **/
	public void removePlayer(GameMsgEntity msg) {
		int pid = msg.getPid();
		Integer nodeId = playerNodeMap.get(pid);
		
		if (nodeId == null) {
			return;
		}
		
		MoveEntity moveEntity = null;
		try {
			moveEntity = MoveEntity.parseFrom(msg.getData());
		} catch (InvalidProtocolBufferException e) {
			//
		}
		
		nodes[nodeId].removePlayer(msg, moveEntity);
		playerNodeMap.remove(pid);
	}
	
	
	/**
	 * 根据坐标计算所在的节点ID
	 **/
	private int getNodeIdByPos(Position pos) {
		if (pos.getX() >= border.leftBorder && pos.getX() <= border.rightBorder && pos.getY() <= border.topBorder && pos.getY() >= border.bottomBorder) {
			int nodeX = (pos.getX() - border.leftBorder) / cellWidth;
			int nodeY = (pos.getY() - border.bottomBorder) / cellHeight;
			return nodeY * childNodeNumX + nodeX;
		}
		return -1;
	}
	
	
	/**
	 * 处理普通的移动信息
	 **/
	public void handleMove(GameMsgEntity msg) {
		
		MoveEntity moveEntity = null;
		try {
			moveEntity = MoveEntity.parseFrom(msg.getData());
			
			int nodeId = playerNodeMap.get(msg.getPid());
			nodes[nodeId].handleMove(msg, moveEntity);
			
		} catch (InvalidProtocolBufferException e) {
			//
		}
	}
	
	
	@Override
	public boolean validateEnter(GameMsgEntity msg, SwitchScene scene) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean validateLeave(GameMsgEntity msg, SwitchScene scene) {
		// TODO Auto-generated method stub
		return true;
	}
	
	
	public int getSceneId() {
		return sceneId;
	}

	public void setSceneId(int sceneId) {
		this.sceneId = sceneId;
	}

	public int getSceneType() {
		return sceneType;
	}

	public void setSceneType(int sceneType) {
		this.sceneType = sceneType;
	}

	public SceneNodeBorder getBorder() {
		return border;
	}

	public void setBorder(SceneNodeBorder border) {
		this.border = border;
	}

	public SceneChildNode[] getNodes() {
		return nodes;
	}

	public void setNodes(SceneChildNode[] nodes) {
		this.nodes = nodes;
	}

	public Map<Integer, Integer> getPlayerNodeMap() {
		return playerNodeMap;
	}

	public void setPlayerNodeMap(Map<Integer, Integer> playerNodeMap) {
		this.playerNodeMap = playerNodeMap;
	}

	public Position getInitPos() {
		return initPos;
	}

	public void setInitPos(Position initPos) {
		this.initPos = initPos;
	}

	public int getCellWidth() {
		return cellWidth;
	}

	public void setCellWidth(int cellWidth) {
		this.cellWidth = cellWidth;
	}

	public int getCellHeight() {
		return cellHeight;
	}

	public void setCellHeight(int cellHeight) {
		this.cellHeight = cellHeight;
	}

	public int getChildNodeNumX() {
		return childNodeNumX;
	}

	public void setChildNodeNumX(int childNodeNumX) {
		this.childNodeNumX = childNodeNumX;
	}

	public int getChildNodeNumY() {
		return childNodeNumY;
	}

	public void setChildNodeNumY(int childNodeNumY) {
		this.childNodeNumY = childNodeNumY;
	}

}
