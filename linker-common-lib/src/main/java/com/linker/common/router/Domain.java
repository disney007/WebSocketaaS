package com.linker.common.router;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Domain {
    String name;
    Set<String> urls;

    @JsonIgnore
    public String getTcpUrl() {
        return urls.stream().filter(u -> StringUtils.startsWith(u, "tcp")).findAny().orElse(null);
    }

    @Override
    public String toString() {
        return String.format("%s %s", this.name, this.urls);
    }
}
