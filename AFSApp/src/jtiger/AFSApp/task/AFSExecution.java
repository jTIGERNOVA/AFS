package jtiger.AFSApp.task;

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

    public AFSExecution(String resultPath) {
        this.resultPath = resultPath;
    }

    private static int incrementRunCount() {
        File prefFile = getPreferencesFile();

        int count = 1;

        try {
            if (prefFile.exists()) {
                JSONObject pref = new JSONObject(FileUtils.readFileToString(prefFile, "UTF-8"));

                if (pref.has("runCount")) {
                    count = pref.getInt("runCount");
                }

                pref.put("runCount", count + 1);
                FileUtils.write(prefFile, pref.toString(), "UTF-8");
            } else {
                JSONObject pref = new JSONObject();

                pref.put("runCount", count + 1);

                FileUtils.write(prefFile, pref.toString(), "UTF-8");
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
                JSONObject pref = new JSONObject(FileUtils.readFileToString(prefFile, "UTF-8"));

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

    public void execute(String[] files, String endpoint, AFSOption startOption, boolean autoContinue) {
        int count = incrementRunCount();
        String executionID = formatExecutionID(count);

        if (onExecutionIDUpdate != null)
            onExecutionIDUpdate.update(executionID);

        for (String file : files) {
            if (startOption == AFSOption.POST) {
                AFSPost task = new AFSPost(executionID, file, endpoint, autoContinue);
                task.setResultDir(resultPath);
                task.runTask();
            } else if (startOption == AFSOption.GETSTATUS) {
                AFSGetStatus task = new AFSGetStatus(executionID, file, endpoint, autoContinue);
                task.setResultDir(resultPath);
                task.runTask();
            } else if (startOption == AFSOption.GETRESULT) {
                AFSGet task = new AFSGet(executionID, file, endpoint);
                task.setResultDir(resultPath);
                task.runTask();
            }
        }
    }

    public OnExecutionIDUpdate getOnExecutionIDUpdate() {
        return onExecutionIDUpdate;
    }

    public void setOnExecutionIDUpdate(OnExecutionIDUpdate onExecutionIDUpdate) {
        this.onExecutionIDUpdate = onExecutionIDUpdate;
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
}

