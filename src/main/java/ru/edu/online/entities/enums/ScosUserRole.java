package ru.edu.online.entities.enums;

public enum ScosUserRole {
    SECURITY_OFFICER("SECURITY_OFFICER"),
    UNIVERSITY("UNIVERSITY"),
    SUPER_USER("SUPER_USER")
    ;

    private final String value;

    ScosUserRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
