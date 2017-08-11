/*   CPXPart.
**
**  (c) Dassault Systemes, 1993 - 2016.  All rights reserved.
**
**   This program contains proprietary and trade secret information of MatrixOne,
**   Inc.  Copyright notice is precautionary only
**   and does not evidence any actual or intended publication of such program.
**
**
*/

import matrix.db.*;
import matrix.util.*;

import java.util.*;

import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.common.Company;
import com.matrixone.apps.common.Person;
import com.matrixone.apps.componentcentral.CPCConstants;
import com.matrixone.apps.componentcentral.CPCPart;
import com.matrixone.json.*;

import matrix.db.*;

public class CPXPartBase_mxJPO extends DomainObject {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new PartBase JPO object.
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds no arguments
     * @throws Exception
     *             if the operation fails
     */

    public CPXPartBase_mxJPO(Context context, String[] args) throws Exception {
        super();
    }


   public Boolean isComponentAuthor(Context context, String[] args)
             throws Exception
      {
      	Person person = Person.getPerson(context);
      	// Promoted as part of fix for IR-074154 - Component Author role changed to Component User
      	boolean flag = person.hasRole(context, CPCConstants.ROLE_COMPONENTUSER);
      	Boolean hasAccess = new Boolean(flag);
      	return hasAccess;

      }

      public Boolean isAnyComponentUser(Context context, String[] args)
                throws Exception
         {
         	Vector roles = PersonUtil.getUserRoles(context);
         	boolean flag = false;
         	for(int i=0;i<roles.size();i++){
         		String role = (String)roles.get(i);
         		if(role.equals(CPCConstants.ROLE_COMPONENTAUTHOR) ||
         		   role.equals(CPCConstants.ROLE_COMPONENTUSER)	||
         		   role.equals(CPCConstants.ROLE_COMPONENTQUALENGINEER)){
         		   flag = true;
         		}

         	}
         	Boolean hasAccess = new Boolean(flag);
         	return hasAccess;

      }


   public StringList getMEPart(Context context, String[] args)
       throws Exception
   {

       HashMap paramMap = (HashMap) JPO.unpackArgs(args);
       StringList objlist = new StringList();

       try {
            objlist =  getEPInfoHTMLOutput (context, args, "mepname");
       }
       catch(Exception e){
             throw e;
      }
       return objlist;
   }


   public StringList getManufacturer (Context context, String[] args)
       throws Exception
   {
         StringList objlist = new StringList();
        try {
            objlist =  getCompanyInfoHTMLOutput (context, args, "manufacturer");
        }
        catch(Exception e){
             throw e;
        }
       return objlist;
   }

   public StringList getMEPQualification(Context context, String[] args)
          throws Exception
   {
           StringList qualificationList = new StringList();
           try {
           	HashMap paramMap = (HashMap) JPO.unpackArgs(args);
		Map paramList = (HashMap) paramMap.get("paramList");
		String useBOMQualification = (String) paramList.get("useBOMQualification");
		qualificationList =  getQualInfoHTMLOutput (context, args, "mepqualification", "preference", useBOMQualification);
           }
           catch(Exception e){
                throw e;
           }
          return qualificationList;
   }

   public StringList getMEPPreferenceScore(Context context, String[] args)
          throws Exception
   {
           StringList qualificationList = new StringList();
           try {
           	HashMap paramMap = (HashMap) JPO.unpackArgs(args);
		Map paramList = (HashMap) paramMap.get("paramList");
		String useBOMQualification = (String) paramList.get("useBOMQualification");
		qualificationList =  getQualInfoHTMLOutput (context, args, "mepqualification", "preferenceScore", useBOMQualification);
           }
           catch(Exception e){
                throw e;
           }
          return qualificationList;
   }


   public StringList getMEPQualStatus(Context context, String[] args)
             throws Exception
   {
	  StringList qualificationList = new StringList();
	  try {
		HashMap paramMap = (HashMap) JPO.unpackArgs(args);
		Map paramList = (HashMap) paramMap.get("paramList");
		String useBOMQualification = (String) paramList.get("useBOMQualification");
		qualificationList =  getQualInfoHTMLOutput (context, args, "mepqualification", "qualificationStatus", useBOMQualification);
	  }
	  catch(Exception e){
		   throw e;
	  }
	 return qualificationList;
   }

