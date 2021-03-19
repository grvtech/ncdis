/*
 * link event to button to open the list
 * */
$('#locallist-button').click(function(){
	openList();
});

/*
 * global variable fo list
 * */
var containerApp = $('#wraper');
var toolbarConfig = {"container":"locallist-toolbar","lists":[{"id":"list101","title":"Trend of HBA1c value","selected":"false"},{"id":"list102","title":"Patients with old HBA1c value collected","selected":"false"},{"id":"list103","title":"Patients with high HBA1c value","selected":"false"},{"id":"list104","title":"Patients with no HBA1c value","selected":"false"}],"options":[]};
var dataperiodValues = [3,6,12,18,0];
var listConfig = {
		"container":"locallist-list",
		"initFilter":{"list":"list101","idcommunity":"0","dp":"4","dtype":"1_2","age":"0","hba1c":"0"},
		"list101":{"header":["fullname","ramq","chart","age","dtype","trend","last_hba1c","last_hba1c_collecteddate","secondlast_hba1c","secondlast_hba1c_collecteddate"]},
		"list102":{"header":["fullname","ramq","chart","age","dtype","trend","last_hba1c","last_hba1c_collecteddate"]},
		"list103":{"header":["fullname","ramq","chart","age","dtype","trend","last_hba1c","last_hba1c_collecteddate"]},
		"list104":{"header":["fullname","ramq","chart","age","dtype","idcommunity","sex"]}
};
var statsConfig = {"container":"locallist-stats"};
var appFilter = listConfig.initFilter;
var globalReport = null;
var testReport = null;
var globalNoHBA1cReport = null;
var testNoHBA1cReport = null;

var nbImproved = 0;
var prImproved = 0;

var nbDegrade = 0;
var prDegrade = 0;

var nbNodata = 0;
var prNodata = 0;

var deciles = ["0-9", "10-19", "20-29", "30-39", "40-49", "50-59", "60-69","70-79", "80+"];
var male = [];
var female = [];


/*
 * function to open the window of the report
 * the window will be splited in 3 parts ( subwindows)
 * 1 on the left - filters and choises 
 * 2 the main list
 * 3 on the bottom - stats of the list
 * 
 * */
function openList(){
	//create the modal window
	var id = "locallist";
	var modal = $('<div>',{id:"fullscreen_"+id,class:"fullscreen-modal"}).appendTo(containerApp);
	buildFrameList(modal);
	setTimeout(loadReport, 10, "locallist");
	setTimeout(loadNoHbA1cPatients,100);
}

/*
 * function to load the report in the JSON object
 * */
function loadReport(reportID){
	var url = "/ncdis/client/reports/report."+reportID+"?ts="+moment();
	var result = null;
	var req1 = $.ajax({dataType: "json",url: url,async:false,cache:false});
	req1.success(function(obj){
		result = obj;
		testReport = obj;
		globalReport = obj;
		setTimeout(displayData,305,testReport);
	});
	req1.fail(function( jqXHR, textStatus, errorThrown){console.log("AJAX failed!", jqXHR, textStatus, errorThrown)});
	return result;
}


function loadNoHbA1cPatients(){
	var url = "/ncdis/client/reports/report.nohba1c?ts="+moment();
	$.getJSON(url, function(data){
		globalNoHBA1cReport = data;
		testNoHBA1cReport = data;
	}).fail(function(){
        console.log("An error has occurred.");
    });
}

/*
 * function to build the frame
 * */
function buildFrameList(container){
	var header = $('<div>',{class:"fullscreen-modal-header"}).appendTo(container);
	$('<div>',{class:"fullscreen-modal-header-logo"}).appendTo(header);
	$('<div>',{class:"fullscreen-modal-header-cdis"}).text("CDIS").appendTo(header);
	$('<div>',{class:"fullscreen-modal-header-title"}).text("Reports").appendTo(header);
	$('<div>',{class:"fullscreen-modal-header-close"}).html($('<i>',{class:"fa fa-times"})).click(function(){container.remove();}).appendTo(header);
	var body = $('<div>',{class:"fullscreen-modal-body"}).appendTo(container);
	$('<div>',{"id":"locallist-toolbar",class:"fullscreen-modal-toolbar"}).appendTo(body);
	var c = $('<div>',{"id":"locallist-list",class:"fullscreen-modal-list"}).appendTo(body);
	showProgress(c);
	$('<div>',{"id":"locallist-stats",class:"fullscreen-modal-stats"}).appendTo(body);
	
	
	$('<div>',{class:"fullscreen-modal-footer"}).appendTo(container);
}


/*function to display data from the report
 * will build the toolbar an the stats 
 * */
function displayData(report){
	
	
	//drawList(appFilter.list, listConfig, report);
	//setTimeout(drawToolbar,100, toolbarConfig);
	//setTimeout(drawStats, 200, statsConfig);
	setTimeout(drawToolbar,100, toolbarConfig);
	setTimeout(drawStats,50, statsConfig, report);
	setTimeout(drawList,200, appFilter.list, listConfig, report);
	
	
	
	//drawList(appFilter.list, listConfig, report);
	
	
}




