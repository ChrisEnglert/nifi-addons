package org.apache.nifi.authorization;

import org.apache.nifi.attribute.expression.language.StandardPropertyValue;
import org.apache.nifi.parameter.ParameterLookup;
import org.apache.nifi.util.NiFiProperties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

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

    @Test()
    public void testGetUser() throws Exception {
        final AuthorizerConfigurationContext configurationContext = getBaseConfiguration();
        kcUserGroupProvider.onConfigured(configurationContext);


        var userByIdentifier = kcUserGroupProvider.getUser("84a423b7-5e14-4a99-b7f8-c790e7e31cbe");
        Assert.assertEquals("chris", userByIdentifier.getIdentity());

        var userByIdentity = kcUserGroupProvider.getUserByIdentity("chris");
        Assert.assertEquals("chris", userByIdentity.getIdentity());

        var groupByIdentifier = kcUserGroupProvider.getGroup("8d7ab8e9-073c-4e04-b8ce-8a7b8503ae4f");
        Assert.assertEquals("nifi-devs", groupByIdentifier.getName());

        var res = kcUserGroupProvider.getUserAndGroups("chris");
        Assert.assertNotNull(res.getGroups());
        Assert.assertFalse(res.getGroups().isEmpty());
    }

    private AuthorizerConfigurationContext getBaseConfiguration() {
        final AuthorizerConfigurationContext configurationContext = mock(AuthorizerConfigurationContext.class);
        when(configurationContext.getProperty(eq(KeycloakConfig.PROP_SERVER_URL))).thenReturn(new StandardPropertyValue("http://localhost:4000/auth", null, ParameterLookup.EMPTY));

        return configurationContext;
    }

    private NiFiProperties getNiFiProperties(final Properties properties) {
        final NiFiProperties nifiProperties = Mockito.mock(NiFiProperties.class);
        when(nifiProperties.getPropertyKeys()).thenReturn(properties.stringPropertyNames());
        when(nifiProperties.getProperty(anyString())).then(invocationOnMock -> properties.getProperty((String) invocationOnMock.getArguments()[0]));
        return nifiProperties;
    }
}
