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
package de.tor.tribes.util.interfaces;

/**
 *
 * @author Torridity
 */
public interface BBFormatterInterface {

    public final static String LIST_START = "%LIST_START%";
    public final static String LIST_END = "%LIST_END%";
    public final static String ELEMENT_ID = "%ELEMENT_ID%";
    public final static String ELEMENT_COUNT = "%ELEMENT_COUNT%";

    public String getPropertyKey();

    public void storeProperty();

    public String getStandardTemplate();

    public String getTemplate();

    public String[] getTemplateVariables();

    public void setCustomTemplate(String pTemplate);
}
