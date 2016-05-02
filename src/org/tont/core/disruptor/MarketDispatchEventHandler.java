package org.tont.core.disruptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.tont.core.netty.ServerChannelManager;
import org.tont.proto.GameMsgEntity;
import org.tont.proto.MarketTrade.BuyRequest;
import org.tont.proto.MarketTrade.CancelSaleRequest;
import org.tont.proto.MarketTrade.QueryItemsRequest;
import org.tont.proto.MarketTrade.QueryItemsResponse;
import org.tont.proto.MarketTrade.ResourceItem;
import org.tont.proto.MarketTrade.SellRequest;
import org.tont.proto.MarketTrade.TradeResultResponse;
import org.tont.proto.pojo.MarketResEntity;
import org.tont.util.ConstantUtil;
import org.tont.util.MyBatisUtil;

import com.google.protobuf.InvalidProtocolBufferException;
import com.lmax.disruptor.EventHandler;

public class MarketDispatchEventHandler implements EventHandler<DispatchEvent> {
	
	private final String GATEWAY = ConstantUtil.GATEWAY;
	private final String BUY_MARKET_RES = "org.tont.proto.pojo.MarketResEntityMapper.buyMarketRes";
	private final String SELL_MARKET_RES = "org.tont.proto.pojo.MarketResEntityMapper.sellMarketRes";
	private final String CANCEL_MARKET_RES_SALE = "org.tont.proto.pojo.MarketResEntityMapper.cancelMarketResSale";
	
	private final String GET_MARKET_RES_BY_NAME = "org.tont.proto.pojo.MarketResEntityMapper.getMarketResByName";
	private final String GET_TOTAL_COUNT_BY_NAME = "org.tont.proto.pojo.MarketResEntityMapper.getTotalCountByName";
	
	private final String GET_MARKET_RES_BY_RID = "org.tont.proto.pojo.MarketResEntityMapper.getMarketResByRid";
	private final String GET_TOTAL_COUNT_BY_RID = "org.tont.proto.pojo.MarketResEntityMapper.getTotalCountByRid";
	
	private final String GET_MARKET_RES_BY_PID = "org.tont.proto.pojo.MarketResEntityMapper.getMarketResByPid";
	private final String GET_TOTAL_COUNT_BY_PID = "org.tont.proto.pojo.MarketResEntityMapper.getTotalCountByPid";
	
	private final String GET_MARKET_RES_BY_TYPE = "org.tont.proto.pojo.MarketResEntityMapper.getMarketResByType";
	private final String GET_TOTAL_COUNT_BY_TYPE = "org.tont.proto.pojo.MarketResEntityMapper.getTotalCountByType";
	
	private final int numInPage = 8;

