package jtiger.AFSApp;

import jtiger.AFSApp.task.AFSExecution;
import jtiger.AFSApp.ui.AFSApp;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
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
                File[] files = FileUtils.listFiles(new File(input.srcDir), Util.getSupportedFileExtensions(), false).toArray(new File[0]);

                if (files.length == 0) {
                    JOptionPane.showMessageDialog(null, "No files found. Select another directory");
                    return;
                }

                String[] filePaths = new String[files.length];
                for (int c = 0; c < files.length; c++) {
                    filePaths[c] = files[c].getPath();
                }

                boolean autoContinue;
                boolean autoContinueLevelTwo;
                AFSExecution.AFSOption start = AFSExecution.AFSOption.POST;

                if (input.shouldDoPost && input.shouldDoGetStatus && input.shouldDoGetResult) {
                    start = AFSExecution.AFSOption.POST;

                    autoContinue = true;
                    autoContinueLevelTwo = true;
                } else if (input.shouldDoPost && input.shouldDoGetStatus) {
                    start = AFSExecution.AFSOption.POST;

                    autoContinue = true;
                    autoContinueLevelTwo = false;
                } else if (input.shouldDoGetStatus && input.shouldDoGetResult) {
                    start = AFSExecution.AFSOption.GETSTATUS;

                    autoContinue = true;
                    autoContinueLevelTwo = true;
                } else {
                    //single task runs
                    if (input.shouldDoPost)
                        start = AFSExecution.AFSOption.POST;
                    else if (input.shouldDoGetStatus)
                        start = AFSExecution.AFSOption.GETSTATUS;
                    else if (input.shouldDoGetResult)
                        start = AFSExecution.AFSOption.GETRESULT;

                    //no starting tasks automatically
                    autoContinue = false;
                    autoContinueLevelTwo = false;
                }

                afsExecution.execute(filePaths, input.endpoint, start, autoContinue, autoContinueLevelTwo);

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
            public void onStart(String executionID, int fileCount) {
                frame.addPendingTask(executionID);
            }

            @Override
            public void onDone(String executionID, String resultDirectory) {
                frame.removePendingTask(executionID);

                if (new File(resultDirectory).isDirectory())
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
}
