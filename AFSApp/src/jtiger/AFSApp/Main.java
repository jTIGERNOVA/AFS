package jtiger.AFSApp;

import jtiger.AFSApp.task.AFSExecution;
import jtiger.AFSApp.ui.AFSApp;

public class Main {

    public static void main(String[] args) {

        final AFSExecution afsExecution = new AFSExecution("C:/temp6/cmd");

        String[] files = new String[]{"C:/temp/a.txt", "C:/temp/a2.txt"};
        String endpoint = "http://gmail.com";

        afsExecution.execute(files, endpoint, AFSExecution.AFSOption.POST, true);

        AFSApp frame = new AFSApp();
        frame.setOnClose(new Runnable() {
            @Override
            public void run() {
                afsExecution.exit();
            }
        });
        frame.setVisible(true);
    }
}
