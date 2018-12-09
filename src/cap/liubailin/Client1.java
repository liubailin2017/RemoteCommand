package cap.liubailin;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;

public class Client1 {


	public static void main(String[] args) {
		
		
		byte[] remainBuf = new byte[0xffff];
		int remainLen = -1;
		boolean isremain = false;
		
		
		while(true) {
		
			try {
				Socket socket1 = new Socket("127.0.0.1", 6000);
				OutputStream os = socket1.getOutputStream();
				 
		        InputStream is = socket1.getInputStream();
	 
				 
				File tmp = new File("tmp.file");
				if(!tmp.exists()) {
					tmp.createNewFile();
				}
				
				FileOutputStream fos = new FileOutputStream(tmp); 
				

				new Thread(new Runnable() {
					
					@Override
					public void run() {
						try {
								int c = -1;
						    	byte [] buf = new byte[2048];
						    	
										while((c = is.read(buf,0,2048)) != -1) {
											 byte t[] = buf;
											 if(c != 2048) {
												 t = new byte[c];
												 for(int i = 0; i< c; i++) {
													 t[i] = buf[i];
												 }
											 }
											System.out.print(new String(t));
										  
											fos.write(buf, 0, c);
										}
								System.out.println("\n-------------------\n");
								fos.close();
								socket1.shutdownOutput();
							} catch (IOException e) {
								e.printStackTrace();
							};
					}
				}).start();
				
				if(isremain) {
					isremain = false;
					os.write(remainBuf,0,remainLen);
				}else {	
			    	while((remainLen = System.in.read(remainBuf,0,2048)) != -1) {
						os.write(remainBuf,0,remainLen);
					}
				}
				
			}catch(SocketException e) {
				isremain = true;
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
