package jtiger.AFSApp;

import jtiger.AFSApp.task.AFSExecution;
import jtiger.AFSApp.ui.AFSApp;
import org.apache.commons.io.FileUtils;

import java.io.File;

public class Main {

    public static void main(String[] args) {
        String[] endpoints = new String[]{"http://gmail.com"};

        final AFSExecution afsExecution = new AFSExecution("C:/temp6/cmd");

        final AFSApp frame = new AFSApp(endpoints);

        frame.setDestinationSelectedListener(new AFSApp.OnDestinationSelectedListener() {
            @Override
            public void onSelected(String dest) {
                afsExecution.setResultPath(dest);
            }
        });

        frame.setExecutionID(AFSExecution.formatExecutionID((AFSExecution.getRunCount())));

        frame.setExecutedListener(new AFSApp.OnAFSExecutedListener() {
            @Override
            public void onExecute(AFSApp.AFSInput input) {
                File[] files = FileUtils.listFiles(new File(input.srcDir), getSupportedFileExtensions(), false).toArray(new File[0]);
                String[] filePaths = new String[files.length];
                for (int c = 0; c < files.length; c++) {
                    filePaths[c] = files[c].getPath();
                }

                //TODO allow for starting from getstatus or getresult
                boolean autoContinue = true;
                afsExecution.execute(filePaths, input.endpoint, AFSExecution.AFSOption.POST, autoContinue);

                //update next run count
                frame.setExecutionID(AFSExecution.formatExecutionID((AFSExecution.getRunCount())));
            }
        });

        afsExecution.setOnExecutionIDUpdate(new AFSExecution.OnExecutionIDUpdate() {
            @Override
            public void update(String newExecutionID) {
                frame.setExecutionID(newExecutionID);
            }
        });

        afsExecution.setExecutionStatusListener(new AFSExecution.ExecutionStatusListener() {
            @Override
            public void onStart(String executionID) {
                frame.addPendingTask(executionID);
            }

            @Override
            public void onDone(String executionID, String resultDirectory) {
                frame.removePendingTask(executionID);
                frame.addFinishedTask(executionID, resultDirectory);
            }
        });

        frame.setOnClose(new Runnable() {
            @Override
            public void run() {
                afsExecution.exit();
            }
        });
        frame.setVisible(true);
    }

    public static String[] getSupportedFileExtensions() {
        return new String[]{"txt"};
    }
}
