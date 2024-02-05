<template>
  <page-header-wrapper>
    <a-card>
      <div class="table-page-search-wrapper" @keyup.enter="queryFunc">
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
            <jeepay-text-up :placeholder="'订单号'" :msg="searchData.orderId" v-model="searchData.orderId" />
            <jeepay-text-up :placeholder="'通道订单号'" :msg="searchData.passageOrderNo" v-model="searchData.passageOrderNo" />
            <jeepay-text-up :placeholder="'商户号'" :msg="searchData.mchNo" v-model="searchData.mchNo" />
            <a-form-item label="" class="table-head-layout">
              <a-select v-model="searchData.state" placeholder="通知状态" default-value="3">
                <a-select-option value="">全部</a-select-option>
                <a-select-option value="1">通知中</a-select-option>
                <a-select-option value="2">通知成功</a-select-option>
                <a-select-option value="3">通知失败</a-select-option>
              </a-select>
            </a-form-item>
            <span class="table-page-search-submitButtons">
              <a-button type="primary" icon="search" @click="queryFunc" :loading="btnLoading">搜索</a-button>
              <a-button style="margin-left: 8px" icon="reload" @click="resetSearch">重置</a-button>
              <a-popconfirm v-if="$access('ENT_MCH_NOTIFY_RESEND')" title="确认重发全部通知么?" ok-text="确认" cancel-text="取消" @confirm="resendAllNotify">
                <a-button style="margin-left: 8px" type="danger" icon="minus-circle" >重发全部通知</a-button>
              </a-popconfirm>
            </span>
          </div>
        </a-form>
      </div>

      <!-- 列表渲染 -->
      <JeepayTable
        @btnLoadClose="btnLoading=false"
        ref="infoTable"
        :closable="true"
        :initData="false"
        :reqTableDataFunc="reqTableDataFunc"
        :tableColumns="tableColumns"
        :searchData="searchData"
        :pageSize="50"
        rowKey="orderId"
      >
        <template slot="orderIdSlot" slot-scope="{record}">
          <b>{{record.orderId}}</b>
        </template>
        <template slot="stateSlot" slot-scope="{record}">
          <a-tag
            :key="record.state"
            :color="record.state === 1?'orange':record.state === 2?'#4BD884':'#F03B44'"
          >
            {{ record.state === 1?'通知中':record.state === 2?'通知成功':record.state === 3?'通知失败':'未知' }}
          </a-tag>
        </template>
        <template slot="passageSlot" slot-scope="{record}">
          <span style="color: #1A79FF;">[{{ record.payPassageId }}]</span>&nbsp;<span>{{ record.payPassageName }}</span>
        </template> <!-- 商户信息插槽 -->
        <template slot="orderTypeSlot" slot-scope="{record}">
          <a-tag
            :key="record.orderType"
            :color="record.orderType === 1?'#4BD884':record.orderType === 2?'#F03B44': record.orderType === 3? 'blue': 'orange'"
          >
            {{ record.orderType === 1?'支付':record.orderType === 2?'代付':record.orderType === 3? '提现':'未知' }}
          </a-tag>
        </template>
        <template slot="opSlot" slot-scope="{record}">  <!-- 操作列插槽 -->
          <JeepayTableColumns>
            <a-button type="link" v-if="$access('ENT_MCH_NOTIFY_VIEW')" @click="detailFunc(record.notifyId)">详情</a-button>
            <a-button type="link" v-if="$access('ENT_MCH_NOTIFY_RESEND') && record.state === 3" @click="resendFunc(record.notifyId)">重发通知</a-button>
          </JeepayTableColumns>
        </template>
      </JeepayTable>
    </a-card>
    <!-- 日志详情抽屉 -->
    <template>
      <a-drawer
        width="40%"
        placement="right"
        :closable="true"
        :visible="visible"
        :title="visible === true? '商户通知详情':''"
        @close="onClose"
      >
        <a-row justify="space-between" type="flex">
          <a-col :sm="12">
            <a-descriptions>
              <a-descriptions-item label="订单号">
                <a-tag color="purple">
                  {{ detailData.orderId }}
                </a-tag>
              </a-descriptions-item>
            </a-descriptions>
          </a-col>
          <a-col :sm="12">
            <a-descriptions>
              <a-descriptions-item label="代理商号">
                {{ detailData.agentNo }}
              </a-descriptions-item>
            </a-descriptions>
          </a-col>
          <a-col :sm="12">
            <a-descriptions>
              <a-descriptions-item label="通道订单号">
                {{ detailData.passageOrderNo }}
              </a-descriptions-item>
            </a-descriptions>
          </a-col>
          <a-col :sm="12">
            <a-descriptions>
              <a-descriptions-item label="商户号">
                {{ detailData.mchNo }}
              </a-descriptions-item>
            </a-descriptions>
          </a-col>
          <a-col :sm="12">
            <a-descriptions>
              <a-descriptions-item label="订单类型">
                <a-tag :color="detailData.orderType === 1?'#4BD884':detailData.orderType === 2?'#F03B44': detailData.orderType === 3? 'blue' : 'orange'">
                  {{ detailData.orderType === 1?'支付':detailData.orderType === 2?'代付':detailData.orderType === 3 ? '提现': '未知' }}
                </a-tag>
              </a-descriptions-item>
            </a-descriptions>
          </a-col>
          <a-col :sm="12">
            <a-descriptions>
              <a-descriptions-item label="通知状态">
                <a-tag :color="detailData.state === 1?'orange':detailData.state === 2?'#4BD884':'#F03B44'">
                  {{ detailData.state === 1?'通知中':detailData.state === 2?'通知成功':detailData.state === 3?'通知失败':'未知' }}
                </a-tag>
              </a-descriptions-item>
            </a-descriptions>
          </a-col>
          <a-col :sm="12">
            <a-descriptions>
              <a-descriptions-item label="通知次数">
                {{ detailData.notifyCount }}
              </a-descriptions-item>
            </a-descriptions>
          </a-col>
          <a-col :sm="24">
            <a-descriptions>
              <a-descriptions-item label="最后通知时间">
                {{ detailData.lastNotifyTime }}
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
          <a-col :sm="24">
            <a-form-model-item label="通知地址">
              <a-input
                type="textarea"
                disabled="disabled"
                style="height: 100px;color: black"
                v-model="detailData.notifyUrl"
              />
            </a-form-model-item>
          </a-col>
          <a-col :sm="24">
            <a-form-model-item label="响应结果">
              <a-input
                type="textarea"
                disabled="disabled"
                style="height: 100px;color: black"
                v-model="detailData.resResult"
              />
            </a-form-model-item>
          </a-col>
        </a-row>
      </a-drawer>
    </template>
  </page-header-wrapper>
