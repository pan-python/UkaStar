// 初始化聊天功能
function initChat() {
	const userInput = document.getElementById('user-input');
	const sendBtn = document.getElementById('send-btn');
	const chatMessages = document.getElementById('chat-messages');
	
	// 发送按钮点击事件
	sendBtn.addEventListener('click', sendMessage);
	
	// 输入框回车事件
	userInput.addEventListener('keypress', (e) => {
		if (e.key === 'Enter' && !e.shiftKey) {
			e.preventDefault();
			sendMessage();
		}
	});
	
	addMessage('你好！我是你的AI学习助手小星，可以帮你解答作业问题、讲解知识点，也可以陪你聊天哦！今天有什么需要帮助的吗？', 'ai');
	
	// 滚动到底部
	scrollToBottom();

}

// 发送消息函数
let chatHistory = [];
async function sendMessage() {
	const userInput = document.getElementById('user-input');
	const chatMessages = document.getElementById('chat-messages');
	const message = userInput.value.trim();
	
	if (!message) return;
	
	// 添加用户消息到聊天界面和历史记录
	addMessage(message, 'user');
	chatHistory.push({ role: "user", content: message });
	// 保存用户消息
	await saveChatMessage('user', message);
	userInput.value = '';
	
	// 添加"思考中..."消息
	const thinkingMsg = addThinkingMessage();
	
	try {
		// 发送请求到后端API（使用流式API）
		const response = await fetch(`${API_BASE}/chat`, {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json'
			},
			body: JSON.stringify({
				user_id: currentUser ? currentUser.id : 0,
				messages: chatHistory
			})
		});
		
		if (!response.ok) {
			throw new Error(`请求失败: ${response.status}`);
		}
		
		// 移除"思考中..."消息
		thinkingMsg.remove();
		
		const reader = response.body.getReader();
		const decoder = new TextDecoder();
		
		// 创建AI消息容器
		const aiMessageDiv = document.createElement('div');
		aiMessageDiv.className = 'message ai-message';
		aiMessageDiv.innerHTML = `
			<div class="message-avatar">
				<i class="fas fa-robot"></i>
			</div>
			<div class="message-content">
				<span id="streaming-text"></span>
				<span class="typing-cursor"></span>
			</div>
		`;
		chatMessages.appendChild(aiMessageDiv);
		
		const streamingText = aiMessageDiv.querySelector('#streaming-text');
		const typingCursor = aiMessageDiv.querySelector('.typing-cursor');
		
		// 状态变量 - 在外部作用域声明
		let finalContent = '';
		let buffer = '';
		
		// 在流式处理循环中修改这部分
		while (true) {
			const { value, done } = await reader.read();
			if (done) {
				typingCursor.style.display = 'none';
				// 流结束时添加到历史记录
				chatHistory.push({ role: "assistant", content: finalContent });
				break;
			}
			
			// 解码数据块
			const chunk = decoder.decode(value, { stream: true });
			buffer += chunk;
			
			// 处理可能的多条消息
			while (true) {
				// 检查完整消息边界（SSE格式）
				const boundaryIndex = buffer.indexOf('\n\n');
				if (boundaryIndex === -1) break;
				
				const event = buffer.substring(0, boundaryIndex);
				buffer = buffer.substring(boundaryIndex + 2);
				
				// 处理SSE事件
				if (event.startsWith('data: ')) {
					const eventData = event.substring(6).trim();
					
					// 检查结束标记
					if (eventData === '[DONE]') {
						typingCursor.style.display = 'none';
						continue;
					}
					
					try {
						const data = JSON.parse(eventData);
						
						// 处理内容 - 使用 formatModelResponse
						if (data.choices?.[0]?.delta?.content) {
							finalContent += data.choices[0].delta.content;
							
							// 使用 formatModelResponse 格式化内容
							streamingText.innerHTML = formatModelResponse(finalContent);
						}
					} catch (e) {
						console.error('解析JSON失败', e, eventData);
					}
				}
			}
			
			// 确保滚动到底部
			scrollToBottom();
		}
		// 当AI回复完成后保存记录
		await saveChatMessage('assistant', finalContent);
		
	} catch (error) {
		console.error('聊天请求失败:', error);
		
		// 移除"思考中..."消息
		thinkingMsg.remove();
		
		// 添加错误消息 
		addMessage('抱歉，请求失败，请稍后再试', 'ai');
	}
}


