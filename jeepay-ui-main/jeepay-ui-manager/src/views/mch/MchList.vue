<template>
  <page-header-wrapper>
    <a-card>
      <div class="table-page-search-wrapper" @keydown.enter="queryFunc">
        <a-form layout="inline" class="table-head-ground">
          <div class="table-layer">
            <jeepay-text-up :placeholder="'商户号'" :msg="searchData.mchNo" v-model="searchData.mchNo"/>
            <jeepay-text-up :placeholder="'商户名称'" :msg="searchData.mchName" v-model="searchData.mchName"/>
            <jeepay-text-up :placeholder="'代理商号'" :msg="searchData.agentNo" v-model="searchData.agentNo"/>
            <a-form-item label="" class="table-head-layout">
              <a-select v-model="searchData.state" placeholder="商户状态" default-value="">
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
          <a-button v-if="$access('ENT_MCH_INFO_ADD')" type="primary" icon="plus" @click="addFunc" class="mg-b-30">新建</a-button>
          <a-button type="danger" style="margin-left: 8px" icon="download" @click="exportExcel">导出</a-button>
        </div>
      </div>
      <div style="background-color: #fafafa;padding-left: 15px;padding-top: 10px;padding-bottom: 10px;border-bottom: 1px solid #e8e8e8">
        <a-row>
          <a-col class="stat-col bg-color-1" :span="4">
            <span class="title">商户总数</span>
            <b style="color: #DB4B4B;">{{this.totalMchInfo.mchNum}}</b>
          </a-col>
          <a-col class="stat-col bg-color-2" :span="4" :offset="1">
            <span class="title">商户总余额</span>
            <b style="color: #FA9D2A;">{{(this.totalMchInfo.totalBalance/100).toFixed(2)}}</b>
          </a-col>
          <a-col class="stat-col bg-color-3" :span="4" :offset="1">
            <span class="title">冻结金额汇总</span>
            <b style="color: #2F61DC;">{{(this.totalMchInfo.freezeBalance/100).toFixed(2)}}</b>
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
        rowKey="mchNo"
      >
        <template slot="mchNameSlot" slot-scope="{record}">
<!--          <b>{{ record.mchName }}</b>-->
          <a style="font-weight: bold" v-if="$access('ENT_MCH_INFO_VIEW')" @click="detailFunc(record.mchNo)">{{ record.mchName }}</a>
        </template> <!-- 自定义插槽 -->
        <template slot="stateSlot" slot-scope="{record}">
          <a-badge :status="record.state === 0?'error':'processing'" :text="record.state === 0?'禁用':'启用'" />
        </template>
      <!--    全局金额颜色参考此处    -->
        <template slot="balanceSlot" slot-scope="{record}">
          <a-button size="small" icon="edit" v-if="$access('ENT_MCH_INFO_EDIT')" type="primary" @click="clickChangeBalance(record)" >调额</a-button>
          &nbsp;&nbsp;<b :style="{'color': record.balance >0 ? '#4BD884' : '#DB4B4B'}" >{{ (record.balance/100).toFixed(2) }}</b>
        </template> <!-- 自定义插槽 -->
        <template slot="agentInfoSlot" slot-scope="{record}"><a-tag v-if="record.agentNo!=''" color="blue">{{record.agentNo}}</a-tag>{{ record.agentName}}</template>
        <template slot="opSlot" slot-scope="{record}">  <!-- 操作列插槽 -->
          <JeepayTableColumns>
            <a-button type="link" v-if="$access('ENT_MCH_INFO_EDIT')" @click="editFunc(record.mchNo)">修改</a-button>
            <a-button type="link" v-if="$access('ENT_MCH_APP_CONFIG')" @click="mchAppConfig(record)">支付配置</a-button>
            <a-button type="link" v-if="$access('ENT_MCH_APP_CONFIG')" @click="mchPassageConfig(record)">通道绑定</a-button>
            <a-button type="link" v-if="$access('ENT_MCH_INFO_DEL') && record.mchNo!=='M1691231056'" style="color: red" @click="delFunc(record.mchNo)">删除</a-button>
          </JeepayTableColumns>
        </template>
      </JeepayTable>
    </a-card>
    <!-- 新增页面组件  -->
    <InfoAddOrEdit ref="infoAddOrEdit" :callbackFunc="searchFunc"/>
    <!-- 新增页面组件  -->
    <InfoDetail ref="infoDetail" :callbackFunc="searchFunc"/>
    <MchProductEdit ref="infoProduct" :callbackFunc="searchFunc"/>
    <MchPassageEdit ref="infoPassage" :callbackFunc="searchFunc"/>
    <!-- 弹窗 -->
    <template>
      <a-modal v-model="isShowModal" title="调整商户余额" @ok="handleOkFunc">
        <a-form-model ref="infoFormModel" :model="changeObject" :label-col="{span: 6}" :wrapper-col="{span: 15}" :rules="changeRules">
          <a-form-model-item label="商户号：" >
            <span style="color: black">{{selectMch.mchNo}}</span>
          </a-form-model-item>
          <a-form-model-item label="商户名称：">
            <span style="color: black">{{selectMch.mchName}}</span>
          </a-form-model-item>
          <a-form-model-item label="调整金额：" prop="changeAmount">
            <a-input prefix="￥" type="number" v-model="changeObject.changeAmount" />
            <b style="color: rgb(128,128,128)">如需扣余额，则输入负数，例如:-10.50</b>
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
import { API_URL_MCH_BALANCE, API_URL_MCH_LIST, API_URL_MCH_STAT_LIST, exportExcel, req, reqLoad } from '@/api/manage'
import InfoAddOrEdit from './AddOrEdit'
import InfoDetail from './Detail'
import MchProductEdit from '@/views/mch/MchProductEdit.vue'
import MchPassageEdit from '@/views/mch/MchPassageEdit.vue'
import moment from 'moment/moment'
import { saveAs } from 'file-saver'

