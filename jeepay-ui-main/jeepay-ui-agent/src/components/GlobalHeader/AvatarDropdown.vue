<template>
  <div>
    <a-dropdown placement="bottomRight">
    <span class="ant-pro-account-avatar">
      <b style="margin-right: 10px;font-size: 15px">{{ currentUserName }}</b>
      <a-icon type="user" class="circle" :style="{fontSize: '32px', color: '#1a53ff'}"></a-icon>
    </span>
      <template v-slot:overlay>
        <a-menu class="ant-pro-drop-down menu" :selected-keys="[]">
          <a-menu-item key="settings" @click="handleToSettings">
            <a-icon type="setting"/>
            安全设置
          </a-menu-item>
          <a-menu-divider/>
          <a-menu-item key="logout" @click="handleLogout">
            <a-icon type="logout"/>
            退出登录
          </a-menu-item>
        </a-menu>
      </template>
    </a-dropdown>
  </div>
</template>

<script>

import { UserOutlined } from '@ant-design/icons-vue'
export default {
  name: 'AvatarDropdown',
  props: {},
  data: function () {
    return {
      greetImg: require('@/assets/logo.svg') // 头像图片地址
    }
  },
  components: {
    UserOutlined
  },
  computed: {
    // 返回用户名
    currentUserName () {
      return this.$store.state.user.loginUsername
    }
  },
  methods: {
    handleToSettings () {
      this.$router.push({ name: 'ENT_C_USERINFO' })
    },
    handleLogout: function (e) {
      this.$infoBox.confirmPrimary('确认退出?', '', () => {
        this.$store.dispatch('Logout').then(() => {
          this.$router.push({ name: 'login' })
        })
      })
    }
  }

}
</script>

<style lang="less" scoped>
.ant-pro-drop-down {
  /deep/ .action {
    margin-right: 8px;
  }

  /deep/ .ant-dropdown-menu-item {
    min-width: 160px;
  }
}
.circle{
  border: #1a53ff 2px solid;
  border-radius: 16px;
}
</style>
