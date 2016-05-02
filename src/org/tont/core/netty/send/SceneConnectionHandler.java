package org.tont.core.netty.send;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import org.tont.core.netty.ServerChannelManager;
import org.tont.core.netty.gateway.Gateway;
import org.tont.core.session.SessionEntity;
import org.tont.proto.GameMsgEntity;
import org.tont.proto.MoveBroadcast.MoveBroadcastPacket;
import org.tont.proto.MoveBroadcast.MoveEntity;

import com.google.protobuf.InvalidProtocolBufferException;

import io.netty.channel.ChannelHandler.Sharable;

// 网关->市场服务器	之间连接的处理器
@Sharable
public class SceneConnectionHandler extends ChannelInboundHandlerAdapter {
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ServerChannelManager.putChannel("SceneServerChannel", ctx.channel());
		System.out.println("成功连接至场景服务器");
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		GameMsgEntity msgEntity = (GameMsgEntity) msg;
		
		switch (msgEntity.getMsgCode()) {
			case 99:
				SessionEntity session = Gateway.getSessionPool().findSession(msgEntity.getPid());
				session.getChannel().close();
				session.setChannel(null);
				break;
			
			case 420:
				broadcastMoveAction(msgEntity);
				break;
			
			default:
				Gateway.getSessionPool().findSession(msgEntity.getPid()).getChannel().writeAndFlush(msgEntity);
				break;
		}
	}
	
	
	//广播场景移动信息
	private void broadcastMoveAction(GameMsgEntity msg) {
		try {
			MoveBroadcastPacket packet = MoveBroadcastPacket.parseFrom(msg.getData());
			MoveEntity moveEntity = packet.getMove();
			byte [] data = moveEntity.toByteArray();
			
			int num = packet.getRecIdCount();
			for (int i = 0; i < num; i++) {
				int recPid = packet.getRecId(i);
				SessionEntity session = Gateway.getSessionPool().findSession(recPid);
				if (session !=null && session.getChannel() != null && session.getChannel().isActive()) {
					GameMsgEntity broadcastMsg = new GameMsgEntity();
					broadcastMsg.setMsgCode((short) 420);
					broadcastMsg.setPid(recPid);
					broadcastMsg.setData(data);
					session.getChannel().writeAndFlush(broadcastMsg);
				}
			}
		} catch (InvalidProtocolBufferException e) {
			//非法信息，记录日志
		}
	}

}
