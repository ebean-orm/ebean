package com.avaje.tests.xml.runtime;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class XrOutputDocument {

    private final Document document;

    public XrOutputDocument(Document document) {
        this.document = document;
    }
    
    public Document getDocument() {
        return document;
    }
    
    
    public void appendChild(Node node, Node newChild) {
        if (node != null){
            node.appendChild(newChild);
        } else {
            node = document.getDocumentElement();
            if (node == null){
                document.appendChild(newChild);
            } else {
                node.appendChild(newChild);
            }
        }
    }
    
}
