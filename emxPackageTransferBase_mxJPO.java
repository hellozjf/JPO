/*   emxPackageTransferBase.
**
**   Copyright (c) 2002-2016 Dassault Systemes.
**   All Rights Reserved.
**   This program contains proprietary and trade secret information of MatrixOne,
**   Inc.  Copyright notice is precautionary only
**   and does not evidence any actual or intended publication of such program.
**
**   This JPO contains the method to check whether download package link is to be displayed.
**
*/

import matrix.db.*;
import matrix.util.*;
import java.io.*;
import java.util.*;
import java.text.*;

import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.common.*;
import com.matrixone.apps.common.util.*;
/**
 * The <code>emxPackageTransferBase</code> class contains implementation code for emxPackageTransfer.
 *
 * @version EC 10.5 - Copyright (c) 2002, MatrixOne, Inc.
 */

@Deprecated
public class emxPackageTransferBase_mxJPO extends emxDomainObject_mxJPO
{
    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds no arguments.
     * @throws Exception if the operation fails.
     * @since EC 10.5.
     */
    public emxPackageTransferBase_mxJPO (Context context, String[] args)
        throws Exception
    {
        super(context, args);
    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds no arguments.
     * @return an int.
     * @throws Exception if the operation fails.
     * @since EC 10.5.
     */
    public int mxMain(Context context, String[] args)
        throws Exception
    {
        if (true)
        {
            throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.PackageTransfer.SpecifyMethodOnPackageTransferInvocation", context.getLocale().getLanguage()));
        }
        return 0;
    }

    /**
     * This method return true if FCS is enabled & property is set to true
     * otherwise return false.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds objectId and param values.
     * @return Boolean true if FCS is enabled and property set to true othewise return false.
     * @throws Exception If the operation fails.
     * @since EC 10-5.
     */
    public Boolean hasDownloadAndPackageTransfer(Context context, String[] args)
         throws Exception
    {
        Boolean showDownloadLink = Boolean.valueOf(false);

        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        String objectId  = (String) paramMap.get("objectId");

        setId(objectId);

        try{
            String enablePackageTransfer = EnoviaResourceBundle.getProperty(context,"emxFramework.PackageAndTransfer.Enabled");
            boolean isPackageTransferEnabled = "true".equalsIgnoreCase(enablePackageTransfer);
            String enableFCSCheck = EnoviaResourceBundle.getProperty(context,"emxFramework.FcsEnabled");
            boolean isFcsEnabled = "true".equalsIgnoreCase(enableFCSCheck);

            if (isPackageTransferEnabled && isFcsEnabled)
            {
                showDownloadLink = Boolean.valueOf(true);
            }
        }
        catch(Exception e)
        {
        }

        return showDownloadLink;
    }


}
