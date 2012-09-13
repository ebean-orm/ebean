/**
 * Copyright (C) 2009 Authors
 * 
 * This file is part of Ebean.
 * 
 * Ebean is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *  
 * Ebean is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Ebean; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA  
 */
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
