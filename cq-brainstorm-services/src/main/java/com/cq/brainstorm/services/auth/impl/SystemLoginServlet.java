package com.cq.brainstorm.services.auth.impl;

import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.auth.Authenticator;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;

import javax.servlet.ServletException;
import java.io.IOException;

@SlingServlet(
        label = "Samples - System Login Servlet",
        paths = "/services/cq-brainstorm/login",
        methods = "[POST,GET]"
)
public class SystemLoginServlet extends SlingAllMethodsServlet {

    @Reference
    private Authenticator authenticator;

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        if(Boolean.parseBoolean(request.getParameter("logout"))) {
            authenticator.logout(request, response);
        } else {
            authenticator.login(request, response);
        }
    }

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
