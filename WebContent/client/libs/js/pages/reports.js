var reportsSection = "dashboard";
var default_color = "#4d90fe";
var dashboard_color = "#fcfcfc";
var patient_color = "#fcfcfc";
var reportsObject = null;
var reportObjectToExecute = null;
var criteriaArray = [];
var graphtype_line = {title:'Data Graph', axes:{xaxis:{renderer:$.jqplot.DateAxisRenderer} },series:[{lineWidth:4, markerOptions:{style:'square'}}]};
var graphtype_bar =  {title:'Data Graph',seriesDefaults:{renderer:$.jqplot.BarRenderer},axes:{xaxis:{renderer: $.jqplot.CategoryAxisRenderer}}};
var graphtype_pie =  {title:'Data Graph',gridPadding: {top:0, bottom:38, left:0, right:0},seriesDefaults:{renderer:$.jqplot.PieRenderer,trendline:{ show:false },rendererOptions: { padding: 8, showDataLabels: true }},legend:{show:true,placement: 'outside',rendererOptions: {numberRows: 1},location:'s',marginTop: '15px'}};
var exportImage = null;
var isSurveillance = false;
var isPvalidation = false;
var showPopupFlag = true;
var pvalidationPopupTitle = "Patient Validation";
var pvalidationText = "<p>Patient Validation is a tool that allows CDIS users to screen for patients whose information may require updating (e.g. change of diagnosis, patient deceased, permanently moved from region).</p>"
						+"<ul><li>No data in last 5 years (unless GDM)</li>"
						+"<li>Age > 95</li>"
						+"<li>Duplicate name</li>"
						+"<li>Predm and value > 0.065 X 2 : if reclassifying as diabetic first verify that patient is aware of diagnosis</li></ul>"
						+"<span>Corrections can be done by CDIS users. For deletions, an email explaining the problem must be sent to support@grvtech.ca</span>";


//var dashboard_graph_data = [['2008-08-12 4:00PM',4], ['2008-09-12 4:00PM',6.5], ['2008-10-12 4:00PM',5.7], ['2008-11-12 4:00PM',9], ['2008-12-12 4:00PM',8.2]];
//var dahsboard_graph_options = {title:'Data Graph', axes:{xaxis:{renderer:$.jqplot.DateAxisRenderer} },series:[{lineWidth:4, markerOptions:{style:'square'}}]};

if (!isUserLoged(sid)){
	logoutUser(sid);
}else{
	if(getParameterByName("reportid") == "surveillance"){
		isSurveillance = true;
	}else if(getParameterByName("reportid") == "pvalidation"){
		isPvalidation = true;
	}else{
		isSurveillance = false;
		isPvalidation = false;
	}
	loadTemplate(page,loadReportsTemplate);
}	


function loadReportsTemplate(){
	if(isUserLoged(sid)){
		
		//$("body").append($("<div>",{class:"modal"}).append($("<div>",{class:"modal-span"}).text("Loading CDIS Report page"))).addClass("loading");
		$("#ub_reportsbody_page").load("/ncdis/client/templates/reports.dashboard.html", function(){
			reportsSection = "dashboard";
			reportsObject = getReports(sid);
			var list = $("#dashboard");
			var predefinedReports = reportsObject.predefined;
			if(predefinedReports.length > 0){
				
				$.each(predefinedReports, function(k, vObj){
					var raport = prepareDefinedReport(vObj.code);
					reportObjectToExecute = loadReport(raport);
					list.append(
							$("<div>",{class:"dashboard-item-"+reportObjectToExecute.class+" uss",id:reportObjectToExecute.id})
								.append($("<div>",{class:"title"}))
								.append($("<div>",{class:"form"}))
								.append($("<div>",{class:"graph",id:"graph-"+reportObjectToExecute.id}).append($("<div>",{class:"loading-span"}).text("Loading ...")))
					);
					renderReport(reportObjectToExecute);
				});
			}
			
			//load surveillance tabs as well
			$("#surveillance").load("/ncdis/client/templates/reports.sur.html",function(){
				setTimeout(setEvent,100,"SUR");
				initSurveillance(1);
				
			});
			
			if(isDemo){
				alert("This function is not available in demo mode");
			}else{
				//load patient validation tool
				$("#pvalidation").load("/ncdis/client/templates/reports.patv.html",function(){
					//setTimeout(setEvent,100,"PATV");
					initPvalidation();
				});
			}
			
			//load patient validation tool
			$("#pandi").load("/ncdis/client/templates/reports.pandi.html",function(){
				//setTimeout(setEvent,100,"PANDI");
				reportsSection = "pandi";
				initPandi();
			});
			
			if(isDemo){
				//alert("This function si not available in demo mode");
			}else{
				//load import omnilab
				$("#importomnilab").load("/ncdis/client/templates/reports.importomnilab.html",function(){
					reportsSection = "importomnilab";
					initImportOmnilab();
				});
			}
			
			if(isSurveillance){
				reportsSection = "surveillance";
				isSurveilance = false;
				$("#tabs").tabs({"active":2});
			}
			
			if(isPvalidation){
				if(!isDemo){
					reportsSection = "pvalidation";
					isPvalidation = false;
					$("#tabs").tabs({"active":3});
				}
			}
			
			initPage();
		});
	}else{
		logoutUser(sid);
	}
}

