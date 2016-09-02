
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
 * functioin 请求处理的Runnable,负责分析客户端的请求内容以及返回内容
 * @param rootDirectory File类型		服务器根目录
 * @param indexFileName String类型	 默认主页
 * @param connection 	Socket类型	用请求创建的一个socket对象，用于和用户交互		
 */
public class RequestProcessor implements Runnable {

//	private static final Logger logger = Logger.getLogger(RequestProcessor.class.getCanonicalName());
	
	private File rootDirectory;
	private String indexFileName = "index.html";
	private Socket connection;
	
	public RequestProcessor(File rootDirectory, String indexFileName,Socket connection){
		
		//防止系统错误
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
			System.out.println("DEBUG: 根目录  " + rootDirectory.getAbsolutePath());
			System.out.println("DEBUG: " + "启动线程");
		}
		
		try{
			//客户端请求的对象除了文本文件还有其他文件，如图片，视屏等，所以这里用底层的输出流返回数据
			oStream = connection.getOutputStream();
			InputStream inStream = connection.getInputStream();
			//缓冲输入流，有利于数据的读取
			bufReader = new BufferedReader(new InputStreamReader(inStream));
			
			byte b[]=new byte[1024];
			String headStr = null;
			
			//获取实体头的请求行到headStr中
			while(true){
				headStr =  bufReader.readLine();
				if(headStr != null && headStr.length() != 0){
					break;
				}
			}
			
			//将请求行内容写入日志
//			logger.info(connection.getRemoteSocketAddress() + " " + headStr);
			HttpForPersonPages.writeIntoLog(connection.getRemoteSocketAddress() + " " + headStr);
			
			//分析请求行
			String[] tokens = headStr.split("\\s+");
			String method = tokens[0];
			String httpVersion = "";
			File requestFile = null;
			
			//分析请求的方法：这里分别有GET,DELETE,HEAD,OPTIONS
			if(method.toUpperCase().equals("GET")){
				String filePath = tokens[1];
				if(filePath.endsWith("/")){
					filePath += indexFileName;
				}
				
				if(tokens.length > 2){
					httpVersion = tokens[2]; //首行第三个字符串是HTTP的版本		
				}
				
				//获得请求的文件的完整路径
				requestFile = new File(rootDirectory,filePath.substring(1, filePath.length()));
				
				//获得文件类型
				//URLConnection.getFileNameMap().getContentTypeFor(fileName);方法无法获取到部分文件的contentType，如：text/css无法获取，
				//从而造成无法文件，导致文件显示不正确，如text/css方法会返回null,最终导致浏览器无法解析样式表，网页样式丢失
//		    	String contentType = URLConnection.getFileNameMap().getContentTypeFor(fileName);
				//Helper.getContentType(requestFile.getAbsolutePath());是我结合上面一行代码以及文件的拓展名和MIME类型来获取contentType的方法，更加全面
				String contentType = Helper.getContentType(requestFile.getAbsolutePath());
				
				//确保文件存在，同时确保无法访问到文件系统的其他文件
				if(requestFile.getAbsolutePath().startsWith(rootDirectory.getAbsolutePath()) && requestFile.canRead() ){
					
					//开始将数据返回
					//请求消息中包含http版本号时，返回头部信息
					if(httpVersion.toUpperCase().startsWith("HTTP/")){
						sendHeader(oStream,"HTTP/1.0 200 OK",contentType,requestFile.length());
					}
					//返回文件
					fInS = new FileInputStream(requestFile);
					int len = -1;
					
					while((len = fInS.read(b, 0, 1024)) != -1){
						oStream.write(b, 0, len);
					}
					//结果写入日志
					HttpForPersonPages.writeIntoLog("Result: 200 OK" + "\r\n");
//					writeIntoLogger("info",null,"Result: 200 OK" + "\r\n",null);
					
					if(HttpForPersonPages.isDebug){
						System.out.println("DEBUG: 文件返回成功！");
						System.out.println("DEBUG: 请求文件路径为  " + requestFile.getAbsolutePath());
					}
				}else{
					//无法找到文件
					cannotFindFile(oStream,httpVersion);
					
					HttpForPersonPages.writeIntoLog("Result: 404 NOT FOUND" + "\r\n");
				}
			}//[end] if(method.equals("GET"))
			else if(method.toUpperCase().equals("DELETE")){
				//将文件删除，如果指定文件不存在，将会就会返回错误信息
				String filePath = tokens[1];
				if(tokens.length > 2){
					httpVersion = tokens[2]; //首行第三个字符串是HTTP的版本		
				}
				if(filePath.endsWith("/") || filePath.endsWith("index.html")){
					//本服务器禁止删除默认首页
					String html = createHTML("Forbidden","HTTP Error 403: Forbidden");
					if(httpVersion.startsWith("HTTP/")){
						//发送一个MIME首部
						sendHeader(oStream,"HTTP/1.0 403 Forbidden","text/html; charset=utf-8",html.length());
					}
					responseErrorHtml(oStream, html);
					
				}else{
					
					//获得请求的文件的完整路径
					requestFile = new File(rootDirectory,filePath.substring(1, filePath.length()));
					
					//获得contentType
					String contentType = Helper.getContentType(requestFile.getAbsolutePath());
					
					//判断文件是否存在服务器限制的文件夹中
					if(requestFile.canRead() && requestFile.getAbsolutePath().startsWith(rootDirectory.getAbsolutePath())){
						requestFile.delete();
						if(httpVersion.toUpperCase().startsWith("HTTP/")){
							sendHeader(oStream,"HTTP/1.0 200 OK",contentType,requestFile.length());
						}
					}else{
						//无法找到文件
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
					httpVersion = tokens[2]; //首行第三个字符串是HTTP的版本		
				}
				
				//获得请求的文件的完整路径
				requestFile = new File(rootDirectory,filePath.substring(1, filePath.length()));
				//获得文件类型
				String contentType = Helper.getContentType(requestFile.getAbsolutePath());
				
				//确保文件存在，同时确保无法而已访问到文件系统的其他文件
				if(requestFile.canRead() && requestFile.getAbsolutePath().startsWith(rootDirectory.getAbsolutePath())){
					//开始将数据返回
					
					//请求消息中包含http版本号时，返回头部信息
					if(httpVersion.toUpperCase().startsWith("HTTP/")){
						sendHeader(oStream,"HTTP/1.0 200 OK",contentType,requestFile.length());
					}else{
						headStr = "\r\n\r\n";
						oStream.write(headStr.getBytes());
						oStream.flush();
					}
					
				}else{
					//无法找到文件，这里仅仅放回一个头部，所以不是用函数cannotFindFile(oStream,httpVersion);
					if(HttpForPersonPages.isDebug){
						System.out.println("DEBUG: 找不到文件！");
					}
					
					cannotFindFile(oStream,httpVersion);
					
					HttpForPersonPages.writeIntoLog("Result: 404 NOT FOUND" + "\r\n");
				}
					
			}//[end] if((method.toUpperCase().equals("HEAD"))){
			else if((method.toUpperCase().equals("OPTIONS"))){
				//TODO 还需要完善
				Date now = new Date();
				headStr = new StringBuilder("HTTP/1.0 200 OK" + "\r\n")
						.append("Date: " + now + "\r\n")
						.append("Server: JHTTP 2.0\r\n")
						.append("Allow: GET,DELETE,HEAD,OPTIONS" + "\r\n") //支持的请求类型
						.append("Content-Style-Type: " + "text/css" + "\r\n")
						.append("Content-Length: " + 0 + "\r\n")
						.append("Connection: " + "close" + "\r\n")
						.append("Content-Type: " + "text/html" + "\r\n")
						.append("\r\n").toString(); //空行表示头信息输入结束
				oStream.write(headStr.getBytes());
				oStream.flush();
			}
			else{
				//方法不为"GET"，"DELETE","HEAD"或者"OPTIONS"
				String html = createHTML("Not Implemented","HTTP Error 501: Not Implemented");
				
				if(HttpForPersonPages.isDebug){
					System.out.println("DEBUG: 请求方法为GET,DELETE,HEAD或者OPTIONS！");
					System.out.println("DEBUG: 请求方式为  " + method);
				}
				if(httpVersion.startsWith("HTTP/")){
					//发送一个MIME首部
					sendHeader(oStream,"HTTP/1.0 501 Not Implemented","text/html; charset=utf-8",html.length());
				}
				
				responseErrorHtml(oStream, html);
				
				if(HttpForPersonPages.isDebug){
					System.out.println("DEBUG: 非GET返回完成！");
				}
			}
		}catch(IOException ex){
			HttpForPersonPages.writeIntoLog(Level.WARNING,"Error talking to " + connection.getRemoteSocketAddress(), ex);
			ex.printStackTrace();
		}finally {
			try{
				//socket的数据流使用后需要关闭，防止占用资源
				if(connection != null && !connection.isClosed() && connection.isConnected()){
					connection.close();
				}
				
				if(oStream != null){
					oStream.close();
				}
				//记得关闭文件输出流，否则无法对文件进行删除等操作
				if(fInS != null){
					fInS.close();
				}
				
			}catch(IOException ex){
				ex.printStackTrace();
			}
		}
	}
	
