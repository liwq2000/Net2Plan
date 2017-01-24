package com.net2plan.gui.utils.topologyPane;

import static com.net2plan.gui.utils.topologyPane.VisualizationConstants.*;
import static com.net2plan.gui.utils.topologyPane.VisualizationConstants.DEFAULT_GUINODE_COLOR_PICK;
import static com.net2plan.gui.utils.topologyPane.VisualizationConstants.DEFAULT_GUINODE_COLOR;
import static com.net2plan.gui.utils.topologyPane.VisualizationConstants.DEFAULT_GUINODE_COLOR_PICK;
import static com.net2plan.gui.utils.topologyPane.VisualizationConstants.DEFAULT_INTRANODEGUILINK_EDGEDRAWCOLOR;
import static com.net2plan.gui.utils.topologyPane.VisualizationConstants.DEFAULT_INTRANODEGUILINK_EDGESTROKE;
import static com.net2plan.gui.utils.topologyPane.VisualizationConstants.DEFAULT_INTRANODEGUILINK_EDGESTROKE_ACTIVE;
import static com.net2plan.gui.utils.topologyPane.VisualizationConstants.DEFAULT_INTRANODEGUILINK_HASARROW;
import static com.net2plan.gui.utils.topologyPane.VisualizationConstants.DEFAULT_REGGUILINK_EDGECOLOR;
import static com.net2plan.gui.utils.topologyPane.VisualizationConstants.DEFAULT_REGGUILINK_EDGECOLOR_FAILED;
import static com.net2plan.gui.utils.topologyPane.VisualizationConstants.DEFAULT_REGGUILINK_EDGESTROKE;
import static com.net2plan.gui.utils.topologyPane.VisualizationConstants.DEFAULT_REGGUILINK_EDGESTROKE_ACTIVELAYER;
import static com.net2plan.gui.utils.topologyPane.VisualizationConstants.DEFAULT_REGGUILINK_HASARROW;
import static com.net2plan.gui.utils.topologyPane.VisualizationConstants.SCALE_IN;
import static com.net2plan.gui.utils.topologyPane.VisualizationConstants.SCALE_OUT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections15.BidiMap;
import org.apache.commons.collections15.bidimap.DualHashBidiMap;

import com.google.common.collect.Sets;
import com.net2plan.interfaces.networkDesign.Demand;
import com.net2plan.interfaces.networkDesign.InterLayerPropagationGraph;
import com.net2plan.interfaces.networkDesign.Link;
import com.net2plan.interfaces.networkDesign.MulticastDemand;
import com.net2plan.interfaces.networkDesign.MulticastTree;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.interfaces.networkDesign.NetworkElement;
import com.net2plan.interfaces.networkDesign.NetworkLayer;
import com.net2plan.interfaces.networkDesign.Node;
import com.net2plan.internal.Constants.NetworkElementType;
import com.net2plan.utils.Pair;
import com.net2plan.utils.Triple;

public class VisualizationState
{
    private boolean showNodeNames;
    private boolean showLinkLabels;
    private boolean showLinksInNonActiveLayer;
    private boolean showInterLayerLinks;
    private boolean showLowerLayerPropagation;
    private boolean showUpperLayerPropagation;
    private boolean showNonConnectedNodes;
    private int interLayerSpaceInPixels;
    private boolean isNetPlanEditable;
    private NetPlan currentNp;
    private Set<Node> nodesToHideAsMandatedByuserInTable;
    private Set<Link> linksToHideAsMandatedByUserInTable;

    /* This only can be changed calling to rebuild */
    private BidiMap<NetworkLayer, Integer> mapLayer2VisualizationOrder; // as many entries as layers
    private Map<NetworkLayer, Boolean> layerVisibilityMap;
    private Map<NetworkLayer, Boolean> mapShowLayerLinks;

    /* These need is recomputed inside a rebuild */
    private Map<Node, Set<GUILink>> cache_intraNodeGUILinks;
    private Map<Link, GUILink> cache_regularLinkMap;
    private BidiMap<NetworkLayer, Integer> cache_mapVisibleLayer2VisualizationOrderRemovingNonVisible; // as many elements as visible layers
    private Map<Node, Map<Pair<Integer, Integer>, GUILink>> cache_mapNode2IntraNodeGUILinkMap; // integers are orders of REAL VISIBLE LAYERS
    private Map<Node, List<GUINode>> cache_mapNode2ListVerticallyStackedGUINodes;

    private NetworkElementType pickedElementType;
    private NetworkElement pickedElementNotFR;
    private Pair<Demand,Link> pickedElementFR;

    public NetPlan getNetPlan()
    {
        return currentNp;
    }

    
    
    /**
	 * @return the showLowerLayerPropagation
	 */
	public boolean isShowLowerLayerPropagation()
	{
		return showLowerLayerPropagation;
	}



	/**
	 * @param showLowerLayerPropagation the showLowerLayerPropagation to set
	 */
	public void setShowLowerLayerPropagation(boolean showLowerLayerPropagation)
	{
		this.showLowerLayerPropagation = showLowerLayerPropagation;
		if (pickedElementType != null)
			if (pickedElementNotFR != null)
				this.pickElement(pickedElementNotFR);
			else
				this.pickElement(pickedElementFR);
	}



	/**
	 * @return the showUpperLayerPropagation
	 */
	public boolean isShowUpperLayerPropagation()
	{
		return showUpperLayerPropagation;
	}



	/**
	 * @param showUpperLayerPropagation the showUpperLayerPropagation to set
	 */
	public void setShowUpperLayerPropagation(boolean showUpperLayerPropagation)
	{
		this.showUpperLayerPropagation = showUpperLayerPropagation;
		if (pickedElementType != null)
			if (pickedElementNotFR != null)
				this.pickElement(pickedElementNotFR);
			else
				this.pickElement(pickedElementFR);
	}



