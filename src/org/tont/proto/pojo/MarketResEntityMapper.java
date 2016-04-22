package org.tont.proto.pojo;

import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface MarketResEntityMapper {
	
	List<MarketResEntity> getMarketResByName(@Param("resName") String resName, @Param("startNum") int startNum, @Param("numInPage") int numInPage, @Param("orderByColumn") String orderBy);
	
	List<MarketResEntity> getMarketResByRid(@Param("resId") int rid, @Param("startNum") int startNum, @Param("numInPage") int numInPage, @Param("orderByColumn") String orderBy);
	
	List<MarketResEntity> getMarketResByPid(@Param("pid") int pid, @Param("startNum") int startNum, @Param("numInPage") int numInPage, @Param("orderByColumn") String orderBy);
	
	List<MarketResEntity> getMarketResByType(@Param("type") int type, @Param("startNum") int startNum, @Param("numInPage") int numInPage, @Param("orderByColumn") String orderBy);
	
	void buyMarketRes(int customerId, int itemId);
	
	void sellMarketRes(int sellerId, int rid, int num, int unit_price);
	
}
