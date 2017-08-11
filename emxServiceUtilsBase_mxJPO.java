/*
 ** ${CLASSNAME}
 **
 ** Copyright (c) 1993-2016 Dassault Systemes. All Rights Reserved.
 ** This program contains proprietary and trade secret information of
 ** Dassault Systemes.
 ** Copyright notice is precautionary only and does not evidence any actual
 ** or intended publication of such program
 */

import matrix.db.*;
import java.util.Vector;
import java.util.StringTokenizer;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;

/**
 * The <code>emxServiceUtilsBase</code> class contains implementation of emxServiceUtils.
 *
 * @version AEF 10-0-1-0 - Copyright (c) 2002, MatrixOne, Inc.
 */

public class emxServiceUtilsBase_mxJPO {

  /**
    * Constructor.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds input arguments.
    * @throws Exception if the operation fails.
    * @since AEF 10-0-1-0.
    */
    public emxServiceUtilsBase_mxJPO(Context context, String[] args) throws Exception
    {
		// Constructor
    }

    /**
       * This method checks revisions.
       * @param context the eMatrix <code>Context</code> object
       * @param args holds following input arguments
       *     0 - id - id
       *     1 - reqPolicyState - reqPolicyState
       *     2 - searchCriteria - searchCriteria
       * @return int
       * @throws Exception if the operation fails
       * @since AEF 10-0-1-0.
       */

    public int checkRevisions(Context context, String[] args) throws Exception
    {
        String id = args[0];
        String reqPolicyState = args[1];
        String searchCriteria = args[2];

        MQLCommand cmd = new MQLCommand();

        if (searchCriteria.equalsIgnoreCase("LATER_REVISIONS"))
            cmd.executeCommand(context, "print bus $1 select $2 $3 $4 dump $5",id,"policy","id","current","|");
        else
            cmd.executeCommand(context, "print bus $1 select $2 $3 $4 dump $5",id,"policy","revisions.id","revisions.current","|");

        StringTokenizer tokens = new StringTokenizer(cmd.getResult(), "|\n");
        String policyName = tokens.nextToken();
        int j, count = (tokens.countTokens()) / 2;

        // find index of object id in revision chain
        int rev = 0;
        for (j=0; j<count; j++)  {
            if (id.equals(tokens.nextToken()))
                rev = j;
        }

       // save list of states for all objects following this in revision chain
       Vector revisionStates = new Vector(count - rev);
       for (j=0; j<count; j++) {
           String state = tokens.nextToken();
           if (j >= rev)
               revisionStates.add(FrameworkUtil.reverseLookupStateName(context, policyName, state));
       }

    Vector stateNames = getPolicyStates(context, policyName);
    int iRequestedStateIndex = stateNames.indexOf(reqPolicyState);

    //
    // set flags indicating if this part is itself in the requested state, and whether
    // there are higher revisions of the same state of this part.
    //
    boolean requestedStateFlag = false;
    int iCurrentStateIndex = stateNames.indexOf(revisionStates.elementAt(0));
    if (iCurrentStateIndex == iRequestedStateIndex)
      requestedStateFlag = true;

    //
    // Check the BOs with revisions greater than the current revision of this BO.
    // find out if the next revision has the same current state.
    //
    boolean higherRequestedStateFlag = false;
    for (j=1; j<revisionStates.size(); j++) {

        // check if this BO's current state is greater than requested state
        int iNextBOCurState = stateNames.indexOf(revisionStates.elementAt(j));
        if (iNextBOCurState >= iRequestedStateIndex) {
          higherRequestedStateFlag = true;
          break;
      }
    }

    //
    // Now decide whether to add the current Bus Obj
    // to the BusObjList based on the request by the user
    //
    String status = "true";
    if (searchCriteria.equalsIgnoreCase("HIGHEST_REVISION")) {
      if (!requestedStateFlag || higherRequestedStateFlag)
        status = "false";
    }
    else if (searchCriteria.equalsIgnoreCase("HIGHEST_AND_PRESTATE_REVS")) {
      if ((!requestedStateFlag || higherRequestedStateFlag) && (iCurrentStateIndex >= iRequestedStateIndex))
        status = "false";
    }
    else if (searchCriteria.equalsIgnoreCase("LATER_REVISIONS")) {
      if (iCurrentStateIndex <= iRequestedStateIndex)
        status = "false";
    }

    cmd.executeCommand(context, "set env global $1 $2","emxServiceUtils",status);
    if ("true".equals(status))
      return 0;
    else
      return 1;
  }


