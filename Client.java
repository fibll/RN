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
			String startString = "start";
			byte[] sendData = new byte[1024];
			byte[] receiveData = new byte[1024];
			ByteBuffer bufReceive;
			ByteBuffer bufSend;
			ByteBuffer buf;
			boolean first = true;
			
			// file
			File file = new File(fileNameString);
			
			// all the special byte arrays
			byte[] sessionNumber = new byte[2];
			byte[] sessionNumberReceived = new byte[2];
			
			byte packageNumber = 0;
			byte packageNumberReceived = -1;
			
			byte[] start = startString.getBytes(StandardCharsets.US_ASCII);	// is always 5 byte
			
			byte[] fileLength = new byte[8];
			byte[] fileNameLength = new byte[2];
			byte[] fileName = fileNameString.getBytes(StandardCharsets.UTF_8); // is as long as expected  
			
			byte[] crc = new byte[4];
			byte[] sendPacketWithoutCRC;
			
			// session number
			new Random().nextBytes(sessionNumber);
			
			// file length
			buf = ByteBuffer.wrap(fileLength);
			buf.allocate(fileLength.length);
		    buf.putLong(file.getTotalSpace());
		    
		    // file name length
		    buf = ByteBuffer.wrap(fileNameLength);
		    buf.allocate(fileNameLength.length);
/*??????*/  buf.putShort((short)fileName.length);
			
			// connection stuff
			System.out.println("Connect...");
			DatagramSocket clientSocket = new DatagramSocket();
			
			// set ip address
			InetAddress IPAddress = InetAddress.getByName(host);
			
			// create send packet
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port); 
			
			// create receive packet
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			
// loop with condition: if boolean first = true, then startPacket, else Datapacket
			for(int i = 0; i < 2; i++)
			{
				if(first)
				{
					System.out.println("\n\nFirst Time");
					// prepare start package
					bufSend = ByteBuffer.wrap(sendData);
					bufSend.put(sessionNumber);
					bufSend.put(packageNumber);
					bufSend.put(start);
					bufSend.put(fileLength);
					bufSend.put(fileNameLength);
					bufSend.put(fileName);
					
					System.out.println("CRC check...");

					
					// crc
					int bla = getCRC(sendData, crc, sessionNumber.length + 1 + start.length + fileLength.length + fileNameLength.length + fileName.length);
/*????????????*/	putIntintoByteBuffer(crc, bla);
					System.out.println(bla);
					
					bufSend.put(crc);
					System.out.println("..sendData contains values up to CRC32");
				}
				else
				{
					System.out.println("\n\nSecond Time");
					// prepare data package
					bufSend = ByteBuffer.wrap(sendData);
					bufSend.put(sessionNumber);
					bufSend.put(packageNumber);
				}
							
				// send
				clientSocket.send(sendPacket);
				
				
				// receive
				System.out.println("Package received");
			    clientSocket.receive(receivePacket);
			    
		        // create byteBuffer to read parts of the received Package
		        bufReceive = ByteBuffer.wrap(receiveData);
		        
		        // fill bytebuffer
				bufReceive.get(sessionNumberReceived);
				packageNumberReceived = bufReceive.get();
				
			    if(Arrays.equals(sessionNumber, sessionNumberReceived) && (packageNumber == packageNumberReceived))
					System.out.println("ACK stimmt überein");
				else
					System.out.println("ACK stimmt nicht");
			    
				// prepare for next send process
				sendData = new byte[1024];
				receiveData = new byte[1024];
			    packageNumber++;
			    
			    if(first)
			    	first = false;

			}
			// loop end
		    
		    		    
		    clientSocket.close();
		}
		else
			System.out.println("Not the correct amount of arguments");
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
