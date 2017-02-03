package jtiger.AFSApp.task;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Created by antonioj on 2/2/2017.
 */
public abstract class AFSTask {

    private static final List<String> runningTasks = Collections.synchronizedList(new ArrayList<String>(100));
    private static ExecutorService executor;
    private final String taskID;
    private final String executionID;
    protected String _token;
    protected String _resultDir = "C:/temp6/cmd/";

    public AFSTask(String executionID) {
        if (executionID == null)
            throw new IllegalArgumentException("executionID");

        this.executionID = executionID;
        this.taskID = executionID + "-" + String.valueOf(hashCode());
    }

    public static ExecutorService getExecutor() {
        return executor;
    }

    public static void setExecutor(ExecutorService executor) {
        AFSTask.executor = executor;
    }

    protected static boolean isFile(String path) {
        return (path != null) && new File(path).exists();
    }

    protected static boolean isUrlValid(String path) {
        return (path != null) && path.trim().length() > 0;
    }

    public boolean runTask() {
        //check for any tasks that are running for this token, right now

        if (executor == null) {
            System.err.println("Can not run task without executor.");
            return false;
        }

        executor.execute(new Runnable() {
            @Override
            public void run() {
                String exeKey = getExecutionKey();

                if (runningTasks.contains(exeKey)) {
                    System.err.println(String.format("Task with %s is already running. Skipping for now...", exeKey));
                    return;
                }

                runningTasks.add(exeKey);

                doRun();

                runningTasks.remove(exeKey);
            }
        });


        return true;
    }

    protected abstract void doRun();

    protected abstract String getExecutionKey();

    public String getResultDir() {
        return _resultDir;
    }

    public void setResultDir(String resultDir) {
        _resultDir = resultDir;
    }

    public String getTaskID() {
        return taskID;
    }

    public String getExecutionID() {
        return executionID;
    }

    public String getToken() {
        return _token;
    }

    public void setToken(String token) {
        this._token = token;
    }
}
