/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.test;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.internal.ObjectContainerBase;
import com.db4o.internal.query.Db4oQueryExecutionListener;
import com.db4o.internal.query.NQOptimizationInfo;
import com.db4o.query.Predicate;
import com.db4o.query.Query;
import de.tor.tribes.types.FightReport;
import de.tor.tribes.types.ReportSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

/*import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;*/
/**
 *
 * @author Torridity
 */
public class DBTest {

    public static void main(String[] args) {
        final ObjectContainer db = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), "database.dbo");
        ((ObjectContainerBase) db).getNativeQueryHandler().addListener(new Db4oQueryExecutionListener() {

            public void notifyQueryExecuted(NQOptimizationInfo info) {
                System.err.println(info);
            }
        });
        try {
            for (int i = 0; i < 3; i++) {
                Hashtable<String, ReportSet> reports = new Hashtable<String, ReportSet>();
                /*ReportSet proto = new ReportSet(null);
                Query q = db.query();
                 */
                final long s = System.currentTimeMillis();
                final List<FightReport> results = new LinkedList<FightReport>();
                db.query(FightReport.class);
                Query q = db.query();
                q.descend("won").constrain(true).equal();
                System.out.println("S1: " + q.execute().size());

                System.out.println((System.currentTimeMillis() - s));
                //ObjectSet repRes = db.query(new Predicate<ReportSet>() {
                ObjectSet repRes = db.query(new Predicate<FightReport>() {

//                public boolean match(ReportSet rep) {
                    public boolean match(FightReport rep) {
                        return rep.isWon();
                            
                        /*        if (!rep.getName().equals("-ph-")) {
                        return false;
                        } else {

                        System.out.println(System.currentTimeMillis() - s);
                        Query q = db.query();
                        q.constrain(FightReport.class);
                        //ObjectSet<FightReport> f = q.execute();

                        //while(f.hasNext()){
                        // System.out.println(f.next());
                        //  f.next();
                        //}
                        q.descend("won").constrain(Boolean.FALSE).equal();

                        //q.descend("won").constrain(Boolean.TRUE);
                        /* ObjectSet reports = db.query(new Predicate<FightReport>() {

                        public boolean match(FightReport frep) {
                        if (frep.getLuck() > 0.0) {
                        results.add(frep);
                        return true;
                        }
                        return false;
                        }
                        });*/
                        /*   ObjectSet res = q.execute();
                        System.out.println("Size: " + res.size());
                        //listResult(res);
                        System.out.println(System.currentTimeMillis() - s);
                        return true;
                        }
                         */                    }
                });
                //ObjectSet results = db.query(FightReport)
            /*List<ReportSet> results = db.query(new Predicate<ReportSet>() {
                public boolean match(ReportSet set) {
                return true;
                }
                });*/

                System.out.println("Size: " + repRes.size());
                System.out.println((System.currentTimeMillis() - s));
                // listResult(results);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    public static void listResult(List result) {
        System.out.println(result.size());
        for (Object o : result) {
            System.out.println(o);
        }
    }

    public static void listResult(ObjectSet result) {
        System.out.println(result.size());
        while (result.hasNext()) {
            System.out.println(result.next());
        }
    }
}
