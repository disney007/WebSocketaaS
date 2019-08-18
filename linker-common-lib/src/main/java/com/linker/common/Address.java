package com.linker.common;

import lombok.*;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class Address implements Serializable {
    private static final long serialVersionUID = -4325109190976894583L;
    String domainName;
    String connectorName = null;
    Long socketId = -1L;

    public Address(String domainName, String connectorName) {
        this.domainName = domainName;
        this.connectorName = connectorName;
    }
}
