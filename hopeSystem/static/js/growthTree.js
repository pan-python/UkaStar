// 模拟成长树动画
const treeImage = document.querySelector('.tree-image');


// 初始化点状动画
createDotAnimation();
createLeafAnimation();
// 添加树叶飘落效果
function createLeafAnimation() {
	const treeContainer = document.querySelector('.tree');
	if (!treeContainer) return;
	
	for (let i = 0; i < 8; i++) {
		const leaf = document.createElement('div');
		leaf.className = 'leaf';
		leaf.innerHTML = '❀';
		leaf.style.position = 'absolute';
		leaf.style.fontSize = '16px';
		leaf.style.color = '#57A773';
		leaf.style.opacity = '0.7';
		leaf.style.left = `${Math.random() * 100}%`;
		leaf.style.top = `${Math.random() * 100}%`;
		leaf.style.animation = `floatLeaf ${10 + Math.random() * 10}s linear infinite`;
		leaf.style.animationDelay = `${Math.random() * 5}s`;
		treeContainer.appendChild(leaf);
	}
}

// 添加树叶飘落动画
const style = document.createElement('style');
style.innerHTML = `
	@keyframes floatLeaf {
		0% {
			transform: translate(0, 0) rotate(0deg);
			opacity: 0.7;
		}
		50% {
			opacity: 1;
		}
		100% {
			transform: translate(${Math.random() > 0.5 ? '-' : ''}${20 + Math.random() * 30}px, 100px) rotate(${360 + Math.random() * 360}deg);
			opacity: 0;
		}
	}
`;
document.head.appendChild(style);

// 成长树阶段定义
const growthStages = [
	{ min: 0, max: 100, name: "嫩芽", image: "images/1.png", next: "小树苗" },
	{ min: 100, max: 200, name: "小树苗", image: "images/2.png", next: "小树" },
	{ min: 200, max: 400, name: "小树", image: "images/3.png", next: "中树" },
	{ min: 400, max: 600, name: "中树", image: "images/4.png", next: "大树" },
	{ min: 600, max: 800, name: "大树", image: "images/5.png", next: "参天大树" },
	{ min: 800, max: 1000, name: "参天大树", image: "images/6.png", next: "森林之王" },
	{ min: 1000, max: 3000, name: "森林之王", image: "images/7.png", next: "MAX" }
];

// 初始化成长树
function initGrowthTree() {
	// 从用户数据获取当前积分
	const currentPoints = currentUser ? currentUser.total_points : 0;
	updateGrowthTree(currentPoints);
}

// 更新成长树
function updateGrowthTree(points) {
	const treeImage = document.getElementById('tree-image');
	const treeStage = document.getElementById('tree-stage');
	const energyValue = document.getElementById('energy-value');
	const nextStage = document.getElementById('next-stage');
	const progressFill = document.querySelector('.energy-progress');
	
	// 找到当前阶段
	const currentStage = growthStages.find(stage => points >= stage.min && points <= stage.max);
	
	if (currentStage) {
		// 更新树图片
		treeImage.src = currentStage.image;
		
		// 更新阶段名称
		treeStage.textContent = currentStage.name;
		
		// 更新能量值显示
		energyValue.textContent = `${points}/${currentStage.max}`;
		
		// 更新进度条 - 这是修复的关键部分
		const progress = Math.min(100, Math.max(0, 
			((points - currentStage.min) / (currentStage.max - currentStage.min)) * 100
		));
		
		progressFill.style.width = `${progress}%`;
		
		// 更新下一阶段提示
		if (currentStage.next === "MAX") {
			nextStage.textContent = "即将达到最高阶段！";
		} else {
			if (points >= currentStage.max) {
				// 找到下一阶段
				const nextStageIndex = growthStages.findIndex(stage => stage.name === currentStage.next);
				if (nextStageIndex !== -1) {
					const nextStageObj = growthStages[nextStageIndex];
					nextStage.textContent = `即将进入下一阶段: ${nextStageObj.name}`;
				} else {
					nextStage.textContent = `准备进入下一阶段: ${currentStage.next}`;
				}
			} else {
				const pointsNeeded = currentStage.max - points;
				nextStage.textContent = `下一阶段: ${currentStage.next} (还需${pointsNeeded}分)`;
			}
		}
	}
}

// 修改点状动画创建函数
function createDotAnimation() {
	const container = document.querySelector('.dot-animation');
	if (!container) return;
	
	// 清除现有内容
	container.innerHTML = '';
	
	// 创建点 - 数量减少以提高性能
	for (let i = 0; i < 8; i++) {
		const dot = document.createElement('div');
		dot.className = 'dot';
		dot.style.left = `${Math.random() * 100}%`;
		dot.style.animationDelay = `${Math.random() * 2}s`;
		container.appendChild(dot);
	}
}


function showNumberFirework(number, soundEnabled = true) {
	// 确保滚动到绝对顶部
	window.scrollTo(0, 0);
	document.documentElement.scrollTop = 0;
	document.body.scrollTop = 0;
	
	const container = document.getElementById('fireworksContainer');
	container.innerHTML = '';
	
	const numberElement = document.createElement('div');
	numberElement.className = 'firework-number';
	numberElement.textContent = `+${number}`;
	container.appendChild(numberElement);
	
	if (soundEnabled) playFireworkSound();
	
	createParticles(container);
	
	setTimeout(() => numberElement.remove(), 11200);
}
// 创建烟花粒子
function createParticles(container) {
	const particleCount = 120;
	const colors = ['#00c853', '#64dd17', '#a7ffeb', '#f4ff81', '#76ff03'];
	
	for (let i = 0; i < particleCount; i++) {
		const particle = document.createElement('div');
		particle.className = 'particle';
		
		// 随机颜色
		const color = colors[Math.floor(Math.random() * colors.length)];
		particle.style.backgroundColor = color;
		
		// 随机大小
		const size = 3 + Math.random() * 7;
		particle.style.width = `${size}px`;
		particle.style.height = `${size}px`;
		
		// 随机位置（在屏幕中心附近）
		const centerX = window.innerWidth / 2;
		const centerY = window.innerHeight / 2;
		const startX = centerX + (Math.random() - 0.5) * 50;
		const startY = centerY + (Math.random() - 0.5) * 50;
		
		particle.style.left = `${startX}px`;
		particle.style.top = `${startY}px`;
		
		// 随机运动方向
		const angle = Math.random() * Math.PI * 2;
		const distance = 100 + Math.random() * 300;
		const tx = Math.cos(angle) * distance;
		const ty = Math.sin(angle) * distance;
		
		particle.style.setProperty('--tx', `${tx}px`);
		particle.style.setProperty('--ty', `${ty}px`);
		
		// 随机动画延迟和时长
		const delay = Math.random() * 0.3;
		const duration = 0.7 + Math.random() * 0.5;
		
		particle.style.animationDelay = `${delay}s`;
		particle.style.animationDuration = `${duration}s`;
		
		container.appendChild(particle);
		
		// 动画结束后移除粒子
		setTimeout(() => {
			particle.remove();
		}, (delay + duration) * 1000);
	}
}
