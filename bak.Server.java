import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.io.FileInputStream;

class Server {
	public static void main(String argv[]) throws Exception
	{		
		if(argv.length == 1)
		{
			int port = Integer.parseInt(argv[0]);
			
			// Socket für Anfragen auf Port (chosen)
			ServerSocket welcomeSocket = new ServerSocket(port);

			while(true)
			{
				// variables
				String startString = "start";
				byte[] sendData = new byte[1024];
				
				// start package
				byte[] sessionNumber = new byte[2];
				byte[] sessionNumberTemp = new byte[2];
				byte packageNumber;
				byte[] start = startString.getBytes(StandardCharsets.US_ASCII);	// is always 5 byte
				byte[] startTemp = new byte[5];
				
				
				/*
				byte[] fileLength = new byte[8];
				byte[] fileNameLength = new byte[2];
				byte[] fileName = fileNameString.getBytes(StandardCharsets.UTF_8); // is as long as expected   
				byte[] crc = new byte[2];
				*/
				// open file output stream
				//FileOutputStream fos = new FileOutputStream("outputFile", true);
				
				
				
				// new socket
				Socket socket = welcomeSocket.accept();
				
		  		// create input stream and connect it with the socket
				DataInputStream inFromClient = new DataInputStream( socket.getInputStream());

				// create output stream and connect it with the socket
				DataOutputStream outToClient = new DataOutputStream( socket.getOutputStream());

				// client loop
				// while() {
				
				// get sessionNumber and packageNumber first
				inFromClient.read(sessionNumberTemp, 0, sessionNumberTemp.length);								// sessionNumber
				packageNumber = inFromClient.readByte();														// packageNumber
				inFromClient.read(startTemp, 0, startTemp.length);
			
				if(!Arrays.equals(sessionNumber, sessionNumberTemp))
				{
					if(!Arrays.equals(start, startTemp))
					{
						System.out.println("Failure");
						break;
					}
					else
					{
						// save sessionNumber
						sessionNumber = sessionNumberTemp;
					
						// if crc fail, kein ack
					
						// prepare ack
						ByteBuffer wrapped = ByteBuffer.wrap(sendData);
						wrapped.put(sessionNumber);
						wrapped.put(packageNumber);
						wrapped.put(start);
					
						// first ack (current client)
						outToClient.write(sendData, 0, sendData.length);
					}
				}
				else
				{
					// more receive
					// more ack
				}
				// }
			}
			// auf nächsten Client warten
		}
		else
			System.out.println("Not the correct amount of arguments");
	}
	
	
	//bytesAppend(sendData, sessionNumber);
	//byteAppend(sendData, packageNumber);
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
