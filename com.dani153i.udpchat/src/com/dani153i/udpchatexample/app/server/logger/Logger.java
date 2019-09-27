package com.dani153i.udpchatexample.app.server.logger;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A file event logger.
 * @author daniel blom
 * @version 1.0
 */
public class Logger
{
    private final String filePath;
    private final String fileName;
    //private PrintWriter outputWriter;

    public Logger(final String filePath, final String fileName) throws IOException {
        this.filePath = filePath;
        this.fileName = fileName + ".txt";
    }

    public void log(final String message) throws IOException {
        Date timestamp = new Date();

        try(FileWriter fw = new FileWriter(filePath + "/" + fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            out.println(new SimpleDateFormat("EEE MMM dd, yyyy - HH:mm:ss").format(new Date()) + " : " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
