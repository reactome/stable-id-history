package org.reactome.release.deprecated;

import org.reactome.release.dbModel.StableIdentifierReleaseStatus;

public class ElectronicStableIdentifierState extends StableIdentifierState {
	private long instanceDbId;

	private ElectronicStableIdentifierState(StableIdentifierReleaseStatus releaseStatus, long instanceDbId) {
		super(releaseStatus);

		this.instanceDbId = instanceDbId;
	}

	public static ElectronicStableIdentifierState getInstance(StableIdentifierReleaseStatus releaseStatus, long instanceDbId) {
		return new ElectronicStableIdentifierState(releaseStatus, instanceDbId);
	}

	public long getInstanceDbId() {
		return instanceDbId;
	}
}