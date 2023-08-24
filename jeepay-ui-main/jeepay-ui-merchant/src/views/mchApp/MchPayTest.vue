<template>
  <a-drawer
      :visible="visible"
      title="产品下单测试"
      width="40%"
      :maskClosable="false"
      @close="onClose">
    <a-row justify="space-between" type="flex">
      <a-col :sm="12">
        <a-descriptions>
          <a-descriptions-item label="产品ID">
            <b style="color: #1A79FF">{{ product.productId }}</b>
          </a-descriptions-item>
        </a-descriptions>
      </a-col>
      <a-col :sm="12">
        <a-descriptions>
          <a-descriptions-item label="产品名称">
            <b>{{ product.productName }}</b>
          </a-descriptions-item>
        </a-descriptions>
      </a-col>
      <a-divider/>
      <a-form-model ref="infoFormModel" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="24">
            <a-form-model-item label="支付金额">
              <a-input prefix="￥" v-model="payTestAmount" placeholder="请输入" type="number"/>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-button type="primary" @click="onSubmit" icon="check">下单测试</a-button>
          </a-col>
        </a-row>
      </a-form-model>
    </a-row>
    <a-divider/>
    <a-row justify="start" type="flex">
      <a-col :sm="24">
        <a-form-model-item label="测试订单号">
          <a-input
              type="text"
              disabled="disabled"
              style="color: black"
              v-model="testPayOrderId"
          />
        </a-form-model-item>
      </a-col>
    </a-row>
    <a-row justify="start" type="flex">
      <a-col :sm="24">
        <a-form-model-item label="下单返回参数">
          <a-input
              type="textarea"
              disabled="disabled"
              style="height: 100px;color: black"
              v-model="respParams"
          />
        </a-form-model-item>
      </a-col>
    </a-row>
    <a-row justify="start" type="flex" v-if="isShowReturnUrl">
      <a-col :sm="24">
        <a-form-model-item label="支付链接（点击直接跳转）">
          <a style="font-size: 18px" :href="returnUrl" target="_blank">{{ returnUrl }}</a>
        </a-form-model-item>
      </a-col>
      <a-col :sm="24">
        <a-form-model-item label="手动跳转">
          <a-button type="primary" size="small" class="copy-btn" v-clipboard:copy="returnUrl" v-clipboard:success="onCopy">一键复制链接</a-button>
        </a-form-model-item>
      </a-col>
    </a-row>
  </a-drawer>
</template>

<script>
import { payTestOrder } from '@/api/manage'

export default {
  props: {
    callbackFunc: { type: Function, default: () => () => ({}) }
  },

  data () {
    return {
      visible: false, // 抽屉开关
      payTestAmount: 0,
      product: {},
      ifParams: {},
      respParams: '',
      testPayOrderId: '',
      isShowReturnUrl: false,
      returnUrl: ''
    }
  },
  methods: {
    // 抽屉显示
    show (product) {
      this.product = product
      this.visible = true
      this.isShowReturnUrl = false
      this.respParams = ''
      this.testPayOrderId = ''
      this.returnUrl = ''
      this.payTestAmount = 0
    },
    // 表单提交
    onSubmit () {
      if (this.payTestAmount === '' || this.payTestAmount === 0) {
        this.$message.error('金额不能为空')
        return
      }
      const that = this
      const mchOrderNo = 'T' + new Date().getTime() + Math.floor(Math.random() * (9999 - 1000) + 1000)
      that.$store.commit('showLoading') // 打开全局刷新
      payTestOrder({
        amount: that.payTestAmount * 100, // 支付金额
        productId: that.product.productId, // productId
        mchOrderNo: mchOrderNo // 订单编号
      }).then(res => {
        that.$store.commit('hideLoading') // 关闭全局刷新
        that.respParams = JSON.stringify(res)
        that.testPayOrderId = mchOrderNo
        if (res.data.payData === undefined) {
          that.$message.error(`出码失败`)
        } else {
          that.returnUrl = res.data.payData
          that.isShowReturnUrl = true
          that.$message.info(`下单成功`)
        }
      }).catch((resErr) => {
        that.respParams = JSON.stringify(resErr.msg)
        that.$store.commit('hideLoading') // 关闭全局刷新
        that.$message.error(`拉起订单异常`)
      })
    },
    onClose () {
      this.visible = false
    },
    onCopy () {
      this.$message.success('复制成功')
    }
  }
}
</script>

<style lang="less" scoped>
</style>
