/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rarto360;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Tical6110
 */
public class QueueDialog extends JDialog {

    public QueueDialog(JFrame parent) {
        super(parent, "Processing Queue", true);

        Box box = Box.createVerticalBox();
        box.add(Box.createGlue());

//        List qListing = RarTo360Form.getQueueListing();
//        int qSize = qListing.size();

//        for (Object aQListing : qListing) {
//            box.add(new JLabel(" " + aQListing));
//        }


        box.add(Box.createGlue());
        getContentPane().add(box, "Center");

        JPanel p2 = new JPanel();
        JButton ok = new JButton("Ok");
        p2.add(ok);
        getContentPane().add(p2, "South");

        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                setVisible(false);
            }
        });

//        int length = 100 + (qSize * 20);
//        setSize(250, length);
    }

}
