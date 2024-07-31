package cn.learn.trigger.listener;

import cn.learn.domain.activity.service.IRaffleActivitySkuStockService;
import cn.learn.types.event.BaseEvent;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 *  <p>监听 RabbitMQ 队列消息的处理器。当活动 SKU 库存耗尽时，该类会接收到相应的消息并进行处理。</p>
 *  <h2>此MQ消息用于抽奖系统内部使用，用于在库存为0时立即更新，终止趋势更新的流程。</h2>
 */
@Slf4j
@Component
public class ActivitySkuStockZeroCustomer {

    @Value("${spring.rabbitmq.topic.activity_sku_stock_zero}")
    private String topic;

    @Resource
    private IRaffleActivitySkuStockService skuStock;

    // note: 需要学习 RabbitMQ 的使用
    @RabbitListener(queuesToDeclare = @Queue(value = "activity_sku_stock_zero"))
    public void listener(String message) {
        try {
            log.info("监听活动sku库存消耗为0消息 topic: {} message: {}", topic, message);
            // 转换对象
            // note: new TypeReference<BaseEvent.EventMessage<Long>>() {} 创建了一个 TypeReference 的匿名子类实例。
            //  由于 Java 的类型擦除机制，泛型类型参数在运行时会被擦除，不能直接获取。因此，通过创建一个匿名子类，可以在运行时保留类型信息。
            BaseEvent.EventMessage<Long> eventMessage = JSON.parseObject(message, new TypeReference<BaseEvent.EventMessage<Long>>() {}.getType());
            Long sku = eventMessage.getData();

            // 接收到 MQ 消息的处理流程：直接置库存为0，并清空延迟队列
            // 更新库存
            skuStock.clearActivitySkuStock(sku);
            log.info("MQ 消息处理：设置数据库 sku 缓存为 0");

            // 清空队列（此时就不需要延迟更新数据库记录了）
            // fixme: 所有的sku使用的是同一个队列，所以这里清空会将所有sku都清空，而我们的目的是只将当前sku相关的记录清楚，而不是全部清空。（21jie）
            // todo: 优化的措施是，为每个SKU设置一个队列
            skuStock.clearQueueValue();
            log.info("MQ 消息处理：清空延迟队列中待更新的 sku 库存数据");
        } catch (Exception e) {
            log.error("监听活动sku库存消耗为0消息，消费失败 topic: {} message: {}", topic, message);
            throw e;
        }
    }

}
