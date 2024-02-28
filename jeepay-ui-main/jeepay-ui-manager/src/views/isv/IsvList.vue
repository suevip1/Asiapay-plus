<template>
  <page-header-wrapper>
    <a-card>
      <div class="table-page-search-wrapper" @keydown.enter="queryFunc">
        <a-form layout="inline" class="table-head-ground">
          <div class="table-layer">
            <jeepay-text-up :placeholder="'代理商号'" :msg="searchData.agentNo" v-model="searchData.agentNo" />
            <jeepay-text-up :placeholder="'代理商名称'" :msg="searchData.agentName" v-model="searchData.agentName" />
            <a-form-item label="" class="table-head-layout">
              <a-select v-model="searchData.state" placeholder="代理商状态" default-value="">
                <a-select-option value="">全部</a-select-option>
                <a-select-option value="0">禁用</a-select-option>
                <a-select-option value="1">启用</a-select-option>
              </a-select>
            </a-form-item>
            <span class="table-page-search-submitButtons">
              <a-button type="primary" icon="search" @click="queryFunc" :loading="btnLoading">搜索</a-button>
              <a-button style="margin-left: 8px" icon="reload" @click="() => this.searchData = {}">重置</a-button>
            </span>
          </div>
        </a-form>
        <div>
          <a-button icon="plus" v-if="$access('ENT_ISV_INFO_ADD')" type="primary" @click="addFunc" class="mg-b-30">新建</a-button>
        </div>
      </div>
      <div style="background-color: #fafafa;padding-left: 15px;padding-top: 10px;padding-bottom: 10px;border-bottom: 1px solid #e8e8e8">
        <a-row>
          <a-col class="stat-col bg-color-1" :span="4">
            <span class="title">代理总数</span>
            <b style="color: #DB4B4B;">{{this.totalAgentInfo.agentNum}}</b>
          </a-col>
          <a-col class="stat-col bg-color-2" :span="4" :offset="1">
            <span class="title">代理总余额</span>
            <b style="color: #FA9D2A;">{{(this.totalAgentInfo.totalBalance/100).toFixed(2)}}</b>
          </a-col>
          <a-col class="stat-col bg-color-3" :span="4" :offset="1">
            <span class="title">冻结金额汇总</span>
            <b style="color: #2F61DC;">{{(this.totalAgentInfo.freezeBalance/100/100).toFixed(2)}}</b>
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
        rowKey="agentNo"
      >
        <template slot="isvNameSlot" slot-scope="{record}"><b>{{ record.agentName }}</b></template> <!-- 自定义插槽 -->
        <template slot="stateSlot" slot-scope="{record}">
          <a-badge :status="record.state === 0?'error':'processing'" :text="record.state === 0?'禁用':'启用'" />
        </template>
        <template slot="balanceSlot" slot-scope="{record}">
          <a-button size="small" icon="edit" v-if="$access('ENT_ISV_INFO_EDIT')" type="primary" @click="clickChangeBalance(record)" >调额</a-button>
          &nbsp;&nbsp;￥<b :style="{'color': record.balance >0 ? '#4BD884' : '#DB4B4B'}" >{{ (record.balance/100).toFixed(2) }}</b>
        </template> <!-- 自定义插槽 -->
        <template slot="opSlot" slot-scope="{record}">  <!-- 操作列插槽 -->
          <JeepayTableColumns>
            <a-button type="link" v-if="$access('ENT_ISV_INFO_EDIT')" @click="editFunc(record.agentNo)">修改</a-button>
            <a-button type="link" v-if="$access('ENT_ISV_INFO_DEL')" style="color: red" @click="delFunc(record.agentNo)">删除</a-button>
          </JeepayTableColumns>
        </template>
      </JeepayTable>
    </a-card>
    <!-- 新增页面组件  -->
    <InfoAddOrEdit ref="infoAddOrEdit" :callbackFunc="searchFunc"/>

    <!-- 弹窗 -->
    <template>
      <a-modal v-model="isShowModal" title="调整代理商余额" @ok="handleOkFunc">
        <a-form-model ref="infoFormModel" :model="changeObject" :label-col="{span: 6}" :wrapper-col="{span: 15}" :rules="changeRules">
          <a-form-model-item label="代理商户号：" >
            <span style="color: black">{{selectAgent.agentNo}}</span>
          </a-form-model-item>
          <a-form-model-item label="代理商名称：">
            <span style="color: black">{{selectAgent.agentName}}</span>
          </a-form-model-item>
          <a-form-model-item label="调整金额：" prop="changeAmount">
            <a-input prefix="￥" type="number" v-model="changeObject.changeAmount" />
            <b style="color: gray">如需扣余额，则输入负数，例如:-10.50</b>
          </a-form-model-item>
          <a-form-model-item label="备注：" prop="changeRemark">
            <a-input v-model="changeObject.changeRemark" />
          </a-form-model-item>
        </a-form-model>
      </a-modal>
    </template>
  </page-header-wrapper>
