import { 
  getUserGroups, 
  createGroup, 
  disbandGroup, 
  updateGroupInfo, 
  getGroupInfo,
  addGroupMember,
  addGroupMembers,
  removeGroupMember,
  quitGroup,
  setGroupMemberRole,
  setGroupMemberNickname,
  muteGroupMember,
  unmuteGroupMember,
  getGroupMembers,
  getGroupMemberUsers,
  checkUserInGroup
} from '@/api/group';

const state = {
  groups: [],
  currentGroup: null,
  groupMembers: {}
};

const mutations = {
  SET_GROUPS(state, groups) {
    state.groups = groups;
  },
  SET_CURRENT_GROUP(state, group) {
    state.currentGroup = group;
  },
  ADD_GROUP(state, group) {
    state.groups.push(group);
  },
  UPDATE_GROUP(state, { groupId, data }) {
    const index = state.groups.findIndex(g => g.id === groupId);
    if (index !== -1) {
      state.groups[index] = { ...state.groups[index], ...data };
    }
    if (state.currentGroup && state.currentGroup.id === groupId) {
      state.currentGroup = { ...state.currentGroup, ...data };
    }
  },
  REMOVE_GROUP(state, groupId) {
    state.groups = state.groups.filter(g => g.id !== groupId);
    if (state.currentGroup && state.currentGroup.id === groupId) {
      state.currentGroup = null;
    }
  },
  SET_GROUP_MEMBERS(state, { groupId, members }) {
    state.groupMembers = { ...state.groupMembers, [groupId]: members };
  },
  CLEAR_GROUPS(state) {
    state.groups = [];
    state.currentGroup = null;
    state.groupMembers = {};
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
  
  // 创建群组
  createGroup({ commit }, groupData) {
    return new Promise((resolve, reject) => {
      createGroup(groupData)
        .then(response => {
          const { data } = response;
          commit('ADD_GROUP', data);
          resolve(data);
        })
        .catch(error => {
          reject(error);
        });
    });
  },
  
  // 解散群组
  disbandGroup({ commit }, groupId) {
    return new Promise((resolve, reject) => {
      disbandGroup(groupId)
        .then(response => {
          commit('REMOVE_GROUP', groupId);
          resolve(response);
        })
        .catch(error => {
          reject(error);
        });
    });
  },
  
  // 更新群组信息
  updateGroupInfo({ commit }, { groupId, data }) {
    return new Promise((resolve, reject) => {
      updateGroupInfo(groupId, data)
        .then(response => {
          commit('UPDATE_GROUP', { groupId, data });
          resolve(response);
        })
        .catch(error => {
          reject(error);
        });
    });
  },
  
  // 获取群组信息
  getGroupInfo({ commit }, groupId) {
    return new Promise((resolve, reject) => {
      getGroupInfo(groupId)
        .then(response => {
          const { data } = response;
          commit('SET_CURRENT_GROUP', data);
          resolve(data);
        })
        .catch(error => {
          reject(error);
        });
    });
  },
  
  // 添加群成员（单个）
  addGroupMember({ commit }, { groupId, userId }) {
    return new Promise((resolve, reject) => {
      addGroupMember(groupId, { userId })
        .then(response => {
          resolve(response);
        })
        .catch(error => {
          reject(error);
        });
    });
  },
  
  // 添加群成员（批量）
  addGroupMembers({ commit }, { groupId, userIds }) {
    return new Promise((resolve, reject) => {
      addGroupMembers(groupId, { userIds })
        .then(response => {
          resolve(response);
        })
        .catch(error => {
          reject(error);
        });
    });
  },
  
  // 移除群成员
  removeGroupMember({ commit }, { groupId, userId }) {
    return new Promise((resolve, reject) => {
      removeGroupMember(groupId, userId)
        .then(response => {
          resolve(response);
        })
        .catch(error => {
          reject(error);
        });
    });
  },
  
  // 退出群组
  quitGroup({ commit }, groupId) {
    return new Promise((resolve, reject) => {
      quitGroup(groupId)
        .then(response => {
          commit('REMOVE_GROUP', groupId);
          resolve(response);
        })
        .catch(error => {
          reject(error);
        });
    });
  },
  
  // 设置群成员角色
  setGroupMemberRole({ commit }, { groupId, userId, role }) {
    return new Promise((resolve, reject) => {
      setGroupMemberRole(groupId, userId, { role })
        .then(response => {
          resolve(response);
        })
        .catch(error => {
          reject(error);
        });
    });
  },
  
  // 设置群内昵称
  setGroupMemberNickname({ commit }, { groupId, nickname }) {
    return new Promise((resolve, reject) => {
      setGroupMemberNickname(groupId, { nickname })
        .then(response => {
          resolve(response);
        })
        .catch(error => {
          reject(error);
        });
    });
  },
  
  // 禁言群成员
  muteGroupMember({ commit }, { groupId, userId, muteMinutes }) {
    return new Promise((resolve, reject) => {
      muteGroupMember(groupId, userId, { muteMinutes })
        .then(response => {
          resolve(response);
        })
        .catch(error => {
          reject(error);
        });
    });
  },
  
  // 取消禁言
  unmuteGroupMember({ commit }, { groupId, userId }) {
    return new Promise((resolve, reject) => {
      unmuteGroupMember(groupId, userId)
        .then(response => {
          resolve(response);
        })
        .catch(error => {
          reject(error);
        });
    });
  },
  
  // 获取群成员列表
  getGroupMembers({ commit }, groupId) {
    return new Promise((resolve, reject) => {
      getGroupMembers(groupId)
        .then(response => {
          const { data } = response;
          commit('SET_GROUP_MEMBERS', { groupId, members: data });
          resolve(data);
        })
        .catch(error => {
          reject(error);
        });
    });
  },
  
  // 获取群成员用户信息列表
  getGroupMemberUsers({ commit }, groupId) {
    return new Promise((resolve, reject) => {
      getGroupMemberUsers(groupId)
        .then(response => {
          const { data } = response;
          resolve(data);
        })
        .catch(error => {
          reject(error);
        });
    });
  },
  
  // 检查用户是否在群组中
  checkUserInGroup({ commit }, groupId) {
    return new Promise((resolve, reject) => {
      checkUserInGroup(groupId)
        .then(response => {
          const { data } = response;
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