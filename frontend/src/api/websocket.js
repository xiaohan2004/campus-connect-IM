import SockJS from 'sockjs-client';
import Stomp from 'webstomp-client';
import { ElMessage } from 'element-plus';

let stompClient = null;
let socket = null;
let reconnectTimer = null;
let isReconnecting = false;
let messageCallback = null;
let statusCallback = null;
let authToken = null;

// WebSocket URL，确保与后端配置匹配
const websocketUrl = 'http://localhost:8080/ws';

// 连接状态变量
let connectionStatus = 'disconnected'; // 'disconnected', 'connecting', 'connected'
let connectionCheckInterval = null;

// 检查连接状态并在必要时重连
function startConnectionCheck() {
  // 清除之前的检查
  if (connectionCheckInterval) {
    clearInterval(connectionCheckInterval);
  }
  
  // 每30秒检查一次连接状态
  connectionCheckInterval = setInterval(() => {
    console.log('[WebSocket] 定期检查连接状态:', connectionStatus);
    
    if (connectionStatus === 'connected' && stompClient && !stompClient.connected) {
      // 连接状态不一致，可能连接已断开但状态未更新
      console.warn('[WebSocket] 检测到连接状态不一致，更新为断开状态');
      connectionStatus = 'disconnected';
      
      // 触发连接关闭事件
      window.dispatchEvent(new CustomEvent('websocket-closed'));
    }
    
    // 如果有token但未连接，尝试重连
    if (authToken && connectionStatus === 'disconnected' && !isReconnecting) {
      console.log('[WebSocket] 检测到未连接状态，尝试自动重连');
      connectWebSocket(authToken, messageCallback, statusCallback);
    }
  }, 30000);
}

