import java.io.*;
import java.net.*;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

class Client {
	public static void main( String argv[]) throws Exception
	{
		if(argv.length == 3)
		{
			// arguments
			String host = argv[0];
			int port = Integer.parseInt(argv[1]);
			String fileNameString = argv[2];			
			
			// file
			File file = new File(fileNameString);
			
			// start package
			String startString = "start";
			
			// all the byte arrays
			byte[] sessionNumber = new byte[2];
			byte packageNumber = 0;
			byte[] start = startString.getBytes(StandardCharsets.US_ASCII);	// is always 5 byte
			byte[] fileLength = new byte[8];
			byte[] fileNameLength = new byte[2];
			byte[] fileName = fileNameString.getBytes(StandardCharsets.UTF_8); // is as long as expected   
			byte[] crc = new byte[2];
			
			// session number
			new Random().nextBytes(sessionNumber);
			
			// package number
			
			// file length
			
			// send and receive buffer
			byte[] sendData = new byte[1024];
			byte[] receiveData = new byte[1024];
			
			//FileInputStream fis = new FileInputStream(file);
		
			
			// connection stuff
			// create client socket
			Socket clientSocket = new Socket( host, port);	
			// create output stream
			DataOutputStream outToServer = new DataOutputStream( clientSocket.getOutputStream());
			// create input stream
			DataInputStream inFromServer = new DataInputStream( clientSocket.getInputStream());
			
			
			
			
			// prepare sendData
			ByteBuffer wrapped = ByteBuffer.wrap(sendData);
			wrapped.put(sessionNumber);
			wrapped.put(packageNumber);
			wrapped.put(start);
			
			// send
			outToServer.write(sendData, 0, sendData.length);
			
			// receive
			inFromServer.read(receiveData , 0, receiveData.length);
			
			if(Arrays.equals(sendData, receiveData))
				System.out.println("Gleich");
			else
				System.out.println("Ungleich");
			
			// close socket
			clientSocket.close();			

		}
		else
			System.out.println("Not the correct amount of arguments");
	}
	
	public static short getShort(byte[] buf)
	{
		ByteBuffer wrapped = ByteBuffer.wrap(buf);
		short var1 = wrapped.getShort();
		return var1;
	}
}

















/*
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
