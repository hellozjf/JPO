//
// $Id: ${CLASSNAME}.java.rca 1.9 Wed Oct 22 16:02:20 2008 przemek Experimental przemek $ 
//
/**
 *  emxWorkspaceVault
 *
 *  Copyright (c) 1992-2016 Dassault Systemes.
 *  All Rights Reserved.
 *  This program contains proprietary and trade secret information of MatrixOne,
 *  Inc.  Copyright notice is precautionary only
 *  and does not evidence any actual or intended publication of such program
 *
 *  FileName : "$RCSfile: ${CLASSNAME}.java.rca $"
 *  Author   : "$Author: przemek $"
 *  Version  : "$Revision: 1.9 $"
 *  Date     : "$Date: Wed Oct 22 16:02:20 2008 $"
 *
 */

import matrix.db.Context;

/**
 * The <code>emxWorkspaceVault</code> class represents the "Workspace Vault"
 * type in the AEF.
 *
 * @version AEF 9.5.4.0 - Copyright (c) 2003, MatrixOne, Inc.
 */
public class emxWorkspaceVault_mxJPO extends emxWorkspaceVaultBase_mxJPO
{
    //~ Constructors -----------------------------------------------------------

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     */
    public emxWorkspaceVault_mxJPO (Context context, String[] args) throws Exception
    {
        super (context, args);

        if ((args != null) && (args.length > 0))
        {
            setId (args[0]);
        }
    }

    /**
     * Constructor
     * @param id the Java <code>String</code> object
     * @throws Exception if the operation fails
     */
    public emxWorkspaceVault_mxJPO (String id) throws Exception
    {

        super (id);
    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return int
     * @throws Exception if the operation fails
     */
    public int mxMain (Context context, String[] args) throws Exception
    {
        if (true)
        {
            throw new Exception (
                    "Must specify method on emxWorkspaceVault invocation");
        }

        return 0;
    }
}
