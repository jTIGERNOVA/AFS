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
                File[] files = FileUtils.listFiles(new File(input.srcDir), new String[]{"txt"}, false).toArray(new File[0]);
                String[] filePaths = new String[files.length];
                for (int c = 0; c < files.length; c++) {
                    filePaths[c] = files[c].getPath();
                }

                afsExecution.execute(filePaths, input.endpoint, AFSExecution.AFSOption.POST, true);
            }
        });

        afsExecution.setOnExecutionIDUpdate(new AFSExecution.OnExecutionIDUpdate() {
            @Override
            public void update(String newExecutionID) {
                frame.setExecutionID(newExecutionID);
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
