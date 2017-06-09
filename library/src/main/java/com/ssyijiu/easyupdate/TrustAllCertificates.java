package com.ssyijiu.easyupdate;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by ssyijiu on 2017/6/1.
 * Github: ssyijiu
 * E-mail: lxmyijiu@163.com
 */

public class TrustAllCertificates implements X509TrustManager, HostnameVerifier {

    @Override
    public boolean verify(String hostname, SSLSession session) {
        return true;
    }


    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType)
        throws CertificateException {

    }


    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType)
        throws CertificateException {

    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }


    static void install() {
        try {
            TrustAllCertificates trustAll = new TrustAllCertificates();

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null,
                new TrustManager[] { trustAll },
                new SecureRandom());

            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(trustAll);

        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
    }
}
