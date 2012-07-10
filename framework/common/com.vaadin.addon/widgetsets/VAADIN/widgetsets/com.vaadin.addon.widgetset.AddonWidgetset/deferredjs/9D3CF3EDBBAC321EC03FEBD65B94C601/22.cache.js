function QP(){}
function LP(){}
function o3(){}
function d5(){}
function lvb(){}
function kvb(){}
function y6b(){}
function O6b(){}
function S6b(){}
function W6b(){}
function $6b(){}
function P6b(b){this.b=b}
function T6b(b){this.b=b}
function X6b(b){Ub();this.b=b}
function M6b(b,c){b.enctype=c;b.encoding=c}
function RAb(b,c){b.onload=function(){c.Tf()}}
function A6b(b){ACb(b.n,false);b.o||(b.e.Lb[Ppc]=true,undefined);b.d=false}
function B6b(b){ACb(b.n,true);b.e.Lb[Ppc]=false;b.d=true;if(b.o){z6b(b);b.o=false}}
function z6b(b){if(b.p){$doc.body.removeChild(b.p);b.p.onload=null;b.p=null}}
function e5(){var b;this.Lb=(b=$doc.createElement(Upc),b.type=cqc,b)}
function _6b(b){var c;this.b=b;this.Lb=(c=$doc.createElement(Upc),c.type='file',c);this.Lb[boc]='gwt-FileUpload'}
function E6b(b,c){if(b.f!=c){b.f=c;if(b.f){zb(b.e,1024);zb(b.e,2048)}}jb(b.Lb,'v-upload-immediate',b.f)}
function D6b(b){__(b.k,b.n);__(b.k,b.e);b.e=new _6b(b);b.e.Lb.name=b.j+Qyc;b.e.Lb[Ppc]=!b.d;_3(b.k,b.e);_3(b.k,b.n);b.f&&zb(b.e,1024)}
function SP(){OP=new QP;_c((Zc(),Yc),22);!!$stats&&$stats(Ed(Pyc,toc,-1,-1));OP.ed();!!$stats&&$stats(Ed(Pyc,bwc,-1,-1))}
function PP(){var b,c,d;while(MP){d=uc;MP=MP.b;!MP&&(NP=null);if(!d){(Itb(),Htb).rg(kH,new lvb);jlb()}else{try{(Itb(),Htb).rg(kH,new lvb);jlb()}catch(b){b=qK(b);if(Es(b,37)){c=b;crb.De(c)}else throw b}}}}
function F6b(b){if(b.e.Lb.value.length==0||b.o||!b.d){crb.Fe('Submit cancelled (disabled, no file or already submitted)');return}Gib(b.b);b.c.submit();b.o=true;crb.Fe('Submitted form');A6b(b);b.q=new X6b(b);Wb(b.q,800)}
function C6b(b){var c;if(!b.p){c=$doc.createElement(aqc);c.innerHTML="<iframe src=\"javascript:''\" name='"+b.j+"_TGT_FRAME' style='position:absolute;width:0;height:0;border:0'>"||loc;b.p=qf(c);$doc.body.appendChild(b.p);b.c.target=b.j+'_TGT_FRAME';RAb(b.p,b)}}
function G6b(){this.Lb=$doc.createElement('form');this.e=new _6b(this);this.k=new c4;this.g=new e5;this.c=this.Lb;M6b(this.Lb,Ryc);this.c.method='post';m2(this,this.k);_3(this.k,this.g);_3(this.k,this.e);this.n=new CCb;qb(this.n,new P6b(this),(Jl(),Jl(),Il));_3(this.k,this.n);this.Lb[boc]='v-upload';this.Ib==-1?WZ(this.Lb,241|(this.Lb.__eventBits||0)):(this.Ib|=241)}
var Qyc='_file',Syc='buttoncaption',Pyc='runCallbacks22';_=QP.prototype=LP.prototype=new L;_.gC=function RP(){return gw};_.ed=function VP(){PP()};_.cM={};_=o3.prototype=new J;_.gC=function p3(){return sy};_.$b=function q3(b){ub(this,b)};_.cM={10:1,13:1,15:1,22:1,69:1,70:1};_=e5.prototype=d5.prototype=new J;_.gC=function f5(){return Hy};_.cM={10:1,13:1,15:1,22:1,69:1,70:1};_=lvb.prototype=kvb.prototype=new L;_.Qe=function mvb(){return new G6b};_.gC=function nvb(){return eC};_.cM={137:1};_=G6b.prototype=y6b.prototype=new j2;_.gC=function H6b(){return kH};_.Zb=function I6b(){tb(this);!!this.b&&C6b(this)};_.$b=function J6b(b){(W$(b.type)&241)>0&&(Lsb(this.b.G,b,this,null),undefined);ub(this,b)};_._b=function K6b(){vb(this);this.o||z6b(this)};_.Tf=function L6b(){Csb((Nd(),Md),new T6b(this))};_.dc=function N6b(b,c){var d;if(Oib(c,this,b,true)){return}if('notStarted' in b[1]){Wb(this.q,400);return}if('forceSubmit' in b[1]){F6b(this);return}E6b(this,Boolean(b[1][$qc]));this.b=c;this.j=b[1][Ypc];this.i=b[1]['nextid'];d=Lib(c,b[1][brc][Euc]);this.c.action=d;if(Syc in b[1]){this.n.c.textContent=b[1][Syc]||loc;this.n.Lb.style.display=loc}else{this.n.Lb.style.display=joc}this.e.Lb.name=this.j+Qyc;if(Ppc in b[1]||Yqc in b[1]){A6b(this)}else if(!Boolean(b[1][qrc])){B6b(this);C6b(this)}};_.cM={10:1,13:1,15:1,17:1,19:1,20:1,21:1,22:1,26:1,33:1,69:1,70:1,75:1,76:1};_.b=null;_.c=null;_.d=true;_.f=false;_.i=0;_.j=null;_.n=null;_.o=false;_.p=null;_.q=null;_=P6b.prototype=O6b.prototype=new L;_.gC=function Q6b(){return gH};_.Cc=function R6b(b){this.b.f?(this.b.e.Lb.click(),undefined):F6b(this.b)};_.cM={12:1,39:1};_.b=null;_=T6b.prototype=S6b.prototype=new L;_.oc=function U6b(){if(this.b.o){if(this.b.b){!!this.b.q&&Vb(this.b.q);crb.Fe('VUpload:Submit complete');Gib(this.b.b)}D6b(this.b);this.b.o=false;B6b(this.b);this.b.Hb||z6b(this.b)}};_.gC=function V6b(){return hH};_.cM={3:1,14:1};_.b=null;_=X6b.prototype=W6b.prototype=new Sb;_.gC=function Y6b(){return iH};_.fc=function Z6b(){crb.Fe('Visiting server to see if upload started event changed UI.');eib(this.b.b,this.b.j,'pollForStart',loc+this.b.i,true,105)};_.cM={65:1};_.b=null;_=_6b.prototype=$6b.prototype=new o3;_.gC=function a7b(){return jH};_.$b=function b7b(b){ub(this,b);if(W$(b.type)==1024){this.b.f&&this.b.e.Lb.value!=null&&!Tfc(loc,this.b.e.Lb.value)&&F6b(this.b)}else if((umb(),!tmb&&(tmb=new Pmb),umb(),tmb).b.i&&W$(b.type)==2048){this.b.e.Lb.click();this.b.e.Lb.blur()}};_.cM={10:1,13:1,15:1,22:1,69:1,70:1};_.b=null;var gw=yec(Pvc,'AsyncLoader22'),sy=yec(yvc,'FileUpload'),Hy=yec(yvc,'Hidden'),eC=yec(Yvc,'WidgetMapImpl$28$1'),gH=yec(Xvc,'VUpload$1'),hH=yec(Xvc,'VUpload$2'),iH=yec(Xvc,'VUpload$3'),jH=yec(Xvc,'VUpload$MyFileUpload');$nc(SP)();