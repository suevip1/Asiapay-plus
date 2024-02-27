<template>
  <page-header-wrapper>
    <a-card>
      <div class="table-page-search-wrapper">
        <a-form layout="inline" class="table-head-ground">
          <div class="table-layer">
            <jeepay-text-up :placeholder="'商户号'" :msg="searchData.mchNo" v-model="searchData.mchNo" />
            <jeepay-text-up :placeholder="'商户名'" :msg="searchData.mchName" v-model="searchData.mchName" />
            <span class="table-page-search-submitButtons">
              <a-button type="primary" icon="search" @click="queryFunc" :loading="btnLoading">搜索</a-button>
              <a-button style="margin-left: 8px" icon="reload" @click="() => this.searchData = {}">重置</a-button>
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
          rowKey="mchNo"
      >
        <template slot="balanceSlot" slot-scope="{record}">
          &nbsp;<b :style="{'color': record.balance >0 ? '#4BD884' : '#DB4B4B'}" >{{ (record.balance/100).toFixed(2) }}</b>
        </template> <!-- 自定义插槽 -->
        <template slot="stateSlot" slot-scope="{record}">
          <a-badge :status="record.state === 0?'error':'processing'" :text="record.state === 0?'禁用':'启用'" />
        </template>
        <template slot="amountSlot" slot-scope="{record}">
          &nbsp;<b style="color: #4BD884" >{{ (record.ext.stat.totalSuccessAmount / 100).toFixed(2) }}</b>
        </template> <!-- 自定义插槽 -->
        <template slot="opSlot" slot-scope="{record}">  <!-- 操作列插槽 -->
          <JeepayTableColumns>
            <a-button type="link" @click="detailFunc(record)">查看产品费率</a-button>
          </JeepayTableColumns>
        </template>
      </JeepayTable>
    </a-card>
    <MchProductEdit ref="infoProduct" :callbackFunc="searchFunc" />
  </page-header-wrapper>
</template>

<script>
import JeepayTable from '@/components/JeepayTable/JeepayTable'
import JeepayTextUp from '@/components/JeepayTextUp/JeepayTextUp' // 文字上移组件
import JeepayTableColumns from '@/components/JeepayTable/JeepayTableColumns'
import { API_URL_AGENT_MCH, req } from '@/api/manage'
import MchProductEdit from '@/views/mchs/MchProductEdit.vue'

// eslint-disable-next-line no-unused-vars
const tableColumns = [
  { key: 'mchNo', fixed: 'left', width: '180px', title: '商户号', dataIndex: 'mchNo' },
  { key: 'mchName', title: '商户名称', dataIndex: 'mchName' },
  { key: 'balance', title: '当前余额', scopedSlots: { customRender: 'balanceSlot' } },
  { key: 'state', title: '状态', scopedSlots: { customRender: 'stateSlot' } },
  { key: 'today', title: '今日成交金额', scopedSlots: { customRender: 'amountSlot' } },
  { key: 'op', title: '操作', width: '200px', fixed: 'right', align: 'center', scopedSlots: { customRender: 'opSlot' } }
]

export default {
  name: 'MchAppPage',
  components: { MchProductEdit, JeepayTable, JeepayTableColumns, JeepayTextUp },
  data () {
    return {
      btnLoading: false,
      tableColumns: tableColumns,
      searchData: {}
    }
  },
  methods: {
    queryFunc () {
      this.btnLoading = true
      this.$refs.infoTable.refTable(true)
    },
    // 请求table接口数据
    reqTableDataFunc: (params) => {
      return req.list(API_URL_AGENT_MCH, params)
    },
    searchFunc: function () { // 点击【查询】按钮点击事件
      this.$refs.infoTable.refTable(true)
    },
    detailFunc (mchInfo) {
      // 打开抽屉
      this.$refs.infoProduct.show(mchInfo)
    }
  }
}
</script>

<style lang="less" scoped>
</style>
