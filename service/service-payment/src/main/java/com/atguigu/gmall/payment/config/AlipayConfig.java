package com.atguigu.gmall.payment.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @author Yuehong Zhang
 * @date 2020/5/8 9:48
 */
@Configuration
@PropertySource("classpath:alipay.properties") // classpath: refers to the resource directory, indicating which configuration file to read
public class AlipayConfig {

    // Get the data in the configuration file
    @Value("${alipay_url}")
    private String alipay_url;

    @Value("${app_private_key}")
    private String app_private_key;

    @Value("${app_id}")
    private String app_id;


    public final static String format="json";
    public final static String charset="utf-8";
    public final static String sign_type="RSA2";


    public static String return_payment_url;

    public static String notify_payment_url;

    public static String return_order_url;

    public static String alipay_public_key;

    @Value("${alipay_public_key}")
    public void setAlipay_public_key(String alipay_public_key) {
        AlipayConfig.alipay_public_key = alipay_public_key;
    }

    @Value("${return_payment_url}")
    public void setReturn_url(String return_payment_url) {
        AlipayConfig.return_payment_url = return_payment_url;
    }

    @Value("${notify_payment_url}")
    public void setNotify_url(String notify_payment_url) {
        AlipayConfig.notify_payment_url = notify_payment_url;
    }

    @Value("${return_order_url}")
    public void setReturn_order_url(String return_order_url) {
        AlipayConfig.return_order_url = return_order_url;
    }

    // Inject AlipayClient into the spring container.
    // <bean id="alipayClient" class="com.alipay.api.AlipayClient"> </bean>
    @Bean
    public AlipayClient alipayClient(){
        // AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", APP_ID, APP_PRIVATE_KEY, FORMAT, CHARSET, ALIPAY_PUBLIC_KEY, SIGN_TYPE); //Get initialized AlipayClient
        AlipayClient alipayClient=new DefaultAlipayClient(alipay_url,app_id,app_private_key,format,charset, alipay_public_key,sign_type );
        return alipayClient;
    }
}