package org.apache.nifi.otel;

import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.annotation.lifecycle.OnUnscheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.controller.ConfigurationContext;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.reporting.AbstractReportingTask;
import org.apache.nifi.reporting.InitializationException;
import org.apache.nifi.reporting.ReportingContext;
import org.apache.nifi.reporting.ReportingInitializationContext;
import org.apache.nifi.reporting.util.provenance.ProvenanceEventConsumer;

import java.io.IOException;

@Tags({"otel"})
public class OpenTelemetryProvenanceReportingTask extends AbstractReportingTask {

    static PropertyDescriptor PROP_SERVICE = new PropertyDescriptor.Builder()
            .name("OpenTelemetry exporter service")
            .description("OpenTelemetry exporter service")
            .identifiesControllerService(OtelService.class)
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    private volatile ProvenanceEventConsumer consumer;
    private OtelService service;
    private ComponentLog log;

    @Override
    protected void init(ReportingInitializationContext config) throws InitializationException {
        super.init(config);

        log = config.getLogger();
    }

    @OnScheduled
    public void onConfigured(final ConfigurationContext context) {

    }

    @OnScheduled
    public void onScheduled(final ConfigurationContext context) throws IOException {

        service = context.getProperty(PROP_SERVICE).asControllerService(OtelService.class);
        if (service == null) {
            log.error("No Service");
        }

        consumer = new ProvenanceEventConsumer();
        consumer.setLogger(log);
        consumer.setScheduled(true);
    }

    @OnUnscheduled
    public void onUnscheduled() {
        if (consumer != null) {
            consumer.setScheduled(false);
        }
    }

    @Override
    public void onTrigger(ReportingContext reportingContext) {
        consumer.consumeEvents(reportingContext, (mapHolder, events) -> {
            service.add(NiFiSpanData.fromProvenanceEventRecords(events));
        });
    }
}
