package rarto360;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * A class to do the extraction of ISO files.
 *
 * @author Jesenko Mehmedbasic
 *         created 23-11-11,  20:16
 */
public abstract class ExtendedSwingWorker extends SwingWorker<Process, String> {

    private static final String STATUS_LABEL_EVENT = "StatusLabelEvent";
    private static final String BUTTONS_ENABLED_EVENT = "ButtonsEnabledEvent";
    private static final String COMMAND_LINE_OUTPUT_EVENT = "CommandLineOutputEvent";
    private static final String PROCESS_FINISHED_EVENT = "ProcessFinishedEvent";

    protected ProgressUpdateListener progressUpdateListener;

    protected ExtendedSwingWorker(ProgressUpdateListener progressUpdateListener) {
        this.progressUpdateListener = progressUpdateListener;
        addPropertyChangeListener(progressUpdateListener);
    }

    @Override
    protected void done() {
        processFinished();
        enableButtons();
    }

    protected void disableButtons() {
        firePropertyChange(BUTTONS_ENABLED_EVENT, null, false);
    }

    protected void enableButtons() {
        firePropertyChange(BUTTONS_ENABLED_EVENT, null, true);
    }

    protected void updateStatusLabel(String value) {
        firePropertyChange(STATUS_LABEL_EVENT, null, value);
    }

    protected void updateCommandLine(String line) {
        firePropertyChange(COMMAND_LINE_OUTPUT_EVENT, null, line);
    }

    protected void processFinished() {
        firePropertyChange(PROCESS_FINISHED_EVENT, null, null);
    }

    /**
     * An update listener to allow the gui to update whilst a process is running in the background.
     * <p/>
     * It's important to note that all the events are originating from the background process.
     */
    public static abstract class ProgressUpdateListener implements PropertyChangeListener {
        @Override
        public final void propertyChange(PropertyChangeEvent event) {
            String name = event.getPropertyName();
            if (STATUS_LABEL_EVENT.equals(name)) {
                onStatusLabelChanged(event.getNewValue().toString());
            }
            if (BUTTONS_ENABLED_EVENT.equals(name)) {
                if (Boolean.TRUE.equals(event.getNewValue())) {
                    onButtonsEnabled();
                }
                if (Boolean.FALSE.equals(event.getNewValue())) {
                    onButtonsDisabled();
                }
            }
            if (COMMAND_LINE_OUTPUT_EVENT.equals(name)) {
                onCommandLineUpdated(event.getNewValue().toString());
            }
            if (PROCESS_FINISHED_EVENT.equals(name)) {
                onProcessFinished();
            }
        }

        /**
         * Called when the buttons are to be enabled.
         */
        public void onButtonsEnabled() {
        }


        /**
         * Called when the buttons are to be disabled.
         */
        public void onButtonsDisabled() {
        }

        /**
         * Called when a line is added to the commandline buffer.
         *
         * @param line the line added.
         */
        public void onCommandLineUpdated(String line) {
        }

        /**
         * Called when the worker sets the status label.
         *
         * @param label the text to set on the label.
         */
        public void onStatusLabelChanged(String label) {
        }

        /**
         * This is called when the worker is finished.
         */
        public void onProcessFinished() {

        }
    }
}
