package com.linker.common;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class MessageMeta {
    Address originalAddress;
    Address targetAddress;
    String note;
    Integer ttl = 10;
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
}
