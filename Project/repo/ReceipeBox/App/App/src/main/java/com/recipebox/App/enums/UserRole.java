package com.recipebox.App.enums;

public enum UserRole {
	USER("user"),SUPERADMIN("superadmin"), ADMIN("admin");

	private final String value;

	UserRole(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static UserRole fromValue(String value) {
		for (UserRole role : UserRole.values()) {
			if (role.value.equalsIgnoreCase(value)) {
				return role;
			}
		}
		throw new IllegalArgumentException("Unknown role: " + value);
	}
}