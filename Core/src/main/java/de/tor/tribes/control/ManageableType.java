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
package de.tor.tribes.control;

import org.jdom.Element;

/**
 *
 * @author Torridity
 */
public abstract class ManageableType {

    public ManageableType() {
    }

    public ManageableType(Element e) {
        loadFromXml(e);
    }

    public abstract String getElementIdentifier();

    public abstract String getElementGroupIdentifier();

    public abstract String getGroupNameAttributeIdentifier();

    public abstract String toXml();

    public abstract void loadFromXml(Element e);
}
