(window.webpackJsonp=window.webpackJsonp||[]).push([[19],{"+/d2":function(e,t,r){"use strict";var a=r("Yajr");r.n(a).a},"+n+t":function(e,t,r){"use strict";r.r(t);var a=r("uYse"),n=r.n(a);for(var u in a)"default"!==u&&function(e){r.d(t,e,function(){return a[e]})}(u);t.default=n.a},"+rKV":function(e,t,r){"use strict";Object.defineProperty(t,"__esModule",{value:!0}),t.updatePermissionsIds=t.getPermissionsIds=t.del=t.resetPassword=t.edit=t.add=t.getTableData=void 0;var a=function(e){return e&&e.__esModule?e:{default:e}}(r("Nlzp"));t.getTableData=function(e){return console.log("P-userPage",e),a.default.request({url:"/users",method:"post",data:e})},t.add=function(e){return a.default.request({url:"/user",method:"post",data:e})},t.edit=function(e){return a.default.request({url:"/user",method:"put",data:e})},t.resetPassword=function(e){return a.default.request({url:"/user/password/"+e,method:"get"})},t.del=function(e){return a.default.request({url:"/user",method:"delete",data:e})},t.getPermissionsIds=function(e){return a.default.request({url:"/user/roles/"+e,method:"get"})},t.updatePermissionsIds=function(e){return a.default.request({url:"/user/roles",method:"put",data:e})}},"2N6j":function(e,t,r){"use strict";r.r(t);var a=r("dkcq"),n=r("+n+t");for(var u in n)"default"!==u&&function(e){r.d(t,e,function(){return n[e]})}(u);r("+/d2");var i=r("KHd+"),s=Object(i.a)(n.default,a.a,a.b,!1,null,"93e89ed8",null);t.default=s.exports},Yajr:function(e,t,r){},dkcq:function(e,t,r){"use strict";var a=function(){var e=this.$createElement,t=this._self._c||e;return t("div",{staticClass:"user-edit"},[t("component-form",{attrs:{data:this.form,width:80}})],1)},n=[];r.d(t,"a",function(){return a}),r.d(t,"b",function(){return n})},uYse:function(e,t,r){"use strict";Object.defineProperty(t,"__esModule",{value:!0});var a=function(e){return e&&e.__esModule?e:{default:e}}(r("GQeE")),n=r("+rKV");t.default={name:"user-edit",data:function(){var e=this;return{form:{labelWidth:"200px",formFields:{id:"",username:"",phone:"",email:"",nickName:"",realName:"",sex:1,createWhere:1,status:1},formLabel:[{prop:"username",title:"账号",type:"input",placeholder:"请输入账号"},{prop:"phone",title:"手机号",type:"input",placeholder:"请输入手机号码"},{prop:"email",title:"邮箱",type:"input",placeholder:"请输入邮箱"},{prop:"nickName",title:"昵称",type:"input",placeholder:"请输入昵称"},{prop:"realName",title:"真实姓名",type:"input",placeholder:"请输入真实姓名"},{prop:"sex",title:"性别",type:"radio",options:[{label:"男",value:1},{label:"女",value:2}],change:function(t){return e.form.formFields.sex=t}},{prop:"createWhere",title:"创建来源",type:"radio",options:[{label:"web",value:1},{label:"android",value:2},{label:"ios",value:3}],change:function(t){return e.form.formFields.createWhere=t}}],buttons:{options:[{title:"提交",type:"primary",loading:!1,style:{marginRight:"20px"},click:function(t,r){e.handleSubmit(n.edit,t,e,function(t){return e.$emit("handleGetTableData")})}},{title:"重置密码",type:"warning",loading:!1,click:function(t,r){r.loading=!0,e.alert("确定要重置密码吗？默认密码 123456",function(){e.request(n.resetPassword,t.model.id,e,function(){return r.loading=!1},!0,function(t){r.loading=!1,e.error(t.msg)})})}}]},rules:{username:[{required:!0,message:"请输入账号",trigger:"blur"},{min:3,max:20,message:"长度在 20 个字符内",trigger:"blur"}],phone:[{required:!0,message:"请输入手机号码",trigger:"blur"},{pattern:this.validator.regExpPhone,message:"手机号码不正确",trigger:"blur"}],email:[{required:!0,message:"请输入邮箱",trigger:"blur"},{pattern:this.validator.regExpEmail,message:"邮箱不正确",trigger:"blur"}],nickName:[{required:!0,message:"请输入昵称",trigger:"blur"},{min:1,max:20,message:"长度在 20 个字符内",trigger:"blur"}],realName:[{required:!0,message:"请输入真实姓名",trigger:"blur"},{min:1,max:20,message:"长度在 20 个字符内",trigger:"blur"}]}}}},methods:{currentData:function(e){var t=this;e&&(0,a.default)(this.form.formFields).forEach(function(r){return t.form.formFields[r]=e[r]})},change:function(e){this.form.pid=e}}}}}]);