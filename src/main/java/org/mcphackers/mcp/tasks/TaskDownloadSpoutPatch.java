package org.mcphackers.mcp.tasks;

import static org.mcphackers.mcp.MCPPaths.PATCH;
import static org.mcphackers.mcp.MCPPaths.PATCHDIR;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.mcphackers.mcp.MCP;
import org.mcphackers.mcp.MCPPaths;
import org.mcphackers.mcp.tools.FileUtil;

public class TaskDownloadSpoutPatch extends TaskStaged {
	private static final String PATCH_ZIP_URL = "https://github.com/RedstoneWizard08/Grease/archive/refs/heads/main.zip";
	private static final String PATCH_PATH_PREFIX = "Grease-main/patches/";

	public TaskDownloadSpoutPatch(Side side, MCP instance) {
		super(side, instance);
	}

	@Override
	protected Stage[] setStages() {
		final Path patchesPath = MCPPaths.get(mcp, PATCHDIR, side);
		final Path zipFilePath = patchesPath.resolve("spoutcraft-patches.zip");
		final Path patchFolderPath = patchesPath.resolve("spoutcraft-patches");

		return new Stage[] {
			stage(getLocalizedStage("download", "Spoutcraft's patch file"), () -> {
				patchesPath.toFile().mkdirs();
				// FileUtil.downloadFile("https://raw.githubusercontent.com/ReSpouted/Grease/main/client.patch", patchesPath.resolve("client.patch"));
				FileUtil.downloadFile(PATCH_ZIP_URL, zipFilePath);
			}),
			stage(getLocalizedStage("extract", "Spoutcraft's patch file"), () -> {
				ZipInputStream zip = new ZipInputStream(new FileInputStream(zipFilePath.toFile()));
				ZipEntry entry = zip.getNextEntry();
				byte[] buf = new byte[1024];

				while (entry != null) {
					if (!entry.isDirectory() && entry.getName().endsWith(".patch")) {
						Path outputPath = patchFolderPath.resolve(entry.getName().replaceFirst(PATCH_PATH_PREFIX, ""));

						outputPath.getParent().toFile().mkdirs();

						FileOutputStream fos = new FileOutputStream(outputPath.toFile());
						int len;
        				
						while ((len = zip.read(buf)) > 0) {
            				fos.write(buf, 0, len);
        				}
        				
						fos.close();
					}

					entry = zip.getNextEntry();
				}

				zip.closeEntry();
				zip.close();
			})
		};
	}
}
