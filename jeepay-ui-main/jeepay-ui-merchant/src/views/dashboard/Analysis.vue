<template>
  <div id="chart-card">
    <div class="chart-top">
      <div class="chart-item top-left">
        <div class="chart-data" style="position:relative">
          <a-skeleton active :loading="true" v-if="skeletonIsShow" style="padding:20px" :paragraph="{ rows: 6 }" />
          <div v-show="!skeletonIsShow">
            <div class="analy-title" style="padding:20px;box-sizing:border-box;padding-bottom:10px">
              <span>今日成交金额</span>
            </div>
            <div>
              <div class="pay-amount-text">
                <b class="pay-amount">&nbsp;{{ (mainChart.todayCount.totalSuccessAmount/100).toFixed(2) }}</b>
              </div>
            </div>
            <div class="payAmountSpan">
              <span>昨日成交金额：&nbsp;{{ (mainChart.yesterdayCount.totalSuccessAmount/100).toFixed(2) }}</span>
            </div>
          </div>
        </div>
      </div>
      <div class="chart-item top-left" style="width: 250px">
        <div class="chart-data" style="position:relative">
          <!-- 骨架屏与图表有冲突，故不使用内嵌方式。 因为内边距的原因，采取v-if的方式 -->
          <a-skeleton active :loading="true" v-if="skeletonIsShow" style="padding:20px" :paragraph="{ rows: 6 }" />
          <div v-show="!skeletonIsShow">
            <div class="analy-title" style="padding:20px;box-sizing:border-box;padding-bottom:10px">
              <span>今日成交订单数</span>
            </div>
            <div>
              <div class="pay-amount-text">
                <b class="pay-amount">{{ mainChart.todayCount.orderSuccessCount }}</b>
              </div>
            </div>
            <div class="payAmountSpan">
              <span>昨日成交订单数：{{ mainChart.yesterdayCount.orderSuccessCount }}</span>
            </div>
          </div>
        </div>
      </div>
      <div class="chart-item top-left" style="width: 250px">
        <div class="chart-data" style="position:relative">
          <!-- 骨架屏与图表有冲突，故不使用内嵌方式。 因为内边距的原因，采取v-if的方式 -->
          <a-skeleton active :loading="true" v-if="skeletonIsShow" style="padding:20px" :paragraph="{ rows: 6 }" />
          <div v-show="!skeletonIsShow">
            <div class="analy-title" style="padding:20px;box-sizing:border-box;padding-bottom:10px">
              <span>今日服务费</span>
            </div>
            <div>
              <div class="pay-amount-text">
                <span class="pay-amount">{{ (mainChart.todayCount.totalMchCost /100).toFixed(2) }}</span>
              </div>
            </div>
            <div class="payAmountSpan">
              <span>昨日服务费：{{ (mainChart.yesterdayCount.totalMchCost / 100).toFixed(2) }}</span>
            </div>
          </div>
        </div>
      </div>
      <div class="top-middle">
        <div class="middle-top">
          <div class="chart-item middle-larger">
            <div class="chart-data">
              <a-skeleton active :loading="skeletonIsShow" :paragraph="{ rows: 1 }">
                <div class="analy-title">
                  <span>今日成功率</span>
                </div>
                <a-card :bordered="false">
                  <a-statistic style="margin-top: 10px;font-weight: bold" :value="(mainChart.todaySuccessRate*100).toFixed(2)+ '%'" />
                </a-card>
              </a-skeleton>
            </div>
          </div>
          <div class="chart-item middle-larger">
            <div class="chart-data">
              <a-skeleton active :loading="skeletonIsShow" :paragraph="{ rows: 1 }">
                <div class="analy-title">
                  <span>今日订单量</span>
                </div>
                <a-card :bordered="false">
                  <a-statistic style="margin-top: 10px" :value="mainChart.todayCount.totalOrderCount" />
                </a-card>
              </a-skeleton>
            </div>
          </div>
        </div>
        <div class="middle-bottom">
          <div class="chart-item middle-larger">
            <div class="chart-data">
              <a-skeleton active :loading="skeletonIsShow" :paragraph="{ rows: 1 }">
                <div class="analy-title">
                  <span>昨日成功率</span>
                </div>
                <a-card :bordered="false">
                  <a-statistic style="margin-top: 10px" :value="(mainChart.yesterdaySuccessRate * 100).toFixed(2)+ '%'" />
                </a-card>
              </a-skeleton>
            </div>
          </div>
          <div class="chart-item middle-larger">
            <div class="chart-data">
              <a-skeleton active :loading="skeletonIsShow" :paragraph="{ rows: 1 }">
                <div class="analy-title">
                  <span>昨日订单量</span>
                </div>
                <a-card :bordered="false">
                  <a-statistic style="margin-top: 10px" :value="mainChart.yesterdayCount.totalOrderCount" />
                </a-card>
              </a-skeleton>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="chart-bottom">
      <div class="chart-item bottom-left">
        <div class="chart-data">
          <a-skeleton active :loading="skeletonIsShow" :paragraph="{ rows: 6 }"/>
          <div v-show="!skeletonIsShow">
            <div>
              <div class="pay-count-title">
                <span class="chart-title">支付产品统计</span>
              </div>
              <a-radio-group v-model="tableDate" button-style="solid" @change="selectTab">
                <a-radio-button value="1">今日</a-radio-button>
                <a-radio-button value="2">昨日</a-radio-button>
              </a-radio-group>
              <div style="margin-top: 12px;">
                <JeepayTable
                    @btnLoadClose="btnLoading=false"
                    ref="infoTable"
                    :initData="false"
                    :reqTableDataFunc="reqTableDataFunc"
                    :tableColumns="tableColumns"
                    :pageSize = 10
                    :searchData="searchData"
                    rowKey="productId"
                >
                  <template slot="nameSlot" slot-scope="{record}">
                    <b>[{{ record.productId }}]</b>&nbsp;<span>{{ record.productName }}</span>
                  </template> <!-- 自定义插槽 -->
                  <template slot="stateSlot" slot-scope="{record}">
                    <b>{{ (record.orderSuccessCount/record.totalOrderCount*100).toFixed(2) }}%</b>
                  </template>
                  <!--    全局金额颜色参考此处    -->
                  <template slot="balanceSlot" slot-scope="{record}">
                    &nbsp;&nbsp;<b style="color:#4BD884" >{{ (record.totalSuccessAmount/100).toFixed(2) }}</b>
                  </template> <!-- 自定义插槽 -->
                  <template slot="feeSlot" slot-scope="{record}">
                    <span>{{ (record.totalCost/100).toFixed(2) }}</span>
                  </template>
                </JeepayTable>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="chart-item bottom-right">
        <div class="chart-data">
          <div v-show="!skeletonIsShow">
            <div class="pay-count-title">
              <span class="chart-title" style="font-weight: bold">商户信息</span>
            </div>
            <template>
              <div id="chartContainer" style="max-width:500px;width: 500px;min-width: 500px; height:500px;">
                <a-row justify="space-between" type="flex">
                  <a-col :sm="12">
                    <a-descriptions>
                      <a-descriptions-item label="商户号">
                        <b>{{ detailData.mchInfo.mchNo }}</b>
                      </a-descriptions-item>
                    </a-descriptions>
                  </a-col>
                  <a-col :sm="12">
                    <a-descriptions>
                      <a-descriptions-item label="商户名称">
                        {{ detailData.mchInfo.mchName }}
                      </a-descriptions-item>
                    </a-descriptions>
                  </a-col>
                  <a-col :sm="24">
                    <a-descriptions>
                      <a-descriptions-item label="登录名">
                       <b> {{ detailData.mchInfo.loginUsername }}</b>
                      </a-descriptions-item>
                    </a-descriptions>
                  </a-col>
                  <a-col :sm="24">
                    <a-descriptions>
                      <a-descriptions-item label="商户密钥">
