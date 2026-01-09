import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import AdminOrderDetailsView from "@/views/AdminOrderDetailsView.vue";

const routes = [
  {
    path: '/',
    name: 'home',
    component: () => import('../views/HomeView.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/posts',
    name: 'posts',
    component: () => import('../views/PostsView.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/posts/:id',
    name: 'post-detail',
    component: () => import('../views/PostDetailView.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/tournaments',
    name: 'tournaments',
    component: () => import('../views/TournamentsView.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/shop',
    name: 'shop',
    component: () => import('../views/ShopView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/LoginView.vue'),
    meta: { requiresAuth: false, guestOnly: true },
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('../views/RegisterView.vue'),
    meta: { requiresAuth: false, guestOnly: true },
  },
  {
    path: '/forgot-password',
    name: 'forgot-password',
    component: () => import('../views/ForgotPasswordView.vue'),
    meta: { requiresAuth: false, guestOnly: true },
  },
  {
    path: '/reset-password',
    name: 'reset-password',
    component: () => import('../views/ResetPasswordView.vue'),
    meta: { requiresAuth: false, guestOnly: true },
  },
  {
    path: '/verify-email',
    name: 'verify-email',
    component: () => import('../views/VerifyEmailView.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/shop',
    name: 'shop',
    component: () => import('../views/ShopView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/shop/checkout',
    name: 'checkout',
    component: () => import('../views/CheckoutView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/shop/orders',
    name: 'orders',
    component: () => import('../views/OrdersView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/shop/orders/:id',
    name: 'order-details',
    component: () => import('../views/OrderDetailsView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/shop/products/:id',
    name: 'ProductDetail',
    component: () => import('../views/ProductDetail.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/admin/orders',
    component: () => import('../views/AdminOrdersView.vue') ,
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/orders/:id',
    component: AdminOrderDetailsView,
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('@/views/AdminView.vue'),
    meta: { requiresAdmin: true },
    children: [
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/components/admin/UserManagement.vue')
      },
      {
        path: 'roles',
        name: 'AdminRoles',
        component: () => import('@/components/admin/RoleManagement.vue')
      },
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next('/login')
    return
  }

  if (to.meta.guestOnly && authStore.isAuthenticated) {
    next('/')
    return
  }

  next()
})

export default router
