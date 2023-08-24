<template>
  <div style="background: #fff;border-radius:10px">
    <a-tabs @change="selectTabs">
      <a-tab-pane key="1" tab="基本信息">
        <div class="account-settings-info-view">
          <a-row :gutter="16">
            <a-col :md="16" :lg="16">
              <a-form-model ref="infoFormModel" :model="saveObject" :label-col="{span: 9}" :wrapper-col="{span: 10}">
                <a-form-model-item label="用户登录名:">
                  <a-input v-model="saveObject.loginUsername" disabled/>
                </a-form-model-item>
                <a-form-model-item label="开启谷歌验证：">
                  <a-radio-group v-model="googleAuth" @change="googleAuthChange">
                    <a-radio :value="1">启用</a-radio>
                    <a-radio :value="0">禁用</a-radio>
                  </a-radio-group>
                </a-form-model-item>
                <a-form-model-item label="请使用Google身份验证器扫描码：" v-show="showGoogleQrCode">
                  <GoogleAuthQrCode ref="googleAuthQrCode"/>
                </a-form-model-item>
                <a-form-model-item label="Google验证码：" v-show="showGoogleQrCode">
                  <a-input type="number" v-model="googleCode"></a-input>
                </a-form-model-item>
                <a-form-model-item label="Google验证码：" v-show="showGoogleTryCancel">
                  <a-input type="number" v-model="googleCode"></a-input>
                </a-form-model-item>
              </a-form-model>
              <a-form-item style="display:flex;justify-content:center">
                <a-button type="primary" @click="changeInfo" icon="check-circle" :disabled="canUpdate" :loading="btnLoading">确认更新</a-button>
              </a-form-item>
            </a-col>
          </a-row>
        </div>
      </a-tab-pane>
      <a-tab-pane key="2" tab="密码设置">
        <div class="account-settings-info-view">
          <a-row :gutter="16">
            <a-col :md="16" :lg="16">
              <a-form-model ref="pwdFormModel" :model="updateObject" :label-col="{span: 9}" :wrapper-col="{span: 10}" :rules="rulesPass">
                <a-form-model-item label="原密码：" prop="originalPwd">
                  <a-input-password autocomplete="new-password" v-model="updateObject.originalPwd" placeholder="请输入原密码" />
                </a-form-model-item>
                <a-form-model-item label="新密码：" prop="newPwd">
                  <a-input-password autocomplete="new-password" v-model="updateObject.newPwd" placeholder="请输入新密码" />
                </a-form-model-item>
                <a-form-model-item label="确认新密码：" prop="confirmPwd">
                  <a-input-password autocomplete="new-password" v-model="updateObject.confirmPwd" placeholder="确认新密码" />
                </a-form-model-item>
              </a-form-model>
              <a-form-item style="display:flex;justify-content:center">
                <a-button type="primary" icon="safety-certificate" @click="confirm" :loading="btnLoading">更新密码</a-button>
              </a-form-item>
            </a-col>
          </a-row>
        </div>
      </a-tab-pane>
    </a-tabs>
  </div>