	public VisualizationState(NetPlan currentNp, BidiMap<NetworkLayer, Integer> mapLayer2VisualizationOrder, Map<NetworkLayer,Boolean> layerVisibilityMap)
    {
        this.currentNp = currentNp;
        this.showNodeNames = false;
        this.showLinkLabels = false;
        this.showLinksInNonActiveLayer = true;
        this.showInterLayerLinks = true;
        this.showNonConnectedNodes = true;
        this.isNetPlanEditable = true;
        this.showLowerLayerPropagation = true;
        this.showUpperLayerPropagation = true;
        this.nodesToHideAsMandatedByuserInTable = new HashSet<>();
        this.linksToHideAsMandatedByUserInTable = new HashSet<>();
        this.interLayerSpaceInPixels = 50; 
        this.pickedElementType = null;
        this.pickedElementNotFR = null;
        this.pickedElementFR = null;

//        this.mapLayer2VisualizationOrder = mapLayer2VisualizationOrder;
//        this.mapLayerVisibility = new HashMap<>();
        this.mapShowLayerLinks = new HashMap<>();
        setLayerVisibilityAndOrder(currentNp ,mapLayer2VisualizationOrder , layerVisibilityMap);
    }

    public boolean isVisible(GUINode gn)
    {
        final Node n = gn.getAssociatedNetPlanNode();
        if (nodesToHideAsMandatedByuserInTable.contains(n)) return false;
        if (showNonConnectedNodes) return true;
        if (showInterLayerLinks && (getNumberOfVisibleLayers() > 1)) return true;
        if (n.getOutgoingLinks(gn.getLayer()).size() > 0) return true;
        if (n.getIncomingLinks(gn.getLayer()).size() > 0) return true;
        return false;
    }

    public boolean isVisible(GUILink gl)
    {
        if (gl.isIntraNodeLink()) return showInterLayerLinks;
        final Link e = gl.getAssociatedNetPlanLink();

        if (!mapShowLayerLinks.get(e.getLayer())) return false;
        if (linksToHideAsMandatedByUserInTable.contains(e)) return false;
        final boolean inActiveLayer = e.getLayer() == currentNp.getNetworkLayerDefault();
        if (!showLinksInNonActiveLayer && !inActiveLayer) return false;
        return true;
    }

    /**
     * @return the interLayerSpaceInNetPlanCoordinates
     */
    public int getInterLayerSpaceInPixels()
    {
        return interLayerSpaceInPixels;
    }

    /**
     * @param interLayerSpaceInNetPlanCoordinates the interLayerSpaceInNetPlanCoordinates to set
     */
    public void setInterLayerSpaceInPixels(int interLayerSpaceInPixels)
    {
        this.interLayerSpaceInPixels = interLayerSpaceInPixels;
    }

    /**
     * @return the showInterLayerLinks
     */
    public boolean isShowInterLayerLinks()
    {
        return showInterLayerLinks;
    }

    /**
     * @param showInterLayerLinks the showInterLayerLinks to set
     */
    public void setShowInterLayerLinks(boolean showInterLayerLinks)
    {
        this.showInterLayerLinks = showInterLayerLinks;
    }

    /**
     * @return the isNetPlanEditable
     */
    public boolean isNetPlanEditable()
    {
        return isNetPlanEditable;
    }

    /**
     * @param isNetPlanEditable the isNetPlanEditable to set
     */
    public void setNetPlanEditable(boolean isNetPlanEditable)
    {
        this.isNetPlanEditable = isNetPlanEditable;
    }

    public List<GUINode> getVerticallyStackedGUINodes(Node n)
    {
        return cache_mapNode2ListVerticallyStackedGUINodes.get(n);
    }

    public GUINode getAssociatedGUINode(Node n, NetworkLayer layer)
    {
        final Integer trueVisualizationIndex = cache_mapVisibleLayer2VisualizationOrderRemovingNonVisible.get(layer);
        if (trueVisualizationIndex == null) return null;
        return getVerticallyStackedGUINodes(n).get(trueVisualizationIndex);
    }

    public GUILink getAssociatedGUILink(Link e)
    {
        return cache_regularLinkMap.get(e);
    }

