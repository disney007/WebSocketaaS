package com.linker.common.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ConnectorUserId implements Serializable {

    private static final long serialVersionUID = -8175198130893012353L;
    String userId;
    Long socketId;
}
