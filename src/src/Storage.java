import java.io.*;
import java.util.*;

/**
 * Created by yujian on 5/27/17.
 */
public class Storage {
    private BufferedWriter fileWrite;
    private String fileName;
    private File file;

    public boolean storeNewValue(LogEntry newValue) {
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
            fileWrite = new BufferedWriter(new FileWriter(fileName, true));
        }catch (IOException exception) {
        }
    }

}
