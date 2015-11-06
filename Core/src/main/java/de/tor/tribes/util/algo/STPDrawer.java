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
package de.tor.tribes.util.algo;

import de.tor.tribes.util.algo.types.Source;
import de.tor.tribes.util.algo.types.OffVillage;
import de.tor.tribes.util.algo.types.Order;
import de.tor.tribes.util.algo.types.Destination;
import de.tor.tribes.util.algo.types.TargetVillage;
import java.awt.Color;
import java.awt.Graphics;
import javax.swing.*;
import java.util.ArrayList;

/**
 *
 * @author Robert Nitsch <dev@robertnitsch.de>
 */
public class STPDrawer extends JPanel {
	private static final long serialVersionUID = 2019615504424500317L;

	protected ArrayList<OffVillage> sources;
	protected ArrayList<TargetVillage> destinations;

	public STPDrawer(ArrayList<OffVillage> offs, ArrayList<TargetVillage> targets) {
		this.sources = offs;
		this.destinations = targets;
	}

    @Override
	public void paintComponent(Graphics g) {
    	super.paintComponent(g);

    	g.setColor(new Color(0,0,0));
    	for(Destination d: this.destinations) {
    		TargetVillage t = (TargetVillage) d;
    		g.drawOval(t.getC().getX(), t.getC().getY(), 5, 5);
    		g.drawString(t.getC().toString(), t.getC().getX()+5, t.getC().getY()+15);
    	}

    	g.setColor(new Color(250,0,0));
    	for(Source s: this.sources) {
    		OffVillage ov = (OffVillage) s;

    		g.setColor(new Color(0,250,0));
    		g.drawOval(ov.getC().getX(), ov.getC().getY(), 5, 5);
    		g.setColor(new Color(0,0,0));
    		g.drawString(ov.getC().toString(), ov.getC().getX()+5, ov.getC().getY()+15);

    		for(Order o: ov.getOrders()) {

    			if(o.getAmount() > 0) {
	    			TargetVillage dest = (TargetVillage)o.getDestination();

	    			g.setColor(new Color(250,0,0));
	    			g.drawLine(ov.getC().getX(), ov.getC().getY(), dest.getC().getX(), dest.getC().getY());

	    			g.setColor(new Color(0,0,0));
	    			g.drawString(Integer.toString(o.getAmount()), Math.round(((ov.getC().getX()+dest.getC().getX())/2)), Math.round((ov.getC().getY()+dest.getC().getY())/2));
    			}
    		}
    	}
    }

	public void setSources(ArrayList<OffVillage> sources) {
		this.sources = sources;
	}

    public ArrayList<TargetVillage> getDestinations() {
		return destinations;
	}

	public void setDestinations(ArrayList<TargetVillage> destinations) {
		this.destinations = destinations;
	}

	public ArrayList<OffVillage> getSources() {
		return sources;
	}
}
