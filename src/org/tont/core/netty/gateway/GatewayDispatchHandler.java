package org.tont.core.netty.gateway;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;

import org.tont.core.netty.ServerChannelManager;
import org.tont.core.session.SessionEntity;
import org.tont.core.session.SessionPoolImp;
import org.tont.proto.GameMsgEntity;
import org.tont.util.ConstantUtil;

public class GatewayDispatchHandler extends ChannelInboundHandlerAdapter {
	
	private final String CLOSE = ConstantUtil.CLOSE;
	public final String GATEWAY = ConstantUtil.GATEWAY;
	public final String MARKET = ConstantUtil.MARKET;
	public final String BATTLE = ConstantUtil.BATTLE;
	public final String SCENE = ConstantUtil.SCENE;
	
	private SessionPoolImp sessionPool;
	private static final AttributeKey<Integer> PLAYERID = AttributeKey.valueOf("CHANNEL.PID");
	
	public GatewayDispatchHandler(SessionPoolImp sessionPool) {
		super();
		this.sessionPool = sessionPool;
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		//↓测试代码，测试完成后删除↓
		SessionEntity session = new SessionEntity(3, "", ctx.channel());
		sessionPool.setSession(3, session);
		//↑测试代码，测试完成后删除↑
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		GameMsgEntity msgEntity = (GameMsgEntity) msg;
		msgEntity.setChannel(ctx.channel());
		
		if (msgEntity.getMsgCode() >= 200) {
			if (!tokenCheck(msgEntity)) {
				//无效Token
				msgEntity.setMsgCode((short) 105);
				msgEntity.setData(null);
				ctx.channel().writeAndFlush(msgEntity);
				return;
			} else {
				//将Channel与ID绑定,用于在连接断开后释放SessionPool中对应Session的Channel对象
				ctx.channel().attr(PLAYERID).set(msgEntity.getPid());
			}
		}
		
		//后期将抽离成XML文档进行映射
		switch (msgEntity.getMsgCode()) {
		
			case 100:
			case 110:	//注册登录等直接由网关处理的请求
				Gateway.Dispatcher().onData(msgEntity);
				break;
				
			case 130:	//获取角色信息
				Gateway.Dispatcher().onData(msgEntity);
				break;
				
			case 200:	//战斗数据
				Gateway.Gatherer().handleRequest();
				ServerChannelManager.getChannel(BATTLE).writeAndFlush(msgEntity);
				break;
				
			case 310:	//市场交易数据
			case 330:
			case 340:
			case 350:
				Gateway.Gatherer().handleRequest();
				ServerChannelManager.getChannel(MARKET).writeAndFlush(msgEntity);
				break;
				
			case 410:
			case 411:
			case 412:
			case 413:
				//场景数据
				Gateway.Gatherer().handleRequest();
				ServerChannelManager.getChannel(SCENE).writeAndFlush(msgEntity);
				break;
				
			case 540:
			case 550:
				//角色数据
				Gateway.Gatherer().handleRequest();
				System.out.println(msgEntity.getPid());
				ServerChannelManager.getChannel(SCENE).writeAndFlush(msgEntity);
				break;
				
			default:
				break;
		}
	}
	
	private boolean tokenCheck(GameMsgEntity msgEntity) {
		if (sessionPool.findSession(msgEntity.getPid()).getToken().equals(msgEntity.getToken())) {
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		Integer pid = ctx.channel().attr(PLAYERID).get();
		if (pid != null) {
			sessionPool.findSession(pid).setChannel(null);
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		if (cause.getMessage().equals(CLOSE)) {
			System.out.println("玩家 " + ctx.channel().remoteAddress() + "断开了连接");
		}
	}
	
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
			throws Exception {
		if (IdleStateEvent.class.isAssignableFrom(evt.getClass())) {
			IdleStateEvent event = (IdleStateEvent) evt;
			if (event.state() == IdleState.READER_IDLE || event.state() == IdleState.WRITER_IDLE || event.state() == IdleState.ALL_IDLE) {
				System.out.println("玩家 " + ctx.channel().remoteAddress() + " read/write idle");
				ctx.channel().close();
			}
		}
	}
}