// 连接WebSocket，token放URL参数
export function connectWebSocket(token, msgCallback, statCallback) {
  console.log('[WebSocket] 开始连接...', {
    url: websocketUrl,
    time: new Date().toISOString(),
    tokenValid: !!token && token.length > 10,
    currentStatus: connectionStatus
  });

  // 保存回调函数和token，用于重连
  messageCallback = msgCallback;
  statusCallback = statCallback;
  authToken = token;
  
  // 设置连接状态为正在连接
  connectionStatus = 'connecting';

  if (stompClient && stompClient.connected) {
    console.log('[WebSocket] 已连接，检查订阅状态...');
    // 检查订阅是否存在
    if (stompClient.subscriptions) {
      console.log('[WebSocket] 当前活跃的订阅:', Object.keys(stompClient.subscriptions));
    }
    connectionStatus = 'connected';
    return true;
  }
  
  // 如果正在重连，不要重复连接
  if (isReconnecting) {
    console.log('[WebSocket] 正在重连中，跳过连接过程');
    return false; // 返回连接状态
  }

  if (!token || token.trim() === '') {
    console.error('[WebSocket] 连接失败: Token为空');
    ElMessage.error('WebSocket连接失败: 认证Token为空');
    connectionStatus = 'disconnected';
    return false; // 返回连接状态
  }

  try {
    // 清除之前的重连定时器
    if (reconnectTimer) {
      clearTimeout(reconnectTimer);
      reconnectTimer = null;
    }

    // 带token的URL，放查询参数里
    const urlWithToken = `${websocketUrl}?token=${encodeURIComponent(token)}`;
    console.log('[WebSocket] 使用带token的SockJS URL:', urlWithToken);

    // 关闭之前的连接
    if (socket) {
      try {
        socket.close();
      } catch (e) {
        console.warn('[WebSocket] 关闭之前的连接失败:', e);
      }
    }

    socket = new SockJS(urlWithToken);

    // 添加 SockJS 事件监听
    socket.onopen = function() {
      console.log('[SockJS] 连接已打开');
    };

    socket.onmessage = function(e) {
      console.log('[SockJS] 收到消息:', e.data);
    };

    socket.onerror = function(e) {
      console.error('[SockJS] 发生错误:', e);
    };

    stompClient = Stomp.over(socket);

    // 增强调试日志
    stompClient.debug = function(str) {
      // 保留 CONNECT 和 SUBSCRIBE 相关的日志
      if (str.includes('CONNECT') || str.includes('SUBSCRIBE') || str.includes('MESSAGE')) {
        console.log('[STOMP Debug]', str);
      }
      // 过滤掉心跳日志
      if (str.includes('PING') || str.includes('PONG')) return;
      console.log('[STOMP]', str);
    };

    // 不传token，空头即可
    const headers = {};

    stompClient.heartbeat.outgoing = 20000; // 20秒发心跳
    stompClient.heartbeat.incoming = 20000; // 20秒收心跳

    stompClient.connect(
      headers,
      frame => {
        console.log('[WebSocket] 连接成功!', frame);
        console.log('[WebSocket] STOMP Session ID:', frame.headers['session-id']);
        console.log('[WebSocket] STOMP User:', frame.headers['user-name']);
        console.log('[WebSocket] STOMP 客户端状态:', {
          connected: stompClient.connected,
          subscriptions: stompClient.subscriptions ? Object.keys(stompClient.subscriptions) : []
        });
        
        isReconnecting = false;
        connectionStatus = 'connected';
        
        // 启动连接状态检查
        startConnectionCheck();
        
        try {
          console.log('[WebSocket] 开始订阅私聊消息...');
          const privateSubscription = stompClient.subscribe('/user/queue/private.message', async message => {
            console.log('[WebSocket] 收到私聊消息 - 完整消息对象:', message);
            console.log('[WebSocket] 消息头部信息:', message.headers);
            console.log('[WebSocket] 消息目标地址:', message.headers.destination);
            try {
              const receivedMessage = JSON.parse(message.body);
              console.log('[WebSocket] 解析后的私聊消息内容:', receivedMessage);
              if (messageCallback) {
                await Promise.resolve(messageCallback(receivedMessage, 'private'));
              }
              return false;
            } catch (error) {
              console.error('[WebSocket] 解析私聊消息失败:', error, message);
              return false;
            }
          });
          console.log('[WebSocket] 私聊消息订阅ID:', privateSubscription.id);
          console.log('[WebSocket] 私聊消息订阅详情:', privateSubscription);
          
          console.log('[WebSocket] 开始订阅群聊消息...');
          // 订阅群聊消息
          stompClient.subscribe('/user/queue/group.message', async message => {
            console.log('[WebSocket] 收到群聊消息:', message);
            try {
              const receivedMessage = JSON.parse(message.body);
              if (messageCallback) {
                await Promise.resolve(messageCallback(receivedMessage, 'group'));
              }
              return false; // 显式返回 false 表示不是异步操作
            } catch (error) {
              console.error('[WebSocket] 解析群聊消息失败:', error, message);
              return false;
            }
          });
          
          console.log('[WebSocket] 开始订阅消息已读回执...');
          // 订阅消息已读回执
          stompClient.subscribe('/user/queue/message.read', async message => {
            console.log('[WebSocket] 收到已读回执:', message);
            try {
              const readReceipt = JSON.parse(message.body);
              if (messageCallback) {
                await Promise.resolve(messageCallback(readReceipt, 'read'));
              }
              return false;
            } catch (error) {
              console.error('[WebSocket] 解析已读回执失败:', error, message);
              return false;
            }
          });
          
          console.log('[WebSocket] 开始订阅用户在线状态...');
          // 订阅用户在线状态
          stompClient.subscribe('/topic/user.status', async message => {
            console.log('[WebSocket] 收到用户状态更新:', message);
            try {
              const statusUpdate = JSON.parse(message.body);
              if (statusCallback) {
                await Promise.resolve(statusCallback(statusUpdate));
              }
              return false;
            } catch (error) {
              console.error('[WebSocket] 解析用户状态更新失败:', error, message);
              return false;
            }
          });
          
          console.log('[WebSocket] 开始订阅消息撤回...');
          // 订阅消息撤回
          stompClient.subscribe('/user/queue/message.recall', async message => {
            console.log('[WebSocket] 收到消息撤回:', message);
            try {
              const recallMessage = JSON.parse(message.body);
              if (messageCallback) {
                await Promise.resolve(messageCallback(recallMessage, 'recall'));
              }
              return false;
            } catch (error) {
              console.error('[WebSocket] 解析消息撤回失败:', error, message);
              return false;
            }
          });
          
          console.log('[WebSocket] 发送用户在线状态...');
          // 发送上线状态
          const statusSent = sendUserStatus();
          console.log('[WebSocket] 用户状态发送结果:', statusSent);
        } catch (subscribeError) {
          console.error('[WebSocket] 订阅消息主题失败:', subscribeError);
          console.error('[WebSocket] 订阅时的STOMP客户端状态:', {
            connected: stompClient.connected,
            subscriptions: stompClient.subscriptions ? Object.keys(stompClient.subscriptions) : []
          });
        }
        
        // 发送连接成功事件
        window.dispatchEvent(new CustomEvent('websocket-connected'));
        
        return true; // 连接成功
      },
      error => {
        console.error('[WebSocket] 连接失败!', error);
        console.error('[WebSocket] 连接失败时的STOMP客户端状态:', {
          connected: stompClient?.connected,
          subscriptions: stompClient?.subscriptions ? Object.keys(stompClient.subscriptions) : []
        });
        connectionStatus = 'disconnected';
        
        if (error.body && error.body.includes('Unauthorized')) {
          console.error('[WebSocket] 认证失败，请检查token是否有效');
          ElMessage.error('WebSocket连接失败: 认证失败，请重新登录');
          
          // 如果是认证问题，可能需要重新登录
          window.dispatchEvent(new CustomEvent('websocket-auth-error'));
        } else if (error.body && error.body.includes('timeout')) {
          console.error('[WebSocket] 连接超时，服务器可能未响应');
          ElMessage.error('WebSocket连接超时，请检查网络连接或服务器状态');
        } else {
          ElMessage.error('WebSocket连接失败，请检查网络连接');
        }
        
        // 尝试网络诊断
        checkServerAvailability();
        
        // 设置重连标志
        isReconnecting = true;
        
        // 延迟5秒后重连
        reconnectTimer = setTimeout(() => {
          console.log('[WebSocket] 尝试重新连接...');
          connectWebSocket(authToken, messageCallback, statusCallback);
        }, 5000);
        
        return false; // 连接失败
      }
    );
    
    // 添加连接关闭事件处理
    socket.onclose = function() {
      console.log('[WebSocket] 连接已关闭');
      connectionStatus = 'disconnected';
      
      // 发送连接关闭事件
      window.dispatchEvent(new CustomEvent('websocket-closed'));
      
      // 如果不是主动断开连接，尝试重连
      if (authToken && !isReconnecting) {
        console.log('[WebSocket] 连接意外关闭，5秒后尝试重连');
        isReconnecting = true;
        
        reconnectTimer = setTimeout(() => {
          console.log('[WebSocket] 尝试重新连接...');
          connectWebSocket(authToken, messageCallback, statusCallback);
        }, 5000);
      }
    };
    
    return true; // 初始化成功
  } catch (error) {
    console.error('[WebSocket] 初始化连接时发生异常:', error);
    ElMessage.error(`WebSocket初始化失败: ${error.message}`);
    
    connectionStatus = 'disconnected';
    
    // 设置重连标志
    isReconnecting = true;
    
    // 延迟5秒后重连
    reconnectTimer = setTimeout(() => {
      console.log('[WebSocket] 尝试重新连接...');
      connectWebSocket(authToken, messageCallback, statusCallback);
    }, 5000);
    
    return false; // 初始化失败
  }
}

