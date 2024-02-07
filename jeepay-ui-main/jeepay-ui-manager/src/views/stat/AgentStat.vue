<template>
  <page-header-wrapper>
    <a-card style="margin-top: 20px">
      <div class="table-page-search-wrapper" @keydown.enter="queryFunc">
        <a-form layout="inline" class="table-head-ground">
          <div class="table-layer">
            <a-form-item label="" class="table-head-layout" style="max-width:350px;min-width:300px">
              <a-range-picker
                  @change="onChange"
                  :show-time="{ format: '' }"
                  v-model="selectedRange"
                  format="YYYY-MM-DD"
                  :disabled-date="disabledDate"
                  :ranges="ranges"
              >
                <a-icon slot="suffixIcon" type="sync"/>
              </a-range-picker>
            </a-form-item>
            <jeepay-text-up :placeholder="'代理名称'" :msg="searchData.agentName" v-model="searchData.agentName"/>
            <jeepay-text-up :placeholder="'代理商号'" :msg="searchData.agentNo" v-model="searchData.agentNo"/>
            <span class="table-page-search-submitButtons">
              <a-button type="primary" icon="search" @click="queryFunc" :loading="btnLoading">搜索</a-button>
              <a-button style="margin-left: 8px" icon="reload" @click="resetSearch">重置</a-button>
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
          rowKey="statisticsAgentId"
      >
        <template slot="incomeSlot" slot-scope="{record}">
          <b style="color: #4BD884">{{ ((record.totalAgentIncome)/ 100).toFixed(2) }}</b>
        </template> <!-- 自定义插槽 -->
        <template slot="dateSlot" slot-scope="{record}">
          <b>{{ record.createdAt.substring(0, 10)}}</b>
        </template> <!-- 自定义插槽 -->
        <template slot="amountSlot" slot-scope="{record}">
          <b>{{(record.totalSuccessAmount / 100).toFixed(2)}}</b>
        </template> <!-- after插槽 -->
        <template slot="totalAmountSlot" slot-scope="{record}">
          <span>{{(record.totalAmount / 100).toFixed(2)}}</span>
        </template> <!-- after插槽 -->
        <template slot="successRateSlot" slot-scope="{record}">
          <b>{{ (record.orderSuccessCount / record.totalOrderCount *100).toFixed(2) }}%</b>
        </template> <!-- 订单金额插槽 -->
        <template slot="payOrderAmountSlot" slot-scope="{record}">
          <span>{{ record.totalOrderCount }}</span>
        </template> <!-- 自定义插槽 -->
        <template slot="payOrderSuccessCountSlot" slot-scope="{record}">
          <span>{{ record.orderSuccessCount }}</span>
        </template> <!-- 自定义插槽 -->
      </JeepayTable>
    </a-card>
  </page-header-wrapper>
</template>

<script>
import JeepayTable from '@/components/JeepayTable/JeepayTable'
import JeepayTextUp from '@/components/JeepayTextUp/JeepayTextUp' // 文字上移组件
import JeepayTableColumns from '@/components/JeepayTable/JeepayTableColumns'
import { API_URL_AGENT_STAT, req } from '@/api/manage'
import moment from 'moment'

// eslint-disable-next-line no-unused-vars
const tableColumns = [
  { key: 'date', title: '日期', width: '150px', fixed: 'left', scopedSlots: { customRender: 'dateSlot' } },
  { key: 'agentNo', dataIndex: 'agentNo', width: '150px', fixed: 'left', title: '代理号' },
  { key: 'agentName', dataIndex: 'agentName', width: '200px', fixed: 'left', title: '代理名称' },
  { key: 'amount', width: '200px', title: '成交金额(￥)', scopedSlots: { customRender: 'amountSlot' } },
  { key: 'totalAmount', width: '200px', title: '订单金额(￥)', scopedSlots: { customRender: 'totalAmountSlot' } },
  {
    key: 'agentIncome',
    title: '代理分润(￥)',
    width: '200px',
    scopedSlots: { customRender: 'incomeSlot' }
  },
  {
    key: 'successRate',
    title: '支付成功率',
    width: '200px',
    scopedSlots: { customRender: 'successRateSlot' }
  },
  {
    key: 'payOrderAmount',
    width: '100px',
    title: '订单总笔数',
    scopedSlots: { customRender: 'payOrderAmountSlot' }
  },
  {
    key: 'payOrderAmount1',
    width: '100px',
    title: '成交笔数',
    scopedSlots: { customRender: 'payOrderSuccessCountSlot' }
  }
]

export default {
  name: 'DayStatList',
  components: { JeepayTable, JeepayTableColumns, JeepayTextUp },
  data () {
    return {
      btnLoading: false,
      tableColumns: tableColumns,
      searchData: {},
      selectedRange: [],
      ranges: {
        今天: [moment().startOf('day'), moment().endOf('day')],
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
    this.searchData.createdStart = this.selectedRange[0].format('YYYY-MM-DD') // 开始时间
    this.searchData.createdEnd = this.selectedRange[1].format('YYYY-MM-DD') // 结束时间
    this.$refs.infoTable.refTable(true)
  },
  methods: {
    queryFunc () {
      this.btnLoading = true
      this.$refs.infoTable.refTable(true)
    },
    // 请求table接口数据
    reqTableDataFunc: (params) => {
      return req.list(API_URL_AGENT_STAT, params)
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
    }
  }
}
</script>
<style lang="less" scoped>
.order-list {
  -webkit-text-size-adjust:none;
  font-size: 12px;
  display: flex;
  flex-direction: column;

  p {
    white-space:nowrap;
    span {
      display: inline-block;
      font-weight: 800;
      height: 16px;
      line-height: 16px;
      width: 60px;
      border-radius: 5px;
      text-align: center;
      margin-right: 2px;
    }
  }
}
</style>
