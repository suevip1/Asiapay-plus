<template>
  <a-drawer
      :visible="visible"
      title="通道下单测试"
      width="40%"
      :maskClosable="false"
      @close="onClose">
    <a-row justify="space-between" type="flex">
      <a-col :sm="12">
        <a-descriptions>
          <a-descriptions-item label="通道ID">
            <b style="color: #1A79FF">{{ passage.payPassageId }}</b>
          </a-descriptions-item>
        </a-descriptions>
      </a-col>
      <a-col :sm="12">
        <a-descriptions>
          <a-descriptions-item label="通道名称">
            <b>{{ passage.payPassageName }}</b>
          </a-descriptions-item>
        </a-descriptions>
      </a-col>
      <a-col :sm="24">
        <a-descriptions>
          <a-descriptions-item label="所属产品">
            <a-tag color="purple">
              [{{ passage.productId }}]&nbsp{{ productName }}
            </a-tag>
          </a-descriptions-item>
        </a-descriptions>
      </a-col>
      <a-col :sm="12">
        <a-descriptions>
          <a-descriptions-item label="收款规则类型">
            <a-tag color="blue" v-if="passage.payType == 1">区间范围</a-tag>
            <a-tag color="blue" v-if="passage.payType == 2">固定金额</a-tag>
          </a-descriptions-item>
        </a-descriptions>
      </a-col>
      <a-col :sm="12">
        <a-descriptions>
          <a-descriptions-item label="收款规则">
            <b color="pink">[&nbsp;{{ passage.payRules }}&nbsp;]</b>
          </a-descriptions-item>
        </a-descriptions>
      </a-col>
      <a-divider/>
      <a-col :sm="24">
        <a-descriptions>
          <a-descriptions-item label="三方通道用户标识">
            <b>{{ ifParams.mchNo }}</b>
          </a-descriptions-item>
        </a-descriptions>
      </a-col>
      <a-col :sm="24">
        <a-descriptions>
          <a-descriptions-item label="下单网关">
            <span>{{ ifParams.payGateway }}</span>
          </a-descriptions-item>
        </a-descriptions>
      </a-col>
      <a-divider/>
      <a-form-model ref="infoFormModel" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="24">
            <a-form-model-item label="支付金额">
              <a-input prefix="￥" v-model="payTestAmount" placeholder="请输入" type="number" />
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label='测试订单入库：'>
              <p style="font-size: 13px;color: #8C8C8C">[启用] 此选项自动挂到测试商户下，用于测试下单完整流程包含回调<br>[禁用] 不进入订单库，仅用于测试三方通道是否能正常拉起</p>
              <a-radio-group v-model="testOrderIn">
                <a-radio :value="1">
                  启用
                </a-radio>
                <a-radio :value="0">
                  禁用
                </a-radio>
              </a-radio-group>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-button type="primary" @click="onSubmit" icon="check" >下单测试</a-button>
          </a-col>
        </a-row>
      </a-form-model>
    </a-row>
    <a-divider/>
    <a-row justify="start" type="flex">
      <a-col :sm="24">
        <a-form-model-item label="测试商户订单号">
          <a-input
              type="text"
              disabled="disabled"
              style="color: black"
              v-model="testPayOrderId"
          />
          <a-button v-if="isShowReturnUrl && testOrderIn===1" type="danger" @click="goOrderList" icon="double-right" >去订单页查看</a-button>
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
          <a style="font-size: 18px" :href="returnUrl" target="_blank">{{returnUrl}}</a>
        </a-form-model-item>
      </a-col>
      <a-col :sm="24">
        <a-form-model-item label="手动跳转">
          <a-button type="primary" size="small" class="copy-btn" v-clipboard:copy="returnUrl" v-clipboard:success="onCopy" >一键复制链接</a-button>
        </a-form-model-item>
      </a-col>
    </a-row>
  </a-drawer>
</template>

<script>
import { passageTestOrder } from '@/api/manage'

export default {
  props: {
    callbackFunc: { type: Function, default: () => () => ({}) }
  },

  data () {
    return {
      visible: false, // 抽屉开关
      payTestAmount: 0,
      productName: '',
      passage: {},
      ifParams: {},
      respParams: '',
      testPayOrderId: '',
      isShowReturnUrl: false,
      returnUrl: '',
      testOrderIn: 0
    }
  },
  methods: {
    // 抽屉显示
    show (passage, productName) {
      this.passage = passage
      this.productName = productName
      if (passage.payInterfaceConfig !== undefined) {
        this.ifParams = JSON.parse(passage.payInterfaceConfig)
      }
      this.visible = true
      this.payTestAmount = 0
      this.respParams = ''
      this.testPayOrderId = ''
      this.isShowReturnUrl = false
      this.returnUrl = ''
      this.testOrderIn = 0
    },
    // todo 表单提交 先检测是否设置了对应产品的费率
    onSubmit () {
      if (this.payTestAmount === '' || this.payTestAmount === 0) {
        this.$message.error('金额不能为空')
        return
      }
      const testOrderNo = 'T' + new Date().getTime() + Math.floor(Math.random() * (9999 - 1000) + 1000)
      const params = {}
      const that = this
      params.testOrderNo = testOrderNo
      params.passageId = this.passage.payPassageId
      params.amount = this.payTestAmount
      params.testOrderIn = this.testOrderIn
      params.productId = this.passage.productId
      that.$store.commit('showLoading') // 关闭全局刷新 API_URL_PASSAGE_TEST
      passageTestOrder(params).then(res => {
        that.$store.commit('hideLoading') // 关闭全局刷新
        that.testPayOrderId = testOrderNo
        if (res.code === 0) {
          this.$message.info(`下单成功`)
          that.respParams = JSON.stringify(res)
          if (res.data.payData !== undefined && res.data.payData !== null) {
            that.returnUrl = res.data.payData
            that.isShowReturnUrl = true
          }
        } else {
          that.respParams = JSON.stringify(res.msg)
          that.$message.error(`拉起支付失败`)
        }
        // this.$message.info(`下单成功`)
      }).catch((resErr) => {
        this.$message.error(`拉起支付失败`)
        that.respParams = JSON.stringify(resErr)
        that.$store.commit('hideLoading') // 关闭全局刷新
      })
    },
    onClose () {
      this.visible = false
    },
    onCopy () {
      this.$message.success('复制成功')
    },
    goOrderList: function () { // 应用配置
      this.$router.push({
        path: '/pay',
        query: { unionOrderId: this.testPayOrderId }
      })
      this.visible = false
    }
  }
}
</script>

<style lang="less" scoped>
</style>
