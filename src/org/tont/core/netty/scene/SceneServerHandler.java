package org.tont.core.netty.scene;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import org.tont.core.netty.ServerChannelManager;
import org.tont.proto.GameMsgEntity;
import org.tont.util.ConstantUtil;

public class SceneServerHandler extends ChannelInboundHandlerAdapter {
	private final String CLOSE = ConstantUtil.CLOSE;
	public final String GATEWAY = ConstantUtil.GATEWAY;
	public final String MARKET = ConstantUtil.MARKET;
	public final String BATTLE = ConstantUtil.BATTLE;
	public final String SCENE = ConstantUtil.SCENE;
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ServerChannelManager.putChannel(GATEWAY, ctx.channel());
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		GameMsgEntity msgEntity = (GameMsgEntity) msg;
		msgEntity.setChannel(ctx.channel());
		
		switch (msgEntity.getMsgCode()) {
		
			case 410:
				SceneServer.sceneMaster.handleMove(msgEntity);
				break;
				
			case 411:
			case 412:
			case 413:
				SceneServer.sceneMaster.switchScene(msgEntity);
				break;
				
			default:
				SceneServer.Dispatcher().onData(msgEntity);
				break;
				
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		if (cause.getMessage().equals(CLOSE)) {
			System.out.println("网关服务器 " + ctx.channel().remoteAddress() + " 断开了连接");
		}
	}
	
}
