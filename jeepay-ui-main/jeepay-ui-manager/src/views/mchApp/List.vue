<template>
  <page-header-wrapper>
    <a-card>
      <div class="table-page-search-wrapper" @keydown.enter="queryFunc">
        <a-form layout="inline" class="table-head-ground">
          <div class="table-layer">
            <jeepay-text-up :placeholder="'通道名称'" :msg="searchData.payPassageName" v-model="searchData.payPassageName"/>
            <jeepay-text-up :placeholder="'通道ID'" :msg="searchData.payPassageId" v-model="searchData.payPassageId"/>
            <a-form-model-item label="" class="table-head-layout">
              <a-select v-model="searchData.productId" :allowClear="true" placeholder="对应产品" show-search option-filter-prop="children">
                <a-select-option v-for="d in productList" :value="d.productId" :key="d.productId">
                  {{ d.productName + " [ ID: " + d.productId + " ]" }}
                </a-select-option>
              </a-select>
            </a-form-model-item>
            <a-form-item label="" class="table-head-layout">
              <a-select v-model="searchData.state" placeholder="状态" default-value="">
                <a-select-option value="">全部</a-select-option>
                <a-select-option value="0">禁用</a-select-option>
                <a-select-option value="1">启用</a-select-option>
              </a-select>
            </a-form-item>
            <span class="table-page-search-submitButtons" style="flex-grow: 0; flex-shrink: 0;">
              <a-button type="primary" icon="search" @click="queryFunc" :loading="btnLoading">查询</a-button>
              <a-button style="margin-left: 8px" icon="reload" @click="() => this.searchData = {}">重置</a-button>
            </span>
          </div>
        </a-form>
        <div>
          <a-button v-if="$access('ENT_MCH_APP_ADD')" type="primary" icon="plus" @click="addFunc" class="mg-b-30">新建</a-button>
          <a-button style="margin-left: 16px" v-if="$access('ENT_MCH_APP_EDIT')" type="danger" icon="minus-circle" @click="setAllBalanceZero">余额清空</a-button>
          <a-button style="margin-left: 8px" v-if="$access('ENT_MCH_APP_EDIT')" type="danger" icon="setting" @click="setAutoClean">通道自动日切设置</a-button>
          <a-button style="margin-left: 16px" v-if="$access('ENT_MCH_APP_EDIT')" type="danger" icon="exclamation-circle" @click="setCloseAll">关闭全部通道</a-button>
          <a-button style="margin-left: 16px" v-if="$access('ENT_MCH_APP_EDIT')" type="primary" icon="issues-close" @click="setOpenRecently">打开最近启用通道</a-button>
        </div>
      </div>
      <div style="background-color: #fafafa;padding-left: 15px;padding-top: 10px;padding-bottom: 10px;border-bottom: 1px solid #e8e8e8">
        <a-row>
          <a-col class="stat-col bg-color-1" :span="4">
            <span class="title">通道总数</span>
            <b style="color: #DB4B4B;">{{this.totalPassageInfo.passageNum}}</b>
          </a-col>
          <a-col class="stat-col bg-color-2" :span="4" :offset="1">
            <span class="title">通道总余额</span>
            <b style="color: #FA9D2A;">{{(this.totalPassageInfo.totalBalance/100).toFixed(2)}}</b>
          </a-col>
          <a-col class="stat-col bg-color-3" :span="4" :offset="1">
            <span class="title">通道自动日切状态</span>
            <a-badge class="stat-content" :status="this.totalPassageInfo.payPassageAutoClean === 0?'error':'processing'" :text="this.totalPassageInfo.payPassageAutoClean === 0?'禁用中':'启用中'" />
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
        rowKey="payPassageId"
      >
        <template slot="payPassageId" slot-scope="{record}"> <!-- 通道名插槽 -->
          <span :style="{'color': record.state === 0?'rgba(0, 0, 0, 0.25)':'#007CFD'}">[{{ record.payPassageId }}]</span>&nbsp;<span :style="{'color': record.state === 0?'rgba(0, 0, 0, 0.25)':'#007CFD'}">{{ record.payPassageName }}</span>
          <br/>
          <span :style="{'color': record.state === 0?'rgba(0, 0, 0, 0.25)':''}" style="font-size: 13px;color: #1E2229">[{{ record.productId }}]</span>&nbsp;<span :style="{'color': record.state === 0?'rgba(0, 0, 0, 0.25)':''}" style="font-size: 13px;color: #1E2229">{{ record.productName }}</span>
        </template> <!-- 自定义插槽 -->
        <template slot="productId" slot-scope="{record}">
          <span style="color: #007CFD">[{{ record.productId }}]</span>&nbsp;<span>{{ record.productName }}</span>
        </template> <!-- 自定义插槽 -->
        <template slot="balanceSlot" slot-scope="{record}">
          <a-button size="small" v-if="$access('ENT_MCH_APP_EDIT')" type="primary" @click="clickChangeBalance(record)" >调额</a-button>
          &nbsp;<b :style="{'color': record.balance >0 ? '#4BD884' : '#DB4B4B'}" >{{ (record.balance/100).toFixed(2) }}</b>
        </template> <!-- 自定义插槽 -->
        <template slot="weightsSlot" slot-scope="{record}">
          <a-button size="small" v-if="$access('ENT_MCH_APP_EDIT')" type="primary" @click="clickChangeWeights(record)" >设置</a-button>
          &nbsp;<span style="color: #1E2229">{{record.weights}}</span>
        </template> <!-- 自定义插槽 -->
        <template slot="stateSlot" slot-scope="{record}">
          <JeepayTableColState :state="record.state" :showSwitchType="true" :onChange="(state) => { return onSwitchChange(record.payPassageId, state)}"/>
        </template>
        <template slot="rateSlot" slot-scope="{record}">
          <a-tag color="blue">
            <b>{{ (record.rate * 100).toFixed(2) }}%</b>
          </a-tag>
        </template>
        <template slot="quotaLimitStateSlot" slot-scope="{record}">
           <a-button size="small" v-if="$access('ENT_MCH_APP_EDIT')" type="primary" @click="clickChangeQuota(record)" >设置</a-button>
          &nbsp;<a-badge :status="record.quotaLimitState === 0?'error':'processing'" :text="record.quotaLimitState === 0?'禁用':'启用'" />
          &nbsp;<span :style="{'color': record.quota >0 ? '#4BD884' : '#DB4B4B' ,'text-decoration': record.quotaLimitState === 0?'line-through':''}" >{{ (record.quota / 100).toFixed(2) }}</span>
        </template>
        <template slot="timeLimitStateSlot" slot-scope="{record}">
          <a-button size="small" v-if="$access('ENT_MCH_APP_EDIT')" type="primary" @click="clickChangeTimeLimit(record)" >设置</a-button>
          &nbsp;<a-badge :status="record.timeLimit === 0?'error':'processing'" :text="record.timeLimit === 0?'禁用':'启用'" />
          &nbsp;<span style="color: #1E2229">{{record.timeLimit === 1?record.timeRules : ''}}</span>
        </template>
        <template slot="successRateSlot" slot-scope="{record}">
          &nbsp;<b>{{record.successRate}}%</b>
        </template>
        <template slot="configStrSlot" slot-scope="{record}"> <!-- 通道名插槽 -->
          <a-tooltip placement="bottom" style="font-size: 13px;color: #1E2229" v-if="record.payInterfaceConfig!=undefined && JSON.parse(record.payInterfaceConfig).mchNo.length > 14">
            <template slot="title">
              <span>{{ record.payInterfaceConfig!=undefined?JSON.parse(record.payInterfaceConfig).mchNo:''}}</span>
            </template>
            {{ record.payInterfaceConfig!=undefined?changeStr2ellipsis(JSON.parse(record.payInterfaceConfig).mchNo, 14):'' }}
          </a-tooltip>
          <span style="font-size: 13px;color: #1E2229" v-else>{{ record.payInterfaceConfig!=undefined?JSON.parse(record.payInterfaceConfig).mchNo:'' }}</span>
          <br/>
          <span style="font-size: 13px;color: #1E2229">{{ record.payInterfaceConfig!=undefined && JSON.parse(record.payInterfaceConfig).payType!=undefined?JSON.parse(record.payInterfaceConfig).payType:''}}</span>
        </template> <!-- 自定义插槽 -->
        <template slot="payRuleStrSlot" slot-scope="{record}"> <!-- 收款方式插槽 -->
          <a-tooltip placement="bottom" style="font-size: 13px;color: #1E2229" v-if="record.payRules.length > 14">
            <template slot="title">
              <span>{{ record.payRules}}</span>
            </template>
            {{ changeStr2ellipsis(record.payRules, 14) }}
          </a-tooltip>
          <span style="font-size: 13px;color: #1E2229" v-else>{{ record.payRules }}</span>
        </template> <!-- 自定义插槽 -->
        <template slot="opSlot" slot-scope="{record}">  <!-- 操作列插槽 -->
          <JeepayTableColumns>
            <a-button type="link" @click="detailFunc(record)">查看</a-button>
            <a-button type="link" v-if="$access('ENT_MCH_APP_EDIT')" @click="editFunc(record.payPassageId)">通道修改</a-button>
            <a-button type="link" v-if="$access('ENT_MCH_APP_EDIT')" @click="editPassageMch(record)">通道绑定</a-button>
            <a-button type="link" v-if="$access('ENT_MCH_APP_EDIT')" @click="showPayIfConfigList(record)">支付接口配置</a-button>
            <a-button type="link" v-if="$access('ENT_MCH_APP_EDIT')" @click="copyAndNewPassage(record)">一键复制通道</a-button>
            <a-button type="link" v-if="$access('ENT_MCH_APP_EDIT')" @click="passagePayTest(record)">通道下单测试</a-button>
            <a-button type="link" v-if="$access('ENT_MCH_APP_DEL')" style="color: red" @click="delFunc(record.payPassageId)">删除支付通道</a-button>
          </JeepayTableColumns>
        </template>
      </JeepayTable>
    </a-card>
    <!-- 调整额度弹窗 -->
    <template>
      <a-modal v-model="isShowBalanceModal" title="调整通道余额" @ok="handleBalanceOkFunc">
        <a-form-model ref="infoFormModel" :model="changeBalanceObject" :label-col="{span: 6}" :wrapper-col="{span: 15}" :rules="changeBalanceRules">
          <a-form-model-item label="支付通道名称：" >
            <b style="color: #007CFD">[{{ selectPayPassage.payPassageId }}]</b>&nbsp;<b>{{ selectPayPassage.payPassageName }}</b>
          </a-form-model-item>
          <a-form-model-item label="调整金额：" prop="changeAmount">
            <a-input prefix="￥" type="number" v-model="changeBalanceObject.changeAmount" />
            <b style="color: rgb(128,128,128)">如需扣余额，则输入负数，例如:-10.50</b>
          </a-form-model-item>
          <a-form-model-item label="备注：" prop="changeRemark">
            <a-input v-model="changeBalanceObject.changeRemark" />
          </a-form-model-item>
        </a-form-model>
      </a-modal>
    </template>
    <!-- 调整权重弹窗 -->
    <template>
      <a-modal v-model="isShowWeightsModal" title="调整通道权重" @ok="handleWeightsOkFunc">
        <a-form-model ref="weightsFormModel" :model="selectPayPassage" :label-col="{span: 6}" :wrapper-col="{span: 15}">
          <a-form-model-item label="支付通道名称：" >
            <b style="color: #007CFD">[{{ selectPayPassage.payPassageId }}]</b>&nbsp;<b>{{ selectPayPassage.payPassageName }}</b>
          </a-form-model-item>
          <a-form-model-item label="轮询权重：" prop="changeWeights">
            <a-input type="number" v-model="changeWeights" />
            <b style="color: rgb(128,128,128)">输入1-10的整数</b>
          </a-form-model-item>
        </a-form-model>
      </a-modal>
    </template>
    <!-- 调整授信弹窗 -->
    <template>
      <a-modal v-model="isShowQuotaModal" title="设置通道授信" @ok="handleQuotaOkFunc">
        <a-form-model ref="quotaFormModel" :model="changeQuotaObject" :label-col="{span: 6}" :wrapper-col="{span: 15}">
          <a-form-model-item label="支付通道名称：" >
            <b style="color: #007CFD">[{{ selectPayPassage.payPassageId }}]</b>&nbsp;<b>{{ selectPayPassage.payPassageName }}</b>
          </a-form-model-item>
          <a-form-model-item label="授信限制状态：">
            <a-radio-group v-model="changeQuotaLimitState">
              <a-radio :value="1">
                启用
              </a-radio>
              <a-radio :value="0">
                禁用
              </a-radio>
            </a-radio-group>
          </a-form-model-item>
          <a-form-model-item label="通道授信金额：">
            <a-input prefix="￥" type="number" v-model="changeQuotaNum" />
          </a-form-model-item>
        </a-form-model>
      </a-modal>
    </template>
    <!-- 一键复制通道改名弹窗 -->
    <template>
      <a-modal v-model="isShowCopyAndNewModal" title="通道一键复制" @ok="handleCopyAndNewOkFunc">
        <a-form-model :label-col="{span: 6}" :wrapper-col="{span: 15}">
          <a-form-model-item label="新支付通道名称：">
            <a-input v-model="newPassageName" />
          </a-form-model-item>
        </a-form-model>
      </a-modal>
    </template>
    <!-- 通道定时开启弹窗 -->
    <template>
      <a-modal v-model="isShowTimeLimitModal" title="通道定时开启设置" @ok="handleTimeLimitOkFunc">
        <b style="color: rgb(128,128,128)">设置通道的可用时间，如8:00-23:00，表示通道在这个时间段可用</b><br/><br/>
        <b style="color: rgb(128,128,128)">如:23:00-7:00，表示通道在前一天23点到后一天7点可用，可以跨天</b><br/><br/>
        <a-form-model-item label="通道定时开关：">
          <a-radio-group v-model="changeTimeLimitState">
            <a-radio :value="1">
              启用
            </a-radio>
            <a-radio :value="0">
              禁用
            </a-radio>
          </a-radio-group>
        </a-form-model-item>
        <a-time-picker size="large" :disabled="changeTimeLimitState!='1'" v-model="limitTimeStart" format="HH:mm" placeholder="开启时间" />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
        <a-time-picker size="large" :disabled="changeTimeLimitState!='1'" v-model="limitTimeEnd" format="HH:mm" placeholder="关闭时间" />
      </a-modal>
    </template>
    <!-- 一键清零谷歌验证弹窗 -->
    <template>
      <a-modal v-model="isShowSetZeroModal" title="一键清空通道余额" @ok="handleAllBalanceZeroOkFunc">
        <a-form-model :label-col="{span: 8}" :wrapper-col="{span: 13}">
          <a-form-model-item label="请输入谷歌验证码：">
            <a-input v-model="setZeroGoogleCode" />
          </a-form-model-item>
        </a-form-model>
      </a-modal>
    </template>
    <!-- 一键关闭谷歌验证弹窗 -->
    <template>
      <a-modal v-model="isShowCloseAllModal" title="关闭全部通道" @ok="handleCloseAllOkFunc">
        <a-form-model :label-col="{span: 8}" :wrapper-col="{span: 13}">
          <b style="color: rgb(128,128,128)">此操作将停止所有通道的定时任务!!</b><br/><br/>
          <b style="color: rgb(128,128,128)">可通过<span style="color: #007eff">[打开最近启用通道]</span>恢复到一键关闭前的通道状态</b><br/><br/>
          <b style="color: rgb(128,128,128);font-size: 13px">使用一键关闭前的所有通道状态有效保存时间为三个小时</b><br/>
          <b style="color: rgb(128,128,128);font-size: 13px">过期再使用 [打开最近启用通道] 操作无效请注意！！</b><br/><br/>
          <a-form-model-item label="请输入谷歌验证码：">
            <a-input v-model="setCloseAllGoogleCode" />
          </a-form-model-item>
        </a-form-model>
      </a-modal>
    </template>
    <template>
      <a-modal v-model="isShowOpenRecentlyModal" title="打开最近启用通道" @ok="handleOpenRecentlyOkFunc">
        <a-form-model :label-col="{span: 8}" :wrapper-col="{span: 13}">
          <b style="color: rgb(128,128,128)">此操作会将所有通道状态恢复到<span style="color: #FF4D4F">[关闭全部通道]</span>操作前的状态</b><br/><br/>
          <b style="color: rgb(128,128,128)">包含通道状态、定时任务开启状态</b><br/><br/>
          <b style="color: rgb(128,128,128);font-size: 13px">使用一键关闭前的所有通道状态有效保存时间为三个小时</b><br/>
          <b style="color: rgb(128,128,128);font-size: 13px">过期再使用 [打开最近启用通道] 操作无效请注意！！</b><br/><br/>
        </a-form-model>
      </a-modal>
    </template>
    <template>
      <a-modal v-model="isShowSetAutoCleanModal" title="设置通道自动日切清零" @ok="handleSetAutoCleanOkFunc">
        <a-form-model :label-col="{span: 8}" :wrapper-col="{span: 13}">
          <b style="color: rgb(128,128,128)">开启后每天北京时间 00:00 自动清空所有通道余额</b><br/><br/>
          <a-form-model-item label="通道自动日切开关：">
            <a-radio-group v-model="autoCleanEnable">
              <a-radio :value="1">
                启用
              </a-radio>
              <a-radio :value="0">
                禁用
              </a-radio>
            </a-radio-group>
          </a-form-model-item>
          <a-form-model-item label="请输入谷歌验证码：">
            <a-input v-model="setAutoCleanGoogleCode" />
          </a-form-model-item>
        </a-form-model>
      </a-modal>
    </template>
    <!-- 新增应用  -->
    <MchAppAddOrEdit ref="mchAppAddOrEdit" :callbackFunc="addOrEdit"/>
    <!-- 配置详情页面  -->
    <PayPassageDetail ref="payPassageDetail" />
    <!-- 支付参数配置JSON渲染页面组件  -->
    <MchPayConfigAddOrEdit ref="mchPayConfigAddOrEdit" :callbackFunc="addOrEdit" />
    <PassageMchBlindEdit ref="passageMchBlindEdit" :callbackFunc="addOrEdit" />
    <PassagePayTest ref="passagePayTest"></PassagePayTest>
  </page-header-wrapper>
