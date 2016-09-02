import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/*
 * function 配置日志文件，规定日志文件的名称、格式，并将日志文件保存到服务器的"服务器日志"目录中
 * @param loggerDir String类型 指定日志文件的保存文件目录，该项目中保存到服务器根目录下的"服务器日志"中
 * @author 杨观荣 2016/6/9
 */
public class LoggerUtil{
	
	private String loggerDir;
	
	public LoggerUtil(String loggerDir){
		this.loggerDir = loggerDir;
	}
	
	//使用当天的年-月-日作为日志的名称
	private String getLogName(){
		StringBuffer logPath = new StringBuffer(HttpForPersonPages.SERVERDIR_DEFAULT + File.separator + loggerDir);
		File file = new File(logPath.toString());
		if(!file.exists()){
			file.mkdir();
		}
		
		SimpleDateFormat sdf =  new SimpleDateFormat("yyyy-MM-dd");
		logPath.append(File.separator + sdf.format(new Date()) + ".txt"); 
		
		return logPath.toString();
	}
	
	//设置logger对象输出属性为输出到文件
	public void setLogingProperties(Logger logger){
		setLogingProperties(logger,Level.ALL);
	}
	
	public void setLogingProperties(Logger logger, Level level){
		FileHandler fhandle;
		try{
			//输出到日志文件
			fhandle = new FileHandler(getLogName(),true);
			logger.addHandler(fhandle);
			//输出到控制台,为了清楚的看到错误的调试信息，这里用e.printStackTrace();不用输出日志
//			logger.addHandler(new ConsoleHandler());
			//设置输出格式
			fhandle.setFormatter(new SimpleFormatter());
		}catch(SecurityException e){
			logger.log(Level.SEVERE,"安全错误",e);
		}catch(IOException e){
			logger.log(Level.SEVERE, "读取日志文件出错", e);
		}
	}
	
}