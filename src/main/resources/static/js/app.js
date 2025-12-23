/**
 * CRUD 代码生成器主应用
 */

// 引入自定义下拉选择框组件
function includeCustomSelect() {
    const script = document.createElement('script');
    script.src = 'js/customSelect.js';
    document.head.appendChild(script);
}

// 当DOM加载完成后引入自定义组件
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', includeCustomSelect);
} else {
    includeCustomSelect();
}

// 当前步骤
let currentStep = 1;

// 存储数据库和表的数据
let databaseList = [];
let tableList = [];

// DOM 元素
const elements = {
    // 步骤元素
    steps: document.querySelectorAll('.step'),
    stepContents: document.querySelectorAll('.step-content'),
    
    // 表单元素
    connectionForm: document.getElementById('connectionForm'),
    databaseSelect: document.getElementById('databaseName'),
    tableSelect: document.getElementById('tableName'),
    packageName: document.getElementById('packageName'),
    outputDir: document.getElementById('outputDir'),
    
    // 按钮元素
    connectBtn: document.getElementById('connectBtn'),
    backToStep1: document.getElementById('backToStep1'),
    nextToStep3: document.getElementById('nextToStep3'),
    backToStep2: document.getElementById('backToStep2'),
    generateBtn: document.getElementById('generateBtn'),
    resetBtn: document.getElementById('resetBtn'),
    
    // 状态元素
    loading: document.getElementById('loading'),
    resultContainer: document.getElementById('resultContainer'),
    resultContent: document.getElementById('resultContent'),
    packageHint: document.getElementById('packageHint'),
    
    // 下拉列表元素
    databaseDropdown: document.getElementById('databaseDropdown'),
    tableDropdown: document.getElementById('tableDropdown')
};

// 事件监听器
document.addEventListener('DOMContentLoaded', function() {
    // 绑定事件
    bindEvents();
});

/**
 * 绑定事件监听器
 */
function bindEvents() {
    // 连接数据库按钮
    elements.connectBtn.addEventListener('click', connectDatabase);
    
    // 步骤导航按钮
    elements.backToStep1.addEventListener('click', () => goToStep(1));
    elements.nextToStep3.addEventListener('click', validateAndGoToStep3);
    elements.backToStep2.addEventListener('click', () => goToStep(2));
    
    // 生成代码按钮
    elements.generateBtn.addEventListener('click', downloadGeneratedCode);
    
    // 重置按钮
    elements.resetBtn.addEventListener('click', resetApp);
    
    // 等待自定义下拉组件加载完成后再绑定事件
    setTimeout(() => {
        // 数据库选择变化
        elements.databaseSelect.addEventListener('change', loadTables);
        
        // 表名选择变化
        elements.tableSelect.addEventListener('change', suggestPackageName);
    }, 100);
}

/**
 * 显示加载状态
 */
function showLoading() {
    elements.loading.style.display = 'block';
}

/**
 * 隐藏加载状态
 */
function hideLoading() {
    elements.loading.style.display = 'none';
}

/**
 * 显示结果
 */
function showResult(success, message, data = null) {
    elements.resultContainer.style.display = 'block';
    elements.resultContent.innerHTML = `
        <div class="${success ? 'result-success' : 'result-error'}">
            ${message}
        </div>
        ${data ? `<div style="margin-top: 15px; padding: 15px; background: #e9ecef; border-radius: 8px; text-align: left;">
                <strong>输出路径:</strong> ${data}
            </div>` : ''}
    `;
}

/**
 * 隐藏结果
 */
function hideResult() {
    elements.resultContainer.style.display = 'none';
}

/**
 * 设置按钮状态
 */
function setButtonState(button, disabled, text = null) {
    button.disabled = disabled;
    if (text) {
        button.textContent = text;
    }
}

/**
 * 切换步骤
 */
function goToStep(step) {
    // 隐藏所有步骤内容
    elements.stepContents.forEach(content => {
        content.classList.remove('active');
    });
    
    // 移除所有步骤的激活状态
    elements.steps.forEach(s => {
        s.classList.remove('active');
    });
    
    // 显示目标步骤
    document.getElementById(`step${step}`).classList.add('active');
    document.querySelector(`.step[data-step="${step}"]`).classList.add('active');
    
    // 更新当前步骤
    currentStep = step;
    
    // 如果是第三步，自动生成包名建议
    if (step === 3) {
        suggestPackageName();
    }
}

/**
 * 连接数据库并加载数据库列表
 */
