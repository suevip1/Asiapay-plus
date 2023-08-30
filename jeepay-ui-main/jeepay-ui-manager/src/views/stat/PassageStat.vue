<template>
  <page-header-wrapper>
    <a-card style="margin-top: 20px">
      <div class="table-page-search-wrapper">
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
            <a-form-model-item label="" class="table-head-layout">
              <a-select v-model="searchData.payPassageId" :allowClear="true" placeholder="通道">
                <a-select-option v-for="d in payPassageList" :value="d.payPassageId" :key="d.payPassageId">
                  {{ d.payPassageName + " [ ID: " + d.payPassageId + " ]" }}
                </a-select-option>
              </a-select>
            </a-form-model-item>
            <a-form-model-item label="" class="table-head-layout">
              <a-select v-model="searchData.productId" :allowClear="true" placeholder="对应产品">
                <a-select-option v-for="d in productList" :value="d.productId" :key="d.productId">
                  {{ d.productName + " [ ID: " + d.productId + " ]" }}
                </a-select-option>
              </a-select>
            </a-form-model-item>
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
          :pageSize="100"
          rowKey="statisticsPassageId"
      >
        <template slot="passageCostSlot" slot-scope="{record}">
          <b style="color: #4BD884">{{ ((record.totalPassageCost)/ 100).toFixed(2) }}</b>
        </template> <!-- 自定义插槽 -->
        <template slot="dateSlot" slot-scope="{record}">
          <b>{{ record.createdAt.substring(0, 10)}}</b>
        </template> <!-- 自定义插槽 -->
        <template slot="passageSlot" slot-scope="{record}">
          <b style="color: #1A79FF">[{{ record.payPassageId }}]</b>&nbsp;<span>{{record.payPassageName}}</span>
        </template> <!-- 自定义插槽 -->
        <template slot="productSlot" slot-scope="{record}">
          <b style="color: #1A79FF">[{{ record.productId }}]</b><span>{{record.productName}}</span>
        </template> <!-- 自定义插槽 -->
        <template slot="amountSlot" slot-scope="{record}">
          <b>{{(record.totalSuccessAmount / 100).toFixed(2)}}</b>
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
import { API_URL_MCH_APP, API_URL_MGR_PASSAGE_STAT, API_URL_PAYWAYS_LIST, req } from '@/api/manage'
import moment from 'moment'

// eslint-disable-next-line no-unused-vars
const tableColumns = [
  { key: 'date', title: '日期', width: '150px', fixed: 'left', scopedSlots: { customRender: 'dateSlot' } },
  { key: 'passage', scopedSlots: { customRender: 'passageSlot' }, width: '300px', fixed: 'left', title: '通道类型' },
  { key: 'amount', width: '200px', title: '成交金额(￥)', fixed: 'left', scopedSlots: { customRender: 'amountSlot' } },
  {
    key: 'passageCost',
    title: '通道成本(￥)',
    width: '200px',
    fixed: 'left',
    scopedSlots: { customRender: 'passageCostSlot' }
  },
  {
    key: 'product',
    title: '产品类型',
    scopedSlots: { customRender: 'productSlot' }
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
  },
  {
    key: 'successRate',
    title: '支付成功率',
    scopedSlots: { customRender: 'successRateSlot' }
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
      productList: [],
      payPassageList: [],
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
    const that = this
    req.list(API_URL_PAYWAYS_LIST, { 'pageSize': -1 }).then(res => { // 产品下拉选择列表
      that.productList = res.records
    })
    req.list(API_URL_MCH_APP, { 'pageSize': -1 }).then(res => { // 产品下拉选择列表
      that.payPassageList = res.records
    })
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
      return req.list(API_URL_MGR_PASSAGE_STAT, params)
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
