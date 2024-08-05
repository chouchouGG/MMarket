package cn.learn.trigger.listener;

import cn.learn.domain.activity.model.entity.SkuRechargeEntity;
import cn.learn.domain.activity.service.IRaffleActivityAccountQuotaService;
import cn.learn.domain.rebate.event.SendRebateMessageEvent;
import cn.learn.domain.rebate.model.valobj.RebateTypeVO;
import cn.learn.types.enums.ResponseCode;
import cn.learn.types.event.BaseEvent;
import cn.learn.types.exception.AppException;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @program: MMarket
 * @description: 监听用户行为返利的MQ消息
 * @author: chouchouGG
 * @create: 2024-08-03 17:25
 **/
@Slf4j
@Component
public class RebateMessageCustomer {

    @Value("${spring.rabbitmq.topic.send_rebate}")
    private String topic;
    @Resource
    private IRaffleActivityAccountQuotaService raffleActivityAccountQuotaService;

    @RabbitListener(queuesToDeclare = @Queue(value = "${spring.rabbitmq.topic.send_rebate}"))
    public void listener(String message) throws AppException {
        try {
//            log.info("监听用户行为返利消息 topic: {} message: {}", topic, message);
            // 1. 转换消息
            BaseEvent.EventMessage<SendRebateMessageEvent.RebateMessage> eventMessage =
                    JSON.parseObject(message, new TypeReference<BaseEvent.EventMessage<SendRebateMessageEvent.RebateMessage>>() {
            }.getType());
            SendRebateMessageEvent.RebateMessage rebateMessage = eventMessage.getData();
            // fixme: 暂时由于没有引入积分模块，只进行了额度的奖励处理逻辑，对于非额度的奖励不进行处理，后续进行扩展。
            if (!RebateTypeVO.SKU.getCode().equals(rebateMessage.getRebateType())) {
                log.info("监听用户行为返利消息 - 非sku奖励暂时不处理 topic: {} message: {}", topic, message);
                return;
            }
            // 处理SKU类型返利逻辑
            if (RebateTypeVO.SKU.getCode().equals(rebateMessage.getRebateType())) {
                log.info("监听用户行为返利消息 - 处理sku奖励 topic: {} message: {}", topic, message);
                SkuRechargeEntity skuRechargeEntity = SkuRechargeEntity.builder()
                        .userId(rebateMessage.getUserId())
                        .sku(Long.valueOf(rebateMessage.getRebateConfig()))
                        .outBusinessNo(rebateMessage.getBizId())
                        .build();
                raffleActivityAccountQuotaService.createAccountQuotaRechargeOrder(skuRechargeEntity);
            }
        } catch (AppException e) {
            if (ResponseCode.INDEX_DUP.getCode().equals(e.getCode())) {
                log.warn("监听用户行为返利消息，消费重复 topic: {} message: {}", topic, message, e);
                return;
            } else {
                log.warn("监听用户行为返利消息，消费出现异常 topic: {} message: {}", topic, message, e);
                throw e;
            }
        } catch (Exception e) {
            log.error("监听用户行为返利消息，消费失败 topic: {} message: {}", topic, message, e);
            throw e;
        }
    }


}
