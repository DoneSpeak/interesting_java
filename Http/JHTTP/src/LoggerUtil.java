import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/*
 * function ������־�ļ����涨��־�ļ������ơ���ʽ��������־�ļ����浽��������"��������־"Ŀ¼��
 * @param loggerDir String���� ָ����־�ļ��ı����ļ�Ŀ¼������Ŀ�б��浽��������Ŀ¼�µ�"��������־"��
 * @author ����� 2016/6/9
 */
public class LoggerUtil{
	
	private String loggerDir;
	
	public LoggerUtil(String loggerDir){
		this.loggerDir = loggerDir;
	}
	
	//ʹ�õ������-��-����Ϊ��־������
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
	
	//����logger�����������Ϊ������ļ�
	public void setLogingProperties(Logger logger){
		setLogingProperties(logger,Level.ALL);
	}
	
	public void setLogingProperties(Logger logger, Level level){
		FileHandler fhandle;
		try{
			//�������־�ļ�
			fhandle = new FileHandler(getLogName(),true);
			logger.addHandler(fhandle);
			//���������̨,Ϊ������Ŀ�������ĵ�����Ϣ��������e.printStackTrace();���������־
//			logger.addHandler(new ConsoleHandler());
			//���������ʽ
			fhandle.setFormatter(new SimpleFormatter());
		}catch(SecurityException e){
			logger.log(Level.SEVERE,"��ȫ����",e);
		}catch(IOException e){
			logger.log(Level.SEVERE, "��ȡ��־�ļ�����", e);
		}
	}
	
}