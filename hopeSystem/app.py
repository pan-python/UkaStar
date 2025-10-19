# app.py
from flask import Flask, jsonify
from flask_cors import CORS
from routes import api_bp  # 确保导入蓝图
from database import init_db
from config import Config
import logging

app = Flask(__name__)
CORS(app)  # 允许跨域请求

# 配置日志
logging.basicConfig(level=logging.DEBUG, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

# 加载配置
app.config.from_object(Config)
logger.debug("配置加载完成")

# 注册蓝图
app.register_blueprint(api_bp)
logger.debug("API蓝图已注册")

@app.route('/')
def index():
    return "积分奖励管理系统API服务已启动"

if __name__ == '__main__':
    logger.debug("开始初始化数据库...")
    try:
        init_db(app)
        logger.debug("数据库初始化完成")
    except Exception as e:
        logger.error(f"数据库初始化失败: {str(e)}")
    
    logger.debug("启动Flask应用...")
    try:
        app.run(host='0.0.0.0', port=8080, debug=True)
        logger.debug("Flask应用已启动")
    except Exception as e:
        logger.error(f"Flask启动失败: {str(e)}")