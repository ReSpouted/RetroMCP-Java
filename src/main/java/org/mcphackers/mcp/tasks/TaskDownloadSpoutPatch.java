package org.mcphackers.mcp.tasks;

import static org.mcphackers.mcp.MCPPaths.PATCH;
import static org.mcphackers.mcp.MCPPaths.SOURCE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;

import codechicken.diffpatch.PatchOperation;
import codechicken.diffpatch.util.PatchMode;
import org.mcphackers.mcp.MCP;
import org.mcphackers.mcp.MCPPaths;
import org.mcphackers.mcp.tools.FileUtil;

public class TaskDownloadSpoutPatch extends TaskStaged {

	public TaskDownloadSpoutPatch(Side side, MCP instance) {
		super(side, instance);
	}

	@Override
	protected Stage[] setStages() {
		return new Stage[] {
				stage(getLocalizedStage("download", "Spoutcraft's patch file"), () -> {
					final Path patchesPath = MCPPaths.get(mcp, PATCH, side);
					patchesPath.toFile().mkdirs();
					FileUtil.downloadFile("https://raw.githubusercontent.com/ReSpouted/Grease/main/client.patch", patchesPath.resolve("client.patch"));
				})
		};
	}

}
