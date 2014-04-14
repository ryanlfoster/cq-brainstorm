package com.cq.brainstorm.services.auth.impl;

import com.cq.brainstorm.services.auth.IdentityProviderData;
import com.cq.brainstorm.services.auth.IdentityProvider;

import org.apache.felix.scr.annotations.*;
import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.UserManager;

import com.google.common.base.Stopwatch;

import com.day.crx.security.token.TokenCookie;
import com.day.crx.security.token.TokenUtil;
import org.apache.sling.auth.core.spi.AuthenticationFeedbackHandler;
import org.apache.sling.auth.core.spi.AuthenticationHandler;
import org.apache.sling.auth.core.spi.AuthenticationInfo;
import org.apache.sling.jcr.api.SlingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component(
        label = "Samples - Sling External System Authentication Handler",
        description = "Sample implementation of External System based Sling Handler that uses the AEM Token scheme for authentication.",
        metatype = true,
        immediate = true
)
@Properties({
        @Property(
                label = "Authentication Paths",
                description = "JCR Paths which this Authentication Handler will authenticate",
                name = AuthenticationHandler.PATH_PROPERTY,
                value = {"/content/cq-brainstorm/en/login/", "/services/cq-brainstorm/login"},
                cardinality = Integer.MAX_VALUE),
        @Property(
                label = "Service Ranking",
                description = "Service ranking. Higher gives more priority.",
                name = "service.ranking",
                intValue = 500,
                propertyPrivate = false),
        @Property(label = "Vendor",
                name = "service.vendor",
                value = "SJ",
                propertyPrivate = true),
        @Property(label = "Description",
                name = "service.description",
                value = "Sample implementation of External System based Sling Handler that leverages the AEM Token scheme for authentication.",
                propertyPrivate = true)
})
@Service
public class ExternalSystemAuthenticationHandler implements AuthenticationHandler, AuthenticationFeedbackHandler {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Reference
    private IdentityProvider identityProvider;

    @Reference
    private SlingRepository slingRepository;

    @Override
    public AuthenticationInfo extractCredentials(HttpServletRequest request, HttpServletResponse response) {

        log.debug(String.format("Trying to extract credentials for %s", request.getPathInfo()));
        AuthenticationInfo authenticationInfo = null;
        if(identityProvider.seeCredentialsIn(request)) {
            final IdentityProviderData identityProviderData = identityProvider.authenticate(request, response);
            authenticationInfo = authenticateInCRX(request, response, identityProviderData);
        }
        log.debug(authenticationInfo != null ? "Extracted." : "Returning Null for further processing.");
        return authenticationInfo;
    }

    @Override
    public boolean requestCredentials(HttpServletRequest request, HttpServletResponse response) {

        // Invoked when an anonymous request is made to a resource or after authenticatedFailed if this auth handler is the active one
        // or some client app calls SlingAuthenticator.login()
        log.debug(String.format("Request credentials for %s", request.getPathInfo()));

        // Returns false if the handler is not able to send an authentication inquiry for the given request, false otherwise.
        // E.g. you can setup a redirect to the Login page here and return true:
        try {
            response.sendRedirect("/content/cq-brainstorm/en/login.html");
        } catch (IOException e) {
            log.error("IOException while sending redirect to the login page.");
        }
        return true;
        // or just return false and let other handlers manage that:
        // return false;
    }

    @Override
    public void dropCredentials(HttpServletRequest request, HttpServletResponse response) {

        // Invoked when some client app calls SlingAuthenticator.logout()
        log.debug(String.format("Drop credentials for %s", request.getPathInfo()));

        // Remove the CRX Login Token cookie from the request
        TokenCookie.update(request, response, slingRepository.getDefaultWorkspace(), null, null, true);

        // We can also remove token from CRX here, if it can't wait for the token cleanup service to run..
    }

    @Override
    public void authenticationFailed(HttpServletRequest request, HttpServletResponse response, AuthenticationInfo authenticationInfo) {

        // Executes after extractCredentials returns a credentials object that failed to be authenticated in CRX by the LoginModule
        log.debug(String.format("authenticationFailed for %s", request.getPathInfo()));

        // requestCredentials() will be invoked then..
    }

    @Override
    public boolean authenticationSucceeded(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationInfo authenticationInfo) {

        // Executes after extractCredentials returns a credentials object that has been authenticated in CRX by the LoginModule
        log.debug(String.format("Succeeded for %s", httpServletRequest.getPathInfo()));

        // Returns true if the handler sent back a response to the client and request processing should be terminated at this point.
        // If false is returned, the request proceeds as authenticated.
        return false;
    }

    private AuthenticationInfo authenticateInCRX(HttpServletRequest request, HttpServletResponse response,
                                                 IdentityProviderData identityProviderData) {
        final String crxUserId = mapCrxUser(identityProviderData.getUserRole());
        AuthenticationInfo authenticationInfo = null;
        Session adminSession = null;
        try {
            adminSession = slingRepository.loginAdministrative(null);
            if(userExists(crxUserId, adminSession)) {
                log.debug(String.format("'%s' user exists, generating token...", crxUserId));
                Stopwatch stopwatch = new Stopwatch().start();
                authenticationInfo = TokenUtil.createCredentials(request, response, slingRepository, crxUserId, true);
                stopwatch.stop();
                log.trace("Created token-based credentials in " + stopwatch);
            } else {
                log.warn(String.format("'%s' user doesn't exist in CRX. Some de-synchronization?", crxUserId));
            }
        } catch (RepositoryException e) {
            log.error("Repository error authenticating user: {} ~> {}", crxUserId, e);
        } finally {
            if(adminSession != null && adminSession.isLive()) {
                adminSession.logout();
            }
        }
        return authenticationInfo;
    }

    private String mapCrxUser(IdentityProviderData.Role externalSystemRole) {
        return externalSystemRole.toString().toLowerCase(); // let's say we have such convention
    }

    private UserManager getUserManager(final Session session) throws RepositoryException {
        if(session instanceof JackrabbitSession) {
            final UserManager userManager = ((JackrabbitSession) session).getUserManager();
            userManager.autoSave(true);
            return userManager;
        } else {
            throw new IllegalArgumentException("Waiting a JackrabbitSession here.");
        }
    }

    private Authorizable getAuthorizable(final String userID, final Session session) throws RepositoryException {
        final UserManager userManager = getUserManager(session);
        return userManager.getAuthorizable(userID);
    }

    private boolean userExists(final String userId, final Session session) throws RepositoryException {
        return getAuthorizable(userId, session) != null;
    }
}
