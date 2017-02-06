package jtiger.AFSApp.task;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Random;

/**
 * Created by antonioj on 2/3/2017.
 */
public class AFSGetStatus extends AFSTask {

    static Random r = new Random();
    private final String tokenJsonFilePath;
    private String status;
    private boolean triggerGet;

    public AFSGetStatus(String executionID, String tokenJsonFilePath, boolean triggerGet) {
        super(executionID);
        if (!isFile(tokenJsonFilePath))
            throw new RuntimeException("Could not find token file. Has the post been completed?");

        this.tokenJsonFilePath = tokenJsonFilePath;
        this.triggerGet = triggerGet;
    }

    @Override
    protected void doRun() {
        //read json file

        try {
            JSONObject json = new JSONObject(FileUtils.readFileToString(new File(tokenJsonFilePath), "UTF-8"));
            _token = json.getString("token");
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (_token == null) {
            System.err.println("Invalid json in file: " + tokenJsonFilePath);
            return;
        }

        doStatusCheck();

        boolean done = false;
        int tryLimit = 5;
        int c = 0;

        do {
            System.out.println(">>Get status is not complete for task: " + getTaskID());

            doStatusCheck();

            done = r.nextBoolean();
            c++;
        }
        while (!done && c < tryLimit);

        //save status
        status = new Random().nextDouble() + "";

        String json = "{\"status\": \"" + status + "\", \"token\": \"" + _token + "\"}";

        if (!isUrlValid(getExecutionResultDir())) {
            System.err.println("*Execution result directory must be set. Skipping...");
            return;
        }

        File file = new File(getExecutionResultDir(), getExecutionID() + "-status.json");

        try {
            FileUtils.write(file, json, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Done with 'GETSTATUS' for token: " + _token + " task: " + getTaskID() + ". Results saved at: " + file.getPath());

        if (triggerGet) {
            AFSGet get = new AFSGet(getExecutionID(), file.getPath());
            get.setExecutionResultDir(getExecutionResultDir());

            get.runTask();
        }
    }

    private void doStatusCheck() {
        System.out.println("Performing a status check for task: " + getTaskID());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String getExecutionKey() {
        return tokenJsonFilePath;
    }

    public String getTokenJsonFilePath() {
        return tokenJsonFilePath;
    }

    public boolean isTriggerGet() {
        return triggerGet;
    }

    public void setTriggerGet(boolean triggerGet) {
        this.triggerGet = triggerGet;
    }
}
