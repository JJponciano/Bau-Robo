package BauRobo.models.ply;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Lisa Mosis
 */
public class MyLogManager {

    String logpath = "log.txt";
    final int maxFilesize;

    /**
     * generates a new log-file.
     */
    public MyLogManager(int maxFilesize) {
        this.maxFilesize = maxFilesize;

        try {
            String txt = "timestamp type change undo\n";
            FileWriter myWriter = new FileWriter(logpath);
            myWriter.write(txt);
            myWriter.close();
        } catch (IOException e) {
            System.out.println("Error while trying to write the log file.");
            e.printStackTrace();
        }
    }

    /**
     * writes an entry in the log-file.
     * writeLog(timestamp + "e "+ error) for an error or
     * writeLog(timestamp + "f "+ function + " " + undoFunction) for a function
     */
    public void writeLog(String logtxt){
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy_hhmmss");
        Date curDate = new Date();
        String timestamp = sdf.format(curDate);

        try {
            FileWriter myWriter = new FileWriter(logpath,true);
            myWriter.write(timestamp + " " + logtxt + "\n");
            myWriter.close();
        } catch (IOException e) {
            System.out.println("Error while trying to write in the log file.");
            e.printStackTrace();
        }

        int numOfLines = countLines();
        if(numOfLines > maxFilesize && numOfLines != -1)
            reduceLogSize(numOfLines - maxFilesize);
    }

    /**
     * reads the last log line.
     * @return the last line
     */
    public String readLastLog(){
        String lastLine = "Log file is empty.";
        try {
            FileReader fileReader = new FileReader(logpath);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line = bufferedReader.readLine();//first line is only the header
            while (line != null){
                line = bufferedReader.readLine();
                if(line != null)
                    lastLine = line;
            }
            bufferedReader.close();
        }
        catch(Exception e){
            System.out.println("Error while trying to read the log file.");
            e.printStackTrace();
        }
        return lastLine;
    }

    /**
     * deletes the last line of the log-file.
     * @param numberOfLines how many lines to delete
     */
    public void deleteLastLog(int numberOfLines){
        for(int i = 0; i < numberOfLines;i++) {
            try {
                byte b;
                RandomAccessFile f = new RandomAccessFile(logpath, "rw");
                long length = f.length() - 1;
                do {
                    length -= 1;
                    f.seek(length);
                    b = f.readByte();
                } while (b != 10 && length > 0);
                if (length == 0) {
                    f.setLength(length);
                } else {
                    f.setLength(length + 1);
                }
            } catch (Exception e) {
                System.out.println("Error while trying to delete lines in the log file.");
                e.printStackTrace();
            }
        }
    }

    /**
     * deletes the first lines (not counting the header) of the log-file.
     * @param numOfLines how many lines to delete
     */
    public void reduceLogSize(int numOfLines){
        try {
            File inputFile = new File(logpath);
            File tempFile = new File("tempFile.txt");

            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

            String currentLine;
            int i = 0;
            while((currentLine = reader.readLine()) != null) {
                if(i > numOfLines || i == 0) {
                    writer.write(currentLine + System.getProperty("line.separator"));
                }
                i++;
            }
            writer.close();
            reader.close();
            boolean deleted = inputFile.delete();
            boolean successful = tempFile.renameTo(inputFile);
            if(!deleted || !successful)
                throw new Exception("Problem while changing the log-file content");

        } catch (Exception e) {
            System.out.println("Error while trying to reduce the log file.");
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * counts the lines of the log-file.
     * @return number of lines
     */
    public int countLines() {
        int count = 0;
        boolean empty = true;
        try {
            InputStream is = new BufferedInputStream(new FileInputStream(logpath));
            try {
                byte[] c = new byte[1024];
                int readChars;
                while ((readChars = is.read(c)) != -1) {
                    empty = false;
                    for (int i = 0; i < readChars; ++i) {
                        if (c[i] == '\n') {
                            ++count;
                        }
                    }
                }
            } finally {
                is.close();
            }

        } catch (IOException e) {
                System.out.println("Error while trying to get the size of the log file.");
                e.printStackTrace();
        }
        return (count == 0 && !empty) ? -1 : count;
    }

}