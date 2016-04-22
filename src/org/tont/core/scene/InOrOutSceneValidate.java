package org.tont.core.scene;

import org.tont.proto.GameMsgEntity;
import org.tont.proto.MoveBroadcast.SwitchScene;

/**
 * 验证是否允许玩家进入场景或离开场景的接口
 * @author 95
 *
 */
public interface InOrOutSceneValidate {
	
	boolean validateEnter(GameMsgEntity msg, SwitchScene scene);
	boolean validateLeave(GameMsgEntity msg, SwitchScene scene);
	
}
