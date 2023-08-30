<template>
  <a-drawer
    :visible="visible"
    :title=" isAdd ? '新增通道' : '修改通道'"
    width="40%"
    :maskClosable="true"
    @close="onClose">

    <a-form-model ref="infoFormModel" :model="saveObject" layout="vertical" :rules="rules">
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-model-item label="通道名称" prop="payPassageName">
            <a-input v-model="saveObject.payPassageName" placeholder="请输入" :disabled="!isAdd" />
          </a-form-model-item>
        </a-col>
        <a-col :span="24">
          <a-form-model-item label="所属支付产品" prop="productId">
            <a-select v-model="saveObject.productId" placeholder="请选择对应产品">
              <a-select-option v-for="d in productList" :value="d.productId" :key="d.productId">
                {{ d.productName + " [ ID: " + d.productId + " ]" }}
              </a-select-option>
            </a-select>
          </a-form-model-item>
        </a-col>
        <a-col :span="24">
          <a-form-model-item label="所属支付接口" prop="ifCode">
            <a-select v-model="saveObject.ifCode" placeholder="请选择对应支付接口">
              <a-select-option v-for="d in payDefines" :value="d.ifCode" :key="d.ifCode">
                {{ d.ifName + " [ 接口代码: " + d.ifCode + " ]" }}
              </a-select-option>
            </a-select>
          </a-form-model-item>
        </a-col>
        <a-col :span="24">
          <a-form-model-item label="收款方式" prop="payType">
            <a-select v-model="saveObject.payType" placeholder="请选择收款方式">
              <a-select-option :value="1" :key="1">区间范围&nbsp;<span style="color: #8C8C8C;font-size: 12px">[例如:10-50000]</span></a-select-option>
              <a-select-option :value="2" :key="2">固定金额&nbsp;<span style="color: #8C8C8C;font-size: 12px">[例如:100|200|300]</span></a-select-option>
            </a-select>
          </a-form-model-item>
        </a-col>
        <a-col :span="24">
          <a-form-model-item label="收款规则" prop="payRules" >
            <a-input v-model="saveObject.payRules" :placeholder="saveObject.payRules" type="textarea" />
          </a-form-model-item>
        </a-col>
<!--        <a-col :span="12">-->
<!--          <a-form-model-item label="状态" prop="state">-->
<!--            <a-radio-group v-model="saveObject.state">-->
<!--              <a-radio :value="1">-->
<!--                启用-->
<!--              </a-radio>-->
<!--              <a-radio :value="0">-->
<!--                停用-->
<!--              </a-radio>-->
<!--            </a-radio-group>-->
<!--          </a-form-model-item>-->
<!--        </a-col>-->
        <a-col :span="24">
          <a-form-model-item label="通道费率">
            <a-input prefix="%" v-model="rate" placeholder="请输入" type="number" />
          </a-form-model-item>
        </a-col>
        <a-col :span="24">
          <a-form-model-item label="代理商户号">
            <a-select v-model="saveObject.agentNo" placeholder="请选择代理商" :allowClear="true" default-value="">
              <a-select-option v-for="d in isvList" :value="d.agentNo" :key="d.agentNo">
                {{ d.agentName + " [ ID: " + d.agentNo + " ]" }}
              </a-select-option>
            </a-select>
          </a-form-model-item>
        </a-col>
        <a-col v-if="saveObject.agentNo!=null && saveObject.agentNo!=''" :span="12">
          <a-form-model-item label="代理费率">
            <a-input prefix="%" v-model="lastAgentRate" placeholder="请输入" type="number" />
          </a-form-model-item>
        </a-col>
      </a-row>
    </a-form-model>

    <div class="drawer-btn-center">
      <a-button @click="onClose" icon="close" :style="{ marginRight: '8px' }">取消</a-button>
      <a-button type="primary" @click="onSubmit" icon="check" >保存</a-button>
    </div>

  </a-drawer>
</template>

<script>
import { API_URL_IFDEFINES_LIST, API_URL_ISV_LIST, API_URL_MCH_APP, API_URL_PAYWAYS_LIST, req } from '@/api/manage'

