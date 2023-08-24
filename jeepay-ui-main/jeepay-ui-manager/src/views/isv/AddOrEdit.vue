<template>
  <a-drawer
    :maskClosable="true"
    :visible="visible"
    :title=" isAdd ? '新增代理商' : '修改代理商' "
    @close="onClose"
    :body-style="{ paddingBottom: '80px' }"
    width="40%"
  >
    <a-form-model ref="infoFormModel" :model="saveObject" layout="vertical" :rules="rules">
      <a-row justify="space-between" type="flex">
        <a-col :span="10">
          <a-form-model-item label="代理商名称" prop="agentName">
            <a-input
              placeholder="请输入代理商名称"
              v-model="saveObject.agentName"
            />
          </a-form-model-item>
        </a-col>
        <a-col :span="10">
          <a-form-model-item label="登录名" prop="loginUserName">
            <a-input
                placeholder="请输入代理商登录名"
                v-model="saveObject.loginUserName"
                :disabled="!this.isAdd"
            />
          </a-form-model-item>
        </a-col>
      </a-row>
      <a-row justify="space-between" type="flex">
        <a-col :span="10">
          <a-form-model-item label="状态" prop="state">
            <a-radio-group v-model="saveObject.state" :defaultValue="1">
              <a-radio :value="1">
                启用
              </a-radio>
              <a-radio :value="0">
                禁用
              </a-radio>
            </a-radio-group>
          </a-form-model-item>
        </a-col>
      </a-row>
      <a-row justify="space-between" type="flex">
        <a-col :span="24">
          <a-form-model-item label="备注" prop="remark">
            <a-input v-model="saveObject.remark" placeholder="请输入备注" type="textarea" />
          </a-form-model-item>
        </a-col>
      </a-row>
      <!-- 重置密码板块 -->
      <a-row justify="space-between" type="flex">
        <a-col :span="24">
          <a-divider orientation="left" v-if="resetIsShow">
            <a-tag color="#FF4B33">
              账户安全
            </a-tag>
          </a-divider>
        </a-col>
      </a-row>
      <div>
        <a-row justify="space-between" type="flex">
          <a-col :span="10">
            <a-form-model-item label="" v-if="resetIsShow" >
              重置密码：<a-checkbox v-model="sysPassword.resetPass"></a-checkbox>
            </a-form-model-item>
          </a-col>
          <a-col :span="10">
            <a-form-model-item label="" v-if="sysPassword.resetPass">
              恢复默认密码：<a-checkbox v-model="sysPassword.defaultPass" @click="isResetPass"></a-checkbox>
            </a-form-model-item>
          </a-col>
        </a-row>
      </div>
      <div v-if="sysPassword.resetPass">
        <!-- <div v-else> -->
        <div v-show="!this.sysPassword.defaultPass">
          <a-row justify="space-between" type="flex">
            <a-col :span="10">
              <a-form-model-item label="新密码：" prop="newPwd">
                <a-input-password autocomplete="new-password" v-model="newPwd" :disabled="sysPassword.defaultPass"/>
              </a-form-model-item>
            </a-col>

            <a-col :span="10">
              <a-form-model-item label="确认新密码：" prop="confirmPwd">
                <a-input-password autocomplete="new-password" v-model="sysPassword.confirmPwd" :disabled="sysPassword.defaultPass"/>
              </a-form-model-item>
            </a-col>
          </a-row>
        </div>
      </div>
    </a-form-model>
    <div class="drawer-btn-center">
      <a-button icon="close" @click="onClose" style="margin-right:8px">
        取消
      </a-button>
      <a-button type="primary" style="margin-right:8px" icon="check" @click="handleOkFunc" :loading="btnLoading">
        保存
      </a-button>
    </div>
  </a-drawer>

</template>