</template>
<script>
  import JeepayTable from '@/components/JeepayTable/JeepayTable'
  import JeepayTextUp from '@/components/JeepayTextUp/JeepayTextUp' // 文字上移组件
  import JeepayTableColumns from '@/components/JeepayTable/JeepayTableColumns'
  import { API_URL_MCH_NOTIFY_LIST, req, mchNotifyResend, API_URL_MCH_NOTIFY_RESEND_ALL } from '@/api/manage'
  import moment from 'moment'

  // eslint-disable-next-line no-unused-vars
  const tableColumns = [
    { key: 'orderId', title: '订单号', fixed: 'left', scopedSlots: { customRender: 'orderIdSlot' } },
    { key: 'passageOrderNo', title: '通道订单号', dataIndex: 'passageOrderNo' },
    { key: 'state', title: '通知状态', width: '100px', scopedSlots: { customRender: 'stateSlot' } },
    { key: 'orderType', title: '订单类型', width: '100px', scopedSlots: { customRender: 'orderTypeSlot' } },
    { key: 'mchNo', dataIndex: 'mchNo', title: '商户号' },
    { key: 'passageName', title: '通道', scopedSlots: { customRender: 'passageSlot' }, width: 250 },
    { key: 'updatedAt', dataIndex: 'updatedAt', title: '更新日期' },
    { key: 'op', title: '操作', width: '150px', fixed: 'right', align: 'center', scopedSlots: { customRender: 'opSlot' } }
  ]

  export default {
    name: 'IsvListPage',
    components: { JeepayTable, JeepayTableColumns, JeepayTextUp },
    data () {
      return {
        btnLoading: true,
        tableColumns: tableColumns,
        searchData: {},
        selectedIds: [], // 选中的数据
        createdStart: '', // 选择开始时间
        createdEnd: '', // 选择结束时间
        visible: false,
        detailData: {},
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
    computed: {
      rowSelection () {
        const that = this
        return {
          onChange: (selectedRowKeys, selectedRows) => {
            that.selectedIds = [] // 清空选中数组
            selectedRows.forEach(function (data) { // 赋值选中参数
              that.selectedIds.push(data.payOrderId)
            })
          }
        }
      }
    },
    mounted () {
      this.selectedRange = [moment().startOf('day'), moment().endOf('day')] // 开始时间
      this.searchData.createdStart = this.selectedRange[0].format('YYYY-MM-DD HH:mm:ss') // 开始时间
      this.searchData.createdEnd = this.selectedRange[1].format('YYYY-MM-DD HH:mm:ss') // 结束时间
      this.searchData.state = '3'
      this.$refs.infoTable.refTable(true)
    },
    methods: {
      queryFunc () {
        this.btnLoading = true
        this.$refs.infoTable.refTable(true)
      },
      // 请求table接口数据
      reqTableDataFunc: (params) => {
        return req.list(API_URL_MCH_NOTIFY_LIST, params)
      },
      searchFunc: function () { // 点击【查询】按钮点击事件
        this.$refs.infoTable.refTable(true)
      },
      detailFunc: function (recordId) {
        const that = this
        req.getById(API_URL_MCH_NOTIFY_LIST, recordId).then(res => {
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
      resendFunc (notifyId) { // 重发通知
        const that = this

        this.$infoBox.confirmPrimary('确认重发通知？', '', () => {
          mchNotifyResend(notifyId).then(res => {
            that.$message.success('任务更新成功，请稍后查看最新状态！')
            that.searchFunc()
          })
        })
      },
      resetSearch () {
        this.searchData = {}
        this.selectedRange = []
      },
      resendAllNotify () {
        const that = this
        console.log('resendAllNotify')
        req.postDataNormal(API_URL_MCH_NOTIFY_RESEND_ALL, '', this.searchData).then(res => {
          that.$message.success('任务更新成功，请稍后查看最新状态！')
        })
      }
    }
  }
</script>
