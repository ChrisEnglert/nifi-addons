package org.apache.nifi.otel;

import org.apache.nifi.controller.ControllerService;
import org.apache.nifi.provenance.MockProvenanceRepository;
import org.apache.nifi.provenance.ProvenanceRepository;
import org.apache.nifi.reporting.InitializationException;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.jupiter.api.Test;

public class TestOpenTelemetryProvenanceReportingTask {

    @Test
    public void test() throws InitializationException {

        ProvenanceRepository provenanceRepository = new MockProvenanceRepository();


        TestRunner runner = TestRunners.newTestRunner(OpenTelemetryProcessor.class);

        final OpenTelemetryProvenanceReportingTask reportingTask = new OpenTelemetryProvenanceReportingTask();
        runner.addControllerService("otel-reporting", (ControllerService) reportingTask);

    }
}
