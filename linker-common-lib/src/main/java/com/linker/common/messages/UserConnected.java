package com.linker.common.messages;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserConnected implements Serializable {

    private static final long serialVersionUID = 3111339628950150417L;
    String userId;
}
