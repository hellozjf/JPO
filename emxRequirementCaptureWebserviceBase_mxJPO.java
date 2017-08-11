/**
* @fullreview  Developer Reviewer YY:MM:DD : Comment (e.g. Highlight name)
* @quickreview DJH     13:07:24 : HL -  RMT Requirements Update From Word : Added new services for user defined types and attributes.
* @quickreview DJH ZUD 13:07:15 : HL -  RMT Requirements Update From Word : Added new services getSpecChildren(), disconnectSelectedObject(), getAttributeValues(). Modified getAttributes().
* @quickreview DJH OEP 13:01:23 : IR-195580V6R2014 preconnected context checked in getSecurityContexts() and checkLoginDetails()
* @quickreview qyg     12:12:17 : external authentication fix(IR-195580V6R2014)
* @quickreview QYG     12:10:22 : fix issue with getVaultNames for external authentication (IR-195580V6R2014)
* @quickreview DJH OEP 12:09:21 : HL - Security Context Support. WebService sugnatures updated. Added new services getSecurityContexts() and checkLoginDetails()
* @quickreview QYG     12:09:20 : majorminor fix for "last" keyword
*/

/* emxRequirementCaptureWebserviceBase.java
**
** Created on Oct 11, 2006
**
** Copyright (c) 2005-2016 Dassault Systemes.
** All Rights Reserved
** This program contains proprietary and trade secret information of
** MatrixOne, Inc.  Copyright notice is precautionary only and does
** not evidence any actual or intended publication of such program.
*/

import matrix.db.Context;

import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.requirements.RequirementsCommon;
import com.matrixone.apps.requirements.webservices.Attribute;
import com.matrixone.apps.requirements.webservices.ListOfStringHolder;
import com.matrixone.apps.requirements.webservices.RequirementCaptureService;
import com.matrixone.jsystem.ws.holders.StringHolder;



/**
 * The <code>emxRequirementCaptureWebserviceBase</code> class provides web services associated with Requirements.
 *
 * @author casto
 * @version RMT V6R2008-2.0 - Copyright (c) 2007, MatrixOne, Inc.
 */
public class emxRequirementCaptureWebserviceBase_mxJPO extends jpo.plmprovider.Mat3DLive_mxJPO
{
	/**
	 * Constructor.
	 *
	 * @since RMT V6R2008-2.0
	 */
	public emxRequirementCaptureWebserviceBase_mxJPO()
	{
	}
	public emxRequirementCaptureWebserviceBase_mxJPO(Context context)
	{
	}


	/**
	 * This service starts the checkin .  Used by Word Capture for Requirement Specification
	 *
	 * @param username the matrix user name
	 * @param password password of the user
	 * @param oid the object to check the file into
	 * @param docName the name of file
	 * @param url the location of the service to check in the file
	 * @return a string[]  containing the info needed to continue processing the checkin
	 * @since RMT V6R2008-2.0
	 */
	public String[] checkinStart(Context context, String sContext, String username,
		String password,
		String oid,
		String docName,
		String url)
	{
		String[] returnString = new String[2];
		try
		{
			if(!context.isConnected())
			{
				initContext(username, password, sContext);
				context = getContext();
			}
			RequirementCaptureService service = new RequirementCaptureService();
			ListOfStringHolder result = new ListOfStringHolder();
			service.checkinStart(context, sContext, oid, docName, url, result);
			
			return result.getValue();
		}
		catch (Exception e)
		{
			log("checkinStart exception " + e);
			returnString[0] = "ERROR:" + e.getMessage();
		}

		return returnString;
	}


	/**
	 * This service finishes the checkin .  Used by Word Capture for Requirement Specification
	 *
	 * @param username the matrix user name
	 * @param password password of the user
	 * @param receipt the return from the upload of the file used to finialize the checkin
	 * @return a string containing success or error message
	 * @since RMT V6R2008-2.0
	 */
	public String checkinEnd(Context context, String sContext, String username,
		String password,
		String receipt)
	{
		String returnString = "Success";

		try
		{
			if(!context.isConnected())
			{
				initContext(username, password, sContext);
				context = getContext();
			}
			RequirementCaptureService service = new RequirementCaptureService();
			StringHolder result = new StringHolder();
			service.checkinEnd(context, sContext, receipt, result);
			
			return result.getValue();
		}
		catch (Exception e)
		{
			log("checkinEnd exception " + e);
			returnString = "ERROR:" + e.getMessage();
		}

		return returnString;
	}


