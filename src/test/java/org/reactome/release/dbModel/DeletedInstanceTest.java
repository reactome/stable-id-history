package org.reactome.release.dbModel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DeletedInstanceTest {
	private static final long MOCK_DB_ID = 1L;
	private static final String MOCK_INSTANCE_NAME = "Cell Cycle";
	private static final String MOCK_INSTANCE_CLASS = "Pathway";
	private static final long MOCK_DELETED_DB_ID = 2L;
	private static final String MOCK_SPECIES_NAME = "Homo sapiens";

	private DeletedInstance deletedInstance;

	@BeforeEach
	public void createDeletedInstance() {
		this.deletedInstance = DeletedInstance.getDeletedInstance(
			MOCK_DB_ID, MOCK_INSTANCE_NAME, MOCK_INSTANCE_CLASS, MOCK_DELETED_DB_ID, MOCK_SPECIES_NAME
		);
	}

	@Test
	public void dummyDeletedInstanceHasCorrectDbId() {
		assertThat(deletedInstance.getDbId(), is(equalTo(MOCK_DB_ID)));
	}

	@Test
	public void dummyDeletedInstanceHasCorrectDisplayName() {
		assertThat(deletedInstance.getInstanceName(), is(equalTo(MOCK_INSTANCE_NAME)));
	}

	@Test
	public void dummyDeletedInstanceHasCorrectInstanceClassType() {
		assertThat(deletedInstance.getInstanceClass(), is(equalTo(MOCK_INSTANCE_CLASS)));
	}

	@Test
	public void dummyDeletedInstanceHasCorrectDeletedDbId() {
		assertThat(deletedInstance.getInstanceDbId(), is(equalTo(MOCK_DELETED_DB_ID)));
	}

	@Test
	public void dummyDeletedInstanceHasCorrectSpeciesName() {
		assertThat(deletedInstance.getInstanceSpeciesName(), is(equalTo(MOCK_SPECIES_NAME)));
	}

	@Test
	public void getDeletedInstanceReturnsTheSameObjectForTheSameValues() {
		DeletedInstance secondDeletedInstanceReference = DeletedInstance.getDeletedInstance(
			MOCK_DB_ID, MOCK_INSTANCE_NAME, MOCK_INSTANCE_CLASS, MOCK_DELETED_DB_ID, MOCK_SPECIES_NAME
		);

		assertThat(deletedInstance, is(sameInstance(secondDeletedInstanceReference)));

	}
}