// 检查服务器可用性
function checkServerAvailability() {
  console.log('[WebSocket] 正在检查服务器可用性...');
  
  // 使用fetch API检查服务器是否在线
  fetch('http://localhost:8080/api/health', { method: 'GET' })
    .then(response => {
      if (response.ok) {
        console.log('[WebSocket] 服务器健康检查成功，API端点可访问');
      } else {
        console.warn('[WebSocket] 服务器返回非200状态码:', response.status);
      }
    })
    .catch(error => {
      console.error('[WebSocket] 服务器健康检查失败，API端点不可访问:', error);
      console.error('[WebSocket] 可能原因: 服务器未启动、网络问题或CORS限制');
    });
}

// 断开WebSocket连接
export function disconnectWebSocket() {
  console.log('[WebSocket] 开始断开连接...');
  
  // 清除重连定时器
  if (reconnectTimer) {
    clearTimeout(reconnectTimer);
    reconnectTimer = null;
  }
  
  // 清除连接检查定时器
  if (connectionCheckInterval) {
    clearInterval(connectionCheckInterval);
    connectionCheckInterval = null;
  }
  
  // 清除回调和token
  messageCallback = null;
  statusCallback = null;
  authToken = null;
  isReconnecting = false;
  connectionStatus = 'disconnected';
  
  if (stompClient) {
    try {
      console.log('[WebSocket] 断开STOMP客户端...');
      stompClient.disconnect(() => {
        console.log('[WebSocket] STOMP客户端已成功断开');
      });
      stompClient = null;
    } catch (error) {
      console.error('[WebSocket] 断开STOMP客户端时发生错误:', error);
    }
  }
  
  if (socket) {
    try {
      console.log('[WebSocket] 关闭SockJS连接...');
      socket.close();
      socket = null;
    } catch (error) {
      console.error('[WebSocket] 关闭SockJS连接时发生错误:', error);
    }
  }
  
  console.log('[WebSocket] 已完全断开连接');
}

