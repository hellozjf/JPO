/*   emxOverriddenTypeIconBase
**
**   Copyright (c) 2003-2016 Dassault Systemes.
**   All Rights Reserved.
**   This program contains proprietary and trade secret information of MatrixOne,
**   Inc.  Copyright notice is precautionary only
**   and does not evidence any actual or intended publication of such program
**
**   This JPO contains the implementation of Overridden Type Icon
**
*/

import matrix.db.JPO;
import matrix.db.Context;
import matrix.util.StringList;
import matrix.db.BusinessObject;
import matrix.db.BusinessObjectWithSelectList;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.framework.ui.UINavigatorUtil;


public class emxOverriddenTypeIconBase_mxJPO {
    /**
     *
     * @param context
     * @param args
     * @throws Exception
     */
    public emxOverriddenTypeIconBase_mxJPO (Context context, String[] args) throws Exception
    {

    }
     /**
     * @param context
     * @param args
     * @return MapList
     * @throws Exception
     * @Description This method can be customized based upon the user requirement.
     * But it should return the MapList with Size same as ObjectList.
     * For ObjectList key is objectId and value will be image Name
     * For Alternate Objects Maplist will contain HashMaps where object id as a key and alternate object Map(HashMap)
     * as Value. This alternate object map again contains alternate object ids corresponding
     * to current object id and image name as value.
     */
    public MapList getCustomIcons(Context context,String[] args) throws Exception
    {
        //unpack the incoming arguments into a HashMap called 'programMap'
        Map inputMap = (Map)JPO.unpackArgs(args);
        // get the 'objectList' MapList from the tableData HashMap
        MapList objectList = (MapList) inputMap.get("objectList");
        ArrayList alternateObjectList = (ArrayList) inputMap.get("alternateObjectList");
        String bArr [] = new String[objectList.size()];
        StringList bSel = new StringList();
        MapList iconList = new MapList();
        for(int i=0;i<objectList.size();i++){
            bArr[i]= (String)((Map)objectList.get(i)).get("id");

        }
        bSel.add(DomainConstants.SELECT_CURRENT);
        bSel.add(DomainConstants.SELECT_TYPE);
        BusinessObjectWithSelectList bwsl =BusinessObject.getSelectBusinessObjectData(context,bArr,bSel);
        for(int i=0 ;i<objectList.size();i++){
            String state = bwsl.getElement(i).getSelectData(DomainConstants.SELECT_CURRENT);
            String typeName = bwsl.getElement(i).getSelectData(DomainConstants.SELECT_TYPE);
            HashMap relMap = new HashMap();

            Map objectMap = (Map)objectList.get(i);
            String currentObjectId  = (String)objectMap.get("id");
            if(alternateObjectList!=null){
                StringList alternateOIDs = (StringList)alternateObjectList.get(i);
                HashMap alternateMap = new HashMap();
                if(alternateOIDs!=null && alternateOIDs.size()>0){
                    String alternateObjects [] = new String[alternateOIDs.size()];
                    for(int k=0;k<alternateOIDs.size();k++)
                        alternateObjects[k]=(String)alternateOIDs.get(k);

                    BusinessObjectWithSelectList alternatebwsl =BusinessObject.getSelectBusinessObjectData(context,alternateObjects,bSel);
                    for(int j=0;j<alternateOIDs.size();j++){

                        if(alternatebwsl.getElement(j)!=null){
                            state = alternatebwsl.getElement(j).getSelectData(DomainConstants.SELECT_CURRENT);
                            typeName = alternatebwsl.getElement(j).getSelectData(DomainConstants.SELECT_TYPE);
                        }
                        String altOID = (String)alternateOIDs.get(j);
                        String iconName = getIcons(context,typeName,state,altOID);
                        alternateMap.put(altOID,iconName);

                    }
                }
                // Size of the relMap Should not exceed objectList size
                // For each object in object number of alternate object's can be multiple or blank.
                // So, in order to maintanin the size of relMap we put objectid as key and alternateMap as value
                // Alternate Map contains alternateObjectid and the corresponding icon.
                relMap.put(currentObjectId,alternateMap);
              }

            else{
                String iconName = getIcons(context,typeName,state,currentObjectId);
                relMap.put(currentObjectId,iconName);
                }
            iconList.add(relMap);
        }
        return iconList;
    }


    /* User Need to write logic to return the icon name
     * Which is present in common/images directory
     * Below sample code is based on state of object i.e.
     * User can return a image w.r.t to objectid based upon
     * the state of object. User can change the signature of
     * this method but it should return string(image Name)
     */
    public String getIcons(Context context,String typeName,String state,String objectAltId)
    {
        if(state.equalsIgnoreCase("state_name")) //eg Preliminary
           return "image_name.gif";
        else if(state.equalsIgnoreCase("state_name")) //eg Review
            return "image_name.gif";

        else{
            String typeIcon = UINavigatorUtil.getTypeIconProperty(context,typeName);
            return typeIcon;
        }
     }
}
