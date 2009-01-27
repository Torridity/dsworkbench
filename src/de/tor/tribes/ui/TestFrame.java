/*
 * TestFrame.java
 *
 * Created on 18. Juni 2008, 19:12
 */
package de.tor.tribes.ui;

import com.visutools.nav.bislider.ContentPainterEvent;
import com.visutools.nav.bislider.ContentPainterListener;
import de.tor.tribes.util.Constants;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;

/**
 *
 * @author  Charon
 */
public class TestFrame extends javax.swing.JFrame {

    public enum Command {

        MDSLIST,
        TEST
    };
    private static float LineX = 0f;

    /** Creates new form TestFrame */
    public TestFrame() {
        initComponents();
        try {
            biSlider1.setMinimumValue(0);
            biSlider1.setSliderBackground(Constants.DS_BACK);
            biSlider1.setMaximumColor(Constants.DS_BACK_LIGHT);
            biSlider1.setMinimumColor(Constants.DS_BACK_LIGHT);
            biSlider1.setMaximumValue(24);
            biSlider1.setSegmentSize(1);
            biSlider1.setUnit("h");
            biSlider1.setDecimalFormater(new DecimalFormat("##"));
            /* biSlider1.addContentPainterListener(new ContentPainterListener() {

            public void paint(ContentPainterEvent ContentPainterEvent_Arg) {
            Graphics2D Graphics2 = (Graphics2D) ContentPainterEvent_Arg.getGraphics();
            Rectangle Rect1 = ContentPainterEvent_Arg.getRectangle();
            Rectangle Rect2 = ContentPainterEvent_Arg.getBoundingRectangle();
            if (ContentPainterEvent_Arg.getColor() != null) {
            Graphics2.setColor(ContentPainterEvent_Arg.getColor());
            Graphics2.setPaint(new GradientPaint(Rect2.x, Rect2.y, ContentPainterEvent_Arg.getColor().brighter(),
            Rect2.x + Rect2.width, Rect2.y + Rect2.height, ContentPainterEvent_Arg.getColor().darker()));
            Graphics2.fillRect(Rect1.x, Rect1.y, Rect1.width, Rect1.height);
            }
            }
            });*/

            /*biSlider1.addContentPainterListener(new ContentPainterListener() {

                public void paint(ContentPainterEvent ContentPainterEvent_Arg) {
                    Graphics2D Graphics2 = (Graphics2D) ContentPainterEvent_Arg.getGraphics();
                    Rectangle Rect1 = biSlider1.getBounds();
                    Rectangle Rect2 = ContentPainterEvent_Arg.getBoundingRectangle();

                    double w = (double) biSlider1.getWidth();
                    double perc = 100.0 * ((double) Rect2.x + (double) Rect2.width) / w;
                    Paint old = Graphics2.getPaint();
                    Graphics2.setColor(Constants.DS_BACK_LIGHT);
                    Graphics2.fillRect(0, 0, Rect1.width, Rect1.height);

                    double min = ContentPainterEvent_Arg.getMinimum();
                    double max = ContentPainterEvent_Arg.getMaximum();
                    System.out.println(min + "," + max);

                    System.out.println((9*Rect2.width));
                    Graphics2.setPaint(new GradientPaint(0, 0, Color.BLACK, (int) (9 * Rect2.width), Rect1.height, Color.WHITE));
                    Graphics2.fillRect(0, 0, (int) (9 * Rect2.width), Rect1.height);
                    Graphics2.setPaint(old);
                   /* Composite oldC = Graphics2.getComposite();
                    Graphics2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OUT, 0.5f));

                    if (perc < (900.0 / 24.0)) {
                        Graphics2.setColor(Color.RED);
                        Graphics2.fillRect(Rect2.x, Rect2.y, Rect2.width, Rect2.height);
                    } else {
                        Graphics2.setColor(Color.GREEN);
                        Graphics2.fillRect(Rect2.x, Rect2.y, Rect2.width, Rect2.height);
                    }
                    Graphics2.setComposite(oldC);*/
                /* double Rand = Math.abs(Math.cos(Math.PI * (Rect2.x + Rect2.width / 2) / biSlider1.getWidth()));
                // Rand = (double)(Rect2.x+Rect2.width/2) / biSlider1.getWidth();
                float X = ((float) Rect2.x - biSlider1.getWidth() / 2) / biSlider1.getWidth() * 6;
                // Rand = 1 - Math.exp((-1 * X * X) / 2);
                if (ContentPainterEvent_Arg.getColor() != null) {
                Graphics2.setColor(biSlider1.getSliderBackground().darker());
                Graphics2.fillRect(Rect2.x, Rect2.y, Rect2.width, (int) ((Rand * Rect2.height)));
                Graphics2.setColor(ContentPainterEvent_Arg.getColor());
                Graphics2.fillRect(Rect2.x, Rect2.y + (int) ((Rand * Rect2.height)), Rect2.width - 1, (int) (((1 - Rand) * Rect2.height)));
                }*/
                /*Graphics2.setColor(Color.BLACK);
                Graphics2.drawRect(Rect2.x, Rect2.y + (int) ((Rand * Rect2.height)), Rect2.width - 1, (int) (((1 - Rand) * Rect2.height)));
                 */             /*   }
            });*/

        } catch (Exception e) {
        }
    //bs.set
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
    //  setLocation(200, 500);

    /*new Thread(new Runnable() {

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
    }).start();*/
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        biSlider1 = new com.visutools.nav.bislider.BiSlider();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        getContentPane().add(biSlider1, java.awt.BorderLayout.PAGE_END);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    /*public static void main(String args[]) throws Exception {

    try {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
    }
    java.awt.EventQueue.invokeLater(new Runnable() {

    public void run() {
    new TestFrame().setVisible(true);
    }
    });

    }*/
    public static void main(String args[]) {
        /*try {
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
        } catch (Exception ex) {
        System.err.println("Error loading L&F: " + ex);
        }*/

        TestFrame f = new TestFrame();
        f.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        f.setSize(300, 100);
        f.setVisible(true);

    //System.out.println(System.getProperty("user.dir"));

    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.visutools.nav.bislider.BiSlider biSlider1;
    // End of variables declaration//GEN-END:variables
}
