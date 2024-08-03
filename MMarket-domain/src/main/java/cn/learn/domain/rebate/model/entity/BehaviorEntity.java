package cn.learn.domain.rebate.model.entity;

import cn.learn.domain.rebate.model.valobj.BehaviorTypeVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 行为实体对象，作为整个行为返利流程的入参
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BehaviorEntity {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 行为类型；sign 签到、openai_pay 支付、share 分享
     */
    private BehaviorTypeVO behaviorType;

    /**
     * 业务ID；note：用于保证幂等，写入用户行为返利的订单流水（签到对应的的是日期字符串）
     */
    private String outBusinessNo;

}
