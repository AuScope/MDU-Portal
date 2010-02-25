package org.auscope.portal.server.web.security;

import java.util.ArrayList;

import org.junit.Test;
import org.junit.Before;
import org.junit.Assert;
import org.jmock.Mockery;
import org.jmock.Expectations;
import org.jmock.lib.legacy.ClassImposteriser;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.userdetails.User;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UsernameNotFoundException;

public class TestPortalUserDetailsService {
	
	static final String DefaultRole = "ROLE_DEFAULT";
	static final String UserRole = "ROLE_USER";
	
	PortalUserDetailsService userDetailsService;
	
	@Before
	public void initialise() {
		ArrayList listOfUsers = new ArrayList();
		
		listOfUsers.add(new User("user1","",true,true,true,true,new GrantedAuthority[] {new GrantedAuthorityImpl(UserRole)}));
		listOfUsers.add(new User("user2","",true,true,true,true,new GrantedAuthority[] {new GrantedAuthorityImpl(UserRole)}));
		listOfUsers.add(new User("user3","",true,true,true,true,new GrantedAuthority[] {new GrantedAuthorityImpl(UserRole)}));
		listOfUsers.add(new User("user4","",true,true,true,true,new GrantedAuthority[] {new GrantedAuthorityImpl(UserRole)}));
		
		userDetailsService = new PortalUserDetailsService(DefaultRole, listOfUsers);
	}
	
	@Test
    public void testNoUser() throws Exception {
		
		try {
			userDetailsService.loadUserByUsername(null);
			Assert.fail("No exception thrown");
		}
		catch (UsernameNotFoundException ex) {
		
		}
		
		try {
			userDetailsService.loadUserByUsername("");
			Assert.fail("No exception thrown");
		}
		catch (UsernameNotFoundException ex) {
		
		}
	}
	
	private void testUser(String userName, String expectedRole) {
		UserDetails details = userDetailsService.loadUserByUsername(userName);
		
		Assert.assertNotNull(details);
		Assert.assertEquals(userName, details.getUsername());
		
		GrantedAuthority[] authorityList = details.getAuthorities();
		Assert.assertNotNull(authorityList);
		Assert.assertArrayEquals(new GrantedAuthority[] {new GrantedAuthorityImpl(expectedRole)}, authorityList);
	}
	
	@Test
	public void testNonMatchingUser() throws Exception {
		testUser("user that DNE", DefaultRole);
	}
	
	@Test
	public void testMatchingUser() throws Exception {
		testUser("user1", UserRole);
		testUser("user2", UserRole);
		testUser("user3", UserRole);
		testUser("user4", UserRole);
	}
}
