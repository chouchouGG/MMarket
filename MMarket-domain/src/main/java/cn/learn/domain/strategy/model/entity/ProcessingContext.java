package cn.learn.domain.strategy.model.entity;

import lombok.*;

/**
 * @program: MMarket
 * @description: 封装责任链节点处理过程中所需的所有参数和状态。在增添了决策树的组合模式后也用于决策树中保存状态和控制程序执行流。
 * @author: chouchouGG
 * @create: 2024-06-16 14:11
 **/
@Builder
@Data
@AllArgsConstructor
public class ProcessingContext {

    // 一经初始化后，不会在发生变化
    private final String userId;
    private final Long strategyId;

    // 奖品ID
    private Integer awardId;

    private String awardRuleValue;

    // note: @Builder.Default 使用构建器 builder 生成对象时，用于如果没有显式设置该字段的值，则使用默认值。避免构建器忽略预设的默认值。
    @Builder.Default
    private ProcessStatus status = ProcessStatus.CONTINUE;

    // 记录处理流程终止时的规则模型
    private String ruleModel;

    // 处理流程的结果描述，用于提供反馈
    // note：当 awardId 为 null 时，该字段有效
    private String resultDesc;

    // 标记是否需要分配兜底奖品
    @Builder.Default
    private boolean needsFallbackAward = false;

    /**
     * @program: MMarket
     * @description: 描述责任链模式和组合决策树模式的流程的处理状态
     * @author: chouchouGG
     * @create: 2024-06-21 09:17
     **/
    @Getter
    @AllArgsConstructor
    public enum ProcessStatus {

        CONTINUE("0000", "【继续处理流程】"),
        TERMINATED("0001", "【终止处理流程】")
        ;

        private final String code;
        private final String info;

    }


}
