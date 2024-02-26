<template>
  <div id="chart-card">
    <div class="chart-top">
<!--      <div class="chart-item top-left">-->
<!--        <div class="chart-data" style="position:relative">-->
<!--          &lt;!&ndash; 骨架屏与图表有冲突，故不使用内嵌方式。 因为内边距的原因，采取v-if的方式 &ndash;&gt;-->
<!--          <a-skeleton active :loading="true" v-if="skeletonIsShow" style="padding:20px" :paragraph="{ rows: 6 }" />-->
<!--          <div v-show="!skeletonIsShow">-->
<!--            <div class="analy-title" style="padding:20px;box-sizing:border-box;padding-bottom:10px">-->
<!--              <span>今日总金额</span>-->
<!--            </div>-->
<!--            <div>-->
<!--              <div class="pay-amount-text">-->
<!--                <span class="pay-amount">{{ (mainChart.todayCount.totalAmount/100).toFixed(2) }}</span>-->
<!--              </div>-->
<!--            </div>-->
<!--            <div class="payAmountSpan">-->
<!--              <span>昨日总金额&nbsp;{{ (mainChart.yesterdayCount.totalAmount/100).toFixed(2)}}</span>-->
<!--            </div>-->
<!--          </div>-->
<!--        </div>-->
<!--      </div>-->
      <div class="chart-item top-left">
        <div class="chart-data chengjiaojine" style="position:relative;">
          <a-skeleton active :loading="true" v-if="skeletonIsShow" style="padding:20px" :paragraph="{ rows: 6 }" />
          <div v-show="!skeletonIsShow">
            <div>
              <div class="pay-amount-text">
                <b class="pay-amount">&nbsp;{{ (mainChart.todayCount.totalSuccessAmount/100).toFixed(2) }}</b>
              </div>
              <div class="analy-title" style="padding:20px;box-sizing:border-box;padding-bottom:10px">
                <span>今日成交金额</span>
              </div>
            </div>
            <div class="chart-icon-div">
            </div>
            <div class="payAmountSpan">
              <span class="title">昨日：</span><span>{{ (mainChart.yesterdayCount.totalSuccessAmount/100).toFixed(2) }}</span>
            </div>
          </div>
        </div>
      </div>
      <div class="chart-item top-left">
        <div class="chart-data chengjiaodingdan" style="position:relative;">
          <a-skeleton active :loading="true" v-if="skeletonIsShow" style="padding:20px" :paragraph="{ rows: 6 }" />
          <div v-show="!skeletonIsShow">
            <div>
              <div class="pay-amount-text">
                <b class="pay-amount">&nbsp;{{ mainChart.todayCount.orderSuccessCount }}</b>
              </div>
              <div class="analy-title" style="padding:20px;box-sizing:border-box;padding-bottom:10px">
                <span>今日成交订单数</span>
              </div>
            </div>
            <div class="chart-icon-div-order-success-num">
            </div>
            <div class="payAmountSpan">
              <span class="title">昨日：</span><span>{{ mainChart.yesterdayCount.orderSuccessCount }}</span>
            </div>
          </div>
        </div>
      </div>
      <div class="chart-item top-left">
        <div class="chart-data today-income" style="position:relative;">
          <a-skeleton active :loading="true" v-if="skeletonIsShow" style="padding:20px" :paragraph="{ rows: 6 }" />
          <div v-show="!skeletonIsShow">
            <div>
              <div class="pay-amount-text">
                <b class="pay-amount">&nbsp;{{ (mainChart.todayCount.platTotalIncome/100).toFixed(2) }}</b>
              </div>
              <div class="analy-title" style="padding:20px;box-sizing:border-box;padding-bottom:10px">
                <span>今日平台利润</span>
              </div>
            </div>
            <div class="chart-icon-div-today-income">
            </div>
            <div class="payAmountSpan">
              <span class="title">昨日：</span><span>{{ (mainChart.yesterdayCount.platTotalIncome/100).toFixed(2) }}</span>
            </div>
          </div>
        </div>
      </div>
      <div class="chart-item top-left">
        <div class="chart-data order-num" style="position:relative;">
          <a-skeleton active :loading="true" v-if="skeletonIsShow" style="padding:20px" :paragraph="{ rows: 6 }" />
          <div v-show="!skeletonIsShow">
            <div>
              <div class="pay-amount-text">
                <b class="pay-amount">&nbsp;{{ mainChart.todayCount.totalOrderCount }}</b>
              </div>
              <div class="analy-title" style="padding:20px;box-sizing:border-box;padding-bottom:10px">
                <span>今日订单数</span>
              </div>
            </div>
            <div class="chart-icon-div-order-num">
            </div>
            <div class="payAmountSpan">
              <span class="title">昨日：</span><span>{{ mainChart.yesterdayCount.totalOrderCount }}</span>
            </div>
          </div>
        </div>
      </div>
      <div class="top-middle">
        <div class="middle-top">
          <div class="chart-item middle-larger">
            <div class="four-small">
              <a-skeleton active :loading="skeletonIsShow" :paragraph="{ rows: 1 }">
                <a-card :bordered="false" style="border-radius: 13px;">
                  <a-statistic style="margin-top: 10px;font-weight: bold;padding-left:20px;" :value-style="{ color: '#2F61DC' }" :value="(mainChart.todaySuccessRate*100).toFixed(2)+ '%'" />
                </a-card>
                <img src="~@/assets/dashboard/icon_jinrichengg.png" class="right-top-icon">
                <div class="four-small-title">
                  <span>今日成功率</span>
                </div>
              </a-skeleton>
            </div>
          </div>
          <div class="chart-item middle-smaller">
            <div class="four-small">
              <a-skeleton active :loading="skeletonIsShow" :paragraph="{ rows: 1 }">
                <a-card :bordered="false" style="border-radius: 13px;">
                  <a-statistic style="margin-top: 10px;font-weight: bold;padding-left:20px;" :value-style="{ color: '#30CC99' }" :value="mainChart.mchNum" />
                </a-card>
                <img src="~@/assets/dashboard/icon_shanghushuliang.png" class="right-top-icon">
                <div class="four-small-title">
                  <span>商户数量</span>
                </div>
              </a-skeleton>
            </div>
          </div>
        </div>
        <div class="middle-bottom">
          <div class="chart-item middle-larger">
            <div class="four-small">
              <a-skeleton active :loading="skeletonIsShow" :paragraph="{ rows: 1 }">
                <a-card :bordered="false" style="border-radius: 13px;">
                  <a-statistic style="margin-top: 10px;font-weight: bold;padding-left:20px;" :value-style="{ color: '#DB4B4B' }" :value="(mainChart.yesterdaySuccessRate*100).toFixed(2)+ '%'" />
                </a-card>
                <img src="~@/assets/dashboard/icon_zuorichengg.png" class="right-top-icon">
                <div class="four-small-title">
                  <span>昨日成功率</span>
                </div>
              </a-skeleton>
            </div>
          </div>
          <div class="chart-item middle-smaller">
            <div class="four-small">
              <a-skeleton active :loading="skeletonIsShow" :paragraph="{ rows: 1 }">
                <a-card :bordered="false" style="border-radius: 13px;">
                  <a-statistic style="margin-top: 10px;font-weight: bold;padding-left:20px;" :value-style="{ color: '#864FE1' }" :value="mainChart.agentNum" />
                </a-card>
                <img src="~@/assets/dashboard/icon_dailishuliang.png" class="right-top-icon">
                <div class="four-small-title">
                  <span>代理数量</span>
                </div>
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
                <span class="chart-title" style="font-weight: bold;color: #262626;font-size: 18px">实时并发</span>
                <a-radio-group v-model="tableChannel" button-style="solid" @change="selectTab">
                  <a-radio-button value="5">5分钟</a-radio-button>
                  <a-radio-button value="20">20分钟</a-radio-button>
                  <a-radio-button value="60">60分钟</a-radio-button>
                </a-radio-group>
              </div>
              <div style="margin-top: 12px;">
                <JeepayTable
                    @btnLoadClose="btnLoading=false"
                    ref="infoTable"
                    :initData="true"
                    :reqTableDataFunc="reqTableDataFunc"
                    :tableColumns="tableColumns"
                    :pageSize = 10
                    :searchData="searchData"
                    rowKey="mchName"
                >
                  <template slot="nameSlot" slot-scope="{record}">
                    <span class="name-label">{{ record.mchName }}</span>
                  </template> <!-- 自定义插槽 -->
                  <template slot="perMinCountSlot" slot-scope="{record}">
                    <b>{{record.perMinCount}}</b>
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
              <span class="chart-title" style="font-weight: bold;color: #262626;font-size: 18px">实时监控</span><span style="color: #969B9F">（近30分钟）</span>
            </div>
            <template>
              <div id="chartContainer" style="max-width:800px;width: 700px;min-width: 700px; height:500px;"></div>
            </template>
            <div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>

  // import { TinyArea, Column, Pie, measureTextWidth } from '@antv/g2plot'
  import * as echarts from 'echarts'
  import {
     API_URL_MAIN_STATISTIC,
    getRealTimeStat,
    getTwoDayCount,
    req
  } from '@/api/manage'
  import moment from 'moment'
  import store from '@/store'
  import { timeFix } from '@/utils/util'
  import empty from './empty'
  import JeepayTableColumns from '@/components/JeepayTable/JeepayTableColumns.vue'
  import JeepayTable from '@/components/JeepayTable/JeepayTable.vue' // 空数据展示的组件，首页自用
  const tableColumns = [
    { key: 'name', fixed: 'left', width: '350px', title: '商户名', scopedSlots: { customRender: 'nameSlot' } },
    { key: 'allCount', title: '有效下单次数', dataIndex: 'allCount' },
    { key: 'perMinCount', title: '并发数/每分钟', width: '200px', scopedSlots: { customRender: 'perMinCountSlot' } }
  ]
  export default {
    data () {
      return {
        rowKey: 'mchNo',
        btnLoading: false,
        skeletonIsShow: true, // 骨架屏是否显示
        skeletonReqNum: 0, // 当所有数据请求完毕后关闭骨架屏（共四个请求）
        lastSevenDays: true, // 最近七天是否显示
        pieDays: false, // 饼状图的关闭按钮是否展示
        visible: false,
        recordId: store.state.user.userId,
        searchData: {
          'time': 5
        }, // 查询条件
        greetImg: store.state.user.avatarImgPath, // 头像图片地址
        isPayType: true, // 产品是否存在数据
        isPayCount: true, // 交易统计是否存在数据
        ispayAmount: true, // 今日交易金额是否存在数据
        jeeDate: undefined, // 自定义日期选择框所用状态
        jeeDatePie: undefined, // 自定义日期选择框所用状态-产品
        isAdmin: store.state.user.isAdmin, // 是否为超级管理员
        mainTips: { // 主页提示
          helloTitle: ''
        },
        mainChart: { // 主页统计数据
          todayCount: {},
          yesterdayCount: {}
        },
        tableChannel: '5', // tab选择
        tableColumns: tableColumns,
        passageData: [],
        chartPassage: null,
        timer: null
      }
    },
    components: { JeepayTable, JeepayTableColumns, empty },
    methods: {
      init () {
        const that = this
        if (this.$access('ENT_C_MAIN_PAY_COUNT')) {
          getTwoDayCount().then(res => {
            that.mainChart = res
            that.skeletonClose(that)
          }).catch((err) => {
            console.error(err)
            that.skeletonClose(that)
          })
        } else {
          that.skeletonClose(that)
        }
      },
      initChart () { // 初始化统计表
        var chartContainer = document.getElementById('chartContainer')
        this.chartPassage = echarts.init(chartContainer)
        this.passageData = []
        const that = this
        getRealTimeStat().then(res => {
          // 在这里配置你的图表选项和数据
          const yAxisData = []
          for (var key in res) {
            if (res.hasOwnProperty(key)) {
              var value = res[key]
              var successRate = 0
              if (value.allCount !== 0) {
                successRate = (value.successCount / value.allCount * 100).toFixed(2)
              } else {
                successRate = successRate.toFixed(2)
              }
              that.passageData.push(successRate)
              yAxisData.push(value.passageName + '-成交金额：' + (value.successAmount / 100).toFixed(2))
            }
          }
          const options = {
            align: {
              options: {
                left: 'left',
                center: 'center',
                right: 'right'
              }
            },
            tooltip: {
              trigger: 'axis',
              axisPointer: {
                type: 'shadow'
              }
            },
            xAxis: {
              max: 'dataMax'
            },
            yAxis: {
              type: 'category',
              data: yAxisData,
              animationDuration: 300,
              animationDurationUpdate: 300,
              max: 20,
              axisLabel: {
                show: true,
                inside: true, // 设置文本显示在柱图内部
                textStyle: {
                  color: '#fff',
                  fontSize: 12
                }
              }
            },
            series: [
              {
                // realtimeSort: true,
                name: '通道成功率',
                type: 'bar',
                data: that.passageData,
                label: {
                  show: true,
                  position: 'right',
                  valueAnimation: true
                },
                itemStyle: {
                  color: '#369AFE'
                }
              }
            ],
            legend: {
              show: true
            },
            animationDuration: 0,
            animationDurationUpdate: 3000,
            animationEasing: 'linear',
            animationEasingUpdate: 'linear'
          }
          that.chartPassage.setOption(options)
        }).catch((err) => {
          console.error(err)
        })
        this.timer = setInterval(function () {
          that.updatePassageData()
          that.$refs.infoTable.refTable()
        }, 30000)
      },
      reqTableDataFunc: (params) => {
        return req.list(API_URL_MAIN_STATISTIC + '/realTimeConcurrent', params)
      },
      moment,
      disabledDate (current) {
        // 当天之前的三十天，可选。 当天也可选
        return current < moment().subtract(32, 'days') || current > moment().endOf('day')
      },
      skeletonClose (that) {
        that.skeletonIsShow = false
      },
      selectTab () {
        this.searchData.time = this.tableChannel
        this.$refs.infoTable.refTable(true)
      },
      updatePassageData () {
        const that = this
        that.passageData = []
        getRealTimeStat().then(res => {
          const yAxisData = []
          for (const key in res) {
            if (res.hasOwnProperty(key)) {
              var value = res[key]
              var successRate = 0
              if (value.allCount !== 0) {
                successRate = (value.successCount / value.allCount * 100).toFixed(2)
              } else {
                successRate = successRate.toFixed(2)
              }
              that.passageData.push(successRate)
              yAxisData.push(value.passageName + '-成交金额：' + (value.successAmount / 100).toFixed(2))
            }
          }
          that.chartPassage.setOption({
            series: [
              {
                data: that.passageData
              }
            ],
            yAxis: {
              data: yAxisData
            }
          })
        }).catch((err) => {
          console.error(err)
        })
      }
    },
    computed: {
    },
    mounted () {
      this.initChart()
      // 用户名信息以及时间问候语句。由于退出登陆才让他更改成功，所以这里的数据先从 vuex中获取
      this.mainTips.helloTitle = `${timeFix()}，` + this.$store.state.user.loginUsername
      this.init()
    },
    beforeRouteLeave (to, from, next) {
      // 在离开前执行一些操作，例如提示用户保存数据
      // ...
      // 执行next()，表示继续进行路由切换
      console.log('test')
      clearInterval(this.timer)
      next()
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
    //display:flex;
    //justify-content:space-between;
    //padding-bottom:0;
    //align-items: center;
    color: white;
    padding-right:20px;
    text-align: right;
    width: 100%;
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
   width: 180px;
   height: 26px;
   box-sizing: border-box;
   position: absolute;
    padding-right: 10px;
    padding-left: 10px;
   bottom:30px;
    right: 10px;
   box-sizing: border-box;
    background: rgba(255,255,255,0.2);
    border-radius: 13px 13px 13px 13px;
  }
  .payAmountSpan span{
    text-align: right;
    color: white;
    width: 60%;
    font-weight: bold;
    font-size: 14px;
    line-height: 26px;
  }
  .payAmountSpan .title{
    text-align: left;
    color: white;
    width: 40%;
    font-weight: normal;
    font-size: 14px;
    line-height: 26px;
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
      //margin-right: 20px;
      margin-top: 20px;
      font-weight: bold;
      color: white;
      //padding:20px;
      text-align: right;
      width: 100%;
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
  .chart-icon-div{
    position: absolute;
    width: 62px;
    height: 62px;
    border-radius: 40px 40px 40px 40px;
    opacity: 1;
    bottom: 30px;
    left: 20px;
    background: #FFFFFF url(~@/assets/dashboard/icon_chengjiaojine.png) no-repeat center center ;
  }
  .chart-icon-div-order-success-num{
    position: absolute;
    width: 62px;
    height: 62px;
    border-radius: 40px 40px 40px 40px;
    opacity: 1;
    bottom: 30px;
    left: 20px;
    background: #FFFFFF url(~@/assets/dashboard/icon_chengjiaodingdshu.png) no-repeat center center ;
  }
  .chart-icon-div-today-income{
    position: absolute;
    width: 62px;
    height: 62px;
    border-radius: 40px 40px 40px 40px;
    opacity: 1;
    bottom: 30px;
    left: 20px;
    background: #FFFFFF url(~@/assets/dashboard/icon_pingtailirun.png) no-repeat center center ;
  }
  .chart-icon-div-order-num{
    position: absolute;
    width: 62px;
    height: 62px;
    border-radius: 40px 40px 40px 40px;
    opacity: 1;
    bottom: 30px;
    left: 20px;
    background: #FFFFFF url(~@/assets/dashboard/icon_dingdanshu.png) no-repeat center center ;
  }
  .chart-title {
    font-size: 16px;
    font-weight: 500;
    margin-right:20px;
    margin-bottom:20px;
  }
  .chengjiaojine{
    background-image: url(~@/assets/dashboard/bg_chengjiaojine.png);
    background-size: 100% 100%;
    background-repeat: no-repeat;
  }
  .chengjiaodingdan{
    background-image: url(~@/assets/dashboard/bg_chengjiaodingdanshu.png);
    background-size: 100% 100%;
    background-repeat: no-repeat;
  }
  .today-income{
    background-image: url(~@/assets/dashboard/bg_pingtailirun.png);
    background-size: 100% 100%;
    background-repeat: no-repeat;
  }
  .order-num{
    background-image: url(~@/assets/dashboard/bg_dingdanshu.png);
    background-size: 100% 100%;
    background-repeat: no-repeat;
  }
  .four-small{
    position:relative;
    min-height: 75px;
    min-width: 146px;
    background-color: white;
    height: 100%;
    width: 100%;
    border-radius: 13px;
  }
  .four-small-title {
    //display:flex;
    //justify-content:space-between;
    //padding-bottom:0;
    //align-items: center;
    color: #717579;
    padding-left:20px;
    text-align: left;
    width: 100%;
  }
  .name-label{
    color: #333333;
    font-size: 14px;
    font-weight: 400;
  }
  .right-top-icon{
    position: absolute;
    top:20px;
    right: 20px;
  }
</style>
