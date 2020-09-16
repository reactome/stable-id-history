package org.reactome.release.deprecated;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gk.model.GKInstance;
import org.gk.model.ReactomeJavaConstants;
import org.gk.persistence.MySQLAdaptor;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class InstanceUtils {
	private static final Logger logger = LogManager.getLogger();

	private static MySQLAdaptor curatorDBA;
	private static Map<Long, GKInstance> curatorDBIdToGKInstance;

	public static void setCuratorDBA(MySQLAdaptor dba) {
		curatorDBA = dba;
	}

	public static boolean isElectronicallyInferred(GKInstance instance) {
		String createdNote = getCreatedInstanceNote(instance);
		if (createdNote.matches("inferred events based on (ensembl compara|orthomcl|panther)") ||
			createdNote.matches("Inserted by org.reactome.orthoinference")) {
			return true;
		}

		/*
		if (instance.getSchemClass().isa(ReactomeJavaConstants.Event)) {
			return evidenceTypeIsElectronic(instance);
		} else if (instance.getSchemClass().isa(ReactomeJavaConstants.PhysicalEntity)) {
			return !inCuratorDatabase(instance) && hasHumanSourceInstance(instance);
		} else if (instance.getSchemClass().isa(ReactomeJavaConstants.CatalystActivity)) {
			return getCatalystActivityReferrers(instance).stream().anyMatch(InstanceUtils::isElectronicallyInferred);
		} else if (instance.getSchemClass().isa(ReactomeJavaConstants.Regulation)) {
			return getRegulatedEvents(instance).stream().anyMatch(InstanceUtils::isElectronicallyInferred);
		}
		*/

		return false;
	}

	public static MySQLAdaptor getDBA(String host, String database, String user, String password, int port)
		throws SQLException {
		return new MySQLAdaptor(host, database, user, password, port);
	}

	private static String getCreatedInstanceNote(GKInstance instance) {
		try {
			String note = (String) getCreatedInstance(instance).getAttributeValue(ReactomeJavaConstants.note);
			return note != null ? note : "";
		} catch (Exception e) {
			return "";
		}
	}

	private static GKInstance getCreatedInstance(GKInstance instance) {
		try {
			return (GKInstance) instance.getAttributeValue(ReactomeJavaConstants.created);
		} catch (Exception e) {
			logger.error("Could not get created instance for {}", instance, e);
			throw new RuntimeException(e);
		}
	}

	private static boolean evidenceTypeIsElectronic(GKInstance event) {
		GKInstance evidenceType;
		try {
			evidenceType = (GKInstance) event.getAttributeValue(ReactomeJavaConstants.evidenceType);
		} catch (Exception e) {
			logger.error("Could not retrieve evidence type for event {}", event, e);
			throw new RuntimeException(e);
		}

		return evidenceType != null && evidenceType.getExtendedDisplayName().matches("electronic");
	}

	private static boolean inCuratorDatabase(GKInstance instance) {
		if (curatorDBIdToGKInstance == null) {
			curatorDBIdToGKInstance = fetchInstancesByClass(curatorDBA, ReactomeJavaConstants.PhysicalEntity)
										  .stream()
										  .collect(Collectors.toMap(GKInstance::getDBID, i -> i));
		}

		GKInstance curatorDBInstance = curatorDBIdToGKInstance.get(instance.getDBID());

		// TODO: Check deleted instances
		//Collection<GKInstance> curatorDBA.fetchInstancesByClass(ReactomeJavaConstants._Deleted);

		return curatorDBInstance != null &&
			instance.getSchemClass().getName().equals(curatorDBInstance.getSchemClass().getName()) &&
			instance.getDBID().equals(curatorDBInstance.getDBID()) &&
			instance.getDisplayName().equals(curatorDBInstance.getDisplayName()) &&
			haveSameSpecies(instance, curatorDBInstance);
	}

	public static Collection<GKInstance> fetchInstancesByClass(MySQLAdaptor dba, String className) {
		try {
			@SuppressWarnings("unchecked")
			Collection<GKInstance> physicalEntityInstances =
				dba.fetchInstancesByClass(className);

			return physicalEntityInstances;
		} catch (Exception e) {
			logger.warn("Unable to fetch {} instances from {} at host {}",
						className,
						dba.getDBName(),
						dba.getDBHost(),
						e);
			return new ArrayList<>();
		}
	}

	private static boolean haveSameSpecies(GKInstance firstInstance, GKInstance secondInstance) {
		List<GKInstance> firstInstanceSpecies = getSpecies(firstInstance);
		List<GKInstance> secondInstanceSpecies = getSpecies(secondInstance);

		// No species is considered the 'same' species
		if (firstInstanceSpecies.isEmpty() && secondInstanceSpecies.isEmpty()) {
			return true;
		}

		// If the number of species instances is different, the species each has
		// can't be the same
		if (firstInstanceSpecies.size() != secondInstanceSpecies.size()) {
			return false;
		}

		List<String> secondInstanceSpeciesNames =
			secondInstanceSpecies
				.stream()
				.map(GKInstance::getDisplayName)
				.collect(Collectors.toList());

		// Since the species count is equal, check if all the species
		// that occur in the first collection are present in the second
		return firstInstanceSpecies
				.stream()
				.map(GKInstance::getDisplayName)
				.allMatch(secondInstanceSpeciesNames::contains);
	}

	private static boolean hasHumanSourceInstance(GKInstance instance) {
		@SuppressWarnings("unchecked")
		List<GKInstance> inferenceSourceInstances =
			instance.getAttributeValuesListNoCheck(ReactomeJavaConstants.inferredFrom);

		if (inferenceSourceInstances == null) {
			return false;
		}

		for (GKInstance inferenceSourceInstance : inferenceSourceInstances) {
			try {
				@SuppressWarnings("unchecked")
				List<GKInstance> sourceSpeciesInstances =
					inferenceSourceInstance.getAttributeValuesList(ReactomeJavaConstants.species);
				if (sourceSpeciesInstances.size() == 1 && containsHuman(sourceSpeciesInstances)) {
					return true;
				}
			} catch (Exception e) {
				logger.warn("Could not get species instances for {}", instance, e);
			}
		}

		return false;
	}

	private static boolean containsHuman(List<GKInstance> speciesInstances) {
		return speciesInstances
				.stream()
				.map(GKInstance::getDisplayName)
				.anyMatch(name -> name.matches("^Homo Sapiens$"));
	}

	@SuppressWarnings("unchecked")
	private static List<GKInstance> getRegulatedEvents(GKInstance regulation) {
		try {
			if (regulation.getSchemClass().isValidAttribute(ReactomeJavaConstants.regulatedEntity)) {
				return regulation.getAttributeValuesList(ReactomeJavaConstants.regulatedEntity);
			} else {
				return new ArrayList<>(regulation.getReferers("regulatedBy"));
			}
		} catch (Exception e) {
			logger.error("Unable to get regulated events for regulation instance {}", regulation, e);
			throw new RuntimeException(e);
		}
	}

	private static List<GKInstance> getCatalystActivityReferrers(GKInstance catalystActivity) {
		try {
			@SuppressWarnings("unchecked")
			Collection<GKInstance> catalystActivityReferrers =
				catalystActivity.getReferers(ReactomeJavaConstants.catalystActivity);
			return new ArrayList<>(catalystActivityReferrers);
		} catch (Exception e) {
			logger.error("Unable to get catalyst activity referrers for {}", catalystActivity, e);
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public static List<GKInstance> getSpecies(GKInstance instance) {
		try {
			if (instance.getSchemClass().isa(ReactomeJavaConstants.Regulation)) {
				return getRegulatedEvents(instance)
					.stream()
					.map(InstanceUtils::getSpecies)
					.flatMap(Collection::stream)
					.distinct()
					.collect(Collectors.toList());
			} else {
				return Optional.ofNullable(instance.getAttributeValuesList(ReactomeJavaConstants.species))
					.orElse(Collections.emptyList());
			}
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}
}
