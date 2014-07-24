package com.ozonetel.gmail;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.client.util.store.FileDataStoreFactory;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collection;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Label;
import com.google.api.services.gmail.model.ListLabelsResponse;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.google.api.services.gmail.model.ModifyMessageRequest;
import java.io.BufferedReader;


import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * A helper class for Google's Gmail API.
 *
 */
public final class GoogleAuthHelper {

    /**
     * Directory to store user credentials.
     */
    private static final File DATA_STORE_DIR = new File(System.getProperty("user.home"), "credentials");
    /**
     * Global instance of the
     * {@link com.google.api.client.util.store.DataStoreFactory}. The best
     * practice is to make it a single globally shared instance across your
     * application.
     */
    private static FileDataStoreFactory data_store_factory;
    /**
     * Please provide a value for the CLIENT_ID constant before proceeding, set
     * this up at https://code.google.com/apis/console/
     */
    private static final String CLIENT_ID = "{{Client id from google console}}";
    /**
     * Please provide a value for the CLIENT_SECRET constant before proceeding,
     * set this up at https://code.google.com/apis/console/
     */
    private static final String CLIENT_SECRET = "{{Client secret}}";
    /**
     * Callback URI that google will redirect to after successful authentication
     */
    private static final String CALLBACK_URI = "http://{yourdomain.com}/index.jsp";
    // start google authentication constants
    private static final Collection<String> SCOPE = Arrays.asList("https://www.googleapis.com/auth/userinfo.profile;https://www.googleapis.com/auth/userinfo.email;https://www.googleapis.com/auth/gmail.modify".split(";"));
    private static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v1/userinfo";
    private static final JsonFactory JSON_FACTORY = new JacksonFactory();
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    // end google authentication constants
    private String stateToken;
    private GoogleAuthorizationCodeFlow flow;
    private Credential credential;

