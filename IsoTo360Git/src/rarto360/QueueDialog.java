/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rarto360;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author Tical6110
 */
public class QueueDialog extends JDialog{

    public QueueDialog(JFrame parent) {
    super(parent, "Processing Queue", true);

    Box b = Box.createVerticalBox();
    b.add(Box.createGlue());

    List qListing = RarTo360Form.getQueueListing();
    int qSize = qListing.size();

    Iterator<String> iterator = qListing.iterator();
      while ( iterator.hasNext() ){
	      b.add(new JLabel(" " + iterator.next()));
	  }


    b.add(Box.createGlue());
    getContentPane().add(b, "Center");

    JPanel p2 = new JPanel();
    JButton ok = new JButton("Ok");
    p2.add(ok);
    getContentPane().add(p2, "South");

    ok.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        setVisible(false);
      }
    });

    int length = 100 +(qSize * 20);
    setSize(250, length);
  }

}
