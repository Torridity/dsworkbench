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
package de.tor.tribes.ui.tree;

/**
 *
 * @author Charon
 */
public class ContinentNode extends AbstractTreeNode {

    public ContinentNode(String pContinent) {
        super(pContinent);
    }

    @Override
    public String getUserObject() {
        return (String) super.getUserObject();
    }

    public int getContinent() {
        String cont = getUserObject();
        cont = cont.replaceAll("K", "");
        return Integer.parseInt(cont);
    }

    @Override
    public boolean isAllyNode() {
        return false;
    }

    @Override
    public boolean isTribeNode() {
        return false;
    }

    @Override
    public boolean isTagNode() {
        return false;
    }

    @Override
    public boolean isContinentNode() {
        return true;
    }

    @Override
    public boolean isVillageNode() {
        return false;
    }
}
