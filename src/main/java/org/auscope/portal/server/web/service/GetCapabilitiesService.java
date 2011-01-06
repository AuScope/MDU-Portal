package org.auscope.portal.server.web.service;

import org.auscope.portal.server.domain.ows.GetCapabilitiesRecord;

/**
 * Service for operations on GetCapability objects
 * 
 * @author JarekSanders
 * @version $Id: GetCapabilitiesService.java 1120 2010-06-23 13:12:58Z JarekSanders $
 */
public interface GetCapabilitiesService {
    public GetCapabilitiesRecord getWmsCapabilities(String serviceURL)
    throws Exception;
}
