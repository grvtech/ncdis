loadTemplate(page);


//get The application cache object
var appCache = window.applicationCache;


function getCacheStatus(){
//The <a href="http://dev.w3.org/html5/spec/single-page.html#concept-appcache-status" target="_blank">status</a> object returns a constant that represent the current state of the cache:
	switch (appCache.status) {
	  case appCache.UNCACHED: // UNCACHED == 0
	    return 'UNCACHED';
	    break;
	  case appCache.IDLE: // IDLE == 1
	    return 'IDLE';
	    break;
	  case appCache.CHECKING: // CHECKING == 2
	    return 'CHECKING';
	    break;
	  case appCache.DOWNLOADING: // DOWNLOADING == 3
	    return 'DOWNLOADING';
	    break;
	  case appCache.UPDATEREADY:  // UPDATEREADY == 4
	    return 'UPDATEREADY';
	    break;
	  case appCache.OBSOLETE: // OBSOLETE == 5
	    return 'OBSOLETE';
	    break;
	  default:
	    return 'UKNOWN CACHE STATUS';
	    break;
	};
}

appCache.addEventListener('updateready', function(e) {
	this.swapCache();
});

