<template>
  <div>
    <div ref="qrcode"></div>
  </div>
</template>

<script>
import QRCode from 'qrcode-generator'

export default {
  methods: {
    show (googleAuthKey) {
      // 创建二维码实例
      const qrCode = QRCode(0, 'M')
      qrCode.addData(googleAuthKey)
      qrCode.make()
      // 获取二维码的SVG字符串
      const svgString = qrCode.createSvgTag({ cellSize: 4 })
      // 将SVG字符串渲染到DOM元素
      this.$refs.qrcode.innerHTML = svgString
    }
  },
  mounted () {

  }
}
</script>
