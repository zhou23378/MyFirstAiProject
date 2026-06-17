import { createRouter, createWebHistory } from 'vue-router'
import AppLayout from '../components/AppLayout.vue'
import CustomerLayout from '../layouts/CustomerLayout.vue'

const routes = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('../views/Login.vue'),
        meta: { noAuth: true }
    },
    {
        path: '/',
        component: AppLayout,
        redirect: '/dashboard',
        children: [
            {
                path: '',
                name: 'Dashboard',
                component: () => import('../views/dashboard/index.vue')
            },
            {
                path: 'member',
                name: 'Members',
                component: () => import('../views/member/index.vue')
            },
            {
                path: 'member/:id',
                name: 'MemberDetail',
                component: () => import('../views/member/detail.vue'),
                meta: { title: '会员详情' }
            },

            {
                path: 'service',
                name: 'Services',
                component: () => import('../views/service/index.vue')
            },
            {
                path: 'consumption',
                name: 'Consumption',
                component: () => import('../views/consumption/index.vue')
            },
            {
                path: 'orders',
                name: 'Orders',
                component: () => import('../views/orders/index.vue'),
                meta: { title: '消费订单' }
            },
            {
                path: 'employee',
                name: 'Employees',
                component: () => import('../views/employee/index.vue')
            },
            {
                path: 'member-level',
                name: 'MemberLevels',
                component: () => import('../views/memberLevel/index.vue')
            },
            {
                path: 'appointment',
                name: 'Appointments',
                component: () => import('../views/appointment/index.vue')
            },
            {
                path: 'report',
                name: 'Reports',
                component: () => import('../views/report/index.vue')
            },
            {
                path: 'inventory',
                name: 'Inventory',
                component: () => import('../views/inventory/index.vue')
            },
            {
                path: 'schedule',
                name: 'Schedule',
                component: () => import('../views/schedule/index.vue')
            },
            {
                path: 'admin',
                name: 'Admins',
                component: () => import('../views/admin/index.vue')
            },
            {
                path: 'coupon',
                name: 'Coupons',
                component: () => import('../views/coupon/index.vue')
            },
            {
                path: 'technician-status',
                name: 'TechnicianStatus',
                component: () => import('../views/technician/index.vue')
            },
            {
                path: 'service-queue',
                name: 'ServiceQueue',
                component: () => import('../views/queue/index.vue')
            },
            {
                path: 'daily-close',
                name: 'DailyClose',
                component: () => import('../views/dailyClose/index.vue')
            },
            {
                path: 'marketing/tag-rules',
                name: 'TagRules',
                component: () => import('../views/marketing/tagRules.vue')
            },
            {
                path: 'marketing/birthday-config',
                name: 'BirthdayConfig',
                component: () => import('../views/birthdayConfig/index.vue')
            },
            {
                path: 'commission',
                name: 'Commission',
                component: () => import('../views/commission/index.vue')
            },
            {
                path: 'points',
                name: 'Points',
                meta: { title: '积分商城' },
                component: () => import('../views/points/index.vue')
            },
            {
                path: 'group-buy',
                name: 'GroupBuy',
                meta: { title: '拼团活动' },
                component: () => import('../views/groupBuy/index.vue')
            },
            {
                path: 'menu-config',
                name: 'MenuConfig',
                meta: { title: '菜单配置' },
                component: () => import('../views/menuConfig/index.vue')
            },
            {
                path: 'quick-actions-config',
                name: 'QuickActionsConfig',
                meta: { title: '快捷入口配置' },
                component: () => import('../views/dashboard/QuickActionsConfig.vue')
            },
            {
                path: 'stat-cards-config',
                name: 'StatCardsConfig',
                meta: { title: '统计卡片配置' },
                component: () => import('../views/dashboard/StatCardsConfig.vue')
            },

        ]
    },
    {
        path: '/h5',
        component: CustomerLayout,
        redirect: '/h5/home',
        children: [
            {
                path: 'login',
                name: 'CustomerLogin',
                component: () => import('../views/customer/login.vue'),
                meta: { noAuth: true, title: '登录' }
            },
            {
                path: 'home',
                name: 'CustomerHome',
                component: () => import('../views/customer/index.vue'),
                meta: { title: '美发沙龙' }
            },
            {
                path: 'booking',
                name: 'CustomerBooking',
                component: () => import('../views/customer/booking.vue'),
                meta: { title: '在线预约' }
            },
            {
                path: 'payment',
                name: 'CustomerPayment',
                component: () => import('../views/customer/payment.vue'),
                meta: { title: '支付' }
            },
            {
                path: 'profile',
                name: 'CustomerProfile',
                component: () => import('../views/customer/profile.vue'),
                meta: { title: '我的' }
            },
            {
                path: 'recharge',
                name: 'CustomerRecharge',
                component: () => import('../views/customer/recharge.vue'),
                meta: { title: '余额充值' }
            },
            {
                path: 'service-cards',
                name: 'CustomerServiceCards',
                component: () => import('../views/customer/service-cards.vue'),
                meta: { title: '我的次卡' }
            },
            {
                path: 'progress',
                name: 'CustomerProgress',
                component: () => import('../views/customer/progress.vue'),
                meta: { title: '服务进度' }
            },
            {
                path: 'points-mall',
                name: 'CustomerPointsMall',
                component: () => import('../views/customer/points-mall.vue'),
                meta: { title: '积分商城' }
            },
            {
                path: 'group-buy',
                name: 'CustomerGroupBuy',
                component: () => import('../views/customer/group-buy.vue'),
                meta: { title: '拼团活动' }
            }
        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 路由权限映射
const rolePermissions = {
    admin: ['/', '/member', '/service', '/consumption', '/orders', '/employee', '/member-level', '/appointment', '/report', '/inventory', '/schedule', '/coupon', '/admin', '/technician-status', '/service-queue', '/daily-close', '/marketing/tag-rules', '/marketing/birthday-config', '/commission', '/points', '/group-buy', '/menu-config', '/quick-actions-config', '/stat-cards-config'],
    manager: ['/', '/member', '/service', '/consumption', '/orders', '/employee', '/member-level', '/appointment', '/report', '/inventory', '/schedule', '/coupon', '/technician-status', '/service-queue', '/daily-close', '/marketing/tag-rules', '/marketing/birthday-config', '/commission', '/points', '/group-buy'],
    technician: ['/', '/appointment', '/schedule', '/member', '/technician-status'],
    cashier: ['/', '/member', '/consumption', '/orders', '/coupon', '/service-queue', '/points']
}

// 路由守卫 - 未登录跳转登录页，已登录校验角色权限
router.beforeEach((to, from, next) => {
    // 顾客端路由 — 使用独立的 customer_token 和认证逻辑
    if (to.path.startsWith('/h5')) {
        const customerToken = localStorage.getItem('customer_token')
        if (!customerToken && !to.meta.noAuth) {
            next('/h5/login')
            return
        }
        if (customerToken && to.path === '/h5/login') {
            next('/h5/home')
            return
        }
        next()
        return
    }

    const token = localStorage.getItem('token')
    const role = (localStorage.getItem('role') || '').toLowerCase()

    // 未登录，放行登录页，拦截其他页面
    if (!token && !to.meta.noAuth) {
        next('/login')
        return
    }

    // 已登录但访问登录页，重定向到首页
    if (token && to.path === '/login') {
        next('/')
        return
    }

    // 角色权限校验（白名单：登录页、404等）
    if (token && !to.meta.noAuth) {
        const allowedPaths = rolePermissions[role] || rolePermissions.cashier
        // 匹配允许的路径（支持精确匹配和动态路由前缀匹配）
        const isAllowed = allowedPaths.some(path => {
            if (path === '/') return to.path === '/'
            return to.path.startsWith(path)
        })
        if (!isAllowed) {
            next('/')
            return
        }
    }

    next()
})

export default router
