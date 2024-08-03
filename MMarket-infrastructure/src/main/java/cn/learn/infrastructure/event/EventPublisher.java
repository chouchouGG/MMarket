package cn.learn.infrastructure.event;

import cn.learn.types.event.BaseEvent;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 消息发送
 */
@Slf4j
@Component
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public EventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 发布事件消息到指定的 RabbitMQ 主题。
     *
     * @param topic 发送消息的 RabbitMQ 主题。
     * @param eventMessage 要发送的事件消息对象，包含事件相关数据。
     * @throws RuntimeException 当消息发送失败时抛出异常。
     */
    public void publish(String topic, BaseEvent.EventMessage<?> eventMessage) {
        try {
            // 【事件消息对象转换为JSON字符串】：使用Alibaba的FastJSON库将eventMessage对象序列化为JSON格式的字符串
            String messageJson = JSON.toJSONString(eventMessage);

            // 使用RabbitTemplate将消息发送到指定的RabbitMQ主题：第一个参数是RabbitMQ的主题名称（即交换机），第二个参数是要发送的消息内容
            rabbitTemplate.convertAndSend(topic, messageJson);

            // 记录发送成功的日志信息
            log.info("发送MQ消息 topic:{} message:{}", topic, messageJson);
        } catch (Exception e) {
            // 记录发送失败的日志信息
            log.error("发送MQ消息失败 topic:{} message:{}", topic, JSON.toJSONString(eventMessage), e);

            // 将异常重新抛出，以便上层调用者能够捕获和处理
            throw e;
        }
    }


    // 方法重载
    public void publish(String topic, String eventMessageJSON){
        try {
            rabbitTemplate.convertAndSend(topic, eventMessageJSON);
            log.info("发送MQ消息 topic:{} message:{}", topic, eventMessageJSON);
        } catch (Exception e) {
            log.error("发送MQ消息失败 topic:{} message:{}", topic, eventMessageJSON, e);
            throw e;
        }
    }

}
