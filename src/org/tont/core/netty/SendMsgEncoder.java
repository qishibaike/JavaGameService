package org.tont.core.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import org.tont.proto.GameMsgEntity;

public class SendMsgEncoder extends MessageToByteEncoder<GameMsgEntity> {

	@Override
	protected void encode(ChannelHandlerContext ctx, GameMsgEntity msg,
			ByteBuf out) throws Exception {
		int dataLength = msg.getData() == null ? 0 : msg.getData().length;
		out.ensureWritable(4 + 38 + dataLength);
		out.writeInt(dataLength);
		out.writeShort(msg.getMsgCode());
		out.writeInt(msg.getPid());
		byte[] tokenArray = new byte[32];
		if (msg.getToken() != null) {
			byte[] token = msg.getToken().getBytes("UTF-8");
			int limit = Math.min(token.length, 32);
			for (int i = 0; i<limit ; i++) {
				tokenArray[i] = token[i];
			}
		}
		out.writeBytes(tokenArray);
		if (dataLength > 0) {
			out.writeBytes(msg.getData());
		}
	}

}
