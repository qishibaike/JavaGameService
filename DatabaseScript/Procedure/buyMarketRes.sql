-- �����г���Ʒ
DROP PROCEDURE IF EXISTS buyMarketRes;

DELIMITER //
CREATE PROCEDURE buyMarketRes(
	IN in_pid INT,
	IN in_itemid INT,
	IN in_num INT,
	OUT out_message VARCHAR(10)
)
BEGIN
DECLARE player_gold INTEGER DEFAULT -1;
DECLARE owned_res_num INTEGER DEFAULT 0;
DECLARE seller_id INTEGER DEFAULT -1;
DECLARE res_id INTEGER DEFAULT -1;
DECLARE res_unit_price INTEGER DEFAULT -1;
DECLARE res_total_num INTEGER DEFAULT -1;
DECLARE t_error INTEGER DEFAULT 0;
DECLARE not_find_data INTEGER DEFAULT 0;
DECLARE CONTINUE HANDLER FOR SQLEXCEPTION SET t_error=1;
DECLARE CONTINUE HANDLER FOR NOT FOUND SET not_find_data=1;
	
	START TRANSACTION;
	
		-- ��ȡ��������г���Ʒ����Ϣ
		SELECT MARKET_RES.PID, MARKET_RES.ITEMID, MARKET_RES.RID, MARKET_RES.UNIT_PRICE, MARKET_RES.NUMBER
		FROM MARKET_RES
		WHERE MARKET_RES.ITEMID = in_itemid
		INTO seller_id, in_itemid, res_id, res_unit_price, res_total_num;

		-- ��ȡ��ҽ����
		SELECT PLAYER_INFO.GOLD
		FROM PLAYER_INFO 
		WHERE PLAYER_INFO.PID = in_pid
		INTO player_gold;
	
		IF not_find_data = 1 THEN
			-- δ�ҵ������ �� δ���г����ҵ�����Ʒ
			SET t_error = 4;
		ELSEIF in_num <= res_total_num THEN
			IF (in_num*res_unit_price) <= player_gold THEN
				-- �۳���ҽ��
				UPDATE PLAYER_INFO 
				SET PLAYER_INFO.GOLD = (player_gold - in_num*res_unit_price) 
				WHERE PLAYER_INFO.PID = in_pid;
				
				-- �������ӵ�и���Ʒ����Ŀ
				SELECT PLAYER_RES.NUMBER
				FROM PLAYER_RES
				WHERE PLAYER_RES.PID = in_pid AND PLAYER_RES.RID = res_id
				LIMIT 1
				INTO owned_res_num;
				
				IF not_find_data = 1 THEN
					INSERT INTO PLAYER_RES(PID, RID, NUMBER) VALUES(in_pid, res_id, in_num);
				ELSE
					UPDATE PLAYER_RES 
					SET PLAYER_RES.NUMBER = PLAYER_RES.NUMBER + in_num
					WHERE PLAYER_RES.PID = in_pid AND PLAYER_RES.RID = res_id
					LIMIT 1;
				END IF;
				
				-- ���ٻ��Ƴ��г��ϸ���Ʒ�ĳ���
				IF in_num = res_total_num THEN
					DELETE FROM MARKET_RES
					WHERE MARKET_RES.ITEMID = in_itemid;
				ELSE
					UPDATE MARKET_RES 
					SET MARKET_RES.NUMBER = (res_total_num - in_num)
					WHERE MARKET_RES.ITEMID = in_itemid;
				END IF;
				
				-- �������ҵĽ����
				UPDATE PLAYER_INFO
				SET PLAYER_INFO.GOLD = PLAYER_INFO.GOLD + in_num * res_unit_price
				WHERE PLAYER_INFO.PID = in_pid;
			ELSE
				-- ��ҽ�Ҳ���
				SET t_error = 2;
			END IF;
		ELSE
			-- ���������Ʒ��Ŀ������������Ʒ����Ŀ
			SET t_error = 3;
		END IF;
		
	IF t_error = 1 THEN
		-- ���ݿ��쳣
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