package org.tont.core.disruptor;

import org.tont.proto.GameMsgEntity;

import com.lmax.disruptor.EventHandler;

public class GatewayDispatchEventHandler implements EventHandler<DispatchEvent> {

	@Override
	public void onEvent(DispatchEvent event, long sequence, boolean endOfBatch)
			throws Exception {
		//处理请求
		GameMsgEntity msg = event.getMsgEntity();
		
		switch(msg.getMsgCode()) {
			case 100:
				registe(msg);
				break;
				
			case 101:
				login(msg);
				break;
		}
	}
	
	public void registe(GameMsgEntity msg) {}
	
	public void login(GameMsgEntity msg) {}

}
