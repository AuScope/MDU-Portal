package org.auscope.portal.server.web.service;

import org.apache.log4j.Logger;
import org.apache.commons.httpclient.HttpClient;
import org.w3c.dom.Document;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.auscope.portal.server.util.Util;
import org.auscope.portal.csw.*;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

import javax.servlet.http.HttpServletRequest;

/**
 * Provides some utility methods for accessing data from multiple CSW services
 */
@Service
public class CSWService {
    /**
     * A utility class that groups useful information about a single cache for a single service url
     */
    private class UrlCache implements Runnable{
    	private CSWRecord[] cache;
    	private long lastTimeUpdated;
    	private CSWServiceItem serviceItem;
    	
    	//These are cached for the run method
    	private HttpServiceCaller serviceCaller;
        private Util util;
        private Logger logger;
        
        //This isn't perfect, but it does the job we need it to
        //This will stop multiple updates on different threads from running at the same time
        private volatile boolean updateInProgress;
    	
    	public UrlCache(CSWServiceItem serviceItem, HttpServiceCaller serviceCaller, Util util) {
    		this.serviceItem = serviceItem;
    		this.cache = new CSWRecord[0];
    		this.serviceCaller = serviceCaller;
    		this.util = util;
    		this.logger = Logger.getLogger(getClass());
    	}
    	
    	public synchronized void setCache(CSWRecord[] cache) {
    		this.cache = cache;
    		this.lastTimeUpdated = System.currentTimeMillis();
    	}
    	
    	public synchronized CSWRecord[] getCache() {
    		return this.cache;
    	}
    	
    	public synchronized long getLastTimeUpdated() {
    		return this.lastTimeUpdated;
    	}
    	
    	public CSWServiceItem getServiceItem() {
    		return this.serviceItem;
    	}
    	
    	public boolean getUpdateInProgress(){
    		return updateInProgress;
    	}
    	
    	public void setUpdateInProgress(boolean updateInProgress){
    		this.updateInProgress = updateInProgress;
    	}
    	
    	
    	public void run() {
    		try {
            	//This section is all "threadsafe" because it uses all local variables (or rather its methods do)
        		//It might be useful to mark this behaviour in the definition of the objects
                ICSWMethodMaker getRecordsMethod = new CSWMethodMakerGetDataRecords(this.serviceItem.getServiceUrl());
                HttpClient newClient = serviceCaller.getHttpClient();
                String methodResponse =  serviceCaller.getMethodResponseAsString(getRecordsMethod.makeMethod(), newClient);
                Document document = util.buildDomFromString(methodResponse);
                CSWRecord[] tempRecords = new CSWGetRecordResponse(document).getCSWRecords();
                
                //This is where we need to avoid race conditions 
                this.setCache(tempRecords);
               
            } catch (Exception e) {
                logger.error(e);
            }
            finally {
            	//It is possible that another thread can startup before this thread exits completely
            	//But it won't be a problem. The main issue is to stop MULTIPLE updates firing at once
            	//and hammering an external resource, at this point all comms with the external source have finished
            	this.updateInProgress = false;
            }
    	}
    }
	
    private static final int UPDATE_INTERVAL = 300000;

    /**
     * Each element in this list represents a cache retrieved from a single serviceURL
     */
    private UrlCache[]  cache;
    private CSWThreadExecutor executor;
    private HttpServiceCaller serviceCaller;
    private Util util;

    @Autowired
    public CSWService(CSWThreadExecutor executor,
    				  HttpServiceCaller serviceCaller,
                      Util util,
                      @Qualifier(value = "cswServiceList") ArrayList cswServiceList) throws Exception {
        this.executor = executor;
        this.serviceCaller = serviceCaller;
        this.util = util;
        
        this.cache = new UrlCache[cswServiceList.size()];
    	for (int i = 0; i < cswServiceList.size(); i++) {
    		cache[i] = new UrlCache((CSWServiceItem) cswServiceList.get(i), serviceCaller, util);
    	}
    }
    
    /**
     * Starts a new update thread for each service url that has no records OR hasn't been updated in the last UPDATE_INTERVAL 
     * @throws Exception
     */
    public void updateRecordsInBackground() throws Exception {
    	//Update every service url
    	for (int i = 0; i < cache.length; i++) {
    		UrlCache currentCache = cache[i];
    		
            // Update the cache if older that 5 minutes (and it's not already updating)
            if (!currentCache.getUpdateInProgress() && (System.currentTimeMillis() - currentCache.getLastTimeUpdated() > UPDATE_INTERVAL)) {
            	currentCache.setUpdateInProgress(true);
                executor.execute(currentCache);
            }
    	}
    }

    /**
     * Returns every record in this cache (Even records with empty service Url's)
     
     * @return
     * @throws Exception
     */
    public CSWRecord[] getAllRecords() throws Exception {
    	return getFilteredRecords(null,null, true);
    }
    
    /**
     * Returns only WMS data records
     * @return
     * @throws Exception
     */
    public CSWRecord[] getWMSRecords() throws Exception {
    	return getFilteredRecords("WMS",null, false);
    }

    /**
     * Returns only WFS data records
     * @return
     * @throws Exception
     */
    public CSWRecord[] getWFSRecords() throws Exception {
    	return getFilteredRecords("WFS",null, false);
    }
    
    /**
     * Returns only WFS data records filtered by featureTypeName (against CSWRecord.getOnlineResourceName)
     * @return
     * @throws Exception
     */
    public CSWRecord[] getWFSRecordsForTypename(String featureTypeName) throws Exception {
    	return getFilteredRecords("WFS",featureTypeName, false);
    } 

    /**
     * Returns a filtered list of records from this cache
     * @param featureTypeName
     * @param dataRecordType either "WFS" or "WMS" or null (which will fetch everything)
     * @param featureTypeName set to null otherwise it will filter results according to record.getOnlineResourceName
     * @param includeRestrictedRecords NOTE - This is only a temporary placeholder for where the CSW Service security will sit
     * @return
     * @throws Exception
     */
    private synchronized CSWRecord[] getFilteredRecords(String dataRecordType, String featureTypeName, boolean includeEmptyServiceUrl) throws Exception {
        ArrayList<CSWRecord> records = new ArrayList<CSWRecord>();

        //Iterate EVERY record for EVERY service url
        for (int i = 0; i < cache.length; i++) {
        	for(CSWRecord rec : cache[i].getCache()) {
        		if(rec.getOnlineResourceProtocol() != null) {
        			if ((dataRecordType == null || rec.getOnlineResourceProtocol().contains(dataRecordType)) && 
        					(includeEmptyServiceUrl || !rec.getServiceUrl().equals("")) && 
	                        (featureTypeName == null || featureTypeName.equals(rec.getOnlineResourceName()))) {
	                		records.add(rec);
        			}
        		}
        	}
        }

        return records.toArray(new CSWRecord[records.size()]);
    }
}