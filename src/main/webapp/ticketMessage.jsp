<%@page import="com.ozonetel.gmail.GoogleAuthHelper"%>
<%@page contentType="application/json" pageEncoding="UTF-8"%>
		<%
			/*
			 * The GoogleAuthHelper handles all the heavy lifting, and contains all "secrets"
			 * required for constructing a google login url.
			 */
                    GoogleAuthHelper helper= new GoogleAuthHelper((String)session.getAttribute("user"));
                    if(helper.isCredentialAvailable((String)session.getAttribute("user")))
                    {
                        //out.println(helper.getMessageThread(request.getParameter("ticketId")));
                        //Get only the message now. We can get thread on click later
                        out.println(helper.getMessage(request.getParameter("ticketId")));
                    } 
                    else
                    {
                        //response.sendRedirect(helper.buildLoginUrl());
                        out.println("no token");
                    }
		%>
