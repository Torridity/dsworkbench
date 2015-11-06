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
package de.tor.tribes.util;
import java.awt.*;
/** **************************************************************************
PatchFontMetrics wraps around a FontMetrics to correct some glaring
errors that screw up pages laid out using FontMetrics!

The main problems I've encountered were with heights.  Except for the
maxAdvance (which I dont understand what it's for), the other parameters
have appeared to be reasonable.

I've patched up the heights more or less heuristically;  the big danger
is that on some platform without such a correlation between pixels & points
this may be worse than the original problem!

@author Bruce R. Miller (bruce.miller@nist.gov)
@author Contribution of the National Institute of Standards and Technology,
@author not subject to copyright.
*/

public class PatchFontMetrics extends FontMetrics {
  FontMetrics fm;


  public PatchFontMetrics(FontMetrics fontmetric) {
    super(fontmetric.getFont()); // !?!?!?!
    fm = fontmetric;
    fixupHeights(); }

  public static FontMetrics patch(FontMetrics fontmetric) {
    // return new PatchFontMetrics(fontmetric);
    // Let's try it?
    return fontmetric; }

  int ascent, descent, height, leading;

  void fixupHeights() {
    int size = font.getSize();
    height = fm.getHeight();
    ascent = fm.getAscent();
    descent= fm.getDescent();
    leading= fm.getLeading();

    // Ascent is too large on Irix, Netscape 3 betas & later.
    // Try this: assume pixels~points
    ascent = Math.min(ascent,size);

    // On SunOS, the descent is outrageous.
    // on reasonable systems it appears to be around height/4
    descent = Math.min(descent,ascent/4);

    // On PC's I've seen leading=0, they may be a bit too tight?
    leading = Math.max(leading,1);

    // Now, compute height the right way.
    height = ascent + descent + leading;
  }

  public int getAscent() {  return ascent; }
  public int getDescent() { return descent; }
  public int getHeight() {  return height; }
  public int getLeading() { return leading; }

  /* The rest of these appear more or less reasonable. */

  public int bytesWidth(byte data[], int off, int len) {
    return fm.bytesWidth(data,off,len); }

  public int charsWidth(char data[], int off, int len) {
    return fm.charsWidth(data,off,len); }

  public int charWidth(char ch) {  return fm.charWidth(ch); }
  public int charWidth(int ch) {   return fm.charWidth(ch); }
  public Font getFont() {      return fm.getFont(); }
  public int getMaxAdvance() { return fm.getMaxAdvance(); }
  public int getMaxDescent() { return fm.getMaxDescent(); }
  public int[] getWidths() {   return fm.getWidths(); }

  public int stringWidth(String str) {
    return fm.stringWidth(str); }

}