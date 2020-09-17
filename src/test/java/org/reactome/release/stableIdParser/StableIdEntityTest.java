package org.reactome.release.stableIdParser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactome.release.stableIdParser.StableIdEntity.InstanceType;

public class StableIdEntityTest {
	private StableIdEntity stableIdEntity;
	private final int MOCK_STABLE_ID_ENTITY_VERSION = 19;

	@BeforeEach
	public void createStableIdEntity() throws IOException, ParseException {
		String sampleStableIdFilePath = getSampleStableIdFilePath();

		this.stableIdEntity = StableIdEntity.getStableIdEntity(
			MOCK_STABLE_ID_ENTITY_VERSION,
			StableIdJsonParser.parseStableIdJSONFile(sampleStableIdFilePath)
		);
	}

	@Test
	public void stableIdEntryHasCorrectStableIdentifierValue() {
		assertThat(this.stableIdEntity.getStableId(), is(equalTo("R-HSA-1236834")));
	}

	@Test
	public void stableIdEntryHasCorrectStableIdentifierVersionValue() {
		assertThat(this.stableIdEntity.getStableIdVersion(), is(equalTo(1)));
	}

	@Test
	public void stableIdEntryHasCorrectOldStableId() {
		assertThat(this.stableIdEntity.getOldStableId(), is(equalTo("REACT_111642 (Q9UIQ6)")));
	}

	@Test
	public void stableIdEntryHasCorrectInstanceDbId() {
		assertThat(this.stableIdEntity.getInstanceDbId(), is(equalTo(1236834L)));
	}

	@Test
	public void stableIdEntryHasCorrectInstanceName() {
		assertThat(this.stableIdEntity.getInstanceName(), is(equalTo("LNPEP [early endosome lumen]")));
	}

	@Test
	public void stableIdEntryHasCorrectInstanceClass() {
		assertThat(this.stableIdEntity.getInstanceClass(), is(equalTo("EntityWithAccessionedSequence")));
	}

	@Test
	public void stableIdEntryHasCorrectInstanceType() {
		assertThat(this.stableIdEntity.getInstanceType(), is(equalTo(InstanceType.MANUAL)));
	}

	@Test
	public void stableIdEntryHasCorrectInstanceSpecies() {
		assertThat(this.stableIdEntity.getInstanceSpecies(), contains("Homo sapiens"));
	}

	@Test
	public void stableIdEntryHasCorrectReleaseNumber() {
		assertThat(this.stableIdEntity.getReleaseVersion(), is(equalTo(MOCK_STABLE_ID_ENTITY_VERSION)));
	}

	@Test
	public void getStableIdEntityByStableIdentifier() throws IOException {
		Map<String, List<StableIdEntity>> stableIdMap = StableIdEntity.getStableIdEntitiesByStableIdentifier(
			getFilePathsAsStrings()
		);

		List<StableIdEntity> stableIdEntities = stableIdMap.get("REACT_1000");
		System.out.println(stableIdEntities.get(0));
		System.out.println(stableIdEntities.get(stableIdEntities.size() - 1));
	}

	private static List<String> getFilePathsAsStrings() throws IOException {
		ClassLoader classLoader = StableIdEntityTest.class.getClassLoader();
		URL stableIdJsonFolder = classLoader.getResource("stableIdJson");
		String path = Objects.requireNonNull(stableIdJsonFolder).getPath();
		return Files.list(Paths.get(path))
			.filter(Files::isRegularFile)
			.map(filePath -> filePath.toString())
			.peek(System.out::println)
			.limit(5)
			.collect(Collectors.toList());
	}

	private String getSampleStableIdFilePath() {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("sampleStableId.json").getFile());
		return file.getAbsolutePath();
	}
}
