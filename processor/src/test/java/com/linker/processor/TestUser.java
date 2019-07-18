package com.linker.processor;

import com.linker.common.Address;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class TestUser {
    String userId;
    Address address;
    String connectedMessageId;
    String disconnectedMessageId;
}
