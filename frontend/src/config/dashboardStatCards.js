import {
  UserFilled,
  Coin,
  ShoppingCart,
  TrendCharts,
  Avatar,
  Goods,
  Money,
} from "@element-plus/icons-vue"

export const statOptions = [
  { key: 'memberCount', label: '会员总数', icon: UserFilled, color: '#6C5CE7', path: '/member', format: v => String(v || 0) },
  { key: 'monthRevenue', label: '本月营收', icon: Coin, color: '#00B894', path: '/report', format: v => '¥' + (v || 0).toLocaleString() },
  { key: 'todayOrder', label: '今日订单', icon: ShoppingCart, color: '#FDCB6E', path: '/consumption', format: v => String(v || 0) },
  { key: 'todayMember', label: '今日新增', icon: TrendCharts, color: '#74B9FF', path: '/member', format: v => String(v || 0) },
  { key: 'todayRevenue', label: '今日营收', icon: Money, color: '#E17055', path: '/report', format: v => '¥' + (v || 0).toLocaleString() },
  { key: 'employeeCount', label: '员工总数', icon: Avatar, color: '#00CEC9', path: '/employee', format: v => String(v || 0) },
  { key: 'serviceCount', label: '服务项目', icon: Goods, color: '#A29BFE', path: '/service', format: v => String(v || 0) },
]

export const defaultStatCards = [
  { slot: 0, key: 'memberCount', label: '会员总数', icon: UserFilled, color: '#6C5CE7', path: '/member' },
  { slot: 1, key: 'monthRevenue', label: '本月营收', icon: Coin, color: '#00B894', path: '/report' },
  { slot: 2, key: 'todayOrder', label: '今日订单', icon: ShoppingCart, color: '#FDCB6E', path: '/consumption' },
  { slot: 3, key: 'todayMember', label: '今日新增', icon: TrendCharts, color: '#74B9FF', path: '/member' },
]

export const statMetaMap = Object.fromEntries(
  statOptions.map(s => [s.key, { icon: s.icon, color: s.color, format: s.format }])
)
