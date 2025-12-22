/**
 * 自定义下拉选择框组件
 */

let currentHighlightIndex = -1; // 当前高亮选项的索引
let originalDataList = {}; // 存储原始数据列表

/**
 * 初始化自定义下拉选择框
 */
function initCustomSelect(selectId, dropdownId, dataList) {
    const selectElement = document.getElementById(selectId);
    const dropdown = document.getElementById(dropdownId);
    
    // 存储原始数据列表
    originalDataList[selectId] = [...dataList];
    
    // 设置占位符
    selectElement.setAttribute('data-placeholder', '请选择');
    
    // 如果已有选中值，设置显示文本
    if (selectElement.dataset.value) {
        selectElement.textContent = selectElement.dataset.value;
    }
    
    // 点击选择框显示下拉列表
    selectElement.addEventListener('click', (e) => {
        // 阻止事件冒泡，避免立即关闭下拉列表
        e.stopPropagation();
        toggleDropdown(selectId, dropdownId);
    });
    
    // 输入事件处理
    selectElement.addEventListener('input', () => {
        handleInput(selectId, dropdownId);
    });
    
    // 焦点事件处理
    selectElement.addEventListener('focus', () => {
        // 重新渲染下拉列表，基于当前输入内容进行过滤
        const inputValue = selectElement.textContent.trim();
        const filteredData = filterData(originalDataList[selectId], inputValue);
        renderDropdown(selectId, dropdownId, filteredData);
    });
    
    // 失去焦点事件处理
    selectElement.addEventListener('blur', (e) => {
        // 延迟处理，确保点击选项时不会因为失去焦点而关闭
        setTimeout(() => {
            if (!dropdown.contains(document.activeElement) && dropdown !== document.activeElement) {
                dropdown.classList.remove('show');
                currentHighlightIndex = -1;
                
                // 如果输入的内容不在选项中，清空输入或恢复原值
                const inputValue = selectElement.textContent.trim();
                if (inputValue && !originalDataList[selectId].includes(inputValue)) {
                    // 如果有选中值，恢复选中值；否则清空
                    if (selectElement.dataset.value) {
                        selectElement.textContent = selectElement.dataset.value;
                    } else {
                        selectElement.textContent = '';
                    }
                }
            }
        }, 150);
    });
    
    // 键盘导航
    selectElement.addEventListener('keydown', (e) => {
        handleKeyboardNavigation(e, selectId, dropdownId);
    });
    
    // 点击外部关闭下拉列表
    document.addEventListener('click', (e) => {
        if (!selectElement.contains(e.target) && !dropdown.contains(e.target)) {
            dropdown.classList.remove('show');
            currentHighlightIndex = -1;
        }
    });
    
    // 渲染下拉列表
    renderDropdown(selectId, dropdownId, dataList);
}

/**
 * 处理输入事件
 */
function handleInput(selectId, dropdownId) {
    const selectElement = document.getElementById(selectId);
    const inputValue = selectElement.textContent.trim();
    
    // 根据输入内容过滤数据
    const filteredData = filterData(originalDataList[selectId], inputValue);
    
    // 显示下拉列表
    const dropdown = document.getElementById(dropdownId);
    dropdown.classList.add('show');
    
    // 重新渲染下拉列表
    renderDropdown(selectId, dropdownId, filteredData);
    
    // 重置高亮索引
    currentHighlightIndex = -1;
}

/**
 * 过滤数据
 */
function filterData(dataList, searchTerm) {
    if (!searchTerm) {
        return dataList;
    }
    
    return dataList.filter(item => 
        item.toLowerCase().includes(searchTerm.toLowerCase())
    );
}

/**
 * 切换下拉列表显示状态
 */
function toggleDropdown(selectId, dropdownId) {
    const dropdown = document.getElementById(dropdownId);
    const selectElement = document.getElementById(selectId);
    
    if (dropdown.classList.contains('show')) {
        dropdown.classList.remove('show');
        currentHighlightIndex = -1;
    } else {
        // 获取当前输入内容并过滤数据
        const inputValue = selectElement.textContent.trim();
        const filteredData = filterData(originalDataList[selectId], inputValue);
        
        currentHighlightIndex = -1;
        renderDropdown(selectId, dropdownId, filteredData);
        dropdown.classList.add('show');
    }
}