async function connectDatabase() {
    const dbUrl = document.getElementById('dbUrl').value.trim();
    const dbUsername = document.getElementById('dbUsername').value.trim();
    const dbPassword = document.getElementById('dbPassword').value.trim();

    if (!dbUrl || !dbUsername) {
        showResult(false, '请填写数据库连接信息');
        return;
    }

    setButtonState(elements.connectBtn, true, '连接中...');
    showLoading();
    hideResult();

    try {
        const response = await fetch('/api/generator/databases', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                dbUrl: dbUrl,
                dbUsername: dbUsername,
                dbPassword: dbPassword
            })
        });

        const data = await response.json();
        hideLoading();
        setButtonState(elements.connectBtn, false, '🔌 连接数据库');

        if (data.success && data.data && data.data.length > 0) {
            databaseList = data.data;
            
            // 填充数据库下拉列表
            populateDatabaseSelect();
            
            // 跳转到第二步
            goToStep(2);
            
            showResult(true, `连接成功！找到 ${data.data.length} 个数据库`);
        } else {
            showResult(false, data.message || '未找到数据库');
        }
    } catch (error) {
        hideLoading();
        setButtonState(elements.connectBtn, false, '🔌 连接数据库');
        showResult(false, '连接失败，请检查数据库连接信息');
    }
}

/**
 * 填充数据库下拉列表
 */
function populateDatabaseSelect() {
    // 使用自定义下拉选择框组件
    setTimeout(() => {
        if (typeof initCustomSelect !== 'undefined') {
            initCustomSelect('databaseName', 'databaseDropdown', databaseList);
        }
    }, 100);
}

/**
 * 加载表列表
 */
async function loadTables() {
    // 获取选中的数据库名
    const selectedDatabase = typeof getSelectedValue !== 'undefined' ? 
        getSelectedValue('databaseName') : elements.databaseSelect.textContent.trim();
        
    if (!selectedDatabase) {
        // 清空表下拉列表
        if (typeof setSelectedValue !== 'undefined') {
            setSelectedValue('tableName', '');
        } else {
            elements.tableSelect.innerHTML = '<option value="">请选择表</option>';
        }
        return;
    }

    const dbUrl = document.getElementById('dbUrl').value.trim();
    const dbUsername = document.getElementById('dbUsername').value.trim();
    const dbPassword = document.getElementById('dbPassword').value.trim();

    showLoading();
    hideResult();

    try {
        const response = await fetch('/api/generator/tables', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                dbUrl: dbUrl,
                dbUsername: dbUsername,
                dbPassword: dbPassword,
                databaseName: selectedDatabase
            })
        });

        const data = await response.json();
        hideLoading();

        if (data.success && data.data && data.data.length > 0) {
            tableList = data.data;
            
            // 填充表下拉列表
            populateTableSelect();
            
            showResult(true, `找到 ${data.data.length} 个表`);
        } else {
            // 清空表下拉列表
            if (typeof setSelectedValue !== 'undefined') {
                setSelectedValue('tableName', '');
            } else {
                elements.tableSelect.innerHTML = '<option value="">请选择表</option>';
            }
            showResult(false, data.message || '未找到表');
        }
    } catch (error) {
        hideLoading();
        showResult(false, '获取表列表失败');
    }
}

/**
 * 填充表下拉列表
 */
function populateTableSelect() {
    // 使用自定义下拉选择框组件
    setTimeout(() => {
        if (typeof initCustomSelect !== 'undefined') {
            initCustomSelect('tableName', 'tableDropdown', tableList);
        }
    }, 100);
}

/**
 * 自动生成包名建议
 */
function suggestPackageName() {
    // 获取选中的表名
    const selectedTable = typeof getSelectedValue !== 'undefined' ? 
        getSelectedValue('tableName') : elements.tableSelect.value;
        
    if (selectedTable) {
        // 将表名转换为包名建议 (例如: sys_user -> com.example.sys.user)
        const entityName = selectedTable.replace(/_/g, '.');
        elements.packageHint.textContent = `建议: com.example.${entityName}`;
    } else {
        elements.packageHint.textContent = '根据表名自动生成包名';
    }
}

/**
 * 生成代码
 */
async function generateCode() {
    // 获取表单数据
    // 获取选中的数据库和表名
    const selectedDatabase = typeof getSelectedValue !== 'undefined' ? 
        getSelectedValue('databaseName') : elements.databaseSelect.textContent.trim();
    const selectedTable = typeof getSelectedValue !== 'undefined' ? 
        getSelectedValue('tableName') : elements.tableSelect.textContent.trim();
        
    const formData = {
        dbUrl: document.getElementById('dbUrl').value.trim(),
        dbUsername: document.getElementById('dbUsername').value.trim(),
        dbPassword: document.getElementById('dbPassword').value.trim(),
        databaseName: selectedDatabase,
        tableName: selectedTable,
        packageName: elements.packageName.value.trim(),
        outputDir: elements.outputDir.value.trim()
    };

    // 验证必填项
    if (!formData.databaseName) {
        showResult(false, '请选择数据库');
        return;
    }
    
    if (!formData.tableName) {
        showResult(false, '请选择表');
        return;
    }
    
    if (!formData.packageName) {
        showResult(false, '请填写包名');
        return;
    }

    setButtonState(elements.generateBtn, true, '生成中...');
    showLoading();
    hideResult();

    try {
        const response = await fetch('/api/generator/generate', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(formData)
        });

        const data = await response.json();

        hideLoading();
        setButtonState(elements.generateBtn, false, '🚀 开始生成代码');

        if (data.success) {
            // 生成成功
            showResult(true, data.message || `代码生成成功！文件已保存到: ${data.data || '默认目录'}`, '');
        } else {
            showResult(false, data.message);
        }
    } catch (error) {
        hideLoading();
        setButtonState(elements.generateBtn, false, '🚀 开始生成代码');
        showResult(false, '无法连接到服务器，请检查后端服务是否正常运行');
    }
}

