package cn.learn.trigger.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @program: MMarket
 * @description: 抽奖应答结果
 * @author: chouchouGG
 * @create: 2024-06-23 15:52
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RaffleResDTO {

    // 奖品ID
    private Integer awardId;

    // 排序编号【策略奖品配置的奖品顺序编号】
    private Integer awardIndex;

}
