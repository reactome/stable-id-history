package org.reactome.release.deprecated;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gk.model.GKInstance;
import org.gk.model.ReactomeJavaConstants;
import org.gk.persistence.MySQLAdaptor;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import org.reactome.release.deprecated.InstanceUtils;

public class StableIdHistory {
	private static final Logger logger = LogManager.getLogger();

	public static void main(String[] args) throws SQLException {
		String pathToResources = args.length > 0 ? args[0] : "stableIdHistory/src/main/resources/config.properties";

		Properties props = new Properties();
		try {
			props.load(new FileInputStream(pathToResources));
		} catch (IOException e) {
			e.printStackTrace();
		}

		DBOptions releaseDBOptions = new DBOptions("release", props);
		DBOptions curatorDBOptions = new DBOptions("curator", props);
		DBOptions localDBOptions = new DBOptions("local", props);

		int currentReactomeVersion = 0;
		try {
			if (props.getProperty("reactomeVersion") != null) {
				currentReactomeVersion = Integer.parseInt(props.getProperty("reactomeVersion"));
			} else {
				currentReactomeVersion = currentReactomeVersion();
			}
		} catch (IOException e) {
			logger.error("Unable to query reactome.org for current version", e);
			System.exit(1);
		}

		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			logger.error("Unable to load MySQL driver", e);
			System.exit(1);
		}

		Connection connection;
		try {
			connection =
				DriverManager.getConnection(
					"jdbc:mysql://" + localDBOptions.getHost() +
					"/?user=" + localDBOptions.getUser() + "&password=" + localDBOptions.getPass()
				);
			Statement stm = connection.createStatement();
			stm.executeUpdate("DROP DATABASE IF EXISTS StableIdentifierHistory");
			stm.executeUpdate("CREATE DATABASE StableIdentifierHistory");
			stm.executeUpdate("USE StableIdentifierHistory");
			ScriptRunner runner = new ScriptRunner(connection);
			runner.runScript(
				new BufferedReader(
					new FileReader("stableIdHistory/src/main/resources/stable_id_history.sql")
				)
			);
		} catch (SQLException | FileNotFoundException e) {
			logger.error("Unable to create StableIdentifierHistory database", e);
			System.exit(1);
		}

		InstanceUtils.setCuratorDBA(
			InstanceUtils.getDBA(
				curatorDBOptions.getHost(),
				curatorDBOptions.getDbName(),
				curatorDBOptions.getUser(),
				curatorDBOptions.getPass(),
				curatorDBOptions.getPort()
			)
		);