<!--                        <span style="color:#4BD884" >{{ (detailData.mchInfo.secret) }}</span>-->
                        <a-button icon="eye" size="small" @click="handleView" type="primary" style="margin-left:10px;" >点击查看</a-button>
                      </a-descriptions-item>
                    </a-descriptions>
                  </a-col>
                  <a-col :sm="12">
                    <a-descriptions>
                      <a-descriptions-item label="当前余额">
                        <b style="color:#4BD884" >{{ (detailData.mchInfo.balance/100).toFixed(2) }}</b>
                      </a-descriptions-item>
                    </a-descriptions>
                  </a-col>
                  <a-col :sm="12">
                    <a-descriptions>
                      <a-descriptions-item label="商户状态">
                        <a-badge :status="detailData.mchInfo.state === 0?'error':'processing'" :text="detailData.mchInfo.state === 0?'禁用':'启用'" />
                      </a-descriptions-item>
                    </a-descriptions>
                  </a-col>
                  <a-col :sm="24">
                    <a-descriptions>
                      <a-descriptions-item label="商户创建时间">
                        {{ detailData.mchInfo.createdAt }}
                      </a-descriptions-item>
                    </a-descriptions>
                  </a-col>
                  <a-divider></a-divider>
                </a-row>
              </div>
            </template>
            <div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <!-- 调整权重弹窗 -->
    <template>
      <a-modal v-model="isShowSecretModal" title="商户密钥" @ok="handleSecretOkFunc">
        <b>{{ (detailData.mchInfo.secret) }}</b>
      </a-modal>
    </template>
  </div>
