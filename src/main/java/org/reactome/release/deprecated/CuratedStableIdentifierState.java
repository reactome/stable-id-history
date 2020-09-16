package org.reactome.release.deprecated;

import org.reactome.release.dbModel.StableIdentifierReleaseStatus;

public class CuratedStableIdentifierState extends StableIdentifierState {
	private CuratedStableIdentifierState(StableIdentifierReleaseStatus releaseStatus) {
		super(releaseStatus);
	}

	public static CuratedStableIdentifierState getInstance(StableIdentifierReleaseStatus releaseStatus) {
		return new CuratedStableIdentifierState(releaseStatus);
	}
}
