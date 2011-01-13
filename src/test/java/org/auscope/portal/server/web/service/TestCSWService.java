package org.auscope.portal.server.web.service;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.auscope.portal.csw.CSWThreadExecutor;
import org.auscope.portal.server.util.Util;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.jmock.lib.action.ReturnValueAction;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * User: Mathew Wyatt
 * Date: 20/08/2009
 * @version $Id$
 */
public class TestCSWService {

	//determines the size of the test + congestion
	static final int CONCURRENT_THREADS_TO_RUN = 10;

	//How many of the fake CSW services will require authorization
	static final String AUTHORIZED_ROLE = "ROLE_TEST";
	static final int AUTHORIZED_CSW_COUNT = 7;
	
	//These determine the correct numbers for a single read of the test file
	
	static final int RECORD_COUNT_WMS = 2;
	static final int RECORD_COUNT_WFS = 12;
	static final int RECORD_COUNT_TOTAL = 15;
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
     * Thread executor
     */
    private CSWThreadExecutor threadExecutor;

    /**
     * Mock Util
     * @throws Exception
     */
     private Util util = new Util();//context.mock(Util.class);

    @Before
    public void setup() throws Exception {

    	this.threadExecutor = new CSWThreadExecutor();

      	//Create our service list
      	ArrayList<CSWServiceItem> serviceUrlList = new ArrayList<CSWServiceItem>(CONCURRENT_THREADS_TO_RUN);
      	for (int i = 0; i < CONCURRENT_THREADS_TO_RUN; i++){
      		if (i < AUTHORIZED_CSW_COUNT) {
      			serviceUrlList.add(new CSWServiceItem("http://localhost")); //This constructor has no user restrictions
      		} else {
      			ArrayList tempList = new ArrayList();
      			tempList.add(AUTHORIZED_ROLE);
      			serviceUrlList.add(new CSWServiceItem("http://localhost", "http://localhost", tempList)); //This one is restricted
      		}
      	}

        this.cswService = new CSWService(threadExecutor, httpServiceCaller, util, serviceUrlList);
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
        	threadExecutor.getExecutorService().shutdown();
        	threadExecutor.getExecutorService().awaitTermination(180, TimeUnit.SECONDS);
        }
        catch (Exception ex) {
        	threadExecutor.getExecutorService().shutdownNow();
        	Assert.fail("Exception whilst waiting for update to finish " + ex.getMessage());
        }
      
      //Now lets do the above tests with an unauthorized user
      Assert.assertEquals(RECORD_COUNT_TOTAL * (AUTHORIZED_CSW_COUNT), this.cswService.getAllRecords(httpServletRequest).length);
      Assert.assertEquals(RECORD_COUNT_WMS * (AUTHORIZED_CSW_COUNT), this.cswService.getWMSRecords(httpServletRequest).length);
      Assert.assertEquals(RECORD_COUNT_WFS * (AUTHORIZED_CSW_COUNT), this.cswService.getWFSRecords(httpServletRequest).length);
    }

    /**
     * A simple extension on ReturnValue that adds a delay before the object is returned
     * @author vot002
     *
     */
    private static class DelayedReturnValueAction extends ReturnValueAction {
        private long delayMs;
        public DelayedReturnValueAction(long delayMs, Object returnValue) {
            super(returnValue);
            this.delayMs = delayMs;
        }

        @Override
        public Object invoke(Invocation i) throws Throwable {
            Thread.sleep(delayMs);
            return super.invoke(i);
        }
    }

    private static Action delayReturnValue(long msDelay, Object returnValue) throws Exception {
        return new DelayedReturnValueAction(msDelay, returnValue);
    }

    /**
     * Success if only a single update is able to run at any given time (Subsequent updates are terminated)
     * @throws Exception
     */
    @Test
    public void testSingleUpdate() throws Exception {
        final long delay = 1000;
        final String cswResponse = "<?xml version=\"1.0\"?><node>foo</node>";

        context.checking(new Expectations() {{
            //Cant use oneOf as JUnit can't handle exceptions on other threads (see note below)
            //oneOf(httpServiceCaller).getHttpClient();
            //oneOf(httpServiceCaller).getMethodResponseAsString(with(any(HttpMethodBase.class)), with(any(HttpClient.class)));will(delayReturnValue(delay, cswResponse));

            allowing(httpServiceCaller).getHttpClient();
            allowing(httpServiceCaller).getMethodResponseAsString(with(any(HttpMethodBase.class)), with(any(HttpClient.class)));will(delayReturnValue(delay, cswResponse));
        }});

        final CSWService service = this.cswService;

        Runnable r = new Runnable() {
            public void run() {
            	try {
            		service.updateRecordsInBackground();
            	} catch(Exception e) {
            		Assert.fail(e.toString());
            	}
            }
        };

        Calendar start = Calendar.getInstance();

        //Only one of these threads should actually make a service call
        //otherwise our expectations will fail
        UncaughtExceptionHandler eh = new UncaughtExceptionHandler() {

            public void uncaughtException(Thread t, Throwable e) {
                Assert.fail(e.toString());
            }
        };
        Thread[] threadList = new Thread[5];
        Thread.setDefaultUncaughtExceptionHandler(eh);
        for (int i = 0; i < threadList.length; i++) {
            threadList[i] = new Thread(r);
            threadList[i].setUncaughtExceptionHandler(eh);
            threadList[i].start();
        }

        //Wait for each thread to terminate (we expect the first
        //thread will wait for the full delay whilst all other threads
        //should return immediately)
        //
        //NOTE - JUnit won't pickup the Mock Object exceptions on the other threads
        //     - Workaround - We still work on the assumption that only a single
        //                    Thread will delay and all others will return immediately
        //                    So we just measure the time and as long as it is less than
        //                    threadList.length * delay we are OK
        for (Thread t : threadList) {
            t.join();
        }

        Calendar finish = Calendar.getInstance();
        long totalTime = finish.getTimeInMillis() - start.getTimeInMillis();
        Assert.assertTrue("Test took too long, assuming other threads are NOT returning immediately", totalTime < (delay * 2));
    }

    
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
        	threadExecutor.getExecutorService().shutdown();
        	threadExecutor.getExecutorService().awaitTermination(180, TimeUnit.SECONDS);
        }
        catch (Exception ex) {
        	threadExecutor.getExecutorService().shutdownNow();
        	Assert.fail("Exception whilst waiting for update to finish " + ex.getMessage());
        }
        
      //in the response we loaded from the text file it contains 15 records
      Assert.assertEquals(RECORD_COUNT_TOTAL * CONCURRENT_THREADS_TO_RUN, this.cswService.getAllRecords(httpServletRequest).length);
      
      //in the response we loaded from the text file it contains 2 WMS records
      Assert.assertEquals(RECORD_COUNT_WMS * CONCURRENT_THREADS_TO_RUN, this.cswService.getWMSRecords(httpServletRequest).length);
      
      //in the response we loaded from the text file it contains 12 WFS records
      Assert.assertEquals(RECORD_COUNT_WFS * CONCURRENT_THREADS_TO_RUN, this.cswService.getWFSRecords(httpServletRequest).length);
      
      
    }
}
