package rarto360;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * A worker to replace the old worker in {@link RarTo360Form}.
 * <p/>
 * On every event that occurs, the progress update listener is used to convey
 * this information to the gui.
 *
 * @author Jesenko Mehmedbasic
 *         created 23-11-11, 20:23
 */
public class IsoExtractWorker extends ExtendedSwingWorker {
    private List<String> parameters = new ArrayList<String>();


    protected IsoExtractWorker(ProgressUpdateListener progressUpdateListener, String outputDirectory, String isoPath) {
        super(progressUpdateListener);

        parameters.add("exiso.exe");
        parameters.add("-d");
        parameters.add(outputDirectory);
        parameters.add("-s");
        parameters.add(isoPath);
    }


    @Override
    protected Process doInBackground() throws Exception {
        disableButtons();

        CommandBuilder commandBuilder = new CommandBuilder(parameters);
        Process process = Runtime.getRuntime().exec(commandBuilder.build());

        updateStatusLabel("Processing ISO, please wait...");

        BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = input.readLine()) != null) {
            updateCommandLine(line + "\n");
        }
        input.close();

        return process;
    }
}
