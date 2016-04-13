package test;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import org.tont.proto.GameMsgEntity;
import org.tont.proto.LoginRequest.LoginRequestEntity;
import org.tont.proto.LoginResponse.LoginResponseEntity;

public class LoginTestHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		
		LoginRequestEntity.Builder builder = LoginRequestEntity.newBuilder();
		builder.setAccount("tontboy_test");
		builder.setPassword("123456");
		GameMsgEntity msgEntity = new GameMsgEntity();
		msgEntity.setMsgCode((short)100);
		msgEntity.setData(builder.build().toByteArray());
		
		ctx.writeAndFlush(msgEntity);
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		GameMsgEntity msgEntity = (GameMsgEntity) msg;
		LoginResponseEntity response = LoginResponseEntity.parseFrom(msgEntity.getData());
		System.out.println(response.getSuccess());
		System.out.println(response.getMessage());
		System.out.println(response.getPid());
	}
}
