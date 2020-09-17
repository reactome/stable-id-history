package org.reactome.release.stableIdParser;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.reactome.release.dbModel.StableIdentifierReleaseStatus;

public class StableIdEntity implements Comparable<StableIdEntity> {
	private static final Logger logger = LogManager.getLogger();
	private static final Pattern STABLE_ID_FILE_NAME_PATTERN = Pattern.compile("^stableId(\\d+).json$");

	private int releaseVersion;
	private JSONObject stableIdJSON;

	private StableIdEntity(int releaseVersion, JSONObject stableIdJSON) {
		// TODO: Add class fields and private setters for validation of data
		this.releaseVersion = releaseVersion;
		this.stableIdJSON = stableIdJSON;
	}

	public static StableIdEntity getStableIdEntity(int releaseVersion, JSONObject stableIdJSON) {
		return new StableIdEntity(releaseVersion, stableIdJSON);
	}

	@SuppressWarnings("unchecked")
	public static List<StableIdEntity> getStableIdEntities(String filePathAsString) {
		int releaseVersion = getReleaseNumberFromFileName(Paths.get(filePathAsString).getFileName().toString());

		JSONObject stableIdJSONMap;
		try {
			stableIdJSONMap = StableIdJsonParser.parseStableIdJSONFile(filePathAsString);
		} catch (ParseException | IOException e) {
			logger.error("Unable to parse stable identifier data from " + filePathAsString, e);
			return new ArrayList<>();
		}

		return (List<StableIdEntity>) stableIdJSONMap
			.values()
			.stream()
			.map(stableIdJSON -> getStableIdEntity(releaseVersion, (JSONObject) stableIdJSON))
			.collect(toList());
	}

	public static List<StableIdEntity> getAllStableIdEntities(List<String> filePathsAsStrings) {
		return filePathsAsStrings
			.stream()
			.flatMap(filePathsAsString -> getStableIdEntities(filePathsAsString).stream())
			.collect(toList());
	}

	public static Map<String, List<StableIdEntity>> getStableIdEntitiesByStableIdentifier(List<String> filePathsAsStrings) {
		return getAllStableIdEntities(filePathsAsStrings)
			.stream()
			.collect(
				toMap(
					StableIdEntity::getStableId,
					stableIdEntity -> Arrays.asList(stableIdEntity),
					(currentStableIdEntityList, newStableIdEntityList) -> combineLists(
						currentStableIdEntityList, newStableIdEntityList
					)
				)
			);
	}

	public static List<StableIdEntity> getAllStableIdEntitiesForStableIdentifier(
		Map<String, List<StableIdEntity>> stableIdMap, String stableId
	) {
		if (stableId.matches("^R-\\w{3}-\\d+(-\\d+)?$")) {
			return getAllStableIdEntitiesForNewStableIdentifier(stableIdMap, stableId);
		} else if (stableId.matches("^REACT_\\d+(-\\d+)?$")) {
			return getAllStableIdEntitiesForOldStableIdentifier(stableIdMap, stableId);
		} else {
			throw new RuntimeException(stableId + " does not have a recognized pattern");
		}
	}

	private static List<StableIdEntity> getAllStableIdEntitiesForOldStableIdentifier(
		Map<String, List<StableIdEntity>> stableIdMap, String oldStableId
	) {
		List<StableIdEntity> stableIdEntitiesForOldStableIdentifier = new ArrayList<>();
		stableIdEntitiesForOldStableIdentifier.addAll(stableIdMap.get(oldStableId));
		stableIdEntitiesForOldStableIdentifier.addAll(
			stableIdMap.values()
			.stream()
			.flatMap(Collection::stream)
			.filter(stableIdEntity -> stableIdEntity.getOldStableId().equals(oldStableId))
			.collect(toList())
		);
		return stableIdEntitiesForOldStableIdentifier;
	}

	private static List<StableIdEntity> getAllStableIdEntitiesForNewStableIdentifier(
		Map<String, List<StableIdEntity>> stableIdMap, String newStableId
	) {
		List<StableIdEntity> stableIdEntitiesForNewStableIdentifier = new ArrayList<>();
		stableIdEntitiesForNewStableIdentifier.addAll(stableIdMap.get(newStableId));

		stableIdEntitiesForNewStableIdentifier.addAll(
			getOldStableIdentifiersForNewStableIdentifier(stableIdMap, newStableId)
				.stream()
				.flatMap(oldStableId -> stableIdMap.get(oldStableId).stream())
				.collect(toList())
		);

		return stableIdEntitiesForNewStableIdentifier;
	}

	private static List<String> getOldStableIdentifiersForNewStableIdentifier(
		Map<String, List<StableIdEntity>> stableIdMap, String newStableId
	) {
		return stableIdMap.get(newStableId)
			.stream()
			.map(StableIdEntity::getOldStableId)
			.collect(toList());
	}


	private static List<StableIdEntity> combineLists(List<StableIdEntity> ...lists) {
		List<StableIdEntity> combinedLists = new ArrayList<>();
		for (List<StableIdEntity> list : lists) {
			combinedLists.addAll(list);
		}
		return combinedLists;
	}

	public static List<InstanceType> getInstanceTypes(List<StableIdEntity> stableIdEntities) {
		return stableIdEntities
			.stream()
			.map(StableIdEntity::getInstanceType)
			.distinct()
			.collect(toList());
	}

	public int getReleaseVersion() {
		return this.releaseVersion;
	}

	public String getStableId() {
		return this.stableIdJSON.get("stableId").toString();
	}

	public int getStableIdVersion() {
		return Integer.parseInt(this.stableIdJSON.get("stableIdVersion").toString());
	}

	public String getOldStableId() {
		return this.stableIdJSON.get("oldStableId").toString();
	}

	public long getInstanceDbId() {
		return Long.parseLong(this.stableIdJSON.get("instanceID").toString());
	}

	public String getInstanceName() {
		return this.stableIdJSON.get("displayName").toString();
	}

	public String getInstanceClass() {
		return this.stableIdJSON.get("reactomeClass").toString();
	}

	public InstanceType getInstanceType() {
		return InstanceType.valueOf(this.stableIdJSON.get("instanceType").toString().toUpperCase());
	}

	public List<String> getInstanceSpecies() {
		JSONArray speciesJSONArray = (JSONArray) this.stableIdJSON.get("instanceSpecies");

		@SuppressWarnings("unchecked")
		Iterator<String> speciesIterator = speciesJSONArray.iterator();

		List<String> speciesList = new ArrayList<>();
		while (speciesIterator.hasNext()) {
			String speciesName = speciesIterator.next();
			speciesList.add(speciesName);
		}

		return speciesList;
	}

	@Override
	public String toString() {
		return "Release " + this.releaseVersion + ": " + this.stableIdJSON.toString();
	}

	@Override
	public int compareTo(StableIdEntity otherStableIdEntity) {
		return Integer.compare(this.getReleaseVersion(), otherStableIdEntity.getReleaseVersion());
	}

	public enum InstanceType {
		MANUAL("Manual"),
		ELECTRONIC("Electronic");

		private final String name;

		InstanceType(String name) {
			this.name = name;
		}

		public String toString() {
			return this.name;
		}
	}

	private static int getReleaseNumberFromFileName(String fileName) {
		Matcher m = STABLE_ID_FILE_NAME_PATTERN.matcher(fileName);

		if (!m.find()) {
			throw new RuntimeException("Unable to parse release number from file name " + fileName);
		}

		return Integer.parseInt(m.group(1));
	}
}