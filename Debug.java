
public class Debug {

	static boolean enable = false;
	    	
	Debug(String msg)
	{
		if(enable)
			System.err.println(msg);
	}
}
