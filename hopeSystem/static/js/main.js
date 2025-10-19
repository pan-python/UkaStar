// 加分类别切换
document.querySelectorAll('.category-card').forEach(card => {
	card.addEventListener('click', function() {
		const category = this.getAttribute('data-category');
		const targetPage = `add-${category}`;
		
		document.querySelectorAll('.content-section').forEach(section => {
			section.classList.remove('active');
		});
		document.getElementById(targetPage).classList.add('active');
	});
});

// 减分类别切换
document.querySelectorAll('#minus .category-card').forEach(card => {
	card.addEventListener('click', function() {
		const category = this.getAttribute('data-category');
		const targetPage = `minus-${category}`;
		
		document.querySelectorAll('.content-section').forEach(section => {
			section.classList.remove('active');
		});
		document.getElementById(targetPage).classList.add('active');
	});
});

// 返回按钮功能
document.querySelectorAll('.back-btn').forEach(btn => {
	btn.addEventListener('click', function() {
		const targetId = this.getAttribute('data-back');
		
		document.querySelectorAll('.content-section').forEach(section => {
			section.classList.remove('active');
		});
		document.getElementById(targetId).classList.add('active');
	});
});

// 记录标签切换
document.querySelectorAll('.record-tab').forEach(tab => {
	tab.addEventListener('click', function() {
		const targetRecord = this.getAttribute('data-record');
		
		// 更新标签状态
		document.querySelectorAll('.record-tab').forEach(t => {
			t.classList.remove('active');
		});
		this.classList.add('active');
		
		// 更新记录内容
		document.querySelectorAll('.record-content').forEach(content => {
			content.classList.remove('active');
		});
		document.getElementById(`${targetRecord}-records`).classList.add('active');
	});
});

// 加减分按钮交互效果
document.querySelectorAll('.action-btn').forEach(btn => {
	btn.addEventListener('click', function() {
		const card = this.closest('.card');
		card.style.backgroundColor = '#f8f9ff';
		
		setTimeout(() => {
			card.style.backgroundColor = '';
			
			// 显示操作成功消息
			const isAdd = this.classList.contains('add-btn');
			
			if(!isAdd){
				alert(`${isAdd ? '加分' : '减分'}操作已记录！`);
			}
			
			// 返回主加分页面
			document.querySelectorAll('.content-section').forEach(section => {
				section.classList.remove('active');
			});
			document.getElementById('add').classList.add('active');
			
			// 更新底部导航状态
			document.querySelectorAll('.nav-btn').forEach(btn => {
				btn.classList.remove('active');
				if(btn.getAttribute('data-target') === 'add') {
					btn.classList.add('active');
				}
			});
		}, 500);
	});
});

// 兑换按钮交互效果
document.querySelectorAll('.exchange-btn').forEach(btn => {
	btn.addEventListener('click', function() {
		const card = this.closest('.card');
		
		// 添加动画效果
		card.style.transform = 'scale(0.98)';
		card.style.boxShadow = '0 4px 6px rgba(0,0,0,0.1)';
		
		setTimeout(() => {
			card.style.transform = '';
			card.style.boxShadow = '';
			
		}, 300);
	});
});


// 日期时间显示和更新功能
function updateDateTime() {
	const now = new Date();
	
	// 格式化日期
	const dateOptions = { 
		year: 'numeric', 
		month: '2-digit', 
		day: '2-digit',
		weekday: 'short'
	};
	const dateStr = now.toLocaleDateString('zh-CN', dateOptions);
	
	// 格式化时间
	const timeOptions = {
		hour: '2-digit',
		minute: '2-digit',
		second: '2-digit',
		hour12: false
	};
	const timeStr = now.toLocaleTimeString('zh-CN', timeOptions);
	
	// 更新元素
	document.getElementById('current-date').textContent = dateStr;
	document.getElementById('current-time').textContent = timeStr;
}

// 初始化并每秒更新一次
updateDateTime();
setInterval(updateDateTime, 1000);

// 修改主标签页切换功能，添加记录页面的自动加载
document.querySelectorAll('.nav-tab, .nav-btn').forEach(tab => {
	tab.addEventListener('click', function() {
		const targetId = this.getAttribute('data-target');
		
		// 更新标签状态
		document.querySelectorAll('.nav-tab, .nav-btn').forEach(t => {
			t.classList.remove('active');
		});
		this.classList.add('active');
		
		// 更新内容区域
		document.querySelectorAll('.content-section').forEach(section => {
			section.classList.remove('active');
		});
		document.getElementById(targetId).classList.add('active');
		
		// 如果是首页，更新数据
		if (targetId === 'home' && currentUser) {
			loadUserData();
		}
		// 如果是记录页面，加载今日记录
		else if (targetId === 'records' && currentUser) {
			// 确保今日记录标签激活
			const todayTab = document.querySelector('.record-tab[data-record="today"]');
			if (todayTab) {
				// 更新标签状态
				document.querySelectorAll('.record-tab').forEach(t => {
					t.classList.remove('active');
				});
				todayTab.classList.add('active');
				
				// 更新记录内容
				document.querySelectorAll('.record-content').forEach(content => {
					content.classList.remove('active');
				});
				document.getElementById('today-records').classList.add('active');
				
				// 加载今日记录
				loadRecords('today');
			}
		}
	});
});
