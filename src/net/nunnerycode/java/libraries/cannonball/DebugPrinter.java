package net.nunnerycode.java.libraries.cannonball;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.logging.Level;

public class DebugPrinter {

    private File debugFolder;
    private File debugFile;

    public DebugPrinter(File file) {
        this(file.getParentFile(), file);
    }

    public DebugPrinter(String folderPath, String fileName) {
        this(new File(folderPath), new File(folderPath, fileName));
    }

    public DebugPrinter(File folder, File file) {
        if (!folder.exists() && !folder.mkdirs() || !folder.isDirectory()) {
            return;
        }
        this.debugFolder = folder;
        this.debugFile = file;
    }

    public static void debug(File file, Level level, String... messages) {
        if (file == null) {
            throw new IllegalArgumentException("file cannot be null");
        }
        if (level == null) {
            throw new IllegalArgumentException("level cannot be null");
        }
        try {
            if (!file.exists() && !file.createNewFile()) {
                return;
            }
            FileWriter fw = new FileWriter(file.getPath(), true);
            PrintWriter pw = new PrintWriter(fw);
            for (String message : messages) {
                pw.println("[" + level.getName() + "] " + Calendar.getInstance().getTime().toString() + " | "
                           + message);
            }
            pw.flush();
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void debug(Level level, String... messages) {
        try {
            if (!getDebugFolder().exists() && !getDebugFolder().mkdirs()) {
                return;
            }
            File saveTo = getDebugFile();
            if (!saveTo.exists() && !saveTo.createNewFile()) {
                return;
            }
            FileWriter fw = new FileWriter(saveTo.getPath(), true);
            PrintWriter pw = new PrintWriter(fw);
            for (String message : messages) {
                pw.println("[" + level.getName() + "] " + Calendar.getInstance().getTime().toString() + " | "
                           + message);
            }
            pw.flush();
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void debug(String... messages) {
        debug(Level.INFO, messages);
    }

    public File getDebugFolder() {
        return debugFolder;
    }

    public File getDebugFile() {
        return debugFile;
    }
}