   public StringList getSEPart (Context context, String[] args)
          throws Exception
      {

          StringList objlist = new StringList();
          try {
               objlist =  getEPInfoHTMLOutput (context, args, "sepname");
          }
          catch(Exception e){
                throw e;
         }
          return objlist;
   }

     public StringList getSupplier (Context context, String[] args)
          throws Exception
      {
           StringList objlist = new StringList();
           try {
               objlist =  getCompanyInfoHTMLOutput (context, args, "supplier");
           }
           catch(Exception e){
                throw e;
           }
          return objlist;
      }

      public StringList getSEPQualification(Context context, String[] args)
             throws Exception
         {
              StringList qualificationList = new StringList();
              try {
			HashMap paramMap = (HashMap) JPO.unpackArgs(args);
			Map paramList = (HashMap) paramMap.get("paramList");
			String useBOMQualification = (String) paramList.get("useBOMQualification");
			qualificationList =  getQualInfoHTMLOutput (context, args, "sepqualification", "preference", useBOMQualification);
              }
              catch(Exception e){
                   throw e;
              }
             return qualificationList;
      }

      public StringList getSEPPreferenceScore(Context context, String[] args)
             throws Exception
         {
              StringList qualificationList = new StringList();
              try {
              		HashMap paramMap = (HashMap) JPO.unpackArgs(args);
			Map paramList = (HashMap) paramMap.get("paramList");
			String useBOMQualification = (String) paramList.get("useBOMQualification");
			qualificationList = getQualInfoHTMLOutput (context, args, "sepqualification","preferenceScore", useBOMQualification);
              }
              catch(Exception e){
                   throw e;
              }
             return qualificationList;
      }

      public StringList getSEPQualStatus(Context context, String[] args)
	               throws Exception
     {
		  StringList qualificationList = new StringList();
		  try {
			HashMap paramMap = (HashMap) JPO.unpackArgs(args);
			Map paramList = (HashMap) paramMap.get("paramList");
			String useBOMQualification = (String) paramList.get("useBOMQualification");
			qualificationList = getQualInfoHTMLOutput (context, args, "sepqualification", "qualificationStatus", useBOMQualification);
		  }
		  catch(Exception e){
			   throw e;
		  }
		 return qualificationList;
   }



     public StringList getEPInfoHTMLOutput (Context context,String[] args, String id)
	             throws Exception
	 {

	    DebugUtil.debug("************ Enter getEPInfoHTMLOutput " + id);
	    HashMap paramMap = (HashMap)JPO.unpackArgs(args);

	    Map paramList = (HashMap) paramMap.get("paramList");

		String suiteDir = (String) paramList.get("SuiteDirectory");
		String suiteKey = (String) paramList.get("suiteKey");
		String publicPortal = (String) paramList.get("publicPortal");
		String fromCompare = (String) paramList.get("fromCompare");
  	   	//String linkFile = (publicPortal != null && publicPortal.equalsIgnoreCase("true")) ? "emxNavigator.jsp" : "emxTree.jsp";


	    MapList objectList = (MapList) paramMap.get("objectList");
	    //DebugUtil.debug("************ objectList  " + objectList);

	    StringList result = new StringList();

		if (objectList != null && objectList.size() > 0) {
			// construct array of ids
			int objectListSize = objectList.size();

			for (int i = 0; i < objectListSize; i++) {

				  Map dataMap = (Map) objectList.get(i);
				  String objectId = (String) dataMap.get("id");
				 // DebugUtil.debug("************ objectId  " + objectId);
				 try
				 {
					MapList listCorpEPs = new MapList();
					if(id.equals("mepname"))
						listCorpEPs = getRelatedMEPs(context, paramMap, objectId);
					else
						listCorpEPs = getRelatedSEPs(context, paramMap, objectId);


					StringBuffer output = new StringBuffer(" ");
					//Iterating to Corporate Context MEP list to load MEP Ids
					for(int j=0;j<listCorpEPs.size();j++) {
						Map tempMap = (Map)listCorpEPs.get(j);

						String objname = (String)tempMap.get(SELECT_NAME);
						String objid = (String)tempMap.get(SELECT_ID);


						if(fromCompare != null && fromCompare.equals("true")){
							output.append(objname);
							output.append(" <br/> <br/>");
						}
						else{


                               				output.append("<a href='javascript:showNonModalDialog(\""+"../common/emxTree.jsp?objectId="+objid+"\",575,575)'>");

							//output.append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/"+ linkFile + "?emxSuiteDirectory=");
							//output.append(suiteDir);
							//output.append("&amp;suiteKey=");
							//output.append(suiteKey);
							//output.append("&amp;objectId=");
							//output.append(objid);
							//output.append("', '', '', 'false', 'popup', '')\">");
							output.append(objname);
							output.append("</a> ");
							output.append(" <br/> <br/>");
						}


					}// end of for (listCorpMEPs)

					if (!"".equals(output.toString())) {
						result.add(output.toString());
					}

				 }
				 catch (FrameworkException Ex)
				 {
					  throw Ex;
				 }

			} // end for
		} // end if

		DebugUtil.debug("************ Exit getEPInfoHTMLOutput ");

		return result;
   }

