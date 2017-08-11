/* emxMemberList
 ** Copyright (c) 2002-2016 Dassault Systemes.
 ** All Rights Reserved
 ** This program contains proprietary and trade secret information of
 ** MatrixOne, Inc.  Copyright notice is precautionary only and does
 ** not evidence any actual or intended publication of such program.
 **
 */

import matrix.db.*;
import matrix.util.*;
import java.lang.*;

import java.util.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.domain.*;


public class emxMemberList_mxJPO extends emxMemberListBase_mxJPO
{
	
	//constructor must be provided
	//other wise it will throw "No such Methods" error
	public emxMemberList_mxJPO(Context context, String args[]) throws Exception
	{
		super(context,args);
	}
}
