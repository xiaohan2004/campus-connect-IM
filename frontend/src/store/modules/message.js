import { 
  sendPrivateMessage, 
  sendGroupMessage, 
  getPrivateMessages, 
  getGroupMessages,
  markMessageAsRead,
  recallMessage,
  deleteMessage,
  getUnreadMessageCount,
  getOfflineMessages,
  confirmOfflineMessages
} from '@/api/message';
import { 
  sendPrivateMessage as wsSendPrivateMessage, 
  sendGroupMessage as wsSendGroupMessage,
  sendMessageReadReceipt,
  sendMessageRecall
} from '@/api/websocket';

const state = {
  messages: {}, // 按会话ID分组的消息列表
  unreadCount: 0,
  offlineMessages: []
};

const mutations = {
  SET_MESSAGES(state, { conversationId, messages }) {
    console.log('[Message Store] 设置消息列表:', { conversationId, messageCount: messages?.length || 0 });
    
    if (!messages) {
      console.warn('[Message Store] 消息列表为空');
      return;
    }
    
    // 确保消息按时间升序排列（从旧到新）
    const sortedMessages = [...messages].sort((a, b) => new Date(a.sendTime) - new Date(b.sendTime));
    
    // Vue.set 确保响应式
    state.messages = {
      ...state.messages,
      [conversationId]: sortedMessages
    };
    
    console.log('[Message Store] 设置后的消息列表长度:', state.messages[conversationId]?.length || 0);
  },
  
  ADD_MESSAGE(state, { conversationId, message }) {
    console.log('[Message Store] 添加消息:', { conversationId, message });
    
    if (!message) {
      console.warn('[Message Store] 消息为空，无法添加');
      return;
    }
    
    // 确保该会话的消息列表已初始化
    if (!state.messages[conversationId]) {
      state.messages = {
        ...state.messages,
        [conversationId]: []
      };
    }
    
    // 检查消息是否已存在（避免重复添加）
    const isDuplicate = state.messages[conversationId].some(msg => 
      msg.messageId === message.messageId || 
      (msg.tempId && msg.tempId === message.tempId)
    );
    
    if (isDuplicate) {
      console.warn('[Message Store] 消息已存在，跳过添加:', message);
      return;
    }
    
    // 添加消息
    state.messages[conversationId].push(message);
    
    // 按时间排序
    state.messages[conversationId].sort((a, b) => new Date(a.sendTime) - new Date(b.sendTime));
    
    console.log('[Message Store] 添加后的消息列表长度:', state.messages[conversationId].length);
  },
  UPDATE_MESSAGE(state, { conversationId, messageId, data }) {
    if (state.messages[conversationId]) {
      const index = state.messages[conversationId].findIndex(msg => msg.messageId === messageId);
      if (index !== -1) {
        state.messages[conversationId][index] = { 
          ...state.messages[conversationId][index], 
          ...data 
        };
      }
    }
  },
  REMOVE_MESSAGE(state, { conversationId, messageId }) {
    if (state.messages[conversationId]) {
      state.messages[conversationId] = state.messages[conversationId].filter(
        msg => msg.messageId !== messageId
      );
    }
  },
  SET_UNREAD_COUNT(state, count) {
    state.unreadCount = count;
  },
  SET_OFFLINE_MESSAGES(state, messages) {
    state.offlineMessages = messages;
  },
  CLEAR_OFFLINE_MESSAGES(state) {
    state.offlineMessages = [];
  },
  CLEAR_MESSAGES(state) {
    state.messages = {};
    state.unreadCount = 0;
    state.offlineMessages = [];
  }
};

