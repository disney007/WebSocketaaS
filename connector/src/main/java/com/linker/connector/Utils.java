package com.linker.connector;

import com.linker.common.Address;
import com.linker.common.MessageContext;

public class Utils {
    public static Address getOriginalAddress(MessageContext context){
        String domainName = context.getValue("DOMAIN_NAME");
        String connectorName = context.getValue("CONNECTOR_NAME");
        return new Address(domainName, connectorName);
    }
}
