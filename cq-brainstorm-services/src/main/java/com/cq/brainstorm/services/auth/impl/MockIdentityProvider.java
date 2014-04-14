package com.cq.brainstorm.services.auth.impl;

import com.cq.brainstorm.services.auth.IdentityProviderData;
import com.cq.brainstorm.services.auth.IdentityProvider;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;

@Component(
        label = "Mock Identity Provider",
        description = "",
        metatype = true,
        immediate = true
)
@Service
public class MockIdentityProvider implements IdentityProvider {

    @Override
    public boolean seeCredentialsIn(HttpServletRequest request) {

        Map parameterMap = request.getParameterMap();
        return parameterMap.containsKey("login") && parameterMap.containsKey("password");
    }

    @Override
    public IdentityProviderData authenticate(HttpServletRequest request, HttpServletResponse response) {

        boolean loggedIn = "client".equalsIgnoreCase(request.getParameter("login"))
                && "external".equalsIgnoreCase(request.getParameter("password"));
        return loggedIn ? new IdentityProviderData("Mock", IdentityProviderData.Role.CLIENT, UUID.randomUUID().toString())
                : new IdentityProviderData("Mock", IdentityProviderData.Role.ANONYMOUS, UUID.randomUUID().toString());
    }
}
