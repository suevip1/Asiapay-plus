(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["chunk-57d0c71b","chunk-44d26574"],{"0fea":function(t,e,a){"use strict";a.d(e,"B",(function(){return s})),a.d(e,"d",(function(){return o})),a.d(e,"e",(function(){return r})),a.d(e,"i",(function(){return i})),a.d(e,"f",(function(){return c})),a.d(e,"m",(function(){return u})),a.d(e,"j",(function(){return l})),a.d(e,"n",(function(){return d})),a.d(e,"k",(function(){return p})),a.d(e,"o",(function(){return f})),a.d(e,"h",(function(){return h})),a.d(e,"g",(function(){return m})),a.d(e,"l",(function(){return g})),a.d(e,"c",(function(){return b})),a.d(e,"a",(function(){return v})),a.d(e,"b",(function(){return y})),a.d(e,"F",(function(){return S})),a.d(e,"r",(function(){return w})),a.d(e,"y",(function(){return x})),a.d(e,"z",(function(){return D})),a.d(e,"E",(function(){return _})),a.d(e,"D",(function(){return C})),a.d(e,"v",(function(){return T})),a.d(e,"w",(function(){return k})),a.d(e,"A",(function(){return I})),a.d(e,"s",(function(){return P})),a.d(e,"p",(function(){return Y})),a.d(e,"u",(function(){return q})),a.d(e,"x",(function(){return F})),a.d(e,"C",(function(){return O})),a.d(e,"t",(function(){return N})),a.d(e,"q",(function(){return A}));a("2ca0"),a("ac1f"),a("5319");var n=a("4667"),s={list:function(t,e){return n["a"].request({url:t,method:"GET",params:e},!0,!0,!1)},add:function(t,e){return n["a"].request({url:t,method:"POST",data:e},!0,!0,!1)},getById:function(t,e){return n["a"].request({url:t+"/"+e,method:"GET"},!0,!0,!1)},updateById:function(t,e,a){return n["a"].request({url:t+"/"+e,method:"PUT",data:a},!0,!0,!1)},delById:function(t,e){return n["a"].request({url:t+"/"+e,method:"DELETE"},!0,!0,!1)}},o="/api/mchInfo",r="/api/passageInfo",i="/api/mchApps",c="/api/agentPassage",u="/api/payOrder",l="/api/passagePayOrder",d="/api/refundOrder",p="/api/payWays",f="/api/transferOrders",h="/api/divisionReceiverGroups",m="/api/divisionReceivers",g="/api/division/records",b="/api/agentHistory",v="/api/agentDayStat",y="/api/agentDivision",S={avatar:n["a"].baseUrl+"/api/ossFiles/avatar",cert:n["a"].baseUrl+"/api/ossFiles/cert"};function w(){return n["a"].request({url:"/api/agentInfo",method:"GET"})}function x(t){return n["a"].request({url:"api/paytest/payways/"+t,method:"GET"})}function D(t){return n["a"].request({url:"/api/paytest/payOrders",method:"POST",data:t})}function _(t){return n["a"].request({url:"/api/current/modifyPwd",method:"put",data:t})}function C(t){return n["a"].request({url:"/api/current/user",method:"put",data:t})}function T(){return n["a"].request({url:"/api/current/user",method:"get"})}function k(){var t=document.location.protocol+"//"+document.location.host;return t="http://agent-api.bainian-pay.com",t.startsWith("https:")?"wss://"+t.replace("https://",""):"ws://"+t.replace("http://","")}function I(t){return n["a"].request({url:"api/mchTransfers/ifCodes/"+t,method:"GET"})}function P(t,e,a){return n["a"].request({url:"/api/mchTransfers/channelUserId",method:"GET",params:{ifCode:t,appId:e,extParam:a}})}function Y(t){return n["a"].request({url:"/api/mchTransfers/doTransfer",method:"POST",data:t},!0,!0,!0)}function q(t){return n["a"].request({url:"/api/mch/payConfigs/ifCodes/"+t,method:"GET"},!0,!0,!0)}function F(t,e,a){return n["a"].request({url:"/api/payOrder/refunds/"+t,method:"POST",data:{refundAmount:e,refundReason:a}})}function O(t){return n["a"].request({url:"/api/division/records/resend/"+t,method:"POST"})}function N(){return n["a"].request({url:"/api/current/getGoogleKey",method:"get"})}function A(t,e){return n["a"].request({url:t,method:"POST",data:e,responseType:"arraybuffer"},!0,!1,!0)}},"20c6":function(t,e,a){"use strict";a("f6ea")},"27fc":function(t,e,a){},"28c1":function(t,e,a){t.exports=a.p+"assets/empty.ac4a76d7.svg"},"2f3a":function(t,e,a){"use strict";a.r(e);var n=function(){var t=this,e=t.$createElement,a=t._self._c||e;return a("div",{attrs:{id:"chart-card"}},[a("div",{staticClass:"chart-top"},[a("div",{staticClass:"chart-item top-left"},[a("div",{staticClass:"chart-data",staticStyle:{position:"relative"}},[t.skeletonIsShow?a("a-skeleton",{staticStyle:{padding:"20px"},attrs:{active:"",loading:!0,paragraph:{rows:6}}}):t._e(),a("div",{directives:[{name:"show",rawName:"v-show",value:!t.skeletonIsShow,expression:"!skeletonIsShow"}]},[t._m(0),a("div",[a("div",{staticClass:"pay-amount-text"},[a("b",{staticClass:"pay-amount"},[t._v(" "+t._s((t.detailData.agentAccountInfo.balance/100).toFixed(2)))])])])])],1)]),a("div",{staticClass:"chart-item top-left",staticStyle:{width:"250px"}},[a("div",{staticClass:"chart-data",staticStyle:{position:"relative"}},[t.skeletonIsShow?a("a-skeleton",{staticStyle:{padding:"20px"},attrs:{active:"",loading:!0,paragraph:{rows:6}}}):t._e(),a("div",{directives:[{name:"show",rawName:"v-show",value:!t.skeletonIsShow,expression:"!skeletonIsShow"}]},[a("div",{staticStyle:{padding:"20px","box-sizing":"border-box","padding-bottom":"10px"}},[a("a-row",{attrs:{justify:"space-between",type:"flex"}},[a("a-col",{attrs:{sm:24}},[a("a-descriptions",[a("a-descriptions-item",{attrs:{label:"代理商号"}},[a("b",[t._v(t._s(t.detailData.agentAccountInfo.agentNo))])])],1)],1),a("a-col",{attrs:{sm:24}},[a("a-descriptions",[a("a-descriptions-item",{attrs:{label:"代理名称"}},[t._v(" "+t._s(t.detailData.agentAccountInfo.agentName)+" ")])],1)],1),a("a-col",{attrs:{sm:24}},[a("a-descriptions",[a("a-descriptions-item",{attrs:{label:"登录名"}},[a("b",[t._v(" "+t._s(t.detailData.agentAccountInfo.ext.loginUserName))])])],1)],1),a("a-col",{attrs:{sm:12}},[a("a-descriptions",[a("a-descriptions-item",{attrs:{label:"商户状态"}},[a("a-badge",{attrs:{status:0===t.detailData.agentAccountInfo.state?"error":"processing",text:0===t.detailData.agentAccountInfo.state?"禁用":"启用"}})],1)],1)],1),a("a-col",{attrs:{sm:24}},[a("a-descriptions",[a("a-descriptions-item",{attrs:{label:"创建时间"}},[t._v(" "+t._s(t.detailData.agentAccountInfo.createdAt)+" ")])],1)],1)],1)],1)])],1)]),a("div",{staticClass:"chart-item top-left",staticStyle:{width:"250px"}},[a("div",{staticClass:"chart-data",staticStyle:{position:"relative"}},[t.skeletonIsShow?a("a-skeleton",{staticStyle:{padding:"20px"},attrs:{active:"",loading:!0,paragraph:{rows:6}}}):t._e(),a("div",{directives:[{name:"show",rawName:"v-show",value:!t.skeletonIsShow,expression:"!skeletonIsShow"}]},[t._m(1),a("div",[a("div",{staticClass:"pay-amount-text"},[a("span",{staticClass:"pay-amount"},[t._v(t._s(t.detailData.agentAccountInfo.ext.mchCount))])])])])],1)]),a("div",{staticClass:"chart-item top-left",staticStyle:{width:"250px"}},[a("div",{staticClass:"chart-data",staticStyle:{position:"relative"}},[t.skeletonIsShow?a("a-skeleton",{staticStyle:{padding:"20px"},attrs:{active:"",loading:!0,paragraph:{rows:6}}}):t._e(),a("div",{directives:[{name:"show",rawName:"v-show",value:!t.skeletonIsShow,expression:"!skeletonIsShow"}]},[t._m(2),a("div",[a("div",{staticClass:"pay-amount-text"},[a("span",{staticClass:"pay-amount"},[t._v(t._s(t.detailData.agentAccountInfo.ext.passageCount))])])])])],1)])]),a("div",{staticClass:"chart-bottom"},[a("div",{staticClass:"chart-item bottom-left"},[a("div",{staticClass:"chart-data"},[a("a-skeleton",{attrs:{active:"",loading:t.skeletonIsShow,paragraph:{rows:6}}}),a("div",{directives:[{name:"show",rawName:"v-show",value:!t.skeletonIsShow,expression:"!skeletonIsShow"}]},[a("div",[t._m(3),a("a-radio-group",{attrs:{"button-style":"solid"},on:{change:t.selectTab},model:{value:t.tableDate,callback:function(e){t.tableDate=e},expression:"tableDate"}},[a("a-radio-button",{attrs:{value:"1"}},[t._v("今日")]),a("a-radio-button",{attrs:{value:"2"}},[t._v("昨日")])],1),a("div",{staticStyle:{"margin-top":"12px"}},[a("JeepayTable",{ref:"infoTable",attrs:{initData:!1,reqTableDataFunc:t.reqTableDataFunc,tableColumns:t.tableColumns,pageSize:10,searchData:t.searchData,rowKey:"mchNo"},on:{btnLoadClose:function(e){t.btnLoading=!1}},scopedSlots:t._u([{key:"nameSlot",fn:function(e){var n=e.record;return[a("b",[t._v("["+t._s(n.mchNo)+"]")]),t._v(" "),a("p",[t._v(t._s(n.mchName))])]}},{key:"stateSlot",fn:function(e){var n=e.record;return[0!==n.ext.stat.totalOrderCount?a("b",[t._v(t._s((n.ext.stat.orderSuccessCount/n.ext.stat.totalOrderCount*100).toFixed(2))+"%")]):a("b",[t._v("0%")])]}},{key:"balanceSlot",fn:function(e){var n=e.record;return[t._v("  "),a("b",{style:{color:n.balance>0?"#4BD884":"#DB4B4B"}},[t._v(t._s((n.balance/100).toFixed(2)))])]}},{key:"feeSlot",fn:function(e){var n=e.record;return[a("b",[t._v(t._s((n.ext.stat.totalAgentIncome/100).toFixed(2)))])]}}])})],1)],1)])],1)]),a("div",{staticClass:"chart-item bottom-left"},[a("div",{staticClass:"chart-data"},[a("a-skeleton",{attrs:{active:"",loading:t.skeletonIsShow,paragraph:{rows:6}}}),a("div",{directives:[{name:"show",rawName:"v-show",value:!t.skeletonIsShow,expression:"!skeletonIsShow"}]},[a("div",[t._m(4),a("a-radio-group",{attrs:{"button-style":"solid"},on:{change:t.selectPassageTab},model:{value:t.tablePassageDate,callback:function(e){t.tablePassageDate=e},expression:"tablePassageDate"}},[a("a-radio-button",{attrs:{value:"1"}},[t._v("今日")]),a("a-radio-button",{attrs:{value:"2"}},[t._v("昨日")])],1),a("div",{staticStyle:{"margin-top":"12px"}},[a("JeepayTable",{ref:"infoPassageTable",attrs:{initData:!1,reqTableDataFunc:t.reqTablePassageDataFunc,tableColumns:t.tablePassageColumns,pageSize:10,searchData:t.searchPassageData,rowKey:"payPassageId"},on:{btnLoadClose:function(e){t.btnLoading=!1}},scopedSlots:t._u([{key:"nameSlot",fn:function(e){var n=e.record;return[a("b",[t._v("["+t._s(n.payPassageId)+"]")]),t._v(" "),a("p",[t._v(t._s(n.payPassageName))])]}},{key:"stateSlot",fn:function(e){var n=e.record;return[0!==n.ext.stat.totalOrderCount?a("b",[t._v(t._s((n.ext.stat.orderSuccessCount/n.ext.stat.totalOrderCount*100).toFixed(2))+"%")]):a("b",[t._v("0%")])]}},{key:"balanceSlot",fn:function(e){var n=e.record;return[t._v("   "),a("b",{staticStyle:{color:"#4BD884"}},[t._v(t._s(void 0===n.ext.stat.totalSuccessAmount?(0).toFixed(2):(n.ext.stat.totalSuccessAmount/100).toFixed(2)))])]}},{key:"feeSlot",fn:function(e){var n=e.record;return[a("b",[t._v(t._s((n.ext.stat.totalAgentIncome/100).toFixed(2)))])]}}])})],1)],1)])],1)])])])},s=[function(){var t=this,e=t.$createElement,a=t._self._c||e;return a("div",{staticClass:"analy-title",staticStyle:{padding:"20px","box-sizing":"border-box","padding-bottom":"10px"}},[a("span",[t._v("账户余额")])])},function(){var t=this,e=t.$createElement,a=t._self._c||e;return a("div",{staticClass:"analy-title",staticStyle:{padding:"20px","box-sizing":"border-box","padding-bottom":"10px"}},[a("span",[t._v("商户数")])])},function(){var t=this,e=t.$createElement,a=t._self._c||e;return a("div",{staticClass:"analy-title",staticStyle:{padding:"20px","box-sizing":"border-box","padding-bottom":"10px"}},[a("span",[t._v("通道数")])])},function(){var t=this,e=t.$createElement,a=t._self._c||e;return a("div",{staticClass:"pay-count-title"},[a("span",{staticClass:"chart-title"},[t._v("我的商户")])])},function(){var t=this,e=t.$createElement,a=t._self._c||e;return a("div",{staticClass:"pay-count-title"},[a("span",{staticClass:"chart-title"},[t._v("我的通道")])])}],o=a("c1df"),r=a.n(o),i=a("0fea"),c=a("ca00"),u=a("909f"),l=a("5d5e"),d=a("f339"),p=[{key:"name",fixed:"left",width:"150px",title:"商户号/名称",scopedSlots:{customRender:"nameSlot"}},{key:"balance",title:"余额(￥)",width:150,scopedSlots:{customRender:"balanceSlot"}},{key:"totalCount",title:"总单量",width:"80px",dataIndex:"ext.stat.totalOrderCount"},{key:"successCount",title:"成交单量",width:"80px",dataIndex:"ext.stat.orderSuccessCount"},{key:"state",title:"成功率",scopedSlots:{customRender:"stateSlot"}},{key:"fee",title:"代理收入(￥)",scopedSlots:{customRender:"feeSlot"}}],f=[{key:"name",fixed:"left",width:"150px",title:"产通道ID/名称",scopedSlots:{customRender:"nameSlot"}},{key:"balance",title:"成交额(￥)",width:100,scopedSlots:{customRender:"balanceSlot"}},{key:"totalCount",title:"总单量",width:"80px",dataIndex:"ext.stat.totalOrderCount"},{key:"successCount",title:"成交单量",width:"80px",dataIndex:"ext.stat.orderSuccessCount"},{key:"state",title:"成功率",width:"100px",scopedSlots:{customRender:"stateSlot"}},{key:"fee",title:"代理收入(￥)",width:"100px",scopedSlots:{customRender:"feeSlot"}}],h={data:function(){return{btnLoading:!1,skeletonIsShow:!0,skeletonReqNum:0,visible:!1,searchData:{date:r()().format("YYYY-MM-DD")},searchPassageData:{date:r()().format("YYYY-MM-DD")},isPayType:!0,mainTips:{helloTitle:""},tableDate:"1",queryDate:"",tablePassageDate:"1",queryPassageDate:"",tableColumns:p,tablePassageColumns:f,chartPassage:null,productMchData:[],detailData:{agentAccountInfo:{agentNo:"",agentName:"",balance:0,createdAt:"",state:0,ext:{loginUserName:""}}}}},components:{JeepayTable:d["a"],JeepayTableColumns:l["a"],empty:u["default"]},methods:{init:function(){var t=this;Object(i["r"])().then((function(e){t.detailData.agentAccountInfo=e,t.skeletonClose(t)})).catch((function(t){}))},reqTableDataFunc:function(t){return i["B"].list(i["d"],t)},reqTablePassageDataFunc:function(t){return i["B"].list(i["e"],t)},moment:r.a,skeletonClose:function(t){t.skeletonIsShow=!1},selectTab:function(){"1"===this.tableDate?this.searchData.date=r()().format("YYYY-MM-DD"):this.searchData.date=r()().subtract(1,"days").format("YYYY-MM-DD"),this.$refs.infoTable.refTable(!0)},selectPassageTab:function(){"1"===this.tablePassageDate?this.searchPassageData.date=r()().format("YYYY-MM-DD"):this.searchPassageData.date=r()().subtract(1,"days").format("YYYY-MM-DD"),this.$refs.infoPassageTable.refTable(!0)}},computed:{},mounted:function(){"1"===this.tableDate?this.queryDate=r()().format("YYYY-MM-DD"):this.queryDate=r()().subtract(1,"days").format("YYYY-MM-DD"),"1"===this.tablePassageDate?this.queryPassageDate=r()().format("YYYY-MM-DD"):this.queryPassageDate=r()().subtract(1,"days").format("YYYY-MM-DD"),this.init(),this.mainTips.helloTitle="".concat(Object(c["b"])(),"，")+this.$store.state.user.loginUsername,this.$refs.infoTable.refTable(!0),this.$refs.infoPassageTable.refTable(!0)}},m=h,g=(a("20c6"),a("2877")),b=Object(g["a"])(m,n,s,!1,null,"73f07e81",null);e["default"]=b.exports},44423:function(t,e,a){"use strict";a("aa30")},"5d5e":function(t,e,a){"use strict";a("d81d");var n,s,o={name:"JeepayTableColumns",render:function(t,e){var a=arguments[0],n=[];if(this.$slots.default.map((function(t){return t.tag&&n.push(t),!1})),n.length<=3)return t("div",{style:"display:flex; justify-content: space-evenly;"},n);for(var s=[n[0],n[1]],o=[],r=2;r<n.length;r++)o.push(a("a-menu-item",[n[r]]));return a("div",{style:"display:flex; justify-content: space-evenly;"},[" ",s,a("a-dropdown",[a("a-button",{style:"",attrs:{type:"link"},class:"ant-dropdown-link"},["更多",a("a-icon",{attrs:{type:"down"}})]),a("a-menu",{slot:"overlay"},[o])])])}},r=o,i=(a("44423"),a("2877")),c=Object(i["a"])(r,n,s,!1,null,"207fd926",null);e["a"]=c.exports},8931:function(t,e,a){"use strict";a("de53")},"909f":function(t,e,a){"use strict";a.r(e);var n=function(){var t=this,e=t.$createElement;t._self._c;return t._m(0)},s=[function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("div",{staticClass:"empty"},[n("img",{staticStyle:{width:"100px"},attrs:{src:a("28c1"),alt:""}}),n("p",{staticStyle:{"padding-right":"5px"}},[t._v("暂无数据")])])}],o={name:"Empty"},r=o,i=(a("8931"),a("2877")),c=Object(i["a"])(r,n,s,!1,null,"455687ae",null);e["default"]=c.exports},aa30:function(t,e,a){},ca00:function(t,e,a){"use strict";a.d(e,"b",(function(){return s})),a.d(e,"a",(function(){return o}));var n=1;function s(){var t=new Date,e=t.getHours();return e<9?"早上好":e<=11?"上午好":e<=13?"中午好":e<20?"下午好":"晚上好"}function o(){return(new Date).getTime()+"_"+n++}},de53:function(t,e,a){},f339:function(t,e,a){"use strict";var n=function(){var t=this,e=t.$createElement,a=t._self._c||e;return a("div",[a("a-table",{attrs:{columns:t.tableColumns,"data-source":t.apiResData.records,pagination:t.pagination,loading:t.showLoading,"row-selection":t.rowSelection,rowKey:t.rowKey,scroll:{x:t.scrollX},customRow:function(e,a){return t.tableRowCrossColor?{style:{"background-color":a%2==0?"#FCFCFC":"#FFFFFF"}}:{}}},on:{change:t.handleTableChange},scopedSlots:t._u([t._l(t.columnsCustomSlots,(function(e){return{key:e.customRender,fn:function(a){return[t._t(e.customRender,null,{record:a})]}}}))],null,!0)})],1)},s=[],o=a("5530"),r=(a("a9e3"),a("d81d"),a("4de4"),{name:"JeepayTable",props:{initData:{type:Boolean,default:!0},tableColumns:Array,reqTableDataFunc:{type:Function},currentChange:{type:Function,default:function(t,e){}},searchData:Object,pageSize:{type:Number,default:10},rowSelection:Object,rowKey:{type:[String,Function]},scrollX:{type:Number,default:800},tableRowCrossColor:{type:Boolean,default:!1}},data:function(){return{apiResData:{total:0,records:[]},iPage:{pageNumber:1,pageSize:this.pageSize},pagination:{total:0,current:1,pageSizeOptions:["10","20","50","100"],pageSize:this.pageSize,showSizeChanger:!0,showTotal:function(t){return"共".concat(t,"条")}},showLoading:!1}},computed:{columnsCustomSlots:function(){return this.tableColumns.filter((function(t){return t.scopedSlots})).map((function(t){return t.scopedSlots}))}},mounted:function(){this.initData&&this.refTable(!0)},methods:{handleTableChange:function(t,e,a){this.pagination=t,this.iPage=Object(o["a"])({pageSize:t.pageSize,pageNumber:t.current,sortField:a.columnKey,sortOrder:a.order},e),this.refTable()},refTable:function(){var t=this,e=arguments.length>0&&void 0!==arguments[0]&&arguments[0],a=this;e&&(this.iPage.pageNumber=1,this.pagination.current=1),this.showLoading=!0,this.reqTableDataFunc(Object.assign({},this.iPage,this.searchData)).then((function(e){t.pagination.total=e.total,t.apiResData=e,t.showLoading=!1,0===e.records.length&&t.iPage.pageNumber>1&&a.$nextTick((function(){var n=e.total/t.iPage.pageSize+(e.total%t.iPage.pageSize===0?0:1);if(0===n)return!1;var s=t.iPage.pageNumber-1>n?n:t.iPage.pageNumber-1;t.iPage.pageNumber=s,t.pagination.current=s,a.refTable(!1)})),a.$emit("btnLoadClose")})).catch((function(e){t.showLoading=!1,a.$emit("btnLoadClose")}))}}}),i=r,c=(a("f705"),a("2877")),u=Object(c["a"])(i,n,s,!1,null,null,null);e["a"]=u.exports},f6ea:function(t,e,a){},f705:function(t,e,a){"use strict";a("27fc")}}]);