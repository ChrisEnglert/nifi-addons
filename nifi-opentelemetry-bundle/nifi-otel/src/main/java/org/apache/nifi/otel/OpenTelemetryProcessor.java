package org.apache.nifi.otel;

import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnEnabled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.controller.ConfigurationContext;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.processor.*;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;

import java.util.List;
import java.util.Set;

@Tags({"otel"})
@InputRequirement(InputRequirement.Requirement.INPUT_REQUIRED)
public class OpenTelemetryProcessor extends AbstractProcessor {


    static PropertyDescriptor PROP_SERVICE = new PropertyDescriptor.Builder()
            .name("OpenTelemetry exporter service")
            .description("OpenTelemetry exporter service")
            .identifiesControllerService(OtelService.class)
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    private ComponentLog log;
    private OtelService service;


    @Override
    public Set<Relationship> getRelationships() {
        return Set.of();
    }

    @Override
    protected void init(ProcessorInitializationContext context) {
        super.init(context);
        log = context.getLogger();
    }

    @OnEnabled
    void onEnabled(ConfigurationContext context) {
        service = context.getProperty(PROP_SERVICE).asControllerService(OtelService.class);
        if (service == null) {
            log.error("No exporter service");
        }
    }

    @Override
    public void onTrigger(ProcessContext processContext, ProcessSession processSession) throws ProcessException {

        final List<FlowFile> flowFiles = processSession.get(1);
        if (flowFiles == null) {
            processContext.yield();
            return;
        }

        try {
            
            service.add(NiFiSpanData.fromFlowFiles(flowFiles));

            processSession.remove(flowFiles);
            processSession.commit();

        } catch (Exception e) {

            log.warn("Exception", e);
        }

        processContext.yield();
    }
}
