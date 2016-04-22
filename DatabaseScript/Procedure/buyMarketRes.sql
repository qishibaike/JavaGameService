-- 购买市场物品的存储过程
DROP PROCEDURE buyMarketRes;

DELIMITER //
CREATE PROCEDURE buyMarketRes(
	IN in_pid INT,
	IN in_itemid INT,
	IN in_num INT,
	OUT out_message VARCHAR(10)
)
BEGIN
DECLARE player_gold INTEGER DEFAULT -1;
DECLARE item_id INTEGER DEFAULT -1;
DECLARE res_id INTEGER DEFAULT -1;
DECLARE res_unit_price INTEGER DEFAULT -1;
DECLARE res_total_num INTEGER DEFAULT -1;
DECLARE t_error INTEGER DEFAULT 0;
DECLARE CONTINUE HANDLER FOR SQLEXCEPTION SET t_error=1;
	
	START TRANSACTION;
	
		SELECT PLAYER_INFO.GOLD
		FROM PLAYER_INFO 
		WHERE PLAYER_INFO.PID = in_pid
		INTO player_gold;
		
		SELECT MARKET_RES.ITEMID, MARKET_RES.RID, MARKET_RES.UNIT_PRICE, MARKET_RES.NUMBER
		FROM MARKET_RES
		WHERE MARKET_RES.ITEMID = in_itemid
		INTO item_id, res_id, res_unit_price, res_total_num;
		
		IF in_num <= res_total_num AND (in_num*res_unit_price) < player_gold THEN
			UPDATE PLAYER_INFO 
			SET PLAYER_INFO.GOLD = (player_gold-in_num*res_unit_price) 
			WHERE PLAYER_INFO.PID = in_pid;
			
			INSERT INTO PLAYER_RES(PID, RID, NUMBER) VALUES(in_pid, res_id, in_num);
            
            IF in_num = res_total_num THEN
				DELETE FROM MARKET_RES
                WHERE MARKET_RES.ITEMID = item_id;
			ELSE
				UPDATE MARKET_RES 
                SET MARKET_RES.NUMBER = (res_total_num - in_num)
                WHERE MARKET_RES.ITEMID = item_id;
            END IF;
		ELSE
			SET t_error = 1;
		END IF;
		
	IF t_error = 1 THEN
		SET out_message = 'error';
		ROLLBACK;
	ELSE
		COMMIT;
	END IF;
	
END//

DELIMITER ;