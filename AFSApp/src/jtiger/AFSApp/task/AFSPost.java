package jtiger.AFSApp.task;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Random;

/**
 * Created by antonioj on 2/2/2017.
 */
public class AFSPost extends AFSTask {

    private final String filePath;
    private final String postPath;
    private boolean triggerGetStatus;

    public AFSPost(String executionID, String filePath, String postPath, boolean triggerGetStatus) {
        super(executionID);
        this.filePath = filePath;
        this.postPath = postPath;
        this.triggerGetStatus = triggerGetStatus;
    }

    @Override
    protected void doRun() {
        //post file to server
        System.out.println("Posting file to server for task: " + getTaskID());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //save token
        _token = new Random().nextDouble() + "";

        String json = "{\"token\": \"" + _token + "\"}";
        setExecutionResultDir(getResultDir() + getExecutionID() + File.separator + new File(filePath).getName());
        File resultFilePath = new File(getExecutionResultDir(), getExecutionID() + "-token.json");

        try {
            FileUtils.write(resultFilePath, json, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Done with 'POST' for token: " + _token + " task: " + getTaskID() + ". Results saved at: " + resultFilePath);

        if (triggerGetStatus) {
            AFSGetStatus getStatus = new AFSGetStatus(getExecutionID(), resultFilePath.getPath(), true);
            getStatus.setExecutionResultDir(getExecutionResultDir());

            getStatus.runTask();
        }
    }

    @Override
    protected String getExecutionKey() {
        return filePath;
    }

    public boolean isTriggerGetStatus() {
        return triggerGetStatus;
    }

    public void setTriggerGetStatus(boolean triggerGetStatus) {
        this.triggerGetStatus = triggerGetStatus;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getPostPath() {
        return postPath;
    }
}
