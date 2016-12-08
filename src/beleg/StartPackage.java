package beleg;

import java.io.File;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.zip.CRC32;

public class StartPackage/* extends OwnPackage */{

	private static byte[] sessionNumber = new byte[2];
	private static byte packageNumber = 0;
	private static byte[] start;
	private static byte[] fileLength = new byte[8];
	private static byte[] fileNameLength = new byte[2];
	private static byte[] fileName;// = file.getName().getBytes(StandardCharsets.UTF_8); // is as long as expected  
	private static byte[] crc = new byte[4];
	private static byte[] data;
	private static ByteBuffer buf;
	
	public StartPackage(byte[] data) 
	{
		this.data = data.clone();
	}
	
	public StartPackage(File file, int length)
	{
		data = new byte[length];
		
		// random sessionNumber
		new Random().nextBytes(sessionNumber);
		packageNumber = 0;
		
		this.start = "Start".getBytes(StandardCharsets.US_ASCII);	// is always 5 byte
		
		// file length
		buf = ByteBuffer.wrap(fileLength);
		buf.allocate(fileLength.length);
	    buf.putLong(file.length());
		
		// file name
		this.fileName = file.getName().getBytes(StandardCharsets.UTF_8); // is as long as expected
	    
	    // file name length
	    buf = ByteBuffer.wrap(fileNameLength);
	    buf.allocate(fileNameLength.length);
/**/	buf.putShort((short)fileName.length);
		
		// put everything into data
		buf = ByteBuffer.wrap(data);
		buf.put(sessionNumber);
		buf.put(packageNumber);
		buf.put(start);
		buf.put(fileLength);
		buf.put(fileNameLength);
		buf.put(fileName);


		// calculate crc
		calcCRC();
	}
	
	// getter
	public static byte[] getSessionNumber() {
		return sessionNumber;
	}
	
	public static byte getPackageNumber() {
		return packageNumber;
	}
	
	// setter
	
	
	public static void printByteArray(byte[] array)
	{
		System.out.println();
		
		for(int i = 0; i < array.length; i++)
		{
			System.out.print(array[i] + " ");
		}
	}
	
	public static void print()
	{
		// print sendData
		printByteArray(sessionNumber);
		System.out.println(packageNumber);
		printByteArray(start);
		printByteArray(fileLength);
		printByteArray(fileNameLength);
		printByteArray(fileName);
		System.out.println();

		System.out.println("\n\n\nBLA\n\n\n");
	}
	
	
	public static void calcCRC()
	{
		byte[] startWithoutCRC = new byte[sessionNumber.length + 1 + start.length + fileLength.length + fileNameLength.length + fileName.length];
		
		// prepare CRC Array
		buf = ByteBuffer.wrap(startWithoutCRC);
		buf.put(data, 0, startWithoutCRC.length);
		
		// CRC
		CRC32 crcCheck = new CRC32();
		crcCheck.update(startWithoutCRC);
/**/	crcCheck.getValue();

		buf = ByteBuffer.wrap(crc);
		buf.allocate(crc.length);
		buf.putInt((int)crcCheck.getValue());
	}

}
