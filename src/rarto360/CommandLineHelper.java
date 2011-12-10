package rarto360;

/**
 * TODO[jekm] - someone remind me to document this class.
 *
 * @author Jesenko Mehmedbasic
 *         created 24-11-11, 06:00
 */
public class CommandLineHelper {
    public static interface CommandStrategy {
        public Iterable<String> getCommandForIsoExtraction();

        public Iterable<String> getCommandForIsoToFtpExtraction();
    }
}
