package org.auscope.portal.server.web.service;

import java.util.*;

import javax.servlet.http.HttpServletRequest;

/**
 * A simple class that stores the URL of a CSW service along with extra security / misc information
 * @author VOT002
 *
 */
public class CSWServiceItem {
	private String serviceUrl;
	private String[] restrictedRoleList;
	
	/**
	 * Creates a new service item with NO role restrictions
	 * @param serviceUrl
	 */
	public CSWServiceItem(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}
	
	/**
	 * Creates a new service item that is restricted to users with ANY of the specified roles
	 * @param serviceUrl
	 * @param restrictedToRoles the list of roles (the toString method will be called on each element and stored)
	 */
	public CSWServiceItem(String serviceUrl, Collection restrictedRoleList) {
		this.serviceUrl = serviceUrl;
		
		this.restrictedRoleList = new String[restrictedRoleList.size()];
		int i = 0;
		for (Object role : restrictedRoleList) {
			this.restrictedRoleList[i++] = role.toString(); 
		}
	}
	
	/**
	 * The URL that the CSW service is hosted at.
	 * @return
	 */
	public String getServiceUrl() {
		return this.serviceUrl;
	}
	
	/**
	 * The list of roles that a user must have at least one of to be authorized to see records from the CSW Service
	 * 
	 * Can be null or empty
	 * @return
	 */
	public String[] getRestrictedRoleList() {
		return this.restrictedRoleList;
	}
	
	/**
	 * Gets whether this service URL's record set can be read by the user making the request
	 * @return
	 */
	public boolean isUserAuthorized(HttpServletRequest request) {
		//Shortcut for no restrictions
		if (this.restrictedRoleList == null || this.restrictedRoleList.length == 0)
			return true;
		
		//Otherwise see if we have a role the user is in 
		for (String role : this.restrictedRoleList) {
			if (request.isUserInRole(role)) {
				return true;
			}
		}
		return false;
	}
}