    public Pair<Set<GUILink>, Set<GUILink>> getAssociatedGUILinksIncludingCoupling(Link e, boolean regularLinkIsPrimary)
    {
        Set<GUILink> resPrimary = new HashSet<>();
        Set<GUILink> resBackup = new HashSet<>();
        if (regularLinkIsPrimary) resPrimary.add(getAssociatedGUILink(e));
        else resBackup.add(getAssociatedGUILink(e));
        if (!e.isCoupled()) return Pair.of(resPrimary, resBackup);
        if (e.getCoupledDemand() != null)
        {
            /* add the intranode links */
            final NetworkLayer upperLayer = e.getLayer();
            final NetworkLayer downLayer = e.getCoupledDemand().getLayer();
            if (regularLinkIsPrimary)
            {
                resPrimary.addAll(getIntraNodeGUILinkSequence(e.getOriginNode(), upperLayer, downLayer));
                resPrimary.addAll(getIntraNodeGUILinkSequence(e.getDestinationNode(), downLayer, upperLayer));
            } else
            {
                resBackup.addAll(getIntraNodeGUILinkSequence(e.getOriginNode(), upperLayer, downLayer));
                resBackup.addAll(getIntraNodeGUILinkSequence(e.getDestinationNode(), downLayer, upperLayer));
            }

			/* add the regular links */
            Pair<Set<Link>, Set<Link>> traversedLinks = e.getCoupledDemand().getLinksThisLayerPotentiallyCarryingTraffic(true);
            for (Link ee : traversedLinks.getFirst())
            {
                Pair<Set<GUILink>, Set<GUILink>> pairGuiLinks = getAssociatedGUILinksIncludingCoupling(ee, true);
                if (regularLinkIsPrimary) resPrimary.addAll(pairGuiLinks.getFirst());
                else resBackup.addAll(pairGuiLinks.getFirst());
                resBackup.addAll(pairGuiLinks.getSecond());
            }
            for (Link ee : traversedLinks.getSecond())
            {
                Pair<Set<GUILink>, Set<GUILink>> pairGuiLinks = getAssociatedGUILinksIncludingCoupling(ee, false);
                resPrimary.addAll(pairGuiLinks.getFirst());
                resBackup.addAll(pairGuiLinks.getSecond());
            }
        } else if (e.getCoupledMulticastDemand() != null)
        {
            /* add the intranode links */
            final NetworkLayer upperLayer = e.getLayer();
            final MulticastDemand lowerLayerDemand = e.getCoupledMulticastDemand();
            final NetworkLayer downLayer = lowerLayerDemand.getLayer();
            if (regularLinkIsPrimary)
            {
                resPrimary.addAll(getIntraNodeGUILinkSequence(lowerLayerDemand.getIngressNode(), upperLayer, downLayer));
                resPrimary.addAll(getIntraNodeGUILinkSequence(lowerLayerDemand.getIngressNode(), downLayer, upperLayer));
                for (Node n : lowerLayerDemand.getEgressNodes())
                {
                    resPrimary.addAll(getIntraNodeGUILinkSequence(n, upperLayer, downLayer));
                    resPrimary.addAll(getIntraNodeGUILinkSequence(n, downLayer, upperLayer));
                }
            } else
            {
                resBackup.addAll(getIntraNodeGUILinkSequence(lowerLayerDemand.getIngressNode(), upperLayer, downLayer));
                resBackup.addAll(getIntraNodeGUILinkSequence(lowerLayerDemand.getIngressNode(), downLayer, upperLayer));
                for (Node n : lowerLayerDemand.getEgressNodes())
                {
                    resBackup.addAll(getIntraNodeGUILinkSequence(n, upperLayer, downLayer));
                    resBackup.addAll(getIntraNodeGUILinkSequence(n, downLayer, upperLayer));
                }
            }

            for (MulticastTree t : lowerLayerDemand.getMulticastTrees())
                for (Link ee : t.getLinkSet())
                {
                    Pair<Set<GUILink>, Set<GUILink>> pairGuiLinks = getAssociatedGUILinksIncludingCoupling(ee, true);
                    resPrimary.addAll(pairGuiLinks.getFirst());
                    resBackup.addAll(pairGuiLinks.getSecond());
                }
        }
        return Pair.of(resPrimary, resBackup);
    }

    public GUILink getIntraNodeGUILink(Node n, NetworkLayer from, NetworkLayer to)
    {
        final Integer fromRealVIndex = cache_mapVisibleLayer2VisualizationOrderRemovingNonVisible.get(from);
        final Integer toRealVIndex = cache_mapVisibleLayer2VisualizationOrderRemovingNonVisible.get(to);
        if ((fromRealVIndex == null) || (toRealVIndex == null)) return null;
        return cache_mapNode2IntraNodeGUILinkMap.get(n).get(Pair.of(fromRealVIndex, toRealVIndex));
    }

    public Set<GUILink> getIntraNodeGUILinks(Node n)
    {
        return cache_intraNodeGUILinks.get(n);
    }

    public List<GUILink> getIntraNodeGUILinkSequence(Node n, NetworkLayer from, NetworkLayer to)
    {
        if (from.getNetPlan() != currentNp) throw new RuntimeException("Bad");
        if (to.getNetPlan() != currentNp) throw new RuntimeException("Bad");
        final Integer fromRealVIndex = cache_mapVisibleLayer2VisualizationOrderRemovingNonVisible.get(from);
        final Integer toRealVIndex = cache_mapVisibleLayer2VisualizationOrderRemovingNonVisible.get(to);

        final List<GUILink> res = new LinkedList<>();
        if ((fromRealVIndex == null) || (toRealVIndex == null)) return res;
        if (fromRealVIndex == toRealVIndex) return res;
        final int increment = toRealVIndex > fromRealVIndex ? 1 : -1;
        int vLayerIndex = fromRealVIndex;
        do
        {
            final int origin = vLayerIndex;
            final int destination = vLayerIndex + increment;
            res.add(cache_mapNode2IntraNodeGUILinkMap.get(n).get(Pair.of(origin, destination)));
            vLayerIndex += increment;
        } while (vLayerIndex != toRealVIndex);

        return res;
    }

    public void setLayerVisibilityAndOrder (NetPlan newCurrentNetPlan)
    {
    	setLayerVisibilityAndOrder (newCurrentNetPlan , this.mapLayer2VisualizationOrder , this.layerVisibilityMap);
    }

