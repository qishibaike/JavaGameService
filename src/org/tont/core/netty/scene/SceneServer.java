package org.tont.core.netty.scene;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

import org.tont.core.cache.ServerCache;
import org.tont.core.disruptor.MessageDispatcher;
import org.tont.core.disruptor.SceneDispatchEventHandler;
import org.tont.core.netty.NettyServer;
import org.tont.exceptions.ConfigParseException;

import com.lmax.disruptor.SleepingWaitStrategy;

public class SceneServer extends NettyServer {
	
	private static MessageDispatcher dispatcher = new MessageDispatcher(4096, new SleepingWaitStrategy());	//MessageDispatcher负责分发登录注册消息至处理队列
	
	public SceneServer(String configPath,
			ChannelInitializer<SocketChannel> initializer)
			throws ConfigParseException {
		super(configPath, initializer);
		ServerCache.initPool();
	}
	
	public static MessageDispatcher Dispatcher() {
		return dispatcher;
	}
	
	@Override
	public void run() {
		dispatcher.init(new SceneDispatchEventHandler());
		super.run();
	}

}
