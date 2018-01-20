// <--- --------------------------------------------------------------------------------------- ----
	
// 	Blog Entry:
// 	Ask Ben: Print Part Of A Web Page With jQuery
	
// 	Author:
// 	Ben Nadel / Kinky Solutions
	
// 	Link:
// 	http://www.bennadel.com/index.cfm?event=blog.view&id=1591
	
// 	Date Posted:
// 	May 21, 2009 at 9:10 PM
	
// ---- --------------------------------------------------------------------------------------- --->

// Create a jquery plugin that prints the given element.
jQuery.fn.printJQPlot = function(title){
	// NOTE: We are trimming the jtitlerQuery collection down to the
	// first element in the collection.
	if (this.size() > 1){
		this.eq( 0 ).print();
		return;
	} else if (!this.size()){
		return;
	}
	
	//can be one or can be more
    var charts = $(this).find('div.jqplot-target');
    // var imgelem = chart.jqplotToImageElem();
    var chartsImages = [];
    for(var i=0;i<charts.length;i++){
    	var chart = charts[i];
    	//chartsImages.push($(chart).jqplotToImageElemStr());
    	chartsImages.push($(chart).jqplotToImageStr());
    	//chartsImages.push($(chart).jqplotToImageElem());
    }
    //var imageElemStr = chart.jqplotToImageElemStr();
    
    //var statsTable = $('<div></div>').append($(this).closest('div.quintile-outer-container').find('table.stats-table').clone());
    // var rowstyles = window.getComputedStyle(statsrows.get(0), '');
 
	// ASSERT: At this point, we know that the current jQuery
	// collection (as defined by THIS), contains only one
	// printable element.
 
	// Create a random name for the print frame.
	var strFrameName = ("printer-" + (new Date()).getTime());
	// Create an iFrame with the new name.
	var jFrame = $( "<iframe name='" + strFrameName + "'>" );
	// Hide the frame (sort of) and attach to the body.
	jFrame
		.css( "width", "1px" )
		.css( "height", "1px" )
		.css( "position", "absolute" )
		.css( "left", "-9999px" )
		.appendTo( $( "body:first" ) )
	;
 
	// Get a FRAMES reference to the new frame.
	var objFrame = window.frames[ strFrameName ];
 
	// Get a reference to the DOM in the new frame.
	var objDoc = objFrame.document;
 
	// Grab all the style tags and copy to the new
	// document so that we capture look and feel of
	// the current document.
 
	// Create a temp document DIV to hold the style tags.
	// This is the only way I could find to get the style
	// tags into IE.
	var jStyleDiv = $( "<div>" ).append($( "style" ).clone());
	
	// Write the HTML for the document. In this, we will
	// write out the HTML of the current element.
	objDoc.open();
	objDoc.write( "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">" );
	objDoc.write( "<html>" );
	objDoc.write( "<body style='background-color:#ffffff;'>" );
	objDoc.write( "<head>" );
	objDoc.write( "<link rel='stylesheet' type='text/css' href='client/libs/css/site.css'>");
	objDoc.write( "<link rel='stylesheet' type='text/css' href='client/libs/css/cdis-print.css'>");
	objDoc.write( "<link rel='stylesheet' type='text/css' href='client/libs/css/jqplot/jquery.jqplot.min.css'>");
	objDoc.write( "<title>" );
	objDoc.write( document.title );
	objDoc.write( "</title>" );
	objDoc.write( jStyleDiv.html() );
	objDoc.write( "</head>" );

	// Typically, would just write out the html.	
	// objDoc.write( this.html() );

	objDoc.write( '<div class="cdis-print-container ui-widget ui-corner-all">');
	objDoc.write( '<div class="cdis-print-header">');
	objDoc.write( '<div class="logo">CDIS</div>');
	objDoc.write( '<div class="date">'+(new Date()).toISOString().slice(0, 10)+'</div>');
	objDoc.write( '</div>');
	objDoc.write( '<div class="cdis-print-body"><table class="cdis-print-display" border=0 style="width:600px;">');
	objDoc.write('<tr><td class="cdis-print-title">'+title+'</td></tr>');
	//objDoc.write('<tr> <td class="cdis-print-cell"> '+chartsImages.length+'---'+$(this).attr('id')+'</td></tr>');
	for(var j=0;j<chartsImages.length;j++){
    	var chartImage = chartsImages[j];
    	//alert(chartImage);
    	var rez = chartImage.replace("<img","<img style='width:100%;' ");
    	//alert(rez);
    	//objDoc.write('<tr> <td class="cdis-print-cell">');
    	objDoc.write('<tr><td>');
    	objDoc.write('<img src="'+rez+'">');
    	objDoc.write('</td></tr>');
    }
    
    objDoc.write('</table></div></div>');

	objDoc.write( "</body>" );
	objDoc.write( "</html>" );
	objDoc.close();
 
 	// 
	// When the iframe is completely loaded, print it.
	// This seemed worked in IE 9, but caused problems in FF.
	//
	// $(objFrame).load(function() {
	// 	objFrame.focus();
	// 	objFrame.print();
	// });

	//
	// This works in all supported browsers.
	// Note, might have to adjust time.
	//
	setTimeout(
		function() {
			objFrame.focus();
			objFrame.print();
		}, 750);
 

	// Have the frame remove itself in about a minute so that
	// we don't build up too many of these frames.
	setTimeout(
		function(){
			jFrame.empty();
			jFrame.remove();
		},
		(60 * 1000)
		);
}

jQuery.fn.printCDISSection = function(){
	if (this.size() > 1){
		this.eq( 0 ).print();
		return;
	} else if (!this.size()){
		return;
	}
	
	//can be one or can be more
    var charts = $(this).find('div.jqplot-target');
    var printDivs = $(this).find('div.jqplot-cdistarget');
    var chartsImages = [];
    var chartsUrlArr = [];
    for(var i=0;i<charts.length;i++){
    	var chart = charts[i];
    	html2canvas($(chart), {
			onrendered: function(canvas) {										
				var img = canvas.toDataURL("image/png");
				chartsUrlArr.push(img);
			}
    	});
    }
    
    var strFrameName = ("printer-" + (new Date()).getTime());
    
    setTimeout(function() {
    	
    	var jFrame = $( "<iframe name='" + strFrameName + "'>" );
    	jFrame
    		.css( "width", "1px" )
    		.css( "height", "1px" )
    		.css( "position", "absolute" )
    		.css( "left", "-9999px" )
    		.appendTo( $( "body:first" ) )
    	;
    	var objFrame = window.frames[ strFrameName ];
    	var objDoc = objFrame.document;
    	var jStyleDiv = $( "<div>" ).append($("style" ).clone());
     
    	objDoc.open();
    	objDoc.write( "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">" );
    	objDoc.write( "<html>" );
    	objDoc.write( "<body style='background-color:#ffffff;'>" );
    	objDoc.write( "<head>" );
    	objDoc.write( "<title>" );
    	objDoc.write( document.title );
    	objDoc.write( "</title>" );
    	objDoc.write( jStyleDiv.html() );
    	objDoc.write( "</head>" );
    	
    	
    	 if ($("link[media=print]").length > 0){
             $("link[media=print]").each( function() {
            	 objDoc.write("<link type='text/css' rel='stylesheet' href='" + $(this).attr("href") + "' media='print' />");
             });
         }else{
             $("link").each( function() {
            	 objDoc.write("<link type='text/css' rel='stylesheet' href='" + $(this).attr("href") + "' />");
             });
         }
    	
    	objDoc.write( '<div class="cdis-print-container ui-widget ui-corner-all"> \
        <div class="cdis-print-header"><div class="logo">CDIS</div></div>\
    		<table class="cdis-print-display" border="0" width="100%">');
    	//objDoc.write('<tr> <td class="cdis-print-graph-cell"> '+chartsUrlArr.length+'---'+$(this).attr('class')+'</td></tr>');
    	 for(var i=0;i<chartsUrlArr.length;i++){
    	    	var chartUrl = chartsUrlArr[i];
    	    	objDoc.write('<tr> <td class="cdis-print-graph-cell" align="center"><img src="'+chartUrl+'"/></td></tr>');
    	    }
        objDoc.write('</table>');
        objDoc.write( '<table class="cdis-print-display" border="0" width="100%">');
        for(var jj=0;jj<printDivs.length;jj++){
        	var printDiv = printDivs[jj];
        	objDoc.write('<tr> <td align="center">'+$(printDiv).html()+'</td></tr>');
        	//objDoc.write($(printDiv).html());
        }
        objDoc.write( '</table>');
    	objDoc.write( "</body>" );
    	objDoc.write( "</html>" );
    	objDoc.close();
    }, 2000);
    
    
	setTimeout(function() {var objFrame = window.frames[ strFrameName ];objFrame.focus();objFrame.print();}, 2750);

	// Have the frame remove itself in about a minute so that
	// we don't build up too many of these frames.
	setTimeout(function(){
		jFrame.empty();
		jFrame.remove();
		},
		(60 * 1000)
		);
}