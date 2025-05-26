/**
 * 格式化日期时间
 * @param {string|number|Date} timestamp - 时间戳或日期对象
 * @param {string} format - 格式化模式，默认为 'YYYY-MM-DD HH:mm:ss'
 * @returns {string} 格式化后的日期时间字符串
 */
export function formatTime(timestamp, format = 'YYYY-MM-DD HH:mm:ss') {
  if (!timestamp) return '';
  
  const date = timestamp instanceof Date ? timestamp : new Date(timestamp);
  
  if (isNaN(date.getTime())) return '';
  
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  const diffDays = Math.floor(diff / (1000 * 60 * 60 * 24));
  
  // 如果是今天
  if (diffDays === 0 && 
      date.getDate() === now.getDate() && 
      date.getMonth() === now.getMonth() && 
      date.getFullYear() === now.getFullYear()) {
    return format.includes('HH') ? formatDateByPattern(date, 'HH:mm') : '今天';
  }
  
  // 如果是昨天
  if (diffDays === 1 && 
      date.getDate() === now.getDate() - 1 && 
      date.getMonth() === now.getMonth() && 
      date.getFullYear() === now.getFullYear()) {
    return format.includes('HH') ? `昨天 ${formatDateByPattern(date, 'HH:mm')}` : '昨天';
  }
  
  // 如果是前天
  if (diffDays === 2 && 
      date.getDate() === now.getDate() - 2 && 
      date.getMonth() === now.getMonth() && 
      date.getFullYear() === now.getFullYear()) {
    return format.includes('HH') ? `前天 ${formatDateByPattern(date, 'HH:mm')}` : '前天';
  }
  
  // 如果是今年
  if (date.getFullYear() === now.getFullYear()) {
    return formatDateByPattern(date, format.replace('YYYY-', ''));
  }
  
  // 其他情况
  return formatDateByPattern(date, format);
}

/**
 * 格式化日期（仅日期部分）
 * @param {string|number|Date} timestamp - 时间戳或日期对象
 * @param {string} format - 格式化模式，默认为 'YYYY-MM-DD'
 * @returns {string} 格式化后的日期字符串
 */
export function formatDate(timestamp, format = 'YYYY-MM-DD') {
  if (!timestamp) return '';
  
  const date = timestamp instanceof Date ? timestamp : new Date(timestamp);
  
  if (isNaN(date.getTime())) return '';
  
  const now = new Date();
  
  // 如果是今天
  if (date.getDate() === now.getDate() && 
      date.getMonth() === now.getMonth() && 
      date.getFullYear() === now.getFullYear()) {
    return '今天';
  }
  
  // 如果是昨天
  if (date.getDate() === now.getDate() - 1 && 
      date.getMonth() === now.getMonth() && 
      date.getFullYear() === now.getFullYear()) {
    return '昨天';
  }
  
  // 如果是前天
  if (date.getDate() === now.getDate() - 2 && 
      date.getMonth() === now.getMonth() && 
      date.getFullYear() === now.getFullYear()) {
    return '前天';
  }
  
  // 如果是今年
  if (date.getFullYear() === now.getFullYear()) {
    return formatDateByPattern(date, format.replace('YYYY-', ''));
  }
  
  // 其他情况
  return formatDateByPattern(date, format);
}

/**
 * 根据模式格式化日期
 * @param {Date} date - 日期对象
 * @param {string} pattern - 格式化模式
 * @returns {string} 格式化后的日期字符串
 */
function formatDateByPattern(date, pattern) {
  const year = date.getFullYear();
  const month = date.getMonth() + 1;
  const day = date.getDate();
  const hours = date.getHours();
  const minutes = date.getMinutes();
  const seconds = date.getSeconds();
  
  return pattern
    .replace('YYYY', year)
    .replace('MM', padZero(month))
    .replace('DD', padZero(day))
    .replace('HH', padZero(hours))
    .replace('mm', padZero(minutes))
    .replace('ss', padZero(seconds));
}

/**
 * 数字补零
 * @param {number} num - 数字
 * @returns {string} 补零后的字符串
 */
function padZero(num) {
  return num < 10 ? `0${num}` : `${num}`;
} 