    public void setLayerVisibilityAndOrder (NetPlan newCurrentNetPlan , BidiMap<NetworkLayer,Integer> layerVisiblityOrderMap,
            Map<NetworkLayer,Boolean> layerVisibilityMap)
    {
        if (newCurrentNetPlan == null) throw new RuntimeException("Trying to update an empty topology");
        final boolean netPlanChanged = (this.currentNp != newCurrentNetPlan);

        this.currentNp = newCurrentNetPlan;

        this.mapLayer2VisualizationOrder = new DualHashBidiMap<>(layerVisiblityOrderMap);
        this.layerVisibilityMap = new HashMap<> (layerVisibilityMap);
//        final List<Boolean> isLayerVisibleIndexedByLayerIndex = new ArrayList<>(L);
//        for (NetworkLayer layer : currentNp.getNetworkLayers())
//        {
//           	mapLayer2VisualizationOrder.put(layer, orderOfLayerIndexedByLayerIndex.get(layer.getIndex()));
//           	isLayerVisibleIndexedByLayerIndex.add(layerVisibilityIndexedByLayerIndex.get(layer.getIndex()));
//        }
//        
//        if (mapLayer2VisualizationOrder == null) mapLayer2VisualizationOrder = this.mapLayer2VisualizationOrder;

        if (!mapLayer2VisualizationOrder.keySet().equals(new HashSet<>(currentNp.getNetworkLayers())))
            throw new RuntimeException();
        if (!layerVisibilityMap.keySet().equals(new HashSet<>(currentNp.getNetworkLayers())))
            throw new RuntimeException();

        /* Just in case the layers have changed */
        for (NetworkLayer layer : currentNp.getNetworkLayers())
        	if (!mapShowLayerLinks.keySet().contains(layer))
        		this.mapShowLayerLinks.put(layer , true);

		/* Update the interlayer space */
//        this.interLayerSpaceInPixels = 50; //getDefaultVerticalDistanceForInterLayers();

        if (netPlanChanged)
        {
            nodesToHideAsMandatedByuserInTable = new HashSet<>();
            linksToHideAsMandatedByUserInTable = new HashSet<>();
        }
        this.cache_intraNodeGUILinks = new HashMap<>();
        this.cache_regularLinkMap = new HashMap<>();
        this.cache_mapVisibleLayer2VisualizationOrderRemovingNonVisible = new DualHashBidiMap<>();
        this.cache_mapNode2IntraNodeGUILinkMap = new HashMap<>();
        this.cache_mapNode2ListVerticallyStackedGUINodes = new HashMap<>();
        for (int layerIndex = 0; layerIndex < currentNp.getNumberOfLayers(); layerIndex++)
        {
            final NetworkLayer layer = currentNp.getNetworkLayer(layerIndex);
            if (isLayerVisible(layer))
                cache_mapVisibleLayer2VisualizationOrderRemovingNonVisible.put(layer, cache_mapVisibleLayer2VisualizationOrderRemovingNonVisible.size());
        }
        for (Node n : currentNp.getNodes())
        {
            List<GUINode> guiNodesThisNode = new ArrayList<>();
            cache_mapNode2ListVerticallyStackedGUINodes.put(n, guiNodesThisNode);
            Set<GUILink> intraNodeGUILinksThisNode = new HashSet<>();
            cache_intraNodeGUILinks.put(n, intraNodeGUILinksThisNode);
            Map<Pair<Integer, Integer>, GUILink> thisNodeInterLayerLinksInfoMap = new HashMap<>();
            cache_mapNode2IntraNodeGUILinkMap.put(n, thisNodeInterLayerLinksInfoMap);
            for (int trueVisualizationOrderIndex = 0; trueVisualizationOrderIndex < cache_mapVisibleLayer2VisualizationOrderRemovingNonVisible.size(); trueVisualizationOrderIndex++)
            {
                final NetworkLayer newLayer = cache_mapVisibleLayer2VisualizationOrderRemovingNonVisible.inverseBidiMap().get(trueVisualizationOrderIndex);
                final GUINode gn = new GUINode(n, newLayer, this);
                guiNodesThisNode.add(gn);
                if (trueVisualizationOrderIndex > 0)
                {
                    final GUINode lowerLayerGNode = guiNodesThisNode.get(trueVisualizationOrderIndex - 1);
                    final GUINode upperLayerGNode = guiNodesThisNode.get(trueVisualizationOrderIndex);
                    if (upperLayerGNode != gn) throw new RuntimeException();
                    final GUILink glLowerToUpper = new GUILink(null, lowerLayerGNode, gn);
                    final GUILink glUpperToLower = new GUILink(null, gn, lowerLayerGNode);
                    intraNodeGUILinksThisNode.add(glLowerToUpper);
                    intraNodeGUILinksThisNode.add(glUpperToLower);
                    thisNodeInterLayerLinksInfoMap.put(Pair.of(trueVisualizationOrderIndex - 1, trueVisualizationOrderIndex), glLowerToUpper);
                    thisNodeInterLayerLinksInfoMap.put(Pair.of(trueVisualizationOrderIndex, trueVisualizationOrderIndex - 1), glUpperToLower);
                }
            }
        }
        for (int trueVisualizationOrderIndex = 0; trueVisualizationOrderIndex < cache_mapVisibleLayer2VisualizationOrderRemovingNonVisible.size(); trueVisualizationOrderIndex++)
        {
            final NetworkLayer layer = cache_mapVisibleLayer2VisualizationOrderRemovingNonVisible.inverseBidiMap().get(trueVisualizationOrderIndex);
            for (Link e : currentNp.getLinks(layer))
            {
                final GUINode gn1 = cache_mapNode2ListVerticallyStackedGUINodes.get(e.getOriginNode()).get(trueVisualizationOrderIndex);
                final GUINode gn2 = cache_mapNode2ListVerticallyStackedGUINodes.get(e.getDestinationNode()).get(trueVisualizationOrderIndex);
                final GUILink gl1 = new GUILink(e, gn1, gn2);
                cache_regularLinkMap.put(e, gl1);
            }
        }

        checkCacheConsistency();
    }

