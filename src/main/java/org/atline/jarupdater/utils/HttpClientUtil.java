package org.atline.jarupdater.utils;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.FileOutputStream;
import java.io.InputStream;

public class HttpClientUtil {
	static Logger logger = Logger.getLogger(HttpClientUtil.class);
	
    public static boolean downloadBinary(String url, FileOutputStream fos) {
        if ("ok".equals(httpGet(url, fos))) {
            return true;
        } else {
            return false;
        }
    }

    public static String getInfo(String url) {
        return httpGet(url, null);
    }

    public static String httpGet(String url, FileOutputStream fos) {
        DefaultHttpClient httpclient = null;
        HttpGet httpGet = null;
        HttpResponse response = null;
        HttpEntity entity = null;
        String result = "";

        try {
            httpclient = new DefaultHttpClient();
            httpclient.getParams().setParameter(ClientPNames.COOKIE_POLICY,
                    CookiePolicy.BROWSER_COMPATIBILITY);
            httpGet = new HttpGet(url);
            HttpConnectionParams.setConnectionTimeout(httpclient.getParams(),
                    120000);
            HttpConnectionParams.setSoTimeout(httpGet.getParams(),
                    120000);

            response = httpclient.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                logger.info("Status Code: " + statusCode);
                logger.info("Status Reason: " + response.getStatusLine().getReasonPhrase());
                return "";
            } else {
                entity = response.getEntity();
                if (null != entity) {
                    if (null == fos) {
                        byte[] bytes = EntityUtils.toByteArray(entity);
                        result = new String(bytes, "UTF-8");
                    } else {
                        InputStream fis = entity.getContent();
                        IOUtils.copy(fis, fos);
                        fos.flush();
                        result = "ok";
                    }
                } else {
                }
                return result;
            }
        } catch (Exception e) {
        	logger.error(e);
            return "";
        } finally {
            if (null != httpclient) {
                httpclient.getConnectionManager().shutdown();
            }
        }
    }
}
