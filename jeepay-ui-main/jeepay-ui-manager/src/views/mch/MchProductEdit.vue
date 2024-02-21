<template>
  <a-drawer
      :visible="visible"
      :title=" true ? '商户-支付产品配置' : '' "
      @close="onClose"
      :body-style="{ paddingBottom: '80px' }"
      width="60%"
  >
      <div class="table-page-search-wrapper" @keydown.enter="queryFunc">
        <a-form layout="inline" class="table-head-ground">
          <a-row justify="space-between" type="flex">
            <a-col :sm="12">
              <a-descriptions>
                <a-descriptions-item label="商户号">
                  <b style="color: #1a53ff">{{ mchNo }}</b>
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
            <a-col :sm="12">
              <a-descriptions>
                <a-descriptions-item label="商户代理">
                  <span style="color: #1a53ff">{{ mchInfo.agentNo==''?'无':mchInfo.agentNo }}</span>
                </a-descriptions-item>
              </a-descriptions>
            </a-col>
            <a-col :sm="12">
              <a-descriptions>
                <a-descriptions-item label="代理名称">
                  {{ (mchInfo.agentName ==undefined || mchInfo.agentName == '')?'无代理':mchInfo.agentName }}
                </a-descriptions-item>
              </a-descriptions>
            </a-col>
          </a-row>
          <br/>
          <jeepay-text-up :placeholder="'产品ID'" :msg="searchData.productId" v-model="searchData.productId"/>
          <jeepay-text-up :placeholder="'产品名'" :msg="searchData.productName" v-model="searchData.productName"/>
          <span class="table-page-search-submitButtons" style="flex-grow: 0; flex-shrink: 0;">
            <a-button type="primary" icon="search" @click="queryFunc" :loading="btnLoading">查询</a-button>
            <a-button style="margin-left: 8px" icon="reload" @click="() => this.searchData = {  'mchNo' : mchNo}">重置</a-button>
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
              <a-button type="dashed" style="margin-left: 8px" icon="retweet"  :loading="btnLoading" @click="setAllMch">批量配置</a-button>
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
          rowKey="productId"
      >
        <template slot="nameSlot" slot-scope="{record}">
          <b style="font-weight: bold;color: #1a53ff" >[{{ record.productId }}]</b>&nbsp;<b>{{ record.productName.trim()}}</b>
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
      <a-modal v-model="isShowModal" title="支付产品配置" @ok="handleOkFunc">
        <a-form-model ref="infoFormModel" :label-col="{span: 6}" :wrapper-col="{span: 15}">
          <a-form-model-item label="商户信息：" >
            <b style="color: #1a53ff">[{{mchNo}}]</b>&nbsp;<span>&nbsp;{{mchName}}</span>
          </a-form-model-item>
          <a-form-model-item label="产品信息：">
            <b style="color: #1a53ff">[{{selectMchProduct.productId}}]</b><span>&nbsp;{{selectMchProduct.productName}}</span>
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
      <a-modal v-model="isShowAllSetModal" title="批量配置产品" @ok="confirmSetAll">
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
          <a-form-model-item label="修改选项：" >
            <a-checkbox-group v-model="setEnableItem" @change="onMultipleChange">
              <a-checkbox value="1" name="type">商户费率</a-checkbox>
              <a-checkbox value="2" name="type">代理费率</a-checkbox>
            </a-checkbox-group>
            <br />
            <b style="color: rgb(128,128,128)">请勾选需要批量操作的选项!</b>
          </a-form-model-item>
          <a-form-model-item label="商户费率：">
            <a-input prefix="%" type="number" v-model="setAllRate" :disabled="enableSetMch" />
          </a-form-model-item>
          <a-form-model-item label="代理费率：">
            <a-input prefix="%" type="number" v-model="setAllAgentRate" :disabled="enableSetAgent" />
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
import { API_URL_MCH_PRODUCT_LIST, req } from '@/api/manage'
import { message } from 'ant-design-vue'

// eslint-disable-next-line no-unused-vars
const tableColumns = [
  { key: 'nameSlot', fixed: 'left', width: '450px', title: '支付产品', scopedSlots: { customRender: 'nameSlot' } },
  { key: 'state', title: '状态', width: '100px', scopedSlots: { customRender: 'stateSlot' } },
  { key: 'mchRate', title: '商户费率', scopedSlots: { customRender: 'mchRateSlot' } },
  { key: 'agentRate', title: '代理费率', scopedSlots: { customRender: 'agentRateSlot' } },
  { key: 'op', title: '操作', width: '100px', fixed: 'right', align: 'center', scopedSlots: { customRender: 'opSlot' } }
]

