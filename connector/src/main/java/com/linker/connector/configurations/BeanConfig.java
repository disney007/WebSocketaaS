package com.linker.connector.configurations;


import com.linker.common.codec.Codec;
import com.linker.common.codec.FstCodec;
import com.linker.common.codec.JsonCodec;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {
    @Autowired
    ApplicationConfig applicationConfig;


    @Bean
    public Codec codec() {
        if (StringUtils.equalsIgnoreCase("json", applicationConfig.getCodec())) {
            return new JsonCodec();
        }

        return new FstCodec();
    }
}
