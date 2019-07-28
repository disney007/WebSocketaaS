package com.linker.common.messages;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class GroupMessage implements Serializable {

    private static final long serialVersionUID = -7777757135064012832L;
    Set<String> to;
    String content;
}
