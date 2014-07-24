Gmail Tickets
========

Making use of labels in Gmail, we can create a simple ticketing system. As soon as an email comes, we can mark the email as Open. Use the Gmail API to list all open emails. On replying to the support email, we can remove the UNREAD label and Open label and add Closed,Pending or Resolved label depending on the context. So basically, we use labels to move the email ticket from one state to another until it reaches a logical conclusion. Since Gmail allows an email to have any number of labels, we can think of different combinations to achieve our goals. For example, we can add an agent name as a label to assign a ticket to the agent and move the ticket from one agent to another.

By the same logic, we can even build a CRM system on top of Gmail. So then your CRM sits in your inbox.

This is very basic right now with almost no error checking. Just built as a proof of concept.

- More ideas presented at http://blog.kookoo.in/2014/07/playing-around-with-gmail-api.html

![Alt text](http://3.bp.blogspot.com/-X0zNICPQsVU/U746m_4wl7I/AAAAAAAAMI0/W42kM5tlLZk/s1600/gmail.png "Gmail Tickets")

Have put up a quick demo at http://gmail.ozonetel.com/gmail/login.html. There is no password protection. You can choose any username and test it out. I will clean out data everyday.

Prerequisites
========

- Google API access credentials (Client ID, Client Secret). Set it up here https://code.google.com/apis/console/
- Set up allowed Redirect URIs at Google API -> API Access.

Usage
========

1. Add Client ID, and Client Secret parameters to GoogleAuthHelper.java
2. Add http://localhost:8080/gmail/index.jsp as your callback URL in the Google console
3. Compile the project ($ mvn clean install)
4. Deploy war to application server
5. Browse to: http://localhost:8080/gmail/
6. Click "log in with google" on top of this page

Limitations
========

- Client security credentials are stored in the source.
- Not all buttons are working. Have to link up send and closed etc.
- Very quick hacking project. Can make GoogleAuthHelper singleton. Currently the access is a little slow and code can be improved to speed it up.
- Looking for collaborators as I am currently busy with my startup http://www.ozonetel.com :)

Details
========
- Client side is done in Angularjs and server side code is in Java.


