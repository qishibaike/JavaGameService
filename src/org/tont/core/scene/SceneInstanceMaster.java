package org.tont.core.scene;

import java.util.HashMap;
import java.util.Map;

import org.tont.proto.GameMsgEntity;
import org.tont.proto.MoveBroadcast.MoveEntity;
import org.tont.proto.MoveBroadcast.SwitchScene;

import com.google.protobuf.InvalidProtocolBufferException;

public class SceneInstanceMaster {
	
	private Map<Integer, SceneInstance> sceneMap = new HashMap<Integer,SceneInstance>();
	
	public void initSceneNode() {
		sceneMap.put(1, new SceneInstance(1, 1, 0, 0, new SceneNodeBorder(-500, 500, 500, -500)));
	}
	
	
	/****
	 * 处理玩家进入场景、退出场景或者切换场景
	 * msgCode : 411 , 412 , 413
	 * @param msg
	 */
	public void switchScene(GameMsgEntity msg) {
		try {
			SwitchScene switchScene = SwitchScene.parseFrom(msg.getData());
			int from = switchScene.getFrom();
			int to = switchScene.getTo();
			
			if (from == -1) {
				SceneInstance toScene = sceneMap.get(to);
				if (toScene != null && toScene.validateEnter(msg, switchScene)) {
					sceneMap.get(to).addPlayer(msg);
				}
			} else if (to == -1) {
				SceneInstance fromScene = sceneMap.get(from);
				if (fromScene != null && fromScene.validateLeave(msg, switchScene)) {
					sceneMap.get(from).removePlayer(msg);
				}
			} else {
				SceneInstance fromScene = sceneMap.get(from);
				if (fromScene != null) {
					SceneInstance toScene = sceneMap.get(to);
					if (toScene != null) {
						if (fromScene.validateLeave(msg, switchScene) && toScene.validateEnter(msg, switchScene)) {
							sceneMap.get(to).addPlayer(msg);
							sceneMap.get(from).removePlayer(msg);
						}
					}
				}
			}
		} catch (InvalidProtocolBufferException e) {
			//非法数据，记录日志
		}
	}
	
	
	public void handleMove(GameMsgEntity msg) {
		
		MoveEntity moveEntity = null;
		
		try {
			moveEntity = MoveEntity.parseFrom(msg.getData());
		} catch (InvalidProtocolBufferException e) {
			//
		}
		
		int sid = moveEntity.getSceneId();
		if (sid >= 0) {
			sceneMap.get(sid).handleMove(msg, moveEntity);
		}
	}

	public Map<Integer, SceneInstance> getSceneMap() {
		return sceneMap;
	}

	public void setSceneMap(Map<Integer, SceneInstance> sceneMap) {
		this.sceneMap = sceneMap;
	}

}
