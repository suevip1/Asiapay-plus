<template>
  <a-config-provider :locale="locale">
    <div id="app">
      <router-view/>
      <loading v-show="globalLoading"></loading>
    </div>
  </a-config-provider>
</template>

<script>
import zhCN from 'ant-design-vue/lib/locale-provider/zh_CN'
import Loading from './components/GlobalLoad/GlobalLoad' // 全局Loading组件
import { mapState } from 'vuex' // 引入vuex状态管理，mapState管理中存在全局loading
import { getTitle } from '@/api/login'
import storage from '@/utils/jeepayStorageWrapper'
export default {
  data () {
    return {
      locale: zhCN
    }
  },
  components: {
    Loading // 注册全局loading 组件
  },
  mounted () {
    getTitle().then(res => {
      if (res !== '') {
        storage.setPlatName(res)
      } else {
        storage.setPlatName('亚洲四方科技')
      }
    })
  },
  methods: {
  },
  computed: {
    // 全局 loading
		...mapState([
			'globalLoading'
		])
	}
}
</script>