</template>

<script>

import moment from 'moment'
import {
  API_URL_MAIN_STATISTIC, getMchMainInfo,
  getTwoDayCount, req
} from '@/api/manage'
import { timeFix } from '@/utils/util'
import empty from './empty'
import JeepayTableColumns from '@/components/JeepayTable/JeepayTableColumns.vue'
import JeepayTable from '@/components/JeepayTable/JeepayTable.vue' // 空数据展示的组件，首页自用
const tableColumns = [
  { key: 'name', fixed: 'left', width: '200px', title: '产品ID/名称', scopedSlots: { customRender: 'nameSlot' } },
  { key: 'balance', title: '成交额(￥)', width: 100, scopedSlots: { customRender: 'balanceSlot' }, fixed: 'left' },
  { key: 'totalCount', title: '总单量', width: '100px', dataIndex: 'totalOrderCount' },
  { key: 'successCount', title: '成交单量', width: '100px', dataIndex: 'orderSuccessCount' },
  { key: 'state', title: '成功率', width: '100px', scopedSlots: { customRender: 'stateSlot' } },
  { key: 'fee', title: '服务费(￥)', width: '100px', scopedSlots: { customRender: 'feeSlot' } }
]
export default {
  data () {
    return {
      btnLoading: false,
      skeletonIsShow: true, // 骨架屏是否显示
      skeletonReqNum: 0, // 当所有数据请求完毕后关闭骨架屏（共四个请求）
      visible: false,
      searchData: {
        'date': moment().format('YYYY-MM-DD')
      }, // 查询条件
      isPayType: true, // 产品是否存在数据
      mainTips: { // 主页提示
        helloTitle: ''
      },
      mainChart: { // 主页统计数据
        todayCount: {},
        yesterdayCount: {}
      },
      tableDate: '1', // tab选择
      queryDate: '',
      tableColumns: tableColumns,
      chartPassage: null,
      productMchData: [],
      detailData: { // 商户详细信息
        'mchInfo': {
          'mchNo': '',
          'mchName': '',
          'balance': 0,
          'createdAt': '',
          'secret': '',
          'state': 0,
          'loginUsername': ''
        }
      },
      isShowSecretModal: false
    }
  },
  components: { JeepayTable, JeepayTableColumns, empty },
  methods: {
    init () {
      const that = this
      getTwoDayCount().then(res => {
        that.mainChart = res
        that.skeletonClose(that)
      }).catch((err) => {
        console.error(err)
        that.skeletonClose(that)
      })
      getMchMainInfo().then(res => {
        that.detailData = res
      }).catch((err) => {
        console.error(err)
      })
    },
    reqTableDataFunc: (params) => {
      return req.list(API_URL_MAIN_STATISTIC, params)
    },
    moment,
    skeletonClose (that) {
      // 每次请求成功，skeletonReqNum + 1,当大于等于4时， 取消骨架屏展示
      that.skeletonIsShow = false
    },
    selectTab () {
      // rowKey
      if (this.tableDate === '1') {
        this.searchData.date = moment().format('YYYY-MM-DD')
      } else {
        this.searchData.date = moment().subtract(1, 'days').format('YYYY-MM-DD')
      }
      this.$refs.infoTable.refTable(true)
    },
    handleView () {
      this.isShowSecretModal = true
    },
    handleSecretOkFunc () {
      this.isShowSecretModal = false
    }
  },
  computed: {
  },
  mounted () {
    if (this.tableDate === '1') {
      this.queryDate = moment().format('YYYY-MM-DD')
    } else {
      this.queryDate = moment().subtract(1, 'days').format('YYYY-MM-DD')
    }
    this.init()
    // 用户名信息以及时间问候语句。由于退出登陆才让他更改成功，所以这里的数据先从 vuex中获取
    this.mainTips.helloTitle = `${timeFix()}，` + this.$store.state.user.loginUsername
    this.$refs.infoTable.refTable(true)
  }
}
</script>

