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
package org.apache.nifi.processors.tusio;

import io.tus.java.client.*;
import org.apache.nifi.annotation.behavior.ReadsAttribute;
import org.apache.nifi.annotation.behavior.ReadsAttributes;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.flowfile.attributes.CoreAttributes;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

@Tags({"tus.io"})
@CapabilityDescription("Client for https://tus.io/ to upload flowfiles")
@SeeAlso({})
@ReadsAttributes({@ReadsAttribute(attribute="", description="")})
@WritesAttributes({@WritesAttribute(attribute= TusClientProcessor.ATTR_TUS_UPLOAD_URL, description="Upload URL")})
public class TusClientProcessor extends AbstractTusProcessor {

    public static final PropertyDescriptor PROP_SERVER = new PropertyDescriptor
            .Builder().name("SERVER")
            .displayName("Server URL")
            .description("URL of the tus.io server")
            .defaultValue("http://localhost/uploads")
            .required(true)
            .addValidator(StandardValidators.URL_VALIDATOR)
            .build();

    public static final Relationship REL_SUCCESS = new Relationship.Builder()
            .name("success")
            .description("Success")
            .build();

    public static final String ATTR_TUS_UPLOAD_URL = "tus-upload-url";

    private TusClient client = new TusClient();

    @Override
    protected void init(final ProcessorInitializationContext context) {

        this.logger = getLogger();

        final List<PropertyDescriptor> descriptors = new ArrayList<>();
        descriptors.add(PROP_SERVER);
        this.descriptors = Collections.unmodifiableList(descriptors);

        final Set<Relationship> relationships = new HashSet<>();
        relationships.add(REL_SUCCESS);
        this.relationships = Collections.unmodifiableSet(relationships);
    }



    @OnScheduled
    public void onScheduled(final ProcessContext context) {

        try {
            client.setUploadCreationURL(new URL( context.getProperty(PROP_SERVER).getValue() ));
        } catch (MalformedURLException e) {
            logger.warn(e.getMessage());
        }

        client.enableResuming(new TusURLMemoryStore());
    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
        FlowFile flowFile = session.get();
        if ( flowFile == null ) {
            context.yield();
            return;
        }

        String uuid = flowFile.getAttribute(CoreAttributes.UUID.key());

        final TusUpload upload = new TusUpload();
        upload.setInputStream( session.read(flowFile) );
        upload.setSize( flowFile.getSize() );
        upload.setFingerprint( uuid );
        upload.setMetadata( flowFile.getAttributes() );

        TusExecutor executor = new TusExecutor() {
            @Override
            protected void makeAttempt() throws ProtocolException, IOException {

                TusUploader uploader = client.resumeOrCreateUpload(upload);
                uploader.setChunkSize(1024);

                String uploadUrl = uploader.getUploadURL().toString();

                long start = System.currentTimeMillis();

                do {
                    long totalBytes = upload.getSize();
                    long bytesUploaded = uploader.getOffset();
                    double progress = (double) bytesUploaded / totalBytes * 100;

                    logger.debug (String.format("Upload %s at %06.2f%%", uuid, progress ));
                } while(uploader.uploadChunk() > -1);

                uploader.finish();

                long end = System.currentTimeMillis();
                session.getProvenanceReporter().send(flowFile, uploadUrl, end - start );
                session.putAttribute(flowFile, ATTR_TUS_UPLOAD_URL, uploadUrl );
            }
        };

        try {
            executor.makeAttempts();
            session.transfer(flowFile, REL_SUCCESS);
            session.commit();
        } catch (ProtocolException e) {
            logger.warn(e.getMessage());
            session.penalize(flowFile);
            session.rollback(true);
        } catch (IOException e) {
            logger.warn(e.getMessage());
            session.penalize(flowFile);
            session.rollback(true);
        }
    }
}