   public MapList getRelatedMEPs(Context context, HashMap paramMap, String objectId) throws Exception{

	DebugUtil.debug("************ Enter getRelatedMEPs ");
	DomainObject partObj = DomainObject.newInstance(context, objectId);

	StringList selectStmts = new StringList(2);
	selectStmts.addElement(SELECT_ID);

	selectStmts.addElement(SELECT_NAME);



	StringList selectRelStmts = new StringList();
	selectRelStmts.addElement(DomainRelationship.SELECT_ID);

    	Map paramList = (HashMap) paramMap.get("paramList");
    	String location = (String) paramList.get("location");
    	String reportType = (String) paramList.get("reportType");

    	String filters = (String) paramList.get("filters");
    	String where = "";

    	MapList listCorpMEPs = new MapList();

	if(filters != null){
		JSONObject searchJson = new JSONObject(filters);
		Iterator itr   = searchJson.keys();
		int count = 0;

		while(itr.hasNext()) {
			String key = (String)itr.next();
			if(key.equals("MEPName") || key.equals("Manufacturer") || key.equals("MEPPreference") ){
				String relName = "";
				if(key.equals("MEPName"))
					relName = "name";
				else if(key.equals("Manufacturer"))
					relName = "to["+RELATIONSHIP_MANUFACTURING_RESPONSIBILITY+"].from.name";
				else if(key.equals("MEPPreference"))
					relName = "to["+RELATIONSHIP_MANUFACTURER_EQUIVALENT+"].tomid["+ CPCConstants.RELATIONSHIP_QUALIFICATION+"].from."+ CPCConstants.SELECT_ATTRIBUTE_PREFERENCE+".value";


				JSONArray fieldValues =  searchJson.getJSONArray(key);
				String val = "";
				for(int i = 0; i < fieldValues.length(); i++ ) {
					StringTokenizer tokenizer = new StringTokenizer((String) fieldValues.get(i), "|");
					tokenizer.nextToken();
					if(i == (fieldValues.length() -1))
						val += tokenizer.nextToken();
					else
						val += tokenizer.nextToken()+"|";

				}
				if(count == 0){
					where += relName+" ~~ \""+ val + "\"";
					count++;
				}
				else
					where += " && "+ relName +" ~~ \""+ val +"\"";

			}

		}


	}
	else{


		if (location!=null && ("").equals(location) && reportType!=null && reportType.equals("AVL")){
		    // retrieve the Person object
		   com.matrixone.apps.common.Person person = (com.matrixone.apps.common.Person)DomainObject.newInstance(context, DomainConstants.TYPE_PERSON);
		   // retrieve the Host Company attached to the User.
		    location =com.matrixone.apps.common.Person.getPerson(context).getCompany(context).getObjectId(context);
		}

		if (location!=null && reportType!=null && reportType.equals("AVL"))
		{

		    where = "to["+RELATIONSHIP_ALLOCATION_RESPONSIBILITY+"].from.id=="+ location;



		}
	}

		listCorpMEPs = partObj.getRelatedObjects(context,          RELATIONSHIP_MANUFACTURER_EQUIVALENT,              // relationship pattern
									   TYPE_PART,              // object pattern
									   selectStmts,                 // object selects
									   selectRelStmts,              // relationship selects
									   false,                        // to direction
									   true,                       // from direction
									   (short) 1,                   // recursion level
									   where,                        // object where clause
										null);


	DebugUtil.debug("************ Exit getRelatedMEPs ");
	return listCorpMEPs;

}


