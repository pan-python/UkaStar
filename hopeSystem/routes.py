from flask import Blueprint, request, jsonify,redirect,Flask, Response, stream_with_context
from database import get_db
from models import User, PointCategory, PointItem, DeductionCategory, DeductionItem, RewardItem, PointRecord
import mysql.connector
from datetime import datetime, timedelta
import json
import urllib.parse
import requests
import secrets
import os
import time
import random
import string
import hashlib
import easyocr
import tempfile
import base64

api_bp = Blueprint('api', __name__, url_prefix='/api')


# 公众号配置 (替换为你的实际信息)
APPID = 'wx2e0f9c9110e79d3c'
APPSECRET = 'cef009e75853d77e9d37273ad6ff351e'
REDIRECT_URI = 'https://yunze.mynatapp.cc/api/callback'  # 必须与公众号后台配置的域名一致
FRONTEND_URL = 'https://yunze.mynatapp.cc/static/auth_callback.html'

# 全局缓存（生产环境建议用Redis）
CACHE = {
    "access_token": None,
    "token_expire": 0,
    "jsapi_ticket": None,
    "ticket_expire": 0
}

def get_access_token():
    """获取 Access Token (带缓存机制)"""
    now = time.time()
    if CACHE["access_token"] and now < CACHE["token_expire"]:
        return CACHE["access_token"]
    
    url = f"https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid={APPID}&secret={APPSECRET}"
    res = requests.get(url).json()
    
    if "access_token" in res:
        CACHE["access_token"] = res["access_token"]
        CACHE["token_expire"] = now + 7000  # 提前过期(7200秒有效期)
        return res["access_token"]
    else:
        raise Exception(f"获取Token失败: {res}")

def get_jsapi_ticket():
    """获取 JSAPI Ticket (带缓存)"""
    now = time.time()
    if CACHE["jsapi_ticket"] and now < CACHE["ticket_expire"]:
        return CACHE["jsapi_ticket"]
    
    access_token = get_access_token()
    url = f"https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token={access_token}&type=jsapi"
    res = requests.get(url).json()
    
    if res.get("errcode") == 0:
        CACHE["jsapi_ticket"] = res["ticket"]
        CACHE["ticket_expire"] = now + 7000  # 提前过期
        return res["ticket"]
    else:
        raise Exception(f"获取Ticket失败: {res}")

def generate_signature(ticket, url):
    """生成签名"""
    nonce_str = ''.join(random.choices(string.ascii_letters + string.digits, k=16))
    timestamp = int(time.time())
    
    # 1. 按字典序拼接字符串
    raw_str = f"jsapi_ticket={ticket}&noncestr={nonce_str}&timestamp={timestamp}&url={url}"
    
    # 2. 进行SHA1加密
    signature = hashlib.sha1(raw_str.encode('utf-8')).hexdigest()
    return nonce_str, timestamp, signature

@api_bp.route('/get_wx_config', methods=['GET'])
def get_wx_config():
    try:
        # 从前端获取当前页面完整URL (需解码)
        current_url = request.args.get('url', '')
        if not current_url:
            return jsonify({"error": "缺少URL参数"}), 400
        
        # 核心步骤
        ticket = get_jsapi_ticket()
        noncestr, timestamp, signature = generate_signature(ticket, current_url)
        
        # 返回配置数据
        return jsonify({
            "appId": APPID,
            "nonceStr": noncestr,
            "timestamp": timestamp,
            "signature": signature,
            # 可选：需要使用的JS接口列表
            "jsApiList": ["updateAppMessageShareData", "chooseImage"]  
        })
    except Exception as e:
        return jsonify({"error": str(e)}), 500
        
@api_bp.route('/auth', methods=['GET'])
def wechat_auth():
    """返回微信授权URL给前端"""
    # 生成随机state防止CSRF攻击
    state = secrets.token_urlsafe(16)
    
    # 构造授权URL
    base_url = "https://open.weixin.qq.com/connect/oauth2/authorize"
    scope = "snsapi_userinfo"  # 需要用户信息
    
    # 重定向URL必须进行URL编码
    encoded_uri = urllib.parse.quote(REDIRECT_URI, safe='')
    
    auth_url = (f"{base_url}?appid={APPID}"
                f"&redirect_uri={encoded_uri}"
                f"&response_type=code"
                f"&scope={scope}"
                f"&state={state}#wechat_redirect")
    
    # 返回JSON给前端，而不是直接重定向
    return jsonify({
        'authUrl': auth_url,
        'state': state
    }), 200

@api_bp.route('/callback', methods=['GET'])
def wechat_callback():
    """微信授权回调处理"""
    # 获取微信返回的code和state
    code = request.args.get('code')
    state = request.args.get('state')
    
    
    # 验证state防止CSRF攻击
    # 实际应用中应该将state存储在会话或缓存中
    # if state != session.get('wechat_state'):
    #     return redirect(f"{FRONTEND_URL}?error=invalid_state")
    
    # 使用code换取access_token
    token_url = (f"https://api.weixin.qq.com/sns/oauth2/access_token"
                 f"?appid={APPID}"
                 f"&secret={APPSECRET}"
                 f"&code={code}"
                 f"&grant_type=authorization_code")
    
    try:
        response = requests.get(token_url)
        response.raise_for_status()
        token_data = response.json()
        
        
        access_token = token_data['access_token']
        openid = token_data['openid']
        
        # 获取用户信息
        user_info_url = (f"https://api.weixin.qq.com/sns/userinfo"
                         f"?access_token={access_token}"
                         f"&openid={openid}"
                         f"&lang=zh_CN")
        
        user_response = requests.get(user_info_url)
        user_response.raise_for_status()
        user_data = user_response.json()
        
        
        # 存储用户信息或创建会话
        # 这里简化处理，实际应该保存到数据库
        # 重定向回前端页面，携带用户信息
        user_info = {
            'openid': user_data.get('openid'),
            'nickname': user_data.get('nickname'),
            'avatar': user_data.get('headimgurl'),
            'city': user_data.get('city'),
            'province': user_data.get('province')
        }
        
        # URL编码用户信息
        encoded_user_info = urllib.parse.quote(json.dumps(user_info))
        return redirect(f"{FRONTEND_URL}?auth_success=1&user={encoded_user_info}")
    
    except Exception as e:
        return redirect(f"{REDIRECT_URI}?error=exception&message={urllib.parse.quote(str(e))}")
