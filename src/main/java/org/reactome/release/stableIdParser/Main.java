package org.reactome.release.stableIdParser;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.reactome.release.dbModel.StableIdentifierHistory;
import org.reactome.release.history.StableIdHistoryCreator;

public class Main {
	public static void main(String[] args) throws IOException {
		Map<String, List<StableIdEntity>> stableIdToStableIdEntities =
			StableIdEntity.getStableIdEntitiesByStableIdentifier(getFilePathsAsStrings());

		//stableIdToStableIdEntities.get("R-HSA-1236834").stream()

		Map<String, List<StableIdentifierHistory>> stableIdHistoryMap =
			StableIdHistoryCreator.createStableIdHistoryMap(stableIdToStableIdEntities);
	}

	private static List<String> getFilePathsAsStrings() throws IOException {
		ClassLoader classLoader = Main.class.getClassLoader();
		URL stableIdJsonFolder = classLoader.getResource("stableIdJson");
		String path = Objects.requireNonNull(stableIdJsonFolder).getPath();
		return Files.list(Paths.get(path))
			.filter(Files::isRegularFile)
			.map(filePath -> filePath.getFileName().toString())
			.collect(Collectors.toList());
	}
}
