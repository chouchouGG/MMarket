package cn.learn.domain.rebate.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 行为类型的值对象
 */
@Getter
@AllArgsConstructor
public enum BehaviorTypeVO {

    SIGN("sign", "签到（日历）"), // fixme：主要实现的行为
    SHARE("share", "分享活动"),
    ;

    private final String code;
    private final String info;

}
