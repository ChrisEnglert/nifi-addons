package org.apache.nifi.otel;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.internal.ImmutableSpanContext;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.IdGenerator;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.flowfile.attributes.CoreAttributes;
import org.apache.nifi.provenance.ProvenanceEventRecord;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class NiFiSpanData implements SpanData {

    private final SpanContext spanContext;
    private final Attributes attributes;

    private final List<EventData> events;
    private long startTime;
    private long endTime;

    private final String traceId;
    private final String spanId;


    private NiFiSpanData(long startTime, long endTime, String traceId, String spanId, AttributesBuilder attributesBuilder) {

        this.startTime = startTime;
        this.endTime = endTime;

        this.traceId = traceId;
        this.spanId = spanId;
        this.spanContext = ImmutableSpanContext.create(traceId, spanId, TraceFlags.getSampled(), TraceState.getDefault(), false, false);
        this.attributes = attributesBuilder
                .put("service.name", "NiFi-Service")
                .build();

        this.events = List.of();
    }

    public static Collection<NiFiSpanData> fromFlowFiles(List<FlowFile> flowFiles) {
        return flowFiles.stream().map((NiFiSpanData::from)).collect(Collectors.toList());
    }

    public static Collection<NiFiSpanData> fromProvenanceEventRecords(List<ProvenanceEventRecord> events) {
        return events.stream().map((NiFiSpanData::from)).collect(Collectors.toList());
    }

    public static NiFiSpanData from(FlowFile flowFile) {

        final String traceId = flowFile.getAttribute(CoreAttributes.UUID.key()).replace("-", "");
        final String spanId = IdGenerator.random().generateSpanId();


        Instant instant = Instant.now();

        AttributesBuilder attributesBuilder = Attributes.builder();
        optional(attributesBuilder, "user", flowFile.getAttribute("user"));


        return new NiFiSpanData(
                Long.parseLong("" + instant.getEpochSecond() + instant.getNano()),
                Long.parseLong("" + (instant.getEpochSecond() + 10) + instant.getNano()),
                traceId,
                spanId,
                attributesBuilder
                        .put("nifi.flowfile.uuid", flowFile.getAttribute(CoreAttributes.UUID.key()))
        );
    }

    static AttributesBuilder optional(AttributesBuilder builder, String key, String value) {
        if (value != null) {
            builder.put(key, value);
        }
        return builder;
    }

    static NiFiSpanData from(ProvenanceEventRecord provenanceEventRecord) {

        
        return new NiFiSpanData(
                provenanceEventRecord.getEventTime(),
                provenanceEventRecord.getEventTime() + provenanceEventRecord.getEventDuration(),
                provenanceEventRecord.getFlowFileUuid().replace("-", ""),
                Long.toHexString(provenanceEventRecord.getEventId()),
                Attributes.builder()
                        .put("nifi.provenance.type", provenanceEventRecord.getEventType().name())
                        .put("nifi.component.id", provenanceEventRecord.getComponentId())
                        .put("nifi.component.type", provenanceEventRecord.getComponentType())
                        .put("nifi.flowfile.uuid", provenanceEventRecord.getFlowFileUuid())
        );
    }


    @Override
    public String getName() {
        return "NiFi-Name";
    }

    @Override
    public SpanKind getKind() {
        return SpanKind.INTERNAL;
    }

    @Override
    public SpanContext getSpanContext() {
        return spanContext;
    }

    @Override
    public String getTraceId() {
        return traceId;
    }

    @Override
    public String getSpanId() {
        return spanId;
    }

    @Override
    public SpanContext getParentSpanContext() {
        return SpanContext.create(traceId, null, TraceFlags.getDefault(), TraceState.getDefault());
    }

    @Override
    public StatusData getStatus() {
        return StatusData.ok();
    }

    @Override
    public long getStartEpochNanos() {
        return startTime;
    }

    @Override
    public Attributes getAttributes() {
        return attributes;
    }

    @Override
    public List<EventData> getEvents() {
        return events;
    }

    @Override
    public List<LinkData> getLinks() {
        return List.of();
    }

    @Override
    public long getEndEpochNanos() {
        return endTime;
    }

    @Override
    public boolean hasEnded() {
        return true;
    }

    @Override
    public int getTotalRecordedEvents() {
        return 0;
    }

    @Override
    public int getTotalRecordedLinks() {
        return 0;
    }

    @Override
    public int getTotalAttributeCount() {
        return attributes.size();
    }

    @Override
    public InstrumentationLibraryInfo getInstrumentationLibraryInfo() {
        return InstrumentationLibraryInfo.empty();
    }

    @Override
    public Resource getResource() {
        return Resource.create(getAttributes());
    }
}
