package org.reactome.release.dbModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class StableIdentifierHistory {
	private StableIdWithMinorVersion stableIdWithMinorVersion;
	private Set<StableIdentifierReleaseStatus> historyStatuses;

	private static Map<StableIdWithMinorVersion, StableIdentifierHistory> stableIdToHistoryMap = new HashMap<>();

	public static StableIdentifierHistory getStableIdentifierHistory(String identifier, int identifierVersion) {
		StableIdWithMinorVersion stableIdWithMinorVersion = new StableIdWithMinorVersion(
			identifier, identifierVersion
		);

		return stableIdToHistoryMap.computeIfAbsent(
			stableIdWithMinorVersion, k -> new StableIdentifierHistory(stableIdWithMinorVersion)
		);
	}

	public String getIdentifier() {
		return this.stableIdWithMinorVersion.getIdentifier();
	}

	public int getIdentifierVersion() {
		return this.stableIdWithMinorVersion.getIdentifierVersion();
	}

	public Set<StableIdentifierReleaseStatus> getHistoryStatuses() {
		return this.historyStatuses.stream().sorted().collect(Collectors.toCollection(TreeSet::new));
	}

	public void addHistoryStatus(StableIdentifierReleaseStatus historyStatus) {
		// TODO: Have this method return a new StableIdentifierHistory object to maintain immutability
		this.historyStatuses.add(historyStatus);
	}

	private StableIdentifierHistory(StableIdWithMinorVersion stableIdWithMinorVersion) {
		this.stableIdWithMinorVersion = stableIdWithMinorVersion;
		this.historyStatuses = new TreeSet<>();
	}

	static class StableIdWithMinorVersion {
		private String identifier;
		private int identifierVersion;

		private static Map<String, StableIdWithMinorVersion> identifierWithVersionToInstance = new HashMap<>();

		private StableIdWithMinorVersion(String identifier, int identifierVersion) {
			this.identifier = identifier;
			this.identifierVersion = identifierVersion;
		}

		public static StableIdWithMinorVersion getStableIdWithMinorVersion(String identifier, int identifierVersion) {
			return identifierWithVersionToInstance.computeIfAbsent(
				constructIdentifierWithVersion(identifier, identifierVersion),
				k -> new StableIdWithMinorVersion(identifier, identifierVersion)
			);
		}

		private static String constructIdentifierWithVersion(String identifier, int identifierVersion) {
			return identifier + "." + identifierVersion;
		}

		public String getIdentifier() {
			return this.identifier;
		}

		public int getIdentifierVersion() {
			return this.identifierVersion;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}

			if (!(obj instanceof StableIdWithMinorVersion)) {
				return false;
			}

			StableIdWithMinorVersion otherStableIdWithMinorVersion = (StableIdWithMinorVersion) obj;

			return Objects.equals(this.getIdentifier(), otherStableIdWithMinorVersion.getIdentifier()) &&
				Objects.equals(this.getIdentifierVersion(), otherStableIdWithMinorVersion.getIdentifierVersion());
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.getIdentifier(), this.getIdentifierVersion());
		}

		@Override
		public String toString() {
			return constructIdentifierWithVersion(getIdentifier(), getIdentifierVersion());
		}
	}
}