REDIRECT_URI2 = 'https://yunze.mynatapp.cc/api/callback2'  # 必须与公众号后台配置的域名一致
FRONTEND_URL2 = 'https://yunze.mynatapp.cc/static/register_callback.html'
@api_bp.route('/auth2', methods=['GET'])
def wechat_auth2():
    """返回微信授权URL给前端"""
    # 生成随机state防止CSRF攻击
    state = secrets.token_urlsafe(16)
    
    # 构造授权URL
    base_url = "https://open.weixin.qq.com/connect/oauth2/authorize"
    scope = "snsapi_userinfo"  # 需要用户信息
    
    # 重定向URL必须进行URL编码
    encoded_uri = urllib.parse.quote(REDIRECT_URI, safe='')
    
    auth_url = (f"{base_url}?appid={APPID}"
                f"&redirect_uri={encoded_uri}"
                f"&response_type=code"
                f"&scope={scope}"
                f"&state={state}#wechat_redirect")
    
    # 返回JSON给前端，而不是直接重定向
    return jsonify({
        'authUrl': auth_url,
        'state': state
    }), 200

@api_bp.route('/callback2', methods=['GET'])
def wechat_callback2():
    """微信授权回调处理"""
    # 获取微信返回的code和state
    code = request.args.get('code')
    state = request.args.get('state')
    
    
    # 验证state防止CSRF攻击
    # 实际应用中应该将state存储在会话或缓存中
    # if state != session.get('wechat_state'):
    #     return redirect(f"{FRONTEND_URL}?error=invalid_state")
    
    # 使用code换取access_token
    token_url = (f"https://api.weixin.qq.com/sns/oauth2/access_token"
                 f"?appid={APPID}"
                 f"&secret={APPSECRET}"
                 f"&code={code}"
                 f"&grant_type=authorization_code")
    
    try:
        response = requests.get(token_url)
        response.raise_for_status()
        token_data = response.json()
        
        
        access_token = token_data['access_token']
        openid = token_data['openid']
        
        # 获取用户信息
        user_info_url = (f"https://api.weixin.qq.com/sns/userinfo"
                         f"?access_token={access_token}"
                         f"&openid={openid}"
                         f"&lang=zh_CN")
        
        user_response = requests.get(user_info_url)
        user_response.raise_for_status()
        user_data = user_response.json()
        
        
        # 存储用户信息或创建会话
        # 这里简化处理，实际应该保存到数据库
        # 重定向回前端页面，携带用户信息
        user_info = {
            'openid': user_data.get('openid'),
            'nickname': user_data.get('nickname'),
            'avatar': user_data.get('headimgurl'),
            'city': user_data.get('city'),
            'province': user_data.get('province')
        }
        
        # URL编码用户信息
        encoded_user_info = urllib.parse.quote(json.dumps(user_info))
        return redirect(f"{FRONTEND_URL2}?auth_success=1&user={encoded_user_info}")
    
    except Exception as e:
        return redirect(f"{REDIRECT_URI2}?error=exception&message={urllib.parse.quote(str(e))}")
# 添加加分记录
@api_bp.route('/register', methods=['POST'])
def register():
    data = request.json
    name = data.get('name')
    grade = data.get('grade')
    wechat_openid = data.get('wechat_openid')
    
    if not name:
        return jsonify({'error': '缺少用户姓名'}), 400
    if not wechat_openid:
        return jsonify({'error': '缺少微信用户openid'}), 400
    if not grade:
        return jsonify({'error': '缺少必要的参数'}), 400
    
    db = get_db()
    cursor = db.cursor()
    
    try:
        # 注册用户
        cursor.execute(
            "INSERT INTO sys_user (name, grade, total_points, wechat_openid) "
            "VALUES (%s, %s, %s, %s)",
            (
                name, 
                grade,
                0, 
                wechat_openid
            )
        )
        
        db.commit()
        return jsonify({'message': '注册成功'}), 200
    
    except mysql.connector.Error as err:
        db.rollback()
        return jsonify({'error': f"数据库错误: {err}"}), 500
    finally:
        cursor.close()
# 获取所有用户
@api_bp.route('/users', methods=['GET'])
def get_all_users():
    db = get_db()
    cursor = db.cursor(dictionary=True)
    try:
        cursor.execute("SELECT * FROM sys_user")
        users = cursor.fetchall()
        return jsonify(users), 200
    except mysql.connector.Error as err:
        return jsonify({'error': f"数据库错误: {err}"}), 500
    finally:
        cursor.close()

# 用户相关接口
@api_bp.route('/users/<int:user_id>', methods=['GET'])
def get_user(user_id):
    db = get_db()
    cursor = db.cursor(dictionary=True)
    try:
        cursor.execute("SELECT * FROM sys_user WHERE id = %s", (user_id,))
        user = cursor.fetchone()
        if user:
            return jsonify(user), 200
        else:
            return jsonify({'error': '用户不存在'}), 404
    except mysql.connector.Error as err:
        return jsonify({'error': f"数据库错误: {err}"}), 500
    finally:
        cursor.close()

# 根据微信公众号用户id查询用户信息
@api_bp.route('/fetchUserByOpenid/<string:open_id>', methods=['GET'])
def fetchUserByOpenid(open_id):
    db = get_db()
    cursor = db.cursor(dictionary=True)
    try:
        cursor.execute("SELECT * FROM sys_user WHERE wechat_openid LIKE %s", (f'%{open_id}%',))
        user = cursor.fetchone()
        if user:
            return jsonify(user), 200
        else:
            return jsonify({'error': '用户不存在'}), 404
    except mysql.connector.Error as err:
        return jsonify({'error': f"数据库错误: {err}"}), 500
    finally:
        cursor.close()
        
# 获取加分类别和项目
@api_bp.route('/point_categories_with_items', methods=['GET'])
def get_point_categories_with_items():
    user_id = request.args.get('user_id')
    db = get_db()
    cursor = db.cursor(dictionary=True)
    try:
        # 获取所有加分类别
        cursor.execute("SELECT * FROM point_categories where flag=0 ")
        categories = cursor.fetchall()
        
        # 为每个类别获取对应的加分项
        for category in categories:
            cursor.execute(
                "SELECT id, name, description, points, category_id  "
                "FROM point_items WHERE user_id = %s AND category_id = %s "
                "UNION ALL "
                "SELECT id, name, description, points, category_id "
                "FROM point_items WHERE user_id is null AND category_id = %s",
                ( user_id,category['id'],category['id'])
            )
            items = cursor.fetchall()
            category['items'] = items
        
        return jsonify(categories), 200
    except mysql.connector.Error as err:
        return jsonify({'error': f"数据库错误: {err}"}), 500
    finally:
        cursor.close()

