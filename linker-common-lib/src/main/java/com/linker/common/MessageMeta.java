package com.linker.common;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode
public class MessageMeta implements Serializable {
    private static final long serialVersionUID = -3515282102198514156L;
    Address originalAddress;
    Address targetAddress;
    String note;
    Integer ttl = 10;
    boolean confirmEnabled = false;
    DeliveryType deliveryType = DeliveryType.ALL;

    public MessageMeta() {

    }

    public MessageMeta(Address originalAddress) {
        this.originalAddress = originalAddress;
    }

    public MessageMeta(Address originalAddress, Address targetAddress) {
        this.originalAddress = originalAddress;
        this.targetAddress = targetAddress;
    }

    public MessageMeta(Address originalAddress, String note) {
        this.originalAddress = originalAddress;
        this.note = note;
    }

    public MessageMeta(Address originalAddress, Address targetAddress, String note, Integer ttl) {
        this.originalAddress = originalAddress;
        this.targetAddress = targetAddress;
        this.note = note;
        this.ttl = ttl;
    }

    public MessageMeta(Address originalAddress, Address targetAddress, String note, Integer ttl, boolean confirmEnabled) {
        this.originalAddress = originalAddress;
        this.targetAddress = targetAddress;
        this.note = note;
        this.ttl = ttl;
        this.confirmEnabled = confirmEnabled;
    }

    public MessageMeta(Address originalAddress, Address targetAddress, String note, Integer ttl, boolean confirmEnabled, DeliveryType deliveryType) {
        this.originalAddress = originalAddress;
        this.targetAddress = targetAddress;
        this.note = note;
        this.ttl = ttl;
        this.confirmEnabled = confirmEnabled;
        this.deliveryType = deliveryType;
    }

    public MessageMeta clone() {
        return new MessageMeta(originalAddress, targetAddress, note, ttl, confirmEnabled);
    }
}
