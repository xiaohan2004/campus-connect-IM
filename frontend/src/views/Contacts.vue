<template>
  <div class="contacts-container">
    <!-- 导航栏 -->
    <div class="navbar">
      <div class="navbar-title">联系人</div>
      <div class="navbar-right">
        <button class="btn-icon" @click="showAddFriend = true">
          <i class="el-icon-plus"></i>
        </button>
      </div>
    </div>

    <!-- 联系人列表 -->
    <div class="contacts-content scrollable">
      <div v-if="friends.length === 0" class="empty-state">
        <p>暂无联系人</p>
        <button class="btn btn-outline" @click="showAddFriend = true">
          添加好友
        </button>
      </div>

      <template v-else>
        <!-- 搜索框 -->
        <div class="search-bar">
          <i class="el-icon-search"></i>
          <input
            type="text"
            v-model="searchQuery"
            placeholder="搜索联系人"
            class="search-input"
          />
        </div>

        <!-- 好友分组 -->
        <div v-for="group in groupedFriends" :key="group.id" class="friend-group">
          <div class="group-header" @click="toggleGroup(group.id)">
            <i :class="['el-icon-arrow-right', { 'expanded': expandedGroups.includes(group.id) }]"></i>
            <span class="group-name">{{ group.name }}</span>
            <span class="group-count">{{ group.friends.length }}</span>
          </div>

          <div v-if="expandedGroups.includes(group.id)" class="group-content">
            <div
              v-for="friend in group.friends"
              :key="friend.friendId"
              class="friend-item"
              @click="startChat(friend)"
            >
              <img :src="friend.avatar || defaultAvatar" class="avatar" alt="头像" />
              <div class="friend-info">
                <h3 class="friend-name">{{ friend.nickname }}</h3>
                <p class="friend-status">
                  <span class="status-dot" :class="{ 'online': friend.status === 0 }"></span>
                  {{ friend.status === 0 ? '在线' : '离线' }}
                </p>
              </div>
              <div class="friend-actions">
                <el-dropdown trigger="click" @command="(command) => handleFriendAction(command, friend)">
                  <button class="btn-icon">
                    <i class="el-icon-more"></i>
                  </button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="chat">发起聊天</el-dropdown-item>
                      <el-dropdown-item command="remark" :divided="true">修改备注</el-dropdown-item>
                      <el-dropdown-item command="move">移动分组</el-dropdown-item>
                      <el-dropdown-item command="delete" :divided="true">删除好友</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>

    <!-- 添加好友弹窗 -->
    <el-dialog
      v-model="showAddFriend"
      title="添加好友"
      width="80%"
      :before-close="() => showAddFriend = false"
    >
      <div class="dialog-content">
        <div class="form-group">
          <label>手机号</label>
          <input
            type="tel"
            v-model="addFriendForm.phone"
            placeholder="请输入对方手机号"
            class="form-input"
          />
        </div>
        <div class="form-group">
          <label>备注</label>
          <input
            type="text"
            v-model="addFriendForm.remark"
            placeholder="请输入备注名称"
            class="form-input"
          />
        </div>
        <div class="form-group">
          <label>分组</label>
          <el-select v-model="addFriendForm.groupId" placeholder="请选择分组" style="width: 100%">
            <el-option
              v-for="group in friendGroups"
              :key="group.id"
              :label="group.name"
              :value="group.id"
            ></el-option>
          </el-select>
        </div>
        <div class="dialog-footer">
          <button class="btn btn-text" @click="showAddFriend = false">取消</button>
          <button class="btn btn-primary" @click="handleAddFriend" :disabled="!addFriendForm.phone">
            添加
          </button>
        </div>
      </div>
    </el-dialog>

    <!-- 修改备注弹窗 -->
    <el-dialog
      v-model="showEditRemark"
      title="修改备注"
      width="80%"
      :before-close="() => showEditRemark = false"
    >
      <div class="dialog-content">
        <div class="form-group">
          <label>备注名称</label>
          <input
            type="text"
            v-model="editRemarkForm.remark"
            placeholder="请输入备注名称"
            class="form-input"
          />
        </div>
        <div class="dialog-footer">
          <button class="btn btn-text" @click="showEditRemark = false">取消</button>
          <button class="btn btn-primary" @click="handleUpdateRemark">
            保存
          </button>
        </div>
      </div>
    </el-dialog>

    <!-- 移动分组弹窗 -->
    <el-dialog
      v-model="showMoveGroup"
      title="移动分组"
      width="80%"
      :before-close="() => showMoveGroup = false"
    >
      <div class="dialog-content">
        <div class="form-group">
          <label>选择分组</label>
          <el-select v-model="moveGroupForm.groupId" placeholder="请选择分组" style="width: 100%">
            <el-option
              v-for="group in friendGroups"
              :key="group.id"
              :label="group.name"
              :value="group.id"
            ></el-option>
          </el-select>
        </div>
        <div class="dialog-footer">
          <button class="btn btn-text" @click="showMoveGroup = false">取消</button>
          <button class="btn btn-primary" @click="handleMoveGroup">
            移动
          </button>
        </div>
      </div>
    </el-dialog>

    <!-- 底部导航栏 -->
    <div class="tabbar">
      <router-link to="/chat" class="tabbar-item" :class="{ active: $route.path.startsWith('/chat') }">
        <i class="el-icon-chat-dot-round"></i>
        <span>消息</span>
      </router-link>
      <router-link to="/contacts" class="tabbar-item" :class="{ active: $route.path.startsWith('/contacts') }">
        <i class="el-icon-user"></i>
        <span>联系人</span>
      </router-link>
      <router-link to="/profile" class="tabbar-item" :class="{ active: $route.path.startsWith('/profile') }">
        <i class="el-icon-setting"></i>
        <span>我的</span>
      </router-link>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue';