	/**
	 * This service gets the names of all the vaults.  Used by Word Capture
	 *
	 * @param username the matrix user name
	 * @param password password of the user
	 * @param lang the language of the client
	 * @return a string[] containing the names of the vaults
	 * @since RMT V6R2008-2.0
	 */
	public String[] getVaultNames(Context context, String sContext, String username,
			String password,
			String lang)
	{
		String[] returnString;

		try
		{
			if(!context.isConnected())
			{
				initContext(username, password, sContext);
				context = getContext();
			}
			RequirementCaptureService service = new RequirementCaptureService();
			ListOfStringHolder result = new ListOfStringHolder();
			service.getVaultNames(context, sContext, lang, result);
			
			return result.getValue();
		}
		catch (Exception e)
		{
			log("getVaultNames exception " + e);
			returnString = new String[1];
			returnString[0] = "ERROR:" + e.getMessage();
		}

		return returnString;
	}


	/**
	 * This service gets all the createable subtypes for a given type
	 *
	 * @param username the matrix user name
	 * @param password password of the user
	 * @param lang the language of the client
	 * @param type the root type
	 * @return a string[]  containing  the root type and all of its subtypes
	 * @since RMT V6R2008-2.0
	 */

	public String[] getTypes(Context context, String sContext, String username,
		String password,
		String lang,
		String type)
	{
		String[] returnString;

		try
		{
			if(!context.isConnected())
			{
					initContext(username, password, sContext);
					context = getContext();
				}
			RequirementCaptureService service = new RequirementCaptureService();
			ListOfStringHolder result = new ListOfStringHolder();
			service.getTypes(context, sContext, lang, type, result);
			
			return result.getValue();
		}
		catch (Exception e)
		{
			log("getTypes exception " + e);
			returnString = new String[1];
			returnString[0] = "ERROR:" + e.getMessage();
		}

		return returnString;
	}

	/**
	 * This service gets all the policies for a given type
	 *
	 * @param username the matrix user name
	 * @param password password of the user
	 * @param lang the language of the client
	 * @param type the type to get policies for
	 * @return a string[]  containing all the possible policies for the type
	 * @since RMT V6R2008-2.0
	 */
	public String[] getPolicies(Context context, String sContext, String username,
		String password,
		String lang,
		String type)
	{
		String[] returnString;

		try
		{
			if(!context.isConnected())
			{
				initContext(username, password, sContext);
				context = getContext();
			}

			RequirementCaptureService service = new RequirementCaptureService();
			ListOfStringHolder result = new ListOfStringHolder();
			service.getPolicies(context, sContext, lang, type, result);
			
			return result.getValue();
		}
		catch (Exception e)
		{
			log("getPolicies exception " + e);
			returnString = new String[1];
			returnString[0] = "ERROR:" + new RequirementCaptureService().translateExceptionMessage(context, e, lang);
		}

		return returnString;
	}


	/**
	 * This service gets all the values of a range attribute
	 *
	 * @param username the matrix user name
	 * @param password password of the user
	 * @param lang the language of the client
	 * @param attribute the attribute name
	 * @return a string[]  containing all the possible values for the range attribute
	 * @since RMT V6R2008-2.0
	 */
	public String[] getRange(Context context, String sContext, String username,
		String password,
		String lang,
		String attribute)
	{
		String[] returnString;

		try
		{
			if(!context.isConnected())
			{
				initContext(username, password, sContext);
				context = getContext();
			}
			RequirementCaptureService service = new RequirementCaptureService();
			ListOfStringHolder result = new ListOfStringHolder();
			service.getRange(context, sContext, lang, attribute, result);
			
			return result.getValue();
		}
		catch (Exception e)
		{
			log("getRange exception " + e);
			returnString = new String[1];
			returnString[0] = "ERROR:" + e.getMessage();
		}

		return returnString;

	}


