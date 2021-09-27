package com.atguigu.gmall.activity.redis;

import com.atguigu.gmall.activity.util.CacheHelper;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class MessageReceive {

    /**Method of receiving messages*/
    public void receiveMessage(String message){
        System.out.println("----------Receive a message message: "+message);
        if(!StringUtils.isEmpty(message)) {
             /*
              Message format
                 skuId:0 means no products
                 skuId:1 means there is a product

                 Message sent: 46:1 is actually quoted! ""46:1""
              */
            message = message.replaceAll("\"","");
            String[] split = StringUtils.split(message, ":");
// String[] split = message.split(":");

            if (split == null || split.length == 2) {
                // put(skuId,status);
                CacheHelper.put(split[0], split[1]);
            }
        }
    }

}