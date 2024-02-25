<template>
  <a-drawer
      :visible="visible"
      :title="'批量操作通道'"
      width="20%"
      :maskClosable="true"
      @close="onClose">
    <a-form-model ref="infoFormModel" :model="saveObject" layout="vertical">
      <a-row :gutter="16">
        <a-col :span="24">
          <a-form-model-item :label="tipLabel">
            <a-input v-model="selectPassageInfoStr" disabled="disabled" style="height: 200px;color: black" type="textarea" />
          </a-form-model-item>
        </a-col>
        <a-col :span="24">
          <a-form-model-item label="通道常用操作">
            <a-button type="primary" @click="showSetState">批量开启关闭</a-button>
            <a-popconfirm title="确认清空所选通道余额么?" ok-text="确认" cancel-text="取消" @confirm="handleSetBalanceZeroOkFunc">
              <a-button style="margin-left: 32px" type="danger" >清空通道余额</a-button>
            </a-popconfirm>
          </a-form-model-item>
        </a-col>
        <a-col :span="24">
          <a-form-model-item label="通道属性配置">
            <a-button @click="showSetProduct">修改所属产品</a-button>
            <a-button style="margin-left: 32px" @click="showSetRate">设置通道费率</a-button>
          </a-form-model-item>
        </a-col>
        <a-col :span="24">
          <a-form-model-item label="通道支付配置">
            <a-button @click="showSetGate">修改下单网关</a-button>
            <a-button style="margin-left: 32px" @click="showSetIP">修改回调IP</a-button>
          </a-form-model-item>
        </a-col>
      </a-row>
    </a-form-model>
    <template>
      <a-modal v-model="isShowProductModal" title="批量设置产品" @ok="handleSetProductOkFunc">
        <a-form-model :label-col="{span: 8}" :wrapper-col="{span: 13}">
          <a-form-model-item label="所属产品：">
            <a-select v-model="saveObject.productId" :allowClear="true" placeholder="对应产品" show-search option-filter-prop="children">
              <a-select-option v-for="d in productList" :value="d.productId" :key="d.productId">
                {{ d.productName + " [ ID: " + d.productId + " ]" }}
              </a-select-option>
            </a-select>
          </a-form-model-item>
        </a-form-model>
      </a-modal>
    </template>
    <template>
      <a-modal v-model="isShowStateModal" title="批量开关通道" @ok="handleSetStateOkFunc">
        <a-form-model :label-col="{span: 8}" :wrapper-col="{span: 13}">
          <b style="color: rgb(128,128,128);font-size: 13px">批量操作已选择的通道</b><br/><br/>
          <a-form-model-item label="通道操作：">
            <a-radio-group v-model="saveObject.state">
              <a-radio :value="1">
                启用
              </a-radio>
              <a-radio :value="0">
                禁用
              </a-radio>
            </a-radio-group>
          </a-form-model-item>
        </a-form-model>
      </a-modal>
    </template>
    <template>
      <a-modal v-model="isShowRateModal" title="批量设置费率" @ok="handleSetRateOkFunc">
        <a-form-model :label-col="{span: 8}" :wrapper-col="{span: 13}">
          <b style="color: rgb(128,128,128);font-size: 13px">请先核对后谨慎操作</b><br/><br/>
          <a-form-model-item label="通道费率">
            <a-input prefix="%" v-model="rate" placeholder="请输入" type="number" />
          </a-form-model-item>
        </a-form-model>
      </a-modal>
    </template>
    <template>
      <a-modal v-model="isShowGateModal" title="批量设置下单网关" @ok="handleSetGateOkFunc">
        <a-form-model :label-col="{span: 8}" :wrapper-col="{span: 13}">
          <b style="color: rgb(128,128,128);font-size: 13px">请先核对后谨慎操作</b><br/><br/>
          <a-form-model-item label="下单网关">
            <a-input v-model="payGate" placeholder="请输入" type="text" />
          </a-form-model-item>
        </a-form-model>
      </a-modal>
    </template>
    <template>
      <a-modal v-model="isShowIPModal" title="批量设置回调IP" @ok="handleSetIPOkFunc">
        <a-form-model :label-col="{span: 8}" :wrapper-col="{span: 13}">
          <b style="color: rgb(128,128,128);font-size: 13px">请先核对后谨慎操作</b><br/>
          <b style="color: rgb(128,128,128);font-size: 13px">(多个地址以 | 分隔，*表示允许全部IP)</b><br/><br/>
          <a-form-model-item label="回调IP">
            <a-input v-model="ip" placeholder="请输入" type="textarea" />
          </a-form-model-item>
        </a-form-model>
      </a-modal>
    </template>
    <div class="drawer-btn-center">
      <a-button @click="onClose" icon="close" :style="{ marginRight: '8px' }">关闭</a-button>
    </div>
  </a-drawer>
</template>

<script>
import {
  API_URL_MCH_APP_MULTIPLE_SET,
  API_URL_PAYWAYS_LIST,
  req
} from '@/api/manage'

