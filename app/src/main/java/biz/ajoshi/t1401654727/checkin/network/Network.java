package biz.ajoshi.t1401654727.checkin.network;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by ajoshi on 4/20/2017.
 */
public class Network {
    public static final String SOUTHWEST_CHECKIN_URL1 = "https://www.southwest.com/flight/retrieveCheckinDoc.html?confirmationNumber=%1s&firstName=%2s&lastName=%3s";
    private static final String SOUTHWEST_CHECKIN_URL2 = "https://www.southwest.com/flight/selectPrintDocument.html?int=";
    private static final String SOUTHWEST_CHECKIN_URL3 = "https://www.southwest.com/flight/selectPrintDocument.html";

    /**
     * Checks in to southwest, and returns the httpresponse
     *
     * @param fName String first name of passenger
     * @param lName String last name of passenger
     * @param cCode String confirmation code for flight
     * @return HttpReponse containing checkin info
     * @throws IOException
     */
    public HttpResponse getCheckInPageResponse(String fName, String lName, String cCode) throws IOException {
        //TODO use httpurlconnection
        HttpClient httpclient = new DefaultHttpClient();
        // make first call which will redirect me
        HttpGet httpget = new HttpGet(String.format(SOUTHWEST_CHECKIN_URL1, cCode, fName, lName));
        // Execute HTTP Post Request
        HttpResponse response = httpclient.execute(httpget);
        String firstCookies = getCookiesFromHeaders(response.getHeaders("Set-Cookie"));
        response.getEntity().consumeContent();

        // take redirect
        HttpGet httpget2 = new HttpGet(SOUTHWEST_CHECKIN_URL2);
        httpget2.setHeader("Cookie", firstCookies);
        response = httpclient.execute(httpget);
        String secondCookies = getCookiesFromHeaders(response.getHeaders("Set-Cookie"));
        response.getEntity().consumeContent();

        // submit checkin confirmation
        HttpPost httpPost = new HttpPost(SOUTHWEST_CHECKIN_URL3);
        httpPost.setHeader("Cookie", secondCookies);
        ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
        postParameters.add(new BasicNameValuePair("checkinPassengers[0].selected", "true"));
        postParameters.add(new BasicNameValuePair("printDocuments", "Check In"));
        httpPost.setEntity(new UrlEncodedFormEntity(postParameters));
        response = httpclient.execute(httpPost);
        return response;
    }

    private String getCookiesFromHeaders(Header[] cookieHeaders) {
        StringBuilder cookieBuilder = new StringBuilder();
        for (Header cook : cookieHeaders) {
            // JSESSIONID=F145AF252BD9F751826BF0; Path=/flight/; HttpOnly
            String cookString = cook.getValue();
            String cookieSubstring = cookString.substring(0, cookString.indexOf(';') + 2);
            // we could pull just the tuple and append the semicolon later, or just keep it in
            // I do the latter
            cookieBuilder.append(cookieSubstring);
        }
        return cookieBuilder.toString();
    }
}
