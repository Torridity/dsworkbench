/*
 * TestFrame.java
 *
 * Created on 18. Juni 2008, 19:12
 */
package de.tor.tribes.ui;


import javax.swing.UIManager;

/**
 *
 * @author  Charon
 */
public class TestFrame extends javax.swing.JFrame {

    /** Creates new form TestFrame */
    public TestFrame() {
        initComponents();
        /*System.getProperties().put("proxySet", "true");
        System.getProperties().put("proxyHost", "proxy.fzk.de");
        System.getProperties().put("proxyPort", "8000");
        jEditorPane1.setContentType("text/html");
        try {
        URL url = new URL("http://www.heise.de");
        
        jEditorPane1.setPage(url);
        } catch (Exception e) {
        e.printStackTrace();
        }*/
        /* frameControlPanel1.setupPanel(this, true, true);
        frameControlPanel1.setTitle("Test Frame with title and laberzeug und so weiet damit es lang");*/
        setLocation(200,500);
        new Thread(new Runnable() {

            public void run() {
                int heigth = 10;
                boolean inv = false;
                int y = getLocation().y ;
                while (true) {
                    try {
                        setSize(getWidth(), heigth);
                        setLocation(getLocation().x, y - heigth);
                        Thread.sleep(50);

                        heigth += (inv) ? -10 : 10;
                        if (heigth >= 75) {
                            Thread.sleep(5000);
                            inv = true;
                        } else if (heigth <= 0) {
                            dispose();
                            return;
                        }

                    } catch (Exception e) {
                    }
                }
            }
        }).start();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);

        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane2.setAutoscrolls(true);

        jTextPane1.setEditable(false);
        jTextPane1.setText("Blabal asd werwer wersdf asdf naskdljfn asdf asdf nsadjfnk kasdf askjfnsad asdnkfj wwre kjnweknr nkawkenr klawnerwn ern wanernewrjnwe rwer wer ");
        jScrollPane2.setViewportView(jTextPane1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 361, Short.MAX_VALUE)
                .addGap(11, 11, 11))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 276, Short.MAX_VALUE)
                .addGap(12, 12, 12))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) throws Exception {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }
       /* java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new TestFrame().setVisible(true);
            }
        });*/

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextPane jTextPane1;
    // End of variables declaration//GEN-END:variables
}