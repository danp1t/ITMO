import { createRouter, createWebHistory } from 'vue-router';
import Home from '../views/Home.vue';

const routes = [
    {
        path: '/lab1/',
        name: 'Home',
        component: Home,
    },
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});


export default router;
