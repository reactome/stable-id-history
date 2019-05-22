package org.reactome.release.dbModel;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Relationship;

import java.util.*;

// Abstract class to be inherited from by
// Pathway, RLE, PE, Regulation, and their
// subclasses.  Must use a hierarchy to have
// multiple labels for nodes
public class ReactomeInstance {
	@Id @GeneratedValue
	public Long id;

	private long dbId;
	private String displayName;
	private String schemaClass;
	private String type;
	private Set<String> species;


	private ReactomeInstance() {

	}

	public long getDbId() {
		return dbId;
	}

	public void setDbId(long dbId) {
		this.dbId = dbId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getSchemaClass() {
		return schemaClass;
	}

	public void setSchemaClass(String schemaClass) {
		this.schemaClass = schemaClass;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Set<String> getSpecies() {
		return species;
	}

	public void setSpecies(Set<String> species) {
		this.species = species;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}

		if (!(obj instanceof ReactomeInstance)) {
			return false;
		}

		ReactomeInstance other = (ReactomeInstance) obj;

		return other.getDbId() == this.getDbId() &&
			   other.getDisplayName().equals(this.getDisplayName()) &&
			   other.getSchemaClass().equals(this.getSchemaClass()) &&
			   other.getType().equals(this.getType()) &&
			   other.getSpecies().equals(this.getSpecies());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getDbId(), getDisplayName(), getSchemaClass(), getType(), getSpecies());
	}


	@Relationship(type="HAS_STABLE_ID")
	private StableId ownedStableId;

	//@Relationship(type="HAS_REACTOME_VERSION")
	//private Set<ReactomeVersion> reactomeVersions;

	@Relationship(type="HAS_INSTANCE", direction=Relationship.INCOMING)
	private Set<ReactomeVersion> owningReactomeVersions;

	public void setOwnedStableId(StableId ownedStableId) {
		this.ownedStableId = ownedStableId;
	}

	/*
	public void addReactomeVersion(ReactomeVersion reactomeVersion) {
		if (this.reactomeVersions == null) {
			this.reactomeVersions = new HashSet<>();
		}

		this.reactomeVersions.add(reactomeVersion);
	}
	 */

	// Builder pattern used to avoid convoluted, complex constructors
	// https://dzone.com/articles/design-patterns-the-builder-pattern
	public static class Builder {
		private static Map<ReactomeInstance, ReactomeInstance> uniqueReactomeInstances = new HashMap<>();

		private long dbId;
		private String displayName;
		private String schemaClass;
		private String type;
		private Set<String> species;

		public Builder(long dbId) {
			this.dbId = dbId;
		}

		public Builder withDisplayName(String displayName) {
			this.displayName = displayName;

			return this;
		}

		public Builder withSchemaClass(String schemaClass) {
			this.schemaClass = schemaClass;

			return this;
		}

		public Builder withType(String type) {
			this.type = type;

			return this;
		}

		public Builder withSpecies(Set<String> species) {
			this.species = species;

			return this;
		}

		public ReactomeInstance build() {
			ReactomeInstance instance = new ReactomeInstance();
			instance.dbId = this.dbId;
			instance.displayName = this.displayName;
			instance.schemaClass = this.schemaClass;
			instance.type = this.type;
			instance.species = this.species;

			if (instance.displayName == null || instance.displayName.isEmpty()) {
				throw new IllegalArgumentException("Display name is not set for " + instance.dbId);
			}


			if (instance.schemaClass == null || instance.schemaClass.isEmpty()) {
				throw new IllegalArgumentException("Schema class is not set for " + instance.dbId);
			}


			if (instance.type == null || instance.type.isEmpty()) {
				throw new IllegalArgumentException("Instance type is not set for " + instance.dbId);
			}


			if (instance.species == null) {
				species = new HashSet<>();
			}

			return uniqueReactomeInstances.computeIfAbsent(instance, k -> instance);
		}
	}
}
