import org.apache.nifi.registry.keycloak.authorization.KeycloakConfig;
import org.apache.nifi.registry.keycloak.authorization.KeycloakUserGroupProvider;
import org.apache.nifi.registry.properties.NiFiRegistryProperties;
import org.apache.nifi.registry.security.authorization.*;
import org.apache.nifi.registry.util.StandardPropertyValue;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KeycloakTests {

    private KeycloakUserGroupProvider kcUserGroupProvider;

    @Before
    public void setup() {
        final UserGroupProviderInitializationContext initializationContext = mock(UserGroupProviderInitializationContext.class);
        when(initializationContext.getIdentifier()).thenReturn("identifier");

        kcUserGroupProvider = new KeycloakUserGroupProvider();
        kcUserGroupProvider.setNiFiProperties(getNiFiProperties(new Properties()));
        kcUserGroupProvider.initialize(initializationContext);
    }

    @Test()
    public void testInitialize() throws Exception {
        final AuthorizerConfigurationContext configurationContext = getBaseConfiguration();
        kcUserGroupProvider.onConfigured(configurationContext);
    }


    private AuthorizerConfigurationContext getBaseConfiguration() {
        final AuthorizerConfigurationContext configurationContext = mock(AuthorizerConfigurationContext.class);
        when(configurationContext.getProperty(eq(KeycloakConfig.PROP_SERVER_URL))).thenReturn(new StandardPropertyValue("http://localhost:4000/auth"));

        return configurationContext;
    }

    private NiFiRegistryProperties getNiFiProperties(final Properties properties) {
        final NiFiRegistryProperties nifiProperties = mock(NiFiRegistryProperties.class);
        when(nifiProperties.getPropertyKeys()).thenReturn(properties.stringPropertyNames());
        when(nifiProperties.getProperty(anyString())).then(invocationOnMock -> properties.getProperty((String) invocationOnMock.getArguments()[0]));
        return nifiProperties;
    }
}
