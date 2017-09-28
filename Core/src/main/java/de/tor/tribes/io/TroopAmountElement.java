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
package de.tor.tribes.io;

import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.troops.TroopsManager;
import de.tor.tribes.util.troops.VillageTroopsHolder;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

/**
 * @author Charon
 * @author extremeCrazyCoder
 */
public class TroopAmountElement {
    private static Logger logger = Logger.getLogger("TroopAmountElement");
    public static final String ALL_TROOPS = "Alle";
    private UnitHolder unit = null;
    private String dynamicAmount = "";
    //buffer to speed up
    private boolean fixed = false;

    public TroopAmountElement(UnitHolder pUnit, int pAmount) {
        this(pUnit, Integer.toString(pAmount));
    }
    
    public TroopAmountElement(UnitHolder pUnit, String pAmount) {
        unit = pUnit;
        setDynamicAmount(pAmount);
    }

    public boolean affectsUnit(UnitHolder pUnit) {
        return !(pUnit == null || unit == null) && unit.getPlainName().equals(pUnit.getPlainName());
    }

    public UnitHolder getUnit() {
        return unit;
    }

    public void setDynamicAmount(String pAmount) {
        //Try if we can parse this
        Object val;
        try {
            val = parse(pAmount, 0);
        } catch(Exception e) {
            logger.debug("Parser Crashed ", e);
            throw new IllegalArgumentException("Parser returned error ", e);
        }
        if(val instanceof String) {
            logger.debug("Can't parse Amount " + (String) val);
            throw new IllegalArgumentException("Unable to parse Math");
        }
        //ok we can parse look if its fixed
        val = parse(pAmount, -1);
        if(val instanceof String) {
            fixed = false;
            dynamicAmount = (String) val;
        } else if(val instanceof Double) {
            fixed = true;
            double dVal = (Double) val;
            dynamicAmount = Integer.toString((int) dVal);
        }
    }

    public int getTroopsAmount(Village pVillage) {
        if (pVillage == null && !isFixed()) {
            logger.error("Tried to read fixed troops from Dynamic amount");
            throw new IllegalArgumentException("Tried to read fixed troops from Dynamic amount");
        }
        int availableAmount = getAvailable(pVillage);
        
        Object val = parse(dynamicAmount, availableAmount);
        if(val instanceof String) {
            logger.error("cant get Amount " + availableAmount + "/" + dynamicAmount);
            throw new RuntimeException("cant get Amount");
        } else if(val instanceof Double) {
            int wanted = (int) (((Double) val).doubleValue());
            if(wanted < -1 || (!isFixed() && wanted < 0)) {
                //limit if illigal value
                wanted = 0;
            }

            if (availableAmount >= wanted) {
                //enough troops available
                return wanted;
            } else {
                //return max. avail count
                return availableAmount;
            }
        }
        throw new RuntimeException("The code must not come to this Point");
    }

    @Override
    public String toString() {
        return dynamicAmount;
    }

