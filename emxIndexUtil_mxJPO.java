/*
 **
 **   Copyright (c) 2002-2016 Dassault Systemes.
 **   All Rights Reserved.
 **
 **   This JPO contains the implementation of emxFullSearch.
 **
 */

import java.util.Map;

import matrix.db.Context;
import matrix.db.MQLCommand;

import com.matrixone.search.index.Config;
import com.matrixone.search.index.ConfigModeler;
import com.matrixone.search.index.ConfigParser;


public class emxIndexUtil_mxJPO {

    public emxIndexUtil_mxJPO(Context context, String[] args)throws Exception {
    }

    public static void setActiveApps(Context ctx, String[] args) throws Exception {

        ConfigModeler cfgmdl = new ConfigModeler(ctx);
        for (String arg: args) {
            cfgmdl.setApplication(arg, "true");
        }
        cfgmdl.commit(ctx);

    }

}
