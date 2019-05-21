package org.reactome.release.dbModel;

import org.neo4j.ogm.annotation.*;

import java.util.HashSet;
import java.util.Set;

@NodeEntity
public class ReactomeVersion {
	@Id
	private int releaseNumber;
	private String releaseDate;

	public ReactomeVersion(int releaseNumber, String releaseDate) {
		this.releaseNumber = releaseNumber;
		this.releaseDate = releaseDate;
	}

	//@Relationship(type="HAS_STABLE_ID")
	//private Set<StableId> ownedStableIds;

	@Relationship(type="HAS_INSTANCE")
	private Set<ReactomeInstance> ownedInstances;

	//@Relationship(type="HAS_REACTOME_VERSION", direction=Relationship.INCOMING)
	//private StableId stableId;

	//@Relationship(type="HAS_REACTOME_VERSION", direction=Relationship.INCOMING)
	//private ReactomeInstance instance;

	/*
	public void addOwnedStableId(StableId ownedStableId) {
		if (this.ownedStableIds == null) {
			this.ownedStableIds = new HashSet<>();
		}

		this.ownedStableIds.add(ownedStableId);
	}
	*/

	public void addOwnedInstance(ReactomeInstance ownedInstance) {
		if (this.ownedInstances == null) {
			this.ownedInstances = new HashSet<>();
		}

		this.ownedInstances.add(ownedInstance);
	}
}
