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
    state.messages = {
      ...state.messages,
      [conversationId]: messages
    };
  },
  ADD_MESSAGE(state, { conversationId, message }) {
    if (!state.messages[conversationId]) {
      state.messages[conversationId] = [];
    }
    state.messages[conversationId].push(message);
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
  sendPrivateMessage({ commit, dispatch }, { receiverPhone, contentType, content }) {
    return new Promise((resolve, reject) => {
      // 尝试通过WebSocket发送
      const wsSuccess = wsSendPrivateMessage({ receiverPhone, contentType, content });
      
      // 如果WebSocket发送失败，使用HTTP请求发送
      if (!wsSuccess) {
        sendPrivateMessage({ receiverPhone, contentType, content })
          .then(response => {
            const { data } = response;
            // 更新会话的最后一条消息
            dispatch('conversation/updateLastMessage', {
              conversationId: data.conversationId,
              message: {
                content,
                timestamp: data.timestamp
              }
            }, { root: true });
            resolve(data);
          })
          .catch(error => {
            reject(error);
          });
      } else {
        resolve();
      }
    });
  },

  // 发送群聊消息
  sendGroupMessage({ commit, dispatch }, { groupId, contentType, content }) {
    return new Promise((resolve, reject) => {
      // 尝试通过WebSocket发送
      const wsSuccess = wsSendGroupMessage({ groupId, contentType, content });
      
      // 如果WebSocket发送失败，使用HTTP请求发送
      if (!wsSuccess) {
        sendGroupMessage({ groupId, contentType, content })
          .then(response => {
            const { data } = response;
            // 更新会话的最后一条消息
            dispatch('conversation/updateLastMessage', {
              conversationId: data.conversationId,
              message: {
                content,
                timestamp: data.timestamp
              }
            }, { root: true });
            resolve(data);
          })
          .catch(error => {
            reject(error);
          });
      } else {
        resolve();
      }
    });
  },

  // 获取私聊消息列表
  getPrivateMessages({ commit }, { conversationId, otherPhone }) {
    return new Promise((resolve, reject) => {
      getPrivateMessages(otherPhone)
        .then(response => {
          const { data } = response;
          commit('SET_MESSAGES', { conversationId, messages: data });
          resolve(data);
        })
        .catch(error => {
          reject(error);
        });
    });
  },

  // 获取群聊消息列表
  getGroupMessages({ commit }, { conversationId, groupId }) {
    return new Promise((resolve, reject) => {
      getGroupMessages(groupId)
        .then(response => {
          const { data } = response;
          commit('SET_MESSAGES', { conversationId, messages: data });
          resolve(data);
        })
        .catch(error => {
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
  recallMessage({ commit }, { conversationId, messageId }) {
    return new Promise((resolve, reject) => {
      // 尝试通过WebSocket撤回消息
      const wsSuccess = sendMessageRecall(messageId);
      
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
      confirmOfflineMessages({ messageIds })
        .then(response => {
          commit('CLEAR_OFFLINE_MESSAGES');
          resolve(response);
        })
        .catch(error => {
          reject(error);
        });
    });
  },

  // 处理WebSocket消息
  handleWebSocketMessage({ commit, dispatch }, { message, type }) {
    switch (type) {
      case 'private':
        // 处理私聊消息
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
        commit('UPDATE_MESSAGE', { 
          conversationId: message.conversationId, 
          messageId: message.messageId, 
          data: { isRead: true } 
        });
        break;
      case 'recall':
        // 处理消息撤回
        commit('UPDATE_MESSAGE', { 
          conversationId: message.conversationId, 
          messageId: message.messageId, 
          data: { isRecalled: true } 
        });
        break;
      default:
        console.log('未知的WebSocket消息类型:', type, message);
    }
  }
};

export default {
  namespaced: true,
  state,
  mutations,
  actions
}; 