function renderReport(reportObj){
	$("#"+reportObj.id+" > .title").append($("<div>",{style:"display:inline-block;width:90%;"}).text(reportObj.title));
	
	if($.type(reportObj.input) === "array"){
		$.each(reportObj.input,function(i,v){
			var cinputForm = $("<div>",{class:"form-item"});
			cinputForm.append($("<div>",{class:"form-label"}).text(v.display));
			var rid = reportObj.id;
			$("#"+rid+" > .form").append(cinputForm);
			if(v.name == "idcommunity" ){
				$("<div>",{class:"ms"+rid}).appendTo(cinputForm);
				var voptions = {"container":"ms"+rid,"maxSelect":"2","defaultSelected":"0","idrole":userProfileObj.role.idrole};
				var vlist = [{"name":"All Communities","value":"0"},
				            {"name":"Chisasibi","value":"1"},
				            {"name":"Eastmain","value":"2"},
				            {"name":"Mistissini","value":"3"},
				            {"name":"Nemaska","value":"4"},
				            {"name":"Oujebougoumou","value":"5"},
				            {"name":"Waskaganish","value":"6"},
				            {"name":"Waswanipi","value":"7"},
				            {"name":"Wemindji","value":"8"},
				            {"name":"Whapmagoostui","value":"9"}];
				
				var ms = new GRVMSelect(vlist,voptions);
				$(ms.object).change(function(){
					var v = ms.getValue();
					$(ms.element).attr("value",v);
					var subObj = reportObj["subcriteria"][i];
					var vv = v;
					if(subObj.subsection == "1"){
						if(vv == "0"){
							subObj["suboperator"] = "more than";
						}else{
							subObj["suboperator"] = "equal";
						}
					}
					subObj["subvalue"] = v;
					reportObj["subcriteria"][i] = subObj;
					
					if($("#form-field-cvalue-REP3").val() == "on" && ($(reportObj).attr("id")).indexOf("REP3") >=0 ){
						if(!isDecimal($("#form-field-cvalue-REP3-cvalue-value").val())){
							$("#form-item-cvalue-REP3-cvalue .form-label").addClass("txtErrorRed");
							$("#form-field-cvalue-REP3-cvalue-value").addClass("inputErrorRed");
						}else{
							renderGraphReport3CustomValue($("#form-field-dtype-REP3").val(),$(".msREP3").attr("value"), $("#form-field-cvalue-REP3-cvalue-value").val(),$("#REP3 > .graph"));
						}
					}else if($("#form-field-filter-REP4").val() == "on" && ($(reportObj).attr("id")).indexOf("REP4") >=0 ){
						renderGraphReport4CustomValue($("#form-field-dtype-REP4").val(),$(".msREP4").attr("value"), "sens", "value",$("#REP4 > .graph"));
					}else{
						renderGraphReport(reportObj, $("#"+reportObj.id+" > .graph"));
					}
					
				});
			}else{
				var ri = $("<select>",{id:"form-field-"+v.name+"-"+reportObj.id});
				ri.change(function(){
					var subObj = reportObj["subcriteria"][i];
					var vv = ri.val();
					if(subObj.subsection == "1"){
						if(vv == "0"){
							subObj["suboperator"] = "more than";
						}else{
							subObj["suboperator"] = "equal";
						}
					}
					subObj["subvalue"] = ri.val();
					reportObj["subcriteria"][i] = subObj;
					
					if($("#form-field-cvalue-REP3").val() == "on" && ($(this).attr("id")).indexOf("REP3") >=0 ){
						if(!isDecimal($("#form-field-cvalue-REP3-cvalue-value").val())){
							$("#form-item-cvalue-REP3-cvalue .form-label").addClass("txtErrorRed");
							$("#form-field-cvalue-REP3-cvalue-value").addClass("inputErrorRed");
						}else{
							renderGraphReport3CustomValue($("#form-field-dtype-REP3").val(),$(".msREP3").attr("value"), $("#form-field-cvalue-REP3-cvalue-value").val(),$("#REP3 > .graph"));
						}
					}else if($("#form-field-filter-REP4").val() == "on" && ($(this).attr("id")).indexOf("REP4") >=0 ){
						renderGraphReport4CustomValue($("#form-field-dtype-REP4").val(),$(".msREP4").attr("value"), "sens", "value",$("#REP4 > .graph"));
						
					}else{
						
						renderGraphReport(reportObj, $("#"+reportObj.id+" > .graph"));
					}
					
					
				});
				var ric = $("<div>",{class:"field-container"}).append(ri);
				cinputForm.append(ric);
				$.each(v.values, function (i, item) {
				    ri.append($('<option>', { 
				       value: i,
				        text : item 
				    }));
				});
				
				//all dtype should be default at Type 1 anf type 2 DM 
				if(v.name == "dtype"){
					$(ri).val(1);
					var subObj = reportObj["subcriteria"][i];
					var vv = ri.val();
					if(subObj.subsection == "1"){
						if(vv == "0"){
							subObj["suboperator"] = "more than";
						}else{
							subObj["suboperator"] = "equal";
						}
					}
					subObj["subvalue"] = ri.val();
					reportObj["subcriteria"][i] = subObj;
				}
			}
			
		});
		//out of input loop
		// add checkbox to include custom value
		if(reportObj.id == "REP3"){
			var cc = $("<div>",{class:"form-item"});
			cc.append($("<div>",{class:"form-label"}).text("Custom value"));
			var chekri = $("<input>",{type:"checkbox",id:"form-field-cvalue-REP3",value:"off",style:"vertical-align: middle;position: relative;bottom: 1px;"}).appendTo(cc);
			$("#"+reportObj.id+" > .form").append(cc);
			
			chekri.change(function(){
				if($("#form-field-cvalue-REP3").val() == "off"){
					
					$("#form-field-cvalue-REP3").val("on");
					
					if($("#form-item-cvalue-REP3-cvalue").length <= 0){
						var hfb = $("#"+reportObj.id+" .form").height();
						var c = $("<div>",{class:"form-item",id:"form-item-cvalue-REP3-cvalue"}).appendTo($("#"+reportObj.id+" > .form"));
						var l = $("<div>",{class:"form-label"}).text("Value");
						c.append(l);
						var input = $("<input>",{type:"text",id:"form-field-cvalue-REP3-cvalue-value",style:"width:50px;padding:2px;"}); 
						c.append(input);
						input.focus(function(){
							$("#form-item-cvalue-REP3-cvalue .form-label").removeClass("txtErrorRed");
							$("#form-field-cvalue-REP3-cvalue-value").removeClass("inputErrorRed");
						});
						var b = $("<div>",{class:"cisbutton",style:"color:#fff;margin-left:10px;padding:5px;"}).text("Graph");
						c.append(b);
						b.click(function(){
							if(!isDecimal($(input).val())){
								$("#form-item-cvalue-REP3-cvalue .form-label").addClass("txtErrorRed");
								$("#form-field-cvalue-REP3-cvalue-value").addClass("inputErrorRed");
							}else{
								renderGraphReport3CustomValue($("#form-field-dtype-REP3").val(),$(".msREP3").attr("value"), $("#form-field-cvalue-REP3-cvalue-value").val(),$("#REP3 > .graph"));
							}
						});
						
						var hfa = $("#"+reportObj.id+" .form").height();
						var hg = $("#"+reportObj.id+" .graph").height();
						if(hfa > hfb ){
							$("#"+reportObj.id+" .graph").height(hg - (hfa - hfb));
						}
						renderGraphReport(reportObj, $("#"+reportObj.id+" > .graph"));
						
					}else{
						if(!isDecimal($("#form-field-cvalue-REP3-cvalue-value").val())){
							$("#form-item-cvalue-REP3-cvalue .form-label").addClass("txtErrorRed");
							$("#form-field-cvalue-REP3-cvalue-value").addClass("inputErrorRed");
						}else{
							renderGraphReport3CustomValue($("#form-field-dtype-REP3").val(),$(".msREP3").attr("value"), $("#form-field-cvalue-REP3-cvalue-value").val(),$("#REP3 > .graph"));
						}
						
					}
					
				}else{
					var hfb = $("#"+reportObj.id+" .form").height();
					$("#form-field-cvalue-REP3").val("off");
					$("#form-item-cvalue-REP3-cvalue").remove();
					var hfa = $("#"+reportObj.id+" .form").height();
					var hg = $("#"+reportObj.id+" .graph").height();
					if(hfa < hfb ){
						$("#"+reportObj.id+" .graph").height(hg + (hfb - hfa));
					}
					renderGraphReport(reportObj, $("#"+reportObj.id+" > .graph"));
				}
			});
			
			
			
		}else if(reportObj.id == "REP4"){
			var hfb = $("#"+reportObj.id+" .form").height();
			var cc = $("<div>",{class:"form-item"});
			cc.append($("<div>",{class:"form-label"}).text("Filter"));
			var chekfilter = $("<input>",{type:"checkbox",id:"form-field-filter-REP4",value:"off",style:"vertical-align: middle;position: relative;bottom: 1px;"}).appendTo(cc);
			$("<div>",{class:"form-label"}).text("Reduction of LDL by 50% from baseline").appendTo(cc);
			$("#"+reportObj.id+" > .form").append(cc);
			
			var hfa = $("#"+reportObj.id+" .form").height();
			var hg = $("#"+reportObj.id+" .graph").height();
			if(hfa > hfb ){
				$("#"+reportObj.id+" .graph").height(hg - (hfa - hfb));
			}
			
			chekfilter.change(function(){
				if($("#form-field-filter-REP4").val() == "off"){
					$("#form-field-filter-REP4").val("on");
					renderGraphReport4CustomValue($("#form-field-dtype-REP4").val(),$(".msREP4").attr("value"), "sens", "value",$("#REP4 > .graph"));
				}else{
					$("#form-field-filter-REP4").val("off");
					renderGraphReport(reportObj, $("#"+reportObj.id+" > .graph"));
				}
			});
		}
		
	}else{
		$("#"+reportObj.id+" > .form").hide();
		if(reportObj.class == "half"){
			$("#"+reportObj.id+" > .graph").height($("#"+reportObj.id+" > .graph").height() + 40);
		}else if(reportObj.class == "full"){
			$("#"+reportObj.id+" > .graph").css("width","100%");
		}
	}
	
	renderGraphReport(reportObj, $("#"+reportObj.id+" > .graph"));
}

function renderGraphReport(reportObj, divObj){
	if($.type(reportObj.data) === "object"){
		var obset = [];
		for(var i=0;i<reportObj.subcriteria.length;i++){
			if(obset.length == 0 ) {
				obset = reportObj;
			}
			
			var iname = reportObj.subcriteria[i].subname;
			var ival = reportObj.subcriteria[i].subvalue;
			if(ival.indexOf("_") >= 0){
				var parts = ival.split("_");
				var obsett = obset;
				var obs = [];
				for(var j=0;j<parts.length;j++){
					ival = parts[j];
					obset = getObjects(obsett,iname,ival);
					obs.push(obset);
				}
				obset = obs;
			}else{
				obset = getObjects(obset,iname,ival);
			}
		} 
		var ds = null;
		var r = [];
		for(var k=0;k<obset.length;k++){
			ds = obset[k];
			r.push(ds.set);
		}

		var set = obset[0];
		var obj ={};
		obj["objs"] = [{"dataset":r,"header":reportObj.data.header}];
		
		drawGraphReport(obj,reportObj,divObj);
	}else{
		$.ajax({
		    url: "/ncdis/service/action/executeReport?language=en&idreport="+reportObj.id+"&owner="+reportObj.owner+"&type="+reportObj.type+"&graphtype="+reportObj.graphtype+"&subcriteriatype="+reportObj.subcriteriatype,
		    type: 'POST',
		    data:JSON.stringify(reportObj),
		    contentType: 'application/json; charset=utf-8',
		    dataType: 'json',
		    async: true,
		    success: function(data){
		    	var rob = reportObj;
		    	var dob = divObj;
		    	drawGraphReport(data,rob,dob);
		    }
		});
	}
}


function renderGraphReport3CustomValue(dtype, idcommunity, customValue, divObj){
	$.ajax({
	    url: "/ncdis/service/action/executeReport3CustomValue?language=en&cvalue="+customValue+"&idcommunity="+idcommunity+"&dtype="+dtype,
	    type: 'GET',
	    contentType: 'application/json; charset=utf-8',
	    dataType: 'json',
	    async: true,
	    success: function(data){
	    	var rob = {"graphtype":"pie","subcriteria":[],"id":"REP3"};
	    	var dob = divObj;
	    	drawGraphReport(data,rob,dob);
	    }
	});
}

function renderGraphReport4CustomValue(dtype, idcommunity, sens, pvalue, divObj){
	$.ajax({
	    url: "/ncdis/service/action/executeReport4CustomValue?language=en&sens="+sens+"&pvalue="+pvalue+"&idcommunity="+idcommunity+"&dtype="+dtype,
	    type: 'GET',
	    contentType: 'application/json; charset=utf-8',
	    dataType: 'json',
	    async: true,
	    success: function(data){
	    	var rob = {"graphtype":"pie","subcriteria":[],"id":"REP4"};
	    	var dob = divObj;
	    	drawGraphReport(data,rob,dob);
	    }
	});
}


