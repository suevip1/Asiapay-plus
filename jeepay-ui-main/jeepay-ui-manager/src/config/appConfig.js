/**
 * 全局配置信息， 包含网站标题，  动态组件定义
 *
 * @author terrfly
 * @site https://www.jeepay.vip
 * @date 2021/5/8 07:18
 */

/** 应用配置项 **/
export default {
  APP_TITLE: '四方系统-运营平台', // 设置浏览器title
  ACCESS_TOKEN_NAME: 'iToken', // 设置请求token的名字， 用于请求header 和 localstorage中存在名称
  PLAT_NAME: 'platName'
}

/**
 * 与后端开发人员的路由名称及配置项
 * 组件名称 ：{ 默认跳转路径（如果后端配置则已动态配置为准）， 组件渲染 }
 * */
export const asyncRouteDefine = {
  'CurrentUserInfo': { defaultPath: '/current/userinfo', component: () => import('@/views/current/UserinfoPage') }, // 用户设置
  'MainPage': { defaultPath: '/main', component: () => import('@/views/dashboard/Analysis') },
  'SysUserPage': { defaultPath: '/users', component: () => import('@/views/sysuser/SysUserPage') },
  'RolePage': { defaultPath: '/roles', component: () => import('@/views/role/RolePage') },
  'EntPage': { defaultPath: '/ents', component: () => import('@/views/ent/EntPage') },
  'PayWayPage': { defaultPath: '/payways', component: () => import('@/views/payconfig/payWay/List') },
  'IfDefinePage': { defaultPath: '/ifdefines', component: () => import('@/views/payconfig/payIfDefine/List') },
  'IsvListPage': { defaultPath: '/isv', component: () => import('@/views/isv/IsvList') }, // 代理商列表
  'IsvHistoryList': { defaultPath: '/isvHistory', component: () => import('@/views/isv/IsvHistoryList.vue') }, // 代理资金流水
  'MchListPage': { defaultPath: '/mch', component: () => import('@/views/mch/MchList') }, // 商户列表
  'MchHistoryList': { defaultPath: '/mchHistory', component: () => import('@/views/mch/MchHistoryList.vue') }, // 商户资金流水
  'PassageHistoryList': { defaultPath: '/passageHistory', component: () => import('@/views/mchApp/PassageHistoryList.vue') }, // 通道资金流水
  'MchAppPage': { defaultPath: '/apps', component: () => import ('@/views/mchApp/List') }, // 商户应用列表
  'PayOrderListPage': { defaultPath: '/payOrder', component: () => import('@/views/order/pay/PayOrderList') }, // 支付订单列表
  'OrderForceListPage': { defaultPath: '/orderForce', component: () => import('@/views/order/pay/OrderForceList') }, // 补单列表
  'TransferOrderListPage': { defaultPath: '/transferOrder', component: () => import('@/views/order/transfer/TransferOrderList') }, // 转账订单
  'MchNotifyListPage': { defaultPath: '/notify', component: () => import('@/views/order/notify/MchNotifyList') }, // 商户通知列表
  'SysConfigPage': { defaultPath: '/config', component: () => import('@/views/sys/config/SysConfig') }, // 系统配置
  'RobotsConfigPage': { defaultPath: '/robotsConfig', component: () => import('@/views/sys/config/RobotsConfig.vue') }, // 机器人配置
  'DivisionMchPage': { defaultPath: '/divisionMch', component: () => import('@/views/division/DivisionMchPage.vue') }, // 商户结算
  'DivisionAgentPage': { defaultPath: '/divisionAgent', component: () => import('@/views/division/DivisionAgentPage.vue') }, // 代理结算
  'PlatStatPage': { defaultPath: '/platStat', component: () => import('@/views/stat/PlatStat.vue') }, // 平台统计
  'MchStatPage': { defaultPath: '/mchStat', component: () => import('@/views/stat/MchStat.vue') }, // 商户统计
  'MchProductStatPage': { defaultPath: '/mchProductStat', component: () => import('@/views/stat/MchProductStat.vue') }, // 商户-产品统计
  'PassageStatPage': { defaultPath: '/passageStat', component: () => import('@/views/stat/PassageStat.vue') }, // 通道统计
  'ProductStatPage': { defaultPath: '/productStat', component: () => import('@/views/stat/ProductStat.vue') }, // 产品统计
  'AgentStatPage': { defaultPath: '/agentStat', component: () => import('@/views/stat/AgentStat.vue') }, // 代理统计
  'SysLogPage': { defaultPath: '/log', component: () => import('@/views/sys/log/SysLog') } // 系统日志
}