const actions = {
  // 发送私聊消息
  sendPrivateMessage({ commit, dispatch, rootState }, { receiverId, contentType, content }) {
    return new Promise((resolve, reject) => {
      // 获取当前会话ID
      const conversationId = rootState.conversation.currentConversation?.id;
      if (!conversationId) {
        reject(new Error('当前会话ID不存在'));
        return;
      }
      
      // 获取当前用户ID
      const currentUserId = rootState.user.userInfo?.id;
      if (!currentUserId) {
        reject(new Error('当前用户ID不存在'));
        return;
      }
      
      // 创建临时消息对象（用于本地显示）
      const tempMessage = {
        messageId: 'temp-' + Date.now(), // 临时ID，后续会被服务器返回的ID替换
        senderId: currentUserId,
        receiverId: receiverId,
        conversationId: conversationId,
        contentType: contentType,
        content: content,
        timestamp: new Date().toISOString(),
        isRead: false,
        isRecalled: false,
        sender: currentUserId // 为了兼容现有代码
      };
      
      // 尝试通过WebSocket发送
      const wsSuccess = wsSendPrivateMessage({ receiverId, contentType, content });
      
      // 无论WebSocket是否成功，先在本地添加消息（提高用户体验）
      commit('ADD_MESSAGE', { conversationId, message: tempMessage });
      
      // 更新会话的最后一条消息
      dispatch('conversation/updateLastMessage', {
        conversationId,
        message: {
          content,
          timestamp: tempMessage.timestamp
        }
      }, { root: true });
      
      // 如果WebSocket发送失败，使用HTTP请求发送
      if (!wsSuccess) {
        sendPrivateMessage({ receiverId, contentType, content })
          .then(response => {
            const { data } = response;
            // 更新临时消息ID为服务器返回的ID
            if (data && data.messageId) {
              commit('UPDATE_MESSAGE', {
                conversationId,
                messageId: tempMessage.messageId,
                data: { messageId: data.messageId }
              });
            }
            resolve(data);
          })
          .catch(error => {
            // 发送失败，标记消息为发送失败状态
            commit('UPDATE_MESSAGE', {
              conversationId,
              messageId: tempMessage.messageId,
              data: { sendFailed: true }
            });
            reject(error);
          });
      } else {
        resolve(tempMessage);
      }
    });
  },

  // 发送群聊消息
  sendGroupMessage({ commit, dispatch, rootState }, { groupId, contentType, content }) {
    return new Promise((resolve, reject) => {
      // 获取当前会话ID
      const conversationId = rootState.conversation.currentConversation?.id;
      if (!conversationId) {
        reject(new Error('当前会话ID不存在'));
        return;
      }
      
      // 获取当前用户ID
      const currentUserId = rootState.user.userInfo?.id;
      if (!currentUserId) {
        reject(new Error('当前用户ID不存在'));
        return;
      }
      
      // 创建临时消息对象（用于本地显示）
      const tempMessage = {
        messageId: 'temp-' + Date.now(), // 临时ID，后续会被服务器返回的ID替换
        senderId: currentUserId,
        groupId: groupId,
        conversationId: conversationId,
        contentType: contentType,
        content: content,
        timestamp: new Date().toISOString(),
        isRead: false,
        isRecalled: false,
        sender: currentUserId // 为了兼容现有代码
      };
      
      // 尝试通过WebSocket发送
      const wsSuccess = wsSendGroupMessage({ groupId, contentType, content });
      
      // 无论WebSocket是否成功，先在本地添加消息（提高用户体验）
      commit('ADD_MESSAGE', { conversationId, message: tempMessage });
      
      // 更新会话的最后一条消息
      dispatch('conversation/updateLastMessage', {
        conversationId,
        message: {
          content,
          timestamp: tempMessage.timestamp
        }
      }, { root: true });
      
      // 如果WebSocket发送失败，使用HTTP请求发送
      if (!wsSuccess) {
        sendGroupMessage({ groupId, contentType, content })
          .then(response => {
            const { data } = response;
            // 更新临时消息ID为服务器返回的ID
            if (data && data.messageId) {
              commit('UPDATE_MESSAGE', {
                conversationId,
                messageId: tempMessage.messageId,
                data: { messageId: data.messageId }
              });
            }
            resolve(data);
          })
          .catch(error => {
            // 发送失败，标记消息为发送失败状态
            commit('UPDATE_MESSAGE', {
              conversationId,
              messageId: tempMessage.messageId,
              data: { sendFailed: true }
            });
            reject(error);
          });
      } else {
        resolve(tempMessage);
      }
    });
  },

  // 获取私聊消息列表
  getPrivateMessages({ commit }, { conversationId, otherUserId }) {
    return new Promise((resolve, reject) => {
      console.log('[Message Store] 获取私聊消息, 参数:', { conversationId, otherUserId });
      
      // 确保我们有对方的手机号或ID
      if (!otherUserId) {
        console.error('[Message Store] 获取私聊消息失败: 对方ID为空');
        reject(new Error('对方ID为空'));
        return;
      }
      
      getPrivateMessages(otherUserId)
        .then(response => {
          const { data } = response;
          console.log('[Message Store] 获取私聊消息成功, 原始数据:', data);
          
          if (!data) {
            console.warn('[Message Store] 获取私聊消息成功，但数据为空');
            commit('SET_MESSAGES', { conversationId, messages: [] });
            resolve([]);
            return;
          }
          
          console.log('[Message Store] 获取私聊消息成功, 消息数量:', data?.length || 0);
          
          // 确保消息按时间升序排列（从旧到新）
          if (data && Array.isArray(data)) {
            data.sort((a, b) => new Date(a.timestamp || a.sendTime) - new Date(b.timestamp || b.sendTime));
            
            // 检查消息格式
            data.forEach((msg, index) => {
              if (!msg.messageId && msg.id) {
                // 如果使用id而不是messageId，进行转换
                console.log(`[Message Store] 消息 ${index} 使用id而不是messageId，进行转换`);
                msg.messageId = msg.id;
              }
              
              if (!msg.timestamp && msg.sendTime) {
                // 如果使用sendTime而不是timestamp，进行转换
                console.log(`[Message Store] 消息 ${index} 使用sendTime而不是timestamp，进行转换`);
                msg.timestamp = msg.sendTime;
              }
              
              // 确保conversationId存在
              if (!msg.conversationId) {
                console.log(`[Message Store] 消息 ${index} 缺少conversationId，设置为当前会话ID`);
                msg.conversationId = conversationId;
              }
            });
          }
          
          commit('SET_MESSAGES', { conversationId, messages: data });
          resolve(data);
        })
        .catch(error => {
          console.error('[Message Store] 获取私聊消息失败:', error);
          reject(error);
        });
    });
  },

  // 获取群聊消息列表
  getGroupMessages({ commit }, { conversationId, groupId }) {
    return new Promise((resolve, reject) => {
      console.log('[Message Store] 获取群聊消息, 参数:', { conversationId, groupId });
      
      if (!groupId) {
        console.error('[Message Store] 获取群聊消息失败: 群组ID为空');
        reject(new Error('群组ID为空'));
        return;
      }
      
      getGroupMessages(groupId)
        .then(response => {
          const { data } = response;
          console.log('[Message Store] 获取群聊消息成功, 原始数据:', data);
          
          if (!data) {
            console.warn('[Message Store] 获取群聊消息成功，但数据为空');
            commit('SET_MESSAGES', { conversationId, messages: [] });
            resolve([]);
            return;
          }
          
          console.log('[Message Store] 获取群聊消息成功, 消息数量:', data?.length || 0);
          
          // 确保消息按时间升序排列（从旧到新）
          if (data && Array.isArray(data)) {
            data.sort((a, b) => new Date(a.timestamp || a.sendTime) - new Date(b.timestamp || b.sendTime));
            
            // 检查消息格式
            data.forEach((msg, index) => {
              if (!msg.messageId && msg.id) {
                // 如果使用id而不是messageId，进行转换
                console.log(`[Message Store] 消息 ${index} 使用id而不是messageId，进行转换`);
                msg.messageId = msg.id;
              }
              
              if (!msg.timestamp && msg.sendTime) {
                // 如果使用sendTime而不是timestamp，进行转换
                console.log(`[Message Store] 消息 ${index} 使用sendTime而不是timestamp，进行转换`);
                msg.timestamp = msg.sendTime;
              }
              
              // 确保conversationId存在
              if (!msg.conversationId) {
                console.log(`[Message Store] 消息 ${index} 缺少conversationId，设置为当前会话ID`);
                msg.conversationId = conversationId;
              }
            });
          }
          
          commit('SET_MESSAGES', { conversationId, messages: data });
          resolve(data);
        })
        .catch(error => {
          console.error('[Message Store] 获取群聊消息失败:', error);
          reject(error);
        });
    });
  },

  // 标记消息为已读
  markMessageAsRead({ commit }, messageId) {
    return new Promise((resolve, reject) => {
      // 尝试通过WebSocket发送已读回执
      const wsSuccess = sendMessageReadReceipt(messageId);
      
      // 如果WebSocket发送失败，使用HTTP请求发送
      if (!wsSuccess) {
        markMessageAsRead(messageId)
          .then(response => {
            resolve(response);
          })
          .catch(error => {
            reject(error);
          });
      } else {
        resolve();
      }
    });
  },

  // 撤回消息
  recallMessage({ commit }, { conversationId, messageId, conversationType, targetId }) {
    return new Promise((resolve, reject) => {
      // 尝试通过WebSocket撤回消息
      const wsSuccess = sendMessageRecall(messageId, conversationType, targetId);
      
      // 如果WebSocket发送失败，使用HTTP请求发送
      if (!wsSuccess) {
        recallMessage(messageId)
          .then(response => {
            commit('UPDATE_MESSAGE', { 
              conversationId, 
              messageId, 
              data: { isRecalled: true } 
            });
            resolve(response);
          })
          .catch(error => {
            reject(error);
          });
      } else {
        commit('UPDATE_MESSAGE', { 
          conversationId, 
          messageId, 
          data: { isRecalled: true } 
        });
        resolve();
      }
    });
  },

  // 删除消息
  deleteMessage({ commit }, { conversationId, messageId }) {
    return new Promise((resolve, reject) => {
      deleteMessage(messageId)
        .then(response => {
          commit('REMOVE_MESSAGE', { conversationId, messageId });
          resolve(response);
        })
        .catch(error => {
          reject(error);
        });
    });
  },

  // 获取未读消息数
  getUnreadMessageCount({ commit }) {
    return new Promise((resolve, reject) => {
      getUnreadMessageCount()
        .then(response => {
          const { data } = response;
          commit('SET_UNREAD_COUNT', data);
          resolve(data);
        })
        .catch(error => {
          reject(error);
        });
    });
  },

  // 获取离线消息
  getOfflineMessages({ commit }) {
    return new Promise((resolve, reject) => {
      getOfflineMessages()
        .then(response => {
          const { data } = response;
          commit('SET_OFFLINE_MESSAGES', data);
          resolve(data);
        })
        .catch(error => {
          reject(error);
        });
    });
  },

  // 确认接收离线消息
  confirmOfflineMessages({ commit }, messageIds) {
    return new Promise((resolve, reject) => {
      // 如果 messageIds 为空或不是数组，直接返回
      if (!messageIds || !Array.isArray(messageIds) || messageIds.length === 0) {
        console.warn('[Message Store] confirmOfflineMessages: messageIds 为空或不是数组');
        resolve();
        return;
      }

      console.log('[Message Store] 确认离线消息:', messageIds);
      
      // 确保 messageIds 是数字类型
      const numericMessageIds = messageIds.map(id => {
        if (typeof id === 'string') {
          // 尝试将字符串转换为数字
          return Number(id);
        }
        return id;
      }).filter(id => !isNaN(id)); // 过滤掉无效的 ID
      
      if (numericMessageIds.length === 0) {
        console.warn('[Message Store] confirmOfflineMessages: 没有有效的消息ID');
        resolve();
        return;
      }
      
      console.log('[Message Store] 转换后的消息ID:', numericMessageIds);
      
      // 确保参数格式正确
      const data = { messageIds: numericMessageIds };
      
      confirmOfflineMessages(data)
        .then(response => {
          console.log('[Message Store] 确认离线消息成功:', response);
          commit('CLEAR_OFFLINE_MESSAGES');
          resolve(response);
        })
        .catch(error => {
          console.error('[Message Store] 确认离线消息失败:', error);
          // 即使确认失败，也清空本地离线消息列表，避免重复确认
          commit('CLEAR_OFFLINE_MESSAGES');
          reject(error);
        });
    });
  },

  // 处理WebSocket消息
  handleWebSocketMessage({ commit, dispatch }, { message, type }) {
    console.log('[Message Store] 收到WebSocket消息:', { type, message });
    
    if (!message || !message.conversationId) {
      console.error('[Message Store] 消息格式错误，缺少conversationId:', message);
      return;
    }
    
    switch (type) {
      case 'private':
        // 处理私聊消息
        console.log('[Message Store] 处理私聊消息:', message);
        commit('ADD_MESSAGE', { 
          conversationId: message.conversationId, 
          message 
        });
        // 更新会话的最后一条消息
        dispatch('conversation/updateLastMessage', {
          conversationId: message.conversationId,
          message
        }, { root: true });
        break;
      case 'group':
        // 处理群聊消息
        console.log('[Message Store] 处理群聊消息:', message);
        commit('ADD_MESSAGE', { 
          conversationId: message.conversationId, 
          message 
        });
        // 更新会话的最后一条消息
        dispatch('conversation/updateLastMessage', {
          conversationId: message.conversationId,
          message
        }, { root: true });
        break;
      case 'read':
        // 处理消息已读回执
        console.log('[Message Store] 处理消息已读回执:', message);
        commit('UPDATE_MESSAGE', { 
          conversationId: message.conversationId, 
          messageId: message.messageId, 
          data: { isRead: true } 
        });
        break;
      case 'recall':
        // 处理消息撤回
        console.log('[Message Store] 处理消息撤回:', message);
        commit('UPDATE_MESSAGE', { 
          conversationId: message.conversationId, 
          messageId: message.messageId, 
          data: { isRecalled: true } 
        });
        break;
      default:
        console.log('[Message Store] 未知的WebSocket消息类型:', type, message);
    }
  }
};

export default {
  namespaced: true,
  state,
  mutations,
  actions
}; 