function setToolbar(filter){
	$("#comm-filter").val(filter.idcommunity);
	$("#dp-filter").val(filter.dp);
	
	var dtypeValue = filter.dtype;
	$.each($(".gr-dtype-checkbox div"),function(i,e){
		var rv = $(this).attr("grv-data");
		if(dtypeValue.indexOf(rv) >= 0 ){
			$(this).addClass("selected");
		}
	});
	
	var ageValue = filter.age;
	if(ageValue == "0"){
		$(".gr-age-radio div").removeClass("selected");
		$("#age-filter-1").addClass("selected");
	}else{
		$(".gr-age-radio div").removeClass("selected");
		$("#age-filter-2").addClass("selected");
		$(".gr-age-custom").css("visibility","visible");
		var vs = ageValue.split('_');
		$("#age-custom-min").val(vs[0]);
		$("#age-custom-max").val(vs[1]);
	}
	
	var a1cValue = filter.hba1c;
	if(a1cValue == "0"){
		$(".gr-a1c-radio div").removeClass("selected");
		$("#a1c-filter-1").addClass("selected");
	}else{
		$(".gr-a1c-radio div").removeClass("selected");
		$("#a1c-filter-2").addClass("selected");
		$(".gr-a1c-custom").css("visibility","visible");
		var vs = a1cValue.split('_');
		$("#a1c-custom-min").val(vs[0]);
		$("#a1c-custom-max").val(vs[1]);
	}
	
	$.each($(".list-tabs .list-tab"), function(i, list){
		$(".list-tab").removeClass("selected");
		$("#list-"+filter.list).addClass("selected");
	});
	
}



function renderList(listid){
	//drawHeader
	
}


