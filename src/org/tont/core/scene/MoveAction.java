package org.tont.core.scene;

import org.tont.proto.MoveBroadcast.MoveEntity;

/****
 * 添加time字段用于进行角色移动的合法性检验
 * @author 95
 *
 */
public class MoveAction {
	public MoveEntity moveEntity;
	public long time;
	
	public MoveAction(MoveEntity entity, long time) {
		this.moveEntity = entity;
		this.time = time;
	}
}
