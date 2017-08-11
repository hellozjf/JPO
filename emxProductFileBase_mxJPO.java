/*
**   emxProductFileBase.java
**
**   Copyright (c) 2004-2016 Dassault Systemes.
**   All Rights Reserved.
**   This program contains proprietary and trade secret information of MatrixOne,
**   Inc.  Copyright notice is precautionary only
**   and does not evidence any actual or intended publication of such program
**
**   static const char RCSID[] = $Id: /ENOProductLine/CNext/Modules/ENOProductLine/JPOsrc/base/${CLASSNAME}.java 1.3.2.1.1.1 Wed Oct 29 22:17:06 2008 GMT przemek Experimental$
*/

import matrix.db.*;


/**
 * This JPO class has some methods pertaining to Product File.
 * @author Enovia MatrixOne
 * @version ProductCentral 10.5 - Copyright (c) 2004, MatrixOne, Inc.
 */
public class emxProductFileBase_mxJPO extends emxCommonFile_mxJPO
{
    /** separator used for version objects in revision field. */
    protected static final String DELIMITER = " :: ";


    /**
     * Default Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since ProductCentral 10-0-0-0
     * @grade 0
     */
    public emxProductFileBase_mxJPO (Context context, String[] args)
        throws Exception
    {
        super(context, args);
    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return int 0 -success
     * @throws Exception if the operation fails
     * @since ProductCentral 10-0-0-0
     */
    public int mxMain(Context context, String[] args)
        throws Exception
    {
        if (true)
        {
            throw new Exception("must specify method on emxProductFile invocation");
        }
        return 0;
    }





}
