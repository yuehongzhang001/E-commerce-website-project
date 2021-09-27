package com.atguigu.gmall.activity.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
public class RedisChannelConfig {

    /*
         docker exec -it bc92 redis-cli
         subscribe seckillpush // subscribe to receive messages
         publish seckillpush admin // Publish message
     */
    /**
     * Inject subscription topics
     * @param connectionFactory redis link factory
     * @param listenerAdapter message listener adapter
     * @return subscribe topic object
     */
    @Bean
    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
                                            MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        //Subscribe topic
        container.addMessageListener(listenerAdapter, new PatternTopic("seckillpush"));
        //This container can add multiple messageListeners
        return container;
    }

    /**
     * Return to the message listener
     * @param receiver to create a receiving message object
     * @return
     */
    @Bean
    MessageListenerAdapter listenerAdapter(MessageReceive receiver) {
        //This place is to pass in a message receiving processor to the messageListenerAdapter, and use the reflection method to call "receiveMessage"
        //There are also several overloaded methods, here the default method of calling the processor is called handleMessage, you can go to the source code to see
        return new MessageListenerAdapter(receiver, "receiveMessage");
    }

    @Bean //template for injecting operation data
    StringRedisTemplate template(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

}