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
import de.tor.tribes.util.algo.types.Destination;
import de.tor.tribes.util.algo.types.Order;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

/*
 * Its constructor takes sources, destinations and costs (an array of Hashtables).
 * Each element of the array is related to a source. E.g. the element at position 0
 * is related to the source at index 0 (sources.get(0)). Therefore every source
 * 'owns' a hashtable, which maps all destinations to the according costs (think
 * of distances) in terms of the transport problem.
 * Example:
 * 	costs[0].get(2) is the cost for transports from source 0 to destination 2.
 *
 * @author Robert Nitsch <dev@robertnitsch.de>
 *
 * @param <S>
 * @param <D>
 */
public class Optex<S extends Source, D extends Destination> {

    protected ArrayList<S> sources;
    protected ArrayList<D> destinations;
    protected Hashtable<Destination, Double> costs[];
    protected boolean executed = false;

    /**
     * @see Optex
     *
     * @param sources The sources.
     * @param destinations The destinations.
     * @param costs The costs.
     */
    public Optex(ArrayList<S> sources, ArrayList<D> destinations, Hashtable<Destination, Double> costs[]) {
        this.sources = sources;
        this.destinations = destinations;
        this.costs = costs;
    }

    /**
     * Executes the algorithm, which consists of an approximation method and
     * an optimization.
     *
     * Only one call per instance!
     *
     * @throws Exception The exception is thrown whenever you try to execute the algorithm a second time.
     */
    public void run() throws Exception {
        if (this.executed) {
            throw new Exception("The algorithm mustn't be executed more than once per instance!");
        }

        //System.out.println("Approximation...");
        this.vam();
        //  System.out.println("Approximation complete...");

        //System.out.println("Optimization...");
        this.optimize();
        // System.out.println("Optimization complete...");

        this.executed = true;
    }

    /**
     * Optimizes the current solution by swapping orders if
     * swapping results into an improvement. The optimization ends
     * when no improvement could be made anymore.
     */
    protected void optimize() {
        boolean improvement = true;
        int count = 1;

        while (improvement) {
            improvement = false;

            //    System.out.println("  Round " + count);
            count += 1;

            if (count == 102) {
                System.err.println("Cancelling optimization after 100 rounds...");
                break;
            }


            for (S s1 : this.sources) {
                // System.out.println(s1);
                // System.out.println("Source");
                if (s1.waresAvailable() > 0) {
                    //     System.out.println("Have Wares");
                    for (S s2 : this.sources) {
                        if (s2.getOrdered() > 0) {
                            //    System.out.println("HAVE ORDER");
                            for (Order o : s2.getOrders()) {
                                int val = o.getAmount();
                                if (val > 0) {
                                    Destination o_d = o.getDestination();

                                    double fact = 1.0;
                                    double c1 = this._getCosts(s1, o_d);
                                    double c2 = this._getCosts(s2, o_d) * fact;
                                    if (c1 < c2) {
                                        int swap_amount = 0;
                                        swap_amount = Math.min(s1.waresAvailable(), o.getAmount());
                                        s2.removeOrder(o_d, swap_amount);
                                        s1.addOrder(o_d, swap_amount);
                                        improvement = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Vogel's approximation method (slightly extended).
     *
     * Sometimes already generates optimal solutions.
     *
     * @throws Exception
     */
    protected void vam() throws Exception {
        if (this.sources.isEmpty() || this.destinations.isEmpty()) {
            throw new Exception("Either sources or destinations are missing!");
        }

        /*
         * Make copies of the sources and destinations lists.
         * The algorithm will work with those copies.
         */
        ArrayList<Source> _sources = new ArrayList<>(this.sources.size());
        _sources.addAll(this.sources);
        ArrayList<Destination> _destinations = new ArrayList<>(this.destinations.size());
        _destinations.addAll(this.destinations);

        while (!_sources.isEmpty() && !_destinations.isEmpty()) {
            /*
             * For each source calculate the difference of the distance to
             * the closest and the second closest destination.
             * In other words, this is the distance that would be added if
             * we choose the source to cope with its second closest destination
             * instead of its closest one (so called 'opportunity costs').
             */
            double opportunityCosts = 0;
            double biggest = -1;
            Source biggest_s = _sources.get(0);
            Destination biggest_s_d = _destinations.get(0);

            for (Source _source : _sources) {
                if (_source.waresAvailable() == 0) {
                    continue;
                }

                /* calculate shortest and second shortest costs */
                double _costs = 999999.0;
                Double lowest_cost = 99999.0;
                Double lowest_cost2 = 999999.0;
                Destination lowest_cost_d = _destinations.get(0);
                for (Destination _dest : _destinations) {
                    _costs = this._getCosts(_source, _dest);
                    if (_costs < lowest_cost) {
                        lowest_cost2 = lowest_cost;
                        lowest_cost = _costs;
                        lowest_cost_d = _dest;
                    } else if (_costs < lowest_cost2) {
                        lowest_cost2 = _costs;
                    }
                }

                /* calculate opportunity costs */
                opportunityCosts = lowest_cost2 - lowest_cost;
                if (opportunityCosts > biggest) {
                    biggest = opportunityCosts;
                    biggest_s = _source;
                    biggest_s_d = lowest_cost_d;
                } else if (opportunityCosts == biggest) {
                    /* If there are more than one source having the biggest
                     * opportunityCosts, prefer the sources having a higher minimal cost.
                     * This results in a lesser distribution of costs among the sources.
                     *
                     * This does not seem to be part of the normal approximation method
                     * by Vogel.
                     */
                    if (this._getCosts(_source, lowest_cost_d) > this._getCosts(biggest_s, biggest_s_d)) {
                        biggest_s = _source;
                        biggest_s_d = lowest_cost_d;
                    }
                }
            }
            int amountOrdered = Math.min(biggest_s.waresAvailable(), biggest_s_d.remainingNeeds());
          /*  if (this._getCosts(biggest_s, biggest_s_d) == 99999.0) {
            //remove on max cost
            amountOrdered = 0;
            _sources.remove(biggest_s);
            }*/

            // double fact = Math.pow(Math.E, (((double)amountOrdered - 5) / -2)) + 1;// (e^-((x-5) / 2) + 1)
            if (amountOrdered > 0) {
                biggest_s.addOrder(biggest_s_d, amountOrdered);
                biggest_s_d.addOrdered(amountOrdered);
            }

            if (biggest_s.waresAvailable() <= 0) {
                _sources.remove(biggest_s);
            }
            if (biggest_s_d.remainingNeeds() == 0) {
                _destinations.remove(biggest_s_d);
            }
        }
    }

    protected static int _arrayIndexOf(double[] array, double d) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == d) {
                return i;
            }
        }

        return -1;
    }

    public double _getCosts(Source s, Destination d) {
        return this.costs[this.sources.indexOf(s)].get(d);
    }

    protected double _getCosts(int source_index, Destination d) {
        return this.costs[source_index].get(d);
    }

}
