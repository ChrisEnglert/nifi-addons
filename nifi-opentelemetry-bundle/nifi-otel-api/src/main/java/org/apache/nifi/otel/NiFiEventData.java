package org.apache.nifi.otel;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.trace.data.EventData;

public class NiFiEventData implements EventData {

    private final Attributes attributes;

    public NiFiEventData(Attributes attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getName() {
        return "Test-Event";
    }

    @Override
    public Attributes getAttributes() {
        return attributes;
    }

    @Override
    public long getEpochNanos() {
        return 0;
    }

    @Override
    public int getTotalAttributeCount() {
        return attributes.size();
    }
}
