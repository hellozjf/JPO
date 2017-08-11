/*
**  emxDocumentBase
**
**  Copyright (c) 1992-2016 Dassault Systemes.
**  All Rights Reserved.
**  This program contains proprietary and trade secret information of MatrixOne,
**  Inc.  Copyright notice is precautionary only
**  and does not evidence any actual or intended publication of such program
**
*/

import matrix.db.*;
import matrix.util.*;
import java.util.*;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.UIUtil;

/**
 * The <code>emxDocumentBase</code> class contains methods for document.
 *
 * @version AEF 9.5.1.1 - Copyright (c) 2003, MatrixOne, Inc.
 */

public class emxDocumentBase_mxJPO extends emxDomainObject_mxJPO
{

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *    0 - String that holds the document object id.
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.1
     */

    public emxDocumentBase_mxJPO(Context context, String[] args)
      throws Exception
    {
        super(context, args);
    }

    /**
     * This method float the new document revision to 'Define' and 'In Process' routes.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.1
     */

    public void floatRouteDocument(matrix.db.Context context, String[] args)
      throws Exception
    {

      StringList objectSelects = new StringList(1);
      objectSelects.add(SELECT_ID);

      StringList relSelects = new StringList(1);
      relSelects.add(SELECT_RELATIONSHIP_ID);

      StringBuffer objectWhere = new StringBuffer(75);
      objectWhere.append("(current == '");
      objectWhere.append(STATE_ROUTE_DEFINE);
      objectWhere.append("') || (current == '");
      objectWhere.append(STATE_ROUTE_IN_PROCESS);
      objectWhere.append("')");

      MapList routeList  =  getRelatedObjects(context,
                                                RELATIONSHIP_OBJECT_ROUTE,
                                                TYPE_ROUTE,
                                                objectSelects,
                                                relSelects,
                                                false,
                                                true,
                                                (short)1,
                                                objectWhere.toString(),
                                                null,
                                                0);

      if(routeList.size() != 0 )
      {
          Iterator routeItr = routeList.iterator();
          DomainObject lastRev = new DomainObject(getLastRevision(context));

          ContextUtil.pushContext(context,null,null,null);
          try
          {
            while(routeItr.hasNext())
            {
              Map map = (Map) routeItr.next();
              DomainRelationship.disconnect(context, (String)map.get(SELECT_RELATIONSHIP_ID));
               DomainRelationship.connect(context,
                                          lastRev,
                                          RELATIONSHIP_OBJECT_ROUTE,
                                          (new DomainObject((String)map.get(SELECT_ID))) );
            }
          }
          catch (Exception ex)
        {
            throw ex;
          }
        finally
          {
              ContextUtil.popContext(context);
          }
      }

    }
    
    /**
     * Filter program method for Employee role of "Document" Policy
     * @param context the eMatrix <code>Context</code> object
     * @return true if CLC is installed and  Document type is "ClearCase Document" 
     * @throws Exception
     */
    public boolean isCLCDocument(Context context, String args[]) throws Exception {
        boolean isCLCInstalled = FrameworkUtil.isSuiteRegistered(context,"appVersionX-BOMforRationalClearCase",false,null,null);
        if (isCLCInstalled){
              if(this.isKindOf(context, PropertyUtil.getSchemaProperty(context, "type_ClearCaseDocument"))) {
                    return true;
              }  
        }
        return false;
      }
	  public String getThumbnailExtention(Context context, String args[]) throws Exception {
    	String fileExtension ="";
		String filename = args[0];
    	if(UIUtil.isNotNullAndNotEmpty(filename )){
    		fileExtension = filename.substring(filename.lastIndexOf(".")+1);
    	}
    	return fileExtension;  
    }

}