</template>

<script>
import JeepayTable from '@/components/JeepayTable/JeepayTable'
import JeepayTextUp from '@/components/JeepayTextUp/JeepayTextUp' // 文字上移组件
import JeepayTableColumns from '@/components/JeepayTable/JeepayTableColumns'
import {
  API_URL_AGENT_STAT_LIST,
  API_URL_ISV_BALANCE,
  API_URL_ISV_LIST,
  req
} from '@/api/manage'
import InfoAddOrEdit from './AddOrEdit'

// eslint-disable-next-line no-unused-vars
const tableColumns = [
  { key: 'agentName', width: '200px', sorter: true, title: '代理商名称', fixed: 'left', scopedSlots: { customRender: 'isvNameSlot' } },
  { key: 'agentNo', title: '代理商户号', dataIndex: 'agentNo', width: '150px' },
  { key: 'balance', title: '代理余额', width: 300, sorter: true, scopedSlots: { customRender: 'balanceSlot' } },
  { key: 'state', title: '代理商状态', scopedSlots: { customRender: 'stateSlot' } },
  { key: 'createdAt', dataIndex: 'createdAt', sorter: true, title: '创建日期' },
  { key: 'updatedAt', dataIndex: 'updatedAt', title: '修改日期' },
  { key: 'remark', dataIndex: 'remark', title: '备注' },
  { key: 'op', title: '操作', width: '260px', fixed: 'right', align: 'center', scopedSlots: { customRender: 'opSlot' } }
]

export default {
  name: 'IsvListPage',
  components: { JeepayTable, JeepayTableColumns, InfoAddOrEdit, JeepayTextUp },
  data () {
    return {
      btnLoading: false,
      tableColumns: tableColumns,
      searchData: {},
      isShowModal: false,
      saveObject: {},
      changeObject: {},
      selectAgent: {},
      totalAgentInfo: {
        agentNum: 0,
        totalBalance: 0,
        freezeBalance: 0
      },
      changeRules: {
        changeRemark: [
          { required: true, message: '请输入调额备注', trigger: 'blur' }
        ],
        changeAmount: [
          { required: true, message: '请输入调整金额', trigger: 'blur' }
        ]
      }
    }
  },
  mounted () {
    this.queryFunc()
  },
  methods: {
    queryFunc () {
      this.btnLoading = true
      this.getAgentStatInfo()
      this.$refs.infoTable.refTable(true)
    },
    // 请求table接口数据
    reqTableDataFunc: (params) => {
      return req.list(API_URL_ISV_LIST, params)
    },
    delFunc: function (recordId) {
      const that = this
      this.$infoBox.confirmDanger('确认删除？', '请确认该代理商下未分配商户', () => {
        req.delById(API_URL_ISV_LIST, recordId).then(res => {
          that.$refs.infoTable.refTable()
          this.$message.success('删除成功')
        })
      })
    },
    clickChangeBalance: function (record) { // 业务通用【新增】 函数
      this.isShowModal = true
      this.selectAgent = record
      if (this.$refs.infoFormModel !== undefined) {
        this.$refs.infoFormModel.resetFields()
      }
      this.changeObject = {}
    },
    handleOkFunc: function () {
      if (this.changeObject.changeAmount === '' || this.changeObject.changeAmount === undefined) {
        this.$message.error('金额不能为空')
        return
      }
      if (this.changeObject.changeRemark === '' || this.changeObject.changeRemark === undefined) {
        this.$message.error('备注不能为空')
        return
      }
      req.updateById(API_URL_ISV_BALANCE, this.selectAgent.agentNo, this.changeObject).then(res => {
        this.$refs.infoTable.refTable(true)
        this.$message.success('修改成功')
      })
      this.isShowModal = false
    },
    searchFunc: function () { // 点击【查询】按钮点击事件
      this.getAgentStatInfo()
      this.$refs.infoTable.refTable(true)
    },
    addFunc: function () { // 业务通用【新增】 函数
      this.$refs.infoAddOrEdit.show()
    },
    editFunc: function (recordId) { // 业务通用【修改】 函数
      this.$refs.infoAddOrEdit.show(recordId)
    },
    getAgentStatInfo: function () {
      const that = this
      req.postDataNormal(API_URL_AGENT_STAT_LIST, 'statAgentInfo', this.searchData).then(res => {
        that.totalAgentInfo = res
      })
    }
  }
}
</script>

<style lang="less" scoped>
.stat-col{
  position: relative;
  //background-color: gray;
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
