package org.reactome.release.deprecated;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Relationship;
import org.reactome.release.dbModel.instances.ReactomeInstance;

import java.util.*;

public class StableId {
	private static Map<StableId, StableId> uniqueStableIds = new HashMap<>();

	@Id @GeneratedValue
	private Long id;

	private String displayName;
	private String identifier;
	private long minorVersion;

	public static StableId get(String identifier) {
		return get(identifier, 1);
	}

	public static StableId get(String identifier, long minorVersion) {
		StableId stableId = new StableId(identifier, minorVersion);

		return uniqueStableIds.computeIfAbsent(stableId, k -> stableId);
	}

	public String getIdentifier() {
		return this.identifier;
	}

	public long getMinorVersion() {
		return this.minorVersion;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}

		if (!(obj instanceof StableId)) {
			return false;
		}

		StableId other = (StableId) obj;

		return other.getIdentifier().equals(this.getIdentifier()) &&
			   other.getMinorVersion() == this.getMinorVersion();
	}

	@Override
	public int hashCode() {
		return Objects.hash(getIdentifier(), getMinorVersion());
	}

	private StableId(String identifier, long minorVersion) {
		this.identifier = identifier;
		this.minorVersion = minorVersion;
		this.displayName = identifier + "." + minorVersion;
	}

	@Relationship(type="OLD_STABLE_ID")
	private StableId oldStableId;

	//@Relationship(type="HAS_REACTOME_VERSION")
	//private Set<ReactomeVersion> reactomeVersions;

	@Relationship(type="HAS_INSTANCE", direction=Relationship.INCOMING)
	private ReactomeInstance owningReactomeInstance;

	public void setOldStableId(StableId oldStableId) {
		this.oldStableId = oldStableId;
	}

	/*
	public void addReactomeVersion(ReactomeVersion reactomeVersion) {
		if (this.reactomeVersions == null) {
			this.reactomeVersions = new HashSet<>();
		}

		this.reactomeVersions.add(reactomeVersion);
	}
	*/
}