/**
 * 下载生成的代码zip包
 */
async function downloadGeneratedCode() {
    // 获取表单数据
    // 获取选中的数据库和表名
    const selectedDatabase = typeof getSelectedValue !== 'undefined' ? 
        getSelectedValue('databaseName') : elements.databaseSelect.textContent.trim();
    const selectedTable = typeof getSelectedValue !== 'undefined' ? 
        getSelectedValue('tableName') : elements.tableSelect.textContent.trim();
    
    const formData = {
        dbUrl: document.getElementById('dbUrl').value.trim(),
        dbUsername: document.getElementById('dbUsername').value.trim(),
        dbPassword: document.getElementById('dbPassword').value.trim(),
        databaseName: selectedDatabase,
        tableName: selectedTable,
        packageName: elements.packageName.value.trim(),
        outputDir: elements.outputDir.value.trim()
    };

    // 验证必填项
    if (!formData.databaseName) {
        showResult(false, '请选择数据库');
        return;
    }
    
    if (!formData.tableName) {
        showResult(false, '请选择表');
        return;
    }
    
    if (!formData.packageName) {
        showResult(false, '请填写包名');
        return;
    }

    setButtonState(elements.generateBtn, true, '生成中...');
    showLoading();
    hideResult();

    try {
        const response = await fetch('/api/generator/generate-download', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(formData)
        });

        if (response.ok) {
            // 获取响应数据并创建下载
            const blob = await response.blob();
            const contentDisposition = response.headers.get('Content-Disposition');
            let filename = 'generated-code.zip';
            
            // 从响应头中提取文件名
            if (contentDisposition) {
                const filenameMatch = contentDisposition.match(/filename\*?=(?:"([^\"]+)"|([^;\s]+))/i);
                if (filenameMatch && (filenameMatch[1] || filenameMatch[2])) {
                    filename = decodeURIComponent(filenameMatch[1] || filenameMatch[2]);
                }
            }
            
            // 创建下载链接
            const url = window.URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = url;
            link.download = filename;
            document.body.appendChild(link);
            link.click();
            
            // 清理
            window.URL.revokeObjectURL(url);
            document.body.removeChild(link);
            
            hideLoading();
            setButtonState(elements.generateBtn, false, '🚀 开始生成代码');
            
            // 显示成功消息
            showResult(true, `代码生成成功！文件已下载: ${filename}`);
        } else {
            // 尝试获取错误信息
            const errorText = await response.text();
            let errorMessage = '生成失败';
            
            try {
                const errorJson = JSON.parse(errorText);
                errorMessage = errorJson.message || errorJson.msg || errorText;
            } catch (e) {
                errorMessage = errorText || '生成失败';
            }
            
            hideLoading();
            setButtonState(elements.generateBtn, false, '🚀 开始生成代码');
            showResult(false, errorMessage);
        }
    } catch (error) {
        hideLoading();
        setButtonState(elements.generateBtn, false, '🚀 开始生成代码');
        showResult(false, '无法连接到服务器，请检查后端服务是否正常运行');
    }
}

/**
 * 校验并跳转到第三步
 */
function validateAndGoToStep3() {
    // 获取选中的数据库和表
    const selectedDatabase = typeof getSelectedValue !== 'undefined' ? 
        getSelectedValue('databaseName') : elements.databaseSelect.textContent.trim();
    const selectedTable = typeof getSelectedValue !== 'undefined' ? 
        getSelectedValue('tableName') : elements.tableSelect.textContent.trim();
    
    if (!selectedDatabase) {
        showResult(false, '请选择数据库');
        return;
    }
    
    if (!selectedTable) {
        showResult(false, '请选择表');
        return;
    }
    
    // 跳转到第三步
    goToStep(3);
}

/**
 * 重置应用
 */
function resetApp() {
    // 重置表单
    elements.connectionForm.reset();
    
    // 重置自定义下拉选择框
    if (typeof setSelectedValue !== 'undefined') {
        setSelectedValue('databaseName', '');
        setSelectedValue('tableName', '');
    } else {
        // 清空下拉列表
        elements.databaseSelect.innerHTML = '<option value="">请选择数据库</option>';
        elements.tableSelect.innerHTML = '<option value="">请选择表</option>';
    }
    
    // 清空包名
    elements.packageName.value = '';
    
    // 重置步骤
    goToStep(1);
    
    // 隐藏结果
    hideResult();
    
    // 重置数据
    databaseList = [];
    tableList = [];
}