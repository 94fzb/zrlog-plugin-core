package com.fzb.zrlog.plugin.common;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoggerUtil {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    private static final String LOG_FOLDER_NAME = "logs";

    private static final String LOG_FILE_SUFFIX = ".log";

    private static FileHandler fileHandler;

    private static Logger LOGGER;

    static {
        try {
            fileHandler = new FileHandler(getLogFilePath(), true);
            fileHandler.setFormatter(new SimpleFormatter());
            LOGGER = LoggerUtil.getLogger(LoggerUtil.class);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "", e);
        }
    }

    private LoggerUtil() {
    }

    public static Logger getLogger(Class clazz) {
        Logger logger = Logger.getLogger(clazz.getName());
        try {
            logger.addHandler(fileHandler);
            logger.setLevel(Level.ALL);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, recordStackTraceMsg(e));
        }
        return logger;
    }

    private synchronized static String getLogFilePath() {
        StringBuilder logFilePath = new StringBuilder();
        logFilePath.append(PathKit.getRootPath());
        logFilePath.append(File.separatorChar);
        logFilePath.append(LOG_FOLDER_NAME);

        File file = new File(logFilePath.toString());
        if (!file.exists())
            file.mkdir();

        logFilePath.append(File.separatorChar);
        logFilePath.append(sdf.format(new Date()));
        logFilePath.append(LOG_FILE_SUFFIX);
        return logFilePath.toString().replace("\\", "/");
    }

    /**
     * 记录完善的异常日志信息(包括堆栈信息)
     *
     * @param e
     */
    public static String recordStackTraceMsg(Exception e) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        e.printStackTrace(writer);
        StringBuffer buffer = stringWriter.getBuffer();
        return buffer.toString();
    }
}