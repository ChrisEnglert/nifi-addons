package org.apache.nifi.registry.web.security.authentication.oidc;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.nifi.registry.security.authentication.AuthenticationRequest;
import org.apache.nifi.registry.security.authentication.AuthenticationResponse;
import org.apache.nifi.registry.security.authentication.IdentityProviderConfigurationContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OidcPasswordIdentityProviderTest {

    private OidcPasswordIdentityProvider provider;

    @Before
    public void setup() {
        provider = new OidcPasswordIdentityProvider();
    }

    @Test()
    public void testConfigured() throws Exception {
        final IdentityProviderConfigurationContext configurationContext = getBaseConfiguration();
        provider.onConfigured(configurationContext);
    }

    @Test
    public void testAuth() throws Exception {
        final IdentityProviderConfigurationContext configurationContext = getBaseConfiguration();
        provider.onConfigured(configurationContext);

        final AuthenticationRequest mockAuth = mock(AuthenticationRequest.class);
        when(mockAuth.getUsername()).thenReturn("chris");
        when(mockAuth.getCredentials()).thenReturn("passwordz");

        serviceMock.stubFor(post(urlPathEqualTo("/auth/realms/master/protocol/openid-connect/token"))
                .willReturn(
                        aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody("{\"access_token\":\"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaXNzIjoiTmlGaSBUZXN0IiwiaWF0IjoxNTE2MjM5MDIyfQ.dpVB6Cthj5cyrWiyuyzFAswT-7R0YdaHvJYelPQT5mc\", \"token_type\":\"bearer\"}")));

        AuthenticationResponse result = provider.authenticate(mockAuth);

        Assert.assertEquals("1234567890",result.getIdentity());
        Assert.assertEquals("John Doe", result.getUsername());
        Assert.assertEquals("NiFi Test", result.getIssuer());
    }

    private IdentityProviderConfigurationContext getBaseConfiguration() {
        final IdentityProviderConfigurationContext configurationContext = mock(IdentityProviderConfigurationContext.class);
        when(configurationContext.getProperty(eq(OidcPasswordIdentityProvider.PROP_EXPIRATION))).thenReturn("12 hours");
        when(configurationContext.getProperty(eq(OidcPasswordIdentityProvider.PROP_TOKENENDPOINT))).thenReturn("http://localhost:4001/auth/realms/master/protocol/openid-connect/token");

        when(configurationContext.getProperty(eq(OidcPasswordIdentityProvider.PROP_CLIENTID))).thenReturn("nifi");
        when(configurationContext.getProperty(eq(OidcPasswordIdentityProvider.PROP_CLIENTSECRET))).thenReturn("a356d9e5-001e-4844-aac2-be0838052187");

        when(configurationContext.getProperty(eq(OidcPasswordIdentityProvider.PROP_IDENTITYCLAIM))).thenReturn("sub");
        when(configurationContext.getProperty(eq(OidcPasswordIdentityProvider.PROP_USERNAMECLAIM))).thenReturn("name");

        return configurationContext;
    }

    @Rule
    public final WireMockRule serviceMock = new WireMockRule(wireMockConfig()
            .port(4001));


}