package jtiger.AFSApp;

import jtiger.AFSApp.task.AFSExecution;

public class Main {

    public static void main(String[] args) {

        AFSExecution afsExecution = new AFSExecution("C:/temp6/cmd");

        String[] files = new String[]{"C:/temp/a.txt", "C:/temp/a2.txt"};
        String endpoint = "http://gmail.com";

        afsExecution.execute(files, endpoint, AFSExecution.AFSOption.POST, true);

        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        afsExecution.exit();
    }
}
