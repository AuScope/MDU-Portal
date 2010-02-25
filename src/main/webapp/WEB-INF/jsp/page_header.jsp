<%@ taglib prefix='security' uri='http://www.springframework.org/security/tags' %>
   <div id="header-container">
      <div id="logo">
         <h1>
            <a href="#" onclick="window.open('about.html','AboutWin','toolbar=no, menubar=no,location=no,resizable=no,scrollbars=yes,statusbar=no,top=100,left=200,height=650,width=450');return false"><img alt="" src="img/img-auscope-banner.gif"></a>
            <!-- <a href="login.html"><img alt="" src="img/img-auscope-banner.gif" /></a> -->
         </h1>
      </div>
                                  
      <security:authorize ifAllGranted="ROLE_ADMINISTRATOR">
         <a href="admin.html"><span>Administration</span></a> 
      </security:authorize>
      
      <div id="menu">
         <ul >
            <li ><a href="http://csiro.au/">csiro.au<span></span></a></li>
            <li class="current"><a href="gmap.html">MDU Discovery Portal<span></span></a></li>
            
            <security:authorize ifAllGranted="ROLE_ANONYMOUS">
            	<li><a href="login.html">Login<span></span></a></li>
            </security:authorize>
            
            <security:authorize ifNotGranted="ROLE_ANONYMOUS">
            	<li ><a href="j_spring_security_logout">Logout<span></span></a></li>
            </security:authorize>
         </ul>
      </div>
   </div>
