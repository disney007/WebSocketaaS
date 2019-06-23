package com.linker.common;

import lombok.Data;

@Data
public class MessageMeta {
    Address originalAddress;
    Address targetAddress;
    String note;

    public MessageMeta() {

    }

    public MessageMeta(Address originalAddress) {
        this.originalAddress = originalAddress;
    }

    public MessageMeta(Address originalAddress, String note) {
        this.originalAddress = originalAddress;
        this.note = note;
    }
}
