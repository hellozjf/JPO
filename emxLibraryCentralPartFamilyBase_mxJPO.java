/*
 **   Copyright (c) 1992-2016 Dassault Systemes.
 **   All Rights Reserved.
 **   This program contains proprietary and trade secret information of MatrixOne,
 **   Inc.  Copyright notice is precautionary only
 **   and does not evidence any actual or intended publication of such program
 **
 **
 **  static const RCSID [] = "$Id: ${CLASSNAME}.java.rca 1.6 Wed Oct 22 16:02:23 2008 przemek Experimental przemek $";
 */

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.framework.ui.UICache;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;


/**
 * The <code>emxLibraryCentralPartFamilyBase</code> represents implementation of anything on
 * the "To Side" of "SubClass" Relationship in LC Schema
 *
 */

public class emxLibraryCentralPartFamilyBase_mxJPO  extends emxClassification_mxJPO
{

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates emxLibraryCentralPartFamilyBase object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args the Java <code>String[]</code> object
     *    0 - String entry for "objectId"
     * @throws Exception if the operation fails
     */

    public emxLibraryCentralPartFamilyBase_mxJPO (Context context, String[] args) throws Exception
    {
         super(context, args);
    }


    //~ Methods ----------------------------------------------------------------

    /**
     * This method is executed if a specific method is not specified.
     * @param context the eMatrix <code>Context</code> object
     * @param args the Java <code>String[]</code> object
     * @return the Java <code>int</code>
     * @throws Exception if the operation fails
     * @exclude
     */

    public int mxMain (Context context, String[] args) throws Exception
    {
        if (true)
        {
            throw new Exception (
                    "must specify method on emxLibraryCentralPartFamilyBase invocation"
            );
        }

        return 0;
    }
    
    
    public MapList displayENGPartFamilyAttributesInCreate(Context context, String[] args) throws Exception{
        
        MapList fieldMapList=new MapList();
        boolean engInstalled= FrameworkUtil.isSuiteRegistered(context,"appVersionX-BOMEngineering",false,null,null);
        
        if(!engInstalled){
            // return empty fieldMapList if ENG is not installed
            return fieldMapList;
        }
        
        HashMap programMap=(HashMap)JPO.unpackArgs(args);
        HashMap requestMap = (HashMap)programMap.get("requestMap");
        String type = (String)requestMap.get("type");
        String languageStr = (String) programMap.get("languageStr");
        
        String selectedType = "";
        
        StringList slTypes  = FrameworkUtil.split(type, ",");
        selectedType        = (String)slTypes.get(0);
        // If a type is selected from type chooser, then the value will have
        // the prefix "selected type"
        if (selectedType.indexOf("selectedType") >= 0){
            selectedType    = selectedType.substring(selectedType.indexOf(":") + 1);
        }else{
            selectedType    = PropertyUtil.getSchemaProperty(context, selectedType);
        }
        
        boolean isPartFamilyType = mxType.isOfParentType(context,selectedType,TYPE_PART_FAMILY); 
        
        if(!isPartFamilyType){
            // return empty fieldMapList if creaing Class is not of type Part Family
            return fieldMapList;
        }
        
        
        //********************************************************
        // Start creating field maps
        
        // Copy the Field Map from type_PartFamily form
        
        Vector assignments = PersonUtil.getAssignments(context);
        
        MapList fields = UICache.getFields(context, "type_CreatePartFamily", assignments);
        
        Iterator itr = fields.iterator();
        
        while(itr.hasNext()){
            Map fieldMap = (Map)itr.next();
            String fieldName = (String)fieldMap.get(NAME);
            if(fieldName.equals("Part Family Pattern Separator") ||
               fieldName.equals("Part Family Suffix Pattern") ||
               fieldName.equals("Part Family Prefix Pattern") ||
               fieldName.equals("Part Family Sequence Pattern") ){
               
               Map settingsMap = (Map)fieldMap.get("settings");
               settingsMap.put("Validate", "isBadNameChars");

               fieldMapList.add(fieldMap);
            }else if(fieldName.equals("Part Family Base Number") ){
        	   Map settingsMap = (Map)fieldMap.get("settings");
               settingsMap.put("Validate", "isNumeric");

               fieldMapList.add(fieldMap);
            	
            }else if(fieldName.equals("Part Family Name Generator On")){
        	   fieldMapList.add(fieldMap);
            }
        }
        
        
        return fieldMapList;
    }
}
