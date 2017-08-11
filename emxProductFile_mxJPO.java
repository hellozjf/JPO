/*
 *  emxProductFile.java
 *
 * Copyright (c) 1999-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 * static const char RCSID[] = $Id: /ENOProductLine/CNext/Modules/ENOProductLine/JPOsrc/custom/${CLASSNAME}.java 1.3.2.1.1.1 Wed Oct 29 22:17:06 2008 GMT przemek Experimental$
 */
import matrix.db.*;

import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.i18nNow;
/**
 * This JPO class has some methods pertaining to Product File.
 * @author Enovia MatrixOne
 * @version ProductCentral 10.5 - Copyright (c) 2004, MatrixOne, Inc.
 */
public class emxProductFile_mxJPO extends emxProductFileBase_mxJPO
{
  /**
   * Default constructor.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds no arguments
   * @throws Exception if the operation fails
   * @since ProductCentral 10-0-0-0
   * @grade 0
   */
    public emxProductFile_mxJPO (Context context, String[] args)
    throws Exception
  {
    super(context, args);
    }

    /**
   * Main entry point.
     *
   * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return an integer status code (0 = success)
   * @throws Exception if the operation fails
     * @since ProductCentral 10-0-0-0
     */
    public int mxMain(Context context, String[] args)
        throws Exception
    {
        if (!context.isConnected())
        {
            String sContentLabel = EnoviaResourceBundle.getProperty(context,"ProductLine","emxProduct.Error.UnsupportedClient", context.getSession().getLanguage());
            throw new Exception(sContentLabel);
    }
        return 0;
    }
}