	public MapList getRelatedSEPs(Context context, HashMap paramMap, String objectId) throws Exception{

		   	DebugUtil.debug("************ Enter  getRelatedSEPs ");

		   	DomainObject partObj = DomainObject.newInstance(context, objectId);

			StringList selectStmts = new StringList(2);
			selectStmts.addElement(SELECT_ID);

			selectStmts.addElement(SELECT_NAME);


			StringList selectRelStmts = new StringList();
			selectRelStmts.addElement(DomainRelationship.SELECT_ID);

			 //fetching list of related MEPs via Manufacturer Equivalent
			//String where = getWhereClauseForMEP(paramMap);
			//String where = "name == \"MEPPart-1\"";

	    	Map paramList = (HashMap) paramMap.get("paramList");

	    	String filters = (String) paramList.get("filters");
		String where = "";

	  	if(filters != null){
			JSONObject searchJson = new JSONObject(filters);
			Iterator itr   = searchJson.keys();
			int count = 0;



			while(itr.hasNext()) {
				String key = (String)itr.next();
				if(key.equals("SEPName") || key.equals("Supplier") || key.equals("SEPPreference")){
					String relName = "";
					if(key.equals("SEPName"))
						relName = "name";
					else if(key.equals("Supplier"))
		  				relName = "to["+ CPCConstants.RELATIONSHIP_SUPPLIED_BY+"].from.name";
		  			else if(key.equals("SEPPreference"))
		  				relName = "to["+CPCConstants.RELATIONSHIP_SUPPLIER_EQUIVALENT+"].tomid["+ CPCConstants.RELATIONSHIP_QUALIFICATION+"].from."+ CPCConstants.SELECT_ATTRIBUTE_PREFERENCE+".value";

		  			JSONArray fieldValues =  searchJson.getJSONArray(key);
					String val = "";
					for(int i = 0; i < fieldValues.length(); i++ ) {
						StringTokenizer tokenizer = new StringTokenizer((String) fieldValues.get(i), "|");
						tokenizer.nextToken();
						if(i == (fieldValues.length() -1))
							val += tokenizer.nextToken();
						else
							val += tokenizer.nextToken()+"|";

					}
					if(count == 0){
						where += relName+" ~~ \""+ val + "\"";
						count++;
					}
					else
						where += " && "+ relName +" ~~ \""+ val +"\"";

				}



			}
		}

			MapList listCorpMEPs = partObj.getRelatedObjects(context,
										   CPCConstants.RELATIONSHIP_SUPPLIER_EQUIVALENT,              // relationship pattern
										   CPCConstants.TYPE_SEP,              // object pattern
										   selectStmts,                 // object selects
										   selectRelStmts,              // relationship selects
										   false,                        // to direction
										   true,                       // from direction
										   (short) 1,                   // recursion level
										   where,                        // object where clause
								   			null);                        // relationship where clause

			DebugUtil.debug("************ Exit  getRelatedSEPs ");
			return listCorpMEPs;

		}



