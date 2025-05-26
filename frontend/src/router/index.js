import { createRouter, createWebHistory } from 'vue-router';
import store from '@/store';

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
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth);
  const isLoggedIn = store.state.user.isLoggedIn;

  // 如果需要登录但未登录，重定向到登录页
  if (requiresAuth && !isLoggedIn) {
    next('/login');
  } 
  // 如果已登录但访问登录页，重定向到聊天页
  else if (isLoggedIn && to.path === '/login') {
    next('/chat');
  } 
  // 其他情况正常导航
  else {
    next();
  }
});

export default router; 