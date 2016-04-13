package org.tont.core;

import org.tont.core.netty.ServerChannelInitializer;
import org.tont.core.netty.connector.BaseServerConnector;
import org.tont.core.netty.scene.SceneServer;
import org.tont.core.netty.scene.SceneServerHandler;
import org.tont.core.netty.send.GlobalConnectionHandler;
import org.tont.exceptions.ConfigParseException;

public class SceneServerMain {

	public static void main(String[] args) throws ConfigParseException {
		SceneServer server = new SceneServer("/SceneConfiguration.properties", new ServerChannelInitializer(new SceneServerHandler()));
		new Thread(server).start();
		BaseServerConnector globalCon = new BaseServerConnector(8222,"127.0.0.1",new GlobalConnectionHandler());
		new Thread(globalCon).start();
	}
}
