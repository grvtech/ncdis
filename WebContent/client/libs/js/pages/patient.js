/*
 * patient functions mostly from cdis
 * 
 */

function loadPatientObject(key,value){
	var patient = $.ajax({
		  url: "/ncdis/service/data/getPatientRecord?sid="+sid+"&language=en&"+key+"="+value,
		  type: "GET",
		  async : false,
		  cache : false,
		  dataType: "json"
		});
		patient.done(function( json ) {
			patientObjArray = json.objs;
			patientObj = patientObjArray[0];
			console.log("object patient");
			console.log(patientObjArray);
		});
		patient.fail(function( jqXHR, textStatus ) {
		  alert( "Request failed: " + textStatus );
		  console.log(this.url);
		});	
}

function getValueSectionArray(section, value, arr){
	//cdisSection = section;
	//var objSection = getObjectSection(arr);
	var objSection = getObjectArray(section,arr);
	//console.log('object section - '+section+'  -  value:'+value);
	//console.log(objSection);
	var objValue = eval("objSection."+value);
	
	//console.log(objValue);
	if(typeof(objValue) != 'undefined'){
		return objValue.values;
	}else{
		return [];
	}
}

function getValueObject(section, value, arr){
	//cdisSection = section;
	var objSection = getObjectArray(section,arr);
	var objValue = eval("objSection."+value);
	if(typeof(objValue) != 'undefined'){
		objValue['name'] = value;
		return objValue;
	}else{
		return {};
	}
}


function getObjectArray(objectName, objectArray){
	if(objectName == "mdvisits"){
		return objectArray[3];
	}else if(objectName == "lab"){
		return objectArray[6];
	}else if(objectName == "lipid"){
		return objectArray[5];
	}else if(objectName == "renal"){
		return objectArray[4];
	}else if(objectName == "complications"){
		return objectArray[7];
	}else if(objectName == "meds"){
		return objectArray[9];
	}else if(objectName == "miscellaneous"){
		return objectArray[8];
	}else if(objectName == "depression"){
		return objectArray[10];
	}else if(objectName == "diabet"){
		var oa = objectArray[2];
		 $.each(oa, function(key, value) {
			var oarr = value.values;
			$.each(oarr, function(k, v) {
				var newvalues = {dtype:v.value, ddate:v.date , diabetcode:v.code, diabetidvalue:v.idvalue};
				$.extend(true,v,newvalues);
			});
		 });
		return oa;
	}else if(objectName == "hcp"){
		var oa = objectArray[1];
		return oa;
	}
}



function getObjectSection(arr){
	
	if(cdisSection == "mdvisits"){
		return arr[3];
	}else if(cdisSection == "lab"){
		return arr[6];
	}else if(cdisSection == "lipid"){
		return arr[5];
	}else if(cdisSection == "renal"){
		return arr[4];
	}else if(cdisSection == "complications"){
		return arr[7];
	}else if(cdisSection == "meds"){
		return arr[9];
	}else if(cdisSection == "miscellaneous"){
		return arr[8];
	}else if(cdisSection == "depression"){
		return arr[10];
	}
}




function getValueLimits(valueName){
	var result = null;
	
	if(typeof(window['limits_'+valueName]) != 'undefined'){
		result = window['limits_'+valueName];
	}else{
		var limits = $.ajax({
			  url: "/ncdis/service/data/getValueLimits?sid="+sid+"&language=en&name="+valueName,
			  type: "GET",
			  async : false,
			  cache : false,
			  dataType: "json"
			});
			limits.done(function( json ) {
				result = json.objs[0];
				console.log(result);
			});
			limits.fail(function( jqXHR, textStatus ) {
			  alert( "Request failed: " + textStatus );
			});
	} 
	return result;
}





