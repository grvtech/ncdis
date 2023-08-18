/*
 * GLOGAL variables
 * */
var optionSelected = false;
var userDashboardObj = {};

/*
 * EVENT definitions
 * 
 * */

$("#radios .btn").on("focusin",function() {$("#search").val("");$("#search").focus();});
$("#linkedPatients").on("click",openLinkPatientsList);
$('#surveillance-button').on("click",function(){gtr(sid,applanguage,"surveillance");});
$('#pvalidation-button').on("click",function(){
	if(isDemo){
		var bconfig = {"width":"300","height":"250"};
		var bbut = [{"text":"Close","action":"closeGRVPopup"}];
		var txt = "<p><center><span style='color:yellow;font-size:35px;'><i class='fa fa-exclamation-triangle'></i></span><br><b>This function si not available in DEMO mode.</b></center></p>";
		showGRVPopup("CDIS Demo Mode",txt,bbut,bconfig);
	}else{gtr(sid,applanguage,"pvalidation");}});
//$(document).ready(function(){$('[data-toggle="tooltip"]').tooltip();});
$('#locallist-button').click(function(){
	setTimeout(setEvent,100,"LLIST");
	openList();
});


/*
 * MAIN Section 
 * */

$(".cdisfooter-left").hover(function(){$(".leftfootermenu").toggle("fade");},function(){$(".leftfootermenu").toggle("fade");});
$('#search').focus();
refreshUserNotes(sid);
drawUserDashboard();
if(typeof(userObj[0].idprofesion) != "undefined"){
	var hcpcat = profession_index[userObj[0].idprofesion];
	var iduser = userObj[0].iduser;
	$("#userfullname").text( capitalizeFirstLetter(userObj[0].firstname)+" "+capitalizeFirstLetter(userObj[0].lastname));
	getUserPatients(iduser,hcpcat);	
}else{
	$(".personal-patients").hide();
}

if(userNotes.length > 0){
	$(".notes-alert").show();
	$.each(userNotes, function(i,not){
		var uzer = getUser(not.iduser);
		var patient = getPatientInfo(not.idpatient);
		$("<div>",{class:"message"})
			.append($("<span>").html("New message from <b>"+uzer.firstname+" "+uzer.lastname+ "</b> for the patient <b>"+patient.ramq+"</b>"))
			.append($("<div>",{class:"message-button"}).text("See Message").click(function(){
				//window.location = "cdis.html?section=notes&ramq="+patient.ramq+"&sid="+sid+"&language=en";
				gtc(sid,"en",patient.ramq,"notes");
			}))
		.appendTo($(".notes-alert"));
	});
	
}else{
	$(".cdisbody_patient_alerts").hide();
	$(".notes_icon_").css("background","none");
}

if(isDemo){$("#search").attr("type","password");}


$("#search").autocomplete({
	delay: 300,
	minLength: 2,
	autoFocus: true,
	source: function( request, response ) {
		$.ajax({
			url: "/ncdis/service/data/searchPatient",
			dataType: "json",
			data: {
				criteria: $("#radios :radio:checked").attr('id'),
				term: request.term,
				language: "en",
				sid: sid
			},
			success: function( data ) {
				if(isDemo){data = demoData(data,"search");}
				response( $.map( data.objs, function( item ) {
					return {
						idpatient : item.idpatient,
						lastname : item.lastname,
						firstname : item.firstname,
						chart : item.chart,
						ramq : item.ramq,
						realramq : (isDemo)?item.realramq:item.ramq,
						community: item.community,
						giu: item.giu,
						criteria : $("#radios :radio:checked").attr('id'),
						term : request.term
					};
				}));
			}
		});
	},
	select: function( event, ui ) {
		optionSelected = true;
		patientSearchObj = ui.item;
		if(isDemo)patientSearchObj.ramq = patientSearchObj.realramq;
		$.cookie('ramq',patientSearchObj.ramq);
		gtc(sid,"en",patientSearchObj.ramq,"patient");
		return false;
	},
	open: function() {
		optionSelected = false;
	},
	close: function() {
		if(!optionSelected){
    		$("#ub_cdisbody").fadeTo( "fast", 1 );
    	}
	}
}).data("ui-autocomplete")._renderItem = function(ul, item) {
		var $line = $("<a>");
		var $liline = $("<li>");
		var $container = $("<div>",{class:"search-line"}).appendTo($liline);
		//$line.height("95px");
		if(item.criteria == "fnamelname"){
			var fn = (item.firstname+" "+item.lastname).toString().toLowerCase();
			fn = replaceAll(fn,item.term.toLowerCase(), "<strong>"+item.term.toLowerCase()+"</strong>");
			$("<div>",{class:'searchname criteria'}).appendTo($container).append($("<span>").html(fn.toUpperCase()));
		}else{
			$("<div>",{class:'searchname'}).appendTo($container).append($("<span>").html((item.firstname+" "+item.lastname).toUpperCase()));
		}
		$("<div>",{class:'searchcommunity'}).text(item.community).appendTo($container);
		if(item.criteria == "chart"){
			var cn = item.chart.toString();
			cn = replaceAll(cn,item.term, "<strong>"+item.term+"</strong>");
			//$("<div>",{class:'searchchart'}).html("<label>Chart Number :</label> <span>"+cn+"</span>").appendTo($container);
			$("<div>",{class:'searchchart criteria'}).html("<span> "+cn+" </span>").appendTo($container);
		}else{
			//$("<div>",{class:'searchchart'}).html("<label>Chart Number :</label> <span>"+item.chart+"</span>").appendTo($container);
			$("<div>",{class:'searchchart'}).html("<span>"+item.chart+"</span>").appendTo($container);
		}
		if(item.criteria == "ramq"){
			var ran = (item.ramq).toString().toLowerCase();
			ran = replaceAll(ran, item.term.toLowerCase(), "<strong>"+item.term.toLowerCase()+"</strong>");
			$("<div>",{class:'searchramq criteria'}).html("<span>"+ran.toUpperCase()+"</span>").appendTo($container);
		}else{
			$("<div>",{class:'searchramq'}).html("<span>"+item.ramq+"</span>").appendTo($container);
		}
		
		if(item.criteria == "ipm"){
			var gan = (item.giu).toString().toLowerCase();
			gan = replaceAll(gan, item.term.toLowerCase(), "<strong>"+item.term.toLowerCase()+"</strong>");
			$("<div>",{class:'searchgiu criteria'}).html("<span>"+gan.toUpperCase()+"</span>").appendTo($container);
		}else{
			$("<div>",{class:'searchgiu'}).html("<span>"+item.giu+"</span>").appendTo($container);
		}
		
		//$liline.height("35px");
		$liline.appendTo(ul);
		$(ul).css("overflow-x","hidden");
		return $liline;
};
	



