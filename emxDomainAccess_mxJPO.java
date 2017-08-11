/*
**  emxDomainAccess
**
**  Copyright (c) 1992-2016 Dassault Systemes.
**  All Rights Reserved.
**  This program contains proprietary and trade secret information of MatrixOne,
**  Inc.  Copyright notice is precautionary only
**  and does not evidence any actual or intended publication of such program.
**
*/

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.matrixone.apps.domain.DomainAccess;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;


/**

 */
public class emxDomainAccess_mxJPO extends emxDomainAccessBase_mxJPO {


    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation
     * @since V6R2011x
     * @grade 0
     */
    public emxDomainAccess_mxJPO (Context context, String[] args)
        throws Exception
    {
        super(context, args);
    }
}
