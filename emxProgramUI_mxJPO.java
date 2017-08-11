/* emxProgramUI.java

   Copyright (c) 1992-2016 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of
   Dassault Systemes Inc.  Copyright notice is precautionary only and does
   not evidence any actual or intended publication of such program.

*/

import java.io.*;
import java.util.*;
import matrix.db.*;
import matrix.util.*;
import com.matrixone.apps.program.*;
import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.common.util.*;
import matrix.db.Context;

/**
 * The <code>emxProgramUI</code> class represents the HighCharts UI JPO
 * functionality.
 *
 */
public class emxProgramUI_mxJPO extends emxProgramUIBase_mxJPO
{

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     */
    public emxProgramUI_mxJPO (Context context, String[] args)
        throws Exception
    {
        super(context, args);
    }
}
