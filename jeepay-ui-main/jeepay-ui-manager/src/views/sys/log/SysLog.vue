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
                :ranges="ranges"
                v-model="selectedRange"
              >
                <a-icon slot="suffixIcon" type="sync" />
              </a-range-picker>
            </a-form-item>
            <jeepay-text-up :placeholder="'用户登录名'" :msg="searchData.loginUsername" v-model="searchData.loginUsername" />
            <jeepay-text-up :placeholder="'操作描述'" :msg="searchData.methodRemark" v-model="searchData.methodRemark" />
            <a-form-item label="" class="table-head-layout">
              <a-select v-model="searchData.sysType" placeholder="所属系统" default-value="">
                <a-select-option value="">全部</a-select-option>
                <a-select-option value="MGR">运营平台</a-select-option>
                <a-select-option value="MCH">商户系统</a-select-option>
                <a-select-option value="AGENT">代理系统</a-select-option>
              </a-select>
            </a-form-item>

            <span class="table-page-search-submitButtons">
              <a-button type="primary" icon="search" @click="queryFunc" :loading="btnLoading">搜索</a-button>
              <a-button style="margin-left: 8px" icon="reload" @click="resetData">重置</a-button>
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
        rowKey="sysLogId"
      >
        <template slot="userNameSlot" slot-scope="{record}"><b>{{ record.loginUsername }}</b></template> <!-- 自定义插槽 -->
        <template slot="sysTypeSlot" slot-scope="{record}">
          <a-tag :key="record.sysType" :color="record.sysType === 'MGR'?'#4BD884':record.sysType === 'MCH'?'geekblue':record.sysType === 'AGENT'?'orange':'loser'">
            {{ record.sysType === 'MGR'?'运营平台':record.sysType === 'MCH'?'商户系统':record.sysType === 'AGENT'?'代理系统':'其他' }}
          </a-tag>
        </template>
        <template slot="opSlot" slot-scope="{record}">  <!-- 操作列插槽 -->
          <JeepayTableColumns>
            <a-button type="link" v-if="$access('ENT_SYS_LOG_VIEW')" @click="detailFunc(record.sysLogId)">详情</a-button>
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
        :title="visible === true? '日志详情':''"
        @close="onClose"
      >
        <a-row justify="space-between" type="flex">
          <a-col :sm="12">
            <a-descriptions>
              <a-descriptions-item label="用户登录名">
                {{ detailData.loginUsername }}
              </a-descriptions-item>
            </a-descriptions>
          </a-col>
          <a-col :sm="12">
            <a-descriptions>
              <a-descriptions-item label="用户IP">
                {{ detailData.userIp }}
              </a-descriptions-item>
            </a-descriptions>
          </a-col>
          <a-col :sm="12">
            <a-descriptions>
              <a-descriptions-item label="所属系统">
                <a-tag :key="detailData.sysType" :color="detailData.sysType === 'MGR'?'#4BD884':detailData.sysType === 'MCH'?'geekblue':detailData.sysType === 'AGENT'?'orange':'loser'">
                  {{ detailData.sysType === 'MGR'?'运营平台':detailData.sysType === 'MCH'?'商户系统':detailData.sysType === 'AGENT'?'代理系统':'其他' }}
                </a-tag>
              </a-descriptions-item>
            </a-descriptions>
          </a-col>
        </a-row>
        <a-divider />
        <a-row justify="space-between" type="flex">
          <a-col :sm="24">
            <a-descriptions>
              <a-descriptions-item label="操作描述">
                {{ detailData.methodRemark }}
              </a-descriptions-item>
            </a-descriptions>
          </a-col>
          <a-col :sm="24">
            <a-descriptions>
              <a-descriptions-item label="请求方法">
                {{ detailData.methodName }}
              </a-descriptions-item>
            </a-descriptions>
          </a-col>
          <a-col :sm="24">
            <a-descriptions>
              <a-descriptions-item label="请求地址">
                {{ detailData.reqUrl }}
              </a-descriptions-item>
            </a-descriptions>
          </a-col>
        </a-row>
        <a-row justify="start" type="flex">
          <a-col :sm="24">
            <a-form-model-item label="请求参数">
              <a-input
                type="textarea"
                disabled="disabled"
                style="background-color: black;color: #FFFFFF;height: 100px"
                v-model="detailData.optReqParam"
              />
            </a-form-model-item>
          </a-col>
        </a-row>
        <a-row justify="start" type="flex">
          <a-col :sm="24">
            <a-form-model-item label="响应参数">
              <a-input
                type="textarea"
                disabled="disabled"
                style="background-color: black;color: #FFFFFF;height: 150px"
                v-model="detailData.optResInfo"
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
import { API_URL_SYS_LOG, req } from '@/api/manage'
import moment from 'moment'

// eslint-disable-next-line no-unused-vars
const tableColumns = [
  { key: 'loginUsername', title: '用户名', fixed: 'left', scopedSlots: { customRender: 'userNameSlot' } },
  { key: 'userIp', title: '用户IP', dataIndex: 'userIp' },
  { key: 'sysType', title: '所属系统', scopedSlots: { customRender: 'sysTypeSlot' } },
  { key: 'methodRemark', title: '操作描述', ellipsis: true, dataIndex: 'methodRemark' },
  { key: 'createdAt', dataIndex: 'createdAt', title: '创建日期' },
  { key: 'op', title: '操作', width: '100px', fixed: 'right', align: 'center', scopedSlots: { customRender: 'opSlot' } }
]

export default {
  name: 'IsvListPage',
  components: { JeepayTable, JeepayTableColumns, JeepayTextUp },
  data () {
    return {
      tableColumns: tableColumns,
      searchData: {},
      createdStart: '', // 选择开始时间
      createdEnd: '', // 选择结束时间
      visible: false,
      detailData: {},
      btnLoading: false,
      ranges: {
        今天: [moment().startOf('day'), moment().endOf('day')],
        昨天: [moment().subtract(1, 'day').startOf('day'), moment().subtract(1, 'day').endOf('day')],
        近一周: [
          moment().subtract(1, 'week').startOf('day'),
          moment().endOf('day')
        ]
      },
      selectedRange: null
    }
  },
  computed: {
  },
  mounted () {
    this.selectedRange = [moment().startOf('day'), moment().endOf('day')] // 开始时间
    this.searchData.createdStart = this.selectedRange[0].format('YYYY-MM-DD HH:mm:ss') // 开始时间
    this.searchData.createdEnd = this.selectedRange[1].format('YYYY-MM-DD HH:mm:ss') // 结束时间
    this.$refs.infoTable.refTable(true)
  },
  methods: {
    // 请求table接口数据
    reqTableDataFunc: (params) => {
      return req.list(API_URL_SYS_LOG, params)
    },
    searchFunc: function () { // 点击【查询】按钮点击事件
      this.$refs.infoTable.refTable(true)
    },
    detailFunc: function (recordId) {
      const that = this
      req.getById(API_URL_SYS_LOG, recordId).then(res => {
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
    queryFunc () {
      this.btnLoading = true
      this.$refs.infoTable.refTable(true)
    },
    resetData () {
      this.searchData = {}
      this.selectedRange = []
    }
  }
}
</script>
