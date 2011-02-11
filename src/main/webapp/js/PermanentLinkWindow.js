/**
 * A basic window to show a permanent link to the current map state.
 * 
 * It consists of a simple warning and the link itself
 * 
 * mapStateSerializer - MapStateSerializer - The map state to be 'linked' to
 */
PermanentLinkWindow = function(mapStateSerializer) {
    
    //Rewrite our current URL with the new state info (leave the other URL params intact)
    var urlParams = Ext.urlDecode(location.search.substring(1));
    urlParams.state = mapStateSerializer.serialize();
    var linkedUrl = location.href.split('?')[0];
    linkedUrl = Ext.urlAppend(linkedUrl, Ext.urlEncode(urlParams));
    
    PermanentLinkWindow.superclass.constructor.call(this, {
        id : 'perma-link-window',
        title: 'Permanent Link',
        autoDestroy : true,
        width : 500,
        autoHeight : true,
        layout : 'auto',
        defaultButton : 'perma-link-window-url-field',
        items : [{
            xtype : 'panel',
            style : {
                font : '12px tahoma,arial,helvetica,sans-serif'
            },
            html : '<p><b>Warning:</b></p>' + 
                    '<p>This link will only save your selected layers and queries. The actual data received and displayed may be subject to change</p></br>' 
        }, {
            xtype : 'form',
            layout : 'form',
            autoHeight : true,
            items : [{
                xtype : 'textfield',
                id : 'perma-link-window-url-field',
                anchor : '100%',
                fieldLabel : 'Paste this link',
                labelStyle: 'font-weight:bold;',
                value : linkedUrl,
                readOnly : true
            }]
            
        }]
    });
};

Ext.extend(PermanentLinkWindow, Ext.Window, {
    
});

function permaLinkClickHandler() {
    //Only open a single link window
    var permaWindow = Ext.getCmp('perma-link-window');
    if (!permaWindow) {
        var activeLayersStore = Ext.StoreMgr.get('active-layers-store');
        var serializer = new MapStateSerializer();
        
        serializer.addMapState(map);
        serializer.addActiveLayers(activeLayersStore);
        
        permaWindow = new PermanentLinkWindow(serializer);
        permaWindow.show(null, function() {
            var textField = Ext.getCmp('perma-link-window-url-field');
            if (textField) {
                textField.focus(true);
            }
        });
    } else {
        permaWindow.center();
    }
    
  
    
};