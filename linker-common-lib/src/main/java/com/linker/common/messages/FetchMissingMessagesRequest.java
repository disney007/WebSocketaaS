package com.linker.common.messages;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class FetchMissingMessagesRequest {
    Integer count;
}
