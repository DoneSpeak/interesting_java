
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URLConnection;
import java.util.Date;
import java.util.logging.Level;

/*
 * functioin �������Runnable,��������ͻ��˵����������Լ���������
 * @param rootDirectory File����		��������Ŀ¼
 * @param indexFileName String����	 Ĭ����ҳ
 * @param connection 	Socket����	�����󴴽���һ��socket�������ں��û�����		
 */
public class RequestProcessor implements Runnable {

//	private static final Logger logger = Logger.getLogger(RequestProcessor.class.getCanonicalName());
	
	private File rootDirectory;
	private String indexFileName = "index.html";
	private Socket connection;
	
	public RequestProcessor(File rootDirectory, String indexFileName,Socket connection){
		
		//��ֹϵͳ����
		if(rootDirectory.isFile()){
			throw new IllegalArgumentException("rootDirector must be a directory, not a file");
		}
		
		try{
			rootDirectory = rootDirectory.getCanonicalFile();
			if(HttpForPersonPages.isDebug){
				System.out.println("DEBUG: " + rootDirectory.getCanonicalFile().toString());
			}
		}catch(IOException ex){
			ex.printStackTrace();
		}
		
		this.rootDirectory = rootDirectory;
		
		if(indexFileName != null){
			this.indexFileName = indexFileName;
		}
		
		this.connection = connection;
	}
	