import { useStore } from 'vuex';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import defaultAvatar from '@/assets/default-avatar.png';

export default {
  name: 'Contacts',
  setup() {
    const store = useStore();
    const router = useRouter();
    
    const searchQuery = ref('');
    const showAddFriend = ref(false);
    const showEditRemark = ref(false);
    const showMoveGroup = ref(false);
    const expandedGroups = ref([0]); // 默认展开"我的好友"分组
    
    const addFriendForm = ref({
      phone: '',
      remark: '',
      groupId: 0
    });
    
    const editRemarkForm = ref({
      friendId: null,
      remark: ''
    });
    
    const moveGroupForm = ref({
      friendId: null,
      groupId: null
    });
    
    // 从store获取数据
    const friends = computed(() => store.state.friendship.friends);
    const friendGroups = computed(() => store.state.friendship.friendGroups);
    
    // 根据分组组织好友列表
    const groupedFriends = computed(() => {
      const result = friendGroups.value.map(group => ({
        ...group,
        friends: []
      }));
      
      // 根据搜索条件过滤好友
      const filteredFriends = friends.value.filter(friend => 
        !searchQuery.value || 
        friend.nickname.toLowerCase().includes(searchQuery.value.toLowerCase())
      );
      
      // 将好友按分组归类
      filteredFriends.forEach(friend => {
        const groupIndex = result.findIndex(group => group.id === (friend.groupId || 0));
        if (groupIndex !== -1) {
          result[groupIndex].friends.push(friend);
        } else {
          // 如果没有对应分组，放入默认分组
          result[0].friends.push(friend);
        }
      });
      
      return result;
    });
    
    // 展开/收起分组
    const toggleGroup = (groupId) => {
      const index = expandedGroups.value.indexOf(groupId);
      if (index === -1) {
        expandedGroups.value.push(groupId);
      } else {
        expandedGroups.value.splice(index, 1);
      }
    };
    
    // 开始聊天
    const startChat = async (friend) => {
      try {
        const conversation = await store.dispatch(
          'conversation/createPrivateConversation',
          friend.friendId
        );
        router.push(`/chat/conversation/${conversation.conversationId}`);
      } catch (error) {
        ElMessage.error('创建会话失败');
      }
    };
    
    // 处理好友操作
    const handleFriendAction = (command, friend) => {
      switch (command) {
        case 'chat':
          startChat(friend);
          break;
        case 'remark':
          editRemarkForm.value = {
            friendId: friend.friendId,
            remark: friend.remark || friend.nickname
          };
          showEditRemark.value = true;
          break;
        case 'move':
          moveGroupForm.value = {
            friendId: friend.friendId,
            groupId: friend.groupId || 0
          };
          showMoveGroup.value = true;
          break;
        case 'delete':
          ElMessageBox.confirm(`确定要删除好友 ${friend.nickname} 吗？`, '删除好友', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(() => {
            handleDeleteFriend(friend.friendId);
          }).catch(() => {
            // 用户取消操作
          });
          break;
      }
    };
    
    // 添加好友
    const handleAddFriend = async () => {
      if (!addFriendForm.value.phone) {
        ElMessage.warning('请输入手机号');
        return;
      }
      
      try {
        // 先查询用户是否存在
        const userInfo = await store.dispatch('user/getUserByPhone', addFriendForm.value.phone);
        
        if (!userInfo) {
          ElMessage.error('用户不存在');
          return;
        }
        
        // 添加好友
        await store.dispatch('friendship/addFriend', {
          friendId: userInfo.id,
          remark: addFriendForm.value.remark || userInfo.nickname,
          groupId: addFriendForm.value.groupId
        });
        
        ElMessage.success('添加好友成功');
        showAddFriend.value = false;
        
        // 重置表单
        addFriendForm.value = {
          phone: '',
          remark: '',
          groupId: 0
        };
        
        // 重新获取好友列表
        store.dispatch('friendship/getFriends');
      } catch (error) {
        ElMessage.error('添加好友失败');
      }
    };
    
    // 更新好友备注
    const handleUpdateRemark = async () => {
      if (!editRemarkForm.value.friendId) return;
      
      try {
        await store.dispatch('friendship/updateFriendRemark', {
          friendId: editRemarkForm.value.friendId,
          remark: editRemarkForm.value.remark
        });
        
        ElMessage.success('修改备注成功');
        showEditRemark.value = false;
      } catch (error) {
        ElMessage.error('修改备注失败');
      }
    };
    
    // 移动好友分组
    const handleMoveGroup = async () => {
      if (!moveGroupForm.value.friendId) return;
      
      try {
        await store.dispatch('friendship/updateFriendGroup', {
          friendId: moveGroupForm.value.friendId,
          groupId: moveGroupForm.value.groupId
        });
        
        ElMessage.success('移动分组成功');
        showMoveGroup.value = false;
      } catch (error) {
        ElMessage.error('移动分组失败');
      }
    };
    
    // 删除好友
    const handleDeleteFriend = async (friendId) => {
      try {
        await store.dispatch('friendship/deleteFriend', friendId);
        ElMessage.success('删除好友成功');
      } catch (error) {
        ElMessage.error('删除好友失败');
      }
    };
    
    onMounted(() => {
      // 获取好友列表
      store.dispatch('friendship/getFriends');
    });
    
    return {
      friends,
      friendGroups,
      groupedFriends,
      searchQuery,
      expandedGroups,
      showAddFriend,
      showEditRemark,
      showMoveGroup,
      addFriendForm,
      editRemarkForm,
      moveGroupForm,
      defaultAvatar,
      toggleGroup,
      startChat,
      handleFriendAction,
      handleAddFriend,
      handleUpdateRemark,
      handleMoveGroup
    };
  }
};
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.contacts-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: $white;
}