function drawGraphReport(msg,reportObj, divObj) {
    dataset = msg.objs[0];

    if($.type(dataset) === "object"){
		var ds = dataset.dataset;
		var dh = dataset.header;
		var labels = [];
		var datalabels = [];
		var multiSerie = false;
		if(ds.length > 1)multiSerie = true;
		
		if(ds.length > 0){
			if($.type(ds[0]) != "array"){ds = [ds];}
			$(divObj).empty();
			var series = [];
			var totals = [];
			var total = 0;
			
			for(var x=0;x<ds.length;x++){
				var tt = 0;
				for(var j=0;j<dh.length;j++){
					total += Number(ds[x][j]);
					tt += Number(ds[x][j]);
				}
				totals.push(tt);
			}
			
			var graphOptions=null;
			var axes = null;
			
			if(reportObj.graphtype == "bar"){
				//add to series the percentage 
				var getPercentage = false;
				var pmax = 0;
				if(reportObj.id == "REP3" || reportObj.id == "REP2" || reportObj.id == "REP6"){
					getPercentage = true;
				}
				
				if(reportObj.subcriteria.length > 0 ){
					if(reportObj.subcriteriatype == "single"){
						for(var k=0;k<reportObj.subcriteria.length;k++){
							var s = [];
							for(var kk=0;kk<dh.length;kk++){
								var serie = [dh[kk], Number(ds[k][kk]) ];
								s.push(serie);
							}
							series.push(s);
						}
					}else{
						for(var x=0;x<ds.length;x++){
							var labelp = [];
							if(getPercentage){
								//var pserie = [];
								var nserie = [];
								for(var i=0;i<dh.length;i++){
									var p = Number(100*Number(ds[x][i])/totals[x]).toFixed(2);
									pmax = Math.max(pmax,Number(ds[x][i]));
									//pserie.push([dh[i], p]);
									nserie.push([dh[i], Number(ds[x][i])]);
									labelp.push(Number(ds[x][i])+" - "+p+"%");
								}
								series.push(nserie);
								//series.push(pserie);
								var o = $(".ms"+reportObj.id+" .grvmselect-bar .grvmselect-item-container");
								var lselect = $(o[x]).find(".grvmselect-item-label").text();
								labels.push(lselect);
								//labels.push("Percentage "+lselect);
							}else{
								var nserie = [];
								for(var i=0;i<dh.length;i++){
									nserie.push([dh[i], Number(ds[x][i])]);
								}
								series.push(nseries);
							}
							datalabels.push(labelp);
						}
					}
				}else{
					for(var i=0;i<dh.length;i++){
						var serie = [dh[i], Number(ds[0][i]) ];
						series.push(serie);
					}
					series = [series];
				}
				
				graphOptions = {
						grid:{background:'transparent',drawBorder:false,shadow:false,borderWidth: 1},
						seriesColors:['#9ddb86','#00749F', '#C7754C', '#17BDB8','#85802b','#7ca1d9', '#73C774', '#00749F'],
						seriesDefaults:{renderer:$.jqplot.BarRenderer,pointLabels: { show: true },rendererOptions: {varyBarColor: true}},
				        axes:{xaxis:{renderer: $.jqplot.CategoryAxisRenderer}}
				    };
				
				if(getPercentage && series.length == 1){
					var ymax = Math.round(pmax+Number(pmax*0.2));
					graphOptions = {
							grid:{background:'transparent',drawBorder:false,shadow:false,borderWidth: 1},
							seriesColors:['#9ddb86','#00749F', '#C7754C', '#17BDB8','#85802b','#7ca1d9', '#73C774', '#00749F'],
							series:[
							        {renderer:$.jqplot.BarRenderer,
							        	pointLabels: { 
							        		show: true , location:'n',
							        		labels:datalabels[0]
							        		}
							        ,rendererOptions: {varyBarColor: true,highlightMouseDown: false,highlightMouseOver: false}}
							        
							],
					        axes:{
					            xaxis:{renderer: $.jqplot.CategoryAxisRenderer},
					            yaxis:{label: 'No patients',max:ymax,labelRenderer: $.jqplot.CanvasAxisLabelRenderer,tickOptions: { formatString: "%'d" }}
					        }
					    };
				}else if(getPercentage && series.length == 2){
					var ymax = Math.round(pmax+Number(pmax*0.2));
					graphOptions = {
							grid:{background:'transparent',drawBorder:false,shadow:false,borderWidth: 1},
							seriesColors:['#9ddb86','#00749F', '#C7754C', '#17BDB8','#85802b','#7ca1d9', '#73C774', '#00749F'],
							series:[
							        {renderer:$.jqplot.BarRenderer,pointLabels: { show: true , location:'n',labels:datalabels[0]},rendererOptions: {varyBarColor: false,highlightMouseDown: false,highlightMouseOver: false},yaxis:'yaxis',
							        	seriesColors:['#9ddb86','#00749F', '#C7754C', '#17BDB8','#85802b','#7ca1d9', '#73C774', '#00749F']
							        },
							        {renderer:$.jqplot.BarRenderer,pointLabels: { show: true , location:'n',labels:datalabels[1]},rendererOptions: {varyBarColor: false,highlightMouseDown: false,highlightMouseOver: false},yaxis:'yaxis',
							        	seriesColors:['#9ddb86','#00749F', '#C7754C', '#17BDB8','#85802b','#7ca1d9', '#73C774', '#00749F']
							        }
							        
							],
							legend: {
						    	renderer: $.jqplot.EnhancedLegendRenderer,
						    	labels : labels,
						        show: true,
						        location:'s',
						        border : 'none',
						        placement: 'outsideGrid',
						        fontSize:'0.6vw',
						        rowSpacing:'0.7em',
						        background:'transparent',
						        marginRight:'5px',
						        rendererOptions: {
						            numberRows: 1,
						            numberColumns: 4,
						        }
						       },
					        axes:{
					            xaxis:{renderer: $.jqplot.CategoryAxisRenderer},
					            yaxis:{label: 'No patients',max:ymax,labelRenderer: $.jqplot.CanvasAxisLabelRenderer,tickOptions: { formatString: "%'d" }}
						        
					        }
					    };
				}
			}else if(reportObj.graphtype == "pie"){
				if(ds.length > 1){
					
					var labels = [];
					for(var i=0;i<dh.length;i++){
						labels.push(dh[i]);
					}
					
					for(var x=0;x<ds.length;x++){
						var s = []; 
						var o = $(".ms"+reportObj.id+" .grvmselect-bar .grvmselect-item-container");
						var lselect = $(o[x]).find(".grvmselect-item-label").text();
						for(var i=0;i<dh.length;i++){
							var serie = [dh[i]+" "+lselect+" "+Number(((Number(ds[x][i])*100)/totals[x]).toFixed(2))+"% ["+ds[x][i]+"]",  Number(((Number(ds[x][i])*100)/totals[x]).toFixed(2))  ];
							labels[i] = labels[i] +" "+ lselect + ":[<b> "+ds[x][i]+" "+Number(((Number(ds[x][i])*100)/totals[x]).toFixed(2))+"% </b>] "
							s.push(serie);
						}
						series.push(s);
					}
					graphOptions = {
							grid:{background:'transparent',drawBorder:false,shadow:false,borderWidth: 1},
							seriesColors:['#9ddb86','#00749F', '#C7754C', '#17BDB8','#85802b','#7ca1d9', '#73C774', '#fa756b'],
							gridPadding: {top:0, bottom:0, left:0, right:0},
							seriesDefaults: {
							      // make this a donut chart.
							      renderer:$.jqplot.DonutRenderer,
							      rendererOptions:{
							        // Donut's can be cut into slices like pies.
							        sliceMargin: 3,
							        // Pies and donuts can start at any arbitrary angle.
							        startAngle: -90,
							        showDataLabels: true,
							        // By default, data labels show the percentage of the donut/pie.
							        // You can show the data 'value' or data 'label' instead.
							        dataLabels: 'value',
							        dataLabelFormatString : "%'d%"
							      }
							},
							legend:{show:true,placement: 'inside',location:'e',labels:labels}
					};
					
				}else{
					graphOptions = {
							grid:{background:'transparent',drawBorder:false,shadow:false,borderWidth: 1},
							seriesColors:['#9ddb86','#00749F', '#C7754C', '#17BDB8','#85802b','#7ca1d9', '#73C774', '#fa756b'],
							gridPadding: {top:0, bottom:0, left:0, right:0},
							seriesDefaults:{renderer:$.jqplot.PieRenderer,trendline:{ show:true },rendererOptions: { padding: 4, sliceMargin: 2, showDataLabels: true }},
							legend:{show:true,placement: 'inside',location:'e'}       				
					};
					for(var i=0;i<dh.length;i++){
						var serie = [dh[i]+" "+Number(((Number(ds[0][i])*100)/total).toFixed(2))+"% ["+ds[0][i]+"]",  Number(((Number(ds[0][i])*100)/total).toFixed(2))  ];
						series.push(serie);
					}
					series = [series];
				}
				
			
			}else if(reportObj.graphtype == "line"){
				if(reportObj.subcriteria.length > 0 ){
					if(reportObj.subcriteriatype == "single"){
						for(var k=0;k<reportObj.subcriteria.length;k++){
							var s = [];
							for(var kk=0;kk<dh.length;kk++){
								var serie = [dh[kk], Number(ds[k][kk]) ];
								s.push(serie);
							}
							series.push(s);
						}
					}else{
						for(var i=0;i<dh.length;i++){
							var serie = [dh[i], Number(ds[0][i]) ];
							series.push(serie);
						}
						series = [series];
					}
				}else{
					for(var i=0;i<dh.length;i++){
						var serie = [dh[i], Number(ds[0][i]) ];
						series.push(serie);
					}
					series = [series];
				}
				graphOptions = {
						 grid:{
							 background:'transparent',
							 drawBorder:false,
							 shadow:false,
							 borderWidth: 1 
						},
					      axesDefaults: {
					    	  renderer: $.jqplot.CategoryAxisRenderer,
				             labelRenderer: $.jqplot.CanvasAxisLabelRenderer
					      },
					      seriesDefaults: {
					          rendererOptions: {
					              smooth: true
					          }
					      }
					    };
			}
			
			var plot = jQuery.jqplot($(divObj).attr("id"),series, graphOptions);
			$(divObj).bind('jqplotDataHighlight', function(ev, seriesIndex, pointIndex, series) {
		        var idx = pointIndex;
		        $(divObj).find('tr.jqplot-table-legend').removeClass('legend-row-highlighted');  
		        $(divObj).find('tr.jqplot-table-legend').children('.jqplot-table-legend-label').removeClass('legend-text-highlighted');
		        $(divObj).find('tr.jqplot-table-legend').eq(idx).addClass('legend-row-highlighted');
		        $(divObj).find('tr.jqplot-table-legend').eq(idx).children('.jqplot-table-legend-label').addClass('legend-text-highlighted');
		    });
		 
			$(divObj).bind('jqplotDataUnhighlight', function(ev, seriesIndex, pointIndex, series) {
				$(divObj).find('tr.jqplot-table-legend').removeClass('legend-row-highlighted');  
				$(divObj).find('tr.jqplot-table-legend').children('.jqplot-table-legend-label').removeClass('legend-text-highlighted');
		    });
		}else{
			$("#"+reportObj.id).hide();
		}
	}
    $("#"+reportObj.id+" > .graph").find(".loading-span").remove();
}



