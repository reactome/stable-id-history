package org.reactome.release.stableIdParser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class StableIdJsonParser {

	public static JSONObject parseStableIdJSONFile(String filePathAsString) throws ParseException, IOException {
		JSONParser jsonParser = new JSONParser();
		return (JSONObject) jsonParser.parse(new BufferedReader(new FileReader(filePathAsString)));
	}

	public static JSONObject getJSONObjectForStableId(String filePathAsString, String stableId)
		throws ParseException, IOException {
		return (JSONObject) parseStableIdJSONFile(filePathAsString).get(stableId);
	}

	public static void main(String[] args) throws ParseException, IOException {
		// Testing
		System.out.println(getJSONObjectForStableId("stableId19.json", "REACT_9193").toJSONString());
	}
}
