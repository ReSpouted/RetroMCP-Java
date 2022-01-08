package org.mcphackers.mcp.tasks;

import org.mcphackers.mcp.MCPConfig;
import org.mcphackers.mcp.tasks.info.TaskInfo;
import org.mcphackers.mcp.tools.ProgressInfo;
import org.mcphackers.mcp.tools.Util;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;

public class TaskRecompile extends Task {
	private int total;
	private int progress;

    public TaskRecompile(int side, TaskInfo info) {
        super(side, info);
        this.total = 1;
        this.progress = 0;
	}

	@Override
    public void doTask() throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> ds = new DiagnosticCollector<>();

        Path binPath = side == 1 ? Util.getPath(MCPConfig.SERVER_BIN) 		: Util.getPath(MCPConfig.CLIENT_BIN);
        Path srcPath = side == 1 ? Util.getPath(MCPConfig.SERVER_SOURCES) 	: Util.getPath(MCPConfig.CLIENT_SOURCES);

        step();
        if (Files.exists(binPath)) {
            Util.deleteDirectory(binPath);
        }

        Files.createDirectories(binPath);

        // Compile side
        if (Files.exists(srcPath)) {
            Iterable<File> src = Files.walk(srcPath).filter(path -> !Files.isDirectory(path) && path.getFileName().toString().endsWith(".java")).map(Path::toFile).collect(Collectors.toList());
            Iterable<String> options = Arrays.asList("-d", MCPConfig.CLIENT_BIN, "-cp", String.join(";", new String[] {MCPConfig.CLIENT, MCPConfig.LWJGL, MCPConfig.LWJGL_UTIL, MCPConfig.JINPUT}));
            if(side == 1) {
            	options = Arrays.asList("-d", MCPConfig.SERVER_BIN, "-cp", String.join(";", MCPConfig.SERVER));
            }
            recompile(compiler, ds, src, options);
            // Copy assets from source folder
            Iterable<Path> assets = Files.walk(srcPath).filter(path -> !Files.isDirectory(path) && !path.getFileName().toString().endsWith(".java")).collect(Collectors.toList());
            for(Path path : assets) {
            	if(srcPath.relativize(path).getParent() != null) {
            		Files.createDirectories(Paths.get(binPath.toString(), srcPath.relativize(path).getParent().toString()));
            	}
            	Files.copy(path, Paths.get(binPath.toString(), srcPath.relativize(path).toString()));
            }
        } else {
        	throw new IOException((side == 1 ? "Server" : "Client") + " sources not found!");
        }
    }

	public void recompile(JavaCompiler compiler, DiagnosticCollector<JavaFileObject> ds, Iterable<File> src, Iterable<String> recompileOptions) throws IOException, RuntimeException {
        StandardJavaFileManager mgr = compiler.getStandardFileManager(ds, null, null);
        Iterable<? extends JavaFileObject> sources = mgr.getJavaFileObjectsFromFiles(src);
        JavaCompiler.CompilationTask task = compiler.getTask(null, mgr, ds, recompileOptions, null, sources);
        mgr.close();
        boolean success = task.call();
        for (Diagnostic<? extends JavaFileObject> diagnostic : ds.getDiagnostics())
        	if(diagnostic.getKind() == Diagnostic.Kind.ERROR || diagnostic.getKind() == Diagnostic.Kind.WARNING) {
        		String kind = diagnostic.getKind() == Diagnostic.Kind.ERROR ? "Error" : "Warning";
            	info.addInfo(kind + String.format(" on line %d in %s%n%s%n",
                                  		diagnostic.getLineNumber(),
                                  		diagnostic.getSource().getName(),
                                  		diagnostic.getMessage(null)));
        	}
        if (!success) {
            throw new RuntimeException("Compilation error!");
        }
    }

    @Override
    public ProgressInfo getProgress() {
    	//TODO: Progress values stay at 0 and 1. Add a way to monitor progress of compilation.
        if(step == 1) {
        	return new ProgressInfo("Recompiling...", progress, total);
        }
        return super.getProgress();
    }
}
