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

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.apache.log4j.Logger;
import org.jdom.*;
import org.jdom.input.*;
import org.jaxen.*;
import org.jaxen.dom.*;
import org.jaxen.jdom.*;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class JaxenUtils {

    private static Logger logger = Logger.getLogger("XMLUtils");

    /** Creates a new instance of JaxenUtil.
     * Never created externally!
     */
    private JaxenUtils() {
    }

    /** Convert a DOM document into a JDOM document.
     * @param pDOM an org.w3c.dom.Document
     * @return an org.jdom.Document
     */
    public static org.jdom.Document getDocument(org.w3c.dom.Document pDOM) {
        return new DOMBuilder().build(pDOM);
    }

    /** Get a JDOM document from an InputStream.
     * @param pInputStream an InputStream 
     * @return an org.jdom.Document
     */
    public static org.jdom.Document getDocument(InputStream pInputStream) throws Exception {
        return new SAXBuilder().build(pInputStream);
    }

    /** Get a JDOM document from a String representation.
     * @param pDocument a String containing an XML Document
     * @return an org.jdom.Document
     */
    public static org.jdom.Document getDocument(String pDocument) throws Exception {
        return new SAXBuilder().build(new StringReader(pDocument));
    }

    public static org.jdom.Document getDocument(File xmlFile) throws Exception {
        return new SAXBuilder().build(xmlFile);
    }

    public static org.w3c.dom.Document getW3CDocument(InputStream pInputStream) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(pInputStream);
    }

    public static org.w3c.dom.Document getW3CDocument(File xmlFile) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(xmlFile);
    }

    public static List getNodes(org.jdom.Document document, String xPath, Namespace[] context) {
        return getNodes(document.getRootElement(), xPath, context);
    }

    public static List getNodes(org.jdom.Document document, String xPath) {
        return getNodes(document.getRootElement(), xPath, null);
    }

    public static List getNodes(org.jdom.Element element, String xPath, Namespace[] context) {
        return getList(element, xPath, context);
    }

    public static List getNodes(org.jdom.Element element, String xPath) {
        return getList(element, xPath, null);
    }

    public static List getAttributes(org.jdom.Document document, String xPath, Namespace[] context) {
        return getAttributes(document.getRootElement(), xPath, context);
    }

    public static List getAttributes(org.jdom.Document document, String xPath) {
        return getAttributes(document.getRootElement(), xPath, null);
    }

    public static List getAttributes(org.jdom.Element element, String xPath, Namespace[] context) {
        return getList(element, xPath, context);
    }

    public static List getAttributes(org.jdom.Element element, String xPath) {
        return getList(element, xPath, null);
    }

    /** Get the value of the available first node.
     * @return the value of the first available node
     */
    public static String getNodeValue(org.jdom.Document document, String xPath) {
        return getNodeValue(document, xPath, null);
    }

    /** Get the value of the first available node.
     * @return the value of the first available node
     */
    public static String getNodeValue(org.jdom.Element element, String xPath) {
        return getNodeValue(element, xPath, null);
    }

    /** Get the value of the available first node.
     * @return the value of the first available node
     */
    public static String getNodeValue(org.jdom.Document document, String xPath, Namespace[] context) {
        return getNodeValue(document.getRootElement(), xPath, context);
    }

    /** Get the value of the first available node.
     * @return the value of the first available node
     */
    public static String getNodeValue(org.jdom.Element element, String xPath, Namespace[] context) {
        List list;
        String getNodeValue;
        //
        list = getList(element, xPath, context);
        if (list.size() == 0) {
            getNodeValue = null;
        } else {
            getNodeValue = ((Element) list.get(0)).getTextTrim();
        }
        return getNodeValue;
    }

    public static String[] getNodesValues(org.jdom.Document document, String xPath, Namespace[] context) {
        return getNodesValues(document.getRootElement(), xPath, context);
    }

    public static String[] getNodesValues(org.jdom.Element element, String xPath, Namespace[] context) {
        int index;
        String[] values;
        List result = getList(element, xPath, context);

        values = new String[result.size()];
        for (index = 0; index < result.size(); index++) {
            values[index] = ((Element) result.get(index)).getTextTrim();
        }

        return values;
    }

    public static String[] getAttributesValues(org.jdom.Document document, String xPath, Namespace[] context) {
        return getAttributesValues(document.getRootElement(), xPath, context);
    }

    public static String[] getAttributesValues(org.jdom.Element element, String xPath, Namespace[] context) {
        int index;
        String[] values;
        List result = getList(element, xPath, context);

        values = new String[result.size()];
        for (index = 0; index < result.size(); index++) {
            values[index] = ((Attribute) result.get(index)).getValue();
        }

        return values;
    }

    public static String getAttributeValue(org.jdom.Element element, String xPath, Namespace[] context) {
        return getAttributesValues(element, xPath, context)[0];
    }

    public static String getAttributeValue(org.jdom.Document document, String xPath, Namespace[] context) {
        return getAttributesValues(document, xPath, context)[0];
    }

    public static String getAttributeValue(org.jdom.Document document, String xPath) {
        return getAttributesValues(document, xPath, null)[0];
    }

    public static String getAttributeValue(org.jdom.Element element, String xPath) {
        String[] list;
        String getAttributeValue;
        //
        list = getAttributesValues(element, xPath, null);
        if (list.length == 0) {
            getAttributeValue = null;
        } else {
            getAttributeValue = list[0].trim();
        }
        return getAttributeValue;
    }

    public static String[] getValues(org.jdom.Document document, String xPath, Namespace[] context) {
        return getValues(document.getRootElement(), xPath, context);
    }

    public static String[] getValues(org.jdom.Element element, String xPath, Namespace[] context) {
        int index;
        String[] values;
        List result = getList(element, xPath, context);

        values = new String[result.size()];
        Object item;
        for (index = 0; index < result.size(); index++) {
            item = result.get(index);
            if (item instanceof Attribute) {
                values[index] = ((Attribute) item).getValue();
            } else if (item instanceof Element) {
                values[index] = ((Element) item).getTextNormalize();
            } else {
                values[index] = "unknown type!?: " + item.getClass().toString();
            }
        }

        return values;
    }

    protected static List getList(org.jdom.Element element, String xPath, Namespace[] context) {
        List result = null;
        int index;
        String[] values;
        try {
            XPath filter = createFilter(xPath, context);
            result = filter.selectNodes(element);
        } catch (JaxenException je) {
            logger.error("Jaxen Exception", je);
        }
        return result;
    }

    protected static XPath createFilter(String xPath, Namespace[] context) throws JaxenException {
        XPath filter = new JDOMXPath(xPath);
        if (context != null) {
            for (int i = 0; i < context.length; i++) {
                filter.addNamespace(context[i].getPrefix(), context[i].getURI());
            }
        }
        return filter;
    }

    public static List getNodes(org.w3c.dom.Document document, String xPath, Namespace[] context) {
        return getNodes(document.getDocumentElement(), xPath, context);
    }

    public static List getNodes(org.w3c.dom.Document document, String xPath) {
        return getNodes(document.getDocumentElement(), xPath, null);
    }

    public static List getNodes(org.w3c.dom.Element element, String xPath, Namespace[] context) {
        return getList(element, xPath, context);
    }

    public static List getNodes(org.w3c.dom.Element element, String xPath) {
        return getList(element, xPath, null);
    }

    public static List getAttributes(org.w3c.dom.Document document, String xPath, Namespace[] context) {
        return getAttributes(document.getDocumentElement(), xPath, context);
    }

    public static List getAttributes(org.w3c.dom.Document document, String xPath) {
        return getAttributes(document.getDocumentElement(), xPath, null);
    }

    public static List getAttributes(org.w3c.dom.Element element, String xPath, Namespace[] context) {
        return getList(element, xPath, context);
    }

    public static List getAttributes(org.w3c.dom.Element element, String xPath) {
        return getList(element, xPath, null);
    }

    public static String[] getNodesValues(org.w3c.dom.Document document, String xPath, Namespace[] context) {
        return getNodesValues(document.getDocumentElement(), xPath, context);
    }

    public static String[] getNodesValues(org.w3c.dom.Element element, String xPath, Namespace[] context) {
        int index;
        int node;
        StringBuffer buffer;
        String[] values;
        NodeList nodeList;
        List result = getList(element, xPath, context);

        values = new String[result.size()];
        for (index = 0; index < result.size(); index++) {
            try {
                buffer = new StringBuffer();
                nodeList = ((org.w3c.dom.Element) result.get(index)).getChildNodes();
                for (node = 0; node < nodeList.getLength(); node++) {
                    if (nodeList.item(node).getNodeType() == Node.TEXT_NODE) {
                        buffer.append(nodeList.item(node).getNodeValue());
                    }
                }
                values[index] = buffer.toString().trim();
            } catch (DOMException de) {
                values[index] = "An Exception occured!";
            }
        }

        return values;
    }

    public static String[] getAttributesValues(org.w3c.dom.Document document, String xPath, Namespace[] context) {
        return getAttributesValues(document.getDocumentElement(), xPath, context);
    }

    public static String[] getAttributesValues(org.w3c.dom.Element element, String xPath, Namespace[] context) {
        int index;
        String[] values;
        List result = getList(element, xPath, context);

        values = new String[result.size()];
        for (index = 0; index < result.size(); index++) {
            values[index] = ((org.w3c.dom.Attr) result.get(index)).getValue();
        }

        return values;
    }

    public static String getAttributeValue(org.w3c.dom.Element element, String xPath, Namespace[] context) {
        return getAttributesValues(element, xPath, context)[0];
    }

    public static String getAttributeValue(org.w3c.dom.Document document, String xPath, Namespace[] context) {
        return getAttributesValues(document, xPath, context)[0];
    }

    public static String getAttributeValue(org.w3c.dom.Document document, String xPath) {
        return getAttributesValues(document, xPath, null)[0];
    }

    public static String getAttributeValue(org.w3c.dom.Element element, String xPath) {
        return getAttributesValues(element, xPath, null)[0];
    }

    public static String[] getValues(org.w3c.dom.Document document, String xPath, Namespace[] context) {
        return getValues(document.getDocumentElement(), xPath, context);
    }

    public static String[] getValues(org.w3c.dom.Element element, String xPath, Namespace[] context) {
        int index;
        int node;
        StringBuffer buffer;
        String[] values;
        NodeList nodeList;
        List result = getList(element, xPath, context);

        values = new String[result.size()];
        Object item;
        for (index = 0; index < result.size(); index++) {
            item = result.get(index);
            if (item instanceof org.w3c.dom.Attr) {
                values[index] = ((org.w3c.dom.Attr) item).getValue();
            } else if (item instanceof org.w3c.dom.Element) {
                try {
                    buffer = new StringBuffer();
                    nodeList = ((org.w3c.dom.Element) item).getChildNodes();
                    for (node = 0; node < nodeList.getLength(); node++) {
                        if (nodeList.item(node).getNodeType() == Node.TEXT_NODE) {
                            buffer.append(nodeList.item(node).getNodeValue());
                        }
                    }
                    values[index] = buffer.toString().trim();
                } catch (DOMException de) {
                    values[index] = "An Exception occured!";
                }
            } else {
                values[index] = "unknown type!?: " + item.getClass().toString();
            }
        }

        return values;
    }

    protected static List getList(org.w3c.dom.Element element, String xPath, Namespace[] context) {
        List result = null;
        int index;
        String[] values;
        try {
            XPath filter = createW3CFilter(xPath, context);
            result = filter.selectNodes(element);
        } catch (JaxenException je) {
            je.printStackTrace();
        }
        return result;
    }

    protected static XPath createW3CFilter(String xPath, Namespace[] context) throws JaxenException {
        XPath filter = new DOMXPath(xPath);
        if (context != null) {
            for (int i = 0; i < context.length; i++) {
                filter.addNamespace(context[i].getPrefix(), context[i].getURI());
            }
        }
        return filter;
    }
}


