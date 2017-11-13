package de.graeuler.garden.monitor.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;

import org.junit.Test;

import de.graeuler.garden.data.DataRecord;

public class ObjectSerializationUtilTest {

	ObjectSerializationUtil objectSerializationUtil = new ObjectSerializationUtil();
	
	@Test
	public final void testDeSerialize() {
		DataRecord<Serializable> drd = new DataRecord<>("double", new Double(1.452));
		InputStream stream = objectSerializationUtil.serializeToByteStream(drd);
		assertNotNull(stream);
		DataRecord<Serializable> result = objectSerializationUtil.deserializeFromByteStream(stream);
		assertNotNull(result);
		Serializable sdrd = drd.getValue();
		Serializable sres = result.getValue();
		assertTrue(sdrd instanceof Double);
		assertTrue(sres instanceof Double);
		assertEquals((Double)sdrd, (Double)sres, 0);
	}
	
	@Test
	public final void testMultiChunkDeSerialize() {
		final int listSize = 1000;
		final int byteArraySize = 1024;
		ArrayList<Byte[]> listOfByte = new ArrayList<>(listSize);
		for(int i = 0; i < listSize; i++) {
			Byte[] bytes = new Byte[byteArraySize];
			for( int j = 0; j < byteArraySize; j++) {
				bytes[j] = new Byte((byte)(Math.random()*255));
			}
			listOfByte.add(bytes);
		}
		DataRecord<Serializable> drLoB = new DataRecord<>("lob", listOfByte);
		InputStream stream = objectSerializationUtil.serializeToByteStream(drLoB);
		assertNotNull(stream);
		DataRecord<Serializable> drResult = objectSerializationUtil.deserializeFromByteStream(stream);
		assertNotNull(drResult);
	}

}