function getReports(sid){
	var result = null;
	
	var reps = $.ajax({
		  url: "/ncdis/service/action/getReports?sid="+sid+"&language=en",
		  type: "GET",
		  async : false,
		  cache : false,
		  dataType: "json"
		});
		reps.done(function( json ) {
			result = json.objs[0];
			
		});
		reps.fail(function( jqXHR, textStatus ) {
		  alert( "Request failed: " + textStatus );
		});
	return result;
}

function prepareDefinedReport(reportID){
	var url = "/ncdis/client/reports/report."+reportID+"?ts="+moment().format('X');
	var result = null;
	var req1 = $.ajax({
		  dataType: "json",
		  url: url,
		  async:false,
		  cache : false
		});	
	req1.success(function(obj){
		result = obj;
	});
	req1.fail(function( jqXHR, textStatus, errorThrown){console.log("AJAX failed!", jqXHR, textStatus, errorThrown)});
	return result;
}

function buildReport(repObject, dataset){
	
		var raper = $("#wraper");
		$("html, body").animate({ scrollTop: 0 }, "fast");
		$("html, body").css("overflow", "hidden");
		
		var report = $("<div>",{class:"raportContainer"}).appendTo(raper);
		var reportH = $("<div>",{class:"raportHeader umbra"}).append($("<div>",{class:"raportHeaderLogo"}).text("CDIS")).append($("<div>",{class:"raportHeaderButtons"})).appendTo(report);
		var reportB = $("<div>",{class:"raportBody"}).appendTo(report);
		reportB.css("height",(report.height()-110)+"px");
		
		buildReportToolbar($(".raportHeaderButtons"), repObject);
		
		if(repObject.type == "graph"){
			var graphContainer = $("<div>",{class:"graphContainer"}).appendTo(reportB);
			$("<div>",{id:"graphReport"}).appendTo(graphContainer);
			drawReportGraph(repObject,dataset);
		}
		var listContainer = $("<div>",{class:"listContainer jqplot-cdistarget"}).appendTo(reportB);
		var reportTable = $("<table>",{class:"raportTable",cellpadding:3,cellspacing:0}).appendTo(listContainer);
		var thead = $("<thead>").appendTo(reportTable);
		var tbody = $("<tbody>").appendTo(reportTable);
		var reportHeadLine = $("<tr>").appendTo(thead);
		$.each(dataset.header,function(index, value){
			$("<th>").text(value).appendTo(reportHeadLine);
		});
		
		$.each(dataset.dataset,function(index, arrLine){
			var rline = $("<tr>").appendTo(tbody);
			$.each(arrLine,function(index, arrValue){
				$("<td>").text(arrValue).appendTo(rline);
			});
		});
	
}


function getReportObjectFromCriterias(){
	var result = {};
	result["id"] = "0";
	result["type"] = $("input[name='reporttype']:checked").val();
	result["title"] = "Custom Report";
	result["owner"] = "System";
	var now = moment();
	result["generated"] = now.format('YYYY-MM-DD HH:mm:ss');
	result["graphtype"] = $("input[name='graphtype']:checked").val();
	result["filter"] = $("input[name='reportfilter']:checked").val();
	result["hcp"] = $("#hcptype").val();
	result["hcpid"] = $("#hcpid").val();;
	
	var subcriterias = [];
	if(result["type"] == "graph"){
		$.each($("#criteria-summary div"),function(index,obj){
			var sub = {};
			sub["subname"] =$(obj).attr("name");
			sub["subsection"] =$(obj).attr("section");
			sub["subdisplay"] =$(obj).attr("display");
			sub["subvalue"] =$(obj).attr("value");
			sub["suboperator"] =$(obj).attr("operator");
			subcriterias[subcriterias.length] = sub;
		});
		result["subcriteria"] = subcriterias;
	}else{
		result["subcriteria"] = subcriterias;
	}
	
	
	
	var criterias = [];
	if(result["type"] == "graph"){
		var cval = $("input[name='reportcriteria']:checked").val();
		if(cval == "diabet"){
			var dtypes = report_dtype;
			$.each(dtypes, function(index, value){
				if(value != "All"){
					var criteria = {};
					criteria["name"] = "dtype";
					criteria["section"] = "2";
					
					criteria["value"] = index;
					/*
					if(index == "3"){
						criteria["value"] = "10";
					}else if(index == "4"){
						criteria["value"] = "11";
					}else{
						criteria["value"] = index;
					}
					*/
					criteria["operator"] = "equal";
					criteria["display"] = value;
					criteria["date"] = "no";
					criteria["datename"] = "";
					criteria["datevalue"] = "";
					criteria["dateoperator"] = "";
					criteria["datedisplay"] = "";
					criteria["type"] = "all";
					criterias[criterias.length] = criteria;
				}
			});
		}else if(cval == "community"){
			var communities = report_idcommunity;
			$.each(communities, function(index, value){
				if(value != "All" && value != "Non-Cree Community"){
					var criteria = {};
					criteria["name"] = "idcommunity";
					criteria["section"] = "1";
					criteria["value"] = index;
					criteria["operator"] = "equal";
					criteria["display"] = value;
					criteria["date"] = "no";
					criteria["datename"] = "";
					criteria["datevalue"] = "";
					criteria["dateoperator"] = "";
					criteria["datedisplay"] = "";
					criteria["type"] = "all";
					criterias[criterias.length] = criteria;
				}
			});
		}else if(cval == "gender"){
			var genders = report_sex;
			$.each(genders, function(index, value){
				if(value != "All" ){
					var criteria = {};
					criteria["name"] = "sex";
					criteria["section"] = "1";
					criteria["value"] = index;
					criteria["operator"] = "equal";
					criteria["display"] = value;
					criteria["date"] = "no";
					criteria["datename"] = "";
					criteria["datevalue"] = "";
					criteria["dateoperator"] = "";
					criteria["datedisplay"] = "";
					criteria["type"] = "all";
					criterias[criterias.length] = criteria;
				}
			});
		}
	}else{
		$.each($("#criteria-summary div"),function(index,obj){
			var criteria = {};
			criteria["name"] = $(obj).attr("name");
			criteria["section"] = $(obj).attr("section");
			if($(obj).attr("type") == "select"){
				var v = $(obj).attr("value");
				v = v.toString().toLowerCase();
				if(v != "all"){
					var valArr = eval("report_"+$(obj).attr("name"));
					criteria["value"] =  $.inArray($(obj).attr("value"),valArr);
				}else{
					criteria["value"] =  "all";
				}
				
			}else{
				criteria["value"] = $(obj).attr("value");
			}
			
			criteria["operator"] = $(obj).attr("operator");
			criteria["display"] = $(obj).attr("display");
			criteria["date"] = $(obj).attr("date");
			criteria["datename"] = $(obj).attr("datename");
			criteria["dateoperator"] = $(obj).attr("dateoperator");
			criteria["datevalue"] = $(obj).attr("datevalue");
			criteria["datedisplay"] = $(obj).attr("datedisplay");
			var v = criteria["value"];
			v = v.toString().toLowerCase();
			if(v == "all"){
				criteria["type"] = "all";
			}else{
				criteria["type"] = "set";
			}
			criterias[criterias.length] = criteria;
		});
	}
	result["criteria"] = criterias;
	return result;
}



function executeReport(report){
	$("body").append($("<div>",{class:"modal"}).append($("<div>",{class:"modal-span"}).text("CDIS Loading..."))).addClass("loading");
	var dataset = prepareCustomReport(report);
	if($.type(dataset) === "object"){
		var ds = dataset.dataset;
		if(ds.length > 0){
			buildReport(report, dataset);
		}else{  }
	}
	$("body").removeClass("loading");
	$(".modal").remove();
}


function loadReport(rObj){
	var report = rObj;
	if(report.input!= null &&  $.type(report.input) === "array" ){
		$.each(report.input, function(ix, objInput){
			var obj = {};
			var val = "0";
			obj["subname"] = objInput.name;
			if(objInput.section != "90"){
				if((objInput.name == "idcommunity" || objInput.name == "dtype") && (val == "0")){
					obj["suboperator"] = "more than";
				}else{
					obj["suboperator"] = objInput.operator;
				}
			}else{
				obj["suboperator"] = objInput.operator;
			}
			obj["subvalue"] = val;
			obj["subdisplay"] = objInput.display;
			obj["subsection"] = objInput.section;
			report.subcriteria[ix] = obj;
			var t = report.title;
			reportObjectToExecute = report;
		});
	}
	return report;
}




