package org.reactome.release.dbModel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class DeletedInstance {
	private long dbId;
	private String instanceName;
	private String instanceClass;
	private long instanceDbId;
	private String instanceSpeciesName;

	private static Map<Long, Set<DeletedInstance>> deletedInstanceMap = new HashMap<>();

	private DeletedInstance(long dbId, String instanceName, String instanceClass, long instanceDbId, String speciesName) {
		this.dbId = dbId;
		this.instanceName = instanceName;
		this.instanceClass = instanceClass;
		this.instanceDbId = instanceDbId;
		this.instanceSpeciesName = speciesName;
	}

	public static DeletedInstance getDeletedInstance(long dbId, String instanceName, String instanceClass, long instanceDbId) {
		String noSpeciesName = "";

		return getDeletedInstance(dbId, instanceName, instanceClass, instanceDbId, noSpeciesName);
	}

	public static DeletedInstance getDeletedInstance(long dbId, String instanceName, String instanceClass, long instanceDbId, String speciesName) {
		Set<DeletedInstance> deletedInstanceSet = deletedInstanceMap.computeIfAbsent(
			dbId, k -> new HashSet<>()
		);

		if (!deletedInstanceSet.isEmpty() && deletedInstanceSet.add(new DeletedInstance(dbId, instanceName, instanceClass, instanceDbId, speciesName))) {
			throw new RuntimeException("Deleted instance with different values already existed for " + dbId);
		}

		return deletedInstanceSet.iterator().next();

	}

	public long getDbId() {
		return this.dbId;
	}

	public String getInstanceName() {
		return this.instanceName;
	}

	public String getInstanceClass() {
		return this.instanceClass;
	}

	public long getInstanceDbId() {
		return this.instanceDbId;
	}

	public String getInstanceSpeciesName() {
		return this.instanceSpeciesName;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (!(obj instanceof DeletedInstance)) {
			return false;
		}

		DeletedInstance otherDeletedInstance = (DeletedInstance) obj;

		return Objects.equals(this.getDbId(), otherDeletedInstance.getDbId());
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.getDbId());
	}
}
