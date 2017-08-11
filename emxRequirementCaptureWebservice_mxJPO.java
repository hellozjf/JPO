/**
* @fullreview  Developer Reviewer YY:MM:DD : Comment (e.g. Highlight name)
* @quickreview DJH     13:07:24 : HL -  RMT Requirements Update From Word : Added new services for user defined types and attributes.
* @quickreview DJH ZUD 13:07:15 : HL -  RMT Requirements Update From Word : Added new services getSpecChildren(), disconnectSelectedObject(), getAttributeValues().
* @quickreview qyg     12:12:17 external authentication fix(IR-195580V6R2014)
* @quickreview DJH OEP 12:09:21 : HL - Security Context Support. WebService sugnatures updated. Added new services getSecurityContexts() and checkLoginDetails()
*/


/* emxRequirementCaptureWebservice.java
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
import matrix.util.MatrixWrappedService;

import com.matrixone.apps.requirements.webservices.Attribute;

/**
 * @author mkeirstead
 *
 * The <code>${CLASSNAME}</code> class provides web services associated with database queries.
 *
 * @version RMT V6R2008-2.0 - Copyright (c) 2007, MatrixOne, Inc.
 */
public class emxRequirementCaptureWebservice_mxJPO extends emxRequirementCaptureWebserviceBase_mxJPO implements MatrixWrappedService
{
	/**
	 * Constructor.
	 *
	 * @since RMT V6R2008-2.0
	 */
	public emxRequirementCaptureWebservice_mxJPO()
	{
	}
	public emxRequirementCaptureWebservice_mxJPO(Context context)
	{
		super(context);
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
	public String[] checkinStart(Context context, String sContext, String username, String password, String oid, String docName, String url)
	{
		return (super.checkinStart(context, sContext, username, password, oid, docName, url));
	}

	/**
	 * This service finishes the checkin .  Used by Word Capture for Requirement Specification
	 *
	 * @param username the matrix user name
	 * @param password password of the user
	 * @param reciept the return from the upload of the file used to finialize the checkin
	 * @return a string containing success or error message
	 * @since RMT V6R2008-2.0
	 */
	public String checkinEnd(Context context, String sContext, String username, String password, String receipt)
	{
		return (super.checkinEnd(context, sContext, username, password, receipt));
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
	public String[] getVaultNames(Context context, String sContext,  String username, String password, String lang)
	{
		return (super.getVaultNames(context, sContext, username, password, lang));
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
	public String[] getTypes(Context context, String sContext, String username, String password, String lang, String type)
	{
		return (super.getTypes(context, sContext, username, password, lang, type));
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
	public String[] getPolicies(Context context, String sContext, String username, String password, String lang, String type)
	{
		return (super.getPolicies(context, sContext, username, password, lang, type));
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
	public String[] getRange(Context context, String sContext, String username, String password, String lang, String attribute)
	{
		return (super.getRange(context, sContext, username, password, lang, attribute));
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
	public String[] getExistingObjects(Context context, String sContext, String username, String password, String type)
	{
		return (super.getExistingObjects(context, sContext, username, password, type));
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
	public String[] getAvailableCaptureTypes(Context context, String sContext, String username, String password, String lang)
	{
		return (super.getAvailableCaptureTypes(context, sContext, username, password, lang));
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
	public String[] getAttributes(Context context, String sContext, String username, String password, String lang, String type)
	{
		return (super.getAttributes(context, sContext, username, password, lang, type));
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
	public String[] getAvailableParentTypes(Context context, String sContext, String username, String password, String lang, String type)
	{
		return (super.getAvailableParentTypes(context, sContext, username, password, lang, type));
	}

	/**
	 * This service creates an object
	 *
	 * @param username the matrix user name
	 * @param password password of the user
	 * @param lang the language of the client
	 * @param source where the call is coming from, Word or Excel
	 * @param attributes name/value pairs for all the attributes of the new object
	 * @return a string containing the name and Id of the new object
	 * @since RMT V6R2009x
	 */
	public String createObject(Context context, String sContext, String username, String password, String lang, String source, Attribute[] attributes)
	{
		return (super.createObject(context, sContext, username, password, lang, source, attributes, "", "", null));
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
	 * @return a string containing the name and Id of the new object
	 * @since RMT V6R2009x
	 */
	public String createObjectWithParent(Context context, String sContext, String username, String password, String lang, String source, Attribute[] attributes, String parentId, String parentRel)
	{
		return (super.createObject(context, sContext, username, password, lang, source, attributes, parentId, parentRel, null));
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
	public String createObjectWithParentAdvanced(Context context, String sContext, String username, String password, String lang, String source, Attribute[] attributes, String parentId, String parentRel, Attribute[] relAttributes)
	{
		return (super.createObject(context, sContext, username, password, lang, source, attributes, parentId, parentRel, relAttributes));
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
	public String getExistingObject(Context context, String sContext, String username, String password, String type, String name)
	{
		return (super.getExistingObject(context, sContext, username, password, type, name));
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
	public String[] getSecurityContexts(Context context, String username, String password, String lang)
	{
		return (super.getSecurityContexts(context, username, password, lang));
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
		return super.checkLoginDetails(context, username, password);
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
		return super.getSpecChildren(context, sContext, username, password, objID);
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
	public String[] disconnectSelectedObject ( Context context, String sContext,String username,
                                             String password, String objID  ) 
	{
       return super.disconnectSelectedObject(context, sContext, username, password, objID);
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
		return super.getAttributeValues( context,  sContext,  userName, password,  lang,  objId, attributes);
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
		return super.getCustoSubTypes( context,  sContext,  username, password,  lang,  type);
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
			                            String password, String lang, String type )
	{
		return super.getMaskStatus( context,  sContext,  userName, password,  lang,  type);
	}
	
	/**
	 * This service gets a list of all attributes for a given sub type
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
		return super.getAdditionalAttributes( context,  sContext,  username, password,  lang,  subtype);
	}

}
