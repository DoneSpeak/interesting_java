import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.Scanner;

/*
 * 测试数据：bbs.lb.pku.edu.cn
 * bbs.lb.pku.edu.cn 或者 bbs.ustc.edu.cn
 */

public class Telnet {
	//是否调试,用于规定是否进行调试操作，比如是否输出错误信息
	static boolean debug = true;
	//程序是否结束，用于结束线程
	static boolean stop = false;
	//是否需要协商
	static boolean needNegotiate = false;
	//是否为Linux系统，如果是是linux系统则保留颜色标签，和添加按键事件
	static boolean isLinux = false;
	
	//telnet默认端口
	static int DEFAULT_TELNET_PORT = 23;
	
	public static void main(String args[]){
		//获取操作系统名称 ，windows下的操作和linux下的操作会有所不同。
		String osName = System.getProperty("os.name");
System.out.print("运行的操作系统为：" + osName + "\n");
		if(debug){
			System.out.print("运行的操作系统为：" + osName + "\n");
		}
		if(osName.toUpperCase().contains("LINUX")){
			//判断是否为linux操作系统，如果是linux操作系统，保留颜色标签并添加按键事件
			isLinux = true;
		}
System.out.print("运行的操作系统为：" + isLinux + "\n");

		int argsNum = args.length;
		String host = null;
		//port默认使用telnet的端口
		int port = DEFAULT_TELNET_PORT;
		TelnetClass tn = null;
		//判断传入参数的个数
		switch(argsNum){
		//只有主机名
		case 1: {
			host = args[0];
			System.out.print("正在连接" + host + "...");
			break;
		}
		//两个参数，包括host和port,port为指定的端口
		case 2:{
			if(Telnet.debug){
				System.out.println("有两个参数");
			}
			if(!args[1].matches("[0-9]+")){
				System.out.println("无法打开到主机的连接。在端口 " + args[1] + ":连接失败");
				return;
			}
			port = Integer.parseInt(args[1]);
			host = args[0];
			System.out.print("正在连接" + host + "...");
			
			
			
			break;
		}
		case 0:{
			System.out.println("请输入服务器域名：");
			Scanner scan = new Scanner(System.in);
			host = scan.nextLine();
			break;
		}
		default:{
			System.out.println("");
			System.out.println("java Telnet [host [port]]");
			System.out.println("host	指定要连接的远程计算机的主机名或IP地址。");
			System.out.println("port	指定端口号或服务器名。");
			return;
		}
		}

		tn = new TelnetClass(host,port);
		try {
			tn.openConnection();
		} catch (Exception e) {
			System.out.println("无法打开到主机的连接。在端口 " + port + ":连接失败");
			Telnet.stop = true;
			//异常原因：获取输入输出流出错
			if(Telnet.debug){
				e.printStackTrace();
			}
			return;
		}
		tn.interactWithTelnetServer();
		
	}
	
}

/*
 * @function 该类用于连接telnet服务器和与服务器交互
 * @param telnetSocket 创建与telnet服务socket,用于获取输入输出类
 * @param serverOutput 对telnet服务器的输出流
 * @param serverInput 获取到的telnet服务器的输入流
 * @param host 要连接的服务器域名或者ip地址
 * @param port 连接的端口号
 * @author yangguanrong 2016/5/21 8:49
 */
class TelnetClass{
	
	protected Socket telnetSocket;
	public OutputStream serverOutput;
	public InputStream serverInput;
	
	private String host;
	private int port;

	//输入地址和端口号的连接方式
	public TelnetClass(String host,int port){
		this.host = host;
		this.port = port;
	}
	//连接,分别得到服务器的输入流和输出流
	public void openConnection() throws Exception{
		try {
			telnetSocket = new Socket(host,port);
			serverOutput = telnetSocket.getOutputStream();
			serverInput = telnetSocket.getInputStream();
			if(port == Telnet.DEFAULT_TELNET_PORT && Telnet.needNegotiate){
				negotiate(serverInput,serverOutput);
			}
		} catch (IOException e) {
			if(Telnet.debug){
				e.printStackTrace();
			}
			Telnet.stop = true;
			close();
			throw new Exception("openfail");
		}
		
	}
	
	//启动与telnet服务器的交互，包括输入及输出
	public void interactWithTelnetServer(){
		//通过键盘输入向服务器发送请求
		InputToServer stdinToServer = new InputToServer(System.in,serverOutput);
		//读取服务器返回的数据，显示到终端
		OutputToScreen serverToStdout = new OutputToScreen(serverInput,System.out);
		//开启这两个线程，让他们时时在监控
		new Thread(stdinToServer).start();
		new Thread(serverToStdout).start(); 
	}
	
	//定义协商用的命令
	//标志符,代表是一个TELNET 指令,接到该指令之后的数据都是命令
	static final byte IAC = (byte)255;
	//表示一方要求另一方停止使用，或者确认你不再希望另一方使用指定的选项。
	static final byte DONT = (byte)254;
	//表示一方要求另一方使用，或者确认你希望另一方使用指定的选项。
	static final byte DO = (byte)253;
	//表示拒绝使用或者继续使用指定的选项。
	static final byte WONT = (byte)252;
	//表示希望开始使用或者确认所使用的是指定的选项。
	static final byte WILL = (byte)251;	
	
