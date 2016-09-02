import java.util.HashMap;

public class Helper {
	
	public static void initHelper(){
		//TODO将需要一次性初始化的操作放在这里
	}

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
    
    //end键
    public static final int ENDKEY = 0;
	//向上键 ^[[A
	public static final int UPKEY = 1;
	//向下键 ^[[B
	public static final int DOWNKEY = 2;
	//向右键 ^[[C
	public static final int RIGHTKEY = 3;
	//向左键 ^[[D
	public static final int LEFTKEY = 4;
	//esc键   ^[
	public static final int ESCKEY = 5;
	//insert键 ^[[2~
	public static final int INSERTKEY = 6;
	//delete键 ^[[3~
	public static final int DELETEKEY = 7;
	//home键
	public static final int HOMEKEY = 8;
	
	
}