function buildReportToolbar(divToolbarObj, reportObject){
	
	var $subtoolbar = $("<div>",{id:"report-export-toolbar",class:"umbra"}).appendTo(divToolbarObj);
	$subtoolbar.css("position","absolute");
	$subtoolbar.css("height","100%");
	$subtoolbar.css("width","100%");
	$subtoolbar.css("background-color","#cdcdcd");
	$subtoolbar.css("display","none");
	$subtoolbar.css("text-align","left");

	
	if(reportObject.id == "0"){
		var $savetoolbar = $("<div>",{id:"report-save-toolbar",class:"umbra"}).appendTo(divToolbarObj);
		$savetoolbar.css("position","absolute");
		$savetoolbar.css("height","100%");
		$savetoolbar.css("width","100%");
		$savetoolbar.css("background-color","#cdcdcd");
		$savetoolbar.css("display","none");
		$savetoolbar.css("text-align","right");
	
		$("<a>",{id:"report-save-button", class:"cisbutton",style:"margin-top:13px;"}).text("Save Report As...").appendTo($savetoolbar);
		$("<input>",{id:"report-save-name", type:"text",style:"margin:13px 10px 0px 10px;width:300px;vertical-align:middle;padding:5px;"}).appendTo($savetoolbar);
		$("<div>",{id:"report-save-exit", class:"cisbutton",style:"margin-top:13px;right:10px;"}).html("<i class=\"fa fa-times\" aria-hidden=\"true\"></i>").appendTo($savetoolbar).click(function(){$( "#report-save-toolbar").toggle( "slide" );});
		
		var $saveTo = $("<div>",{id:"report-save", class:"cisbutton"}).text("Save Report").appendTo(divToolbarObj);
		
		$saveTo.click(function(){
			 $("#report-save-toolbar").toggle( "slide" );
		});
		
		$("#report-save-button").click(function(){
			var result = null;
			if($("#report-save-name").val() != ""){
				reportObject.title = $("#report-save-name").val(); 
				$.ajax({
				    url: "/ncdis/service/action/saveReport?language=en&iduser="+userObj[0].iduser,
				    type: 'POST',
				    data:JSON.stringify(reportObject),
				    contentType: 'application/json; charset=utf-8',
				    dataType: 'json',
				    async: false,
				    success: function(msg) {
				        window.location = "reports.html?section=dahboard&sid="+sid+"&language=en";
				    }
				});
			}
		});
	}
	
	var $exportTo = $("<div>",{id:"report-export", class:"cisbutton"}).text("Export").appendTo(divToolbarObj);
	var $printReport = $("<div>",{id:"report-print", class:"cisbutton"}).text("Print Report").appendTo(divToolbarObj);
	var $closeReport = $("<div>",{id:"report-close", class:"cisbutton"}).text("Close").appendTo(divToolbarObj);
	
	var $exportCSVReport = $("<a>",{id:"report-export-csv", class:"cisbutton",style:"margin-left:10px;margin-top:13px;"}).text("Export to CSV (table only)").appendTo($subtoolbar);
	
	//var $exportEXCELReport = $("<a>",{id:"report-export-excel", class:"cisbutton",style:"margin-top:13px;"}).text("Export to Excel (table only)").appendTo($subtoolbar);
	
	if(reportObject.type != "list"){
		var $exportPNGReport = $("<a>",{id:"report-export-png", class:"cisbutton",style:"margin-top:13px;"}).text("Export to PNG (image)").appendTo($subtoolbar);
		$exportPNGReport.click(function(){
			$(".raportBody").tableExport({type:'png',escape:'false'});
			$("#report-export-toolbar").toggle( "slide" );
		});
	}
	
	
	//var $exportPDFReport = $("<a>",{id:"report-export-pdf", class:"cisbutton",style:"margin-top:13px;"}).text("Export to PDF").appendTo($subtoolbar);
	var $exitSubReport = $("<div>",{id:"report-export-exit", class:"cisbutton",style:"margin-top:13px;right:10px;"}).html("<i class=\"fa fa-times\" aria-hidden=\"true\"></i>").appendTo($subtoolbar).click(function(){$( "#report-export-toolbar").toggle( "slide" );});
	
	$printReport.click(function(){
		$(".raportBody").printCDISSection();
		$("html, body").css("overflow", "show");
		return false;
	});

	$closeReport.click(function(){
		//window.location = "reports.html?section=dahboard&sid="+sid+"&language=en";
		$(".raportContainer").remove();
	});

	$exportCSVReport.click(function(){
		$(".raportTable").tableExport({type:'csv',escape:'false'});
		$("#report-export-toolbar").toggle( "slide" );
	});
	
	/*
	$exportEXCELReport.click(function(){
		$(".raportTable").tableExport({type:'excel',escape:'false'});
		$("#report-export-toolbar").toggle( "slide" );
	});
	*/
	
	$exportTo.click(function(){
		if(reportObject.type != "list"){
			html2canvas($(".raportBody"), {
				onrendered: function(canvas) {										
					var img = canvas.toDataURL("image/png");
					exportImage = img;
				}
			});
		}
		 $("#report-export-toolbar").toggle( "slide" );
	});
}


function buildCriteriaList(criteriasArray, divObj){
	var list = divObj;
	$.each(criteriasArray, function(index, objItem){
		if(objItem.visible != "0"){
			if(objItem.type != "category"){
				var line = $("<div>",{id:objItem.name,class:"report-criteria"}).appendTo(list);
				var tab = $("<table>",{id:objItem.name+"-table",class:"report-criteria-table",cellspacing:0,cellpadding:0,border:0}).appendTo(line);
				var tr = $("<tr>").appendTo(tab);
				var prima = $("<td>",{class:"report-criteria-table-label"}).appendTo(tr); 
				var dd = $("<div>",{id:"div-"+objItem.name+"-id",class:"report-criteria-label"}).text(objItem.label).appendTo(prima);
				dd.click(function(e){
					var isGraph = $("input[name='reporttype']").filter("[value='graph']").prop('checked');
					var v = $(this).hasClass("report-criteria-label-selected");
					if(v){
						$(this).removeClass("report-criteria-label-selected");
						$("#"+objItem.name).css("background-color","#fcfcfc");
						
						
						var line = $("#div-"+objItem.name+"-id").parent().parent();
						
						$.each($(line).find("td"), function(index, cel){
							if(!$(cel).hasClass("report-criteria-table-label")){
								$(cel).empty();
							}
						});
						
						/*
						$("#"+objItem.name+"-operator-id-button").hide();
						$("#"+objItem.name+"-"+objItem.type+"-value1-id-button").hide();
						$("#"+objItem.name+"-"+objItem.type+"-value2-id-button").hide();
						
						var ss = $("#"+objItem.name+"-"+objItem.type+"-collected-date-operator-id");
						if(ss.length > 0){
							ss[0].selectedIndex = 0;
							ss.selectmenu("refresh");
						}
						$("#"+objItem.name+"-"+objItem.type+"-value1-id").hide();
						$("#"+objItem.name+"-"+objItem.type+"-value2-id").hide();
						$("#"+objItem.name+"-"+objItem.type+"-collected-date-value1-id").hide();
						$("#"+objItem.name+"-"+objItem.type+"-collected-date-value1-id").val("");
						$("#"+objItem.name+"-"+objItem.type+"-collected-date-value2-id").hide();
						$("#"+objItem.name+"-"+objItem.type+"-collected-date-value2-id").val("");
						$("#"+objItem.name+"-"+objItem.type+"-collected-date-id").hide();
						*/
						removeFromSummary(objItem);
						//removeFromSummaryCD(objItem);
						
						
						$.each($(".report-criteria"),function(index,obj){
							if(isGraph){
								if($(obj).attr("id") != "dtype" && $(obj).attr("id") != "idcommunity" && $(obj).attr("id") != "sex"){
									$(obj).show();
								}
							}else{
								$(obj).show();
							}
						});
						
						$(".report-criteria-category").show();

						
					}else{
						$(this).addClass("report-criteria-label-selected");
						$("#"+objItem.name).css("background-color","#ccffcc");
						
						createOperator(objItem);
						createValues(objItem);
						
						$("#"+objItem.name+"-operator-id-button").show();
						var s = $("#"+objItem.name+"-operator-id");
						
						if(objItem.type == "select"){
							s.val("1");
							s.selectmenu("refresh");
							$("#"+objItem.name+"-"+objItem.type+"-value1-id-button").show();
							$("#"+objItem.name+"-"+objItem.type+"-value1-id").val("0");
							$("#"+objItem.name+"-"+objItem.type+"-value1-id").selectmenu("refresh");
							var mes = s.find(":selected").text()+"|"+ $("#"+objItem.name+"-"+objItem.type+"-value1-id").find(":selected").text();
							
							addToSummary(objItem, mes);
							//addToSummaryCD(objItem);
						}else{
							s[0].selectedIndex = 0;
							s.selectmenu("refresh");
							$("#"+objItem.name+"-"+objItem.type+"-value1-id").val("");
							$("#"+objItem.name+"-"+objItem.type+"-value2-id").val("");
							addToSummary(objItem);
							//addToSummaryCD(objItem);	
						}
						if(isGraph){
							$.each($(".report-criteria"),function(index,obj){
								if($(obj).attr("id") != objItem.name){
									$(obj).hide();
								}
							});
							$(".report-criteria-category").hide();
						}
						
					}
				});
				
				if($("#div-"+objItem.name+"-id").hasClass("report-criteria-label-selected")){
					addToSummary(objItem);
					$("#"+objItem.name).css("background-color","#ccffcc");
				}else{
					$("#"+objItem.name).css("background-color","#fcfcfc");
				}
				
				createOperator(objItem);
				createValues(objItem);
				
				
			}else{
				//category line
				var line = $("<div>",{id:objItem.name,class:"report-criteria-category"}).appendTo(list);
				var tab = $("<table>",{id:objItem.name+"-table",class:"report-criteria-table"}).appendTo(line);
				var tr = $("<tr>").appendTo(tab);
				$("<span>",{class:"criteria-category"}).text(objItem.label).appendTo($("<td>").appendTo(tr));
			}

		}else{
			//invisible elements
			//check if status is 1 to add them to criteria list
			if(objItem.status == "1"){
				addToSummary(objItem);
			}
		}
	});
}	


