package cn.learn.domain.strategy.service.rule.chain;

import cn.learn.domain.strategy.model.entity.ProcessingContext;
import cn.learn.domain.strategy.service.rule.chain.logicChain.ILogicChain;
import cn.learn.domain.strategy.service.rule.chain.logicChain.ILogicChainAware;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


/**
 * @program: MMarket
 * @description: 实现基本的责任链操作
 * @author: chouchouGG
 * @create: 2024-06-15 19:20
 **/
@Slf4j
public abstract class AbstractLogicChain implements ILogicChain, ILogicChainAware {

    private ILogicChain next;

    @Override
    public ILogicChain setNext(ILogicChain next) {
        this.next = next;
        return next;
    }

    @Override
     public void process(ProcessingContext context) {
         // 1. 调用当前节点的处理方法
        handle(context);

        // 2. 根据流程的状态 status 决定是否继续传递请求
         if (context.getStatus() == ProcessingContext.ProcessStatus.CONTINUE && next != null) {
             next.process(context);
             return;
         }

         return;
    }

    @Override
    public ILogicChain next() {
        return next;
    }

    /**
     * note：这个方法用于再责任链节点中获取当前节点对应的规则，不使用这个方法，直接讲规则写死在责任链节点中也是可以的
     * @return
     */
    @Override
    public String getRuleModelName() {
        Component component = this.getClass().getAnnotation(Component.class);
        if (component != null) {
            return component.value();
        }
        return null;
    }

}
