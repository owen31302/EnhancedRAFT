import java.io.*;
import java.util.*;

/**
 * Created by yujian on 5/27/17.
 */
public class Storage {
    private FileWriter fileWrite;
    private String fileName;
    private File file;



    public boolean storeNewValue(LogEntry newValue) {
        String newStringToStore = String.valueOf(newValue.getIndex()) + String.valueOf(newValue.getTerm()) + String.valueOf(newValue.getState().getStateValue());
        try {
            fileWrite.write(newStringToStore);
            fileWrite.flush();
        }catch (IOException exception) {
            // why this could happen?

            return false;
        }
        return true;
    }
    public LogEntry getLatestValue() {
        return new LogEntry(new State("default", 0), 1, 1);
    }
    public ArrayList<LogEntry> getAllValue() {
        return new ArrayList<LogEntry>();
    }


    public Storage(String valueName) {
        fileName = "./" + valueName + "Storage.txt";
        // build the storage file
        try{
            file = new File(fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        }catch (IOException exception){
            System.out.println(exception);
        }

        // set up write, appending mode
        try {
            fileWrite = new FileWriter(file, true);
        }catch (IOException exception) {
        }
    }

}
