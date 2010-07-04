/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

/*import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.query.Predicate;
import de.tor.tribes.types.Tribe;
import java.util.List;*/

/**
 *
 * @author Torridity
 */
public class DatabaseManager {

    

    public static void main(String[] args) {
       /* ObjectContainer db = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), "database.dbo");
        try {
            /* for (int i = 0; i < 1000; i++) {
            Tribe t = new Tribe();
            t.setId(i);
            t.setName("Tribe " + i);
            t.setRank(i);
            t.setVillages((short) (i * 2));
            t.setAllyID(-1);
            db.store(t);
            }*/
            //  ObjectSet result = db.queryByExample(new Tribe());
 /*           List<Tribe> tribes = db.query(new Predicate<Tribe>() {

                public boolean match(Tribe tribe) {
                    return tribe.getId() == 10;
                }
            });

            Tribe t = tribes.get(0);
            System.out.println("Tribe: " + t);
            t.setName("Tribe c");
            db.store(t);
            db.commit();
            ////////////////////
            tribes = db.query(new Predicate<Tribe>() {

                public boolean match(Tribe tribe) {
                    return tribe.getId() == 10;
                }
            });
            
            t = tribes.get(0);
            System.out.println("After Commit: " + t);
            t.setName("Tribe cc");
            db.store(t);
            ////////////////////
            tribes = db.query(new Predicate<Tribe>() {

                public boolean match(Tribe tribe) {
                    return tribe.getId() == 10;
                }
            });
            t = tribes.get(0);
            System.out.println("After Second Commit: " + t);
            /////////////
            System.out.println("Rollback");
            db.rollback();
            db.ext().refresh(t, Integer.MAX_VALUE);
            tribes = db.query(new Predicate<Tribe>() {

                public boolean match(Tribe tribe) {
                    return tribe.getId() == 10;
                }
            });
            System.out.println("After Rollback: " + t);
            db.rollback();
            db.ext().refresh(t, Integer.MAX_VALUE);
            tribes = db.query(new Predicate<Tribe>() {

                public boolean match(Tribe tribe) {
                    return tribe.getId() == 10;
                }
            });
            System.out.println("After Second Rollback: " + t);


        } finally {
            db.close();
        }*/
    }
}
