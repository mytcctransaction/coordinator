package com.mymq.amqp.core;

import org.springframework.amqp.rabbit.support.CorrelationData;

public class CompleteCorrelationData extends CorrelationData {

    private String coordinator;

    public CompleteCorrelationData(String id, String coordinatorPara){
        super(id);
        coordinator = coordinatorPara;
    }

    public String getCoordinator(){
        return coordinator;
    }

    @Override
    public String toString(){
        return "CompleteCorrelationData id=" + super.getId() +",coordinator" + this.coordinator;
    }
}
