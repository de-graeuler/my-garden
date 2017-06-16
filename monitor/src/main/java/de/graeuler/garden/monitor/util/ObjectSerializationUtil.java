package de.graeuler.garden.monitor.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.graeuler.garden.data.model.DataRecord;

public class ObjectSerializationUtil {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	public DataRecord<Serializable> deserializeFromByteStream(InputStream stream) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] chunk = new byte[1024];
			int nRead;
			bos.reset();
			while ((nRead = stream.read(chunk)) > 0){
				bos.write(chunk,0, nRead);
			}
			bos.flush();
			byte [] buf = bos.toByteArray();
			ByteArrayInputStream bis = new ByteArrayInputStream(buf);
			ObjectInputStream ois = new ObjectInputStream(bis);
			Object o = ois.readObject();
			@SuppressWarnings("unchecked")
			DataRecord<Serializable> dr =  (DataRecord<Serializable>) o;
			return dr;
		}
		catch (IOException e) {
			log.error("Unable to read bytes from stream", e);
		}
		catch (ClassCastException e) {
			log.error("Unable to cast deserialized object back to original type.", e);
		}
		catch (ClassNotFoundException e) {
			log.error("Unable to read serialized object from stream.", e);
		}
		return null;
	}

	public InputStream serializeToByteStream(DataRecord<Serializable> record) {
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(record);
			oos.flush();
			byte[] recordAsByteArray = bos.toByteArray();
			oos.close();
			bos.close();
		
			return new ByteArrayInputStream(recordAsByteArray);
		}
		catch (IOException e) {
			log.error("Unable to write serialized object to stream.", e);
		}
		return null;
	}

}