// 发送私聊消息
export function sendPrivateMessage(data) {
  console.log('[WebSocket] 准备发送私聊消息:', data);
  
  if (!stompClient || !stompClient.connected) {
    console.error('[WebSocket] 未连接，无法发送私聊消息');
    ElMessage.error('WebSocket未连接，无法发送消息');
    return false;
  }
  
  // 验证必要的字段
  if (!data.receiverId) {
    console.error('[WebSocket] 发送私聊消息失败: 缺少接收者ID');
    return false;
  }
  
  if (!data.contentType) {
    console.error('[WebSocket] 发送私聊消息失败: 缺少内容类型');
    return false;
  }
  
  if (!data.content) {
    console.error('[WebSocket] 发送私聊消息失败: 缺少消息内容');
    return false;
  }
  
  try {
    stompClient.send('/app/private.message', JSON.stringify(data));
    console.log('[WebSocket] 私聊消息发送成功');
    return true;
  } catch (error) {
    console.error('[WebSocket] 发送私聊消息失败:', error);
    ElMessage.error(`发送消息失败: ${error.message}`);
    return false;
  }
}

// 发送群聊消息
export function sendGroupMessage(data) {
  console.log('[WebSocket] 准备发送群聊消息:', data);
  
  if (!stompClient || !stompClient.connected) {
    console.error('[WebSocket] 未连接，无法发送群聊消息');
    ElMessage.error('WebSocket未连接，无法发送消息');
    return false;
  }
  
  // 验证必要的字段
  if (!data.groupId) {
    console.error('[WebSocket] 发送群聊消息失败: 缺少群组ID');
    return false;
  }
  
  if (!data.contentType) {
    console.error('[WebSocket] 发送群聊消息失败: 缺少内容类型');
    return false;
  }
  
  if (!data.content) {
    console.error('[WebSocket] 发送群聊消息失败: 缺少消息内容');
    return false;
  }
  
  try {
    stompClient.send('/app/group.message', JSON.stringify(data));
    console.log('[WebSocket] 群聊消息发送成功');
    return true;
  } catch (error) {
    console.error('[WebSocket] 发送群聊消息失败:', error);
    ElMessage.error(`发送消息失败: ${error.message}`);
    return false;
  }
}

// 发送消息已读回执
export function sendMessageReadReceipt(messageId) {
  console.log('[WebSocket] 准备发送消息已读回执:', messageId);
  
  if (!stompClient || !stompClient.connected) {
    console.error('[WebSocket] 未连接，无法发送已读回执');
    ElMessage.error('WebSocket未连接，无法发送已读回执');
    return false;
  }
  
  try {
    stompClient.send('/app/message.read', JSON.stringify({ messageId }));
    console.log('[WebSocket] 已读回执发送成功');
    return true;
  } catch (error) {
    console.error('[WebSocket] 发送已读回执失败:', error);
    return false;
  }
}

// 发送用户在线状态
export function sendUserStatus() {
  console.log('[WebSocket] 准备发送用户在线状态');
  
  if (!stompClient || !stompClient.connected) {
    console.error('[WebSocket] 未连接，无法发送用户状态');
    return false;
  }
  
  try {
    stompClient.send('/app/user.status', JSON.stringify({}));
    console.log('[WebSocket] 用户在线状态发送成功');
    return true;
  } catch (error) {
    console.error('[WebSocket] 发送用户在线状态失败:', error);
    return false;
  }
}

// 发送消息撤回请求
export function sendMessageRecall(messageId, conversationType, targetId) {
  console.log('[WebSocket] 准备发送消息撤回请求:', { messageId, conversationType, targetId });
  
  if (!stompClient || !stompClient.connected) {
    console.error('[WebSocket] 未连接，无法撤回消息');
    ElMessage.error('WebSocket未连接，无法撤回消息');
    return false;
  }
  
  // 验证必要的字段
  if (!messageId) {
    console.error('[WebSocket] 发送消息撤回请求失败: 缺少消息ID');
    return false;
  }
  
  if (conversationType === undefined) {
    console.error('[WebSocket] 发送消息撤回请求失败: 缺少会话类型');
    return false;
  }
  
  if (!targetId) {
    console.error('[WebSocket] 发送消息撤回请求失败: 缺少目标ID');
    return false;
  }
  
  const data = {
    messageId,
    conversationType,
    // 根据会话类型添加不同的字段
    ...(conversationType === 0 ? { receiverId: targetId } : { groupId: targetId })
  };
  
  try {
    stompClient.send('/app/message.recall', JSON.stringify(data));
    console.log('[WebSocket] 消息撤回请求发送成功');
    return true;
  } catch (error) {
    console.error('[WebSocket] 发送消息撤回请求失败:', error);
    return false;
  }
}

// 检查WebSocket连接状态
export function isWebSocketConnected() {
  const connected = stompClient && stompClient.connected;
  console.log('[WebSocket] 检查连接状态:', connected ? '已连接' : '未连接', '内部状态:', connectionStatus);
  return connected;
}

// 获取连接状态
export function getConnectionStatus() {
  return connectionStatus;
} 