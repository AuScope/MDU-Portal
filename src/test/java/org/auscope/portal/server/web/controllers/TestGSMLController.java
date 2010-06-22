package org.auscope.portal.server.web.controllers;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;

import org.auscope.portal.server.util.GmlToKml;
import org.auscope.portal.server.web.service.HttpServiceCaller;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import org.jmock.Mockery;
import org.jmock.Expectations;
import org.jmock.lib.legacy.ClassImposteriser;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.auscope.portal.server.web.view.JSONView;
import org.auscope.portal.server.util.GmlToKml;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpClient;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

/**
 * User: Mathew Wyatt
 * Date: 27/08/2009
 * Time: 4:59:56 PM
 */
public class TestGSMLController {

    /**
     * JMock context
     */
    private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};

    /**
     * Mock httpService caller
     */
    private HttpServiceCaller httpServiceCaller = context.mock(HttpServiceCaller.class);

    /**
     * Mock gml to kml converter
     */
    private GmlToKml gmlToKml = context.mock(GmlToKml.class);

    /**
     * The controller to test
     */
    private GSMLController gsmlController;

    /**
     * Mock response
     */
    private HttpServletResponse mockHttpResponse = context.mock(HttpServletResponse.class);
    
    /**
     * Mock request
     */
    private HttpServletRequest mockHttpRequest = context.mock(HttpServletRequest.class);
    
    /**
     * Mock session
     */
    private HttpSession mockHttpSession = context.mock(HttpSession.class);
    
    /**
     * Mock session
     */
    private ServletContext mockServletContext = context.mock(ServletContext.class);

    @Before
    public void setup() {
        gsmlController = new GSMLController(httpServiceCaller, gmlToKml);
    }

    /**
     * Test that all classes are invoked correctly and return valid JSON
     */
    @Test
    public void testGetAllFeatures() throws Exception {
        final String kmlBlob = "kmlBlob";
        

        context.checking(new Expectations() {{
            oneOf(httpServiceCaller).getHttpClient();
            oneOf(httpServiceCaller).getMethodResponseAsString(with(any(HttpMethodBase.class)), with(any(HttpClient.class)));

            oneOf(gmlToKml).convert(with(any(String.class)), with(any(InputStream.class)),with(any(String.class)));will(returnValue(kmlBlob));
            
            oneOf(mockHttpRequest).getSession();will(returnValue(mockHttpSession));
            oneOf(mockHttpSession).getServletContext();will(returnValue(mockServletContext));
            oneOf(mockServletContext).getResourceAsStream(with(any(String.class))); will(returnValue(null));
        }});

        ModelAndView modelAndView = gsmlController.requestAllFeatures("fake", "fake", mockHttpRequest);

        //check that the kml blob has been put ont he model
        Assert.assertEquals(kmlBlob, ((Map)modelAndView.getModel().get("data")).get("kml"));
        Assert.assertTrue(modelAndView.getModel().get("success").equals(true));
    }
    
    @Test
    public void testRequestFeature() throws Exception {
        final String kmlBlob = "kmlBlob";
        
        //httpRequest.getSession().getServletContext().getResourceAsStream("/WEB-INF/xsl/kml.xsl");

        context.checking(new Expectations() {{
            oneOf(httpServiceCaller).getHttpClient();
            oneOf(httpServiceCaller).getMethodResponseAsString(with(any(HttpMethodBase.class)), with(any(HttpClient.class)));

            oneOf(gmlToKml).convert(with(any(String.class)), with(any(InputStream.class)), with(any(String.class)));will(returnValue(kmlBlob));
            
            oneOf(mockHttpRequest).getSession();will(returnValue(mockHttpSession));
            oneOf(mockHttpSession).getServletContext();will(returnValue(mockServletContext));
            oneOf(mockServletContext).getResourceAsStream(with(any(String.class))); will(returnValue(null));
        }});

        ModelAndView modelAndView = gsmlController.requestFeature("fake","fake", "fake", mockHttpRequest);

        //check that the kml blob has been put ont he model
        Assert.assertEquals(kmlBlob, ((Map)modelAndView.getModel().get("data")).get("kml"));
        Assert.assertTrue(modelAndView.getModel().get("success").equals(true));
    }
    
    @Test
    public void testGetFeatureCount() throws Exception {
    	final String docString = org.auscope.portal.Util.loadXML("src/test/resources/GetWFSFeatureCount.xml");
        

        context.checking(new Expectations() {{
            oneOf(httpServiceCaller).getHttpClient();
            oneOf(httpServiceCaller).getMethodResponseAsString(with(any(HttpMethodBase.class)), with(any(HttpClient.class)));will(returnValue(docString));
        }});

        ModelAndView modelAndView = gsmlController.requestFeatureCount("foo", "bar", null);

        //check that we returned the correct value
        JSONView view = (JSONView)modelAndView.getView();
        Assert.assertEquals("161", view.getJSONArray().get(0));
    }
    
    

    /**
     * Test that the gmltokml converter is called and the response put on the servlet response
     */
    @Test
    public void testXSLTRestProxy() throws Exception {
        final String kmlBlob = "kmlBlob";
        final StringWriter responseString = new StringWriter();

        context.checking(new Expectations() {{
            oneOf(httpServiceCaller).getHttpClient();
            oneOf(httpServiceCaller).getMethodResponseAsString(with(any(HttpMethodBase.class)), with(any(HttpClient.class)));

            oneOf(gmlToKml).convert(with(any(String.class)), with(any(InputStream.class)),with(any(String.class)));will(returnValue(kmlBlob));
            oneOf(mockHttpResponse).getWriter();will(returnValue(new PrintWriter(responseString)));
        }});

        gsmlController.xsltRestProxy("fake", null, mockHttpResponse);

        //check that kmlblob made it through
        if(kmlBlob.equals(responseString.getBuffer().toString()))
            Assert.assertTrue(true);
    }
}