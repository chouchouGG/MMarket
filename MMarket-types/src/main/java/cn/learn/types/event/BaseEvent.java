package cn.learn.types.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 基础事件
 * @create 2024-03-30 12:42
 */
@Data
public abstract class BaseEvent<T> {

    /**
     * 构建事件消息的方法，需要子类实现。
     *
     * @param data 事件数据
     * @return 构建的事件消息
     */
    public abstract EventMessage<T> buildEventMessage(T data);

    /**
     * 获取事件主题的方法，需要子类实现。
     *
     * @return 事件主题
     */
    public abstract String topic();


    // note: EventMessage 类表示事件消息，定义了标准的消息格式（包含 id、timestamp 和 data 三个字段）
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EventMessage<T> {
        private String id;
        private Date timestamp;
        private T data;
    }

}
