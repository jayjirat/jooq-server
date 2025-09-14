package jooq.server.myserver.dto;

import java.util.List;

public record MethodKey(
    String className, 
    String methodName, 
    List<String> paramTypes
) {}
