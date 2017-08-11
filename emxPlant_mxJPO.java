/*
**   emxPlant
**
**   Copyright (c) 2007-2008 Enovia MatrixOne, Inc.
**   All Rights Reserved.
**   This program contains proprietary and trade secret information of MatrixOne,
**   Inc.  Copyright notice is precautionary only
**   and does not evidence any actual or intended publication of such program
**
*/

import matrix.db.Context;

import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.i18nNow;

/**
 * The <code>emxPlant</code> class contains methods for emxPlant.
 *
 * @version Common X3 - Copyright(c) 2007, MatrixOne, Inc.
 */


public class emxPlant_mxJPO extends emxPlantBase_mxJPO
{
      /**
       * Constructor.
       *
       * @param context the eMatrix <code>Context</code> object
       * @param args holds no arguments
       * @throws Exception if the operation fails
       */
      public emxPlant_mxJPO (Context context, String[] args)
          throws Exception
      {
          super(context, args);
      }

      /**
       * This method is executed if a specific method is not specified.
       *
       * @param context the eMatrix <code>Context</code> object
       * @param args holds no arguments
       * @returns int
       * @throws Exception if the operation fails
       * @version X3
       */
       public int mxMain(Context context, String[] args)
	            throws Exception
	        {
	            if (true)
	            {
	              String strAlertMessage = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Plant.MainMethodNotLoaded");

	                throw new Exception(strAlertMessage);
	            }
	            return 0;
      }

}