# 修改现有积分项的获取接口，合并系统默认项和用户自定义项
@api_bp.route('/point_items', methods=['GET'])
def get_point_items():
    user_id = request.args.get('user_id')
    db = get_db()
    cursor = db.cursor(dictionary=True)
    
    try:
        # 获取系统默认项
        cursor.execute("SELECT * FROM point_items WHERE user_id IS NULL")
        items = cursor.fetchall()
        
        # 获取用户自定义项
        if user_id:
            cursor.execute(
                "SELECT id, name, description, points, category_id as category "
                "FROM point_items WHERE user_id = %s "
                "UNION ALL "
                "SELECT id, name, description, points, category "
                "FROM point_items WHERE user_id = %s AND item_type = 'point'",
                (user_id, user_id)
            )
            custom_items = cursor.fetchall()
            items.extend(custom_items)
        
        return jsonify(items), 200
    except mysql.connector.Error as err:
        return jsonify({'error': f"数据库错误: {err}"}), 500
    finally:
        cursor.close()

# 获取减分类别和项目
@api_bp.route('/deduction_categories_with_items', methods=['GET'])
def get_deduction_categories_with_items():
    user_id = request.args.get('user_id')
    db = get_db()
    cursor = db.cursor(dictionary=True)
    try:
        # 获取所有减分类别
        cursor.execute("SELECT * FROM point_categories  where flag=1")
        categories = cursor.fetchall()
        
        # 为每个类别获取对应的减分项
        for category in categories:
            cursor.execute("SELECT id, name, description, points, category_id  "
                "FROM point_items WHERE user_id = %s AND category_id = %s "
                "UNION ALL "
                "SELECT id, name, description, points, category_id "
                "FROM point_items WHERE user_id is null AND category_id = %s",
                ( user_id,category['id'],category['id']))
            items = cursor.fetchall()
            category['items'] = items
        
        return jsonify(categories), 200
    except mysql.connector.Error as err:
        return jsonify({'error': f"数据库错误: {err}"}), 500
    finally:
        cursor.close()
# 获取加分类别
@api_bp.route('/point_categories', methods=['GET'])
def get_point_categories():
    db = get_db()
    cursor = db.cursor(dictionary=True)
    try:
        cursor.execute("SELECT * FROM point_categories where flag=0 ")
        categories = cursor.fetchall()
        return jsonify(categories), 200
    except mysql.connector.Error as err:
        return jsonify({'error': f"数据库错误: {err}"}), 500
    finally:
        cursor.close()



# 获取减分类别
@api_bp.route('/deduction_categories', methods=['GET'])
def get_deduction_categories():
    db = get_db()
    cursor = db.cursor(dictionary=True)
    try:
        cursor.execute("SELECT * FROM point_categories where flag=1 ")
        categories = cursor.fetchall()
        return jsonify(categories), 200
    except mysql.connector.Error as err:
        return jsonify({'error': f"数据库错误: {err}"}), 500
    finally:
        cursor.close()


# 添加加分记录
@api_bp.route('/add_points', methods=['POST'])
def add_points():
    data = request.json
    user_id = data.get('user_id')
    item_id = data.get('item_id')
    custom_name = data.get('custom_name')
    custom_points = data.get('custom_points')
    description = data.get('description', '')
    
    if not user_id:
        return jsonify({'error': '缺少用户ID'}), 400
    
    db = get_db()
    cursor = db.cursor()
    
    try:
        # 获取用户当前积分
        cursor.execute("SELECT total_points FROM sys_user WHERE id = %s FOR UPDATE", (user_id,))
        user = cursor.fetchone()
        if not user:
            return jsonify({'error': '用户不存在'}), 404
        
        current_points = user[0]
        
        # 处理加分逻辑
        if item_id:
            # 使用预设加分项
            cursor.execute("SELECT points, name FROM point_items WHERE id = %s", (item_id,))
            item = cursor.fetchone()
            if not item:
                return jsonify({'error': '加分项不存在'}), 404
            points = item[0]
            # 如果没有提供描述，使用默认描述
            if not description:
                description = item[1]  # 使用加分项名称作为描述
        elif custom_name and custom_points is not None:
            # 使用自定义加分项
            points = custom_points
            # 如果没有提供描述，使用自定义名称作为描述
            if not description:
                description = custom_name
        else:
            return jsonify({'error': '需要提供item_id或custom_name和custom_points'}), 400
        
        # 更新用户积分
        new_points = current_points + points
        cursor.execute("UPDATE sys_user SET total_points = %s WHERE id = %s", (new_points, user_id))
        
        # 获取当前时间
        current_time = datetime.now()
        
        # 创建记录 - 添加created_at字段
        cursor.execute(
            "INSERT INTO point_records (user_id, type, item_id, custom_name, custom_points, points, description, created_at) "
            "VALUES (%s, %s, %s, %s, %s, %s, %s, %s)",
            (
                user_id, 
                1,  # 类型1表示加分
                item_id if item_id else None,
                custom_name if custom_name else None, 
                custom_points if custom_points else None, 
                points, 
                description,
                current_time  # 添加当前时间
            )
        )
        
        # 更新每日积分统计
        today = current_time.date()
        cursor.execute(
            "INSERT INTO daily_points (user_id, date, added_points, net_points) "
            "VALUES (%s, %s, %s, %s) "
            "ON DUPLICATE KEY UPDATE added_points = added_points + VALUES(added_points), "
            "net_points = net_points + VALUES(net_points)",
            (user_id, today, points, points)
        )
        
        db.commit()
        return jsonify({'message': '加分成功', 'new_points': new_points, 'points': points}), 200
    
    except mysql.connector.Error as err:
        db.rollback()
        return jsonify({'error': f"数据库错误: {err}"}), 500
    finally:
        cursor.close()

