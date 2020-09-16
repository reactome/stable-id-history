package org.reactome.release.dbModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.gk.model.GKInstance;

public class DeletedEvent {
	// Required attributes
	private long dbId;
	private List<DeletedInstance> deletedInstances;
	private DeletionReason reason;

	// Optional attributes
	private GKInstance createdInstanceEdit;
	private String curatorComment;
	private List<GKInstance> replacementInstances;

	private static Map<Long, DeletedEvent> deletedEventCache = new HashMap<>();

	private DeletedEvent(DeletedEventBuilder deletedEventBuilder) {
		this.dbId = deletedEventBuilder.dbId;
		this.deletedInstances = deletedEventBuilder.deletedInstances;
		this.reason = deletedEventBuilder.reason;

		this.createdInstanceEdit = deletedEventBuilder.createdInstanceEdit;
		this.curatorComment = deletedEventBuilder.curatorComment;
		this.replacementInstances = deletedEventBuilder.replacementInstances;
	}

	private static DeletedEvent getDeletedEvent(DeletedEventBuilder deletedEventBuilder) {
		long deletedEventDbId = deletedEventBuilder.dbId;

		return deletedEventCache.computeIfAbsent(deletedEventDbId, k -> new DeletedEvent(deletedEventBuilder));
	}

	static void clearCache() {
		deletedEventCache.clear();
	}

	public GKInstance getCreatedInstanceEdit() {
		return createdInstanceEdit;
	}

	public String getCuratorComment() {
		return curatorComment;
	}

	public long getDbId() {
		return dbId;
	}

	public List<DeletedInstance> getDeletedInstances() {
		return deletedInstances;
	}

	public DeletionReason getReason() {
		return reason;
	}

	public List<GKInstance> getReplacementInstances() {
		return replacementInstances;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (!(obj instanceof DeletedEvent)) {
			return false;
		}

		DeletedEvent otherDeletedEvent = (DeletedEvent) obj;

		return Objects.equals(this.getDbId(), otherDeletedEvent.getDbId());
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.getDbId());
	}

	public enum DeletionReason {
		DUPLICATE("Duplicate"),
		KILLED_NOT_REPLACED("Killed not replaced"),
		MERGED("Merged"),
		OBSOLETED("Obsoleted"),
		SPLIT("Split");

		private final String name;

		DeletionReason(String name) {
			this.name = name;
		}

		public String toString() {
			return this.name;
		}
	}

	public static class DeletedEventBuilder {
		// Required attributes
		private long dbId;
		private List<DeletedInstance> deletedInstances;
		private DeletionReason reason;

		// Optional attributes
		private GKInstance createdInstanceEdit;
		private String curatorComment;
		private List<GKInstance> replacementInstances;

		public DeletedEventBuilder(long dbId, List<DeletedInstance> deletedInstances, DeletionReason reason) {
			this.dbId = dbId;
			this.deletedInstances = deletedInstances;
			this.reason = reason;

			// Default values (may be overridden by the DeletedEventBuilder "with" setters below)
			this.createdInstanceEdit = null;
			this.curatorComment = "";
			this.replacementInstances = new ArrayList<>();
		}

		public DeletedEventBuilder withCreatedInstanceEdit(GKInstance createdInstanceEdit) {
			this.createdInstanceEdit = createdInstanceEdit;
			return this;
		}

		public DeletedEventBuilder withCuratorComment(String curatorComment) {
			this.curatorComment = curatorComment;
			return this;
		}

		public DeletedEventBuilder withReplacementInstance(List<GKInstance> replacementInstances) {
			this.replacementInstances = replacementInstances;
			return this;
		}

		public DeletedEvent build() {
			return DeletedEvent.getDeletedEvent(this);
		}
	}
}