function drawToolbarFilters(container){
	//container
	var gr2col = $("<div>",{class:"gr-2col1"}).appendTo(container);
	var gr2col1 = $("<div>",{class:"gr-2col2"}).appendTo(container);
	//community
	var grComm = $("<div>",{"id":"toolbarFilterCommunity",class:"gr-comm"}).appendTo(gr2col);
	$("<div>",{"for":"comm-filter"}).text("Community").appendTo(grComm);
	var grCommS = $("<select>",{"id":"comm-filter",class:""}).appendTo(grComm);
	$.each(report_idcommunity, function(j,com){
		$("<option>",{"value":j}).text(com).appendTo(grCommS);
	});
	grCommS.on("change",function(){
		var v = $(this).val();
		if(v!=appFilter.idcommunity){appFilter["idcommunity"]=v;}
	});
	
	//data period
	var grDp = $("<div>",{"id":"toolbarFilterDataperiod",class:"gr-dp"}).appendTo(gr2col);
	$("<div>",{"for":"dp-filter"}).text("Data period").appendTo(grDp);
	var grDpS = $("<select>",{"id":"dp-filter"}).appendTo(grDp);
	$.each(report_dp, function(x,dp){
		$("<option>",{"value":x}).text(dp).appendTo(grDpS);
	});
	grDpS.on("change",function(){
		var v = $(this).val();
		if(v!=appFilter.dp){appFilter["dp"]=v;}
	});
	
	//dtype
	var grDtype = $("<div>",{"id":"toolbarFilterDtype",class:"gr-dtype"}).appendTo(gr2col);
	$("<div>").text("Type of diabetes").appendTo(grDtype);
	var grDtypeC = $("<div>",{class:"gr-dtype-checkbox"}).appendTo(grDtype);
	$("<div>",{"id":"dtype-filter-1",class:"gr-radio","grv-data":"1_2"}).appendTo(grDtypeC).text("Type1 and Type 2");
	$("<div>",{"id":"dtype-filter-2",class:"gr-radio","grv-data":"3"}).appendTo(grDtypeC).text("Pre DM");
	$("<div>",{"id":"dtype-filter-3",class:"gr-radio last","grv-data":"4"}).appendTo(grDtypeC).text("GDM");
	$(".gr-dtype-checkbox div").click(function(){
		var dValue = appFilter.dtype;
		var rv = $(this).attr("grv-data");
		if($(this).hasClass("selected")){
			$(this).removeClass("selected");
			dValue = dValue.replace(rv,'');
		}else{
			$(this).addClass("selected");
			dValue = dValue+rv;
		}
		appFilter['dtype'] = dValue;
	});
	//age
	var grAge = $("<div>",{"id":"toolbarFilterAge",class:"gr-age"}).appendTo(gr2col1);
	$("<div>").text("Age").appendTo(grAge);
	var grAgeC = $("<div>",{class:"gr-age-radio gr"}).appendTo(grAge);
	$("<div>",{"id":"age-filter-1",class:"gr-radio","grv-data":"1"}).appendTo(grAgeC).text("All");
	$("<div>",{"id":"age-filter-2",class:"gr-radio last","grv-data":"2"}).appendTo(grAgeC).text("Custom");
	var grAgeCustom = $("<div>",{class:"gr-age-custom gr-custom"}).appendTo(grAgeC);
	$("<label>",{}).appendTo(grAgeCustom).text("Between age min");
	$("<input>",{"type":"text","id":"age-custom-min"}).appendTo(grAgeCustom);
	$("<label>",{}).appendTo(grAgeCustom).text(" and max");
	$("<input>",{"type":"text","id":"age-custom-max"}).appendTo(grAgeCustom);
	//$("<button>",{"id":"age-custom-button",class:"cisbutton"}).text("Set").appendTo(grAgeCustom);
	$(".gr-age-radio .gr-radio").click(function(){
		$(".gr-age-radio div").removeClass("selected");
		$(this).addClass("selected");
		if($(this).attr("grv-data") == "2"){
			$(".gr-age-custom").css("visibility","visible");
			
		}else{
			$(".gr-age-custom").css("visibility","hidden");
			appFilter["age"] = "0";
			$("#age-custom-min").val("");
			$("#age-custom-max").val("");
			//draw data
		}
	});
	$("#age-custom-button").click(function(){
		var min = isNaN($("#age-custom-min").val())?"0":($("#age-custom-min").val() === '')?"0":$("#age-custom-min").val();
		var max = isNaN($("#age-custom-max").val())?"0":($("#age-custom-max").val() === '')?"0":$("#age-custom-max").val();
		$("#age-custom-min").val(min);
		$("#age-custom-max").val(max);
		appFilter["age"] = min+"_"+max;
		//draw data
	});
	//a1c
	var grA1c = $("<div>",{"id":"toolbarFilterA1c",class:"gr-a1c"}).appendTo(gr2col1);
	$("<div>").text("HbA1c").appendTo(grA1c);
	var grA1cC = $("<div>",{class:"gr-a1c-radio gr"}).appendTo(grA1c);
	$("<div>",{"id":"a1c-filter-1",class:"gr-radio","grv-data":"1"}).appendTo(grA1cC).text("Latest value >= 0.075");
	$("<div>",{"id":"a1c-filter-2",class:"gr-radio last","grv-data":"2"}).appendTo(grA1cC).text("Custom values");
	var grA1cCustom = $("<div>",{class:"gr-a1c-custom gr-custom"}).appendTo(grA1cC);
	$("<label>",{}).appendTo(grA1cCustom).text("Between value min");
	$("<input>",{"type":"text","id":"a1c-custom-min"}).appendTo(grA1cCustom);
	$("<label>",{}).appendTo(grA1cCustom).text(" and max");
	$("<input>",{"type":"text","id":"a1c-custom-max"}).appendTo(grA1cCustom);
	//$("<button>",{"id":"a1c-custom-button",class:"cisbutton"}).text("Set").appendTo(grA1cCustom);
	$(".gr-a1c-radio .gr-radio").click(function(){
		$(".gr-a1c-radio div").removeClass("selected");
		$(this).addClass("selected");
		if($(this).attr("grv-data") == "2"){
			$(".gr-a1c-custom").css("visibility","visible");
			
		}else{
			$(".gr-a1c-custom").css("visibility","hidden");
			appFilter["hba1c"] = "0";
			$("#a1c-custom-min").val("");
			$("#a1c-custom-max").val("");
			//draw data
		}
	});
	$("#a1c-custom-button").click(function(){
		var min = isNaN($("#a1c-custom-min").val())?"0":($("#a1c-custom-min").val() === '')?"0":$("#a1c-custom-min").val();
		var max = isNaN($("#a1c-custom-max").val())?"0":($("#a1c-custom-max").val() === '')?"0":$("#a1c-custom-max").val();
		$("#a1c-custom-min").val(min);
		$("#a1c-custom-max").val(max);
		appFilter["hba1c"] = min+"_"+max;
		//draw data
	});
}


function drawToolbarButtons(container){
	
	//buttons
	$("<div>",{class:"gr-gap"}).appendTo(container);
	var genBtn = $("<button>",{class:"cisbutton"}).text("Generate List").appendTo(container);
	//genBtn.prop("disabled","true");
	genBtn.click(function(){
		console.log("global");
		console.log(globalReport);
		console.log("filter");
		console.log(appFilter);
		
		if($("#age-filter-2").hasClass("selected")){
			var min = isNaN($("#age-custom-min").val())?"0":($("#age-custom-min").val() === '')?"0":$("#age-custom-min").val();
			var max = isNaN($("#age-custom-max").val())?"0":($("#age-custom-max").val() === '')?"0":$("#age-custom-max").val();
			$("#age-custom-min").val(min);
			$("#age-custom-max").val(max);
			appFilter["age"] = min+"_"+max;
		}
		//a1c
		if($("#a1c-filter-2").hasClass("selected")){
			var min = isNaN($("#a1c-custom-min").val())?"0":($("#a1c-custom-min").val() === '')?"0":$("#a1c-custom-min").val();
			var max = isNaN($("#a1c-custom-max").val())?"0":($("#a1c-custom-max").val() === '')?"0":$("#a1c-custom-max").val();
			$("#a1c-custom-min").val(min);
			$("#a1c-custom-max").val(max);
			appFilter["hba1c"] = min+"_"+max;
		}
		
		
		
		setTimeout(drawList,100, appFilter.list, listConfig, globalReport) ;
		
		
	});
	$("<div>",{class:"gr-gap other-buttons"}).appendTo(container);
	var expBtn = $("<button>",{class:"cisbutton"}).text("Export list to CSV").appendTo(container);
	var prtBtn = $("<button>",{class:"cisbutton"}).text("Print list to PDF").appendTo(container);
	var prtGBtn = $("<button>",{class:"cisbutton"}).text("Print graphs").appendTo(container);
	$("<div>",{class:"gr-gap"}).appendTo(container);
}

