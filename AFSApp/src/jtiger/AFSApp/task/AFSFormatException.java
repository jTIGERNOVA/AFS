package jtiger.AFSApp.task;

/**
 * Created by antonioj on 2/15/2017.
 */
public class AFSFormatException extends RuntimeException {
    public AFSFormatException(String taskName, String suggestion) {
        super("No valid file format found for: " + taskName + ". " + suggestion);
    }
}
