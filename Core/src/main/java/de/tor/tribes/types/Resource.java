/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

/**
 *
 * @author Torridity
 */
 public class Resource {

        public enum Type {

            WOOD, CLAY, IRON
        }
        private int amount = 0;
        private Type type;

        public Resource(int pAmount, Type pType) {
            setAmount(pAmount);
            setType(pType);
        }

        /**
         * @return the amount
         */
        public int getAmount() {
            return amount;
        }

        /**
         * @param amount the amount to set
         */
        public void setAmount(int amount) {
            this.amount = (amount > 0) ? amount : 0;
        }

        /**
         * @return the type
         */
        public Type getType() {
            return type;
        }

        /**
         * @param type the type to set
         */
        public void setType(Type type) {
            this.type = type;
        }
    }