<script>
import { API_URL_ISV_LIST, req } from '@/api/manage'
import { Base64 } from 'js-base64'
export default {

  props: {
    callbackFunc: { type: Function }
  },

  data () {
    return {
      newPwd: '', //  新密码
      btnLoading: false,
      resetIsShow: false, // 重置密码是否展现
      sysPassword: {
        resetPass: false, // 重置密码
        defaultPass: true, // 使用默认密码
        confirmPwd: '' //  确认密码
      },
      isAdd: true, // 新增 or 修改页面标志
      saveObject: {}, // 数据对象
      recordId: null, // 更新对象ID
      visible: false, // 是否显示弹层/抽屉
      rules: {
        agentName: [ { required: true, message: '请输入代理商名称', trigger: 'blur' } ],
        loginUserName: [{ required: true, pattern: /^[a-zA-Z][a-zA-Z0-9]{5,17}$/, message: '请输入字母开头，长度为6-18位的登录名', trigger: 'blur' }],
        newPwd: [{ required: false, trigger: 'blur' }, {
          validator: (rule, value, callBack) => {
            if (!this.sysPassword.defaultPass) {
              if (this.newPwd.length < 6 || this.newPwd.length > 12) {
                callBack('请输入6-12位新密码')
              }
            }
            callBack()
          }
        }], // 新密码
        confirmPwd: [{ required: false, trigger: 'blur' }, {
          validator: (rule, value, callBack) => {
            if (!this.sysPassword.defaultPass) {
              this.newPwd === this.sysPassword.confirmPwd ? callBack() : callBack('新密码与确认密码不一致')
            } else {
              callBack()
            }
          }
        }] // 确认新密码
      }
    }
  },
  created () {
  },
  methods: {
    show: function (recordId) { // 弹层打开事件
      this.isAdd = !recordId
      this.saveObject = { 'state': 1 } // 数据清空

      if (this.$refs.infoFormModel !== undefined) {
        this.$refs.infoFormModel.resetFields()
      }

      const that = this
      if (!this.isAdd) { // 修改信息 延迟展示弹层
        that.resetIsShow = true // 展示重置密码板块
        that.recordId = recordId
        req.getById(API_URL_ISV_LIST, recordId).then(res => {
          that.saveObject = res
          })
        this.visible = true
      } else {
        that.visible = true // 立马展示弹层信息
      }
    },

    handleOkFunc: function () { // 点击【确认】按钮事件
        const that = this
        this.$refs.infoFormModel.validate(valid => {
          if (valid) { // 验证通过
            that.btnLoading = true
            // 请求接口
            if (that.isAdd) {
              req.add(API_URL_ISV_LIST, that.saveObject).then(res => {
                that.$message.success('新增成功')
                that.visible = false
                that.callbackFunc() // 刷新列表
                that.btnLoading = false
              }).catch(res => {
                that.btnLoading = false
              })
            } else {
              that.sysPassword.confirmPwd = Base64.encode(that.sysPassword.confirmPwd)
              console.log(that.sysPassword.confirmPwd)
              Object.assign(that.saveObject, that.sysPassword) // 拼接对象
              console.log(that.saveObject)
              req.updateById(API_URL_ISV_LIST, that.recordId, that.saveObject).then(res => {
                that.$message.success('修改成功')
                that.visible = false
                that.callbackFunc() // 刷新列表
                that.btnLoading = false
                that.resetIsShow = true // 展示重置密码板块
                that.sysPassword.resetPass = false
                that.sysPassword.defaultPass = true	// 是否使用默认密码默认为true
                that.resetPassEmpty(that) // 清空密码
              }).catch(res => {
                that.btnLoading = false
                that.resetIsShow = true // 展示重置密码板块
                that.sysPassword.resetPass = false
                that.sysPassword.defaultPass = true	// 是否使用默认密码默认为true
                that.resetPassEmpty(that) // 清空密码
              })
            }
          }
        })
    },
    onClose () {
      this.visible = false
    },
    searchFunc: function () { // 点击【查询】按钮点击事件
      this.$refs.infoTable.refTable(true)
    },
    // 使用默认密码重置是否为true
    isResetPass () {
      if (!this.sysPassword.defaultPass) {
        this.newPwd = ''
        this.sysPassword.confirmPwd = ''
      }
    },
    // 保存后清空密码
    resetPassEmpty (that) {
      that.newPwd = ''
      that.sysPassword.confirmPwd = ''
    }
  }
}
</script>