# 添加减分记录
@api_bp.route('/deduct_points', methods=['POST'])
def deduct_points():
    data = request.json
    user_id = data.get('user_id')
    item_id = data.get('item_id')
    custom_name = data.get('custom_name')
    custom_points = data.get('custom_points')
    description = data.get('description', '')
    
    if not user_id:
        return jsonify({'error': '缺少用户ID'}), 400
    
    db = get_db()
    cursor = db.cursor()
    
    try:
        # 获取用户当前积分
        cursor.execute("SELECT total_points FROM sys_user WHERE id = %s FOR UPDATE", (user_id,))
        user = cursor.fetchone()
        if not user:
            return jsonify({'error': '用户不存在'}), 404
        
        current_points = user[0]
        
        # 处理减分逻辑
        if item_id:
            # 使用预设减分项
            cursor.execute("SELECT points, name FROM point_items WHERE id = %s", (item_id,))
            item = cursor.fetchone()
            if not item:
                return jsonify({'error': '减分项不存在'}), 404
            points = -abs(item[0])  # 确保为负值
            if not description:
                description = item[1]
        elif custom_name and custom_points is not None:
            # 使用自定义减分项 - 确保为负值
            points = -abs(int(custom_points))  # 转换为整数并取绝对值后取负
            if not description:
                description = custom_name
        else:
            return jsonify({'error': '需要提供item_id或custom_name和custom_points'}), 400
        
        # 检查积分是否足够
        if current_points + points < 0:
            return jsonify({'error': '积分不足，无法减分'}), 400
        
        # 更新用户积分（执行减法）
        new_points = current_points + points  # 因为points是负值，所以实际是减法
        cursor.execute("UPDATE sys_user SET total_points = %s WHERE id = %s", (new_points, user_id))
        
        # 创建记录
        current_time = datetime.now()
        cursor.execute(
            "INSERT INTO point_records (user_id, type, item_id, custom_name, custom_points, points, description, created_at) "
            "VALUES (%s, %s, %s, %s, %s, %s, %s, %s)",
            (
                user_id, 
                2,  # 类型2表示减分
                item_id if item_id else None,
                custom_name if custom_name else None,
                # 修复：存储原始自定义值（正数），而不是绝对值
                custom_points if custom_points else None,  # 修改这里
                points,  # 存储负值
                description,
                current_time
            )
        )
        
        # 更新每日积分统计
        today = current_time.date()
        cursor.execute(
            "INSERT INTO daily_points (user_id, date, deducted_points, net_points) "
            "VALUES (%s, %s, %s, %s) "
            "ON DUPLICATE KEY UPDATE deducted_points = deducted_points + VALUES(deducted_points), "
            "net_points = net_points + VALUES(net_points)",
            (user_id, today, abs(points), points)
        )
        
        db.commit()
        return jsonify({'message': '减分成功', 'new_points': new_points}), 200
    
    except mysql.connector.Error as err:
        db.rollback()
        return jsonify({'error': f"数据库错误: {err}"}), 500
    except ValueError:
        return jsonify({'error': '无效的分值格式'}), 400
    finally:
        cursor.close()

# 兑换奖励
@api_bp.route('/exchange_reward', methods=['POST'])
def exchange_reward():
    data = request.json
    user_id = data.get('user_id')
    item_id = data.get('item_id')
    custom_name = data.get('custom_name')
    custom_points = data.get('custom_points')
    description = data.get('description', '')
    
    if not user_id:
        return jsonify({'error': '缺少用户ID'}), 400
    
    db = get_db()
    cursor = db.cursor()
    
    try:
        # 获取用户当前积分
        cursor.execute("SELECT total_points FROM sys_user WHERE id = %s FOR UPDATE", (user_id,))
        user = cursor.fetchone()
        if not user:
            return jsonify({'error': '用户不存在'}), 404
        
        current_points = user[0]
        
        # 处理兑换逻辑
        if item_id:
            # 使用预设兑换项
            cursor.execute("SELECT points, is_custom,description FROM reward_items WHERE id = %s", (item_id,))
            item = cursor.fetchone()
            if not item:
                return jsonify({'error': '兑换项不存在'}), 404
            points = -abs(item[0])  # 负值
            description = item[2]
            if item[1]:  # 如果是自定义兑换
                if not custom_name or custom_points is None:
                    return jsonify({'error': '自定义兑换需要提供custom_name和custom_points'}), 400
                points = -abs(custom_points)  # 使用自定义积分值
        elif custom_name and custom_points is not None:
            # 使用自定义兑换项
            points = -abs(custom_points)  # 负值
            description = custom_name
        else:
            return jsonify({'error': '需要提供item_id或custom_name和custom_points'}), 400
        
        # 检查积分是否足够
        if current_points < abs(points):
            return jsonify({'error': '积分不足'}), 400
        
        # 更新用户积分
        new_points = current_points + points
        cursor.execute("UPDATE sys_user SET total_points = %s WHERE id = %s", (new_points, user_id))
        
        # 创建记录
        current_time = datetime.now()
        cursor.execute(
            "INSERT INTO point_records (user_id, type, item_id, custom_name, custom_points, points, description, status, created_at) "
            "VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)",
            (user_id, 3, item_id, custom_name, abs(points), points, description, 1,current_time)
        )
        
        db.commit()
        return jsonify({'message': '兑换成功', 'new_points': new_points}), 200
    
    except mysql.connector.Error as err:
        db.rollback()
        return jsonify({'error': f"数据库错误: {err}"}), 500
    finally:
        cursor.close()