function drawToolbarLists(container){
	container.empty();
	container.append($("<div>",{class:"list-gap"}))
	.append($("<div>",{class:"list-tabs"}))
	.append($("<div>",{class:"list-gap"}))
	.append($("<div>",{class:"list-button"}))
	.append($("<div>",{class:"list-gap"}));
$.each(toolbarConfig.lists, function(i, list){
	
	$("<div>",{"id":"list-"+list.id,class:"list-tab"}).appendTo($(".list-tabs")).text(list.title).click(function(){
		$(".list-container").css("visibility","hiden");
		console.log(appFilter);
		$(".list-tab").removeClass("selected");
		$(this).addClass("selected");
		appFilter["list"] = $(this).attr("id").replace("list-",""); 
		console.log(appFilter);
		showProgress($("#locallist-list"));
		//$(".list-container").trigger("myCustomEvent",[ list.id, listConfig, globalReport ] );
		setTimeout(drawList,1000,appFilter.list, listConfig, globalReport);
	});
});
}



function drawToolbar(toolbarConfig){
	var c = $("#"+toolbarConfig.container);
	$("<div>",{class:"fs-gap"}).appendTo(c);
	var fsFilters = $("<div>",{class:"fs-filters"}).appendTo(c);
	var fsButtons = $("<div>",{class:"fs-buttons"}).appendTo(c);
	var fsLists = $("<div>",{class:"fs-lists"}).appendTo(c);
	drawToolbarFilters(fsFilters);
	drawToolbarButtons(fsButtons);
	drawToolbarLists(fsLists);
	setToolbar(appFilter);
}


function getReportHeaderConfig(column, headerArray){
	var result = {};
	$.each(headerArray,function(i,ob){
		if(ob.name == column) result = ob;
	});
	return result;
}


