import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * function 服务器的入口，每接收一个请求就创建一个RequestProcessor线程到线程池中。该服务器为多线程服务器，面向不多的访问对象
 * @param rootDirectory File类型，服务器根目录
 * @param INDEX_FILE String类型  默认主页
 * @author 杨观荣 2016/6/9
 */
public class HttpForPersonPages {
	//以JHTTP.class.getCanonicalName()(类的标准名)创建一个日志，这个类标准名我猜测应该和ID是相同的，用于区别不同的日志对象
    private static final Logger logger = Logger.getLogger(HttpForPersonPages.class.getCanonicalName());
    //线程池的大小，这里用50个。使用线程池的作用是防止过多的链接造成的资源大量使用，而使得服务器奔溃。
    //见《java 网络编程》ISBN 978-7-5123-6188-1;285页
    private static final int NUM_THREADS = 50;
  //默认主页
	private static final String INDEX_FILE = "index.html";
	
	public static boolean isDebug = true;
	public static final String SERVERDIR_DEFAULT = "D:\\java\\JHTTP\\server"; 
	public static final int PORT_DEFAULT = 80;
	
	private final File rootDirectory; 	//文件系统根目录
	private final int port; // 访问端口
	
	//JHTTP构造函数：用根目录和端口号创建对象
	public HttpForPersonPages(File rootDirectory,int port)throws IOException{
		if(!rootDirectory.isDirectory()){
			throw new IOException(rootDirectory + "does not exist as a directory");
		}
		this.rootDirectory = rootDirectory;
		this.port = port;
	}
	
	//启动函数
	public void start() throws IOException{
		//配置日志文件
		LoggerUtil serverLog = new LoggerUtil("服务器日志");
		serverLog.setLogingProperties(logger);
		//创建线程池
		ExecutorService pool = Executors.newFixedThreadPool(NUM_THREADS);
		//设置监督端口，这种try方式会在ServerSocket发生异常是自动关闭ServerSocket
		try(ServerSocket server = new ServerSocket(port)){
			//链接成功
			logger.info("Acception connections on port " + server.getLocalPort());
			logger.info("Document Root: " + rootDirectory);
			
			while(true){
				try{
					//接收到请求，有数据写入该服务器的数据流
					Socket request = server.accept();
					//为每一个请求创建一个独立的线程，并使线程在线程池中运行
					Runnable r = new RequestProcessor(rootDirectory,INDEX_FILE,request);
					pool.submit(r);
				}catch(IOException ex){
					//发生错误，写入到日志中
					logger.log(Level.WARNING, "Error accepting connection",ex);
				}
			}
		}
	}
	
	//写入日志文件
	public static void writeIntoLog(Level level,String msg,Throwable thrown){
		logger.log(level,msg,thrown);
	}
	
	//写入日志文件：info级别
	public static void writeIntoLog(String msg){
		logger.info(msg);
	}
	
	//主函数：启动服务器
	//需要输入文档根目录和端口号，端口号可以不输入，不输入或者输入错误时为默认端口号80
	public static void main(String args[]){
		File serverDir =new File(SERVERDIR_DEFAULT);
		int port = PORT_DEFAULT;
		try{
			HttpForPersonPages webServer = new HttpForPersonPages(serverDir, port);
			webServer.start();
		}catch(IOException ex){
			logger.log(Level.SEVERE, "Server could not start", ex);
		}
	}
	
	
}


