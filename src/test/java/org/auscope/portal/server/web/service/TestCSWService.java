package org.auscope.portal.server.web.service;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;

import org.auscope.portal.csw.CSWThreadExecutor;
import org.auscope.portal.server.util.PortalPropertyPlaceholderConfigurer;
import org.auscope.portal.server.util.Util;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
/**
 * User: Mathew Wyatt
 * Date: 20/08/2009
 * @version $Id$
 */
public class TestCSWService {
	//determines the size of the test + congestion
	static final int CONCURRENT_THREADS_TO_RUN = 20;
	
	//How many of the fake CSW services will require authorization
	static final String AUTHORIZED_ROLE = "ROLE_TEST";
	static final int AUTHORIZED_CSW_COUNT = 8;
	
	//These determine the correct numbers for a single read of the test file
	static final int RECORD_COUNT_WMS = 6;
	static final int RECORD_COUNT_WFS = 41;
	static final int RECORD_COUNT_TOTAL = 53;
	static final int RECORD_COUNT_ERMINE_RECORDS = 2;
	
    /**
     * JMock context
     */
    private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};

    /**
     * Main object we are testing
     */
    private CSWService cswService;

    /**
     * Mock httpService caller
     */
    private HttpServiceCaller httpServiceCaller = context.mock(HttpServiceCaller.class);
    
    private HttpServletRequest httpServletRequest = context.mock(HttpServletRequest.class);
    
     /**
     * Mock property configurer
     */
    private PortalPropertyPlaceholderConfigurer propertyConfigurer = context.mock(PortalPropertyPlaceholderConfigurer.class);
    
    private CSWThreadExecutor executor;

    /**
     * Mock Util
     * @throws Exception
     */
     private Util util = new Util();//context.mock(Util.class);

     @Before
     public void initialize() throws Exception {
    	 
    	this.executor = new CSWThreadExecutor();
    	
      	//Create our service list (make the first X requiring authorization)
      	java.util.ArrayList serviceUrlList = new java.util.ArrayList(CONCURRENT_THREADS_TO_RUN);
      	for (int i = 0; i < CONCURRENT_THREADS_TO_RUN; i++){
      		if (i < AUTHORIZED_CSW_COUNT) {
      			serviceUrlList.add(new CSWServiceItem("http://localhost")); //This constructor has no user restrictions
      		} else {
      			ArrayList tempList = new ArrayList();
      			tempList.add(AUTHORIZED_ROLE);
      			serviceUrlList.add(new CSWServiceItem("http://localhost", "http://localhost", tempList)); //This one is restricted
      		}
      	}
      	
      	this.cswService = new CSWService(executor, httpServiceCaller, util, serviceUrlList);
     }
     
     /**
      * Tests whether an unauthorized user returns the correct number of records
      * @throws Exception
      */
     @Test
     public void testUnauthorizedRecordUpdate() throws Exception {
    	 final String docString = org.auscope.portal.Util.loadXML("src/test/resources/cswRecordResponse.xml");

         context.checking(new Expectations() {{
        	 
             exactly(CONCURRENT_THREADS_TO_RUN).of(httpServiceCaller).getHttpClient();
             exactly(CONCURRENT_THREADS_TO_RUN).of(httpServiceCaller).getMethodResponseAsString(with(any(HttpMethodBase.class)), with(any(HttpClient.class)));will(returnValue(docString));
             
             
             atLeast(1).of(httpServletRequest).isUserInRole(AUTHORIZED_ROLE); will(returnValue(false));
         }});

         //We call this twice to test that an update wont commence whilst
         //an update for a service is already running (if it does it will trigger too many calls to getHttpClient
         this.cswService.updateRecordsInBackground();
         this.cswService.updateRecordsInBackground();
         try {
         	executor.getExecutorService().shutdown();
         	executor.getExecutorService().awaitTermination(180, TimeUnit.SECONDS);
         }
         catch (Exception ex) {
         	executor.getExecutorService().shutdownNow();
         	Assert.fail("Exception whilst waiting for update to finish " + ex.getMessage());
         }
       
       //Now lets do the above tests with an unauthorized user
       Assert.assertEquals(RECORD_COUNT_TOTAL * (AUTHORIZED_CSW_COUNT), this.cswService.getAllRecords(httpServletRequest).length);
       Assert.assertEquals(RECORD_COUNT_WMS * (AUTHORIZED_CSW_COUNT), this.cswService.getWMSRecords(httpServletRequest).length);
       Assert.assertEquals(RECORD_COUNT_WFS * (AUTHORIZED_CSW_COUNT), this.cswService.getWFSRecords(httpServletRequest).length);
       Assert.assertEquals(RECORD_COUNT_ERMINE_RECORDS * (AUTHORIZED_CSW_COUNT), this.cswService.getWFSRecordsForTypename("er:Mine", httpServletRequest).length);
     }
     
     /**
      * Tests whether an authorized user returns the correct list of records
      * @throws Exception
      */
     @Test
     public void testAuthorizedRecordUpdate() throws Exception {
         final String docString = org.auscope.portal.Util.loadXML("src/test/resources/cswRecordResponse.xml");

         context.checking(new Expectations() {{
             exactly(CONCURRENT_THREADS_TO_RUN).of(httpServiceCaller).getHttpClient();
             exactly(CONCURRENT_THREADS_TO_RUN).of(httpServiceCaller).getMethodResponseAsString(with(any(HttpMethodBase.class)), with(any(HttpClient.class)));will(returnValue(docString));
             
             atLeast(1).of(httpServletRequest).isUserInRole(AUTHORIZED_ROLE); will(returnValue(true));
         }});

         //We call this twice to test that an update wont commence whilst
         //an update for a service is already running (if it does it will trigger too many calls to getHttpClient
         this.cswService.updateRecordsInBackground();
         this.cswService.updateRecordsInBackground();
         try {
         	executor.getExecutorService().shutdown();
         	executor.getExecutorService().awaitTermination(180, TimeUnit.SECONDS);
         }
         catch (Exception ex) {
         	executor.getExecutorService().shutdownNow();
         	Assert.fail("Exception whilst waiting for update to finish " + ex.getMessage());
         }
         
       //in the response we loaded from the text file it contains 53 records
       Assert.assertEquals(RECORD_COUNT_TOTAL * CONCURRENT_THREADS_TO_RUN, this.cswService.getAllRecords(httpServletRequest).length);
       
       //in the response we loaded from the text file it contains 6 WMS records
       Assert.assertEquals(RECORD_COUNT_WMS * CONCURRENT_THREADS_TO_RUN, this.cswService.getWMSRecords(httpServletRequest).length);
       
       //in the response we loaded from the text file it contains 41 WFS records
       Assert.assertEquals(RECORD_COUNT_WFS * CONCURRENT_THREADS_TO_RUN, this.cswService.getWFSRecords(httpServletRequest).length);
       
       //in the response we loaded from the text file it contains 2 er:Mine records
       Assert.assertEquals(RECORD_COUNT_ERMINE_RECORDS * CONCURRENT_THREADS_TO_RUN, this.cswService.getWFSRecordsForTypename("er:Mine", httpServletRequest).length);
     }
}