// 添加用户/AI消息
function addMessage(text, sender) {
	const messageDiv = document.createElement('div');
	messageDiv.className = `message ${sender}-message`;
	
	// 格式化模型返回的文本
	const formattedContent = formatModelResponse(text);
	
	messageDiv.innerHTML = `
		<div class="message-content">
			${formattedContent}
		</div>
	`;
	if(sender=='ai'){
		messageDiv.innerHTML = `<div class="message-avatar">
					<i class="fas fa-robot"></i>
				</div><div class="message-content" style="animation: fadeIn 1.5s ease;">
			${formattedContent}
		</div>`
	}
	
	document.getElementById('chat-messages').appendChild(messageDiv);
	scrollToBottom();
}

// 添加"思考中..."消息
function addThinkingMessage() {
	const chatMessages = document.getElementById('chat-messages');
	const messageDiv = document.createElement('div');
	messageDiv.className = 'message ai-message';
	messageDiv.id = 'thinking-message';
	
	messageDiv.innerHTML = `
		<div class="message-avatar">
			<i class="fas fa-robot"></i>
		</div>
		<div class="message-content">
			<div class="typing-indicator">
				<span></span>
				<span></span>
				<span></span>
			</div>
		</div>
	`;
	
	chatMessages.appendChild(messageDiv);
	scrollToBottom();
	return messageDiv;
}


// 在外部作用域定义变量
let typeSpeed = 30; // 每个字符的打印间隔(毫秒)
let isTyping = false; // 当前是否正在打印
let contentQueue = []; // 待打印的字符队列
let typingTimer = null; // 打印计时器

// 打印函数 - 从队列中取出字符并渲染
function typeCharacter() {
	if (contentQueue.length === 0) {
		isTyping = false;
		clearInterval(typingTimer);
		return;
	}
	
	const nextChar = contentQueue.shift();
	finalContent += nextChar;
	streamingText.innerHTML = formatModelResponse(finalContent);
	
	// 滚动到底部确保内容可见
	streamingText.scrollTop = streamingText.scrollHeight;
}

// 滚动到聊天底部
function scrollToBottom() {
	const chatMessages = document.getElementById('chat-messages');
	chatMessages.scrollTop = chatMessages.scrollHeight;
}


