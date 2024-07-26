package cn.learn.domain.activity.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 活动状态值对象
 * @create 2024-03-16 11:16
 */
@Getter
@AllArgsConstructor
public enum ActivityStateVO {

    create("create", "创建"), // 活动创建状态
    open("open", "开启"),     // 活动开启状态（活动只有为开启状态时，当前活动才是可用的）
    close("close", "关闭"),   // 活动关闭状态
    ;

    private final String code;
    private final String desc;

}
