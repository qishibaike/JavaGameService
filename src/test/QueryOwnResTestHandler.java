package test;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import org.tont.proto.GameMsgEntity;
import org.tont.proto.PlayerOwnedRes;

public class QueryOwnResTestHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		
		GameMsgEntity msgEntity = new GameMsgEntity();
		msgEntity.setMsgCode((short)540);
		msgEntity.setPid(3);
		
		ctx.writeAndFlush(msgEntity);
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		GameMsgEntity msgEntity = (GameMsgEntity) msg;
		PlayerOwnedRes.Resources resources = PlayerOwnedRes.Resources.parseFrom(msgEntity.getData());
		System.out.println(resources.getResMap());
	}
}
