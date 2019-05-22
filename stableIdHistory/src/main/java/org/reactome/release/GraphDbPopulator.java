package org.reactome.release;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.transaction.Transaction;
import org.reactome.release.dbModel.ReactomeInstance;
import org.reactome.release.dbModel.ReactomeVersion;
import org.reactome.release.dbModel.StableId;

import java.io.FileReader;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class GraphDbPopulator {

	public static void main(String[] args) throws IOException, ParseException, java.text.ParseException {
		Configuration conf = new Configuration.Builder()
			.uri("bolt://localhost")
			.credentials("neo4j", "neo4j")
			.build();
		SessionFactory factory = new SessionFactory(conf, "org.reactome.release.dbModel");
		Session session = factory.openSession();
		session.purgeDatabase();

		Map<Integer, Date> releaseVersionToDateMap = getReleaseVersionToDateMap("ReactomeReleaseDates.txt");

		for (int releaseVersion = 19; releaseVersion <= 68; releaseVersion++) {
			JSONObject stableIdJSON = parseJSONFile("stableId" + releaseVersion + ".json");

			String releaseDate = new SimpleDateFormat("yyyy-MM-dd").format(
				releaseVersionToDateMap.get(releaseVersion)
			);
			ReactomeVersion reactomeVersion = new ReactomeVersion(releaseVersion, releaseDate);
			//session.save(reactomeVersion);

			//Transaction tx = session.beginTransaction();

			final int totalCount = stableIdJSON.keySet().size();
			AtomicInteger count = new AtomicInteger();
			stableIdJSON.keySet().stream().limit(1).forEach(key -> {
				if (count.incrementAndGet() % 100 == 0) {
					System.out.println(count + " out of " + totalCount + " " +
						(new Timestamp(new Date().getTime())));
				}
				JSONObject data = (JSONObject) stableIdJSON.get(key);

				String stableId = (String) data.get("stableId");
				System.out.println(reactomeVersion.getReleaseNumber() + ": " + stableId);

				long stableIdMinorVersion = (Long) data.get("stableIdVersion");

				StableId stableIdObject = StableId.get(stableId, stableIdMinorVersion);
				//stableIdObject.addReactomeVersion(reactomeVersion);

				String oldStableId = (String) data.get("oldStableId");
				if (!stableId.equals(oldStableId)) {
					StableId oldStableIdObject = StableId.get(oldStableId);
					stableIdObject.setOldStableId(oldStableIdObject);
					//session.save(oldStableIdObject);
				}
				//session.save(stableIdObject);

				long instanceDbId = (Long) data.get("instanceID");
				String instanceDisplayName = (String) data.get("displayName");
				String instanceSchemaClass = (String) data.get("reactomeClass");
				String instanceType = (String) data.get("instanceType");
				Set<String> instanceSpecies = jsonStringArrayToSet(data.get("instanceSpecies"));

				ReactomeInstance instance = new ReactomeInstance.Builder(instanceDbId)
												.withDisplayName(instanceDisplayName)
												.withSchemaClass(instanceSchemaClass)
												.withType(instanceType)
												.withSpecies(instanceSpecies)
												.build();
				System.out.println(instance.id);
				//session.save(instance);
				//System.out.println(instance.id);

				instance.setOwnedStableId(stableIdObject);
				//instance.addReactomeVersion(reactomeVersion);

				//session.save(instance);

				//reactomeVersion.addOwnedStableId(stableIdObject);
				reactomeVersion.addOwnedInstance(instance);

				//session.save(instance);
			});
			//tx.commit();
			session.save(reactomeVersion);
		}
		factory.close();
	}

	private static JSONObject parseJSONFile(String fileName) throws IOException, ParseException {
		JSONParser parser = new JSONParser();
		Object obj =  parser.parse(new FileReader(fileName));
		return (JSONObject) obj;
	}

	private static Map<Integer, Date> getReleaseVersionToDateMap(String tsvVersionToDateFile)
		throws IOException, java.text.ParseException {
		Map<Integer, Date> releaseVersionToDateMap = new HashMap<>();

		//TsvParserSettings settings = new TsvParserSettings();
		//settings.getFormat().setLineSeparator("\n");

		//TsvParser parser = new TsvParser(settings);

		//List<String[]> rows = parser.parseAll(new FileReader(tsvVersionToDateFile));
		List<String> rows = Files.readAllLines(Paths.get(tsvVersionToDateFile));


		for (String row : rows) {
			String[] columns = row.split("\t");

			int releaseVersion = Integer.parseInt(columns[0]);
			Date releaseDate = getDate(columns[1]);

			releaseVersionToDateMap.put(releaseVersion, releaseDate);
		}

		return releaseVersionToDateMap;
	}

	private static Date getDate(String dateString) throws java.text.ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
		return dateFormat.parse(dateString);
	}

	private static Set<String> jsonStringArrayToSet(Object jsonStringArrayObj) {
		JSONArray jsonStringArray = (JSONArray) jsonStringArrayObj;

		Set<String> stringSet = new HashSet<>();
		if (jsonStringArray == null) {
			return stringSet;
		}

		for (int i = 0; i < jsonStringArray.size(); i++) {
			stringSet.add((String) jsonStringArray.get(i));
		}

		return stringSet;
	}
}