	//返回头部信息
	private void sendHeader(OutputStream oStream, String responseCode, String contentType, long length)throws IOException{
		Date now = new Date();
		String headStr = new StringBuilder(responseCode + "\r\n")
				.append("Date: " + now + "\r\n")
				.append("Server: JHTTP 2.0\r\n")
				.append("Content-length: " + length + "\r\n")
				.append("Content-type: " + contentType + "\r\n")
				.append("\r\n").toString(); //空行表示头信息输入结束
		oStream.write(headStr.getBytes());
		oStream.flush();
	}
	
	//返回错误网页错误信息
	private void responseErrorHtml(OutputStream oStream, String html) throws IOException{
		oStream.write(html.getBytes());
		oStream.flush();
	}
	
	//构造错误提示网页
	private String createHTML(String title, String info){
		String html = new StringBuilder("<HTML>\r\n")
				.append("<HEAD><TITLE>" + title + "</TITLE>\r\n")
				.append("</HEAD>\r\n")
				.append("<BODY>")
				.append("<H1>" + info + "</H1>\r\n")
				.append("</BODY></HTML>\r\n").toString();
		return html;
	}
	
	//返回无法找到文件网页
	public void cannotFindFile(OutputStream oStream, String httpVersion) throws IOException{
		if(HttpForPersonPages.isDebug){
			System.out.println("DEBUG: 找不到文件！");
		}
		String html = createHTML( "File Not Found", "HTTP Error 404: File Not Found");
		if(httpVersion.startsWith("HTTP/")){
			//发送一个MIME首部
			sendHeader(oStream,"HTTP/1.0 404 File Not Found","text/html; charset=utf-8",html.length());
		}
		responseErrorHtml(oStream,html);
		
		if(HttpForPersonPages.isDebug){
			System.out.println("DEBUG: 文件找不到错误返回！");
		}
		HttpForPersonPages.writeIntoLog("Result: 404 NOT FOUND" + "\r\n");
	}

}
