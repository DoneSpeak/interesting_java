
import java.net.URLConnection;
import java.util.HashMap;

/*function �ṩͨ�ú������ڸ���Ŀ����Ҫ�ṩ��ȡ�ļ�content-type������
 *@author �����
 *@created 2016/5/8
 *@modified 2016/6/9
 */
public class Helper {

    //����ж����ͬ��charset���ͣ�����ʹ��""�������ָ��ͳһ���ļ����ͣ�ָ���������Ҫ����������ͬ�����ĵ�һ��λ��
    private static final String[][] MIME_StrTable = {
            //{��׺����MIME����}
            //Video
            {".3gp", "video/3gpp"},
            {".asf", "video/x-ms-asf"},
            {".avi", "video/x-msvideo"},
            {".m4u", "video/vnd.mpegurl"},
            {".m4v", "video/x-m4v"},
            {".mov", "video/quicktime"},
            //mp4 ͳһʹ��mp4
            {".mp4", "video/mp4"},
            {".mpg4", "video/*"},
            //mpeg ʹ����Ӧ��Ĭ�ϳ���򿪣���������ļ���չ��
            {"", "video/mpeg"},
            {".mpe", "video/*"},
            {".mpeg", "video/*"},
            {".mpg", "video/*"},

            //audio
            {".m3u", "audio/x-mpegurl"},
            //mp4a-latm ʹ����Ӧ��Ĭ�ϳ���򿪣���������ļ���չ��
            {"", "audio/mp4a-latm"},
            {".m4a", "audio/*"},
            {".m4b", "audio/*"},
            {".m4p", "audio/*"},

            //x-mpeg
            {".mp2", "x-mpeg"},
            {".mp3", "audio/x-mpeg"},

            {".mpga", "audio/mpeg"},
            {".ogg", "audio/ogg"},
            {".rmvb", "audio/x-pn-realaudio"},
            {".wav", "audio/x-wav"},
            {".wma", "audio/x-ms-wma"},
            {".wmv", "audio/x-ms-wmv"},

            //text
            //plain ʹ����Ӧ��Ĭ�ϳ���򿪣���������ļ���չ��
            {"", "text/plain"},
            {".c", "text/plain"},
            {".java", "text/plain"},
            {".conf", "text/plain"},
            {".cpp", "text/plain"},
            {".h", "text/plain"},
            {".prop", "text/plain"},
            {".rc", "text/plain"},
            {".sh", "text/plain"},
            {".log", "text/plain"},
            {".txt", "text/plain"},
            {".xml", "text/plain"},

            //ͳһʹ��html
            {".html", "text/html"},
            {".htm", "text/html"},

            {".css", "text/css"},

            //image
            //jpegͳһʹ��jpg
            {".jpg", "image/jpeg"},
            {".jpeg", "image/jpeg"},


            {".bmp", "image/bmp"},
            {".gif", "image/gif"},
            {".png", "image/png"},

            //application
            {"", "application/octet-stream"},
            {".bin", "application/octet-stream"},
            {".class", "application/octet-stream"},
            {".exe", "application/octet-stream"},
            {"class", "application/octet-stream"},

            {".apk", "application/vnd.android.package-archive"},
            {".doc", "application/msword"},
            {".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"},
            {".xls", "application/vnd.ms-excel"},
            {".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"},

            {".gtar", "application/x-gtar"},
            {".gz", "application/x-gzip"},
            {".jar", "application/java-archive"},
            {".js", "application/x-javascript"},
            {".mpc", "application/vnd.mpohun.certificate"},
            {".msg", "application/vnd.ms-outlook"},
            {".pdf", "application/pdf"},
            //vnd.ms-powerpoint ʹ����Ӧ��Ĭ�ϳ���򿪣���������ļ���չ��
            {"", "application/vnd.ms-powerpoint"},
            {".pps", "application/vnd.ms-powerpoint"},
            {".ppt", "application/vnd.ms-powerpoint"},

            {".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"},
            {".rtf", "application/rtf"},
            {".tar", "application/x-tar"},
            {".tgz", "application/x-compressed"},
            {".wps", "application/vnd.ms-works"},
            {".z", "application/x-compress"},
            {".zip", "application/x-zip-compressed"},
//		{"", "*/*"}
    };

    static HashMap<String,String> mimeMapKeyIsExpands = null;

    //��������չ��Ϊkeyֵ��HahsMap
    public static HashMap<String,String> CreateMIMEMapKeyIsExpands(){

        HashMap<String,String> mimeHashMap = new HashMap<String,String>();

        for(int i = 0; i < MIME_StrTable.length; i ++){
            if(MIME_StrTable[i][0].length() > 0 && (!mimeHashMap.containsKey(MIME_StrTable[i][0]))){
                mimeHashMap.put(MIME_StrTable[i][0],MIME_StrTable[i][1]);
            }
        }
        return mimeHashMap;
    }

    //��ȡMIME�б��HashMap,������չ��Ϊ�ļ���չ��������"."��
    public static HashMap<String,String> getMIMEMapKeyIsExpands(){
        //Ϊ�˷�ֹ�ظ���������ʱ���������Դ����mimeMapKeyIsExpands����ȫ�ֱ���������ֵnull
        if(mimeMapKeyIsExpands == null){
            mimeMapKeyIsExpands = CreateMIMEMapKeyIsExpands();
        }
        return mimeMapKeyIsExpands;
    }

    //ͨ���ļ�����ȡContentType
    public static String getContentType(String fileName){
    	//����ͨ������һ�д����޷���ȡ�������ļ���contentType���Ӷ�����޷��ļ��������ļ���ʾ����ȷ
    	String contentType = URLConnection.getFileNameMap().getContentTypeFor(fileName);
    	//����һ�д����޷���ȡ���ļ���չ��ʱ�������ļ�����չ����MIME���ͻ�ȡ�ļ�content-type
		if(contentType == null){
			int lastIndexOfDot = fileName.lastIndexOf(".");
		    if(lastIndexOfDot < 0)
		        return null;//û����չ��,Ҳ��û��contentType
		    
		    //��չ��������
		    String extension = fileName.substring(lastIndexOfDot);
		   
	        HashMap<String,String> expandMap = getMIMEMapKeyIsExpands();
	        contentType = expandMap.get(extension);
		}
		
        return contentType;
    }
}
