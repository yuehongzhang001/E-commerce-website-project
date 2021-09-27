package com.atguigu.gmall.common.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * HttpClient class WeChat:
 *
 */
public class HttpClientUtil {

    public static String doGet(String url) {

        // Create Httpclient object
        CloseableHttpClient httpclient = HttpClients.createDefault();
        // Create http GET request
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = null;
        try {
            // execute request
            response = httpclient.execute(httpGet);
            // Determine whether the return status is 200
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity, "UTF-8");
                EntityUtils.consume(entity);
                httpclient.close();
                return result;
            }
            httpclient.close();
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }

        return null;
    }


    public static void download(String url,String fileName) {

        // Create Httpclient object
        CloseableHttpClient httpclient = HttpClients.createDefault();
        // Create http GET request
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = null;
        try {
            // execute request
            response = httpclient.execute(httpGet);
            // Determine whether the return status is 200
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();

                // String result = EntityUtils.toString(entity, "UTF-8");
                byte[] bytes = EntityUtils.toByteArray(entity);
                File file =new File(fileName);
                // InputStream in = entity.getContent();
                FileOutputStream fout = new FileOutputStream(file);
                fout.write(bytes);

                EntityUtils.consume(entity);

                httpclient.close();
                fout.flush();
                fout.close();
                return;
            }
            httpclient.close();
        }catch (IOException e){
            e.printStackTrace();
            return;
        }

        return;
    }
}