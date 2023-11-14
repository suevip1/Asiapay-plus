<template>
  <page-header-wrapper>
    <a-row :gutter="16">
      <a-col :span="4">
        <a-card>
          <a-statistic
                title="账户总余额"
                :value="((mchInfo.balance + mchInfo.freezeBalance)/100).toFixed(2)"
                :precision="2"
                :value-style="{ color: '#439dc4' }"
                style="margin: 10px"
            >
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="4">
        <a-card>
          <a-statistic
                title="可提现余额"
                :value="((mchInfo.balance)/100).toFixed(2)"
                :precision="2"
                :value-style="{ color: '#3f8600' }"
                style="margin: 10px"
          >
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="4">
        <a-card>
          <a-statistic
                title="冻结余额"
                :value="(mchInfo.freezeBalance/100).toFixed(2)"
                :precision="2"
                class="demo-class"
                :value-style="{ color: '#cf1322' }"
                style="margin: 10px"
          >
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>
    <a-row style="margin-top: 20px">
      <a-col>
        <a-button type="primary" icon="plus" @click="openDivision">申请结算</a-button>
      </a-col>
    </a-row>
    <a-card style="margin-top: 20px">
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
                <a-icon slot="suffixIcon" type="sync"/>
              </a-range-picker>
            </a-form-item>
            <jeepay-text-up :placeholder="'流水单号'" :msg="searchData.recordId" v-model="searchData.recordId"/>
            <a-form-item label="" class="table-head-layout">
              <!-- 1-待结算 2-结算成功, 3-结算失败(取消) 4 超时 -->
              <a-select v-model="searchData.state" placeholder="状态" default-value="0">
                <a-select-option value="">全部</a-select-option>
                <a-select-option value="1">待结算</a-select-option>
                <a-select-option value="2">结算成功</a-select-option>
                <a-select-option value="3">结算失败</a-select-option>
              </a-select>
            </a-form-item>
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
            :initData="true"
            :reqTableDataFunc="reqTableDataFunc"
            :tableColumns="tableColumns"
            :searchData="searchData"
            rowKey="recordId"
      >
        <template slot="nameSlot" slot-scope="{record}">
          <b>[{{ record.userNo }}]</b><span>{{ record.userName }}</span>
        </template>
        <template slot="amountSlot" slot-scope="{record}">
          <b style="color: #1b8fcd">{{(record.amount / 100).toFixed(2)}}</b>
        </template>
        <template slot="divisionAmountCount" slot-scope="{record}">
          <b style="color: #4BD884">{{(record.divisionAmount / 100).toFixed(2)}}</b>
        </template>
        <template slot="feeSlot" slot-scope="{record}">
          <span >{{(record.divisionFeeRate / 100).toFixed(2)}}</span>
        </template>
        <template slot="stateSlot" slot-scope="{record}">
          <!-- 1-待结算 2-结算成功, 3-结算失败(取消),4-超时关闭 -->
          <a-tag :key="record.state" :color="record.state === 1?'blue':record.state === 2?'#4BD884':record.state === 3?'#F03B44':''">
            {{ record.state === 1?'待结算':record.state === 2?'结算成功':record.state === 3?'结算失败':'超时关闭'}}
          </a-tag>
        </template>
      </JeepayTable>
    </a-card>
    <!-- 弹窗 -->
    <template>
      <a-modal v-model="isShowModal" title="申请结算" @ok="handleOkFunc">
        <a-form-model ref="infoFormModel" :model="changeObject" :label-col="{span: 6}" :wrapper-col="{span: 15}" :rules="changeRules">
          <a-form-model-item label="商户号：" >
            <span style="color: black">{{mchInfo.mchNo}}</span>
          </a-form-model-item>
          <a-form-model-item label="商户名称：">
            <span style="color: black">{{mchInfo.mchName}}</span>
          </a-form-model-item>
          <a-form-model-item label="申请金额：" prop="amount">
            <a-input prefix="￥" type="number" v-model="changeObject.amount" />
            <b style="color: gray">请输入申请金额，例如:1000.50</b>
          </a-form-model-item>
          <a-form-model-item label="备注：">
            <a-input v-model="changeObject.remark" />
          </a-form-model-item>
        </a-form-model>
      </a-modal>
    </template>
  </page-header-wrapper>
</template>
<script>

import moment from 'moment'
import {
  API_URL_MCH_DIVISION, API_URL_MCH_INFO, req
} from '@/api/manage'
import JeepayTableColumns from '@/components/JeepayTable/JeepayTableColumns.vue'
import JeepayTable from '@/components/JeepayTable/JeepayTable.vue'
import JeepayTextUp from '@/components/JeepayTextUp/JeepayTextUp.vue' // 空数据展示的组件，首页自用
const tableColumns = [
  { key: 'id', fixed: 'left', width: '150px', title: '流水单号', dataIndex: 'recordId' },
  { key: 'name', fixed: 'left', width: '250px', title: '商户', scopedSlots: { customRender: 'nameSlot' } },
  { key: 'createdAt', fixed: 'left', width: '200px', title: '申请时间', dataIndex: 'createdAt' },
  { key: 'amount', title: '申请金额(￥)', width: 150, scopedSlots: { customRender: 'amountSlot' }, fixed: 'left' },
  { key: 'divisionAmount', title: '到账金额(￥)', width: '150px', scopedSlots: { customRender: 'divisionAmountCount' } },
  { key: 'fee', title: '服务费(￥)', width: '150px', scopedSlots: { customRender: 'feeSlot' } },
  { key: 'state', title: '状态', width: '150px', scopedSlots: { customRender: 'stateSlot' } }
]
export default {
  data () {
    return {
      btnLoading: false,
      searchData: {}, // 查询条件
      selectedRange: [],
      changeObject: {},
      ranges: {
        今天: [moment().startOf('day'), moment().endOf('day')],
        昨天: [moment().subtract(1, 'day').startOf('day'), moment().subtract(1, 'day').endOf('day')],
        近一周: [
          moment().subtract(1, 'week').startOf('day'),
          moment().endOf('day')
        ]
      },
      tableColumns: tableColumns,
      mchInfo: {},
      isShowModal: false,
      changeRules: {
        amount: [
          { required: true, message: '请输入申请金额', trigger: 'blur' }
        ]
      }
    }
  },
  components: { JeepayTextUp, JeepayTable, JeepayTableColumns },
  methods: {
    reqTableDataFunc: (params) => {
      return req.list(API_URL_MCH_DIVISION, params)
    },
    moment,
    queryFunc () {
      this.btnLoading = true
      this.$refs.infoTable.refTable(true)
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
    openDivision () {
      this.isShowModal = true
    },
    handleOkFunc () {
      const that = this
      if (this.changeObject.amount < 0) {
        this.$message.error('金额格式错误')
        return
      }
      that.isShowModal = false
      const param = {}
      param.amount = this.changeObject.amount * 100
      param.remark = this.changeObject.remark
      req.add(API_URL_MCH_DIVISION, param).then(res => {
        that.$message.info('提交成功')
        req.getById(API_URL_MCH_INFO, 'info').then(res => {
          that.mchInfo = res
          that.$refs.infoTable.refTable(true)
        }).catch((err) => {
          console.error(err)
        })
      }).catch((err) => {
        console.error(err)
      })
    }
  },
  computed: {
  },
  mounted () {
    const that = this
    req.getById(API_URL_MCH_INFO, 'info').then(res => {
      that.mchInfo = res
    }).catch((err) => {
      console.error(err)
    })
  }
}
</script>
<style scoped lang="less">

</style>