     /**
       * This method returns the build status.
       * @param context the eMatrix <code>Context</code> object
       * @param args holds following input arguments
       *     0 - id - id
       *     1 - type - type
       *     2 - which - which
       *     3 - sECRType - sECRType
       *     4 - sECOType - sECOType
       *     5 - sPartType - sPartType
       *     6 - sDrwPrintType - sDrwPrintType
       *     7 - sPdtLineType - sPdtLineType
       *     8 - sSketchType - sSketchType
       *     9 - sMarkupType - sMarkupType
       *     10 - sCADMdlType - sCADMdlType
       *     11 - sEBOMMkpType - sEBOMMkpType
       *     12 - sECRMnPdtAfftdRelName - sECRMnPdtAfftdRelName
       *     13 - sECRSuppDocRelName - sECRSuppDocRelName
       *     14 - sNewRevSpecRelName - sNewRevSpecRelName
       *     15 - sRequestSpecRevRelName - sRequestSpecRevRelName
       *     16 - sMakeObsRelName - sMakeObsRelName
       *     17 - sNewRevPartRelName - sNewRevPartRelName
       *     18 - sRequestPartObsRelName - sRequestPartObsRelName
       *     19 - sRequestPartRevRelName - sRequestPartRevRelName
       *     20 - sECOChgRequestRelName - sECOChgRequestRelName
       * @return int
       * @throws Exception if the operation fails
       * @since AEF 10-0-1-0.
       */

