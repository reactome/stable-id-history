package org.reactome.release.dbModel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactome.release.dbModel.StableIdentifierHistory.StableIdWithMinorVersion;
import org.reactome.release.dbModel.StableIdentifierReleaseStatus.Status;

public class StableIdentifierHistoryTest {
	private final String DUMMY_IDENTIFIER_WITH_VERSION = "R-HSA-123456.1";
	private final String DUMMY_IDENTIFIER = "R-HSA-123456";
	private final int DUMMY_IDENTIFIER_VERSION = 1;

	private StableIdWithMinorVersion stableIdWithMinorVersion;

	@BeforeEach
	public void createStableIdWithMinorVersion() {
		this.stableIdWithMinorVersion = StableIdWithMinorVersion.getStableIdWithMinorVersion(
			DUMMY_IDENTIFIER, DUMMY_IDENTIFIER_VERSION
		);
	}

	@Test
	public void stableIdentifierWithMinorVersionHasCorrectStringRepresentation() {
		assertThat(stableIdWithMinorVersion.toString(), is(equalTo(DUMMY_IDENTIFIER_WITH_VERSION)));
	}

	@Test
	public void stableIdentifierWithMinorVersionHasCorrectIdentifier() {
		assertThat(stableIdWithMinorVersion.getIdentifier(), is(equalTo(DUMMY_IDENTIFIER)));
	}

	@Test
	public void stableIdentifierWithMinorVersionHasCorrectIdentifierVersion() {
		assertThat(stableIdWithMinorVersion.getIdentifierVersion(), is(equalTo(DUMMY_IDENTIFIER_VERSION)));
	}

	@Test
	public void getStableIdentifierReleaseStatusReturnsTheSameObjectForTheSameReleaseNumberAndStatus() {
		StableIdWithMinorVersion secondReferenceToStableIdWithMinorVersion =
			StableIdWithMinorVersion.getStableIdWithMinorVersion(DUMMY_IDENTIFIER, DUMMY_IDENTIFIER_VERSION);

		assertThat(stableIdWithMinorVersion, is(sameInstance(secondReferenceToStableIdWithMinorVersion)));
	}

	@Test
	public void stableIdentifierHistoryStatusesAddedAndSortedCorrectly() {
		StableIdentifierReleaseStatus statusExists52 =
			StableIdentifierReleaseStatus.getStableIdentifierReleaseStatus(52, Status.EXISTS);
		StableIdentifierReleaseStatus statusIncremented51 =
			StableIdentifierReleaseStatus.getStableIdentifierReleaseStatus(51, Status.INCREMENTED);
		StableIdentifierReleaseStatus statusRemoved53 =
			StableIdentifierReleaseStatus.getStableIdentifierReleaseStatus(53, Status.REMOVED);
		StableIdentifierReleaseStatus statusCreated51 =
			StableIdentifierReleaseStatus.getStableIdentifierReleaseStatus(51, Status.CREATED);
		StableIdentifierReleaseStatus statusOrtho52 =
			StableIdentifierReleaseStatus.getStableIdentifierReleaseStatus(52, Status.ORTHO);

		StableIdentifierHistory stableIdentifierHistory = StableIdentifierHistory.getStableIdentifierHistory(
			DUMMY_IDENTIFIER, DUMMY_IDENTIFIER_VERSION
		);
		stableIdentifierHistory.addHistoryStatus(statusExists52);
		stableIdentifierHistory.addHistoryStatus(statusIncremented51);
		stableIdentifierHistory.addHistoryStatus(statusRemoved53);
		stableIdentifierHistory.addHistoryStatus(statusCreated51);
		stableIdentifierHistory.addHistoryStatus(statusOrtho52);


		assertThat(
			stableIdentifierHistory.getHistoryStatuses(),
			contains(statusCreated51, statusIncremented51, statusExists52, statusOrtho52, statusRemoved53)
		);
	}

	private List<StableIdentifierReleaseStatus> getStableIdentifierReleaseStatuses() {
		return Arrays.asList(

		);
	}
}