import java.io.*;
import java.net.*;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.CRC32;

import beleg.StartPackage;

class Client {
	private static final int PAKETSIZE = 1500; //65536 - 29;
	private static byte[] sessionNumber = new byte[2];
	private static byte packageNumber = 0;
	
	// received variables
	private	static byte[] sessionNumberReceived = new byte[2];
	private static byte packageNumberReceived = -1;
	
	
	private static CRC32 crcData = new CRC32();
	
	public static void main( String argv[]) throws Exception
	{
		if(argv.length == 3)
		{
			// arguments
			String host = argv[0];
			int port = Integer.parseInt(argv[1]);
			String fileNameString = argv[2];
			
			ByteBuffer buf;
			ByteBuffer bufReceive;
			boolean first = true;
			
			// file
			File file = new File(fileNameString);
			FileInputStream fis = new FileInputStream(file);
//			int fileDataCounter = 0;
			
			
			// connection stuff
			System.out.println("Connect...");
			DatagramSocket clientSocket = new DatagramSocket();
			clientSocket.setSoTimeout(1000);
			

			// send and receive Buffer
			byte[] sendData = new byte[PAKETSIZE];
			byte[] receiveData = new byte[PAKETSIZE];
			
			
			// set ip address
			InetAddress IPAddress = InetAddress.getByName(host);
			
			// create send packet
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
			
			// create receive packet
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			

			// send loop
			while(fis.available() > 0)
			{
				if(first)
				{
					// First sending process (startPackage)
					createStartPackage(sendData, file);

			    	first = false;
				}
				else
				{
					// continue sending process (dataPackage)
					createDataPackage(sendData, fis, crcData);
				}
				
				// send
				sendPacket.setData(sendData);
				clientSocket.send(sendPacket);
				System.out.println("Package sent");
				
				
				// receive
				try
				{
					receivePacket.setData(receiveData);
					clientSocket.receive(receivePacket);
					System.out.println("Package received");
				}
				catch (SocketTimeoutException e)
				{
					System.out.println("Timeout occured!");
					break;
				}
					
		        // read out the session- and PackageNumber and check if they are correct
				checkSNandPN(receiveData);
/**/		    // what to do?, send package again
			    
				// prepare for next send process
				sendData = new byte[PAKETSIZE];
				receiveData = new byte[PAKETSIZE];
			    
				// flip packageNumber
				packageNumber = flip(packageNumber);
			}
			
			
			
			
			
			
			
			
			
			// last paket
/**/		System.out.println("Done with the file, now follows the crc");

			createLastDataPackage(sendData, crcData);
			
			
			// send
			sendPacket.setData(sendData);
			clientSocket.send(sendPacket);
			System.out.println("Package sent");
			
			
			// receive
			try
			{
				receivePacket.setData(receiveData);
				clientSocket.receive(receivePacket);
				System.out.println("Package received");
			}
			catch (SocketTimeoutException e)
			{
				System.out.println("Timeout occured!");
			}
				
	        // read out the session- and PackageNumber and check if they are correct
			checkSNandPN(receiveData);

		    clientSocket.close();
		}
		else
			System.out.println("Not the correct amount of arguments");
	}

	
	
	
	
	
	
	// Functions
	/***********************************************************************************************************/
	/***********************************************************************************************************/
	/***********************************************************************************************************/
	/***********************************************************************************************************/
	/***********************************************************************************************************/
	/***********************************************************************************************************/
	
	public static int checkSNandPN(byte[] receiveData)
	{
        ByteBuffer buf = ByteBuffer.wrap(receiveData);
		buf.get(sessionNumberReceived);
		packageNumberReceived = buf.get();
		
	    if(!Arrays.equals(sessionNumber, sessionNumberReceived))
	    {
	    	System.out.println("SN is incorrect");
	    	return 1;
	    }
	    
	    if (packageNumber != (packageNumberReceived))
		{
			System.out.println("PN is incorrect");
			return 2;
		}
		
		return 0;
	}
	
	
	
