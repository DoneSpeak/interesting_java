
import java.net.URLConnection;
import java.util.HashMap;

/*function 提供通用函数，在该项目中主要提供获取文件content-type的作用
 *@author 杨观荣
 *@created 2016/5/8
 *@modified 2016/6/9
 */
public class Helper {

    //如果有多个相同的charset类型，可以使用""代替或者指定统一的文件类型，指定的语句需要放在所有相同的语句的第一个位置
    private static final String[][] MIME_StrTable = {
            //{后缀名，MIME类型}
            //Video
            {".3gp", "video/3gpp"},
            {".asf", "video/x-ms-asf"},
            {".avi", "video/x-msvideo"},
            {".m4u", "video/vnd.mpegurl"},
            {".m4v", "video/x-m4v"},
            {".mov", "video/quicktime"},
            //mp4 统一使用mp4
            {".mp4", "video/mp4"},
            {".mpg4", "video/*"},
            //mpeg 使用相应的默认程序打开，但不添加文件拓展名
            {"", "video/mpeg"},
            {".mpe", "video/*"},
            {".mpeg", "video/*"},
            {".mpg", "video/*"},

            //audio
            {".m3u", "audio/x-mpegurl"},
            //mp4a-latm 使用相应的默认程序打开，但不添加文件拓展名
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
            //plain 使用相应的默认程序打开，但不添加文件拓展名
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

            //统一使用html
            {".html", "text/html"},
            {".htm", "text/html"},

            {".css", "text/css"},

            //image
            //jpeg统一使用jpg
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
            //vnd.ms-powerpoint 使用相应的默认程序打开，但不添加文件拓展名
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

    //创建以拓展名为key值的HahsMap
    public static HashMap<String,String> CreateMIMEMapKeyIsExpands(){

        HashMap<String,String> mimeHashMap = new HashMap<String,String>();

        for(int i = 0; i < MIME_StrTable.length; i ++){
            if(MIME_StrTable[i][0].length() > 0 && (!mimeHashMap.containsKey(MIME_StrTable[i][0]))){
                mimeHashMap.put(MIME_StrTable[i][0],MIME_StrTable[i][1]);
            }
        }
        return mimeHashMap;
    }

    //获取MIME列表的HashMap,设置拓展名为文件拓展名（含有"."）
    public static HashMap<String,String> getMIMEMapKeyIsExpands(){
        //为了防止重复创建消耗时间和消耗资源，将mimeMapKeyIsExpands设置全局变量并赋初值null
        if(mimeMapKeyIsExpands == null){
            mimeMapKeyIsExpands = CreateMIMEMapKeyIsExpands();
        }
        return mimeMapKeyIsExpands;
    }

    //通过文件名获取ContentType
    public static String getContentType(String fileName){
    	//仅仅通过下面一行代码无法获取到部分文件的contentType，从而造成无法文件，导致文件显示不正确
    	String contentType = URLConnection.getFileNameMap().getContentTypeFor(fileName);
    	//当上一行代码无法获取到文件拓展名时，利用文件的拓展名和MIME类型获取文件content-type
		if(contentType == null){
			int lastIndexOfDot = fileName.lastIndexOf(".");
		    if(lastIndexOfDot < 0)
		        return null;//没有拓展名,也就没有contentType
		    
		    //拓展名包含点
		    String extension = fileName.substring(lastIndexOfDot);
		   
	        HashMap<String,String> expandMap = getMIMEMapKeyIsExpands();
	        contentType = expandMap.get(extension);
		}
		
        return contentType;
    }
}
