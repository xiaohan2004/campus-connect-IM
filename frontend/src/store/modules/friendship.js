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
          console.log('获取到的好友列表数据:', data);
          
          // 检查好友数据结构
          if (data && data.length > 0) {
            console.log('好友数据示例:', data[0]);
            console.log('好友ID字段:', data[0].id ? 'id' : (data[0].friendId ? 'friendId' : (data[0].userId ? 'userId' : '未知')));
          }
          
          commit('SET_FRIENDS', data);
          resolve(data);
        })
        .catch(error => {
          console.error('获取好友列表失败:', error);
          reject(error);
        });
    });
  },

  // 获取好友列表并加载备注信息
  getFriendsWithDetails({ commit, dispatch }) {
    return new Promise((resolve, reject) => {
      // 先获取好友列表
      dispatch('getFriends')
        .then(async (friends) => {
          console.log('开始处理好友列表，获取备注信息');
          try {
            if (!friends || friends.length === 0) {
              console.log('好友列表为空，无需获取备注信息');
              resolve([]);
              return;
            }
            
            console.log('好友列表数据结构:', JSON.stringify(friends[0]));
            
            // 为每个好友获取详细的关系信息
            const friendsWithDetails = await Promise.all(
              friends.map(async (friend) => {
                try {
                  console.log(`开始获取好友 ${friend.nickname}(ID:${friend.id}) 的关系详情`);
                  
                  // 尝试使用不同的ID字段
                  const friendId = friend.id || friend.friendId || friend.userId;
                  console.log('使用的friendId:', friendId);
                  
                  // 获取好友关系详情
                  const friendshipData = await dispatch('getFriendship', friendId);
                  console.log(`好友 ${friend.nickname} 的关系详情:`, friendshipData);
                  
                  // 检查friendshipData是否包含remark字段
                  if (friendshipData && friendshipData.remark) {
                    console.log(`好友 ${friend.nickname} 的备注名为: ${friendshipData.remark}`);
                    // 将备注信息添加到好友对象中
                    return {
                      ...friend,
                      remark: friendshipData.remark
                    };
                  } else {
                    console.log(`好友 ${friend.nickname} 没有备注名，尝试手动添加模拟数据`);
                    // 模拟添加备注名，用于测试
                    return {
                      ...friend,
                      // remark: `${friend.nickname}的备注名`\
                      remark: ""
                    };
                  }
                } catch (error) {
                  console.error(`获取好友 ${friend.nickname} 的关系详情失败:`, error);
                  console.log('尝试手动添加模拟数据');
                  // 模拟添加备注名，用于测试
                  return {
                    ...friend,
                    // remark: `${friend.nickname}的备注名(错误后添加)`
                    remark: ""
                  };
                }
              })
            );
            
            console.log('处理后的带备注的好友列表:', friendsWithDetails);
            commit('SET_FRIENDS', friendsWithDetails);
            resolve(friendsWithDetails);
          } catch (error) {
            console.error('处理好友详情失败:', error);
            reject(error);
          }
        })
        .catch(error => {
          console.error('获取好友列表失败:', error);
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
      console.log(`开始获取好友关系，friendId: ${friendId}`);
      if (!friendId) {
        console.error('friendId为空，无法获取好友关系');
        reject(new Error('friendId不能为空'));
        return;
      }
      
      getFriendship(friendId)
        .then(response => {
          const { data } = response;
          console.log(`获取到的好友关系数据:`, data);
          // 这里只是获取数据，不需要修改状态
          resolve(data);
        })
        .catch(error => {
          console.error(`获取好友关系失败:`, error);
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