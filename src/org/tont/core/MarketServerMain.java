package org.tont.core;

import org.tont.core.netty.NettyServer;
import org.tont.core.netty.ServerChannelInitializer;
import org.tont.core.netty.connector.BaseServerConnector;
import org.tont.core.netty.market.MarketServerHandler;
import org.tont.core.netty.send.GlobalConnectionHandler;
import org.tont.exceptions.ConfigParseException;

public class MarketServerMain {

	public static void main(String[] args) throws ConfigParseException {
		NettyServer server = new NettyServer("/MarketConfiguration.properties", new ServerChannelInitializer(new MarketServerHandler()));
		new Thread(server).start();
		BaseServerConnector globalCon = new BaseServerConnector(8222,"127.0.0.1",new GlobalConnectionHandler());
		new Thread(globalCon).start();
	}
}
