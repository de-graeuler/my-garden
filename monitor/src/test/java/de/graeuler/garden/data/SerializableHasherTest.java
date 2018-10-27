package de.graeuler.garden.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import de.graeuler.garden.testhelpers.DataRecordBuilder;

public class SerializableHasherTest {

	private SerializableToSha256 testee;

	@Before
	public final void setUp() {
		testee = new SerializableToSha256();
	}
	
	@Test
	public final void testHash() {
		String referenceValue = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		String referenceHash = "d6ec6898de87ddac6e5b3611708a7aa1c2d298293349cc1a6c299a1db7149d38";
		StringBuffer hash = new StringBuffer();
		testee.hash(referenceValue.getBytes(), hash);
		assertEquals(referenceHash, hash.toString());
	}

	@Test
	public final void testHashUniqueness() {
		Set<String> uniqueHashes = new HashSet<>();
		Collection<String> allDataRecordHashes;

		allDataRecordHashes= DataRecordBuilder.stream(1000000).parallel().map(calculateHash()).collect(Collectors.toList());
		assertTrue(uniqueHashes.addAll(allDataRecordHashes));
		
		uniqueHashes.clear();
		allDataRecordHashes.clear();
		
		allDataRecordHashes.addAll(DataRecordBuilder.stream(1000000, 1).parallel().map(calculateHash()).collect(Collectors.toList()));
		assertTrue(uniqueHashes.addAll(allDataRecordHashes));
	}
	
	@Test
	public final void testHashConsistancy() {
		Collection<DataRecord> allDataRecords = DataRecordBuilder.stream(1000000, 1).parallel().collect(Collectors.toList());
		Set<String> uniqueHashes = allDataRecords.parallelStream().map(calculateHash()).collect(Collectors.toCollection(() -> Collections.synchronizedSet(new HashSet<String>())));
		assertEquals(allDataRecords.size(), uniqueHashes.size());
		allDataRecords.parallelStream().map(calculateHash()).forEach(uniqueHashes::remove);
		assertTrue(uniqueHashes.isEmpty());
	}

	private Function<? super DataRecord, ? extends String> calculateHash() {
		return record -> {
			StringBuffer hash = new StringBuffer();
			testee.hash(record, hash);
			return hash.toString();
		};
	}
	
	
}
