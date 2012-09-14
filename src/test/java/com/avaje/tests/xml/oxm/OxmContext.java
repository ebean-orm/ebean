package com.avaje.tests.xml.oxm;

import java.io.IOException;
import java.io.Writer;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public interface OxmContext {

    public void writeBean(Object bean, Document document) throws IOException;

    public void writeBean(Object bean, Writer writer) throws IOException;

    public Object readBean(Node node);

}
