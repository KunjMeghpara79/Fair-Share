package com.expense_splitter.Fair_Share.Enums;

public enum SplitType {
    EQUAL("Equal"),
    PERCENTAGE("By Percentage"),
    CUSTOM("Custom");

    private final String value;

    SplitType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    // For converting String from DB/Request to Enum
    public static SplitType fromValue(String value) {
        for (SplitType type : SplitType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown split type: " + value);
    }
}
