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
@NoArgsConstructor
public class ProcessingContext {

    private String userId;
    private Long strategyId;

    // note: @Builder.Default 使用构建器 builder 生成对象时，用于如果没有显式设置该字段的值，则使用默认值。避免构建器忽略预设的默认值。
    @Builder.Default
    private Integer awardId = null;
    @Builder.Default
    private ProcessStatus status = ProcessStatus.CONTINUE;

    // 记录处理流程终止时的规则模型
    private String ruleModel;

    // 结果描述，用于向用户提供反馈
    // note：当 awardId 为 null 时，该字段有效
    private String resultDesc;


    /**
     * @program: MMarket
     * @description: 描述责任链模式和组合决策树模式的流程的处理状态
     * @author: chouchouGG
     * @create: 2024-06-21 09:17
     **/
    @Getter
    @AllArgsConstructor
    public enum ProcessStatus {

        CONTINUE("0000", "【继续处理】下一个责任链节点"),
        TERMINATED("0001", "【终止处理】，不再调用后续责任链节点")
        ;

        private final String code;
        private final String info;

    }


}