function applyFilter(report,filter){
	var result = {};
	var ds = report.data.datasets;
	var dsresult = [];
	result["data"] = {};
	result["data"]["header"] = report.data.header; 
	console.log("report before filter");
	console.log(report);
	
	//reset values
	nbImproved = 0; 
	prImproved = 0;
	nbDegrade = 0;
	prDegrade = 0; 
	nbNodata = 0; 
	prNodata = 0;
	male = []; 
	female = [];

	var totalTotal = ds.length;
	var totalFiltered = 0;
	
	var m0=0;m1=0;m2=0;m3=0;m4=0;m5=0;m6=0;m7=0;m8=0;
	var f0=0;f1=0;f2=0;f3=0;f4=0;f5=0;f6=0;f7=0;f8=0;
	
	$.each(ds, function(i,obj){
		//community
		var hasCommunity = false;
		if(filter.idcommunity != "0"){
			if(obj.idcommunity == filter.idcommunity) hasCommunity=true;
		}else{
			hasCommunity = true;
		}

		var dpValue = dataperiodValues[filter.dp];
		var hasDate = false;
		/*
		 * temporar add 36 month 
		 * */
		
		if(dpValue != "0"){
			dpValue = dpValue +36;
			var filterDate = moment().subtract(dpValue, 'months');
			var reportDate = moment(obj.last_hba1c_collecteddate);
			hasDate = moment(reportDate).isAfter(moment(filterDate));
			//alert(moment(reportDate).format("YYYY-MM-DD")+"     "+moment(filterDate).format("YYYY-MM-DD") + "    "+ hasDate);
		}else{
			//no data period filter = all time
			hasDate = true;
		}

		var hasDtype = false;
		if(filter.dtype.indexOf(obj.dtype) >= 0 )hasDtype = true; 
		
		var hasAge = false;
		if(filter.age.indexOf("_") >= 0){
			var ages = filter.age.split("_");
			if(obj.age >= ages[0] && obj.age <= ages[1]){
				hasAge = true;
			}
		}else{
			hasAge=true;
		}
		
		var hasHBA1c = false;
		if(typeof(obj.last_hba1c) != "undefined"){
			if(filter.hba1c.indexOf("_") >= 0){
				var a1cs = filter.hba1c.split("_");
				if(obj.last_hba1c >= a1cs[0] && obj.last_hba1c <= a1cs[1]){
					hasHBA1c = true;
				}
			}else if(obj.last_hba1c >= 0.075){
				hasHBA1c=true;
			}
		}else{
			hasHBA1c=true;
		}
		
		
		//if(hasCommunity && hasDate && hasDtype && hasAge && hasHBA1c){
		if(hasCommunity && hasDate && hasDtype && hasAge && hasHBA1c){
			var isGood = false;
			if(filter.list == "list101"){
				if(obj.delta != ""){dsresult.push(obj);isGood = true;}
			}else if(filter.list == "list102"){
				if(obj.last_hba1c_colleteddate != ""){dsresult.push(obj);isGood = true;}
			}else if(filter.list == "list103"){
				if(obj.last_hba1c != ""){dsresult.push(obj);isGood = true;}
			}else if(filter.list == "list104"){
				dsresult.push(obj);
				isGood = true;
			}
			
			if(isGood){
				//validate if improved patient
				//rule both hba1c >= 7 nd delta negative
				//console.log(obj.delta +"     "+obj.secondlast_hba1c);
				if(obj.delta < 0 && obj.secondlast_hba1c > 0.07 ){
					nbImproved = nbImproved +1;
				}else if(obj.delta > 0 && obj.secondlast_hba1c > 0.07 ){
					nbDegrade = nbDegrade +1;
				}
				
				if(filter.dp == "4"){
					
				}else{
					nbNodata = nbNodata +1;
				}
				
				if(obj.sex == "1" && obj.age >= 0 && obj.age <=9) m0=m0+1;
				if(obj.sex == "1" && obj.age >= 10 && obj.age <=19) m1=m1+1;
				if(obj.sex == "1" && obj.age >= 20 && obj.age <=29) m2=m2+1;
				if(obj.sex == "1" && obj.age >= 30 && obj.age <=39) m3=m3+1;
				if(obj.sex == "1" && obj.age >= 40 && obj.age <=49) m4=m4+1;
				if(obj.sex == "1" && obj.age >= 50 && obj.age <=59) m5=m5+1;
				if(obj.sex == "1" && obj.age >= 60 && obj.age <=69) m6=m6+1;
				if(obj.sex == "1" && obj.age >= 70 && obj.age <=79) m7=m7+1;
				if(obj.sex == "1" && obj.age >= 80 ) m8=m8+1;
				
				
				if(obj.sex == "2" && obj.age >= 0 && obj.age <=9) f0=f0+1;
				if(obj.sex == "2" && obj.age >= 10 && obj.age <=19) f1=f1+1;
				if(obj.sex == "2" && obj.age >= 20 && obj.age <=29) f2=f2+1;
				if(obj.sex == "2" && obj.age >= 30 && obj.age <=39) f3=f3+1;
				if(obj.sex == "2" && obj.age >= 40 && obj.age <=49) f4=f4+1;
				if(obj.sex == "2" && obj.age >= 50 && obj.age <=59) f5=f5+1;
				if(obj.sex == "2" && obj.age >= 60 && obj.age <=69) f6=f6+1;
				if(obj.sex == "2" && obj.age >= 70 && obj.age <=79) f7=f7+1;
				if(obj.sex == "2" && obj.age >= 80 ) f8=f8+1;
				
			}
		}
	});
	
	if(filter.list == "list101"){dsresult.sort(compareDeltaDesc);}
	if(filter.list == "list102"){dsresult.sort(compareLastDateDesc);}
	if(filter.list == "list103"){dsresult.sort(compareHBA1cDesc);}
	console.log(dsresult);
	
	
	totalFiltered = dsresult.length;
	console.log("total filtererd : "+totalFiltered);
	
	prImproved = Number(nbImproved/totalFiltered).toFixed(2)*100;
	prDegrade = Number(nbDegrade/totalFiltered).toFixed(2)*100;
	
	if(filter.dp == "4"){
		totalTotal = globalNoHBA1cReport.data.datasets.length;
		$.each(globalNoHBA1cReport.data.datasets, function(i,s){
			var hComm = false;hDt = false;hAge = false;
			if(filter.idcommunity == "0"){
				hComm = true;
			}else{
				if(s.idcommunity == filter.idcommunity){hComm = true;}
			}
			
			if(filter.dtype.indexOf(s.dtype) >= 0){hDt = true;}
			if(filter.age.indexOf("_") >= 0){
				var ages = filter.age.split("_");
				if(s.age >= ages[0] && s.age <= ages[1]){
					hAge = true;
				}
			}else{
				hAge=true;
			}
			//console.log(hComm +"   "+hDt+"    "+hAge+"    "+filter.dtype+"    "+filter.age+"    "+filter.idcommunity+ "   "+s.dtype+"   "+s.idcommunity+"    "+s.age);
			if(hComm && hDt && hAge){
				nbNodata = nbNodata +1;
			}
		});
	}
	
	if(filter.dp == "4" && filter.list != "list104"){
		var t = getTotal(globalReport, appFilter);
		prNodata = Math.round(nbNodata/(t+nbNodata));
	}else{
		prNodata = Math.round(nbNodata/(totalFiltered+nbNodata));
	}
	male.push(m0,m1,m2,m3,m4,m5,m6,m7,m8);
	female.push(f0,f1,f2,f3,f4,f5,f6,f7,f8);
	result["data"]["datasets"] = dsresult;
	console.log("report after filter");
	console.log(result);
	
	console.log("nbI = "+nbImproved+"   prIm="+prImproved+"   nbD="+nbDegrade+"  prD="+prDegrade+"   nbNo="+nbNodata+"  prNo="+prNodata);
	console.log(male);
	console.log(female);
	
	return result;
	/**/
} 


