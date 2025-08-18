import { createRouter, createWebHistory } from 'vue-router'

const routes = [
    {
        path: '/',
        name: 'Home',
        component: () => import('@/views/HomeView.vue')
    },
    {
        path: '/rankings',
        name: 'Rankings',
        component: () => import('@/views/RankingsView.vue')
    },
    {
        path: '/rankings/leaders',
        name: 'AllLeaders',
        component: () => import('@/views/AllLeadersView.vue'),
        meta: { title: '全部领军人才' } // 添加 meta 信息到合并后的路由
    },
    {
        path: '/rankings/rising-stars',
        name: 'AllRisingStars',
        component: () => import('@/views/AllRisingStarsView.vue')
    },
    {
        path: '/rankings/institutions',
        name: 'AllInstitutions',
        component: () => import('@/views/AllInstitutionsView.vue')
    },
    {
        path: '/network',
        name: 'Network',
        component: () => import('@/views/NetworkView.vue')
    },
    {
        path: '/author/:id',
        name: 'AuthorDetail',
        component: () => import('@/views/AuthorDetailView.vue'),
        props: true
    },
    {
        path: '/author/search',
        name: 'AuthorSearch',
        component: () => import('@/views/AuthorSearchView.vue')
    }
]

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes
})

export default router