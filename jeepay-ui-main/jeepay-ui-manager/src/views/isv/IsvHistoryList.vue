<template>
  <page-header-wrapper>
    <a-card>
      <div class="table-page-search-wrapper">
        <a-form layout="inline" class="table-head-ground">
          <div class="table-layer">
            <a-form-item label="" class="table-head-layout" style="max-width:350px;min-width:300px">
              <a-range-picker
                  @change="onChange"
                  :show-time="{ format: 'HH:mm:ss' }"
                  v-model="selectedRange"
                  format="YYYY-MM-DD HH:mm:ss"
                  :disabled-date="disabledDate"
                  :ranges="ranges"
              >
                <a-icon slot="suffixIcon" type="sync" />
              </a-range-picker>
            </a-form-item>
            <jeepay-text-up :placeholder="'代理商商户号'" :msg="searchData.agentNo" v-model="searchData.agentNo" />
            <jeepay-text-up :placeholder="'代理商名称'" :msg="searchData.agentName" v-model="searchData.agentName" />
            <jeepay-text-up :placeholder="'订单号'" :msg="searchData.payOrderId" v-model="searchData.payOrderId" />
            <a-form-item label="" class="table-head-layout">
              <a-select v-model="searchData.fundDirection" placeholder="资金变动方向" default-value="0">
                <a-select-option value="0">全部</a-select-option>
                <a-select-option value="1">加款</a-select-option>
                <a-select-option value="2">减款</a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item label="" class="table-head-layout">
              <a-select v-model="searchData.bizType" placeholder="业务类型" default-value="0">
                <a-select-option value="0">全部</a-select-option>
                <a-select-option value="1">分润</a-select-option>
                <a-select-option value="2">提现</a-select-option>
                <a-select-option value="3">调账</a-select-option>
                <a-select-option value="4">提现驳回</a-select-option>
                <a-select-option value="6">测试冲正</a-select-option>
              </a-select>
            </a-form-item>
            <span class="table-page-search-submitButtons">
              <a-button type="primary" icon="search" @click="queryFunc" :loading="btnLoading">搜索</a-button>
              <a-button style="margin-left: 8px" icon="reload" @click="resetSearch">重置</a-button>
              <a-button type="danger" style="margin-left: 8px" icon="download" @click="exportExcel">导出</a-button>
            </span>
          </div>
        </a-form>
      </div>

      <!-- 列表渲染 -->
      <JeepayTable
          @btnLoadClose="btnLoading=false"
          ref="infoTable"
          :initData="false"
          :reqTableDataFunc="reqTableDataFunc"
          :tableColumns="tableColumns"
          :searchData="searchData"
          :pageSize="50"
          rowKey="agentAccountHistoryId"
      >
        <template slot="isvNameSlot" slot-scope="{record}"><b>{{ record.agentName }}</b></template> <!-- 自定义插槽 -->
        <template slot="beforeSlot" slot-scope="{record}">
          <b>{{ (record.beforeBalance/100).toFixed(2) }}</b>
        </template> <!-- 自定义插槽 -->
        <template slot="amountSlot" slot-scope="{record}">
          <b :style="{'color': record.amount >0 ? '#4BD884' : '#DB4B4B'}" >{{ record.amount >0 ? '+'+(record.amount/100).toFixed(2) : (record.amount/100).toFixed(2) }}</b>
        </template> <!-- 自定义插槽 -->
        <template slot="afterSlot" slot-scope="{record}">
          <b>{{ (record.afterBalance/100).toFixed(2) }}</b>
        </template> <!-- 自定义插槽 -->
        <template slot="bizTypeSlot" slot-scope="{record}">
          <span v-if="record.bizType === 0"> - </span><!-- 业务类型,1-分润,2-提现,3-调账 orange -->
          <a-tag color="green" v-else-if="record.bizType === 1">分润</a-tag>
          <a-tag color="blue" v-else-if="record.bizType === 2">提现</a-tag>
          <a-tag color="orange" v-else-if="record.bizType === 3">调账(ID:{{record.createdUid}}--{{record.createdLoginName}})</a-tag>
          <a-tag color="red" v-else-if="record.bizType === 4">
            提现驳回
          </a-tag>
          <a-tag color="#F03B44" v-else-if="record.bizType === 6">
            测试冲正
          </a-tag>
          <span v-else>未知</span>
        </template>
      </JeepayTable>
    </a-card>
  </page-header-wrapper>
