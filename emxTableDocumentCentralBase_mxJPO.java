/* emxTableDocumentCentralBase.java

   Copyright (c) 1992-2016 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of
   MatrixOne, Inc.  Copyright notice is precautionary only and does
   not evidence any actual or intended publication of such program.

   static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.11 Wed Oct 22 16:02:47 2008 przemek Experimental przemek $
*/

import java.util.HashMap;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Iterator;


import matrix.db.Context;
import matrix.db.JPO;

import matrix.util.StringList;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.PropertyUtil;

import com.matrixone.apps.document.util.MxDebug;

/**
 *  The <code>${CLASSNAME}</code> class is used
 *  to generate HTML code for the business type column on business type
 *  list page for display rule.
 *
 *  @exclude
 *
 */
public class emxTableDocumentCentralBase_mxJPO
{
   /**
    *  Constructs a new JPO object.
    *
    *  @param context the eMatrix <code>Context</code> object
    *  @param args holds no arguments
    *  @throws Exception if the operation fails
    *
    *  @since AEF 9.5.6.0
    */
   public emxTableDocumentCentralBase_mxJPO ( Context context, String[] args )
          throws Exception
   {
     /*
      *  Author    : MT
      *  Date      : 04/07/2003
      *  Notes     :
      *  History   :
      */

      if ( !context.isConnected() )
      {
         throw new Exception( "not supported on desktop client" );
      }
   }

   /**
    *  This method is executed if a specific method is not specified.
    *
    *  @param context the eMatrix <code>Context</code> object
    *  @param args holds no arguments
    *  @return int 0
    *  @throws Exception if the operation fails
    *
    *  @since AEF 9.5.6.0
    */
   public int mxMain( Context context, String[] args )
          throws Exception
   {
     /*
      *  Author    : MT
      *  Date      : 1/30/2003
      *  Notes     :
      *  History   :
      */

      if ( !context.isConnected() )
      {
         throw new Exception( "not supported on desktop client" );
      }

      return 0;
   }

   /**
    *  This method is to be called from a UI component Table. The method
    *  suppress the revision value for the type type_ProjectVault.
    *  The type type_ProjectVault uses a auto generated value and users don't
    *  want to see the value in the UI interface.  The method takes the
    *  objectList from the UI table and parses through the revision values.
    *  Objects of type type_ProjectVault have the revision value replaced with
    *  a blank value.  For all other types, the revision values is returned.
    *
    *  @param context the eMatrix <code>Context</code> object
    *  @param args an array of String arguments for this method
    *  @return Vector object that contains a vector of revision values
    *          for the column.
    *  @throws Exception if the operation fails
    *
    *  @since AEF 9.5.6.0
    */
   public Vector getRevisionLevel ( Context context, String[] args )
          throws Exception
   {
     /*
      *  Author    : MT
      *  Date      : 04/07/2003
      *  Notes     :
      *  History   :
      */


      MxDebug.enter ();

      // unpack and get parameter
      HashMap programmap = (HashMap)JPO.unpackArgs(args);
      MapList busTypeList = (MapList)programmap.get("objectList");

      Vector busTypes = new Vector(busTypeList.size());

       if (busTypeList != null)
       {
          StringList listSelect = new StringList(1);
          listSelect.addElement(DomainObject.SELECT_TYPE);
          listSelect.addElement(DomainObject.SELECT_ID);
          listSelect.addElement(DomainObject.SELECT_REVISION);

          String objIdArray[] = new String[busTypeList.size()];

          for (int i = 0; i < busTypeList.size(); i++)
          {
              try
              {
                  objIdArray[i] = (String)((HashMap)busTypeList.get(i)).get("id");
              } catch (Exception ex)
              {
                  objIdArray[i] = (String)((Hashtable)busTypeList.get(i)).get("id");
              }
          }

          MapList resultList = null;
          resultList =DomainObject.getInfo(context,objIdArray, listSelect);

          Iterator iterator = resultList.iterator();
          HashMap aMap;

          while (iterator.hasNext())
          {
             aMap=(HashMap)iterator.next();
             String objId  = (String)aMap.get(DomainObject.SELECT_ID);
             String typeName  = (String)aMap.get(DomainObject.SELECT_TYPE);

             if(typeName != null && !typeName.equals("null") && !typeName.equals(""))
             {
               // initialize to be blank, will be returned for type_ProjectVault
               // objects.
               String revLevel = "";

               // if the type isn't a type_ProjectVault, set gather revision level.
               if ( ! typeName.equals (
				       PropertyUtil.getSchemaProperty (context, "type_ProjectVault" ) ) )
               {
                 revLevel  = (String)aMap.get(DomainObject.SELECT_REVISION);
               }

               MxDebug.message (MxDebug.DL_5,
                                "Object details: "
                                + "\n\tobjId:     " + objId
                                + "\n\ttype:      " + typeName
                                + "\n\trevLevel:  " + revLevel);


               // set a revision level for the object.
               busTypes.add(revLevel);
           }
          }
      }

      MxDebug.exit ();
      return busTypes;
   }

}