    private void checkCacheConsistency()
    {
        for (Node n : currentNp.getNodes())
        {
            assertTrue(cache_intraNodeGUILinks.get(n) != null);
            assertTrue(cache_mapNode2IntraNodeGUILinkMap.get(n) != null);
            assertTrue(cache_mapNode2ListVerticallyStackedGUINodes.get(n) != null);
            for (Entry<Pair<Integer, Integer>, GUILink> entry : cache_mapNode2IntraNodeGUILinkMap.get(n).entrySet())
            {
                final int fromLayer = entry.getKey().getFirst();
                final int toLayer = entry.getKey().getSecond();
                final GUILink gl = entry.getValue();
                assertTrue(gl.isIntraNodeLink());
                assertTrue(gl.getOriginNode().getAssociatedNetPlanNode() == n);
                assertTrue(gl.getOriginNode().getVisualizationOrderRemovingNonVisibleLayers() == fromLayer);
                assertTrue(gl.getDestinationNode().getVisualizationOrderRemovingNonVisibleLayers() == toLayer);
            }
            assertEquals(new HashSet<>(cache_mapNode2IntraNodeGUILinkMap.get(n).values()), cache_intraNodeGUILinks.get(n));
            for (GUILink gl : cache_intraNodeGUILinks.get(n))
            {
                assertTrue(gl.isIntraNodeLink());
                assertEquals(gl.getOriginNode().getAssociatedNetPlanNode(), n);
                assertEquals(gl.getDestinationNode().getAssociatedNetPlanNode(), n);
            }
            assertEquals(cache_mapNode2ListVerticallyStackedGUINodes.get(n).size(), getNumberOfVisibleLayers());
            int indexLayer = 0;
            for (GUINode gn : cache_mapNode2ListVerticallyStackedGUINodes.get(n))
            {
                assertEquals(gn.getLayer(), cache_mapVisibleLayer2VisualizationOrderRemovingNonVisible.inverseBidiMap().get(indexLayer));
                assertEquals(gn.getVisualizationOrderRemovingNonVisibleLayers(), indexLayer++);
                assertEquals(gn.getAssociatedNetPlanNode(), n);
            }
        }
//
//		for (NetworkLayer layer : currentNp.getNetworkLayers())
//		{
//			if (cache_layer2VLayerMap.get(layer) == null) 
//				for (VisualizationLayer vl : vLayers) 
//					assertTrue (!vl.npLayersToShow.contains(layer));
//			if (cache_layer2VLayerMap.get(layer) != null) 
//				for (VisualizationLayer vl : vLayers) 
//					if (vl == cache_layer2VLayerMap.get(layer))
//						assertTrue (vl.npLayersToShow.contains(layer));
//					else
//						assertTrue (!vl.npLayersToShow.contains(layer));
//			if (cache_layer2VLayerMap.get(layer) != null)
//				for (Link e : currentNp.getLinks(layer))
//				{
//					final GUILink gl = regularLinkMap.get(e);
//					assertTrue (gl != null);
//					assertEquals (gl.getAssociatedNetPlanLink() , e);
//					assertTrue (cache_layer2VLayerMap.get(layer).guiIntraLayerLinks.contains(gl));
//				}
//		}
//		
    }

    public boolean decreaseFontSizeAll()
    {
        boolean changedSize = false;
        for (Node n : currentNp.getNodes())
            for (GUINode gn : cache_mapNode2ListVerticallyStackedGUINodes.get(n))
                changedSize |= gn.decreaseFontSize();
        return changedSize;
    }

    public void increaseFontSizeAll()
    {
        for (Node n : currentNp.getNodes())
            for (GUINode gn : cache_mapNode2ListVerticallyStackedGUINodes.get(n))
                gn.increaseFontSize();
    }

    public void decreaseNodeSizeAll()
    {
        for (Node n : currentNp.getNodes())
            for (GUINode gn : cache_mapNode2ListVerticallyStackedGUINodes.get(n))
                gn.setShapeSize(gn.getShapeSize() * SCALE_OUT);
    }

    public void increaseNodeSizeAll()
    {
        for (GUINode gn : getAllGUINodes())
            gn.setShapeSize(gn.getShapeSize() * SCALE_IN);
    }

    public int getNumberOfVisibleLayers()
    {
        return cache_mapVisibleLayer2VisualizationOrderRemovingNonVisible.size();
    }

//	public void setVisualizationLayers (List<List<NetworkLayer>> listOfLayersPerVL , NetPlan netPlan)
//	{
//		if (listOfLayersPerVL.isEmpty()) throw new Net2PlanException ("At least one visualization layer is needed");
//		Set<NetworkLayer> alreadyAppearingLayer = new HashSet<> ();
//		for (List<NetworkLayer> layers : listOfLayersPerVL)
//		{	
//			if (layers.isEmpty()) throw new Net2PlanException ("A visualization layer cannot be empty");
//			for (NetworkLayer layer : layers)
//			{
//				if (layer.getNetPlan() != netPlan) throw new RuntimeException ("Bad");
//				if (alreadyAppearingLayer.contains(layer)) throw new Net2PlanException ("A layer cannot belong to more than one visualization layer");
//				alreadyAppearingLayer.add(layer);
//			}
//		}
//		this.currentNp = netPlan;
//		this.vLayers.clear();
//		for (List<NetworkLayer> layers : listOfLayersPerVL)
//			vLayers.add(new VisualizationLayer(layers, this , vLayers.size()));
//
//		this.cache_layer2VLayerMap = new HashMap<> (); 
//		for (VisualizationLayer visualizationLayer : vLayers) 
//			for (NetworkLayer layer : visualizationLayer.npLayersToShow) 
//				cache_layer2VLayerMap.put(layer , visualizationLayer);
////		for (VisualizationLayer visualizationLayer : vLayers) 
////			visualizationLayer.updateGUINodeAndGUILinks();
//	}

    /**
     * @return the showNodeNames
     */
    public boolean isShowNodeNames()
    {
        return showNodeNames;
    }

    /**
     * @param showNodeNames the showNodeNames to set
     */
    public void setShowNodeNames(boolean showNodeNames)
    {
        this.showNodeNames = showNodeNames;
    }

    /**
     * @return the showLinkLabels
     */
    public boolean isShowLinkLabels()
    {
        return showLinkLabels;
    }

    /**
     * @param showLinkLabels the showLinkLabels to set
     */
    public void setShowLinkLabels(boolean showLinkLabels)
    {
        this.showLinkLabels = showLinkLabels;
    }

    /**
     * @return the showNonConnectedNodes
     */
    public boolean isShowNonConnectedNodes()
    {
        return showNonConnectedNodes;
    }

    /**
     * @param showNonConnectedNodes the showNonConnectedNodes to set
     */
    public void setShowNonConnectedNodes(boolean showNonConnectedNodes)
    {
        this.showNonConnectedNodes = showNonConnectedNodes;
    }

    public void setVisibilityState(Node n, boolean isVisible)
    {
        if (isVisible) nodesToHideAsMandatedByuserInTable.remove(n);
        else nodesToHideAsMandatedByuserInTable.add(n);
    }

