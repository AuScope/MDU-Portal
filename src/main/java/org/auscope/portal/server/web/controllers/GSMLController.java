package org.auscope.portal.server.web.controllers;

import net.sf.json.JSONArray;

import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.auscope.portal.server.web.view.JSONModelAndView;
import org.auscope.portal.server.util.GmlToKml;
import org.auscope.portal.server.util.Util;
import org.auscope.portal.csw.CSWNamespaceContext;
import org.auscope.portal.csw.ICSWMethodMaker;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Acts as a proxy to WFS's
 *
 * User: Mathew Wyatt
 * Date: 17/08/2009
 * Time: 12:10:41 PM
 */

@Controller
public class GSMLController {
    protected final Log logger = LogFactory.getLog(getClass().getName());
    private HttpServiceCaller serviceCaller;
    private GmlToKml gmlToKml;
    private Util util;

    @Autowired
    public GSMLController(HttpServiceCaller serviceCaller,
                          GmlToKml gmlToKml) {
        this.serviceCaller = serviceCaller;
        this.gmlToKml = gmlToKml;
        this.util = new Util();
    }
    
    /**
     * Given a service Url and a feature type this will query for the count of all of the features
     * Returns a JSON response with a data structure like so
     * [
     * [recordCount]
     * ]
     * @param serviceUrl
     * @param featureType
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getFeatureCount.do", params = {"serviceUrl", "typeName"})
    public ModelAndView requestFeatureCount(@RequestParam("serviceUrl") final String serviceUrl,
                                           @RequestParam("typeName") final String featureType,
                                           HttpServletRequest request) throws Exception {
    	return requestFeatureCount(serviceUrl, featureType, null, request);
    }

    /**
     * Given a service Url and a feature type this will query for the count of all of the features within a given bounding box
     * Returns a JSON response with a data structure like so
     * [
     * [recordCount]
     * ]
     */
    @RequestMapping(value = "/getFeatureCount.do", params = {"serviceUrl", "typeName", "boundingBox"})
    public ModelAndView requestFeatureCount(@RequestParam("serviceUrl") final String serviceUrl,
                                           @RequestParam("typeName") final String featureType,
                                           @RequestParam("boundingBox") final String boundingBox,
                                           HttpServletRequest request) throws Exception {
    	
    	JSONArray dataItems = new JSONArray();
    	
    	String gmlResponse = serviceCaller.getMethodResponseAsString(new ICSWMethodMaker() {
    		public HttpMethodBase makeMethod() {
                GetMethod method = new GetMethod(serviceUrl);

                ArrayList<NameValuePair> valuePairs = new ArrayList<NameValuePair>();
                
                //set all of the parameters
                valuePairs.add(new NameValuePair("request", "GetFeature"));
                valuePairs.add(new NameValuePair("typeName", featureType));
                valuePairs.add(new NameValuePair("resultType", "hits"));
                if (boundingBox != null && boundingBox.length() > 0)
                	valuePairs.add(new NameValuePair("bbox", boundingBox));

                //attach them to the method
                method.setQueryString((NameValuePair[]) valuePairs.toArray(new NameValuePair[valuePairs.size()]));

                return method;
            }
        }.makeMethod(), serviceCaller.getHttpClient());
    	
    	//Now that we have a response from the server, lets pull out the number of features returned
    	XPath xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(new CSWNamespaceContext());

        String extractCountExpression = "@numberOfFeatures";
		String featureCountString = (String)xPath.evaluate(extractCountExpression, util.buildDomFromString(gmlResponse).getDocumentElement(), XPathConstants.STRING);
        
        dataItems.add(featureCountString);
    	
    	return new JSONModelAndView(dataItems);
    }
    
