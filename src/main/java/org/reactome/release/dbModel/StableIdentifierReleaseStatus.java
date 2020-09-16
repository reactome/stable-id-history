package org.reactome.release.dbModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class StableIdentifierReleaseStatus implements Comparable<StableIdentifierReleaseStatus> {
	private int releaseNumber;
	private Status status;

	private static Map<Integer, Map<Status, StableIdentifierReleaseStatus>> releaseNumberStatusMap = new HashMap<>();

	private StableIdentifierReleaseStatus(int releaseNumber, Status status) {
		this.releaseNumber = releaseNumber;
		this.status = status;
	}

	public static StableIdentifierReleaseStatus getStableIdentifierReleaseStatus(int releaseNumber, Status status) {
		Map<Status, StableIdentifierReleaseStatus> statusTypeToReleaseStatus =
			releaseNumberStatusMap.computeIfAbsent(releaseNumber, k -> new HashMap<>());

		return statusTypeToReleaseStatus.computeIfAbsent(
			status, k -> new StableIdentifierReleaseStatus(releaseNumber, status)
		);
	}

	public int getReleaseNumber() {
		return this.releaseNumber;
	}

	public Status getStatus() {
		return this.status;
	}

	@Override
	public int compareTo(StableIdentifierReleaseStatus otherStableIdentifierReleaseStatus) {
		if (this.getReleaseNumber() < otherStableIdentifierReleaseStatus.getReleaseNumber()) {
			return -1;
		} else if (this.getReleaseNumber() > otherStableIdentifierReleaseStatus.getReleaseNumber()) {
			return 1;
		} else {
			return this.getStatus().toString().compareTo(otherStableIdentifierReleaseStatus.getStatus().toString());
		}
	}

	@Override
	public String toString() {
		return "Release " + this.getReleaseNumber() + ": " + this.getStatus().toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (!(obj instanceof StableIdentifierReleaseStatus)) {
			return false;
		}

		StableIdentifierReleaseStatus otherStableIdentifierReleaseStatus = (StableIdentifierReleaseStatus) obj;

		return Objects.equals(this.getReleaseNumber(), otherStableIdentifierReleaseStatus.getReleaseNumber()) &&
			Objects.equals(this.getStatus(), otherStableIdentifierReleaseStatus.getStatus());
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.getReleaseNumber(), this.getStatus());
	}

	public enum Status {
		CREATED("Created"),
		EXISTS("Exists"),
		INCREMENTED("Incremented"),
		REPLACED("Replaced"),
		ORTHO("Ortho"),
		REMOVED("Removed");

		private final String name;

		Status(String name) {
			this.name = name;
		}

		public String toString() {
			return this.name;
		}
	}
}
