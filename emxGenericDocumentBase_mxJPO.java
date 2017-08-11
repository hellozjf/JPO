/*
   **   emxGenericDocumentBase
   *
   *   Copyright (c) 1992-2016 Dassault Systemes.
   *   All Rights Reserved.
   *   This program contains proprietary and trade secret information of
   *   MatrixOne, Inc.  Copyright notice is precautionary only
   *   and does not evidence any actual or intended publication of such program
   */

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.MatrixException;
import matrix.util.StringList;

import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.document.DocumentCentralConstants;
import com.matrixone.apps.document.GenericDocument;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.library.LibraryCentralConstants;


  /**
   * The <code>emxGenericDocumentBase</code> represents implementation of
   * anything on the "To Side" of "Has Documents" Relationship in DC Schema
   *
   * @exclude
   */

    public class emxGenericDocumentBase_mxJPO extends emxCommonDocument_mxJPO
       implements DocumentCentralConstants
    {

      protected static final String THIS_FILE = "emxGenericDocumentBase";

      //~ Constructors ---------------------------------------------------------

      /**
       * Creates ${CLASSNAME} Object.
       *
       * @param context the eMatrix <code>Context</code> object
       * @param args holds no arguments
       * @throws Exception if the operation fails
       */
      public emxGenericDocumentBase_mxJPO (Context context, String[] args) throws Exception
      {
          super(context,args);

          if ((args != null) && (args.length > 0))
          {
              setId (args[0]);
          }
      }

      /**
       * Creates a new emxGenericDocumentBase object given the ObjectId.
       *
       * @param id the Java <code>String</code> object
       * @throws Exception if the operation fails
       *//*

      public ${CLASSNAME} (String id) throws Exception
      {
          // Call the super constructor

          super(id);
      }*/

      /**
       * This method is executed if a specific method is not specified.
       *
       * @param context the eMatrix <code>Context</code> object
       * @param args holds no arguments
       * @return a Java <code>int</code>
       * @throws Exception if the operation fails
       * @exclude
       */

      public int mxMain (Context context, String[] args) throws Exception
      {
          if (true)
          {
              throw new Exception(
                  "Must specify method on emxGenericDocumentBase invocation"
              );
          }

          return 0;
      }
      
    public CommonDocument createAndConnect (Context context,String[] args) throws Exception {
        HashMap paramMap = JPO.unpackArgs(args);
        
        String type         = (String)paramMap.get("type");
        String name         = (String)paramMap.get("name");
        String revision     = (String)paramMap.get("revision");
        String description  = (String)paramMap.get("description");
        String policy       = (String)paramMap.get("policy");
        String vault        = (String)paramMap.get("vault");
        String approverName = (String)paramMap.get("approverName");
        Map map             = (Map)paramMap.get("map");
        String relationshipName = (String)paramMap.get("relationshipName");
        String parentId     = (String)paramMap.get("parentId");
        
        return createAndConnect(context, type, name, revision, description, policy, vault, approverName, map, relationshipName, parentId);
    }

      /**
       * This Method Creates Generic Document with a valid Approver Attribute
       * @param context
       * @param type - for creating subtype of GD, assumed as GD if null. If not null, it must be a subtype of GD
       * @param name - Name of the Generic Document
       * @param revision - revision of the Generic Document , default revision is applied if null
       * @param description - description of the Generic Document , no description is set if null
       * @param policy - Policy for the Generic Document, default policy is applied if null
       * @param vault - Vault for the Generic Document, default vault is applied if null
       * @param approverName - Approver Name for the Generic Document, User Name of a Person with Reviewer role
       * @param map - a HashMap containing values for other Attributes
       * @param relationshipName - name of the relationship to use to connect, if null, Classified Item will be used
       * @param parentId - parentID to connect, if relationshipName is Classified Item, this object must be of type Classification, If both relationshipName and parentId are null then Created Generic Document is not connected to any objects
       * @return void
       * @throws Exception in Failure
       */
      public CommonDocument createAndConnect (Context context,String type, String name,String revision,
                                               String description,String policy,String vault,
                                               String approverName, Map map,String relationshipName,
                                               String parentId) throws Exception {

          GenericDocument genericDocument = new GenericDocument();
          genericDocument.createAndConnect(context, type,  name, revision,
                                           description, policy, vault,
                                           approverName,  map, relationshipName,
                                           parentId);
          return genericDocument;
      }


      /**
       * This Method Returns All Approvers in the system,(An Approver is the Person having Reviewer Role)
       * @param context
       * @return StringList of Person Names who has Reviewer Role
       * @throws Exception
       */

      public StringList getApprovers(Context context, String[] args) throws Exception{
          return GenericDocument.getApprovers(context);
      }


    /**
      *  The method checks if all DocumentSheet children
      *  of this object are beyond WIP state when object is promoted to Released state.
      *
      *  @param context the eMatrix <code>Context</code> object
      *  @param args an array of String arguments for this method
      *         0 - objectId, the id of this Generic Document
      *  @return either 0 or exception
      *  @throws Exception if the operation fails
      */
     public int checkAllDocumentSheetState(Context context, String[] args)
         throws Exception
     {

        String currentLocation = "emxGenericDocuemntBase.checkAllDocumentSheetState";


       try
       {
          String objectId = args[0];

          DomainObject doc = DomainObject.newInstance(context, PropertyUtil.getSchemaProperty (context,"type_GenericDocument"));
          doc.setId(objectId);

          String sDSPolicyName = PropertyUtil.getSchemaProperty(context,"policy_DocumentSheet");
          String dsWipStateName = PropertyUtil.getSchemaProperty(context,"policy",sDSPolicyName,"state_WIP");

          String busWhere = "current==\"" + dsWipStateName + "\"";


          MapList wipDocSheetList = doc.getRelatedObjects(context,
                                                  LibraryCentralConstants.RELATIONSHIP_DOCUMENT_SHEETS,
                                                  "*",
                                                  null,
                                                  null,
                                                  false,  //get the "to"
                                                  true, //get the "from"
                                                  (short)1, //recurse level
                                                  busWhere, //object where
                                                  ""  //relationship where
                                                  );



          // do not promote if at least 1 WIP state Document Sheet present
          if(wipDocSheetList.size() > 0 )
          {
             String errorString = EnoviaResourceBundle.getProperty(context,"emxDocumentCentralStringResource",new Locale(context.getSession().getLanguage()),"emxDocumentCentral.ErrorMsg.AllDocumentSheetChildrenNotActive");
             throw (new MatrixException( errorString ) );
           }
           else
           {
             return 0;
           }

       }
       catch (Exception e)
       {
          throw new MatrixException (e.toString());
       }



     }



}
