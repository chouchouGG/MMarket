package cn.learn.types.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * <h2>{@link BaseEvent} 提供了一种模板方法模式。它定义了
 * ‘构建事件消息标准格式的方法’ {@link BaseEvent#buildEventMessage(Object)} 和
 * ‘获取事件主题的方法’ {@link BaseEvent#topic()}。</h2>
 */
@Data
public abstract class BaseEvent<T> {

    /**
     * <h1>构建事件消息的方法</h1>
     *
     * @param data 事件消息
     * @return 标准格式的事件消息对象
     */
    public abstract EventMessage<T> buildEventMessage(T data);


    /**
     * <h1>获取事件主题的方法</h1>
     *
     * @return 事件主题
     */
    public abstract String topic();


    /**
     * <h2>note: {@link cn.learn.types.event.BaseEvent.EventMessage} 类用于表示事件消息的标准格式，
     *  定义了标准的消息格式
     *  （包含 {@link cn.learn.types.event.BaseEvent.EventMessage#id}、
     *  {@link cn.learn.types.event.BaseEvent.EventMessage#timestamp} 和
     *  {@link cn.learn.types.event.BaseEvent.EventMessage#data} 三个字段）</h2>
     */
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
