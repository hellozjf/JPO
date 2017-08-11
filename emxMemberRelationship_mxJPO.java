//
// $Id: ${CLASSNAME}.java.rca 1.13.2.2 Thu Dec  4 07:56:07 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.13.2.1 Thu Dec  4 01:54:58 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.13 Wed Oct 22 15:49:43 2008 przemek Experimental przemek $ 
//
// emxMemberRelationship.java
//
// Copyright (c) 2002-2016 Dassault Systemes.
// All Rights Reserved
// This program contains proprietary and trade secret information of
// MatrixOne, Inc.  Copyright notice is precautionary only and does
// not evidence any actual or intended publication of such program.
//

import java.io.*;
import java.util.*;
import matrix.db.*;
import matrix.util.*;
import com.matrixone.apps.program.*;
import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.common.util.*;

/**
 * The <code>emxMemberRelationship</code> class represents the
 * Member relationship JPO functionality for the AEF type.
 *
 * @version AEF 9.5.1.3 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxMemberRelationship_mxJPO extends emxMemberRelationshipBase_mxJPO
{

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since PMC 10.0.0.0
     */
    public emxMemberRelationship_mxJPO (Context context, String[] args)
        throws Exception
    {
        super(context, args);
    }
}
