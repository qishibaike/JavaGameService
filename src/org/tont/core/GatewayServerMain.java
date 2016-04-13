package org.tont.core;

import org.tont.core.netty.connector.BaseServerConnector;
import org.tont.core.netty.gateway.Gateway;
import org.tont.core.netty.send.GlobalConnectionHandler;
import org.tont.core.netty.send.MarketConnectionHandler;
import org.tont.core.netty.send.SceneConnectionHandler;
import org.tont.exceptions.ConfigParseException;

public class GatewayServerMain {

	public static void main(String[] args) throws ConfigParseException {
		Gateway gateway = new Gateway("/GatewayConfiguration.properties");
		new Thread(gateway).start();
		BaseServerConnector marketCon = new BaseServerConnector(2185,"127.0.0.1",new MarketConnectionHandler());
		new Thread(marketCon).start();
		BaseServerConnector globalCon = new BaseServerConnector(8222,"127.0.0.1",new GlobalConnectionHandler());
		new Thread(globalCon).start();
		BaseServerConnector sceneCon = new BaseServerConnector(3685,"127.0.0.1",new SceneConnectionHandler());
		new Thread(sceneCon).start();
	}
}