	@Override
	public void onEvent(DispatchEvent event, long sequence, boolean endOfBatch)
			throws Exception {
		//处理请求
		GameMsgEntity msg = event.getMsgEntity();
		switch(msg.getMsgCode()) {
		
			case 310:	//获取市场条目
				queryResItems(msg);
				break;
				
			case 330:	//购买市场物品
				buy(msg);
				break;
				
			case 340:	//出售物品
				sell(msg);
				break;
				
			case 350:	//取消正在出售的物品
				cancelSale(msg);
				break;
				
			default:
				break;
		}
	}
	
	
	public void queryResItems(GameMsgEntity msg) {
		
		QueryItemsRequest tradeReq = null;
		
		try {
			tradeReq = QueryItemsRequest.parseFrom(msg.getData());
		} catch (InvalidProtocolBufferException e) {
			//非法数据
			return;
		}
		
		int queryBy = tradeReq.getQueryBy();
		String queryKey = tradeReq.getQueryKey();
		switch (queryBy) {
		
			//case 1 : By Name
			case 1:
				queryByName(msg, queryKey, tradeReq.getPage(), this.numInPage, getColumnName(tradeReq.getOrderBy()));
				break;
			
			//case 2 : By Rid
			case 2:
				queryByRid(msg, Integer.parseInt(queryKey), tradeReq.getPage(), this.numInPage, getColumnName(tradeReq.getOrderBy()));
				break;
			
			//case 3 : By Pid
			case 3:
				queryByPid(msg, Integer.parseInt(queryKey), tradeReq.getPage(), this.numInPage, getColumnName(tradeReq.getOrderBy()));
				break;
			
			//case 4 : By Type
			case 4:
				queryByType(msg, Integer.parseInt(queryKey), tradeReq.getPage(), this.numInPage, getColumnName(tradeReq.getOrderBy()));
				break;
		}
		
	}
	
	
	private String getColumnName(int orderBy) {
		return "RID";
	}
	
	
	public void queryByName(GameMsgEntity msg, String name, int curPage, int numInPage, String orderByColumn) {
		
		SqlSession session = MyBatisUtil.getSqlSession(false);
		
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("resName", name);
		param.put("startNum", (curPage - 1) * numInPage);
		param.put("numInPage", numInPage);
		param.put("orderByColumn", orderByColumn);
		
		List<MarketResEntity> list = session.selectList(GET_MARKET_RES_BY_NAME, param);
		int totalCount = session.selectOne(GET_TOTAL_COUNT_BY_NAME, name);
		
		session.commit();
		
		sendQueryResult(msg, curPage, list, totalCount);
	}
	
	
	public void queryByRid(GameMsgEntity msg, int rid, int curPage, int numInPage, String orderByColumn) {
		
		SqlSession session = MyBatisUtil.getSqlSession(false);
		
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("resId", rid);
		param.put("startNum", (curPage - 1) * numInPage);
		param.put("numInPage", numInPage);
		param.put("orderByColumn", orderByColumn);
		
		List<MarketResEntity> list = session.selectList(GET_MARKET_RES_BY_RID, param);
		int totalCount = session.selectOne(GET_TOTAL_COUNT_BY_RID, rid);
		
		session.commit();
		
		sendQueryResult(msg, curPage, list, totalCount);
	}
	
	
	public void queryByPid(GameMsgEntity msg, int pid, int curPage, int numInPage, String orderByColumn) {
		
		SqlSession session = MyBatisUtil.getSqlSession(false);
		
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("pid", pid);
		param.put("startNum", (curPage - 1) * numInPage);
		param.put("numInPage", numInPage);
		param.put("orderByColumn", orderByColumn);
		
		List<MarketResEntity> list = session.selectList(GET_MARKET_RES_BY_PID, param);
		int totalCount = session.selectOne(GET_TOTAL_COUNT_BY_PID, pid);
		
		session.commit();
		
		sendQueryResult(msg, curPage, list, totalCount);
	}
	
	
	public void queryByType(GameMsgEntity msg, int type, int curPage, int numInPage, String orderByColumn) {
		
		SqlSession session = MyBatisUtil.getSqlSession(false);
		
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("type", type);
		param.put("startNum", (curPage - 1) * numInPage);
		param.put("numInPage", numInPage);
		param.put("orderByColumn", orderByColumn);
		
		List<MarketResEntity> list = session.selectList(GET_MARKET_RES_BY_TYPE, param);
		int totalCount = session.selectOne(GET_TOTAL_COUNT_BY_TYPE, type);
		
		session.commit();
		
		sendQueryResult(msg, curPage, list, totalCount);
	}
	
	
	//将查询结果封装成数据包发送
	public void sendQueryResult(GameMsgEntity msg, int curPage, List<MarketResEntity> list, int totalCount) {
		QueryItemsResponse.Builder builder = QueryItemsResponse.newBuilder();
		builder.setPage(curPage);
		builder.setTotalPageCount(getValue(totalCount,numInPage));
		
		ResourceItem.Builder itemBuilder = ResourceItem.newBuilder();
		for (int i = 0 ; i < list.size() ; i++) {
			MarketResEntity entity = list.get(i);
			itemBuilder.setItemId(entity.getItemId());
			itemBuilder.setSellerId(entity.getPid());
			itemBuilder.setRid(entity.getRid());
			itemBuilder.setNumber(entity.getNumber());
			itemBuilder.setUnitPrice(entity.getUnit_Price());
			itemBuilder.setCreateTime(entity.getCreateTime().getTime());
			ResourceItem item = itemBuilder.build();
			builder.setResult(i, item);
		}
		
		QueryItemsResponse response = builder.build();
		
		msg.setData(response.toByteArray());
		ServerChannelManager.getChannel(GATEWAY).writeAndFlush(msg);
	}
	
	
	//购买
	public void buy(GameMsgEntity msg) {
		
		BuyRequest req = null;
		
		try {
			req = BuyRequest.parseFrom(msg.getData());
		} catch (InvalidProtocolBufferException e) {
			//
		}
		
		if (req == null) {
			return;
		}
		
		SqlSession session = MyBatisUtil.getSqlSession();
		
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("in_pid", req.getPid());
		param.put("in_itemid", req.getItemid());
		param.put("in_num", req.getNum());
		param.put("out_message", "");
		
		int result_code = session.selectOne(BUY_MARKET_RES, param);
		
		TradeResultResponse.Builder builder = TradeResultResponse.newBuilder();
		//Type 1 表示 购买请求
		builder.setType(1);
		builder.setIsSuccess(result_code);
		builder.setResultMsg((String)param.get("out_message"));
		builder.setSeq(req.getSeq());
		TradeResultResponse response = builder.build();
		
		msg.setMsgCode((short)380);
		msg.setData(response.toByteArray());
		ServerChannelManager.getChannel(GATEWAY).writeAndFlush(msg);
	}
	
	
	//出售
	public void sell(GameMsgEntity msg) {
		
		SellRequest req = null;
		
		try {
			req = SellRequest.parseFrom(msg.getData());
		} catch (InvalidProtocolBufferException e) {
			//
		}
		
		if (req == null) {
			return;
		}
		
		SqlSession session = MyBatisUtil.getSqlSession();
		
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("in_pid", req.getPid());
		param.put("in_rid", req.getRid());
		param.put("in_sale_num", req.getNum());
		param.put("in_unit_price", req.getUnitPrice());
		param.put("out_message", "");
		
		int result_code = session.selectOne(SELL_MARKET_RES, param);
		
		TradeResultResponse.Builder builder = TradeResultResponse.newBuilder();
		//Type 2 表示 出售请求
		builder.setType(2);
		builder.setIsSuccess(result_code);
		builder.setResultMsg((String)param.get("out_message"));
		builder.setSeq(req.getSeq());
		TradeResultResponse response = builder.build();
		
		msg.setMsgCode((short)380);
		msg.setData(response.toByteArray());
		ServerChannelManager.getChannel(GATEWAY).writeAndFlush(msg);
	}
	
	
	//取消出售
	public void cancelSale(GameMsgEntity msg) {
		
		CancelSaleRequest req = null;
		
		try {
			req = CancelSaleRequest.parseFrom(msg.getData());
		} catch (InvalidProtocolBufferException e) {
			//
		}
		
		if (req == null) {
			return;
		}
		
		SqlSession session = MyBatisUtil.getSqlSession();
		
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("in_pid", req.getPid());
		param.put("in_itemid", req.getItemid());
		param.put("out_message", "");
		
		int result_code = session.selectOne(CANCEL_MARKET_RES_SALE, param);
		
		TradeResultResponse.Builder builder = TradeResultResponse.newBuilder();
		//Type 3 表示 取消出售请求
		builder.setType(3);
		builder.setIsSuccess(result_code);
		builder.setResultMsg((String)param.get("out_message"));
		builder.setSeq(req.getSeq());
		TradeResultResponse response = builder.build();
		
		msg.setMsgCode((short)380);
		msg.setData(response.toByteArray());
		ServerChannelManager.getChannel(GATEWAY).writeAndFlush(msg);
	}
	
	
	private int getValue(int a, int b) {
		return a % b == 0 ? a/b : a/b + 1 ;
	}

}