/* function to draw graph of value like in abc graphs - for normal values : without _and_ or _or_ in name */
function drawGraphValue(section, valueName){
	// section get data for graph
	//get data from patient object
	var valueArray = getValueSectionArray(section, valueName, patientObjArray);
	
	
	//get value limits
	var limitsObj = getValueLimits(valueName);
	
	//data formated for plot
	var values = [];
	var labels = [];
	$(valueArray).each(function( index ) {
		var ob = $(this)[0];
		if(ob.value != "" && ob.value!=null){
			values.push(ob.value);
		}
		if(ob.date != "" && ob.date!=null){
			labels.push(ob.date);
		}
	});
	
	
	//section prepare graph place
	var valueContainer = $("#"+section+"_"+valueName);
	var gc = $("<div>",{"id":"graph-"+valueName}).appendTo(valueContainer);
	//
	
	//section plotGraph in place with data
	loadGraphValue(gc, values, labels,"title",limitsObj,valueName);
}

/*function to draw graf for blood pressure - not existent yet in patient summary*/
function drawGraphBP(){
	
}

/*function to draw grapf for values with _or_ in name , with condition*/
function drawGraphOrValues(valueName, condition){
	
}




function loadGraphValue(graphContainer, values, labels, title, limits, valueName){
	graphContainer.empty();
	var divStr = $(graphContainer).attr("id");
	
	var target=0;
	var series = [];
	var ten = 10;
	var serie = [];
	var lastvalues = [];
	
		if(values.length > 0){
			if(values.length < ten){ten = values.length;}
			var iday  = moment(labels[0]);
			var limitday = iday.subtract(24,'months');
			var dc = eval(valueName+"_dec");
			var lastday = moment(labels[0]);
			var iscut = false;
			 for(var i=0;i<ten;i++){
				 var lab = Number(values[i]).trimNum(dc);
				 var day = moment(labels[i]);
				 
				 /**/
				 if(i >0){
					 if(day.isAfter(limitday)){
						 serie = [labels[i],Number(values[i]).trimNum(dc),lab];
						 series.push(serie);
						 lastvalues.push(values[i]);
						 //iday = moment(labels[i]);
					 }			 
				 }else{
					 serie = [labels[i],Number(values[i]).trimNum(dc),lab];
					 series.push(serie);
					 lastvalues.push(values[i]);
				 }
				 lastday = day;
			 }
			 var arrLimits = [];
			 var hasMax = false;
			 var hasMin = false;
			 
			 var fs = "%."+dc+"f";
			 if(dc == 0){
				 fs = "%d";
			 }
			 
			 var minNum = lastvalues.min();
			 var maxNum = lastvalues.max();
			 if(limits != null){
				 
					 if(minNum > Number(limits.minvalue)){minNum = limits.minvalue;}
					 if(maxNum < Number(limits.maxvalue)){maxNum = limits.maxvalue;}
					 $.each(limits.stages,function(idx,val){
						 if(idx == 0){
							 var limitObj = { rectangle: {name:"stage-"+valueName+"-"+idx, ymin:Number(val.min), xminOffset: "0px", xmaxOffset: "0px", yminOffset: "0px", ymaxOffset: "0px",color: val.color, showTooltip: false, tooltipFormatString: val.title } };
						 }else if(idx == limits.stages.length-1){
							 var limitObj = { rectangle: {name:"stage-"+valueName+"-"+idx, ymax: Number(val.max),xminOffset: "0px", xmaxOffset: "0px", yminOffset: "0px", ymaxOffset: "0px",color: val.color, showTooltip: false, tooltipFormatString: val.title } }; 
						 }else{
							 var limitObj = { rectangle: {name:"stage-"+valueName+"-"+idx, ymax: Number(val.max),ymin:Number(val.min), xminOffset: "0px", xmaxOffset: "0px", yminOffset: "0px", ymaxOffset: "0px",color: val.color, showTooltip: false, tooltipFormatString: val.title } };
						 }
						 arrLimits.push(limitObj);
					 });
					
			 }

			minNum = Number(minNum); 
			maxNum = Number(maxNum);

			 
			 $.jqplot.postDrawHooks.push(function() {
				 	for(var i=0;i<$('.jqplot-overlayCanvas-canvas').length;i++){
				 		var overlayCanvas = $($('.jqplot-overlayCanvas-canvas')[i]);
					    var seriesCanvas = $($('.jqplot-series-canvas')[i]);
					    seriesCanvas.detach();
					    overlayCanvas.after(seriesCanvas);
				 	}
			});
			
			var lc = title.toLowerCase();
			 
			 if (valueName == "hba1c"){
				 var lineLimitObj1 = {horizontalLine: {name: 'lineObj1',y: 0.060,lineWidth:1,xminOffset: '2px',xmaxOffset: '2px',color: 'rgb(5,5,5)',shadow: false}};
				 var lineLimitObj2 = {horizontalLine: {name: 'lineObj', y: 0.070,lineWidth: 1,xminOffset: '2px', xmaxOffset: '2px', color: 'rgb(5,5,5)',	shadow: false	}};
				 arrLimits.push(lineLimitObj1);
				 arrLimits.push(lineLimitObj2);
			 }
			 
			 if (valueName == "acglu"){
				 var lineLimitObj1 = {horizontalLine: {name: 'lineObj1',y: 6,lineWidth: 1,xminOffset: '2px',xmaxOffset: '2px',color: 'rgb(5,5,5)',shadow: false}};
				 var lineLimitObj2 = {horizontalLine: {name: 'lineObj2',y: 7,lineWidth: 1,xminOffset: '2px',xmaxOffset: '2px',color: 'rgb(5,5,5)',shadow: false}};
				 arrLimits.push(lineLimitObj1);
				 arrLimits.push(lineLimitObj2);
			 }
			 
			 var plot = jQuery.jqplot(divStr,[series], {
				 title:{text:"",show:false},
				 
				 grid:{backgroundColor: '#ffffff', gridLineColor: '#cdcdcd',gridLineWidth: 0.5, borderWidth: 0.5, borderColor: '#4d4d4d'},
				 seriesDefaults: {
					 rendererOptions: {smooth: true},
					 pointLabels:{ show:true, location:'s'}
				 },
			     axes:{
			            xaxis:{renderer:$.jqplot.DateAxisRenderer,rendererOptions:{tickRenderer:$.jqplot.CanvasAxisTickRenderer,tickInset:0.2},tickOptions:{fontSize:'8pt',textColor:'#000000',fontFamily:'Sans-serif',show:true,formatString:'%b-%Y',markSize:8}},
			            yaxis:{rendererOptions:{tickRenderer:$.jqplot.CanvasAxisTickRenderer,tickInset:0.5},tickOptions:{fontSize:'8pt',textColor:'#000000',fontFamily:'Sans-serif',show:true,formatString: fs,markSize:2},min:Number(minNum),max:Number(maxNum)}
			        },
			     canvasOverlay: {
			            show: true,
			            objects: arrLimits
			          }, 
		        series:[{lineWidth:2,markerOptions:{style:'filledCircle', color:'#cdcdcd',size:4 },color:'#4d4d4d'}],
		        
		        highlighter: {
		        	show: true,sizeAdjust: 10,tooltipLocation:'n',tooltipOffset:2,
		        	formatString:'<table class="jqplot-highlighter" border="0"><tr><td>Date:</td><td>%s</td></tr><tr><td>Value:</td><td>%s</td></tr></table>'},
		        cursor:{
		        	show: true,
		        	showTooltip:false
		        	}
				});
			
			 /*
			 if(limits != null){
				 var co = plot.plugins.canvasOverlay;
				 $.each(limits.stages,function(ii,vv){
					 var lineN = co.get('stage-'+valueName+'-'+ii);
					 var normLabel = lineN.gridStop[1]+11;
					 $('<div class="my-jqplot-normal-title" style="top:'+normLabel+'px;">'+vv.title+'</div>').insertBefore('#'+divStr+' .jqplot-overlayCanvas-canvas');
				 });
			 }
			 */
		}else{
			graphContainer.html("<p class='text-center'>No data for <b>"+valueName+"</b></p>");
		}/* end values lenght check */
		
}