	public StringList getCompanyInfoHTMLOutput (Context context,String[] args, String id)
		             throws Exception
		 {

		    DebugUtil.debug(" ************ Enter getCompanyInfoHTMLOutput ****************************************  " + id);

		    HashMap paramMap = (HashMap)JPO.unpackArgs(args);
		    Map paramList = (HashMap) paramMap.get("paramList");
			String suiteDir = (String) paramList.get("SuiteDirectory");
			String suiteKey = (String) paramList.get("suiteKey");
			String publicPortal = (String) paramList.get("publicPortal");
	  	   	//String linkFile = (publicPortal != null && publicPortal.equalsIgnoreCase("true")) ? "emxNavigator.jsp" : "emxTree.jsp";
		    MapList objectList = (MapList) paramMap.get("objectList");
		    String fromCompare = (String) paramList.get("fromCompare");

		    StringList result = new StringList();

			if (objectList != null && objectList.size() > 0) {
				// construct array of ids
				int objectListSize = objectList.size();

				for (int i = 0; i < objectListSize; i++) {

					Map dataMap = (Map) objectList.get(i);
					String objectId = (String) dataMap.get("id");
					MapList epList = new MapList();
					if(id.equals("manufacturer"))
						epList = getRelatedMEPs(context, paramMap, objectId);
					else if(id.equals("supplier"))
						epList = getRelatedSEPs(context, paramMap, objectId);

					StringBuffer output = new StringBuffer(" ");

					for(int j=0;j< epList.size();j++){

						Map epMap = (Map) epList.get(j);
						String epId = (String) epMap.get("id");
						MapList compList = new MapList();
						if(id.equals("manufacturer"))
							 compList = getRelatedManufacturers(context, paramMap, epId);
						if(id.equals("supplier"))
							 compList = getRelatedSuppliers(context, paramMap, epId);
						//Iterating to Corporate Context MEP list to load MEP Ids
						for(int k=0;k<compList.size();k++) {

							Map tempMap = (Map)compList.get(k);
							String objname = (String)tempMap.get(SELECT_NAME);
							String objid = (String)tempMap.get(SELECT_ID);

							if(fromCompare != null && fromCompare.equals("true")){
								output.append(objname);
								output.append(" <br/> <br/>");
							}else{

								output.append("<a href='javascript:showNonModalDialog(\""+"../common/emxTree.jsp?objectId="+objid+"\",575,575)'>");


								//output.append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/"+ linkFile + "?emxSuiteDirectory=");
								//output.append(suiteDir);
								//output.append("&amp;suiteKey=");
								//output.append(suiteKey);
								//output.append("&amp;objectId=");
								//output.append(objid);
								//output.append("', '', '', 'false', 'popup', '')\">");
								output.append(objname);
								output.append("</a> ");
								output.append(" <br/> <br/>");
							}


						}// end of for (listCorpMEPs)
					}

					if (!"".equals(output.toString())) {
						result.add(output.toString());
					}

			} // end for
		} // end if

		DebugUtil.debug("************ Exit getCompanyInfoHTMLOutput ");
		return result;
   }


	public MapList getRelatedManufacturers(Context context, HashMap paramMap, String objectId) throws Exception{

	    	DebugUtil.debug("************ Enter getRelatedManufacturers ");
	    	DomainObject partObj = DomainObject.newInstance(context,objectId);
	    	StringList selectStmts = new StringList(2);
		selectStmts.addElement(SELECT_ID);
		selectStmts.addElement(SELECT_NAME);
		StringList selectRelStmts = new StringList();

	    	Map paramList = (HashMap) paramMap.get("paramList");

	    	String filters = (String) paramList.get("filters");
	    	String where = "";

	  	if(filters != null){
			JSONObject searchJson = new JSONObject(filters);
			Iterator itr   = searchJson.keys();
			int count = 0;



			while(itr.hasNext()) {
					String key = (String)itr.next();
					String relName = "";
					if(key.equals("Manufacturer")){
						relName = "name";
						JSONArray fieldValues =  searchJson.getJSONArray(key);
						String val = "";
						for(int i = 0; i < fieldValues.length(); i++ ) {
							StringTokenizer tokenizer = new StringTokenizer((String) fieldValues.get(i), "|");
							tokenizer.nextToken();
							if(i == (fieldValues.length() -1))
								val += tokenizer.nextToken();
							else
								val += tokenizer.nextToken()+"|";

						}
						if(count == 0){
							where += relName+" == \""+ val + "\"";
							count++;
						}
						else
							where += " && "+ relName +" == \""+ val +"\"";

				}

			}
		}

		MapList listCorpManus = partObj.getRelatedObjects(context,
												   RELATIONSHIP_MANUFACTURING_RESPONSIBILITY,              // relationship pattern
												   TYPE_COMPANY,              // object pattern
												   selectStmts,                 // object selects
												   selectRelStmts,              // relationship selects
												   true,                        // to direction
												   false,                       // from direction
												   (short) 1,                   // recursion level
												   where,                        // object where clause
										   			null);                        // relationship where clause


		DebugUtil.debug("************ Exit getRelatedManufacturers ");
		return listCorpManus;

	}


