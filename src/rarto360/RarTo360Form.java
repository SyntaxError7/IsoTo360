/*
 * I should comment my code but I dont - so if anyone is trying to read this 
 * and frustrated by the lack of documentation I apologize.
 * 
 * Used base code from RarTo360 so the form is still named RarTo360Form
 * 
 * 
 * Coded by Illusions0fGrander 
 * RarTo360
 * Started September 11, 2010
 * 
 * IsoTo360
 * Started October 31, 2011
 * 
 */

/*
 * RarTo360Form.java
 *
 * Created on Sep 11, 2010, 2:46:42 AM
 */


package rarto360;

import java.net.SocketException;
import org.apache.commons.io.FileUtils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JRadioButton;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.commons.net.ftp.FTPClient;

public class RarTo360Form extends javax.swing.JFrame implements ActionListener {

    static List<String> jobQueueDescription = new ArrayList();
    List<String[]> jobQueue = new ArrayList<String[]>();
    Properties config = new Properties();
    String workingDir, batDir = "";
    String IsoNameNoExt = "";
    String rarNameNoExt, rarName = "";
    String startLoc = "";
    String extractLoc = "";
    JFileChooser chooser;
    JFileChooser fileChooser;
    JFileChooser localChooser;
    int IsoRarSelection = 0;
    JRadioButton button1 = new JRadioButton("Rar");
    JRadioButton button2 = new JRadioButton("ISO");
    String gameDir, gameName, ipAddy, rarIsoDir, isoName, appDir = "";
    File curDir;
    String[] cmdString = null;
    String exisoCmd = "";
    String progressLine = "";
    Boolean isoSelected = false;
    String isoPathAndName = "";
    long totalSize = 0;
    String extractionPath = "";

    /** Creates new form RarTo360Form */
    public RarTo360Form() {

        initComponents();
        selectIsoBtn.setVisible(true);

        setButtons();
        setConfig();
        repaint();

    }

    public static List getQueueListing() {
        return jobQueueDescription;
    }

    private void setButtons() {

        ButtonGroup group = new ButtonGroup();

        group.add(button1);
        group.add(button2);

        button1.addActionListener(this);
        button2.addActionListener(this);

    }

    public void actionPerformed(ActionEvent evt) {

        if (evt.getSource() == button1) {
            selectIsoBtn.setVisible(true);
            selectIsoBtn.setText("Select Rar File");
            statusLbl.setText("Status: Use browser to select the archive to extract and transfer.");
            IsoRarSelection = 1;
        }

        if (evt.getSource() == button2) {
            selectIsoBtn.setVisible(true);
            selectIsoBtn.setText(("Select ISO File"));
            IsoRarSelection = 2;
            selectISO();
            //statusLbl.setText("Status: Use browser to select the ISO file to transfer.");
        }

    }

    private void setConfig() {

        try {
            config.load(new FileInputStream("settings.conf"));
        } catch (IOException ioex) {
            System.err.println("Error loading config file: settings.conf");
            System.exit(0);
        }

        gameDirTxt.setText(config.getProperty("gamesDir"));
        ipAddyTxt.setText(config.getProperty("ip"));
        startLoc = config.getProperty("startLoc");
        extractLoc = config.getProperty("extractLoc");


    }

    private void gameNameTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gameNameTxtActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gameNameTxtActionPerformed

