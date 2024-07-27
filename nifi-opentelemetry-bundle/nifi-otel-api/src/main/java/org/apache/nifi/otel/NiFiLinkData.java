package org.apache.nifi.otel;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.sdk.trace.data.LinkData;

public class NiFiLinkData implements LinkData {

    private final SpanContext spanContext;
    private final Attributes attributes;

    public NiFiLinkData(SpanContext spanContext, Attributes attributes) {
        this.spanContext = spanContext;
        this.attributes = attributes;
    }

    @Override
    public SpanContext getSpanContext() {
        return spanContext;
    }

    @Override
    public Attributes getAttributes() {
        return attributes;
    }

    @Override
    public int getTotalAttributeCount() {
        return attributes.size();
    }
}
