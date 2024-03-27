<template>
  <page-header-wrapper>
    <a-card>
      <div class="table-page-search-wrapper" @keydown.enter="queryFunc">
        <a-form layout="inline" class="table-head-ground">
          <div class="table-layer">
            <a-form-item label="" class="table-head-layout" style="max-width:350px;min-width:300px">
              <a-range-picker
                  @change="onChange"
                  :show-time="{ format: 'HH:mm:ss' }"
                  format="YYYY-MM-DD HH:mm:ss"
                  :disabled-date="disabledDate"
                  v-model="selectedRange"
                  :ranges="ranges"
              >
                <a-icon slot="suffixIcon" type="sync" />
              </a-range-picker>
            </a-form-item>
            <jeepay-text-up :placeholder="'商户名'" :msg="searchData.mchName" v-model="searchData.mchName" />
            <jeepay-text-up :placeholder="'商户号'" :msg="searchData.mchNo" v-model="searchData.mchNo" />
            <jeepay-text-up :placeholder="'商户订单号'" :msg="searchData.mchOrderNo" v-model="searchData.mchOrderNo" />
            <span class="table-page-search-submitButtons">
              <a-button type="primary" icon="search" @click="queryFunc" :loading="btnLoading">搜索</a-button>
              <a-button style="margin-left: 8px" icon="reload" @click="resetSearch">重置</a-button>
            </span>
          </div>
        </a-form>
      </div>
      <!-- 统计部分 -->
      <div style="background-color: #fafafa;padding-top: 10px;padding-bottom: 10px;border-bottom: 1px solid rgba(179,179,179,0.4)">
        <a-row style="padding-left: 15px;padding-right: 15px;">
          <a-col class="stat-col bg-color-1" :span="4">
            <span class="title">订单总金额</span>
            <b style="color: #DB4B4B;">{{ (realTimeStatData.totalAmount/100).toFixed(2) }}</b>
            <img src="~@/assets/dashboard/icon_zuorichengg.png">
          </a-col>
          <a-col class="stat-col bg-color-2" :span="4" :offset="1">
            <span class="title">订单总数</span>
            <b style="color: #FA9D2A;">{{ realTimeStatData.totalCount }}</b>
            <img src="~@/assets/dashboard/orange-icon.png">
          </a-col>
        </a-row>
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
          rowKey="payOrderId"
      >
        <template slot="mchSlot" slot-scope="{record}">
          <span style="color: #007EFF;font-size: 14px">{{ record.mchNo }}</span>
        </template> <!-- 商户信息插槽 -->
        <template slot="amountSlot" slot-scope="{record}"><b>￥{{ record.amount/100 }}</b></template> <!-- 订单金额插槽 -->
        <template slot="productSlot" slot-scope="{record}"><b style="color: #007EFF;">[{{record.productId}}]</b>&nbsp;<span>{{record.productName}}</span></template> <!-- 订单金额插槽 -->
        <template slot="stateSlot">
          <a-tag color="#F03B44">
            异常
          </a-tag>
        </template>
        <template slot="orderSlot" slot-scope="{record}">
          <a-tooltip placement="bottom" style="font-weight: normal;" v-if="record.mchOrderNo.length > 20">
            <template slot="title">
              <span>{{ record.mchOrderNo }}</span>
            </template>
            {{ changeStr2ellipsis(record.mchOrderNo, 20) }}
          </a-tooltip>
          <span style="font-weight: normal;" v-else>{{ record.mchOrderNo }}</span>
        </template>
        <template slot="opSlot" slot-scope="{record}">  <!-- 操作列插槽 -->
          <JeepayTableColumns>
            <a-button type="link" v-if="$access('ENT_PAY_ORDER_VIEW')" @click="detailFunc(record.errorOrderId)">查看详情</a-button>
          </JeepayTableColumns>
        </template>
      </JeepayTable>
    </a-card>
    <!-- 日志详情抽屉 -->
    <template>
      <a-drawer
          width="50%"
          placement="right"
          :closable="true"
          :visible="visible"
          :title="visible === true? '异常订单详情':''"
          @close="onClose"
      >
        <a-row justify="space-between" type="flex">
          <a-col :sm="12">
            <a-descriptions>
              <a-descriptions-item label="商户号">
                <b>{{ detailData.mchNo }}</b>
              </a-descriptions-item>
            </a-descriptions>
          </a-col>
          <a-col :sm="12">
            <a-descriptions>
              <a-descriptions-item label="商户名称">
                {{ detailData.mchName }}
              </a-descriptions-item>
            </a-descriptions>
          </a-col>
          <a-col :sm="24">
            <a-descriptions>
              <a-descriptions-item label="商户订单号">
                {{ detailData.mchOrderNo }}
              </a-descriptions-item>
            </a-descriptions>
          </a-col>
          <a-col :sm="12">
            <a-descriptions>
              <a-descriptions-item label="支付金额">
                <a-tag color="green">
                  {{ detailData.amount/100 }}
                </a-tag>
              </a-descriptions-item>
            </a-descriptions>
          </a-col>
          <a-col :sm="12">
            <a-descriptions>
              <a-descriptions-item label="订单状态">
                <a-tag color="#F03B44">
                  异常
                </a-tag>
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
          <a-divider />
        </a-row>
        <a-row justify="start" type="flex">
          <a-col :sm="24">
            <a-form-model-item label="商户请求参数">
              <a-input
                  type="textarea"
                  disabled="disabled"
                  style="height: 100px;color: black"
                  v-model="detailData.mchReq"
              />
            </a-form-model-item>
          </a-col>
        </a-row>
        <a-row justify="start" type="flex">
          <a-col :sm="24">
            <a-form-model-item label="错误信息">
              <a-input
                  type="textarea"
                  disabled="disabled"
                  style="height: 100px;color: black"
                  v-model="detailData.mchResp"
              />
            </a-form-model-item>
          </a-col>
        </a-row>
      </a-drawer>
    </template>
  </page-header-wrapper>
