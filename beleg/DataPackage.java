
package beleg;

public class DataPackage {

		private byte[] sessionNumber = new byte[2];		
		private byte packageNumber = -1;
		private byte[] sendData;
		private int sendDataSize = 0;
				
		public DataPackage(byte[] sessionNumber, byte packageNumber, int sendDataSize)
		{
			this.sessionNumber = sessionNumber;
			this.packageNumber = packageNumber;
			sendData = new byte[sendDataSize];
		}
		
		public byte[] getSessionNumber()
		{
			return sessionNumber;
		}
		public byte getPackageNumber()
		{
			return packageNumber;
		}
		public byte[] getSendData()
		{
			return sendData;
		}
}