function formatModelResponse(text) {
    // 辅助函数：递归处理数学表达式
    function processMathExpression(content) {
        // 处理 \frac{分子}{分母}
        content = content.replace(/\\frac{([^{}]+)}{([^{}]+)}/g, (match, numerator, denominator) => {
            return `<span class="math-frac"><span class="frac-top">${processMathExpression(numerator)}</span><span class="frac-line">/</span><span class="frac-bottom">${processMathExpression(denominator)}</span></span>`;
        });
        
        // 处理 \text{文本内容}
        content = content.replace(/\\text{([^{}]+)}/g, (match, textContent) => {
            return `<span class="math-text">${textContent}</span>`;
        });
        
        // 处理 \boxed{内容}
        content = content.replace(/\\boxed{([^{}]+)}/g, (match, boxedContent) => {
            return `<span class="math-boxed">${processMathExpression(boxedContent)}</span>`;
        });
        
        // 处理其他数学符号
        content = content.replace(/(\\[a-zA-Z]+)/g, '<span class="math-symbol">$1</span>');
        
        // 处理数字间的运算符
        content = content.replace(/(\d+)\s*([+\-×÷=])\s*(\d+)/g, (match, num1, op, num2) => {
            const operatorMap = {
                '+': '+',
                '-': '−', // 使用减号字符（不是连字符）
                '×': '×',
                '÷': '÷',
                '=': '='
            };
            return `${num1} <span class="math-operator">${operatorMap[op]}</span> ${num2}`;
        });
        
        return content;
    }

    // 1. 提取所有数学块（公式、boxed、方括号块）
    const mathBlocks = [];
    let blockIndex = 0;
    const extractBlocks = text.replace(/(\$(.*?)\$|\\boxed\{([^}]*)\}|\[([\s\S]*?)\])/gs, (match, p1, p2, p3, p4) => {
        if (p2) {
            mathBlocks.push({ type: "formula", content: p2 });
            return `__MATH_BLOCK_${blockIndex++}__`;
        } else if (p3) {
            mathBlocks.push({ type: "boxed", content: p3 });
            return `__MATH_BLOCK_${blockIndex++}__`;
        } else if (p4) {
            mathBlocks.push({ type: "bracket", content: p4 });
            return `__MATH_BLOCK_${blockIndex++}__`;
        }
        return match;
    });

    // 安全处理：转义 HTML 特殊字符
    const escapeHtml = (unsafe) => {
        return unsafe
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    };

    // 2. 安全处理基础文本
    let safeText = escapeHtml(extractBlocks);
    
    // 3. 处理特殊符号
    safeText = safeText
        .replace(/\\\\/g, '⧵⧵')  // 临时替换双反斜杠
        .replace(/\\\[/g, '[').replace(/\\\]/g, ']')
        .replace(/\\\(/g, '(').replace(/\\\)/g, ')')
        .replace(/\\\{/g, '{').replace(/\\\}/g, '}')
        .replace(/\\_/g, '_').replace(/\\#/g, '#')
        .replace(/⧵⧵/g, '\\\\'); // 恢复双反斜杠

    // 4. 还原数学块并递归处理内容
    let finalText = safeText.replace(/__MATH_BLOCK_(\d+)__/g, (_, index) => {
        const block = mathBlocks[index];
        let processedContent = block.content;
        
        // 递归处理数学表达式
        processedContent = processMathExpression(processedContent);
        
        // 处理换行符
        processedContent = processedContent.replace(/\n/g, '<br>');
        
        switch (block.type) {
            case "formula":
                return `<span class="math-formula">${processedContent}</span>`;
            case "boxed":
                return `<span class="math-boxed">${processedContent}</span>`;
            case "bracket":
                return `<div class="math-bracket">${processedContent}</div>`;
        }
    });

    // 5. 优化标题处理
    let formattedText = finalText
		.replace(/^####\s+(.+)$/gm, '<h4>$1</h4>')
        .replace(/^###\s+(.+)$/gm, '<h3>$1</h3>')
        .replace(/^##\s+(.+)$/gm, '<h2>$1</h2>')
        .replace(/^#\s+(.+)$/gm, '<h1>$1</h1>')
        .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
        .replace(/\*(.*?)\*/g, '<em>$1</em>')
        .replace(/([\u{1F600}-\u{1F6FF}])/gu, '$1')
        .replace(/\n/g, '<br>');

    // 6. 处理列表
    formattedText = formattedText.replace(
        /(?:<br>)?-\s+(.*?)(?=(?:<br>)-|\s*$)/g, 
        (_, p1) => `<ul><li>${p1}</li></ul>`
    );
    
	 // 7. 后处理：移除多余的反斜杠
    formattedText = formattedText.replace(/([^\\])\\/g, '$1').replace(/^\\/g, '');
    return formattedText;
}

//保存聊天记录
async function saveChatMessage(role, message) {
	if (!currentUser) return null;
	
	try {
		const response = await fetch(`${API_BASE}/chat/save`, {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json'
			},
			body: JSON.stringify({
				user_id: currentUser.id,
				role: role,
				message: message
			})
		});
		
		const result = await response.json();
		if (result.success) {
			return result.message_id;
		} else {
			return null;
		}
	} catch (error) {
		return null;
	}
}

  

// 拍照解题功能
const cameraModal = document.getElementById('camera-modal');
const cameraBtn = document.getElementById('camera-btn');
const closeCameraBtn = document.getElementById('close-camera');
const takePhotoBtn = document.getElementById('take-photo');
const choosePhotoBtn = document.getElementById('choose-photo');
const solveBtn = document.getElementById('solve-btn');
const cameraImage = document.getElementById('camera-image');
const cameraPlaceholder = document.getElementById('camera-placeholder');
const solutionContainer = document.getElementById('solution-container');
const solutionImage = document.getElementById('solution-image');
const solutionContent = document.getElementById('solution-content');

// 当前选择的图片
let selectedImage = null;

// 打开拍照弹窗
cameraBtn.addEventListener('click', () => {
	cameraModal.classList.add('active');
	solutionContainer.style.display = 'none';
	cameraImage.style.display = 'none';
	cameraPlaceholder.style.display = 'block';
	selectedImage = null;
});

// 关闭拍照弹窗
closeCameraBtn.addEventListener('click', () => {
	cameraModal.classList.remove('active');
});

// 拍照
takePhotoBtn.addEventListener('click', () => {
	wx.chooseImage({
		count: 1,
		sizeType: ['compressed'],
		sourceType: ['camera'],
		success: function(res) {
			handleImageSelected(res.localIds[0]);
		},
		fail: function(res) {

			alert(JSON.stringify(res)+"拍照失败，请重试");
		}
	});
});

// 从相册选择
choosePhotoBtn.addEventListener('click', () => {
	wx.chooseImage({
		count: 1,
		sizeType: ['compressed'],
		sourceType: ['album'],
		success: function(res) {
			handleImageSelected(res.localIds[0]);
		},
		fail: function(res) {
			alert("选择图片失败，请重试");
		}
	});
});


// 在文件顶部添加裁剪相关变量
// 在文件顶部添加裁剪相关变量
let cropper = null;
let cropModal = null;

// 创建裁剪模态框（在initChat函数中调用）
function initCropModal() {
    if (cropModal) return;
    
    cropModal = document.createElement('div');
    cropModal.id = 'crop-modal';
    cropModal.style.display = 'none';
    cropModal.style.position = 'fixed';
    cropModal.style.top = '0';
    cropModal.style.left = '0';
    cropModal.style.width = '100%';
    cropModal.style.height = '100%';
    cropModal.style.backgroundColor = 'rgba(0, 0, 0, 0.9)';
    cropModal.style.zIndex = '2000';
    cropModal.style.display = 'flex';
    cropModal.style.justifyContent = 'center';
    cropModal.style.alignItems = 'center';
    
    cropModal.innerHTML = `
        <div class="crop-container" style="width: 90%; max-width: 600px; background: white; border-radius: 8px; overflow: hidden; position: relative;">
            <img id="crop-image" src="" alt="裁剪图片" style="max-width: 100%; max-height: 60vh; display: block;">
            <div class="crop-controls" style="padding: 15px; background: #f5f5f5; display: flex; justify-content: space-between;">
                <button id="crop-cancel" style="padding: 8px 15px; border: none; border-radius: 4px; cursor: pointer; font-size: 14px; background: #f44336; color: white;">取消</button>
                <button id="crop-confirm" style="padding: 8px 15px; border: none; border-radius: 4px; cursor: pointer; font-size: 14px; background: #4CAF50; color: white;">确认裁剪</button>
            </div>
        </div>
    `;
    
    document.body.appendChild(cropModal);
    
    // 添加裁剪控制事件
    document.getElementById('crop-cancel').addEventListener('click', cancelCrop);
    document.getElementById('crop-confirm').addEventListener('click', confirmCrop);
}

// 修改handleImageSelected函数 - 添加裁剪步骤
function handleImageSelected(localId) {
    selectedImage = localId;
    
    wx.getLocalImgData({
        localId: localId,
        success: function (res) {
            let imgUrl = res.localData;
            if (!imgUrl.startsWith('data:image')) {
                imgUrl = 'data:image/jpeg;base64,' + imgUrl;
            }
            
            // 显示裁剪模态框而不是直接预览
            showCropModal(imgUrl);
        },
        fail: function (res) {
            // 回退到原始方式
            cameraImage.src = localId;
            cameraImage.style.display = 'block';
            cameraPlaceholder.style.display = 'none';
            cameraImage.style.maxWidth = '100%';
            cameraImage.style.maxHeight = '400px';
            cameraImage.style.objectFit = 'contain';
            
            // 更新为base64格式
            selectedImage = localId;
        }
    });
}

// 显示裁剪模态框
function showCropModal(imgUrl) {
	// 初始化裁剪模态框
	initCropModal();
    const cropImage = document.getElementById('crop-image');
    cropImage.src = imgUrl;
    
    // 显示裁剪模态框
    cropModal.style.display = 'flex';
    
    // 初始化Cropper.js
    if (cropper) {
        cropper.destroy();
    }
    
    // 等待图片加载完成再初始化cropper
    cropImage.onload = function() {
        cropper = new Cropper(cropImage, {
            aspectRatio: NaN, // 默认裁剪比例3/1  NaN表示不限制比例
            viewMode: 0,      // 限制裁剪框不超过画布
            autoCropArea: 0.6, // 初始裁剪区域大小
            movable: true,
            zoomable: true,
            rotatable: true,
            scalable: true,
            minContainerWidth: 380,
            minContainerHeight: 400
        });
    };
}

// 取消裁剪
function cancelCrop() {
    cropModal.style.display = 'none';
    if (cropper) {
        cropper.destroy();
        cropper = null;
    }
    
    // 关闭拍照模态框
    cameraModal.classList.remove('active');
}

// 确认裁剪
function confirmCrop() {
    if (!cropper) return;
    
    // 获取裁剪后的图片
    const croppedCanvas = cropper.getCroppedCanvas();
    const croppedImageData = croppedCanvas.toDataURL('image/jpeg');
    
    // 隐藏裁剪模态框
    cropModal.style.display = 'none';
    cropper.destroy();
    cropper = null;
    
    // 在拍照模态框中显示裁剪后的图片
    cameraImage.src = croppedImageData;
    cameraImage.style.display = 'block';
    cameraPlaceholder.style.display = 'none';
    cameraImage.style.maxWidth = '100%';
    cameraImage.style.maxHeight = '400px';
    cameraImage.style.objectFit = 'contain';
    
    // 更新选中的图片为裁剪后的图片
    selectedImage = croppedImageData;
    
    // 确保拍照模态框仍然显示
    cameraModal.classList.add('active');
}

// 修改solveBtn事件处理 - 使用裁剪后的图片
solveBtn.addEventListener('click', async () => {
    if (!selectedImage) {
        alert("请先拍照或选择题目图片");
        return;
    }
    
    // 显示加载层
    const overlay = createLoadingOverlay();
    overlay.style.display = 'flex';
    
    try {
        // 如果是base64图片数据（裁剪后的图片）
        if (selectedImage.startsWith('data:image')) {
            // 1. 将base64图片发送到后端OCR接口
            const ocrResult = await fetchOCRResultBase64(selectedImage);
            
            // 2. 处理OCR结果
            processOCRResult(ocrResult);
        } 
        // 如果是微信本地图片ID（未裁剪的情况）
        else {
            // 1. 将微信本地图片上传到微信服务器
            const serverId = await uploadImageToServer(selectedImage);
            
            // 2. 将serverId发送到后端OCR接口
            const ocrResult = await fetchOCRResult(serverId);
            
            // 3. 处理OCR结果
            processOCRResult(ocrResult);
        }
    } catch (error) {
        console.error('OCR处理失败:', error);
        solutionContent.innerHTML = `<div class="error-message">识别失败: ${error.message || '请重试'}</div>`;
    } finally {
        // 无论成功失败，都关闭加载层
        overlay.style.display = 'none';
    }
});

// 添加base64图片识别接口
async function fetchOCRResultBase64(base64Data) {
    // 提取base64数据部分（去掉data:image/jpeg;base64,前缀）
    const base64Content = base64Data.split(',')[1];
    
    const response = await fetch(`${API_BASE}/ocr/base64`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            image: base64Content,
            userId: currentUser ? currentUser.id : 0
        })
    });
    
    if (!response.ok) {
        throw new Error(`OCR请求失败: ${response.status}`);
    }
    
    return response.json();
}