function getTotal(report, filter){
	//idcommunity=x  age=y or y_z  dtype=1_234 
	var result = 0;
	$.each(report.data.datasets,function(i,s){
		var hComm = hDt = hAge = false;
		if(s.idcommunity == filter.idcommunity){hComm = true;}
		if(filter.dtype.indexOf(s.dtype) >= 0){hDt = true;}
		if(filter.age.indexOf("_") >= 0){
			var ages = filter.age.split("_");
			if(s.age >= ages[0] && s.age <= ages[1]){
				hAge = true;
			}
		}else{
			hAge=true;
		}
		if(hComm && hDt && hAge){
			result = result +1;
		}
	});
	return result;
}


function getColor(index, total){
	var result = "#ffffff";
	var r = 252;
	var g = 0;
	var b = 0;
	var bond = Math.round(252/total); 
	if(total >= 252){
		bond = Math.round(total/252);
		g = Math.round(index/bond);
		b = Math.round(index/bond);
	}else{
		bond = Math.round(252/total);
		g = Math.round(index*bond);
		b = Math.round(index*bond);
	}
	result = "rgb("+r+","+g+","+b+")";
	return result;
}



function compareDeltaAsc(a,b) {
  if (Number(a.delta) < Number(b.delta))return -1;
  if (Number(a.delta) > Number(b.delta))return 1;
  return 0;
}

function compareDeltaDesc(a,b) {
	if (Number(a.delta) < Number(b.delta))return 1;
	if (Number(a.delta) > Number(b.delta))return -1;
	 return 0;
}

function compareLastDateDesc(a,b) {
	if (moment(a.last_hba1c_collecteddate).isAfter(moment(b.last_hba1c_collecteddate)))return 1;
	if (moment(a.last_hba1c_collecteddate).isBefore(moment(b.last_hba1c_collecteddate)))return -1;
	 return 0;
}

function compareLastDateAsc(a,b) {
	if (moment(a.last_hba1c_collecteddate).isBefore(moment(b.last_hba1c_collecteddate)))return 1;
	if (moment(a.last_hba1c_collecteddate).isAfter(moment(b.last_hba1c_collecteddate)))return -1;	 
	return 0;
}


function compareHBA1cDesc(a,b) {
	if (a.last_hba1c < b.last_hba1c)return 1;
	if (a.last_hba1c > b.last_hba1c)return -1;
	 return 0;
}

function compareHBA1cAsc(a,b) {
	if (a.last_hba1c > b.last_hba1c)return 1;
	if (a.last_hba1c < b.last_hba1c)return -1;
	 return 0;
}



function drawStats(statsConfig, report){
	
	//4 graphs
	//patients  number - percentage improvved a1c from list
	var container = $("#"+statsConfig.container);

	var c = $("<div>",{class:"stats-layout"}).appendTo(container);
	c.append($("<div>",{class:"stats-gap"})).append($("<div>",{class:"stats-gap"})).append($("<div>",{class:"stats-gap"}))
	.append($("<div>",{class:"stats-gap"})).append($("<div>",{class:"stats-container"})).append($("<div>",{class:"stats-gap"}))
	.append($("<div>",{class:"stats-gap"})).append($("<div>",{class:"stats-gap"})).append($("<div>",{class:"stats-gap"}));
	
	
	$("<div>",{class:"s-container t1","id":"s1"}).appendTo($(".stats-container"));
	$("<div>",{class:"s-container t1","id":"s2"}).appendTo($(".stats-container"));
	$("<div>",{class:"s-container t1","id":"s3"}).appendTo($(".stats-container"));
	$("<div>",{class:"s-container t2","id":"s4"}).appendTo($(".stats-container"));
	
	
	//refreshStats();
	
}

function refreshStats(){
	drawStatsImproved($("#s1"));
	drawStatsDegrade($("#s2"));
	drawStatsNodata($("#s3"));
	drawStatsDeciles($("#s4"));
}


function drawStatsImproved(container){
	$(container).empty();
	$("<div>",{class:"title"}).appendTo(container).text("Patients with improved HBA1c latest values");
	$("<div>",{class:"sn panel"}).appendTo(container).text(nbImproved);
	$("<div>",{class:"sp panel"}).appendTo(container).text(prImproved+"%");
}

function drawStatsDegrade(container){
	$(container).empty();
	$("<div>",{class:"title"}).appendTo(container).text("Patients with increased HBA1c latest values");
	$("<div>",{class:"sn panel"}).appendTo(container).text(nbDegrade);
	$("<div>",{class:"sp panel"}).appendTo(container).text(prDegrade+"%");
}

function drawStatsNodata(container){
	$(container).empty();
	$("<div>",{class:"title"}).appendTo(container).text("Patients with NO HBA1c value");
	$("<div>",{class:"sn panel"}).appendTo(container).text(nbNodata);
	$("<div>",{class:"sp panel"}).appendTo(container).text(prNodata+"%");
}



function drawStatsDeciles(container){
	$(container).empty();
}



