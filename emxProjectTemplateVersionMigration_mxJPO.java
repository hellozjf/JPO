/*
 * Copyright (c) 2003-2016 Dassault Systemes.  All Rights Reserved.
 *  This program contains proprietary and trade secret information of
 *  Dassault Systemes.  Copyright notice is precautionary only and does not
 *  evidence any actual or intended publication of such program.
 *
 *    $Id:$
 */


import matrix.db.Context;

// Usage:
//     export bus Timephase * * xml continue !archive into file "C:\Temp\Timephase_preMigrate.xml";
//     exec prog emxCommonFindObjects 1000 Timephase C:/temp;
//     exec prog EVMTimephaseMigrate C:/temp 1 n;
//     export bus Timephase * * xml continue !archive into file "C:\Temp\Timephase_postMigrate.xml";
//
public class emxProjectTemplateVersionMigration_mxJPO extends emxProjectTemplateVersionMigrationBase_mxJPO {

    public emxProjectTemplateVersionMigration_mxJPO(Context context, String[] args)
            throws Exception
    {
        super(context, args);
    }

}
