package net.vjdv.baz.om2.models;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.extern.java.Log;
import net.vjdv.baz.exceptions.GitException;

/**
 *
 * @author B187926
 */
@Log(topic = "git")
public class Git {

    private final AtomicInteger outputController = new AtomicInteger(0);
    private final SimpleStringProperty output = new SimpleStringProperty("");
    private final String[] outputs = new String[100];
    private final String root;
    private final Path rootPath;

    public Git(String root) {
        this.root = root;
        rootPath = Paths.get(this.root);
        try {
            FileHandler fh = new FileHandler(Paths.get("").resolve("git_" + LocalDate.now() + ".log").toString(), true);
            log.addHandler(fh);
        } catch (IOException | SecurityException ex) {
            log.log(Level.WARNING, "No fue posible crear fileHandler para log", ex);
        }
    }

    public void init() throws IOException {
        log.log(Level.INFO, "git init {0}", this.root);
        Process p = new ProcessBuilder("git", "init", ".").directory(rootPath.toFile()).redirectErrorStream(true).start();
        String str = readProcess(p);
        if (!str.contains("Initialized") && str.contains("Reinitialized")) {
            throw new GitException(str);
        }
    }

    public boolean addOrigin(String url) throws IOException {
        Process p = new ProcessBuilder("git", "remote", "add", "origin", url).directory(rootPath.toFile()).redirectErrorStream(true).start();
        String str = readProcess(p);
        return str.contains("OK");
    }

    public String clone(String url) throws IOException {
        log.log(Level.INFO, "git clone {0}", url);
        appendOutput("git clone " + url);
        Process p = new ProcessBuilder("git", "clone", url, ".").directory(rootPath.toFile()).redirectErrorStream(true).start();
        return readProcess(p);
    }

    public String[] status() throws IOException {
        appendOutput("status");
        Process p = new ProcessBuilder("git", "ls-files", "--other", "--modified", "--exclude-standard").directory(rootPath.toFile()).redirectErrorStream(true).start();
        return readProcess(p).split("\n");
    }

    public String pull() throws IOException {
        appendOutput("pull");
        Process p = new ProcessBuilder("git", "pull").directory(rootPath.toFile()).redirectErrorStream(true).start();
        return readProcess(p);
    }

    public void push() throws IOException {
        appendOutput("push");
        Process p = new ProcessBuilder("git", "push").directory(rootPath.toFile()).redirectErrorStream(true).start();
        readProcess(p);
    }

    public void add(String path) throws IOException {
        appendOutput("push");
        executeGit("add", path);
    }

    public void commit(String msg) throws IOException {
        appendOutput("commit");
        executeGit("commit", "-m", msg);
    }

    public void addAndCommit(String path, String msg) throws IOException {
        add(path);
        commit(msg);
    }

    private String executeGit(String... args) throws IOException {
        log.info(String.join(" ", args));
        String[] args2 = new String[args.length + 1];
        args2[0] = "git";
        for (int i = 1, j = 0; i < args2.length; i++, j++) {
            args2[i] = args[j];
        }
        Process p = new ProcessBuilder(args2).directory(rootPath.toFile()).redirectErrorStream(true).start();
        String str = readProcess(p);
        log.info(str);
        return str;
    }

    private String readProcess(Process process) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            appendOutput(line.trim());
            sb.append(sb).append("\n");
            System.out.println(line.trim());
        }
        String str = sb.toString();
        log.info(str);
        return str;
    }

    private void appendOutput(String str) {
        int i = outputController.getAndIncrement() % 100;
        outputs[i] = str;
        StringBuilder sb = new StringBuilder();
        for (int j = i + 1; j < 100; j++) {
            if (outputs[j] != null) {
                sb.append(outputs[j]).append("\n");
            }
        }
        for (int j = 0; j <= i; j++) {
            sb.append(outputs[j]).append("\n");
        }
        output.set(sb.toString());
    }

    public StringProperty outputProperty() {
        return output;
    }

    public String getOutput() {
        return output.get();
    }

}
