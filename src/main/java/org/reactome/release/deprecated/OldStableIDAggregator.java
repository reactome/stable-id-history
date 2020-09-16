package org.reactome.release.deprecated;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.gk.model.GKInstance;
import org.gk.model.ReactomeJavaConstants;
import org.gk.persistence.MySQLAdaptor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import org.reactome.release.deprecated.InstanceUtils;

import static org.gk.model.ReactomeJavaConstants.Event;
import static org.gk.model.ReactomeJavaConstants.PhysicalEntity;
import static org.gk.model.ReactomeJavaConstants.Regulation;

public class OldStableIDAggregator {
	public static void main(String[] args) throws Exception {
		String pathToResources = args.length > 0 ? args[0] : "stableIdHistory/src/main/resources/config.properties";

		Properties props = new Properties();
		try {
			props.load(new FileInputStream(pathToResources));
		} catch (IOException e) {
			e.printStackTrace();
		}

		String releaseHost = props.getProperty("releaseHost");
		String user = props.getProperty("user");
		String password = props.getProperty("pass");
		int port = Integer.parseInt(props.getProperty("port", "3306"));

		InstanceUtils.setCuratorDBA(
			InstanceUtils.getDBA("localhost", "gk_central", "", "", port)
		);

		int reactomeVersion = 68;
		final String progressString = "Processed %d %s instances out of %s (%.2f%%)";
		while(true) {
			System.out.println("Querying Reactome version " + reactomeVersion);
			MySQLAdaptor releaseDBA;
			try {
				releaseDBA = InstanceUtils.getDBA(
					releaseHost,
					"test_reactome_" + reactomeVersion,
					user,
					password,
					port
				);
			} catch (SQLException e) {
				e.printStackTrace();
				break;
			}

			Map<String, InstanceInfo> stableIdToInstanceInfo = new HashMap<>();
			Map<Long, InstanceInfo> dbIdToInstanceInfo = new HashMap<>();
			String[] classNames = { Event, PhysicalEntity, Regulation };
			for (String className : classNames) {
				Collection<GKInstance> instances = InstanceUtils.fetchInstancesByClass(releaseDBA, className);

				int count = 0;
				for (GKInstance instance : instances) {
					count++;
					String oldIdentifier = getOldIdentifier(instance);

					if (!oldIdentifier.isEmpty()) {
						if (instance.getSchemClass().isa(ReactomeJavaConstants.EntityWithAccessionedSequence)) {
							oldIdentifier = oldIdentifier + " (" + getReferenceIdentifier(instance) + ")";
						}

						InstanceInfo instanceInfo = new InstanceInfo(instance, oldIdentifier);
						//if (!instanceInfo.getInstanceType().equals("manual"))
						//	System.out.println(instanceInfo);
						stableIdToInstanceInfo.put(oldIdentifier, instanceInfo);
						dbIdToInstanceInfo.put(instance.getDBID(), instanceInfo);
					}

					if (count % 100 == 0) {
						double percent = count * 100d / instances.size();
						System.out.println(String.format(progressString, count, className, instances.size(), percent));
					}
				}
			}
			ObjectMapper mapper = new ObjectMapper();
			mapper.writeValue(new File("./stableId" + reactomeVersion + ".json"), stableIdToInstanceInfo);
			mapper.writeValue(new File("./dbId" + reactomeVersion + ".json"), dbIdToInstanceInfo);

			reactomeVersion++;
		}
	}

	private static String getOldIdentifier(GKInstance instance) throws Exception {
		GKInstance stableIdentifierInstance =
			(GKInstance) instance.getAttributeValue(ReactomeJavaConstants.stableIdentifier);

		if (stableIdentifierInstance == null) {
			return "";
		}

		String attribute = stableIdentifierInstance.getSchemClass().isValidAttribute("oldIdentifier") ?
			"oldIdentifier" : ReactomeJavaConstants.identifier;

		return Objects.toString(stableIdentifierInstance.getAttributeValue(attribute), "");
	}

	private static String getReferenceIdentifier(GKInstance ewasInstance) {
		assert ewasInstance.getSchemClass().isa(ReactomeJavaConstants.EntityWithAccessionedSequence);

		try {
			GKInstance referenceEntityInstance =
				(GKInstance) ewasInstance.getAttributeValue(ReactomeJavaConstants.referenceEntity);

			String identifierAttribute =
				referenceEntityInstance.getSchemClass().isValidAttribute(ReactomeJavaConstants.variantIdentifier) ?
				ReactomeJavaConstants.variantIdentifier :
				ReactomeJavaConstants.identifier;

			return Objects.toString(referenceEntityInstance.getAttributeValue(identifierAttribute), "");

		} catch (Exception e) {
			System.err.println("Problem getting reference entity identifier for " + ewasInstance);
			e.printStackTrace();
			return "";
		}
	}

	private static class InstanceInfo {
		private GKInstance instance;
		private String oldStableId;

		InstanceInfo(GKInstance instance, String oldStableId) {
			this.instance = Optional.ofNullable(instance).orElseThrow(NullPointerException::new);
			this.oldStableId = oldStableId;
		}

		public long getInstanceID() {
			return instance.getDBID();
		}

		public String getDisplayName() {
			return instance.getDisplayName();
		}

		public String getReactomeClass() {
			return instance.getSchemClass().getName();
		}

		public String getInstanceType() {
			return InstanceUtils.isElectronicallyInferred(instance) ? "electronic" : "manual";
		}

		public Set<String> getInstanceSpecies() {
			return InstanceUtils
				.getSpecies(instance)
				.stream()
				.map(GKInstance::getDisplayName)
				.collect(Collectors.toSet());
		}

		public String getStableId() throws Exception {
			GKInstance stableIdInstance = getStableIdInstance();
			return stableIdInstance != null ?
				   (String) stableIdInstance.getAttributeValue(ReactomeJavaConstants.identifier) :
				   "";
		}

		public int getStableIdVersion() throws Exception {
			GKInstance stableIdInstance = getStableIdInstance();
			return stableIdInstance != null ?
				   Integer.parseInt(
					   (String) stableIdInstance.getAttributeValue(ReactomeJavaConstants.identifierVersion)) :
				   -1;
		}

		private GKInstance getStableIdInstance() throws Exception {
			return (GKInstance) instance.getAttributeValue(ReactomeJavaConstants.stableIdentifier);
		}

		public String getOldStableId() {
			return oldStableId;
		}

		@Override
		public String toString() {
			String stableIdentifier;
			String stableIdentifierVersion;
			try {
				stableIdentifier = this.getStableId();
				stableIdentifierVersion = Integer.toString(this.getStableIdVersion());
			} catch (Exception e) {
				stableIdentifier = "";
				stableIdentifierVersion = "";
			}

			return String.join("\t",
				this.getOldStableId(),
				instance.getDBID().toString(),
				this.getReactomeClass(),
				instance.getDisplayName(),
				stableIdentifier,
				stableIdentifierVersion,
				this.getInstanceType(),
				this.getInstanceSpecies().toString()
			);
		}
	}
}