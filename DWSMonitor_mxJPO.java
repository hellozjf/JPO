/*

To put the program in a database:
insert prog /tmp/${CLASSNAME}.java;


USAGE : exec prog DWSMonitor -workspaceid WSID -outputfile OUTPUTFILE
 */

import java.util.*;
import matrix.util.*;
import matrix.db.*;
import com.dassault_systemes.dwsmonitor.*;

public final class DWSMonitor_mxJPO {
       	
    public static void mxMain(Context context, String[] args) throws Exception 
	{	
		DWSMonitor.run(context, args);		
    }	
}