function drawList(listid,listConfig,report){
	
	console.log("start draw list : "+ moment().format('MMMM Do YYYY, h:mm:ss.SSS a'));
	if(listid == "list104" && appFilter.dp == "4" ){
		report = globalNoHBA1cReport;
	}
	
	
	
	if($(".list-container").length){
		$(".list-container").remove();
	}
	//$("#"+listConfig.container).empty();
	var listContainer = $("<div>",{class:"list-container lista"+listid}).appendTo($("#"+listConfig.container));
	
	if($(".list-container .list-header-container").length){
		$(".list-container .list-header-container").remove();
	}
	if($(".list-container .list-body-container").length){
		$(".list-container .list-body-container").remove();
	}
	var headerContainer = $("<div>",{class:"list-header-container"}).appendTo(listContainer);
	var bodyContainer = $("<div>",{"id":"list-body",class:"list-body-container"}).appendTo(listContainer);
	var header = eval("listConfig."+listid+".header");
	
	console.log("before filter "+moment().format('MMMM Do YYYY, h:mm:ss.SSS a'));
	
	var r = applyFilter(report,appFilter);
	console.log("after filter "+moment().format('MMMM Do YYYY, h:mm:ss.SSS a'));
	
	console.log("before header "+moment().format('MMMM Do YYYY, h:mm:ss.SSS a'));
	var reportHeader = r.data.header;
	var hconfObj = {};
	$.each(header,function(index,column){
		var hconf = getReportHeaderConfig(column,reportHeader);
		hconfObj[column] = hconf;
		if(column == "trend"){
			$("<div>").text("Trend").appendTo(headerContainer);
		}else{
			var cls = "";
			if(column == "fullname") cls = "fullname";
			$("<div>",{class:cls}).text(hconf.display).appendTo(headerContainer);
		}
	});
	console.log("after header "+moment().format('MMMM Do YYYY, h:mm:ss.SSS a'));
	
	console.log("before body "+moment().format('MMMM Do YYYY, h:mm:ss.SSS a'));
	var reportBody = r.data.datasets;
	bodyContainer.css("height",bodyContainer.height()+"px");
	
	var bc = document.getElementById("list-body");
	var c = document.createDocumentFragment();
	$.each(reportBody,function(i,row){
		
		var e = document.createElement("div");
	    e.className = "list-body-container-line";
	    e.id = row.ramq+"_"+row.idpatient;
	    bc.appendChild(e);
		//var linie = $("<div>",{class:"list-body-container-line","id":row.idpatient}).appendTo(bodyContainer);
		
		
		$.each(header,function(index,column){
			if(column == "trend"){
				var e1 = document.createElement("div");
			    e1.style="background-color:"+getColor(i,reportBody.length);
			    e1.className="trend";
			    e1.id = "trend_"+row.idpatient;
			    e.appendChild(e1);
			    
				//$("<div>").appendTo(linie).css("background-color",getColor(i,reportBody.length));
			}else{
				var cls = "";
			    var e2 = document.createElement("div");
			    if(column == "fullname"){ 
			    	cls = "fullname";
			    	e2.id = cls+"_"+row.idpatient;
			    }
				e2.className = cls;
				e2.innerText = renderValue(eval("row."+column),hconfObj[column]);
				e.appendChild(e2);
			}
			/**/
		});
		
	});
	console.log("after body "+moment().format('MMMM Do YYYY, h:mm:ss.SSS a'));
	
	$(".list-body-container-line").on("click",function(){
		var lid = $(this).attr("id");
		var parts = lid.split("_");
		if(!$(this).hasClass("selected")){
			$(".list-body-container-line").removeClass("selected");
			var hasTrend = false;
			if($(".trend").length){
				hasTrend = true;
				$(".trend").empty();
			}else{
				$(".fullname-button").remove();
			}
			
			closeAllPatientView();
			$(this).addClass("selected");
			loadPatientObject("ramq",parts[0]);
			var bc = $("#trend_"+parts[1]);
			var nc = "";
			if(!hasTrend) {
				bc =  $("#fullname_"+parts[1]);
				nc = "fullname-button";
			}
			$("<div>",{class:"cisbutton "+nc}).text("view").appendTo(bc).click(function(){
				if($(this).hasClass("view-on")){
					closeAllPatientView();
					$(this).removeClass("view-on");
					$(this).text("view");
					//$(".list-body-container-line").removeClass("selected");
				}else{
					$(this).addClass("view-on");
					$(this).text("close");
					createPatientView($("#"+lid));
				}
			});
		}else{
			
		}
		
		
		
		/*
		$(".other-buttons").empty();
		$("<div>",{class:"cisbutton"}).text("View Record").appendTo($(".other-buttons")).click(function(){
			var r = $(".list-body-container-line.selected").attr("id");
			gtc(sid,"en",r,"patient");	
		});
		*/
	});
	
	listContainer.css("visibility","visible");
	refreshStats();
	hideProgress($("#locallist-list"));
}

function renderValue(value,valueConfig){
	if(valueConfig.type == "text"){ 
		return value;
	}else if(valueConfig.type == "number" ){
		if(valueConfig.format.indexOf("N.") >=0 ){ 
			return Number(value).toFixed(valueConfig.format.replace("N.","").length);
		}else{
			return value;
		}
	}else if(valueConfig.type == "date" ){
		return moment(value).format(valueConfig.format);
	}else if(valueConfig.type == "array"){
		var arr = eval("report_"+valueConfig.name);
		return arr[value];
	}else{
		return value;
	}

}