    public boolean isVisible(Node n)
    {
        return !nodesToHideAsMandatedByuserInTable.contains(n);
    }

    public void setVisibilityState(Link e, boolean isVisible)
    {
        if (isVisible) linksToHideAsMandatedByUserInTable.remove(e);
        else linksToHideAsMandatedByUserInTable.add(e);
    }

    public boolean isVisible(Link e)
    {
        return !linksToHideAsMandatedByUserInTable.contains(e);
    }

    /* Everything to its default color, shape. Separated nodes, are set together again. Visibility state is unchanged */
//    public void resetColorAndShapeState()
//    {
//        for (GUINode n : getAllGUINodes())
//        {
//            n.setFont(DEFAULT_GUINODE_FONT);
//            n.setDrawPaint(DEFAULT_GUINODE_DRAWCOLOR);
//            n.setFillPaint(DEFAULT_GUINODE_FILLCOLOR);
//            n.setShape(DEFAULT_GUINODE_SHAPE);
//            n.setShapeSize(DEFAULT_GUINODE_SHAPESIZE);
//        }
//        for (GUILink e : getAllGUILinks(true, false))
//        {
//            e.setHasArrow(DEFAULT_REGGUILINK_HASARROW);
//            e.setArrowStroke(DEFAULT_REGGUILINK_EDGESTROKE_ACTIVELAYER , DEFAULT_REGGUILINK_EDGESTROKE);
//            e.setEdgeStroke(DEFAULT_REGGUILINK_EDGESTROKE_ACTIVELAYER , DEFAULT_REGGUILINK_EDGESTROKE);
//            e.setArrowDrawPaint(DEFAULT_REGGUILINK_EDGECOLOR);
//            e.setArrowFillPaint(DEFAULT_REGGUILINK_EDGECOLOR);
//            e.setEdgeDrawPaint(DEFAULT_REGGUILINK_EDGECOLOR);
//            e.setShownSeparated(false);
//        }
//        for (GUILink e : getAllGUILinks(false, true))
//        {
//            e.setHasArrow(DEFAULT_INTRANODEGUILINK_HASARROW);
//            e.setArrowStroke(DEFAULT_INTRANODEGUILINK_EDGESTROKE_ACTIVE , DEFAULT_INTRANODEGUILINK_EDGESTROKE);
//            e.setEdgeStroke(DEFAULT_INTRANODEGUILINK_EDGESTROKE_ACTIVE , DEFAULT_INTRANODEGUILINK_EDGESTROKE);
//            e.setArrowDrawPaint(DEFAULT_INTRANODEGUILINK_EDGEDRAWCOLOR);
//            e.setArrowFillPaint(DEFAULT_INTRANODEGUILINK_EDGEDRAWCOLOR);
//            e.setEdgeDrawPaint(DEFAULT_INTRANODEGUILINK_EDGEDRAWCOLOR);
//            e.setShownSeparated(false);
//        }
//    }

    public Set<GUILink> getAllGUILinks(boolean includeRegularLinks, boolean includeInterLayerLinks)
    {
        Set<GUILink> res = new HashSet<>();
        if (includeRegularLinks) res.addAll(cache_regularLinkMap.values());
        if (includeInterLayerLinks) for (Node n : currentNp.getNodes()) res.addAll(this.cache_intraNodeGUILinks.get(n));
        return res;
    }

    public Set<GUINode> getAllGUINodes()
    {
        Set<GUINode> res = new HashSet<>();
        for (List<GUINode> list : this.cache_mapNode2ListVerticallyStackedGUINodes.values()) res.addAll(list);
        return res;
    }


    public void setNodeProperties(Collection<GUINode> nodes, Color color, Shape shape, double shapeSize)
    {
        for (GUINode n : nodes)
        {
            if (color != null)
            {
                n.setDrawPaint(color);
                n.setFillPaint(color);
            }
            if (shape != null)
            {
                n.setShape(shape);
            }
            if (shapeSize > 0)
            {
                n.setShapeSize(shapeSize);
            }
        }
    }

    public void setLinkProperties(Collection<GUILink> links, Color color, Stroke stroke, Boolean hasArrows, Boolean shownSeparated)
    {
        for (GUILink e : links)
        {
            if (color != null)
            {
                e.setArrowDrawPaint(color);
                e.setArrowFillPaint(color);
                e.setEdgeDrawPaint(color);
            }
            if (stroke != null)
            {
                e.setArrowStroke(stroke , stroke);
                e.setEdgeStroke(stroke , stroke);
            }
            if (hasArrows != null)
            {
                e.setHasArrow(hasArrows);
            }
            if (shownSeparated != null)
            {
                e.setShownSeparated(shownSeparated);
            }
        }
    }

    public double getDefaultVerticalDistanceForInterLayers()
    {
        if (currentNp.getNumberOfNodes() == 0) return 1.0;
        final int numVisibleLayers = getNumberOfVisibleLayers() == 0 ? currentNp.getNumberOfLayers() : getNumberOfVisibleLayers();
        double minY = Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        for (Node n : currentNp.getNodes())
        {
            final double y = n.getXYPositionMap().getY();
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
        }
        if ((maxY - minY < 1e-6)) return Math.abs(maxY) / (30 * numVisibleLayers);
        return (maxY - minY) / (30 * numVisibleLayers);
    }

    public void setLayerVisibility(final NetworkLayer layer, final boolean isVisible)
    {
    	if (!this.currentNp.getNetworkLayers().contains(layer)) throw new RuntimeException ();
    	BidiMap<NetworkLayer,Integer> new_layerVisiblityOrderMap = new DualHashBidiMap<> (this.mapLayer2VisualizationOrder);
    	Map<NetworkLayer,Boolean> new_layerVisibilityMap = new HashMap<> (this.layerVisibilityMap);
    	new_layerVisibilityMap.put(layer,isVisible);
    	setLayerVisibilityAndOrder(this.currentNp , new_layerVisiblityOrderMap , new_layerVisibilityMap);
    }

