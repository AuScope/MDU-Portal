package org.auscope.portal.csw;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

/**
 * User: Mathew Wyatt
 * Date: 02/07/2009
 * Time: 12:12:34 PM
 */
public class CSWMethodMakerGetDataRecords implements ICSWMethodMaker {
    private String serviceURL;

    public CSWMethodMakerGetDataRecords(String serviceURL) throws Exception {
        this.serviceURL = serviceURL;

        //pretty hard to do updateCSWRecords GetFeature query with updateCSWRecords serviceURL, so we had better check that we have one
        if(serviceURL == null || serviceURL.equals(""))
            throw new Exception("serviceURL parameter can not be null or empty.");
    }

    public HttpMethodBase makeMethod() throws Exception {
    	PostMethod httpMethod = new PostMethod(this.serviceURL);

        //Currently this is static
        String postString =  
        "<csw:GetRecords xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\" service=\"CSW\" constraint_language_version=\"1.1.0\" maxRecords=\"10000\" startPosition=\"1\" outputFormat=\"application/xml\" outputSchema=\"csw:IsoRecord\" resultType=\"results\" typeNames=\"csw:Record\" namespace=\"csw:http://www.opengis.net/cat/csw\">\n" + 
			"<csw:Query typeNames=\"csw:Record\">\n" +
				"<csw:ElementSetName>full</csw:ElementSetName>\n" +
				"<csw:Constraint version=\"1.1.0\">\n" +
					"<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns=\"http://www.opengis.net/ogc\" xmlns:gml=\"http://www.opengis.net/gml\">\n" +
						"<ogc:Or>\n" +
							"<ogc:PropertyIsLike escape=\"\\\" singleChar=\"_\" wildCard=\"%\">\n" +
								"<ogc:PropertyName>any</ogc:PropertyName>\n" +
								"<ogc:Literal>%wms%</ogc:Literal>\n" +
							"</ogc:PropertyIsLike>\n" +
							"<ogc:PropertyIsLike escape=\"\\\" singleChar=\"_\" wildCard=\"%\">\n" +
								"<ogc:PropertyName>any</ogc:PropertyName>\n" +
								"<ogc:Literal>%wfs%</ogc:Literal>\n" +
							"</ogc:PropertyIsLike>\n" +
						"</ogc:Or>\n" +
					"</ogc:Filter>\n" +
				"</csw:Constraint>\n" +
				"<ogc:SortBy xmlns:ogc=\"http://www.opengis.net/ogc\">\n" +
					"<ogc:SortProperty>\n" +
						"<ogc:PropertyName>none</ogc:PropertyName>\n" +
						"<ogc:SortOrder>ASC</ogc:SortOrder>\n" +
					"</ogc:SortProperty>\n" +
				"</ogc:SortBy>\n" +
			"</csw:Query>\n" +
		"</csw:GetRecords>";

        
        // If this does not work, try params: "text/xml; charset=ISO-8859-1"
        httpMethod.setRequestEntity(new StringRequestEntity(postString,"text/xml","ISO-8859-1"));

        return httpMethod;
    }
}