export default {
  props: {
    callbackFunc: { type: Function, default: () => () => ({}) }
  },
  data () {
    return {
      visible: false, // 抽屉开关
      saveObject: {}, // 数据对象
      rate: 0,
      payGate: '',
      ip: '',
      productList: {},
      isShowProductModal: false,
      isShowStateModal: false,
      isShowRateModal: false,
      isShowGateModal: false,
      isShowIPModal: false,
      selectedIds: [],
      selectPassageInfo: [],
      selectPassageInfoStr: '',
      tipLabel: ''
    }
  },
  methods: {
    // 抽屉显示
    show (selectedIds, selectPassageInfo) {
      // 数据清空
      this.saveObject = {}
      this.visible = true
      this.selectedIds = selectedIds
      this.selectPassageInfo = selectPassageInfo
      this.selectPassageInfoStr = ''
      for (var i = 0; i < selectPassageInfo.length; i++) {
        this.selectPassageInfoStr += (selectPassageInfo[i] + '\n')
      }
      this.tipLabel = '当前已选择通道 ' + selectPassageInfo.length + ' 条'
      const that = this
      req.list(API_URL_PAYWAYS_LIST, { 'pageSize': -1 }).then(res => { // 产品下拉选择列表
        that.productList = res.records
      })
      if (this.$refs.infoFormModel !== undefined) {
        this.$refs.infoFormModel.resetFields()
      }

      this.visible = true
    },
    showSetState () {
      this.isShowStateModal = true
    },
    handleSetStateOkFunc () {
      const that = this
      const state = that.saveObject.state
      if (state === undefined || state == null || state === '') {
        that.$message.error('请先选择状态')
        return
      }
      const params = { }
      params.state = state
      params.selectedIds = this.selectedIds
      req.postDataNormal(API_URL_MCH_APP_MULTIPLE_SET, 'multipleSetState/', params).then(res => {
        that.$message.success('操作成功')
        that.resetTempData()
      })
      this.isShowStateModal = false
    },
    showSetProduct () {
      this.isShowProductModal = true
    },
    handleSetProductOkFunc () {
      console.log(this.saveObject)
      const that = this
      const productId = that.saveObject.productId
      if (productId === undefined || productId == null || productId === '') {
        that.$message.error('请选择所属产品')
        return
      }
      const params = { }
      params.productId = productId
      params.selectedIds = this.selectedIds
      req.postDataNormal(API_URL_MCH_APP_MULTIPLE_SET, 'multipleSetProduct/', params).then(res => {
        that.$message.success('操作成功')
        that.resetTempData()
      })
      this.isShowProductModal = false
    },
    handleSetBalanceZeroOkFunc () {
      const that = this
      const params = { }
      params.selectedIds = this.selectedIds
      req.postDataNormal(API_URL_MCH_APP_MULTIPLE_SET, 'multipleSetBalanceZero/', params).then(res => {
        that.$message.success('操作成功')
        that.resetTempData()
      })
    },
    showSetRate () {
      this.isShowRateModal = true
    },
    handleSetRateOkFunc () {
      const that = this
      const pattern = /^\d+(?:\.\d+)?$/
      if (!pattern.test(this.rate)) {
        that.$message.error('费率格式错误')
        return
      }
      const params = { }
      params.rate = that.rate / 100
      params.selectedIds = that.selectedIds
      req.postDataNormal(API_URL_MCH_APP_MULTIPLE_SET, 'multipleSetRate/', params).then(res => {
        that.$message.success('操作成功')
        that.resetTempData()
      })
      this.isShowRateModal = false
    },
    showSetGate () {
      this.isShowGateModal = true
    },
    handleSetGateOkFunc () {
      const that = this
      if (!this.isValidUrl(this.payGate.trim())) {
        that.$message.error('下单地址格式错误，请核实')
        return
      }
      const params = { }
      params.payGate = that.payGate.trim()
      params.selectedIds = that.selectedIds
      req.postDataNormal(API_URL_MCH_APP_MULTIPLE_SET, 'multipleSetGate/', params).then(res => {
        that.$message.success('操作成功')
        that.resetTempData()
      })
      this.isShowGateModal = false
    },
    showSetIP () {
      this.isShowIPModal = true
    },
    handleSetIPOkFunc () {
      const that = this
      if (!this.isValidIpList(this.ip.trim())) {
        that.$message.error('回调IP格式错误，请核实')
        return
      }
      const params = { }
      params.ip = that.ip.trim()
      params.selectedIds = that.selectedIds
      req.postDataNormal(API_URL_MCH_APP_MULTIPLE_SET, 'multipleSetIP/', params).then(res => {
        that.$message.success('操作成功')
        that.resetTempData()
      })
      this.isShowIPModal = false
    },
    resetTempData () {
      this.saveObject = { }
      this.callbackFunc()
      this.isShowProductModal = false
      this.isShowStateModal = false
      this.isShowRateModal = false
      this.isShowGateModal = false
      this.isShowIPModal = false
      this.rate = 0
      this.payGate = ''
      this.ip = ''
    },
    onClose () {
      this.visible = false
      this.callbackFunc() // 刷新列表
    },
    isValidUrl (url) {
      if (url.indexOf('http') !== 0) {
        return false
      }
      try {
        // eslint-disable-next-line no-new
        new URL(url)
      } catch (_) {
        return false
      }
      return true
    },
    isValidIpList (ipList) {
      if (ipList === '*') {
        return true
      }
    // 正则表达式，匹配IPV4或IPV6地址列表，地址之间用|分隔
      const pattern = /^((\b25[0-5]|\b2[0-4][0-9]|\b[01]?[0-9][0-9]?)\.(\b25[0-5]|\b2[0-4][0-9]|\b[01]?[0-9][0-9]?)\.(\b25[0-5]|\b2[0-4][0-9]|\b[01]?[0-9][0-9]?)\.(\b25[0-5]|\b2[0-4][0-9]|\b[01]?[0-9][0-9]?)|\b([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))(\|(\b25[0-5]|\b2[0-4][0-9]|\b[01]?[0-9][0-9]?)\.(\b25[0-5]|\b2[0-4][0-9]|\b[01]?[0-9][0-9]?)\.(\b25[0-5]|\b2[0-4][0-9]|\b[01]?[0-9][0-9]?)\.(\b25[0-5]|\b2[0-4][0-9]|\b[01]?[0-9][0-9]?)|\b([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))*$/
      return pattern.test(ipList)
    }
  }
}
</script>

<style lang="less" scoped>
</style>
