(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["chunk-2167ee41"],{"0fea":function(t,e,a){"use strict";a.d(e,"cb",(function(){return r})),a.d(e,"db",(function(){return s})),a.d(e,"d",(function(){return o})),a.d(e,"K",(function(){return u})),a.d(e,"J",(function(){return i})),a.d(e,"N",(function(){return c})),a.d(e,"P",(function(){return d})),a.d(e,"h",(function(){return l})),a.d(e,"f",(function(){return p})),a.d(e,"g",(function(){return f})),a.d(e,"r",(function(){return h})),a.d(e,"o",(function(){return m})),a.d(e,"y",(function(){return y})),a.d(e,"C",(function(){return g})),a.d(e,"c",(function(){return b})),a.d(e,"v",(function(){return S})),a.d(e,"H",(function(){return v})),a.d(e,"u",(function(){return T})),a.d(e,"Q",(function(){return D})),a.d(e,"B",(function(){return C})),a.d(e,"q",(function(){return w})),a.d(e,"i",(function(){return O})),a.d(e,"l",(function(){return P})),a.d(e,"k",(function(){return _})),a.d(e,"j",(function(){return I})),a.d(e,"n",(function(){return R})),a.d(e,"m",(function(){return q})),a.d(e,"F",(function(){return x})),a.d(e,"E",(function(){return k})),a.d(e,"I",(function(){return F})),a.d(e,"s",(function(){return L})),a.d(e,"t",(function(){return E})),a.d(e,"M",(function(){return N})),a.d(e,"L",(function(){return A})),a.d(e,"e",(function(){return z})),a.d(e,"D",(function(){return G})),a.d(e,"p",(function(){return $})),a.d(e,"a",(function(){return j})),a.d(e,"O",(function(){return Y})),a.d(e,"G",(function(){return J})),a.d(e,"x",(function(){return U})),a.d(e,"w",(function(){return M})),a.d(e,"z",(function(){return K})),a.d(e,"A",(function(){return H})),a.d(e,"b",(function(){return X})),a.d(e,"hb",(function(){return W})),a.d(e,"U",(function(){return Q})),a.d(e,"bb",(function(){return V})),a.d(e,"eb",(function(){return Z})),a.d(e,"X",(function(){return tt})),a.d(e,"W",(function(){return et})),a.d(e,"gb",(function(){return at})),a.d(e,"fb",(function(){return nt})),a.d(e,"Y",(function(){return rt})),a.d(e,"V",(function(){return st})),a.d(e,"S",(function(){return ot})),a.d(e,"T",(function(){return ut})),a.d(e,"Z",(function(){return it})),a.d(e,"ab",(function(){return ct})),a.d(e,"R",(function(){return dt}));var n=a("4667"),r={list:function(t,e){return n["a"].request({url:t,method:"GET",params:e},!0,!0,!1)},add:function(t,e){return n["a"].request({url:t,method:"POST",data:e},!0,!0,!1)},getById:function(t,e){return n["a"].request({url:t+"/"+e,method:"GET"},!0,!0,!1)},updateById:function(t,e,a){return n["a"].request({url:t+"/"+e,method:"PUT",data:a},!0,!0,!1)},delById:function(t,e){return n["a"].request({url:t+"/"+e,method:"DELETE"},!0,!0,!1)},postNormal:function(t,e){return n["a"].request({url:t+"/"+e,method:"POST"},!0,!0,!0)},postDataNormal:function(t,e,a){return n["a"].request({url:t+"/"+e,method:"POST",data:a},!0,!0,!0)},getNormal:function(t,e){return n["a"].request({url:t+"/"+e,method:"GET"},!0,!0,!0)}},s={list:function(t,e){return n["a"].request({url:t,method:"GET",params:e},!0,!0,!0)},add:function(t,e){return n["a"].request({url:t,method:"POST",data:e},!0,!0,!0)},getById:function(t,e){return n["a"].request({url:t+"/"+e,method:"GET"},!0,!0,!0)},updateById:function(t,e,a){return n["a"].request({url:t+"/"+e,method:"PUT",data:a},!0,!0,!0)},delById:function(t,e){return n["a"].request({url:t+"/"+e,method:"DELETE"},!0,!0,!0)}},o="/api/sysEnts",u="/api/sysRoles",i="/api/sysRoleEntRelas",c="/api/sysUsers",d="/api/sysUserRoleRelas",l="/api/isvInfo",p="/api/isvBalance",f="/api/agentHistory",h="/api/mchInfo",m="/api/mchBalance",y="/api/mchStatInfo",g="/api/passageStatInfo",b="/api/agentStatInfo",S="/api/mchProductInfo",v="/api/productMchInfo",T="/api/mchPassageInfo",D="/api/payOrder",C="/api/passageMchInfo",w="/api/mchHistory",O="/api/mchApps",P="/api/mchAppsList",_="/api/passageHistory",I="/api/mchAppsBalance",R="/api/mchAppsBalanceReset",q="/api/mchAppsMultipleSet",x="/api/payOrder",k="/api/payOrderForceList",F="/api/refundOrder",L="/api/mchNotify",E="/api/mchNotifyResend/resendAll",N="api/sysLog",A="api/sysConfigs",B="api/mainChart",z="/api/payIfDefines",G="/api/payWays",$="/api/mchDivision",j="/api/agentDivision",Y="/api/transferOrders",J="/api/platStat",U="/api/mchStat",M="/api/mchProductStat",K="/api/passageStat",H="/api/productStat",X="/api/agentStat",W={avatar:n["a"].baseUrl+"/api/ossFiles/avatar",ifBG:n["a"].baseUrl+"/api/ossFiles/ifBG",cert:n["a"].baseUrl+"/api/ossFiles/cert"};function Q(t){return n["a"].request({url:"/api/sysEnts/showTree?sysType="+t,method:"GET"})}function V(t,e,a){return n["a"].request({url:"/api/payOrder/refunds/"+t,method:"POST",data:{refundAmount:e,refundReason:a}})}function Z(t,e){return n["a"].request({url:"api/sysUserRoleRelas/relas/"+t,method:"POST",data:{roleIdListStr:JSON.stringify(e)}})}function tt(){return n["a"].request({url:B+"/twoDayCount",method:"GET"})}function et(){return n["a"].request({url:B+"/realTimeCount",method:"GET"})}function at(t){return n["a"].request({url:"/api/current/modifyPwd",method:"put",data:t})}function nt(t){return n["a"].request({url:"/api/current/user",method:"put",data:t})}function rt(){return n["a"].request({url:"/api/current/user",method:"get"})}function st(){return n["a"].request({url:"/api/current/getGoogleKey",method:"get"})}function ot(t){return n["a"].request({url:A+"/"+t,method:"GET"})}function ut(t,e){return n["a"].request({url:"/api/sysEnts/bySysType",method:"GET",params:{entId:t,sysType:e}})}function it(t){return n["a"].request({url:"/api/mchNotify/resend/"+t,method:"POST"})}function ct(t){return n["a"].request({url:"/api/passageTest/doPay",method:"POST",data:t})}function dt(t,e){return n["a"].request({url:t,method:"POST",data:e,responseType:"arraybuffer"},!0,!1,!0)}},"27fc":function(t,e,a){},3001:function(t,e,a){},"3cbe":function(t,e,a){"use strict";a("a415")},"4f53":function(t,e,a){"use strict";var n=function(){var t=this,e=t.$createElement,a=t._self._c||e;return a("div",{staticClass:"jee-text-up table-head-layout"},[a("a-input",{attrs:{required:"required",value:t.msg},on:{input:function(e){return t.$emit("input",e.target.value)}}}),a("label",[t._v(t._s(t.placeholder))])],1)},r=[],s={name:"JeepayTextUp",props:{msg:{type:String,default:""},placeholder:{type:String,default:""}}},o=s,u=(a("8bf8"),a("2877")),i=Object(u["a"])(o,n,r,!1,null,"4708ca2b",null);e["a"]=i.exports},5277:function(t,e,a){"use strict";a.r(e);var n=function(){var t=this,e=t.$createElement,a=t._self._c||e;return a("page-header-wrapper",[a("a-card",{staticStyle:{"margin-top":"20px"}},[a("div",{staticClass:"table-page-search-wrapper"},[a("a-form",{staticClass:"table-head-ground",attrs:{layout:"inline"}},[a("div",{staticClass:"table-layer"},[a("a-form-item",{staticClass:"table-head-layout",staticStyle:{"max-width":"350px","min-width":"300px"},attrs:{label:""}},[a("a-range-picker",{attrs:{"show-time":{format:""},format:"YYYY-MM-DD","disabled-date":t.disabledDate,ranges:t.ranges},on:{change:t.onChange},model:{value:t.selectedRange,callback:function(e){t.selectedRange=e},expression:"selectedRange"}},[a("a-icon",{attrs:{slot:"suffixIcon",type:"sync"},slot:"suffixIcon"})],1)],1),a("jeepay-text-up",{attrs:{placeholder:"通道名",msg:t.searchData.payPassageName},model:{value:t.searchData.payPassageName,callback:function(e){t.$set(t.searchData,"payPassageName",e)},expression:"searchData.payPassageName"}}),a("a-form-model-item",{staticClass:"table-head-layout",attrs:{label:""}},[a("a-select",{attrs:{allowClear:!0,placeholder:"对应通道","show-search":"","option-filter-prop":"children"},model:{value:t.searchData.payPassageId,callback:function(e){t.$set(t.searchData,"payPassageId",e)},expression:"searchData.payPassageId"}},t._l(t.payPassageList,(function(e){return a("a-select-option",{key:e.payPassageId,attrs:{value:e.payPassageId}},[t._v(" "+t._s(e.payPassageName+" [ ID: "+e.payPassageId+" ]")+" ")])})),1)],1),a("a-form-model-item",{staticClass:"table-head-layout",attrs:{label:""}},[a("a-select",{attrs:{allowClear:!0,placeholder:"对应产品","show-search":"","option-filter-prop":"children"},model:{value:t.searchData.productId,callback:function(e){t.$set(t.searchData,"productId",e)},expression:"searchData.productId"}},t._l(t.productList,(function(e){return a("a-select-option",{key:e.productId,attrs:{value:e.productId}},[t._v(" "+t._s(e.productName+" [ ID: "+e.productId+" ]")+" ")])})),1)],1),a("span",{staticClass:"table-page-search-submitButtons"},[a("a-button",{attrs:{type:"primary",icon:"search",loading:t.btnLoading},on:{click:t.queryFunc}},[t._v("搜索")]),a("a-button",{staticStyle:{"margin-left":"8px"},attrs:{icon:"reload"},on:{click:t.resetSearch}},[t._v("重置")])],1)],1)])],1),a("JeepayTable",{ref:"infoTable",attrs:{initData:!1,reqTableDataFunc:t.reqTableDataFunc,tableColumns:t.tableColumns,searchData:t.searchData,pageSize:100,rowKey:"statisticsPassageId"},on:{btnLoadClose:function(e){t.btnLoading=!1}},scopedSlots:t._u([{key:"passageCostSlot",fn:function(e){var n=e.record;return[a("b",{staticStyle:{color:"#4BD884"}},[t._v(t._s((n.totalPassageCost/100).toFixed(2)))])]}},{key:"dateSlot",fn:function(e){var n=e.record;return[a("b",[t._v(t._s(n.createdAt.substring(0,10)))])]}},{key:"passageSlot",fn:function(e){var n=e.record;return[a("b",{staticStyle:{color:"#1A79FF"}},[t._v("["+t._s(n.payPassageId)+"]")]),t._v(" "),a("span",[t._v(t._s(n.payPassageName))])]}},{key:"productSlot",fn:function(e){var n=e.record;return[a("b",{staticStyle:{color:"#1A79FF"}},[t._v("["+t._s(n.productId)+"]")]),a("span",[t._v(t._s(n.productName))])]}},{key:"amountSlot",fn:function(e){var n=e.record;return[a("b",[t._v(t._s((n.totalSuccessAmount/100).toFixed(2)))])]}},{key:"successRateSlot",fn:function(e){var n=e.record;return[a("b",[t._v(t._s((n.orderSuccessCount/n.totalOrderCount*100).toFixed(2))+"%")])]}},{key:"payOrderAmountSlot",fn:function(e){var n=e.record;return[a("span",[t._v(t._s(n.totalOrderCount))])]}},{key:"payOrderSuccessCountSlot",fn:function(e){var n=e.record;return[a("span",[t._v(t._s(n.orderSuccessCount))])]}}])})],1)],1)},r=[],s=a("f339"),o=a("4f53"),u=a("5d5e"),i=a("0fea"),c=a("c1df"),d=a.n(c),l=[{key:"date",title:"日期",width:"150px",fixed:"left",scopedSlots:{customRender:"dateSlot"}},{key:"passage",scopedSlots:{customRender:"passageSlot"},width:"300px",fixed:"left",title:"通道类型"},{key:"amount",width:"200px",title:"成交金额(￥)",fixed:"left",scopedSlots:{customRender:"amountSlot"}},{key:"passageCost",title:"通道成本(￥)",width:"200px",fixed:"left",scopedSlots:{customRender:"passageCostSlot"}},{key:"product",title:"产品类型",scopedSlots:{customRender:"productSlot"}},{key:"payOrderAmount",width:"100px",title:"订单总笔数",scopedSlots:{customRender:"payOrderAmountSlot"}},{key:"payOrderAmount1",width:"100px",title:"成交笔数",scopedSlots:{customRender:"payOrderSuccessCountSlot"}},{key:"successRate",title:"支付成功率",scopedSlots:{customRender:"successRateSlot"}}],p={name:"DayStatList",components:{JeepayTable:s["a"],JeepayTableColumns:u["a"],JeepayTextUp:o["a"]},data:function(){return{btnLoading:!1,tableColumns:l,searchData:{},selectedRange:[],productList:[],payPassageList:[],productListOptions:null,ranges:{"今天":[d()().startOf("day"),d()().endOf("day")],"昨天":[d()().subtract(1,"day").startOf("day"),d()().subtract(1,"day").endOf("day")],"近一周":[d()().subtract(1,"week").startOf("day"),d()().endOf("day")]}}},mounted:function(){var t=this;i["cb"].list(i["D"],{pageSize:-1}).then((function(e){t.productList=e.records})),i["cb"].list(i["i"],{pageSize:-1}).then((function(e){t.payPassageList=e.records})),this.selectedRange=[d()().startOf("day"),d()().endOf("day")],this.searchData.createdStart=this.selectedRange[0].format("YYYY-MM-DD"),this.searchData.createdEnd=this.selectedRange[1].format("YYYY-MM-DD"),this.$refs.infoTable.refTable(!0)},methods:{queryFunc:function(){this.btnLoading=!0,this.$refs.infoTable.refTable(!0)},reqTableDataFunc:function(t){return i["cb"].list(i["z"],t)},disabledDate:function(t){return t&&t>d()().endOf("day")},onChange:function(t,e){this.searchData.createdStart=e[0],this.searchData.createdEnd=e[1],this.$refs.infoTable.refTable(!0)},resetSearch:function(){this.searchData={},this.selectedRange=[]}}},f=p,h=(a("665b"),a("2877")),m=Object(h["a"])(f,n,r,!1,null,"0e29a464",null);e["default"]=m.exports},"5d5e":function(t,e,a){"use strict";a("d81d");var n,r,s={name:"JeepayTableColumns",render:function(t,e){var a=arguments[0],n=[];if(this.$slots.default.map((function(t){return t.tag&&n.push(t),!1})),n.length<=4)return t("div",{style:"display:flex; justify-content: space-evenly;"},n);for(var r=[n[0],n[1],n[2]],s=[],o=3;o<n.length;o++)s.push(a("a-menu-item",[n[o]]));return a("div",{style:"display:flex; justify-content: space-evenly;"},[" ",r,a("a-dropdown",[a("a-button",{class:"ant-dropdown-link",attrs:{type:"link"},style:""},["更多",a("a-icon",{attrs:{type:"down"}})]),a("a-menu",{slot:"overlay"},[s])])])}},o=s,u=(a("3cbe"),a("2877")),i=Object(u["a"])(o,n,r,!1,null,"d8995c5c",null);e["a"]=i.exports},"665b":function(t,e,a){"use strict";a("3001")},"8bf8":function(t,e,a){"use strict";a("eaa4")},a415:function(t,e,a){},eaa4:function(t,e,a){},f339:function(t,e,a){"use strict";var n=function(){var t=this,e=t.$createElement,a=t._self._c||e;return a("div",[a("a-table",{attrs:{columns:t.tableColumns,"data-source":t.apiResData.records,pagination:t.pagination,loading:t.showLoading,"row-selection":t.rowSelection,rowKey:t.rowKey,scroll:{x:t.scrollX},customRow:function(e,a){return t.tableRowCrossColor?{style:{"background-color":a%2==0?"#FCFCFC":"#FFFFFF"}}:{}}},on:{change:t.handleTableChange},scopedSlots:t._u([t._l(t.columnsCustomSlots,(function(e){return{key:e.customRender,fn:function(a){return[t._t(e.customRender,null,{record:a})]}}}))],null,!0)})],1)},r=[],s=a("5530"),o=(a("a9e3"),a("d81d"),a("4de4"),{name:"JeepayTable",props:{initData:{type:Boolean,default:!0},tableColumns:Array,reqTableDataFunc:{type:Function},currentChange:{type:Function,default:function(t,e){}},searchData:Object,pageSize:{type:Number,default:20},rowSelection:Object,rowKey:{type:[String,Function]},scrollX:{type:Number,default:500},tableRowCrossColor:{type:Boolean,default:!1}},data:function(){return{apiResData:{total:0,records:[]},iPage:{pageNumber:1,pageSize:this.pageSize},pagination:{total:0,current:1,pageSizeOptions:["10","20","50","100"],pageSize:this.pageSize,showSizeChanger:!0,showTotal:function(t){return"共".concat(t,"条")}},showLoading:!1}},computed:{columnsCustomSlots:function(){return this.tableColumns.filter((function(t){return t.scopedSlots})).map((function(t){return t.scopedSlots}))}},mounted:function(){this.initData&&this.refTable(!0)},methods:{handleTableChange:function(t,e,a){this.pagination=t,this.iPage=Object(s["a"])({pageSize:t.pageSize,pageNumber:t.current,sortField:a.columnKey,sortOrder:a.order},e),this.refTable()},refTable:function(){var t=this,e=arguments.length>0&&void 0!==arguments[0]&&arguments[0],a=this;e&&(this.iPage.pageNumber=1,this.pagination.current=1),this.showLoading=!0,this.reqTableDataFunc(Object.assign({},this.iPage,this.searchData)).then((function(e){t.pagination.total=e.total,t.apiResData=e,t.showLoading=!1,0===e.records.length&&t.iPage.pageNumber>1&&a.$nextTick((function(){var n=e.total/t.iPage.pageSize+(e.total%t.iPage.pageSize===0?0:1);if(0===n)return!1;var r=t.iPage.pageNumber-1>n?n:t.iPage.pageNumber-1;t.iPage.pageNumber=r,t.pagination.current=r,a.refTable(!1)})),a.$emit("btnLoadClose")})).catch((function(e){t.showLoading=!1,a.$emit("btnLoadClose")}))}}}),u=o,i=(a("f705"),a("2877")),c=Object(i["a"])(u,n,r,!1,null,null,null);e["a"]=c.exports},f705:function(t,e,a){"use strict";a("27fc")}}]);