// 在文件顶部添加变量声明
let loadingOverlay = null; // 加载层引用
let loadingSpinner = null; // 加载动画元素

// 在拍照解题功能部分添加以下代码
// 创建加载层（如果不存在）
function createLoadingOverlay() {
	if (!loadingOverlay) {
		loadingOverlay = document.createElement('div');
		loadingOverlay.id = 'loading-overlay';
		loadingOverlay.style.position = 'fixed';
		loadingOverlay.style.top = '0';
		loadingOverlay.style.left = '0';
		loadingOverlay.style.width = '100%';
		loadingOverlay.style.height = '100%';
		loadingOverlay.style.backgroundColor = 'rgba(0,0,0,0.5)';
		loadingOverlay.style.zIndex = '1000';
		loadingOverlay.style.display = 'none';
		loadingOverlay.style.justifyContent = 'center';
		loadingOverlay.style.alignItems = 'center';
		loadingOverlay.style.flexDirection = 'column';
		
		// 创建加载动画
		loadingSpinner = document.createElement('div');
		loadingSpinner.className = 'spinner';
		loadingSpinner.style.width = '50px';
		loadingSpinner.style.height = '50px';
		loadingSpinner.style.border = '5px solid rgba(255,255,255,0.3)';
		loadingSpinner.style.borderRadius = '50%';
		loadingSpinner.style.borderTop = '5px solid #fff';
		loadingSpinner.style.animation = 'spin 1s linear infinite';
		
		// 创建提示文字
		const loadingText = document.createElement('p');
		loadingText.textContent = '图片正在识别中...';
		loadingText.style.color = 'white';
		loadingText.style.marginTop = '20px';
		loadingText.style.fontSize = '16px';
		
		loadingOverlay.appendChild(loadingSpinner);
		loadingOverlay.appendChild(loadingText);
		cameraModal.appendChild(loadingOverlay);
		
		// 添加旋转动画关键帧
		const style = document.createElement('style');
		style.textContent = `
			@keyframes spin {
				0% { transform: rotate(0deg); }
				100% { transform: rotate(360deg); }
			}
		`;
		document.head.appendChild(style);
	}
	return loadingOverlay;
}

// 修改后的解题按钮处理



// 上传图片到微信服务器
function uploadImageToServer(localId) {
    return new Promise((resolve, reject) => {
        wx.uploadImage({
            localId: localId,
            isShowProgressTips: 1, // 显示上传进度提示
            success: function(res) {
                resolve(res.serverId);
            },
            fail: function(err) {
                reject(new Error('图片上传失败: ' + err.errMsg));
            }
        });
    });
}

// 获取OCR识别结果
async function fetchOCRResult(serverId) {
    const response = await fetch(`${API_BASE}/ocr`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            serverId: serverId,
            userId: currentUser ? currentUser.id : 0
        })
    });
    
    if (!response.ok) {
        throw new Error(`OCR请求失败: ${response.status}`);
    }
    
    return response.json();
}



// 处理OCR识别结果
function processOCRResult(ocrResult) {	
	// 关闭拍照弹窗
	cameraModal.classList.remove('active');
	
	// 将识别结果赋值给输入框并发送
	document.getElementById('user-input').value = ocrResult.text;
	sendMessage();
	
}

