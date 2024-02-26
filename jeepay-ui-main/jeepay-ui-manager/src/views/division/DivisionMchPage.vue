<template>
  <page-header-wrapper>
    <a-row :gutter="16">
      <a-col :span="4">
        <a-card>
          <a-statistic
              title="待审核申请（条）"
              :value="countData.count"
              :precision="0"
              :value-style="{ color: '#439dc4' }"
              style="margin: 10px"
          >
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="4">
        <a-card>
          <a-statistic
              title="待审核总金额"
              :value="((countData.totalAmount)/100).toFixed(2)"
              :precision="2"
              :value-style="{ color: '#cf1322' }"
              style="margin: 10px"
          >
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="4">
        <a-card class="division" style="height: 81px">
          <p>最小结算: <b>{{ (mchMinWithdraw / 100).toFixed(2) }}</b></p>
          <p>单笔手续费: <b>{{ (mchFee / 100).toFixed(2) }}</b></p>
          <p>单笔费率: <b>{{ (mchFeeRate * 100).toFixed(2) }}%</b></p>
        </a-card>
      </a-col>
      <a-col :span="4">
        <a-button type="primary" @click="showSetModal">商户结算设置</a-button>
      </a-col>
    </a-row>
    <a-card style="margin-top: 20px">
      <div class="table-page-search-wrapper" @keydown.enter="queryFunc">
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
            <jeepay-text-up :placeholder="'商户号'" :msg="searchData.userNo" v-model="searchData.userNo"/>
            <a-form-item label="" class="table-head-layout">
              <!-- 1-待结算 2-结算成功, 3-结算失败(取消) -->
              <a-select v-model="searchData.state" placeholder="状态" default-value="1">
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
        <template slot="opSlot" slot-scope="{record}">  <!-- 操作列插槽 -->
          <JeepayTableColumns>
