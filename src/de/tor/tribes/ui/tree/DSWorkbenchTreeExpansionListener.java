/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.tree;

import de.tor.tribes.types.Ally;
import de.tor.tribes.types.Tag;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.tree.AllyNode;
import de.tor.tribes.util.tag.TagManager;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 *
 * @author Jejkal
 */
public class DSWorkbenchTreeExpansionListener implements TreeExpansionListener {

    private DefaultTreeModel mModel = null;

    public DSWorkbenchTreeExpansionListener(DefaultTreeModel pModel) {
        mModel = pModel;
    }

    @Override
    public void treeExpanded(TreeExpansionEvent event) {
        try {
            AbstractTreeNode lastNode = (AbstractTreeNode) event.getPath().getLastPathComponent();
            if (lastNode.isTribeNode()) {
                lastNode.removeAllChildren();
              ///  expandTribe((TribeNode) lastNode);
            } else if (lastNode.isAllyNode()) {
                //remove dummy
                lastNode.removeAllChildren();
              // expandAlly((AllyNode) lastNode);
            } 
        } catch (Exception e) {
        }
    }

    @Override
    public void treeCollapsed(TreeExpansionEvent event) {
        try {
            AbstractTreeNode lastNode = (AbstractTreeNode) event.getPath().getLastPathComponent();
            if (lastNode.isAllyNode()) {
                lastNode.removeAllChildren();
                lastNode.add(new DefaultMutableTreeNode(""));
            }
        } catch (Exception e) {
        }
    }

    /**Expand ally node
     * |-Tribe
     * |-Tribe
     */
    private void expandAlly(AllyNode pNode) {
       /* Ally a = pNode.getDSUserObject();
        for (Tribe t : a.getTribes()) {
            TribeNode tNode = new TribeNode(t);
            tNode.add(new DefaultMutableTreeNode());
            pNode.add(tNode);
        }
        mModel.nodeStructureChanged(pNode);*/
    }

    /**Expand tribe node
     * |-[Tag]
     *     |-Cont
     *        |-Village
     * |-Kein Tag
     *     |-Cont
     *         |-Village
     */
    private void expandTribe(TribeNode pNode) {
      /*  Tribe t = pNode.getDSUserObject();
        List<Tag> tags = new LinkedList<Tag>();
        Hashtable<String, TagNode> tagNodes = new Hashtable<String, TagNode>();
        Hashtable<TagNode, List<String>> tagContMappings = new Hashtable<TagNode, List<String>>();
        Hashtable<String, ContinentNode> contNodes = new Hashtable<String, ContinentNode>();
        //Hashtable<String, DSWorkbenchTreeNode<String>> contNodes = new Hashtable<String, DSWorkbenchTreeNode<String>>();
        for (Village v : t.getVillageList()) {
            //get continent
            String sCont = "K";
            int cont = v.getContinent();
            if (cont < 10) {
                sCont += "0" + cont;
            } else {
                sCont += cont;
            }
            ContinentNode contNode = contNodes.get(sCont);
            if (contNode == null) {
                contNode = new ContinentNode(sCont);
                contNodes.put(sCont, contNode);
            }
            //add village to cont
            contNode.add(new VillageNode(v));

            //get tag
            List<Tag> vTags = TagManager.getSingleton().getTags(v);
            TagNode tagNode = null;
            if (tags == null || tags.isEmpty()) {
                //no tag
                tagNode = tagNodes.get(TagNode.NO_TAG);
                if (tagNode == null) {
                    tagNode = new TagNode(new Tag(TagNode.NO_TAG, false));
                    tagNodes.put(TagNode.NO_TAG, tagNode);
                    pNode.add(tagNode);
                }

                List<String> tagConts = tagContMappings.get(tagNode.getDSUserObject().getName());
                if (tagConts == null) {
                    tagConts = new LinkedList<String>();
                    tagContMappings.put(tagNode, tagConts);
                    tagConts.add(sCont);
                    tagNode.add(contNode);
                } else {
                    if (!tagConts.contains(sCont)) {
                        //add continent and continent node
                        tagConts.add(sCont);
                        tagNode.add(contNode);
                    }
                }
            } else {
                for (Tag vTag : vTags) {
                    TagNode tNode = tagNodes.get(t.getName());
                    if (tNode == null) {
                        tagNodes.put(t.getName(), new TagNode(vTag));
                    }

                    List<String> tagConts = tagContMappings.get(tagNode.getDSUserObject().getName());
                    if (!tagConts.contains(sCont)) {
                        //add continent and continent node
                        tagConts.add(sCont);
                        tagNode.add(contNode);
                    }
                }
            }

        }
        mModel.nodeStructureChanged(pNode);*/
    }
}
