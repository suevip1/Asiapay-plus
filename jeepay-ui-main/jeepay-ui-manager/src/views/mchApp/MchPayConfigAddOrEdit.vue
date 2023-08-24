<template>
  <a-drawer
    title="通道支付参数配置"
    width="40%"
    :closable="true"
    :maskClosable="true"
    :visible="visible"
    :body-style="{ paddingBottom: '80px' }"
    @close="onClose"
  >
    <a-divider orientation="left">
      <a-tag color="#FF4B33">
        {{ saveObject.ifCode }} 商户参数配置
      </a-tag>
    </a-divider>
    <a-form-model ref="mchParamFormModel" :model="ifParams" layout="vertical" :rules="ifParamsRules">
      <a-row :gutter="16">
        <a-col v-for="(item, key) in mchParams" :key="key" :span="item.type === 'text' ? 12 : 24">
          <a-form-model-item :label="item.desc" :prop="item.name" v-if="item.type === 'text' || item.type === 'textarea'">
            <a-input v-model="ifParams[item.name]" placeholder="请输入" :type="item.type" />
          </a-form-model-item>
          <a-form-model-item :label="item.desc" :prop="item.name" v-else-if="item.type === 'radio'">
            <a-radio-group v-model="ifParams[item.name]">
              <a-radio v-for="(radioItem, radioKey) in item.values" :key="radioKey" :value="radioItem.value">
                {{ radioItem.title }}
              </a-radio>
            </a-radio-group>
          </a-form-model-item>
        </a-col>
      </a-row>
    </a-form-model>
    <div class="drawer-btn-center" v-if="$access('ENT_MCH_PAY_PASSAGE_CONFIG')">
      <a-button :style="{ marginRight: '8px' }" @click="onClose" icon="close">取消</a-button>
      <a-button type="primary" @click="onSubmit" icon="check" :loading="btnLoading">保存</a-button>
    </div>
  </a-drawer>

</template>

<script>
import JeepayCard from '@/components/JeepayCard/JeepayCard'
import JeepayUpload from '@/components/JeepayUpload/JeepayUpload'
import {
  req,
  API_URL_IFDEFINES_LIST,
  API_URL_MCH_APP
} from '@/api/manage'
export default {
  components: {
      JeepayCard,
      JeepayUpload
  },
  props: {
    callbackFunc: { type: Function, default: () => ({}) }
  },

  data () {
    return {
      btnLoading: false,
      visible: false, // 抽屉开关
      appId: null, // 商户号
      ifCode: null, // 接口代码
      mchParams: {}, // 支付接口定义描述
      saveObject: {}, // 保存的对象
      ifParams: {}, // 参数配置对象
      rules: {
        infoId: [{ required: true, trigger: 'blur' }],
        ifCode: [{ required: true, trigger: 'blur' }]
        // ifRate: [{ required: false, pattern: /^(([1-9]{1}\d{0,1})|(0{1}))(\.\d{1,4})?$/, message: '请输入0-100之间的数字，最多四位小数', trigger: 'blur' }]
      },
      ifParamsRules: {}
    }
  },
  methods: {
    // 弹层打开事件
    show: function (record) {
      if (this.$refs.mchParamFormModel !== undefined) {
        this.$refs.mchParamFormModel.resetFields()
      }
      const that = this
      that.saveObject = record
      req.getById(API_URL_IFDEFINES_LIST, record.ifCode).then(res => {
        if (record.payInterfaceConfig !== undefined) {
          that.ifParams = JSON.parse(record.payInterfaceConfig)
        }
        const newItems = [] // 重新加载支付接口配置定义描述json
        let radioItems = [] // 存放单选框value title
        const mchParams = res.ifParams // 根据商户类型获取接口定义描述
        JSON.parse(mchParams).forEach(item => {
          radioItems = []
          if (item.type === 'radio') {
            const valueItems = item.values.split(',')
            const titleItems = item.titles.split(',')
            for (const i in valueItems) {
              // 检查参数是否为数字类型 然后赋值给radio值
              let radioVal = valueItems[i]
              if (!isNaN((radioVal))) { radioVal = Number(radioVal) }
              radioItems.push({
                value: radioVal,
                title: titleItems[i]
              })
            }
          }
          newItems.push({
            name: item.name,
            desc: item.desc,
            type: item.type,
            verify: item.verify,
            values: radioItems
          })
        })
        that.mchParams = newItems // 重新赋值接口定义描述
        that.visible = true // 打开支付参数配置抽屉
        that.generateRules()
      })
    },
    // 表单提交
    onSubmit () {
      const that = this
      this.$refs.mchParamFormModel.validate(valid2 => {
        if (valid2) { // 验证通过
          that.btnLoading = true
          // 支付参数配置不能为空
          if (Object.keys(that.ifParams).length === 0) {
            this.$message.error('参数不能为空！')
            return
          }
          that.saveObject.payInterfaceConfig = JSON.stringify(that.ifParams)

          req.updateById(API_URL_MCH_APP, that.saveObject.payPassageId, that.saveObject).then(res => {
            that.$message.success('保存成功')
            that.visible = false
            that.btnLoading = false
            that.callbackFunc()
          })
        }
      })
    },
    // 上传文件成功回调方法，参数value为文件地址，name是自定义参数
    uploadSuccess (value, name) {
      this.ifParams[name] = value
      this.$forceUpdate()
    },
    generateRules () {
      const rules = {}
      let newItems = []
      this.mchParams.forEach(item => {
        newItems = []
        if (item.verify === 'required' && item.star !== '1') {
          newItems.push({
            required: true,
            message: '请输入' + item.desc,
            trigger: 'blur'
          })
          rules[item.name] = newItems
        }
      })
      this.ifParamsRules = rules
    },
    onClose () {
      this.visible = false
    }
  }
}
</script>
<style lang="less" scoped>
  .jeepay-card-content {
    width: 100%;
    position: relative;
    background-color: @jee-card-back;
    border-radius: 6px;
    overflow:hidden;
  }
  .jeepay-card-ops {
    width: 100%;
    height: 50px;
    background-color: @jee-card-back;
    display: flex;
    flex-direction: row;
    justify-content: space-around;
    align-items: center;
    border-top: 1px solid @jee-back;
    position: absolute;
    bottom: 0;
  }
  .jeepay-card-content-header {
    width: 100%;
    display: flex;
    flex-direction: row;
    justify-content: center;
    align-items: center;
  }
  .jeepay-card-content-body {
    display: flex;
    flex-direction: column;
    justify-content: start;
    align-items: center;
  }
  .title {
    font-size: 16px;
    font-family: PingFang SC, PingFang SC-Bold;
    font-weight: 700;
    color: #1a1a1a;
    letter-spacing: 1px;
  }
</style>
