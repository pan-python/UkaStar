import os
import requests
from flask import Flask, request, Response, jsonify, stream_with_context

app = Flask(__name__)

# 从环境变量获取 DeepSeek API 密钥
DEEPSEEK_API_KEY = os.getenv("DEEPSEEK_API_KEY")
DEEPSEEK_API_URL = "https://api.deepseek.com/v1/chat/completions"

@app.route('/api/chat', methods=['POST'])
def chat_stream():
    # 验证 API 密钥
    if not DEEPSEEK_API_KEY:
        return jsonify({"error": "Missing API key"}), 500
    
    # 获取请求数据
    data = request.get_json()
    
    # 验证请求数据
    if not data or 'messages' not in data:
        return jsonify({"error": "Invalid request data"}), 400
    
    # 准备请求头
    headers = {
        "Authorization": f"Bearer {DEEPSEEK_API_KEY}",
        "Content-Type": "application/json",
        "Accept": "application/json"
    }
    
    # 准备请求体
    payload = {
        "model": "deepseek-chat",  # 使用最新的 DeepSeek 模型
        "messages": data['messages'],
        "stream": True,  # 启用流式传输
        "temperature": data.get('temperature', 0.7),
        "max_tokens": data.get('max_tokens', 2048)
    }
    
    # 创建生成器函数用于流式响应
    def generate():
        try:
            # 发送请求到 DeepSeek API
            with requests.post(
                DEEPSEEK_API_URL,
                headers=headers,
                json=payload,
                stream=True  # 保持连接开启以接收流式数据
            ) as response:
                # 检查响应状态
                if response.status_code != 200:
                    yield f"data: {response.json()}\n\n"
                    return
                
                # 逐块读取流式响应
                for chunk in response.iter_lines():
                    # 过滤掉保持连接的空行
                    if chunk:
                        decoded_chunk = chunk.decode('utf-8')
                        
                        # 检查是否是数据行
                        if decoded_chunk.startswith('data:'):
                            # 发送有效数据 (移除 "data: " 前缀)
                            yield f"{decoded_chunk}\n\n"
                        
                        # 检查流结束标记
                        elif "[DONE]" in decoded_chunk:
                            yield "data: [DONE]\n\n"
                            break
        
        except Exception as e:
            # 错误处理
            yield f"data: {{\"error\": \"{str(e)}\"}}\n\n"
    
    # 返回流式响应
    return Response(
        stream_with_context(generate()),
        mimetype='text/event-stream'
    )

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)