    private void selectRAR() {
        JButton go;
        String choosertitle = "";


        chooser = new JFileChooser(startLoc);
        //chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle(choosertitle);


        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            RarTo360Form.this.statusLbl.setText(chooser.getSelectedFile().toString());
            if (this.gameNameTxt.getText().equals("")) {
                statusLbl.setText("Status: RAR Selected, Enter a game name.");
            } else {
                statusLbl.setText("Status: RAR Selected, begin transfer or add to queue.");
            }
        }

    }

    private void selectISO() {

        String choosertitle = "";



        FileFilter filter = new FileNameExtensionFilter("ISO file", "iso");
        chooser = new JFileChooser(startLoc);
        chooser.addChoosableFileFilter(filter);
        chooser.setFileFilter(filter);
        chooser.setDialogTitle(choosertitle);

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            statusLbl.setText(chooser.getSelectedFile().toString());
            isoSelected = true;
            if (this.gameNameTxt.getText().equals("")) {
                statusLbl.setText("Status: ISO Selected, enter a game name.");
            } else {
                statusLbl.setText("Status: ISO Selected, ready to extract.");
            }
        }



    }
    private void gameDirTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gameDirTxtActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gameDirTxtActionPerformed

    private void ipAddyTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ipAddyTxtActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ipAddyTxtActionPerformed

    private void startXferBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startXferBtnActionPerformed

        if (this.gameNameTxt.getText().equals("")) {
            statusLbl.setText("Please enter a game name.");
            this.gameNameTxt.requestFocusInWindow();
        } else {

            if (isoSelected == false) {
                statusLbl.setText("Status:Please choose an ISO to extract or transfer");
            } else if (localExtractChkBx.getState() == true) {
                IsoToFTPLocal(false, localChooser.getSelectedFile());
            } else if (defaultlExtractChkBx.getState() == true) {
                IsoToFTPLocal(false, localChooser.getCurrentDirectory());
            } else {
                try {
                    IsoToFTP(false);
                } catch (SocketException ex) {
                    Logger.getLogger(RarTo360Form.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(RarTo360Form.class.getName()).log(Level.SEVERE, null, ex);
                }
            }



//            } else if (IsoRarSelection == 1) {
//                if (isoSelected == true) {
//                    RunRarBatchLocal(false, localChooser.getSelectedFile());
//                } else {
//                    RunRarBatch(false);
//                }
//            } else if (IsoRarSelection == 2) {
//                if (localExtractChkBx.getState() == true) {
//                    IsoToFTPLocal(false, localChooser.getSelectedFile());
//                } else {
//                    IsoToFTP(false);
//                }
//            }
        }

    }

    private void IsoToFTPLocal(boolean batchBool, File extDir) {
        //localExtractChkBxItemStateChanged
        isoName = chooser.getName(chooser.getSelectedFile());
        IsoNameNoExt = isoName.substring(0, (isoName.length() - 4));
        curDir = chooser.getCurrentDirectory();

        setVariables();
        rarIsoDir = curDir.toString();
        String dirToExt = extDir.toString();
        //dirToExt = "\"" + dirToExt + "\"";

        checkForSpaces();
        try {
            IsoToDisc(dirToExt, gameName, rarIsoDir, isoName, workingDir);
        } catch (IOException ex) {
            Logger.getLogger(RarTo360Form.class.getName()).log(Level.SEVERE, null, ex);
        }


        //        
        //
        //        if (batchBool == false)
        //        {
        //        Process pr = null;
        //
        //        java.lang.Runtime rt = java.lang.Runtime.getRuntime();
        //        
        //        try {
        //            
        //            Process p = Runtime.getRuntime().exec("cmd mkdir w:\\Extracted\\abc");
        //            
        //            pr = rt.exec("cmd mkdir w:\\Extracted\\abc");
        //            pr = rt.exec("mkdir " + dirToExt + "\\" + gameName);
        //            pr = rt.exec(workingDir + "\\exiso.exe -d " + dirToExt + "\"" + gameName + " -s " + rarIsoDir + "\"" + isoName);
        //            pr = rt.exec("cd ..");
        //            
        //        }
        //        catch (IOException ex) {
        //           Logger.getLogger(RarTo360Form.class.getName()).log(Level.SEVERE, null, ex);
        //       }

        //        if (batchBool == false)
        //        {
        //        Process pr = null;
        //
        //        java.lang.Runtime rt = java.lang.Runtime.getRuntime();
        //        String[] commands = {"cmd", "/c", "start/wait", workingDir, "batchFiles\\IsoToDisc.cmd", gameDir, gameName, ipAddy, rarIsoDir, isoName, IsoNameNoExt, batDir, dirToExt};
        //        try {
        //            pr = rt.exec(commands);
        //
        //       } catch (IOException ex) {
        //            Logger.getLogger(RarTo360Form.class.getName()).log(Level.SEVERE, null, ex);
        //        }
        //        try {
        //            pr.waitFor();
        //            statusLbl.setText("Status: Processing " + gameName + " finished.");
        //        } catch (InterruptedException ex) {
        //            Logger.getLogger(RarTo360Form.class.getName()).log(Level.SEVERE, null, ex);
        //        }
        //        }
        // else
        //        {
        //         String[] commands = {"cmd", "/c", "start/wait", workingDir, "batchFiles\\IsoToDisc.cmd", gameDir, gameName, ipAddy, rarIsoDir, isoName, IsoNameNoExt, batDir, dirToExt};
        //         jobQueue.add(commands);
        //         jobQueueDescription.add("ISO: " + gameName);
        //         this.gameNameTxt.setText("");
        //         statusLbl.setText(gameName + " added to queue (ISO).");
        // }
        //
//            Logger.getLogger(RarTo360Form.class.getName()).log(Level.SEVERE, null, ex);
//        }


    }

    private void RunRarBatchLocal(boolean batchBool, File extDir) {

        // localExtractChkBxItemStateChanged
        rarName = chooser.getName(chooser.getSelectedFile());
        rarNameNoExt = rarName.substring(0, (rarName.length() - 4));
        curDir = localChooser.getCurrentDirectory();
        setVariables();
        rarIsoDir = curDir.toString();

        String dirToExt = extDir.toString();
        dirToExt = "\"" + dirToExt + "\"";



        checkForSpaces();
        workingDir = "\"" + workingDir + "\"";

        if (batchBool == false) {
            Process pr = null;

            java.lang.Runtime rt = java.lang.Runtime.getRuntime();
            String[] commands = {"cmd", "/c", "start/wait", workingDir, "batchFiles\\RarToDisc.cmd", gameDir, gameName, ipAddy, rarIsoDir, batDir, rarNameNoExt, dirToExt};
            try {
                pr = rt.exec(commands);

            } catch (IOException ex) {
                Logger.getLogger(RarTo360Form.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                pr.waitFor();
                statusLbl.setText("Status: Processing " + gameName + " finished.");
            } catch (InterruptedException ex) {
                Logger.getLogger(RarTo360Form.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            String[] commands = {"cmd", "/c", "start/wait", workingDir, "batchFiles\\RarToDisc.cmd", gameDir, gameName, ipAddy, rarIsoDir, batDir, rarNameNoExt, dirToExt};
            jobQueue.add(commands);
            jobQueueDescription.add("ISO: " + gameName);
            this.gameNameTxt.setText("");
            statusLbl.setText(gameName + " added to queue (ISO).");
        }



    }

    private void RunRarBatch(boolean batchBool) {

        rarName = chooser.getName(chooser.getSelectedFile());
        rarNameNoExt = rarName.substring(0, (rarName.length() - 4));
        curDir = chooser.getCurrentDirectory();
        setVariables();
        rarIsoDir = curDir.toString();

        checkForSpaces();
        workingDir = "\"" + workingDir + "\"";

        if (batchBool == false) {

            Process pr = null;
            // statusLbl.setText("Status: Processing started on " + this.gameNameTxt.getText() + ", see command window for progress.");
            java.lang.Runtime rt = java.lang.Runtime.getRuntime();
            String[] commands = {"cmd.exe", "/c", "start/wait", workingDir, "batchFiles\\RarTo360.cmd", gameDir, gameName, ipAddy, rarIsoDir, batDir, rarNameNoExt};
            try {
                pr = rt.exec(commands);

            } catch (IOException ex) {
                Logger.getLogger(RarTo360Form.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                pr.waitFor();
                statusLbl.setText("Status: Processing " + gameName + " finished.");
            } catch (InterruptedException ex) {
                Logger.getLogger(RarTo360Form.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else // add to batch list...
        {
            String[] commands = {"cmd.exe", "/c", "start/wait", workingDir, "batchFiles\\RarTo360.cmd", gameDir, gameName, ipAddy, rarIsoDir, batDir, rarNameNoExt};
            jobQueue.add(commands);
            jobQueueDescription.add("RAR: " + gameName);
            this.gameNameTxt.setText("");
            statusLbl.setText(gameName + " added to queue (Rar).");
        }

    }//GEN-LAST:event_startXferBtnActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        try {
            Runtime.getRuntime().exec("cmd.exe /c start http://forums.xbox-scene.com/index.php?showtopic=737562");
        } catch (IOException ex) {
            Logger.getLogger(RarTo360Form.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void addToQueueBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addToQueueBtnActionPerformed

        if (!this.gameNameTxt.getText().isEmpty()) {

            if (IsoRarSelection == 1) {
                if (localExtractChkBx.getState() == true) {
                    RunRarBatchLocal(true, localChooser.getSelectedFile());
                } else {
                    RunRarBatch(true);
                }
            } else if (IsoRarSelection == 2) {
                if (localExtractChkBx.getState() == true) {
                    IsoToFTPLocal(true, localChooser.getSelectedFile());
                } else {
                    try {
                        IsoToFTP(true);
                    } catch (SocketException ex) {
                        Logger.getLogger(RarTo360Form.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(RarTo360Form.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } else {
                statusLbl.setText("Please select a file type.");
            }
        } else {
            statusLbl.setText("Please enter a game name");
        }
        ;
    }//GEN-LAST:event_addToQueueBtnActionPerformed

    private void startBatchQueueBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startBatchQueueBtnActionPerformed


        Iterator<String[]> iterator = jobQueue.iterator();
        while (iterator.hasNext()) {
            processJob(iterator.next());
        }
        statusLbl.setText("Status: Batch job complete.");
        jobQueue.clear();
        jobQueueDescription.clear();




    }//GEN-LAST:event_startBatchQueueBtnActionPerformed

    private void clearQBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearQBtnActionPerformed

        // TODO add your handling code here:
        jobQueue.clear();
        jobQueueDescription.clear();
        curDir = new File(System.getProperty("user.dir") + "/logs");
        boolean completed = deleteAll(curDir);
        statusLbl.setText("Status: Logs and queue cleared.");
    }//GEN-LAST:event_clearQBtnActionPerformed

    private void qViewBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_qViewBtnActionPerformed

        JDialog f = new QueueDialog(new JFrame());
        f.setVisible(true);

    }//GEN-LAST:event_qViewBtnActionPerformed

    private void localExtractChkBxPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_localExtractChkBxPropertyChange
        // TODO add your handling code here:
    }//GEN-LAST:event_localExtractChkBxPropertyChange

    private void localExtractChkBxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_localExtractChkBxItemStateChanged

        if (localExtractChkBx.getState() == true) {
            gameDirTxt.setEnabled(false);
            ipAddyTxt.setEnabled(false);
            startXferBtn.setText("Extract To Directory");
            defaultlExtractChkBx.setState(false);
            setExtractDirectory();
        } else {
            gameDirTxt.setEnabled(true);
            ipAddyTxt.setEnabled(true);
            startXferBtn.setText("Start FTP Transfer");
            statusLbl.setText("Status: Select an ISO, enter a game name and click Start FTP Transfer");

        }
    }//GEN-LAST:event_localExtractChkBxItemStateChanged

private void defaultlExtractChkBxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_defaultlExtractChkBxItemStateChanged
    if (defaultlExtractChkBx.getState() == true) {

        localChooser = new JFileChooser(extractLoc);
        localChooser.setCurrentDirectory(new java.io.File(extractLoc));
        gameDirTxt.setEnabled(false);
        ipAddyTxt.setEnabled(false);
        startXferBtn.setText("Extract To Directory");
        statusLbl.setText("Game will be extracted to: " + localChooser.getCurrentDirectory().toString() + "\\" + "\"Game Name\"");
        localExtractChkBx.setState(false);

    } else {
        gameDirTxt.setEnabled(true);
        ipAddyTxt.setEnabled(true);
        startXferBtn.setText("Start FTP Transfer");
        statusLbl.setText("Status: Select an ISO, enter a game name and click Start FTP Transfer");

    }
}//GEN-LAST:event_defaultlExtractChkBxItemStateChanged

private void defaultlExtractChkBxPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_defaultlExtractChkBxPropertyChange
// TODO add your handling code here:
}//GEN-LAST:event_defaultlExtractChkBxPropertyChange

private void selectIsoBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectIsoBtnActionPerformed

    selectISO();
    // TODO add your handling code here:
}//GEN-LAST:event_selectIsoBtnActionPerformed

    public void setExtractDirectory() {

        localChooser = new JFileChooser(startLoc);
        localChooser.setCurrentDirectory(new java.io.File(extractLoc));
        localChooser.setDialogTitle("Choose Extraction Location");
        localChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        localChooser.setAcceptAllFileFilterUsed(false);

        if (localChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            System.out.println("getCurrentDirectory(): " + localChooser.getCurrentDirectory());
            System.out.println("getSelectedFile() : " + localChooser.getSelectedFile());
            statusLbl.setText("Game will be extracted to: " + localChooser.getSelectedFile().toString() + "\\" + "\"Game Name\"");

        } else {
            System.out.println("No Selection ");

        }


    }

    public void setChooserDirectory() {
        localChooser = new JFileChooser(startLoc);
        localChooser.setCurrentDirectory(new java.io.File(startLoc));
        localChooser.setDialogTitle("Choose Extraction Location");
        localChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        localChooser.setAcceptAllFileFilterUsed(false);

        if (localChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            System.out.println("getCurrentDirectory(): " + localChooser.getCurrentDirectory());
            System.out.println("getSelectedFile() : " + localChooser.getSelectedFile());
            statusLbl.setText("Game will be extracted to: " + localChooser.getSelectedFile().toString() + "\\" + "\"Game Name\"");

        } else {
            System.out.println("No Selection ");

        }


    }

    public static boolean deleteAll(File dir) {
        if (!dir.exists()) {
            return true;
        }
        boolean res = true;
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; i++) {
                res &= deleteAll(files[i]);
            }
        } else {
            res = dir.delete();
        }
        return res;
    }

    private void processJob(String[] command) {


        Process pr = null;
        // statusLbl.setText("Status: Processing started on " + this.gameNameTxt.getText() + ", see command window for progress.");
        java.lang.Runtime rt = java.lang.Runtime.getRuntime();
        try {
            pr = rt.exec(command);
        } catch (IOException ex) {
            Logger.getLogger(RarTo360Form.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            pr.waitFor();
            statusLbl.setText("Status: Processing " + gameName + " finished.");
        } catch (InterruptedException ex) {
            Logger.getLogger(RarTo360Form.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void setVariables() {

        workingDir = System.getProperty("user.dir");
        batDir = System.getProperty("user.dir");
        gameDir = this.gameDirTxt.getText();
        gameName = this.gameNameTxt.getText();
        ipAddy = this.ipAddyTxt.getText();
    }

    private void checkForSpaces() {
//        if (rarIsoDir.contains(" ")) {
//            rarIsoDir = "\"" + rarIsoDir + "\"";
//        }
//
//        if (gameName.contains(" ")) {
//            gameName = "\"" + gameName + "\"";
//        }
//        if (batDir.contains(" ")) {
//            batDir = "\"" + batDir + "\"";
//        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new RarTo360Form().setVisible(true);
            }
        });
    }

    // CODED
    public Process doExisoLocal() throws InterruptedException {
        try {

            String[] command = new String[3];
            command[0] = "cmd";
            command[1] = "/C";
            command[2] = getExisoCmd();

            String commandString = getExisoCmd();
            
            disableButtons();
            Process p = Runtime.getRuntime().exec("cmd.exe /c start " + commandString);

            String line;

            BufferedReader input =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = input.readLine()) != null) {
                System.out.println(line);
            }
            input.close();
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            

            //BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            //BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

            String s = null;
            statusLbl.setText("Processing ISO, please wait...");


          

            String destFolder = getExtractionPath();
            long isoSize = getIsoSize(isoPathAndName);



//            int i = 0;


//            while ((s = stdInput.readLine()) != null) {
//
//                if (i > 5) {
//
//                    i = 0;
//                    long destSize = getFolderSize(destFolder);
//                    float percentage = (((float) destSize / (float) isoSize) * 100);
//                    double newNum = Math.round(percentage * 100.0) / 100.0;
//
//                    pctComplete.setText("Overall Progress: " + newNum + "% (Approx: " + (destSize / 1000000) + " MB out of " + (isoSize / 1000000) + " MB)");
//                    outputLbl.setText(s);
//                    statusLbl.repaint();
//                    outputLbl.repaint();
//
//                } else {
//                    i++;
//                }
//
//
//
//
//
//            }
//            System.out.println("Here is the standard error of the command (if any):\n");
//            while ((s = stdError.readLine()) != null) {
//                System.out.println(s);
//            }

            return p;

        } catch (IOException ex) {
            Logger.getLogger(RarTo360Form.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

    }

    public Process doExisoFTP() {
        try {

            String[] command = new String[4];
            command[0] = "cmd";
            command[1] = "/C";
            command[2] = getExisoCmd();

            String commandString = getExisoCmd();
            //Process p = Runtime.getRuntime().exec(commandString);
            disableButtons();
            Process p = Runtime.getRuntime().exec("cmd.exe /c start " + commandString);

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

            String s = null;
            statusLbl.setText("Processing ISO, please wait...");



            //String destFolder = getExtractionPath();
            //long isoSize = getIsoSize(isoPathAndName);



            statusLbl.repaint(100);
            //txtOutputLabel.repaint(100);

            int i = 0;

            while ((s = stdInput.readLine()) != null) {

                if (i > 10) {
                    i = 0;
                    //outputLbl.setText(s);



                } else {
                    i++;
                }
            }
            System.out.println("Here is the standard error of the command (if any):\n");
            while ((s = stdError.readLine()) != null) {
                System.out.println(s);
            }


            return p;

        } catch (IOException ex) {
            Logger.getLogger(RarTo360Form.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

    }

    public void enableButtons() {


        selectIsoBtn.setEnabled(true);
        gameNameTxt.setEditable(true);
        gameDirTxt.setEditable(true);
        ipAddyTxt.setEditable(true);
        //qViewBtn.setEnabled(true);
        defaultlExtractChkBx.setEnabled(true);
        localExtractChkBx.setEnabled(true);
        startXferBtn.setEnabled(true);
        //addToQueueBtn.setEnabled(true);
        //startBatchQueueBtn.setEnabled(true);
        //clearQBtn.setEnabled(true);



    }

    public void disableButtons() {

        selectIsoBtn.setEnabled(false);
        gameNameTxt.setEditable(false);
        gameDirTxt.setEditable(false);
        ipAddyTxt.setEditable(false);
        qViewBtn.setEnabled(false);
        defaultlExtractChkBx.setEnabled(false);
        localExtractChkBx.setEnabled(false);
        startXferBtn.setEnabled(false);
        addToQueueBtn.setEnabled(false);
        startBatchQueueBtn.setEnabled(false);
        clearQBtn.setEnabled(false);
        repaint();


    }

    private void IsoToFTP(boolean batchBool) throws SocketException, IOException {

        ipAddy = ipAddyTxt.getText();
        String extractDir = gameDirTxt.getText();
        gameName = gameNameTxt.getText();
        isoName = chooser.getName(chooser.getSelectedFile());
        IsoNameNoExt = isoName.substring(0, (isoName.length() - 4));
        curDir = chooser.getCurrentDirectory();


        String extractTo = extractDir + gameName;
        isoPathAndName = curDir + "\\" + isoName;

        if (extractTo.contains(" ")) {
            extractTo = "\"" + extractTo + "\"";
        }
        if (isoPathAndName.contains(" ")) {
            isoPathAndName = "\"" + isoPathAndName + "\"";
        }

        setIsoPathAndName(isoPathAndName);
        setExtractionPath(extractTo);

        String extractDirToRename = gameDirTxt.getText() + IsoNameNoExt;
        String extractDirRenameTo = extractTo;
        extractDirRenameTo = extractDirRenameTo.replaceAll("\"", "");

        disableButtons();

//        if (extractDirToRename.contains(" ")) {
//            extractDirToRename = "\"" + extractDirToRename + "\"";
//        }
//        if (extractDirRenameTo.contains(" ")) {
//            extractDirRenameTo = "\"" + extractDirRenameTo + "\"";
//        }        

        final String extractDirToRenameFinal = extractDirToRename;
        final String extractDirRenameToFinal = extractDirRenameTo;



        // Create Directory on 360...
//        FTPClient ftp = new FTPClient();
//        ftp.connect(ipAddy);
//        ftp.login("xbox","xbox");
//        ftp.mkd(extractDirToRename);
//        ftp.disconnect();

        setExisoCmd("exiso -d " + extractDir + " -f " + ipAddy + " -s " + isoPathAndName);

        class IsoTo360Class extends SwingWorker<Process, Object> {

            @Override
            public Process doInBackground() {
                return doExisoFTP();

            }

            @Override
            protected void done() {
                try {

                    statusLbl.setText("Status: Ready.");
                    //pctComplete.setText("Process Complete.");
                    //outputLbl.setText("");
                    FTPClient ftp = new FTPClient();
                    ftp.connect(ipAddy);
                    ftp.login("xbox", "xbox");
                    ftp.rename(extractDirToRenameFinal.toLowerCase(), extractDirRenameToFinal.toLowerCase());
                    ftp.disconnect();

                    enableButtons();




                } catch (Exception ignore) {
                }
            }
        }

        (new IsoTo360Class()).execute();






//            Process pr = null;
//
//            java.lang.Runtime rt = java.lang.Runtime.getRuntime();
//            String[] commands = {"cmd", "/c", "start/wait", workingDir, "batchFiles\\IsoTo360.cmd", gameDir, gameName, ipAddy, rarIsoDir, isoName, IsoNameNoExt, batDir};
//            try {
//                pr = rt.exec(commands);
//
//            } catch (IOException ex) {
//                Logger.getLogger(RarTo360Form.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            try {
//                pr.waitFor();
//                statusLbl.setText("Status: Processing " + gameName + " finished.");
//            } catch (InterruptedException ex) {
//                Logger.getLogger(RarTo360Form.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        } else {
//            
// 
//            
//            String[] commands = {"cmd", "/c", "start/wait", workingDir, "batchFiles\\IsoTo360.cmd", gameDir, gameName, ipAddy, rarIsoDir, isoName, IsoNameNoExt, batDir};
//            jobQueue.add(commands);
//            jobQueueDescription.add("ISO: " + gameName);
//            this.gameNameTxt.setText("");
//            statusLbl.setText(gameName + " added to queue (ISO).");
//        }

    }

    public void IsoToDisc(String extractDir, String gameName, String isoDir, String isoName, String workingDir) throws IOException {



        String extractTo = extractDir + "\\" + gameName;
        String isoPathAndName = isoDir + "\\" + isoName;

        if (extractTo.contains(" ")) {
            extractTo = "\"" + extractTo + "\"";
        }
        if (isoPathAndName.contains(" ")) {
            isoPathAndName = "\"" + isoPathAndName + "\"";
        }

        setIsoPathAndName(isoPathAndName);
        setExtractionPath(extractTo);

        makeExtractDir(extractTo);



        //String commandString = workingDir + "\\exiso.exe -d " + extractTo + " -s " + isoPathAndName;
        String commandString = "exiso.exe -d " + extractTo + " -s " + isoPathAndName;
        setExisoCmd(commandString);

        class IsoToDiscClass extends SwingWorker<Process, Object> {

            @Override
            public Process doInBackground() throws InterruptedException {
                return doExisoLocal();

            }

            @Override
            protected void done() {
                try {
                    statusLbl.setText("Status: Ready.");
                    //pctComplete.setText("Finished Extracting.");
                    //outputLbl.setText("");
                    enableButtons();

                } catch (Exception ignore) {
                }
            }
        }

        (new IsoToDiscClass()).execute();

    }

    public void makeExtractDir(String extractDir) throws IOException {


        String[] command = new String[3];
        command[0] = "cmd";
        command[1] = "/C";
        command[2] = "mkdir " + extractDir;

        Process p = Runtime.getRuntime().exec(command);

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

        BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));


        String s = null;
        System.out.println("Here is the standard output of the command:\n");
        while ((s = stdInput.readLine()) != null) {
            //txtOutputLabel.setText(s);
            System.out.println(s);

        }

        // read any errors from the attempted command

        System.out.println("Here is the standard error of the command (if any):\n");
        while ((s = stdError.readLine()) != null) {
            System.out.println(s);
        }

    }

    // Getters and Setters
    public void setIsoPathAndName(String isoPath) {
        isoPathAndName = isoPath;
    }

    public String getIsoPathAndName() {
        return isoPathAndName;
    }

    public long getIsoSize(String pathAndName) {
        long i = 0;
        File file = new File(pathAndName);
        i = file.length();

        return i;
    }

    public long getFolderSize(String folderName) {

        long size = FileUtils.sizeOfDirectory(new File(folderName));

        return size;


    }

    public void setCmdString(String[] cmd) {
        cmdString = cmd;
    }

    public String[] getCmdString() {
        return cmdString;
    }

    public void setExisoCmd(String exCmd) {
        exisoCmd = exCmd;
    }

    public String getExisoCmd() {
        return exisoCmd;
    }

    public void setExtractionPath(String path) {
        extractionPath = path;
    }

    public String getExtractionPath() {
        return extractionPath;
    }

    // MORE CUTPASTE CODE
    class ExtensionFileFilter {

        String description;
        String extensions[];

        public ExtensionFileFilter(String description, String extension) {
            this(description, new String[]{extension});
        }

        public ExtensionFileFilter(String description, String extensions[]) {
            if (description == null) {
                this.description = extensions[0];
            } else {
                this.description = description;
            }
            this.extensions = (String[]) extensions.clone();
            toLower(this.extensions);
        }

        private void toLower(String array[]) {
            for (int i = 0, n = array.length; i < n; i++) {
                array[i] = array[i].toLowerCase();
            }
        }

        public String getDescription() {
            return description;
        }
    }
	
	
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        ipAddyTxt = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        gameNameTxt = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        gameDirTxt = new javax.swing.JTextField();
        selectIsoBtn = new javax.swing.JButton();
        defaultlExtractChkBx = new java.awt.Checkbox();
        localExtractChkBx = new java.awt.Checkbox();
        statusLbl = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        percentLbl = new javax.swing.JLabel();
        pctLbl = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        startXferBtn = new javax.swing.JButton();
        clearQBtn = new javax.swing.JButton();
        qViewBtn = new javax.swing.JButton();
        addToQueueBtn = new javax.swing.JButton();
        startBatchQueueBtn = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("IsoTo360 v1");
        setBackground(new java.awt.Color(153, 153, 255));
        setForeground(java.awt.Color.blue);
        setIconImages(null);
        setResizable(false);

        jPanel2.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        ipAddyTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ipAddyTxtActionPerformed(evt);
            }
        });

        jLabel1.setText("Game Name:");

        gameNameTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gameNameTxtActionPerformed(evt);
            }
        });

        jLabel6.setText("360 IP Address:");

        jLabel4.setText("360 Game Directory:");

        gameDirTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gameDirTxtActionPerformed(evt);
            }
        });

        selectIsoBtn.setText("Select ISO");
        selectIsoBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectIsoBtnActionPerformed(evt);
            }
        });

        defaultlExtractChkBx.setFont(new java.awt.Font("Tahoma", 0, 11));
        defaultlExtractChkBx.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                defaultlExtractChkBxItemStateChanged(evt);
            }
        });
        defaultlExtractChkBx.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                defaultlExtractChkBxPropertyChange(evt);
            }
        });

        localExtractChkBx.setFont(new java.awt.Font("Tahoma", 0, 11));
        localExtractChkBx.setLabel("Extract to local directory?");
        localExtractChkBx.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                localExtractChkBxItemStateChanged(evt);
            }
        });
        localExtractChkBx.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                localExtractChkBxPropertyChange(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(gameDirTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 159, Short.MAX_VALUE)
                    .addComponent(gameNameTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 159, Short.MAX_VALUE)
                    .addComponent(ipAddyTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 159, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(localExtractChkBx, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(defaultlExtractChkBx, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(selectIsoBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel2Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {gameDirTxt, gameNameTxt});

        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gameNameTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(selectIsoBtn))
                .addGap(10, 10, 10)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(gameDirTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(defaultlExtractChkBx, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(ipAddyTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(localExtractChkBx, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(19, Short.MAX_VALUE))
        );

        statusLbl.setText("Status: Ready... Please select an ISO to extract or transfer");

        startXferBtn.setText("Start FTP Transfer");
        startXferBtn.setToolTipText("Select directory containing your game files");
        startXferBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startXferBtnActionPerformed(evt);
            }
        });

        clearQBtn.setText("Clear Queue");
        clearQBtn.setToolTipText("Select directory containing your game files");
        clearQBtn.setEnabled(false);
        clearQBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearQBtnActionPerformed(evt);
            }
        });

        qViewBtn.setText("Queue Viewer");
        qViewBtn.setToolTipText("Select directory containing your game files");
        qViewBtn.setEnabled(false);
        qViewBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                qViewBtnActionPerformed(evt);
            }
        });

        addToQueueBtn.setText("Add To Queue");
        addToQueueBtn.setEnabled(false);
        addToQueueBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addToQueueBtnActionPerformed(evt);
            }
        });

        startBatchQueueBtn.setText("Process Queue");
        startBatchQueueBtn.setEnabled(false);
        startBatchQueueBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startBatchQueueBtnActionPerformed(evt);
            }
        });

        jButton3.setText("Visit Support Page");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(startXferBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addToQueueBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(clearQBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(startBatchQueueBtn)
                    .addComponent(qViewBtn))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {addToQueueBtn, clearQBtn, jButton3, qViewBtn, startBatchQueueBtn, startXferBtn});

        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(startBatchQueueBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(qViewBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addComponent(addToQueueBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(clearQBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addComponent(startXferBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(15, Short.MAX_VALUE))
        );

        jPanel3Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {addToQueueBtn, clearQBtn, jButton3, qViewBtn, startBatchQueueBtn, startXferBtn});

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pctLbl)
            .addComponent(percentLbl)
            .addComponent(jLabel3)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 480, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pctLbl)
                    .addComponent(percentLbl)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 14, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
	
	
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addToQueueBtn;
    private javax.swing.JButton clearQBtn;
    private java.awt.Checkbox defaultlExtractChkBx;
    private javax.swing.JTextField gameDirTxt;
    private javax.swing.JTextField gameNameTxt;
    private javax.swing.JTextField ipAddyTxt;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private java.awt.Checkbox localExtractChkBx;
    private javax.swing.JLabel pctLbl;
    private javax.swing.JLabel percentLbl;
    private javax.swing.JButton qViewBtn;
    private javax.swing.JButton selectIsoBtn;
    private javax.swing.JButton startBatchQueueBtn;
    private javax.swing.JButton startXferBtn;
    private javax.swing.JLabel statusLbl;
    // End of variables declaration//GEN-END:variables
}
