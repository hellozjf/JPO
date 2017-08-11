/*   emxSearchIndexExpansionBase.java
**
**   Copyright (c) 2002-2016 Dassault Systemes.
**   All Rights Reserved.
**   This program contains proprietary and trade secret information of MatrixOne,
**   Inc.  Copyright notice is precautionary only
**   and does not evidence any actual or intended publication of such program.
**
**   This JPO contains the implementation of emxSearchIndexExpansion
**
*/
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import matrix.db.Context;
import matrix.db.SelectConstants;
import matrix.util.StringList;

import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;

public class emxSearchIndexExpansionBase_mxJPO {
    protected String _relNamesCSL = null;
    protected String _direction = "TO";
    public static final String DELIMITER_MINOR = SelectConstants.cSelectDelimiter;
    public static final String DELIMITER_MAJOR = "|";
    
    public emxSearchIndexExpansionBase_mxJPO (Context context, String[] args) throws Exception {
        if (args.length != 1) {
            throw new Exception("Expected 1 constructor arg (CSL of symbolic rel names); got " + args.length);
        }
        _relNamesCSL = args[0];
        //initRelNames(context, args[0]);
    }
/*
    public void initRelNames(Context context, String symRelsCsv) throws Exception {
        StringList symRels = FrameworkUtil.split(symRelsCsv, ",");
        StringList rels = new StringList(symRels.size());
        
        for (int i = 0; i < symRels.size(); i++) {
            String sym = (String) symRels.get(i);
            String relName = PropertyUtil.getSchemaProperty(context, sym);
            if (relName == null || relName.equals("")) {
                relName = sym;
            }
            rels.add(relName);
        }
        _relNamesCSL = FrameworkUtil.join(rels, ",");
    }
*/
    public String mxMain(Context context, String[] args) throws Exception {
        return getExpansionPaths(context, args);
    }

    public String getExpansionPaths(Context context, String[] args) throws Exception {
        if (args.length != 1) {
            throw new Exception("Expected 1 arg, got " + args.length);
        }
        
        String objId = args[0];
            
        StringList tmpList = new StringList();
        List data = getExpansionPaths(context, objId);
        for (int i = 0; i < data.size(); i++) {
            StringList element = (StringList) data.get(i);
            tmpList.add(FrameworkUtil.join(element, DELIMITER_MINOR));
        }
        return FrameworkUtil.join(tmpList, DELIMITER_MAJOR);
    }
    
    public ArrayList getExpansionPaths(Context context, String objectId) throws Exception {
        ArrayList res = new ArrayList();
        StringList objectSelects = new StringList(6);
        objectSelects.addElement("id");
        objectSelects.addElement("type");
        objectSelects.addElement("name");
        objectSelects.addElement("revision");
        StringList relselects = new StringList();

        DomainObject domObj = new DomainObject(objectId);
        MapList mList = null;
        mList = domObj.getRelatedObjects(context,
        		_relNamesCSL,
                "*",
                objectSelects,
                relselects,
                true,//boolean getTo,
                false,//boolean getFrom,
                (short)0,
                null,
                null,
                0);

        java.util.Iterator itrr = mList.iterator();
        StringList path = new StringList();
        int prevLevel = 1;
        while(itrr.hasNext()){
            Map hMap = (Map) itrr.next();
            String lvlStr = (String)hMap.get("level");
            String oid = (String)hMap.get("id");
            int level = Integer.parseInt(lvlStr);
            if (level <= prevLevel) {
                StringList newPath = new StringList();
                newPath.addAll(path.subList(0,level-1));
                res.add(newPath);
                path = newPath;
            }
            prevLevel = level;
            path.add(0, oid);
        }
        return res;
    }
     
}
