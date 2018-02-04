/*generic functions*/

function loadTemplate(pageName,callBack){
	var ua = window.navigator.userAgent;
	var msie1 = ua.indexOf("Edge");
	var msie2 = ua.indexOf(".NET");
	var msie3 = ua.indexOf("MSIE");
	if((msie1 >= 0) || (msie2 >= 0)  || (msie3 >= 0)){
		$("<div>",{id:"dialog-msie"}).appendTo($("body")).html("<p>CDIS application is not supported using Internet Explorer or Edge Browser.</p><p>Please use <b>Chrome</b>  or <b>Firefox</b> browser.</p><p>If Chrome of Firefox are not installed on your computer please contact your system administrator.</p>");
		$("#dialog-msie").dialog({
			autoOpen: true,
		    resizable: false,
		    height: 350,
		    width: 400,
		    modal: true,
		    buttons: {
		      OK: function() {
		    	  $( this ).dialog( "close" );
		        }
		    },
		    close: function() { }
		  });
	}else{
		$body.append($("<div>",{class:"cdismodal"}).append($("<div>",{class:"modal-span"}).text("CDIS Loading..."))).addClass("loading");
		if(callBack == null){
			$( "#wraper").load( "client/templates/"+pageName+".html");
		}else{
			$( "#wraper").load("client/templates/"+pageName+".html", callBack);
		}
		setTimeout(function(){
			$body.removeClass("loading");
			$(".cdismodal").remove();
		},500);
	}
}

function logout(){
	logoutUser(sid);
}

function logoutLocal(){
	logoutUserLocal(sid);
}

function escapeRegExp(string) {
    return string.replace(/([.*+?^=!:${}()|\[\]\/\\])/g, "\\$1");
}

function replaceAll(string, find, replace) {
  return string.replace(new RegExp(escapeRegExp(find), 'g'), replace);
}


Array.prototype.sum = function(selector) {
    if (typeof selector !== 'function') {
        selector = function(item) {
            return item;
        };
    }
    var sum = 0;
    for (var i = 0; i < this.length; i++) {
        sum += parseFloat(selector(this[i]));
    }
    return sum;
};

Array.prototype.max = function() {
	  return Math.max.apply(null, this);
	};

Array.prototype.min = function() {
  return Math.min.apply(null, this);
};


Number.prototype.trimNum = function(places,rounding){
	(rounding != 'floor' && rounding != 'ceil') ? rounding = 'round' : rounding = rounding;
	var result, num = this, multiplier = Math.pow( 10,places );
	result = Math[rounding](num * multiplier) / multiplier;
	return Number( result );
};

function capitalizeFirstLetter(string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
}




/* tooltip function*/
//Use a closure to keep vars out of global scope
/*
(function () {
var ID = "tooltip_cdis", CLS_ON = "tooltip_ON", FOLLOW = true,
DATA = "_tooltip", OFFSET_X = 20, OFFSET_Y = 10,
showAt = function (e) {
    var ntop = e.pageY + OFFSET_Y, nleft = e.pageX + OFFSET_X;
	  //var ntop =  OFFSET_Y, nleft = OFFSET_X;
    $("#" + ID).html($(e.target).data(DATA)).css({
        position: "absolute", top: ntop, left: nleft
    }).show();
};
$(document).on("mouseenter", "*[title]", function (e) {
    $(this).data(DATA, $(this).attr("title"));
    $(this).removeAttr("title").addClass(CLS_ON);
    $("<div id='" + ID + "' />").appendTo("body");
    showAt(e);
});
$(document).on("mouseleave", "." + CLS_ON, function (e) {
    $(this).attr("title", $(this).data(DATA)).removeClass(CLS_ON);
    $("#" + ID).remove();
});
if (FOLLOW) { $(document).on("mousemove", "." + CLS_ON, showAt); }
}());

*/