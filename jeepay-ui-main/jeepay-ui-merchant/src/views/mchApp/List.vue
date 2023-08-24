<template>
  <page-header-wrapper>
    <a-card>
      <!-- 列表渲染 -->
      <JeepayTable
        @btnLoadClose="btnLoading=false"
        ref="infoTable"
        :initData="true"
        :reqTableDataFunc="reqTableDataFunc"
        :tableColumns="tableColumns"
        :searchData="searchData"
        rowKey="productId"
      >
        <template slot="appIdSlot" slot-scope="{record}">
          <b>{{ record.productId }}</b>
        </template> <!-- 自定义插槽 -->
        <template slot="stateSlot" slot-scope="{record}">
          <a-badge :status="record.state === 0?'error':'processing'" :text="record.state === 0?'禁用':'启用'" />
        </template>
        <template slot="rateSlot" slot-scope="{record}">
          <span>{{(record.mchRate*100).toFixed(2)}}%</span>
        </template>
        <template slot="opSlot" slot-scope="{record}">  <!-- 操作列插槽 -->
          <JeepayTableColumns>
            <a-button type="link" @click="mchPayTest(record)">支付测试</a-button>
          </JeepayTableColumns>
        </template>
      </JeepayTable>
    </a-card>
    <MchPayTest ref="mchPayTest"></MchPayTest>
  </page-header-wrapper>
</template>

<script>
import JeepayTable from '@/components/JeepayTable/JeepayTable'
import JeepayTextUp from '@/components/JeepayTextUp/JeepayTextUp' // 文字上移组件
import JeepayTableColumns from '@/components/JeepayTable/JeepayTableColumns'
import { API_URL_MCH_APP, req } from '@/api/manage'
import MchPayTest from '@/views/mchApp/MchPayTest.vue'

// eslint-disable-next-line no-unused-vars
const tableColumns = [
  { key: 'appId', fixed: 'left', width: '220px', title: '产品Id', scopedSlots: { customRender: 'appIdSlot' } },
  { key: 'productName', title: '产品名称', dataIndex: 'productName' },
  { key: 'rate', title: '商户费率', scopedSlots: { customRender: 'rateSlot' } },
  { key: 'state', title: '状态', scopedSlots: { customRender: 'stateSlot' } },
  { key: 'createdAt', dataIndex: 'createdAt', title: '创建日期' },
  { key: 'op', title: '操作', width: '260px', fixed: 'right', align: 'center', scopedSlots: { customRender: 'opSlot' } }
]

export default {
  name: 'MchAppPage',
  components: { JeepayTable, JeepayTableColumns, JeepayTextUp, MchPayTest },
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
      return req.list(API_URL_MCH_APP, params)
    },
    searchFunc: function () { // 点击【查询】按钮点击事件
      this.$refs.infoTable.refTable(true)
    },
    addFunc: function () { // 业务通用【新增】 函数
      this.$refs.mchAppAddOrEdit.show()
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
    mchPayTest (record) {
      this.$refs.mchPayTest.show(record)
    }
  }
}
</script>

<style lang="less" scoped>
</style>