    /**
     * Given a service Url and a feature type this will query for all of the features, then convert them into KML,
     * to be displayed, assuming that the response will be complex feature GeoSciML
     *
     * @param serviceUrl
     * @param featureType
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getAllFeatures.do", params = {"serviceUrl","typeName"})
    public ModelAndView requestAllFeatures(@RequestParam("serviceUrl") final String serviceUrl,
                                           @RequestParam("typeName") final String featureType,
                                           HttpServletRequest request) throws Exception {
    	return requestAllFeatures(serviceUrl, featureType, null, null, request);
    }
    
    /**
     * Given a service Url and a feature type this will query for all of the features within a given bounding box, 
     * then convert them into KML to be displayed, assuming that the response will be complex feature GeoSciML
     *
     * @param serviceUrl
     * @param featureType
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getAllFeatures.do", params = {"serviceUrl","typeName", "boundingBox"})
    public ModelAndView requestAllFeatures(@RequestParam("serviceUrl") final String serviceUrl,
                                           @RequestParam("typeName") final String featureType,
                                           @RequestParam("boundingBox") final String boundingBox,
                                           HttpServletRequest request) throws Exception {
    	return requestAllFeatures(serviceUrl, featureType, boundingBox, null, request);
    }
    
    /**
     * Given a service Url and a feature type this will query for up to the count of maxFeatures of the features, 
     * then convert them into KML to be displayed, assuming that the response will be complex feature GeoSciML
     *
     * @param serviceUrl
     * @param featureType
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getAllFeatures.do", params = {"serviceUrl","typeName", "maxFeatures"})
    public ModelAndView requestAllFeatures_(@RequestParam("serviceUrl") final String serviceUrl,
                                           @RequestParam("typeName") final String featureType,
                                           @RequestParam("maxFeatures") final String maxFeatures,
                                           HttpServletRequest request) throws Exception {
    	return requestAllFeatures(serviceUrl, featureType, null, maxFeatures, request);
    }
    
    /**
     * Given a service Url and a feature type this will query for up to the count of maxFeatures of the features within a given bounding box, 
     * then convert them into KML to be displayed, assuming that the response will be complex feature GeoSciML
     *
     * @param serviceUrl
     * @param featureType
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getAllFeatures.do", params = {"serviceUrl","typeName" , "boundingBox", "maxFeatures"})
    public ModelAndView requestAllFeatures(@RequestParam("serviceUrl") final String serviceUrl,
                                           @RequestParam("typeName") final String featureType,
                                           @RequestParam("boundingBox") final String boundingBox,
                                           @RequestParam("maxFeatures") final String maxFeatures,
                                           HttpServletRequest request) throws Exception {
    	
        String gmlResponse = serviceCaller.getMethodResponseAsString(new ICSWMethodMaker() {
            public HttpMethodBase makeMethod() {
                GetMethod method = new GetMethod(serviceUrl);

                ArrayList<NameValuePair> valuePairs = new ArrayList<NameValuePair>();
                
                //set all of the parameters
                valuePairs.add(new NameValuePair("request", "GetFeature"));
                valuePairs.add(new NameValuePair("typeName", featureType));
                if (maxFeatures != null && maxFeatures.length() > 0)
                	valuePairs.add(new NameValuePair("maxFeatures", maxFeatures));
                if (boundingBox != null && boundingBox.length() > 0)
                	valuePairs.add(new NameValuePair("bbox", boundingBox));

                //attach them to the method
                method.setQueryString((NameValuePair[]) valuePairs.toArray(new NameValuePair[valuePairs.size()]));

                return method;
            }
        }.makeMethod(), serviceCaller.getHttpClient());

         return makeModelAndViewKML(convertToKml(gmlResponse, request, serviceUrl), gmlResponse);
    }
    
    /**
     * Given a service Url, a feature type and a specific feature ID, this function will fetch the specific feature and 
     * then convert it into KML to be displayed, assuming that the response will be complex feature GeoSciML
     * @param serviceUrl
     * @param featureType
     * @param featureId
     * @param request
     * @return
     */
    @RequestMapping("/requestFeature.do")
    public ModelAndView requestFeature(@RequestParam("serviceUrl") final String serviceUrl,
            						   @RequestParam("typeName") final String featureType,
            						   @RequestParam("featureId") final String featureId,
            						   HttpServletRequest request) throws Exception {
    	String gmlResponse = serviceCaller.getMethodResponseAsString(new ICSWMethodMaker() {
            public HttpMethodBase makeMethod() {
                GetMethod method = new GetMethod(serviceUrl);

                ArrayList<NameValuePair> valuePairs = new ArrayList<NameValuePair>();
                
                //set all of the parameters
                valuePairs.add(new NameValuePair("request", "GetFeature"));
                valuePairs.add(new NameValuePair("typeName", featureType));
                valuePairs.add(new NameValuePair("featureId", featureId));
                

                //attach them to the method
                method.setQueryString((NameValuePair[]) valuePairs.toArray(new NameValuePair[valuePairs.size()]));

                return method;
            }
        }.makeMethod(), serviceCaller.getHttpClient());

        return makeModelAndViewKML(convertToKml(gmlResponse, request, serviceUrl), gmlResponse);
    }

    @RequestMapping("/xsltRestProxy.do")
    public void xsltRestProxy(@RequestParam("serviceUrl") String serviceUrl,
                              HttpServletRequest request,
                              HttpServletResponse response) {
        try {
            String result = serviceCaller.getMethodResponseAsString(new GetMethod(serviceUrl), serviceCaller.getHttpClient());

            // Send response back to client
            response.getWriter().println(convertToKml(result, request, serviceUrl));
        } catch (Exception e) {
            logger.error(e);
        }
    }

    /**
     * Insert a kml block into a successful JSON response
     * @param kmlBlob
     * @return
     */
    private ModelAndView makeModelAndViewKML(final String kmlBlob, final String gmlBlob) {
        final Map data = new HashMap() {{
            put("kml", kmlBlob);
            put("gml", gmlBlob);
        }};

        ModelMap model = new ModelMap() {{
            put("success", true);
            put("data", data);
        }};

        return new JSONModelAndView(model);
    }
    
    /**
     * Assemble a call to convert GeoSciML into kml format 
     * @param geoXML
     * @param httpRequest
     * @param serviceUrl
     */
    private String convertToKml(String geoXML, HttpServletRequest httpRequest, String serviceUrl) {
        InputStream inXSLT = httpRequest.getSession().getServletContext().getResourceAsStream("/WEB-INF/xsl/kml.xsl");
        return gmlToKml.convert(geoXML, inXSLT, serviceUrl);
    }
}