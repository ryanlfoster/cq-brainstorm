package com.cq.brainstorm.services.auth;

/**
 * Transfer object for {@link IdentityProvider}
 */
public final class IdentityProviderData {

    private Role userRole;
    private String userName;
    private String token;

    public IdentityProviderData(String userName, Role userRole, String token) {
        this.userRole = userRole;
        this.userName = userName;
        this.token = token;
    }

    public Role getUserRole() {
        return userRole;
    }

    public String getUserName() {
        return userName;
    }

    public String getToken() {
        return token;
    }

    public static enum Role {
        ANONYMOUS, CLIENT
    }
}
