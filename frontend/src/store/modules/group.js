import { getUserGroups } from '@/api/group';

const state = {
  groups: []
};

const mutations = {
  SET_GROUPS(state, groups) {
    state.groups = groups;
  },
  CLEAR_GROUPS(state) {
    state.groups = [];
  }
};

const actions = {
  // 获取用户加入的群组列表
  getGroups({ commit }) {
    return new Promise((resolve, reject) => {
      getUserGroups()
        .then(response => {
          const { data } = response;
          commit('SET_GROUPS', data);
          resolve(data);
        })
        .catch(error => {
          reject(error);
        });
    });
  },
  
  // 清空群组数据
  clearGroups({ commit }) {
    commit('CLEAR_GROUPS');
  }
};

export default {
  namespaced: true,
  state,
  mutations,
  actions
}; 