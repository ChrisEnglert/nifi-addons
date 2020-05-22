package org.apache.nifi.processors.tusio;

import io.tus.java.client.TusClient;
import org.apache.nifi.flowfile.attributes.CoreAttributes;
import org.apache.nifi.provenance.ProvenanceEventRecord;
import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockserver.model.HttpRequest.*;
import static org.mockserver.model.HttpResponse.response;

public class ClientProcessorTest extends MockServerProvider {

    private TestRunner testRunner;

    @Before
    public void init() {
        testRunner = TestRunners.newTestRunner(TusClientProcessor.class);
    }

    @Test
    public void testProcessor() {

        // setup flowfile
        InputStream content = new ByteArrayInputStream("{\"hello\":\"nifi rocks\"}".getBytes());
        Map<String, String> attributes = new HashMap<>();
        attributes.put(CoreAttributes.FILENAME.key(), "rocks.json");

        // setup tus server
        mockServer.when(request("/files")
                .withMethod("POST"))
                .respond(response()
                        .withStatusCode(201)
                        .withHeader("Tus-Resumable", TusClient.TUS_VERSION)
                        .withHeader("Location", mockServerURL + "/foo"));

        mockServer.when(request("/files/foo")
                .withMethod("POST"))
                .respond(response()
                        .withStatusCode(201)
                        .withHeader("Tus-Resumable", TusClient.TUS_VERSION)
                        .withHeader("Upload-Offset", "22"));

        testRunner.setProperty( TusClientProcessor.PROP_SERVER, mockServerURL.toString() );
        testRunner.enqueue(content, attributes);
        testRunner.run();

        List<MockFlowFile> files = testRunner.getFlowFilesForRelationship(TusClientProcessor.REL_SUCCESS);
        Assert.assertTrue(files.size() > 0);

        MockFlowFile file = files.get(0);
        file.assertAttributeExists( TusClientProcessor.ATTR_TUS_UPLOAD_URL );

        List<ProvenanceEventRecord> provEvents = testRunner.getProvenanceEvents();
        Assert.assertTrue( provEvents.size() > 0 );
    }

}