<!--            <a-button type="link" v-if="$access('ENT_DIVISION_MCH') " @click="detailFunc(record.payOrderId)">查看详情</a-button>-->
            <a-button type="link" v-if="$access('ENT_DIVISION_MCH') && record.state === 1" @click="clickReviewedFunc(record)">审核</a-button>
          </JeepayTableColumns>
        </template>
      </JeepayTable>
    </a-card>
    <!-- 审核抽屉 -->
    <template>
      <a-drawer
          width="30%"
          placement="right"
          :closable="true"
          :visible="visible"
          :title="visible === true? '订单详情':''"
          @close="onClose"
      >
        <a-row justify="space-between" type="flex">
          <a-col :sm="24">
            <a-descriptions>
              <a-descriptions-item label="商户号">
                <b>{{ detailData.userNo }}</b>
              </a-descriptions-item>
            </a-descriptions>
          </a-col>
          <a-col :sm="24">
            <a-descriptions>
              <a-descriptions-item label="商户名称">
                {{ detailData.userName }}
              </a-descriptions-item>
            </a-descriptions>
          </a-col>
          <a-col :sm="24">
            <a-descriptions>
              <a-descriptions-item label="申请时间">
                {{ detailData.createdAt }}
              </a-descriptions-item>
            </a-descriptions>
          </a-col>
          <a-col :sm="24">
            <a-descriptions>
              <a-descriptions-item label="过期时间">
                {{ detailData.expiredTime }}
              </a-descriptions-item>
            </a-descriptions>
          </a-col>
          <a-col :sm="24">
            <a-descriptions>
              <a-descriptions-item label="订单状态">
                <a-tag :key="detailData.state" :color="detailData.state === 1?'blue':detailData.state === 2?'#4BD884':detailData.state === 3?'#F03B44':''">
                  {{ detailData.state === 1?'待结算':detailData.state === 2?'结算成功':detailData.state === 3?'结算失败':'超时关闭'}}
                </a-tag>
              </a-descriptions-item>
            </a-descriptions>
          </a-col>
          <a-divider />
          <a-col :sm="24">
            <a-descriptions>
              <a-descriptions-item label="申请金额">
                <a-tag color="green">
                  {{ (detailData.amount/100).toFixed(2) }}
                </a-tag>
              </a-descriptions-item>
            </a-descriptions>
          </a-col>
          <a-col :sm="24">
            <a-descriptions><a-descriptions-item label="手续费"><b>{{ (detailData.divisionAmountFee/100).toFixed(2) }}</b></a-descriptions-item></a-descriptions>
          </a-col>
          <a-col :sm="24">
            <a-form-model-item label="商户备注">
              <a-input
                  type="textarea"
                  disabled="true"
                  style="height: 100px;color: black"
                  v-model="detailData.remark"
              />
            </a-form-model-item>
          </a-col>
          <a-divider />
        </a-row>
        <a-row justify="start" type="flex">
          <a-col :sm="24">
            <a-form-model-item label="审核备注">
              <a-input
                  type="textarea"
                  style="height: 100px;color: black"
                  v-model="reviewedRemark"
              />
            </a-form-model-item>
          </a-col>
        </a-row>
        <div class="drawer-btn-center">
          <a-button type="primary" style="margin-right:8px" icon="check" @click="handleOkFunc" :loading="btnLoading">
            已汇款
          </a-button>
          <a-button type="danger" style="margin-right:8px" icon="close" @click="handleRefuseFunc" :loading="btnLoading">
            驳回
          </a-button>
          <a-button @click="onClose" style="margin-right:8px">
            关闭
          </a-button>
        </div>
      </a-drawer>
    </template>
    <template>
      <a-modal v-model="isShowSetModal" title="商户结算设置" @ok="handleSetOkFunc">
        <a-form-model :label-col="{span: 8}" :wrapper-col="{span: 13}">
          <b style="color: rgb(128,128,128);font-size: 13px">商户结算手续费=结算金额*比例手续费+固定手续费</b><br/>
          <span style="color: rgb(128,128,128);font-size: 13px">如：申请结算10000元，手续费1%，固定手续费10元。</span><br/>
          <span style="color: rgb(128,128,128);font-size: 13px">综合手续费=10000*1%+10 为110元</span><br/><br/>
          <a-form-model-item label="最小结算金额设置">
            <a-input prefix="￥" v-model="saveObject.mchMinWithdraw" placeholder="请输入" type="number" />
          </a-form-model-item>
          <a-form-model-item label="单笔 [固定] 手续费">
            <a-input prefix="￥" v-model="saveObject.mchFee" placeholder="请输入" type="number" />
          </a-form-model-item>
          <a-form-model-item label="单笔 [比例] 手续费">
            <a-input prefix="%" v-model="saveObject.mchFeeRate" placeholder="请输入" type="number" />
          </a-form-model-item>
        </a-form-model>
      </a-modal>
    </template>
  </page-header-wrapper>
</template>
<script>

