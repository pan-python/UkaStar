# database.py
import mysql.connector
from mysql.connector import errorcode
from config import Config

db = None

def init_db(app):
    global db
    try:
        db = mysql.connector.connect(
            host=app.config['MYSQL_HOST'],
            port=app.config['MYSQL_PORT'],
            user=app.config['MYSQL_USER'],
            password=app.config['MYSQL_PASSWORD'],
            database=app.config['MYSQL_DB']
        )
        print("数据库连接成功")
    except mysql.connector.Error as err:
        if err.errno == errorcode.ER_ACCESS_DENIED_ERROR:
            print("数据库用户名或密码错误")
        elif err.errno == errorcode.ER_BAD_DB_ERROR:
            print("数据库不存在")
        else:
            print(f"数据库连接错误: {err}")

def get_db():
    # 每次请求都创建新连接
    try:
        db = mysql.connector.connect(
            host=Config.MYSQL_HOST,
            port=Config.MYSQL_PORT,
            user=Config.MYSQL_USER,
            password=Config.MYSQL_PASSWORD,
            database=Config.MYSQL_DB
        )
        return db
    except mysql.connector.Error as err:
        print(f"数据库连接失败: {err}")
        raise