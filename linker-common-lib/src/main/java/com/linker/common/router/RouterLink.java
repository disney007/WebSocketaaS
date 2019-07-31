package com.linker.common.router;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class RouterLink implements Serializable {
    private static final long serialVersionUID = -7884296277376589741L;
    String n1;
    String n2;
}
