(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["chunk-14748245","chunk-90316dec"],{"0fea":function(t,e,r){"use strict";r.d(e,"z",(function(){return n})),r.d(e,"c",(function(){return o})),r.d(e,"d",(function(){return s})),r.d(e,"j",(function(){return i})),r.d(e,"k",(function(){return u})),r.d(e,"l",(function(){return c})),r.d(e,"b",(function(){return l})),r.d(e,"a",(function(){return d})),r.d(e,"i",(function(){return p})),r.d(e,"g",(function(){return f})),r.d(e,"e",(function(){return m})),r.d(e,"h",(function(){return h})),r.d(e,"f",(function(){return y})),r.d(e,"D",(function(){return b})),r.d(e,"s",(function(){return g})),r.d(e,"r",(function(){return v})),r.d(e,"w",(function(){return T})),r.d(e,"x",(function(){return w})),r.d(e,"C",(function(){return S})),r.d(e,"B",(function(){return C})),r.d(e,"t",(function(){return P})),r.d(e,"u",(function(){return x})),r.d(e,"y",(function(){return O})),r.d(e,"o",(function(){return k})),r.d(e,"m",(function(){return F})),r.d(e,"q",(function(){return q})),r.d(e,"v",(function(){return _})),r.d(e,"A",(function(){return D})),r.d(e,"p",(function(){return I})),r.d(e,"n",(function(){return $}));r("2ca0"),r("ac1f"),r("5319");var a=r("4667"),n={list:function(t,e){return a["a"].request({url:t,method:"GET",params:e},!0,!0,!1)},add:function(t,e){return a["a"].request({url:t,method:"POST",data:e},!0,!0,!1)},getById:function(t,e){return a["a"].request({url:t+"/"+e,method:"GET"},!0,!0,!1)},updateById:function(t,e,r){return a["a"].request({url:t+"/"+e,method:"PUT",data:r},!0,!0,!1)},delById:function(t,e){return a["a"].request({url:t+"/"+e,method:"DELETE"},!0,!0,!1)}},o="api/mainChart",s="/api/mchApps",i="/api/payOrder",u="/api/refundOrder",c="/api/transferOrders",l="/api/divisionReceiverGroups",d="/api/divisionReceivers",p="/api/division/records",f="/api/mchHistory",m="/api/mchDayStat",h="/api/mchInfo",y="/api/mchDivision",b={avatar:a["a"].baseUrl+"/api/ossFiles/avatar",cert:a["a"].baseUrl+"/api/ossFiles/cert"};function g(){return a["a"].request({url:o+"/twoDayCount",method:"GET"})}function v(){return a["a"].request({url:o+"/mchInfo",method:"GET"})}function T(t){return a["a"].request({url:"api/paytest/payways/"+t,method:"GET"})}function w(t){return a["a"].request({url:"/api/paytest/payOrders",method:"POST",data:t})}function S(t){return a["a"].request({url:"/api/current/modifyPwd",method:"put",data:t})}function C(t){return a["a"].request({url:"/api/current/user",method:"put",data:t})}function P(){return a["a"].request({url:"/api/current/user",method:"get"})}function x(){var t=document.location.protocol+"//"+document.location.host;return t="http://mch-api.bainian-pay.com",t.startsWith("https:")?"wss://"+t.replace("https://",""):"ws://"+t.replace("http://","")}function O(t){return a["a"].request({url:"api/mchTransfers/ifCodes/"+t,method:"GET"})}function k(t,e,r){return a["a"].request({url:"/api/mchTransfers/channelUserId",method:"GET",params:{ifCode:t,appId:e,extParam:r}})}function F(t){return a["a"].request({url:"/api/mchTransfers/doTransfer",method:"POST",data:t},!0,!0,!0)}function q(t){return a["a"].request({url:"/api/mch/payConfigs/ifCodes/"+t,method:"GET"},!0,!0,!0)}function _(t,e,r){return a["a"].request({url:"/api/payOrder/refunds/"+t,method:"POST",data:{refundAmount:e,refundReason:r}})}function D(t){return a["a"].request({url:"/api/division/records/resend/"+t,method:"POST"})}function I(){return a["a"].request({url:"/api/current/getGoogleKey",method:"get"})}function $(t,e){return a["a"].request({url:t,method:"POST",data:e,responseType:"arraybuffer"},!0,!1,!0)}},"124d":function(t,e,r){"use strict";r.r(e);var a=function(){var t=this,e=t.$createElement,r=t._self._c||e;return r("a-drawer",{attrs:{visible:t.visible,title:"产品下单测试",width:"40%",maskClosable:!1},on:{close:t.onClose}},[r("a-row",{attrs:{justify:"space-between",type:"flex"}},[r("a-col",{attrs:{sm:12}},[r("a-descriptions",[r("a-descriptions-item",{attrs:{label:"产品ID"}},[r("b",{staticStyle:{color:"#1A79FF"}},[t._v(t._s(t.product.productId))])])],1)],1),r("a-col",{attrs:{sm:12}},[r("a-descriptions",[r("a-descriptions-item",{attrs:{label:"产品名称"}},[r("b",[t._v(t._s(t.product.productName))])])],1)],1),r("a-divider"),r("a-form-model",{ref:"infoFormModel",attrs:{layout:"vertical"}},[r("a-row",{attrs:{gutter:16}},[r("a-col",{attrs:{span:24}},[r("a-form-model-item",{attrs:{label:"支付金额"}},[r("a-input",{attrs:{prefix:"￥",placeholder:"请输入",type:"number"},model:{value:t.payTestAmount,callback:function(e){t.payTestAmount=e},expression:"payTestAmount"}})],1)],1),r("a-col",{attrs:{span:24}},[r("a-button",{attrs:{type:"primary",icon:"check"},on:{click:t.onSubmit}},[t._v("下单测试")])],1)],1)],1)],1),r("a-divider"),r("a-row",{attrs:{justify:"start",type:"flex"}},[r("a-col",{attrs:{sm:24}},[r("a-form-model-item",{attrs:{label:"测试订单号"}},[r("a-input",{staticStyle:{color:"black"},attrs:{type:"text",disabled:"disabled"},model:{value:t.testPayOrderId,callback:function(e){t.testPayOrderId=e},expression:"testPayOrderId"}})],1)],1)],1),r("a-row",{attrs:{justify:"start",type:"flex"}},[r("a-col",{attrs:{sm:24}},[r("a-form-model-item",{attrs:{label:"下单返回参数"}},[r("a-input",{staticStyle:{height:"100px",color:"black"},attrs:{type:"textarea",disabled:"disabled"},model:{value:t.respParams,callback:function(e){t.respParams=e},expression:"respParams"}})],1)],1)],1),t.isShowReturnUrl?r("a-row",{attrs:{justify:"start",type:"flex"}},[r("a-col",{attrs:{sm:24}},[r("a-form-model-item",{attrs:{label:"支付链接（点击直接跳转）"}},[r("a",{staticStyle:{"font-size":"18px"},attrs:{href:t.returnUrl,target:"_blank"}},[t._v(t._s(t.returnUrl))])])],1),r("a-col",{attrs:{sm:24}},[r("a-form-model-item",{attrs:{label:"手动跳转"}},[r("a-button",{directives:[{name:"clipboard",rawName:"v-clipboard:copy",value:t.returnUrl,expression:"returnUrl",arg:"copy"},{name:"clipboard",rawName:"v-clipboard:success",value:t.onCopy,expression:"onCopy",arg:"success"}],staticClass:"copy-btn",attrs:{type:"primary",size:"small"}},[t._v("一键复制链接")])],1)],1)],1):t._e()],1)},n=[],o=r("0fea"),s={props:{callbackFunc:{type:Function,default:function(){return function(){return{}}}}},data:function(){return{visible:!1,payTestAmount:0,product:{},ifParams:{},respParams:"",testPayOrderId:"",isShowReturnUrl:!1,returnUrl:""}},methods:{show:function(t){this.product=t,this.visible=!0,this.isShowReturnUrl=!1,this.respParams="",this.testPayOrderId="",this.returnUrl="",this.payTestAmount=0},onSubmit:function(){if(""!==this.payTestAmount&&0!==this.payTestAmount){var t=this,e="T"+(new Date).getTime()+Math.floor(8999*Math.random()+1e3);t.$store.commit("showLoading"),Object(o["x"])({amount:100*t.payTestAmount,productId:t.product.productId,mchOrderNo:e}).then((function(r){t.$store.commit("hideLoading"),t.respParams=JSON.stringify(r),t.testPayOrderId=e,void 0===r.data.payData?t.$message.error("出码失败"):(t.returnUrl=r.data.payData,t.isShowReturnUrl=!0,t.$message.info("下单成功"))})).catch((function(e){t.respParams=JSON.stringify(e.msg),t.$store.commit("hideLoading"),t.$message.error("拉起订单异常")}))}else this.$message.error("金额不能为空")},onClose:function(){this.visible=!1},onCopy:function(){this.$message.success("复制成功")}}},i=s,u=r("2877"),c=Object(u["a"])(i,a,n,!1,null,"042f05e0",null);e["default"]=c.exports},"27fc":function(t,e,r){},"30b1":function(t,e,r){"use strict";r.r(e);var a=function(){var t=this,e=t.$createElement,r=t._self._c||e;return r("page-header-wrapper",[r("a-card",[r("JeepayTable",{ref:"infoTable",attrs:{initData:!0,reqTableDataFunc:t.reqTableDataFunc,tableColumns:t.tableColumns,searchData:t.searchData,rowKey:"productId"},on:{btnLoadClose:function(e){t.btnLoading=!1}},scopedSlots:t._u([{key:"appIdSlot",fn:function(e){var a=e.record;return[r("b",[t._v(t._s(a.productId))])]}},{key:"stateSlot",fn:function(t){var e=t.record;return[r("a-badge",{attrs:{status:0===e.state?"error":"processing",text:0===e.state?"禁用":"启用"}})]}},{key:"rateSlot",fn:function(e){var a=e.record;return[r("span",[t._v(t._s((100*a.mchRate).toFixed(2))+"%")])]}},{key:"opSlot",fn:function(e){var a=e.record;return[r("JeepayTableColumns",[r("a-button",{attrs:{type:"link"},on:{click:function(e){return t.mchPayTest(a)}}},[t._v("支付测试")])],1)]}}])})],1),r("MchPayTest",{ref:"mchPayTest"})],1)},n=[],o=r("f339"),s=r("4f53"),i=r("5d5e"),u=r("0fea"),c=r("124d"),l=[{key:"appId",fixed:"left",width:"220px",title:"产品Id",scopedSlots:{customRender:"appIdSlot"}},{key:"productName",title:"产品名称",dataIndex:"productName"},{key:"rate",title:"商户费率",scopedSlots:{customRender:"rateSlot"}},{key:"state",title:"状态",scopedSlots:{customRender:"stateSlot"}},{key:"createdAt",dataIndex:"createdAt",title:"创建日期"},{key:"op",title:"操作",width:"260px",fixed:"right",align:"center",scopedSlots:{customRender:"opSlot"}}],d={name:"MchAppPage",components:{JeepayTable:o["a"],JeepayTableColumns:i["a"],JeepayTextUp:s["a"],MchPayTest:c["default"]},data:function(){return{btnLoading:!1,tableColumns:l,searchData:{}}},methods:{queryFunc:function(){this.btnLoading=!0,this.$refs.infoTable.refTable(!0)},reqTableDataFunc:function(t){return u["z"].list(u["d"],t)},searchFunc:function(){this.$refs.infoTable.refTable(!0)},addFunc:function(){this.$refs.mchAppAddOrEdit.show()},delFunc:function(t){var e=this;this.$infoBox.confirmDanger("确认删除？","",(function(){u["z"].delById(u["d"],t).then((function(t){e.$message.success("删除成功！"),e.searchFunc()}))}))},mchPayTest:function(t){this.$refs.mchPayTest.show(t)}}},p=d,f=r("2877"),m=Object(f["a"])(p,a,n,!1,null,"e8aa2134",null);e["default"]=m.exports},44423:function(t,e,r){"use strict";r("aa30")},"4f53":function(t,e,r){"use strict";var a=function(){var t=this,e=t.$createElement,r=t._self._c||e;return r("div",{staticClass:"jee-text-up"},[r("a-input",{attrs:{required:"required",value:t.msg},on:{input:function(e){return t.$emit("input",e.target.value)}}}),r("label",[t._v(t._s(t.placeholder))])],1)},n=[],o={name:"JeepayTextUp",props:{msg:{type:String},placeholder:{type:String}}},s=o,i=(r("a72a"),r("2877")),u=Object(i["a"])(s,a,n,!1,null,"4d207278",null);e["a"]=u.exports},"5d5e":function(t,e,r){"use strict";r("d81d");var a,n,o={name:"JeepayTableColumns",render:function(t,e){var r=arguments[0],a=[];if(this.$slots.default.map((function(t){return t.tag&&a.push(t),!1})),a.length<=3)return t("div",{style:"display:flex; justify-content: space-evenly;"},a);for(var n=[a[0],a[1]],o=[],s=2;s<a.length;s++)o.push(r("a-menu-item",[a[s]]));return r("div",{style:"display:flex; justify-content: space-evenly;"},[" ",n,r("a-dropdown",[r("a-button",{style:"",attrs:{type:"link"},class:"ant-dropdown-link"},["更多",r("a-icon",{attrs:{type:"down"}})]),r("a-menu",{slot:"overlay"},[o])])])}},s=o,i=(r("44423"),r("2877")),u=Object(i["a"])(s,a,n,!1,null,"207fd926",null);e["a"]=u.exports},a72a:function(t,e,r){"use strict";r("de7e")},aa30:function(t,e,r){},de7e:function(t,e,r){},f339:function(t,e,r){"use strict";var a=function(){var t=this,e=t.$createElement,r=t._self._c||e;return r("div",[r("a-table",{attrs:{columns:t.tableColumns,"data-source":t.apiResData.records,pagination:t.pagination,loading:t.showLoading,"row-selection":t.rowSelection,rowKey:t.rowKey,scroll:{x:t.scrollX},customRow:function(e,r){return t.tableRowCrossColor?{style:{"background-color":r%2==0?"#FCFCFC":"#FFFFFF"}}:{}}},on:{change:t.handleTableChange},scopedSlots:t._u([t._l(t.columnsCustomSlots,(function(e){return{key:e.customRender,fn:function(r){return[t._t(e.customRender,null,{record:r})]}}}))],null,!0)})],1)},n=[],o=r("5530"),s=(r("a9e3"),r("d81d"),r("4de4"),{name:"JeepayTable",props:{initData:{type:Boolean,default:!0},tableColumns:Array,reqTableDataFunc:{type:Function},currentChange:{type:Function,default:function(t,e){}},searchData:Object,pageSize:{type:Number,default:10},rowSelection:Object,rowKey:{type:[String,Function]},scrollX:{type:Number,default:800},tableRowCrossColor:{type:Boolean,default:!1}},data:function(){return{apiResData:{total:0,records:[]},iPage:{pageNumber:1,pageSize:this.pageSize},pagination:{total:0,current:1,pageSizeOptions:["10","20","50","100"],pageSize:this.pageSize,showSizeChanger:!0,showTotal:function(t){return"共".concat(t,"条")}},showLoading:!1}},computed:{columnsCustomSlots:function(){return this.tableColumns.filter((function(t){return t.scopedSlots})).map((function(t){return t.scopedSlots}))}},mounted:function(){this.initData&&this.refTable(!0)},methods:{handleTableChange:function(t,e,r){this.pagination=t,this.iPage=Object(o["a"])({pageSize:t.pageSize,pageNumber:t.current,sortField:r.columnKey,sortOrder:r.order},e),this.refTable()},refTable:function(){var t=this,e=arguments.length>0&&void 0!==arguments[0]&&arguments[0],r=this;e&&(this.iPage.pageNumber=1,this.pagination.current=1),this.showLoading=!0,this.reqTableDataFunc(Object.assign({},this.iPage,this.searchData)).then((function(e){t.pagination.total=e.total,t.apiResData=e,t.showLoading=!1,0===e.records.length&&t.iPage.pageNumber>1&&r.$nextTick((function(){var a=e.total/t.iPage.pageSize+(e.total%t.iPage.pageSize===0?0:1);if(0===a)return!1;var n=t.iPage.pageNumber-1>a?a:t.iPage.pageNumber-1;t.iPage.pageNumber=n,t.pagination.current=n,r.refTable(!1)})),r.$emit("btnLoadClose")})).catch((function(e){t.showLoading=!1,r.$emit("btnLoadClose")}))}}}),i=s,u=(r("f705"),r("2877")),c=Object(u["a"])(i,a,n,!1,null,null,null);e["a"]=c.exports},f705:function(t,e,r){"use strict";r("27fc")}}]);