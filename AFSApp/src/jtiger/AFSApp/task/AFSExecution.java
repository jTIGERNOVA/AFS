package jtiger.AFSApp.task;

import jtiger.AFSApp.Util;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by antonioj on 2/10/2017.
 */
public class AFSExecution {
    private static final ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(10);

    static {
        AFSTask.setExecutor(threadPoolExecutor);
    }

    private String resultPath;
    private OnExecutionIDUpdate onExecutionIDUpdate;
    private ExecutionStatusListener executionStatusListener;

    public AFSExecution(String resultPath) {
        this.resultPath = resultPath;
    }

    private static int incrementRunCount() {
        File prefFile = getPreferencesFile();

        int count = 1;

        try {
            if (prefFile.exists()) {
                JSONObject pref = new JSONObject(FileUtils.readFileToString(prefFile, Util.ENCODING));

                if (pref.has("runCount")) {
                    count = pref.getInt("runCount");
                }

                pref.put("runCount", count + 1);
                FileUtils.write(prefFile, pref.toString(), Util.ENCODING);
            } else {
                JSONObject pref = new JSONObject();

                pref.put("runCount", count + 1);

                FileUtils.write(prefFile, pref.toString(), Util.ENCODING);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return count;
    }

    public static int getRunCount() {
        int count = 1;
        File prefFile = getPreferencesFile();
        try {
            if (prefFile.exists()) {
                JSONObject pref = new JSONObject(FileUtils.readFileToString(prefFile, Util.ENCODING));

                if (pref.has("runCount")) {
                    count = pref.getInt("runCount");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return count;
    }

    public static String formatExecutionID(Number number) {
        return String.format("%04d", number);
    }

    public static File getPreferencesFile() {
        return new File(System.getProperty("user.home") + File.separator + ".jTIGERNOVA" + File.separator + "AFS.settings");
    }

    public void execute(String[] files, String endpoint, AFSOption startOption, boolean autoContinue, boolean autoContinueLevelTwo)
            throws AFSFormatException {
        int count = incrementRunCount();
        final String executionID = formatExecutionID(count);

        //execution started
        executionStatusListener.onStart(executionID, files.length);

        if (onExecutionIDUpdate != null)
            onExecutionIDUpdate.update(executionID);

        for (String file : files) {
            try {
                handleFile(startOption, executionID, file, endpoint, autoContinue, autoContinueLevelTwo);
            } catch (AFSFormatException ex) {
                System.err.println("Error: " + ex.getMessage());
            }
        }

        //start a new thread to check for execution status of tasks
        threadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                boolean tasksRunning = true;
                do {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    tasksRunning = AFSTask.doesTaskWithExecutionIDExist(executionID);

                    //no more tasks for this execution
                    if (!tasksRunning) {
                        String tPath = resultPath;
                        if (!tPath.endsWith(File.separator))
                            tPath += File.separator;
                        tPath += executionID;

                        executionStatusListener.onDone(executionID, tPath);
                    }
                } while (tasksRunning);
            }
        });
    }

    private void handleFile(AFSOption startOption, String executionID, String file, String endpoint, boolean autoContinue, boolean autoContinueLevelTwo) {
        if (startOption == AFSOption.POST) {
            AFSPost task = new AFSPost(executionID, file, endpoint, autoContinue, autoContinueLevelTwo);
            task.setResultDir(resultPath);
            task.runTask();
        } else if (startOption == AFSOption.GETSTATUS) {
            AFSGetStatus task = new AFSGetStatus(executionID, file, endpoint, autoContinue);
            task.setResultDir(resultPath);
            task.runTask();
        } else if (startOption == AFSOption.GETRESULT) {
            AFSGetResult task = new AFSGetResult(executionID, file, endpoint);
            task.setResultDir(resultPath);
            task.runTask();
        }
    }

    public OnExecutionIDUpdate getOnExecutionIDUpdate() {
        return onExecutionIDUpdate;
    }

    public void setOnExecutionIDUpdate(OnExecutionIDUpdate onExecutionIDUpdate) {
        this.onExecutionIDUpdate = onExecutionIDUpdate;
    }

    public ExecutionStatusListener getExecutionStatusListener() {
        return executionStatusListener;
    }

    public void setExecutionStatusListener(ExecutionStatusListener executionStatusListener) {
        this.executionStatusListener = executionStatusListener;
    }

    public String getResultPath() {
        return resultPath;
    }

    public void setResultPath(String resultPath) {
        this.resultPath = resultPath;
    }

    public void exit() {
        AFSTask.getExecutor().shutdown();
    }

    public enum AFSOption {
        POST, GETSTATUS, GETRESULT
    }

    public interface OnExecutionIDUpdate {
        void update(String newExecutionID);
    }

    public interface ExecutionStatusListener {
        void onStart(String executionID, int fileCount);

        void onDone(String executionID, String resultsDirectory);
    }
}