export default {
  name: 'MchProductEdit',
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
      isShowModal: false,
      mchNo: '',
      mchName: '',
      mchInfo: {},
      selectedIds: [],
      changeAllState: '',
      isShowAllSetModal: false,
      setAllRate: 0,
      setAllAgentRate: 0,
      setEnableItem: [],
      enableSetMch: true,
      enableSetAgent: true
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
            that.selectedIds.push(data.productId)
          })
        }
      }
    }
  },
  watch: {
    isShowAllSetModal: function (o, n) {
      if (n) {
        this.resetMultipleSet()
      }
    }
  },
  methods: {
    show: function (record) { // 弹层打开事件
      // 查询商户所有开通的产品
      this.visible = true
      this.mchInfo = record
      this.mchNo = record.mchNo
      this.mchName = record.mchName
      this.searchData.mchNo = record.mchNo
      this.resetMultipleSet()
      this.selectedIds = []
      if (this.$refs.mchProductTable !== undefined) {
        this.$refs.mchProductTable.refTable(true)
      }
    },
    queryFunc: function () {
      this.btnLoading = true
      this.$refs.mchProductTable.refTable(true)
    },
    // 请求table接口数据
    reqTableDataFunc: (params) => {
      return req.list(API_URL_MCH_PRODUCT_LIST, params)
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
      console.log(mchRate)
      console.log(agentRate)
      if (!(typeof mchRate === 'number' && !isNaN(mchRate)) || !(typeof agentRate === 'number' && !isNaN(agentRate))) {
        this.$message.error('费率设置格式错误')
        return
      }
      const params = { }
      params.productId = this.selectMchProduct.productId
      params.state = this.changeState
      params.mchRate = this.changeMchRate / 100
      params.agentRate = this.changeAgentRate / 100
      params.mchNo = this.mchNo
      req.updateById(API_URL_MCH_PRODUCT_LIST, '', params).then(res => {
        this.$refs.mchProductTable.refTable()
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
      this.isShowModal = true
    },
    blindAll () {
      this.btnLoading = true
      req.postNormal(API_URL_MCH_PRODUCT_LIST + '/blindAll', this.mchNo).then(res => {
        setTimeout(() => {
          this.btnLoading = false
          this.$refs.mchProductTable.refTable(true)
          this.$message.success('修改成功')
        }, 500) // 1000毫秒等于1秒
      })
    },
    unBlindAll () {
      this.btnLoading = true
      req.postNormal(API_URL_MCH_PRODUCT_LIST + '/unBlindAll', this.mchNo).then(res => {
        setTimeout(() => {
          this.btnLoading = false
          this.$refs.mchProductTable.refTable(true)
          this.$message.success('修改成功')
        }, 500) // 1000毫秒等于1秒
      })
    },
    onClose () {
      this.visible = false
      this.selectedIds = []
      this.searchData = {}
    },
    setAllMch () {
      if (this.selectedIds.length === 0) {
        message.error('请先选择要配置的产品')
        return
      }
      this.isShowAllSetModal = true
      this.resetMultipleSet()
    },
    confirmSetAll () {
      this.btnLoading = true
      const that = this
      const params = { }
      if (!this.enableSetMch) {
        params.setAllRate = this.setAllRate / 100
      }
      if (!this.enableSetAgent) {
        params.setAllAgentRate = this.setAllAgentRate / 100
      }
      params.selectedIds = this.selectedIds
      params.changeAllState = this.changeAllState
      that.isShowAllSetModal = false
      req.postDataNormal(API_URL_MCH_PRODUCT_LIST + '/setAllRate', this.mchNo, params).then(res => {
        that.resetMultipleSet()
        setTimeout(() => {
          that.btnLoading = false
          that.$refs.mchProductTable.refTable()
          that.$message.success('修改成功')
        }, 500) // 1000毫秒等于1秒
      })
    },
    onMultipleChange () {
      this.enableSetMch = !this.setEnableItem.includes('1')
      if (this.enableSetMch) {
        this.setAllRate = 0
      }
      this.enableSetAgent = !this.setEnableItem.includes('2')
      if (this.enableSetAgent) {
        this.setAllAgentRate = 0
      }
    },
    resetMultipleSet () {
      this.enableSetMch = true
      this.setAllRate = 0
      this.enableSetAgent = true
      this.setAllAgentRate = 0
      this.changeAllState = ''
      this.setEnableItem = []
    }
  }
}
</script>
