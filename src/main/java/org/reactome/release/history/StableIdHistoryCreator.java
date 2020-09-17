package org.reactome.release.history;

import static org.reactome.release.stableIdParser.StableIdEntity.getInstanceTypes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.reactome.release.dbModel.StableIdentifierHistory;
import org.reactome.release.dbModel.StableIdentifierReleaseStatus;
import org.reactome.release.dbModel.StableIdentifierReleaseStatus.Status;
import org.reactome.release.stableIdParser.StableIdEntity;
import org.reactome.release.stableIdParser.StableIdEntity.InstanceType;

public class StableIdHistoryCreator {
	private static final Logger logger = LogManager.getLogger();

	public static Map<String, List<StableIdentifierHistory>> createStableIdHistoryMap(
		Map<String, List<StableIdEntity>> stableIdEntitiesByStableIdentifier
	) {
		Map<String, List<StableIdentifierHistory>> stableIdHistoryMap = new HashMap<>();

		for (
			Entry<String, List<StableIdEntity>> stableIdToStableIdEntitiesEntry :
			stableIdEntitiesByStableIdentifier.entrySet()
		) {
			String stableId = stableIdToStableIdEntitiesEntry.getKey();
			List<StableIdEntity> stableIdEntities = stableIdToStableIdEntitiesEntry.getValue();
			stableIdEntities.sort(byAscendingReleaseNumber());

			if (stableIdHistoryMap.containsKey(stableId)) {
				throwFatalError(stableId + " has already been seen in creating stable id history");
			}

			stableIdHistoryMap.put(stableId, createStableIdentifierHistory(stableId, stableIdEntities));
		}

		return stableIdHistoryMap;
	}

	private static Comparator<StableIdEntity> byAscendingReleaseNumber() {
		return Comparator.comparingInt(StableIdEntity::getReleaseVersion);
	}

	private static List<StableIdentifierHistory> createStableIdentifierHistory(String stableId, List<StableIdEntity> stableIdEntities) {
		List<InstanceType> stableIdEntityInstanceTypes = getInstanceTypes(stableIdEntities);

		if (stableIdEntityInstanceTypes.size() > 1) {
			throwFatalError(
				"There are different instance types for the stable id " + stableId + " across releases for the " +
				"collection " + stableIdEntities
			);
		}

		InstanceType stableIdEntitiesInstanceType = stableIdEntityInstanceTypes.get(0);
		if (stableIdEntitiesInstanceType == InstanceType.MANUAL || stableIdEntitiesInstanceType == InstanceType.ELECTRONIC) {
			return createStableIdentifierHistory(stableIdEntities);
		} else {
			throwFatalError(
				"Unknown instance type for the identifier " + stableId +  " collection of stable id entities " +
				stableIdEntities
			);
			return new ArrayList<>();
		}
	}

	private static void throwFatalError(String errorMessage) throws RuntimeException {
		logger.fatal(errorMessage);
		throw new RuntimeException(errorMessage);
	}

	private static List<StableIdentifierHistory> createStableIdentifierHistory(List<StableIdEntity> stableIdEntities) {
		List<StableIdentifierHistory> stableIdentifierHistoryList = new ArrayList<>();

		for (StableIdEntity stableIdEntity : stableIdEntities) {
			String stableId = stableIdEntity.getStableId();
			int stableIdVersion = stableIdEntity.getStableIdVersion();

			StableIdentifierHistory stableIdentifierHistory = StableIdentifierHistory.getStableIdentifierHistory(
				stableId, stableIdVersion
			);

			StableIdentifierReleaseStatus stableIdentifierReleaseStatus = StableIdentifierReleaseStatus.getStableIdentifierReleaseStatus(
				stableIdEntity.getReleaseVersion(), getNewStatus(stableIdEntity, stableIdentifierHistoryList)
			);
			stableIdentifierHistory.addHistoryStatus(stableIdentifierReleaseStatus);

			stableIdentifierHistoryList.add(stableIdentifierHistory);
		}

		return stableIdentifierHistoryList;
	}

	private static Status getNewStatus(StableIdEntity stableIdEntity, List<StableIdentifierHistory> stableIdentifierHistoryList) {
		if (stableIdEntity.getInstanceType() == InstanceType.ELECTRONIC) {
			return Status.ORTHO;
		}

		if (stableIdentifierHistoryList.isEmpty()) {
			return Status.CREATED;
		}

		StableIdentifierHistory mostRecentStableIdentifierHistory =  stableIdentifierHistoryList.get(stableIdentifierHistoryList.size() - 1);

		if (differentStableIdentifierValue(mostRecentStableIdentifierHistory, stableIdEntity)) {
			return Status.REPLACED;
		} else {
			if (sameStableIdentifierMinorVersion(mostRecentStableIdentifierHistory, stableIdEntity)) {
				return Status.EXISTS;
			} else {
				return Status.INCREMENTED;
			}
		}
	}

	private static boolean differentStableIdentifierValue(
		StableIdentifierHistory mostRecentStableIdentifierHistory, StableIdEntity stableIdEntity
	) {
		return !(mostRecentStableIdentifierHistory.getIdentifier().equals(stableIdEntity.getStableId()));
	}

	private static boolean sameStableIdentifierMinorVersion(
		StableIdentifierHistory mostRecentStableIdentifierHistory, StableIdEntity stableIdEntity
	) {
		if (mostRecentStableIdentifierHistory.getIdentifierVersion() > stableIdEntity.getStableIdVersion()) {
			throwFatalError(
				stableIdEntity + " has a lower minor version than the previous stable identifier history instance"
			);
		}

		return mostRecentStableIdentifierHistory.getIdentifierVersion() == stableIdEntity.getStableIdVersion();
	}
}