    /**
     * Constructor initializes the Google Authorization Code Flow with CLIENT
     * ID, SECRET, and SCOPE
     */
    public GoogleAuthHelper(String userId) {

        try {
            data_store_factory = new FileDataStoreFactory(DATA_STORE_DIR);
            flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT,
                    JSON_FACTORY, CLIENT_ID, CLIENT_SECRET, SCOPE) //.setApprovalPrompt("force")
                    .setDataStoreFactory(data_store_factory)
                    .setApprovalPrompt("force")
                    .setAccessType("offline").build();

            credential = flow.loadCredential(userId);
            System.out.println("cred=" + credential);
        } catch (IOException ex) {
            Logger.getLogger(GoogleAuthHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        generateStateToken();
    }
    /*
     JsonBatchCallback<Gmail> callback = new JsonBatchCallback<Gmail>() {
     public void onSuccess(Gmail gmail, HttpHeaders responseHeaders) {
     //printCalendar(calendar);
     //addedCalendarsUsingBatch.add(calendar);
     }

     public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) {
     System.out.println("Error Message: " + e.getMessage());
     }
     };*/

    public boolean isCredentialAvailable(String userName) {
        Credential credential = null;
        try {
            credential = flow.loadCredential(userName);
        } catch (IOException ex) {
            Logger.getLogger(GoogleAuthHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (credential == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Builds a login URL based on client ID, secret, callback URI, and scope
     */
    public String buildLoginUrl() {

        final GoogleAuthorizationCodeRequestUrl url = flow.newAuthorizationUrl();

        return url.setRedirectUri(CALLBACK_URI).setState(stateToken).build();
        //return url.setState(stateToken).build();
    }

    /**
     * Generates a secure state token
     */
    private void generateStateToken() {

        SecureRandom sr1 = new SecureRandom();

        stateToken = "google;" + sr1.nextInt();

    }

    /**
     * Accessor for state token
     */
    public String getStateToken() {
        return stateToken;
    }

    /**
     * Expects an Authentication Code, and makes an authenticated request for
     * the user's profile information
     *
     * @return JSON formatted user profile information
     * @param authCode authentication code provided by google
     */
    public String getUserInfoJson(final String authCode, final String userName) throws IOException {
        final GoogleTokenResponse response = flow.newTokenRequest(authCode).setRedirectUri(CALLBACK_URI).execute();
        System.out.println("resp=" + response);
        Credential credential = flow.createAndStoreCredential(response, userName);
        final HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(credential);
        // Make an authenticated request
        final GenericUrl url = new GenericUrl(USER_INFO_URL);
        final HttpRequest request = requestFactory.buildGetRequest(url);
        request.getHeaders().setContentType("application/json");
        final String jsonIdentity = request.execute().parseAsString();

        return jsonIdentity;

    }

    public void getCredential(String authCode) {
        try {
            GoogleTokenResponse response = flow.newTokenRequest(authCode).setRedirectUri(CALLBACK_URI).execute();
            System.out.println("refresh token:" + response.getRefreshToken());
//            credential = flow.createAndStoreCredential(response, null);

            Credential credential = createCredentialWithRefreshToken(HTTP_TRANSPORT,
                    JSON_FACTORY, CLIENT_ID, CLIENT_SECRET, response);
        } catch (IOException ex) {
            Logger.getLogger(GoogleAuthHelper.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static GoogleCredential createCredentialWithRefreshToken(HttpTransport transport,
            JsonFactory jsonFactory, String clientId, String clientSecret, TokenResponse tokenResponse) {
        return new GoogleCredential.Builder().setTransport(transport).setJsonFactory(jsonFactory).setClientSecrets(clientId, clientSecret).build().setFromTokenResponse(tokenResponse);
    }

    public String getUserEmails() throws IOException {

        Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName("Gmail Quickstart").build();
        JSONObject ticketDetails = new JSONObject();
        ListMessagesResponse openMessages = service.users().messages().list("me") //.setLabelIds(labelIds)
                .setQ("is:unread label:inbox").setMaxResults(new Long(3)).execute();
        ticketDetails.put("open", "" + openMessages.getResultSizeEstimate());

        ListMessagesResponse closedMessages = service.users().messages().list("me") //.setLabelIds(labelIds)
                .setQ("label:inbox label:closed").setMaxResults(new Long(1)).execute();
        ticketDetails.put("closed", "" + closedMessages.getResultSizeEstimate());

        ListMessagesResponse pendingMessages = service.users().messages().list("me") //.setLabelIds(labelIds)
                .setQ("label:inbox label:pending").setMaxResults(new Long(1)).execute();
        ticketDetails.put("pending", "" + pendingMessages.getResultSizeEstimate());

        ticketDetails.put("unassigned", "0");
        List<Message> messages = openMessages.getMessages();
        //List<Map> openTickets=new ArrayList<Map>();
        JSONArray openTickets = new JSONArray();
        String returnVal = "";
        // Print ID and snippet of each Thread.
        if (messages != null) {

            for (Message message : messages) {
                openTickets.add(new JSONObject(getBareGmailMessageDetails(message.getId())));
            }
            ticketDetails.put("openTicketDetails", openTickets);
        }
        return ticketDetails.toJSONString();

    }

    public String getUserEmails(String label) throws IOException {

        Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName("Gmail Quickstart").build();
        JSONObject ticketDetails = new JSONObject();
        ListMessagesResponse labelMessages = service.users().messages().list("me") //.setLabelIds(labelIds)
                .setQ("label:inbox label:" + label).setMaxResults(new Long(3)).execute();
        List<Message> messages = labelMessages.getMessages();
        JSONArray labelTickets = new JSONArray();
        String returnVal = "";
        if (messages != null) {
            for (Message message : messages) {
                labelTickets.add(new JSONObject(getBareGmailMessageDetails(message.getId())));
            }
            ticketDetails.put("labelTicketDetails", labelTickets);
        }
        return ticketDetails.toJSONString();

    }

    public String setMessageLabel(String messageId, String labelName) {
        try {
            Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName("Gmail Quickstart").build();
            //For now assuming that when a label is set, we make it read also.

            List<String> labelsToRemove = new ArrayList();
            labelsToRemove.add("UNREAD");
            List<String> labelsToAdd = new ArrayList();
            if (labelName != null) {
                //this can be optimized by get all label names once they login
                ListLabelsResponse response = service.users().labels().list("me").execute();
                List<Label> labels = response.getLabels();
                boolean labelExists = false;
                for (Label label : labels) {
                    //System.out.println(label.toPrettyString());
                    if (label.getName().equalsIgnoreCase(labelName)) {
                        labelsToAdd.add(label.getId());
                        System.out.println("Adding label:" + labelName);
                        labelExists = true;
                        break;
                    }
                }

                if (!labelExists) {
                    Label label = new Label().setName(labelName)
                            .setLabelListVisibility("labelShow")
                            .setMessageListVisibility("show");
                    label = service.users().labels().create("me", label).execute();
                    labelsToAdd.add(label.getId());
                }
            }

            ModifyMessageRequest mods;
            mods = new ModifyMessageRequest().setRemoveLabelIds(labelsToRemove)
                    .setAddLabelIds(labelsToAdd);
            Message message = service.users().messages().modify("me", messageId, mods).execute();
            return "success";
        } catch (IOException ioe) {
            System.out.println("ioerror");
            //ioe.printStackTrace();
            return "error";
        }
    }

    public String trashMessage(String messageId) {
        String msg = "";
        try {
            Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName("Gmail Quickstart").build();
            service.users().messages().trash("me", messageId).execute();
            return "success";
        } catch (IOException ex) {
            Logger.getLogger(GoogleAuthHelper.class.getName()).log(Level.SEVERE, null, ex);
            return "error";
        }
    }

    public String getMessage(String messageId) {
        //helper function to get message details in JSON format
        return new JSONObject(getMessageDetails(messageId)).toJSONString();
    }

    public Map getMessageDetails(String messageId) {
        Map<String, Object> messageDetails = new HashMap<String, Object>();
        try {
            Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName("Gmail Quickstart").build();
            Message message = service.users().messages().get("me", messageId).setFormat("raw").execute();

            byte[] emailBytes = Base64.decodeBase64(message.getRaw());

            Properties props = new Properties();
            Session session = Session.getDefaultInstance(props, null);

            MimeMessage email = new MimeMessage(session, new ByteArrayInputStream(emailBytes));
            messageDetails.put("subject", email.getSubject());
            messageDetails.put("from", email.getSender() != null ? email.getSender().toString() : "None");
            messageDetails.put("time", email.getSentDate() != null ? email.getSentDate().toString() : "None");
            messageDetails.put("snippet", message.getSnippet());
            messageDetails.put("threadId", message.getThreadId());
            messageDetails.put("id", message.getId());
            messageDetails.put("body", getText(email));

        } catch (MessagingException ex) {
            Logger.getLogger(GoogleAuthHelper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GoogleAuthHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return messageDetails;

    }

    public Map getBareMessageDetails(String messageId) {
        Map<String, Object> messageDetails = new HashMap<String, Object>();
        try {
            Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName("Gmail Quickstart").build();
            Message message = service.users().messages().get("me", messageId).setFormat("raw").execute();

            byte[] emailBytes = Base64.decodeBase64(message.getRaw());

            Properties props = new Properties();
            Session session = Session.getDefaultInstance(props, null);

            MimeMessage email = new MimeMessage(session, new ByteArrayInputStream(emailBytes));
            messageDetails.put("subject", email.getSubject());
            messageDetails.put("from", email.getSender() != null ? email.getSender().toString() : "None");
            messageDetails.put("time", email.getSentDate() != null ? email.getSentDate().toString() : "None");
            messageDetails.put("snippet", message.getSnippet());
            messageDetails.put("threadId", message.getThreadId());
            messageDetails.put("id", message.getId());
            messageDetails.put("body", getText(email));

        } catch (MessagingException ex) {
            Logger.getLogger(GoogleAuthHelper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GoogleAuthHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return messageDetails;

    }

    public Map getBareGmailMessageDetails(String messageId) {
        Map<String, Object> messageDetails = new HashMap<String, Object>();
        try {
            Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName("Gmail Quickstart").build();
            Message message = service.users().messages().get("me", messageId).setFormat("full")
                    .setFields("id,payload,sizeEstimate,snippet,threadId").execute();
            List<MessagePartHeader> headers = message.getPayload().getHeaders();
            for (MessagePartHeader header : headers) {
                if (header.getName().equals("From") || header.getName().equals("Date")
                        || header.getName().equals("Subject") || header.getName().equals("To")
                        || header.getName().equals("CC")) {
                    messageDetails.put(header.getName().toLowerCase(), header.getValue());
                }
            }
            messageDetails.put("snippet", message.getSnippet());
            messageDetails.put("threadId", message.getThreadId());
            messageDetails.put("id", message.getId());
            //messageDetails.put("body",message.getPayload().getBody().getData());

        } catch (IOException ex) {
            Logger.getLogger(GoogleAuthHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return messageDetails;

    }
    private boolean textIsHtml = false;

    /**
     * Return the primary text content of the message.
     */
    private String getText(Part p) throws
            MessagingException, IOException {
        if (p.isMimeType("text/*")) {
            String s = (String) p.getContent();
            textIsHtml = p.isMimeType("text/html");
            return s;
        }

        if (p.isMimeType("multipart/alternative")) {
            // prefer html text over plain text
            Multipart mp = (Multipart) p.getContent();
            String text = null;
            for (int i = 0; i < mp.getCount(); i++) {
                Part bp = mp.getBodyPart(i);
                if (bp.isMimeType("text/plain")) {
                    if (text == null) {
                        text = getText(bp);
                    }
                    continue;
                } else if (bp.isMimeType("text/html")) {
                    String s = getText(bp);
                    if (s != null) {
                        return s;
                    }
                } else {
                    return getText(bp);
                }
            }
            return text;
        } else if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) p.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                String s = getText(mp.getBodyPart(i));
                if (s != null) {
                    return s;
                }
            }
        }

        return null;
    }

    public String getMessageThread(String threadId) {
        String returnVal = "";
        JSONObject threadDetails = new JSONObject();
        try {

            Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName("Gmail Quickstart").build();
            com.google.api.services.gmail.model.Thread thread = service.users().threads().get("me", threadId).execute();
            System.out.println("Thread id: " + thread.getId());
            System.out.println("No. of messages in this thread: " + thread.getMessages().size());
            List<Message> messages = thread.getMessages();
            if (messages != null) {
                for (Message message : messages) {
                    Message msg1 = service.users().messages().get("me", message.getId()).execute();
                    //returnVal = returnVal + getMessage(message.getId());
                    returnVal = returnVal + getMessageDetails(message.getId()).get("body");
                }

                System.out.println(thread.toPrettyString());
            }
            threadDetails.put("message", returnVal);
            return threadDetails.toJSONString();
        } catch (IOException ex) {
            Logger.getLogger(GoogleAuthHelper.class.getName()).log(Level.SEVERE, null, ex);
            return "error";
        }
    }

    /**
     * Send an email from the user's mailbox to its recipient.
     *
     * @param service Authorized Gmail API instance.
     * @param userId User's email address. The special value "me" can be used to
     * indicate the authenticated user.
     * @param email Email to be sent.
     * @throws MessagingException
     * @throws IOException
     */
    public String sendMessage(String to, String from, String subject, String body)
            throws MessagingException, IOException {

        Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName("Gmail Quickstart").build();
        MimeMessage email = createMimeMessage(to, from, subject, body);
        Message message = createMessageWithEmail(email);
        message = service.users().messages().send("me", message).execute();

        System.out.println("Message id: " + message.getId());
        System.out.println(message.toPrettyString());
        if (message.getId() != null) {
            return "success";
        } else {
            return "fail";
        }
    }

    /**
     * Create a Message from an email
     *
     * @param email Email to be set to raw of message
     * @return Message containing base64 encoded email.
     * @throws IOException
     * @throws MessagingException
     */
    private Message createMessageWithEmail(MimeMessage email)
            throws MessagingException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        email.writeTo(baos);
        String encodedEmail = Base64.encodeBase64URLSafeString(baos.toByteArray());
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }

    public String getBody(HttpServletRequest request) throws IOException {

        String body = null;
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;

        try {
            InputStream inputStream = request.getInputStream();
            if (inputStream != null) {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                char[] charBuffer = new char[128];
                int bytesRead = -1;
                while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                    stringBuilder.append(charBuffer, 0, bytesRead);
                }
            } else {
                stringBuilder.append("");
            }
        } catch (IOException ex) {
            throw ex;
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ex) {
                    throw ex;
                }
            }
        }

        body = stringBuilder.toString();
        return body;
    }

    private MimeMessage createMimeMessage(String to, String from, String subject, String body) {
        try {
            Properties props = new Properties();
            Session session = Session.getDefaultInstance(props, null);
            MimeMessage message = new MimeMessage(session);
            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));

            // Set To: header field of the header.
            message.addRecipient(javax.mail.Message.RecipientType.TO,
                    new InternetAddress(to));

            // Set Subject: header field
            message.setSubject(subject);

            // Send the actual HTML message, as big as you like
            message.setContent(body,
                    "text/html");
            return message;
        } catch (MessagingException ex) {
            Logger.getLogger(GoogleAuthHelper.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
