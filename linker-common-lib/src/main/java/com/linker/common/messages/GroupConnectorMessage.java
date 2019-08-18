package com.linker.common.messages;

import com.linker.common.models.ConnectorUserId;
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
public class GroupConnectorMessage implements Serializable {

    private static final long serialVersionUID = 5779574895010485207L;

    Set<ConnectorUserId> connectorUserIds;
    MessageForward content;
}
