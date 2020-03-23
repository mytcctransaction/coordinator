package com.mymq.amqp.resp;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Builder
public class MQMessageResp<T> {
    //类型
    private int type;
    //消息体
    private T message;
    //业务表id
    private Object bizId;
    private List<T> resultList;
}
