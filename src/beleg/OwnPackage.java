package beleg;

import java.nio.ByteBuffer;
import java.util.Set;


public class OwnPackage {
	
	public static int port;
	public static String ipaddress;
	public static byte[] sessionNumber = new byte[2];
	public static byte packageNumber;
	public static int length;
	public static byte[] data;
	public static int fillCounter;
	public static int readCounter;

	public OwnPackage(int port, String ipaddress, byte[] sessionNumber, byte packageNumber, int length) {
		OwnPackage.port = port;
		OwnPackage.ipaddress = ipaddress;
		OwnPackage.sessionNumber = sessionNumber;
		OwnPackage.packageNumber = packageNumber;
		OwnPackage.length = length;
		data = new byte[length];
		fillCounter = 0;
		readCounter = 0;
	}
	
	public OwnPackage(int length)
	{
		data = new byte[length];
		fillCounter = 0;
		readCounter = 0;
	}
	
	// getter -----
	public static int getPort() {
		return port;
	}
	
	public static String getIpaddress() {
		return ipaddress;
	}
	
	public static byte[] getSessionNumber() {
		return sessionNumber;
	}
	
	public static byte getPackageNumber() {
		return packageNumber;
	}
	
	public static int getLength() {
		return length;
	}
	
	public static byte[] getData() {
		return data;
	}
	
	
	// setter -----
	public static void setPort(int port) {
		OwnPackage.port = port;
	}
	
	public static void setIpaddress(String ipaddress) {
		OwnPackage.ipaddress = ipaddress;
	}
	
	public static void setPackageNumber(byte packageNumber) {
		OwnPackage.packageNumber = packageNumber;
	}
	
	public static void setData(byte[] data) {
		OwnPackage.data = data;
	}
	

	// more -----
	public void catData(byte[] addData) {
		ByteBuffer buf = ByteBuffer.wrap(data);
		buf.position(fillCounter);
		buf.put(addData);
		fillCounter += addData.length;
	}
	
	public void catData(byte addData) {
		ByteBuffer buf = ByteBuffer.wrap(data);
		buf.position(fillCounter);
		buf.put(addData);
		fillCounter++;
	}
	
	public byte[] getNextData(int length)
	{
		byte[] array = new byte[length];
        ByteBuffer buf = ByteBuffer.wrap(data); 
        buf.position(readCounter);
		buf.get(array);
		readCounter += array.length;
        return array;
	}
	
	public byte getNextData()
	{
		byte array;
        ByteBuffer buf = ByteBuffer.wrap(data); 
        buf.position(readCounter); 
        array = buf.get();
		readCounter++;
        return array;
	}

}
