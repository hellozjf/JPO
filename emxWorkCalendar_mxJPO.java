/*
**   emxWorkCalendar.java
**
**   Copyright (c) 1992-2016 Dassault Systemes.
**   All Rights Reserved.
**   This program contains proprietary and trade secret information of MatrixOne,
**   Inc.  Copyright notice is precautionary only
**   and does not evidence any actual or intended publication of such program
**
**   static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.7.2.1 Thu Dec  4 07:56:10 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.7 Wed Oct 22 15:49:10 2008 przemek Experimental przemek $
*/

import matrix.db.*;

/**
 * The <code>emxWorkCalendar</code> class contains code for the "Work Calendar" business type
 *
 * @version PMC 10.5.1.2 - Copyright(c) 2004, MatrixOne, Inc.
 */

public class emxWorkCalendar_mxJPO extends emxWorkCalendarBase_mxJPO
{
      /**
       * Constructor.
       *
       * @param context the eMatrix <code>Context</code> object
       * @param args holds no arguments
       * @throws Exception if the operation fails
       * @since PMC 10.5.1.2
       * @grade 0
       */
      public emxWorkCalendar_mxJPO (Context context, String[] args)
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
       * @since PMC 10.5.1.2
       */
      public int mxMain(Context context, String[] args)
          throws Exception
      {
          if (true)
          {
              throw new Exception("must specify method on emxWorkCalendar invocation");
          }
          return 0;
      }

}
