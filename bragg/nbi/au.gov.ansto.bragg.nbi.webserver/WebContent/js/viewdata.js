<html xmlns="http://www.w3.org/1999/xhtml"><head runat="server"><title>View Data</title>
<META HTTP-EQUIV=REFRESH CONTENT=1>
<script type="text/javascript" src="/admin/wz_jsgraphics.js"></script>
<script type="text/javascript" src="/admin/cooltree.js"></script>
<script type="text/javascript" src="/admin/cookies.js"></script>
<script type="text/javascript">
function chgd_hostselection()
{
	hostselection_sel=document.forms[0].hostselection.value;
	window.location="selecthost.egi?hostselection="+hostselection_sel;
}
function chgd_read_data_type()
{
	read_data_type_sel=document.forms[1].read_data_type.value;
	read_data_format=document.forms[1].read_data_format.value;
	window.location="readdataselectdatatype.egi?read_data_type="+read_data_type_sel+"&read_data_format="+read_data_format;
}
function chgd_softveto()
{
	softveto1=document.forms[1].softveto1.checked;
	softveto2=document.forms[1].softveto2.checked;
	softveto3=document.forms[1].softveto3.checked;
	softveto4=document.forms[1].softveto4.checked;
	window.location="commitsoftvetostates.egi"
		+"?softveto1="+softveto1
		+"&softveto2="+softveto2
		+"&softveto3="+softveto3
		+"&softveto4="+softveto4;
}
function chgd_clientmanager()
{
	block_user_control=document.forms[1].block_user_control.checked;
	block_auto_control=document.forms[1].block_auto_control.checked;
	window.location="commitclientmanagerstates.egi"
		+"?block_user_control="+block_user_control
		+"&block_auto_control="+block_auto_control;
}
function chgd_mode_type()
{
	if(document.forms[1].mode!=undefined)view_data_mode=document.forms[1].mode.value;
	if(document.forms[1].type!=undefined)view_data_type=document.forms[1].type.value;
	if(document.forms[1].type_show!=undefined)view_data_type_show=document.forms[1].type_show.value;
	if(document.forms[1].uncal_cal_browse!=undefined)uncal_cal_browse=document.forms[1].uncal_cal_browse.value;
	if(document.forms[1].enable_count_ROI_overlay!=undefined)enable_count_ROI_overlay=document.forms[1].enable_count_ROI_overlay.checked;
	new_window_location="selectviewdatagui.egi?";
	if(document.forms[1].mode!=undefined)new_window_location+="&mode="+view_data_mode;
	if(document.forms[1].type!=undefined)new_window_location+="&type="+view_data_type;
	if(document.forms[1].type_show!=undefined)new_window_location+="&type_show="+view_data_type_show;
	if(document.forms[1].uncal_cal_browse!=undefined)new_window_location+="&uncal_cal_browse="+uncal_cal_browse;
	if(document.forms[1].enable_count_ROI_overlay!=undefined)new_window_location+="&enable_count_ROI_overlay="+enable_count_ROI_overlay;
	window.location=new_window_location;
}
function chgd_viewdatagenparam()
{
	rate_map_time_const=document.forms[2].rate_map_time_const.value;
	window.location="changeviewdatagenparam.egi?rate_map_time_const="+rate_map_time_const;
}
function chgd_format()
{
	if(document.forms[3].uncal_cal!=undefined)uncal_cal=document.forms[3].uncal_cal.value;
	if(document.forms[3].multihost_join_or_stitch!=undefined)multihost_join_or_stitch=document.forms[3].multihost_join_or_stitch.value;
	if(document.forms[3].display_type!=undefined)display_type=document.forms[3].display_type.value;
	if(document.forms[3].display_transpose_xy!=undefined)display_transpose_xy=document.forms[3].display_transpose_xy.checked;
	if(document.forms[3].scaling_type!=undefined)scaling_type=document.forms[3].scaling_type.value;
	if(document.forms[3].log_scaling_range!=undefined)log_scaling_range=document.forms[3].log_scaling_range.value;
	if(document.forms[3].root_scaling_range!=undefined)root_scaling_range=document.forms[3].root_scaling_range.value;
	new_window_location="selectviewformatgui.egi?";
	if(document.forms[3].uncal_cal!=undefined)new_window_location+="&uncal_cal="+uncal_cal;
	if(document.forms[3].multihost_join_or_stitch!=undefined)new_window_location+="&multihost_join_or_stitch="+multihost_join_or_stitch;
	if(document.forms[3].display_type!=undefined)new_window_location+="&display_type="+display_type;
	if(document.forms[3].display_transpose_xy!=undefined)new_window_location+="&display_transpose_xy="+display_transpose_xy;
	if(document.forms[3].scaling_type!=undefined)new_window_location+="&scaling_type="+scaling_type;
	if(document.forms[3].log_scaling_range!=undefined)new_window_location+="&log_scaling_range="+log_scaling_range;
	if(document.forms[3].root_scaling_range!=undefined)new_window_location+="&root_scaling_range="+root_scaling_range;
	window.location=new_window_location;
}
function chgd_options()
{
	if(document.forms[4].display_format!=undefined)display_format=document.forms[4].display_format.value;
	if(document.forms[4].colour_table!=undefined)colour_table=document.forms[4].colour_table.value;
	if(document.forms[4].font_bmp!=undefined)font_bmp=document.forms[4].font_bmp.value;
	if(document.forms[4].font_vector!=undefined)font_vector=document.forms[4].font_vector.value;
	if(document.forms[4].font_vector_shade!=undefined)font_vector_shade=document.forms[4].font_vector_shade.value;
	if(document.forms[4].plot_zero_pixels!=undefined)plot_zero_pixels=document.forms[4].plot_zero_pixels.value;
	if(document.forms[4].display_annotations!=undefined)display_annotations=document.forms[4].display_annotations.value;
	if(document.forms[4].flip_x!=undefined)flip_x=document.forms[4].flip_x.value;
	if(document.forms[4].ROI_xmin!=undefined)ROI_xmin=document.forms[4].ROI_xmin.value;
	if(document.forms[4].ROI_xmax!=undefined)ROI_xmax=document.forms[4].ROI_xmax.value;
	if(document.forms[4].MAG_x!=undefined)MAG_x=document.forms[4].MAG_x.value;
	if(document.forms[4].interp_x!=undefined)interp_x=document.forms[4].interp_x.value;
	if(document.forms[4].major_tick_marks_x!=undefined)major_tick_marks_x=document.forms[4].major_tick_marks_x.value;
	if(document.forms[4].minor_tick_marks_x!=undefined)minor_tick_marks_x=document.forms[4].minor_tick_marks_x.value;
	if(document.forms[4].flip_y!=undefined)flip_y=document.forms[4].flip_y.value;
	if(document.forms[4].ROI_ymin!=undefined)ROI_ymin=document.forms[4].ROI_ymin.value;
	if(document.forms[4].ROI_ymax!=undefined)ROI_ymax=document.forms[4].ROI_ymax.value;
	if(document.forms[4].MAG_y!=undefined)MAG_y=document.forms[4].MAG_y.value;
	if(document.forms[4].interp_y!=undefined)interp_y=document.forms[4].interp_y.value;
	if(document.forms[4].major_tick_marks_y!=undefined)major_tick_marks_y=document.forms[4].major_tick_marks_y.value;
	if(document.forms[4].minor_tick_marks_y!=undefined)minor_tick_marks_y=document.forms[4].minor_tick_marks_y.value;
	if(document.forms[4].ROI_1Dmin!=undefined)ROI_1Dmin=document.forms[4].ROI_1Dmin.value;
	if(document.forms[4].ROI_1Dmax!=undefined)ROI_1Dmax=document.forms[4].ROI_1Dmax.value;
	if(document.forms[4].histo_period!=undefined)histo_period=document.forms[4].histo_period.value;
	if(document.forms[4].histo_oat_t_bin_min!=undefined)histo_oat_t_bin_min=document.forms[4].histo_oat_t_bin_min.value;
	if(document.forms[4].histo_oat_t_bin_max!=undefined)histo_oat_t_bin_max=document.forms[4].histo_oat_t_bin_max.value;
	if(document.forms[4].auto_refresh!=undefined)auto_refresh=document.forms[4].auto_refresh.value;
	if(document.forms[4].auto_refresh_type!=undefined)auto_refresh_type=document.forms[4].auto_refresh_type.value;
	new_window_location="viewdataoptionsgui.egi?";
	if(document.forms[4].display_format!=undefined)new_window_location+="&display_format="+display_format;
	if(document.forms[4].colour_table!=undefined)new_window_location+="&colour_table="+colour_table;
	if(document.forms[4].font_bmp!=undefined)new_window_location+="&font_bmp="+font_bmp;
	if(document.forms[4].font_vector!=undefined)new_window_location+="&font_vector="+font_vector;
	if(document.forms[4].font_vector_shade!=undefined)new_window_location+="&font_vector_shade="+font_vector_shade;
	if(document.forms[4].plot_zero_pixels!=undefined)new_window_location+="&plot_zero_pixels="+plot_zero_pixels;
	if(document.forms[4].display_annotations!=undefined)new_window_location+="&display_annotations="+display_annotations;
	if(document.forms[4].flip_x!=undefined)new_window_location+="&flip_x="+flip_x;
	if(document.forms[4].ROI_xmin!=undefined)new_window_location+="&ROI_xmin="+ROI_xmin;
	if(document.forms[4].ROI_xmax!=undefined)new_window_location+="&ROI_xmax="+ROI_xmax;
	if(document.forms[4].MAG_x!=undefined)new_window_location+="&MAG_x="+MAG_x;
	if(document.forms[4].interp_x!=undefined)new_window_location+="&interp_x="+interp_x;
	if(document.forms[4].major_tick_marks_x!=undefined)new_window_location+="&major_tick_marks_x="+major_tick_marks_x;
	if(document.forms[4].minor_tick_marks_x!=undefined)new_window_location+="&minor_tick_marks_x="+minor_tick_marks_x;
	if(document.forms[4].flip_y!=undefined)new_window_location+="&flip_y="+flip_y;
	if(document.forms[4].ROI_ymin!=undefined)new_window_location+="&ROI_ymin="+ROI_ymin;
	if(document.forms[4].ROI_ymax!=undefined)new_window_location+="&ROI_ymax="+ROI_ymax;
	if(document.forms[4].MAG_y!=undefined)new_window_location+="&MAG_y="+MAG_y;
	if(document.forms[4].interp_y!=undefined)new_window_location+="&interp_y="+interp_y;
	if(document.forms[4].major_tick_marks_y!=undefined)new_window_location+="&major_tick_marks_y="+major_tick_marks_y;
	if(document.forms[4].minor_tick_marks_y!=undefined)new_window_location+="&minor_tick_marks_y="+minor_tick_marks_y;
	if(document.forms[4].ROI_1Dmin!=undefined)new_window_location+="&ROI_1Dmin="+ROI_1Dmin;
	if(document.forms[4].ROI_1Dmax!=undefined)new_window_location+="&ROI_1Dmax="+ROI_1Dmax;
	if(document.forms[4].histo_period!=undefined)new_window_location+="&histo_period="+histo_period;
	if(document.forms[4].histo_oat_t_bin_min!=undefined)new_window_location+="&histo_oat_t_bin_min="+histo_oat_t_bin_min;
	if(document.forms[4].histo_oat_t_bin_max!=undefined)new_window_location+="&histo_oat_t_bin_max="+histo_oat_t_bin_max;
	if(document.forms[4].auto_refresh!=undefined)new_window_location+="&auto_refresh="+auto_refresh;
	if(document.forms[4].auto_refresh_type!=undefined)new_window_location+="&auto_refresh_type="+auto_refresh_type;
	window.location=new_window_location;
}
function chgd_open_format()
{
	new_window_location="changeopenformat.egi?";
	if(document.forms[7].open_format!=undefined)new_window_location+="&open_format="+document.forms[7].open_format.value;
	if(document.forms[7].open_colour_table!=undefined)new_window_location+="&open_colour_table="+document.forms[7].open_colour_table.value;
	if(document.forms[7].open_font_bmp!=undefined)new_window_location+="&open_font_bmp="+document.forms[7].open_font_bmp.value;
	if(document.forms[7].open_font_ps!=undefined)new_window_location+="&open_font_ps="+document.forms[7].open_font_ps.value;
	if(document.forms[7].open_font_win!=undefined)new_window_location+="&open_font_win="+document.forms[7].open_font_win.value;
	if(document.forms[7].open_font_vector!=undefined)new_window_location+="&open_font_vector="+document.forms[7].open_font_vector.value;
	if(document.forms[7].open_font_vector_shade!=undefined)new_window_location+="&open_font_vector_shade="+document.forms[7].open_font_vector_shade.value;
	if(document.forms[7].open_plot_zero_pixels!=undefined)new_window_location+="&open_plot_zero_pixels="+document.forms[7].open_plot_zero_pixels.value;
	if(document.forms[7].open_annotations!=undefined)new_window_location+="&open_annotations="+document.forms[7].open_annotations.value;
	window.location=new_window_location;
}
function chgd_t_bin_range(forward_back)
{
	t_bin_min=0; if(document.forms[4].histo_oat_t_bin_min!=undefined) t_bin_min=parseInt(document.forms[4].histo_oat_t_bin_min.value);
	t_bin_max=0; if(document.forms[4].histo_oat_t_bin_max!=undefined) t_bin_max=parseInt(document.forms[4].histo_oat_t_bin_max.value);
	t_bin_diff=t_bin_max-t_bin_min+1; if (forward_back) t_bin_diff=-t_bin_diff; t_bin_min=t_bin_min+t_bin_diff; t_bin_max=t_bin_max+t_bin_diff;
	if(document.forms[4].histo_oat_t_bin_min!=undefined) document.forms[4].histo_oat_t_bin_min.value=t_bin_min;
	if(document.forms[4].histo_oat_t_bin_max!=undefined) document.forms[4].histo_oat_t_bin_max.value=t_bin_max;
	chgd_options();
}
function startDAQpressed()
{
	document.startDAQ.src="/admin/icons/StepForwardHot.png";
}
var screenWidth = 0, screenHeight = 0;
function getscreensize()
{
	if( typeof( window.innerWidth ) == 'number' ) {
		//Non-IE
		screenWidth = window.innerWidth;
		screenHeight = window.innerHeight;
	} else if( document.documentElement && (document.documentElement.clientWidth || document.documentElement.clientHeight ) ) {
		//IE 6+ in 'standards compliant mode'
		screenWidth = document.documentElement.clientWidth;
		screenHeight = document.documentElement.clientHeight;
	} else if( document.body && ( document.body.clientWidth || document.body.clientHeight ) ) {
		//IE 4 compatible
		screenWidth = document.body.clientWidth;
		screenHeight = document.body.clientHeight;
	}
}
function autosetscreensize_browser()
{
	window.location="setscreensizegui.egi?screen_size_x="+screenWidth+"&screen_size_y="+screenHeight+"&browser="+navigator.appName;
}
function setscreensize(confirm_dialog)
{
	getscreensize();
	var r=false; if (confirm_dialog) r=confirm("Your window size is detected as "+screenWidth+" * "+screenHeight+".\nSet it?");
	if (!confirm_dialog||r==true)
	{
		autosetscreensize_browser();
	}
}
function findPos(obj)
{
	var curleft = curtop = 0;
	if (obj.offsetParent) {
		do {
			curleft+=obj.offsetLeft;
			curtop+=obj.offsetTop;
		} while(obj=obj.offsetParent);
		return [curleft,curtop];
	}
}
function findScroll()
{
	var scroll_x=0,scroll_y=0;
	if (self.pageYOffset) { scroll_x=self.pageXOffset; scroll_y=self.pageYOffset; }
	else if (document.documentElement&&document.documentElement.scrollTop) { scroll_x=document.documentElement.scrollLeft; scroll_y=document.documentElement.scrollTop; }
	else if (document.body) { scroll_x=document.body.scrollLeft; scroll_y=document.body.scrollTop; }
	return [scroll_x,scroll_y];
}
function saveScrollPos(coords_scroll)
{
	Cookies.set("scroll_x",String(coords_scroll[0]),1);
	Cookies.set("scroll_y",String(coords_scroll[1]),1);
}
function saveTreeSel()
{ var tree_sel="";
var i=0; if (tree1)while(i<tree1.Nodes.length){if(tree1.Nodes[i]){if(tree1.Nodes[i].expanded) tree_sel+="1"; else tree_sel+="0";} else tree_sel+="0";i++;}
Cookies.set("tree_sel",tree_sel,1);
}
function chgdxATmodify(modify_level)
{
	saveScrollPos(findScroll());
	saveTreeSel();
	dynamicxATmodifysectionname=document.forms[5].dynamicxATmodifysectionname.value;
	windowlocsectparam="selectdynamicxatmodifygui.egi?dynamicxATmodifysectionname="+dynamicxATmodifysectionname;
	if (modify_level>=1) {
		dynamicxATmodifyparamname=document.forms[5].dynamicxATmodifyparamname.value;
		windowlocsectparam=windowlocsectparam+"&dynamicxATmodifyparamname="+dynamicxATmodifyparamname;}
	if (modify_level>=2) {
		dynamicxATmodifyparamvalue=document.forms[5].dynamicxATmodifyparamvalue.value;
		windowlocsectparam=windowlocsectparam+"&dynamicxATmodifyparamvalue="+dynamicxATmodifyparamvalue;}
	window.location=windowlocsectparam;
}
function chgd_xAT(xat_attr_id,orig_value_or_index)
{
	saveScrollPos(findScroll());
	saveTreeSel();
	var xat_attr=document.getElementById(xat_attr_id);
	var xat_attr_name=xat_attr.id;
	var xat_attr_val=xat_attr.value;
	var r=confirm("Set "+xat_attr_name+" value to "+xat_attr_val+"?");
	if (r==true) { HOSTindex=xat_attr_name.indexOf("HOST");dashindex=xat_attr_name.indexOf("-");
	if (HOSTindex==0&&dashindex>0) { r=confirm("Duplicate the setting to all hosts?");
	if (r==true) xat_attr_name=xat_attr_name.substring(dashindex+1); }
	window.location="selectdynamicxatmodifygui.egi?dynamicxATmodifyparamname="+xat_attr_name+"&dynamicxATmodifyparamvalue="+xat_attr_val;
	} else {xat_attr.selectedIndex=orig_value_or_index; xat_attr.value=orig_value_or_index;}
}
function manuallyreconfiguresettings()
{
	var r=confirm("This will stop DAQ control and configuration from all other clients while settings are reconfigured.\nAny changes made will immediately modify the current configuration.\nDo you want to proceed?");
	if (r==true) { alert("Entering manual reconfiguration mode.  All settings can now be modified.\nPress the 'Apply Manual reconfigured settings' button when all modifications have been made.");
	}
	alert("Sorry, this feature isn't implemented yet...");
}
function endmanualreconfiguresettings()
{
	alert("Exiting manual reconfiguration mode and updating the internal configuration.\nIf the Status pane indicates an error, check the Log to find out the cause.");
}
function bin_at_pixel_loc(imageloc,imageframe_0,imageframe_1,bin_0,bin_1,dualview)
{
	if(dualview) {
		if(imageloc<(imageframe_0+imageframe_1)/2) {
			imageloc=imageloc*2-imageframe_0;
			if (bin_0<bin_1) bin_1=bin_1-0.5; else bin_1=bin_1+0.5;
		} else {
			imageloc=imageloc*2-imageframe_1;
			if (bin_0<bin_1) bin_0=bin_0+0.5; else bin_0=bin_0-0.5;
		}
	}
	var bin_loc=bin_0+(imageloc-imageframe_0)/(imageframe_1-imageframe_0)*(bin_1-bin_0);
	return(bin_loc);
}
function bin_at_pixel(imageloc,imageframe_0,imageframe_1,bin_0,bin_1,dualview)
{
	var bin_loc=bin_at_pixel_loc(imageloc,imageframe_0,imageframe_1,bin_0,bin_1,dualview);
	var bin=Math.floor(bin_loc+0.5);
	return(bin);
}
function inside_xy(x,y,ix0,iy0,ix1,iy1,margin)
{
	return(x>=ix0-margin&&x<=ix1+margin&&y>=iy0-margin&&y<=iy1+margin);
}
function relimit_x(x,ix0,ix1)
{
	if(x<ix0) x=ix0;
	if(x>ix1) x=ix1;
	return x;
}
function relimit_y(y,iy0,iy1)
{
	if(y<iy0) y=iy0;
	if(y>iy1) y=iy1;
	return y;
}
var t=0,jg_doc,coords_container=[0,0],init_ROIselect_x=0,init_ROIselect_y=0,init_ROIselect=1,colortoggle=0;
var imageclick_x1=-1,imageclick_y1=-1,imageclick_bin_x1=-1,imageclick_bin_y1=-1,old_imagecurr_x=-1,old_imagecurr_y=-1,imgContainer,tooltip;
var margin=10;
function onmousemove_ROIselect_handler(eventData,force_redraw,ix0,iy0,ix1,iy1,is_1D,bin_x0,bin_y0,bin_x1,bin_y1,viewing_full_ROI,dualview,scaling_type_eff,root_scaling_range,type_is_floating_point,num_floating_point_dp)
{
	if (init_ROIselect>0)
	{
		imgContainer = document.getElementById("imgContainer");
		coords_container = findPos(imgContainer);
		tooltip = document.getElementById("imgContainerTooltip");
		init_ROIselect = 0;
	}
	var coords_scroll = findScroll();
	var imagecurr_x=eventData.clientX-coords_container[0]+coords_scroll[0]; // eventData.offsetX
	var imagecurr_y=eventData.clientY-coords_container[1]+coords_scroll[1]; // eventData.offsetY;
	if (imagecurr_x!=old_imagecurr_x||imagecurr_y!=old_imagecurr_y||force_redraw) {
		jg_img.clear();
		if (inside_xy(imagecurr_x,imagecurr_y,ix0,iy0,ix1,iy1,margin)) {
			imagecurr_x=relimit_x(imagecurr_x,ix0,ix1);
			imagecurr_y=relimit_y(imagecurr_y,iy0,iy1);
			imgContainer.style.cursor="crosshair";
			if (imageclick_x1>=0&&imageclick_y1>=0) {
				var x0,y0,x1,y1;
				if (imagecurr_x<imageclick_x1) {x0=imagecurr_x;x1=imageclick_x1;} else {x1=imagecurr_x;x0=imageclick_x1;}
				if (is_1D) {y0=iy0;y1=iy1;}
				else if (imagecurr_y<imageclick_y1) {y0=imagecurr_y;y1=imageclick_y1;} else {y1=imagecurr_y;y0=imageclick_y1;}
				if(!colortoggle)
					jg_img.setColor("#7f20ff");
				else
					jg_img.setColor("#ffffff");
				jg_img.drawLine(x0-1,y0-1,x1+1,y0-1);
				jg_img.drawLine(x1+1,y0-1,x1+1,y1+1);
				jg_img.drawLine(x1+1,y1+1,x0-1,y1+1);
				jg_img.drawLine(x0-1,y1+1,x0-1,y0-1);
				if(colortoggle)
					jg_img.setColor("#7f20ff");
				else
					jg_img.setColor("#ffffff");
				jg_img.drawLine(x0+1,y0+1,x1-1,y0+1);
				jg_img.drawLine(x1-1,y0+1,x1-1,y1-1);
				jg_img.drawLine(x1-1,y1-1,x0+1,y1-1);
				jg_img.drawLine(x0+1,y1-1,x0+1,y0+1);
				jg_img.paint();
				old_imagecurr_x=imagecurr_x;
				old_imagecurr_y=imagecurr_y;
			}
			tooltip.style.left=imagecurr_x+8;
			tooltip.style.top=imagecurr_y+8;
			tooltip.style.width="auto";
			tooltip.style.height="auto";
			var bin_x=bin_at_pixel(imagecurr_x,ix0,ix1,bin_x0,bin_x1,dualview);
			if(!is_1D) bin_y=bin_at_pixel(imagecurr_y,iy0,iy1,bin_y0,bin_y1,0); else {
				bin_y=bin_at_pixel_loc(imagecurr_y,iy0,iy1,bin_y0,bin_y1,0);
				if(scaling_type_eff==1) bin_y=Math.exp(bin_y*Math.log(10));
				else if(scaling_type_eff==2) bin_y=Math.exp(root_scaling_range*Math.log(bin_y));
				if(!type_is_floating_point) bin_y=Math.floor(bin_y+0.5);
				else { var val_num_dp=Math.pow(10,num_floating_point_dp); bin_y=Math.floor(bin_y*val_num_dp+0.5)/val_num_dp; }
			}
			tooltip.innerHTML="["+bin_x+","+bin_y+"]";
		} else {
			tooltip.style.width=0;
			tooltip.style.height=0;
			tooltip.innerHTML="";
			if (!viewing_full_ROI&&!(imageclick_x1>=0&&imageclick_y1>=0))
				imgContainer.style.cursor="pointer";
			else
				imgContainer.style.cursor="default";
		}
	}
}
function onclick_ROIselect_handler(eventData,ix0,iy0,ix1,iy1,bin_x0,bin_y0,bin_x1,bin_y1,viewing_full_ROI,dualview)
{
	coords_container = findPos(document.getElementById("imgContainer"));
	var coords_scroll = findScroll();
	var imageclick_x=eventData.clientX-coords_container[0]+coords_scroll[0]; // eventData.offsetX
	var imageclick_y=eventData.clientY-coords_container[1]+coords_scroll[1]; // eventData.offsetY;
	var do_redirect=0;
	if (!inside_xy(imageclick_x,imageclick_y,ix0,iy0,ix1,iy1,margin)) { 
		if (imageclick_x1>=0&&imageclick_y1>=0) { imageclick_x1=-1; imageclick_y1=-1; imageclick_bin_x1=-1; imageclick_bin_y1=-1; }
		else if (!viewing_full_ROI) { do_redirect=1; imageclick_x1=-1; imageclick_y1=-1; imageclick_x=0; imageclick_y=0; }
	} else {  
		imageclick_x=relimit_x(imageclick_x,ix0,ix1);
		imageclick_y=relimit_y(imageclick_y,iy0,iy1);
		var bin_x=bin_at_pixel(imageclick_x,ix0,ix1,bin_x0,bin_x1,dualview);
		var bin_y=bin_at_pixel(imageclick_y,iy0,iy1,bin_y0,bin_y1,0);
		if (imageclick_x1<0&&imageclick_y1<0) { imageclick_x1=imageclick_x; imageclick_y1=imageclick_y; imageclick_bin_x1=bin_x; imageclick_bin_y1=bin_y; }
		else do_redirect=1;
	}
	Cookies.erase("imageclickx1");
	Cookies.erase("imageclicky1");
	Cookies.erase("imageclickbinx1");
	Cookies.erase("imageclickbiny1");
	if (do_redirect>0) {
		saveScrollPos(coords_scroll);
		window.location="hsgraphclick.egi?"+imageclick_x1+","+imageclick_y1+","+imageclick_x+","+imageclick_y+"&bin_x1="+imageclick_bin_x1+"&bin_y1="+imageclick_bin_y1+"&bin_x2="+bin_x+"&bin_y2="+bin_y;
	} else {
		Cookies.set("imageclickx1",String(imageclick_x1),1);
		Cookies.set("imageclicky1",String(imageclick_y1),1);
		Cookies.set("imageclickbinx1",String(imageclick_bin_x1),1);
		Cookies.set("imageclickbiny1",String(imageclick_bin_y1),1);
	}
}
function ReadAllCookies()
{
	if (1) {
		if (Cookies.get("scroll_x")) scroll_x=parseInt(Cookies.get('scroll_x'));
		if (Cookies.get("scroll_y")) scroll_y=parseInt(Cookies.get('scroll_y'));
		if (Cookies.get("tree_sel")) tree_sel=Cookies.get('tree_sel'); else tree_sel="1";
		if (Cookies.get("imageclickx1")) imageclick_x1=parseInt(Cookies.get('imageclickx1'));
		if (Cookies.get("imageclicky1")) imageclick_y1=parseInt(Cookies.get('imageclicky1'));
		if (Cookies.get("imageclickbinx1")) imageclick_bin_x1=parseInt(Cookies.get('imageclickbinx1'));
		if (Cookies.get("imageclickbiny1")) imageclick_bin_y1=parseInt(Cookies.get('imageclickbiny1'));
	} else alert('Cookies not supported!!!');
}
function onmousemove_ROIselect_handler_rearm(eventData)
{
	init_ROIselect=1;
	window.location="viewdata.egi";
}
function onresize_handler(event)
{
	setscreensize(false);
}
function onload_handler(event)
{
	ReadAllCookies();
	onmousemove_ROIselect_handler_noarg(event,1);
	if (scroll_x||scroll_y) { window.scrollTo(scroll_x,scroll_y); saveScrollPos([0,0]); }
	var i=0;while(i<tree1.Nodes.length){if (tree1&&tree_sel!=undefined&&tree_sel.charAt(i)=='1') tree1.expandNode(i);i++;}
}
</script></HEAD>
<link rel="stylesheet" type="text/css" href="/home.css">
<BODY onload="onload_handler(event);" onresize="onresize_handler(event);">
<script type="text/javascript">
function xATname(nhost,idstr_section,idstr_attr_or_index)
{
	var htmlout="";
	if (nhost>=0) htmlout+="HOST"+nhost+"-";
	htmlout+=idstr_section+"."+idstr_attr_or_index;
	return htmlout;
}
function xATinput(nhost,idstr_section,idstr_attr_or_index,value,size,disabled)
{
	var htmlout="<input style=\"font-size: 8pt;\"";
	if (disabled) htmlout+=" disabled=\"disabled\"";
	htmlout+=" type=\"text\" id=\""+xATname(nhost,idstr_section,idstr_attr_or_index)+"\" onchange=\"chgd_xAT(this.id,'"+value+"')\" size=\""+size+"\" value=\""+value+"\">\n";
	return htmlout;
}
function xATselect(nhost,idstr_section,idstr_attr_or_index,options,option_selected_index,disabled)
{
	var htmlout="<select style=\"font-size: 8pt;\"";
	if (disabled) htmlout+=" disabled=\"disabled\"";
	htmlout+=" id=\""+xATname(nhost,idstr_section,idstr_attr_or_index)+"\" onchange=\"chgd_xAT(this.id,"+option_selected_index+")\">\n";
	var i;var ni=options.length;for(i=0;i<ni;i++) {
		htmlout+="<option";
		if (i==option_selected_index) htmlout+=" selected=\"selected\"";
		htmlout+=" value=\""+options[i]+"\">"+options[i]+" ("+String(i)+")</option>";
	}
	htmlout+="</select>";
	return htmlout;
}
function xATarray(nhost,idstr_section,isenum,options,arrayvar,disabled)
{
	var htmlout="";var i;var ni=arrayvar.length;var il;for(i=0;i<ni;i++) {
		il=i%10;
		if (il==0) htmlout+="<tr>";
		htmlout+="<td>";
		if (isenum) htmlout+=xATselect(nhost,idstr_section,i,options,arrayvar[i],disabled); else htmlout+=xATinput(nhost,idstr_section,i,arrayvar[i],12,disabled);
		htmlout+="</td>";
		if (il==9) htmlout+="</tr>";
	}
	if (il!=9) htmlout+="</tr>";
	return htmlout;
}
function mytest()
{
	return 'This is a test!!!';}
