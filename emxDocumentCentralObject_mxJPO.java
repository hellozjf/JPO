//
// $Id: ${CLASSNAME}.java.rca 1.8 Wed Oct 22 16:02:35 2008 przemek Experimental przemek $ 
//
/*
 **  emxDocumentCentralObject.java
 **
 **  Copyright (c) 1992-2016 Dassault Systemes.
 **  All Rights Reserved.
 **  This program contains proprietary and trade secret information of MatrixOne,
 **  Inc.  Copyright notice is precautionary only
 **  and does not evidence any actual or intended publication of such program
 **
 **  FileName : "$RCSfile: ${CLASSNAME}.java.rca $"
 **  Author   : Brian R. Tibbetts
 **  Version  : "$Revision: 1.8 $"
 **  Date     : "$Date: Wed Oct 22 16:02:35 2008 $"
 **
 */

import matrix.db.Context;

/**
 * The <code>emxDocumentCentralObject</code> class.
 *
 * @version AEF 9.5.7.0 - Copyright (c) 2002, MatrixOne, Inc.
 */

public class emxDocumentCentralObject_mxJPO extends emxDocumentCentralObjectBase_mxJPO
{

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args that hold objectid"
     *
     * @throws Exception if the operation fails
     *
     * @since AEF 9.5.0.0
     */

    public emxDocumentCentralObject_mxJPO (Context context, String[] args) throws Exception
    {
        super (args[0]);

        if ((args != null) && (args.length > 0))
        {
            setId (args[0]);
        }
    }

    /**
     * Constructor
     *
     * @param id the Java <code>String</code> object
     *
     * @throws Exception if the operation fails
     *
     */

    public emxDocumentCentralObject_mxJPO (String id) throws Exception
    {
        // Call the super constructor

        super (id);
    }
}
