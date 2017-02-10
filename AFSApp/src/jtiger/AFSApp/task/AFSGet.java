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
public class AFSGet extends AFSTask {

    static Random r = new Random();
    private final String statusJsonFilePath;
    private String status;
    private String taskResult;

    public AFSGet(String executionID, String statusJsonFilePath, String getEndpoint) {
        super(executionID, getEndpoint);
        if (!isFile(statusJsonFilePath))
            throw new RuntimeException("Could not find status file. Has the GetStatus task been completed?");

        this.statusJsonFilePath = statusJsonFilePath;
    }

    @Override
    protected String getTaskSuffix() {
        return "get";
    }

    @Override
    protected void doRun() {
        //read json file

        try {
            JSONObject json = new JSONObject(FileUtils.readFileToString(new File(statusJsonFilePath), "UTF-8"));
            _token = json.getString("token");
            status = json.getString("status");
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (_token == null || status == null) {
            System.err.println("Invalid json in file: " + statusJsonFilePath);
            return;
        }

        doGetCheck();

        //save get result
        taskResult = new Random().nextDouble() + "";

        String json = "{\"status\": \"" + status + "\", \"token\": \"" + _token + "\", \"get\": \"" + taskResult + "\"}";

        if (!isUrlValid(getExecutionResultDir())) {
            System.err.println("*Execution result directory must be set. Skipping...");
            return;
        }

        File file = getResultFullPath();

        try {
            FileUtils.write(file, json, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Done with 'GET' for token: " + _token + " task: " + getTaskID() + ". Results saved at: " + file.getPath());
    }

    private void doGetCheck() {
        System.out.println("Performing a get check for task: " + getTaskID());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String getExecutionKey() {
        return statusJsonFilePath;
    }


}
