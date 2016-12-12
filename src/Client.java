import java.io.*;
import java.net.*;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;
import java.util.Set;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.CRC32;

import org.omg.CORBA.PRIVATE_MEMBER;

import beleg.OwnPackage;
import beleg.StartPackage;

class Client {
	private static final int PAKETSIZE = 1500; //65536 - 29;
	private static byte[] sessionNumber = new byte[2];
	private static byte packageNumber = 0;
	
	// received variables
	private	static byte[] sessionNumberReceived = new byte[2];
	private static byte packageNumberReceived = -1;
	
	private static DatagramSocket clientSocket;
	private static DatagramPacket sendPacket;
	private static DatagramPacket receivePacket;
	
	private static byte[] sendData;
	
	private static CRC32 crcData = new CRC32();
	
	private static boolean repeat = false;
	
	private static int packageCounter = 0;
	
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
			
			
			// connection stuff
			System.out.println("Connect...");
			clientSocket = new DatagramSocket();
			clientSocket.setSoTimeout(1000);
			

			// send and receive Buffer
			sendData = new byte[PAKETSIZE];
			byte[] receiveData = new byte[PAKETSIZE];
			
			// set ip address
			InetAddress IPAddress = InetAddress.getByName(host);
			
			// initialize send packet
			sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
			
			// initialize receive packet
			receivePacket = new DatagramPacket(receiveData, receiveData.length);
			

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
//					if(!repeat)
						createDataPackage(fis, crcData);
//					else
//						repeat = false;
				}				
				
				// send and receive
				if(sendAndReceive(receiveData) == 0)
				{
					System.out.println("Server is not reachable");
					return;
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
			
			// last package
/**/		System.out.println("--- last package ---");

			createLastDataPackage(crcData);
			
			
			// send and receive
			if(sendAndReceive(receiveData) == 0)
			{
				System.out.println("Server is not reachable!");
				return;
			}
			
/**/        // read out the session- and PackageNumber and check if they are correct, if packagenumber is old should it resend?
			if(checkSNandPN(receiveData) == 0)
				System.out.println("File fully transfered!\n--------------------------------");

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
	/**
	 * @throws IOException *********************************************************************************************************/
	
	public static void send() throws IOException
	{
		System.out.println("\n" + packageCounter);
		packageCounter++;
		
		sendPacket.setData(sendData);
		clientSocket.send(sendPacket);
		System.out.println("SN: " + sendData[0] + " " + sendData[1]);
		System.out.println("PN: " + sendData[2]);
		System.out.println("Package sent");
	}
	
	public static int sendAndReceive(byte[] receiveData) throws IOException
	{
		int i = 0;
		int iMax = 10;
		
		for(i = 0; i < iMax; i++)
		{
			try
			{
				send();
				
				receivePacket.setData(receiveData);
				clientSocket.receive(receivePacket);
				System.out.println("Package received");
				break;
			}
			catch (SocketTimeoutException e)
			{
				System.out.println("------ Timeout occured!");
				repeat = true;
				packageCounter--;
			}
		}
		if(i == iMax)
			return 0;
		else
			return 1;
	}
	
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
/**/	calcCRC(crc, sendData, sessionNumber.length + 1 + start.length + fileLength.length + fileNameLength.length + fileName.length);


		buf.put(crc);
		System.out.println("..sendData contains values up to CRC32");		
	}
	
	public static void printByteArray(byte[] array)
	{
//		System.out.println();
		
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

	public static void createDataPackage(FileInputStream fis, CRC32 crcData) throws IOException
	{
		// get data out of file
		byte[] data;		
				
		// is more or an equal amount of bytes remaining in the file? If not set byte[] to the remaining amount of bytes
		if(fis.available() >= PAKETSIZE - (sessionNumber.length + 1))
			data = new byte[PAKETSIZE - (sessionNumber.length + 1)];
		else 
		{
			data = new byte[fis.available()];
			sendData = new byte[fis.available() + sessionNumber.length + 1];
		}
		
		fis.read(data);

		// prepare data package
		ByteBuffer buf = ByteBuffer.wrap(sendData);
		System.out.println("Before: " + sendData[0] + " " + sendData[1]);
		buf.put(sessionNumber);
		System.out.println("After: " + sendData[0] + " " + sendData[1]);
		buf.put(packageNumber);
		
		// add fileData to sendData
		buf.put(data);
		
		// get new data in crcData
		crcData.update(data);		
	}
	
	public static void createLastDataPackage(CRC32 crcData)
	{	
		// add fileData to sendData
		byte[] crc = new byte[4];

		// prepare data package
		sendData = new byte[sessionNumber.length + 1 + crc.length];
		
		ByteBuffer buf = ByteBuffer.wrap(sendData);
		buf.put(sessionNumber);
		buf.put(packageNumber);
		
/**/	putIntintoByteBuffer(crc, (int)crcData.getValue());
		buf.put(crc);
		
		printByteArray(crc);
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
	
	public static void calcCRC(byte[] crc, byte[] data, int length)
	{
		byte[] startWithoutCRC = new byte[length];
		
		// prepare CRC Array
		ByteBuffer buf = ByteBuffer.wrap(startWithoutCRC);
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