		final int firstVersionWithStableIds = 19;
		for (int reactomeVersion = firstVersionWithStableIds; reactomeVersion <= currentReactomeVersion; reactomeVersion++) {
			String releaseDB = "test_reactome_" + reactomeVersion;

			MySQLAdaptor releaseDBA;
			try {
				releaseDBA = InstanceUtils.getDBA(
					releaseDBOptions.getHost(),
					releaseDB,
					releaseDBOptions.getUser(),
					releaseDBOptions.getPass(),
					releaseDBOptions.getPort()
				);
			} catch (SQLException e) {
				logger.error("Could not get MySQLAdaptor for " + releaseDB + " on host "
					+ releaseDBOptions.getHost(), e);
				continue;
			}

			for (GKInstance instance : getInstances(releaseDBA, Arrays.asList(ReactomeJavaConstants.Event,
					ReactomeJavaConstants.PhysicalEntity, ReactomeJavaConstants.Regulation))) {
				//TODO Extract the relevant instance information and store it in the new stable id history database
				long instanceDbId = instance.getDBID();
				String instanceDisplayName = instance.getDisplayName();
				String instanceClass = instance.getSchemClass().getName();
				String instanceType = InstanceUtils.isElectronicallyInferred(instance) ? "electronic" : "manual";
				Optional<GKInstance> stableIdentifierInstance = getStableIdentifierInstance(instance);
				String stableIdReactomeVersion = Integer.toString(reactomeVersion);

				stableIdentifierInstance.ifPresent(stableIdInstance -> {
					String stableIdentifier = getStableIdentifier(stableIdInstance);
					int stableIdentifierVersion = getStableIdentifierVersion(stableIdInstance);
					String oldStableId = "";

					System.out.println(String.join("\t",
						stableIdentifier,
						Integer.toString(stableIdentifierVersion),
						oldStableId,
						Long.toString(instanceDbId),
						instanceDisplayName,
						instanceClass,
						instanceType,
						stableIdReactomeVersion
					));
				});
			}
		}
	}

	public static class DBOptions {
		private String host;
		private String user;
		private String pass;
		private int port;
		private String dbName;

		public DBOptions(String dbLocation, Properties props) {
			this.host = props.getProperty(dbLocation + "Host", "localhost");
			this.user = props.getProperty(dbLocation + "User", "");
			this.pass = props.getProperty(dbLocation + "Pass", "");
			this.port = Integer.parseInt(props.getProperty(dbLocation + "Port", "3306"));
			this.dbName = props.getProperty(dbLocation + "Db", "");
		}

		public String getHost() {
			return host;
		}

		public String getUser() {
			return user;
		}

		public String getPass() {
			return pass;
		}

		public int getPort() {
			return port;
		}

		public String getDbName() {
			return dbName;
		}

		public void setDbName(String dbName) {
			this.dbName = dbName;
		}
	}

	public static int currentReactomeVersion() throws IOException {
		URL url = new URL("https://reactome.org/ContentService/data/database/version");
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");

		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String line;
		StringBuilder content = new StringBuilder();
		while ((line = in.readLine()) != null) {
			content.append(line);
		}
		in.close();

		return Integer.parseInt(content.toString());
	}

	private static List<GKInstance> getInstances(MySQLAdaptor dba, List<String> classNames) {
		List<GKInstance> instances = new ArrayList<>();
		for (String className : classNames) {
			instances.addAll(getInstances(dba, className));
		}
		return instances;
	}

	@SuppressWarnings("unchecked")
	private static List<GKInstance> getInstances(MySQLAdaptor dba, String className) {
		List<GKInstance> instances = new ArrayList<>();

		try {
			instances.addAll(dba.fetchInstancesByClass(className));
		} catch (Exception e) {
			logger.error("Unable to fetch {} from database", className, e);
			System.exit(1);
		}

		return instances;
	}

	private static Optional<GKInstance> getStableIdentifierInstance(GKInstance instance) {
		GKInstance stableIdentifierInstance = null;
		try {
			stableIdentifierInstance = (GKInstance) instance.getAttributeValue(ReactomeJavaConstants.stableIdentifier);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (stableIdentifierInstance == null) {
			return Optional.empty();
		}

		return Optional.of(stableIdentifierInstance);
	}

	private static String getStableIdentifier(GKInstance stableIdentifierInstance) {
		try {
			return (String) stableIdentifierInstance.getAttributeValue(ReactomeJavaConstants.identifier);
		} catch (Exception e) {
			throw new IllegalStateException("Stable identifier instance " +
				stableIdentifierInstance.getExtendedDisplayName() + " in db " +
				((MySQLAdaptor) stableIdentifierInstance.getDbAdaptor()).getDBName() + " has no identifier", e);
		}
	}

	private static int getStableIdentifierVersion(GKInstance stableIdentifierInstance) {
		try {
			return Integer.parseInt(
				(String) stableIdentifierInstance.getAttributeValue(ReactomeJavaConstants.identifierVersion)
			);
		} catch (Exception e) {
			throw new IllegalStateException("Stable identifier instance " +
				stableIdentifierInstance.getExtendedDisplayName() + " in db " +
				((MySQLAdaptor) stableIdentifierInstance.getDbAdaptor()).getDBName() + " has no identifier version", e);
		}
	}
}