</template>
<script>
import JeepayTextUp from '@/components/JeepayTextUp/JeepayTextUp' // 文字上移组件
import JeepayTable from '@/components/JeepayTable/JeepayTable'
import JeepayTableColumns from '@/components/JeepayTable/JeepayTableColumns'
import {
  ERROR_ORDER_LIST,
  req
} from '@/api/manage'
import moment from 'moment'
import { getOrderStateColor, getOrderStateName } from '@/utils/util'

// eslint-disable-next-line no-unused-vars
const tableColumns = [
  { key: 'mchNo', title: '商户号', ellipsis: true, width: 100, scopedSlots: { customRender: 'mchSlot' } },
  { key: 'mchName', title: '商户名（快照）', ellipsis: true, width: 200, dataIndex: 'mchName' },
  { key: 'orderNo', title: '商户订单号', scopedSlots: { customRender: 'orderSlot' }, width: 250 },
  { key: 'amount', title: '支付金额', ellipsis: true, width: 150, scopedSlots: { customRender: 'amountSlot' } },
  { key: 'product', title: '支付产品', ellipsis: true, width: 150, scopedSlots: { customRender: 'productSlot' } },
  { key: 'state', title: '支付状态', scopedSlots: { customRender: 'stateSlot' }, width: 100 },
  { key: 'createdAt', dataIndex: 'createdAt', title: '创建日期', width: 150 },
  { key: 'op', title: '操作', width: 150, align: 'center', scopedSlots: { customRender: 'opSlot' } }
]

