package org.tont.core.netty.connector;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import org.tont.core.netty.RevMsgDecoder;
import org.tont.core.netty.SendMsgEncoder;

public class BaseServerConnector implements Runnable {
	
	private boolean closed = false;
	private int port;
	private String host;
	private long period = 8000L;
	private final ChannelInboundHandlerAdapter handler;
	private Bootstrap boot;
	private EventLoopGroup group;
	
	public BaseServerConnector(int port, String ip, ChannelInboundHandlerAdapter handler) {
		this.port = port;
		this.host = ip;
		this.handler = handler;
	}
	
	public void connect() throws InterruptedException {
		group = new NioEventLoopGroup();
		try {
			boot = new Bootstrap();
			boot.group(group)
				.channel(NioSocketChannel.class)
				.option(ChannelOption.TCP_NODELAY, true)
				.handler(new ChannelInitializer<SocketChannel>() {

					@Override
					protected void initChannel(SocketChannel ch)
							throws Exception {
						ch.pipeline().addLast("Decoder", new RevMsgDecoder());
						ch.pipeline().addLast("Encoder", new SendMsgEncoder());
						ch.pipeline().addLast(handler);
					}
					
				});
			tryConnect();
		} finally {
			group.shutdownGracefully();
			if (!closed) {
				Thread.sleep(period);
				BaseServerConnector con = new BaseServerConnector(port,host,handler);
				new Thread(con).start();
			}
		}
	}
	
	public void tryConnect() throws InterruptedException {
		System.out.println("tryConnect"+port);
		ChannelFuture cFuture = boot.connect(host, port);
		cFuture.sync();
		cFuture.channel().closeFuture().sync();
	}

	@Override
	public void run() {
		try {
			this.connect();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Exception ex) {
			if (!ex.getMessage().startsWith("Connection refused:")) {
				throw ex;
			}
		}
	}

}
