/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rarto360;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;

/**
 *
 * @author JC Denton
 */
public class IsoToDisc extends RarTo360Form {

    public IsoToDisc(String extractDir, String gameName, String isoDir, String isoName, String workingDir) throws IOException {

        redirectSystemStreams();

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


        (new IsoToDiscClass()).execute();

    }

    class IsoToDiscClass extends SwingWorker<Process, Object> {

        @Override
        public Process doInBackground() throws InterruptedException {
             return doExisoLocal();

        }

        @Override
        protected void done() {
            try {
                setStatus("Ready...");
                repaint();
                //pctComplete.setText("Finished Extracting.");
                //outputLbl.setText("");
                enableButtons();

            } catch (Exception ignore) {
            }
        }
    }
    
      public Process doExisoLocal() throws InterruptedException {
        try {

            String[] command = new String[3];
            command[0] = "cmd";
            command[1] = "/C";
            command[2] = getExisoCmd();

            String commandString = getExisoCmd();
            
            disableButtons();
            // Process p = Runtime.getRuntime().exec("cmd.exe /c start " + commandString);
            Process p = Runtime.getRuntime().exec("cmd.exe /c " + commandString);

            String line;

            BufferedReader input =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = input.readLine()) != null) {
                System.out.println(line);
            }
            input.close();
          

            String s = null;
            setStatus("Processing ISO, please wait...");

            String destFolder = getExtractionPath();
            long isoSize = getIsoSize(isoPathAndName);

            return p;

        } catch (IOException ex) {
            Logger.getLogger(RarTo360Form.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

    }
    
}
