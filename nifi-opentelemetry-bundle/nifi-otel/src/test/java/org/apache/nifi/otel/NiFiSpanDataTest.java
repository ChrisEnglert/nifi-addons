package org.apache.nifi.otel;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.trace.data.SpanData;
import org.apache.nifi.provenance.ProvenanceEventRecord;
import org.apache.nifi.provenance.ProvenanceEventType;
import org.apache.nifi.provenance.StandardProvenanceEventRecord;
import org.apache.nifi.util.MockFlowFile;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class NiFiSpanDataTest {

    Random random = new Random();

    @Test
    void fromFlowFile() {

        MockFlowFile flowFile = new MockFlowFile(random.nextLong());
        SpanData spanData = NiFiSpanData.from(flowFile);

        Attributes attributes = spanData.getAttributes();

        assertThat(attributes.isEmpty(), is(false));
    }

    @Test
    void fromProvenance() {


        long flowFileId = random.nextLong();
        MockFlowFile flowFile = new MockFlowFile(flowFileId);
        flowFile.setData(new byte[10]);

        ProvenanceEventRecord record = new StandardProvenanceEventRecord.Builder()
                .fromFlowFile(flowFile)
                .setEventTime(0)
                .setEventDuration(1000)
                .setEventType(ProvenanceEventType.DOWNLOAD)
                .setComponentId("949d81ea-aaa8-45a5-b3fe-f6cdb30818bc")
                .setComponentType("Nifi Flow")
                .setFlowFileUUID("64c5d8f7-d12d-43e0-9183-f701d36ba317")
                .build();

        SpanData spanData = NiFiSpanData.from(record);
        Attributes attributes = spanData.getAttributes();

        assertThat(attributes.isEmpty(), is(false));


        assertThat(spanData.getTraceId(), is("64c5d8f7d12d43e09183f701d36ba317"));

        assertThat(attributes.get(AttributeKey.stringKey("nifi.component.id")), is("949d81ea-aaa8-45a5-b3fe-f6cdb30818bc"));
    }
}