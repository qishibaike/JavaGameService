package org.tont.proto.pojo;

import java.util.List;

public interface ShipMapper {

	List<Ship> getShipsByPid(int pid);
	
}