</template>

<script>
import JeepayTable from '@/components/JeepayTable/JeepayTable'
import JeepayTextUp from '@/components/JeepayTextUp/JeepayTextUp' // 文字上移组件
import JeepayTableColumns from '@/components/JeepayTable/JeepayTableColumns'
import {
  API_URL_MCH_APP,
  API_URL_MCH_APP_BALANCE, API_URL_MCH_APP_MULTIPLE_SET,
  API_URL_MCH_APP_RESET_BALANCE, API_URL_PASSAGE_STAT_LIST,
  API_URL_PAYWAYS_LIST,
  req, reqLoad
} from '@/api/manage'
import MchAppAddOrEdit from './AddOrEdit'
import MchPayConfigAddOrEdit from './MchPayConfigAddOrEdit'
import PayPassageDetail from './PayPassageDetail'
import PassageMchBlindEdit from '@/views/mchApp/PassageMchBlindEdit.vue'
import moment from 'moment'
import PassagePayTest from '@/views/mchApp/PassagePayTest.vue'
import JeepayTableColState from '@/components/JeepayTable/JeepayTableColState.vue'

// eslint-disable-next-line no-unused-vars
const tableColumns = [
  { key: 'payPassageId', fixed: 'left', width: '240px', title: '通道名称/所属产品', scopedSlots: { customRender: 'payPassageId' } },
  { key: 'state', title: '状态', fixed: 'left', width: 100, scopedSlots: { customRender: 'stateSlot' } },
  { key: 'balance', title: '通道余额', width: 180, scopedSlots: { customRender: 'balanceSlot' } },
  { key: 'weightsSlot', title: '轮询权重', width: 100, scopedSlots: { customRender: 'weightsSlot' } },
  { key: 'quotaLimitState', width: 210, title: '通道授信限制', scopedSlots: { customRender: 'quotaLimitStateSlot' } },
  { key: 'timeLimitState', width: 210, title: '通道定时设置', scopedSlots: { customRender: 'timeLimitStateSlot' } },
  { key: 'rate', title: '通道费率', width: 100, scopedSlots: { customRender: 'rateSlot' } },
  { key: 'successRate', title: '成功率(天)', width: 100, scopedSlots: { customRender: 'successRateSlot' } },
  { key: 'payRuleStr', width: 120, title: '收款规则', scopedSlots: { customRender: 'payRuleStrSlot' } },
  { key: 'configStr', width: 160, title: '三方用户/通道标识', scopedSlots: { customRender: 'configStrSlot' } },
  { key: 'op', title: '操作', width: '280px', fixed: 'right', align: 'center', scopedSlots: { customRender: 'opSlot' } }
]

