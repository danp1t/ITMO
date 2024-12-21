import { createRouter, createWebHistory } from 'vue-router';
import Home from '../views/Home.vue';
import Main from '../views/Main.vue';

const routes = [
    {
        path: '/',
        name: 'Home',
        component: Home,
    },
    {
        path: '/main',
        name: 'Main',
        component: Main,
        meta: { requiresAuth: true }, 
    },
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

router.beforeEach((to, from, next) => {
    const isAuthenticated = !!localStorage.getItem('token');
    if (to.matched.some(record => record.meta.requiresAuth) && !isAuthenticated) {
        next({ path: '/' });
    } else {
        next();
    }
});

export default router;
