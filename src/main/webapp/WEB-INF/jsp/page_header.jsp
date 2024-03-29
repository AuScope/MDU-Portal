<%@ taglib prefix='security' uri='http://www.springframework.org/security/tags' %>
   <div id="header-container">
      <div id="logo">
         <h1>
            <a href="#" onclick="window.open('about.html','AboutWin','toolbar=no, menubar=no,location=no,resizable=no,scrollbars=yes,statusbar=no,top=100,left=200,height=650,width=450');return false"><img alt="" src="img/img-auscope-banner.gif"></a>
            <!-- <a href="login.html"><img alt="" src="img/img-auscope-banner.gif" /></a> -->
         </h1>
      </div>
                                  
     
      <div id="menu">
         <ul >
            <security:authorize ifAllGranted="ROLE_ADMINISTRATOR">
				<li <%if (request.getRequestURL().toString().contains("/admin.jsp")) {%>class="current" <%} %>><a href="admin.html">Administration<span></span></a></li>
      		</security:authorize>
			<li ><a href="http://csiro.au/">csiro.au<span></span></a></li>
            <li  <%if (request.getRequestURL().toString().contains("/gmap.jsp")) {%>class="current" <%} %>><a href="gmap.html">MDU Discovery Portal<span></span></a></li>
            
            <security:authorize ifAllGranted="ROLE_ANONYMOUS">
            	<li><a href="login.html">Login<span></span></a></li>
            </security:authorize>
            
            <security:authorize ifNotGranted="ROLE_ANONYMOUS">
            	<li ><a href="j_spring_security_logout">Logout<span></span></a></li>
            </security:authorize>
         </ul>
      </div>
      
      <span id="latlng" class="input-text"></span>
      
      <div id="permalinkicon"><a href="javascript:void(0)" onclick="permaLinkClickHandler()"><img src="img/link.png" width="16" height="16"/></a></div>
      <div id="permalink"><a href="javascript:void(0)" onclick="permaLinkClickHandler()">Permanent Link</a></div>
 
   </div>
