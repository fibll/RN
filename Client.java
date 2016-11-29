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

class Client {
	public static void main( String argv[]) throws Exception
	{
		if(argv.length == 3)
		{
			// arguments
			String host = argv[0];
			int port = Integer.parseInt(argv[1]);
			String fileNameString = argv[2];
			
			// other
			byte[] sessionNumber = new byte[2];
			byte[] sessionNumberReceived = new byte[2];

			byte packageNumber = 0;
			byte packageNumberReceived = -1;
			
			ByteBuffer buf;
			ByteBuffer bufReceive;
			boolean first = true;
			
			// file
			File file = new File(fileNameString);
			FileInputStream fis = new FileInputStream(file);
			
			
			// connection stuff
			System.out.println("Connect...");
			DatagramSocket clientSocket = new DatagramSocket();
			clientSocket.setSoTimeout(1000);
			

			// send and receive Buffer
			System.out.println(clientSocket.getReceiveBufferSize());
/**/		byte[] sendData = new byte[65536 - 29];
/**/		byte[] receiveData = new byte[65536 - 29];
			
			
			// set ip address
			InetAddress IPAddress = InetAddress.getByName(host);
			
			// create send packet
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
			
			// create receive packet
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			

			// send loop
//			while(fis.available() > 0)
			for(int i = 0; i < 5; i++)
			{
				if(first)
				{
					// First sending process (startPackage)
					createStartPackage(sendData, sessionNumber, packageNumber, file);

			    	first = false;
				}
				else
				{
					// continue sending process (dataPackage)
					createDataPackage(sendData, sessionNumber, packageNumber, file);		
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
		        bufReceive = ByteBuffer.wrap(receiveData);
				bufReceive.get(sessionNumberReceived);
				packageNumberReceived = bufReceive.get();
				
			    if(!Arrays.equals(sessionNumber, sessionNumberReceived))
					System.out.println("SN is incorrect");
				if (packageNumber != (packageNumberReceived))
					System.out.println("PN is incorrect");
			    
			    
				// prepare for next send process
				sendData = new byte[1024];
				receiveData = new byte[1024];
			    
				// flip packageNumber
				packageNumber = flip(packageNumber);
			}		    
		    
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
	public static byte flip(byte var)
	{
		// flip packageNumber
		if(var == 0)
			return 1;
		else
			return 0;
	}
	
	
	public static void createStartPackage(byte[] sendData, byte[] sessionNumber, byte packageNumber, File file)
	{
		byte[] start = "start".getBytes(StandardCharsets.US_ASCII);	// is always 5 byte
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
	    buf.putLong(file.getTotalSpace());
	    
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
/**/	putIntintoByteBuffer(crc, getCRC(sendData, crc, sessionNumber.length + 1 + start.length + fileLength.length + fileNameLength.length + fileName.length));
		
		buf.put(crc);
		System.out.println("..sendData contains values up to CRC32");

	}

	public static void createDataPackage(byte[] sendData, byte[] sessionNumber, byte packageNumber, FileInputStream fis)
	{
		byte[] data = new byte[];
		
		// prepare data package
		ByteBuffer buf = ByteBuffer.wrap(sendData);
		buf.put(sessionNumber);
		buf.put(packageNumber);
		
		// get data out of file
		fis.read();
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
	
	public static int getCRC(byte[] receiveData, byte[] crc, int dataLength)
	{
		// prepare CRC Array
		byte[] sendPacketWithoutCRC = new byte[dataLength];
		ByteBuffer buf = ByteBuffer.wrap(sendPacketWithoutCRC);
		buf.put(receiveData, 0, sendPacketWithoutCRC.length);
		
		// CRC
		CRC32 crcCheck = new CRC32();
		crcCheck.update(sendPacketWithoutCRC);
		return (int)crcCheck.getValue();
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
