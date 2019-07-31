package com.linker.common.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HttpException extends RuntimeException {
    int httpCode;
    Map<String, List<String>> headers;
    String body;
}
