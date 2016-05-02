package test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.tont.proto.pojo.MarketResEntity;
import org.tont.util.MyBatisUtil;

public class DatabaseTest {
	
	private final String BUY_MARKET_RES = "org.tont.proto.pojo.MarketResEntityMapper.buyMarketRes";
	private final String SELL_MARKET_RES = "org.tont.proto.pojo.MarketResEntityMapper.sellMarketRes";
	private final String CANCEL_MARKET_RES_SALE = "org.tont.proto.pojo.MarketResEntityMapper.cancelMarketResSale";
	private final String GET_MARKET_RES_BY_RID = "org.tont.proto.pojo.MarketResEntityMapper.getMarketResByRid";
	private final String GET_TOTAL_COUNT_BY_RID = "org.tont.proto.pojo.MarketResEntityMapper.getTotalCountByRid";
	
	public void test_getMarketResByRid() {
		SqlSession session = MyBatisUtil.getSqlSession();
		
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("resId", 2);
		param.put("startNum", 0);
		param.put("numInPage", 8);
		param.put("orderByColumn", "RID");
		
		List<MarketResEntity> list = session.selectList(GET_MARKET_RES_BY_RID, param);
		for (int i = 0 ; i < list.size() ; i++) {
			MarketResEntity entity = list.get(i);
			System.out.println(entity);
		}
		
		int totalCount = session.selectOne(GET_TOTAL_COUNT_BY_RID, 2);
		System.out.println(totalCount);
	}
	
	public void test_buyMarketRes() {
		SqlSession session = MyBatisUtil.getSqlSession();
		
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("in_pid", 1);
		param.put("in_itemid", 6);
		param.put("in_num", 2);
		param.put("out_message", "");
		
		int result_code = session.selectOne(BUY_MARKET_RES, param);
		System.out.println("result_code = " + result_code);
		System.out.println(param.get("out_message"));
		assert(result_code == 0);
	}
	
	public void test_sellMarketRes() {
		SqlSession session = MyBatisUtil.getSqlSession();
		
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("in_pid", 1);
		param.put("in_rid", 2);
		param.put("in_sale_num", 4);
		param.put("in_unit_price", 34);
		param.put("out_message", "");
		
		int result_code = session.selectOne(SELL_MARKET_RES, param);
		System.out.println("result_code = " + result_code);
		System.out.println(param.get("out_message"));
		assert(result_code == 0);
	}
	
	public void test_cancelMarketResSale() {
		SqlSession session = MyBatisUtil.getSqlSession();
		
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("in_pid", 1);
		param.put("in_itemid", 8);
		param.put("out_message", "");
		
		int result_code = session.selectOne(CANCEL_MARKET_RES_SALE, param);
		System.out.println("result_code = " + result_code);
		System.out.println(param.get("out_message"));
		assert(result_code == 0);
	}
	
	public static void main(String[] args) {
		new DatabaseTest().test_getMarketResByRid();
	}

}
