package org.tont.proto.pojo;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

public interface MarketResEntityMapper {
	
	List<MarketResEntity> getMarketResByName(@Param("resName") String resName, @Param("startNum") int startNum, @Param("numInPage") int numInPage, @Param("orderByColumn") String orderBy);
	
	int getTotalCountByName(@Param("resName") String resName);
	
	List<MarketResEntity> getMarketResByRid(@Param("resId") int rid, @Param("startNum") int startNum, @Param("numInPage") int numInPage, @Param("orderByColumn") String orderBy);
	
	int getTotalCountByRid(@Param("resId") int rid);
	
	List<MarketResEntity> getMarketResByPid(@Param("pid") int pid, @Param("startNum") int startNum, @Param("numInPage") int numInPage, @Param("orderByColumn") String orderBy);
	
	int getTotalCountByPid(@Param("pid") int pid);
	
	List<MarketResEntity> getMarketResByType(@Param("type") int type, @Param("startNum") int startNum, @Param("numInPage") int numInPage, @Param("orderByColumn") String orderBy);
	
	int getTotalCountByType(@Param("type") int type);
	
	int buyMarketRes(Map<String, Object> param);
	
	int sellMarketRes(Map<String, Object> param);
	
	int cancelMarketResSale(Map<String, Object> param);
	
}