	/**
	 * This service gets the latest revision of all objects of a given type
	 *
	 * @param username the matrix user name
	 * @param password password of the user
	 * @param type the object type
	 * @return a string[]  containing all the possible objects names
	 * @since RMT V6R2008-2.0
	 */
	public String[] getExistingObjects(Context context, String sContext, String username,
		String password,
		String type)
	{
		String[] returnString;

		try
		{
			if(!context.isConnected())
			{
				initContext(username, password, sContext);
				context = getContext();
			}
			RequirementCaptureService service = new RequirementCaptureService();
			ListOfStringHolder result = new ListOfStringHolder();
			service.getExistingObjects(context, sContext, type, result);
			
			return result.getValue();
		}
		catch (Exception e)
		{
			log("getExistingObjects exception " + e);
			returnString = new String[1];
			returnString[0] = "ERROR:" + e.getMessage();
		}

		return returnString;
	}


	/**
	 * This service gets a list of all types that can be captured using Excel
	 *
	 * @param username the matrix user name
	 * @param password password of the user
	 * @param lang the language of the client
	 * @return a string[]  containing all the possible types
	 * @since RMT V6R2009x
	 */
	public String[] getAvailableCaptureTypes(Context context, String sContext, String username,
			String password,
			String lang)
	{
		String[] returnString;

		try
		{
			if(!context.isConnected())
			{
				initContext(username, password, sContext);
				context = getContext();
			}
			RequirementCaptureService service = new RequirementCaptureService();
			ListOfStringHolder result = new ListOfStringHolder();
			service.getAvailableCaptureTypes(context, sContext, lang, result);
			
			return result.getValue();
		}
		catch (Exception e)
		{
			returnString = new String[1];

			log("getAvailableCaptureTypes exception " + e);
			returnString[0] = "ERROR:" + e.getMessage();
		}

		return returnString;
	}


	/**
	 * This service gets a list of all captureable attributes for a given type
	 *
	 * @param username the matrix user name
	 * @param password password of the user
	 * @param lang the language of the client
	 * @param type the type of object
	 * @return a string[]  containing all the possible attributes
	 * @since RMT V6R2009x
	 */
	public String[] getAttributes(Context context, String sContext, String username,
			String password,
			String lang,
			String type)
	{
		String[] returnString;

		try
		{
			if(!context.isConnected())
			{
				initContext(username, password, sContext);
				context = getContext();
			}
			RequirementCaptureService service = new RequirementCaptureService();
			ListOfStringHolder result = new ListOfStringHolder();
			service.getAttributes(context, sContext, lang, type, result);
			
			return result.getValue();
		}
		catch (Exception e)
		{
			returnString = new String[1];

			log("getAttributes exception " + e);
			returnString[0] = "ERROR:" + e.getMessage();
		}

		return returnString;
	}

	/**
	 * This service returns the attribute values of an input object
	 *
	 * @param sContext the current security context of user
	 * @param user name
	 * @param password
	 * @param objID object id of the spec
	 * @return a string list  containing  attribute name and respective attribute values
	 * @since RMT V6R2014x
	 */
	public String[] getAttributeValues( Context context, String sContext, String userName,
			                            String password, String lang, String objId, String[]attributes )
	{
		String[] returnString;

		try
		{
			if(!context.isConnected())
			{
				initContext(userName, password, sContext);
				context = getContext();
			}

			RequirementCaptureService service = new RequirementCaptureService();
			ListOfStringHolder result = new ListOfStringHolder();
			service.getAttributeValues(context, sContext, lang, objId, attributes, result);
			
			return result.getValue();
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			returnString = new String[1];

			log("getAttributeValues exception " + e);
			returnString[0] = "ERROR:" + e.getMessage();
		}

		return returnString;
	}

	/**
	 * This service gets a list of all possible parent types for excel capture for a given type
	 *
	 * @param username the matrix user name
	 * @param password password of the user
	 * @param lang the language of the client
	 * @param type the type of object
	 * @return a string[]  containing all the possible parent types
	 * @since RMT V6R2009x
	 */
	public String[] getAvailableParentTypes(Context context, String sContext, String username,
			String password,
			String lang,
			String type)
	{
		String[] returnString = new String[3];
		returnString[0] = "";
		returnString[1] = "";
		returnString[2] = "";
		try
		{
			if(!context.isConnected())
			{
				initContext(username, password, sContext);
				context = getContext();
			}
			RequirementCaptureService service = new RequirementCaptureService();
			ListOfStringHolder result = new ListOfStringHolder();
			service.getAvailableParentTypes(context, sContext, lang, type, result);
			
			return result.getValue();
		}
		catch (Exception e)
		{
			log("getAvailableParentTypes exception " + e);
		}
		return returnString;
	}