	//Telnet协商，协商指令常用指令是<IAC,DO,24>，24是协商选项的“终端类型”
	public void negotiate(InputStream in,OutputStream out) throws IOException{
		byte[] buff = new byte[3];
		BufferedInputStream bfin = new BufferedInputStream(in);
		while(true){
			//做标记，同时在读取了3个数据后标记失效
			bfin.mark(buff.length);
			//available 判断人可以获得的数据长度
			if(bfin.available() >= buff.length){
				bfin.read(buff);
				//协商指令以IAC开始，如果第一个字符不是IAC则表示协商结束
				if(buff[0] != IAC){
					bfin.reset();
					return;
				}
			}else if(buff[1] == DO){
				//这里仅仅使用NVT功能进行通讯，所以拒绝服务器的所以有关执行选项的请求
				//所以这里对于DO指令请求，我们回复WONT
				buff[1] = WONT;
				out.write(buff);
			}
		}
	}
	
	//关闭socket
	public void close(){
		if(telnetSocket != null && !telnetSocket.isClosed() && telnetSocket.isConnected()){
			try {
				telnetSocket.close();
			} catch (IOException e) {
				Telnet.stop = true;
				if(Telnet.debug){
					e.printStackTrace();
				}
			}
		}
	}
}

/*
 * @function 通过这个Runnable类，可以通过键盘输入数据与服务器进行请求交互
 * @param input 输入流，分别获得服务器的输入流和键盘的输入流
 * @param output 输出流，分别输出到服务器和显示器
 * @author yangguanrong 2016/5/21 8:49
 */
class InputToServer implements Runnable{
	
	//按键获取
    public static final String[] keyStrArray = {
		//向上键 ^[[A
		(char)27 + "[A",
		//向下键 ^[[B
		(char)27 + "[B",
		//向右键 ^[[C
		(char)27 + "[C",
		//向左键 ^[[D
		(char)27 + "[D",
		//esc键   ^[
		(char)27 + "",
		//insert键 ^[[2~
		(char)27 + "[2~",
		//delete键 ^[[3~
		(char)27 + "[A",
		//home键 ^[[OH
		(char)27 + "OH",
		//end键 ^[OF
		(char)27 + "OF",
    };
	
	private BufferedInputStream input =  null;
	private OutputStream output = null;
	
	public InputToServer(InputStream input, OutputStream output){
		this.input = new BufferedInputStream(input);
		this.output = output;
	}
	
	@Override
	public void run(){
		byte[] buff = new byte[1024];
		try{
			while(true){
				
				int n = input.read(buff);
				if(Telnet.stop){
					break;
				}
				if(n > 0){
					//在linux下对特殊按键指令进行解析发送
					if(false){
						//cmdStr 不可以用 cmdStr = new String(buf);这样得到的数据会有其他的字符
						String cmdStr = new String(buff,0,n);
						int i;
						for(i = 0; i < keyStrArray.length; i ++){
							if(cmdStr.equals(keyStrArray[i] + "\n")){
								//在linux下，enter键为"\n"而在windows下为"\r\n"
								break;
							}
						}
						//如果是指令按键，只需要将最后的enter字符消除即可
						if(i < keyStrArray.length){
							//消除最后一个字符，即不发送"\n"
							output.write(buff,0,n-1);
						}else{
							output.write(buff,0,n);
						}
						output.flush();
						buff = new byte[1024];
					}else{
						//其他系统则直接发送所有指令数据到服务器
						output.write(buff,0,n);
						output.flush();
						buff = new byte[1024];
					}
				}
			}
		}catch(Exception err){
			if(Telnet.debug){
				err.printStackTrace();
			}
			//如果有输出错，直接结束，不做任何操作
			//主要出错的地方是，在对服务器发送请求时，服务器没有响应的数据回复，在进行读写时发生异常
			Telnet.stop = true;
			System.out.println("遗失对主机的连接。");
			return;
		}finally{
			try {
				//关闭输入输出流
				if(input != null){
					input.close();
				}
				if(output != null){
					output.close();
				}
				
			} catch (IOException e) {
				Telnet.stop = true;
				if(Telnet.debug){
					e.printStackTrace();
				}
			}
		}
		
	}
}


/*
 * @function 通过这个Runnable类，将服务器返回的数据显示到终端
 * @param input 输入流，获得服务器的输入流
 * @param output 输出流，输出到显示器
 * @author yangguanrong 2016/5/21 8:49
 */
class OutputToScreen implements Runnable{
	private BufferedReader reader =  null;
	private PrintStream printer = null;
	public String colorCodeRegex = (char)27 + "\\u005B(\\d+[;])*\\d*[m,H]";
	public OutputToScreen(InputStream input, OutputStream output){
		try {
			this.reader = new BufferedReader(new InputStreamReader(input,"GBK"));
			this.printer = new PrintStream(output);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void run(){
		String line;
		try{
			while((line = reader.readLine()) != null){
				if(Telnet.stop){
					break;
				}
				if(!Telnet.isLinux){
					//不是linux操作系统，将颜色标签消除
					line = line.replaceAll(colorCodeRegex, "");
				}
				printer.print(line);
				printer.print("\r\n");
				printer.flush();
			}
		}catch(Exception err){
			if(Telnet.debug){
				err.printStackTrace();
			}
			//如果有输出错，直接结束，不做任何操作
			//主要出错的地方是，在对服务器发送请求时，服务器没有响应的数据回复，在进行读写时发生异常
			Telnet.stop = true;
			System.out.println("遗失对主机的连接。");
			return;
		}finally{
			try {
				//关闭输入输出流
				if(reader != null){
					reader.close();
				}
				if(printer != null){
					printer.close();
				}
				
			} catch (IOException e) {
				Telnet.stop = true;
				if(Telnet.debug){
					e.printStackTrace();
				}
			}
		}
		
	}
}