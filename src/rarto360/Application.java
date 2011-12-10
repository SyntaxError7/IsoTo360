/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rarto360;

import org.apache.log4j.Logger;

import javax.swing.*;
import java.util.Locale;

public class Application {
    private static final Logger logger = Logger.getLogger(Application.class);

    private static Application ourInstance = new Application();
    private Locale locale = new Locale("en");

    public static Application getInstance() {
        return ourInstance;
    }

    private Application() {
    }

    public static Locale getLocale() {
        return getInstance().locale;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        try {
            if (args.length == 1 && args[0].equals("systemtheme")) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                if (System.getProperty("os.name").startsWith("Mac")) {
                    // This is a machine running osx, handle menu bar
                    System.setProperty("apple.laf.useScreenMenuBar", "true");
                }
            } else {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
            }

            RarTo360Form form = new RarTo360Form();
            form.setVisible(true);
        } catch (Exception ex) {
            logger.fatal("Exception in application startup!", ex);
        }
    }

}
