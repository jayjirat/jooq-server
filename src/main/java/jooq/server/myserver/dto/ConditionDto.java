package jooq.server.myserver.dto;

import java.util.List;

public record ConditionDto(
    String field,
    MyOperator operator,
    LogicalOperator logicalOperator,
    List<Object> values,
    List<ConditionDto> nestedConditions
) {
    public static enum MyOperator { EQUALS, NOT_EQUALS, GREATER_THAN, LESS_THAN, BETWEEN, IN, LIKE }

    public static enum LogicalOperator { AND, OR, NOT }
}