  public int getBuildStatus(Context context, String[] args) throws Exception {

    //Debug mode settings
    boolean boolECOObsRelFound = false;
    boolean boolECRObsRelFound = false;

    //Whether the object's next revision connected to any ECO
    boolean boolNextRevConnected = false;


    MQLCommand  cmd = new MQLCommand();
    String id = args[0];
    String type = args[1];
    String which = args[2];
	
    //Get absolute name for types
    String sECRType = args[3];
    String sECOType = args[4];
    String sPartType = args[5];
    String sDrwPrintType = args[6];
    String sPdtLineType = args[7];
    String sSketchType = args[8];
    String sMarkupType = args[9];
    String sCADMdlType = args[10];
    String sCADDrwType = args[11];
    String sEBOMMkpType = args[12];

    //Get absolute name for the relationship
    String sECRMnPdtAfftdRelName = args[13];
    String sECRSuppDocRelName = args[14];
    String sNewRevSpecRelName = args[15];
    String sRequestSpecRevRelName = args[16];
    String sMakeObsRelName = args[17];
    String sNewRevPartRelName = args[18];
    String sRequestPartObsRelName = args[19];
    String sRequestPartRevRelName = args[20];
    String sECOChgRequestRelName = args[21];

    String sTypePattern = sECRType + "," + sECOType;
    String sRelPattern = null;
    String sConnectStatus = null;
    String sResult = null;
	String sBusObjType=null;

    String checkType = null;
    if ("ECR".equals(which))
      checkType = sECRType;
    else
      checkType = sECOType;

//   Added code for the bug 299849 :Begin   
 
	String sDocuments=PropertyUtil.getSchemaProperty(context,"type_DOCUMENTS");
	String sECADMdlType=PropertyUtil.getSchemaProperty(context,"type_ECADModel");
	String sMCADMdlType=PropertyUtil.getSchemaProperty(context,"type_MCADModel");
	String sMCADAssemblyType=PropertyUtil.getSchemaProperty(context,"type_MCADAssembly");
	String sMCADAssemblyInstanceType=PropertyUtil.getSchemaProperty(context,"type_MCADAssemblyInstance");
	String sMCADComponentType=PropertyUtil.getSchemaProperty(context,"type_MCADComponent");
	String sMCADComponentInstanceType=PropertyUtil.getSchemaProperty(context,"type_MCADComponentInstance");
	String sMCADParameterizedModelType=PropertyUtil.getSchemaProperty(context,"type_MCADParameterizedModel");
	String sMCADParameterizedAssemblyModelType=PropertyUtil.getSchemaProperty(context,"type_MCADParameterizedAssemblyModel");
	String sMCADParameterizedComponentModelType=PropertyUtil.getSchemaProperty(context,"type_MCADParameterizedComponentModel");
		 
	//Check whether the start business object is a CAD Model or other type
    if(type.equals(sPartType)||type.equals(sECADMdlType)||type.equals(sMCADMdlType)||type.equals(sMCADAssemblyType)||type.equals(sMCADAssemblyInstanceType)||type.equals(sMCADComponentType)||type.equals(sMCADComponentInstanceType)||type.equals(sMCADParameterizedModelType)||type.equals(sMCADParameterizedAssemblyModelType)||type.equals(sMCADParameterizedComponentModelType))
	 {
	    sBusObjType = getBaseType(context, id, type);
	 }
	 else
	 {
        sBusObjType = type;
	 }

//   Added code for the bug 299849 : End    
   
     
    sRelPattern = "*";
    if (sBusObjType.equals(sPartType))
    {
      sRelPattern = sRequestPartObsRelName + "," + sRequestPartRevRelName + "," +
                    sMakeObsRelName + "," + sNewRevPartRelName + "," +
                    sECOChgRequestRelName;
    }

    if (sBusObjType.equals(sDrwPrintType) || sBusObjType.equals(sCADMdlType) || sBusObjType.equals(sCADDrwType))
    {
      sRelPattern = sNewRevSpecRelName + "," + sRequestSpecRevRelName + "," +
                    sECOChgRequestRelName;

    }

    if (sBusObjType.equals(sPdtLineType))
    {
      sRelPattern = sECRMnPdtAfftdRelName;
    }

    if (sBusObjType.equals(sSketchType) || sBusObjType.equals(sEBOMMkpType) || sBusObjType.equals(sMarkupType))
    {
      sRelPattern = sECRSuppDocRelName;
    }

    cmd.executeCommand(context, "expand bus $1 terse to rel $2 type $3 select bus name current dump $4",id,sRelPattern,sTypePattern,"|");

    StringTokenizer tokens = new StringTokenizer(cmd.getResult(), "|\n");

    Vector relNames = new Vector();
    Vector busIds = new Vector();
    Vector busNames = new Vector();

    Vector currentStates = new Vector();

    int count = tokens.countTokens() / 6;

    for (int i=0; i<count; i++) {
      tokens.nextToken();
      relNames.add(tokens.nextToken());
      tokens.nextToken();
      busIds.add(tokens.nextToken());
      busNames.add(tokens.nextToken());
      currentStates.add(tokens.nextToken());
    }
 
    //Find whether there is an ECO connected to the next revision of the object
    if(checkType.equals(sECOType)) {

        cmd.executeCommand(context, "print bus $1 select $2 dump",id,"next.id");
      String sPartNextRevId = cmd.getResult();

      if(!sPartNextRevId.trim().equals("")) {		  
        sRelPattern =  sMakeObsRelName + "," + sNewRevPartRelName + "," + sNewRevSpecRelName;
        sTypePattern = DomainConstants.TYPE_ECO;
	cmd.executeCommand(context, "expand bus $1 terse to rel $2 type $3 select bus name dump $4",sPartNextRevId,sRelPattern,sTypePattern,"|");

        String sNextRevConnected = cmd.getResult();

        if(!"".equals(sNextRevConnected)) {
          boolNextRevConnected = true;
          tokens = new StringTokenizer(sNextRevConnected, "|\n");
          tokens.nextToken();
          tokens.nextToken();
          tokens.nextToken();
          sConnectStatus = "Connected" +"\t" + tokens.nextToken()  + "\t" + tokens.nextToken() + "\t NEXTREVISION";
        }
      }
    }

    if(checkType.equals(sECRType) || (checkType.equals(sECOType) && !boolNextRevConnected)) {

      boolECOObsRelFound = false;
//      boolECORevRelFound = false;
      boolECRObsRelFound = false;
//      boolECRRevRelFound = false;
      int index = -1;
      int sIndex = -1;

      if (sBusObjType.equals(sPartType))
      {
        //Find ECR connected to this part
        if (checkType.equals(sECRType) && (index = relNames.indexOf(sRequestPartObsRelName)) >= 0)
        {
          boolECRObsRelFound = true;
          sConnectStatus = "Requested" + ": " + busNames.elementAt(index);
         sIndex = index;
        }

        if (checkType.equals(sECRType) && (index = relNames.indexOf(sRequestPartRevRelName)) >= 0)
        {
//          boolECRRevRelFound = true;
          sIndex = index;
          if (boolECRObsRelFound)
          {
            sConnectStatus = "Conflict" + ": " + busNames.elementAt(index);
          }
          else
          {
            sConnectStatus = "Requested" + ": " + busNames.elementAt(index);
          }
        }

        //Find ECO connected to this part
        if (checkType.equals(sECOType) && (index = relNames.indexOf(sMakeObsRelName)) >= 0)
        {
          sIndex = index;
          boolECOObsRelFound = true;

          cmd.executeCommand(context, "print bus $1 select $2 dump $3",(String)busIds.elementAt(sIndex),"state","|");
          sResult = cmd.getResult().trim();
          sResult = sResult.substring(sResult.lastIndexOf('|')+1,sResult.length());
          if (( (String)currentStates.elementAt(sIndex)).equals(sResult) )
          {
            sConnectStatus = "Available\t \t";
          }
          else
          {
            sConnectStatus = "Connected";
          }

        }

        if (checkType.equals(sECOType) && (index = relNames.indexOf(sNewRevPartRelName)) >= 0)
        {
//          boolECORevRelFound = true;
          sIndex = index;
          if (boolECOObsRelFound)
          {
            sConnectStatus = "Conflict";
          }
          else
          {
              cmd.executeCommand(context, "print bus $1 select $2 dump $3",(String)busIds.elementAt(sIndex),"state","|");
            sResult = cmd.getResult().trim();
            sResult = sResult.substring(sResult.lastIndexOf('|')+1,sResult.length());
            if (( (String)currentStates.elementAt(sIndex)).equals(sResult) )
            {
              sConnectStatus = "Available\t \t";
            }
            else
            {
              sConnectStatus = "Connected";
            }

           }//end of else
          }//end of check for ECO Type
      }//end of Check for Part Type
 

 // Added code for the bug 299849 : Begin
 
      if (sBusObjType.equals(sDrwPrintType) || sBusObjType.equals(sCADMdlType) || sBusObjType.equals(sCADDrwType) ||sBusObjType.equals(sDocuments))
 // Added code for the bug 299849 : End
      {
        //Find ECR connected
        if (checkType.equals(sECRType) && (index = relNames.indexOf(sRequestSpecRevRelName)) >= 0)
        {
          sIndex = index;
          cmd.executeCommand(context, "print bus $1 select state dump $2",
                             (String)busIds.elementAt(sIndex),"|");
          sResult = cmd.getResult().trim();
          sResult = sResult.substring(sResult.lastIndexOf('|')+1,sResult.length());
          if (( (String)currentStates.elementAt(sIndex)).equals(sResult) )
          {
            sConnectStatus = "Available\t \t";
          }
          else
          {
          sConnectStatus = "Requested" + ": " + busNames.elementAt(index);
          }

        } 

        //Find ECO connected
        if (checkType.equals(sECOType) && (index = relNames.indexOf(sNewRevSpecRelName)) >= 0)
        {
          sIndex = index;
          cmd.executeCommand(context, "print bus $1 select $2 dump $3;",
                             (String)busIds.elementAt(sIndex),"state","|");
          sResult = cmd.getResult().trim();
          sResult = sResult.substring(sResult.lastIndexOf('|')+1,sResult.length());
		  
          if (( (String)currentStates.elementAt(sIndex)).equals(sResult) )
          {
            sConnectStatus = "Available\t \t";
          }
          else
          {
             boolECOObsRelFound = true;
             sConnectStatus = "Connected" + ": " + busNames.elementAt(index);
          }

        } 
      } 

      if (sBusObjType.equals(sSketchType) || sBusObjType.equals(sMarkupType) || sBusObjType.equals(sEBOMMkpType))
      {
        //Find ECR connected
        if (checkType.equals(sECRType) && (index = relNames.indexOf(sECRSuppDocRelName)) >= 0)
        {
          sIndex = index;
          sConnectStatus = "Requested" + ": " + busNames.elementAt(index);
        }
      }

      if (sBusObjType.equals(sPdtLineType))
      {
        //Find ECR connected
        if (checkType.equals(sECRType) && (index = relNames.indexOf(sECRMnPdtAfftdRelName)) >= 0)
        {
          sIndex = index;
          sConnectStatus = "Requested" + ": " + busNames.elementAt(index);
        }
      }


      if (sConnectStatus == null)
      {
        sConnectStatus = "Available\t \t";
      } else {
        sConnectStatus += "\t" + (String)busIds.elementAt(sIndex)  + "\t" + (String)busNames.elementAt(sIndex);
      }

    }

    cmd.executeCommand(context, "set env global $1 $2","emxServiceUtils", sConnectStatus);
    return 0;
  }


  /**
    * This method will get Base type of given type.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param id String representing objectId.
    * @param typeName String representing typeName.
    * @return String representing Base Type.
    * @throws Exception if the operation fails.
    * @since AEF 10-0-1-0.
    */
  static protected String getBaseType(Context context, String id, String typeName) throws Exception {
	  return FrameworkUtil.getBaseType(context, typeName, null);
  }

  /**
    * This method will get states associated with a policy.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param policy String representing policy.
    * @return Vector representing states associated with a Policy.
    * @throws Exception if the operation fails.
    * @since AEF 10-0-1-0.
    */
  static protected Vector getPolicyStates(Context context, String policy) throws Exception {
	  Vector states = new Vector();
    
      MQLCommand cmd = new MQLCommand();
      cmd.executeCommand(context, "print policy $1 select state dump $2", policy,"|");
      StringTokenizer tokens = new StringTokenizer(cmd.getResult(), "|\n");      
      
      while (tokens.hasMoreTokens()) {
        String name = tokens.nextToken();
		name = FrameworkUtil.reverseLookupStateName(context, policy, name);
        states.add(name);
      }
      
      return states;
  }

}
