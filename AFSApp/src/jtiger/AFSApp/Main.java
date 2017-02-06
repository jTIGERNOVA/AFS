package jtiger.AFSApp;

import jtiger.AFSApp.task.AFSPost;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    private static final ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(10);

    static {
        AFSPost.setExecutor(threadPoolExecutor);
    }

    public static void main(String[] args) {

        int count = incrementRunCount();
        String resultPath = "C:/temp6/cmd";
        String executionID = String.format("%04d", count);

        AFSPost post1 = new AFSPost(executionID, "C:/temp/a.txt", "http://gmail.com", true);
        AFSPost post2 = new AFSPost(executionID, "C:/temp/a2.txt", "http://gmail.com", true);
        AFSPost post3 = new AFSPost(executionID, "C:/temp/a3.txt", "http://gmail.com", true);
        AFSPost post4 = new AFSPost(executionID, "C:/temp/a4.txt", "http://gmail.com", true);
        AFSPost post5 = new AFSPost(executionID, "C:/temp/a5.txt", "http://gmail.com", true);

        post1.setResultDir(resultPath);
        post2.setResultDir(resultPath);
        post3.setResultDir(resultPath);
        post4.setResultDir(resultPath);
        post5.setResultDir(resultPath);

        post1.runTask();
        post2.runTask();
        post3.runTask();
        post4.runTask();
        post5.runTask();

        //threadPoolExecutor.shutdown();
    }

    private static int incrementRunCount() {
        String preferencesPath = System.getProperty("user.home") + File.separator + ".jTIGERNOVA" + File.separator + "AFS.settings";

        int count = 1;

        try {
            if (new File(preferencesPath).exists()) {
                JSONObject pref = new JSONObject(FileUtils.readFileToString(new File(preferencesPath), "UTF-8"));

                if (pref.has("runCount")) {
                    count = pref.getInt("runCount");
                }

                pref.put("runCount", count + 1);
                FileUtils.write(new File(preferencesPath), pref.toString(), "UTF-8");
            } else {
                JSONObject pref = new JSONObject();

                pref.put("runCount", count + 1);

                FileUtils.write(new File(preferencesPath), pref.toString(), "UTF-8");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return count;
    }
}