/*
 * FUNCTIONS
 * */

	
function openLinkPatientsList(){
	$(".personal-patients table").toggle();
	if($(".personal-patients table").is(":visible")){
		$("#linkedPatients").text("Close Patient List");
	}else{
		$("#linkedPatients").text("Open Patient List");
	}
}	

function getUserDashboard(){
	var iduser = userObj[0].iduser;
	var data = "iduser="+iduser+"&language=en";
	$.ajax({
		  url: "/ncdis/service/data/getUserDashboard",
		  type: "POST",
		  async : false,
		  cache : false,
		  data : data,
		  dataType: "json"
		}).done(function( json ) {
			userDashboardObj = json.objs[0];
			if(isDemo)userDashboardObj = demoData(userDashboardObj,"userdashboard");
		}).fail(function( jqXHR, textStatus ) {
			alert( "Request failed: " + textStatus );
	});	
}	

function drawUserDashboard(){
	var container = $(".dashboard-user");
	getUserDashboard();
	
	var actions = $("<div>",{class:"panel5-dashboard"}).appendTo(container);
	$.each(userDashboardObj.actions,function(key,value){
		$("<div>",{class:"singlevalue-dashboard","data-toggle":"tooltip",title:"Number of "+value[1]+" in the last 30 days"})
			.append($("<div>",{class:"singlevalue-dashboard-header"}).text(value[0]))
			.append($("<div>",{class:"singlevalue-dashboard-value"}).text(value[1]))
			.appendTo(actions);
	});
	
	var uh = userDashboardObj.history;
	uh.sort(compareDateAsc);
	var p2 = $("<div>",{class:"panel2-dashboard"}).appendTo(container);
	var history = $("<div>",{class:"tablevalue-dashboard"}).appendTo(p2);
	$("<div>",{class:"tablevalue-header-dashboard"}).text("Last view patients (click to view details)").appendTo(history);
	$.each(uh,function(key,value){
		$("<div>",{class:"tablevalue-line-dashboard",data:value[1]})
			.append($("<div>",{class:"tablevalue-dashboard-label"}).text(value[1]))
			.append($("<div>",{class:"tablevalue-dashboard-value"}).text(value[0]))
			.appendTo(history)
			.click(function(){
				if(isDemo){
					gtc(sid,"en",value[2],"patient");
				}else{
					gtc(sid,"en",value[1],"patient");
				}
				
			});
	});
	$("<div>",{class:"activitygraph-dahsboard",id:"activity-graph"}).appendTo(p2);
	var series=[];
	var ticks = [];
	var s = [];
	var uact = userDashboardObj.activity;
	uact.sort(compareDateAsc);
	$.each(uact,function(key,value){
		s.push(value[1]);
		ticks.push(value[0]);
	});
	series.push(s);
	
	drawLineGraphDashboard($("#activity-graph"), series, ticks);
	
}

function compareDateAsc(a,b) {if (moment(a[0]).isAfter(moment(b[0])))return 1;if (moment(a[0]).isBefore(moment(b[0])))return -1;return 0;}

function drawLineGraphDashboard(container, series, ticks){
	$(container).empty();
	var cid = $(container).attr("id");
    var max = series[0].max() + 50;
    var showTick = true;
	var plot4 = $.jqplot(cid, series, {
			title:"Number of activities in CDIS in last 30 days",
			highlighter: {show: true,showTooltip: false},
		    grid: {drawBorder: false,borderWidth: 0.5,shadow: false},
		    series:[{renderer:$.jqplot.BarRenderer,highlightMouseOver: false,label: "aaa",pointLabels: { show: true,location : 'n'}}],
            axes: {xaxis: {ticks: ticks,drawMajorGridlines: false,renderer: $.jqplot.CategoryAxisRenderer,labelRenderer: $.jqplot.CanvasAxisLabelRenderer,tickRenderer: $.jqplot.CanvasAxisTickRenderer,tickOptions: {show: showTick,angle: 0}},
                  yaxis: {autoscale:true,min: 0,max: max,labelRenderer: $.jqplot.CanvasAxisLabelRenderer,tickRenderer: $.jqplot.CanvasAxisTickRenderer,tickOptions: {angle: 0,prefix: '',formatString: '%d'}}}
          });
    
}

	