package org.auscope.portal.csw;

import org.w3c.dom.Node;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathExpressionException;

/**
 * User: Mathew Wyatt
 * Date: 11/02/2009
 * Time: 11:58:21 AM
 */
//TODO: refactor into data and service records
public class CSWRecord {
	//removed this link as it was not used and contained a massive chunk of memory 
    //private Node recordNode; 
    private String serviceName;
    private String serviceUrl;
    private String onlineResourceName;
    private String onlineResourceDescription;
    private String onlineResourceProtocol;
    private String contactOrganisation;


    private String dataIdentificationAbstract;


    public CSWRecord(Node node) throws XPathExpressionException {

        XPath xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(new CSWNamespaceContext());

        String serviceTitleExpression = "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString";
        Node tempNode = (Node)xPath.evaluate(serviceTitleExpression, node, XPathConstants.NODE);
        serviceName = tempNode != null ? tempNode.getTextContent() : "";

        String dataIdentificationAbstractExpression = "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract/gco:CharacterString";
        tempNode = (Node)xPath.evaluate(dataIdentificationAbstractExpression, node, XPathConstants.NODE);
        dataIdentificationAbstract = tempNode != null ? tempNode.getTextContent() : "";

        String serviceUrleExpression = "gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:linkage/gmd:URL";
        tempNode = (Node)xPath.evaluate(serviceUrleExpression, node, XPathConstants.NODE);
        serviceUrl = tempNode != null ? tempNode.getTextContent() : "";

        String onlineResourceNameExpression = "gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:name/gco:CharacterString";
        tempNode = (Node)xPath.evaluate(onlineResourceNameExpression, node, XPathConstants.NODE);
        onlineResourceName = tempNode != null ? tempNode.getTextContent() : "";

        String onlineResourceDescriptionExpression = "gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:description/gco:CharacterString";
        tempNode = (Node)xPath.evaluate(onlineResourceDescriptionExpression, node, XPathConstants.NODE);
        onlineResourceDescription = tempNode != null ? tempNode.getTextContent() : "";

        String onlineResourceProtocolExpression = "gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:protocol/gco:CharacterString";
        tempNode = (Node)xPath.evaluate(onlineResourceProtocolExpression, node, XPathConstants.NODE);
        onlineResourceProtocol = tempNode != null ? tempNode.getTextContent() : "";
        
        String contactOrganisationExpression = "gmd:contact/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString";
        tempNode = (Node)xPath.evaluate(contactOrganisationExpression, node, XPathConstants.NODE);
        contactOrganisation = tempNode != null ? tempNode.getTextContent() : "";
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public String getOnlineResourceName() {
        return onlineResourceName;
    }

    public String getOnlineResourceDescription() {
        return onlineResourceDescription;
    }

    public String getOnlineResourceProtocol() {
        return onlineResourceProtocol;
    }

    public String getContactOrganisation() {
        return contactOrganisation;
    }

    public String getDataIdentificationAbstract() {
        return dataIdentificationAbstract;
    }
}