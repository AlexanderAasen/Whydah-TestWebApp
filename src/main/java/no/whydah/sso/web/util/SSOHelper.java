package no.whydah.sso.web.util;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import no.whydah.sso.web.data.ApplicationCredential;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;

public class SSOHelper {
    private static final URI BASE_URI = UriBuilder.fromUri("http://localhost/tokenservice/").port(9998).build();
    private WebResource r;

    public SSOHelper(){
        Client c = Client.create();
        r = c.resource(BASE_URI);
    }

    public void logonApplication() {
        PostMethod p = setUpApplicationLogon();
        HttpClient c = new HttpClient();
        try {
            int v = c.executeMethod(p);
            if (v == 201) {
                System.out.println("Post" + p.getRequestHeader("Location").getValue());
            }
            if (v == 400) {
                System.out.println("Internal error");
            }
            if (v == 500 || v == 501) {
                System.out.println("Internal error");
// retry
            }
            System.out.println(p.getResponseBodyAsString());

        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            p.releaseConnection();
        }
    }

    private PostMethod setUpApplicationLogon() {
        String requestXML = "";
        PostMethod p = new PostMethod(r.path("/logon").toString());
        p.addParameter("applicationcredential",requestXML);
        return p;
    }

    public String getUserToken(String usertokenid) {
        if (usertokenid==null){
            usertokenid="dummy";
        }
        PostMethod p = setupRealApplicationLogon();
        HttpClient c = new HttpClient();
        try {
            int v = c.executeMethod(p);
            if (v == 201) {
                System.out.println("Post" + p.getRequestHeader("Location").getValue());
            }
            if (v == 400) {
                System.out.println("Internal error");
            }
            if (v == 406) {
                System.out.println("Not accepted");
            }
            if (v == 500 || v == 501) {
                System.out.println("Internal error");
// retry
            }
            System.out.println("ApplicationToken:"+p.getResponseBodyAsString());
            PostMethod p2 = setUpGetUserToken(p,usertokenid);
            v = c.executeMethod(p2);
            if (v == 201) {
                System.out.println("Post" + p2.getRequestHeader("Location").getValue());
            }
            if (v == 400 || v == 404 ) {
                System.out.println("Internal error");
            }
            if (v == 406) {
                System.out.println("Not accepted");
            }
            if (v == 415 ) {
                System.out.println("Internal error, unsupported media type");
            }
            if (v == 500 || v == 501) {
                System.out.println("Internal error");// retry
            }
//            System.out.println("Request:"+p2.
            System.out.println("v:"+v);
            System.out.println("Response:"+p2.getResponseBodyAsString());
            return p2.getResponseBodyAsString();


        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            p.releaseConnection();
        }
        return null;
    }


    private PostMethod setUpGetUserToken(PostMethod p,String userTokenid) throws IOException {
        String appTokenXML = p.getResponseBodyAsString();
        String appid = appTokenXML.substring(appTokenXML.indexOf("<applicationtoken>") + "<applicationtoken>".length(), appTokenXML.indexOf("</applicationtoken>"));
        String path = r.path("/iam/").toString() + appid + "/getusertokenbytokenid";

        System.out.println("POST:"+path);

        PostMethod p2 = new PostMethod(path);
        p2.addParameter("apptoken",appTokenXML);
        p2.addParameter("usertokenid",userTokenid);

        System.out.println("apptoken:"+appTokenXML);
        System.out.println("usertokenid:"+userTokenid);
        return p2;
    }

    private PostMethod setupRealApplicationLogon() {
        ApplicationCredential acred = new ApplicationCredential();
        acred.setApplicationID("Styrerommet");
        acred.setApplicationPassord("dummy");


        PostMethod p = new PostMethod(r.path("/logon").toString());
        p.addParameter("applicationcredential",acred.toXML());
        return p;
    }
}

