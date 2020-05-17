package org.apache.nifi.registry.web.security.authentication.oidc;

import org.apache.nifi.registry.security.authentication.IdentityProviderConfigurationContext;
import org.junit.Before;
import org.junit.Test;

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

    private IdentityProviderConfigurationContext getBaseConfiguration() {
        final IdentityProviderConfigurationContext configurationContext = mock(IdentityProviderConfigurationContext.class);
        when(configurationContext.getProperty(eq(OidcPasswordIdentityProvider.PROP_EXPIRATION))).thenReturn("12 hours");
        when(configurationContext.getProperty(eq(OidcPasswordIdentityProvider.PROP_TOKENENDPOINT))).thenReturn("http://localhost:4000/auth");

        when(configurationContext.getProperty(eq(OidcPasswordIdentityProvider.PROP_CLIENTID))).thenReturn("nifi");
        when(configurationContext.getProperty(eq(OidcPasswordIdentityProvider.PROP_CLIENTSECRET))).thenReturn("secret");

        when(configurationContext.getProperty(eq(OidcPasswordIdentityProvider.PROP_IDENTITYCLAIM))).thenReturn("sub");
        when(configurationContext.getProperty(eq(OidcPasswordIdentityProvider.PROP_USERNAMECLAIM))).thenReturn("email");

        return configurationContext;
    }
}