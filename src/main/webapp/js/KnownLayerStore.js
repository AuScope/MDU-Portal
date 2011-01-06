/**
 * An extension of a normal JSON store that makes it specialize into storing and retrieving CSWRecord's
 */
KnownLayerStore = function(url) {
	var conn = new Ext.data.Connection({
		url: url, 
		timeout:180000
	});
	
	KnownLayerStore.superclass.constructor.call(this, {
		proxy			: new Ext.data.HttpProxy(conn),
		storeId			: 'knownLayerRecordStore',
		root			: 'records',
		id				: 'featureTypeName',
		successProperty	: 'success',
		messageProperty : 'msg',
		fields			: [
		    'featureTypeName',
		    'type',
		    'hidden',
		    'layerName',
		    'title',
		    'descriptiveKeyword',
		    'styleName',
		    'description',
		    'relatedFeatureTypeNames',
		    'id',
		    'proxyUrl',
		    'proxyRecordCountUrl',
		    'iconUrl',
		    'iconAnchor',
		    'infoWindowAnchor',
		    'iconSize',
		    'disableBboxFiltering'
		]
	});
};


Ext.extend(KnownLayerStore, Ext.data.JsonStore, {

	/**
	 * Clears this store and then copies all records from sourceKnownLayerStore into this store.
	 * 
	 * sourceKnownLayerStore : The source KnownLayerStore
	 * filterFunc : [Optional] A function(KnownLayer) that will be called on each record copied, if the 
	 *              the function returns true, the record will be copied
	 */
	copyFrom	: function(sourceKnownLayerStore, filterFunc) {
		this.removeAll();
		
		var recordsToCopy = sourceKnownLayerStore.getRange();		
		
		if (filterFunc) {
			var recordsToAdd = [];
			
			for (var i = 0; i < recordsToCopy.length; i++) {
				var knownLayerRecord = new KnownLayerRecord(recordsToCopy[i]);
					
				if (!filterFunc(knownLayerRecord)) {
					continue;
				}
					
				recordsToAdd.push(recordsToCopy[i]);
			}
			
			this.add(recordsToAdd);
		} else {
			this.add(recordsToCopy);
		}
	},
	
	
	/**
	 * Gets a KnownLayerRecord object representation of the record at the specified location
	 */
    getKnownLayerAt		: function(index) {
		var dataRecord = KnownLayerStore.superclass.getAt.call(this,index);
		if (!dataRecord) {
			return null;
		}
		
		return new KnownLayerRecord(dataRecord);
	},

	/**
	 * Gets a KnownLayerRecord object representation of the record with the specified id
	 */
	getKnownLayerById	: function(id) {
		var dataRecord = KnownLayerStore.superclass.getById.call(this, id);
		if (!dataRecord) {
			return null;
		}
		
		return new KnownLayerRecord(dataRecord);
	}
});