	@Override
	public void run(){
		OutputStream oStream = null;
		BufferedReader bufReader = null;
		FileInputStream fInS = null;
		
		if(HttpForPersonPages.isDebug){
			System.out.println("DEBUG: ��Ŀ¼  " + rootDirectory.getAbsolutePath());
			System.out.println("DEBUG: " + "�����߳�");
		}
		
		try{
			//�ͻ�������Ķ�������ı��ļ����������ļ�����ͼƬ�������ȣ����������õײ���������������
			oStream = connection.getOutputStream();
			InputStream inStream = connection.getInputStream();
			//���������������������ݵĶ�ȡ
			bufReader = new BufferedReader(new InputStreamReader(inStream));
			
			byte b[]=new byte[1024];
			String headStr = null;
			
			//��ȡʵ��ͷ�������е�headStr��
			while(true){
				headStr =  bufReader.readLine();
				if(headStr != null && headStr.length() != 0){
					break;
				}
			}
			
			//������������д����־
//			logger.info(connection.getRemoteSocketAddress() + " " + headStr);
			HttpForPersonPages.writeIntoLog(connection.getRemoteSocketAddress() + " " + headStr);
			
			//����������
			String[] tokens = headStr.split("\\s+");
			String method = tokens[0];
			String httpVersion = "";
			File requestFile = null;
			
			//��������ķ���������ֱ���GET,DELETE,HEAD,OPTIONS
			if(method.toUpperCase().equals("GET")){
				String filePath = tokens[1];
				if(filePath.endsWith("/")){
					filePath += indexFileName;
				}
				
				if(tokens.length > 2){
					httpVersion = tokens[2]; //���е������ַ�����HTTP�İ汾		
				}
				
				//���������ļ�������·��
				requestFile = new File(rootDirectory,filePath.substring(1, filePath.length()));
				
				//����ļ�����
				//URLConnection.getFileNameMap().getContentTypeFor(fileName);�����޷���ȡ�������ļ���contentType���磺text/css�޷���ȡ��
				//�Ӷ�����޷��ļ��������ļ���ʾ����ȷ����text/css�����᷵��null,���յ���������޷�������ʽ����ҳ��ʽ��ʧ
//		    	String contentType = URLConnection.getFileNameMap().getContentTypeFor(fileName);
				//Helper.getContentType(requestFile.getAbsolutePath());���ҽ������һ�д����Լ��ļ�����չ����MIME��������ȡcontentType�ķ���������ȫ��
				String contentType = Helper.getContentType(requestFile.getAbsolutePath());
				
				//ȷ���ļ����ڣ�ͬʱȷ���޷����ʵ��ļ�ϵͳ�������ļ�
				if(requestFile.getAbsolutePath().startsWith(rootDirectory.getAbsolutePath()) && requestFile.canRead() ){
					
					//��ʼ�����ݷ���
					//������Ϣ�а���http�汾��ʱ������ͷ����Ϣ
					if(httpVersion.toUpperCase().startsWith("HTTP/")){
						sendHeader(oStream,"HTTP/1.0 200 OK",contentType,requestFile.length());
					}
					//�����ļ�
					fInS = new FileInputStream(requestFile);
					int len = -1;
					
					while((len = fInS.read(b, 0, 1024)) != -1){
						oStream.write(b, 0, len);
					}
					//���д����־
					HttpForPersonPages.writeIntoLog("Result: 200 OK" + "\r\n");
//					writeIntoLogger("info",null,"Result: 200 OK" + "\r\n",null);
					
					if(HttpForPersonPages.isDebug){
						System.out.println("DEBUG: �ļ����سɹ���");
						System.out.println("DEBUG: �����ļ�·��Ϊ  " + requestFile.getAbsolutePath());
					}
				}else{
					//�޷��ҵ��ļ�
					cannotFindFile(oStream,httpVersion);
					
					HttpForPersonPages.writeIntoLog("Result: 404 NOT FOUND" + "\r\n");
				}
			}//[end] if(method.equals("GET"))
			else if(method.toUpperCase().equals("DELETE")){
				//���ļ�ɾ�������ָ���ļ������ڣ�����ͻ᷵�ش�����Ϣ
				String filePath = tokens[1];
				if(tokens.length > 2){
					httpVersion = tokens[2]; //���е������ַ�����HTTP�İ汾		
				}
				if(filePath.endsWith("/") || filePath.endsWith("index.html")){
					//����������ֹɾ��Ĭ����ҳ
					String html = createHTML("Forbidden","HTTP Error 403: Forbidden");
					if(httpVersion.startsWith("HTTP/")){
						//����һ��MIME�ײ�
						sendHeader(oStream,"HTTP/1.0 403 Forbidden","text/html; charset=utf-8",html.length());
					}
					responseErrorHtml(oStream, html);
					
				}else{
					
					//���������ļ�������·��
					requestFile = new File(rootDirectory,filePath.substring(1, filePath.length()));
					
					//���contentType
					String contentType = Helper.getContentType(requestFile.getAbsolutePath());
					
					//�ж��ļ��Ƿ���ڷ��������Ƶ��ļ�����
					if(requestFile.canRead() && requestFile.getAbsolutePath().startsWith(rootDirectory.getAbsolutePath())){
						requestFile.delete();
						if(httpVersion.toUpperCase().startsWith("HTTP/")){
							sendHeader(oStream,"HTTP/1.0 200 OK",contentType,requestFile.length());
						}
					}else{
						//�޷��ҵ��ļ�
						cannotFindFile(oStream,httpVersion);
						
						HttpForPersonPages.writeIntoLog("Result: 404 NOT FOUND" + "\r\n");
					}
				}
				
				
			}//[end] if(method.equals("DELETE"))
			else if((method.toUpperCase().equals("HEAD"))){
				String filePath = tokens[1];
				if(filePath.endsWith("/")){
					filePath += indexFileName;
				}
				
				
				
				if(tokens.length > 2){
					httpVersion = tokens[2]; //���е������ַ�����HTTP�İ汾		
				}
				
				//���������ļ�������·��
				requestFile = new File(rootDirectory,filePath.substring(1, filePath.length()));
				//����ļ�����
				String contentType = Helper.getContentType(requestFile.getAbsolutePath());
				
				//ȷ���ļ����ڣ�ͬʱȷ���޷����ѷ��ʵ��ļ�ϵͳ�������ļ�
				if(requestFile.canRead() && requestFile.getAbsolutePath().startsWith(rootDirectory.getAbsolutePath())){
					//��ʼ�����ݷ���
					
					//������Ϣ�а���http�汾��ʱ������ͷ����Ϣ
					if(httpVersion.toUpperCase().startsWith("HTTP/")){
						sendHeader(oStream,"HTTP/1.0 200 OK",contentType,requestFile.length());
					}else{
						headStr = "\r\n\r\n";
						oStream.write(headStr.getBytes());
						oStream.flush();
					}
					
				}else{
					//�޷��ҵ��ļ�����������Ż�һ��ͷ�������Բ����ú���cannotFindFile(oStream,httpVersion);
					if(HttpForPersonPages.isDebug){
						System.out.println("DEBUG: �Ҳ����ļ���");
					}
					
					cannotFindFile(oStream,httpVersion);
					
					HttpForPersonPages.writeIntoLog("Result: 404 NOT FOUND" + "\r\n");
				}
					
			}//[end] if((method.toUpperCase().equals("HEAD"))){
			else if((method.toUpperCase().equals("OPTIONS"))){
				//TODO ����Ҫ����
				Date now = new Date();
				headStr = new StringBuilder("HTTP/1.0 200 OK" + "\r\n")
						.append("Date: " + now + "\r\n")
						.append("Server: JHTTP 2.0\r\n")
						.append("Allow: GET,DELETE,HEAD,OPTIONS" + "\r\n") //֧�ֵ���������
						.append("Content-Style-Type: " + "text/css" + "\r\n")
						.append("Content-Length: " + 0 + "\r\n")
						.append("Connection: " + "close" + "\r\n")
						.append("Content-Type: " + "text/html" + "\r\n")
						.append("\r\n").toString(); //���б�ʾͷ��Ϣ�������
				oStream.write(headStr.getBytes());
				oStream.flush();
			}
			else{
				//������Ϊ"GET"��"DELETE","HEAD"����"OPTIONS"
				String html = createHTML("Not Implemented","HTTP Error 501: Not Implemented");
				
				if(HttpForPersonPages.isDebug){
					System.out.println("DEBUG: ���󷽷�ΪGET,DELETE,HEAD����OPTIONS��");
					System.out.println("DEBUG: ����ʽΪ  " + method);
				}
				if(httpVersion.startsWith("HTTP/")){
					//����һ��MIME�ײ�
					sendHeader(oStream,"HTTP/1.0 501 Not Implemented","text/html; charset=utf-8",html.length());
				}
				
				responseErrorHtml(oStream, html);
				
				if(HttpForPersonPages.isDebug){
					System.out.println("DEBUG: ��GET������ɣ�");
				}
			}
		}catch(IOException ex){
			HttpForPersonPages.writeIntoLog(Level.WARNING,"Error talking to " + connection.getRemoteSocketAddress(), ex);
			ex.printStackTrace();
		}finally {
			try{
				//socket��������ʹ�ú���Ҫ�رգ���ֹռ����Դ
				if(connection != null && !connection.isClosed() && connection.isConnected()){
					connection.close();
				}
				
				if(oStream != null){
					oStream.close();
				}
				//�ǵùر��ļ�������������޷����ļ�����ɾ���Ȳ���
				if(fInS != null){
					fInS.close();
				}
				
			}catch(IOException ex){
				ex.printStackTrace();
			}
		}
	}
	
