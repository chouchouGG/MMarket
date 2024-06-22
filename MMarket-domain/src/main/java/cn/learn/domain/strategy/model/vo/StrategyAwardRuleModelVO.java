package cn.learn.domain.strategy.model.vo;

import cn.learn.types.common.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 抽奖策略规则规则值对象；值对象，没有唯一ID，仅限于从数据库查询对象
 * @create 2024-01-13 09:30
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StrategyAwardRuleModelVO {

    private String ruleModels;

    /**
     * @return 返回一个包含多个规则模型字符串数组
     */
    public String[] getRuleModelsArray() {
        return ruleModels.split(Constants.SPLIT);
    }

}