    public boolean isLayerVisible(final NetworkLayer layer)
    {
        return layerVisibilityMap.get(layer);
    }

    public void setLayerLinksVisibility(final NetworkLayer layer, final boolean showLinks)
    {
    	if (!this.currentNp.getNetworkLayers().contains(layer)) throw new RuntimeException ();
        mapShowLayerLinks.put(layer, showLinks);
    }

    public boolean isLayerLinksShown(final NetworkLayer layer)
    {
        return mapShowLayerLinks.get(layer);
    }

    public NetworkLayer getNetworkLayerAtVisualizationOrderRemovingNonVisible(int trueVisualizationOrder)
    {
        if (trueVisualizationOrder < 0) throw new RuntimeException("");
        if (trueVisualizationOrder >= getNumberOfVisibleLayers()) throw new RuntimeException("");
        return cache_mapVisibleLayer2VisualizationOrderRemovingNonVisible.inverseBidiMap().get(trueVisualizationOrder);
    }

    public NetworkLayer getNetworkLayerAtVisualizationOrderNotRemovingNonVisible(int visualizationOrder)
    {
        if (visualizationOrder < 0) throw new RuntimeException("");
        if (visualizationOrder >= currentNp.getNumberOfLayers()) throw new RuntimeException("");
        return mapLayer2VisualizationOrder.inverseBidiMap().get(visualizationOrder);
    }

    public int getVisualizationOrderRemovingNonVisible(NetworkLayer layer)
    {
        Integer res = cache_mapVisibleLayer2VisualizationOrderRemovingNonVisible.get(layer);
        if (res == null) throw new RuntimeException("");
        return res;
    }

    public int getVisualizationOrderNotRemovingNonVisible(NetworkLayer layer)
    {
        Integer res = mapLayer2VisualizationOrder.get(layer);
        if (res == null) throw new RuntimeException("");
        return res;
    }

    public static Pair<BidiMap<NetworkLayer, Integer>, Map<NetworkLayer,Boolean>> generateDefaultVisualizationLayerInfo(NetPlan np)
    {
        final int L = np.getNumberOfLayers();
        final BidiMap<NetworkLayer, Integer> res_1 = new DualHashBidiMap<>();
        final Map<NetworkLayer,Boolean> res_2 = new HashMap<>(L);

        for (NetworkLayer layer : np.getNetworkLayers())
        {
        	res_1.put (layer,res_1.size());
        	res_2.put (layer , true);
        }
        return Pair.of(res_1, res_2);
    }

    public boolean isPickedElement () { return pickedElementType != null; }
    
    public NetworkElementType getPickedElementType () { return pickedElementType; }
    
    public void pickElement (Pair<Demand,Link> fr)
    {
    	
    }

    public void pickElement (NetworkElement e)
    {
    	resetPickedState();
    	this.pickedElementType = NetworkElementType.getType(e);
    	this.pickedElementFR = null;
    	this.pickedElementNotFR = e;
    	if (e instanceof Node)
    	{
    		final Node ee = (Node) e;
    		for (GUINode gn : getVerticallyStackedGUINodes(ee))
    		{
                gn.setDrawPaint(DEFAULT_GUINODE_COLOR_PICK);
                gn.setFillPaint(DEFAULT_GUINODE_COLOR_PICK);
    		}
    	} else if (e instanceof Link)
    	{
    		final Link pickedLink = (Link) e;
    		final GUILink gl = getAssociatedGUILink(pickedLink);
    		if (gl == null) return; // this means the link layer is not visible
    		final Triple<Map<Demand,Set<Link>>,Map<Demand,Set<Link>>,Map<Pair<MulticastDemand,Node>,Set<Link>>> triple = 
    				pickedLink.getLinksThisLayerPotentiallyCarryingTrafficTraversingThisLink(false);
    		final Set<Link> linksPrimary = triple.getFirst().values().stream().flatMap(set->set.stream()).collect (Collectors.toSet());
    		final Set<Link> linksBackup = triple.getSecond().values().stream().flatMap(set->set.stream()).collect (Collectors.toSet());
    		final Set<Link> linksMulticast = triple.getThird().values().stream().flatMap(set->set.stream()).collect (Collectors.toSet());
    		drawColateralLinks (Sets.union(Sets.union(linksPrimary , linksBackup) , linksMulticast) , DEFAULT_REGGUILINK_EDGECOLOR_PICKED);
    		if (showLowerLayerPropagation)
    		{
    			final InterLayerPropagationGraph ipg = new InterLayerPropagationGraph (null , Sets.newHashSet(pickedLink) , null , false , false);
    			drawColateralLinks (ipg.getLinksInGraph() , DEFAULT_REGGUILINK_EDGECOLOR_PICKED);
    		}
    		if (showUpperLayerPropagation)
    		{
    			final InterLayerPropagationGraph ipg = new InterLayerPropagationGraph (null , Sets.newHashSet(pickedLink) , null , true , false);
    			drawColateralLinks (ipg.getLinksInGraph() , DEFAULT_REGGUILINK_EDGECOLOR_PICKED);
    		}
    		/* Picked link the last, so overrides the rest */
    		gl.setHasArrow(true);
			gl.setArrowStroke(DEFAULT_REGGUILINK_EDGESTROKE_PICKED , DEFAULT_REGGUILINK_EDGESTROKE_PICKED);
			gl.setEdgeStroke(DEFAULT_REGGUILINK_EDGESTROKE_PICKED , DEFAULT_REGGUILINK_EDGESTROKE_PICKED);
			final Paint color = pickedLink.isDown()? DEFAULT_REGGUILINK_EDGECOLOR_FAILED : DEFAULT_REGGUILINK_EDGECOLOR_PICKED;
			gl.setArrowDrawPaint(color);
			gl.setArrowFillPaint(color);
			gl.setEdgeDrawPaint(color);
			gl.setShownSeparated(true);
    	} else if (e instanceof Demand)
    	{
    		final Demand pickedDemand = (Demand) e;
    		final GUINode gnOrigin = getAssociatedGUINode(pickedDemand.getIngressNode() , pickedDemand.getLayer());
    		if (gnOrigin == null) return; // not a visible layer
    		final GUINode gnDestination = getAssociatedGUINode(pickedDemand.getEgressNode() , pickedDemand.getLayer());
    		final Pair<Set<Link>,Set<Link>> pair = pickedDemand.getLinksThisLayerPotentiallyCarryingTraffic(false);
    		final Set<Link> linksPrimary = pair.getFirst();
    		final Set<Link> linksBackup = pair.getSecond();
    		final Set<Link> linksPrimaryAndBackup = Sets.intersection(linksPrimary , linksBackup);
    		drawColateralLinks (Sets.difference(linksPrimary , linksPrimaryAndBackup) , DEFAULT_REGGUILINK_EDGECOLOR_PICKED);
    		drawColateralLinks (Sets.difference(linksBackup , linksPrimaryAndBackup) , DEFAULT_REGGUILINK_EDGECOLOR_BACKUP);
    		drawColateralLinks (linksPrimaryAndBackup , DEFAULT_REGGUILINK_EDGECOLOR_BACKUPANDPRIMARY);
    		if (showLowerLayerPropagation)
    		{
    			final InterLayerPropagationGraph ipg = new InterLayerPropagationGraph (Sets.newHashSet(pickedDemand) , null , null , false , false);
    			drawColateralLinks (ipg.getLinksInGraph() , DEFAULT_REGGUILINK_EDGECOLOR_PICKED);
    		}
    		if (showUpperLayerPropagation)
    		{
    			final InterLayerPropagationGraph ipg = new InterLayerPropagationGraph (Sets.newHashSet(pickedDemand) , null , null , true , false);
    			drawColateralLinks (ipg.getLinksInGraph() , DEFAULT_REGGUILINK_EDGECOLOR_PICKED);
    		}
    		/* Picked link the last, so overrides the rest */
            gnOrigin.setDrawPaint(DEFAULT_GUINODE_COLOR_ORIGINFLOW);
            gnOrigin.setFillPaint(DEFAULT_GUINODE_COLOR_ORIGINFLOW);
            gnDestination.setDrawPaint(DEFAULT_GUINODE_COLOR_ENDFLOW);
            gnDestination.setFillPaint(DEFAULT_GUINODE_COLOR_ENDFLOW);
    	} else throw new RuntimeException ("Bad");
    	
    }
    
