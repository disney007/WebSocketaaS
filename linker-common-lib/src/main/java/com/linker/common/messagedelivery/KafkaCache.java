package com.linker.common.messagedelivery;

import java.util.List;
import java.util.Set;

public interface KafkaCache {
    void addItem(String item);

    Set<String> getDuplicateItems(List<String> items);

    void deleteItems(List<String> items);
}