    public boolean isFixed() {
        return fixed;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + this.unit.hashCode();
        hash = 53 * hash + this.dynamicAmount.hashCode();
        hash = 53 * hash + (this.fixed ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object pOther) {
        if(pOther instanceof TroopAmountElement) {
            TroopAmountElement otherAmount = (TroopAmountElement) pOther;
            if(!this.unit.equals(otherAmount.getUnit())) return false;
            if(!this.dynamicAmount.equals(otherAmount.toString())) return false;
            return true;
        }
        return false;
    }
    
    public TroopAmountElement loadFromBase64(String base64) {
        try {
            setDynamicAmount(new String(Base64.getUrlDecoder().decode(base64), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            logger.fatal("Wrong encoding", e);
            throw new RuntimeException("Unsupported encoding", e);
        }
        return this;
    }
    
    public String toBase64() {
        try {
            return Base64.getUrlEncoder().encodeToString(toString().getBytes("UTF-8"));
        } catch(UnsupportedEncodingException e) {
            logger.fatal("Wrong encoding", e);
            throw new RuntimeException("Unsupported encoding", e);
        }
    }
    
    /**
     * 
     * @param math the String to parse
     * @param dynValue the Amount to replace dynamic Amounts with
     * @return 
     * returns a double if able to parse
     * returns a String if unable to parse
     */
    Object parse(String math, int pDynValue) {
        if(math.length() == 0) return (double) 0;
        
        //1. Level of parsing
        //finds brackets
        StringBuilder noBrackets = new StringBuilder();
        int currentIndex = 0;
        boolean ableToParse = true;
        while(math.substring(currentIndex).contains("(")) {
            int opening = math.substring(currentIndex).indexOf("(");
            int closing = findMatchingClosingBracket(math, '(', ')');
            if(closing < 0) {
                logger.info("no matching closing bracket found(" + closing + "): "
                        + math.substring(opening + 1));
                throw new IllegalArgumentException("no matching closing bracket found");
            }
            
            //append part in Front of bracket
            noBrackets.append(math.substring(currentIndex, opening));
            Object inner = parse(math.substring(opening + 1, closing), pDynValue);
            if(inner instanceof Double) {
                double innerVal = (Double) inner;
                if(innerVal < 0) {
                    //negative val special char to preserve minus
                    noBrackets.append("_");
                    innerVal*= -1;
                }
                noBrackets.append(innerVal);
            } else if(inner instanceof String) {
                //subelement failed to parse data maybe contains Dynamic amount
                ableToParse = false;
                noBrackets.append("(").append((String) inner).append(")");
            }
            currentIndex = closing + 1;
        }
        if(math.substring(currentIndex).contains(")")) {
            logger.info("Found to many closing brackets " + math.substring(currentIndex));
            throw new IllegalArgumentException("Found to many closing brackets");
        }
        //add last part of String that does not contains any brackets
        noBrackets.append(math.substring(currentIndex));
        
        if(!ableToParse) {
            //something went wrong during removing brackets
            return noBrackets.toString();
        }
        
        return innerParse(noBrackets.toString(),
                pDynValue, 0);
    }
    
    /**
     * 
     * @param noBrackets the String to parse (must not contain brackets)
     * @param dynValue the Amount to replace dynamic Amounts with
     * @param level the current level of parsing (must always be zero; algorithm calls itself with higher values)
     * @return 
     * returns a double if able to parse
     * returns a String if unable to parse
     */
    private final String[] mathChars = {"+", "-", "*", "/", "%", "^"};
    private Object innerParse(String noBrackets, int pDynValue, int level) {
        if(noBrackets.length() == 0) return (double) 0;
        
        //2.Level of parsing
        //remove + - * / and so on...
        Object result = null;
        boolean ableToParse = true;
        boolean alsoOperatorForElements =
                ArrayUtils.contains(elementPostChars, mathChars[level])
                || ArrayUtils.contains(elementPreChars, mathChars[level]);
        int currentIndex = 0;
        while(currentIndex < noBrackets.length()) {
            int next = findNextOperator(noBrackets, level, currentIndex, alsoOperatorForElements);
            String inner;
            if(next == -1) {
                //no more occurrences
                inner = noBrackets.substring(currentIndex);
                next = noBrackets.length(); //exit loop
            } else {
                inner = noBrackets.substring(currentIndex, next);
            }
            
            Object innerResult;
            if(level + 1 < mathChars.length) {
                innerResult = innerParse(inner, pDynValue, level + 1);
            } else {
                innerResult = elementParse(inner, pDynValue);
            }
            
            if(innerResult instanceof String) {
                //unable to parse
                if(ableToParse && result != null) {
                    //everyting bevore was ok result must be a double
                    result = result.toString();
                }
                ableToParse = false;
                if(result == null) {
                    result = innerResult;
                } else {
                    result = ((String) result) + mathChars[level] + ((String) innerResult);
                }
            } else if(innerResult instanceof Double) {
                //able to parse
                if(!ableToParse) {
                    //we had problems before
                    result = ((String) result) + mathChars[level] + innerResult.toString();
                } else if(result == null) {
                    result = innerResult;
                } else {
                    //no problems we have to use arithmetic
                    double dInner = (Double)innerResult;
                    double dAll = (Double)result;
                    switch(mathChars[level]) {
                        case "+":
                            dAll+= dInner;
                            break;
                        case "-":
                            dAll-= dInner;
                            break;
                        case "*":
                            dAll*= dInner;
                            break;
                        case "/":
                            dAll/= dInner;
                            break;
                        case "%":
                            dAll= dAll % dInner;
                            break;
                        case "^":
                            dAll= Math.pow(dAll, dInner);
                            break;
                    }
                    result = dAll;
                }
            }
            currentIndex = next + 1;
        }
        
        return result;
    }

    private final String[] elementPostChars = {"%"};
    private final String[] elementPreChars = {"_"};
    private Object elementParse(String element, int pDynValue) {
        if(element.length() == 0) return (double) 0;
        
        //3.Level of parsing
        //remove all operators that belong to a single Number
        if(element.equalsIgnoreCase(ALL_TROOPS)) {
            //element is all Troops placeholder
            if(pDynValue >= 0) {
                return (double) pDynValue;
            } else {
                //no dynamic Value given
                return element;
            }
        }
        for (String elementPostChar : elementPostChars) {
            if (element.endsWith(elementPostChar)) {
                //element uses postfix
                Object val = elementParse(element.substring(0, element.length() - elementPostChar.length()), pDynValue);
                if (val instanceof String) {
                    //unable to parse
                    return ((String) val) + elementPostChar;
                } else if (val instanceof Double) {
                    double dVal = (Double) val;
                    //able to parse
                    switch (elementPostChar) {
                        case"%":
                            if(pDynValue >= 0) {
                                return (double) pDynValue * dVal / 100;
                            } else {
                                return val.toString() + elementPostChar;
                            }
                    }
                }
            }
        }
        for (String elementPreChar : elementPreChars) {
            if (element.startsWith(elementPreChar)) {
                //element uses postfix
                Object val = elementParse(element.substring(elementPreChar.length()), pDynValue);
                if (val instanceof String) {
                    //unable to parse
                    return elementPreChar + ((String) val);
                } else if (val instanceof Double) {
                    double dVal = (Double) val;
                    //able to parse
                    switch (elementPreChar) {
                        case"_":
                            return (double) dVal* -1;
                    }
                }
            }
        }
        
        try {
            return Double.parseDouble(element);
        } catch(NumberFormatException ignored) {
            return element;
        }
    }
    
    /**
     * Finds the matching closing bracket for the first opening bracket
     * @param input the text to parse
     * @param opening character of a opening bracket
     * @param closing character of a closing bracket
     * @return the index of the closing bracket
     * returns -1 if a closing bracket was found before a opening was found
     * returns -2-x with x the level after reaching the end of Text
     */
    private int findMatchingClosingBracket(String input, char opening, char closing) {
        int level = 0;
        for(int i = 0; i < input.length(); i++) {
            char current = input.charAt(i);
            if(current == opening) {
                level++;
            } else if(current == closing) {
                level--;
                if(level < 0) {
                    //closing bracket was too early (before opening)
                    return -1;
                } else if(level == 0) {
                    //found the matching closing bracket
                    return i;
                }
            }
        }
        //no matching closing found
        return -2 - level;
    }
    
    private int findNextOperator(String input, int parsingLevel, int startIndex, boolean alsoOperatorForElements) {
        int next = input.indexOf(mathChars[parsingLevel], startIndex);
        
        if(alsoOperatorForElements && next != -1) {
            //check if operator belongs to element or not
            while(input.length() > next + 1 && next != -1 &&
                    ArrayUtils.contains(mathChars, "" + input.charAt(next + 1))) {
                //find next valid operator
                next = input.indexOf(mathChars[parsingLevel], next + 1);
            }
            
            if(input.length() <= next + 1) {
                //reached end of String without finding
                return -1;
            }
        }
        return next;
    }

    private int getAvailable(Village pVillage) {
        if(pVillage == null) {
            //don't care
            return 0;
        } else {
            VillageTroopsHolder own = TroopsManager.getSingleton().getTroopsForVillage(pVillage, TroopsManager.TROOP_TYPE.OWN);
            if (own == null) {
                //no info available
                if (logger.isDebugEnabled()) {
                    logger.debug("No troop information found for village '" + pVillage + "'");
                }
                //just use 0 for all Units
                return 0;
            }
            return own.getTroops().getAmountForUnit(unit);
        }
    }
}
