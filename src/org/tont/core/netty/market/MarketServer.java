package org.tont.core.netty.market;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

import org.tont.core.disruptor.MarketDispatchEventHandler;
import org.tont.core.disruptor.MessageDispatcher;
import org.tont.core.netty.NettyServer;
import org.tont.exceptions.ConfigParseException;

import com.lmax.disruptor.SleepingWaitStrategy;

public class MarketServer extends NettyServer {
	
	private static MessageDispatcher dispatcher = new MessageDispatcher(4096, new SleepingWaitStrategy());	//MessageDispatcher负责分发登录注册消息至处理队列

	public MarketServer(String configPath,
			ChannelInitializer<SocketChannel> initializer)
			throws ConfigParseException {
		super(configPath, initializer);
	}
	
	public static MessageDispatcher Dispatcher() {
		return dispatcher;
	}
	
	@Override
	public void run() {
		dispatcher.init(new MarketDispatchEventHandler());
		super.run();
	}

}
