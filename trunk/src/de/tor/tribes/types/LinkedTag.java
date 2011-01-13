/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import de.tor.tribes.util.tag.TagManager;
import java.net.URLDecoder;
import org.jdom.Element;

/**
 *
 * @author Jejkal
 */
public class LinkedTag extends Tag {

    public final static int AND_RELATION = 0;
    public final static int OR_RELATION = 1;
    public final static int XOR_RELATION = 2;
    public final static int NOT_RELATION = 3;
    private String sLeftTagName = null;
    private String sRightTagName = null;
    private int iRelation = AND_RELATION;

    /**Factor a linked tag from its DOM representation
     * @param pElement DOM element received while reading the user tags
     * @return Tag Tag instance parsed from pElement
     */
    public static LinkedTag fromXml(Element pElement) throws Exception {
        Tag t = Tag.fromXml(pElement);
        LinkedTag lt = new LinkedTag(t.getName(), t.isShowOnMap());
        lt.setMapMarker(t.getMapMarker());
        String leftTagName = URLDecoder.decode(pElement.getChild("relation/left").getTextTrim(), "UTF-8");
        String rightTagName = URLDecoder.decode(pElement.getChild("relation/right").getTextTrim(), "UTF-8");
        int relationOperator = Integer.parseInt(pElement.getAttributeValue("relation/operator"));
        lt.setRelation(leftTagName, relationOperator, rightTagName);
        return lt;
    }

    public LinkedTag(String pName, boolean pShowOnMap) {
        super(pName, pShowOnMap);
    }

    public void setRelation(String pLeftTagName, int pRelation, String pRightTagName) {
        sLeftTagName = pLeftTagName;
        iRelation = pRelation;
        sRightTagName = pRightTagName;
    }

    public String getLeftTag() {
        return sLeftTagName;
    }

    public String getRightTag() {
        return sRightTagName;
    }

    public int getRelation() {
        return iRelation;
    }

    public void updateVillageList() {
        clearTaggedVillages();
        Tag left = TagManager.getSingleton().getTagByName(sLeftTagName);
        Tag right = TagManager.getSingleton().getTagByName(sRightTagName);
        if (left == null || right == null) {
            //linked tag not longer valid
            return;
        }

        switch (iRelation) {
            case AND_RELATION: {
                performAndLink(left, right);
                break;
            }
            case OR_RELATION: {
                performOrLink(left, right);
                break;
            }
            case XOR_RELATION: {
                performXOrLink(left, right);
                break;
            }
            case NOT_RELATION: {
                performNotLink(left, right);
                break;
            }
        }

    }

    /**Link the left and right tag by the AND operator, which means, that all villages tagged by both tags are also tagged by this linked tag*/
    private void performAndLink(Tag pLeftTag, Tag pRightTag) {
        for (Integer villageId : pLeftTag.getVillageIDs()) {
            //go through all villages tagged by the left hand tag
            if (pRightTag.getVillageIDs().contains(villageId)) {
                //if the village with the id 'villageId' is also tagged by the right hand tag, the add-link is accepted
                //and the village is added to the list of tagges villages
                tagVillage(villageId);
            }
        }
    }

    /**Link the left and right tag by the OR operator, which means, that all villages tagged by one of both tags are also tagged by this linked tag*/
    private void performOrLink(Tag pLeftTag, Tag pRightTag) {
        //add all villages tagged be the left hand tag
        for (Integer villageId : pLeftTag.getVillageIDs()) {
            tagVillage(villageId);
        }
        //add all villages tagged be the right hand tag
        for (Integer villageId : pRightTag.getVillageIDs()) {
            tagVillage(villageId);
        }
    }

    /**Link the left and right tag by the XOR operator, which means, that all villages tagged by one of both tags and
     * that are not tagged by the other tag are also tagged by this linked tag*/
    private void performXOrLink(Tag pLeftTag, Tag pRightTag) {
        for (Integer villageId : pLeftTag.getVillageIDs()) {
            //go through all villages tagged by the left hand tag
            if (!pRightTag.getVillageIDs().contains(villageId)) {
                //if the right hand tag does not contain this village, tag the village
                tagVillage(villageId);
            }
        }
        //add all villages tagged be the right hand tag
        for (Integer villageId : pRightTag.getVillageIDs()) {
            //go through all villages tagged by the right hand tag
            if (!pLeftTag.getVillageIDs().contains(villageId)) {
                //if the left hand tag does not contain this village, tag the village
                tagVillage(villageId);
            }
        }
    }

    /**Link the left and right tag by the NOT operator, which means, that all villages tagged by the left tag and not tagged by the right tag are tagged by this linked tag*/
    private void performNotLink(Tag pLeftTag, Tag pRightTag) {
        for (Integer villageId : pLeftTag.getVillageIDs()) {
            //go through all villages tagged by the left hand tag
            if (!pRightTag.getVillageIDs().contains(villageId)) {
                //if the right hand tag does not contain this village, tag the village
                tagVillage(villageId);
            }
        }
    }

    public static void main(String[] args) {
        LinkedTag tag = new LinkedTag("test", true);
        tag.updateVillageList();

    }
}