function createPatientView(line){
	var lid = $(line).attr("id");
	var parts = lid.split("_");
	//loadPatientObject("ramq",parts[0]);
	var viewContainer = $("<div>",{class:"view-patient-container","id":"view-patient_"+lid});
	viewContainer.append($("<div>",{class:"view-patient-progress"}));
	var c = $("<div>",{class:"grafs-container"}).appendTo(viewContainer);
	var gap = $("<div>",{class:"graf-gap"});
	c.append($("<div>",{class:"graf-gap"})).append($("<div>",{class:"graf-gap"})).append($("<div>",{class:"graf-gap"}));
	var gc = $("<div>",{class:"view-patient-graphs-container"})
		.append($("<div>",{class:"view-patient-graph-container","id":"lab_hba1c"}).append($("<div>",{class:"title"}).text("HBA1c (Percentage)")))
		.append($("<div>",{class:"view-patient-graph-container","id":"lipid_ldl"}).append($("<div>",{class:"title"}).text("LDL (mmol/L)")))   
		.append($("<div>",{class:"view-patient-graph-container","id":"renal_acratio"}).append($("<div>",{class:"title"}).text("AcRatio (mg/mmol)")))
		.append($("<div>",{class:"view-patient-graph-container","id":"renal_egfr"}).append($("<div>",{class:"title"}).text("eGFR (ml/min)")));
	
	var bc = $("<div>",{class:"view-patient-buttons-container"}).append($("<div>",{class:"cisbutton view-patient-detail","id":"detail_"+lid}).text("View Patient Detail")).append($("<div>",{class:"cisbutton view-patient-close","id":"close_"+lid}).text("Close"));
	c.append($("<div>",{class:"graf-gap"})).append(gc).append($("<div>",{class:"graf-gap"}));
	c.append($("<div>",{class:"graf-gap"})).append(bc).append($("<div>",{class:"graf-gap"}));
	
	viewContainer.insertAfter(line);
	
	viewContainer.slideDown('slow', function() {
		$(".grafs-container").css("visibility","visible");
		setTimeout(dg,100);
	});
	
	
	
}

function dg(){
	var p = "lab_hba1c".split("_");
	drawGraphValue(p[0],p[1]);
	var p1 = "lipid_ldl".split("_");
	drawGraphValue(p1[0],p1[1]);
	var p2 = "renal_acratio".split("_");
	drawGraphValue(p2[0],p2[1]);
	var p3 = "renal_egfr".split("_");
	drawGraphValue(p3[0],p3[1]);
	
	$(".view-patient-close").click(function(){
		var lid = $(this).attr("id").replace("close_","");
		var parts = lid.split("_");
		closeAllPatientView();
		if($(".trend").length){
			$("#trend_"+parts[1]).empty();
		}else{
			$(".fullname-button").remove();
		}
		$("#"+lid).removeClass("selected");
	});
	
	$(".view-patient-detail").click(function(){
		var lid = $(this).attr("id").replace("detail_","");
		var parts = lid.split("_");
		gtc(sid,"en",parts[0],"patient");	
	});
	
	$(".view-patient-progress").remove();
}


function closeAllPatientView(){
	$(".view-patient-container").slideUp("fast", function() {
		 $(".grafs-container").css("visibility","hidden");
		 $(".view-patient-container").remove();
	});
}


/*
 * function to show the progress in words
 * use parameter to place progress
 * */
var progressOn=false;
function showProgress(container){
	if(!progressOn){
		var p = $('<div>',{id:"progress",class:"fullscreen-progress"}).appendTo(container);
		var c = $('<div>',{class:"fullscreen-progress-container"}).appendTo(p);
		var l = $('<div>',{class:"fullscreen-progress-container-logo"}).appendTo(c);
		var t = $('<div>',{class:"fullscreen-progress-container-text"}).appendTo(c);
		progressOn=true;
	}
	
	//blink();
}

function blink(){
	var progressText = ['Generating list','Preparing repport', 'Rendering interface', 'Please wait', 'Geting ready', 'Measuring screen', 'Preparing data', 'Almost ready', 'Making a small pasue', 'Start working again'];
	var r = randomIntFromInterval(0,9);
	console.log("blink1 "+progressText[r]);
	$('.fullscreen-progress-container-text').text(progressText[r]);
	 for(var i=0;i<10;i++){
		 $('.fullscreen-progress-container-text').fadeIn(300);
		 $('.fullscreen-progress-container-text').fadeOut(300);
	 }
	
	/*
	
	$('.fullscreen-progress-container-text').fadeIn(300, function(){
		console.log("blink2 "+progressText[r]);
		$('.fullscreen-progress-container-text').delay(300);
		$('.fullscreen-progress-container-text').fadeOut(300, blink);
	});
	*/
}

function hideProgress(container){
	$(container).find($("#progress")).fadeOut(500, function(){
		$(container).find($("#progress")).remove();
		progressOn=false;
	}).delay(500, function(){
		$(container).find($("#progress")).remove();
		progressOn=false;
	});
}
