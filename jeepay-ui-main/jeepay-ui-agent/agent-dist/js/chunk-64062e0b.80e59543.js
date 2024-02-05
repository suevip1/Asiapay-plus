(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["chunk-64062e0b"],{ca00:function(e,t,a){"use strict";a.d(t,"b",(function(){return r})),a.d(t,"a",(function(){return i}));var o=1;function r(){var e=new Date,t=e.getHours();return t<9?"早上好":t<=11?"上午好":t<=13?"中午好":t<20?"下午好":"晚上好"}function i(){return(new Date).getTime()+"_"+o++}},e01b:function(e,t,a){"use strict";a.r(t);var o=function(){var e=this,t=e.$createElement,a=e._self._c||t;return e.visible?a("a-drawer",{attrs:{visible:e.visible,closable:!0,maskClosable:!1,"body-style":{paddingBottom:"80px"},"drawer-style":{backgroundColor:"#f0f2f5"},width:"80%"},on:{close:e.onClose}},[a("a-descriptions",{attrs:{title:"绑定分账接收者账号"}},[a("a-descriptions-item",{attrs:{label:"当前应用"}},[a("span",{staticStyle:{color:"red"}},[e._v(e._s(e.appInfo.appName)+" ["+e._s(e.appInfo.appId)+"]")])]),a("a-descriptions-item",{attrs:{label:"选择要加入到的账号分组"}},[a("a-select",{staticStyle:{width:"210px"},attrs:{placeholder:"账号分组"},model:{value:e.selectedReceiverGroupId,callback:function(t){e.selectedReceiverGroupId=t},expression:"selectedReceiverGroupId"}},e._l(e.allReceiverGroup,(function(t){return a("a-select-option",{key:t.receiverGroupId,attrs:{value:t.receiverGroupId}},[e._v(e._s(t.receiverGroupName))])})),1)],1)],1),a("a-divider"),a("a-card",{directives:[{name:"show",rawName:"v-show",value:e.appSupportIfCodes.indexOf("wxpay")>=0,expression:"appSupportIfCodes.indexOf('wxpay') >= 0"}],attrs:{title:"微信账号"}},[a("a",{attrs:{slot:"extra",href:"#"},slot:"extra"},[a("a-button",{staticStyle:{background:"#4BD884",color:"white"},attrs:{icon:"wechat"},on:{click:function(t){return e.addReceiverRow("wxpay")}}},[e._v("添加【微信官方】分账接收账号")])],1),a("a-table",{attrs:{columns:e.accTableColumns,"data-source":e.receiverTableData.filter((function(e){return"wxpay"==e.ifCode})),pagination:!1,rowKey:"rowKey"},scopedSlots:e._u([{key:"reqBindStateSlot",fn:function(t){return[a("div",{directives:[{name:"show",rawName:"v-show",value:0==t.reqBindState,expression:"record.reqBindState == 0"}],staticStyle:{color:"salmon"}},[a("a-icon",{attrs:{type:"info-circle"}}),e._v(" 待绑定 ")],1),a("div",{directives:[{name:"show",rawName:"v-show",value:1==t.reqBindState,expression:"record.reqBindState == 1"}],staticStyle:{color:"#4BD884"}},[a("a-icon",{attrs:{type:"check-circle"}}),e._v(" 绑定成功 ")],1),a("div",{directives:[{name:"show",rawName:"v-show",value:2==t.reqBindState,expression:"record.reqBindState == 2"}],staticStyle:{color:"red"}},[a("a-icon",{attrs:{type:"close-circle"}}),e._v(" 绑定异常 ")],1)]}},{key:"receiverAliasSlot",fn:function(t){return[a("a-input",{staticStyle:{width:"150px"},attrs:{placeholder:"(选填)默认为账号"},model:{value:t.receiverAlias,callback:function(a){e.$set(t,"receiverAlias",a)},expression:"record.receiverAlias"}})]}},{key:"accTypeSlot",fn:function(t){return[a("a-select",{staticStyle:{width:"110px"},attrs:{placeholder:"账号类型","default-value":"0"},model:{value:t.accType,callback:function(a){e.$set(t,"accType",a)},expression:"record.accType"}},[a("a-select-option",{attrs:{value:"0"}},[e._v("个人")]),a("a-select-option",{attrs:{value:"1"}},[e._v("微信商户")])],1)]}},{key:"accNoSlot",fn:function(t){return[a("a-input",{staticStyle:{width:"150px"},model:{value:t.accNo,callback:function(a){e.$set(t,"accNo",a)},expression:"record.accNo"}}),0==t.accType?a("a-button",{attrs:{type:"link"},on:{click:function(a){return e.showChannelUserModal("wxpay",t)}}},[e._v("扫码获取")]):e._e()]}},{key:"accNameSlot",fn:function(t){return[a("a-input",{model:{value:t.accName,callback:function(a){e.$set(t,"accName",a)},expression:"record.accName"}})]}},{key:"relationTypeSlot",fn:function(t){return[a("a-select",{staticStyle:{width:"110px"},attrs:{labelInValue:"",placeholder:"分账关系类型",defaultValue:{key:"PARTNER"}},on:{change:function(a){return e.changeRelationType(t,a)}}},[a("a-select-option",{key:"PARTNER"},[e._v("合作伙伴")]),a("a-select-option",{key:"SERVICE_PROVIDER"},[e._v("服务商")]),a("a-select-option",{key:"STORE"},[e._v("门店")]),a("a-select-option",{key:"STAFF"},[e._v("员工")]),a("a-select-option",{key:"STORE_OWNER"},[e._v("店主")]),a("a-select-option",{key:"HEADQUARTER"},[e._v("总部")]),a("a-select-option",{key:"BRAND"},[e._v("品牌方")]),a("a-select-option",{key:"DISTRIBUTOR"},[e._v("分销商")]),a("a-select-option",{key:"USER"},[e._v("用户")]),a("a-select-option",{key:"SUPPLIER"},[e._v("供应商")]),a("a-select-option",{key:"CUSTOM"},[e._v("自定义")])],1)]}},{key:"relationTypeNameSlot",fn:function(t){return[a("a-input",{attrs:{disabled:"CUSTOM"!==t.relationType},model:{value:t.relationTypeName,callback:function(a){e.$set(t,"relationTypeName",a)},expression:"record.relationTypeName"}})]}},{key:"divisionProfitSlot",fn:function(t){return[a("a-input",{staticStyle:{width:"65px"},model:{value:t.divisionProfit,callback:function(a){e.$set(t,"divisionProfit",a)},expression:"record.divisionProfit"}}),e._v(" % ")]}},{key:"opSlot",fn:function(t){return[a("a-button",{attrs:{type:"link"},on:{click:function(a){return e.delRow(t)}}},[e._v("删除")])]}}],null,!1,656490347)})],1),a("br"),a("a-card",{directives:[{name:"show",rawName:"v-show",value:e.appSupportIfCodes.indexOf("alipay")>=0,expression:"appSupportIfCodes.indexOf('alipay') >= 0"}],attrs:{title:"支付宝账号"}},[a("a",{attrs:{slot:"extra",href:"#"},slot:"extra"},[a("a-button",{staticStyle:{background:"dodgerblue",color:"white"},attrs:{icon:"alipay-circle"},on:{click:function(t){return e.addReceiverRow("alipay")}}},[e._v("添加【支付宝官方】分账接收账号")])],1),a("a-table",{attrs:{columns:e.accTableColumns,"data-source":e.receiverTableData.filter((function(e){return"alipay"==e.ifCode})),pagination:!1,rowKey:"rowKey"},scopedSlots:e._u([{key:"reqBindStateSlot",fn:function(t){return[a("div",{directives:[{name:"show",rawName:"v-show",value:0==t.reqBindState,expression:"record.reqBindState == 0"}],staticStyle:{color:"salmon"}},[a("a-icon",{attrs:{type:"info-circle"}}),e._v(" 待绑定 ")],1),a("div",{directives:[{name:"show",rawName:"v-show",value:1==t.reqBindState,expression:"record.reqBindState == 1"}],staticStyle:{color:"#4BD884"}},[a("a-icon",{attrs:{type:"check-circle"}}),e._v(" 绑定成功 ")],1),a("div",{directives:[{name:"show",rawName:"v-show",value:2==t.reqBindState,expression:"record.reqBindState == 2"}],staticStyle:{color:"red"}},[a("a-icon",{attrs:{type:"close-circle"}}),e._v(" 绑定异常 ")],1)]}},{key:"receiverAliasSlot",fn:function(t){return[a("a-input",{staticStyle:{width:"150px"},attrs:{placeholder:"(选填)默认为账号"},model:{value:t.receiverAlias,callback:function(a){e.$set(t,"receiverAlias",a)},expression:"record.receiverAlias"}})]}},{key:"accTypeSlot",fn:function(t){return[a("a-select",{staticStyle:{width:"110px"},attrs:{placeholder:"账号类型","default-value":"0"},model:{value:t.accType,callback:function(a){e.$set(t,"accType",a)},expression:"record.accType"}},[a("a-select-option",{attrs:{value:"0"}},[e._v("个人")]),a("a-select-option",{attrs:{value:"1"}},[e._v("微信商户")])],1)]}},{key:"accNoSlot",fn:function(t){return[a("a-input",{staticStyle:{width:"150px"},model:{value:t.accNo,callback:function(a){e.$set(t,"accNo",a)},expression:"record.accNo"}}),0==t.accType?a("a-button",{attrs:{type:"link"},on:{click:function(a){return e.showChannelUserModal("alipay",t)}}},[e._v("扫码获取")]):e._e()]}},{key:"accNameSlot",fn:function(t){return[a("a-input",{model:{value:t.accName,callback:function(a){e.$set(t,"accName",a)},expression:"record.accName"}})]}},{key:"relationTypeSlot",fn:function(t){return[a("a-select",{staticStyle:{width:"110px"},attrs:{labelInValue:"",placeholder:"分账关系类型",defaultValue:{key:"PARTNER"}},on:{change:function(a){return e.changeRelationType(t,a)}}},[a("a-select-option",{key:"PARTNER"},[e._v("合作伙伴")]),a("a-select-option",{key:"SERVICE_PROVIDER"},[e._v("服务商")]),a("a-select-option",{key:"STORE"},[e._v("门店")]),a("a-select-option",{key:"STAFF"},[e._v("员工")]),a("a-select-option",{key:"STORE_OWNER"},[e._v("店主")]),a("a-select-option",{key:"HEADQUARTER"},[e._v("总部")]),a("a-select-option",{key:"BRAND"},[e._v("品牌方")]),a("a-select-option",{key:"DISTRIBUTOR"},[e._v("分销商")]),a("a-select-option",{key:"USER"},[e._v("用户")]),a("a-select-option",{key:"SUPPLIER"},[e._v("供应商")]),a("a-select-option",{key:"CUSTOM"},[e._v("自定义")])],1)]}},{key:"relationTypeNameSlot",fn:function(t){return[a("a-input",{attrs:{disabled:"CUSTOM"!==t.relationType},model:{value:t.relationTypeName,callback:function(a){e.$set(t,"relationTypeName",a)},expression:"record.relationTypeName"}})]}},{key:"divisionProfitSlot",fn:function(t){return[a("a-input",{staticStyle:{width:"65px"},model:{value:t.divisionProfit,callback:function(a){e.$set(t,"divisionProfit",a)},expression:"record.divisionProfit"}}),e._v(" % ")]}},{key:"opSlot",fn:function(t){return[a("a-button",{attrs:{type:"link"},on:{click:function(a){return e.delRow(t)}}},[e._v("删除")])]}}],null,!1,1413224800)})],1),a("div",{staticClass:"drawer-btn-center "},[a("a-button",{style:{marginRight:"8px"},attrs:{type:"primary",icon:"rocket"},on:{click:function(t){return e.reqBatchBindReceiver(0)}}},[e._v("发起绑定请求")]),a("a-button",{attrs:{icon:"close"},on:{click:e.onClose}},[e._v("关闭")])],1),a("ChannelUserModal",{ref:"channelUserModal",on:{changeChannelUserId:function(t){return e.changeChannelUserIdFunc(t)}}})],1):e._e()},r=[],i=(a("a434"),a("ca00")),n=a("29b2"),c=a("0fea"),l=[{key:"reqBindState",title:"状态",scopedSlots:{customRender:"reqBindStateSlot"}},{key:"receiverAlias",title:"账号别名",scopedSlots:{customRender:"receiverAliasSlot"}},{key:"accType",title:"账号类型",scopedSlots:{customRender:"accTypeSlot"}},{key:"accNo",width:"300px",title:"接收方账号",scopedSlots:{customRender:"accNoSlot"}},{key:"accName",width:"180px",title:"接收方姓名",scopedSlots:{customRender:"accNameSlot"}},{key:"relationType",title:"分账关系",scopedSlots:{customRender:"relationTypeSlot"}},{key:"relationTypeName",width:"200px",title:"关系名称",scopedSlots:{customRender:"relationTypeNameSlot"}},{key:"divisionProfit",title:"默认分账比例",scopedSlots:{customRender:"divisionProfitSlot"}},{key:"op",title:"操作",scopedSlots:{customRender:"opSlot"}}],s={reqBindState:0,receiverAlias:"",receiverGroupId:"",appId:"",ifCode:"",accType:"0",accNo:"",accName:"",relationType:"PARTNER",relationTypeName:"合作伙伴",divisionProfit:""},p={components:{ChannelUserModal:n["a"]},props:{callbackFunc:{type:Function,default:function(){return{}}}},data:function(){return{visible:!1,appInfo:null,accTableColumns:l,allReceiverGroup:[],selectedReceiverGroupId:"",appSupportIfCodes:[],receiverTableData:[]}},methods:{show:function(e){var t=this;this.appSupportIfCodes=[],this.receiverTableData=[],c["B"].list(c["h"],{pageSize:-1}).then((function(e){t.allReceiverGroup=e.records,t.allReceiverGroup&&t.allReceiverGroup.length>0&&(t.selectedReceiverGroupId=t.allReceiverGroup[0].receiverGroupId)})),Object(c["u"])(e.appId).then((function(e){t.appSupportIfCodes=e})),this.appInfo=e,this.visible=!0},onClose:function(){this.callbackFunc(),this.visible=!1},delRow:function(e){var t=this.receiverTableData.indexOf(e);t>-1&&this.receiverTableData.splice(t,1)},changeRelationType:function(e,t){e.relationType=t.key,"CUSTOM"!==t.key?e.relationTypeName=t.label:e.relationTypeName=""},showChannelUserModal:function(e,t){this.$refs.channelUserModal.showModal(this.appInfo.appId,e,t)},changeChannelUserIdFunc:function(e){var t=e.channelUserId,a=e.extObject;a.accNo=t},addReceiverRow:function(e){if(!this.selectedReceiverGroupId)return this.$message.error("请选选择要加入的分组");this.receiverTableData.push(Object.assign({},s,{rowKey:Object(i["a"])(),ifCode:e,appId:this.appInfo.appId}))},reqBatchBindReceiver:function(e){var t=this.$createElement,a=this;if(a.receiverTableData.length<=0)return a.$message.error("请先添加账号");if(e>=a.receiverTableData.length)return this.$message.success("已完成所有账号的绑定操作");var o=a.receiverTableData[e];return o.receiverGroupId=a.selectedReceiverGroupId,1===o.reqBindState?a.reqBatchBindReceiver(++e):o.accNo?"CUSTOM"!==o.relationType||o.relationTypeName?!o.divisionProfit||o.divisionProfit<=0||o.divisionProfit>100?this.$message.error("第".concat(e+1,"条： 默认分账比例请设置在[0.01% ~ 100% ] 之间")):void c["B"].add(c["g"],o).then((function(r){1===r.bindState?(a.reqBatchBindReceiver(++e),o.reqBindState=1):(o.reqBindState=2,a.$infoBox.modalError("第".concat(e+1,"条： 绑定异常"),t("div",[t("div",["错误码：",r.errCode]),t("div",["错误信息：",r.errMsg])])))})).catch((function(){o.reqBindState=2})):this.$message.error("第".concat(e+1,"条： 自定义类型时接收方账号名称不能为空")):this.$message.error("第".concat(e+1,"条： 接收方账号不能为空"))}}},d=p,u=a("2877"),v=Object(u["a"])(d,o,r,!1,null,null,null);t["default"]=v.exports}}]);