/*
**  emxPartFamilyConversion
**
**  Copyright (c) 1992-2015 Dassault Systemes.
**  All Rights Reserved.
**  This program contains proprietary and trade secret information of MatrixOne,
**  Inc.  Copyright notice is precautionary only
**  and does not evidence any actual or intended publication of such program
**
*/

import matrix.db.Context;

/**
 * The <code>emxPartFamilyConversion</code>
 * class contains script to migrate 'Part Family Member' relationship to
 * 'Classified Item', 'Part Family' policy to 'Classification' policy
 *
 * @since AEF 10-6
 */

public class emxPartFamilyConversion_mxJPO extends emxPartFamilyConversionBase_mxJPO
{

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 10-6
     */

    public emxPartFamilyConversion_mxJPO (Context context, String[] args)
        throws Exception
    {
        super(context, args);
    }

}
