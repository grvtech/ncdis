var importFilesPeriod = 7;
var importFilesArray = new Array();;


function loadImportFiles(period){
	var omni = $.ajax({
		  url: "/ncdis/service/action/getImportOmnilabFiles?sid="+sid+"&language=en&period="+period,
		  type: "GET",
		  async : false,
		  cache : false,
		  dataType: "json"
		});
		omni.done(function( json ) {
			importFilesArray = json;
			console.log(importFilesArray);
			importFilesArray.sort(function(a,b){
				 var ma = moment(a.replace("import_","").replace(".html",""), "DD-MM-YYYY").utc();
				 var mb = moment(b.replace("import_","").replace(".html",""), "DD-MM-YYYY").utc();
				 return mb - ma;
				});
			console.log(importFilesArray);
		});
		omni.fail(function( jqXHR, textStatus ) {
		  alert( "Request failed: " + textStatus );
		});	
}



function initImportOmnilab(){
	loadImportFiles(importFilesPeriod);
	buildFileSelector(importFilesArray);
}


function buildFileSelector(filesArray){
	var container = $(".importomnilab-files-elements");
	$.each(filesArray, function(k,v){
		var fd = v.replace("import_","").replace(".html",""); 
		$("<div>",{class:"felem"}).appendTo(container).text(fd).click(function(){
			$(".felem").removeClass("selected");
			$(this).addClass("selected");
			$(".importomnilab-report").empty();
			$(".importomnilab-report").load("/ncdis/client/reports/import/"+v);
		});
	})
}