function cleanAll(){
	var isGraph = $("input[name='reporttype']").filter("[value='graph']").prop('checked');
	if(isGraph){
		$.each($(".report-criteria"), function(index,obj){
			var id = $(obj).attr("id");
			if(id != "dtype" && id != "idcommunity" && id != "sex"){
				$(obj).css("background-color","#fcfcfc");
				
				$("#div-"+id+"-id").removeClass("report-criteria-label-selected");
				var line = $("#div-"+id+"-id").parent().parent();
				
				$.each($(line).find("td"), function(index, cel){
					if(!$(cel).hasClass("report-criteria-table-label")){
						$(cel).empty();
					}
				});
				$(obj).show();
				removeFromSummary({"name":id});
			}else{
				removeFromSummary({"name":id});
			}
		});
		
	}else{
		$.each($(".report-criteria"), function(index,obj){
			$(obj).css("background-color","#fcfcfc");
			var id = $(obj).attr("id");
			$("#div-"+id+"-id").removeClass("report-criteria-label-selected");
			var line = $("#div-"+id+"-id").parent().parent();
			
			$.each($(line).find("td"), function(index, cel){
				if(!$(cel).hasClass("report-criteria-table-label")){
					$(cel).empty();
				}
			});
			$(obj).show();
			
			removeFromSummary({"name":id});
			
		});
		
		addToSummary
	}
	$(".report-criteria-category").show();
	
	
	
}


function addToSummary(objItem, message){
	var isGraph = $("input[name='reporttype']").filter("[value='graph']").prop('checked');
	if(message == null){
		var summary = $("<div>",{id:objItem.name+"-summary",class:"report-criteria-summary"}).appendTo($("#criteria-summary"));
		summary.attr("section",objItem.section);
		summary.attr("name",objItem.name);
		summary.attr("operator","equal");
		summary.attr("value","all");
		summary.attr("display",objItem.label);
		summary.attr("type","all");
		if(objItem.hasdate == "true"){
			summary.attr("date","yes");
			summary.attr("datedisplay",objItem.datedisplay);
			summary.attr("datename",objItem.name+"_collected_date");
			if(isGraph){
				summary.attr("dateoperator","equal");
				summary.attr("datevalue","last");
			}else{
				if($("input[name='reportperiod']").filter("[value='between']").prop('checked')){
					summary.attr("dateoperator","between");
					summary.attr("datevalue",$("#betweenv1").val()+" and "+$("#betweenv2").val());
				}else{
					summary.attr("dateoperator","equal");
					summary.attr("datevalue",($("#reportPeriod input[type='radio']:checked").val()));
				}
			}
		}else{
			summary.attr("date","no");
			summary.attr("datename","");
			summary.attr("dateoperator","");
			summary.attr("datevalue","");
			summary.attr("datedisplay","");
		}
		
		var spanSummary = $("<span>",{id:objItem.name+"-summary-span",class:"report-criteria-summary-span"}).text(objItem.label).appendTo(summary);
		//var closeSummary = $("<div>",{id:objItem.name+"-summary-div",class:"report-criteria-summary-div"}).text("X").appendTo(summary);
	}else{
		if($("#"+objItem.name+"-summary").length == 0 ){
			var summary = $("<div>",{id:objItem.name+"-summary",class:"report-criteria-summary"}).appendTo($("#criteria-summary"));
			$("<span>",{id:objItem.name+"-summary-span",class:"report-criteria-summary-span"}).text(objItem.label).appendTo(summary);
		}
		var sum = $("#"+objItem.name+"-summary");
		var parts = message.split("|");
		sum.attr("name",objItem.name);
		sum.attr("display",objItem.label);
		sum.attr("operator",parts[0]);
		sum.attr("value",parts[1]);
		sum.attr("section",objItem.section);
		sum.attr("type",objItem.type);
		
		
		if(objItem.hasdate == "true"){
			sum.attr("date","yes");
			sum.attr("datedisplay",objItem.datedisplay);
			sum.attr("datename",objItem.name+"_collected_date");
			if(isGraph){
				sum.attr("dateoperator","equal");
				sum.attr("datevalue","last");
			}else{
				if($("input[name='reportperiod']").filter("[value='between']").prop('checked')){
					sum.attr("dateoperator","between");
					sum.attr("datevalue",$("#betweenv1").val()+" and "+$("#betweenv2").val());
				}else{
					sum.attr("dateoperator","equal");
					sum.attr("datevalue",$("input[name='reportperiod']:checked").val());
				}
				
			}
			
		}else{
			sum.attr("date","no");
			sum.attr("datename","");
			sum.attr("dateoperator","");
			sum.attr("datevalue","");
			sum.attr("datedisplay","");
		}
		
		
		$("#"+objItem.name+"-summary-span").text(objItem.label +" "+ replaceAll(message,"|"," "));
	}
}

function addToSummaryCD(objItem, message){
	if(objItem.section != "1"){
		var d = "Date";
		if(objItem.name == "dtype"){
			d = "Date of diagnosis";
		}
		if(message == null){
			var summary1 = $("<div>",{id:objItem.name+"-collected-date-summary",class:"report-criteria-summary",section:objItem.section,value:"-1",operator:"-1",type:""}).appendTo($("#criteria-summary"));
			$("<span>",{id:objItem.name+"-collected-date-summary-span",class:"report-criteria-summary-span"}).text(objItem.label+" "+d).appendTo(summary1);
			//var closeSummary1 = $("<div>",{id:objItem.name+"-collected-date-summary-div",class:"report-criteria-summary-div"}).text("X").appendTo(summary1);
		}else{
			//var spanSummaryText = $("#"+objItem.name+"-collected-date-summary-span").text();
			if($("#"+objItem.name+"-summary").length == 0 ){
				var summary1 = $("<div>",{id:objItem.name+"-summary",class:"report-criteria-summary",section:objItem.section,value:"-1",operator:"-1",type:"date"}).appendTo($("#criteria-summary"));
				$("<span>",{id:objItem.name+"-summary-span",class:"report-criteria-summary-span"}).text(objItem.label).appendTo(summary1);
			}
			var sum = $("#"+objItem.name+"-collected-date-summary");
			var parts = message.split("|");
			sum.attr("operator",parts[1].toLowerCase());
			sum.attr("value",parts[2].toLowerCase());
			sum.attr("section",objItem.section);
			sum.attr("type",objItem.type);
			
			$("#"+objItem.name+"-collected-date-summary-span").text(objItem.label +" "+d+" "+ replaceAll(message,"|"," "));
		}
	}
}


function removeFromSummary(objItem){
	if($("#"+objItem.name+"-summary").length > 0 ){
		$("#"+objItem.name+"-summary").remove();
	}
}
function removeFromSummaryCD(objItem){
	if($("#"+objItem.name+"-collected-date-summary").length > 0 ){
		$("#"+objItem.name+"-collected-date-summary").remove();
	}
}


function createOperator(objItem){
	if(objItem.type != "none"){
		var tr = $("#"+objItem.name+"-table tr");
		
		if($("#"+objItem.name+"-operator-id").length > 0){
			
		}else{
			var selOperator = $("<select>",{id:objItem.name+"-operator-id",name:objItem.name+"-operator"});
			if($("."+objItem.name+"-operator-cell").length > 0 ){
				$("."+objItem.name+"-operator-cell").append(selOperator);
			}else{
				tr.append($("<td>",{class:objItem.name+"-operator-cell"}).append(selOperator));
			} 
			var so = selOperator.selectmenu({
				change: function( event, ui ) {
					if(objItem.type == "select"){
						$("#"+objItem.name+"-"+objItem.type+"-value1-id-button").show();
					}else{
						var v = selOperator.val();
						if(v == 0){
							if(objItem.type == "select"){
								$("#"+objItem.name+"-"+objItem.type+"-value1-id-button").hide();
							}else{
								$("#"+objItem.name+"-"+objItem.type+"-value1-id").hide();
								$("#"+objItem.name+"-"+objItem.type+"-value2-id").hide();
							}
							
						}else if(v == 4){
							if(objItem.type == "select"){
								$("#"+objItem.name+"-"+objItem.type+"-value1-id-button").show();
								//$("#"+objItem.name+"-"+objItem.type+"-value2-id").selectmenu().parent().show();
							}else{
								$("#"+objItem.name+"-"+objItem.type+"-value1-id").show();
								$("#"+objItem.name+"-"+objItem.type+"-value2-id").show();
							}
							
						}else{
							if(objItem.type == "select"){
								$("#"+objItem.name+"-"+objItem.type+"-value1-id-button").show();
								//$("#"+objItem.name+"-"+objItem.type+"-value2-id").selectmenu().parent().hide();
							}else{
								$("#"+objItem.name+"-"+objItem.type+"-value1-id").show();
								$("#"+objItem.name+"-"+objItem.type+"-value2-id").hide();
							}
							
						}
					}
			
					
				}
			});
			
			$("#"+objItem.name+"-operator-id-button").hide();
			
			var check = $("#check-"+objItem.name+"-id");
			if(check.attr("checked") == "checked"){
				$("#"+objItem.name+"-operator-id-button").show();
			}	
			
			
			var dataOperator = eval("report_"+objItem.type+"_operator");
			$.each(dataOperator, function(idx, item){
				$("<option>",{value:idx}).text(item).appendTo(selOperator);
			});

		}
	}
}


