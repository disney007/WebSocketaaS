package com.linker.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Address {
    String domainName;
    String connectorName;
    Long socketId;

    public Address(String domainName, String connectorName) {
        this.domainName = domainName;
        this.connectorName = connectorName;
    }
}