	//����ͷ����Ϣ
	private void sendHeader(OutputStream oStream, String responseCode, String contentType, long length)throws IOException{
		Date now = new Date();
		String headStr = new StringBuilder(responseCode + "\r\n")
				.append("Date: " + now + "\r\n")
				.append("Server: JHTTP 2.0\r\n")
				.append("Content-length: " + length + "\r\n")
				.append("Content-type: " + contentType + "\r\n")
				.append("\r\n").toString(); //���б�ʾͷ��Ϣ�������
		oStream.write(headStr.getBytes());
		oStream.flush();
	}
	
	//���ش�����ҳ������Ϣ
	private void responseErrorHtml(OutputStream oStream, String html) throws IOException{
		oStream.write(html.getBytes());
		oStream.flush();
	}
	
	//���������ʾ��ҳ
	private String createHTML(String title, String info){
		String html = new StringBuilder("<HTML>\r\n")
				.append("<HEAD><TITLE>" + title + "</TITLE>\r\n")
				.append("</HEAD>\r\n")
				.append("<BODY>")
				.append("<H1>" + info + "</H1>\r\n")
				.append("</BODY></HTML>\r\n").toString();
		return html;
	}
	
	//�����޷��ҵ��ļ���ҳ
	public void cannotFindFile(OutputStream oStream, String httpVersion) throws IOException{
		if(HttpForPersonPages.isDebug){
			System.out.println("DEBUG: �Ҳ����ļ���");
		}
		String html = createHTML( "File Not Found", "HTTP Error 404: File Not Found");
		if(httpVersion.startsWith("HTTP/")){
			//����һ��MIME�ײ�
			sendHeader(oStream,"HTTP/1.0 404 File Not Found","text/html; charset=utf-8",html.length());
		}
		responseErrorHtml(oStream,html);
		
		if(HttpForPersonPages.isDebug){
			System.out.println("DEBUG: �ļ��Ҳ������󷵻أ�");
		}
		HttpForPersonPages.writeIntoLog("Result: 404 NOT FOUND" + "\r\n");
	}

}
