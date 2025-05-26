import SockJS from 'sockjs-client';
import Stomp from 'webstomp-client';
import { ElMessage } from 'element-plus';

let stompClient = null;
let socket = null;
// WebSocket URL，确保与后端配置匹配
const websocketUrl = 'http://localhost:8080/ws';

// 连接WebSocket，token放URL参数
export function connectWebSocket(token, messageCallback, statusCallback) {
  console.log('[WebSocket] 开始连接...', {
    url: websocketUrl,
    time: new Date().toISOString(),
    tokenValid: !!token && token.length > 10
  });

  if (stompClient && stompClient.connected) {
    console.log('[WebSocket] 已连接，跳过连接过程');
    return;
  }

  if (!token || token.trim() === '') {
    console.error('[WebSocket] 连接失败: Token为空');
    ElMessage.error('WebSocket连接失败: 认证Token为空');
    return;
  }

  try {
    // 带token的URL，放查询参数里
    const urlWithToken = `${websocketUrl}?token=${encodeURIComponent(token)}`;
    console.log('[WebSocket] 使用带token的SockJS URL:', urlWithToken);

    socket = new SockJS(urlWithToken);

    stompClient = Stomp.over(socket);

    // 调试日志，过滤心跳
    stompClient.debug = function(str) {
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
        
        try {
          console.log('[WebSocket] 开始订阅私聊消息...');
          // 订阅私聊消息
          stompClient.subscribe('/user/queue/private.message', message => {
            console.log('[WebSocket] 收到私聊消息:', message);
            try {
              const receivedMessage = JSON.parse(message.body);
              messageCallback && messageCallback(receivedMessage, 'private');
            } catch (error) {
              console.error('[WebSocket] 解析私聊消息失败:', error, message);
            }
          });
          
          console.log('[WebSocket] 开始订阅群聊消息...');
          // 订阅群聊消息
          stompClient.subscribe('/user/queue/group.message', message => {
            console.log('[WebSocket] 收到群聊消息:', message);
            try {
              const receivedMessage = JSON.parse(message.body);
              messageCallback && messageCallback(receivedMessage, 'group');
            } catch (error) {
              console.error('[WebSocket] 解析群聊消息失败:', error, message);
            }
          });
          
          console.log('[WebSocket] 开始订阅消息已读回执...');
          // 订阅消息已读回执
          stompClient.subscribe('/user/queue/message.read', message => {
            console.log('[WebSocket] 收到已读回执:', message);
            try {
              const readReceipt = JSON.parse(message.body);
              messageCallback && messageCallback(readReceipt, 'read');
            } catch (error) {
              console.error('[WebSocket] 解析已读回执失败:', error, message);
            }
          });
          
          console.log('[WebSocket] 开始订阅用户在线状态...');
          // 订阅用户在线状态
          stompClient.subscribe('/topic/user.status', message => {
            console.log('[WebSocket] 收到用户状态更新:', message);
            try {
              const statusUpdate = JSON.parse(message.body);
              statusCallback && statusCallback(statusUpdate);
            } catch (error) {
              console.error('[WebSocket] 解析用户状态更新失败:', error, message);
            }
          });
          
          console.log('[WebSocket] 开始订阅消息撤回...');
          // 订阅消息撤回
          stompClient.subscribe('/user/queue/message.recall', message => {
            console.log('[WebSocket] 收到消息撤回:', message);
            try {
              const recallMessage = JSON.parse(message.body);
              messageCallback && messageCallback(recallMessage, 'recall');
            } catch (error) {
              console.error('[WebSocket] 解析消息撤回失败:', error, message);
            }
          });
          
          console.log('[WebSocket] 发送用户在线状态...');
          // 发送上线状态
          const statusSent = sendUserStatus();
          console.log('[WebSocket] 用户状态发送结果:', statusSent);
        } catch (subscribeError) {
          console.error('[WebSocket] 订阅消息主题失败:', subscribeError);
        }
      },
      error => {
        console.error('[WebSocket] 连接失败!', error);
        if (error.body && error.body.includes('Unauthorized')) {
          console.error('[WebSocket] 认证失败，请检查token是否有效');
          ElMessage.error('WebSocket连接失败: 认证失败，请重新登录');
        } else if (error.body && error.body.includes('timeout')) {
          console.error('[WebSocket] 连接超时，服务器可能未响应');
          ElMessage.error('WebSocket连接超时，请检查网络连接或服务器状态');
        } else {
          ElMessage.error('WebSocket连接失败，请检查网络连接');
        }
        
        // 尝试网络诊断
        checkServerAvailability();
      }
    );
  } catch (error) {
    console.error('[WebSocket] 初始化连接时发生异常:', error);
    ElMessage.error(`WebSocket初始化失败: ${error.message}`);
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
export function sendMessageRecall(messageId) {
  console.log('[WebSocket] 准备发送消息撤回请求:', messageId);
  
  if (!stompClient || !stompClient.connected) {
    console.error('[WebSocket] 未连接，无法撤回消息');
    ElMessage.error('WebSocket未连接，无法撤回消息');
    return false;
  }
  
  try {
    stompClient.send('/app/message.recall', JSON.stringify({ messageId }));
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
  console.log('[WebSocket] 检查连接状态:', connected ? '已连接' : '未连接');
  return connected;
} 