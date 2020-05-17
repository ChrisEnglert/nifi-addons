package org.apache.nifi.registry.web.security.authentication.oidc;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.AccessTokenResponse;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.ResourceOwnerPasswordCredentialsGrant;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.oauth2.sdk.util.StringUtils;
import org.apache.nifi.registry.security.authentication.AuthenticationRequest;
import org.apache.nifi.registry.security.authentication.AuthenticationResponse;
import org.apache.nifi.registry.security.authentication.BasicAuthIdentityProvider;
import org.apache.nifi.registry.security.authentication.IdentityProviderConfigurationContext;
import org.apache.nifi.registry.security.authentication.exception.IdentityAccessException;
import org.apache.nifi.registry.security.authentication.exception.InvalidCredentialsException;
import org.apache.nifi.registry.security.exception.SecurityProviderCreationException;
import org.apache.nifi.registry.security.exception.SecurityProviderDestructionException;
import org.apache.nifi.registry.util.FormatUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

public class OidcPasswordIdentityProvider extends BasicAuthIdentityProvider {

    public static final String PROP_EXPIRATION = "Authentication Expiration";
    public static final String PROP_CLIENTID = "Client Id";
    public static final String PROP_CLIENTSECRET = "Client Secret";
    public static final String PROP_IDENTITYCLAIM = "Identity Claim";
    public static final String PROP_USERNAMECLAIM = "Username Claim";
    public static final String PROP_TOKENENDPOINT = "Token Endpoint";

    private Scope scope;
    private ClientAuthentication clientAuthentication;
    private URI tokenEndpoint;
    private String identityClaim;
    private String usernameClaim;
    private long expiration;

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) throws InvalidCredentialsException, IdentityAccessException {

        TokenRequest request = getTokenRequest(authenticationRequest);

        TokenResponse response;
        try {
            response = TokenResponse.parse(request.toHTTPRequest().send());
        } catch (ParseException e) {
            throw new IdentityAccessException("Failed to parse token response");
        } catch (IOException e) {
            throw new IdentityAccessException("Connection error");
        }

        if (! response.indicatesSuccess()) {
            TokenErrorResponse errorResponse = response.toErrorResponse();
            throw new InvalidCredentialsException(errorResponse.getErrorObject().getDescription());
        }

        AccessTokenResponse successResponse = response.toSuccessResponse();
        BearerAccessToken bearerAccessToken = successResponse.getTokens().getBearerAccessToken();
        if (bearerAccessToken == null) {
            throw new IdentityAccessException("Response is not a BearerAccessToken");
        }

        try {
            JWTClaimsSet claims = SignedJWT.parse(bearerAccessToken.toString()).getJWTClaimsSet();

            String identity = claims.getStringClaim(identityClaim);
            if (StringUtils.isBlank(identity)) {
                throw new IdentityAccessException("Invalid Identity Claim " + identityClaim);
            }

            String username = claims.getStringClaim(usernameClaim);
            if (StringUtils.isBlank(username)) {
                throw new IdentityAccessException("Invalid Username Claim " + usernameClaim);
            }

            String issuer = claims.getIssuer();

            return new AuthenticationResponse(identity, username, expiration, issuer);
        } catch (java.text.ParseException e) {
            throw new IdentityAccessException("Failed to parse response as signed JWT");
        }
    }

    private TokenRequest getTokenRequest(AuthenticationRequest authenticationRequest) throws InvalidCredentialsException {
        final String username = authenticationRequest.getUsername();
        if (username == null) {
            throw new InvalidCredentialsException("Invalid Username");
        }

        final Secret password = new Secret(authenticationRequest.getCredentials().toString());

        final AuthorizationGrant passwordGrant = new ResourceOwnerPasswordCredentialsGrant(username, password);

        return new TokenRequest(tokenEndpoint, clientAuthentication, passwordGrant, scope);
    }

    @Override
    public void onConfigured(IdentityProviderConfigurationContext configurationContext) throws SecurityProviderCreationException {

        final String rawExpiration = configurationContext.getProperty(PROP_EXPIRATION);
        if (StringUtils.isBlank(rawExpiration)) {
            throw new SecurityProviderCreationException("The Authentication Expiration must be specified.");
        }

        try {
            this.expiration = FormatUtils.getTimeDuration(rawExpiration, TimeUnit.MILLISECONDS);
        } catch (final IllegalArgumentException iae) {
            throw new SecurityProviderCreationException(String.format("The Expiration Duration '%s' is not a valid time duration", rawExpiration));
        }

        final String rawClientId = configurationContext.getProperty(PROP_CLIENTID);
        if (StringUtils.isBlank(rawClientId)) {
            throw new SecurityProviderCreationException("The Client ID must be specified.");
        }

        final String rawClientSecret = configurationContext.getProperty(PROP_CLIENTSECRET);
        if (StringUtils.isBlank(rawClientSecret)) {
            throw new SecurityProviderCreationException("The Client Secret must be specified.");
        }

        final String rawIdentityClaim = configurationContext.getProperty(PROP_IDENTITYCLAIM);
        if (StringUtils.isBlank(rawIdentityClaim)) {
            throw new SecurityProviderCreationException("The User Identity Claim must be specified. Recommended: sub, email");
        }
        this.identityClaim = rawIdentityClaim;

        final String rawUsernameClaim = configurationContext.getProperty(PROP_USERNAMECLAIM);
        if (StringUtils.isBlank(rawUsernameClaim)) {
            throw new SecurityProviderCreationException("The Username Claim must be specified. Recommended: email, username");
        }
        this.usernameClaim = rawUsernameClaim;

        this.clientAuthentication = new ClientSecretBasic(new ClientID(rawClientId), new Secret(rawClientSecret));
        this.scope = new Scope("openid");

        try {
            this.tokenEndpoint = new URI( configurationContext.getProperty(PROP_TOKENENDPOINT) );
        } catch (URISyntaxException e) {
            throw new SecurityProviderCreationException("Token Endpoint invalid URI format");
        }
    }

    @Override
    public void preDestruction() throws SecurityProviderDestructionException {

    }
}