export default {
  name: 'MchAppPage',
  components: { JeepayTableColState, PassagePayTest, JeepayTable, JeepayTableColumns, JeepayTextUp, MchAppAddOrEdit, MchPayConfigAddOrEdit, PayPassageDetail, PassageMchBlindEdit },
  data () {
    return {
      btnLoading: false,
      tableColumns: tableColumns,
      searchData: {},
      productList: {},
      isShowBalanceModal: false, // 调额设置弹窗
      isShowWeightsModal: false, // 权重设置弹窗
      isShowQuotaModal: false, // 额度设置弹窗
      isShowTimeLimitModal: false, // 定时设置弹窗
      isShowCopyAndNewModal: false, // 一键复制通道改名提示的弹窗
      isShowSetZeroModal: false, // 一键清零
      isShowSetAutoCleanModal: false, // 自动日切
      isShowCloseAllModal: false, // 关闭全部
      isShowOpenRecentlyModal: false, // 最近启用
      newPassageName: '', // 新通道名称
      copiedPassage: {}, // 复制的通道obj
      selectPayPassage: {}, // 当前选择通道
      changeObject: {},
      changeBalanceObject: {},
      changeWeights: 0,
      changeQuotaObject: {},
      changeQuotaNum: 0,
      setZeroGoogleCode: '',
      setCloseAllGoogleCode: '',
      setAutoCleanGoogleCode: '',
      changeQuotaLimitState: false,
      changeTimeLimitState: false,
      limitTimeStart: '',
      limitTimeEnd: '',
      autoCleanEnable: 0, // 零点自动清零是否打开
      totalPassageInfo: {
        passageNum: 0,
        totalBalance: 0
      },
      changeBalanceRules: {
        changeRemark: [
          { required: true, message: '请输入调额备注', trigger: 'blur' }
        ],
        changeAmount: [
          { required: true, message: '请输入调整金额', trigger: 'blur' }
        ]
      },
      stateLoading: false
    }
  },
  mounted () {
    this.queryFunc()
    const that = this
    req.list(API_URL_PAYWAYS_LIST, { 'pageSize': -1 }).then(res => { // 产品下拉选择列表
      that.productList = res.records
    })
    this.getPassageStatInfo()
  },
  methods: {
    queryFunc () {
      const that = this
      this.btnLoading = true
      this.$refs.infoTable.refTable(true)
      req.postDataNormal('/api/passageRealTimeStat', '', that.searchData).then(res => { // 产品下拉列表
        that.totalPassageInfo = res
      })
    },
    // 请求table接口数据
    reqTableDataFunc: (params) => {
      return req.list(API_URL_MCH_APP, params)
    },
    searchFunc: function () { // 点击【查询】按钮点击事件
      this.$refs.infoTable.refTable(true)
    },
    addOrEdit: function () { // 点击【查询】按钮点击事件
      this.$refs.infoTable.refTable()
    },
    addFunc: function () { // 业务通用【新增】 函数
      this.$refs.mchAppAddOrEdit.show()
    },
    editFunc: function (recordId) { // 业务通用【修改】 函数
      this.$refs.mchAppAddOrEdit.show(recordId)
    },
    editPassageMch: function (record) { // 业务通用【修改】 函数
      this.$refs.passageMchBlindEdit.show(record)
    },
    detailFunc: function (record) { // 业务通用【修改】 函数
      this.$refs.payPassageDetail.show(record)
    },
    delFunc (appId) {
      const that = this
      this.$infoBox.confirmDanger('确认删除？', '', () => {
        req.delById(API_URL_MCH_APP, appId).then(res => {
          that.$message.success('删除成功！')
          that.searchFunc()
        })
      })
    },
    showPayIfConfigList: function (record) { // 支付参数配置
      this.$refs.mchPayConfigAddOrEdit.show(record)
    },
    copyAndNewPassage: function (record) { // 一键复制通道
      // this.$refs.mchPayConfigAddOrEdit.show(record)
      // 弹窗设置名字
      this.newPassageName = ''
      this.copiedPassage = record
      this.isShowCopyAndNewModal = true
    },
    passagePayTest: function (record) { // 通道下单测试
      let productName = ''
      for (let i = 0; i < this.productList.length; i++) {
        if (record.productId === this.productList[i].productId) {
          productName = this.productList[i].productName
          break
        }
      }
      this.$refs.passagePayTest.show(record, productName)
    },
    clickChangeBalance: function (record) { // 点击调账
      this.isShowBalanceModal = true
      this.selectPayPassage = record
      if (this.$refs.infoFormModel !== undefined) {
        this.$refs.infoFormModel.resetFields()
      }
      this.changeBalanceObject = {}
    },
    clickChangeWeights: function (record) { // 点击修改权重
      this.isShowWeightsModal = true
      this.selectPayPassage = record
      if (this.$refs.weightsFormModel !== undefined) {
        this.$refs.weightsFormModel.resetFields()
      }
      this.changeWeights = record.weights
    },
    clickChangeQuota: function (record) { // 点击额度授信
      this.isShowQuotaModal = true
      this.selectPayPassage = record
      if (this.$refs.quotaFormModel !== undefined) {
        this.$refs.quotaFormModel.resetFields()
      }
      this.changeQuotaNum = (record.quota / 100).toFixed(2)
      this.changeQuotaLimitState = record.quotaLimitState
    },
    clickChangeTimeLimit: function (record) { // 设置通道定时
      this.isShowTimeLimitModal = true
      this.limitTimeStart = null
      this.limitTimeEnd = null
      this.selectPayPassage = record
      this.changeTimeLimitState = record.timeLimit
      if (record.timeRules !== '' && record.timeRules !== '|') {
        const timeRulesStr = record.timeRules.split('|')
        const format = 'HH:mm'
        this.limitTimeStart = moment(timeRulesStr[0], format)
        this.limitTimeEnd = moment(timeRulesStr[1], format)
      }
    },
    handleBalanceOkFunc: function () {
      if (this.changeBalanceObject.changeAmount === '' || this.changeBalanceObject.changeAmount === undefined) {
        this.$message.error('金额不能为空')
        return
      }
      if (this.changeBalanceObject.changeRemark === '' || this.changeBalanceObject.changeRemark === undefined) {
        this.$message.error('备注不能为空')
        return
      }
      req.updateById(API_URL_MCH_APP_BALANCE, this.selectPayPassage.payPassageId, this.changeBalanceObject).then(res => {
        this.$refs.infoTable.refTable()
        this.$message.success('修改成功')
      })
      this.isShowBalanceModal = false
    },
    handleWeightsOkFunc: function () {
      if (!(typeof this.changeWeights === 'number' && !isNaN(this.changeWeights))) {
        if (this.changeWeights < 1 || this.changeWeights > 10) {
          this.$message.error('请输入1-10的整数')
          return
        }
      }
      this.changeObject = this.selectPayPassage
      this.changeObject.weights = this.changeWeights
      req.updateById(API_URL_MCH_APP, this.selectPayPassage.payPassageId, this.changeObject).then(res => {
        this.$refs.infoTable.refTable()
        this.$message.success('修改成功')
      })
      this.isShowWeightsModal = false
    },
    handleQuotaOkFunc: function () {
      this.changeObject = this.selectPayPassage
      this.changeObject.quotaLimitState = this.changeQuotaLimitState
      this.changeObject.quota = this.changeQuotaNum * 100
      this.isShowQuotaModal = false
      req.updateById(API_URL_MCH_APP, this.selectPayPassage.payPassageId, this.changeObject).then(res => {
        this.$refs.infoTable.refTable()
        this.$message.success('修改成功')
      })
    },
    handleCopyAndNewOkFunc: function () { // 复制并新建通道
      if (this.newPassageName.replaceAll(' ', '') === '') {
        this.$message.error('请输入通道名称')
        return
      }
      const that = this
      var copiedObj = JSON.parse(JSON.stringify(that.copiedPassage))
      copiedObj.payPassageId = null
      copiedObj.state = 1 // 通道默认打开
      copiedObj.balance = 0
      copiedObj.payPassageName = that.newPassageName.replaceAll(' ', '')
      this.isShowCopyAndNewModal = false
      req.add(API_URL_MCH_APP, copiedObj).then(res => {
        that.$message.success('新增成功')
        this.$refs.infoTable.refTable()
      })
    },
    handleTimeLimitOkFunc: function () { // 复制并新建通道  此处是moment对象
      this.changeObject = this.selectPayPassage
      let start = ''
      let end = ''
      if (this.changeTimeLimitState) {
        if (this.limitTimeStart == null || this.limitTimeEnd == null) {
          this.$message.error('请选择开启、关闭时间')
          return
        }
      }
      if (this.limitTimeStart !== null) {
        start = this.limitTimeStart.format('HH:mm')
      }
      if (this.limitTimeStart !== null) {
        end = this.limitTimeEnd.format('HH:mm')
      }
      if (start === end) {
        this.$message.error('开启、关闭时间不能相同')
        return
      }
      this.changeObject.timeRules = start + '|' + end
      this.changeObject.timeLimit = this.changeTimeLimitState
      this.isShowTimeLimitModal = false
      req.updateById(API_URL_MCH_APP, this.selectPayPassage.payPassageId, this.changeObject).then(res => {
        this.$refs.infoTable.refTable()
        this.$message.success('修改成功')
      })
    },
    setAllBalanceZero: function () { // 一键清零
      this.isShowSetZeroModal = true
      this.setZeroGoogleCode = ''
    },
    setCloseAll: function () { // 一键全关
      this.isShowCloseAllModal = true
      this.setCloseAllGoogleCode = ''
    },
    setOpenRecently: function () { // 打开最近启用
      this.isShowOpenRecentlyModal = true
    },
    setAutoClean: function () { // 自动清零
      this.isShowSetAutoCleanModal = true
      this.setAutoCleanGoogleCode = ''
    },
    handleSetAutoCleanOkFunc: function () { // 开关自动日切
      const param = {}
      // autoCleanEnable setAutoCleanGoogleCode
      param.googleCode = this.setAutoCleanGoogleCode
      param.autoCleanEnable = this.autoCleanEnable
      if (this.setAutoCleanGoogleCode === undefined || this.setAutoCleanGoogleCode === '') {
        this.$message.error('请输入谷歌验证码')
        return
      }
      this.isShowSetAutoCleanModal = false
      req.postDataNormal('/api/passageStatInfo', 'setPassageAutoClean/', param).then(res => {
        this.getPassageStatInfo()
        this.$message.success('设置成功')
      })
    },
    handleAllBalanceZeroOkFunc: function () {
      const param = {}
      param.googleCode = this.setZeroGoogleCode
      if (this.setZeroGoogleCode === undefined || this.setZeroGoogleCode === '') {
        this.$message.error('请输入谷歌验证码')
        return
      }
      this.isShowSetZeroModal = false
      req.postDataNormal(API_URL_MCH_APP_RESET_BALANCE, 'resetAll/', param).then(res => {
        this.$refs.infoTable.refTable()
        this.$message.success('通道清空成功')
      })
    },
    handleCloseAllOkFunc: function () {
      const param = {}
      param.googleCode = this.setCloseAllGoogleCode
      if (this.setCloseAllGoogleCode === undefined || this.setCloseAllGoogleCode === '') {
        this.$message.error('请输入谷歌验证码')
        return
      }
      this.isShowCloseAllModal = false
      req.postDataNormal(API_URL_MCH_APP_MULTIPLE_SET, 'closeAll/', param).then(res => {
        this.$refs.infoTable.refTable()
        this.$message.success('关闭全部通道成功')
      })
    },
    handleOpenRecentlyOkFunc: function () {
      this.isShowOpenRecentlyModal = false
      req.postDataNormal(API_URL_MCH_APP_MULTIPLE_SET, 'openRecently/', '').then(res => {
        this.$refs.infoTable.refTable()
        this.$message.success('恢复最近关闭通道状态成功')
      })
    },
    getPassageStatInfo: function () {
      const that = this
      req.getNormal(API_URL_PASSAGE_STAT_LIST, 'statPassageInfo').then(res => {
        that.totalPassageInfo = res
        that.autoCleanEnable = res.payPassageAutoClean
      })
    },
    changeStr2ellipsis (orderNo, baseLength) {
      const halfLengh = parseInt(baseLength / 2)
      return orderNo.substring(0, halfLengh - 1) + '...' + orderNo.substring(orderNo.length - halfLengh, orderNo.length)
    },
    onSwitchChange (recordId, state) {
      const that = this
      const title = state === 1 ? '确认[启用]该通道？' : '确认[停用]该通道？'
      const content = state === 1 ? '启用后请检查额度限制、通道绑定、定时任务等,确保通道正常拉起' : '请注意手动停用将立即关闭通道定时任务！'
      const param = {
        state: state
      }
      if (state === 0) {
        param.timeLimit = 0
      }
      return new Promise((resolve, reject) => {
        that.$infoBox.confirmDanger(title, content, () => {
              return reqLoad.updateById(API_URL_MCH_APP, recordId, param).then(res => {
                that.searchFunc()
                resolve()
              }).catch(err => reject(err))
            },
            () => {
              reject(new Error())
            })
      })
    }
  }
}
</script>

<style lang="less" scoped>
.stat-col{
  position: relative;
  //background-color: rgb(128,128,128);
  height: 100px;
  border-radius: 13px;
}

.stat-col b{
  position: absolute;
  font-size: 30px;
  top: 10px;
  left: 20px;
}
.stat-content{
  position: absolute;
  //font-size: 30px;
  top: 20px;
  left: 20px;
  font-weight: bold;
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
