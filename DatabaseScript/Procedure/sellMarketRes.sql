-- 出售市场物品
DROP PROCEDURE IF EXISTS sellMarketRes;

DELIMITER //
CREATE PROCEDURE sellMarketRes(
	IN in_pid INT,
	IN in_rid INT,
	IN in_sale_num INT,
	IN in_unit_price INT,
	OUT out_message VARCHAR(10)
)
BEGIN
DECLARE player_gold INTEGER DEFAULT -1;
DECLARE owned_res_num INTEGER DEFAULT -1;
DECLARE t_error INTEGER DEFAULT 0;
DECLARE not_find_data INTEGER DEFAULT 0;
DECLARE CONTINUE HANDLER FOR SQLEXCEPTION SET t_error=1;
DECLARE CONTINUE HANDLER FOR NOT FOUND SET not_find_data=1;
	
	START TRANSACTION;
	
		SELECT PLAYER_INFO.GOLD
		FROM PLAYER_INFO 
		WHERE PLAYER_INFO.PID = in_pid
		INTO player_gold;
		
		SELECT PLAYER_RES.NUMBER 
		FROM PLAYER_RES 
		WHERE PLAYER_RES.PID = in_pid AND PLAYER_RES.RID = in_rid
		LIMIT 1
		INTO owned_res_num;
		
		IF not_find_data = 1 THEN
			-- 未找到该玩家 或 该玩家未拥有此物品
			SET t_error = 4;
		ELSEIF in_sale_num <= owned_res_num THEN
			IF (in_sale_num * in_unit_price * 0.03) <= player_gold THEN
				UPDATE PLAYER_INFO 
				SET PLAYER_INFO.GOLD = (player_gold - in_sale_num * in_unit_price * 0.03) 
				WHERE PLAYER_INFO.PID = in_pid;
				
				INSERT INTO MARKET_RES(PID, RID, NUMBER, UNIT_PRICE) VALUES(in_pid, in_rid, in_sale_num, in_unit_price);
				
				IF in_sale_num = owned_res_num THEN
					DELETE FROM PLAYER_RES
					WHERE PLAYER_RES.PID = in_pid AND PLAYER_RES.RID = in_rid;
				ELSE
					UPDATE PLAYER_RES 
					SET PLAYER_RES.NUMBER = (owned_res_num - in_sale_num)
					WHERE PLAYER_RES.PID = in_pid AND PLAYER_RES.RID = in_rid
					LIMIT 1;
				END IF;
			ELSE
				-- 金币不足以支付手续费
				SET t_error = 2;
			END IF;
		ELSE
			-- 欲出售物品数超过已拥有数
			SET t_error = 3;
		END IF;
		
	IF t_error = 1 THEN
		SET out_message = 'sql error';
		ROLLBACK;
	ELSEIF t_error = 2 THEN
		SET out_message = 'lack gold';
		ROLLBACK;
	ELSEIF t_error = 3 THEN
		SET out_message = 'error num';
		ROLLBACK;
	ELSEIF t_error = 4 THEN
		SET out_message = 'dont find';
		ROLLBACK;
	ELSE
		SET out_message = 'success';
		COMMIT;
	END IF;
	
	select
		t_error;
	
END//

DELIMITER ;