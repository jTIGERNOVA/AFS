package jtiger.AFSApp.task;

import jtiger.AFSApp.Util;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Random;

/**
 * Created by antonioj on 2/2/2017.
 */
public class AFSPost extends AFSTask {

    private final String filePath;
    private boolean triggerGetStatus;

    public AFSPost(String executionID, String filePath, String endpoint, boolean triggerGetStatus) {
        super(executionID, endpoint);
        this.filePath = filePath;
        this.triggerGetStatus = triggerGetStatus;
    }

    @Override
    protected String getTaskSuffix() {
        return "token";
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
        File resultFilePath = getResultFullPath();

        try {
            FileUtils.write(resultFilePath, json, Util.ENCODING);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Done with 'POST' for token: " + _token + " task: " + getTaskID() + ". Results saved at: " + resultFilePath);

        if (triggerGetStatus) {
            AFSGetStatus getStatus = new AFSGetStatus(getExecutionID(), resultFilePath.getPath(), getExecutionKey(), true);
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
}
