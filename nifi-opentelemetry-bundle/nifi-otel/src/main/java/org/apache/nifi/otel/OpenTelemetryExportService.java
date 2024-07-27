/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.nifi.otel;

import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnDisabled;
import org.apache.nifi.annotation.lifecycle.OnEnabled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.controller.ConfigurationContext;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.reporting.InitializationException;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@Tags({"otel"})
@CapabilityDescription("Example ControllerService implementation of MyService.")
public class OpenTelemetryExportService extends AbstractControllerService implements OtelService {

    static final String[] supportedExporters = {"JaegerGrpc"};

    public static final PropertyDescriptor PROP_ENDPOINT = new PropertyDescriptor.Builder()
            .name("Endpoint")
            .displayName("Endpoint")
            .description("Endpoint")
            .defaultValue("http://localhost:14250")
            .required(true)
            .addValidator(StandardValidators.URI_VALIDATOR)
            .build();

    public static final PropertyDescriptor PROP_EXPORTER = new PropertyDescriptor.Builder()
            .name("Exporter")
            .displayName("Exporter")
            .description("Exporters:\n - JaegerGrpc default port :14250")
            .defaultValue(supportedExporters[0])
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .allowableValues(supportedExporters)
            .build();

    private static final List<PropertyDescriptor> properties;

    SpanExporter spanExporter;
    ConcurrentLinkedQueue<NiFiSpanData> spanQueue = new ConcurrentLinkedQueue<>();


    static {
        properties = List.of(PROP_ENDPOINT, PROP_EXPORTER);
    }

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return properties;
    }

    /**
     * @param context the configuration context
     * @throws InitializationException if unable to create a database connection
     */
    @OnEnabled
    public void onEnabled(final ConfigurationContext context) throws InitializationException {

        String endpoint = context.getProperty(PROP_ENDPOINT).getValue();

        final String exporterName = context.getProperty(PROP_EXPORTER).getValue();

        if ("JaegerGrpc".equals(exporterName)) {
            spanExporter = JaegerGrpcSpanExporter.builder()
                    .setEndpoint(endpoint)
                    .build();
        } else {
            throw new InitializationException("Invalid Exporter");
        }
    }

    @OnDisabled
    public void shutdown() {
        spanExporter.shutdown();
    }

    @Override
    public void execute() {

        NiFiSpanData span = spanQueue.poll();
        if (span == null) {
            return;
        }

        Collection<SpanData> spans = new HashSet<>();
        spans.add(span);

        spanExporter.export(spans);
        final CompletableResultCode result = spanExporter.flush();

        if (result.isSuccess()) {

        }
    }


    public void add(Collection<NiFiSpanData> spans) {
        spanQueue.addAll(spans);
    }
}
