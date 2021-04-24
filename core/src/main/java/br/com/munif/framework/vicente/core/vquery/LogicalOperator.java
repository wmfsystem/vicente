package br.com.munif.framework.vicente.core.vquery;

import java.util.stream.Collectors;

/**
 *
 * @author wmfsystem
 */
public enum LogicalOperator {
    SIMPLE, NOT, OR, AND;

    public String getOperation(VQuery vQuery) {
        switch (this) {
            case SIMPLE:
                if (vQuery.getSubQuerys().size() > 0) {
                    System.out.println("aqui");
                }
                return "(" + vQuery.getAliasWithDot() + vQuery.getCriteria().toString() + ")";
            case NOT:
                return "(!" + vQuery.getAliasWithDot() + vQuery.getCriteria().toString() + ")";
            case OR:
            case AND:
                if (vQuery.getSubQuerys() == null || vQuery.getSubQuerys().isEmpty()) {
                    return vQuery.getCriteria().toString();
                }
                StringBuilder toReturn = new StringBuilder();
                if (vQuery.getCriteria() != null) {
                    toReturn.append("(" + vQuery.getCriteria().toString())
                            .append(") ")
                            .append(vQuery.getLogicalOperator().toString())
                            .append(" ");
                }
                toReturn.append("(" + vQuery.getSubQuerys().get(0));
                for (int i = 1; i < vQuery.getSubQuerys().size(); i++) {
                    toReturn.append(" ")
                            .append(vQuery.getLogicalOperator().toString())
                            .append(" ")
                            .append(vQuery.getSubQuerys().get(i).toString());
                }
                toReturn.append(")");
                return toReturn.toString();
            default:
                break;
        }
        return LogicalOperator.defaultOperation(vQuery);
    }


    public static String defaultOperation(VQuery vQuery) {
        return "(" + vQuery.getCriteria() + ")";
    }
}
