package org.reactome.release;

import org.gk.model.GKInstance;
import org.gk.persistence.MySQLAdaptor;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class InstanceUtilsTest {

	@Test
	public void testIsElectronicallyInferred() throws Exception {
		final boolean MANUALLY_CURATED = false;
		final boolean ELECTRONICALLY_INFERRED = true;

		MySQLAdaptor releaseDBA = InstanceUtils.getDBA("localhost", "test_reactome_59", "",
													   "", 3306);

		InstanceUtils.setCuratorDBA(InstanceUtils.getDBA("localhost", "gk_central", "", "", 3306));

		Map<Integer, Boolean> instanceIdToIsElectronicallyInferred = new HashMap<>();
		instanceIdToIsElectronicallyInferred.put(193508, MANUALLY_CURATED); // manually curated reaction
		instanceIdToIsElectronicallyInferred.put(9063027, ELECTRONICALLY_INFERRED); // elec inferred reaction
		instanceIdToIsElectronicallyInferred.put(5419271, MANUALLY_CURATED); // manually curated black box event
		instanceIdToIsElectronicallyInferred.put(9099164, ELECTRONICALLY_INFERRED); // elec inferred black box event
		instanceIdToIsElectronicallyInferred.put(5229194, MANUALLY_CURATED); // manually curated depolymerisation
		instanceIdToIsElectronicallyInferred.put(9304349, ELECTRONICALLY_INFERRED); // elec inferred depolymerisation
		instanceIdToIsElectronicallyInferred.put(5688025, MANUALLY_CURATED); // manually curated failed reaction
		instanceIdToIsElectronicallyInferred.put(936986, MANUALLY_CURATED); // manually curated polymerisation
		instanceIdToIsElectronicallyInferred.put(9028516, ELECTRONICALLY_INFERRED); // elec inferred polymerisation
		instanceIdToIsElectronicallyInferred.put(182065, MANUALLY_CURATED); // manually curated non-human reaction
		instanceIdToIsElectronicallyInferred.put(372742, MANUALLY_CURATED); // manually inferred reaction
		instanceIdToIsElectronicallyInferred.put(8849347, MANUALLY_CURATED); // manually curated catalyst activity
		instanceIdToIsElectronicallyInferred.put(9176553, ELECTRONICALLY_INFERRED); // elec inferred catalyst activity
		instanceIdToIsElectronicallyInferred.put(5692833, MANUALLY_CURATED); // manually curated negative regulation
		instanceIdToIsElectronicallyInferred.put(9254173, ELECTRONICALLY_INFERRED); // elec inferred negative regulation
		instanceIdToIsElectronicallyInferred.put(1663719, MANUALLY_CURATED); // manually curated positive regulation
		instanceIdToIsElectronicallyInferred.put(9363542, ELECTRONICALLY_INFERRED); // elec inferred positive regulation
		instanceIdToIsElectronicallyInferred.put(111928, MANUALLY_CURATED); // manually curated positive regulation
														// regulating a catalyst activity
		instanceIdToIsElectronicallyInferred.put(8952721, ELECTRONICALLY_INFERRED); // elec inferred positive
		// regulation
														// regulating a catalyst activity
		instanceIdToIsElectronicallyInferred.put(927835, MANUALLY_CURATED); // manually curated other entity
		instanceIdToIsElectronicallyInferred.put(937266, MANUALLY_CURATED); // manually curated simple entity
		instanceIdToIsElectronicallyInferred.put(8944351, MANUALLY_CURATED); // manually curated complex
		instanceIdToIsElectronicallyInferred.put(8962101, ELECTRONICALLY_INFERRED); // elec inferred complex
		instanceIdToIsElectronicallyInferred.put(1629806, MANUALLY_CURATED); // manually curated EWAS
		instanceIdToIsElectronicallyInferred.put(9492442, ELECTRONICALLY_INFERRED); // elec inferred EWAS
		instanceIdToIsElectronicallyInferred.put(5652148, MANUALLY_CURATED); // manually curated GEE
		instanceIdToIsElectronicallyInferred.put(8982667, ELECTRONICALLY_INFERRED); // elec inferred GEE
		instanceIdToIsElectronicallyInferred.put(1467470, MANUALLY_CURATED); // manually curated defined set
		instanceIdToIsElectronicallyInferred.put(8986465, ELECTRONICALLY_INFERRED);// elec inferred defined set
		instanceIdToIsElectronicallyInferred.put(983323, MANUALLY_CURATED); // manually curated polymer
		instanceIdToIsElectronicallyInferred.put(9029501, ELECTRONICALLY_INFERRED); // elec inferred polymer,
		instanceIdToIsElectronicallyInferred.put(1214171, MANUALLY_CURATED); // manually curated cow EWAS (re-used by
												 // elec inferred RLE)

		for (int instanceId : instanceIdToIsElectronicallyInferred.keySet()) {
			GKInstance instance = releaseDBA.fetchInstance((long) instanceId);
			assertEquals(
				instance + " is electronically inferred actual value " + instanceIdToIsElectronicallyInferred.get(instanceId),
				instanceIdToIsElectronicallyInferred.get(instanceId),
				InstanceUtils.isElectronicallyInferred(instance)
			);
		}
	}
}
