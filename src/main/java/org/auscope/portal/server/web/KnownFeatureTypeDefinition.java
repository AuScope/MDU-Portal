package org.auscope.portal.server.web;

/**
 * User: Mathew Wyatt
 * Date: 27/08/2009
 * Time: 11:08:17 AM
 */
public class KnownFeatureTypeDefinition {
    private String featureTypeName;
    private String displayName;
    private String description;
    private String proxyRecordFetchUrl;
    private String proxyRecordCountUrl;
    private String iconUrl;
    private boolean ignored;

    public KnownFeatureTypeDefinition(String featureTypeName, boolean ignored) {
    	this.featureTypeName = featureTypeName;
    	this.ignored = ignored;
    }
    
    public KnownFeatureTypeDefinition(String featureTypeName, String displayName, String description, String proxyRecordFetchUrl, String proxyRecordCountUrl, String iconUrl) {
        this.featureTypeName = featureTypeName;
        this.displayName = displayName;
        this.description = description;
        this.proxyRecordFetchUrl = proxyRecordFetchUrl;
        this.proxyRecordCountUrl = proxyRecordCountUrl;
        this.iconUrl = iconUrl;
        this.ignored = false;
    }
    
    /**
     * Ignored feature types are NOT displayed to the user. This allows specific feature types to be excluded for whatever reason
     * @return
     */
    public boolean getIgnored() {
    	return ignored;
    }
    
    public void setIgnored(boolean ignored) {
    	this.ignored = ignored;
    }

    public String getFeatureTypeName() {
        return featureTypeName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Get the URL that is used to actual fetch the records for a URL
     * @return
     */
    public String getProxyRecordFetchUrl() {
        return proxyRecordFetchUrl;
    }
    
    /**
     * Get the URL that is used to count the number of records for this feature (Can be null / empty)
     * @return
     */
    public String getProxyRecordCountUrl() {
        return proxyRecordCountUrl;
    }

    public String getIconUrl() {
        return iconUrl;
    }
    
    public int hashCode() {
    	return featureTypeName.hashCode();
    }
    
    public boolean equals(Object obj) {
    	if (obj instanceof KnownFeatureTypeDefinition)
    		return featureTypeName.equals(((KnownFeatureTypeDefinition) obj).featureTypeName);
    	
    	return super.equals(obj);	
    }
}