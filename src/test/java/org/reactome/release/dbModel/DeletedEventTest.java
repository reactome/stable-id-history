package org.reactome.release.dbModel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactome.release.dbModel.DeletedEvent.DeletedEventBuilder;
import org.reactome.release.dbModel.DeletedEvent.DeletionReason;

public class DeletedEventTest {
	// Required attributes
	private final long MOCK_DB_ID = 1L;
	private final List<DeletedInstance> MOCK_DELETED_INSTANCES = getMockDeletedInstances();
	private final DeletionReason MOCK_REASON = DeletionReason.OBSOLETED;

//	// Optional attributes
//	private GKInstance createdInstanceEdit;
	private final String MOCK_CURATOR_COMMENT = "I had my reasons for this deletion...";
//	private List<GKInstance> replacementInstances;

	@BeforeEach
	public void clearDeletedEventCache() {
		DeletedEvent.clearCache();
	}

	@Test
	public void dummyDeletedEventHasCorrectDbId() {
		DeletedEvent deletedEvent = createDummyDeletedEventBuilder().build();

		assertThat(deletedEvent.getDbId(), is(equalTo(MOCK_DB_ID)));
	}

	@Test
	public void dummyDeletedEventHasCorrectDeletedInstances() {
		DeletedEvent deletedEvent = createDummyDeletedEventBuilder().build();

		// TODO: Add implementation details for DeletedInstance objects so they can be used and mock instances
		//  can be tested
	}

	@Test
	public void dummyDeletedEventHasCorrectReason() {
		DeletedEvent deletedEvent = createDummyDeletedEventBuilder().build();

		assertThat(deletedEvent.getReason(), is(equalTo(MOCK_REASON)));
	}

	@Test
	public void dummyDeletedEventHasCorrectDefaultCuratorComment() {
		DeletedEvent deletedEvent = createDummyDeletedEventBuilder().build();

		assertThat(deletedEvent.getCuratorComment(), is(emptyString()));
	}

	@Test
	public void dummyDeletedEventHasCorrectSetCuratorComment() {
		DeletedEvent deletedEvent = createDummyDeletedEventBuilder().withCuratorComment(MOCK_CURATOR_COMMENT).build();

		assertThat(deletedEvent.getCuratorComment(), is(equalTo(MOCK_CURATOR_COMMENT)));
	}

	private DeletedEventBuilder createDummyDeletedEventBuilder() {
		return new DeletedEvent.DeletedEventBuilder(MOCK_DB_ID, MOCK_DELETED_INSTANCES, MOCK_REASON);
	}

	private List<DeletedInstance> getMockDeletedInstances() {
		return Arrays.asList(getMockDeletedInstance());
	}

	private DeletedInstance getMockDeletedInstance() {
		return DeletedInstance.getDeletedInstance(
			1L,
			"Name",
			"Class",
			2L,
			"Species"
		);
	}
}