# 获取用户今日记录 (修复不显示数据问题)
@api_bp.route('/records/today/<int:user_id>', methods=['GET'])
def get_today_records(user_id):
    db = get_db()
    cursor = db.cursor(dictionary=True)
    today = datetime.now().date()
    
    try:
        # 更可靠的查询，确保能获取到数据
        cursor.execute(
            """
            SELECT 
                pr.id,
                pr.user_id,
                pr.type,
                pr.points AS raw_points,
                pr.description,
                pr.created_at,
                pr.custom_name,
                pr.custom_points,
                pi.name AS point_item_name,
                ri.name AS reward_item_name
            FROM point_records pr
            LEFT JOIN point_items pi ON pr.item_id = pi.id AND pr.type IN (1, 2)
            LEFT JOIN reward_items ri ON pr.item_id = ri.id AND pr.type = 3
            WHERE pr.user_id = %s 
                AND DATE(pr.created_at) = %s
            ORDER BY pr.created_at DESC
            """,
            (user_id, today)
        )
        records = cursor.fetchall()
        
        # 调试信息：打印查询到的记录数量
        print(f"Found {len(records)} records for user {user_id} on {today}")
        
        # 格式化记录数据 - 确保即使数据为空也能返回结构化结果
        formatted_records = []
        for record in records:
            # 确定项目名称
            if record.get('custom_name'):
                item_name = record['custom_name']
            elif record.get('point_item_name'):
                item_name = record['point_item_name']
            elif record.get('reward_item_name'):
                item_name = record['reward_item_name']
            else:
                item_name = record.get('description', '自定义操作')
            
            # 确定显示点数
            raw_points = record.get('raw_points', 0)
            if record.get('type') == 1:  # 加分
                points = f"+{abs(raw_points)}"
            else:  # 减分或兑换
                points = f"-{abs(raw_points)}"
            
            # 格式化时间
            created_at = record.get('created_at')
            formatted_time = created_at.strftime("%H:%M") if created_at else "未知时间"
            
            formatted_records.append({
                "id": record.get('id'),
                "type": record.get('type'),
                "type_name": "加分" if record.get('type') == 1 else "减分" if record.get('type') == 2 else "兑换",
                "type_class": "success" if record.get('type') == 1 else "danger" if record.get('type') == 2 else "warning",
                "item_name": item_name,
                "points": points,
                "description": record.get('description', ''),
                "time": formatted_time
            })
        
        # 添加今日统计信息
        cursor.execute(
            "SELECT "
            "COALESCE(SUM(CASE WHEN type=1 THEN points ELSE 0 END), 0) AS added, "
            "COALESCE(SUM(CASE WHEN type=2 THEN points ELSE 0 END), 0) AS deducted, "
            "COALESCE(SUM(CASE WHEN type=3 THEN points ELSE 0 END), 0) AS exchanged "
            "FROM point_records "
            "WHERE user_id = %s AND DATE(created_at) = %s",
            (user_id, today)
        )
        stats = cursor.fetchone() or {}
        
        # 返回结构化的结果，即使没有记录
        return jsonify({
            "success": True,
            "date": today.isoformat(),
            "stats": {
                "added": stats.get('added', 0),
                "deducted": stats.get('deducted', 0),
                "exchanged": stats.get('exchanged', 0),
                "net_change": stats.get('added', 0) + stats.get('deducted', 0) + stats.get('exchanged', 0)
            },
            "records": formatted_records
        }), 200
        
    except mysql.connector.Error as err:
        # 返回详细的错误信息
        return jsonify({
            "success": False,
            "error": f"数据库错误: {err}",
            "sql_state": err.sqlstate if hasattr(err, 'sqlstate') else None
        }), 500
    except Exception as e:
        # 捕获其他可能的异常
        return jsonify({
            "success": False,
            "error": f"服务器错误: {str(e)}"
        }), 500
    finally:
        cursor.close()

