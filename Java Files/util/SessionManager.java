package com.prison.util;

import com.prison.model.User;

public final class SessionManager {
    private static User currentUser;

    private SessionManager() {
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean hasRole(String role) {
        if (currentUser == null || currentUser.getRole() == null || role == null) {
            return false;
        }
        return currentUser.getRole().trim().equalsIgnoreCase(role.trim());
    }

    public static boolean isVisitor() {
        return hasRole("Visitor");
    }

    public static void clear() {
        currentUser = null;
    }
}