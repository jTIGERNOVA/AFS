package jtiger.AFSApp;

import java.util.Arrays;

/**
 * Created by antonioj on 2/15/2017.
 */
public class Util {

    public static final String ENCODING = "UTF-8";

    private Util() {
    }

    public static String[] getSupportedFileExtensions() {
        return new String[]{"json", "doc", "xls", "xlsx", "csv", "ppt", "pptx", "pdf",
                "mp3", "mp4", "m4a", "wav", "avi", "mov"
        };
    }

    public static boolean isFilePostSupported(String extension) {
        if (extension != null)
            extension = extension.toLowerCase();

        return Arrays.binarySearch(getSupportedFileExtensions(), extension) != -1 && extension != "json";
    }
}