function createValues(objItem){
	if(objItem.type != "none"){
		var tr = $("#"+objItem.name+"-table tr");
		
		if(($("#"+objItem.name+"-"+objItem.type+"-value-id").length > 0) || ($("#"+objItem.name+"-"+objItem.type+"-value1-id").length > 0)){
			
		}else{
			if(objItem.type == "select"){
				var sel = $("<select>",{id:objItem.name+"-"+objItem.type+"-value1-id",name:objItem.name+"-"+objItem.type+"-value1"}).css("width","300px");
					
				if($("."+objItem.name+"-value-cell").length > 0){
					$("."+objItem.name+"-value-cell").empty();
					$("."+objItem.name+"-value-cell").append(sel);
				}else{
					tr.append($("<td>",{class:objItem.name+"-value-cell"}).append(sel));
				}
				
				sel.selectmenu({
					change : function(event, ui){
						var message = $("#"+objItem.name+"-operator-id").find(":selected").text()+"|"+ ui.item.label;
						addToSummary(objItem,message);
					}
				});
				
				//sel.hide();
				$("#"+objItem.name+"-"+objItem.type+"-value1-id-button").hide();
				var data = eval("report_"+objItem.name);
				$.each(data, function(idx, item){
					$("<option>",{value:idx}).text(item).appendTo(sel);
				});
				
				
			}else if(objItem.type == "date"){
				var v1 = $("<input>",{id:objItem.name+"-"+objItem.type+"-value1-id",name:objItem.name+"-"+objItem.type+"-value1",type:"text",class:"ui-button ui-widget ui-state-default ui-corner-all ui-state-hover"});
				if($("."+objItem.name+"-value1-cell").length > 0){
					$("."+objItem.name+"-value1-cell").empty();
					$("."+objItem.name+"-value1-cell").append(v1);
				}else{
					tr.append($("<td>",{class:objItem.name+"-value1-cell"}).append(v1));
				}
				
				
				v1.datepicker({
				    changeMonth: true,
				    yearRange: "1900:"+moment().year(),
				    dateFormat: "yy-mm-dd",
				    changeYear: true
				  });
				v1.hide();
				v1.change(function(e){
					var v2v = $("#"+objItem.name+"-"+objItem.type+"-value2-id");
					var vo = $("#"+objItem.name+"-operator-id").find(":selected").val();
					var message = $("#"+objItem.name+"-operator-id").find(":selected").text()+"|"+ $(this).val();
					if(vo == 4){
						if(v2v.val() != ""){
							message = $("#"+objItem.name+"-operator-id").find(":selected").text()+"|"+ $(this).val() +" and "+v2v.val();
						}
					}
					addToSummary(objItem,message);
				});
				var v2 = $("<input>",{id:objItem.name+"-"+objItem.type+"-value2-id",name:objItem.name+"-"+objItem.type+"-value2",type:"text",class:"ui-button ui-widget ui-state-default ui-corner-all ui-state-hover"});
				if($("."+objItem.name+"-value2-cell").length > 0){
					$("."+objItem.name+"-value2-cell").empty();
					$("."+objItem.name+"-value2-cell").append(v2);
				}else{
					tr.append($("<td>",{class:objItem.name+"-value2-cell"}).append(v2));
				}
				v2.datepicker({
				    changeMonth: true,
				    yearRange: "1900:"+moment().year(),
				    dateFormat: "yy-mm-dd",
				    changeYear: true
				  });
				v2.hide();
				v2.change(function(e){
					var message = $("#"+objItem.name+"-operator-id").find(":selected").text()+"|"+$("#"+objItem.name+"-"+objItem.type+"-value1-id").val()+" and "+ $(this).val();
					addToSummary(objItem,message);
				});
			}else if(objItem.type == "value"){
				var v1 = $("<input>",{id:objItem.name+"-"+objItem.type+"-value1-id",name:objItem.name+"-"+objItem.type+"-value1",type:"text",class:"ui-button ui-widget ui-state-default ui-corner-all ui-state-hover"});
				if($("."+objItem.name+"-value1-cell").length > 0){
					$("."+objItem.name+"-value1-cell").empty();
					$("."+objItem.name+"-value1-cell").append(v1);
				}else{
					tr.append($("<td>",{class:objItem.name+"-value1-cell"}).append(v1));
				}
				v1.button();
				v1.hide();
				$("#"+objItem.name+"-"+objItem.type+"-value1-id").change(function(e){
					var v2v = $("#"+objItem.name+"-"+objItem.type+"-value2-id");
					var vo = $("#"+objItem.name+"-operator-id").find(":selected").val();
					var message = $("#"+objItem.name+"-operator-id").find(":selected").text()+"|"+ $(this).val();
					if(vo == 4){
						if(v2v.val() != ""){
							message = $("#"+objItem.name+"-operator-id").find(":selected").text()+"|"+ $(this).val() +" and "+v2v.val();
						}
					}
					addToSummary(objItem,message);
				});
				var v2 = $("<input>",{id:objItem.name+"-"+objItem.type+"-value2-id",name:objItem.name+"-"+objItem.type+"-value2",type:"text",class:"ui-button ui-widget ui-state-default ui-corner-all ui-state-hover"});
				if($("."+objItem.name+"-value2-cell").length > 0){
					$("."+objItem.name+"-value2-cell").empty();
					$("."+objItem.name+"-value2-cell").append(v2);
				}else{
					tr.append($("<td>",{class:objItem.name+"-value2-cell"}).append(v2));
				}
				
				v2.button();
				v2.hide();
				$("#"+objItem.name+"-"+objItem.type+"-value2-id").change(function(e){
					var message = $("#"+objItem.name+"-operator-id").find(":selected").text()+"|"+$("#"+objItem.name+"-"+objItem.type+"-value1-id").val()+" and "+ $(this).val();
					addToSummary(objItem,message);
				});
			}	
		}

	}else{
		
	}
}


function createCD(objItem){
	if(objItem.type == "value"){
		addToSummaryCD(objItem);
		
		if($("#"+objItem.name+"-"+objItem.type+"-collected-date-id").length > 0){
			$("#"+objItem.name+"-"+objItem.type+"-collected-date-id").show();
		}else{
			var vline = $("#"+objItem.name);
			var newline = $("<div>",{id:objItem.name+"-"+objItem.type+"-collected-date-id",class:"report-criteria"}).insertAfter(vline);
			var tab1 = $("<table>",{id:objItem.name+"-collected-date-table",class:"report-criteria-table",cellspacing:0,cellpadding:0,border:0}).appendTo(newline);
			var tr1 = $("<tr>").appendTo(tab1);
			var prima1 = $("<td>",{class:"report-criteria-table-label"}).appendTo(tr1); 
			var newcheck = $("<input>",{id:"check-"+objItem.name+"-collected-date-id",type:"checkbox",name:"check-"+objItem.name+"-collected-date",value:"1",checked:true}).appendTo(prima1);
			var label = $("<label>").text(objItem.label+" Collected Date").appendTo(prima1);
			$(label).attr("for","check-"+objItem.name+"-collected-date-id");
			newcheck.button();
			newcheck.click(function(){
				var vv = this.checked;
				if(vv){
					$("#"+objItem.name+"-"+objItem.type+"-collected-date-operator-id-button").show();
					var s = $("#"+objItem.name+"-"+objItem.type+"-collected-date-operator-id");
					s[0].selectedIndex = 0;
					s.selectmenu("refresh");
					$("#"+objItem.name+"-"+objItem.type+"-collected-date-value1-id").hide();
					$("#"+objItem.name+"-"+objItem.type+"-collected-date-value1-id").val("");
					$("#"+objItem.name+"-"+objItem.type+"-collected-date-value2-id").hide();
					$("#"+objItem.name+"-"+objItem.type+"-collected-date-value2-id").val("");
					$("#"+objItem.name+"-"+objItem.type+"-collected-date-id").css("background-color","#ccffcc");
					addToSummaryCD(objItem);
				}else{
					$("#"+objItem.name+"-"+objItem.type+"-collected-date-operator-id-button").hide();
					$("#"+objItem.name+"-"+objItem.type+"-collected-date-value1-id").hide();
					$("#"+objItem.name+"-"+objItem.type+"-collected-date-value2-id").hide();
					$("#"+objItem.name+"-"+objItem.type+"-collected-date-id").css("background-color","#fcfcfc");
					removeFromSummaryCD(objItem);
				}
			});
			var newSelOperator = $("<select>",{id:objItem.name+"-"+objItem.type+"-collected-date-operator-id",name:objItem.name+"-operator"}).appendTo($("<td>").appendTo(tr1));
			newSelOperator.selectmenu({
				change: function(event, ui){
					var v = newSelOperator.val();
					if(v == 0){
						$("#"+objItem.name+"-"+objItem.type+"-collected-date-value1-id").hide();
						$("#"+objItem.name+"-"+objItem.type+"-collected-date-value2-id").hide();
					}else if(v == 4){
						$("#"+objItem.name+"-"+objItem.type+"-collected-date-value1-id").show();
						$("#"+objItem.name+"-"+objItem.type+"-collected-date-value2-id").show();
					}else{
						$("#"+objItem.name+"-"+objItem.type+"-collected-date-value1-id").show();
						$("#"+objItem.name+"-"+objItem.type+"-collected-date-value2-id").hide();
					}
				}
			});
			var newDataOperator = report_date_operator;
			$.each(newDataOperator, function(idx, item){
				$("<option>",{value:idx}).text(item).appendTo(newSelOperator);
			});
			var new_v1 = $("<input>",{id:objItem.name+"-"+objItem.type+"-collected-date-value1-id",name:objItem.name+"-collected-date-value1",type:"text",class:"ui-button ui-widget ui-state-default ui-corner-all ui-state-hover"}).appendTo($("<td>").appendTo(tr1));
			new_v1.datepicker({
			    changeMonth: true,
			    yearRange: "1900:"+moment().year(),
			    dateFormat: "yy-mm-dd",
			    changeYear: true
			  });
			new_v1.hide();
			new_v1.change(function(e){
				var v2v = $("#"+objItem.name+"-"+objItem.type+"-collected-date-value2-id");
				var vo = $("#"+objItem.name+"-collected-date-operator-id").find(":selected").val();
				var message = "|"+$("#"+objItem.name+"-"+objItem.type+"-collected-date-operator-id").find(":selected").text()+"|"+ $(this).val();
				if(vo == 4){
					if(v2v.val() != ""){
						message = "|"+$("#"+objItem.name+"-"+objItem.type+"-collected-date-operator-id").find(":selected").text()+"|"+ $(this).val()+" and "+v2v.val();
					}
				}
				
				addToSummaryCD(objItem,message);
			});
			var new_v2 = $("<input>",{id:objItem.name+"-"+objItem.type+"-collected-date-value2-id",name:objItem.name+"-collected-date-value2",type:"text",class:"ui-button ui-widget ui-state-default ui-corner-all ui-state-hover"}).appendTo($("<td>").appendTo(tr1));
			new_v2.datepicker({
			    changeMonth: true,
			    yearRange: "1900:"+moment().year(),
			    dateFormat: "yy-mm-dd",
			    changeYear: true
			  });
			new_v2.hide();
			new_v2.change(function(e){
				var message = "|"+$("#"+objItem.name+"-"+objItem.type+"-collected-date-operator-id").find(":selected").text()+"|"+$("#"+objItem.name+"-"+objItem.type+"-collected-date-value1-id").val()+" and "+ $(this).val();
				addToSummaryCD(objItem,message);
			});
			if(newcheck.attr("checked") == "checked"){
				$("#"+objItem.name+"-"+objItem.type+"-collected-date-id").css("background-color","#ccffcc");
			}else{
				$("#"+objItem.name+"-"+objItem.type+"-collected-date-id").css("background-color","#fcfcfc");
			}
		}	
			
	}
}