    public void resetPickedState ()
    {
    	this.pickedElementType = null;
    	this.pickedElementFR = null;
    	this.pickedElementNotFR = null;

        for (GUINode n : getAllGUINodes())
        {
            n.setDrawPaint(DEFAULT_GUINODE_COLOR);
            n.setFillPaint(DEFAULT_GUINODE_COLOR);
        }
        for (GUILink e : getAllGUILinks(true, false))
        {
            e.setHasArrow(DEFAULT_REGGUILINK_HASARROW);
            e.setArrowStroke(DEFAULT_REGGUILINK_EDGESTROKE_ACTIVELAYER , DEFAULT_REGGUILINK_EDGESTROKE);
            e.setEdgeStroke(DEFAULT_REGGUILINK_EDGESTROKE_ACTIVELAYER , DEFAULT_REGGUILINK_EDGESTROKE);
            final boolean isDown = e.getAssociatedNetPlanLink().isDown(); 
            final Paint color = isDown? DEFAULT_REGGUILINK_EDGECOLOR_FAILED : DEFAULT_REGGUILINK_EDGECOLOR; 
            e.setArrowDrawPaint(color);
            e.setArrowFillPaint(color);
            e.setEdgeDrawPaint(color);
            e.setShownSeparated(isDown);
        }
        for (GUILink e : getAllGUILinks(false, true))
        {
            e.setHasArrow(DEFAULT_INTRANODEGUILINK_HASARROW);
            e.setArrowStroke(DEFAULT_INTRANODEGUILINK_EDGESTROKE_ACTIVE , DEFAULT_INTRANODEGUILINK_EDGESTROKE_ACTIVE);
            e.setEdgeStroke(DEFAULT_INTRANODEGUILINK_EDGESTROKE_ACTIVE , DEFAULT_INTRANODEGUILINK_EDGESTROKE);
            e.setArrowDrawPaint(DEFAULT_INTRANODEGUILINK_EDGEDRAWCOLOR);
            e.setArrowFillPaint(DEFAULT_INTRANODEGUILINK_EDGEDRAWCOLOR);
            e.setEdgeDrawPaint(DEFAULT_INTRANODEGUILINK_EDGEDRAWCOLOR);
            e.setShownSeparated(false);
        }

    }

    private void drawColateralLinks (Set<Link> links , Paint colorIfNotFailedLink)
    {
		for (Link link : links)
		{
			final GUILink glColateral = getAssociatedGUILink(link);
			if (glColateral == null) continue;
			glColateral.setArrowStroke(DEFAULT_REGGUILINK_EDGESTROKE_PICKED_COLATERALACTVELAYER , DEFAULT_REGGUILINK_EDGESTROKE_PICKED_COLATERALNONACTIVELAYER);
			glColateral.setEdgeStroke(DEFAULT_REGGUILINK_EDGESTROKE_PICKED_COLATERALACTVELAYER , DEFAULT_REGGUILINK_EDGESTROKE_PICKED_COLATERALNONACTIVELAYER);
			final Paint color = link.isDown()? DEFAULT_REGGUILINK_EDGECOLOR_FAILED : DEFAULT_REGGUILINK_EDGECOLOR_PICKED;
			glColateral.setArrowDrawPaint(color);
			glColateral.setArrowFillPaint(color);
			glColateral.setEdgeDrawPaint(color);
			glColateral.setShownSeparated(true);
    		glColateral.setHasArrow(true);
		}
    }
    
}
