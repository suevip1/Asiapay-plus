<template>
  <a-drawer
      :visible="visible"
      :title="'商户-支付产品费率'"
      @close="onClose"
      :body-style="{ paddingBottom: '80px' }"
      width="50%"
  >
    <div class="table-page-search-wrapper" @keydown.enter="queryFunc">
      <a-form layout="inline" class="table-head-ground">
        <a-row justify="space-between" type="flex">
          <a-col :sm="12">
            <a-descriptions>
              <a-descriptions-item label="商户号">
                <b style="color: #1a53ff">{{ mchNo }}</b>
              </a-descriptions-item>
            </a-descriptions>
          </a-col>
          <a-col :sm="12">
            <a-descriptions>
              <a-descriptions-item label="商户名称">
                {{ mchName }}
              </a-descriptions-item>
            </a-descriptions>
          </a-col>
        </a-row>
        <br/>
        <jeepay-text-up :placeholder="'产品ID'" :msg="searchData.productId" v-model="searchData.productId"/>
        <span class="table-page-search-submitButtons" style="flex-grow: 0; flex-shrink: 0;">
          <a-button type="primary" icon="search" @click="queryFunc" :loading="btnLoading">查询</a-button>
          <a-button style="margin-left: 8px" icon="reload" @click="() => this.searchData = { 'mchNo' : mchNo}">重置</a-button>
        </span>
      </a-form>
    </div>

    <!-- 列表渲染 -->
    <JeepayTable
        @btnLoadClose="btnLoading=false"
        ref="mchProductTable"
        :initData="true"
        :reqTableDataFunc="reqTableDataFunc"
        :tableColumns="tableColumns"
        :searchData="searchData"
        rowKey="productId"
    >
      <template slot="nameSlot" slot-scope="{record}">
        <b style="font-weight: bold;color: #1a53ff" >[{{ record.productId }}]</b>&nbsp;<b>{{ record.ext.productName.trim()}}</b>
      </template> <!-- 自定义插槽 -->
      <template slot="stateSlot" slot-scope="{record}">
        <a-badge :status="record.state === 0?'error':'processing'" :text="record.state === 0?'禁用':'启用'" />
      </template>
      <!--    全局金额颜色参考此处    -->
      <template slot="mchRateSlot" slot-scope="{record}">
        <b>{{ (record.mchRate*100).toFixed(2) }}%</b>
      </template>
      <template slot="agentRateSlot" slot-scope="{record}">
        <b>{{ (record.agentRate*100).toFixed(2) }}%</b>
      </template>
    </JeepayTable>
    <div class="drawer-btn-center" >
      <a-button icon="close" :style="{ marginRight: '8px' }" @click="onClose" style="margin-right:8px">
        关闭
      </a-button>
    </div>
  </a-drawer>
</template>
<script>
import JeepayTable from '@/components/JeepayTable/JeepayTable'
import JeepayTextUp from '@/components/JeepayTextUp/JeepayTextUp' // 文字上移组件
import JeepayTableColumns from '@/components/JeepayTable/JeepayTableColumns'
import { API_URL_AGENT_MCH_PRODUCT_LIST, req } from '@/api/manage'

// eslint-disable-next-line no-unused-vars
const tableColumns = [
  { key: 'nameSlot', fixed: 'left', width: '300px', title: '支付产品', scopedSlots: { customRender: 'nameSlot' } },
  { key: 'state', title: '状态', width: '50px', scopedSlots: { customRender: 'stateSlot' } },
  { key: 'mchRate', title: '商户费率', width: '100px', scopedSlots: { customRender: 'mchRateSlot' } },
  { key: 'agentRate', title: '代理费率', width: '100px', scopedSlots: { customRender: 'agentRateSlot' } }
]

export default {
  name: 'MchProductEdit',
  components: { JeepayTable, JeepayTableColumns, JeepayTextUp },
  data () {
    return {
      visible: false, // 是否显示弹层/抽屉
      btnLoading: false,
      tableColumns: tableColumns,
      searchData: {},
      value: "''",
      mchNo: '',
      mchName: '',
      mchInfo: {}
    }
  },
  mounted () {
  },
  computed: {
  },
  watch: {
  },
  methods: {
    show: function (record) { // 弹层打开事件
      // 查询商户所有开通的产品
      this.visible = true
      this.mchInfo = record
      this.mchNo = record.mchNo
      this.mchName = record.mchName
      this.searchData.mchNo = record.mchNo
      this.selectedIds = []
      if (this.$refs.mchProductTable !== undefined) {
        this.$refs.mchProductTable.refTable(true)
      }
    },
    queryFunc: function () {
      this.btnLoading = true
      this.$refs.mchProductTable.refTable(true)
    },
    // 请求table接口数据
    reqTableDataFunc: (params) => {
      return req.list(API_URL_AGENT_MCH_PRODUCT_LIST, params)
    },
    onClose () {
      this.visible = false
      this.selectedIds = []
      this.searchData = {}
    }
  }
}
</script>