# 获取用户历史记录 (修复不显示数据问题)
@api_bp.route('/records/history/<int:user_id>', methods=['GET'])
def get_history_records(user_id):
    db = get_db()
    cursor = db.cursor(dictionary=True)
    
    # 获取分页参数
    page = request.args.get('page', default=1, type=int)
    per_page = request.args.get('per_page', default=10000, type=int)
    offset = (page - 1) * per_page
    
    try:
        # 获取总记录数
        cursor.execute("SELECT COUNT(*) as total FROM point_records WHERE user_id = %s", (user_id,))
        total_result = cursor.fetchone()
        total_records = total_result['total'] if total_result else 0
        
        # 获取历史记录
        cursor.execute(
            """
            SELECT 
                pr.id,
                pr.user_id,
                pr.type,
                pr.points AS raw_points,
                pr.description,
                pr.created_at,
                pr.custom_name,
                pr.custom_points,
                pi.name AS point_item_name,
                ri.name AS reward_item_name
            FROM point_records pr
            LEFT JOIN point_items pi ON pr.item_id = pi.id AND pr.type IN (1, 2)
            LEFT JOIN reward_items ri ON pr.item_id = ri.id AND pr.type = 3
            WHERE pr.user_id = %s
            ORDER BY pr.created_at DESC
            LIMIT %s OFFSET %s
            """,
            (user_id, per_page, offset)
        )
        records = cursor.fetchall()
        
        # 调试信息
        print(f"Found {len(records)} history records for user {user_id}")
        
        # 格式化记录数据
        formatted_records = []
        for record in records:
            # 确定项目名称
            if record.get('custom_name'):
                item_name = record['custom_name']
            elif record.get('point_item_name'):
                item_name = record['point_item_name']
            elif record.get('reward_item_name'):
                item_name = record['reward_item_name']
            else:
                item_name = record.get('description', '自定义操作')
            
            # 确定显示点数
            raw_points = record.get('raw_points', 0)
            if record.get('type') == 1:  # 加分
                points = f"+{abs(raw_points)}"
            else:  # 减分或兑换
                points = f"-{abs(raw_points)}"
            
            # 格式化完整时间
            created_at = record.get('created_at')
            formatted_date = created_at.strftime("%Y-%m-%d %H:%M") if created_at else "未知时间"
            
            formatted_records.append({
                "id": record.get('id'),
                "type": record.get('type'),
                "type_name": "加分" if record.get('type') == 1 else "减分" if record.get('type') == 2 else "兑换",
                "item_name": item_name,
                "points": points,
                "description": record.get('description', ''),
                "date": formatted_date
            })
        
        # 返回结构化的结果
        return jsonify({
            "success": True,
            "records": formatted_records,
            "pagination": {
                "page": page,
                "per_page": per_page,
                "total_records": total_records,
                "total_pages": max(1, (total_records + per_page - 1) // per_page)
            }
        }), 200
        
    except mysql.connector.Error as err:
        return jsonify({
            "success": False,
            "error": f"数据库错误: {err}",
            "sql_state": err.sqlstate if hasattr(err, 'sqlstate') else None
        }), 500
    except Exception as e:
        return jsonify({
            "success": False,
            "error": f"服务器错误: {str(e)}"
        }), 500
    finally:
        cursor.close()

# 获取积分图表数据 (修复版)
@api_bp.route('/chart_data/<int:user_id>', methods=['GET'])
def get_chart_data(user_id):
    db = get_db()
    cursor = db.cursor(dictionary=True)
    
    try:
        # 获取最近7天的日期
        end_date = datetime.now().date()
        start_date = end_date - timedelta(days=6)
        
        # 查询最近7天的积分变化
        cursor.execute(
            """
            SELECT 
                DATE(created_at) AS date,
                COALESCE(SUM(CASE WHEN type = 1 THEN points ELSE 0 END), 0) AS added,
                COALESCE(SUM(CASE WHEN type = 2 THEN points ELSE 0 END), 0) AS deducted
            FROM point_records
            WHERE user_id = %s AND created_at BETWEEN %s AND %s + INTERVAL 1 DAY
            GROUP BY DATE(created_at)
            ORDER BY DATE(created_at)
            """,
            (user_id, start_date, end_date)
        )
        
        # 获取结果
        raw_data = cursor.fetchall()
        chart_data = []
        
        # 确保返回7天的数据，即使某些天没有记录
        current_date = start_date
        while current_date <= end_date:
            # 查找对应日期的数据
            found = False
            for row in raw_data:
                if row['date'] == current_date:
                    chart_data.append({
                        "date": current_date.isoformat(),
                        "added": row['added'],
                        "deducted": row['deducted']
                    })
                    found = True
                    break
            
            # 如果没有找到当天的记录，添加空数据
            if not found:
                chart_data.append({
                    "date": current_date.isoformat(),
                    "added": 0,
                    "deducted": 0
                })
            
            current_date += timedelta(days=1)
        
        return jsonify({
            "success": True,
            "chart_data": chart_data
        }), 200
        
    except mysql.connector.Error as err:
        return jsonify({
            "success": False,
            "error": f"数据库错误: {err}",
            "sql_state": err.sqlstate if hasattr(err, 'sqlstate') else None
        }), 500
    finally:
        cursor.close()

# 获取用户积分统计
@api_bp.route('/summary/<int:user_id>', methods=['GET'])
def get_user_summary(user_id):
    db = get_db()
    cursor = db.cursor()
    
    try:
        # 获取今日加分总和
        cursor.execute(
            "SELECT COALESCE(SUM(points),0) FROM point_records "
            "WHERE user_id = %s AND type = 1 AND DATE(created_at) = CURDATE()",
            (user_id,)
        )
        today_added = cursor.fetchone()[0]  # 读取结果
        
        # 获取今日减分总和
        cursor.execute(
            "SELECT COALESCE(SUM(points),0) FROM point_records "
            "WHERE user_id = %s AND type = 2 AND DATE(created_at) = CURDATE()",
            (user_id,)
        )
        today_deducted = cursor.fetchone()[0]  # 读取结果
        
        # 获取最近记录时间
        cursor.execute(
            "SELECT MAX(created_at) FROM point_records "
            "WHERE user_id = %s AND DATE(created_at) = CURDATE()",
            (user_id,)
        )
        last_record = cursor.fetchone()[0]  # 读取结果
        
        # 获取今日记录
        cursor.execute(
            "SELECT description, points, created_at FROM point_records "
            "WHERE user_id = %s AND DATE(created_at) = CURDATE() "
            "ORDER BY created_at DESC  LIMIT 5",
            (user_id,)
        )
        today_records = []
        for record in cursor.fetchall():  # 读取所有结果
            today_records.append({
                "description": record[0],
                "points": record[1],
                "created_at": record[2].isoformat() if record[2] else None
            })
        
        return jsonify({
            "today_added": today_added,
            "today_deducted": today_deducted,
            "last_record": last_record.isoformat() if last_record else None,
            "today_records": today_records
        }), 200
        
    except mysql.connector.Error as err:
        return jsonify({'error': f"数据库错误: {err}"}), 500
    finally:
        cursor.close()  # 确保在读取所有结果后关闭游标


@api_bp.route('/chart_data/<int:user_id>', methods=['GET'])
def get_chart_data2(user_id):
    db = get_db()
    cursor = db.cursor()
    
    try:
        # 获取最近7天的日期
        end_date = datetime.now().date()
        start_date = end_date - timedelta(days=6)
        
        # 查询最近7天的积分变化
        cursor.execute(
            "SELECT DATE(created_at) AS date, "
            "SUM(CASE WHEN type = 1 THEN points ELSE 0 END) AS added, "
            "SUM(CASE WHEN type = 2 THEN points ELSE 0 END) AS deducted "
            "FROM point_records "
            "WHERE user_id = %s AND created_at BETWEEN %s AND %s "
            "GROUP BY DATE(created_at) "
            "ORDER BY DATE(created_at)",
            (user_id, start_date, end_date)
        )
        
        # 获取结果并转换为字典
        raw_data = cursor.fetchall()
        chart_data = []
        
        # 确保返回7天的数据，即使某些天没有记录
        current_date = start_date
        while current_date <= end_date:
            # 查找对应日期的数据
            found = False
            for row in raw_data:
                if row[0] == current_date:
                    chart_data.append({
                        "date": current_date.isoformat(),
                        "added": row[1] or 0,
                        "deducted": row[2] or 0
                    })
                    found = True
                    break
            
            # 如果没有找到当天的记录，添加空数据
            if not found:
                chart_data.append({
                    "date": current_date.isoformat(),
                    "added": 0,
                    "deducted": 0
                })
            
            current_date += timedelta(days=1)
        
        return jsonify(chart_data), 200
        
    except mysql.connector.Error as err:
        return jsonify({'error': f"数据库错误: {err}"}), 500
    finally:
        cursor.close()
        
# 获取用户自定义积分项
@api_bp.route('/custom_items', methods=['GET'])
def get_custom_items():
    user_id = request.args.get('user_id')
    item_type = request.args.get('item_type')
    
    if not user_id or not item_type:
        return jsonify({'error': '缺少必要参数'}), 400
    
    db = get_db()
    cursor = db.cursor(dictionary=True)
    
    try:
        cursor.execute(
            "SELECT a.* FROM point_items a inner join point_categories b on a.category_id=b.id WHERE  a.user_id = %s  AND b.flag=%s ",
            (user_id, item_type)
        )
        items = cursor.fetchall()
        return jsonify(items), 200
    except mysql.connector.Error as err:
        return jsonify({'error': f"数据库错误: {err}"}), 500
    finally:
        cursor.close()
# 获取用户自定义兑换项
@api_bp.route('/user_reward_items', methods=['GET'])
def get_user_reward_items():
    user_id = request.args.get('user_id')
    
    if not user_id :
        return jsonify({'error': '缺少必要参数'}), 400
    
    db = get_db()
    cursor = db.cursor(dictionary=True)
    
    try:
        cursor.execute(
            "SELECT a.* FROM reward_items a  WHERE  a.user_id = %s  ",
            (user_id,)
        )
        items = cursor.fetchall()
        return jsonify(items), 200
    except mysql.connector.Error as err:
        return jsonify({'error': f"数据库错误: {err}"}), 500
    finally:
        cursor.close()

# 添加/更新自定义积分项
@api_bp.route('/custom_items', methods=['POST'])
def save_custom_item():
    data = request.json
    user_id = data.get('user_id')
    item_id = data.get('id')  # 更新时提供
    name = data.get('name')
    description = data.get('description')
    points = data.get('points')
    category = data.get('category_id')
    
    # 验证必要参数
    if not user_id or not name or points is None:
        return jsonify({'error': '缺少必要参数'}), 400
    
    db = get_db()
    cursor = db.cursor()
    
    try:
        if item_id:
            # 更新现有项
            cursor.execute(
                "UPDATE point_items SET name = %s, description = %s, points = %s, category_id = %s "
                "WHERE id = %s AND user_id = %s",
                (name, description, points, category, item_id, user_id)
            )
            if cursor.rowcount == 0:
                return jsonify({'error': '未找到可更新的项目'}), 404
        else:
            # 添加新项
            cursor.execute(
                "INSERT INTO point_items (user_id,  name, description, points, category_id) "
                "VALUES (%s, %s, %s, %s, %s)",
                (user_id, name, description, points, category)
            )
            item_id = cursor.lastrowid
        
        db.commit()
        return jsonify({'success': True, 'item_id': item_id}), 200
    
    except mysql.connector.Error as err:
        db.rollback()
        return jsonify({'error': f"数据库错误: {err}"}), 500
    finally:
        cursor.close()

# 删除自定义积分项
@api_bp.route('/custom_items/<int:item_id>', methods=['DELETE'])
def delete_custom_item(item_id):
    user_id = request.args.get('user_id')
    
    if not user_id:
        return jsonify({'error': '缺少用户ID'}), 400
    
    db = get_db()
    cursor = db.cursor()
    
    try:
        cursor.execute(
            "DELETE FROM point_items WHERE id = %s AND user_id = %s",
            (item_id, user_id)
        )
        
        if cursor.rowcount == 0:
            return jsonify({'error': '未找到可删除的项目'}), 404
        
        db.commit()
        return jsonify({'success': True}), 200
    
    except mysql.connector.Error as err:
        db.rollback()
        return jsonify({'error': f"数据库错误: {err}"}), 500
    finally:
        cursor.close()
 
# 添加/更新自定义积分项
@api_bp.route('/custom_reward_items', methods=['POST'])
def save_custom_reward_items():
    data = request.json
    user_id = data.get('user_id')
    item_id = data.get('id')  # 更新时提供
    name = data.get('name')
    description = data.get('description')
    points = data.get('points')
    
    # 验证必要参数
    if not user_id or not name or points is None:
        return jsonify({'error': '缺少必要参数'}), 400
    
    db = get_db()
    cursor = db.cursor()
    
    try:
        if item_id:
            # 更新现有项
            cursor.execute(
                "UPDATE reward_items SET name = %s, description = %s, points = %s "
                "WHERE id = %s AND user_id = %s",
                (name, description, points, item_id, user_id)
            )
            if cursor.rowcount == 0:
                return jsonify({'error': '未找到可更新的项目'}), 404
        else:
            # 添加新项
            cursor.execute(
                "INSERT INTO reward_items (user_id,  name, description, points, is_custom) "
                "VALUES (%s, %s, %s, %s, %s)",
                (user_id, name, description, points, 0)
            )
            item_id = cursor.lastrowid
        
        db.commit()
        return jsonify({'success': True, 'item_id': item_id}), 200
    
    except mysql.connector.Error as err:
        db.rollback()
        return jsonify({'error': f"数据库错误: {err}"}), 500
    finally:
        cursor.close()

# 删除自定义积分项
@api_bp.route('/custom_reward_items/<int:item_id>', methods=['DELETE'])
def delete_custom_reward_items(item_id):
    user_id = request.args.get('user_id')
    
    if not user_id:
        return jsonify({'error': '缺少用户ID'}), 400
    
    db = get_db()
    cursor = db.cursor()
    
    try:
        cursor.execute(
            "DELETE FROM reward_items WHERE id = %s AND user_id = %s",
            (item_id, user_id)
        )
        
        if cursor.rowcount == 0:
            return jsonify({'error': '未找到可删除的项目'}), 404
        
        db.commit()
        return jsonify({'success': True}), 200
    
    except mysql.connector.Error as err:
        db.rollback()
        return jsonify({'error': f"数据库错误: {err}"}), 500
    finally:
        cursor.close()


# 同样修改减分项和兑换项接口
@api_bp.route('/deduction_items', methods=['GET'])
def get_deduction_items():
    user_id = request.args.get('user_id')
    db = get_db()
    cursor = db.cursor(dictionary=True)
    
    try:
        # 获取系统默认项
        cursor.execute("SELECT * FROM point_items WHERE user_id IS NULL")
        items = cursor.fetchall()
        
        # 获取用户自定义项
        if user_id:
            cursor.execute(
                "SELECT id, name, description, points, category_id as category "
                "FROM point_items WHERE user_id = %s "
                "UNION ALL "
                "SELECT id, name, description, points, category "
                "FROM point_items WHERE user_id = %s AND item_type = 'deduction'",
                (user_id, user_id)
            )
            custom_items = cursor.fetchall()
            items.extend(custom_items)
        
        return jsonify(items), 200
    except mysql.connector.Error as err:
        return jsonify({'error': f"数据库错误: {err}"}), 500
    finally:
        cursor.close()

@api_bp.route('/reward_items', methods=['GET'])
def get_reward_items():
    user_id = request.args.get('user_id')
    db = get_db()
    cursor = db.cursor(dictionary=True)
    
    try:
        # 获取系统默认项
        cursor.execute("SELECT * "
                "FROM reward_items WHERE user_id = %s  or user_id IS NULL order by user_id desc ",
                (user_id,))
        items = cursor.fetchall()
        
        return jsonify(items), 200
    except mysql.connector.Error as err:
        return jsonify({'error': f"数据库错误: {err}"}), 500
    finally:
        cursor.close()
# 从环境变量获取 DeepSeek API 密钥
DEEPSEEK_API_KEY = "sk-f2892c1d1a194ab682b7772d6f860665"
DEEPSEEK_API_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions"

@api_bp.route('/chat', methods=['POST'])
def chat_stream():
    # 验证 API 密钥
    if not DEEPSEEK_API_KEY:
        def error_generator():
            yield "data: " + json.dumps({"error": "Missing API key"}) + "\n\n"
        return Response(error_generator(), mimetype='text/event-stream')
    
    # 获取请求数据
    try:
        data = request.get_json()
        if not data or 'messages' not in data:
            def error_generator():
                yield "data: " + json.dumps({"error": "Invalid request data"}) + "\n\n"
            return Response(error_generator(), mimetype='text/event-stream')
    except Exception as e:
        def error_generator():
            yield "data: " + json.dumps({"error": "Invalid JSON payload"}) + "\n\n"
        return Response(error_generator(), mimetype='text/event-stream')
    
    # 准备请求头
    headers = {
        "Authorization": f"Bearer {DEEPSEEK_API_KEY}",
        "Content-Type": "application/json",
        "Accept": "application/json"
    }
    print(data['messages'])
    # 准备符合 DeepSeek 要求的请求体
    payload = {
        "model": "deepseek-v3",  # 使用官方支持的模型名称
        "messages": data['messages'],
        "stream": True,
        "temperature": data.get('temperature', 0.7),
        "max_tokens": data.get('max_tokens', 16384)
    }
    
    # 创建生成器函数用于流式响应
    def generate():
        try:
            # 发送请求到 DeepSeek API
            with requests.post(
                DEEPSEEK_API_URL,
                headers=headers,
                json=payload,  # 确保使用 json 参数发送标准 JSON
                stream=True,
                timeout=30
            ) as response:
                if response.status_code != 200:
                    try:
                        error_data = response.json()
                        error_msg = error_data.get("error", {}).get("message", "Unknown error")
                        yield "data: " + json.dumps({
                            "error": f"API error ({response.status_code})",
                            "message": error_msg
                        }) + "\n\n"
                    except:
                        error_text = response.text[:500]
                        yield "data: " + json.dumps({
                            "error": f"API error ({response.status_code})",
                            "message": error_text
                        }) + "\n\n"
                    return
                
                # 逐块读取流式响应
                for chunk in response.iter_lines():
                    if chunk:
                        decoded_chunk = chunk.decode('utf-8')
                        if decoded_chunk.startswith('data:'):
                            yield decoded_chunk + "\n\n"
                        elif "[DONE]" in decoded_chunk:
                            yield "data: [DONE]\n\n"
                            return
        
        except Exception as e:
            yield "data: " + json.dumps({
                "error": "Internal server error",
                "message": str(e)
            }) + "\n\n"
    
    return Response(
        stream_with_context(generate()),
        mimetype='text/event-stream'
    )
    
@api_bp.route('/chat/save', methods=['POST'])
def save_chat_message():
    data = request.json
    user_id = data.get('user_id')
    role = data.get('role')
    message = data.get('message')
    
    if not all([user_id, role, message]):
        return jsonify({"success": False, "error": "Missing required fields"}), 400
    
    db = get_db()
    cursor = db.cursor()
    
    try:
        # 注册用户
        cursor.execute(
            "INSERT INTO chat_history (user_id, role, message) VALUES (%s, %s, %s)",
            (user_id, role, message)
        )
        
        db.commit()
        return jsonify({'message': '保存聊天记录成功'}), 200
    
    except mysql.connector.Error as err:
        db.rollback()
        return jsonify({'error': f"数据库错误: {err}"}), 500
    finally:
        cursor.close()

reader = easyocr.Reader(['ch_sim', 'en'])
@api_bp.route('/ocr', methods=['POST'])        
def handle_ocr():
    try:
        # 初始化EasyOCR阅读器
        
        data = request.json
        server_id = data.get('serverId')
        
        if not server_id:
            return jsonify({"success": False, "message": "缺少serverId参数"})
        
        # 获取微信access token
        access_token = get_access_token()
        
        # 从微信服务器下载图片
        media_url = f"https://api.weixin.qq.com/cgi-bin/media/get?access_token={access_token}&media_id={server_id}"
        response = requests.get(media_url, stream=True)
        
        if response.status_code != 200:
            return jsonify({
                "success": False,
                "message": f"下载图片失败: {response.status_code}"
            })
        
        # 保存临时图片文件
        with tempfile.NamedTemporaryFile(delete=False, suffix=".jpg") as tmp_file:
            for chunk in response.iter_content(chunk_size=8192):
                tmp_file.write(chunk)
            tmp_path = tmp_file.name
        
        # 使用EasyOCR识别
        result = reader.readtext(tmp_path, detail=0)
        text = "\n".join(result)
        print(text)
        # 清理临时文件
        os.unlink(tmp_path)
        
        return jsonify({
            "success": True,
            "text": text,
            "message": "识别成功"
        })
        
    except Exception as e:
        print(f"OCR处理失败: {str(e)}")
        return jsonify({
            "success": False,
            "message": f"OCR处理失败: {str(e)}"
        })
        
@api_bp.route('/ocr/base64', methods=['POST']) 
def ocr_base64():
    try:
        # 获取请求数据
        data = request.get_json()
        base64_image = data.get('image')
        user_id = data.get('userId', 0)
        
        if not base64_image:
            return jsonify({
                "success": False,
                "message": "缺少图像数据"
            }), 400
        
        # 检查并移除base64前缀
        if 'base64,' in base64_image:
            base64_image = base64_image.split('base64,')[1]
        
        # 将base64转换为图像
        image_data = base64.b64decode(base64_image)
        
        # 使用EasyOCR识别
        # 创建临时文件保存图像
        with tempfile.NamedTemporaryFile(delete=False, suffix=".jpg") as tmp_file:
            tmp_file.write(image_data)
            tmp_path = tmp_file.name
        
        # 初始化EasyOCR阅读器
        reader = easyocr.Reader(['ch_sim', 'en'])
        result = reader.readtext(tmp_path, detail=0)
        text = "\n".join(result)
        
        # 清理临时文件
        os.unlink(tmp_path)
        
        return jsonify({
            "success": True,
            "text": text,
            "message": "识别成功"
        })
        
    except Exception as e:
        print(f"OCR处理失败: {str(e)}")
        return jsonify({
            "success": False,
            "message": f"OCR处理失败: {str(e)}"
        }), 500