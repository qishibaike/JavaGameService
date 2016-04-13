package test;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.lang.management.ManagementFactory;

import org.tont.core.netty.RevMsgDecoder;
import org.tont.core.netty.SendMsgEncoder;

public class TestClient implements Runnable {
	public static String pid = ManagementFactory.getRuntimeMXBean().getName();
	
	private static final int readerIdleTimeSeconds = 10;
	private static final int writerIdleTimeSeconds = 10;
	private static final int allIdleTimeSeconds = 0;
	
	private ChannelInboundHandlerAdapter handler;
	
	public TestClient(ChannelInboundHandlerAdapter handler) {
		this.handler = handler;
	}
	
	public void connect(int port, String host) throws Exception {
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap boot = new Bootstrap();
			boot.group(group)
				.channel(NioSocketChannel.class)
				.option(ChannelOption.TCP_NODELAY, true)
				.handler(new ChannelInitializer<SocketChannel>() {

					@Override
					protected void initChannel(SocketChannel ch)
							throws Exception {
						ch.pipeline().addLast("idleStateHandler", new IdleStateHandler(readerIdleTimeSeconds, writerIdleTimeSeconds,allIdleTimeSeconds));
						ch.pipeline().addLast("Decoder", new RevMsgDecoder());
						ch.pipeline().addLast("Encoder", new SendMsgEncoder());
						ch.pipeline().addLast(handler);
					}
					
				});
			
			ChannelFuture cFuture = boot.connect(host, port).sync();
			cFuture.channel().closeFuture().sync();
			
		} finally {
			group.shutdownGracefully();
		}
	}

	@Override
	public void run() {
		try {
			this.connect(59427, "127.0.0.1");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
