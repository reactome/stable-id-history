package org.reactome.release.dbModel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;

import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.reactome.release.dbModel.StableIdentifierReleaseStatus.Status;

public class StableIdentifierReleaseStatusTest {
	private final static int DUMMY_RELEASE_NUMBER = 51;
	private final static Status DUMMY_RELEASE_STATUS = Status.CREATED;

	private StableIdentifierReleaseStatus stableIdentifierReleaseStatus;

	@Before
	public void createStableIdentifierReleaseStatus() {
		this.stableIdentifierReleaseStatus = StableIdentifierReleaseStatus.getStableIdentifierReleaseStatus(
			DUMMY_RELEASE_NUMBER, DUMMY_RELEASE_STATUS
		);
	}

	@Test
	public void dummyStableIdentifierReleaseStatusHasCorrectReleaseNumber() {
		assertThat(stableIdentifierReleaseStatus.getReleaseNumber(), is(equalTo(DUMMY_RELEASE_NUMBER)));
	}

	@Test
	public void dummyStableIdentifierReleaseStatusHasCorrectStatus() {
		assertThat(stableIdentifierReleaseStatus.getStatus(), is(equalTo(DUMMY_RELEASE_STATUS)));
	}

	@Test
	public void getStableIdentifierReleaseStatusReturnsTheSameObjectForTheSameReleaseNumberAndStatus() {
		StableIdentifierReleaseStatus secondReferenceToStableIdentifierReleaseStatus =
			StableIdentifierReleaseStatus.getStableIdentifierReleaseStatus(DUMMY_RELEASE_NUMBER, DUMMY_RELEASE_STATUS);

		assertThat(stableIdentifierReleaseStatus, is(sameInstance(secondReferenceToStableIdentifierReleaseStatus)));
	}
}
