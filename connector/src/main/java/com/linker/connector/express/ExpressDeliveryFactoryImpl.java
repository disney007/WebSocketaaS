package com.linker.connector.express;

import com.linker.common.messagedelivery.ExpressDelivery;
import com.linker.common.messagedelivery.KafkaExpressDelivery;
import com.linker.common.messagedelivery.NatsExpressDelivery;
import com.linker.connector.configurations.ApplicationConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExpressDeliveryFactoryImpl implements ExpressDeliveryFactory {

    ApplicationConfig applicationConfig;

    @Autowired
    public ExpressDeliveryFactoryImpl(ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
    }


    public ExpressDelivery createKafkaExpressDelivery() {
        final String connectorName = applicationConfig.getConnectorName();
        final String consumerTopics = connectorName;
        return new KafkaExpressDelivery(applicationConfig.getKafkaHosts(), consumerTopics, connectorName);
    }

    public ExpressDelivery createNatsExpressDelivery() {
        final String consumerTopics = applicationConfig.getConnectorName();
        return new NatsExpressDelivery(applicationConfig.getNatsHosts(), consumerTopics);
    }
}
