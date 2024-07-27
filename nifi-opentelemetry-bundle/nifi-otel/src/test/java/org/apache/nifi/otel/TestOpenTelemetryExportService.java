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

import org.apache.nifi.provenance.ProvenanceEventRecord;
import org.apache.nifi.provenance.ProvenanceEventType;
import org.apache.nifi.provenance.StandardProvenanceEventRecord;
import org.apache.nifi.reporting.InitializationException;
import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

public class TestOpenTelemetryExportService {

    Random random = new Random();

    @BeforeEach
    public void init() {

    }

    @Test
    public void testServiceWithFlowFile() throws InitializationException, InterruptedException {
        final TestRunner runner = TestRunners.newTestRunner(OpenTelemetryProcessor.class);
        final OpenTelemetryExportService service = new OpenTelemetryExportService();
        runner.addControllerService("test-good", service);
        runner.setProperty(service, OpenTelemetryExportService.PROP_ENDPOINT, "http://localhost:14250");
        runner.enableControllerService(service);
        runner.assertValid(service);


        MockFlowFile flowFile = new MockFlowFile(random.nextLong());


        service.add(List.of(NiFiSpanData.from(flowFile)));

        service.execute();

    }

    @Test
    public void testServiceWithProvenance() throws InitializationException, InterruptedException {
        final TestRunner runner = TestRunners.newTestRunner(OpenTelemetryProcessor.class);
        final OpenTelemetryExportService service = new OpenTelemetryExportService();
        runner.addControllerService("test-good", service);
        runner.setProperty(service, OpenTelemetryExportService.PROP_ENDPOINT, "http://localhost:14250");
        runner.enableControllerService(service);
        runner.assertValid(service);

        /*

        {"provenanceEvent":{
        "id":"622",
        "eventId":622,
        "eventTime":"12/07/2022 22:41:13.364 UTC",
        "lineageDuration":32518767,
        "eventType":"DOWNLOAD",
        "flowFileUuid":"64c5d8f7-d12d-43e0-9183-f701d36ba317",
        "fileSize":"93 bytes",
        "fileSizeBytes":93,
        "componentId":"949d81ea-aaa8-45a5-b3fe-f6cdb30818bc",
        "componentType":"Nifi Flow",

        "attributes":[{
            "name":"Content-Type","value":"application/json","previousValue":"application/json"},{
            "name":"env","value":"test","previousValue":"test"},{
            "name":"event","value":"certificate_release_status_changed","previousValue":"certificate_release_status_changed"},{
            "name":"filename","value":"64c5d8f7-d12d-43e0-9183-f701d36ba317","previousValue":"64c5d8f7-d12d-43e0-9183-f701d36ba317"},{
            "name":"language","value":"de","previousValue":"de"},{
            "name":"mime.type","value":"application/json","previousValue":"application/json"},{
            "name":"path","value":"./","previousValue":"./"},{
            "name":"restlistener.remote.issuer.dn","value":"none","previousValue":"none"},{
            "name":"restlistener.remote.source.host","value":"192.168.130.63","previousValue":"192.168.130.63"},{
            "name":"restlistener.remote.user.dn","value":"none","previousValue":"none"},{
            "name":"restlistener.request.uri","value":"/","previousValue":"/"},{
            "name":"RouteOnAttribute.Route","value":"env-test","previousValue":"env-test"},{
            "name":"tenant","value":"050","previousValue":"050"},{
            "name":"traceid","value":"0000000000000000068622175c1b447c","previousValue":"0000000000000000068622175c1b447c"},{
            "name":"user","value":"STEPKUN","previousValue":"STEPKUN"},{
            "name":"uuid","value":"64c5d8f7-d12d-43e0-9183-f701d36ba317","previousValue":"64c5d8f7-d12d-43e0-9183-f701d36ba317"}],
        "parentUuids":[],
        "childUuids":[],
        "transitUri":"/flowfile-queues/d2478904-0184-1000-57a7-ff1b7dc90be0/flowfiles/64c5d8f7-d12d-43e0-9183-f701d36ba317/content",
        "details":"Download of Content requested by username for StandardFlowFileRecord[uuid=64c5d8f7-d12d-43e0-9183-f701d36ba317,claim=StandardContentClaim [resourceClaim=StandardResourceClaim[id=1670237886138-1, container=default, section=1], offset=3049, length=93],offset=0,name=64c5d8f7-d12d-43e0-9183-f701d36ba317,size=93]",
        "contentEqual":false, "inputContentAvailable":false,"outputContentAvailable":true,"outputContentClaimSection":"1","outputContentClaimContainer":"default","outputContentClaimIdentifier":"1670237886138-1","outputContentClaimOffset":3049,"outputContentClaimFileSize":"93 bytes","outputContentClaimFileSizeBytes":93,"replayAvailable":false,"replayExplanation":"Cannot replay data from Provenance Event because the Source FlowFile Queue with ID No Value no longer exists","sourceConnectionIdentifier":"No Value"}}
         */

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


        service.add(List.of(NiFiSpanData.from(record)));

        service.execute();

    }

}
