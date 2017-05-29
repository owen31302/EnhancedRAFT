import java.io.*;
import java.nio.Buffer;
import java.util.*;

/**
 * Created by yujian on 5/27/17.
 */
public class Storage {
    private FileWriter fileWrite;
    private String fileName;
    private File file;



    public boolean storeNewValue(LogEntry newValue) {
        try {
            fileWrite.write(String.valueOf(newValue.getIndex()));
            fileWrite.write("\t");
            fileWrite.write(String.valueOf(newValue.getTerm()));
            fileWrite.write("\t");
            fileWrite.write(String.valueOf(newValue.getState().getStateValue()));
            fileWrite.write("\t");
            fileWrite.write(String.valueOf(newValue.getState().getStateName()));
            fileWrite.write("\n");
            fileWrite.flush();
        }catch (IOException exception) {
            // why this could happen?

            return false;
        }
        return true;
    }
    public LogEntry getLatestCommitedValue() {
        String lastLine = getLastLine();
        String[] stringArray = lastLine.split("\t");
        return new LogEntry(new State(stringArray[3], Integer.valueOf(stringArray[2])), Integer.valueOf(stringArray[1]), Integer.valueOf(stringArray[0]));
    }
    public LogEntry[] getAllCommitedValue() {
        ArrayList<String> logInFile = getAllLine();
        String[][] stringArray = new String[logInFile.size()][];
        LogEntry[] output = new LogEntry[logInFile.size()];
        for (int i = 0; i < logInFile.size(); i ++) {
            stringArray[i] = logInFile.get(i).split("\t");
            output[i] = new LogEntry(new State(stringArray[i][3], Integer.valueOf(stringArray[i][2])), Integer.valueOf(stringArray[i][1]), Integer.valueOf(stringArray[i][0]));
        }
        return output;
    }
    public boolean deleteLatestCommitedValue() {
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            byte b;
            long length = randomAccessFile.length() ;
            if (length != 0) {
                do {
                    length -= 1;
                    randomAccessFile.seek(length);
                    b = randomAccessFile.readByte();
                } while (b != 10 && length > 0);
                randomAccessFile.setLength(length);
                randomAccessFile.close();
            }
        }catch (IOException exception) {
            System.out.println("deleteLatestCommitedValue: ");
            exception.printStackTrace();
            return false;
        }
        return true;
    }


    public Storage(String valueName)  {
        fileName = "./" + valueName + "Storage.txt";
        // build the storage file
        try{
            file = new File(fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        }catch (IOException exception){
            System.out.println("open file");
            exception.printStackTrace();
        }

        // set up write, appending mode
        try {
            fileWrite = new FileWriter(file, true);
        }catch (IOException exception) {
            System.out.println("open file writer");
            exception.printStackTrace();
            System.exit(0);
        }
    }

    private ArrayList<String> getAllLine() {
        if (file == null) {
            System.out.println("file has not created yet");
            return null;
        }
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(file));
        }catch (IOException exception) {
            System.out.println("getAllLine: build buffer reader");
            exception.printStackTrace();
            return null;
        }
        ArrayList<String> allLog = new ArrayList<String>();
        String newLine;
        try {
            while ((newLine = reader.readLine()) != null) {
                allLog.add(newLine);
            }
        } catch (IOException e) {
            System.out.println("getAllLine: reading line");
            e.printStackTrace();
        }
        return allLog;
    }
    private String getLastLine() {
        RandomAccessFile fileHandler = null;
        try {
            fileHandler = new RandomAccessFile( file, "r" );
            long fileLength = fileHandler.length() - 1;
            StringBuilder sb = new StringBuilder();

            for(long filePointer = fileLength; filePointer != -1; filePointer--){
                fileHandler.seek( filePointer );
                int readByte = fileHandler.readByte();

                if( readByte == 0xA ) {
                    if( filePointer == fileLength ) {
                        continue;
                    }
                    break;
                } else if( readByte == 0xD ) {
                    if( filePointer == fileLength - 1 ) {
                        continue;
                    }
                    break;
                }
                sb.append( ( char ) readByte );
            }
            String lastLine = sb.reverse().toString();
            return lastLine;
        } catch( java.io.FileNotFoundException e ) {
            e.printStackTrace();
            return null;
        } catch( java.io.IOException e ) {
            e.printStackTrace();
            return null;
        } finally {
            if (fileHandler != null )
                try {
                    fileHandler.close();
                } catch (IOException e) {
                /* ignore */
                }
        }
    }
}
