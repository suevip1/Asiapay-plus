import request from '@/http/request'

/*
 *  全系列 restful api格式, 定义通用req对象
 *
 *  @author terrfly
 *  @site https://www.jeepay.vip
 *  @date 2021/5/8 07:18
 */
export const req = {

  // 通用列表查询接口
  list: (url, params) => {
    return request.request({ url: url, method: 'GET', params: params }, true, true, false)
  },

  // 通用新增接口
  add: (url, data) => {
    return request.request({ url: url, method: 'POST', data: data }, true, true, false)
  },

  // 通用查询单条数据接口
  getById: (url, bizId) => {
    return request.request({ url: url + '/' + bizId, method: 'GET' }, true, true, false)
  },

  // 通用修改接口
  updateById: (url, bizId, data) => {
    return request.request({ url: url + '/' + bizId, method: 'PUT', data: data }, true, true, false)
  },

  // 通用删除接口
  delById: (url, bizId) => {
    return request.request({ url: url + '/' + bizId, method: 'DELETE' }, true, true, false)
  },

  postNormal: (url, apiUrl) => {
    return request.request({ url: url + '/' + apiUrl, method: 'POST' }, true, true, true)
  },
  postDataNormal: (url, apiUrl, data) => {
    return request.request({ url: url + '/' + apiUrl, method: 'POST', data: data }, true, true, true)
  },
  getNormal: (url, apiUrl) => {
    return request.request({ url: url + '/' + apiUrl, method: 'GET' }, true, true, true)
  }
}

// 全系列 restful api格式 (全局loading方式)
export const reqLoad = {

  // 通用列表查询接口
  list: (url, params) => {
    return request.request({ url: url, method: 'GET', params: params }, true, true, true)
  },

  // 通用新增接口
  add: (url, data) => {
    return request.request({ url: url, method: 'POST', data: data }, true, true, true)
  },

  // 通用查询单条数据接口
  getById: (url, bizId) => {
    return request.request({ url: url + '/' + bizId, method: 'GET' }, true, true, true)
  },

  // 通用修改接口
  updateById: (url, bizId, data) => {
    return request.request({ url: url + '/' + bizId, method: 'PUT', data: data }, true, true, true)
  },

  // 通用删除接口
  delById: (url, bizId) => {
    return request.request({ url: url + '/' + bizId, method: 'DELETE' }, true, true, true)
  }
}

/** 角色管理页面 **/
export const API_URL_ENT_LIST = '/api/sysEnts'
export const API_URL_ROLE_LIST = '/api/sysRoles'
export const API_URL_ROLE_ENT_RELA_LIST = '/api/sysRoleEntRelas'
export const API_URL_SYS_USER_LIST = '/api/sysUsers'
export const API_URL_USER_ROLE_RELA_LIST = '/api/sysUserRoleRelas'

/** 代理商、商户管理 **/
export const API_URL_ISV_LIST = '/api/isvInfo'

export const API_URL_ISV_BALANCE = '/api/isvBalance'
/**
 * 代理商资金流水
 * @type {string}
 */
export const API_URL_ISV_HISTORY_LIST = '/api/agentHistory'
export const API_URL_MCH_LIST = '/api/mchInfo'

export const API_URL_MCH_BALANCE = '/api/mchBalance'

export const API_URL_MCH_STAT_LIST = '/api/mchStatInfo'

export const API_URL_PASSAGE_STAT_LIST = '/api/passageStatInfo'

export const API_URL_AGENT_STAT_LIST = '/api/agentStatInfo'

/**
 * 商户-产品绑定关系
 * @type {string}
 */
export const API_URL_MCH_PRODUCT_LIST = '/api/mchProductInfo'

/**
 * 产品-商户绑定
 * @type {string}
 */
export const API_URL_PRODUCT_MCH_LIST = '/api/productMchInfo'

/**
 * 商户-通道绑定关系
 * @type {string}
 */
export const API_URL_MCH_PASSAGE_LIST = '/api/mchPassageInfo'

export const PAY_ORDER_FORCE_SUCCESS = '/api/payOrder'

export const API_PAY_ORDER_TEST = '/api/payOrderTest'

/**
 *  通道-商户绑定关系
 * @type {string}
 */
export const API_URL_PASSAGE_MCH_LIST = '/api/passageMchInfo'
/**
 * 商户资金流水
 * @type {string}
 */
export const API_URL_MCH_HISTORY_LIST = '/api/mchHistory'
/** 商户App管理 **/
export const API_URL_MCH_APP = '/api/mchApps'

export const API_URL_MCH_APP_LIST = '/api/mchAppsList'

export const API_URL_MCH_APP_HISTORY_LIST = '/api/passageHistory'

export const API_URL_MCH_APP_BALANCE = '/api/mchAppsBalance'

export const API_URL_MCH_APP_RESET_BALANCE = '/api/mchAppsBalanceReset'

export const API_URL_MCH_APP_MULTIPLE_SET = '/api/mchAppsMultipleSet'

/** 支付订单管理 **/
export const API_URL_PAY_ORDER_LIST = '/api/payOrder'

