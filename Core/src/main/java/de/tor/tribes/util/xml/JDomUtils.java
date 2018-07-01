/* 
 * Copyright 2015 Torridity.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tor.tribes.util.xml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/**
 * Utility Class for easier reading / writing of jdom documents
 * 
 * @author extremeCrazyCoder
 */
public class JDomUtils {
    private static final Logger logger = LogManager.getLogger("JDomUtils");
    /*
     * Reading part
     */
    public static Document getDocument(String pDocument) throws Exception {
        return getDocument(new ByteArrayInputStream(pDocument.getBytes()));
    }

    public static Document getDocument(File xmlFile) throws Exception {
        return getDocument(new FileInputStream(xmlFile));
    }

    public static Document getDocument(InputStream inStream) throws Exception {
        return new SAXBuilder().build(inStream);
    }
    
    public static List<Element> getNodes(Document document, String path) {
        return getList(document.getRootElement(), path);
    }

    public static List<Element> getNodes(Element element, String path) {
        return getList(element, path);
    }

    /** Get the value of the available first node.
     * @return the value of the first available node
     */
    public static String getNodeValue(Document document, String path) {
        return getNodeValue(document.getRootElement(), path);
    }

    /** Get the value of the first available node.
     * @return the value of the first available node
     */
    public static String getNodeValue(Element element, String path) {
        List list;
        String getNodeValue;
        
        list = getList(element, path);
        if (list.isEmpty()) {
            getNodeValue = null;
        } else {
            getNodeValue = ((Element) list.get(0)).getTextTrim();
        }
        return getNodeValue;
    }

    private static List<Element> getList(Element pElement, String path) {
        if(path == null) {
            return pElement.getChildren();
        }
        if(path.indexOf('/') != -1) {
            List<Element> result = new ArrayList<>();
            
            //this is a path we can only get sub elements --> split
            String preparedPath = path.substring(path.indexOf('/') + 1);
            for(Element e: pElement.getChildren(path.substring(0, path.indexOf('/')))) {
                result.addAll(getList(e, preparedPath));
            }
            return result;
        }
        return pElement.getChildren(path);
    }
    
    /**
     * Writing part
     * 
     * to generate Elements use something like this
     * 
        Element extensions = new Element("extensions");
        extensions.setAttribute(new Attribute("id", "2"));
        extensions.addContent(amounts.toXml("amounts"));
     */
    
    public static Document createDocument() {
        return new Document(new Element("data"));
    }
    
    public static void saveDocument(Document pDoc, String filename) {
        try {
            XMLOutputter xmlOutput = new XMLOutputter();
            
            // display nice nice
            xmlOutput.setFormat(Format.getPrettyFormat());
            xmlOutput.output(pDoc, new FileWriter(filename));
        } catch (Exception e) {
            logger.warn("Unable to save document", e);
        }
    }
    
    public static String toShortString(Element pElm) {
        return toShortString(createDocument().addContent(pElm));
    }
    
    public static String toShortString(Document pDoc) {
        XMLOutputter xmlOutput = new XMLOutputter();

        // display nice nice
        xmlOutput.setFormat(Format.getCompactFormat());
        return xmlOutput.outputString(pDoc);
    }
}


