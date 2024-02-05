(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["chunk-4cd32e9e"],{"0544":function(t,e,n){"use strict";n.r(e);var a=function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("page-header-wrapper",[n("a-card",{staticStyle:{"margin-top":"20px"}},[n("div",{staticClass:"table-page-search-wrapper"},[n("a-form",{staticClass:"table-head-ground",attrs:{layout:"inline"}},[n("div",{staticClass:"table-layer"},[n("a-form-item",{staticClass:"table-head-layout",staticStyle:{"max-width":"350px","min-width":"300px"},attrs:{label:""}},[n("a-range-picker",{attrs:{"show-time":{format:""},format:"YYYY-MM-DD","disabled-date":t.disabledDate,ranges:t.ranges},on:{change:t.onChange},model:{value:t.selectedRange,callback:function(e){t.selectedRange=e},expression:"selectedRange"}},[n("a-icon",{attrs:{slot:"suffixIcon",type:"sync"},slot:"suffixIcon"})],1)],1),n("a-form-model-item",{staticClass:"table-head-layout",attrs:{label:""}},[n("a-select",{attrs:{allowClear:!0,placeholder:"对应产品","show-search":"","option-filter-prop":"children"},model:{value:t.searchData.productId,callback:function(e){t.$set(t.searchData,"productId",e)},expression:"searchData.productId"}},t._l(t.productList,(function(e){return n("a-select-option",{key:e.productId,attrs:{value:e.productId}},[t._v(" "+t._s(e.productName+" [ ID: "+e.productId+" ]")+" ")])})),1)],1),n("span",{staticClass:"table-page-search-submitButtons"},[n("a-button",{attrs:{type:"primary",icon:"search",loading:t.btnLoading},on:{click:t.queryFunc}},[t._v("搜索")]),n("a-button",{staticStyle:{"margin-left":"8px"},attrs:{icon:"reload"},on:{click:t.resetSearch}},[t._v("重置")])],1)],1)])],1),n("JeepayTable",{ref:"infoTable",attrs:{initData:!1,reqTableDataFunc:t.reqTableDataFunc,tableColumns:t.tableColumns,searchData:t.searchData,pageSize:100,rowKey:"statisticsProductId"},on:{btnLoadClose:function(e){t.btnLoading=!1}},scopedSlots:t._u([{key:"productTotalSlot",fn:function(e){var a=e.record;return[n("b",[t._v(t._s((a.totalAmount/100).toFixed(2)))])]}},{key:"dateSlot",fn:function(e){var a=e.record;return[n("b",[t._v(t._s(a.createdAt.substring(0,10)))])]}},{key:"productSlot",fn:function(e){var a=e.record;return[n("b",{staticStyle:{color:"#1A79FF"}},[t._v("["+t._s(a.productId)+"]")]),t._v(" "),n("span",[t._v(t._s(a.productName))])]}},{key:"amountSlot",fn:function(e){var a=e.record;return[n("b",{staticStyle:{color:"#4BD884"}},[t._v(t._s((a.totalSuccessAmount/100).toFixed(2)))])]}},{key:"successRateSlot",fn:function(e){var a=e.record;return[n("b",[t._v(t._s((a.orderSuccessCount/a.totalOrderCount*100).toFixed(2))+"%")])]}},{key:"payOrderAmountSlot",fn:function(e){var a=e.record;return[n("span",[t._v(t._s(a.totalOrderCount))])]}},{key:"payOrderSuccessCountSlot",fn:function(e){var a=e.record;return[n("span",[t._v(t._s(a.orderSuccessCount))])]}}])})],1)],1)},r=[],o=n("f339"),u=n("4f53"),i=n("5d5e"),s=n("0fea"),c=n("c1df"),d=n.n(c),l=[{key:"date",title:"日期",width:"150px",fixed:"left",scopedSlots:{customRender:"dateSlot"}},{key:"product",title:"产品类型",width:"350px",fixed:"left",scopedSlots:{customRender:"productSlot"}},{key:"amount",width:"200px",title:"成交金额(￥)",scopedSlots:{customRender:"amountSlot"}},{key:"productTotal",title:"订单总额(￥)",width:"200px",scopedSlots:{customRender:"productTotalSlot"}},{key:"successRate",title:"支付成功率",width:"200px",scopedSlots:{customRender:"successRateSlot"}},{key:"payOrderAmount",width:"100px",title:"订单总笔数",scopedSlots:{customRender:"payOrderAmountSlot"}},{key:"payOrderAmount1",width:"100px",title:"成交笔数",scopedSlots:{customRender:"payOrderSuccessCountSlot"}}],f={name:"DayStatList",components:{JeepayTable:o["a"],JeepayTableColumns:i["a"],JeepayTextUp:u["a"]},data:function(){return{btnLoading:!1,tableColumns:l,searchData:{},selectedRange:[],productList:[],ranges:{"今天":[d()().startOf("day"),d()().endOf("day")],"昨天":[d()().subtract(1,"day").startOf("day"),d()().subtract(1,"day").endOf("day")],"近一周":[d()().subtract(1,"week").startOf("day"),d()().endOf("day")]}}},mounted:function(){var t=this;s["cb"].list(s["D"],{pageSize:-1}).then((function(e){t.productList=e.records})),this.selectedRange=[d()().startOf("day"),d()().endOf("day")],this.searchData.createdStart=this.selectedRange[0].format("YYYY-MM-DD"),this.searchData.createdEnd=this.selectedRange[1].format("YYYY-MM-DD"),this.$refs.infoTable.refTable(!0)},methods:{queryFunc:function(){this.btnLoading=!0,this.$refs.infoTable.refTable(!0)},reqTableDataFunc:function(t){return s["cb"].list(s["A"],t)},disabledDate:function(t){return t&&t>d()().endOf("day")},onChange:function(t,e){this.searchData.createdStart=e[0],this.searchData.createdEnd=e[1],this.$refs.infoTable.refTable(!0)},resetSearch:function(){this.searchData={},this.selectedRange=[]}}},p=f,h=(n("5130"),n("2877")),m=Object(h["a"])(p,a,r,!1,null,"cb7ea45e",null);e["default"]=m.exports},"0ec8":function(t,e,n){},"0fea":function(t,e,n){"use strict";n.d(e,"cb",(function(){return r})),n.d(e,"db",(function(){return o})),n.d(e,"d",(function(){return u})),n.d(e,"K",(function(){return i})),n.d(e,"J",(function(){return s})),n.d(e,"N",(function(){return c})),n.d(e,"P",(function(){return d})),n.d(e,"h",(function(){return l})),n.d(e,"f",(function(){return f})),n.d(e,"g",(function(){return p})),n.d(e,"r",(function(){return h})),n.d(e,"o",(function(){return m})),n.d(e,"y",(function(){return y})),n.d(e,"C",(function(){return b})),n.d(e,"c",(function(){return g})),n.d(e,"v",(function(){return S})),n.d(e,"H",(function(){return T})),n.d(e,"u",(function(){return v})),n.d(e,"Q",(function(){return w})),n.d(e,"B",(function(){return C})),n.d(e,"q",(function(){return D})),n.d(e,"i",(function(){return O})),n.d(e,"l",(function(){return q})),n.d(e,"k",(function(){return R})),n.d(e,"j",(function(){return _})),n.d(e,"n",(function(){return x})),n.d(e,"m",(function(){return k})),n.d(e,"F",(function(){return F})),n.d(e,"E",(function(){return I})),n.d(e,"I",(function(){return P})),n.d(e,"s",(function(){return E})),n.d(e,"t",(function(){return L})),n.d(e,"M",(function(){return N})),n.d(e,"L",(function(){return A})),n.d(e,"e",(function(){return z})),n.d(e,"D",(function(){return G})),n.d(e,"p",(function(){return Y})),n.d(e,"a",(function(){return j})),n.d(e,"O",(function(){return $})),n.d(e,"G",(function(){return J})),n.d(e,"x",(function(){return U})),n.d(e,"w",(function(){return M})),n.d(e,"z",(function(){return K})),n.d(e,"A",(function(){return H})),n.d(e,"b",(function(){return X})),n.d(e,"hb",(function(){return W})),n.d(e,"U",(function(){return Q})),n.d(e,"bb",(function(){return V})),n.d(e,"eb",(function(){return Z})),n.d(e,"X",(function(){return tt})),n.d(e,"W",(function(){return et})),n.d(e,"gb",(function(){return nt})),n.d(e,"fb",(function(){return at})),n.d(e,"Y",(function(){return rt})),n.d(e,"V",(function(){return ot})),n.d(e,"S",(function(){return ut})),n.d(e,"T",(function(){return it})),n.d(e,"Z",(function(){return st})),n.d(e,"ab",(function(){return ct})),n.d(e,"R",(function(){return dt}));var a=n("4667"),r={list:function(t,e){return a["a"].request({url:t,method:"GET",params:e},!0,!0,!1)},add:function(t,e){return a["a"].request({url:t,method:"POST",data:e},!0,!0,!1)},getById:function(t,e){return a["a"].request({url:t+"/"+e,method:"GET"},!0,!0,!1)},updateById:function(t,e,n){return a["a"].request({url:t+"/"+e,method:"PUT",data:n},!0,!0,!1)},delById:function(t,e){return a["a"].request({url:t+"/"+e,method:"DELETE"},!0,!0,!1)},postNormal:function(t,e){return a["a"].request({url:t+"/"+e,method:"POST"},!0,!0,!0)},postDataNormal:function(t,e,n){return a["a"].request({url:t+"/"+e,method:"POST",data:n},!0,!0,!0)},getNormal:function(t,e){return a["a"].request({url:t+"/"+e,method:"GET"},!0,!0,!0)}},o={list:function(t,e){return a["a"].request({url:t,method:"GET",params:e},!0,!0,!0)},add:function(t,e){return a["a"].request({url:t,method:"POST",data:e},!0,!0,!0)},getById:function(t,e){return a["a"].request({url:t+"/"+e,method:"GET"},!0,!0,!0)},updateById:function(t,e,n){return a["a"].request({url:t+"/"+e,method:"PUT",data:n},!0,!0,!0)},delById:function(t,e){return a["a"].request({url:t+"/"+e,method:"DELETE"},!0,!0,!0)}},u="/api/sysEnts",i="/api/sysRoles",s="/api/sysRoleEntRelas",c="/api/sysUsers",d="/api/sysUserRoleRelas",l="/api/isvInfo",f="/api/isvBalance",p="/api/agentHistory",h="/api/mchInfo",m="/api/mchBalance",y="/api/mchStatInfo",b="/api/passageStatInfo",g="/api/agentStatInfo",S="/api/mchProductInfo",T="/api/productMchInfo",v="/api/mchPassageInfo",w="/api/payOrder",C="/api/passageMchInfo",D="/api/mchHistory",O="/api/mchApps",q="/api/mchAppsList",R="/api/passageHistory",_="/api/mchAppsBalance",x="/api/mchAppsBalanceReset",k="/api/mchAppsMultipleSet",F="/api/payOrder",I="/api/payOrderForceList",P="/api/refundOrder",E="/api/mchNotify",L="/api/mchNotifyResend/resendAll",N="api/sysLog",A="api/sysConfigs",B="api/mainChart",z="/api/payIfDefines",G="/api/payWays",Y="/api/mchDivision",j="/api/agentDivision",$="/api/transferOrders",J="/api/platStat",U="/api/mchStat",M="/api/mchProductStat",K="/api/passageStat",H="/api/productStat",X="/api/agentStat",W={avatar:a["a"].baseUrl+"/api/ossFiles/avatar",ifBG:a["a"].baseUrl+"/api/ossFiles/ifBG",cert:a["a"].baseUrl+"/api/ossFiles/cert"};function Q(t){return a["a"].request({url:"/api/sysEnts/showTree?sysType="+t,method:"GET"})}function V(t,e,n){return a["a"].request({url:"/api/payOrder/refunds/"+t,method:"POST",data:{refundAmount:e,refundReason:n}})}function Z(t,e){return a["a"].request({url:"api/sysUserRoleRelas/relas/"+t,method:"POST",data:{roleIdListStr:JSON.stringify(e)}})}function tt(){return a["a"].request({url:B+"/twoDayCount",method:"GET"})}function et(){return a["a"].request({url:B+"/realTimeCount",method:"GET"})}function nt(t){return a["a"].request({url:"/api/current/modifyPwd",method:"put",data:t})}function at(t){return a["a"].request({url:"/api/current/user",method:"put",data:t})}function rt(){return a["a"].request({url:"/api/current/user",method:"get"})}function ot(){return a["a"].request({url:"/api/current/getGoogleKey",method:"get"})}function ut(t){return a["a"].request({url:A+"/"+t,method:"GET"})}function it(t,e){return a["a"].request({url:"/api/sysEnts/bySysType",method:"GET",params:{entId:t,sysType:e}})}function st(t){return a["a"].request({url:"/api/mchNotify/resend/"+t,method:"POST"})}function ct(t){return a["a"].request({url:"/api/passageTest/doPay",method:"POST",data:t})}function dt(t,e){return a["a"].request({url:t,method:"POST",data:e,responseType:"arraybuffer"},!0,!1,!0)}},"27fc":function(t,e,n){},"3cbe":function(t,e,n){"use strict";n("a415")},"4f53":function(t,e,n){"use strict";var a=function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("div",{staticClass:"jee-text-up table-head-layout"},[n("a-input",{attrs:{required:"required",value:t.msg},on:{input:function(e){return t.$emit("input",e.target.value)}}}),n("label",[t._v(t._s(t.placeholder))])],1)},r=[],o={name:"JeepayTextUp",props:{msg:{type:String,default:""},placeholder:{type:String,default:""}}},u=o,i=(n("8bf8"),n("2877")),s=Object(i["a"])(u,a,r,!1,null,"4708ca2b",null);e["a"]=s.exports},5130:function(t,e,n){"use strict";n("0ec8")},"5d5e":function(t,e,n){"use strict";n("d81d");var a,r,o={name:"JeepayTableColumns",render:function(t,e){var n=arguments[0],a=[];if(this.$slots.default.map((function(t){return t.tag&&a.push(t),!1})),a.length<=4)return t("div",{style:"display:flex; justify-content: space-evenly;"},a);for(var r=[a[0],a[1],a[2]],o=[],u=3;u<a.length;u++)o.push(n("a-menu-item",[a[u]]));return n("div",{style:"display:flex; justify-content: space-evenly;"},[" ",r,n("a-dropdown",[n("a-button",{class:"ant-dropdown-link",attrs:{type:"link"},style:""},["更多",n("a-icon",{attrs:{type:"down"}})]),n("a-menu",{slot:"overlay"},[o])])])}},u=o,i=(n("3cbe"),n("2877")),s=Object(i["a"])(u,a,r,!1,null,"d8995c5c",null);e["a"]=s.exports},"8bf8":function(t,e,n){"use strict";n("eaa4")},a415:function(t,e,n){},eaa4:function(t,e,n){},f339:function(t,e,n){"use strict";var a=function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("div",[n("a-table",{attrs:{columns:t.tableColumns,"data-source":t.apiResData.records,pagination:t.pagination,loading:t.showLoading,"row-selection":t.rowSelection,rowKey:t.rowKey,scroll:{x:t.scrollX},customRow:function(e,n){return t.tableRowCrossColor?{style:{"background-color":n%2==0?"#FCFCFC":"#FFFFFF"}}:{}}},on:{change:t.handleTableChange},scopedSlots:t._u([t._l(t.columnsCustomSlots,(function(e){return{key:e.customRender,fn:function(n){return[t._t(e.customRender,null,{record:n})]}}}))],null,!0)})],1)},r=[],o=n("5530"),u=(n("a9e3"),n("d81d"),n("4de4"),{name:"JeepayTable",props:{initData:{type:Boolean,default:!0},tableColumns:Array,reqTableDataFunc:{type:Function},currentChange:{type:Function,default:function(t,e){}},searchData:Object,pageSize:{type:Number,default:20},rowSelection:Object,rowKey:{type:[String,Function]},scrollX:{type:Number,default:500},tableRowCrossColor:{type:Boolean,default:!1}},data:function(){return{apiResData:{total:0,records:[]},iPage:{pageNumber:1,pageSize:this.pageSize},pagination:{total:0,current:1,pageSizeOptions:["10","20","50","100"],pageSize:this.pageSize,showSizeChanger:!0,showTotal:function(t){return"共".concat(t,"条")}},showLoading:!1}},computed:{columnsCustomSlots:function(){return this.tableColumns.filter((function(t){return t.scopedSlots})).map((function(t){return t.scopedSlots}))}},mounted:function(){this.initData&&this.refTable(!0)},methods:{handleTableChange:function(t,e,n){this.pagination=t,this.iPage=Object(o["a"])({pageSize:t.pageSize,pageNumber:t.current,sortField:n.columnKey,sortOrder:n.order},e),this.refTable()},refTable:function(){var t=this,e=arguments.length>0&&void 0!==arguments[0]&&arguments[0],n=this;e&&(this.iPage.pageNumber=1,this.pagination.current=1),this.showLoading=!0,this.reqTableDataFunc(Object.assign({},this.iPage,this.searchData)).then((function(e){t.pagination.total=e.total,t.apiResData=e,t.showLoading=!1,0===e.records.length&&t.iPage.pageNumber>1&&n.$nextTick((function(){var a=e.total/t.iPage.pageSize+(e.total%t.iPage.pageSize===0?0:1);if(0===a)return!1;var r=t.iPage.pageNumber-1>a?a:t.iPage.pageNumber-1;t.iPage.pageNumber=r,t.pagination.current=r,n.refTable(!1)})),n.$emit("btnLoadClose")})).catch((function(e){t.showLoading=!1,n.$emit("btnLoadClose")}))}}}),i=u,s=(n("f705"),n("2877")),c=Object(s["a"])(i,a,r,!1,null,null,null);e["a"]=c.exports},f705:function(t,e,n){"use strict";n("27fc")}}]);