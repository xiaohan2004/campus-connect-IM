import { getCurrentUser, updateUserStatus } from '@/api/user';
import { login } from '@/api/auth';
import { connectWebSocket, disconnectWebSocket } from '@/api/websocket';

const state = {
  token: localStorage.getItem('token') || '',
  userInfo: null,
  isLoggedIn: !!localStorage.getItem('token')
};

const mutations = {
  SET_TOKEN(state, token) {
    console.log('执行 SET_TOKEN mutation，token：', token);
    state.token = token;
    state.isLoggedIn = !!token;
    if (token) {
      localStorage.setItem('token', token);
      console.log('Token 已保存到 localStorage');
    } else {
      localStorage.removeItem('token');
      console.log('Token 已从 localStorage 中移除');
    }
  },
  SET_USER_INFO(state, userInfo) {
    state.userInfo = userInfo;
  },
  CLEAR_USER_INFO(state) {
    state.userInfo = null;
  }
};

const actions = {
  // 用户登录
  login({ commit, dispatch }, userInfo) {
    return new Promise((resolve, reject) => {
      login(userInfo)
        .then(response => {
          const { data } = response;
          commit('SET_TOKEN', data);
          connectWebSocket(data, 
            (message, type) => {
              dispatch('message/handleWebSocketMessage', { message, type }, { root: true });
            },
            (status) => {
              dispatch('user/handleStatusUpdate', status, { root: true });
            }
          );
          resolve(response);
        })
        .catch(error => {
          reject(error);
        });
    });
  },

  // 获取用户信息
  getUserInfo({ commit }) {
    return new Promise((resolve, reject) => {
      getCurrentUser()
        .then(response => {
          const { data } = response;
          commit('SET_USER_INFO', data);
          resolve(data);
        })
        .catch(error => {
          reject(error);
        });
    });
  },

  // 更新用户在线状态
  updateStatus() {
    return updateUserStatus();
  },

  // 处理用户状态更新
  handleStatusUpdate({ commit }, statusUpdate) {
    console.log('用户状态更新:', statusUpdate);
    // 可以在这里处理好友状态更新等操作
  },

  // 用户登出
  logout({ commit }) {
    return new Promise(resolve => {
      // 断开WebSocket连接
      disconnectWebSocket();
      // 清除用户信息
      commit('SET_TOKEN', '');
      commit('CLEAR_USER_INFO');
      // 清除其他模块的状态
      commit('conversation/CLEAR_CONVERSATIONS', null, { root: true });
      commit('message/CLEAR_MESSAGES', null, { root: true });
      commit('friendship/CLEAR_FRIENDS', null, { root: true });
      resolve();
    });
  }
};

export default {
  namespaced: true,
  state,
  mutations,
  actions
}; 