package cap.liubailin;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Service {
	
	private int portBase = 6000;
	private ServerSocket  client1;
	private ServerSocket client2;
	
	

	
	public Service() { 
		
		try {
			client1 = new ServerSocket(portBase);
			client2 = new ServerSocket(portBase + 1);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	public void awClient(Socket socket1, Socket socket2) throws IOException { 
		if(socket2 != null && socket1 != null ) {
			Log.write("service", "start anwser client");
			
			OutputStream os2 = socket2.getOutputStream(); //命令送到client2流
	        InputStream is2 = socket2.getInputStream();
			
			OutputStream os1 = socket1.getOutputStream();
			InputStream is1 = socket1.getInputStream(); //从client1读取命令的流
			
				
		 // Client2 -> Client1	
   		 new Thread(new Runnable() {
				@Override
				public void run() { 
					try { 
						byte buf[] = new byte[2048];
						int c = -1;
						while((c = is2.read(buf,0,2048)) != -1) {
							System.out.println("Client2 >>> Client1:" + c + "byte");
							os1.write(buf,0,c);
							os1.flush();
						};
						socket1.shutdownOutput();
						Log.write("Client2", "Response have finshed");
					} catch (IOException e) {
						e.printStackTrace();
					}	
				}
			}).start();
			
		 // Client1 -> Client2	
   		 new Thread(new Runnable() {
				@Override
				public void run() { 
					try { 
						byte buf[] = new byte[2048];
						int c = -1;
						while((c = is1.read(buf,0,2048)) != -1) {
							System.out.println("Client1 >>> Client2:" + c + "byte");
							os2.write(buf,0,c);
							os2.flush();
						};
						socket2.shutdownOutput();
						Log.write("Client1", "request have finshed");
					} catch (IOException e) {
						e.printStackTrace();
					}	
				}
			}).start();
		
		}
	}
	
	
	public void start() {
		Log.write("service", "start");
		while(true) {
			Socket socket1 = null;
			Socket socket2 = null;
			try {
				socket2 = client2.accept();
				Log.write("service", "client2 is connneted");
				socket1 = client1.accept();
				Log.write("service", "client1 is connneted");
				
				awClient(socket1,socket2);
			} catch (IOException e) {
					e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) { 
		new Service().start();;
	}
	
}
