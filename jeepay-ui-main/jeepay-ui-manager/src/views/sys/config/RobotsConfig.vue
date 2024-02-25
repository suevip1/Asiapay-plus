<template>
  <div style="background: #fff">
    <a-tabs
        @change="selectTabs"
    >
      <a-tab-pane key="applicationConfig" tab="机器人配置">
        <div class="account-settings-info-view">
          <a-row>
            <a-col :span="8" :offset="1"><b>机器人帮助说明：</b></a-col>
          </a-row>
          <br/>
          <a-row>
            <a-col :span="23" :offset="1"><b style="color: black">优先绑定管理群，管理群用于接收预警、提示等！管路群、商户群、通道群每个群只能同时绑定一种类型，管理群唯一</b></a-col>
          </a-row>
          <a-row>
            <a-col :span="23" :offset="1"><b style="color: black">后台只包含四方工作人员部分命令,其他命令直接拉机器人进去 <b style="color: red">/help</b> 实时查看</b></a-col>
          </a-row>
          <br/>
          <a-row>
            <a-col :span="23" :offset="1">1. 先将机器人添加到群组内，然后给机器人<b>管理员</b>权限。</a-col>
          </a-row>
          <a-row>
            <a-col :span="23" :offset="1">2. 管理员在系统后台配置，管理员可以设置其他账号成为操作员，机器人只会执行管理员和操作员发送的下发、记账等敏感指令。</a-col>
          </a-row>
          <a-row>
            <a-col :span="23" :offset="1">3. 下发数据按天统计，数据只保留<b>昨日</b>和<b>今日</b>两天的数据。</a-col>
          </a-row>
          <a-row>
            <a-col :span="23" :offset="1">4. 查单功能需要先绑定好商户群以及通道群后才能正确识别、转发</a-col>
          </a-row>
          <a-row>
            <a-col :span="23" :offset="1">5. 补单预警以及异常订单预警均为<b>每分钟</b>检测一次，可在下方自由设置预警阈值</a-col>
          </a-row>
          <br/>
          <a-row>
            <a-col :span="8" :offset="1"><b>机器人使用说明：</b></a-col>
          </a-row>
          <br/>
          <a-row>
            <a-col :span="23" :offset="1"><b>绑定管理群</b>--管理员发送，在不同群重复发送即可换绑，管理群用于接受预警相关提醒</a-col>
          </a-row>
          <a-row>
            <a-col :span="23" :offset="1"><b>设置操作员</b>--格式[设置操作员 @xxx] (如：设置操作员 @test @后面是用户名) 管理员发送，不同的群可通用只需要设置一次，下发、记账操作需要操作员权限</a-col>
          </a-row>
          <a-row>
            <a-col :span="23" :offset="1"><b>删除操作员</b>--格式[删除操作员 @xxx] (如：设置操作员 @test @后面是用户名) 管理员发送，下发、记账操作需要操作员权限</a-col>
          </a-row>
          <a-row>
            <a-col :span="23" :offset="1"><b>操作员名单</b>--格式[操作员名单]管理员发送，查看操作员名单</a-col>
          </a-row>
          <br/>
          <a-row>
            <a-col :span="23" :offset="1"><b>绑定商户</b>--格式[绑定商户 XXXXXXXX] (如：绑定商户 M10000000)管理员发送，在不同群重复发送即可换绑</a-col>
          </a-row>
          <a-row>
            <a-col :span="23" :offset="1"><b>解绑商户</b>--格式[解绑商户 XXXXXXXX] (如：解绑商户 M10000000)管理员发送，解绑当前群绑定的商户</a-col>
          </a-row>
          <a-row>
            <a-col :span="23" :offset="1"><b>全部商户</b>--格式[全部商户] 管理员发送，查询当前群绑定的商户</a-col>
          </a-row>
          <a-row>
            <a-col :span="23" :offset="1"><b>解绑全部商户</b>--格式[解绑全部商户] 管理员发送，解绑当前群绑定的所有商户</a-col>
          </a-row>
          <br/>
          <a-row>
            <a-col :span="23" :offset="1"><b>绑定通道</b>--格式[绑定通道 通道ID] (如：绑定通道 1000 或者 绑定通道 1000,2000,3000) 管理员发送，在不同群重复发送即可换绑</a-col>
          </a-row>
          <a-row>
            <a-col :span="23" :offset="1"><b>解绑通道</b>--格式[解绑通道 通道ID] (如：解绑通道 1000 或者 解绑通道 1000,2000,3000) 管理员发送</a-col>
          </a-row>
          <a-row>
            <a-col :span="23" :offset="1"><b>全部通道</b>--格式[全部通道] 管理员发送，查看当前群绑定的所有通道</a-col>
          </a-row>
          <a-row>
            <a-col :span="23" :offset="1"><b>解绑全部通道</b>--格式[解绑全部通道] 管理员发送，解绑当前群绑定的所有通道</a-col>
          </a-row>
          <br/>
          <a-row>
            <a-col :span="23" :offset="1"><b>群发全部</b>--格式[群发全部] 管理员/操作员发送，私发机器人内容，再回复该内容：群发全部</a-col>
          </a-row>
          <a-row>
            <a-col :span="23" :offset="1"><b>群发商户</b>--格式[群发商户] 管理员/操作员发送，私发机器人内容，再回复该内容：群发商户</a-col>
          </a-row>
          <a-row>
            <a-col :span="23" :offset="1"><b>群发通道</b>--格式[群发通道] 管理员/操作员发送，私发机器人内容，再回复该内容：群发通道</a-col>
          </a-row>
          <br/>
          <a-row>
            <a-col :span="23" :offset="1"><b>删除</b>--回复需要删除的机器人消息。 管理员/操作员发送，可删除机器人发出的任何消息</a-col>
          </a-row>
          <a-row>
            <a-col :span="23" :offset="1"><b>今日结算</b>--私聊机器人。 管理员/操作员发送，自动向所有商户群发送结算消息</a-col>
          </a-row>
          <a-row>
            <a-col :span="23" :offset="1"><b>昨日结算</b>--私聊机器人。 管理员/操作员发送，自动向所有商户群发送结算消息</a-col>
          </a-row>
          <br/>
          <a-form-model ref="configFormModel">
            <a-row>
              <a-col :span="8" :offset="1" :key="config" v-for="(item, config) in configData">
                <a-form-model-item :label="item.configName">
                  <a-input :type="item.type==='text'?'text':'textarea'" v-model="item.configVal" autocomplete="off" />
                </a-form-model-item>
              </a-col>
            </a-row>
            <a-form-item style="display:flex;justify-content:center">
              <a-button type="primary" icon="check-circle" @click="confirm" :loading="btnLoading">确认更新</a-button>
            </a-form-item>
          </a-form-model>
        </div>
      </a-tab-pane>
    </a-tabs>
  </div>
</template>
<script>
import { API_URL_SYS_CONFIG, req, getConfigs } from '@/api/manage'

export default {
  components: {
  },
  data () {
    return {
      btnLoading: false,
      configData: {},
      groupKey: 'robotsConfigGroup'
    }
  },
  created () {
    this.detail()
  },
  methods: {
    detail () { // 获取基本信息
      const that = this
      getConfigs(that.groupKey).then(res => {
        that.configData = res
        that.groupKey = res[0].groupKey
      })
    },
    selectTabs (key) { // 清空必填提示
      if (key) {
        this.groupKey = key
        this.detail()
      }
    },
    confirm (e) { // 确认更新
      const that = this
      this.$infoBox.confirmPrimary('确认修改应用配置吗？', '', () => {
        that.btnLoading = true // 打开按钮上的 loading
        const formData = new FormData()
        for (var i in that.configData) {
          formData.append(that.configData[i].configKey, that.configData[i].configVal)
        }
        req.updateById(API_URL_SYS_CONFIG, that.groupKey, formData).then(res => {
          that.$message.success('修改成功')
          that.btnLoading = false
        }).catch(res => {
          that.btnLoading = false
        })
      })
    }
  }
}
</script>