	public static byte flip(byte var)
	{
		// flip packageNumber
		if(var == 0)
			return 1;
		else
			return 0;
	}
	
	
	public static void createStartPackage(byte[] sendData, File file)
	{
		byte[] start = "Start".getBytes(StandardCharsets.US_ASCII);	// is always 5 byte
		byte[] fileLength = new byte[8];
		byte[] fileNameLength = new byte[2];
		byte[] fileName = file.getName().getBytes(StandardCharsets.UTF_8); // is as long as expected  
		byte[] crc = new byte[4];
		byte[] sendPacketWithoutCRC;
		
		// session number
		new Random().nextBytes(sessionNumber);
		
		// file length
		ByteBuffer buf = ByteBuffer.wrap(fileLength);
		buf.allocate(fileLength.length);
	    buf.putLong(file.length());
	    
	    
	    // file name length
	    buf = ByteBuffer.wrap(fileNameLength);
	    buf.allocate(fileNameLength.length);
/**/	buf.putShort((short)fileName.length);


		// prepare start package
		buf = ByteBuffer.wrap(sendData);
		buf.put(sessionNumber);
		buf.put(packageNumber);
		buf.put(start);
		buf.put(fileLength);
		buf.put(fileNameLength);
		buf.put(fileName);

		
		// crc
/**/	putIntintoByteBuffer(crc, getCRC(sendData, sessionNumber.length + 1 + start.length + fileLength.length + fileNameLength.length + fileName.length));
		
		buf.put(crc);
		System.out.println("..sendData contains values up to CRC32");
		
		// print sendData
		printByteArray(sessionNumber);
		printByteArray(packageNumber);
		printByteArray(start);
		printByteArray(fileLength);
		printByteArray(fileNameLength);
		printByteArray(fileName);
		 printByteArray(crc);
		System.out.println();
		
	}
	
	public static void printByteArray(byte[] array)
	{
		System.out.println();
		
		for(int i = 0; i < array.length; i++)
		{
			System.out.print(array[i] + " ");
		}
		
		System.out.println();
	}
	
	public static void printByteArray(byte array)
	{
		System.out.println();
		
		System.out.print(array);
		
		System.out.println();
	}

	public static void createDataPackage(byte[] sendData, FileInputStream fis, CRC32 crcData) throws IOException
	{	
		// prepare data package
		ByteBuffer buf = ByteBuffer.wrap(sendData);
		buf.put(sessionNumber);
		buf.put(packageNumber);
		
		// get data out of file
		byte[] data;
		
		if(fis.available() > PAKETSIZE - (sessionNumber.length + 1))
			data = new byte[PAKETSIZE - (sessionNumber.length + 1)];
		else 
			data = new byte[fis.available()];
		
		fis.read(data);
		
		// add fileData to sendData
		buf.put(data);
		
		// get new data in crcData
		crcData.update(data);
//		System.out.println("CRC: " + (int)crcData.getValue());
		
	}
	
	public static void createLastDataPackage(byte[] sendData, CRC32 crcData)
	{	
		// prepare data package
		ByteBuffer buf = ByteBuffer.wrap(sendData);
		buf.put(sessionNumber);
		buf.put(packageNumber);
		
		// add fileData to sendData
		byte[] crc = new byte[4];
			
		System.out.println("CRC: " + (int)crcData.getValue());
		
/**/	putIntintoByteBuffer(crc, (int)crcData.getValue());
		buf.put(crc);
	}
	
	
	public static void putIntoByteBuffer(byte[] bufInto)
	{
		ByteBuffer buf = ByteBuffer.wrap(bufInto);
		buf.put(bufInto);
	}
	
	public static void putIntintoByteBuffer(byte[] bufInto, int integer)
	{
		ByteBuffer buf = ByteBuffer.wrap(bufInto);
		buf.allocate(bufInto.length);
		buf.putInt(integer);
	}
	
	public static int getCRC(byte[] src, int dataLength)
	{
		// prepare CRC Array
		byte[] sendPacketWithoutCRC = new byte[dataLength];
		ByteBuffer buf = ByteBuffer.wrap(sendPacketWithoutCRC);
		buf.put(src, 0, sendPacketWithoutCRC.length);
		
		// CRC
		CRC32 crc = new CRC32();
		crc.update(sendPacketWithoutCRC);
		return (int)crc.getValue();
	}
}







/*
// crc test
String bString = "123456789";
byte[] a = new byte[4];
byte[] b = bString.getBytes(StandardCharsets.US_ASCII);	
byte[] CRC = new byte[4];

CRC32 bla = new CRC32();
bla.update(b);
System.out.println("CRC: " + Long.toHexString(bla.getValue()));
System.out.println("CRC: " + bla.getValue());
*/





/*
//FileInputStream fis = new FileInputStream(file);
			
 * 
// equal?
if(Arrays.equals(sessionNumber, receiveData))
	System.out.println("Gleich");
else
	System.out.println("Ungleich!");
*/


// Datei übertragung
/*
// if start package was successful
while(fis.read(bArray) != -1)
{
	// send
	outToServer.write(sendData, 0, sendData.length);

	// receive
	inFromServer.readLine();
}
*/

// close socket
//clientSocket.close();

// close file input stream
//fis.close();


/*
ByteBuffer b = ByteBuffer.allocate(2);
b.order(ByteOrder.BIG_ENDIAN); // optional, the initial order of a byte buffer is always BIG_ENDIAN.
b.putShort((short)5);
byte[] result = b.array();
*/
