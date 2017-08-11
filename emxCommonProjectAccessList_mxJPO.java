// emxCommonProjectAccessList.java
//
// Copyright (c) 2002-2016 Dassault Systemes.
// All Rights Reserved
// This program contains proprietary and trade secret information of
// MatrixOne, Inc.  Copyright notice is precautionary only and does
// not evidence any actual or intended publication of such program.
//
//

import java.io.*;
import java.util.*;
import matrix.db.*;
import matrix.util.*;

import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.common.util.*;
import com.matrixone.apps.common.Person;

/**
 * The <code>emxProjectAccessList</code> class represents the Project Access
 * List JPO functionality for the AEF type.
 *
 * @version AEF 9.5.1.1 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxCommonProjectAccessList_mxJPO extends emxCommonProjectAccessListBase_mxJPO
{

    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since PMC 10.0.0.0
     * @grade 0
     */
    public emxCommonProjectAccessList_mxJPO (Context context, String[] args)
        throws Exception
    {
      super(context, args);
    }
}
