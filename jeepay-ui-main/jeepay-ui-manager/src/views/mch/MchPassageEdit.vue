<template>
  <a-drawer :visible="visible" :title=" true ? '支付通道绑定' : '' " @close="onClose" :body-style="{ paddingBottom: '80px' }" width="40%">
    <div class="table-page-search-wrapper">
      <a-form layout="inline" class="table-head-ground">
        <a-row justify="space-between" type="flex">
          <a-col :sm="12">
            <a-descriptions>
              <a-descriptions-item label="商户号">
                <b>{{ mchNo }}</b>
              </a-descriptions-item>
            </a-descriptions>
          </a-col>
          <a-col :sm="12">
            <a-descriptions>
              <a-descriptions-item label="商户名称">
                {{ mchName }}
              </a-descriptions-item>
            </a-descriptions>
          </a-col>
        </a-row>
        <br/>
        <div class="table-layer">
          <jeepay-text-up :placeholder="'通道ID'" :msg="searchData.payPassageId" v-model="searchData.payPassageId"/>
          <jeepay-text-up :placeholder="'通道名'" :msg="searchData.payPassageName" v-model="searchData.payPassageName"/>
          <span class="table-page-search-submitButtons" style="flex-grow: 0; flex-shrink: 0;">
            <a-button type="primary" icon="search" @click="queryFunc" :loading="btnLoading">查询</a-button>
            <a-button style="margin-left: 8px" icon="reload" @click="() => this.searchData = {'mchNo' : mchNo}">重置</a-button>
          </span>
          <br/>
          <br/>
          <span class="table-page-search-submitButtons" style="flex-grow: 0; flex-shrink: 0;">
            <template>
              <a-popconfirm title="确认全部绑定么?" ok-text="确认" cancel-text="取消" @confirm="blindAll"><a-button type="primary" icon="check" :loading="btnLoading">一键全绑定</a-button></a-popconfirm>
            </template>
            <template>
              <a-popconfirm title="确认全部解绑么?" ok-text="确认" cancel-text="取消" @confirm="unBlindAll"><a-button style="margin-left: 8px" icon="close"  :loading="btnLoading" >一键全解绑</a-button>
              </a-popconfirm>
            </template>
          </span>
        </div>
      </a-form>
    </div>

    <!-- 列表渲染 -->
    <JeepayTable
@btnLoadClose="btnLoading=false"
ref="mchPassageTable"
:initData="true"
:reqTableDataFunc="reqTableDataFunc"
:tableColumns="tableColumns"
:searchData="searchData"
rowKey="payPassageId">
      <template slot="nameSlot" slot-scope="{record}">
        <b style="font-weight: bold" >[{{ record.payPassageId }}]{{ record.payPassageName}}</b>
      </template> <!-- 自定义插槽 -->
      <template slot="stateSlot" slot-scope="{record}">
        <a-badge :status="record.state === 0?'error':'processing'" :text="record.state === 0?'未绑定':'已绑定'" />
      </template>
      <!--    全局金额颜色参考此处    -->
      <template slot="mchRateSlot" slot-scope="{record}">
        <b>{{ (record.rate*100).toFixed(2) }}%</b>
      </template>
      <template slot="opSlot" slot-scope="{record}">  <!-- 操作列插槽 -->
        <JeepayTableColumns>
          <a-button type="link" @click="blindSelect(record)">绑定</a-button>
          <a-button type="link" @click="unBlindSelect(record)" style="color: #f5222d">解绑</a-button>
        </JeepayTableColumns>
      </template>
    </JeepayTable>
    <div class="drawer-btn-center" >
      <a-button icon="close" :style="{ marginRight: '8px' }" @click="onClose" style="margin-right:8px">
        关闭
      </a-button>
    </div>
  </a-drawer>
</template>
<script>
import JeepayTable from '@/components/JeepayTable/JeepayTable'
import JeepayTextUp from '@/components/JeepayTextUp/JeepayTextUp' // 文字上移组件
import JeepayTableColumns from '@/components/JeepayTable/JeepayTableColumns'
import { API_URL_MCH_PASSAGE_LIST, req } from '@/api/manage'

// eslint-disable-next-line no-unused-vars
const tableColumns = [
  { key: 'nameSlot', fixed: 'left', width: '280px', title: '通道名称', scopedSlots: { customRender: 'nameSlot' } },
  { key: 'state', title: '状态', width: '100px', scopedSlots: { customRender: 'stateSlot' } },
  { key: 'mchRate', title: '通道费率', scopedSlots: { customRender: 'mchRateSlot' } },
  { key: 'op', title: '操作', width: '150px', fixed: 'right', align: 'center', scopedSlots: { customRender: 'opSlot' } }
]

export default {
  name: 'MchPassageEdit',
  components: { JeepayTable, JeepayTableColumns, JeepayTextUp },
  data () {
    return {
      visible: false, // 是否显示弹层/抽屉
      btnLoading: false,
      tableColumns: tableColumns,
      searchData: {},
      value: "''",
      selectMchPassage: {},
      mchNo: '',
      mchName: ''
    }
  },
  mounted () {
  },
  methods: {
    show: function (mchNo, mchName) { // 弹层打开事件
      // 查询商户所有开通的产品
      this.visible = true
      this.mchNo = mchNo
      this.mchName = mchName
      this.searchData.mchNo = mchNo
      if (this.$refs.mchPassageTable !== undefined) {
        this.$refs.mchPassageTable.refTable(true)
      }
    },
    queryFunc: function () {
      this.btnLoading = true
      this.$refs.mchPassageTable.refTable(true)
    },
    // 请求table接口数据
    reqTableDataFunc: (params) => {
      return req.list(API_URL_MCH_PASSAGE_LIST, params)
    },
    blindSelect: function (record) {
      const params = { }
      params.payPassageId = record.payPassageId
      params.state = 1
      params.mchNo = this.mchNo
      req.updateById(API_URL_MCH_PASSAGE_LIST, '', params).then(res => {
        this.$refs.mchPassageTable.refTable(true)
        this.$message.success('修改成功')
      })
    },
    unBlindSelect: function (record) {
      const params = { }
      params.payPassageId = record.payPassageId
      params.state = 0
      params.mchNo = this.mchNo
      req.updateById(API_URL_MCH_PASSAGE_LIST, '', params).then(res => {
        this.$refs.mchPassageTable.refTable(true)
        this.$message.success('修改成功')
      })
    },
    blindAll () {
      this.btnLoading = true
      req.postNormal(API_URL_MCH_PASSAGE_LIST + '/blindAll', this.mchNo).then(res => {
        setTimeout(() => {
          this.btnLoading = false
          this.$refs.mchPassageTable.refTable(true)
          this.$message.success('修改成功')
        }, 500) // 1000毫秒等于1秒
      })
    },
    unBlindAll () {
      this.btnLoading = true
      req.postNormal(API_URL_MCH_PASSAGE_LIST + '/unBlindAll', this.mchNo).then(res => {
        setTimeout(() => {
          this.btnLoading = false
          this.$refs.mchPassageTable.refTable(true)
          this.$message.success('修改成功')
        }, 500) // 1000毫秒等于1秒
      })
    },
    onClose () {
      this.visible = false
    }
  }
}
</script>
