(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["chunk-e401853a"],{"4b70":function(a,e,t){"use strict";t("fa43")},"4be7":function(a,e,t){"use strict";t.r(e);var i=function(){var a=this,e=a.$createElement,t=a._self._c||e;return t("div",[t("a-card",{staticStyle:{"box-sizing":"border-box",padding:"30px"}},[t("a-form",[t("div",{staticStyle:{display:"flex","flex-direction":"row"}},[t("a-form-item",{staticClass:"table-head-layout",attrs:{label:""}},[t("a-select",{staticStyle:{width:"300px"},on:{change:a.changeAppId},model:{value:a.reqData.appId,callback:function(e){a.$set(a.reqData,"appId",e)},expression:"reqData.appId"}},[t("a-select-option",{key:""},[a._v("请选择应用APPID")]),a._l(a.mchAppList,(function(e){return t("a-select-option",{key:e.appId},[a._v(a._s(e.appName)+" ["+a._s(e.appId)+"]")])}))],2)],1)],1)]),a.reqData.appId?0==a.ifCodeList.length?t("a-divider",[a._v("该应用尚未配置任何通道")]):t("div",[t("div",{staticClass:"paydemo",staticStyle:{width:"100%"}},[t("div",{staticClass:"paydemo-type-content"},[t("div",{staticClass:"paydemo-type-name article-title"},[a._v("选择通道")]),t("div",{staticClass:"paydemo-type-body"},[a._l(a.ifCodeList,(function(e){return[t("div",{key:e.ifCode,class:{"paydemo-type":!0,"color-change":!0,this:a.reqData.ifCode===e.ifCode},on:{click:function(t){return a.changeCurrentIfCode(e.ifCode)}}},[t("span",{staticClass:"color-change"},[a._v(a._s(e.ifName))])])]}))],2)]),t("div",{staticClass:"paydemo-form-item"},[t("span",[a._v("入账方式：")]),t("a-radio-group",{staticStyle:{display:"flex"},model:{value:a.reqData.entryType,callback:function(e){a.$set(a.reqData,"entryType",e)},expression:"reqData.entryType"}},[t("div",{staticStyle:{display:"flex"}},[t("a-radio",{attrs:{value:"WX_CASH",disabled:"wxpay"!=a.reqData.ifCode}},[a._v("微信零钱")]),t("a-radio",{attrs:{value:"ALIPAY_CASH",disabled:"alipay"!=a.reqData.ifCode}},[a._v("支付宝余额")]),t("a-radio",{attrs:{value:"BANK_CARD",disabled:""}},[a._v("银行卡（暂未支持）")])],1)])],1),t("a-divider"),t("div",{staticClass:"paydemo-type-content"},[t("div",{staticClass:"paydemo-type-name article-title"},[a._v("转账信息")]),t("form",{staticClass:"layui-form"},[t("div",{staticClass:"paydemo-form-item"},[t("label",[a._v("订单编号：")]),t("span",{attrs:{id:"payMchOrderNo"}},[a._v(a._s(a.reqData.mchOrderNo))]),t("span",{staticClass:" paydemo-btn",staticStyle:{padding:"0 3px"},on:{click:a.randomOrderNo}},[a._v("刷新订单号")])]),t("div",{staticClass:"paydemo-form-item"},[t("span",[a._v("转账金额(元)：")]),t("a-input-number",{attrs:{max:1e5,min:.01,precision:2},model:{value:a.reqData.amount,callback:function(e){a.$set(a.reqData,"amount",e)},expression:"reqData.amount"}})],1),t("div",{staticClass:"paydemo-form-item"},[t("span",[a._v("收款账号：")]),t("a-input",{staticStyle:{width:"200px","margin-right":"10px"},model:{value:a.reqData.accountNo,callback:function(e){a.$set(a.reqData,"accountNo",e)},expression:"reqData.accountNo"}}),t("a-button",{directives:[{name:"show",rawName:"v-show",value:"WX_CASH"==a.reqData.entryType,expression:"reqData.entryType=='WX_CASH'"}],attrs:{size:"small",type:"danger"},on:{click:a.showChannelUserQR}},[a._v("自动获取openID")])],1),t("div",{staticStyle:{"margin-left":"10px",color:"red"}},[a._v("提示：【微信官方】需要填入对应应用收款方的openID")]),t("div",{staticStyle:{"margin-left":"10px",color:"red"}},[a._v(" 【支付宝官方】需要填入支付宝登录账号")]),t("div",{staticClass:"paydemo-form-item",staticStyle:{"margin-top":"10px"}},[t("span",[a._v("收款人姓名：")]),t("a-input",{staticStyle:{width:"200px"},model:{value:a.reqData.accountName,callback:function(e){a.$set(a.reqData,"accountName",e)},expression:"reqData.accountName"}}),t("div",{staticStyle:{"margin-left":"10px",color:"red"}},[a._v("提示： 填入则验证，否则不验证收款人姓名")])],1),t("div",{staticClass:"paydemo-form-item"},[t("span",[a._v("转账备注：")]),t("a-input",{staticStyle:{width:"200px"},model:{value:a.reqData.transferDesc,callback:function(e){a.$set(a.reqData,"transferDesc",e)},expression:"reqData.transferDesc"}})],1),t("div",{staticStyle:{"margin-top":"20px","text-align":"left"}},[t("a-button",{staticStyle:{padding:"5px 20px","background-color":"#1953ff","border-radius":"5px",color:"#fff"},on:{click:a.immediatelyPay}},[a._v("立即转账")])],1)])])],1)]):t("a-divider",[a._v("请选择应用APPID")])],1),t("ChannelUserModal",{ref:"channelUserModal",on:{changeChannelUserId:function(e){return a.changeChannelUserIdFunc(e)}}})],1)},r=[],s=t("0fea"),n=t("29b2"),o={components:{ChannelUserModal:n["a"]},data:function(){return{ifCodeList:[],reqData:{appId:"",mchOrderNo:"",ifCode:"",entryType:"",amount:.01,accountNo:"",accountName:"",transferDesc:"打款"},mchAppList:[]}},mounted:function(){var a=this,e=this.$route.params.appId;e&&(this.reqData.appId=e,this.changeAppId(e));var t=this;s["B"].list(s["i"],{pageSize:-1}).then((function(e){t.mchAppList=e.records,t.mchAppList.length>0&&(t.reqData.appId=t.mchAppList[0].appId,a.changeAppId(t.reqData.appId))})),this.randomOrderNo()},methods:{changeAppId:function(a){if(!a)return this.ifCodeList=[],!1;var e=this;Object(s["A"])(a).then((function(a){e.ifCodeList=a}))},randomOrderNo:function(){this.reqData.mchOrderNo="M"+(new Date).getTime()+Math.floor(8999*Math.random()+1e3)},immediatelyPay:function(){var a=this,e=this.$createElement;if(!this.reqData.amount||this.reqData.amount<=0)return this.$message.error("请输入转账金额");if(!this.reqData.ifCode)return this.$message.error("请选择转账通道");if(!this.reqData.entryType)return this.$message.error("请选择入账方式");if(!this.reqData.accountNo)return this.$message.error("请输入收款账号");if(!this.reqData.transferDesc)return this.$message.error("请输入转账备注");var t=this;Object(s["p"])(this.reqData).then((function(i){if(t.randomOrderNo(),2===i.state){var r=t.$infoBox.modalSuccess("转账成功",e("div",["2s后自动关闭..."]));setTimeout((function(){r.destroy()}),2e3)}else if(1===i.state)t.$infoBox.modalWarning("转账处理中",e("div",["请前往转账订单列表查看最终状态"]));else{if(3!==i.state)return a.$message.error("转账异常");t.$infoBox.modalError("转账处理失败",e("div",[e("div",["错误码：",i.errCode]),e("div",["错误信息：",i.errMsg])]))}})).catch((function(){t.randomOrderNo()}))},changeCurrentIfCode:function(a){this.reqData.ifCode=a,this.reqData.entryType="wxpay"===a?"WX_CASH":"alipay"===a?"ALIPAY_CASH":""},showChannelUserQR:function(){this.$refs.channelUserModal.showModal(this.reqData.appId,this.reqData.ifCode)},changeChannelUserIdFunc:function(a){var e=a.channelUserId;this.$message.success("成功获取渠道用户ID"),this.reqData.accountNo=e}}},c=o,d=(t("4b70"),t("2877")),l=Object(d["a"])(c,i,r,!1,null,"b9fa85e6",null);e["default"]=l.exports},fa43:function(a,e,t){}}]);