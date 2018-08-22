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
package de.tor.tribes.util.algo.types;

/**
 *
 * @author Robert Nitsch <dev@robertnitsch.de>
 */
public class Coordinate implements Comparable<Coordinate> {

    protected int x;
    protected int y;

    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return Integer.toString(this.x) + "|" + Integer.toString(this.y);
    }

    public double distanceTo(Coordinate coordinate) {
        return Math.sqrt(Math.pow(Math.abs(this.x - coordinate.x), 2) + Math.pow(Math.abs(this.y - coordinate.y), 2));
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public int compareTo(Coordinate o) {
        if (x == o.getX() && y == o.getY()) {
            return 0;
        }
        return 1;
    }
}