function getCriterias(cObject){
	var result = [];
	if($.type(cObject) === "array"){
		//there is no id for object it mus be a div object
		result = cObject;
	}else{
		$.each($("#"+cObject.attr("id")+" div"), function(idx, item){
			var crit = {};
			var v = $(item).attr("value");
			var id = $(item).attr("id");
			var idname =  id.substring(0,id.indexOf("-summary"));
			if(idname.indexOf("-collected-date") >= 0 ){
				idname = replaceAll(idname,"-","_");
			}
			
			if($(item).attr("type") == "select"){
				if(idname == "dtype"){
					if(v == "PRE DM"){
						v ="3";
					}else if(v == "GDM"){
						v = "4";
					}else if(v == "All"){
						v = "-1";
					}else{
						var sel = eval("report_"+idname);
						v = sel.indexOf(v);
					};
				}else{
					if(v == "All"){
						v = "-1";
					}else{
						var sel = eval("report_"+idname);
						v = sel.indexOf(v);
					};
					
				};
				
				
			}
			
			crit["name"] = idname;
			//alert(id.substring(0,id.indexOf("-summary")));
			
			crit["section"] = $(item).attr("section");
			if(typeof $(item).attr("section") == "undefined"){
				if(idname == "dtype"){
					crit["section"] = "2";
				}else{
					crit["section"] = "1";	
				};
				
			}
			crit["operator"] = $(item).attr("operator");
			if(typeof $(item).attr("operator") == "undefined"){
				crit["operator"] = "";
			}
			
			
			crit["value"] = v;
			if(typeof $(item).attr("value") == "undefined"){
				crit["value"] = "";
			}
			result.push(crit);
		});
		
	}
	return result;
}



function prepareCustomReport(reportObject){
	var result = null;
	$.ajax({
	    url: "/ncdis/service/action/executeReport?language=en&idreport="+reportObject.id+"&owner="+reportObject.owner+"&type="+reportObject.type+"&graphtype="+reportObject.graphtype+"&subcriteriatype="+reportObject.subcriteriatype,
	    type: 'POST',
	    data:JSON.stringify(reportObject),
	    contentType: 'application/json; charset=utf-8',
	    dataType: 'json',
	    async: false,
	    success: function(msg) {
	        result = msg.objs[0];
	    }
	});
	return result;
}


function executeAsyncReport(reportObject){
	buildAsyncReport(reportObject);
	$.ajax({
	    url: "/ncdis/service/action/executeReport?language=en&idreport="+reportObject.id+"&owner="+reportObject.owner+"&type="+reportObject.type+"&graphtype="+reportObject.graphtype+"&subcriteriatype="+reportObject.subcriteriatype,
	    type: 'POST',
	    data:JSON.stringify(reportObject),
	    contentType: 'application/json; charset=utf-8',
	    dataType: 'json',
	    async: true,
	    success: function(msg) {
	    	var dataset = msg.objs[0];
	    	$("#loadingLineTr").remove();
	    	if($.type(dataset) === "object"){
	    		var ds = dataset.dataset;
	    		if(reportObject.type == "graph"){
	    			drawReportGraph(reportObject,dataset);
	    		}
	    		if(ds.length > 0){
	    			$.each(dataset.header,function(qq, value){
	    				$("<th>").text(value).appendTo($("#headLineTr"));
	    			});
	    			$.each(dataset.dataset,function(index, arrLine){
	    				var rline = $("<tr>").appendTo($("#reportBodyTable"));
	    				$.each(arrLine,function(ii, arrValue){
	    					$("<td>").text(arrValue).appendTo(rline);
	    				});
	    			});
	    		}
	    	}
	    }
	});
}


function buildAsyncReport(ro){
	var raper = $("#wraper");
	$("html, body").animate({ scrollTop: 0 }, "fast");
	$("html, body").css("overflow", "hidden");
	
	var report = $("<div>",{class:"raportContainer"}).appendTo(raper);
	var reportH = $("<div>",{class:"raportHeader umbra"}).append($("<div>",{class:"raportHeaderLogo"}).text("CDIS")).append($("<div>",{class:"raportHeaderButtons"})).appendTo(report);
	var reportB = $("<div>",{class:"raportBody"}).appendTo(report);
	reportB.css("height",(report.height()-110)+"px");
	
	buildReportToolbar($(".raportHeaderButtons"), ro);
	
	if(ro.type == "graph"){
		var graphContainer = $("<div>",{class:"graphContainer"}).appendTo(reportB);
		$("<div>",{id:"graphReport"}).appendTo(graphContainer);
	}
	var listContainer = $("<div>",{class:"listContainer jqplot-cdistarget", style:"border:0px solid #000000;"}).appendTo(reportB);
	var reportTable = $("<table>",{class:"raportTable",cellpadding:3,cellspacing:0,border:0}).appendTo(listContainer);
	var thead = $("<thead>").appendTo(reportTable);
	var tbody = $("<tbody>",{id:"reportBodyTable"}).appendTo(reportTable);
	$(tbody).append($("<tr>",{id:"loadingLineTr"}).append("<td>",{valign:"middle",align:"center"}).append($("<div>",{class:"modal-span"}).text("Generating CDIS report...")));
	var reportHeadLine = $("<tr>",{id:"headLineTr"}).appendTo(thead);
	
}


var progressOn=false;
function showProgress(container){
	if(!progressOn){
		var p = $('<div>',{id:"progress",class:"fullscreen-progress"}).appendTo(container);
		var c = $('<div>',{class:"fullscreen-progress-container"}).appendTo(p);
		var l = $('<div>',{class:"fullscreen-progress-container-logo"}).appendTo(c);
		var t = $('<div>',{class:"fullscreen-progress-container-text"}).appendTo(c);
		progressOn=true;
	}
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


function showPopupMessage(title,text){
	var id = moment();
	$("body").css("overflow-y","hidden");
	var modal = $('<div>',{id:"fullscreen_"+id,class:"popupmessage-fullscreen-modal"}).appendTo($("#wraper"));
	var sett = $('<div>',{class:"popupmessage-window"}).appendTo(modal);
	var settH = $('<div>',{class:"popupmessage-window-header"}).appendTo(sett);
	var settB = $('<div>',{class:"popupmessage-window-body"}).appendTo(sett);
	var settBB = $('<div>',{class:"popupmessage-window-body-body"}).appendTo(settB);
	var settBF = $('<div>',{class:"popupmessage-window-body-footer"}).appendTo(settB);
	
	$('<div>',{class:"gap"}).appendTo(settBF);
	var cb = $('<button>',{class:"cisbutton"}).text("Close").appendTo(settBF);
	cb.click(function(){
		$(".popupmessage-fullscreen-modal").remove();
		$("body").css("overflow-y","auto");
	});
	settBB.html(text);
	$('<div>',{class:"popupmessage-window-header-title"}).text(title).appendTo(settH);
	var settHC = $('<div>',{class:"popupmessage-window-header-close"}).html("<i class='fa fa-times'></i>").appendTo(settH);
	settHC.click(function(){
		$(".popupmessage-fullscreen-modal").remove();
		$("body").css("overflow-y","auto");
	});
	
}

