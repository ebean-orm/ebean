/**
 *  Copyright (C) 2006  Robin Bygrave
 *  
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *  
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */
package com.avaje.ebeaninternal.server.lib.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Parses an XML inputstream returning a Dnode tree.
 */
public class DnodeReader {

    public Dnode parseXml(String str) {
        
        try {
            ByteArrayOutputStream bao = new ByteArrayOutputStream(str.length());
            OutputStreamWriter osw = new OutputStreamWriter(bao);
            
            StringReader sr = new StringReader(str);
            
            int charBufferSize = 1024;
            char[] buf = new char[charBufferSize];
            int len;
            while ((len = sr.read(buf, 0, buf.length)) != -1) {
                osw.write(buf, 0, len);
            }
            sr.close();
            osw.flush();
            osw.close();
            
            bao.flush();
            bao.close();
    
            InputStream is = new ByteArrayInputStream(bao.toByteArray());
            return parseXml(is);
            
        } catch (IOException ex){
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * Parse the XML inputstream returning the Dnode tree.
     */
    public Dnode parseXml(InputStream in) {

        try {
            InputSource inSource = new InputSource(in); 

            DnodeParser parser = new DnodeParser();

            XMLReader myReader = XMLReaderFactory.createXMLReader();
            myReader.setContentHandler(parser);
           
            myReader.parse(inSource);
            
            return parser.getRoot();
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
}
