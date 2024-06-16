package cn.learn.domain.strategy.model.entity;

import jdk.nashorn.internal.ir.ContinueNode;
import lombok.*;

/**
 * @program: MMarket
 * @description: 封装责任链节点处理过程中所需的所有参数和状态。
 * @author: chouchouGG
 * @create: 2024-06-16 14:11
 **/
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogicChainContext {

    private String userId;
    private Long strategyId;
    @Builder.Default // note: 当使用构建器生成对象时，如果没有显式设置该字段的值，则使用默认值，避免构建器忽略预设的默认值。
    private Integer awardId = null;
    @Builder.Default
    private ProcessStatus status = ProcessStatus.CONTINUE;

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