	/**
	 * This service creates an object
	 *
	 * @param username the matrix user name
	 * @param password password of the user
	 * @param lang the language of the client
	 * @param source where the call is coming from, Word or Excel
	 * @param attributes name/value pairs for all the attributes of the new object
	 * @param parentId the oid of the object to be connected to new object as a parent
	 * @param parentRel the relationship to use to connect the new object to its parent
	 * @param relAttributes name/value pairs for all the attributes of the relationship
	 * @return a string containing the name and Id of the new object
	 * @since RMT V6R2009x
	 */
	public String createObject(Context context, String sContext, String username,
		String password,
		String lang,
		String source,
		Attribute[] attributes,
		String parentId,
		String parentRel,
		Attribute[] relAttributes)
	{
		String returnString = "";
		try
		{
			if(!context.isConnected())
			{
				initContext(username, password, sContext);
				context = getContext();
			}
            ComponentsUtil.checkLicenseReserved (context, "ENO_RMF_TP");
			returnString = RequirementsCommon.createObject(context, lang, source, attributes, parentId, parentRel, relAttributes);
		}
		catch (Exception e)
		{
			log("captureRequirement exception " + e);
			returnString = "ERROR:" + e.toString();
		}

		return returnString;
	}


	/**
	 * This service gets a the id of an existing object
	 *
	 * @param username the matrix user name
	 * @param password password of the user
	 * @param type the type of object
	 * @param name the name of the existing object
	 * @return a string containing the name and Id of the new object
	 * @since RMT V6R2009x
	 */
	public String getExistingObject(Context context, String sContext, String username,
		String password,
		String type,
		String name)
	{
		String returnString = "";

		try
		{
			if(!context.isConnected())
			{
				initContext(username, password, sContext);
				context = getContext();
			}
			RequirementCaptureService service = new RequirementCaptureService();
			StringHolder result = new StringHolder();
			service.getExistingObject(context, sContext, type, name, result);
			
			return result.getValue();
		}
		catch (Exception e)
		{
			log("captureRequirement exception " + e);
			returnString = "ERROR:" + e.toString();
		}

		return returnString;
	}

	/**
	 * This service gets all the Security Contexts for a user
	 *
	 * @param username the matrix user name
	 * @param password password of the user
	 * @param lang the language of the client
	 * @param type the root type
	 * @return a string[]  containing  all available Security Contexts
	 * @since RMT V6R2013x
	 */
	public String[] getSecurityContexts(Context context, String username,
		String password,
		String lang )
	{
		String[] returnString;

		try
		{

			// Start Security Context Details
			if(!context.isConnected())
			{
				String key = initContext(username, password);
				if( key.contains("FAILURE") )
				{
					returnString = new String[1];
					returnString[0] = key;
					return returnString;
				}
				context = getContext();
			}
			RequirementCaptureService service = new RequirementCaptureService();
			ListOfStringHolder result = new ListOfStringHolder();
			service.getSecurityContexts(context, lang, result);
			
			return result.getValue();
		}
		catch (Exception e)
		{
			log("getSecurityContexts exception " + e);
			returnString = new String[1];
			returnString[0] = "ERROR:" + e.getMessage();
		}

		return returnString;
	}

	/**
	 * This service checks if input user id and password are correct or not
	 *
	 * @param username the matrix user name
	 * @param password password of the user
	 * @return a string  containing  login validation info (SUCCEESS\FAILURE)
	 * @since RMT V6R2013x
	 */
	public String checkLoginDetails( Context context, String username,
		                               String password )
	{
		String returnString = "FAILURE";

		try
		{
			if(!context.isConnected())
			{
				returnString = initContext(username, password);
			}
			else
			{
			  returnString = "SUCCESS";
			}

		}
		catch (Exception e)
		{

		}
		return returnString;
	}

	/**
	 * This service returns all the related objects of an input req spec
	 *
	 * @param sContext the current security context of user
	 * @param objID object id of the spec
	 * @return a string list  containing  information (name and id) of all the related objects
	 * @since RMT V6R2014x
	 */
	public String[] getSpecChildren ( Context context, String sContext, String username,
                                      String password, String objID  )
	{
		String[] arrayOfStrings;

		try
		{

		  if(!context.isConnected())
		  {
			  initContext("Test Everything", "", sContext);
			  context = getContext();
		  }
			RequirementCaptureService service = new RequirementCaptureService();
			ListOfStringHolder result = new ListOfStringHolder();
			service.getSpecChildren(context, sContext, objID, result);
			
			return result.getValue();
		}
		catch (Exception e)
		{
			log("getSpecChildren exception " + e);
			arrayOfStrings = new String[1];
			arrayOfStrings[0] = "ERROR:" + e.getMessage();

		}
 		 return arrayOfStrings;
	}

