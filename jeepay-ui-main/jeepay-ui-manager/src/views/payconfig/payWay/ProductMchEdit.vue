<template>
  <a-drawer
      :visible="visible"
      :title=" true ? '商户-产品配置' : '' "
      @close="onClose"
      :body-style="{ paddingBottom: '80px' }"
      width="50%"
  >
    <div class="table-page-search-wrapper">
      <a-form layout="inline" class="table-head-ground">
        <a-row justify="space-between" type="flex">
          <a-col :sm="12">
            <a-descriptions>
              <a-descriptions-item label="产品ID">
                <b style="color: #1a53ff">{{ product.productId }}</b>
              </a-descriptions-item>
            </a-descriptions>
          </a-col>
          <a-col :sm="12">
            <a-descriptions>
              <a-descriptions-item label="产品名">
                {{ product.productName }}
              </a-descriptions-item>
            </a-descriptions>
          </a-col>
        </a-row>
        <br/>
        <jeepay-text-up :placeholder="'商户号'" :msg="searchData.mchNo" v-model="searchData.mchNo"/>
        <jeepay-text-up :placeholder="'商户名'" :msg="searchData.mchName" v-model="searchData.mchName"/>
        <jeepay-text-up :placeholder="'上级代理号'" :msg="searchData.agentNo" v-model="searchData.agentNo"/>
        <a-form-item label="" class="table-head-layout">
          <a-select v-model="searchData.haveAgent" placeholder="商户是否存在代理" default-value="">
            <a-select-option value="">全部</a-select-option>
            <a-select-option value="0">无</a-select-option>
            <a-select-option value="1">有</a-select-option>
          </a-select>
        </a-form-item>
        <span class="table-page-search-submitButtons" style="flex-grow: 0; flex-shrink: 0;">
          <a-button type="primary" icon="search" @click="queryFunc" :loading="btnLoading">查询</a-button>
          <a-button style="margin-left: 8px" icon="reload" @click="() => this.searchData = {  'productId' : productId}">重置</a-button>
        </span>
        <div class="table-layer" style="width: 100%">
          <span class="table-page-search-submitButtons" style="flex-grow: 0; flex-shrink: 0;">
            <template>
              <a-popconfirm title="确认全部绑定么?" ok-text="确认" cancel-text="取消" @confirm="blindAll">
                <a-button type="primary" icon="check" :loading="btnLoading">一键全绑定</a-button>
              </a-popconfirm>
            </template>
            <template>
              <a-popconfirm title="确认全部解绑么?" ok-text="确认" cancel-text="取消" @confirm="unBlindAll">
                <a-button style="margin-left: 8px" icon="close"  :loading="btnLoading" >一键全解绑</a-button>
              </a-popconfirm>
            </template>
            <template>
              <a-button type="dashed" style="margin-left: 8px" icon="retweet"  :loading="btnLoading" @click="setAllMch">批量配置产品费率</a-button>
            </template>
          </span>
        </div>
      </a-form>
    </div>

    <!-- 列表渲染 -->
    <JeepayTable
        @btnLoadClose="btnLoading=false"
        ref="mchProductTable"
        :initData="true"
        :reqTableDataFunc="reqTableDataFunc"
        :tableColumns="tableColumns"
        :searchData="searchData"
        :rowSelection="rowSelection"
        rowKey="mchNo"
    >
      <template slot="agentSlot" slot-scope="{record}">
        <span style="color: #1A79FF;font-size: 12px">{{ record.agentNo!=""?'['+record.agentNo+']':'' }}</span>
        <br>
        <span style="font-size: 12px">{{ record.agentName}}</span>
      </template> <!-- 自定义插槽 -->
      <template slot="nameSlot" slot-scope="{record}">
        <b style="color: #1A79FF">[{{ record.mchNo }}]</b><br><b>{{ record.mchName}}</b>
      </template> <!-- 自定义插槽 -->
      <template slot="stateSlot" slot-scope="{record}">
        <a-badge :status="record.state === 0?'error':'processing'" :text="record.state === 0?'禁用':'启用'" />
      </template>
      <!--    全局金额颜色参考此处    -->
      <template slot="mchRateSlot" slot-scope="{record}">
        <b>{{ (record.mchRate*100).toFixed(2) }}%</b>
      </template>
      <template slot="agentRateSlot" slot-scope="{record}">
        <b>{{ (record.agentRate*100).toFixed(2) }}%</b>
      </template>
      <template slot="opSlot" slot-scope="{record}">  <!-- 操作列插槽 -->
        <JeepayTableColumns>
          <a-button type="link" @click="editFunc(record)">修改</a-button>
        </JeepayTableColumns>
      </template>
    </JeepayTable>
    <!-- 弹窗 -->
    <template>
      <a-modal v-model="isShowModal" title="产品费率配置" @ok="handleOkFunc">
        <a-form-model ref="infoFormModel" :label-col="{span: 6}" :wrapper-col="{span: 15}">
          <a-form-model-item label="商户信息：" >
            <b style="color: #1a53ff">[{{selectMchProduct.mchNo}}]</b>&nbsp;<span>&nbsp;{{selectMchProduct.mchName}}</span>
          </a-form-model-item>
          <a-form-model-item label="产品信息：">
            <b style="color: #1a53ff">[{{product.productId}}]</b><span>&nbsp;{{product.productName}}</span>
          </a-form-model-item>
          <a-form-model-item label="状态" prop="state">
            <a-radio-group v-model="changeState">
              <a-radio :value="1">
                启用
              </a-radio>
              <a-radio :value="0">
                禁用
              </a-radio>
            </a-radio-group>
          </a-form-model-item>
          <a-form-model-item label="商户费率：">
            <a-input prefix="%" type="number" v-model="changeMchRate" />
          </a-form-model-item>
          <a-form-model-item label="代理费率：">
            <a-input prefix="%" type="number" v-model="changeAgentRate" />
          </a-form-model-item>
        </a-form-model>
      </a-modal>
    </template>
    <!-- 统一设置弹窗 -->
    <template>
      <a-modal v-model="isShowAllSetModal" title="统一配置产品费率" @ok="confirmSetAll">
        <a-form-model :label-col="{span: 6}" :wrapper-col="{span: 15}">
          <a-form-model-item label="状态" prop="state">
            <a-radio-group v-model="changeAllState">
              <a-radio :value="1">
                启用
              </a-radio>
              <a-radio :value="0">
                禁用
              </a-radio>
            </a-radio-group>
          </a-form-model-item>
          <a-form-model-item label="商户费率：">
            <a-input prefix="%" type="number" v-model="setAllRate" />
          </a-form-model-item>
          <a-form-model-item label="代理费率：">
            <a-input prefix="%" type="number" v-model="setAllAgentRate" />
          </a-form-model-item>
        </a-form-model>
      </a-modal>
    </template>
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
import { API_URL_PRODUCT_MCH_LIST, req } from '@/api/manage'
import { message } from 'ant-design-vue'

