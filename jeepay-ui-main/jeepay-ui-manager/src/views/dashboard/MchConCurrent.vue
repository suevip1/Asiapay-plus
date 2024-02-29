<template>
  <div>
    <div class="pay-count-title">
      <span class="chart-title" style="font-weight: bold;color: #262626;font-size: 18px"></span>
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
          :pageSize=10
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
</template>

<script>

import {
  API_URL_MAIN_STATISTIC,
  req
} from '@/api/manage'
import empty from './empty'
import JeepayTableColumns from '@/components/JeepayTable/JeepayTableColumns.vue'
import JeepayTable from '@/components/JeepayTable/JeepayTable.vue' // 空数据展示的组件，首页自用
const tableColumns = [
  { key: 'name', fixed: 'left', width: '350px', title: '商户名', scopedSlots: { customRender: 'nameSlot' } },
  { key: 'allCount', title: '下单次数', dataIndex: 'allCount' },
  { key: 'perMinCount', title: '下单次数/每分钟', width: '200px', scopedSlots: { customRender: 'perMinCountSlot' } }
]
export default {
  data () {
    return {
      rowKey: 'mchNo',
      btnLoading: false,
      searchData: {
        'time': 20
      }, // 查询条件
      tableChannel: '20', // tab选择
      tablePassageChannel: '20', // tab选择
      tableColumns: tableColumns
    }
  },
  props: {
    callbackFunc: { type: Function, default: () => ({}) }
  },
  components: { JeepayTable, JeepayTableColumns, empty },
  methods: {
    reqTableDataFunc: (params) => {
      return req.list(API_URL_MAIN_STATISTIC + '/realTimeConcurrent', params)
    },
    selectTab () {
      this.searchData.time = this.tableChannel
      this.$refs.infoTable.refTable(true)
    },
    show () {
      this.reqTableDataFunc(this.searchData)
    }
  },
  computed: {
  },
  mounted () {
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
