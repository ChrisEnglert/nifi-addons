package org.apache.nifi.processors.tusio;

import org.junit.After;
import org.junit.Before;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.socket.PortFactory;

import java.net.URL;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;

public class MockServerProvider {
    protected ClientAndServer mockServer;
    protected URL mockServerURL;
    protected URL creationUrl;

    @Before
    public void setUp() throws Exception {
        creationUrl = new URL("http://master.tus.io");
        int port = PortFactory.findFreePort();
        mockServerURL = new URL("http://localhost:" + port + "/files");
        mockServer = startClientAndServer(port);
    }

    @After
    public void tearDown() {
        mockServer.stop();
    }
}
