import { 
  getFriendList, 
  addFriend, 
  deleteFriend, 
  updateFriendRemark, 
  updateFriendGroup,
  updateFriendStatus,
  getFriendship,
  checkFriendship
} from '@/api/friendship';

const state = {
  friends: [],
  friendGroups: [
    { id: 0, name: '我的好友' },
    { id: 1, name: '家人' },
    { id: 2, name: '同学' },
    { id: 3, name: '同事' }
  ]
};

const mutations = {
  SET_FRIENDS(state, friends) {
    state.friends = friends;
  },
  ADD_FRIEND(state, friend) {
    const index = state.friends.findIndex(item => item.friendId === friend.friendId);
    if (index === -1) {
      state.friends.push(friend);
    }
  },
  UPDATE_FRIEND(state, { friendId, data }) {
    const index = state.friends.findIndex(item => item.friendId === friendId);
    if (index !== -1) {
      state.friends[index] = { ...state.friends[index], ...data };
    }
  },
  REMOVE_FRIEND(state, friendId) {
    state.friends = state.friends.filter(item => item.friendId !== friendId);
  },
  CLEAR_FRIENDS(state) {
    state.friends = [];
  },
  ADD_FRIEND_GROUP(state, group) {
    const index = state.friendGroups.findIndex(item => item.id === group.id);
    if (index === -1) {
      state.friendGroups.push(group);
    }
  },
  UPDATE_FRIEND_GROUP(state, { groupId, name }) {
    const index = state.friendGroups.findIndex(item => item.id === groupId);
    if (index !== -1) {
      state.friendGroups[index].name = name;
    }
  },
  REMOVE_FRIEND_GROUP(state, groupId) {
    state.friendGroups = state.friendGroups.filter(item => item.id !== groupId);
  }
};

const actions = {
  // 获取好友列表
  getFriends({ commit }) {
    return new Promise((resolve, reject) => {
      getFriendList()
        .then(response => {
          const { data } = response;
          commit('SET_FRIENDS', data);
          resolve(data);
        })
        .catch(error => {
          reject(error);
        });
    });
  },

  // 添加好友
  addFriend({ commit }, friendData) {
    return new Promise((resolve, reject) => {
      addFriend(friendData)
        .then(response => {
          commit('ADD_FRIEND', {
            friendId: friendData.friendId,
            nickname: friendData.remark || '新好友',
            groupId: friendData.groupId,
            status: 0
          });
          resolve(response);
        })
        .catch(error => {
          reject(error);
        });
    });
  },

  // 删除好友
  deleteFriend({ commit }, friendId) {
    return new Promise((resolve, reject) => {
      deleteFriend(friendId)
        .then(response => {
          commit('REMOVE_FRIEND', friendId);
          resolve(response);
        })
        .catch(error => {
          reject(error);
        });
    });
  },

  // 更新好友备注
  updateFriendRemark({ commit }, { friendId, remark }) {
    return new Promise((resolve, reject) => {
      updateFriendRemark({ friendId, remark })
        .then(response => {
          commit('UPDATE_FRIEND', {
            friendId,
            data: { remark }
          });
          resolve(response);
        })
        .catch(error => {
          reject(error);
        });
    });
  },

  // 更新好友分组
  updateFriendGroup({ commit }, { friendId, groupId }) {
    return new Promise((resolve, reject) => {
      updateFriendGroup({ friendId, groupId })
        .then(response => {
          commit('UPDATE_FRIEND', {
            friendId,
            data: { groupId }
          });
          resolve(response);
        })
        .catch(error => {
          reject(error);
        });
    });
  },

  // 更新好友状态
  updateFriendStatus({ commit }, { friendId, status }) {
    return new Promise((resolve, reject) => {
      updateFriendStatus({ friendId, status })
        .then(response => {
          commit('UPDATE_FRIEND', {
            friendId,
            data: { status }
          });
          resolve(response);
        })
        .catch(error => {
          reject(error);
        });
    });
  },

  // 获取好友关系
  // eslint-disable-next-line no-unused-vars
  getFriendship({ commit }, friendId) {
    return new Promise((resolve, reject) => {
      getFriendship(friendId)
        .then(response => {
          const { data } = response;
          // 这里只是获取数据，不需要修改状态
          resolve(data);
        })
        .catch(error => {
          reject(error);
        });
    });
  },

  // 检查是否为好友
  // eslint-disable-next-line no-unused-vars
  checkFriendship({ commit }, friendId) {
    return new Promise((resolve, reject) => {
      checkFriendship(friendId)
        .then(response => {
          const { data } = response;
          // 这里只是检查状态，不需要修改状态
          resolve(data);
        })
        .catch(error => {
          reject(error);
        });
    });
  }
};

export default {
  namespaced: true,
  state,
  mutations,
  actions
}; 