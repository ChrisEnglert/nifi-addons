package org.apache.nifi.processors.tusio;

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.Relationship;

import java.util.List;
import java.util.Set;

public abstract class AbstractTusProcessor extends AbstractProcessor {
    protected List<PropertyDescriptor> descriptors;
    protected Set<Relationship> relationships;
    protected ComponentLog logger;

    @Override
    public Set<Relationship> getRelationships() {
        return this.relationships;
    }

    @Override
    public final List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return descriptors;
    }
}
