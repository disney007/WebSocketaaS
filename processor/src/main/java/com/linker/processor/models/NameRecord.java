package com.linker.processor.models;

import com.linker.common.Keywords;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class NameRecord {
    String appName;
    Long number;

    public boolean isValid() {
        return StringUtils.isNotBlank(appName) && number != null;
    }

    public static NameRecord parse(String name) {
        if (StringUtils.isNotBlank(name)) {
            String[] texts = StringUtils.split(name, "-");
            if (texts.length == 2) {
                String appName = texts[0];
                Long number = null;

                try {
                    number = Long.parseLong(texts[1]);
                } catch (NumberFormatException e) {
                    log.error("failed to parse [{}] as name record", name);
                }
                return new NameRecord(appName, number);
            }
        }

        return new NameRecord();
    }
}
