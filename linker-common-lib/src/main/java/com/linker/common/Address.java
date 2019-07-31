package com.linker.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Address implements Serializable {
    private static final long serialVersionUID = -4325109190976894583L;
    String domainName;
    String connectorName;
    Long socketId;

    public Address(String domainName, String connectorName) {
        this.domainName = domainName;
        this.connectorName = connectorName;
    }
}
