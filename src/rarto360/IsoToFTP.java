/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rarto360;

import java.io.IOException;
import java.net.SocketException;
import javax.swing.SwingWorker;
import org.apache.commons.net.ftp.FTPClient;

/**
 *
 * @author JC Denton
 */
public class IsoToFTP extends RarTo360Form{
        
   
    
        IsoToFTP(boolean batchBool) throws SocketException, IOException {

        

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

        String extractDirToRename = gameDirTxtStr + IsoNameNoExt;
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

                    statusReady();
                    
                    
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



    }
    
    
    
    
}