</template>
<script>
import JeepayUpload from '@/components/JeepayUpload/JeepayUpload'
import { getGoogleAuthInfo, getUserInfo, updateUserInfo, updateUserPass, upload } from '@/api/manage'
import AvatarModal from './AvatarModal'
import store from '@/store'
import { getInfo } from '@/api/login'
import { Base64 } from 'js-base64'
import GoogleAuthQrCode from '@/views/user/GoogleAuthQrCode.vue'
export default {
  components: {
    AvatarModal,
    JeepayUpload,
    GoogleAuthQrCode
  },
  data () {
    return {
      action: upload.avatar, // 上传图标地址
      btnLoading: false,
      saveObject: {
        loginUsername: '' // 登录名
      },
      googleCode: '',
      googleAuth: 0,
      googleKey: '',
      originalGoogleAuth: 0,
      showGoogleTryCancel: false,
      canUpdate: true,
      // avatarUrl: store.state.user.avatarImgPath,
      updateObject: {
        originalPwd: '', // 原密码
        newPwd: '', //  新密码
        confirmPwd: '' //  确认密码
      },
      showGoogleQrCode: false,
      recordId: store.state.user.userId, // 拿到ID
      rulesPass: {
        originalPwd: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
        newPwd: [{ min: 6, max: 12, required: true, message: '请输入6-12位新密码', trigger: 'blur' }],
        confirmPwd: [{ required: true, message: '请确认输入新密码', trigger: 'blur' }, {
          validator: (rule, value, callBack) => {
            this.updateObject.newPwd === value ? callBack() : callBack('新密码与确认密码不一致')
          }
        }]
      }

    }
  },
  computed: {

  },
  created () {
    this.detail()
  },
  methods: {
    detail () { // 获取基本信息
      const that = this
      getUserInfo().then(res => {
        that.saveObject = res
        that.googleAuth = res.googleAuth
        that.originalGoogleAuth = res.googleAuth
      })
    },
    googleAuthChange () {
      const that = this
      if (this.googleAuth === 1 && this.originalGoogleAuth === 0) { // 原来没打开，现在要打开
        that.$store.commit('showLoading') // 关闭全局刷新
        getGoogleAuthInfo().then(res => {
          that.$store.commit('hideLoading') // 关闭全局刷新
          that.$refs.googleAuthQrCode.show(res.qrCode)
          that.googleKey = res.key
          that.showGoogleQrCode = true
          that.showGoogleTryCancel = false
          that.canUpdate = false
        })
      } else if (this.originalGoogleAuth === 1 && this.googleAuth === 0) { // 原来已绑定，现在要关闭
        that.showGoogleTryCancel = true
        that.googleKey = ''
        that.canUpdate = false
      } else {
        that.showGoogleQrCode = false
        that.googleCode = ''
        that.canUpdate = true
        that.showGoogleTryCancel = false
      }
    },
    changeInfo () { // 更新基本信息事件  更新前先检查之前是否开启谷歌-需要关闭时验证
      const that = this
      this.$refs.infoFormModel.validate(valid => {
        if (valid) { // 验证通过
          this.$infoBox.confirmPrimary('确认更新信息吗？', '', () => {
            that.btnLoading = true // 打开按钮上的 loading
            that.$store.commit('showLoading') // 关闭全局刷新
            // 请求接口
            that.saveObject.googleAuth = that.googleAuth
            that.saveObject.googleKey = that.googleKey
            that.saveObject.googleCode = that.googleCode
            updateUserInfo(that.saveObject).then(res => {
              that.btnLoading = false // 关闭按钮刷新  同步最新用户信息
              that.showGoogleQrCode = false
              that.googleCode = ''
              that.canUpdate = true
              that.showGoogleTryCancel = false
              that.$store.commit('hideLoading') // 关闭全局刷新
              return getInfo()
            }).then(bizData => {
              store.commit('SET_USER_INFO', bizData) // 调用vuex设置用户基本信息
              that.originalGoogleAuth = bizData.googleAuth
              that.$message.success('修改成功')
            }).catch(err => {
              that.$store.commit('hideLoading') // 关闭全局刷新
              that.btnLoading = false
              console.log(err)
              that.$message.error('修改失败')
            })
          })
        }
      })
    },
    confirm (e) { // 确认更新密码
      const that = this
      this.$refs.pwdFormModel.validate(valid => {
        if (valid) { // 验证通过
          this.$infoBox.confirmPrimary('确认更新密码吗？', '', () => {
            // 请求接口
            that.btnLoading = true // 打开按钮上的 loading
            that.confirmLoading = true // 显示loading
            that.updateObject.recordId = that.recordId // 用户ID
            that.updateObject.originalPwd = Base64.encode(that.updateObject.originalPwd)
            that.updateObject.confirmPwd = Base64.encode(that.updateObject.confirmPwd)
            this.$delete(this.updateObject, 'newPwd')
            updateUserPass(that.updateObject).then(res => {
              that.$message.success('修改成功')
              // 退出登录
              this.$store.dispatch('Logout').then(() => {
                this.$router.push({ name: 'login' })
              })
            }).catch(res => {
              that.confirmLoading = false
              that.btnLoading = false
            })
          })
        }
      })
    },
    selectTabs () { // 清空必填提示
      this.updateObject.originalPwd = ''
      this.updateObject.newPwd = ''
      this.updateObject.confirmPwd = ''
      console.log(this.updateObject)
    },
    // 上传文件成功回调方法，参数value为文件地址，name是自定义参数
    uploadSuccess (value, name) {
      this.saveObject.avatarUrl = value
      this.$forceUpdate()
    }
  }
}

</script>
<style lang="less" scoped>

.avatar-upload-wrapper {
  height: 200px;
  width: 100%;
}

.ant-upload-preview {
  text-align:center ;
  position: relative;
  margin: 0 auto;
  width: 100%;
  // max-width: 180px;
  border-radius: 50%;
  // box-shadow: 0 0 4px #ccc;

  .upload-icon {
    position: absolute;
    top: 0;
    right: 10px;
    font-size: 1.4rem;
    padding: 0.5rem;
    background: rgba(222, 221, 221, 0.7);
    border-radius: 50%;
    border: 1px solid rgba(0, 0, 0, 0.2);
  }
  .mask {
    opacity: 0;
    position: absolute;
    background: rgba(0,0,0,0.4);
    cursor: pointer;
    transition: opacity 0.4s;

    &:hover {
      opacity: 1;
    }

    i {
      font-size: 2rem;
      position: absolute;
      top: 50%;
      left: 50%;
      margin-left: -1rem;
      margin-top: -1rem;
      color: #d6d6d6;
    }
  }

  img, .mask {
    width: 150px;
    height: 150px;
    border-radius: 50%;
    overflow: hidden;
  }
}
</style>
