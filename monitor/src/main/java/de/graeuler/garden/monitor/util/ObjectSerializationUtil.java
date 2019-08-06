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

public class ObjectSerializationUtil {

	private final static Logger log = LoggerFactory.getLogger(ObjectSerializationUtil.class);
	
	private final static int CHUNK_SIZE = 4096;

	public static <T extends Serializable> T deserializeFromByteStream(InputStream stream, Class<T> genericType) throws ClassCastException{
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream(4096);
			byte[] chunk = new byte[CHUNK_SIZE];
			int nRead;
			bos.reset();
			while ((nRead = stream.read(chunk)) != -1){
				bos.write(chunk, 0, nRead);
			}
			bos.flush();
			byte [] buf = bos.toByteArray();
			ByteArrayInputStream bis = new ByteArrayInputStream(buf);
			ObjectInputStream ois = new ObjectInputStream(bis);
			Object object = ois.readObject();
			if (genericType.isAssignableFrom(object.getClass())) {
				return genericType.cast(object);
			} else {
				throw new ClassCastException("Deserialized object is not an instance of DataRecord.");
			}
		}
		catch (IOException e) {
			log.error("Unable to read bytes from stream: {}", e.getMessage());
		}
		catch (ClassCastException e) {
			log.error("Unable to cast deserialized object back to original type: {}", e.getMessage());
		}
		catch (ClassNotFoundException e) {
			log.error("Unable to read serialized object from stream: {}", e.getMessage());
		}
		return null;
	}

	public static byte[] serializeToByteArray(Serializable serializable) {
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream(4096);
		try {
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(serializable);
			oos.flush();
			byte[] recordAsByteArray = bos.toByteArray();
			oos.close();
			bos.close();
		
			return recordAsByteArray;
		}
		catch (IOException e) {
			log.error("Unable to serialize object", e);
		}
		return null;
	}

}
