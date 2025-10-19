import os
from dotenv import load_dotenv

load_dotenv()

class Config:
    # 数据库配置
    MYSQL_HOST = os.getenv('MYSQL_HOST', '192.168.2.104')
    MYSQL_PORT = int(os.getenv('MYSQL_PORT', 3306))
    MYSQL_USER = os.getenv('MYSQL_USER', 'root')
    MYSQL_PASSWORD = os.getenv('MYSQL_PASSWORD', '123456')
    MYSQL_DB = os.getenv('MYSQL_DB', 'hopesystem')
    
    # Flask配置
    SECRET_KEY = os.getenv('SECRET_KEY', 'your_secret_key_here')