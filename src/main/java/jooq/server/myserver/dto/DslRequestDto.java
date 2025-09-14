package jooq.server.myserver.dto;

public record DslRequestDto(
        String jarName,
        String methodName,
        ConditionDto conditions) {
}