// eslint-disable-next-line no-unused-vars
const tableColumns = [
  { key: 'R', fixed: 'left', width: '250px', title: '商户', scopedSlots: { customRender: 'nameSlot' } },
  { key: 'agentSlot', title: '上级代理', scopedSlots: { customRender: 'agentSlot' } },
  { key: 'state', title: '状态', scopedSlots: { customRender: 'stateSlot' } },
  { key: 'mchRate', title: '商户费率', scopedSlots: { customRender: 'mchRateSlot' } },
  { key: 'agentRate', title: '代理费率', scopedSlots: { customRender: 'agentRateSlot' } },
  { key: 'op', title: '操作', width: '100px', fixed: 'right', align: 'center', scopedSlots: { customRender: 'opSlot' } }
]

export default {
  name: 'ProductMchEdit',
  components: { JeepayTable, JeepayTableColumns, JeepayTextUp },
  data () {
    return {
      visible: false, // 是否显示弹层/抽屉
      btnLoading: false,
      tableColumns: tableColumns,
      searchData: {},
      value: "''",
      selectMchProduct: {},
      changeState: 0,
      changeMchRate: 0,
      changeAgentRate: 0,
      setAllRate: 0,
      setAllAgentRate: 0,
      isShowModal: false,
      isShowAllSetModal: false,
      mchNo: '',
      mchName: '',
      productId: '',
      product: {},
      selectedIds: [],
      changeAllState: 0
    }
  },
  mounted () {
  },
  computed: {
    rowSelection () {
      const that = this
      return {
        onChange: (selectedRowKeys, selectedRows) => {
          that.selectedIds = [] // 清空选中数组
          selectedRows.forEach(function (data) { // 赋值选中参数
            that.selectedIds.push(data.mchNo)
          })
        }
      }
    }
  },
  methods: {
    show: function (product) { // 弹层打开事件
      // 查询商户所有开通的产品
      this.visible = true
      this.product = product
      this.productId = product.productId
      this.searchData.productId = product.productId
      if (this.$refs.mchProductTable !== undefined) {
        this.$refs.mchProductTable.refTable(true)
      }
      this.setAllRate = 0
      this.setAllAgentRate = 0
      this.selectedIds = []
    },
    queryFunc: function () {
      this.btnLoading = true
      this.$refs.mchProductTable.refTable(true)
    },
    // 请求table接口数据
    reqTableDataFunc: (params) => {
      return req.list(API_URL_PRODUCT_MCH_LIST, params)
    },
    handleOkFunc: function () {
      if (this.changeMchRate === undefined || this.changeMchRate === '') {
        this.changeMchRate = 0
      }
      if (this.changeAgentRate === undefined || this.changeAgentRate === '') {
        this.changeAgentRate = 0
      }
      const mchRate = Number(this.changeMchRate)
      const agentRate = Number(this.changeAgentRate)
      if (!(typeof mchRate === 'number' && !isNaN(mchRate)) || !(typeof agentRate === 'number' && !isNaN(agentRate))) {
        this.$message.error('费率设置格式错误')
        return
      }
      const params = { }
      params.productId = this.selectMchProduct.productId
      params.state = this.changeState
      params.mchRate = this.changeMchRate / 100
      params.agentRate = this.changeAgentRate / 100
      params.mchNo = this.selectMchProduct.mchNo
      req.updateById(API_URL_PRODUCT_MCH_LIST, '', params).then(res => {
        this.$refs.mchProductTable.refTable(true)
        this.$message.success('修改成功')
      })
      this.isShowModal = false
    },
    editFunc: function (record) {
      this.selectMchProduct = { }
      this.changeState = record.state
      this.changeMchRate = (record.mchRate * 100).toFixed(2)
      this.changeAgentRate = (record.agentRate * 100).toFixed(2)
      this.selectMchProduct.productId = record.productId
      this.selectMchProduct.productName = record.productName
      this.selectMchProduct.mchNo = record.mchNo
      this.selectMchProduct.mchName = record.mchName
      this.isShowModal = true
    },
    blindAll () {
      this.btnLoading = true
      req.postNormal(API_URL_PRODUCT_MCH_LIST + '/blindAll', this.productId).then(res => {
        setTimeout(() => {
          this.btnLoading = false
          this.$refs.mchProductTable.refTable(true)
          this.$message.success('修改成功')
        }, 500) // 1000毫秒等于1秒
      })
    },
    unBlindAll () {
      this.btnLoading = true
      req.postNormal(API_URL_PRODUCT_MCH_LIST + '/unBlindAll', this.productId).then(res => {
        setTimeout(() => {
          this.btnLoading = false
          this.$refs.mchProductTable.refTable(true)
          this.$message.success('修改成功')
        }, 500) // 1000毫秒等于1秒
      })
    },
    setAllMch () {
      if (this.selectedIds.length === 0) {
        message.error('请先选择要配置的商户')
        return
      }
      this.isShowAllSetModal = true
      this.setAllRate = 0
      this.setAllAgentRate = 0
    },
    confirmSetAll () {
      this.btnLoading = true
      const that = this
      const params = { }
      params.setAllRate = this.setAllRate / 100
      params.setAllAgentRate = this.setAllAgentRate / 100
      params.selectedIds = this.selectedIds
      params.changeAllState = this.changeAllState
      req.postDataNormal(API_URL_PRODUCT_MCH_LIST + '/setAllRate', this.productId, params).then(res => {
        that.setAllRate = 0
        that.setAllAgentRate = 0
        setTimeout(() => {
          that.btnLoading = false
          that.isShowAllSetModal = false
          that.$refs.mchProductTable.refTable(true)
          that.$message.success('修改成功')
        }, 500) // 1000毫秒等于1秒
      })
    },
    onClose () {
      this.visible = false
      this.selectedIds = []
      this.searchData = {}
    }
  }
}
</script>
