/** emxTriggerDynamicColumns

**   Copyright (c) 1992-2016 Dassault Systemes.
**   All Rights Reserved.
**   This program contains proprietary and trade secret information of MatrixOne,
**   Inc.  Copyright notice is precautionary only
**   and does not evidence any actual or intended publication of such program
**
**/



import java.util.*;
import matrix.db.*;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.DomainObject;
import matrix.util.*;



public class emxTriggerDynamicColumns_mxJPO extends emxTriggerDynamicColumnsBase_mxJPO
{
	/**
	 * Constructor.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param sAppInfoFile AppInfo.rul file name
	 * @throws Exception if the operation fails
	 * @since AEF 10.Next
     */
	public emxTriggerDynamicColumns_mxJPO (Context context, String args[])throws Exception
	{
		super(context,args);
	}

}
