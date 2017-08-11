/*
 * emxCommonDocumentFindUnConvertableObjectsBase.java
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

public class emxCommonDocumentFindUnConvertableObjectsBase_mxJPO
{
    BufferedWriter writer = null;
    /** Create new instance of emxIntegrationMigration class. */
    protected emxCommonDocumentMigration_mxJPO documentMigration = null;

    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @grade 0
     */
    public emxCommonDocumentFindUnConvertableObjectsBase_mxJPO (Context context, String[] args)
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
     */
    public int mxMain(Context context, String[] args)
        throws Exception
    {
        writer     = new BufferedWriter(new MatrixWriter(context));
        System.out.println("******* In Scan JPO11 with Mig ************");

        writer.write("=======================================================\n\n");
        writer.write("                Finding UnConvertable Document Objects...\n");
        writer.write("                File (" + args[1] + ") to (" + args[2] + ")\n");
        writer.write("                Reading files from: " + args[0] + "\n");
        writer.write("                Objects which cannot be migrated will be written to:  unConvertedObjectIds.csv\n");
        writer.write("                Logging of this Migration will be written to: migration.log\n\n");
        writer.write("=======================================================\n\n");
        writer.flush();

        Map paramMap = new HashMap();
        paramMap.put("args", args);
        paramMap.put("writer", writer);

        documentMigration = new emxCommonDocumentMigration_mxJPO(context, null);
        documentMigration.scanObjects(context, paramMap);

        /*
        JPO.invoke(context,                        // matrix context
                   "emxCommonDocumentMigration",   // program name
                    null,                          // constructor arguments
                    "scanObjects",                 // method name
                    args);                       // method arguments
        */

        writer.write("=======================================================\n");
        writer.write("                Finding UnConvertable Document Objects  COMPLETE\n");
        writer.write(" \n");
        writer.write("                Objects which cannot be migrated will be written to:  unConvertedObjectIds.csv\n");
        writer.write("                Logging of this Migration will be written to: migration.log\n\n");
        writer.write("=======================================================\n");
        writer.flush();

        System.out.println("******* In Scan JPO22 with Mig ************ ");

        writer.close();
        return 0;
    }
}
