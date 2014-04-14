package com.cq.brainstorm.services.auth;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface IdentityProvider {

    boolean seeCredentialsIn(HttpServletRequest request);

    IdentityProviderData authenticate(HttpServletRequest request, HttpServletResponse response);
}
