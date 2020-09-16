package org.reactome.release.deprecated;

import org.reactome.release.dbModel.StableIdentifierReleaseStatus;

public abstract class StableIdentifierState {
	private StableIdentifierReleaseStatus releaseStatus;

	protected StableIdentifierState(StableIdentifierReleaseStatus releaseStatus) {
		this.releaseStatus = releaseStatus;
	}

	public StableIdentifierReleaseStatus getReleaseStatus() {
		return this.releaseStatus;
	}
}
