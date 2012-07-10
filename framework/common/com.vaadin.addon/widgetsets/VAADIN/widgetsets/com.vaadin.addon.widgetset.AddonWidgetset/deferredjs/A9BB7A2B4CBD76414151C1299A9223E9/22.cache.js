function mP(){}
function hP(){}
function I2(){}
function L2(){}
function P2(){}
function N2(){}
function B4(){}
function qub(){}
function pub(){}
function D5b(){}
function T5b(){}
function X5b(){}
function _5b(){}
function d6b(){}
function U5b(b){this.b=b}
function Y5b(b){this.b=b}
function a6b(b){Ub();this.b=b}
function R5b(b,c){b.enctype=c;b.encoding=c}
function Wzb(b,c){b.onload=function(){c.Nf()}}
function F5b(b){FBb(b.n,false);b.o||(b.e.Lb[Qoc]=true,undefined);b.d=false}
function G5b(b){FBb(b.n,true);b.e.Lb[Qoc]=false;b.d=true;if(b.o){E5b(b);b.o=false}}
function E5b(b){if(b.p){$doc.body.removeChild(b.p);b.p.onload=null;b.p=null}}
function C4(){var b;this.Lb=(b=$doc.createElement(Uoc),b.type=cpc,b)}
function J5b(b,c){if(b.f!=c){b.f=c;if(b.f){zb(b.e,1024);zb(b.e,2048)}}jb(b.Lb,'v-upload-immediate',b.f)}
function I5b(b){v_(b.k,b.n);v_(b.k,b.e);b.e=new e6b(b);b.e.Lb.name=b.j+Vxc;b.e.Lb[Qoc]=!b.d;z3(b.k,b.e);z3(b.k,b.n);b.f&&zb(b.e,1024)}
function oP(){kP=new mP;_c((Zc(),Yc),22);!!$stats&&$stats(Ed(Txc,ync,-1,-1));kP.ad();!!$stats&&$stats(Ed(Txc,fvc,-1,-1))}
function lP(){var b,c,d;while(iP){d=uc;iP=iP.b;!iP&&(jP=null);if(!d){(Nsb(),Msb).lg(IG,new qub);okb()}else{try{(Nsb(),Msb).lg(IG,new qub);okb()}catch(b){b=OJ(b);if(fs(b,37)){c=b;hqb.xe(c)}else throw b}}}}
function e6b(b){var c;this.b=b;this.Lb=(c=$doc.createElement(Uoc),c.type='file',c);this.Lb[gnc]='gwt-FileUpload';this.c=new P2;this.c.d=this;this.Ib==-1?sZ(this.Lb,4096|(this.Lb.__eventBits||0)):(this.Ib|=4096)}
function K5b(b){if(b.e.Lb.value.length==0||b.o||!b.d){hqb.ze('Submit cancelled (disabled, no file or already submitted)');return}Lhb(b.b);b.c.submit();b.o=true;hqb.ze('Submitted form');F5b(b);b.q=new a6b(b);Wb(b.q,800)}
function O2(b,c){var d;switch(s$(c.type)){case 1024:if(!b.b){b.c=true;return false}break;case 4096:if(b.c){b.b=true;b.d.Lb.dispatchEvent((d=$doc.createEvent(Uxc),d.initEvent(Snc,false,true),d));b.b=false;b.c=false}}return true}
function H5b(b){var c;if(!b.p){c=$doc.createElement(apc);c.innerHTML="<iframe src=\"javascript:''\" name='"+b.j+"_TGT_FRAME' style='position:absolute;width:0;height:0;border:0'>"||qnc;b.p=Xe(c);$doc.body.appendChild(b.p);b.c.target=b.j+'_TGT_FRAME';Wzb(b.p,b)}}
function L5b(){this.Lb=$doc.createElement('form');this.e=new e6b(this);this.k=new C3;this.g=new C4;this.c=this.Lb;R5b(this.Lb,Wxc);this.c.method='post';G1(this,this.k);z3(this.k,this.g);z3(this.k,this.e);this.n=new HBb;qb(this.n,new U5b(this),(kl(),kl(),jl));z3(this.k,this.n);this.Lb[gnc]='v-upload';this.Ib==-1?sZ(this.Lb,241|(this.Lb.__eventBits||0)):(this.Ib|=241)}
var Vxc='_file',Xxc='buttoncaption',Txc='runCallbacks22';_=mP.prototype=hP.prototype=new L;_.gC=function nP(){return Gv};_.ad=function rP(){lP()};_.cM={};_=I2.prototype=new J;_.gC=function J2(){return Tx};_.$b=function K2(b){O2(this.c,b)&&ub(this,b)};_.cM={10:1,13:1,15:1,22:1,69:1,70:1};_.c=null;_=L2.prototype=new L;_.gC=function M2(){return Sx};_.cM={};_=P2.prototype=N2.prototype=new L2;_.gC=function Q2(){return Rx};_.cM={};_.b=false;_.c=false;_.d=null;_=C4.prototype=B4.prototype=new J;_.gC=function D4(){return gy};_.cM={10:1,13:1,15:1,22:1,69:1,70:1};_=qub.prototype=pub.prototype=new L;_.Ke=function rub(){return new L5b};_.gC=function sub(){return CB};_.cM={137:1};_=L5b.prototype=D5b.prototype=new D1;_.gC=function M5b(){return IG};_.Zb=function N5b(){tb(this);!!this.b&&H5b(this)};_.$b=function O5b(b){(s$(b.type)&241)>0&&(Qrb(this.b.G,b,this,null),undefined);ub(this,b)};_._b=function P5b(){vb(this);this.o||E5b(this)};_.Nf=function Q5b(){Hrb((Nd(),Md),new Y5b(this))};_.dc=function S5b(b,c){var d;if(Thb(c,this,b,true)){return}if('notStarted' in b[1]){Wb(this.q,400);return}if('forceSubmit' in b[1]){K5b(this);return}J5b(this,Boolean(b[1][Zpc]));this.b=c;this.j=b[1][Yoc];this.i=b[1]['nextid'];d=Qhb(c,b[1][aqc][Itc]);this.c.action=d;if(Xxc in b[1]){this.n.c.textContent=b[1][Xxc]||qnc;this.n.Lb.style.display=qnc}else{this.n.Lb.style.display=onc}this.e.Lb.name=this.j+Vxc;if(Qoc in b[1]||Xpc in b[1]){F5b(this)}else if(!Boolean(b[1][rqc])){G5b(this);H5b(this)}};_.cM={10:1,13:1,15:1,17:1,19:1,20:1,21:1,22:1,26:1,33:1,69:1,70:1,75:1,76:1};_.b=null;_.c=null;_.d=true;_.f=false;_.i=0;_.j=null;_.n=null;_.o=false;_.p=null;_.q=null;_=U5b.prototype=T5b.prototype=new L;_.gC=function V5b(){return EG};_.yc=function W5b(b){this.b.f?(this.b.e.Lb.click(),undefined):K5b(this.b)};_.cM={12:1,39:1};_.b=null;_=Y5b.prototype=X5b.prototype=new L;_.oc=function Z5b(){if(this.b.o){if(this.b.b){!!this.b.q&&Vb(this.b.q);hqb.ze('VUpload:Submit complete');Lhb(this.b.b)}I5b(this.b);this.b.o=false;G5b(this.b);this.b.Hb||E5b(this.b)}};_.gC=function $5b(){return FG};_.cM={3:1,14:1};_.b=null;_=a6b.prototype=_5b.prototype=new Sb;_.gC=function b6b(){return GG};_.fc=function c6b(){hqb.ze('Visiting server to see if upload started event changed UI.');jhb(this.b.b,this.b.j,'pollForStart',qnc+this.b.i,true,105)};_.cM={65:1};_.b=null;_=e6b.prototype=d6b.prototype=new I2;_.gC=function f6b(){return HG};_.$b=function g6b(b){O2(this.c,b)&&ub(this,b);if(s$(b.type)==1024){this.b.f&&this.b.e.Lb.value!=null&&!Yec(qnc,this.b.e.Lb.value)&&K5b(this.b)}else if((zlb(),!ylb&&(ylb=new Ulb),zlb(),ylb).b.i&&s$(b.type)==2048){this.b.e.Lb.click();this.b.e.Lb.blur()}};_.cM={10:1,13:1,15:1,22:1,69:1,70:1};_.b=null;var Gv=Ddc(Tuc,'AsyncLoader22'),Tx=Ddc(Cuc,'FileUpload'),Sx=Ddc(Cuc,'FileUpload$FileUploadImpl'),Rx=Ddc(Cuc,'FileUpload$FileUploadImplOpera'),gy=Ddc(Cuc,'Hidden'),CB=Ddc(avc,'WidgetMapImpl$28$1'),EG=Ddc(_uc,'VUpload$1'),FG=Ddc(_uc,'VUpload$2'),GG=Ddc(_uc,'VUpload$3'),HG=Ddc(_uc,'VUpload$MyFileUpload');dnc(oP)();