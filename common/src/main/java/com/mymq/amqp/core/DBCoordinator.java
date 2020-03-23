package com.mymq.amqp.core;

import java.util.List;

/**
 * 可靠消息协调者接口
 */
public interface DBCoordinator {

    /**设置消息为prepare状态*/
    void setMsgPrepare(String msgId);
    //删除消息
    void delMsgPrepare(String msgId);

    /**设置消息为ready状态，删除prepare状态*/
    void setMsgReady(String msgId, RabbitMetaMessage rabbitMetaMessage);

    /**消息发送成功，删除ready状态消息*/
    void setMsgSuccess(String msgId);

    void delHashItem(String hashKey, String msgId);

    Long  incrementHashItem(String hashKey, String msgId, Object item);

    boolean putHashItem(String hashKey, String msgId, Object item);

    void setMessageStatus(String msgId, int status, RabbitMetaMessage rabbitMetaMessageTemp);

    /**从db中获取消息实体*/
    RabbitMetaMessage getMetaMsg(String msgId);

    /**获取ready状态消息*/
    List getMsgReady() throws Exception;

    /**获取prepare状态消息*/
    List getMsgPrepare() throws Exception;

    /**消息重发次数+1*/
    Long incrResendKey(String key, String hashKey);

    Long getResendValue(String key, String hashKey);



}