/**
 * 处理键盘导航
 */
function handleKeyboardNavigation(e, selectId, dropdownId) {
    const dropdown = document.getElementById(dropdownId);
    const options = dropdown.querySelectorAll('.custom-select-option:not(.no-result)');
    
    if (!dropdown.classList.contains('show') || options.length === 0) {
        // 如果下拉列表未显示，则按Enter键时显示
        if (e.key === 'Enter') {
            e.preventDefault();
            toggleDropdown(selectId, dropdownId);
        }
        return;
    }
    
    switch(e.key) {
        case 'ArrowDown':
            e.preventDefault();
            currentHighlightIndex = (currentHighlightIndex + 1) % options.length;
            updateHighlight(options);
            scrollToHighlighted(dropdown, options[currentHighlightIndex]);
            break;
            
        case 'ArrowUp':
            e.preventDefault();
            currentHighlightIndex = currentHighlightIndex <= 0 ? options.length - 1 : currentHighlightIndex - 1;
            updateHighlight(options);
            scrollToHighlighted(dropdown, options[currentHighlightIndex]);
            break;
            
        case 'Enter':
            e.preventDefault();
            if (currentHighlightIndex >= 0 && currentHighlightIndex < options.length) {
                const selectedValue = options[currentHighlightIndex].textContent;
                selectOption(selectId, selectedValue);
                dropdown.classList.remove('show');
                currentHighlightIndex = -1;
            } else if (options.length > 0) {
                // 如果没有高亮选项但有选项，选择第一个
                const selectedValue = options[0].textContent;
                selectOption(selectId, selectedValue);
                dropdown.classList.remove('show');
                currentHighlightIndex = -1;
            }
            break;
            
        case 'Escape':
            e.preventDefault();
            dropdown.classList.remove('show');
            currentHighlightIndex = -1;
            break;
    }
}

/**
 * 渲染下拉列表
 */
function renderDropdown(selectId, dropdownId, dataList) {
    const dropdown = document.getElementById(dropdownId);
    
    if (dataList.length === 0) {
        dropdown.innerHTML = '<div class="custom-select-option no-result">未找到匹配结果</div>';
        return;
    }
    
    dropdown.innerHTML = '';
    dataList.forEach(item => {
        const option = document.createElement('div');
        option.className = 'custom-select-option';
        option.textContent = item;
        
        // 检查是否已选中
        const selectElement = document.getElementById(selectId);
        const currentValue = selectElement.dataset.value;
        if (currentValue === item) {
            option.classList.add('selected');
        }
        
        option.addEventListener('click', (e) => {
            e.stopPropagation();
            selectOption(selectId, item);
            dropdown.classList.remove('show');
            currentHighlightIndex = -1;
        });
        
        dropdown.appendChild(option);
    });
}

/**
 * 选择选项
 */
function selectOption(selectId, value) {
    const selectElement = document.getElementById(selectId);
    
    // 更新显示文本
    selectElement.textContent = value;
    
    // 保存选中值
    selectElement.dataset.value = value;
    
    // 触发change事件
    const event = new Event('change');
    selectElement.dispatchEvent(event);
}

/**
 * 更新高亮选项
 */
function updateHighlight(options) {
    options.forEach((option, index) => {
        if (index === currentHighlightIndex) {
            option.classList.add('highlighted');
        } else {
            option.classList.remove('highlighted');
        }
    });
}

/**
 * 滚动到高亮选项
 */
function scrollToHighlighted(dropdown, highlightedOption) {
    if (highlightedOption) {
        const dropdownRect = dropdown.getBoundingClientRect();
        const optionRect = highlightedOption.getBoundingClientRect();
        
        if (optionRect.bottom > dropdownRect.bottom) {
            dropdown.scrollTop += optionRect.bottom - dropdownRect.bottom;
        } else if (optionRect.top < dropdownRect.top) {
            dropdown.scrollTop -= dropdownRect.top - optionRect.top;
        }
    }
}

/**
 * 获取选中值
 */
function getSelectedValue(selectId) {
    const selectElement = document.getElementById(selectId);
    return selectElement.dataset.value || '';
}

/**
 * 设置选中值
 */
function setSelectedValue(selectId, value) {
    const selectElement = document.getElementById(selectId);
    selectElement.dataset.value = value;
    selectElement.textContent = value || '';
}