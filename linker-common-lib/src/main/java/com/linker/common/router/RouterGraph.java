package com.linker.common.router;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RouterGraph {
    List<Router> routers;
    Set<RouterLink> links;
}
