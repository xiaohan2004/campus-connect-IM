import { 
  getConversationList, 
  getConversationDetail, 
  createPrivateConversation as apiCreatePrivateConversation, 
  createGroupConversation as apiCreateGroupConversation,
  deleteConversation,
  topConversation,
  muteConversation,
  clearConversation,
  markConversationAsRead
} from '@/api/conversation';

const state = {
  conversations: [],
  currentConversation: null,
  currentConversationId: null
};

const mutations = {
  SET_CONVERSATIONS(state, conversations) {
    state.conversations = conversations;
  },
  SET_CURRENT_CONVERSATION(state, conversation) {
    state.currentConversation = conversation;
    state.currentConversationId = conversation ? conversation.conversationId : null;
  },
  ADD_CONVERSATION(state, conversation) {
    const index = state.conversations.findIndex(item => item.conversationId === conversation.conversationId);
    if (index === -1) {
      state.conversations.unshift(conversation);
    }
  },
  UPDATE_CONVERSATION(state, { conversationId, data }) {
    const index = state.conversations.findIndex(item => item.conversationId === conversationId);
    if (index !== -1) {
      state.conversations[index] = { ...state.conversations[index], ...data };
    }
  },
  REMOVE_CONVERSATION(state, conversationId) {
    state.conversations = state.conversations.filter(item => item.conversationId !== conversationId);
    if (state.currentConversationId === conversationId) {
      state.currentConversation = null;
      state.currentConversationId = null;
    }
  },
  UPDATE_CONVERSATION_LAST_MESSAGE(state, { conversationId, message }) {
    const index = state.conversations.findIndex(item => item.conversationId === conversationId);
    if (index !== -1) {
      state.conversations[index].lastMessage = message.content;
      state.conversations[index].timestamp = message.timestamp;
      // 将更新的会话移到顶部
      const conversation = state.conversations[index];
      state.conversations.splice(index, 1);
      state.conversations.unshift(conversation);
    }
  },
  CLEAR_CONVERSATIONS(state) {
    state.conversations = [];
    state.currentConversation = null;
    state.currentConversationId = null;
  }
};

const actions = {
  // 获取会话列表
  getConversations({ commit }) {
    return new Promise((resolve, reject) => {
      getConversationList()
        .then(response => {
          const { data } = response;
          commit('SET_CONVERSATIONS', data);
          resolve(data);
        })
        .catch(error => {
          reject(error);
        });
    });
  },

  // 获取会话详情
  getConversationDetail({ commit }, conversationId) {
    return new Promise((resolve, reject) => {
      getConversationDetail(conversationId)
        .then(response => {
          const { data } = response;
          commit('SET_CURRENT_CONVERSATION', data);
          resolve(data);
        })
        .catch(error => {
          reject(error);
        });
    });
  },

  // 创建或获取私聊会话
  createPrivateConversation({ commit }, payload) {
    return new Promise((resolve, reject) => {
      console.log('Vuex action: 创建私聊会话，参数:', payload);
      apiCreatePrivateConversation(payload)
        .then(response => {
          console.log('API 响应成功:', response);
          const { data } = response;
          commit('ADD_CONVERSATION', data);
          commit('SET_CURRENT_CONVERSATION', data);
          resolve(data);
        })
        .catch(error => {
          console.error('API 调用失败:', error);
          reject(error);
        });
    });
  },

  // 创建或获取群聊会话
  createGroupConversation({ commit }, payload) {
    return new Promise((resolve, reject) => {
      console.log('Vuex action: 创建群聊会话，参数:', payload);
      apiCreateGroupConversation(payload)
        .then(response => {
          console.log('API 响应成功:', response);
          const { data } = response;
          commit('ADD_CONVERSATION', data);
          commit('SET_CURRENT_CONVERSATION', data);
          resolve(data);
        })
        .catch(error => {
          console.error('API 调用失败:', error);
          reject(error);
        });
    });
  },

  // 删除会话
  deleteConversation({ commit }, conversationId) {
    return new Promise((resolve, reject) => {
      deleteConversation(conversationId)
        .then(response => {
          commit('REMOVE_CONVERSATION', conversationId);
          resolve(response);
        })
        .catch(error => {
          reject(error);
        });
    });
  },

  // 置顶会话
  topConversation({ commit }, { conversationId, isTop }) {
    return new Promise((resolve, reject) => {
      topConversation(conversationId, { isTop })
        .then(response => {
          commit('UPDATE_CONVERSATION', { 
            conversationId, 
            data: { isTop } 
          });
          resolve(response);
        })
        .catch(error => {
          reject(error);
        });
    });
  },

  // 设置会话免打扰
  muteConversation({ commit }, { conversationId, isMuted }) {
    return new Promise((resolve, reject) => {
      muteConversation(conversationId, { isMuted })
        .then(response => {
          commit('UPDATE_CONVERSATION', { 
            conversationId, 
            data: { isMuted } 
          });
          resolve(response);
        })
        .catch(error => {
          reject(error);
        });
    });
  },

  // 清空会话消息
  // eslint-disable-next-line no-unused-vars
  clearConversation({ commit }, conversationId) {
    return new Promise((resolve, reject) => {
      clearConversation(conversationId)
        .then(response => {
          // 可以添加commit来更新状态，但目前API不返回需要更新的数据
          resolve(response);
        })
        .catch(error => {
          reject(error);
        });
    });
  },

  // 标记会话已读
  markConversationAsRead({ commit }, conversationId) {
    return new Promise((resolve, reject) => {
      markConversationAsRead(conversationId)
        .then(response => {
          commit('UPDATE_CONVERSATION', { 
            conversationId, 
            data: { unreadCount: 0 } 
          });
          resolve(response);
        })
        .catch(error => {
          reject(error);
        });
    });
  },

  // 更新会话最后一条消息
  updateLastMessage({ commit }, { conversationId, message }) {
    commit('UPDATE_CONVERSATION_LAST_MESSAGE', { conversationId, message });
  },

  // 设置当前会话
  setCurrentConversation({ commit }, conversation) {
    commit('SET_CURRENT_CONVERSATION', conversation);
    return conversation;
  },
  
  // 创建新会话（用于WebSocket消息）
  createConversation({ commit }, conversation) {
    console.log('[Conversation Store] 创建新会话:', conversation);
    
    // 检查必要字段
    if (!conversation.id) {
      console.error('[Conversation Store] 创建会话失败: 缺少ID');
      return null;
    }
    
    // 创建会话对象
    const newConversation = {
      conversationId: conversation.id,
      id: conversation.id,
      conversationType: conversation.conversationType,
      targetId: conversation.targetId,
      title: conversation.title || (conversation.conversationType === 0 ? '私聊' : '群聊'),
      lastMessage: conversation.lastMessage || '',
      timestamp: conversation.lastMessageTime || new Date().toISOString(),
      unreadCount: 1, // 新会话默认有一条未读消息
      isTop: false,
      isMuted: false
    };
    
    // 添加到会话列表
    commit('ADD_CONVERSATION', newConversation);
    
    return newConversation;
  }
};

export default {
  namespaced: true,
  state,
  mutations,
  actions
}; 