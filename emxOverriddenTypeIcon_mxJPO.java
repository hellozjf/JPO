/*   emxOverriddenTypeIcon
**
**   Copyright (c) 2003-2016 Dassault Systemes.
**   All Rights Reserved.
**   This program contains proprietary and trade secret information of MatrixOne,
**   Inc.  Copyright notice is precautionary only
**   and does not evidence any actual or intended publication of such program
**
**   This JPO contains the implementation of Overridden Type Icon
**
*/

import java.util.Locale;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import matrix.db.Context;

public class emxOverriddenTypeIcon_mxJPO extends emxOverriddenTypeIconBase_mxJPO
{
      /**
       * Constructor.
       *
       * @param context the eMatrix <code>Context</code> object
       * @param args holds no arguments
       * @throws Exception if the operation fails
       * @since Common 10.0.0.0
       * @grade 0
       */
      public emxOverriddenTypeIcon_mxJPO (Context context, String[] args)
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
       * @since Common 10.0.0.0
       */
      public int mxMain(Context context, String[] args)
          throws Exception
      {
          if (true)
          {
              String languageStr = context.getSession().getLanguage();
              String exMsg = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Message.Invocation", new Locale(languageStr));              
              exMsg += EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.CommonFile", new Locale(languageStr));              
              throw new Exception(exMsg);
          }
          return 0;
      }

}
