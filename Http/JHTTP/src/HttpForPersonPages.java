import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * function ����������ڣ�ÿ����һ������ʹ���һ��RequestProcessor�̵߳��̳߳��С��÷�����Ϊ���̷߳����������򲻶�ķ��ʶ���
 * @param rootDirectory File���ͣ���������Ŀ¼
 * @param INDEX_FILE String����  Ĭ����ҳ
 * @author ����� 2016/6/9
 */
public class HttpForPersonPages {
	//��JHTTP.class.getCanonicalName()(��ı�׼��)����һ����־��������׼���Ҳ²�Ӧ�ú�ID����ͬ�ģ���������ͬ����־����
    private static final Logger logger = Logger.getLogger(HttpForPersonPages.class.getCanonicalName());
    //�̳߳صĴ�С��������50����ʹ���̳߳ص������Ƿ�ֹ�����������ɵ���Դ����ʹ�ã���ʹ�÷�����������
    //����java �����̡�ISBN 978-7-5123-6188-1;285ҳ
    private static final int NUM_THREADS = 50;
  //Ĭ����ҳ
	private static final String INDEX_FILE = "index.html";
	
	public static boolean isDebug = true;
	public static final String SERVERDIR_DEFAULT = "D:\\java\\JHTTP\\server"; 
	public static final int PORT_DEFAULT = 80;
	
	private final File rootDirectory; 	//�ļ�ϵͳ��Ŀ¼
	private final int port; // ���ʶ˿�
	
	//JHTTP���캯�����ø�Ŀ¼�Ͷ˿ںŴ�������
	public HttpForPersonPages(File rootDirectory,int port)throws IOException{
		if(!rootDirectory.isDirectory()){
			throw new IOException(rootDirectory + "does not exist as a directory");
		}
		this.rootDirectory = rootDirectory;
		this.port = port;
	}
	
	//��������
	public void start() throws IOException{
		//������־�ļ�
		LoggerUtil serverLog = new LoggerUtil("��������־");
		serverLog.setLogingProperties(logger);
		//�����̳߳�
		ExecutorService pool = Executors.newFixedThreadPool(NUM_THREADS);
		//���üල�˿ڣ�����try��ʽ����ServerSocket�����쳣���Զ��ر�ServerSocket
		try(ServerSocket server = new ServerSocket(port)){
			//���ӳɹ�
			logger.info("Acception connections on port " + server.getLocalPort());
			logger.info("Document Root: " + rootDirectory);
			
			while(true){
				try{
					//���յ�����������д��÷�������������
					Socket request = server.accept();
					//Ϊÿһ�����󴴽�һ���������̣߳���ʹ�߳����̳߳�������
					Runnable r = new RequestProcessor(rootDirectory,INDEX_FILE,request);
					pool.submit(r);
				}catch(IOException ex){
					//��������д�뵽��־��
					logger.log(Level.WARNING, "Error accepting connection",ex);
				}
			}
		}
	}
	
	//д����־�ļ�
	public static void writeIntoLog(Level level,String msg,Throwable thrown){
		logger.log(level,msg,thrown);
	}
	
	//д����־�ļ���info����
	public static void writeIntoLog(String msg){
		logger.info(msg);
	}
	
	//������������������
	//��Ҫ�����ĵ���Ŀ¼�Ͷ˿ںţ��˿ںſ��Բ����룬����������������ʱΪĬ�϶˿ں�80
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


