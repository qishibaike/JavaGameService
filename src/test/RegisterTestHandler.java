package test;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import org.tont.proto.GameMsgEntity;
import org.tont.proto.RegisterRequest;
import org.tont.proto.RegisterResponse;

public class RegisterTestHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		
		RegisterRequest.RegisterRequestEntity.Builder regReqBuilder = RegisterRequest.RegisterRequestEntity.newBuilder();
		regReqBuilder.setAccount("tontboy_test2");
		regReqBuilder.setPassword("123456");
		regReqBuilder.setNickname("赤炎鬼凤2");
		RegisterRequest.RegisterRequestEntity regReq = regReqBuilder.build();
		
		GameMsgEntity msgEntity = new GameMsgEntity();
		msgEntity.setMsgCode((short)110);
		msgEntity.setData(regReq.toByteArray());
		
		ctx.writeAndFlush(msgEntity);
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		GameMsgEntity msgEntity = (GameMsgEntity) msg;
		RegisterResponse.RegisterResponseEntity response = RegisterResponse.RegisterResponseEntity.parseFrom(msgEntity.getData());
		System.out.println(response.getSuccess());
		System.out.println(response.getMessage());
		System.out.println(response.getPid());
		//GameMessage.PlayerAccountInfo info = GameMessage.PlayerAccountInfo.parseFrom(msgEntity.getData());
	}
}
