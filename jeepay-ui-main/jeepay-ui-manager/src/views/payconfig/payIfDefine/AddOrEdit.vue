<template>
  <a-drawer
      :visible="visible"
      :title=" isAdd ? '新增支付接口' : '修改支付接口'"
      width="40%"
      :maskClosable="false"
      @close="onClose">

    <a-form-model ref="infoFormModel" :model="saveObject" layout="vertical" :rules="rules">

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-model-item label="接口代码" prop="ifCode">
            <a-input v-model="saveObject.ifCode" placeholder="请输入" :disabled="!isAdd"/>
          </a-form-model-item>
        </a-col>
        <a-col :span="12">
          <a-form-model-item label="接口名称" prop="ifName">
            <a-input v-model="saveObject.ifName" placeholder="请输入"/>
          </a-form-model-item>
        </a-col>
        <a-col :span="24">
          <a-form-model-item label="接口配置定义描述" prop="normalMchParams">
            <a-input v-model="saveObject.ifParams" placeholder="请输入" type="textarea"/>
          </a-form-model-item>
        </a-col>
        <a-col :span="24">
          <a-form-model-item label="状态" prop="state">
            <a-radio-group v-model="saveObject.state">
              <a-radio :value="1">
                启用
              </a-radio>
              <a-radio :value="0">
                停用
              </a-radio>
            </a-radio-group>
          </a-form-model-item>
        </a-col>
        <a-col :span="12">
          <a-form-model-item label="备注" prop="remark">
            <a-input v-model="saveObject.remark" placeholder="请输入"/>
          </a-form-model-item>
        </a-col>
        <a-col :span="12">
          <a-form-model-item v-if="!isAdd" label="页面展示：卡片背景色" prop="bgColor">
            <a-input v-model="saveObject.bgColor" placeholder="请输入"/>
          </a-form-model-item>
        </a-col>
      </a-row>
    </a-form-model>

    <div class="drawer-btn-center">
      <a-button @click="onClose" icon="close" :style="{ marginRight: '8px' }">取消</a-button>
      <a-button type="primary" @click="onSubmit" icon="check">保存</a-button>
    </div>

  </a-drawer>
</template>

<script>
import JeepayUpload from '@/components/JeepayUpload/JeepayUpload'
import { API_URL_IFDEFINES_LIST, req } from '@/api/manage'

export default {
  components: {
    JeepayUpload
  },
  props: {
    callbackFunc: { type: Function, default: () => () => ({}) }
  },

  data () {
    const validateNormalMchParams = (rule, value, callback) => { // 普通商户接口配置定义描述 验证器
      callback()
    }
    return {
      isAdd: true, // 新增 or 修改
      visible: false, // 抽屉开关
      ifCode: '', // 支付接口定义id
      saveObject: {}, // 数据对象
      rules: {
        ifCode: [{ required: true, message: '请输入接口代码', trigger: 'blur' }],
        ifName: [{ required: true, message: '请输入接口名称', trigger: 'blur' }],
        normalMchParams: [{ validator: validateNormalMchParams, trigger: 'blur' }]
      }
    }
  },
  created () {
  },
  methods: {
    // 抽屉显示
    show (ifCode) {
      this.isAdd = !ifCode
      // 数据清空
      this.saveObject = {
        'state': 1
      }

      if (this.$refs.infoFormModel !== undefined) {
        this.$refs.infoFormModel.resetFields()
      }

      const that = this
      if (!this.isAdd) { // 修改信息 延迟展示弹层
        that.ifCode = ifCode
        // 拉取详情
        req.getById(API_URL_IFDEFINES_LIST, ifCode).then(res => {
          that.saveObject = res
        })
        this.visible = true
      } else {
        that.visible = true // 展示弹层信息
      }
    },
    onClose () {
      this.visible = false
    },
    // 表单提交
    onSubmit () {
      const that = this
      this.$refs.infoFormModel.validate(valid => {
        if (valid) { // 验证通过
          // 请求接口
          if (that.isAdd) {
            that.saveObject.bgColor = that.generateRandomColor()
            req.add(API_URL_IFDEFINES_LIST, that.saveObject).then(res => {
              that.$message.success('新增成功')
              that.visible = false
              that.callbackFunc() // 刷新列表
            })
          } else {
            req.updateById(API_URL_IFDEFINES_LIST, that.ifCode, that.saveObject).then(res => {
              that.$message.success('修改成功')
              that.visible = false
              that.callbackFunc() // 刷新列表
            })
          }
        }
      })
    },
    // 生成随机颜色
    generateRandomColor () {
      const letters = '0123456789ABCDEF'
      let color = '#'
      for (let i = 0; i < 6; i++) {
        color += letters[Math.floor(Math.random() * 16)]
      }
      return color
    }
  }
}
</script>

<style lang="less" scoped>
</style>