<style lang="less" scoped>
@import './index.less'; // 响应式布局
.user-greet {
  font-size: 19px;
  font-weight: 500;

  .quick-start {
    box-sizing: border-box;
    padding-top: 20px;

    .quick-start-title {
      font-size: 16px;
      font-weight: 500;
      text-align: left;
      margin-bottom:0;
    }
    .quick-start-ul {
      font-size: 13px;
      display: flex;
      flex-direction: row;
      flex-wrap: wrap;
      width: 100%;
      padding: 0;
      margin-bottom:0;

      li {
        margin-right: 20px;
        margin-top: 10px;
        text-align: left;

        :hover {
          color: @jee-inside-link
        }
      }
      li:hover {
        cursor:pointer;
      }
    }
  }
}
.chart-padding {
  box-sizing: border-box;
  padding: 0 5px;
  width:300px;
}

.user-greet-title{
  box-sizing:border-box;
  padding-bottom:20px;
  display:flex;
  justify-content:space-between;
  border-bottom:1px solid #ddd;

  .user-greet-all {
    display:flex;
    flex-direction:row;

    .user-greet-img {
      width:60px;
      height:60px;
      border-radius:50%;
      overflow:hidden;
      background:#ddd;
      margin-right:10px;
      img {
        width:60px;
        height:60px;
        border:1px solid rgba(0,0,0,0.08)
      }
    }
    .user-greet-all {
      display:flex;
      flex-direction:column;
      justify-content: space-around;
    }
  }
}
.analy-title {
  display:flex;
  justify-content:space-between;
  padding-bottom:0;
  align-items: center;
}
.there-spot:hover {
  cursor:pointer;
}
.ant-calendar-picker-input {
  border:none !important
}
.payAmountSpan {
  display:flex;
  justify-content:space-between;
  width: 100%;
  box-sizing: border-box;
  position: absolute;
  bottom:20px;
  padding:0 20px;
  box-sizing: border-box;
}

.chart-data {
  padding:20px;
  box-sizing: border-box;
}

.top-left {

  .chart-data {
    padding:0;
  }
}

.pay-amount-text {
  display: flex;
  padding: 0 20px 0 16px;
  box-sizing: border-box;
  height: 33px;
  line-height: 33px;
  align-items: baseline;
  margin-bottom: 10px;
  .pay-amount {
    font-size: 33px;
    margin-right: 10px;
  }
}

.pay-count-title {
  display:flex;
  flex-wrap: wrap;
  justify-content:space-between;
  align-items:center;
  .pay-count-date{
    display:flex;
    justify-content:space-around;
  }
}
.chart-padding {
  box-sizing: border-box;
  max-width:330px;
  min-width:260px;
  flex-grow: 1;
  flex-shrink:1;
  margin-bottom: 20px;
}
.change-date-layout {
  padding-left: 11px;
  align-items: center;
  display:flex;
  justify-content:space-between;

  .change-date-icon {
    width:50px;
    height:36px;
    display:flex;
    align-items:center;
    justify-content:center;
  }
}

.chart-title {
  font-size: 16px;
  font-weight: 500;
  margin-right:20px;
  margin-bottom:20px;
}
</style>