	public MapList getRelatedSuppliers(Context context, HashMap paramMap, String objectId) throws Exception{

		    	DebugUtil.debug("************ Enter getRelatedSuppliers ");
		    	DomainObject partObj = DomainObject.newInstance(context,objectId);
		    	StringList selectStmts = new StringList(2);
				selectStmts.addElement(SELECT_ID);
				selectStmts.addElement(SELECT_NAME);
				StringList selectRelStmts = new StringList();

		    	Map paramList = (HashMap) paramMap.get("paramList");

		    	String filters = (String) paramList.get("filters");
			String where = "";
		  	if(filters  != null){
		  	JSONObject searchJson = new JSONObject(filters);
		    	Iterator itr   = searchJson.keys();
		    	int count = 0;


		    	while(itr.hasNext()) {
					String key = (String)itr.next();
					String relName = "";
					if(key.equals("Supplier")){
						relName = "name";
						JSONArray fieldValues =  searchJson.getJSONArray(key);
						String val = "";
						for(int i = 0; i < fieldValues.length(); i++ ) {
							StringTokenizer tokenizer = new StringTokenizer((String) fieldValues.get(i), "|");
							tokenizer.nextToken();
							if(i == (fieldValues.length() -1))
								val += tokenizer.nextToken();
							else
								val += tokenizer.nextToken()+"|";

						}
						if(count == 0){
							where += relName+" == \""+ val + "\"";
							count++;
						}
						else
							where += " && "+ relName +" == \""+ val +"\"";

					}

				}
			}

				MapList listCorpSupps = partObj.getRelatedObjects(context,
													   CPCConstants.RELATIONSHIP_SUPPLIED_BY,              // relationship pattern
													   TYPE_COMPANY,              // object pattern
													   selectStmts,                 // object selects
													   selectRelStmts,              // relationship selects
													   true,                        // to direction
													   false,                       // from direction
													   (short) 1,                   // recursion level
													   where,                        // object where clause
											   			null);                        // relationship where clause




				DebugUtil.debug("************ Exit getRelatedSuppliers ");

				return listCorpSupps;

	}


