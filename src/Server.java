import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;
import java.util.zip.CRC32;
import java.io.FileInputStream;

import sun.security.x509.IPAddressName;

import beleg.OwnPackage;





// Anmerkungen

/*
 * Server benötigt auch einen Timeout beim receive, auf 10s gesetzt
 * 
 * 
 * 
 * 
 * */





class Server {
	
	private static final int PAKETSIZE = 1500; //65536 - 29;
	
	// connection
	private static DatagramSocket serverSocket;
	private static DatagramPacket receivePacket;
	private static DatagramPacket sendPacket;
	
	private static final double lossrate = 0.3;
	private static final int delay = 100;  // milliseconds
	
	private static int packageCounter = 0;
	
	public static void main(String argv[]) throws Exception
	{		
		if(argv.length == 1)
		{
			// variables
			String startString = "Start";
			ByteBuffer buf;
			boolean loop = true;
			
			// start package
			byte[] sessionNumber = new byte[2];
			byte[] sessionNumberReceived = new byte[2];
			
			byte packageNumber = 0;
			byte packageNumberReceived;
			
			byte[] start = startString.getBytes(StandardCharsets.US_ASCII);	// is always 5 byte
			byte[] startReceived = new byte[5];
			
			byte[] fileLength = new byte[8];
			long fileLengthInt = 0;
			
			byte[] fileNameLength = new byte[2];
			byte[] fileName;
			String fileNameString = "fail";
			
			byte[] crc = new byte[4];
			byte[] crcReceived = new byte[4];
			CRC32 crcData = new CRC32();
			
			
			
			
			
		      // Create random number generator for use in simulating 
		      // packet loss and network delay.
		      Random random = new Random();
			
			
			
			
			
			
			
			// file stuff
			byte[] fileData = new byte[PAKETSIZE - (sessionNumber.length + 1)];
			int fileDataCounter = 0;
			
			// connection
			int port = Integer.parseInt(argv[0]);
			InetAddress IPAddress = null;
			
			// Socket für Anfragen auf Port (chosen)
			serverSocket = new DatagramSocket(port);
			
			// send and receive Buffer
/**/		byte[] sendData = new byte[PAKETSIZE];
/**/		byte[] receiveData = new byte[PAKETSIZE];


			// create receive DatagramPacket
			receivePacket =  new DatagramPacket(receiveData, receiveData.length);
			
			// create send DatagramPacket
			sendPacket = new DatagramPacket(sendData, sendData.length);

			// from now on it works with one client
			while(true)
			{
				while(loop)
				{					
					// open file output stream
					FileOutputStream fos = new FileOutputStream(fileNameString, true);
					
					// define packages
					OwnPackage receive = new OwnPackage(PAKETSIZE);
					OwnPackage ack = new OwnPackage(PAKETSIZE);

					// receive				
					receivePacket.setData(receiveData);
					serverSocket.receive(receivePacket); // wird nicht ausgeführt
					receive.setData(receiveData);
					//System.out.println("--------------------------------\n");

					System.out.println("\n" + packageCounter);
					packageCounter++;
					System.out.println("Package received");
			        
					// initiate sessionNumber and packageNumber
					sessionNumberReceived = receive.getNextData(sessionNumber.length);
					packageNumberReceived = receive.getNextData();			


					printByteArray(sessionNumberReceived);
					System.out.println("PN: " + packageNumberReceived);
					
					
					
			         // Decide whether to reply, or simulate packet loss.
			         if (random.nextDouble() < lossrate) {
			            System.out.println("   Reply not sent.");
			            packageCounter--;
			            continue; 
			         }
					
						
			         // Simulate network delay.
			         Thread.sleep((int) (random.nextDouble() * 2 * delay));
					
					
					
					// new session?
					if(!Arrays.equals(sessionNumber, sessionNumberReceived))
					{
						if(packageNumberReceived != 0)
						{
							System.out.println("Package Number has to be 0 in the beginning!");
							break;
						}
						
						startReceived	 = receive.getNextData(startReceived.length);
						fileLength		 = receive.getNextData(fileLength.length);
						fileNameLength	 = receive.getNextData(fileNameLength.length);
						
						
				        // was start text correctly sent?
						if(!Arrays.equals(start, startReceived))
						{
							// Cancel session
							System.out.println("Failure, new sessionNumber but no start string");
							break;
						}
						else
						{
							// new session started
							System.out.println("New session");
						
							// save sessionNumber
							sessionNumber = sessionNumberReceived.clone();

							// get file name
							buf = ByteBuffer.wrap(fileNameLength);
							fileName = new byte[(int)buf.getShort()];
							fileName = receive.getNextData(fileName.length);
							
							// CRC
							calcCRC(crc, receiveData, sessionNumber.length + 1 + start.length + fileLength.length + fileNameLength.length + fileName.length);
							
							// get CRC and check if it is the same
							crcReceived = receive.getNextData(crcReceived.length);
							
							if(!Arrays.equals(crc, crcReceived))
								System.out.println("CRC not equal");
							
							
							
							// get fileLengthInt
							buf = ByteBuffer.wrap(fileLength);
							fileLengthInt = buf.getLong();
							
							// prepare fileDataCounter and CRC32
							fileDataCounter = (int)fileLengthInt;
							crcData.reset();
							
							// get fileNameString
/**/						fileNameString = new String(fileName);
							
							// is there already a file with that name?
							fileNameString = getFileName(fileNameString);

							fos = new FileOutputStream(fileNameString, true);
							
							
							// set ip address and port right for the client
							IPAddress = receivePacket.getAddress(); 
							port = receivePacket.getPort();
						}
					}
					else if(packageNumberReceived != packageNumber)
					{
						System.out.println("Wrong Package Number!");
	/**/				break;
					}
					else 
					{	
						if(fileDataCounter > 0)
						{						
							if(fileDataCounter > (PAKETSIZE - (sessionNumber.length + 1)))
								fileData = new byte[(PAKETSIZE - (sessionNumber.length + 1))];
							else
							{
								System.out.println("Last write!");
								fileData = new byte[fileDataCounter];
							}
								
							// read out the fileData and update crcData
							fileData = receive.getNextData(fileData.length);
							crcData.update(fileData);
							
							// write into file and add amount of written data to fileDataCounter
							fos.write(fileData);
							fileDataCounter -= (PAKETSIZE - (sessionNumber.length + 1));
						}
						else
						{
							// get last Package with crc of client
							crcReceived = receive.getNextData(4).clone();
							
							printByteArray(crcReceived);
							
							// end it
							System.out.println("CRC: " + (int)crcData.getValue());
							fos.close();
							loop = false;
						}
					}							         
					
					// prepare ack
					ack = new OwnPackage(PAKETSIZE);
					ack.catData(sessionNumber);
					ack.catData(packageNumber);
					sendData = ack.getData();

	  
					// send
					send(IPAddress, port, sendData);			
					
					// prepare for next send process
					sendData = new byte[PAKETSIZE];
					receiveData = new byte[PAKETSIZE];
					
					packageNumber = flip(packageNumber);
					
					if(!loop)
						System.out.println("File fully transfered!");
				}
				sessionNumber = new byte[2];
				packageNumber = 0;
				loop = true;
				
				//System.out.println("\nwaiting for new client\n--------------------------------");
				
				// wait for next client

				break;
				}
			// another while loop is needed
			//serverSocket.close();
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
	
	public static String getFileName(String fileNameString)
	{
		if(new File(fileNameString).exists())
		{
			for(int i = 1; i < 10; i++)
			{
				if(!new File(fileNameString + "_" + i).exists())
				{
					fileNameString += "_" + i;
					return fileNameString;
				}
			}
		}
		return "fail";
	}
	
	
	public static void send(InetAddress IPAddress, int port, byte[] sendData) throws IOException
	{
		sendPacket.setAddress(IPAddress);
		sendPacket.setPort(port);
		sendPacket.setData(sendData);
		serverSocket.send(sendPacket);
		System.out.println("Package sent");
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
	
	
	
	public static byte flip(byte var)
	{
		// flip packageNumber
		if(var == 0)
			return 1;
		else
			return 0;
	}
	
	public static void printShortB(byte[] intBuffer)
	{
		ByteBuffer buf = ByteBuffer.wrap(intBuffer);
		System.out.println ("INT: " + buf.getShort());
	}
	
	
	public static void putIntintoByteBuffer(byte[] bufInto, int integer)
	{
		ByteBuffer buf = ByteBuffer.wrap(bufInto);
		buf.allocate(bufInto.length);
		buf.putInt(integer);
	}
	
	public static int getCRC(byte[] receiveData, int dataLength)
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