import moment from 'moment'
import {
  API_URL_MCH_DIVISION, req
} from '@/api/manage'
import JeepayTableColumns from '@/components/JeepayTable/JeepayTableColumns.vue'
import JeepayTable from '@/components/JeepayTable/JeepayTable.vue'
import JeepayTextUp from '@/components/JeepayTextUp/JeepayTextUp.vue' // 空数据展示的组件，首页自用
const tableColumns = [
  { key: 'id', fixed: 'left', width: '150px', title: '流水单号', dataIndex: 'recordId' },
  { key: 'name', width: '250px', title: '商户', scopedSlots: { customRender: 'nameSlot' }, fixed: 'left' },
  { key: 'createdAt', width: '200px', title: '申请时间', dataIndex: 'createdAt' },
  { key: 'amount', title: '申请金额(￥)', width: 150, scopedSlots: { customRender: 'amountSlot' } },
  { key: 'divisionAmount', title: '到账金额(￥)', width: '150px', scopedSlots: { customRender: 'divisionAmountCount' } },
  { key: 'fee', title: '服务费(￥)', width: '150px', scopedSlots: { customRender: 'feeSlot' } },
  { key: 'state', title: '状态', width: '150px', scopedSlots: { customRender: 'stateSlot' } },
  { key: 'op', title: '操作', width: 180, fixed: 'right', align: 'center', scopedSlots: { customRender: 'opSlot' } }
]
export default {
  data () {
    return {
      btnLoading: false,
      searchData: {
        'state': '1'
      }, // 查询条件
      selectedRange: [],
      changeObject: {},
      groupKey: 'payConfigGroup',
      saveObject: {
        'mchFee': 0,
        'mchFeeRate': 0,
        'mchMinWithdraw': 0
      },
      mchFee: 0,
      mchFeeRate: 0,
      mchMinWithdraw: 0,
      visible: false, // 审核抽屉
      isShowSetModal: false, // 结算设置弹窗
      ranges: {
        今天: [moment().startOf('day'), moment().endOf('day')],
        昨天: [moment().subtract(1, 'day').startOf('day'), moment().subtract(1, 'day').endOf('day')],
        近一周: [
          moment().subtract(1, 'week').startOf('day'),
          moment().endOf('day')
        ]
      },
      tableColumns: tableColumns,
      isShowModal: false,
      detailData: {},
      reviewedRemark: '',
      countData: {
        'totalAmount': 0,
        'count': 0
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
    clickReviewedFunc (record) {
      this.detailData = record
      this.visible = true
    },
    handleOkFunc () {
      const that = this
      const param = {}
      param.state = 2
      param.remark = this.reviewedRemark
      req.postDataNormal(API_URL_MCH_DIVISION, 'reviewOk/' + this.detailData.recordId, param).then(res => {
        that.$message.info('操作成功')
        that.visible = false
        that.reviewedRemark = ''
        that.detailData = {}
        that.$refs.infoTable.refTable(true)
      }).catch((err) => {
        console.error(err)
      })
    },
    handleRefuseFunc () {
      const that = this
      const param = {}
      param.state = 3
      param.remark = this.reviewedRemark
      req.postDataNormal(API_URL_MCH_DIVISION, 'reviewRefuse/' + this.detailData.recordId, param).then(res => {
        that.$message.info('操作成功')
        that.visible = false
        that.reviewedRemark = ''
        that.detailData = {}
        that.$refs.infoTable.refTable(true)
      }).catch((err) => {
        console.error(err)
      })
    },
    onClose () {
      this.visible = false
    },
    showSetModal () {
      this.isShowSetModal = true
      this.saveObject = {
        'mchFee': this.mchFee / 100,
        'mchFeeRate': this.mchFeeRate * 100,
        'mchMinWithdraw': this.mchMinWithdraw / 100
      }
    },
    handleSetOkFunc () {
      if (this.mchMinWithdraw <= this.mchFee && this.mchFee !== 0) {
        this.$message.error('提现手续费不能大于最小提现金额')
        return
      }
      this.setWithdrawConfig()
      this.isShowSetModal = false
    },
    setWithdrawConfig () {
      const that = this
      const num1 = Number(that.saveObject.mchFee)
      const num2 = Number(that.saveObject.mchFeeRate)
      const num3 = Number(that.saveObject.mchMinWithdraw)
      if (!(typeof num1 === 'number' && !isNaN(num1))) {
        that.$message.error('设置的格式有误，只能输入数字')
        return
      }
      if (!(typeof num2 === 'number' && !isNaN(num2))) {
        that.$message.error('设置的格式有误，只能输入数字')
        return
      }
      if (!(typeof num3 === 'number' && !isNaN(num3))) {
        that.$message.error('设置的格式有误，只能输入数字')
        return
      }
      const params = { }
      params.mchFee = that.saveObject.mchFee * 100
      params.mchFeeRate = that.saveObject.mchFeeRate / 100
      params.mchMinWithdraw = that.saveObject.mchMinWithdraw * 100
      req.postDataNormal(API_URL_MCH_DIVISION, 'setConfig', params).then(res => {
        that.$message.success('修改结算设置成功')
        that.getInitData()
      }).catch((err) => {
        console.error(err)
      })
    },
    getInitData () {
      const that = this
      req.postDataNormal(API_URL_MCH_DIVISION, 'count').then(res => {
        that.countData = res
      }).catch((err) => {
        console.error(err)
      })
      req.postDataNormal(API_URL_MCH_DIVISION, 'getConfig').then(res => {
        // that.saveObject = res
        that.mchFee = res.mchFee
        that.mchFeeRate = res.mchFeeRate
        that.mchMinWithdraw = res.mchMinWithdraw
      }).catch((err) => {
        console.error(err)
      })
    }
  },
  computed: {
  },
  mounted () {
    this.getInitData()
  }
}
</script>
<style scoped lang="less">
.division p{
  //margin-top: 6px;
  margin: 3px;
  margin-left: 12px;
}
</style>