	/**
	 * This service disconnects selected object from its req spec
	 *
	 * @param sContext the current security context of user
	 * @param user name
	 * @param password
	 * @param objID object id of the spec
	 * @return a string list  containing  information (name and id) of all the related objects
	 * @since RMT V6R2014x
	 */
	public String[] disconnectSelectedObject ( Context context, String sContext, String username,
                                               String password, String objID  )
	{
		String[] arrayOfStrings;

		try
		{
		  if(!context.isConnected())
		  {
		    initContext(username, password, sContext);
		    context = getContext();
		  }
			RequirementCaptureService service = new RequirementCaptureService();
			ListOfStringHolder result = new ListOfStringHolder();
			service.disconnectSelectedObject(context, sContext, objID, result);
			
			return result.getValue();
		}
		catch (Exception e)
		{
			log("disconnectSelectedObject exception " + e);
			arrayOfStrings = new String[1];
			arrayOfStrings[0] = "ERROR:" + e.getMessage();
		}
 		return arrayOfStrings;
	}

	/**
	 * This service gets a list of all additional attributes for a given sub type
	 *
	 * @param username the matrix user name
	 * @param password password of the user
	 * @param lang the language of the client
	 * @param type the type of object
	 * @return a string[]  containing all the possible attributes
	 * @since RMT V6R2009x
	 */
	public String[] getAdditionalAttributes(Context context, String sContext, String username,
			String password,
			String lang,
			String subtype)
	{
		String[] returnString;

		try
		{
			if(!context.isConnected())
			{
				initContext(username, password, sContext);
				context = getContext();
			}
			RequirementCaptureService service = new RequirementCaptureService();
			ListOfStringHolder result = new ListOfStringHolder();
			service.getAdditionalAttributes(context, sContext, lang, subtype, result);
			
			return result.getValue();
		}
		catch (Exception e)
		{
			returnString = new String[1];

			log("getAdditionalAttributes exception " + e);
			returnString[0] = "ERROR:" + e.getMessage();
		}

		return returnString;
	}

	/**
	 * This service returns a list of all custo syb types of a given type
	 *
	 * @param username the matrix user name
	 * @param password password of the user
	 * @param lang the language of the client
	 * @param type the type of object
	 * @return a string[]  containing all the Custo Sub Types
	 * @since RMT V6R2009x
	 */

	public String[] getCustoSubTypes(Context context, String sContext, String username,
			String password,
			String lang,
			String type)
	{
		String[] returnString;

		try
		{
			if(!context.isConnected())
			{
				initContext(username, password, sContext);
				context = getContext();
			}
			RequirementCaptureService service = new RequirementCaptureService();
			ListOfStringHolder result = new ListOfStringHolder();
			service.getCustoSubTypes(context, sContext, lang, type, result);
			
			return result.getValue();
		}

		catch (Exception e)
		{
			returnString = new String[1];

			log("getCustoSubTypes exception " + e);
			returnString[0] = "ERROR:" + e.getMessage();
		}

		return returnString;
	}

	/**
	 * This service returns the attributes and their mask values of an input type, as defined in the emxrequirement.Properties file
	 *
	 * @param sContext the current security context of user
	 * @param user name
	 * @param password
	 * @param type
	 * @return a string list  containing  attributes names and respective mask values
	 * @since RMT V6R2014x
	 */
	public String[] getMaskStatus( Context context, String sContext, String userName,
			                            String password, String lang, String subtype )
	{
		String[] returnString = new String[1];

		try
		{
			if(!context.isConnected())
			{
				initContext(userName, password, sContext);
				context = getContext();
			}
			RequirementCaptureService service = new RequirementCaptureService();
			ListOfStringHolder result = new ListOfStringHolder();
			service.getMaskStatus(context, sContext, lang, subtype, result);
			
			return result.getValue();
		}
		catch (Exception e)
		{
			returnString = new String[1];

			log("getMaskStatus exception " + e);
			returnString[0] = "ERROR:" + e.getMessage();
		}

		return returnString;
	}

}