</script>
<!-- BeginDsi "dsi/menu.html" --><div class="top">    <div class="topLeft">        <map name="home" id="home0">            <area coords="5,15,200,150" href="/admin/menu.egi" alt="ANSTO OPAL NBI Histogram Server"/>        </map>        <img src="/admin/icons/topLeftSmall.gif" ismap="1" usemap="#home" alt="ANSTO OPAL NBI Histogram Server"/>    </div>    <div class="topRight">        <img src="/admin/icons/topRightSmall.gif" alt="ANSTO OPAL NBI Histogram Server"/>    </div>    <div class="topMiddleSmall">        <img src="/admin/icons/topMiddleSmall.gif" alt="ANSTO OPAL NBI Histogram Server"/>    </div>    <div class="menu">        <ul>            <!-- <li><a href="/admin/rawdata.egi">Raw Data</a></li> -->            <li><a href="/admin/clientdata.egi">Clients</a></li>            <li><a href="/admin/debugbuffer.egi">Log</a></li> <!-- was 'Debug SHM' -->            <!-- <li><a href="/admin/datashm.egi">Data SHM</a></li> -->            <!-- <li><a href="/admin/configshm.egi">Config SHM</a></li> -->            <!-- <li><a href="/admin/statusshm.egi">Status SHM</a></li> -->            <li><a href="/admin/viewdata.egi">View</a></li>            <li><a href="/admin/vetodata.egi">Soft Veto</a></li>            <li><a href="/admin/readdata.egi">Read</a></li>            <li><a href="/admin/streamdata.egi">Stream</a></li>            <li><a href="/admin/showconfig.egi">Config</a></li>            <li><a href="/admin/status.egi">Status</a></li>        </ul>    </div></div><div class="content"><table width="100%" bgcolor="silver" border="1" style="border: 1px solid #A0A0A0;"><tr><td width="440" align="center" ><table bgcolor="silver" border="1" style="border: 1px solid #A0A0A0;"><tr><td align="center" bgcolor="#8080FF"><small>Updated: Tue Nov 10 14:14:26 2015
</small></td></tr><tr><td><table border="1" style="border: 1px solid #A0A0A0;" width="420" bgcolor="silver" border="0"><tr><td align="center" valign="baseline"><h3 style="color:darkblue"><i><b>Pelican</b></i></h3><b>Mesytec MPSD8+/MCPD8 Ethernet DAE</b><BR><BR><form action="/admin/autorefreshcontrol.egi" method="get"><input type="submit" value="Disable auto-refresh"></form></td><td width="70%" align="center" ><b style="color:red">Controlled by SICS@ics1-pelican.nbi.ansto.gov.au:8080</b><BR><a href="/admin/guistopdaq.egi?viewdata"><img src="/admin/icons/Stop1Disabled.png" hspace="8" vspace="4" alt="Stop"></a><a href="/admin/guipausedaq.egi?viewdata"><img src="/admin/icons/StepForwardDisabled.png" hspace="8" vspace="4" alt="Pause"></a><a href="/admin/guistartdaq.egi?viewdata"><img src="/admin/icons/StepForwardDisabled.png" hspace="8" vspace="4" alt="Start" name="startDAQ" onClick="startDAQpressed()" ></a><BR><a href="/admin/guienablesoftveto.egi?viewdata"><img src="/admin/icons/Stop1Disabled.png" hspace="8" vspace="4" alt="Veto"></a><a href="/admin/guidisablesoftveto.egi?viewdata"><img src="/admin/icons/StepForwardDisabled.png" hspace="8" vspace="4" alt="Unveto"></a></td></tr></table></tr></table></td><td align="center" bgcolor="#009900"><table border="1" style="border: 1px solid #A0A0A0;"><tr><td align="center" bgcolor="#009900"> DAQ Started<BR>DATASET 21 </td></tr></table></td><td align="center" bgcolor="#8080FF"><b>DAQ_2015-11-09T14-53-06</b><BR><b>Acquiring DATASET 21</b><BR>Events in this DATASET:<BR>1381248 (1.38 million)<BR>Frame: 110071, Period: 0</td><td align="left" bgcolor="#c0c055"><pre>  All DAQ:  771:15:21.93
Curr DAQ:   23:21:14.91
DATASET:    0:18:20.72</pre><pre>Server Up:  817:22:39</pre></td><td align="left" bgcolor="#d08855"><pre>BM1: 911.6 counts/sec
1062708 counts
BM2: 0.000 counts/sec
0 counts
</pre></td><td align="center" bgcolor="#ff3300">Errors<BR>Logged</td></tr></table><script type="text/javascript">function onclick_ROIselect_handler_noarg(event) { onclick_ROIselect_handler(event,134,95,1350,765,251.000000,2.877371,333.000000,0.877371,0,0); }function onmousemove_ROIselect_handler_noarg(event,force_redraw) { onmousemove_ROIselect_handler(event,force_redraw,134,95,1350,765,1,251.000000,2.877371,333.000000,0.877371,0,0,1,2.000000,0,1); }</script><div id="imgContainer" style="position: relative; z-index: 4; cursor: pointer;" onclick="onclick_ROIselect_handler_noarg(event)" onmousemove="onmousemove_ROIselect_handler_noarg(event,0)"><IMG id="HSgraph" SRC="ram_images/HSG_1_2015-11-10T14-14-26.png" ALT="HSgraph" WIDTH=1451 HEIGHT=868></IMG><div id="imgContainerInner" style="position: absolute; cursor: crosshair; left: 134; top: 95; width: 1217; height: 671; z-index: 3;"></div><div id="imgContainerTooltip" style="position: absolute; left: 0; top: 0; width: 0; height: 0; background:#ffffff"; z-index: 5;></div></div></td></tr></table></div><pre>
Questions and Comments to Mark Lesha (ANSTO)
Original version: Gerd Theidel (SINQ), Mark Koennecke (SINQ)
</pre>
<PRE>ANSTO OPAL NBI Histogram Server Version 2.12.2.0 built Sep 21 2015 14:41:04 gcc-4.3.0 20071129 (experimental) [trunk revision 130511]</PRE><script type="text/javascript">
jg_img = new jsGraphics("imgContainer");
</script></body></html>