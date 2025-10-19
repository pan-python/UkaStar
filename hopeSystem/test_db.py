# test_db.py
from config import Config
import mysql.connector
from mysql.connector import errorcode
import logging

logging.basicConfig(level=logging.DEBUG, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

def test_db_connection():
    config = Config()
    try:
        logger.debug("尝试连接数据库...")
        conn = mysql.connector.connect(
            host=config.MYSQL_HOST,
            port=config.MYSQL_PORT,
            user=config.MYSQL_USER,
            password=config.MYSQL_PASSWORD,
            database=config.MYSQL_DB,
            connection_timeout=5
        )
        logger.info("数据库连接成功！")
        
        cursor = conn.cursor()
        cursor.execute("SHOW TABLES")
        tables = cursor.fetchall()
        logger.info(f"数据库包含 {len(tables)} 张表")
        
        cursor.close()
        conn.close()
    except mysql.connector.Error as err:
        logger.error(f"数据库连接失败: {err}")
    except Exception as e:
        logger.error(f"发生未知错误: {str(e)}")

if __name__ == '__main__':
    test_db_connection()