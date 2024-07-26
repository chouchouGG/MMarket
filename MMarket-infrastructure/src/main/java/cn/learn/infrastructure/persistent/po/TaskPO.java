package cn.learn.infrastructure.persistent.po;

import lombok.Data;

import java.util.Date;

/**
 * 任务表，发送MQ
 */
@Data
public class TaskPO {

    /** 自增ID */
    private String id;
    /** 消息主题 */
    private String topic;
    /** 消息主体 */
    private String message;
    /** 任务状态；create-创建、completed-完成、fail-失败 */
    private String state;
    /** 创建时间 */
    private Date createTime;
    /** 更新时间 */
    private Date updateTime;

}