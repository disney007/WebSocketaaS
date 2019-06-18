package com.linker.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageMeta {
    Address originalAddress;
    Address targetAddress;
}
