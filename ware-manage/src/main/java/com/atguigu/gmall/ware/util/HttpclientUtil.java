package com.atguigu.gmall.ware.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @param
 * @return
 */
public class HttpclientUtil {

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


    public static String doPost(String url, Map<String,String> paramMap) {

        // Create Httpclient object
        CloseableHttpClient httpclient = HttpClients.createDefault();

        // Create http Post request
        HttpPost httpPost = new HttpPost(url);

        CloseableHttpResponse response = null;
        try {
            List<BasicNameValuePair> list=new ArrayList<>();
            for (Map.Entry<String, String> entry: paramMap.entrySet()) {
                list.add(new BasicNameValuePair(entry.getKey(),entry.getValue()));
            }
            HttpEntity httpEntity=new UrlEncodedFormEntity(list,"utf-8");

            httpPost.setEntity(httpEntity);
            // execute request
            response = httpclient.execute(httpPost);

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
}