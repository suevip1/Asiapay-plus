(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["chunk-fc38f99e","chunk-7d4192d8"],{"04d4":function(t,e,n){"use strict";n.r(e);var r=function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("page-header-wrapper",[n("a-card",[n("a-table",{attrs:{columns:t.tableColumns,"data-source":t.dataSource,pagination:!1,loading:t.loading,rowKey:"entId",scroll:{x:1450}},scopedSlots:t._u([{key:"stateSlot",fn:function(e){return[n("JeepayTableColState",{attrs:{state:e.state,showSwitchType:t.$access("ENT_UR_ROLE_ENT_EDIT"),onChange:function(n){return t.updateState(e.entId,n)}}})]}},{key:"opSlot",fn:function(e){return[n("JeepayTableColumns",[t.$access("ENT_UR_ROLE_ENT_EDIT")?n("a",{on:{click:function(n){return t.editFunc(e.entId)}}},[t._v("修改")]):t._e()])]}}])})],1),n("InfoAddOrEdit",{ref:"infoAddOrEdit",attrs:{callbackFunc:t.refTable}})],1)},a=[],u=n("0fea"),i=n("c22a"),o=n("5d5e"),s=n("b42c"),c=[{title:"资源权限ID",dataIndex:"entId"},{title:"资源名称",dataIndex:"entName"},{title:"图标",dataIndex:"menuIcon"},{title:"路径",dataIndex:"menuUri"},{title:"组件名称",dataIndex:"componentName"},{title:"类型",dataIndex:"entType"},{title:"状态",scopedSlots:{customRender:"stateSlot"},align:"center"},{title:"排序",dataIndex:"entSort"},{title:"修改时间",dataIndex:"updatedAt"},{title:"操作",width:"100px",fixed:"right",align:"center",scopedSlots:{customRender:"opSlot"}}],d={name:"EntPage",components:{JeepayTableColState:i["a"],JeepayTableColumns:o["a"],InfoAddOrEdit:s["default"]},data:function(){return{querySysType:"MGR",tableColumns:c,dataSource:[],loading:!1}},mounted:function(){this.refTable()},methods:{refTable:function(){var t=this;t.loading=!0,Object(u["U"])(t.querySysType).then((function(e){t.dataSource=e,t.loading=!1}))},updateState:function(t,e){var n=this;return u["db"].updateById(u["d"],t,{state:e,sysType:n.querySysType}).then((function(t){n.$message.success("更新成功"),n.refTable()}))},editFunc:function(t){this.$refs.infoAddOrEdit.show(t,this.querySysType)}}},l=d,f=n("2877"),p=Object(f["a"])(l,r,a,!1,null,null,null);e["default"]=p.exports},"0fea":function(t,e,n){"use strict";n.d(e,"cb",(function(){return a})),n.d(e,"db",(function(){return u})),n.d(e,"d",(function(){return i})),n.d(e,"K",(function(){return o})),n.d(e,"J",(function(){return s})),n.d(e,"N",(function(){return c})),n.d(e,"P",(function(){return d})),n.d(e,"h",(function(){return l})),n.d(e,"f",(function(){return f})),n.d(e,"g",(function(){return p})),n.d(e,"r",(function(){return h})),n.d(e,"o",(function(){return m})),n.d(e,"y",(function(){return y})),n.d(e,"C",(function(){return b})),n.d(e,"c",(function(){return v})),n.d(e,"v",(function(){return T})),n.d(e,"H",(function(){return g})),n.d(e,"u",(function(){return S})),n.d(e,"Q",(function(){return O})),n.d(e,"B",(function(){return w})),n.d(e,"q",(function(){return I})),n.d(e,"i",(function(){return q})),n.d(e,"l",(function(){return E})),n.d(e,"k",(function(){return k})),n.d(e,"j",(function(){return j})),n.d(e,"n",(function(){return x})),n.d(e,"m",(function(){return C})),n.d(e,"F",(function(){return _})),n.d(e,"E",(function(){return N})),n.d(e,"I",(function(){return P})),n.d(e,"s",(function(){return F})),n.d(e,"t",(function(){return R})),n.d(e,"M",(function(){return A})),n.d(e,"L",(function(){return U})),n.d(e,"e",(function(){return B})),n.d(e,"D",(function(){return G})),n.d(e,"p",(function(){return L})),n.d(e,"a",(function(){return J})),n.d(e,"O",(function(){return D})),n.d(e,"G",(function(){return M})),n.d(e,"x",(function(){return H})),n.d(e,"w",(function(){return K})),n.d(e,"z",(function(){return W})),n.d(e,"A",(function(){return z})),n.d(e,"b",(function(){return Q})),n.d(e,"hb",(function(){return V})),n.d(e,"U",(function(){return X})),n.d(e,"bb",(function(){return Y})),n.d(e,"eb",(function(){return Z})),n.d(e,"X",(function(){return tt})),n.d(e,"W",(function(){return et})),n.d(e,"gb",(function(){return nt})),n.d(e,"fb",(function(){return rt})),n.d(e,"Y",(function(){return at})),n.d(e,"V",(function(){return ut})),n.d(e,"S",(function(){return it})),n.d(e,"T",(function(){return ot})),n.d(e,"Z",(function(){return st})),n.d(e,"ab",(function(){return ct})),n.d(e,"R",(function(){return dt}));var r=n("4667"),a={list:function(t,e){return r["a"].request({url:t,method:"GET",params:e},!0,!0,!1)},add:function(t,e){return r["a"].request({url:t,method:"POST",data:e},!0,!0,!1)},getById:function(t,e){return r["a"].request({url:t+"/"+e,method:"GET"},!0,!0,!1)},updateById:function(t,e,n){return r["a"].request({url:t+"/"+e,method:"PUT",data:n},!0,!0,!1)},delById:function(t,e){return r["a"].request({url:t+"/"+e,method:"DELETE"},!0,!0,!1)},postNormal:function(t,e){return r["a"].request({url:t+"/"+e,method:"POST"},!0,!0,!0)},postDataNormal:function(t,e,n){return r["a"].request({url:t+"/"+e,method:"POST",data:n},!0,!0,!0)},getNormal:function(t,e){return r["a"].request({url:t+"/"+e,method:"GET"},!0,!0,!0)}},u={list:function(t,e){return r["a"].request({url:t,method:"GET",params:e},!0,!0,!0)},add:function(t,e){return r["a"].request({url:t,method:"POST",data:e},!0,!0,!0)},getById:function(t,e){return r["a"].request({url:t+"/"+e,method:"GET"},!0,!0,!0)},updateById:function(t,e,n){return r["a"].request({url:t+"/"+e,method:"PUT",data:n},!0,!0,!0)},delById:function(t,e){return r["a"].request({url:t+"/"+e,method:"DELETE"},!0,!0,!0)}},i="/api/sysEnts",o="/api/sysRoles",s="/api/sysRoleEntRelas",c="/api/sysUsers",d="/api/sysUserRoleRelas",l="/api/isvInfo",f="/api/isvBalance",p="/api/agentHistory",h="/api/mchInfo",m="/api/mchBalance",y="/api/mchStatInfo",b="/api/passageStatInfo",v="/api/agentStatInfo",T="/api/mchProductInfo",g="/api/productMchInfo",S="/api/mchPassageInfo",O="/api/payOrder",w="/api/passageMchInfo",I="/api/mchHistory",q="/api/mchApps",E="/api/mchAppsList",k="/api/passageHistory",j="/api/mchAppsBalance",x="/api/mchAppsBalanceReset",C="/api/mchAppsMultipleSet",_="/api/payOrder",N="/api/payOrderForceList",P="/api/refundOrder",F="/api/mchNotify",R="/api/mchNotifyResend/resendAll",A="api/sysLog",U="api/sysConfigs",$="api/mainChart",B="/api/payIfDefines",G="/api/payWays",L="/api/mchDivision",J="/api/agentDivision",D="/api/transferOrders",M="/api/platStat",H="/api/mchStat",K="/api/mchProductStat",W="/api/passageStat",z="/api/productStat",Q="/api/agentStat",V={avatar:r["a"].baseUrl+"/api/ossFiles/avatar",ifBG:r["a"].baseUrl+"/api/ossFiles/ifBG",cert:r["a"].baseUrl+"/api/ossFiles/cert"};function X(t){return r["a"].request({url:"/api/sysEnts/showTree?sysType="+t,method:"GET"})}function Y(t,e,n){return r["a"].request({url:"/api/payOrder/refunds/"+t,method:"POST",data:{refundAmount:e,refundReason:n}})}function Z(t,e){return r["a"].request({url:"api/sysUserRoleRelas/relas/"+t,method:"POST",data:{roleIdListStr:JSON.stringify(e)}})}function tt(){return r["a"].request({url:$+"/twoDayCount",method:"GET"})}function et(){return r["a"].request({url:$+"/realTimeCount",method:"GET"})}function nt(t){return r["a"].request({url:"/api/current/modifyPwd",method:"put",data:t})}function rt(t){return r["a"].request({url:"/api/current/user",method:"put",data:t})}function at(){return r["a"].request({url:"/api/current/user",method:"get"})}function ut(){return r["a"].request({url:"/api/current/getGoogleKey",method:"get"})}function it(t){return r["a"].request({url:U+"/"+t,method:"GET"})}function ot(t,e){return r["a"].request({url:"/api/sysEnts/bySysType",method:"GET",params:{entId:t,sysType:e}})}function st(t){return r["a"].request({url:"/api/mchNotify/resend/"+t,method:"POST"})}function ct(t){return r["a"].request({url:"/api/passageTest/doPay",method:"POST",data:t})}function dt(t,e){return r["a"].request({url:t,method:"POST",data:e,responseType:"arraybuffer"},!0,!1,!0)}},"3cbe":function(t,e,n){"use strict";n("a415")},"5d5e":function(t,e,n){"use strict";n("d81d");var r,a,u={name:"JeepayTableColumns",render:function(t,e){var n=arguments[0],r=[];if(this.$slots.default.map((function(t){return t.tag&&r.push(t),!1})),r.length<=4)return t("div",{style:"display:flex; justify-content: space-evenly;"},r);for(var a=[r[0],r[1],r[2]],u=[],i=3;i<r.length;i++)u.push(n("a-menu-item",[r[i]]));return n("div",{style:"display:flex; justify-content: space-evenly;"},[" ",a,n("a-dropdown",[n("a-button",{class:"ant-dropdown-link",attrs:{type:"link"},style:""},["更多",n("a-icon",{attrs:{type:"down"}})]),n("a-menu",{slot:"overlay"},[u])])])}},i=u,o=(n("3cbe"),n("2877")),s=Object(o["a"])(i,r,a,!1,null,"d8995c5c",null);e["a"]=s.exports},a415:function(t,e,n){},b42c:function(t,e,n){"use strict";n.r(e);var r=function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("a-modal",{attrs:{title:t.isAdd?"新增菜单":"修改菜单",confirmLoading:t.confirmLoading},on:{ok:t.handleOkFunc},model:{value:t.isShow,callback:function(e){t.isShow=e},expression:"isShow"}},[n("a-form-model",{ref:"infoFormModel",attrs:{model:t.saveObject,"label-col":{span:6},"wrapper-col":{span:15},rules:t.rules}},[n("a-form-model-item",{attrs:{label:"资源名称：",prop:"entName"}},[n("a-input",{model:{value:t.saveObject.entName,callback:function(e){t.$set(t.saveObject,"entName",e)},expression:"saveObject.entName"}})],1),n("a-form-model-item",{attrs:{label:"路径地址：",prop:"menuUri"}},[n("a-input",{model:{value:t.saveObject.menuUri,callback:function(e){t.$set(t.saveObject,"menuUri",e)},expression:"saveObject.menuUri"}})],1),n("a-form-model-item",{attrs:{label:"排序（正序显示）：",prop:"entSort"}},[n("a-input",{model:{value:t.saveObject.entSort,callback:function(e){t.$set(t.saveObject,"entSort",e)},expression:"saveObject.entSort"}})],1),n("a-form-model-item",{attrs:{label:"快速开始：",prop:"quickJump"}},[n("a-radio-group",{attrs:{disabled:"PB"==t.saveObject.menuType||!t.saveObject.menuUri},model:{value:t.saveObject.quickJump,callback:function(e){t.$set(t.saveObject,"quickJump",e)},expression:"saveObject.quickJump"}},[n("a-radio",{attrs:{value:1}},[t._v("是")]),n("a-radio",{attrs:{value:0}},[t._v("否")])],1)],1),n("a-form-model-item",{attrs:{label:"状态：",prop:"state"}},[n("a-radio-group",{model:{value:t.saveObject.state,callback:function(e){t.$set(t.saveObject,"state",e)},expression:"saveObject.state"}},[n("a-radio",{attrs:{value:1}},[t._v("启用")]),n("a-radio",{attrs:{value:0}},[t._v("停用")])],1)],1)],1)],1)},a=[],u=n("0fea"),i={props:{callbackFunc:{type:Function}},data:function(){return{confirmLoading:!1,isAdd:!0,isShow:!1,saveObject:{},recordId:null,sysType:"MGR",rules:{entName:[{required:!0,message:"请输入资源名称",trigger:"blur"}]}}},created:function(){},methods:{show:function(t,e){this.isAdd=!t,this.sysType=e,this.saveObject={},this.confirmLoading=!1,void 0!==this.$refs.infoFormModel&&this.$refs.infoFormModel.resetFields();var n=this;this.isAdd?n.isShow=!0:(n.recordId=t,Object(u["T"])(t,e).then((function(t){n.saveObject=t})),this.isShow=!0)},handleOkFunc:function(){var t=this;this.$refs.infoFormModel.validate((function(e){e&&(t.confirmLoading=!0,t.isAdd||u["cb"].updateById(u["d"],t.recordId,t.saveObject).then((function(e){t.$message.success("修改成功"),t.isShow=!1,t.callbackFunc()})).catch((function(e){t.confirmLoading=!1})))}))}}},o=i,s=n("2877"),c=Object(s["a"])(o,r,a,!1,null,null,null);e["default"]=c.exports},c22a:function(t,e,n){"use strict";var r=function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("div",[t.showSwitchType?t._e():[0==t.state?n("div",[n("a-badge",{attrs:{status:"error",text:"停用"}})],1):1==t.state?n("div",[n("a-badge",{attrs:{status:"processing",text:"启用"}})],1):n("div",[n("a-badge",{attrs:{status:"warning",text:"未知"}})],1)],t.showSwitchType?[n("a-switch",{staticClass:"els",attrs:{"checked-children":"启用","un-checked-children":"停用",checked:t.switchChecked},on:{change:t.onChangeInner}})]:t._e()],2)},a=[],u=(n("a9e3"),n("d3b7"),{name:"JeepayTableColState",props:{state:{type:Number,default:-1},showSwitchType:{type:Boolean,default:!1},onChange:{type:Function,default:function(t){return new Promise((function(t){t()}))}}},data:function(){return{switchChecked:!1}},mounted:function(){this.switchChecked=1===this.state},watch:{state:function(t,e){this.switchChecked=1===this.state}},methods:{onChangeInner:function(t){var e=this;this.switchChecked=t,this.onChange(t?1:0).then().catch((function(n){e.$nextTick((function(){e.switchChecked=!t}))}))}}}),i=u,o=n("2877"),s=Object(o["a"])(i,r,a,!1,null,null,null);e["a"]=s.exports}}]);