export const API_URL_PAY_ORDER_FORCE_LIST = '/api/payOrderForceList'
/** 退款订单管理 **/
export const API_URL_REFUND_ORDER_LIST = '/api/refundOrder'
/** 商户通知管理 **/
export const API_URL_MCH_NOTIFY_LIST = '/api/mchNotify'

export const API_URL_MCH_NOTIFY_RESEND_ALL = '/api/mchNotifyResend/resendAll'
/** 系统日志 **/
export const API_URL_SYS_LOG = 'api/sysLog'
/** 系统配置 **/
export const API_URL_SYS_CONFIG = 'api/sysConfigs'
/** 首页统计 **/
export const API_URL_MAIN_STATISTIC = 'api/mainChart'

/** 支付接口定义页面 **/
export const API_URL_IFDEFINES_LIST = '/api/payIfDefines'
export const API_URL_PAYWAYS_LIST = '/api/payWays'
/** 代理商、商户支付参数配置 **/
export const API_URL_MCH_DIVISION = '/api/mchDivision'

export const API_URL_AGENT_DIVISION = '/api/agentDivision'
export const API_URL_MCH_PAYCONFIGS_LIST = '/api/mch/payConfigs'
/** 商户支付通道配置 **/
export const API_URL_MCH_PAYPASSAGE_LIST = '/api/mch/payPassages'
/** 转账订单管理 **/
export const API_URL_TRANSFER_ORDER_LIST = '/api/transferOrders'

/**
 * 通道测试下单
 * @type {string}
 */
export const API_URL_PASSAGE_TEST = '/api/passageTest/doPay'

/**
 * 平台统计
 * @type {string}
 */
export const API_URL_PLAT_STAT = '/api/platStat'

export const API_URL_MCH_STAT = '/api/mchStat'

export const API_URL_MCH_PRODUCT_STAT = '/api/mchProductStat'

export const API_URL_MGR_PASSAGE_STAT = '/api/passageStat'

export const API_URL_MGR_PRODUCT_STAT = '/api/productStat'

export const API_URL_AGENT_STAT = '/api/agentStat'

/** 上传图片/文件地址 **/
export const upload = {
  avatar: request.baseUrl + '/api/ossFiles/avatar',
  ifBG: request.baseUrl + '/api/ossFiles/ifBG',
  cert: request.baseUrl + '/api/ossFiles/cert'
}

const api = {
  user: '/user',
  role_list: '/role',
  service: '/service',
  permission: '/permission',
  permissionNoPager: '/permission/no-pager',
  orgTree: '/org/tree'
}

export default api

/** 获取权限树状结构图 **/
export function getEntTree (sysType) {
  return request.request({ url: '/api/sysEnts/showTree?sysType=' + sysType, method: 'GET' })
}

/** 退款接口 */
export function payOrderRefund (payOrderId, refundAmount, refundReason) {
  return request.request({
    url: '/api/payOrder/refunds/' + payOrderId,
    method: 'POST',
    data: { refundAmount, refundReason }
  })
}

/** 更新用户角色信息 */
export function uSysUserRoleRela (sysUserId, roleIdList) {
  return request.request({
    url: 'api/sysUserRoleRelas/relas/' + sysUserId,
    method: 'POST',
    data: { roleIdListStr: JSON.stringify(roleIdList) }
  })
}

export function getAvailablePayInterfaceList (mchNo, wayCode) {
  return request.request({
    url: '/api/mch/payPassages/availablePayInterface/' + mchNo + '/' + wayCode,
    method: 'GET'
  })
}

export function getTwoDayCount () {
  return request.request({
    url: API_URL_MAIN_STATISTIC + '/twoDayCount',
    method: 'GET'
  })
}

export function getRealTimeStat () {
  return request.request({
    url: API_URL_MAIN_STATISTIC + '/realTimeCount',
    method: 'GET'
  })
}

export function getMainUserInfo (parameter) {
  return request.request({
    url: API_URL_MAIN_STATISTIC + '/' + parameter,
    method: 'GET'
  })
}

export function updateUserPass (parameter) {
  return request.request({
    url: '/api/current/modifyPwd',
    method: 'put',
    data: parameter
  })
}

export function updateUserInfo (parameter) {
  return request.request({
    url: '/api/current/user',
    method: 'put',
    data: parameter
  })
}

export function getUserInfo () {
  return request.request({
    url: '/api/current/user',
    method: 'get'
  })
}

export function getGoogleAuthInfo () {
  return request.request({
    url: '/api/current/getGoogleKey',
    method: 'get'
  })
}

export function getConfigs (parameter) {
  return request.request({
    url: API_URL_SYS_CONFIG + '/' + parameter,
    method: 'GET'
  })
}

export function getEntBySysType (entId, sysType) {
  return request.request({
    url: '/api/sysEnts/bySysType',
    method: 'GET',
    params: { entId: entId, sysType: sysType }
  })
}

export function mchNotifyResend (notifyId) {
  return request.request({
    url: '/api/mchNotify/resend/' + notifyId,
    method: 'POST'
  })
}

export function passageTestOrder (parameter) {
  return request.request({
    url: '/api/passageTest/doPay',
    method: 'POST',
    data: parameter
  })
}

export function exportExcel (url, parameter) {
  return request.request({
    url: url,
    method: 'POST',
    data: parameter,
    responseType: 'arraybuffer'
  }, true, false, true)
}
