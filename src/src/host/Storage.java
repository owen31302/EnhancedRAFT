package host;

import java.io.*;
import java.util.*;

/**
 * Created by yujian on 5/27/17.
 */
public class Storage {
    private FileWriter logFileWrite;
    private File logFile;
    private String logFilePath = "./logFile.txt";
    private FileWriter voteFileWrite;
    private File voteFile;
    private String voteFilePath = "./vote.txt";


    /**
     * This method would write the entry into file and return true if success
     * @param newValue the entry need to be stored
     * @return the data has been successfully stored
     */
    public boolean storeNewValue(LogEntry newValue) {
        try {
            logFileWrite.write(String.valueOf(newValue.getIndex()));
            logFileWrite.write("\t\t");
            logFileWrite.write(String.valueOf(newValue.getTerm()));
            logFileWrite.write("\t\t");
            logFileWrite.write(String.valueOf(newValue.getState().getStateName()));
            logFileWrite.write("\t\t");
            logFileWrite.write(String.valueOf(newValue.getState().getStateValue()));
            logFileWrite.write("\n");
            logFileWrite.flush();
        }catch (IOException exception) {
            // why this could happen?

            return false;
        }
        return true;
    }



    /**
     * Get last committed log entry
     * @return log entry
     */
    public LogEntry getLatestCommitedValue() {
        String lastLine = getLastLine(logFilePath);
        String[] stringArray = lastLine.split("\t");
        return new LogEntry(new State(stringArray[2], Integer.valueOf(stringArray[3])), Integer.valueOf(stringArray[1]), Integer.valueOf(stringArray[0]));
    }

    /**
     * Get all committed log entry
     * @return log entry array
     */
    public LogEntry[] getAllCommitedValue() {
        ArrayList<String> logInFile = getAllLine();
        String[][] stringArray = new String[logInFile.size()][];
        LogEntry[] output = new LogEntry[logInFile.size()];
        for (int i = 1; i < logInFile.size(); i ++) {
            stringArray[i] = logInFile.get(i).split("\t\t");
            output[i] = new LogEntry(new State(stringArray[i][2], Integer.valueOf(stringArray[i][3])), Integer.valueOf(stringArray[i][1]), Integer.valueOf(stringArray[i][0]));
        }

        return output;
    }

    /**
     * Delete the last log entry in the file
     * @return sccuess
     */
    public boolean deleteLatestCommitedValue() {
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(logFile, "rw");
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


    /**
     * Store vote
     * @param currentTerm current term of host
     * @param voteTo vote to host ID
     * @return success
     */
    public boolean storeVote(int currentTerm, int voteTo) {
        try {
            voteFileWrite.write(currentTerm);
            voteFileWrite.write("\t");
            voteFileWrite.write(voteTo);
            voteFileWrite.write("\n");
        }catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * Return last vote is under term of
     * @return term number
     */
    public int lastVoteAt() {
        String lastLine = getLastLine(logFilePath);
        String[] stringArray = lastLine.split("\t");
        return Integer.valueOf(stringArray[0]);
    }

    /**
     * Return last vote to host ID
     * @return
     */
    public int lastVoteTo() {
        String lastLine = getLastLine(logFilePath);
        String[] stringArray = lastLine.split("\t");
        return Integer.valueOf(stringArray[1]);
    }


    /**
     * Initializer
     */
    Storage() {
        boolean newFile = false;
        // build the storage file
        try{
            logFile = new File(logFilePath);
            if (!logFile.exists()) {
                logFile.createNewFile();
                System.out.println("create file");
                newFile = true;
            }
        }catch (IOException exception){
            System.out.println("log file open failed");
            exception.printStackTrace();
        }

        // set up write, appending mode
        try {
            logFileWrite = new FileWriter(logFile, true);
        }catch (IOException exception) {
            System.out.println("open file writer");
            exception.printStackTrace();
            System.exit(0);
        }
        if (newFile) {
            try {
                logFileWrite.write("Index\tTerm\tVariableName\tValue\n");
            }catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        try {
            voteFile = new File(voteFilePath);
            if (!voteFile.exists()) {
                voteFile.createNewFile();
            }
        }catch (IOException e) {
            System.out.println("vote file open failed");
            e.printStackTrace();
        }
        try {
            voteFileWrite = new FileWriter(voteFile, true);
        }catch (IOException e) {
            System.out.println("vote file writer create failed");
            e.printStackTrace();
        }

    }

    private ArrayList<String> getAllLine() {
        if (logFile == null) {
            System.out.println("file has not created yet");
            return null;
        }
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(logFile));
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
    private String getLastLine(String fileName) {
        RandomAccessFile fileHandler = null;
        try {
            fileHandler = new RandomAccessFile(fileName, "r" );
            long fileLength = fileHandler.length() - 1;
            StringBuilder sb = new StringBuilder();

            for(long filePointer = fileLength; filePointer != -1; filePointer--){
                fileHandler.seek(filePointer );
                int readByte = fileHandler.readByte();

                if(readByte == 0xA) {
                    if( filePointer == fileLength ) {
                        continue;
                    }
                    break;
                } else if(readByte == 0xD) {
                    if(filePointer == fileLength - 1) {
                        continue;
                    }
                    break;
                }
                sb.append((char)readByte );
            }
            String lastLine = sb.reverse().toString();
            return lastLine;
        } catch(java.io.FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch(java.io.IOException e) {
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
