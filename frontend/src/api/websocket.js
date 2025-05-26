import SockJS from 'sockjs-client';
import Stomp from 'webstomp-client';
import { ElMessage } from 'element-plus';

let stompClient = null;
let socket = null;
const websocketUrl = 'http://localhost:8080/ws';

// 连接WebSocket
export function connectWebSocket(token, messageCallback, statusCallback) {
  if (stompClient && stompClient.connected) {
    return;
  }

  // 创建SockJS实例
  socket = new SockJS(websocketUrl);
  // 创建STOMP客户端
  stompClient = Stomp.over(socket);
  
  // 连接WebSocket服务器
  stompClient.connect(
    { 'Authorization': `Bearer ${token}` },
    frame => {
      console.log('WebSocket连接成功:', frame);
      
      // 订阅私聊消息
      stompClient.subscribe('/user/queue/private.message', message => {
        const receivedMessage = JSON.parse(message.body);
        messageCallback && messageCallback(receivedMessage, 'private');
      });
      
      // 订阅群聊消息
      stompClient.subscribe('/user/queue/group.message', message => {
        const receivedMessage = JSON.parse(message.body);
        messageCallback && messageCallback(receivedMessage, 'group');
      });
      
      // 订阅消息已读回执
      stompClient.subscribe('/user/queue/message.read', message => {
        const readReceipt = JSON.parse(message.body);
        messageCallback && messageCallback(readReceipt, 'read');
      });
      
      // 订阅用户在线状态
      stompClient.subscribe('/topic/user.status', message => {
        const statusUpdate = JSON.parse(message.body);
        statusCallback && statusCallback(statusUpdate);
      });
      
      // 订阅消息撤回
      stompClient.subscribe('/user/queue/message.recall', message => {
        const recallMessage = JSON.parse(message.body);
        messageCallback && messageCallback(recallMessage, 'recall');
      });
      
      // 发送上线状态
      sendUserStatus();
    },
    error => {
      console.error('WebSocket连接失败:', error);
      ElMessage.error('WebSocket连接失败，请检查网络连接');
    }
  );
  
  // 设置心跳检测
  stompClient.heartbeat.outgoing = 10000; // 10秒发送一次心跳
  stompClient.heartbeat.incoming = 10000; // 10秒接收一次心跳
}

// 断开WebSocket连接
export function disconnectWebSocket() {
  if (stompClient) {
    stompClient.disconnect();
    stompClient = null;
  }
  if (socket) {
    socket.close();
    socket = null;
  }
  console.log('WebSocket已断开连接');
}

// 发送私聊消息
export function sendPrivateMessage(data) {
  if (!stompClient || !stompClient.connected) {
    ElMessage.error('WebSocket未连接，无法发送消息');
    return false;
  }
  
  stompClient.send('/app/private.message', JSON.stringify(data));
  return true;
}

// 发送群聊消息
export function sendGroupMessage(data) {
  if (!stompClient || !stompClient.connected) {
    ElMessage.error('WebSocket未连接，无法发送消息');
    return false;
  }
  
  stompClient.send('/app/group.message', JSON.stringify(data));
  return true;
}

// 发送消息已读回执
export function sendMessageReadReceipt(messageId) {
  if (!stompClient || !stompClient.connected) {
    ElMessage.error('WebSocket未连接，无法发送已读回执');
    return false;
  }
  
  stompClient.send('/app/message.read', JSON.stringify({ messageId }));
  return true;
}

// 发送用户在线状态
export function sendUserStatus() {
  if (!stompClient || !stompClient.connected) {
    return false;
  }
  
  stompClient.send('/app/user.status', JSON.stringify({}));
  return true;
}

// 发送消息撤回请求
export function sendMessageRecall(messageId) {
  if (!stompClient || !stompClient.connected) {
    ElMessage.error('WebSocket未连接，无法撤回消息');
    return false;
  }
  
  stompClient.send('/app/message.recall', JSON.stringify({ messageId }));
  return true;
}

// 检查WebSocket连接状态
export function isWebSocketConnected() {
  return stompClient && stompClient.connected;
} 