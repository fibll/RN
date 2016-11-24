import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.CRC32;
import java.io.FileInputStream;


class Server {
	public static void main(String argv[]) throws Exception
	{		
		if(argv.length == 1)
		{
			int port = Integer.parseInt(argv[0]);
			InetAddress IPAddress;
			System.out.println("");
			
			// Socket für Anfragen auf Port (chosen)
			DatagramSocket serverSocket = new DatagramSocket(port); 

			while(true)
			{
				// variables
				String startString = "start";
				byte[] sendData = new byte[1024];
				byte[] receiveData = new byte[1024];
				ByteBuffer bufReceive;
				ByteBuffer bufSend;
				ByteBuffer buf;
				
				// start package
				byte[] sessionNumber = new byte[2];
				byte[] sessionNumberReceived = new byte[2];
				
				byte packageNumber;
				byte packageNumberNext;
				
				byte[] start = startString.getBytes(StandardCharsets.US_ASCII);	// is always 5 byte
				byte[] startReceived = new byte[5];
				
				byte[] fileLength = new byte[8];
				byte[] fileNameLength = new byte[2];
				byte[] fileName;
				
				byte[] crc = new byte[4];
				byte[] sendPacketWithoutCRC;
				
				// open file output stream
				//FileOutputStream fos = new FileOutputStream("outputFile", true);
				
				

// client loop
				// create DatagramPacket
				DatagramPacket receivePacket =  new DatagramPacket(receiveData, receiveData.length);
		        
				// receive
				serverSocket.receive(receivePacket);
				System.out.println("Package received");
		        
		        // create byteBuffer to read parts of the received Package
		        bufReceive = ByteBuffer.wrap(receiveData);
		        
		        // fill bytebuffer
				bufReceive.get(sessionNumberReceived);
				packageNumber = bufReceive.get();
				System.out.println("bytebuffer filled");

		        
				if(!Arrays.equals(sessionNumber, sessionNumberReceived))
				{
					// get from start to FileNameLength
					// fill bytebuffer
			        bufReceive.get(startReceived);
			        bufReceive.get(fileLength);
			        bufReceive.get(fileNameLength);
					
			        // was start text correctly sent?
					if(!Arrays.equals(start, startReceived))
					{
						// Cancel session
						System.out.println("Failure, new sessionNumber but no start string");
					}
					else
					{
						// new session started
						System.out.println("New session");
					
						// save sessionNumber
						sessionNumber = sessionNumberReceived;
			
						// get file name
						buf = ByteBuffer.wrap(fileNameLength);
/*??????*/				fileName = new byte[(int)buf.getShort()];
						bufReceive.get(fileName);
						
						
						// CRC
						int bla = getCRC(receiveData, crc, sessionNumber.length + 1 + start.length + fileLength.length + fileNameLength.length + fileName.length);
						putIntintoByteBuffer(crc, bla);
						System.out.println(bla);
												
						
						// set ip address and port right for the client
						IPAddress = receivePacket.getAddress(); 
						port = receivePacket.getPort();
		  
						// prepare ack
						bufSend = ByteBuffer.wrap(sendData);
						bufSend.put(sessionNumber);
						bufSend.put(packageNumber);
		        
						// create send datagram
						DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
		  
						// send
						serverSocket.send(sendPacket);
						System.out.println("Package sent");
						
						// prepare for next send process
						sendData = new byte[1024];
						receiveData = new byte[1024];
						packageNumberNext = packageNumber++;
					}
				}
				else
				{
					System.out.println("No new session!");
					// more receive
					// get data out of receiveData
					
					// set ip address and port right for the client
					IPAddress = receivePacket.getAddress(); 
					port = receivePacket.getPort();
					
					// more ack
					bufSend = ByteBuffer.wrap(sendData);
					bufSend.put(sessionNumber);
					bufSend.put(packageNumber);
	        
					// create send datagram
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
	  
					// send
					serverSocket.send(sendPacket);
					System.out.println("Package sent");
					
					// initialize packageNumberNext;
					packageNumberNext = packageNumber++;
				}
// client loop end
			}
			// wait for next client
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
	
	public static void bytesAppend(byte[] sourceArray, byte[] appendArray)
	{
		ByteBuffer wrapped = ByteBuffer.wrap(sourceArray);
		wrapped.put(appendArray);
	}
	
	public static void byteAppend(byte[] sourceArray, byte appendByte)
	{
		ByteBuffer wrapped = ByteBuffer.wrap(sourceArray);
		wrapped.put(appendByte);
	}
}



// Datei übertragung
/*
// receive as long as there is something so receive
while(inFromClient.read(receiveData, 0, receiveData.length) >= 0)
{
	// write data into file
//how much byte to go?
	fos.write(receiveData, 0, receiveData.length);		

	// send ack
	outToClient.writeBytes("ACK" + '\n');

	fos.close();
}
*/
