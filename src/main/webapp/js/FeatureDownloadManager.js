/*
	Feature Download Manager
	
	A class for dowloading features from a given URL. This class handles all querying for record counts, display
	of modal question's and downloading the actual records
*/
FeatureDownloadManager = function(serviceURL, recordCountURL, recordFetchURL, filterParams, map) {
	this.serviceURL = serviceURL;
	this.filterParams = filterParams;
	this.recordCountURL = recordCountURL;
	this.recordFetchURL = recordFetchURL;
	this.map = map;
	this.currentBoundingBox = null;
};

FeatureDownloadManager.prototype.downloadFinishedHandler = null;
FeatureDownloadManager.prototype.downloadErrorHandler = null;
FeatureDownloadManager.prototype.downloadCancelledHandler = null;
FeatureDownloadManager.prototype.featureSetSizeThreshold = 200;


FeatureDownloadManager.prototype.handleDownloadFinish = function(data, responseCode) {
	if (responseCode == 200) {
		this.downloadFinishedHandler(data, responseCode);
    } else {
    	this.downloadErrorHandler(data, responseCode);
    }
}

FeatureDownloadManager.prototype.doDownload = function (boundingBox) {
	var url = this.recordFetchURL + '?' + this.filterParams + '&serviceUrl=' + this.serviceURL;
	
	if (boundingBox != null && boundingBox != undefined && boundingBox.length > 0) 
		url += '&boundingBox=' + boundingBox;
	
	var callingInstance = this;
	GDownloadUrl(url, function (data, responseCode) {
		callingInstance.handleDownloadFinish(data, responseCode);
	});
}

FeatureDownloadManager.prototype.doCount = function(data, responseCode, alreadyPrompted) {
	if (responseCode == 200) {
        var jsonResponse = eval('(' + data + ')');
        if (jsonResponse[0] > this.featureSetSizeThreshold) {
        	var win = null;
        	var callingInstance = this;
        	
        	//If we have already prompted the user and they selected to only get the visible records 
        	//AND there are still too many records, lets be 'smart' about how we proceed
        	if (alreadyPrompted) {
        		Ext.MessageBox.show({
        			buttons:{yes:'Download Visible', no:'Abort Download'},
        			fn:function (buttonId) {
	        			if (buttonId == 'yes') {
	        				callingInstance.doDownload(callingInstance.currentBoundingBox);
	        			} else if (buttonId == 'no') {
	        				callingInstance.downloadCancelledHandler();
	        			} 
	        		},
	        		modal:true,
	        		msg: '<p>There will still be ' + jsonResponse[0] + ' features visible. Would you still like to download the visible feature set?</p><br/><p>Alternatively you can cancel this download, adjust your zoom level and try again.</p>',
	        		title:'Warning: Large feature set'
        		});
        	}else {
	        	Ext.MessageBox.show({
	        		buttons:{yes:'Download All', no:'Download Visible', cancel:'Abort Download'},
	        		fn:function (buttonId) {
	        			if (buttonId == 'yes') {
	        				callingInstance.doDownload(callingInstance.currentBoundingBox);
	        			} else if (buttonId == 'no') {
	        				var mapBounds = callingInstance.map.getBounds();
	            			var sw = mapBounds.getSouthWest();
	            			var ne = mapBounds.getNorthEast();
	            			var center = mapBounds.getCenter();
	            			
	            			var adjustedSWLng = sw.lng(); 
	            			var adjustedNELng = ne.lng();
	            			
	            			//this is so we can fetch data when our bbox is crossing the anti meridian
	            			//Otherwise our bbox wraps around the WRONG side of the planet
	            			if (adjustedSWLng <= 0 && adjustedNELng >= 0 || 
	            				adjustedSWLng >= 0 && adjustedNELng <= 0) {
	            				adjustedSWLng = (sw.lng() < 0) ? (180 - sw.lng()) : sw.lng();
	            				adjustedNELng = (ne.lng() < 0) ? (180 - ne.lng()) : ne.lng();
	            			}
	            			
	            			callingInstance.startDownload(Math.min(sw.lat(), ne.lat()) + ',' +
	            										  Math.min(adjustedSWLng, adjustedNELng) + ',' +
	            										  Math.max(sw.lat(), ne.lat()) + ',' +
	            										  Math.max(adjustedSWLng, adjustedNELng), 
	            										  true);
	        			} else if (buttonId == 'cancel') {
	        				callingInstance.downloadCancelledHandler();
	        			}
	        		},
	        		modal:true,
	        		msg:'You are about to fetch ' + jsonResponse[0] + ' features, doing so could make the portal run extremely slowly. Would you like to download only the visible markers instead?',
	        		title:'Warning: Large feature set'
	        	});
        	}
        	
        } else {
        	//If we have an acceptable number of records, this is how we shall proceed
        	this.doDownload(this.currentBoundingBox);
        }
    } else {
    	//If the count download fails, 
    	this.downloadErrorHandler(data, responseCode);
    }
}

FeatureDownloadManager.prototype.startDownload = function(boundingBox, alreadyPrompted) {
	this.currentBoundingBox = boundingBox;
	
	if (alreadyPrompted == null || alreadyPrompted == undefined)
		alreadyPrompted = false;
	
	//Firstly discern how many records are available, this will affect how we proceed
	var url = this.recordCountURL + '?' + this.filterParams + '&serviceUrl=' + this.serviceURL;
	if (boundingBox != null && boundingBox != undefined && boundingBox.length > 0) 
		url += '&boundingBox=' + boundingBox;

	//If we have a count function, go through the motions
	//Otherwise just download the URL
	if (!this.recordCountURL || this.recordCountURL.length == 0) {
		this.doDownload(this.currentBoundingBox);
	} else {
		var callingInstance = this;
	    GDownloadUrl(url, function (data, responseCode) {
	    	callingInstance.doCount(data, responseCode, alreadyPrompted);
	    });
	}
}
