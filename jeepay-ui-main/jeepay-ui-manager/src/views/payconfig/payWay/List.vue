<template>
  <page-header-wrapper>
    <a-card>
      <div class="table-page-search-wrapper" @keydown.enter="searchFunc">
        <a-form layout="inline" v-if="$access('ENT_PC_WAY_SEARCH')" class="table-head-ground">
          <div class="table-layer">
            <jeepay-text-up :placeholder="'产品代码'" :msg="searchData.productId" v-model="searchData.productId" />
            <jeepay-text-up :placeholder="'产品名称'" :msg="searchData.productName" v-model="searchData.productName" />
            <a-form-item label="" class="table-head-layout">
              <a-select v-model="searchData.state" placeholder="产品状态" default-value="">
                <a-select-option value="">全部</a-select-option>
                <a-select-option value="0">禁用</a-select-option>
                <a-select-option value="1">启用</a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item label="" class="table-head-layout">
              <a-select v-model="searchData.limitState" placeholder="成本限制状态" default-value="">
                <a-select-option value="">全部</a-select-option>
                <a-select-option value="0">禁用</a-select-option>
                <a-select-option value="1">启用</a-select-option>
              </a-select>
            </a-form-item>
            <span class="table-page-search-submitButtons">
              <a-button type="primary" @click="searchFunc(true)" icon="search" :loading="btnLoading">查询</a-button>
              <a-button style="margin-left: 8px;" @click="() => this.searchData = {}" icon="reload">重置</a-button>
            </span>
          </div>
        </a-form>
        <a-button v-if="$access('ENT_PC_WAY_ADD')" type="primary" icon="plus" @click="addFunc" class="mg-b-30">新建</a-button>
      </div>

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
        <template slot="wayCodeSlot" slot-scope="{record}"><b>{{ record.productId }}</b></template> <!-- 自定义插槽 -->
        <template slot="stateSlot" slot-scope="{record}">
          <JeepayTableColState :state="record.state" :showSwitchType="true" :onChange="(state) => { return onStateSwitchChange(record.productId, state)}"/>
        </template>
        <template slot="limitStateSlot" slot-scope="{record}">
          <JeepayTableColState :state="record.limitState" :showSwitchType="true" :onChange="(state) => { return onLimitStateSwitchChange(record.productId, state)}"/>
        </template>
        <template slot="opSlot" slot-scope="{record}">  <!-- 操作列插槽 -->
          <JeepayTableColumns>
            <a v-if="$access('ENT_PC_WAY_EDIT')" @click="editFunc(record.productId)">修改</a>
            <a v-if="$access('ENT_PC_WAY_EDIT')" @click="editMchRateFunc(record)">费率配置</a>
            <a style="color: red" v-if="$access('ENT_PC_WAY_DEL')" @click="delFunc(record.productId)">删除</a>
          </JeepayTableColumns>
        </template>
      </JeepayTable>
    </a-card>

    <!-- 新增页面组件  -->
    <InfoAddOrEdit ref="infoAddOrEdit" :callbackFunc="searchFunc"/>
    <ProductMchEdit ref="productMchEdit" :callbackFunc="searchFunc"/>
  </page-header-wrapper>

</template>
<script>
import JeepayTable from '@/components/JeepayTable/JeepayTable'
import JeepayTableColumns from '@/components/JeepayTable/JeepayTableColumns'
import { API_URL_PAYWAYS_LIST, req } from '@/api/manage'
import InfoAddOrEdit from './AddOrEdit'
import JeepayTextUp from '@/components/JeepayTextUp/JeepayTextUp'
import ProductMchEdit from '@/views/payconfig/payWay/ProductMchEdit.vue'
import JeepayTableColState from '@/components/JeepayTable/JeepayTableColState.vue' // 文字上移组件
// eslint-disable-next-line no-unused-vars
const tableColumns = [
  {
    key: 'productId', // key为必填项，用于标志该列的唯一
    fixed: 'left',
    title: '产品代码',
    width: '120px',
    scopedSlots: { customRender: 'wayCodeSlot' }
  },
  {
    key: 'productName',
    title: '产品名称',
    dataIndex: 'productName'
  },
  {
    key: 'state', // key为必填项，用于标志该列的唯一
    title: '产品状态',
    scopedSlots: { customRender: 'stateSlot' }
  },
  {
    key: 'limitState', // key为必填项，用于标志该列的唯一
    title: '高于成本拉起',
    scopedSlots: { customRender: 'limitStateSlot' }
  },
  {
    key: 'createdAt',
    title: '创建时间',
    dataIndex: 'createdAt'
  },
  {
    key: 'op',
    title: '操作',
    width: '200px',
    fixed: 'right',
    align: 'center',
    scopedSlots: { customRender: 'opSlot' }
  }
]

export default {
  name: 'PayWayPage',
  components: { JeepayTableColState, JeepayTable, JeepayTableColumns, InfoAddOrEdit, JeepayTextUp, ProductMchEdit },
  data () {
    return {
      tableColumns: tableColumns,
      searchData: {},
      btnLoading: false
    }
  },
  methods: {

    // 请求table接口数据
    reqTableDataFunc: (params) => {
      return req.list(API_URL_PAYWAYS_LIST, params)
    },

    searchFunc (isToFirst = false) { // 点击【查询】按钮点击事件
      this.btnLoading = true
      this.$refs.infoTable.refTable(isToFirst)
    },

    addFunc: function () { // 业务通用【新增】 函数
      this.$refs.infoAddOrEdit.show()
    },
    editFunc: function (wayCode) { // 业务通用【修改】 函数
      this.$refs.infoAddOrEdit.show(wayCode)
    },
    editMchRateFunc: function (product) { // 业务通用【修改】 函数
      this.$refs.productMchEdit.show(product)
    },
    delFunc: function (wayCode) {
      const that = this
      this.$infoBox.confirmDanger('确认删除？', '', () => {
        req.delById(API_URL_PAYWAYS_LIST, wayCode).then(res => {
          that.$message.success('删除成功！')
          that.$refs.infoTable.refTable(false)
        })
      })
    },
    onStateSwitchChange (recordId, state) {
      const that = this
      const title = state === 1 ? '确认[启用]该产品？' : '确认[停用]该产品？'
      const content = state === 1 ? '' : ''
      const param = {
        state: state
      }
      return new Promise((resolve, reject) => {
        that.$infoBox.confirmDanger(title, content, () => {
              return req.updateById(API_URL_PAYWAYS_LIST, recordId, param).then(res => {
                that.$message.success('修改成功')
                that.searchFunc()
                resolve()
              }).catch(err => reject(err))
            },
            () => {
              reject(new Error())
            })
      })
    },
    onLimitStateSwitchChange (recordId, state) {
      const that = this
      const title = state === 1 ? '确认[启用]高于成本拉起？' : '确认[停用]高于成本拉起？'
      const content = state === 1 ? '此设置为防止费率配置错误，启用后允许拉起成本价高于收益的产品下通道，打开限制需谨慎！' : '停用后无法拉起成本价高于收益的产品下通道'
      const param = {
        limitState: state
      }
      return new Promise((resolve, reject) => {
        that.$infoBox.confirmDanger(title, content, () => {
              return req.updateById(API_URL_PAYWAYS_LIST, recordId, param).then(res => {
                that.$message.success('修改成功')
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