	public StringList getQualInfoHTMLOutput(Context context, String[] args, String id, String qualAttrib, String useBOMQualification) throws Exception {

			DebugUtil.debug(" ************ Enter getQualInfoHTMLOutput ");
			StringList result = new StringList();

			try {

				HashMap paramMap = (HashMap) JPO.unpackArgs(args);
				Map paramList = (HashMap) paramMap.get("paramList");

				MapList objectList = (MapList) paramMap.get("objectList");
				if (objectList != null && objectList.size() > 0) {


					// construct array of ids
					int objectListSize = objectList.size();

					StringList selects = new StringList();
					String relQualId = "tomid[Qualification].from.id";
					DomainObject.MULTI_VALUE_LIST.add(relQualId);
					selects.addElement(relQualId);

					for (int i = 0; i < objectListSize; i++) {
						Map dataMap = (Map) objectList.get(i);
						String objectId = (String) dataMap.get("id");
						MapList mepList = new MapList();

						if(id.equals("mepqualification"))
							mepList = getRelatedMEPs(context, paramMap, objectId);
						else
							mepList = getRelatedSEPs(context, paramMap, objectId);

						String[] oidList = new String[mepList.size()];

						for(int j=0; j < mepList.size();j++) {
							Map tempMap = (Map) mepList.get(j);
							oidList[j] = (String) tempMap.get("id[connection]");

						}


						StringBuffer output = new StringBuffer(" ");
						MapList qualMaplist = DomainRelationship.getInfo(context, oidList, selects);
						//DebugUtil.debug("******************************* qualMaplist.size() :"+ qualMaplist.size());
						//DebugUtil.debug("******************************* qualMaplist :"+ qualMaplist);

						if (qualMaplist != null && qualMaplist.size() > 0) {
							Iterator qualMaplistItr = qualMaplist.iterator();

							while (qualMaplistItr.hasNext()) {

								Map qualMap = (Map) qualMaplistItr.next();

								if(qualMap.containsKey(relQualId)){
									Object obj = qualMap.get(relQualId);
									if(obj instanceof String){

										String qid = (String) qualMap.get(relQualId);
										DomainObject qpartObj = DomainObject.newInstance(context, qid);
										Map attrMap = qpartObj.getAttributeMap(context, true);

										String pref = (String)attrMap.get("Preference");
										String prefScore = (String)attrMap.get("Preference Score");;
										String qualStatus = qpartObj.getInfo(context, DomainObject.SELECT_CURRENT);
										String qualID = (String) attrMap.get("Qualification Type ID");

										if((useBOMQualification != null && useBOMQualification.equals("true")) && (qualID != null && qualID.equals("BQML2"))
											||(useBOMQualification != null && useBOMQualification.equals("true")) && (qualID != null && qualID.equals("BQSL2"))
											|| ((useBOMQualification == null || useBOMQualification.equals("")) && qualID != null && qualID.equals("QML2"))
											|| ((useBOMQualification == null || useBOMQualification.equals("")) && qualID != null && qualID.equals("QSL2"))){
											  if(qualAttrib.equals("preference"))
												output.append(pref);
											  else if(qualAttrib.equals("preferenceScore"))
												output.append(prefScore);
											  else if(qualAttrib.equals("qualificationStatus"))
												output.append(qualStatus);
											output.append(" <br/> <br/>");
										}

									}
									else {

										StringList idList = (StringList) qualMap.get(relQualId);
										Iterator idListItr = idList.iterator();

										while (idListItr.hasNext()){

											String qid = (String) idListItr.next();

											DomainObject qpartObj = DomainObject.newInstance(context, qid);
											Map attrMap = qpartObj.getAttributeMap(context, true);


											String pref = (String)attrMap.get("Preference");
											String prefScore = (String)attrMap.get("Preference Score");;
											String qualStatus = qpartObj.getInfo(context, DomainObject.SELECT_CURRENT);
											String qualID = (String) attrMap.get("Qualification Type ID");


											if(((useBOMQualification != null && useBOMQualification.equals("true")) && (qualID != null && qualID.equals("BQML2")))
												||((useBOMQualification != null && useBOMQualification.equals("true")) && (qualID != null && qualID.equals("BQSL2")))
												|| ((useBOMQualification == null || useBOMQualification.equals("") || useBOMQualification.equals("false")) && (qualID != null && qualID.equals("QML2")))
												||((useBOMQualification == null || useBOMQualification.equals("") || useBOMQualification.equals("false")) && (qualID != null && qualID.equals("QSL2")))){

												  if(qualAttrib.equals("preference"))
													output.append(pref);
												  else if(qualAttrib.equals("preferenceScore"))
													output.append(prefScore);
												  else if(qualAttrib.equals("qualificationStatus"))
													output.append(qualStatus);
												 output.append(" <br/> <br/>");

											}


										}//end while
									}
								}
								else{

									 output.append(" <br/> <br/>");
								}


							}//end while
						}


						if (!"".equals(output.toString())) {
							result.add(output.toString());
						}

					}//end for
					DomainObject.MULTI_VALUE_LIST.remove(relQualId);


				}// end if


			} catch (Exception Ex) {
				throw Ex;
			}

			DebugUtil.debug(" ************ Exit getQualInfoHTMLOutput ");
			return result;

	    }// end of method getMEPNamesHTMLOutput()
 }
