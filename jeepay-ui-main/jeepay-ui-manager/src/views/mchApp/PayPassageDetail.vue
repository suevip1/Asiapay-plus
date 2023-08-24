<!-- 日志详情抽屉 -->
<template>
  <a-drawer
      width="50%"
      placement="right"
      :closable="true"
      :visible="visible"
      :title="visible === true? '支付通道详情':''"
      @close="onClose"
  >
    <a-row justify="space-between" type="flex">
      <a-col :sm="12">
        <a-descriptions>
          <a-descriptions-item label="通道ID">
            <b style="color: #1A79FF">{{ detailData.payPassageId }}</b>
          </a-descriptions-item>
        </a-descriptions>
      </a-col>
      <a-col :sm="12">
        <a-descriptions>
          <a-descriptions-item label="通道名称">
            <b>{{ detailData.payPassageName }}</b>
          </a-descriptions-item>
        </a-descriptions>
      </a-col>
      <a-col :sm="24">
        <a-descriptions>
          <a-descriptions-item label="所属产品">
            <a-tag color="purple">
              [{{ detailData.productId }}]&nbsp{{ detailData.productName }}
            </a-tag>
          </a-descriptions-item>
        </a-descriptions>
      </a-col>
      <a-col :sm="12">
        <a-descriptions>
          <a-descriptions-item label="收款规则类型">
            <a-tag color="blue" v-if="detailData.payType == 1">区间范围</a-tag>
            <a-tag color="blue" v-if="detailData.payType == 2">固定金额</a-tag>
          </a-descriptions-item>
        </a-descriptions>
      </a-col>
      <a-col :sm="12">
        <a-descriptions>
          <a-descriptions-item label="收款规则">
            <b color="pink">[&nbsp;{{ detailData.payRules }}&nbsp;]</b>
          </a-descriptions-item>
        </a-descriptions>
      </a-col>
      <a-col :sm="12">
        <a-descriptions>
          <a-descriptions-item label="创建时间">
            {{ detailData.createdAt }}
          </a-descriptions-item>
        </a-descriptions>
      </a-col>
      <a-col :sm="12">
        <a-descriptions>
          <a-descriptions-item label="更新时间">
            {{ detailData.updatedAt }}
          </a-descriptions-item>
        </a-descriptions>
      </a-col>
      <a-divider/>
      <a-col :sm="12">
        <a-descriptions>
          <a-descriptions-item label="支付接口代码">
            {{ detailData.ifCode }}
          </a-descriptions-item>
        </a-descriptions>
      </a-col>
      <a-divider/>
      <a-col :sm="12">
        <a-descriptions>
          <a-descriptions-item label="通道费率">
            <b>{{ (detailData.rate * 100).toFixed(2) }}%</b>
          </a-descriptions-item>
        </a-descriptions>
      </a-col>
      <a-divider/>
      <a-col :sm="12">
        <a-descriptions>
          <a-descriptions-item label=" 代理商商户号">
            {{ detailData.agentNo == ''? '无通道代理': detailData.agentNo }}
          </a-descriptions-item>
        </a-descriptions>
      </a-col>
      <a-col :sm="12">
        <a-descriptions>
          <a-descriptions-item label="代理费率">
            <b>{{ (detailData.agentRate * 100).toFixed(2) }}%</b>
          </a-descriptions-item>
        </a-descriptions>
      </a-col>
      <a-divider/>
      <a-col :sm="24">
        <a-descriptions>
          <a-descriptions-item label="轮询权重">
            <b>{{ detailData.weights }}</b>
          </a-descriptions-item>
        </a-descriptions>
      </a-col>
    </a-row>
    <a-divider/>
    <a-col :sm="12">
      <a-descriptions>
        <a-descriptions-item label="通道授信限制">
          <a-badge :status="detailData.quotaLimitState === 0?'error':'processing'" :text="detailData.quotaLimitState === 0?'禁用':'启用'" />
        </a-descriptions-item>
      </a-descriptions>
    </a-col>
    <a-col :sm="12">
      <a-descriptions>
        <a-descriptions-item label="通道授信限制">
          <a-tag color="blue">
            {{ (detailData.quota / 100).toFixed(2) }}
          </a-tag>
        </a-descriptions-item>
      </a-descriptions>
    </a-col>
    <a-divider/>
    <a-row justify="start" type="flex">
      <a-col :sm="24">
        <a-form-model-item label="支付参数配置">
          <a-input
              type="textarea"
              disabled="disabled"
              style="height: 100px;color: black"
              v-model="detailData.payInterfaceConfig"
          />
        </a-form-model-item>
      </a-col>
    </a-row>
  </a-drawer>
</template>

<script>
export default {
  props: {
    callbackFunc: { type: Function, default: () => () => ({}) }
  },
  data () {
    return {
      isAdd: true, // 新增 or 修改
      visible: false, // 抽屉开关
      appId: '', // 应用AppId
      saveObject: {}, // 数据对象
      detailData: {},
      rules: {
        payRules: [{ required: true, message: '请输入收款规则', trigger: 'blur' }],
        payType: [{ required: true, message: '选择收款规则', trigger: 'blur' }],
        productId: [{ required: true, message: '选择支付产品', trigger: 'blur' }],
        ifCode: [{ required: true, message: '选择支付接口', trigger: 'blur' }],
        payPassageName: [{ required: true, message: '请输入支付通道名称', trigger: 'blur' }]
      },
      productList: {},
      payDefines: {},
      isvList: null // 代理商下拉列表
    }
  },
  methods: {
    // 抽屉显示
    show (record) {
      this.visible = true
      this.detailData = record
    },
    onClose () {
      this.visible = false
    }
  }
}
</script>

<style scoped>

</style>
