package rarto360;

import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO[jekm] - someone remind me to document this class.
 *
 * @author Jesenko Mehmedbasic
 *         created 24-11-11, 01:48
 */
public class IsoFtpWorker extends ExtendedSwingWorker {
    private String host;

    private String username = "xbox";
    private String password = "xbox";

    private String remoteRenameSource;
    private String remoteRenameDestination;

    private List<String> parameters = new ArrayList<String>();

    protected IsoFtpWorker(ProgressUpdateListener progressUpdateListener,
                           String host,
                           String remoteRenameSource,
                           String remoteRenameDestination,
                           String isoPath,
                           String extractionDirectory) {
        super(progressUpdateListener);
        this.remoteRenameDestination = remoteRenameDestination;
        this.remoteRenameSource = remoteRenameSource;
        this.host = host;

        parameters.add("exiso");
        parameters.add("-d");
        parameters.add(extractionDirectory);
        parameters.add("-f");
        parameters.add(host);
        parameters.add("-s");
        parameters.add(isoPath);


    }

    @Override
    protected Process doInBackground() throws Exception {

        CommandBuilder builder = new CommandBuilder(parameters);

        disableButtons();
        Process p = Runtime.getRuntime().exec(builder.build());

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

        updateStatusLabel("Processing ISO, please wait...");
        int i = 0;

        String line;
        //noinspection UnusedAssignment
        while ((line = stdInput.readLine()) != null) {
            System.out.println(line);
        }
        System.out.println("Here is the standard error of the command (if any):\n");
        while ((line = stdError.readLine()) != null) {
            System.err.println(line);
            updateCommandLine(line + "\n");
        }
        updateStatusLabel("Status: Connecting to FTP server...");
        FTPClient ftp = new FTPClient();
        try {
            ftp.connect(host);
            ftp.login(username, password);

            ftp.rename(remoteRenameSource.toLowerCase(), remoteRenameDestination.toLowerCase());
            updateStatusLabel("Status: Ftp working...");

            ftp.disconnect();
            updateStatusLabel("Status: Ready.");
        } catch (IOException e) {
            // TODO logging
            e.printStackTrace();
            updateStatusLabel("Status: Connection error during FTP");
        }

        return p;
    }

    @Override
    protected void done() {
        super.done();
    }
}
