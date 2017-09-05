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
package de.tor.tribes.ui.windows;

import javax.swing.JFrame;

/**
 *
 * @author Torridity
 */
public abstract class DSWorkbenchGesturedFrame extends JFrame {

    public final static int ID_UNKNOWN = -1;
    public final static int ID_CLOSE = 0;
    public final static int ID_TO_BACKGROUND = 1;
    public final static int ID_NEXT_PAGE = 2;
    public final static int ID_PREVIOUS_PAGE = 3;
    public final static int ID_RENAME = 4;
    public final static int ID_EXPORT_BB = 5;
    public final static int ID_EXPORT_PLAIN = 6;

    public boolean handleGesture(String pGesture) {
        if (pGesture == null) {
            return false;
        }

        switch (pGesture) {
            case "DR":
                fireCloseGestureEvent();
                break;
            case "DL":
                fireToBackgroundGestureEvent();
                break;
            case "R":
                fireNextPageGestureEvent();
                break;
            case "L":
                firePreviousPageGestureEvent();
                break;
            case "UR":
                fireExportAsBBGestureEvent();
                break;
            case "UL":
                firePlainExportGestureEvent();
                break;
            case "RDLUR":
                fireRenameGestureEvent();
                break;
            default:
                return false;
        }

        return true;
    }

    /**v ->*/
    public abstract void fireCloseGestureEvent();

    /**v <-*/
    public abstract void fireToBackgroundGestureEvent();

    /**->*/
    public abstract void fireNextPageGestureEvent();

    /**<-*/
    public abstract void firePreviousPageGestureEvent();

    /**-> v <- ^*/
    public abstract void fireRenameGestureEvent();

    /**^ ->*/
    public abstract void fireExportAsBBGestureEvent();

    /**^ <-*/
    public abstract void firePlainExportGestureEvent();
}
