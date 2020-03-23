package com.mymq.amqp.core;


public enum  CoordinatorType {

    REDIS("redis", "redis"), ZK("zk", "zookeeper");

    private String value="";
    private String desc;

    CoordinatorType(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public  String value(){
        return this.value;
    }

}
