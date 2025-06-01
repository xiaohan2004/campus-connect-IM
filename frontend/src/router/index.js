import { createRouter, createWebHistory } from 'vue-router';
import store from '@/store';
import { isWebSocketConnected, connectWebSocket } from '@/api/websocket';

const routes = [
  {
    path: '/',
    redirect: '/chat'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/reset-password',
    name: 'ResetPassword',
    component: () => import('@/views/ResetPassword.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/chat',
    name: 'Chat',
    component: () => import('@/views/Chat.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'ChatDefault',
        component: () => import('@/views/chat/ChatWelcome.vue')
      },
      {
        path: 'conversation/:id',
        name: 'Conversation',
        component: () => import('@/views/chat/Conversation.vue'),
        props: true
      }
    ]
  },
  {
    path: '/contacts',
    name: 'Contacts',
    component: () => import('@/views/Contacts.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/Profile.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue')
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

// 全局前置守卫
router.beforeEach((to, from, next) => {
  // 检查用户是否已登录
  const isLoggedIn = store.state.user.isLoggedIn;
  
  // 如果要访问登录页面且已登录，则重定向到首页
  if (to.path === '/login' && isLoggedIn) {
    next('/');
    return;
  }
  
  // 如果要访问需要登录的页面但未登录，则重定向到登录页面
  if (to.meta.requiresAuth && !isLoggedIn) {
    next('/login');
    return;
  }
  
  // 如果已登录，检查WebSocket连接状态
  if (isLoggedIn) {
    const token = store.state.user.token;
    // 如果WebSocket未连接，尝试重新连接
    if (!isWebSocketConnected() && token) {
      console.log('[Router] WebSocket未连接，尝试重新连接');
      connectWebSocket(token, 
        (message, type) => {
          store.dispatch('message/handleWebSocketMessage', { message, type });
        },
        (status) => {
          store.dispatch('user/handleStatusUpdate', status);
        }
      );
    }
  }
  
  next();
});

export default router; 