/*
 * emxIntegrationMigrationBase.java
 * program migrates the data into Common Document model i.e. to 10.5 schema.
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
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

  public class emxIntegrationMigrationBase_mxJPO
  {
    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF Rossini
     * @grade 0
     */
    public emxIntegrationMigrationBase_mxJPO (Context context, String[] args)
        throws Exception
    {
    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @returns nothing
     * @throws Exception if the operation fails
     * @since VCP 10.5.0.0
     */
    public int mxMain(Context context, String[] args)
        throws Exception
    {
        if (true)
        {
            throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.Generic.MethodOnCommonFile", context.getLocale().getLanguage()));
        }
        return 0;
    }

    public StringList getIEFSelectables(Context context) throws Exception
    {
        return new StringList();
    }

    public boolean validateIEFModel(Context context, Map map) throws Exception
    {
        return false;
    }

    public boolean migrateIEFModel(Context context , Map map) throws Exception
    {
        return true;
    }

  }