export default {
  name: 'OrderErrorList',
  components: { JeepayTable, JeepayTableColumns, JeepayTextUp },
  data () {
    return {
      btnLoading: false,
      tableColumns: tableColumns,
      searchData: { },
      createdStart: '', // 选择开始时间
      createdEnd: '', // 选择结束时间
      visible: false,
      detailData: {},
      selectedRange: [],
      realTimeStatData: {
        'totalAmount': 0,
        'totalCount': 0
      },
      ranges: {
        今天: [moment().startOf('day'), moment().endOf('day')],
        昨天: [moment().subtract(1, 'day').startOf('day'), moment().subtract(1, 'day').endOf('day')]
      }
    }
  },
  computed: {
  },
  mounted () {
    this.$message.info('异常订单仅保留近三天数据，请知悉')
    this.selectedRange = [moment().startOf('day'), moment().endOf('day')] // 开始时间
    this.searchData.createdStart = this.selectedRange[0].format('YYYY-MM-DD HH:mm:ss') // 开始时间
    this.searchData.createdEnd = this.selectedRange[1].format('YYYY-MM-DD HH:mm:ss') // 结束时间
    this.queryFunc()
  },
  methods: {
    getOrderStateColor,
    getOrderStateName,
    queryFunc () {
      this.btnLoading = true
      this.getStatData()
      this.$refs.infoTable.refTable(true)
    },
    // 请求table接口数据
    reqTableDataFunc: (params) => {
      return req.list(ERROR_ORDER_LIST, params)
    },
    searchFunc: function () { // 点击【查询】按钮点击事件
      this.$refs.infoTable.refTable(false)
    },
    detailFunc: function (recordId) {
      const that = this
      req.getById(ERROR_ORDER_LIST, recordId).then(res => {
        that.detailData = res
      })
      this.visible = true
    },
    moment,
    onChange (date, dateString) {
      this.searchData.createdStart = dateString[0] // 开始时间
      this.searchData.createdEnd = dateString[1] // 结束时间
    },
    disabledDate (current) { // 今日之后日期不可选
      return current && current > moment().endOf('day')
    },
    onClose () {
      this.visible = false
    },
    changeStr2ellipsis (orderNo, baseLength) {
      const halfLengh = parseInt(baseLength / 2)
      return orderNo.substring(0, halfLengh - 1) + '...' + orderNo.substring(orderNo.length - halfLengh, orderNo.length)
    },
    resetSearch () {
      this.selectedRange = []
      this.searchData = {}
    },
    getStatData () {
      const that = this
      that.statLoading = true
      req.postDataNormal('/api/errorRealTimeStatOrder', '', this.searchData).then(res => { // 产品下拉列表
        that.realTimeStatData = res
        that.statLoading = false
      })
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
      height: 24px;
      line-height: 24px;
      width: 60px;
      border-radius: 2px;
      text-align: center;
      margin-right: 4px;
    }
  }
}

.mch-name {
  -webkit-text-size-adjust:none;
  font-size: 12px;
  display: flex;
  flex-direction: column;
  p {
    white-space:nowrap;
    span {
      display: inline-block;
      //font-weight: 800;
      height: 16px;
      line-height: 16px;
      width: 60px;
      text-align: center;
    }
  }
}
.stat-col{
  position: relative;
  //background-color: gray;
  height: 100px;
  border-radius: 13px;
}

.stat-col b{
  position: absolute;
  font-size: 30px;
  top: 10px;
  left: 20px;
}

.stat-col .sub-content{
  //font-weight: bold;
  position: absolute;
  font-size: 15px;
  line-height: 20px;
  height: 20px;
  text-align: right;
  bottom: 20px;
  right: 20px;
  color: #717579;
}

.stat-col img{
  position: absolute;
  top: 20px;
  right: 20px;
}

.stat-col .title{
  position: absolute;
  bottom: 20px;
  left: 20px;
  color: #717579;
}

.stat-col .num{
  position: absolute;
  border-radius: 13px;
}

.bg-color-1{
  background-color: #FFDADA;
}
.bg-color-2{
  background-color: #FFEAD1;
}
.bg-color-3{
  background-color: #DFEFFF;
}
.bg-color-4{
  background-color: #EFE1FF;
}
.bg-color-5{
  background-color: #E4FFEF;
}
</style>