.contacts-content {
  flex: 1;
  overflow-y: auto;
  padding-bottom: 50px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
  padding: $spacing-6;
  text-align: center;
  
  p {
    margin-bottom: $spacing-4;
    color: $text-secondary;
  }
}

.search-bar {
  display: flex;
  align-items: center;
  padding: $spacing-3 $spacing-4;
  background-color: $gray-100;
  margin: $spacing-4;
  border-radius: $border-radius-full;
  
  i {
    color: $text-muted;
    margin-right: $spacing-2;
  }
  
  .search-input {
    flex: 1;
    border: none;
    background: transparent;
    padding: 0;
    font-size: $font-size-base;
    
    &:focus {
      outline: none;
      box-shadow: none;
    }
  }
}

.friend-group {
  margin-bottom: $spacing-4;
  
  .group-header {
    display: flex;
    align-items: center;
    padding: $spacing-3 $spacing-4;
    cursor: pointer;
    
    i {
      transition: transform 0.3s ease;
      margin-right: $spacing-2;
      
      &.expanded {
        transform: rotate(90deg);
      }
    }
    
    .group-name {
      flex: 1;
      font-size: $font-size-sm;
      font-weight: $font-weight-medium;
      color: $text-secondary;
    }
    
    .group-count {
      font-size: $font-size-xs;
      color: $text-muted;
    }
  }
  
  .group-content {
    border-top: 1px solid $border-color;
  }
}

.friend-item {
  display: flex;
  align-items: center;
  padding: $spacing-4;
  border-bottom: 1px solid $border-color;
  cursor: pointer;
  transition: $transition-base;
  
  &:hover {
    background-color: $gray-100;
  }
  
  .avatar {
    margin-right: $spacing-4;
  }
  
  .friend-info {
    flex: 1;
    min-width: 0;
  }
  
  .friend-name {
    font-size: $font-size-base;
    font-weight: $font-weight-medium;
    margin: 0 0 $spacing-1;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  
  .friend-status {
    display: flex;
    align-items: center;
    font-size: $font-size-xs;
    color: $text-muted;
    margin: 0;
    
    .status-dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background-color: $gray-400;
      margin-right: $spacing-2;
      
      &.online {
        background-color: $success-color;
      }
    }
  }
}

.form-group {
  margin-bottom: $spacing-4;
  
  label {
    display: block;
    margin-bottom: $spacing-2;
    font-size: $font-size-sm;
    font-weight: $font-weight-medium;
    color: $text-secondary;
  }
  
  .form-input {
    width: 100%;
    padding: $spacing-3 $spacing-4;
    border-radius: $border-radius;
    border: 1px solid $border-color;
    font-size: $font-size-base;
    
    &:focus {
      border-color: $primary-color;
      box-shadow: 0 0 0 2px rgba($primary-color, 0.1);
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: $spacing-6;
  
  .btn {
    margin-left: $spacing-4;
  }
}

.tabbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 100;
}
</style> 