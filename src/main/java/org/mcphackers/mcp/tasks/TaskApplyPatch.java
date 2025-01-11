package org.mcphackers.mcp.tasks;

import static org.mcphackers.mcp.MCPPaths.PATCHDIR;
import static org.mcphackers.mcp.MCPPaths.SOURCE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import codechicken.diffpatch.PatchOperation;
import codechicken.diffpatch.util.PatchMode;
import org.mcphackers.mcp.MCP;
import org.mcphackers.mcp.MCPPaths;

public class TaskApplyPatch extends TaskStaged {
	private int complete = 0;

	public TaskApplyPatch(Side side, MCP instance) {
		super(side, instance);
	}

	@Override
	protected Stage[] setStages() {
		return new Stage[] {
				stage(getLocalizedStage("patching"), () -> {
					final Path patchesRoot = MCPPaths.get(mcp, PATCHDIR, side);
					final Path srcPath = MCPPaths.get(mcp, SOURCE, side);

					try (Stream<Path> walker = Files.walk(patchesRoot)) {
						List<Path> files = walker.filter((path) -> path.toFile().isFile() && path.toString().endsWith(".patch")).collect(Collectors.toList());
						int total = files.size();

						// We can't use parallel streams here, since some patches may modify multiple
						// files, and that would cause conflicts.

						for (Path path : files) {
							patch(this, srcPath, srcPath, path);
							complete++;
							setProgress((int) ((double) complete / (double) total * 100));
						}
					}

					// final Path patchesPath = MCPPaths.get(mcp, PATCH, side);
					// final Path srcPath = MCPPaths.get(mcp, SOURCE, side);
					// patch(this, srcPath, srcPath, patchesPath);
				})
		};
	}

	public static void patch(Task task, Path base, Path out, Path patches) throws IOException {
		ByteArrayOutputStream logger = new ByteArrayOutputStream();
		
		PatchOperation patchOperation = PatchOperation.builder()
				.basePath(base)
				.patchesPath(patches)
				.outputPath(out)
				.mode(PatchMode.OFFSET)
				.build();
		
		boolean success = patchOperation.doPatch();
		
		patchOperation.getSummary().print(new PrintStream(logger), false);

		if (!success) {
			task.addMessage(logger.toString(), Task.INFO);
			task.addMessage("Patching failed!", Task.ERROR);
		}
	}
}
