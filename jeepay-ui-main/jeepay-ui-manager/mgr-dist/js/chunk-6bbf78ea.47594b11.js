(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["chunk-6bbf78ea"],{"0fea":function(t,n,e){"use strict";e.d(n,"cb",(function(){return u})),e.d(n,"db",(function(){return a})),e.d(n,"d",(function(){return i})),e.d(n,"K",(function(){return o})),e.d(n,"J",(function(){return s})),e.d(n,"N",(function(){return c})),e.d(n,"P",(function(){return d})),e.d(n,"h",(function(){return f})),e.d(n,"f",(function(){return p})),e.d(n,"g",(function(){return l})),e.d(n,"r",(function(){return h})),e.d(n,"o",(function(){return m})),e.d(n,"y",(function(){return y})),e.d(n,"C",(function(){return T})),e.d(n,"c",(function(){return E})),e.d(n,"v",(function(){return g})),e.d(n,"H",(function(){return I})),e.d(n,"u",(function(){return q})),e.d(n,"Q",(function(){return S})),e.d(n,"B",(function(){return b})),e.d(n,"q",(function(){return O})),e.d(n,"i",(function(){return P})),e.d(n,"l",(function(){return v})),e.d(n,"k",(function(){return D})),e.d(n,"j",(function(){return L})),e.d(n,"n",(function(){return G})),e.d(n,"m",(function(){return R})),e.d(n,"F",(function(){return k})),e.d(n,"E",(function(){return B})),e.d(n,"I",(function(){return N})),e.d(n,"s",(function(){return A})),e.d(n,"t",(function(){return U})),e.d(n,"M",(function(){return w})),e.d(n,"L",(function(){return K})),e.d(n,"e",(function(){return _})),e.d(n,"D",(function(){return x})),e.d(n,"p",(function(){return C})),e.d(n,"a",(function(){return J})),e.d(n,"O",(function(){return M})),e.d(n,"G",(function(){return H})),e.d(n,"x",(function(){return j})),e.d(n,"w",(function(){return z})),e.d(n,"z",(function(){return W})),e.d(n,"A",(function(){return $})),e.d(n,"b",(function(){return Q})),e.d(n,"hb",(function(){return V})),e.d(n,"U",(function(){return X})),e.d(n,"bb",(function(){return Y})),e.d(n,"eb",(function(){return Z})),e.d(n,"X",(function(){return tt})),e.d(n,"W",(function(){return nt})),e.d(n,"gb",(function(){return et})),e.d(n,"fb",(function(){return rt})),e.d(n,"Y",(function(){return ut})),e.d(n,"V",(function(){return at})),e.d(n,"S",(function(){return it})),e.d(n,"T",(function(){return ot})),e.d(n,"Z",(function(){return st})),e.d(n,"ab",(function(){return ct})),e.d(n,"R",(function(){return dt}));var r=e("4667"),u={list:function(t,n){return r["a"].request({url:t,method:"GET",params:n},!0,!0,!1)},add:function(t,n){return r["a"].request({url:t,method:"POST",data:n},!0,!0,!1)},getById:function(t,n){return r["a"].request({url:t+"/"+n,method:"GET"},!0,!0,!1)},updateById:function(t,n,e){return r["a"].request({url:t+"/"+n,method:"PUT",data:e},!0,!0,!1)},delById:function(t,n){return r["a"].request({url:t+"/"+n,method:"DELETE"},!0,!0,!1)},postNormal:function(t,n){return r["a"].request({url:t+"/"+n,method:"POST"},!0,!0,!0)},postDataNormal:function(t,n,e){return r["a"].request({url:t+"/"+n,method:"POST",data:e},!0,!0,!0)},getNormal:function(t,n){return r["a"].request({url:t+"/"+n,method:"GET"},!0,!0,!0)}},a={list:function(t,n){return r["a"].request({url:t,method:"GET",params:n},!0,!0,!0)},add:function(t,n){return r["a"].request({url:t,method:"POST",data:n},!0,!0,!0)},getById:function(t,n){return r["a"].request({url:t+"/"+n,method:"GET"},!0,!0,!0)},updateById:function(t,n,e){return r["a"].request({url:t+"/"+n,method:"PUT",data:e},!0,!0,!0)},delById:function(t,n){return r["a"].request({url:t+"/"+n,method:"DELETE"},!0,!0,!0)}},i="/api/sysEnts",o="/api/sysRoles",s="/api/sysRoleEntRelas",c="/api/sysUsers",d="/api/sysUserRoleRelas",f="/api/isvInfo",p="/api/isvBalance",l="/api/agentHistory",h="/api/mchInfo",m="/api/mchBalance",y="/api/mchStatInfo",T="/api/passageStatInfo",E="/api/agentStatInfo",g="/api/mchProductInfo",I="/api/productMchInfo",q="/api/mchPassageInfo",S="/api/payOrder",b="/api/passageMchInfo",O="/api/mchHistory",P="/api/mchApps",v="/api/mchAppsList",D="/api/passageHistory",L="/api/mchAppsBalance",G="/api/mchAppsBalanceReset",R="/api/mchAppsMultipleSet",k="/api/payOrder",B="/api/payOrderForceList",N="/api/refundOrder",A="/api/mchNotify",U="/api/mchNotifyResend/resendAll",w="api/sysLog",K="api/sysConfigs",F="api/mainChart",_="/api/payIfDefines",x="/api/payWays",C="/api/mchDivision",J="/api/agentDivision",M="/api/transferOrders",H="/api/platStat",j="/api/mchStat",z="/api/mchProductStat",W="/api/passageStat",$="/api/productStat",Q="/api/agentStat",V={avatar:r["a"].baseUrl+"/api/ossFiles/avatar",ifBG:r["a"].baseUrl+"/api/ossFiles/ifBG",cert:r["a"].baseUrl+"/api/ossFiles/cert"};function X(t){return r["a"].request({url:"/api/sysEnts/showTree?sysType="+t,method:"GET"})}function Y(t,n,e){return r["a"].request({url:"/api/payOrder/refunds/"+t,method:"POST",data:{refundAmount:n,refundReason:e}})}function Z(t,n){return r["a"].request({url:"api/sysUserRoleRelas/relas/"+t,method:"POST",data:{roleIdListStr:JSON.stringify(n)}})}function tt(){return r["a"].request({url:F+"/twoDayCount",method:"GET"})}function nt(){return r["a"].request({url:F+"/realTimeCount",method:"GET"})}function et(t){return r["a"].request({url:"/api/current/modifyPwd",method:"put",data:t})}function rt(t){return r["a"].request({url:"/api/current/user",method:"put",data:t})}function ut(){return r["a"].request({url:"/api/current/user",method:"get"})}function at(){return r["a"].request({url:"/api/current/getGoogleKey",method:"get"})}function it(t){return r["a"].request({url:K+"/"+t,method:"GET"})}function ot(t,n){return r["a"].request({url:"/api/sysEnts/bySysType",method:"GET",params:{entId:t,sysType:n}})}function st(t){return r["a"].request({url:"/api/mchNotify/resend/"+t,method:"POST"})}function ct(t){return r["a"].request({url:"/api/passageTest/doPay",method:"POST",data:t})}function dt(t,n){return r["a"].request({url:t,method:"POST",data:n,responseType:"arraybuffer"},!0,!1,!0)}},"440a":function(t,n,e){"use strict";e.r(n);var r=function(){var t=this,n=t.$createElement,e=t._self._c||n;return e("div",{staticStyle:{"padding-bottom":"50px"}},[t.hasEnt?e("p",[t._v("请选择权限： ")]):t._e(),e("a-tree",{attrs:{"tree-data":t.treeData,replaceFields:t.replaceFields,checkable:!0},model:{value:t.checkedKeys,callback:function(n){t.checkedKeys=n},expression:"checkedKeys"}})],1)},u=[],a=(e("d81d"),e("0fea")),i={data:function(){return{hasEnt:this.$access("ENT_UR_ROLE_DIST"),recordId:null,treeData:[],replaceFields:{key:"entId",title:"entName"},checkedKeys:[],allEntList:{}}},methods:{initTree:function(t){var n=this,e=this;if(!this.hasEnt)return!1;e.checkedKeys=[],e.treeData=[],e.allEntList={},e.recordId=t,Object(a["U"])("MGR").then((function(r){e.treeData=r,n.recursionTreeData(r,(function(t){e.allEntList[t.entId]={pid:t.pid,children:t.children||[]}})),a["cb"].list(a["J"],{roleId:t||"NONE",pageSize:-1}).then((function(t){var n=[];t.records.map((function(t){e.allEntList[t.entId]&&e.allEntList[t.entId].children.length<=0&&n.push(t.entId)})),e.checkedKeys=n}))}))},getSelectedEntIdList:function(){if(!this.hasEnt)return!1;var t=this,n=[];return this.checkedKeys.map((function(e){var r=[];t.getAllPid(e,r),r.map((function(t){n.indexOf(t)<0&&n.push(t)}))})),n},recursionTreeData:function(t,n){for(var e=0;e<t.length;e++){var r=t[e];r.children&&r.children.length>0&&this.recursionTreeData(r.children,n),n(r)}},getAllPid:function(t,n){this.allEntList[t]&&"ROOT"!==t&&(n.push(t),this.getAllPid(this.allEntList[t].pid,n))}}},o=i,s=e("2877"),c=Object(s["a"])(o,r,u,!1,null,null,null);n["default"]=c.exports}}]);