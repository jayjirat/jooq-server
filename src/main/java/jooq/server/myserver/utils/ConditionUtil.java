package jooq.server.myserver.utils;

import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;

import jooq.server.myserver.dto.ConditionDto;
import jooq.server.myserver.dto.ConditionDto.LogicalOperator;

public class ConditionUtil {
    
    public static Condition buildCondition(ConditionDto dto) {
        if (dto == null) return DSL.trueCondition();

        Condition cond = buildSingleCondition(dto);

        if (dto.nestedConditions() != null && !dto.nestedConditions().isEmpty()) {
            Condition nestedCond = buildNestedCondition(dto.nestedConditions());
            if (dto.logicalOperator() == LogicalOperator.NOT) {
                nestedCond = nestedCond.not();
            }
            cond = buildLogical(cond, nestedCond, dto.logicalOperator());
        }

        return cond;
    }

    private static Condition buildSingleCondition(ConditionDto dto) {
        Field<Object> field = DSL.field(dto.field());
        Object dtoVal = dto.values().get(0);
        return switch (dto.operator()) {
            case EQUALS -> field.eq(dtoVal);
            case NOT_EQUALS -> field.ne(dtoVal);
            case GREATER_THAN -> field.gt(dtoVal);
            case LESS_THAN -> field.lt(dtoVal);
            case BETWEEN -> field.between(dtoVal, dto.values().get(1));
            case IN -> field.in(dto.values());
            case LIKE -> field.like(dtoVal.toString());
            };
    }

    private static Condition buildLogical(Condition parentCond, Condition childCond, LogicalOperator logicalOperator){
        if (null != logicalOperator) switch (logicalOperator) {
                case OR -> parentCond = parentCond.or(childCond);
                case AND -> parentCond = parentCond.and(childCond);
                case NOT -> parentCond = parentCond.and(childCond.not());
                default -> {}
            }else{
                parentCond = parentCond.and(childCond);
            }
        return parentCond;
    }

    private static Condition buildNestedCondition(List<ConditionDto> children) {
        Condition result = DSL.trueCondition();
        for (ConditionDto child : children) {
            Condition childCond = buildCondition(child);
            result = buildLogical(result, childCond, child.logicalOperator());
        }
        return result;
    }

}
