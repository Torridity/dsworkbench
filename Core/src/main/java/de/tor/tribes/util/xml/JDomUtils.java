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

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

public class JDomUtils {
    /** Get a JDOM document from a String representation.
     * @param pDocument a String containing an XML Document
     * @return an org.jdom2.Document
     */
    public static Document getDocument(String pDocument) throws Exception {
        return new SAXBuilder().build(new StringReader(pDocument));
    }

    public static Document getDocument(File xmlFile) throws Exception {
        return new SAXBuilder().build(xmlFile);
    }
    
    public static List getNodes(Document document, String xPath) {
        return getList(document.getRootElement(), xPath);
    }

    public static List getNodes(Element element, String xPath) {
        return getList(element, xPath);
    }

    public static List getAttributes(Document document, String xPath) {
        return getList(document.getRootElement(), xPath);
    }

    public static List getAttributes(Element element, String xPath) {
        return getList(element, xPath);
    }

    /** Get the value of the available first node.
     * @return the value of the first available node
     */
    public static String getNodeValue(Document document, String xPath) {
        return getNodeValue(document.getRootElement(), xPath);
    }

    /** Get the value of the first available node.
     * @return the value of the first available node
     */
    public static String getNodeValue(Element element, String xPath) {
        List list;
        String getNodeValue;
        
        list = getList(element, xPath);
        if (list.isEmpty()) {
            getNodeValue = null;
        } else {
            getNodeValue = ((Element) list.get(0)).getTextTrim();
        }
        return getNodeValue;
    }

    private static List getList(Element pElement, String xPath) {
        if(xPath.indexOf('/') != -1) {
            List<Element> result = new ArrayList<>();
            
            //this is a path we can only get sub elements --> split
            String preparedPath = xPath.substring(xPath.indexOf('/') + 1);
            for(Element e: pElement.getChildren(xPath.substring(0, xPath.indexOf('/')))) {
                result.addAll(getList(e, preparedPath));
            }
            return result;
        }
        return pElement.getChildren(xPath);
    }
}


