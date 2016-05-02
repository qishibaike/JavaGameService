-- 取消在售的市场物品
DROP PROCEDURE IF EXISTS cancelMarketResSale;

DELIMITER //
CREATE PROCEDURE cancelMarketResSale(
	IN in_pid INT,
	IN in_itemid INT,
	OUT out_message VARCHAR(15)
)
BEGIN
DECLARE res_id INTEGER DEFAULT -1;
DECLARE res_sale_num INTEGER DEFAULT -1;
DECLARE res_seller_id INTEGER DEFAULT -1;
DECLARE owned_res_num INTEGER DEFAULT 0;
DECLARE t_error INTEGER DEFAULT 0;
DECLARE not_find_data INTEGER DEFAULT 0;
DECLARE CONTINUE HANDLER FOR SQLEXCEPTION SET t_error=1;
DECLARE CONTINUE HANDLER FOR NOT FOUND SET not_find_data=1;
	
	START TRANSACTION;
	
		SELECT MARKET_RES.PID, MARKET_RES.RID, MARKET_RES.NUMBER
		FROM MARKET_RES
		WHERE MARKET_RES.ITEMID = in_itemid
		INTO res_seller_id, res_id, res_sale_num;
		
		IF not_find_data = 1 THEN
			-- 未在市场上找到该物品出售条目
			SET t_error = 4;
		ELSEIF in_pid = res_seller_id THEN
			
			-- 删除市场相应出售条目
			DELETE FROM MARKET_RES WHERE MARKET_RES.ITEMID = in_itemid;
			
			-- 增加玩家该物品的库存
			SELECT PLAYER_RES.NUMBER
			FROM PLAYER_RES
			WHERE PLAYER_RES.PID = in_pid AND PLAYER_RES.RID = res_id
			LIMIT 1
			INTO owned_res_num;
			
			IF not_find_data = 1 THEN
				INSERT INTO PLAYER_RES(PID, RID, NUMBER) VALUES(in_pid, res_id, res_sale_num);
			ELSE
				
				UPDATE PLAYER_RES 
				SET PLAYER_RES.NUMBER = (PLAYER_RES.NUMBER + res_sale_num)
				WHERE PLAYER_RES.PID = in_pid AND PLAYER_RES.RID = res_id
				LIMIT 1;
			
			END IF;
			
		ELSE
			-- 玩家ID和出售者ID不一致，非法篡改
			SET t_error = 2;
		END IF;
		
	IF t_error = 1 THEN
		SET out_message = 'sql error';
		ROLLBACK;
	ELSEIF t_error = 2 THEN
		SET out_message = 'unequal pid';
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