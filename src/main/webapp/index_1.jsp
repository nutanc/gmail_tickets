<%@page import="com.danter.google.auth.GoogleAuthHelper"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Google OAuth 2.0 v1 Demo</title>
</head>
<body>
	<div class="oauthDemo">
		<%
			/*
			 * The GoogleAuthHelper handles all the heavy lifting, and contains all "secrets"
			 * required for constructing a google login url.
			 */
                    GoogleAuthHelper helper=null;
                    if(session.getAttribute("helper")==null)
                    {
			helper = new GoogleAuthHelper("chetan");
                        session.setAttribute("helper", helper);
                    }
                    else
                    {
                        helper=(GoogleAuthHelper)session.getAttribute("helper");
                    }
                    

			if (request.getParameter("code") == null
					|| request.getParameter("state") == null) {
                        //if(request.getParameter("code") == null && !helper.isCredentialAvailable()){
			/*
				 * set the secure state token in session to be able to track what we sent to google
				 */
				session.setAttribute("state", helper.getStateToken());	
                            /*
				 * initial visit to the page
				 */
                                response.sendRedirect(helper.buildLoginUrl());
//				out.println("<a href='" + helper.buildLoginUrl()
//						+ "'>log in with google</a>");
//						
				

			} 
//                        else if (request.getParameter("code") != null) {
                         else if (request.getParameter("code") != null && request.getParameter("state") != null
					&& request.getParameter("state").equals(session.getAttribute("state"))){
                            helper.getCredential(request.getParameter("code"));
                            out.println("<pre>");
                            //out.println(helper.getUserEmails());
                            out.println("Got code");
                            out.println("</pre>");
                        }
                        else{

				session.removeAttribute("state");

				out.println("<pre>");
				/*
				 * Executes after google redirects to the callback url.
				 * Please note that the state request parameter is for convenience to differentiate
				 * between authentication methods (ex. facebook oauth, google oauth, twitter, in-house).
				 * 
				 * GoogleAuthHelper()#getUserInfoJson(String) method returns a String containing
				 * the json representation of the authenticated user's information. 
				 * At this point you should parse and persist the info.
				 */

				//out.println(helper.getUserInfoJson(request.getParameter("code")));
                                out.println("Have cred");
                                //out.println(helper.getUserEmails());

				out.println("</pre>");
			}
		%>
	</div>

</body>
</html>
