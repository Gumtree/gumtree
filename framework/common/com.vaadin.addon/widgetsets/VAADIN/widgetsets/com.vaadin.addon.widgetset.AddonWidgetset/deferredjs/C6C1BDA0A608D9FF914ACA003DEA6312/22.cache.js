function zP(){}
function uP(){}
function $2(){}
function N4(){}
function Gub(){}
function Fub(){}
function T5b(){}
function h6b(){}
function l6b(){}
function p6b(){}
function t6b(){}
function i6b(b){this.a=b}
function m6b(b){this.a=b}
function q6b(b){Ub();this.a=b}
function f6b(b,c){b.enctype=c;b.encoding=c}
function O4(){var b;this.Kb=(b=$doc.createElement(ppc),b.type=xpc,b)}
function u6b(b){var c;this.a=b;this.Kb=(c=$doc.createElement(ppc),c.type='file',c);this.Kb[wnc]='gwt-FileUpload'}
function V5b(b){VBb(b.k,false);b.n||(b.d.Kb[mpc]=true,undefined);b.c=false}
function W5b(b){VBb(b.k,true);b.d.Kb[mpc]=false;b.c=true;if(b.n){U5b(b);b.n=false}}
function U5b(b){if(b.o){$doc.body.removeChild(b.o);b.o.onreadystatechange=null;b.o=null}}
function kAb(b,c){b.onreadystatechange=function(){b.readyState=='complete'&&c.Nf()}}
function Z5b(b,c){if(b.e!=c){b.e=c;if(b.e){zb(b.d,1024);zb(b.d,2048)}}jb(b.Kb,'v-upload-immediate',b.e)}
function Y5b(b){N_(b.j,b.k);N_(b.j,b.d);b.d=new u6b(b);b.d.Kb.name=b.i+kyc;b.d.Kb[mpc]=!b.c;L3(b.j,b.d);L3(b.j,b.k);b.e&&zb(b.d,1024)}
function BP(){xP=new zP;_c((Zc(),Yc),22);!!$stats&&$stats(Ed(jyc,Pnc,-1,-1));xP.ad();!!$stats&&$stats(Ed(jyc,Avc,-1,-1))}
function yP(){var b,c,d;while(vP){d=uc;vP=vP.a;!vP&&(wP=null);if(!d){(btb(),atb).lg(VG,new Gub);Ekb()}else{try{(btb(),atb).lg(VG,new Gub);Ekb()}catch(b){b=_J(b);if(vs(b,37)){c=b;xqb.xe(c)}else throw b}}}}
function $5b(b){if(b.d.Kb.value.length==0||b.n||!b.c){xqb.ze('Submit cancelled (disabled, no file or already submitted)');return}_hb(b.a);b.b.submit();b.n=true;xqb.ze('Submitted form');V5b(b);b.p=new q6b(b);Wb(b.p,800)}
function X5b(b){var c;if(!b.o){c=bf($doc,$nc);c.innerHTML="<iframe src=\"javascript:''\" name='"+b.i+"_TGT_FRAME' style='position:absolute;width:0;height:0;border:0'>"||Gnc;b.o=Ze(c);$doc.body.appendChild(b.o);b.b.target=b.i+'_TGT_FRAME';kAb(b.o,b)}}
function _5b(){this.Kb=bf($doc,'form');this.d=new u6b(this);this.j=new O3;this.f=new O4;this.b=this.Kb;f6b(this.Kb,lyc);this.b.method='post';Y1(this,this.j);L3(this.j,this.f);L3(this.j,this.d);this.k=new XBb;qb(this.k,new i6b(this),(Al(),Al(),zl));L3(this.j,this.k);this.Kb[wnc]='v-upload';this.Hb==-1?FZ(this.Kb,241|(this.Kb.__eventBits||0)):(this.Hb|=241)}
var kyc='_file',myc='buttoncaption',jyc='runCallbacks22';_=zP.prototype=uP.prototype=new L;_.gC=function AP(){return Uv};_.ad=function EP(){yP()};_.cM={};_=$2.prototype=new J;_.gC=function _2(){return ey};_.Zb=function a3(b){ub(this,b)};_.cM={10:1,13:1,15:1,22:1,69:1,70:1};_=O4.prototype=N4.prototype=new J;_.gC=function P4(){return ty};_.cM={10:1,13:1,15:1,22:1,69:1,70:1};_=Gub.prototype=Fub.prototype=new L;_.Ke=function Hub(){return new _5b};_.gC=function Iub(){return PB};_.cM={137:1};_=_5b.prototype=T5b.prototype=new V1;_.gC=function a6b(){return VG};_.Yb=function b6b(){tb(this);!!this.a&&X5b(this)};_.Zb=function c6b(b){(G$(b.type)&241)>0&&(esb(this.a.F,b,this,null),undefined);ub(this,b)};_.$b=function d6b(){vb(this);this.n||U5b(this)};_.Nf=function e6b(){Xrb((Nd(),Md),new m6b(this))};_.cc=function g6b(b,c){var d;if(hib(c,this,b,true)){return}if('notStarted' in b[1]){Wb(this.p,400);return}if('forceSubmit' in b[1]){$5b(this);return}Z5b(this,Boolean(b[1][tqc]));this.a=c;this.i=b[1][spc];this.g=b[1]['nextid'];d=eib(c,b[1][wqc][auc]);this.b.action=d;if(myc in b[1]){this.k.b.innerText=b[1][myc]||Gnc;this.k.Kb.style.display=Gnc}else{this.k.Kb.style.display=Enc}this.d.Kb.name=this.i+kyc;if(mpc in b[1]||rqc in b[1]){V5b(this)}else if(!Boolean(b[1][Nqc])){W5b(this);X5b(this)}};_.cM={10:1,13:1,15:1,17:1,19:1,20:1,21:1,22:1,26:1,33:1,69:1,70:1,75:1,76:1};_.a=null;_.b=null;_.c=true;_.e=false;_.g=0;_.i=null;_.k=null;_.n=false;_.o=null;_.p=null;_=i6b.prototype=h6b.prototype=new L;_.gC=function j6b(){return RG};_.yc=function k6b(b){this.a.e?(this.a.d.Kb.click(),undefined):$5b(this.a)};_.cM={12:1,39:1};_.a=null;_=m6b.prototype=l6b.prototype=new L;_.nc=function n6b(){if(this.a.n){if(this.a.a){!!this.a.p&&Vb(this.a.p);xqb.ze('VUpload:Submit complete');_hb(this.a.a)}Y5b(this.a);this.a.n=false;W5b(this.a);this.a.Gb||U5b(this.a)}};_.gC=function o6b(){return SG};_.cM={3:1,14:1};_.a=null;_=q6b.prototype=p6b.prototype=new Sb;_.gC=function r6b(){return TG};_.ec=function s6b(){xqb.ze('Visiting server to see if upload started event changed UI.');zhb(this.a.a,this.a.i,'pollForStart',Gnc+this.a.g,true,105)};_.cM={65:1};_.a=null;_=u6b.prototype=t6b.prototype=new $2;_.gC=function v6b(){return UG};_.Zb=function w6b(b){ub(this,b);if(G$(b.type)==1024){this.a.e&&this.a.d.Kb.value!=null&&!mfc(Gnc,this.a.d.Kb.value)&&$5b(this.a)}else if((Plb(),!Olb&&(Olb=new imb),Plb(),Olb).a.g&&G$(b.type)==2048){this.a.d.Kb.click();this.a.d.Kb.blur()}};_.cM={10:1,13:1,15:1,22:1,69:1,70:1};_.a=null;var Uv=Tdc(mvc,'AsyncLoader22'),ey=Tdc(Xuc,'FileUpload'),ty=Tdc(Xuc,'Hidden'),PB=Tdc(vvc,'WidgetMapImpl$28$1'),RG=Tdc(uvc,'VUpload$1'),SG=Tdc(uvc,'VUpload$2'),TG=Tdc(uvc,'VUpload$3'),UG=Tdc(uvc,'VUpload$MyFileUpload');tnc(BP)();