// eslint-disable-next-line no-unused-vars
const tableColumns = [
  { key: 'mchName', fixed: 'left', width: '150px', sorter: true, title: '商户名称', scopedSlots: { customRender: 'mchNameSlot' } },
  { key: 'mchNo', title: '商户号', dataIndex: 'mchNo', width: '130px' },
  { key: 'agentNo', title: '代理商', width: '260px', scopedSlots: { customRender: 'agentInfoSlot' } },
  { key: 'balance', title: '商户余额(￥)', sorter: true, width: '300px', scopedSlots: { customRender: 'balanceSlot' } },
  { key: 'state', title: '状态', width: '80px', scopedSlots: { customRender: 'stateSlot' } },
  { key: 'createdAt', dataIndex: 'createdAt', sorter: true, title: '创建日期', width: '250px' },
  { key: 'remark', dataIndex: 'remark', title: '备注', width: '120px' },
  { key: 'op', title: '操作', fixed: 'right', align: 'center', scopedSlots: { customRender: 'opSlot' } }
]

export default {
  name: 'MchListPage',
  components: { JeepayTable, JeepayTableColumns, InfoAddOrEdit, InfoDetail, JeepayTextUp, MchProductEdit, MchPassageEdit },
  data () {
    return {
      btnLoading: false,
      tableColumns: tableColumns,
      searchData: {},
      value: "''",
      selectMch: {},
      changeObject: {},
      isShowModal: false,
      totalMchInfo: {
        mchNum: 0,
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
      this.$refs.infoTable.refTable(true)
      this.getMchStatInfo()
    },
    // 请求table接口数据
    reqTableDataFunc: (params) => {
      return req.list(API_URL_MCH_LIST, params)
    },
    searchFunc: function () { // 点击【查询】按钮点击事件
      this.getMchStatInfo()
      this.$refs.infoTable.refTable(true)
    },
    addFunc: function () { // 业务通用【新增】 函数
      this.$refs.infoAddOrEdit.show()
    },
    editFunc: function (recordId) { // 业务通用【修改】 函数
      this.$refs.infoAddOrEdit.show(recordId)
    },
    detailFunc: function (recordId) { // 商户详情页
      this.$refs.infoDetail.show(recordId)
    },
    // 删除商户
    delFunc: function (recordId) {
      const that = this
      this.$infoBox.confirmDanger('确认删除？', '该操作将删除商户下所有配置及用户信息', () => {
        reqLoad.delById(API_URL_MCH_LIST, recordId).then(res => {
          that.$refs.infoTable.refTable()
          that.getMchStatInfo()
          this.$message.success('删除成功')
        })
      })
    },
    mchAppConfig: function (record) { // 支付产品配置
      this.$refs.infoProduct.show(record)
    },
    mchPassageConfig: function (record) { // 通道绑定
      this.$refs.infoPassage.show(record)
    },
    clickChangeBalance: function (record) { // 业务通用【新增】 函数
      this.isShowModal = true
      this.selectMch = record
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
      this.isShowModal = false
      req.updateById(API_URL_MCH_BALANCE, this.selectMch.mchNo, this.changeObject).then(res => {
        this.$refs.infoTable.refTable()
        this.$message.success('修改成功')
      }).catch(err => {
        console.log(err)
        this.isShowModal = false
      })
    },
    getMchStatInfo: function () {
      const that = this
      req.postDataNormal(API_URL_MCH_STAT_LIST, 'statMchInfo', this.searchData).then(res => {
        that.totalMchInfo = res
      })
    },
    exportExcel: function () { // todo 这里返回的结果没有嵌入到通用返回格式中
      this.$message.success('操作成功！请误操作页面，完成后将自动开启下载请耐心等待')
      exportExcel('/api/mchRealTimeInfo/exportExcel', this.searchData).then(res => {
        const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
        // 使用 FileSaver.js 的 saveAs 方法将 Blob 对象保存为文件
        saveAs(blob, moment().format('YYYY-MM-DD HH:mm:ss') + '-商户实时余额.xlsx')
      }).catch((resErr) => {
        const blob = new Blob([resErr], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
        // 使用 FileSaver.js 的 saveAs 方法将 Blob 对象保存为文件
        saveAs(blob, moment().format('YYYY-MM-DD HH:mm:ss') + '-商户实时余额.xlsx')
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
