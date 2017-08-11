/*   emxEnhancedSearch.
**
**   Copyright (c) 2002-2016 Dassault Systemes.
**   All Rights Reserved.
**   This program contains proprietary and trade secret information of MatrixOne,
**   Inc.  Copyright notice is precautionary only
**   and does not evidence any actual or intended publication of such program.
**
**   This JPO contains the implementation of emxEnhancedSearch.
**
*/

import matrix.db.Context;

import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.util.i18nNow;

public class emxEnhancedSearch_mxJPO extends emxEnhancedSearchBase_mxJPO {


	/* The default constructor */
	public emxEnhancedSearch_mxJPO (Context context, String[] args)
		throws Exception {
	  super(context, args);
	}


	/* Main entry point */
	public int mxMain(Context context, String[] args)
		throws Exception {
		if (!context.isConnected()){
   			throw  new Exception(ComponentsUtil.i18nStringNow("emxComponents.EnhancedSearch.JPONotFound", context.getLocale().getLanguage()));
 		}
		return 0;
	}



}//End of the class
