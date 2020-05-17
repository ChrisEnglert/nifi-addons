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
import org.apache.nifi.registry.security.authentication.AuthenticationRequest;
import org.apache.nifi.registry.security.authentication.AuthenticationResponse;
import org.apache.nifi.registry.security.authentication.BasicAuthIdentityProvider;
import org.apache.nifi.registry.security.authentication.IdentityProviderConfigurationContext;
import org.apache.nifi.registry.security.authentication.exception.IdentityAccessException;
import org.apache.nifi.registry.security.authentication.exception.InvalidCredentialsException;
import org.apache.nifi.registry.security.exception.SecurityProviderCreationException;
import org.apache.nifi.registry.security.exception.SecurityProviderDestructionException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class OidcPasswordIdentityProvider extends BasicAuthIdentityProvider {

    private Scope scope;
    private ClientAuthentication clientAuthentication;
    private URI tokenEndpoint;
    private String identityClaim;
    private String usernameClaim;

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) throws InvalidCredentialsException, IdentityAccessException {

        TokenRequest request = getTokenRequest(authenticationRequest);

        TokenResponse response = null;
        try {
            response = TokenResponse.parse(request.toHTTPRequest().send());
        } catch (ParseException e) {
            throw new IdentityAccessException("Parse");
        } catch (IOException e) {
            throw new IdentityAccessException("IO");
        }

        if (! response.indicatesSuccess()) {
            TokenErrorResponse errorResponse = response.toErrorResponse();
            throw new InvalidCredentialsException("Failed: " + errorResponse.toString());
        }

        AccessTokenResponse successResponse = response.toSuccessResponse();
        BearerAccessToken bearerAccessToken = successResponse.getTokens().getBearerAccessToken();

        try {
            JWTClaimsSet claims = SignedJWT.parse(bearerAccessToken.toString()).getJWTClaimsSet();

            String identity = claims.getClaim("preferred_username").toString();
            String username = claims.getSubject();
            String issuer = claims.getIssuer();
            long expiration = claims.getExpirationTime().getTime();

            return new AuthenticationResponse(identity, username, expiration, issuer);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }

        throw new IdentityAccessException("");
    }

    private TokenRequest getTokenRequest(AuthenticationRequest authenticationRequest) throws InvalidCredentialsException {
        String username = authenticationRequest.getUsername();
        if (username == null) {
            throw new InvalidCredentialsException("Invalid Username");
        }

        Secret password = new Secret(authenticationRequest.getCredentials().toString());

        AuthorizationGrant passwordGrant = new ResourceOwnerPasswordCredentialsGrant(username, password);

        return new TokenRequest(tokenEndpoint, clientAuthentication, passwordGrant, scope);
    }

    @Override
    public void onConfigured(IdentityProviderConfigurationContext configurationContext) throws SecurityProviderCreationException {
        try {
            ClientID clientID = new ClientID(configurationContext.getProperty("Client Id"));
            Secret clientSecret = new Secret(configurationContext.getProperty("Client Secret"));
            this.clientAuthentication = new ClientSecretBasic(clientID, clientSecret);
            this.tokenEndpoint = new URI( configurationContext.getProperty("Token Endpoint") );
            this.scope = new Scope("openid");

            this.identityClaim = configurationContext.getProperty("Identity Claim");
            this.usernameClaim = configurationContext.getProperty("Username Claim");

        } catch (URISyntaxException e) {
            throw new SecurityProviderCreationException("Invalid config");
        }
    }

    @Override
    public void preDestruction() throws SecurityProviderDestructionException {

    }
}
