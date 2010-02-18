
KMLParser = function(kml) {
    this.rootNode = GXml.parse(kml).documentElement;
    this.markers = [];
    this.overlays = [];
};

//Given a series of space seperated CSV tuples, return a list of GLatLng
KMLParser.prototype.generateCoordList = function(coordsAsString) {
	var coordinateList = coordsAsString.split(' ');
	var parsedCoordList = [];
    for (var i = 0; i < coordinateList.length; i++) {
    	if (coordinateList[i].length == 0)
    		continue;
    	
    	var coords = coordinateList[i].split(',');
    	
    	if (coords.length == 0)
    		continue;
    	
    	parsedCoordList.push(new GLatLng(parseFloat(coords[1]), parseFloat(coords[0])));
    }
    
    return parsedCoordList;
}

KMLParser.prototype.parseLineString = function(placemarkNode) {
	var name = GXml.value(placemarkNode.getElementsByTagName("name")[0]);
    var description = GXml.value(placemarkNode.getElementsByTagName("description")[0]);
   
    var parsedCoordList = this.generateCoordList(GXml.value(placemarkNode.getElementsByTagName("coordinates")[0]));
    if (parsedCoordList.length == 0)
    	return null;
    
    var lineString = new GPolyline(parsedCoordList, '#FF0000',3, 1, undefined);
    
    lineString.description = description;
    lineString.title = name;
    
    return lineString;
}

//Given a root placemark node attempt to parse it as a single point and return it
//Returns a single GPolygon
KMLParser.prototype.parsePolygon = function(placemarkNode) {
	
	var name = GXml.value(placemarkNode.getElementsByTagName("name")[0]);
    var description = GXml.value(placemarkNode.getElementsByTagName("description")[0]);
   
    var parsedCoordList = this.generateCoordList(GXml.value(placemarkNode.getElementsByTagName("coordinates")[0]));
    if (parsedCoordList.length == 0)
    	return null;
    	
    var polygon = new GPolygon(parsedCoordList,undefined, undefined, 0.7,undefined, 0.6);
    polygon.description = description;
    polygon.title = name;
    
    return polygon;
};

//Given a root placemark node attempt to parse it as a single point and return it
//Returns a single GMarker
KMLParser.prototype.parsePoint = function(icon, placemarkNode) {
	// var name = GXml.value(placemarks[i].selectSingleNode(".//*[local-name() = 'name']"));
    var name = GXml.value(placemarkNode.getElementsByTagName("name")[0]);

    // var description = GXml.value(placemarks[i].selectSingleNode(".//*[local-name() = 'description']"));
    var description = GXml.value(placemarkNode.getElementsByTagName("description")[0]);

    // var coordinates = GXml.value(placemarks[i].selectSingleNode(".//*[local-name() = 'coordinates']")).split(',');
    var coordinates = GXml.value(placemarkNode.getElementsByTagName("coordinates")[0]).split(',');

    // We do not want placemarks without coordinates
    if (coordinates == "")
        return null;
    
    //iconlast = GXml.value(placemarks[i].selectSingleNode(".//*[local-name() = 'Icon']/*[local-name() = 'href']")).split(',');
    var lon = coordinates[0];
    var lat = coordinates[1];
    var z = coordinates[2];

    var point = new GLatLng(parseFloat(lat), parseFloat(lon));

    var marker = new GMarker(point, {icon: icon});
    marker.description = description;
    marker.title = name;

    return marker;
};

KMLParser.prototype.makeMarkers = function(icon, markerHandler) {
    
    var markers = [];
    // var placemarks = this.rootNode.selectNodes(".//*[local-name() = 'Placemark']");
    // alert(placemarks[0].selectSingleNode(".//*[local-name() = 'Placemark']").text);
    
    var placemarks = this.rootNode.getElementsByTagName("Placemark");
    
    try {
        for(i = 0; i < placemarks.length; i++) {
        	
        	var mapItem = null;
        	
        	//Parse a polygon
        	if (placemarks[i].getElementsByTagName("Polygon").length > 0) {
        		mapItem = this.parsePolygon(placemarks[i]);
        		if (mapItem == null)
        			continue;
        		
        		//if there are some custom properties that need to be set
                if(markerHandler)
                    markerHandler(mapItem);

                this.overlays.push(mapItem);
        	} else if (placemarks[i].getElementsByTagName("LineString").length > 0) {
        		mapItem = this.parseLineString(placemarks[i]);
        		if (mapItem == null)
        			continue;
        		
        		if(markerHandler)
                    markerHandler(mapItem);

                this.overlays.push(mapItem);
        	} else { //otherwise we parse a point
        		mapItem = this.parsePoint(icon, placemarks[i]);
        		if (mapItem == null)
        			continue;
        		
        		//if there are some custom properties that need to be set
                if(markerHandler)
                    markerHandler(mapItem);

                this.markers.push(mapItem);
        	}
        }
    } catch(e) {alert(e);}

    return markers;
};


