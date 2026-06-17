import {
  ShoppingCart,
  Calendar,
  Plus,
  List,
  UserFilled,
  Ticket,
  DataAnalysis,
  Avatar,
  Clock,
  Goods,
  Present,
  Money,
} from "@element-plus/icons-vue"

export const defaultQuickActions = [
  { slot: 0, label: '消费收银', path: '/consumption', icon: ShoppingCart, color: '#6C5CE7' },
  { slot: 1, label: '新增预约', path: '/appointment', icon: Calendar, color: '#00B894' },
  { slot: 2, label: '新增会员', path: '/member', icon: Plus, color: '#FDCB6E' },
  { slot: 3, label: '轮牌排队', path: '/service-queue', icon: List, color: '#74B9FF' },
  { slot: 4, label: '消费订单', path: '/orders', icon: List, color: '#E17055' },
  { slot: 5, label: '优惠券管理', path: '/coupon', icon: Ticket, color: '#00CEC9' },
  { slot: 6, label: '营业报表', path: '/report', icon: DataAnalysis, color: '#A29BFE' },
  { slot: 7, label: '员工管理', path: '/employee', icon: Avatar, color: '#FD79A8' },
]

export const routeOptions = [
  { path: '/consumption', label: '消费收银', icon: ShoppingCart, color: '#6C5CE7' },
  { path: '/appointment', label: '新增预约', icon: Calendar, color: '#00B894' },
  { path: '/member', label: '新增会员', icon: Plus, color: '#FDCB6E' },
  { path: '/service-queue', label: '轮牌排队', icon: List, color: '#74B9FF' },
  { path: '/orders', label: '消费订单', icon: List, color: '#E17055' },
  { path: '/coupon', label: '优惠券管理', icon: Ticket, color: '#00CEC9' },
  { path: '/report', label: '营业报表', icon: DataAnalysis, color: '#A29BFE' },
  { path: '/employee', label: '员工管理', icon: Avatar, color: '#FD79A8' },
  { path: '/schedule', label: '排班考勤', icon: Clock, color: '#FAB1A0' },
  { path: '/inventory', label: '库存管理', icon: Goods, color: '#81ECEC' },
  { path: '/points', label: '积分商城', icon: Present, color: '#FFEAA7' },
  { path: '/commission', label: '提成结算', icon: Money, color: '#55EFC4' },
  { path: '/group-buy', label: '拼团活动', icon: ShoppingCart, color: '#DFE6E9' },
  { path: '/service', label: '服务项目', icon: List, color: '#B2BEC3' },
  { path: '/daily-close', label: '财务日结', icon: DataAnalysis, color: '#636E72' },
]

export const routeMetaMap = Object.fromEntries(
  routeOptions.map(r => [r.path, { icon: r.icon, color: r.color }])
)
