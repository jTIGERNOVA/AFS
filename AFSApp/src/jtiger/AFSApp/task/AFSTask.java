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

    private static final List<TaskKey> runningTasks = Collections.synchronizedList(new ArrayList<TaskKey>(100));
    private static final Object tasksLock = new Object();
    private static ExecutorService executor;

    private final String taskID;
    private final String executionID;
    protected String _token;
    protected String _resultDir;
    protected String _executionResultDir;
    private String endpoint;

    public AFSTask(String executionID, String endpoint) {
        if (executionID == null)
            throw new IllegalArgumentException("executionID");
        if (endpoint == null)
            throw new IllegalArgumentException("endpoint");

        this.executionID = executionID;
        this.endpoint = endpoint;
        this.taskID = executionID + "-" + String.valueOf(hashCode());
        //temp file path
        setResultDir(System.getProperty("user.home") + File.separator + ".jTIGERNOVA" + File.separator + "AFS");
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

    public static boolean doesTaskWithExecutionIDExist(String exeID) {
        if (exeID == null)
            return false;

        boolean found = false;
        synchronized (tasksLock) {
            for (TaskKey t : runningTasks) {
                if (t.executionID.equals(exeID)) {
                    found = true;
                    break;
                }
            }
        }

        return found;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public boolean runTask() {
        //check for any tasks that are running for this token, right now

        if (executor == null) {
            System.err.println("Can not run task without executor.");
            return false;
        }

        if (executor.isShutdown() || executor.isTerminated()) {
            System.err.println("Executor is inactive. Could not finish task");
            return false;
        }

        executor.execute(new Runnable() {
            @Override
            public void run() {
                String exeKey = getExecutionKey();

                if (isExecutionKeyInTask()) {
                    System.err.println(String.format("Task with %s is already running. Skipping for now...", exeKey));
                    return;
                }

                addTaskReference();

                doRun();

                removeTaskReference();
            }
        });


        return true;
    }

    private void addTaskReference() {
        synchronized (tasksLock) {
            runningTasks.add(new TaskKey(taskID, getExecutionKey(), executionID));
        }
    }

    private void removeTaskReference() {
        synchronized (tasksLock) {
            runningTasks.remove(new TaskKey(taskID, getExecutionKey(), executionID));
        }
    }

    protected abstract void doRun();

    protected abstract String getExecutionKey();

    protected abstract String getTaskSuffix();

    protected File getResultFullPath() {
        return new File(getExecutionResultDir(), getExecutionID() + "-" + getTaskSuffix() + ".json");
    }

    public String getResultDir() {
        return _resultDir;
    }

    public void setResultDir(String resultDir) {
        if (resultDir == null)
            return;

        if (!resultDir.endsWith(File.separator))
            resultDir += File.separator;

        _resultDir = resultDir;
        _executionResultDir = _resultDir + "0000";
    }

    public String getExecutionResultDir() {
        return _executionResultDir;
    }

    public void setExecutionResultDir(String executionResultDir) {
        this._executionResultDir = executionResultDir;
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

    public boolean isExecutionKeyInTask() {
        boolean result = false;

        synchronized (tasksLock) {
            for (TaskKey t : runningTasks) {
                if (t.executionKey.equals(getExecutionKey())) {
                    result = true;
                    break;
                }
            }
        }

        return result;
    }

    private class TaskKey {
        private String taskID;
        private String executionKey;
        private String executionID;

        private TaskKey(String taskID, String executionKey, String executionID) {
            if (taskID == null || executionID == null || executionKey == null)
                throw new IllegalArgumentException();

            this.taskID = taskID;
            this.executionKey = executionKey;
            this.executionID = executionID;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TaskKey taskKey = (TaskKey) o;

            if (!taskID.equals(taskKey.taskID)) return false;
            if (!executionKey.equals(taskKey.executionKey)) return false;
            return executionID.equals(taskKey.executionID);

        }

        @Override
        public int hashCode() {
            int result = taskID.hashCode();
            result = 31 * result + executionKey.hashCode();
            result = 31 * result + executionID.hashCode();
            return result;
        }
    }
}