</template>

<script>
import JeepayTable from '@/components/JeepayTable/JeepayTable'
import JeepayTextUp from '@/components/JeepayTextUp/JeepayTextUp' // 文字上移组件
import JeepayTableColumns from '@/components/JeepayTable/JeepayTableColumns'
import { API_URL_ISV_HISTORY_LIST, exportExcel, req } from '@/api/manage'
import moment from 'moment'
import { saveAs } from 'file-saver'

// eslint-disable-next-line no-unused-vars
const tableColumns = [
  { key: 'agentName', width: '150px', title: '代理商名称', fixed: 'left', scopedSlots: { customRender: 'isvNameSlot' } },
  { key: 'agentNo', title: '代理商户号', dataIndex: 'agentNo', width: '150px', fixed: 'left' },
  { key: 'beforeBalance', title: '变更前余额(￥)', width: '180px', scopedSlots: { customRender: 'beforeSlot' } },
  { key: 'amount', width: '150px', title: '变更金额(￥)', scopedSlots: { customRender: 'amountSlot' } },
  { key: 'afterBalance', title: '变更后余额(￥)', width: '180px', scopedSlots: { customRender: 'afterSlot' } },
  { key: 'payOrderId', title: '订单号', dataIndex: 'payOrderId', width: '220px' },
  { key: 'bizType', title: '业务类型', scopedSlots: { customRender: 'bizTypeSlot' } },
  { key: 'createdAt', dataIndex: 'createdAt', title: '创建日期' },
  { key: 'remark', dataIndex: 'remark', title: '备注' }
]

export default {
  name: 'IsvHistoryList',
  components: { JeepayTable, JeepayTableColumns, JeepayTextUp },
  data () {
    return {
      btnLoading: false,
      tableColumns: tableColumns,
      searchData: {},
      selectedRange: [],
      ranges: { 今天: [moment().startOf('day'), moment().endOf('day')],
        昨天: [moment().subtract(1, 'day').startOf('day'), moment().subtract(1, 'day').endOf('day')],
        近一周: [
          moment().subtract(1, 'week').startOf('day'),
          moment().endOf('day')
        ]
      }
    }
  },
  mounted () {
    // 默认今天
    this.selectedRange = [moment().startOf('day'), moment().endOf('day')] // 开始时间
    this.searchData.createdStart = this.selectedRange[0].format('YYYY-MM-DD HH:mm:ss') // 开始时间
    this.searchData.createdEnd = this.selectedRange[1].format('YYYY-MM-DD HH:mm:ss') // 结束时间
    this.$refs.infoTable.refTable(true)
  },
  methods: {
    queryFunc () {
      this.btnLoading = true
      this.$refs.infoTable.refTable(true)
    },
    // 请求table接口数据
    reqTableDataFunc: (params) => {
      return req.list(API_URL_ISV_HISTORY_LIST, params)
    },
    disabledDate (current) { // 今日之后日期不可选
      return current && current > moment().endOf('day')
    },
    onChange: function (date, dateString) {
      this.searchData.createdStart = dateString[0] // 开始时间
      this.searchData.createdEnd = dateString[1] // 结束时间
      this.$refs.infoTable.refTable(true)
    },
    resetSearch: function () {
      this.searchData = {}
      this.selectedRange = [] // 开始时间
    },
    exportExcel: function () { // todo 这里返回的结果没有嵌入到通用返回格式中
      exportExcel('/api/agentHistory/exportExcel', this.searchData).then(res => {
        const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
        // 使用 FileSaver.js 的 saveAs 方法将 Blob 对象保存为文件
        saveAs(blob, moment().format('YYYY-MM-DD') + '-代理资金流水.xlsx')
      }).catch((resErr) => {
        const blob = new Blob([resErr], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
        // 使用 FileSaver.js 的 saveAs 方法将 Blob 对象保存为文件
        saveAs(blob, moment().format('YYYY-MM-DD') + '-代理资金流水.xlsx')
      })
    }
  }
}
</script>
