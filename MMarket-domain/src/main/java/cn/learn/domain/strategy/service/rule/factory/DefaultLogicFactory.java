package cn.learn.domain.strategy.service.rule.factory;

import cn.learn.domain.strategy.model.entity.RuleActionEntity;
import cn.learn.domain.strategy.service.annotation.LogicStrategy;
import cn.learn.domain.strategy.service.rule.ILogicFilter;
import cn.learn.types.common.Constants;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 98389
 * @description note：DefaultLogicFactory 类的主要作用就是在工程层面对 Spring 管理的各种过滤器类
 *               进行统一的管理，更方便的使用预定义的各种规则过滤器。
 */
@Service
public class DefaultLogicFactory {

    /**
     * note：使用 ConcurrentHashMap 来存储【过滤器映射】，其中键为过滤器的名称，值为相应的过滤器实例。
     *  ConcurrentHashMap 用于确保线程安全，因为多个线程可能会同时访问和修改这个映射。
     */
    public Map<String, ILogicFilter<?>> logicFilterMap = new ConcurrentHashMap<>();

    /**
     * note: Spring 容器启动时，会扫描所有实现了 ILogicFilter 接口并标注为 Spring Bean 的类
     *  （如 RuleWeightFilter 和 RuleBlacklistFilter），将它们收集到一个列表中，并注入到 DefaultLogicFactory 的构造器中。
     */
    @Autowired
    public DefaultLogicFactory(List<ILogicFilter<?>> logicFilters) {
        for (ILogicFilter<?> logicFilter : logicFilters) {
            LogicStrategy strategy = AnnotationUtils.findAnnotation(logicFilter.getClass(), LogicStrategy.class);
            if (null != strategy) {
                logicFilterMap.put(strategy.logicMode().getCode(), logicFilter);
            }
        }
    }

    /**
     * note: GPT给出的修改建议：
     *  @SuppressWarnings("unchecked")
     *  public <T extends RuleActionEntity.RaffleEntity> Map<String, ILogicFilter<T>> openLogicFilter(Class<T> clazz) {
     *      Map<String, ILogicFilter<T>> result = new HashMap<>();
     *      for (Map.Entry<String, ILogicFilter<?>> entry : logicFilterMap.entrySet()) {
     *          ILogicFilter<?> filter = entry.getValue();
     *          if (clazz.isInstance(filter)) {
     *              result.put(entry.getKey(), (ILogicFilter<T>) filter);
     *          } else {
     *              throw new IllegalArgumentException("Filter " + entry.getKey() + " is not of the expected type.");
     *          }
     *      }
     *      return result;
     *  }
     */
    public <T extends RuleActionEntity.RaffleEntity> Map<String, ILogicFilter<T>> openLogicFilter() {
        return (Map<String, ILogicFilter<T>>) (Map<?, ?>) logicFilterMap;
    }

    public <T extends RuleActionEntity.RaffleEntity> ILogicFilter<T> createFilter(String logicModel) {
        if (LogicModel.RULE_WIGHT.getCode().equals(logicModel)) {
            return (ILogicFilter<T>) logicFilterMap.get(LogicModel.RULE_WIGHT.getCode());
        }
        if (LogicModel.RULE_BLACKLIST.getCode().equals(logicModel)) {
            return (ILogicFilter<T>) logicFilterMap.get(LogicModel.RULE_BLACKLIST.getCode());
        }
        throw new RuntimeException("不存在的过滤器类型");
    }

    @Getter
    @AllArgsConstructor
    public enum LogicModel {

        RULE_WIGHT(Constants.RuleModel.RULE_WEIGHT, "【抽奖前规则】根据抽奖权重返回可抽奖范围KEY"),
        RULE_BLACKLIST(Constants.RuleModel.RULE_BLACKLIST, "【抽奖前规则】黑名单规则过滤，命中黑名单则直接返回"),
        ;

        private final String code;
        private final String info;

    }

}
