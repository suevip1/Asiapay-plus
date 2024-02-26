export function timeFix () {
  const time = new Date()
  const hour = time.getHours()
  return hour < 9 ? '早上好' : hour <= 11 ? '上午好' : hour <= 13 ? '中午好' : hour < 20 ? '下午好' : '晚上好'
}

export function isIE () {
  const bw = window.navigator.userAgent
  const compare = (s) => bw.indexOf(s) >= 0
  const ie11 = (() => 'ActiveXObject' in window)()
  return compare('MSIE') || ie11
}

/**
 * 获取订单状态字符串
 * @param state
 * @returns {string}
 */
export function getOrderStateName (state) {
  switch (state) {
    case 0:
      return '订单生成'
    case 1:
      return '支付中'
    case 2:
      return '支付成功'
    case 3:
      return '支付失败'
    case 4:
      return '已撤销'
    case 5:
      return '测试冲正'
    case 6:
      return '订单关闭'
    case 7:
      return '出码失败'
    case 8:
      return '调额入账'
  }
  return '未知'
}

export function getOrderStateColor (state) {
  switch (state) {
    case 0:
      return 'blue'
    case 1:
      return 'orange'
    case 2:
      return '#4BD884'
    case 3:
      return '#F03B44'
    case 4:
      return '#F03B44'
    case 5:
      return '#F03B44'
    case 6:
      return ''
    case 7:
      return '#F03B44'
  }
  return ''
}
