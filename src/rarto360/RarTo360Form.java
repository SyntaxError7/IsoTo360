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
 * 
 */

// test.


package rarto360;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class RarTo360Form extends javax.swing.JFrame implements ActionListener {
    private static final Logger logger = Logger.getLogger(RarTo360Form.class);

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private static final List<String> jobQueueDescription = new ArrayList<String>();
    private final List<String[]> jobQueue = new ArrayList<String[]>();
    private final Properties config = new Properties();
    private String workingDir, batDir = "";
    private String isoNameNoExt = "";
    private String rarNameNoExt, rarName = "";
    private String startLoc = "";
    private String extractLoc = "";
    private JFileChooser chooser;
    private JFileChooser localChooser;
    private int IsoRarSelection = 0;

    private final JRadioButton button1 = new JRadioButton("Rar");
    private final JRadioButton button2 = new JRadioButton("ISO");

    private String gameDir;
    private String gameName;
    private String ipAddy;
    private String rarIsoDir;
    private String isoName;

    private File curDir;
    private String exisoCmd = "";

    private Boolean isoSelected = false;
    private String isoPathAndName = "";

    private ExtendedSwingWorker.ProgressUpdateListener progressUpdateListener;

    /**
     * Creates new form RarTo360Form
     */
    public RarTo360Form() {
        initComponents();
        selectIsoBtn.setVisible(true);

        setButtons();
        setConfig();
        repaint();
        setProgressUpdateListener();
    }

    /**
     * Create the progress update listener, which will be used for all the workers
     */
    private void setProgressUpdateListener() {
        progressUpdateListener = new ExtendedSwingWorker.ProgressUpdateListener() {
            @Override
            public void onButtonsEnabled() {
                enableButtons();
            }

            @Override
            public void onButtonsDisabled() {
                disableButtons();
            }

            @Override
            public void onCommandLineUpdated(String line) {
                updateTextArea(line);
            }

            @Override
            public void onStatusLabelChanged(String label) {
                setStatusBarText(label);
            }
        };
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
            setStatusBarText(LocalizeFromResource.getString(this, "rarSelectedStatusLabel"));
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

    private void selectISO() {
        String chooserTitle = "";

        FileFilter filter = new FileNameExtensionFilter("ISO file", "iso");
        chooser = new JFileChooser(startLoc);
        chooser.addChoosableFileFilter(filter);
        chooser.setFileFilter(filter);
        chooser.setDialogTitle(chooserTitle);

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            setStatusBarText(chooser.getSelectedFile().toString());
            isoSelected = true;
            if (this.gameNameTxt.getText().equals("")) {
                setStatusBarText("Status: ISO Selected, enter a game name.");
            } else {
                setStatusBarText("Status: ISO Selected, ready to extract.");
            }
        }


    }

    private void startXferBtnActionPerformed() {//GEN-FIRST:event_startXferBtnActionPerformed

        if (this.gameNameTxt.getText().equals("")) {
            setStatusBarText("Please enter a game name.");
            this.gameNameTxt.requestFocusInWindow();
        } else {
            if (!isoSelected) {
                setStatusBarText("Status:Please choose an ISO to extract or transfer");
            } else if (localExtractChkBx.getState()) {
                IsoToFTPLocal(localChooser.getSelectedFile());
            } else if (defaultlExtractChkBx.getState()) {
                IsoToFTPLocal(localChooser.getCurrentDirectory());
            } else {
                IsoToFTP();
            }
        }

    }

    private void IsoToFTPLocal(File extDir) {
        //localExtractChkBxItemStateChanged
        isoName = chooser.getName(chooser.getSelectedFile());
        isoNameNoExt = isoName.substring(0, (isoName.length() - 4));
        curDir = chooser.getCurrentDirectory();

        setVariables();
        rarIsoDir = curDir.toString();
        String dirToExt = extDir.toString();
        //dirToExt = "\"" + dirToExt + "\"";

        try {
            IsoToDisc(dirToExt, gameName, rarIsoDir, isoName);
        } catch (IOException ex) {
            logger.error("An error occurred", ex);
        }
    }

    private void RunRarBatchLocal(File extDir) {

        // localExtractChkBxItemStateChanged
        rarName = chooser.getName(chooser.getSelectedFile());
        rarNameNoExt = rarName.substring(0, (rarName.length() - 4));
        curDir = localChooser.getCurrentDirectory();
        setVariables();
        rarIsoDir = curDir.toString();

        String dirToExt = extDir.toString();
        dirToExt = "\"" + dirToExt + "\"";


        workingDir = "\"" + workingDir + "\"";

        String[] commands = {"cmd", "/c", "start/wait", workingDir, "batchFiles\\RarToDisc.cmd", gameDir, gameName, ipAddy, rarIsoDir, batDir, rarNameNoExt, dirToExt};
        jobQueue.add(commands);
        jobQueueDescription.add("ISO: " + gameName);
        this.gameNameTxt.setText("");
        setStatusBarText(gameName + " added to queue (ISO).");
    }

    private void RunRarBatch() {

        rarName = chooser.getName(chooser.getSelectedFile());
        rarNameNoExt = rarName.substring(0, (rarName.length() - 4));
        curDir = chooser.getCurrentDirectory();
        setVariables();
        rarIsoDir = curDir.toString();

        workingDir = "\"" + workingDir + "\"";

        String[] commands = {"cmd.exe", "/c", "start/wait", workingDir, "batchFiles\\RarTo360.cmd", gameDir, gameName, ipAddy, rarIsoDir, batDir, rarNameNoExt};
        jobQueue.add(commands);
        jobQueueDescription.add("RAR: " + gameName);
        this.gameNameTxt.setText("");
        setStatusBarText(gameName + " added to queue (Rar).");

    }//GEN-LAST:event_startXferBtnActionPerformed

    private void jButton3ActionPerformed() {//GEN-FIRST:event_jButton3ActionPerformed
        try {
            Runtime.getRuntime().exec("cmd.exe /c start http://forums.xbox-scene.com/index.php?showtopic=737562");
        } catch (IOException ex) {
            logger.error("An error occurred", ex);
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void addToQueueBtnActionPerformed() {//GEN-FIRST:event_addToQueueBtnActionPerformed

        if (!this.gameNameTxt.getText().isEmpty()) {

            if (IsoRarSelection == 1) {
                if (localExtractChkBx.getState()) {
                    RunRarBatchLocal(localChooser.getSelectedFile());
                } else {
                    RunRarBatch();
                }
            } else if (IsoRarSelection == 2) {
                if (localExtractChkBx.getState()) {
                    IsoToFTPLocal(localChooser.getSelectedFile());
                } else {
                    IsoToFTP();
                }
            } else {
                setStatusBarText("Please select a file type.");
            }
        } else {
            setStatusBarText("Please enter a game name");
        }
    }//GEN-LAST:event_addToQueueBtnActionPerformed

    private void startBatchQueueBtnActionPerformed() {//GEN-FIRST:event_startBatchQueueBtnActionPerformed


        for (String[] aJobQueue : jobQueue) {
            processJob(aJobQueue);
        }
        setStatusBarText("Status: Batch job complete.");
        jobQueue.clear();
        jobQueueDescription.clear();


    }//GEN-LAST:event_startBatchQueueBtnActionPerformed

    private void clearQBtnActionPerformed() {//GEN-FIRST:event_clearQBtnActionPerformed

        // TODO add your handling code here:
        jobQueue.clear();
        jobQueueDescription.clear();
        curDir = new File(System.getProperty("user.dir") + "/logs");
        deleteAll(curDir);
        setStatusBarText("Status: Logs and queue cleared.");
    }//GEN-LAST:event_clearQBtnActionPerformed

    private void qViewBtnActionPerformed() {//GEN-FIRST:event_qViewBtnActionPerformed

        JDialog f = new QueueDialog(new JFrame());
        f.setVisible(true);

    }//GEN-LAST:event_qViewBtnActionPerformed

    private void localExtractChkBxItemStateChanged() {//GEN-FIRST:event_localExtractChkBxItemStateChanged

        if (localExtractChkBx.getState()) {
            gameDirTxt.setEnabled(false);
            ipAddyTxt.setEnabled(false);
            startXferBtn.setText("Extract To Directory");
            defaultlExtractChkBx.setState(false);
            setExtractDirectory();
        } else {
            gameDirTxt.setEnabled(true);
            ipAddyTxt.setEnabled(true);
            startXferBtn.setText("Start FTP Transfer");
            setStatusBarText("Status: Select an ISO, enter a game name and click Start FTP Transfer");

        }
    }//GEN-LAST:event_localExtractChkBxItemStateChanged

    private void defaultlExtractChkBxItemStateChanged() {//GEN-FIRST:event_defaultlExtractChkBxItemStateChanged
        if (defaultlExtractChkBx.getState()) {

            localChooser = new JFileChooser(extractLoc);
            localChooser.setCurrentDirectory(new java.io.File(extractLoc));
            gameDirTxt.setEnabled(false);
            ipAddyTxt.setEnabled(false);
            startXferBtn.setText("Extract To Directory");
            setStatusBarText("Game will be extracted to: " + localChooser.getCurrentDirectory().toString() + "\\" + "\"Game Name\"");
            localExtractChkBx.setState(false);

        } else {
            gameDirTxt.setEnabled(true);
            ipAddyTxt.setEnabled(true);
            startXferBtn.setText("Start FTP Transfer");
            setStatusBarText("Status: Select an ISO, enter a game name and click Start FTP Transfer");

        }
    }//GEN-LAST:event_defaultlExtractChkBxItemStateChanged

    private void selectIsoBtnActionPerformed() {//GEN-FIRST:event_selectIsoBtnActionPerformed

        selectISO();
        // TODO add your handling code here:
    }//GEN-LAST:event_selectIsoBtnActionPerformed

    void setExtractDirectory() {

        localChooser = new JFileChooser(startLoc);
        localChooser.setCurrentDirectory(new java.io.File(extractLoc));
        localChooser.setDialogTitle("Choose Extraction Location");
        localChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        localChooser.setAcceptAllFileFilterUsed(false);

        if (localChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            System.out.println("getCurrentDirectory(): " + localChooser.getCurrentDirectory());
            System.out.println("getSelectedFile() : " + localChooser.getSelectedFile());
            setStatusBarText("Game will be extracted to: " + localChooser.getSelectedFile().toString() + "\\" + "\"Game Name\"");

        } else {
            System.out.println("No Selection ");

        }


    }

    private static boolean deleteAll(File dir) {
        if (!dir.exists()) {
            return true;
        }
        boolean res = true;
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            for (File file : files) {
                res &= deleteAll(file);
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
            logger.error("An error occurred", ex);
        }
        try {
            if (pr != null) {
                pr.waitFor();
            }
            setStatusBarText("Status: Processing " + gameName + " finished.");
        } catch (InterruptedException ex) {
            logger.error("An error occurred", ex);
        }

    }

    void setVariables() {

        workingDir = System.getProperty("user.dir");
        batDir = System.getProperty("user.dir");
        gameDir = this.gameDirTxt.getText();
        gameName = this.gameNameTxt.getText();
        ipAddy = this.ipAddyTxt.getText();
    }

    Process doExisoFTP() {
        try {

            String commandString = getExisoCmd();
            //Process p = Runtime.getRuntime().exec(commandString);
            disableButtons();
            Process p = Runtime.getRuntime().exec("cmd.exe /c start " + commandString);

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

            String string;
            setStatusBarText("Processing ISO, please wait...");


            //String destFolder = getExtractionPath();
            //long isoSize = getIsoSize(isoPathAndName);


            statusLbl.repaint(100);
            //txtOutputLabel.repaint(100);

            int i = 0;

            //noinspection UnusedAssignment
            while ((string = stdInput.readLine()) != null) {

                if (i > 10) {
                    i = 0;
                    //outputLbl.setText(s);
                } else {
                    i++;
                }
            }
            System.out.println("Here is the standard error of the command (if any):\n");
            while ((string = stdError.readLine()) != null) {
                System.out.println(string);
            }


            return p;

        } catch (IOException ex) {
            logger.error("An error occurred", ex);
            return null;
        }

    }

    void enableButtons() {


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

    void disableButtons() {

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

    private void IsoToFTP() {

        ipAddy = ipAddyTxt.getText();
        String extractDir = gameDirTxt.getText();
        gameName = gameNameTxt.getText();
        isoName = chooser.getName(chooser.getSelectedFile());
        isoNameNoExt = isoName.substring(0, (isoName.length() - 4));
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

        String extractDirToRename = gameDirTxt.getText() + isoNameNoExt;
        String extractDirRenameTo = extractTo;
        extractDirRenameTo = extractDirRenameTo.replaceAll("\"", "");

        setExisoCmd("exiso -d " + extractDir + " -f " + ipAddy + " -s " + isoPathAndName);

        IsoFtpWorker worker = new IsoFtpWorker(
                progressUpdateListener,
                ipAddy,
                extractDirToRename,
                extractDirRenameTo,
                isoPathAndName,
                extractDir);
        worker.execute();
    }

    void IsoToDisc(String extractDir, String gameName, String isoDir, String isoName) throws IOException {


        String extractTo = extractDir + "\\" + gameName;
        String isoPathAndName = isoDir + "\\" + isoName;

        if (extractTo.contains(" ")) {
            extractTo = "\"" + extractTo + "\"";
        }
        if (isoPathAndName.contains(" ")) {
            isoPathAndName = "\"" + isoPathAndName + "\"";
        }

        setIsoPathAndName(isoPathAndName);

        makeExtractDir(extractTo);

        //String commandString = workingDir + "\\exiso.exe -d " + extractTo + " -s " + isoPathAndName;
        String commandString = "exiso.exe -d " + extractTo + " -s " + isoPathAndName;
        setExisoCmd(commandString);

        new IsoExtractWorker(progressUpdateListener, extractTo, isoPathAndName).execute();
    }

    void makeExtractDir(String extractDir) throws IOException {


        String[] command = new String[3];
        command[0] = "cmd";
        command[1] = "/C";
        command[2] = "mkdir " + extractDir;

        Process p = Runtime.getRuntime().exec(command);

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

        BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));


        String s;
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
    void setIsoPathAndName(String isoPath) {
        isoPathAndName = isoPath;
    }

    void setExisoCmd(String exCmd) {
        exisoCmd = exCmd;
    }

    String getExisoCmd() {
        return exisoCmd;
    }

    private void updateTextArea(final String text) {
        jTextOutput.append(text);
    }


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        JPanel jPanel2 = new JPanel();
        ipAddyTxt = new javax.swing.JTextField();
        JLabel jLabel1 = new JLabel();
        gameNameTxt = new javax.swing.JTextField();
        JLabel jLabel6 = new JLabel();
        JLabel jLabel4 = new JLabel();
        gameDirTxt = new javax.swing.JTextField();
        selectIsoBtn = new javax.swing.JButton();
        defaultlExtractChkBx = new java.awt.Checkbox();
        localExtractChkBx = new java.awt.Checkbox();
        statusLbl = new javax.swing.JLabel();
        JLabel jLabel3 = new JLabel();
        JLabel percentLbl = new JLabel();
        JLabel pctLbl = new JLabel();
        JPanel jPanel3 = new JPanel();
        startXferBtn = new javax.swing.JButton();
        clearQBtn = new javax.swing.JButton();
        qViewBtn = new javax.swing.JButton();
        addToQueueBtn = new javax.swing.JButton();
        startBatchQueueBtn = new javax.swing.JButton();
        JButton jButton3 = new JButton();
        JScrollPane jScrollPane1 = new JScrollPane();
        jTextOutput = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("IsoTo360 v1");
        setBackground(new java.awt.Color(153, 153, 255));
        setForeground(java.awt.Color.blue);
        setIconImages(null);
        setResizable(false);

        jPanel2.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        ipAddyTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            }
        });

        jLabel1.setText("Game Name:");

        gameNameTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            }
        });

        jLabel6.setText("360 IP Address:");

        jLabel4.setText("360 Game Directory:");

        gameDirTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            }
        });

        selectIsoBtn.setText("Select ISO");
        selectIsoBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectIsoBtnActionPerformed();
            }
        });

        defaultlExtractChkBx.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        defaultlExtractChkBx.setLabel("Extract to default directory?");
        defaultlExtractChkBx.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                defaultlExtractChkBxItemStateChanged();
            }
        });
        defaultlExtractChkBx.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
            }
        });

        localExtractChkBx.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        localExtractChkBx.setLabel("Extract to local directory?");
        localExtractChkBx.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                localExtractChkBxItemStateChanged();
            }
        });
        localExtractChkBx.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
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
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(localExtractChkBx, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(selectIsoBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(defaultlExtractChkBx, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addContainerGap())
        );

        jPanel2Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, gameDirTxt, gameNameTxt);

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
                                .addContainerGap(20, Short.MAX_VALUE))
        );

        String text = "Status: Ready... Please select an ISO to extract or transfer";
        setStatusBarText(text);

        jPanel3.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        startXferBtn.setText("Start FTP Transfer");
        startXferBtn.setToolTipText("Select directory containing your game files");
        startXferBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startXferBtnActionPerformed();
            }
        });

        clearQBtn.setText("Clear Queue");
        clearQBtn.setToolTipText("Select directory containing your game files");
        clearQBtn.setEnabled(false);
        clearQBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearQBtnActionPerformed();
            }
        });

        qViewBtn.setText("Queue Viewer");
        qViewBtn.setToolTipText("Select directory containing your game files");
        qViewBtn.setEnabled(false);
        qViewBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                qViewBtnActionPerformed();
            }
        });

        addToQueueBtn.setText("Add To Queue");
        addToQueueBtn.setEnabled(false);
        addToQueueBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addToQueueBtnActionPerformed();
            }
        });

        startBatchQueueBtn.setText("Process Queue");
        startBatchQueueBtn.setEnabled(false);
        startBatchQueueBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startBatchQueueBtnActionPerformed();
            }
        });

        jButton3.setText("Visit Support Page");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed();
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
                jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addGroup(jPanel3Layout.createSequentialGroup()
                                                .addComponent(startXferBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(addToQueueBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel3Layout.createSequentialGroup()
                                                .addComponent(jButton3)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(clearQBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel3Layout.createSequentialGroup()
                                                .addComponent(qViewBtn)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(startBatchQueueBtn)))
                                .addContainerGap())
        );

        jPanel3Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, addToQueueBtn, clearQBtn, jButton3, qViewBtn, startBatchQueueBtn, startXferBtn);

        jPanel3Layout.setVerticalGroup(
                jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel3Layout.createSequentialGroup()
                                                .addGap(35, 35, 35)
                                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(startBatchQueueBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(qViewBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(addToQueueBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(startXferBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(clearQBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(7, Short.MAX_VALUE))
        );

        jPanel3Layout.linkSize(javax.swing.SwingConstants.VERTICAL, addToQueueBtn, clearQBtn, jButton3, qViewBtn, startBatchQueueBtn, startXferBtn);

        jTextOutput.setBackground(new Color(242, 242, 242));
        jTextOutput.setColumns(20);
        jTextOutput.setEditable(false);
        jTextOutput.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
        jTextOutput.setRows(5);
        jTextOutput.setWrapStyleWord(true);
        jTextOutput.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        jScrollPane1.setViewportView(jTextOutput);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                                .addContainerGap()
                                                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 804, Short.MAX_VALUE))
                                        .addComponent(pctLbl, javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(percentLbl, javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                                .addContainerGap()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(statusLbl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(6, 6, 6)
                                                                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                                .addContainerGap())
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
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(jPanel3, 0, 121, Short.MAX_VALUE)
                                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Sets the text on the status bar.
     *
     * @param text the text to set.
     */
    private void setStatusBarText(String text) {
        statusLbl.setText(text);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addToQueueBtn;
    private javax.swing.JButton clearQBtn;
    private java.awt.Checkbox defaultlExtractChkBx;
    private javax.swing.JTextField gameDirTxt;
    private javax.swing.JTextField gameNameTxt;
    private javax.swing.JTextField ipAddyTxt;
    private javax.swing.JTextArea jTextOutput;
    private java.awt.Checkbox localExtractChkBx;
    private javax.swing.JButton qViewBtn;
    private javax.swing.JButton selectIsoBtn;
    private javax.swing.JButton startBatchQueueBtn;
    private javax.swing.JButton startXferBtn;
    private javax.swing.JLabel statusLbl;
    // End of variables declaration//GEN-END:variables
}
