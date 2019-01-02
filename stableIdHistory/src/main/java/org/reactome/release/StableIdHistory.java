package org.reactome.release;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gk.model.GKInstance;
import org.gk.model.ReactomeJavaConstants;
import org.gk.persistence.MySQLAdaptor;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;


public class StableIdHistory {
    private static final Logger logger = LogManager.getLogger();

    public static void main(String[] args) {
        String pathToResources = args.length > 0 ? args[0] : "stableIdHistory/src/main/resources/config.properties";

        Properties props = new Properties();
        try {
            props.load(new FileInputStream(pathToResources));
        } catch (IOException e) {
            e.printStackTrace();
        }

        String releaseHost = props.getProperty("releaseHost");
        String curatorHost = props.getProperty("curatorHost");
        String user = props.getProperty("user");
        String password = props.getProperty("pass");
        int port = Integer.parseInt(props.getProperty("port", "3306"));

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
                DriverManager.getConnection("jdbc:mysql://localhost/?user=" + user + "&password=" + password);
            Statement stm = connection.createStatement();
            stm.executeUpdate("CREATE DATABASE StableIdentifierHistory");
        } catch (SQLException e) {
            logger.error("Unable to create StableIdentifierHistory database", e);
            System.exit(1);
        }

        for (int reactomeVersion = 10; reactomeVersion <= currentReactomeVersion; reactomeVersion++) {
            MySQLAdaptor releaseDBA;
            try {
                releaseDBA = getDBA(releaseHost, "test_reactome_" + reactomeVersion, user, password, port);
            } catch (SQLException e) {
                logger.error("Could not get MySQLAdaptor for test_reactome_" + reactomeVersion + " on host "
                    + releaseHost, e);
                continue;
            }

            for (GKInstance instance : getInstances(releaseDBA, Arrays.asList(ReactomeJavaConstants.Event,
                    ReactomeJavaConstants.PhysicalEntity, ReactomeJavaConstants.Regulation))) {
                //TODO Extract the relevant instance information and store it in the new stable id history database
            }
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

    private static MySQLAdaptor getDBA(String host, String database, String user, String password, int port)
            throws SQLException {
        return new MySQLAdaptor(host, database, user, password, port);
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
            logger.error("Unable to fetch pathways from database", e);
            System.exit(1);
        }

        return instances;
    }

    private static String getStableIdentifier(GKInstance instance) {
        String stableIdentifier = "";

        try {
            stableIdentifier = (String) instance.getAttributeValue(ReactomeJavaConstants.stableIdentifier);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return stableIdentifier;
    }
}