export default {
  props: {
    callbackFunc: { type: Function, default: () => () => ({}) }
  },

  data () {
    return {
      isAdd: true, // 新增 or 修改
      visible: false, // 抽屉开关
      passageId: '', // 应用AppId
      saveObject: {}, // 数据对象
      rules: {
        payRules: [{ required: true, message: '请输入收款规则', trigger: 'blur' }],
        payType: [{ required: true, message: '选择收款规则', trigger: 'blur' }],
        productId: [{ required: true, message: '选择支付产品', trigger: 'blur' }],
        ifCode: [{ required: true, message: '选择支付接口', trigger: 'blur' }],
        payPassageName: [{ required: true, message: '请输入支付通道名称', trigger: 'blur' }]
        // passageRate: [{ required: true, message: '请输入支付通道费率', trigger: 'blur', pattern: /^\d+(?:\.\d+)?$/ }]
      },
      lastAgentRate: 0,
      rate: 0,
      productList: {},
      payDefines: {},
      isvList: null // 代理商下拉列表
    }
  },
  methods: {
    // 抽屉显示
    show (passageId) {
      this.isAdd = !passageId
       // 数据清空
      this.saveObject = {}
      if (this.isAdd) {
        this.lastAgentRate = 0
        this.rate = 0
        this.saveObject = {
          'state': 1
        }
      }

      req.list(API_URL_ISV_LIST, { 'pageSize': -1 }).then(res => { // 代理商下拉选择列表
        that.isvList = res.records
      })

      req.list(API_URL_PAYWAYS_LIST, { 'pageSize': -1 }).then(res => { // 产品下拉选择列表
        that.productList = res.records
      })

      req.list(API_URL_IFDEFINES_LIST, { 'pageSize': -1 }).then(res => { // 支付接口下拉选择列表
        that.payDefines = res
      })

      if (this.$refs.infoFormModel !== undefined) {
        this.$refs.infoFormModel.resetFields()
      }

      const that = this
      if (!this.isAdd) { // 修改信息 延迟展示弹层
        that.passageId = passageId
        // 拉取详情
        req.getById(API_URL_MCH_APP, passageId).then(res => {
          that.saveObject = res
          that.lastAgentRate = (that.saveObject.agentRate * 100).toFixed(2)
          that.rate = (that.saveObject.rate * 100).toFixed(2)
        })
        this.visible = true
      } else {
        that.visible = true // 展示弹层信息
      }
    },
    // 表单提交
    onSubmit () {
      const that = this
      this.$refs.infoFormModel.validate(valid => {
        if (valid) { // 验证通过
          // 校验收款规则
          if (that.saveObject.payType === 1) {
            const payType1 = that.saveObject.payRules.split('-')
            if (payType1.length !== 2) {
              that.$message.error('收款规则格式错误,例如:10-50000')
              return
            }
            const payTypeNum1 = Number(payType1[0])
            const payTypeNum2 = Number(payType1[1])
            if (!(typeof payTypeNum1 === 'number' && !isNaN(payTypeNum1)) || !(typeof payTypeNum2 === 'number' && !isNaN(payTypeNum2))) {
              that.$message.error('收款规则格式错误,例如:10-50000')
              return
            }
            if (payTypeNum1 > payTypeNum2) {
              that.$message.error('收款规则范围错误,例如:10-50000')
              return
            }
          } else {
            const payType2 = that.saveObject.payRules.split('|')
            if (payType2 == null || payType2.length === 0) {
              var payType2OneNum = Number(payType2)
              if (!(typeof payType2OneNum === 'number' && !isNaN(payType2OneNum))) {
                that.$message.error('收款规则格式错误,例如:100|200|300')
                return
              }
            }
            for (let i = 0; i < payType2.length; i++) {
              var payType2Num = Number(payType2[i])
              if (!(typeof payType2Num === 'number' && !isNaN(payType2Num))) {
                that.$message.error('收款规则格式错误,例如:100|200|300')
                return
              }
            }
          }
          const pattern = /^\d+(?:\.\d+)?$/
          if (!pattern.test(this.rate)) {
            that.$message.error('费率格式错误')
          }
          this.saveObject.agentRate = this.lastAgentRate / 100
          if (this.saveObject.agentNo == null || this.saveObject.agentNo === undefined) {
            this.saveObject.agentNo = ''
            this.saveObject.agentRate = 0
          }
          this.saveObject.rate = this.rate / 100
          // 请求接口
          if (that.isAdd) {
            req.add(API_URL_MCH_APP, that.saveObject).then(res => {
              that.$message.success('新增成功')
              that.visible = false
              that.callbackFunc() // 刷新列表
            })
          } else {
            if (that.saveObject.state === 0) {
              that.saveObject.timeLimit = 0
            }
            req.updateById(API_URL_MCH_APP, that.saveObject.payPassageId, that.saveObject).then(res => {
              that.$message.success('修改成功')
              that.visible = false
              // that.callbackFunc() // 刷新列表
            })
          }
        }
      })
    },
    onClose () {
      this.visible = false
    }
  }
}
</script>

<style lang="less" scoped>
</style>
