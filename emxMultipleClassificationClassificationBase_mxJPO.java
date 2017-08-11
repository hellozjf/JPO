/**
 * Copyright (c) 1992-2016 Dassault Systemes. All Rights Reserved. This program
 * contains proprietary and trade secret information of MatrixOne,Inc.
 * Copyright notice is precautionary only and does not evidence any actual or
 * intended publication of such program.
 *
 */

import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import matrix.db.BusinessObject;
import matrix.db.BusinessType;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.RelationshipType;
import matrix.util.MatrixException;
import matrix.util.SelectList;
import matrix.util.StringList;

import com.matrixone.apps.classification.Classification;
import com.matrixone.apps.classification.ClassificationConstants;
import com.matrixone.apps.classification.ClassificationUtil;
import com.matrixone.apps.common.util.CommonElement;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.DebugUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MessageUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.MessageUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.library.LibraryCentralConstants;
import com.matrixone.jdom.Element;
import com.matrixone.jdom.output.XMLOutputter;
import com.matrixone.util.MxXMLUtils;

/**
 * The <code>emxMultipleClassificationClassificationBase</code> class represents the APIs for Classification
 *
 *  @exclude
 */

public class emxMultipleClassificationClassificationBase_mxJPO
  extends emxLibraryCentralCommon_mxJPO
  implements ClassificationConstants
{

    /**
    * Creates emxMultipleClassificationClassificationBase object
    */
    public emxMultipleClassificationClassificationBase_mxJPO (Context context, String[] args) throws Exception {
        super(context, args);
    }
   /**
   * Gets the interace Name
   * @param context the eMatrix <code>Context</code> object
   * @return String the interface name
   * @throws FrameworkException if the operation fails
   */
   public String getInterfaceName(Context context) throws FrameworkException {


        String strResult = getAttributeValue(context, ATTRIBUTE_MXSYS_INTERFACE);


        return strResult;
    }

    /**
     * Returns the complete set of interfaces that this Classification
     * implements directly. If allAncestors, then return inherited interfaces as
     * well.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param allAncestors if true then return inherited interfaces as well
     * @return StringList The list of interface names
     * @throws FrameworkException if the operation fails
     */
    public StringList getInterfaces(Context context, boolean allAncestors)
            throws FrameworkException {

        try {
            StringList resList = new StringList();
            if(getInterfaceName(context) != null && getInterfaceName(context).length() > 0) {
                String cmd      = "print interface $1 select $2 dump";
                String resStr   = MqlUtil.mqlCommand(context, cmd, true, getInterfaceName(context), (allAncestors ? "allparents" : "derived"));

                resList         = FrameworkUtil.split(resStr, ",");
            }

            return resList;
        } catch (MatrixException e) {
            throw new FrameworkException(e);
        }
    }



    /**
     * Returns the complete set of child interfaces of this Classification
     *
     * @param context the eMatrix <code>Context</Code> object
     * @param args holds the following arguments:
     *       0 - allAncestors  if false, return interfaces that are associated to direct subclasses of the context object
     * or if true, return interfaces of all children (multiple level)
     * @return The list of interface names
     * @throws FrameworkException if the operation fails
     */
    public StringList getInterfaces(Context context, String[] args)
            throws FrameworkException {
        if (args == null || args.length < 1) {
            throw (new IllegalArgumentException());
        }

        Map map = null;
        try {
            map = (Map) JPO.unpackArgs(args);
        } catch (Exception e) {
            throw (new FrameworkException(e));
        }

        return getInterfaces(context, ((Boolean) (map.get("allAncestors")))
                .booleanValue());
    }

    /**
     * Returns the complete set of child interfaces of this Classification
     *
     * @param context the eMatrix <code>Context</code> object
     * @param allDerivatives if false, return interfaces that are associated to direct subclasses of the context object
     * or if true, return interfaces of all children (multiple level)
     * @return StringList The list of interface names
     * @throws FrameworkException if the operation fails
     */
    public StringList getDerivativeInterfaceList (Context context, boolean allDerivatives) throws FrameworkException
    {
        try
        {
            StringList derivativeInterfaceList = new StringList();
            StringList objectSelects = new StringList(2);
            MapList subClassObjects = new MapList();
            objectSelects.addElement(DomainConstants.SELECT_ID);
            objectSelects.addElement("attribute["+LibraryCentralConstants.ATTRIBUTE_MXSYSINTERFACE+"]");
            short recurseLevel = (short)(allDerivatives?0:1);

            // getRelatedObjects is used instead of "print interface <interface name> select derivative" to filter out
            // classes that are not accessible to the user e.g. due to being Inactive or Obsolete

            subClassObjects = getRelatedObjects(context,
                                            LibraryCentralConstants.RELATIONSHIP_SUBCLASS,
                                            "*",
                                            objectSelects,
                                            null,
                                            false,
                                            true,
                                            recurseLevel,
                                            "",
                                            "",
                                            null,
                                            null,
                                            null);

            Iterator itrSubClassObjects = subClassObjects.iterator();
            Map mapSubClassObjects = null;

            while(itrSubClassObjects.hasNext())
            {

                mapSubClassObjects = (Map)itrSubClassObjects.next();
                String interfaceName = (String)mapSubClassObjects.get("attribute["+LibraryCentralConstants.ATTRIBUTE_MXSYSINTERFACE+"]");
                if(interfaceName!=null && !"".equals(interfaceName))
                {
                    String strMQL           = "print interface $1 select derived dump";
                    String resStr           = MqlUtil.mqlCommand(context, strMQL, true, interfaceName);
                    StringList resultList   = FrameworkUtil.split(resStr, ",");
                    if(resultList.size()>0)
                    {
                        derivativeInterfaceList.addAll(resultList);
                    }
                }
            }
            return derivativeInterfaceList;
        } catch (Exception e) {
            throw (new FrameworkException(e));
        }
    }


/**
     * Returns the complete set of interfaces that this Classification
     * implements directly. If allAncestors, then return inherited interfaces as
     * well.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param allAncestors
     *            if true then return inherited interfaces as well, else just
     *            directly associated interfaces
     * @return StringList The list of interface names, as a StringList
     * @throws FrameworkException if the operation fails
     */
    public StringList getAttributeGroups(Context context, boolean allAncestors) throws FrameworkException
    {
        //For Search Within feature enhancement, This method is modified to use the overloaded
        //method getAttributeGroups(Context, boolean, boolean).
        try {
            return getAttributeGroups(context, true, allAncestors);
        } catch (MatrixException e) {
            throw new FrameworkException(e);
        }
    }


    /**
     * Returns the complete set of parent or child interfaces of this Classification.
     * If derivedOrDerivatives is true, then it returns all parent interfaces including the interface connectred to this
     * classification else it returns all child interfaces
     *
     * @param context the eMatrix <code>Context</code> object
     * @param derivedOrDerivative If true, returns parent interfaces. If false, returns child interfaces
     * @param flag when derivedOrDerivative is true and If flag is false, return only directly
     *        associated interface or If flag is true, return attribute groups of all ancestorts as well.
     *        When derivedOrDerivative is false and if 'false', return interfaces that are associated to
     *        direct subclasses of the context object or if 'true', return interfaces of all children (multiple level).
     * @return StringList The list of interface names, as a StringList
     * @throws FrameworkException if the operation fails
     */
    public StringList getAttributeGroups(Context context, boolean derivedOrDerivatives, boolean flag) throws FrameworkException
    {

        try {

            StringList myInterfaces = new StringList();

            if(derivedOrDerivatives)
            {
                myInterfaces = getInterfaces(context, flag);
            }
            else
            {
                myInterfaces = getDerivativeInterfaceList(context, flag);
            }

            StringList myAttrGrps = new StringList();
            for (int i = 0; i < myInterfaces.size(); i++)
            {
                String interfaceName = (String) myInterfaces.elementAt(i);
                if (!INTERFACE_CLASSIFICATION_ATTRIBUTE_GROUPS.equals(interfaceName)
                        && ClassificationUtil.isAttributeGroup(context, interfaceName))
                {
                    myAttrGrps.addElement(interfaceName);
                }
            }
            return myAttrGrps;
        } catch (MatrixException e) {
            throw new FrameworkException(e);
        }
    }


    /**
     * Adds the specified attribute group to the classification.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following arguments
     *       0 - grpName Attribute Group name that is to be added.
     * @throws FrameworkException if the operation fails
     */
    public StringList getAttributeGroups(Context context, String[] args) throws FrameworkException
    {
        if (args == null || args.length < 1)
        {
            throw (new IllegalArgumentException());
        }

        Map map = null;
        try {
            map = (Map) JPO.unpackArgs(args);
        } catch (Exception e) {
            throw (new FrameworkException(e));
        }

        //modified for Search Within feature enhancement
        //This method is modified to get the either parent of child attribute
        //groups on the basis of derivedOrDerivative request parameter

        boolean hierarchyParentOrChild = ((Boolean) map.get("derivedOrDerivative")).booleanValue();
        Boolean flag = null;
        StringList attributGroupList = new StringList();

        if(hierarchyParentOrChild)
        {
            flag = (Boolean) map.get("allAncestors");
        }
        else
        {
            flag = (Boolean) map.get("allDerivatives");
        }
        attributGroupList = getAttributeGroups(context, hierarchyParentOrChild, flag.booleanValue());
        return attributGroupList;
    }

    /**
     * Adds the specified attribute group to the classification.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param grpName Attribute Group name that is to be added.
     * @throws FrameworkException if the operation fails
     */
    public void addAttributeGroup(Context context, String grpName)
            throws FrameworkException {
        try {
        	String[] args = null;
        	emxObjectAccess_mxJPO objAccess = new emxObjectAccess_mxJPO(context, args);
        	Boolean hasLibrarianRole = objAccess.hasLibrarianRole(context, args);
        	if(hasLibrarianRole)
        	{
        		StringList myInterfaces = getInterfaces(context, false);
        		myInterfaces.addElement(grpName);
        		if(getInterfaceName(context) != null && myInterfaces.size() > 0) {
        			StringBuffer cmdBuffer = new StringBuffer();
        			String[] methodArgs = new  String[myInterfaces.size()+1];
        			cmdBuffer.append("modify interface $1 ");
        			methodArgs[0] =  getInterfaceName(context);
        			cmdBuffer.append("derived ");
        			for (int i = 0; i < myInterfaces.size(); i++)  {
        				cmdBuffer.append("$").append(i+2).append(",");
        				methodArgs[i+1] =(String) myInterfaces.get(i);

        			}
        			String mqlcommand   = cmdBuffer.toString();
        			mqlcommand          = mqlcommand.substring(0,mqlcommand.lastIndexOf(','));
        			MqlUtil.mqlCommand(context, mqlcommand, true, methodArgs);
        		}
        		else {
        			throw (new FrameworkException("Object does not have an assocaited Interface"));
        		}
        	} 
        	else 
        	{
        		throw (new FrameworkException("User doesnt have Librarian Role"));
        	}
        } catch (Exception e) {
            throw (new FrameworkException(e));
        }
        // TO invalidate VPLM Cache
        invalidateVPLMCatalogueCache(context);

    }


    /**
     * Adds the specified attributed group from the classification
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following list of arguments:
     *      0 - grpName Attribute Group name that is to be removed.
     * @throws FrameworkException if the operation fails
     */
    public void addAttributeGroup(Context context, String[] args)
            throws FrameworkException {
        if (args == null || args.length < 1) {
            throw (new IllegalArgumentException());
        }

        Map map = null;
        try {
            map = (Map) JPO.unpackArgs(args);
        } catch (Exception e) {
            throw (new FrameworkException(e));
        }
        addAttributeGroup(context, (String) map.get("grpName"));
    }


    /**
     * Removes the specified attributed group from the classification
     *
     * @param context the eMatrix <code>Context</code> object
     * @param grpName Attribute Group name that is to be removed.
     * @throws FrameworkException if the operation fails
     */
    public void removeAttributeGroup(Context context, String grpName)
            throws FrameworkException {

    	try {
    		String[] args = null;
    		emxObjectAccess_mxJPO objAccess = new emxObjectAccess_mxJPO(context, args);
    		Boolean hasLibrarianRole = objAccess.hasLibrarianRole(context, args);
    		if(hasLibrarianRole)
    		{
    			StringList myInterfaces = getInterfaces(context, false);
    			for (int i = 0; i < myInterfaces.size(); i++) {
    				if (myInterfaces.elementAt(i).equals(grpName)) {
    					myInterfaces.remove(i);
    					break;
    				}
    			}

    			if(getInterfaceName(context) != null && myInterfaces.size() > 0) {
    				StringBuffer cmdBuffer = new StringBuffer();
    				String[] methodArgs = new  String[myInterfaces.size()+1];
    				cmdBuffer.append("modify interface $1 ");
    				methodArgs[0] =  getInterfaceName(context);
    				cmdBuffer.append("derived ");
    				for (int i = 0; i < myInterfaces.size(); i++)  {
    					cmdBuffer.append("$").append(i+2).append(",");
    					methodArgs[i+1] =(String) myInterfaces.get(i);

    				}
    				String mqlcommand   = cmdBuffer.toString();
    				mqlcommand          = mqlcommand.substring(0,mqlcommand.lastIndexOf(','));
    				MqlUtil.mqlCommand(context, mqlcommand, true, methodArgs);
    			}
    			else {
    				throw (new FrameworkException("Object does not have an associated Interface"));
    			}
    		} 
    		else 
    		{
    			throw (new FrameworkException("User doesnt have Librarian Role"));
    		}
    	} catch (Exception e) {
            throw (new FrameworkException(e));
        }
        // TO invalidate VPLM Cache
        invalidateVPLMCatalogueCache(context);

    }

    /**
     * Removes the specified attributed group from the classification
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following list of arguments:
     *   0 - grpName Attribute Group name that is to be removed.
     * @throws FrameworkException if the operation fails
     */
    public void removeAttributeGroup(Context context, String[] args)
            throws FrameworkException {
        if (args == null || args.length < 1) {
            throw (new IllegalArgumentException());
        }

        Map map = null;
        try {
            map = (Map) JPO.unpackArgs(args);
        } catch (Exception e) {
            throw (new FrameworkException(e));
        }
        removeAttributeGroup(context, (String) map.get("grpName"));
    }

    /**
     * Adds the specified attribute groups to the classification.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following list of arguments:
     *      0 - grpName Attribute Group names that are to be added.
     * @throws FrameworkException if the operation fails
     */
    public void addAttributeGroups(Context context, String[] args)
            throws FrameworkException {
        if (args == null || args.length < 1) {
            throw (new IllegalArgumentException());
        }

        Map map = null;
        try {
            map = (Map) JPO.unpackArgs(args);
        } catch (Exception e) {
            throw (new FrameworkException(e));
        }
        addAttributeGroups(context, (StringList) map.get("grpNames"));
    }

    /**
     * Adds the specified attribute groups to the classification.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param grpName Attribute Group names that are to be added.
     * @throws FrameworkException if the operation fails
     */
    public void addAttributeGroups(Context context, StringList grpNames)
            throws FrameworkException {
        try {
        	String[] args = null;
        	emxObjectAccess_mxJPO objAccess = new emxObjectAccess_mxJPO(context, args);
        	Boolean hasLibrarianRole = objAccess.hasLibrarianRole(context, args);
        	if(hasLibrarianRole)
        	{
        		StringList myInterfaces = getInterfaces(context, false);
        		myInterfaces.addAll(grpNames);
        		if(getInterfaceName(context) != null && myInterfaces.size() > 0) {
        			StringBuffer cmdBuffer = new StringBuffer();
        			String[] methodArgs = new  String[myInterfaces.size()+1];
        			cmdBuffer.append("modify interface $1 ");
        			methodArgs[0] =  getInterfaceName(context);
        			cmdBuffer.append("derived ");
        			for (int i = 0; i < myInterfaces.size(); i++)  {
        				cmdBuffer.append("$").append(i+2).append(",");
        				methodArgs[i+1] =(String) myInterfaces.get(i);

        			}
        			String mqlcommand   = cmdBuffer.toString();
        			mqlcommand          = mqlcommand.substring(0,mqlcommand.lastIndexOf(','));
        			MqlUtil.mqlCommand(context, mqlcommand, true, methodArgs);
        		} else {
        			throw (new FrameworkException("Object does not have an assocaited Interface"));
        		}
        	} 
        	else 
        	{
        		throw (new FrameworkException("User doesnt have Librarian Role"));
        	}
        } catch (Exception e) {
            throw (new FrameworkException(e));
        }
     // TO invalidate VPLM Cache
        invalidateVPLMCatalogueCache(context);
    }
    
    /**
     * Adds the specified attribute groups to the classification.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following list of arguments:
     *      0- grpName Attribute Group names that are to be added.
     * @throws FrameworkException if the operation fails
     */
    public void removeAttributeGroups(Context context, String[] args)
            throws FrameworkException {
        if (args == null || args.length < 1) {
            throw (new IllegalArgumentException());
        }

        Map map = null;
        try {
            map = (Map) JPO.unpackArgs(args);
        } catch (Exception e) {
            throw (new FrameworkException(e));
        }
        removeAttributeGroups(context, (StringList) map.get("grpNames"));
    }

    /**
     * Adds the specified attribute groups to the classification.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param grpName Attribute Group names that are to be added.
     * @throws FrameworkException if the operation fails
     */
    public void removeAttributeGroups(Context context, StringList grpNames)
            throws FrameworkException {
        try {
        	String[] args = null;
    		emxObjectAccess_mxJPO objAccess = new emxObjectAccess_mxJPO(context, args);
    		Boolean hasLibrarianRole = objAccess.hasLibrarianRole(context, args);
    		if(hasLibrarianRole)
    		{
    			StringList myInterfaces = getInterfaces(context, false);
    			myInterfaces.removeAll(grpNames);
    			if(getInterfaceName(context) != null && myInterfaces.size() > 0) {
    				StringBuffer cmdBuffer = new StringBuffer();
    				String[] methodArgs = new  String[myInterfaces.size()+1];
    				cmdBuffer.append("modify interface $1 ");
    				methodArgs[0] =  getInterfaceName(context);
    				cmdBuffer.append("derived ");
    				for (int i = 0; i < myInterfaces.size(); i++)  {
    					cmdBuffer.append("$").append(i+2).append(",");
    					methodArgs[i+1] =(String) myInterfaces.get(i);

    				}
    				String mqlcommand   = cmdBuffer.toString();
    				mqlcommand          = mqlcommand.substring(0,mqlcommand.lastIndexOf(','));
    				MqlUtil.mqlCommand(context, mqlcommand, true, methodArgs);
    			}
    			else if(grpNames.size()>0)
    			{
    				StringBuffer cmdBuffer = new StringBuffer();
    				cmdBuffer.append("modify interface $1 remove derived ");

    				MqlUtil.mqlCommand(context, cmdBuffer.toString(), true, getInterfaceName(context));
    			}
    			else {
    				throw (new FrameworkException("Object does not have an assocaited Interface"));
    			}
    		} 
    		else 
    		{
    			throw (new FrameworkException("User doesnt have Librarian Role"));
    		}
        } catch (Exception e) {
            throw (new FrameworkException(e));
        }
        //TO invalidate VPLM Cache
     invalidateVPLMCatalogueCache(context);
    }


    /**
     * This method transfers the Classification object under a new parent, or
     * orphans it. It takes care of manipulating the interface tree as well as
     * the BO tree.
     *
     * This method is used for these operations: - Add existing subclass - Remove subclass - Move subclass
     *
     * NOTE: manipulations of the data model MUST be done via this method as
     * referential integrity is only ensured here.
     *
     *
     * @param context the eMatrix <code>Context</code> object
     * @param newParent
     *            New parent for this Classification object. May be null. If
     *            null, then this Classifiction becomes orphan.
     * @param carryOverInheritedAttributeGroups
     *            If true, any attribute groups that this classification was
     *            previously inheriting from its parent/ancestors, that would
     *            NOT be inherited from its new parent, will become directly
     *            associated with this Classification prior to the reparenting
     *            taking effect, so as to avoid any loss of attributes on the
     *            affected classified items.
     *
     * @return true for success
     * @throws FrameworkException if the operation fails
     */


    /**Code Altered for R216 Release
     * @param context
     * @param objectId
     * @param oldParent
     * @param newParent
     * @param carryOverInheritedAttributeGroups
     * @return
     * @throws Exception
     */
    public HashMap reparent(Context context, String objectId, String oldParent, String newParent, boolean carryOverInheritedAttributeGroups) throws FrameworkException, Exception
    {
        dbg_break();

        BusinessObject connTrig = null;
        BusinessObject disconnTrig = null;
        boolean success = false;

        try 
        {
            // Find my attribute groups; get either all direct or all direct +
            // inherited, depending on carryOver. These are the attribute
            // groups that will be carried over.
            //
            // NOTE that we need to get this information now, before any
            // connect/disconnect activity, because the disconnect/connect
            // will fire triggers, and the triggers will move interfaces.
            // The intent here is to override the trigger's decisions.
            // The triggers will always do the most conservative thing
            // i.e. carry over inherited attribute groups. Here we may
            // override that, at the user's request.
            //
            // Finally, also NOTE that if carryOver is requested, what
            // we do here should be redundant with the trigger, however, this
            // interface shuffling is critical to referential integrity,
            // so better to be safe than sorry, in case the trigger is
            // disabled or whatever.
        	StringList myAttributeGroups = getAttributeGroups(context,carryOverInheritedAttributeGroups);

        	// disconnect the Classification from its parent
        	StringList busSelects = new StringList();
        	busSelects.addElement("id");
        	Map resultMap = this.getRelatedObject(context,RELATIONSHIP_SUBCLASS, false, busSelects, null);
        	if (resultMap.size() != 0) 
        	{
        		this.disconnect(context, new RelationshipType(RELATIONSHIP_SUBCLASS), false, new BusinessObject((String) resultMap.get("id")));
        	}

        	// Connect to new parent, if any, and get the parent interface name
        	String newParentInterface = null;
        	String oldParentInterface = null;
        	String objectInterface = null;
        	if (newParent != null) 
        	{
        		// connect to new parent
        		this.connect(context, new RelationshipType(RELATIONSHIP_SUBCLASS), false, new BusinessObject(newParent));
            	//Code Added for R216 Release
            	//Check if the New Parent is a Library OR a Classification. 
        		if(new DomainObject(newParent).isKindOf(context, LibraryCentralConstants.TYPE_LIBRARIES))
        		{        			
        			//Get Interface of NewParent
            		String[] parent = {newParent,"attribute["+DomainConstants.ATTRIBUTE_MXSYSINTERFACE+"].value"};
            		newParentInterface = MqlUtil.mqlCommand(context,"print bus $1 select $2 dump",true,parent);
            		
        			DomainObject objOldParent = DomainObject.newInstance (context, oldParent, "Classification");
        			if(!carryOverInheritedAttributeGroups && objOldParent instanceof com.matrixone.apps.classification.Classification)
        			{
        				//Get Interface of Object which is being moved
                		com.matrixone.apps.classification.Classification objectClassification = (com.matrixone.apps.classification.Classification)DomainObject.newInstance (context, objectId, "Classification");
                		objectInterface = objectClassification.getInterfaceName(context);
                		
                		//Get Interface of OldParent
                		com.matrixone.apps.classification.Classification oldParentClassification = (com.matrixone.apps.classification.Classification)DomainObject.newInstance (context, oldParent, "Classification");
                		oldParentInterface = oldParentClassification.getInterfaceName(context);
                		
                		//Remove Old Parent's derived reference
                		String mqlcommand = "modify interface $1 remove derived";
                		String[] methodArgs = {objectInterface};
                		MqlUtil.mqlCommand(context, mqlcommand, true, methodArgs);
        			}
        		}
        		else
        		{
            		com.matrixone.apps.classification.Classification objNewParentClassification = (com.matrixone.apps.classification.Classification)DomainObject.newInstance (context, newParent, "Classification");
            		newParentInterface = objNewParentClassification.getInterfaceName(context);
        		}
        	} 
        	else 
        	{
        		newParentInterface = ClassificationConstants.INTERFACE_CLASSIFICATION_ORPHANS;
        	}

        	// Ok, just add the new parent interface to the list here,
        	// it'll make sense later...
        	if(newParentInterface != null && !newParentInterface.equalsIgnoreCase("null") && !newParentInterface.equalsIgnoreCase(""))
        	{
        		myAttributeGroups.addElement(newParentInterface);

        		// Modify the interface: derived from all the attribute groups
        		//   PLUS the new parent interface(which got added to list, above)
        		//   but NOT the old parent interface (bye bye)
        		// Core takes care of uniquifying and all that in case some of
        		//   carried over attribute groups are already directly attached
        		//   or are already present in newParentInterface.

        		StringBuffer cmdBuffer = new StringBuffer();
        		cmdBuffer.append("modify interface $1");
        		cmdBuffer.append(" derived ");

        		String[] methodArgs = new String[myAttributeGroups.size()+1];
        		//value for $1 i.e object's interface
        		methodArgs[0]       = getInterfaceName(context);
        		//adding interfaces for derived
        		for (int i = 0; i < myAttributeGroups.size(); i++)
        		{
        			cmdBuffer.append("$").append(i+2).append(",");
        			methodArgs[i+1] =(String) myAttributeGroups.get(i);

        		}

        		String mqlcommand   = cmdBuffer.toString();
        		mqlcommand          = mqlcommand.substring(0,mqlcommand.lastIndexOf(','));

        		MqlUtil.mqlCommand(context, mqlcommand, true, methodArgs);
        		success = true;
        	}
        	
        	HashMap map = new HashMap();

        	//TO invalidate VPLM Cache
            try{
                invalidateVPLMCatalogueCache(context);
            }catch (MatrixException e){
            	e.printStackTrace();
                throw (new FrameworkException(e));
            } 
        	return map;
        } 
        catch (MatrixException e) 
        {
        	e.printStackTrace();
            throw (new FrameworkException(e));
        } 
        catch (Exception e) 
        {
        	e.printStackTrace();
        	throw (new Exception(e));
		}
    }
    
    
    /**
     * This method transfers the Classification object under a new parent, or
     * orphans it. It takes care of manipulating the interface tree as well as
     * the BO tree.
     *
     * This method is used for these operations: - Add existing subclass - Remove subclass - Move subclass
     *
     * NOTE: manipulations of the data model MUST be done via this method as
     * referential integrity is only ensured here.
     *
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following list of arguments:
     *    0 - objectId
     *    1 - newParent
     *    2 - oldParent objectId
     *            New parent for this Classification object. May be null. If
     *            null, then this Classifiction becomes orphan.
     *
     *    3 -  carryOverInheritedAttributeGroups
     *            If true, any attribute groups that this classification was
     *            previously inheriting from its parent/ancestors, that would
     *            NOT be inherited from its new parent, will become directly
     *            associated with this Classification prior to the reparenting
     *            taking effect, so as to avoid any loss of attributes on the
     *            affected classified items.
     *
     * @return true for success
     * @throws FrameworkException if the operation fails
     */
    public HashMap reparent(Context context, String[] args) throws FrameworkException,Exception 
    {
        if (args == null || args.length < 1) {
            throw (new IllegalArgumentException());
        }

        Map map = null;
        try {
            map = (Map) JPO.unpackArgs(args);
        } catch (Exception e) {
            throw (new FrameworkException(e));
        }
        String objectId = (String) map.get("objectId");
        String oldParent = (String) map.get("oldParent");
        Boolean carry = (Boolean) map.get("carryOverInheritedAttributeGroups");
        return (HashMap) reparent(context, objectId, oldParent, (String)map.get("newParent"), carry.booleanValue());
    }

    /**
     * Returns number of end items classified anywhere under the classification
     *
     * @param context the eMatrix <code>Context</code> object
     * @param ATTRIBUTE_COUNT
     * @return int the end Item Count
     * @throws FrameworkException if the operation fails
     */
    public int getRecursiveEndItemCount(Context context)
            throws FrameworkException {
        return Integer.parseInt(getAttributeValue(context, ATTRIBUTE_COUNT));
    }

    /** Returns number of end items classified anywhere under the classification
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return int the end Item Count
     * @throws FrameworkException if the operation fails
     */
    public int getRecursiveEndItemCount(Context context, String[] args)
    throws FrameworkException {

        return getRecursiveEndItemCount(context);
    }



    /**
     * The method reclassifies end items from one class to other in one step
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following list of arguments:
     *     0 - childIds the list of end items to be reclassified
     *     1 - currentParent Id
     *     2 - newParent Id
     *     3 - relationship the parent child relationship
     * @return HashMap with the reclassify details
     * @throws FrameworkException if the operation fails
     */
    public HashMap reclassify(Context context, String[] args) throws FrameworkException {
        if (args == null || args.length < 1) {
                    throw (new IllegalArgumentException());
        }

        Map map = null;
        try {
            map = (Map)JPO.unpackArgs(args);
        } catch(Exception e) {
            throw (new FrameworkException(e));
        }

        String[] childIds = (String[])map.get("childIds");
        String currentParentId = (String)map.get("currentParent");
        String newParentId = (String)map.get("newParent");
        String relationship = (String)map.get("relationship");


        StringList toAddIds = toStringList(childIds);
        StringList children = null;
        try {
            children = getClassifiedItems(context, newParentId);
        } catch(Exception ex) {
            throw new FrameworkException(ex);
        }

        toAddIds.removeAll(children);
        String[] toAddIdsArray = toStringArray(toAddIds);
        HashMap childIdsMap = toHashMap(childIds);


        HashMap argsMap = new HashMap();
        argsMap.put("objectId", newParentId);
        argsMap.put("childIds", toAddIdsArray);
        argsMap.put("relationship", relationship);

        HashMap argsMapRemove = new HashMap();
        argsMapRemove.put("parentId", currentParentId);
        argsMapRemove.put("childIds", childIdsMap);
        argsMapRemove.put("relationship", relationship);

        String strLanguageStr = context.getSession().getLanguage();
        // check to see if user has "toconnect" access on the classifiedItems being reclassified
        // if not for any one item, then abort the process with an error
        try {

            StringList selects = new StringList(2);
            selects.add("current.access[toconnect]");
            selects.add(DomainObject.SELECT_ID);
            selects.add(DomainObject.SELECT_TYPE);
            selects.add(DomainObject.SELECT_NAME);
            selects.add(DomainObject.SELECT_REVISION);
            MapList mlist = DomainObject.getInfo(context, childIds, selects);
            Iterator mitr = mlist.iterator();
            MapList noToconnectAccessObjectMapList = new MapList();
            while(mitr.hasNext())
            {
                Map m = (Map)mitr.next();
                boolean hasAccess = new Boolean((String)m.get("current.access[toconnect]")).booleanValue();
                if (!hasAccess)
                {
                    noToconnectAccessObjectMapList.add(m);
                }
            }

            if(noToconnectAccessObjectMapList.size() > 0)
            {
                Iterator itr = noToconnectAccessObjectMapList.iterator();
                String errorMsg = "";
                while (itr.hasNext())
                {
                    Map noToconnectAccessObjectMap = (Map) itr.next();
                    errorMsg += noToconnectAccessObjectMap.get(DomainObject.SELECT_TYPE) + " " + noToconnectAccessObjectMap.get(DomainObject.SELECT_NAME) + " " +noToconnectAccessObjectMap.get(DomainObject.SELECT_REVISION) + "\\n";
                }
                String selectOneObject = EnoviaResourceBundle.getProperty(context,"emxLibraryCentralStringResource",new Locale(strLanguageStr),"emxMultipleClassification.Message.ObjectsNotReclassifiedNoToConnectAccess");
                errorMsg = selectOneObject + "\\n" + errorMsg;

                throw new Exception(errorMsg);
            }

        } catch(Exception exp)
        {
            String error = exp.getMessage();
            throw new FrameworkException(error);
        }

        StringList strList = new StringList();
        if(currentParentId != null)
        {
            strList.addElement(currentParentId);
        }
        if(newParentId != null)
        {
            strList.addElement(newParentId);
        }

        HashMap returnMap = new HashMap();
        String sRelSubclass = com.matrixone.apps.library.LibraryCentralConstants.RELATIONSHIP_SUBCLASS;
        String strResult ="";
        String strId = "";
        for(int i = 0; i <strList.size(); i++) {
            strId = (String)strList.get(i);
            String strQuery     = "expand bus $1 to relationship $2 recurse to all select bus $3 dump $4";
            strResult           = MqlUtil.mqlCommand(context, strQuery, strId, sRelSubclass, "id", ",");
            returnMap.put(strId, strResult);
        }

        try {

            String lastObjectIdInAddChildren = toAddIdsArray[toAddIdsArray.length-1];
            String lastObjectIdInRemoveObjects = "1";

            if(currentParentId != null){
                lastObjectIdInRemoveObjects = (String)childIdsMap.get("childIds["+(childIdsMap.size()-1)+"]");
            }

            String[] modifiedArgs = JPO.packArgs(argsMap);
            //Start the transacation
            if(!context.isTransactionActive()) {
                context.start(true);
                MqlUtil.mqlCommand(context, "set env global $1 $2","LASTOBJECTID",lastObjectIdInAddChildren); 
                addChildren(context,modifiedArgs);
                String[] removeArgs = JPO.packArgs(argsMapRemove);
                if(currentParentId != null) {
                	MqlUtil.mqlCommand(context, "set env global $1 $2","LASTOBJECTID",lastObjectIdInRemoveObjects); 
                    removeObjects(context, removeArgs);
                }
                context.commit();
            } else {
                MqlUtil.mqlCommand(context, "set env global $1 $2","LASTOBJECTID",lastObjectIdInAddChildren); 
                addChildren(context,modifiedArgs);
                String[] removeArgs = JPO.packArgs(argsMapRemove);
                if(currentParentId != null) {
                    MqlUtil.mqlCommand(context, "set env global $1 $2","LASTOBJECTID",lastObjectIdInRemoveObjects); 
                    removeObjects(context, removeArgs);
                }
            }
            
            //End of transaction

        } catch (Exception e) {
            try {
                context.abort();
            } catch (Exception ex) {
                throw new FrameworkException(ex);
            }
            throw new FrameworkException(e);
        }
        return returnMap;
    }


    /**
     * The method gets the list of Classified Items
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following list of arguments:
     * @param objectId
     * @return StringList with the list of end items
     * @throws FrameworkException if the operation fails
     */
    public StringList getClassifiedItems(Context context, String objectId) throws Exception
    {
            StringList resultList = new StringList();
            MapList result = new MapList();
            try{
                SelectList selectStmts = new SelectList(1);
                selectStmts.addElement(DomainObject.SELECT_ID);
                DomainObject doObj = new DomainObject(objectId);
                result = (MapList)doObj.getRelatedObjects(context,com.matrixone.apps.library.LibraryCentralConstants.RELATIONSHIP_CLASSIFIED_ITEM,ClassificationConstants.QUERY_WILDCARD,selectStmts,new StringList(),false, true, (short)1, null, null);
                int iSize = result.size();

                for (int k=0;k<iSize;k++ )
                {

                    Map tempMap = (Map)result.get(k);
                    resultList.addElement((String)tempMap.get("id"));
                }

            }catch(Exception exp)
            {
                throw new Exception(exp.toString());
            }
            return resultList;
    }

    private StringList toStringList(String[] array) {
        int size = array.length;
        StringList returnList = new StringList();
        for(int k = 0; k < size; k++) {
            returnList.addElement(array[k]);
        }
        return returnList;
    }

    private String[] toStringArray(StringList list) {
        int size = list.size();
        String[] returnArray = new String[size];
        for(int k = 0; k < size; k++) {
            returnArray[k] = (String)list.get(k);
        }
        return returnArray;

    }

    private HashMap toHashMap(String[] array) {
        int size = array.length;
        HashMap map = new HashMap();
        for(int k = 0; k < size; k++) {
            map.put("childIds["+k+"]", array[k]);
        }
        return map;
    }



    //////////////// END BEAN METHODS ///////////////////

    /////////////// BEGIN METHODS TO FEED TABLES ////////////


    /**
     * Converts a StringList of attribute group names to a MapList for use in
     * table components.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param items the list of ObjectIds
     * @return MapList with the object details
     * @throws FrameworkException if the operation fails
     */
    public static MapList toMapList(Context context, StringList items)
            throws FrameworkException {
        dbg_break();
        MapList result = new MapList();
        try {
            Iterator iter = items.iterator();
            while (iter.hasNext()) {
                String element = (String) iter.next();
                HashMap hashIDMap = new HashMap();
                hashIDMap.put("id", element);
                result.add(hashIDMap);
            }
        } catch (Exception e) {
            throw new FrameworkException(e);
        }
        return result;
    }

    /**
    * Utility method to get Attribute Group data for multiple Attribute groups passed as StringList
    *
    * @param context the eMatrix <code>Context</code> object
    * @param attrGrpList Attributes Group List
    * @return MapList the Attribute Group Data
    * @throws FrameworkException if the operation fails
    */
    public static MapList getAttributeGroupsData(Context context, StringList attrGrpList) throws Exception {
        return getAttributeGroupsData(context, attrGrpList, null );
    }


    /**
     * Utility method to get Attribute Group data for multiple Attribute groups passed as StringList
     * if there is any inherited attribute group, then it will add the parameter 'disableSelection = true',
     * so that the check box for corresponding attribute group will be disabled in the table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param attrGrpList Attributes Group List
     * @param inheritedAttrGrpList inherited Attributes Group List
     * @return MapList the Attribute Group Data
     * @throws FrameworkException if the operation fails
     */
    public static MapList getAttributeGroupsData(Context context,
                                                StringList attrGrpList,
                                                StringList inheritedAttrGrpList)
    throws Exception {
        Iterator iterator       = attrGrpList.iterator();
        MapList resultMapList   = new MapList();
        while(iterator.hasNext()){
            HashMap hashMap = new HashMap();
            String attrGrpName = (String)iterator.next();
            hashMap.put("id", attrGrpName);
            if (inheritedAttrGrpList != null && inheritedAttrGrpList.contains(attrGrpName)) {
                hashMap.put("disableSelection", "true");
            }
            resultMapList.add(hashMap);
        }
        return resultMapList;
    }
    /**
     * Returns all attribute groups associated with a Classification, whether
     * inherited or directly associated, in the form of a MapList
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following list of arguments:
     *      0 -  objectId
     * @return MapList with all the Attribute Group Details
     * @throws FrameworkException if the operation fails
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public static MapList getAllAttributeGroups(Context context, String[] args)
            throws FrameworkException {
        dbg_break();
        try {
            HashMap paramMap        = (HashMap) JPO.unpackArgs(args);
            String clsId            = (String) paramMap.get("objectId");
            Classification cls      = (Classification)DomainObject.newInstance(context, clsId, CLASSIFICATION);
            StringList attrGrpList  = cls.getAttributeGroups(context, true);
            StringList direct           = cls.getAttributeGroups(context, false);
            StringList inheritedAttrGrpList = cls.getAttributeGroups(context, true);
            inheritedAttrGrpList.removeAll(direct);
            return getAttributeGroupsData (context, attrGrpList, inheritedAttrGrpList);
        } catch (Exception e) {
            throw new FrameworkException(e);
        }
    }


    /**
     * Returns attribute groups directly associated with a Classification, in
     * the form of a MapList
     *
     * @param context the eMatrix <code>Context</code> object
     * @param objectId
     * @return MapList with Attribute Groups Details
     * @throws FrameworkException if the operation fails
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public static MapList getDirectAttributeGroups(Context context, String[] args)
            throws FrameworkException {
        dbg_break();
        try {
        HashMap paramMap = (HashMap) JPO.unpackArgs(args);
        String clsId = (String) paramMap.get("objectId");
        Classification cls = (Classification)DomainObject.newInstance(context, clsId, CLASSIFICATION);
        StringList attrGrpList = cls.getAttributeGroups(context, false);
        return getAttributeGroupsData(context, attrGrpList);
        } catch (Exception e) {
            throw new FrameworkException(e);
        }
    }

    /**
     * Returns only inherited attribute groups associated with a Classification,
     * in the form of a MapList.
     *
     * Impl note: inherited AG's = all AG's - direct AG's
     *
     * @param context the eMatrix <code>Context</code> object
     * @param objectId
     * @return MapList with Attribute Groups Details
     * @throws FrameworkException if the operation fails
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public static MapList getInheritedAttributeGroups(Context context, String[] args)
            throws FrameworkException {
        dbg_break();
        try {
            HashMap paramMap = (HashMap) JPO.unpackArgs(args);
            String clsId = (String) paramMap.get("objectId");
            Classification cls = (Classification)DomainObject.newInstance(context, clsId, CLASSIFICATION);
            StringList all = cls.getAttributeGroups(context, true);
            StringList direct = cls.getAttributeGroups(context, false);
            StringList inherited = new StringList();
            all.removeAll(direct);
            return getAttributeGroupsData(context, all, all);
        } catch (Exception e) {
            throw new FrameworkException (e);
        }
    }

    /**
     * This method is used by getAttributeGroupsInheritedFrom to walk up the
     * inheritance graph, as stored in a HashMap. The structure of the HashMap
     * is map(agName) = StringList(parents of agName). This routine recursively
     * walks up, starting from clsName, looking for an ancestor that is directly
     * derived from agName. That ancestor (which actually could be clsName
     * itself) is returned.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param map
     * @param name
     * @param agName
     * @return String name of the class from which clsName inherited agName, or
     *         null if not found
     */
    private static String gagif_recurse(Context context, HashMap map, String clsName,
            String agName) {

        StringList parents = (StringList) map.get(clsName);
        if (parents == null) {
            return null;
        } else if (parents.contains(agName)) {
            return clsName;
        } else {
            Iterator iter = parents.iterator();
            while (iter.hasNext()) {
                String element = (String) iter.next();
                String ret = gagif_recurse(context, map, element, agName);
                if (ret != null) {
                    return ret;
                }
            }
            return null;
        }
    }
/**
* For debug
* @exclude
*/
    public static void dbg_break() {
        try {
            throw new java.util.zip.ZipException("Debugger breakpoint");
        } catch (Exception e) {
            ;
        }
    }

    /**
     * Given a Classification, and a MapList containing attribute group names,
     * return a Vector of corresponding path-inherited-from.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param clsId string id of the Classification
     * @param agList MapList of attribute group names
     * @return MapList containing Id and name fields
     * @throws FrameworkException if the operation fails
     */
    public static MapList getAttributeGroupInheritedFrom(Context context,
            String clsId, MapList agList) throws FrameworkException {
        dbg_break();
        try {

            Classification cls = (Classification)DomainObject.newInstance(context, clsId, CLASSIFICATION);
            String myInterfaceName = cls.getInterfaceName(context);

            // Build a HashMap of the form:
            // map(interfacename) = (StringList)parentsof(interfacename);
            HashMap derivedMap  = new HashMap();
            String cmd          =  "list interface select name derived dump $1 recordsep $2";
            String dump         = MqlUtil.mqlCommand(context,cmd , ",", "|").trim();
            StringList records  = FrameworkUtil.split(dump, "|");
            Iterator recordIter = records.iterator();
            while (recordIter.hasNext()) {
                String recordCSL = (String) recordIter.next();
                StringList record = FrameworkUtil.split(recordCSL, ",");
                String name = (String) record.get(0);
                record.remove(0);
                derivedMap.put(name, record);
            }



            dbg_break();

            // Now compute the derived-from Classification interface for each
            // requested attribute group. Stuff them in a StringList. Note that
            // these are the names of the interfaces corresponding to the
            // Classification
            // objects to be returned; they are not the objects themselves.
            StringList inheritedFromList = new StringList();
            Iterator itr = agList.iterator();
            while (itr.hasNext()) {
                HashMap agInfo = (HashMap) itr.next();
                String agName = (String) agInfo.get("id");
                String inheritedFrom = gagif_recurse(context, derivedMap,
                        myInterfaceName, agName);
                inheritedFromList.addElement(inheritedFrom);
            }
            dbg_break();

            // Next, find the Classification objects that correspond to
            // each of these computed attribute groups and put their
            // names and id's in a MapList to be returned
            String inheritedFromCSL = FrameworkUtil.join(inheritedFromList, ",");
            String cmdQuery = "temp query bus $1 $2 $3 where $4  select $5 $6 dump $7 recordsep $8";
            dump            = MqlUtil.mqlCommand(context, cmdQuery,
                                                    "Classification",
                                                    "*",
                                                    "*",
                                                    "attribute["+ATTRIBUTE_MXSYS_INTERFACE+"] matchlist '"+inheritedFromCSL+"' ','",
                                                    "id",
                                                    "attribute["+ATTRIBUTE_MXSYS_INTERFACE+"]",
                                                    ",",
                                                    "|"
                                                ).trim();
            records         = FrameworkUtil.split(dump, "|");

            // The following fills a map with records that will ultimately
            // be the result of this method. However, the order in which
            // the records are obtained, is random, so we don't build a MapList,
            // but rather a Map of Maps, for easy lookup during the subsequent
            // sorting loop.
            HashMap unsortedResult = new HashMap();
            recordIter = records.iterator();
            while (recordIter.hasNext()) {
                String recordCSL = (String) recordIter.next();
                StringList record = FrameworkUtil.split(recordCSL, ","); // T,N,R,id,mxsysInterface
                String name = (String) record.get(1);
                String id = (String) record.get(3);
                String intfName = (String) record.get(4);
                HashMap recordInfo = new HashMap();
                recordInfo.put("name", name);
                recordInfo.put("id",id);
                unsortedResult.put(intfName, recordInfo);
            }



            dbg_break();

            // Ok, now sort to obtain the final result. Phew...
            MapList result = new MapList();
            Iterator intfIter = inheritedFromList.iterator();
            int pos = 0;





            while (intfIter.hasNext()) {
                String inhClsIntf = (String) intfIter.next();
                HashMap inhClsInfo = (HashMap)unsortedResult.get(inhClsIntf);
                String inhClsId = (String)inhClsInfo.get("id");
                String id = (String)((HashMap)(agList.get(pos++))).get("id");
                inhClsInfo.put("Attribute Group", id);
                result.add(inhClsInfo);
            }


            return result;
        } catch (Exception ex) {
            throw new FrameworkException(ex.getMessage());
        }
    }

    /**
     * Gets Attribute Groups Inherited from Classification list
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following list of arguments:
     *     0 - objectList Attribute Group List
     *     1 - reportFormat
     *     2 - objectId
     @ return Vector with the list of Classifications
     * @throws FrameworkException if the operation fails
     */
    public static Vector getAttributeGroupInheritedFrom(Context context, String[] args)
    throws FrameworkException {
        dbg_break();
        Vector retVector = new Vector();
        try {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objList = (MapList) programMap.get("objectList");
            HashMap paramMap = (HashMap) programMap.get("paramList");
            boolean isprinterFriendly = false;
            if(paramMap.get("reportFormat") != null)
            {
                isprinterFriendly = true;
            }

            String clsid = (String) paramMap.get("objectId");

            MapList tempMap = getAttributeGroupInheritedFrom(context, clsid, objList);


            Iterator itr = tempMap.iterator();
            HashMap fromTempMap,agMap;
            String agName,agNameFmObjList,idOfConnCls,nameOfConnCls;
            Iterator objectListItr;
            String href = "";
            while(itr.hasNext()) {
                fromTempMap = (HashMap)itr.next();
                agName = (String)fromTempMap.get("Attribute Group");
                idOfConnCls = (String)fromTempMap.get("id");
                nameOfConnCls = (String)fromTempMap.get("name");
                objectListItr = objList.iterator();
                while(objectListItr.hasNext()) {
                    agMap = (HashMap)objectListItr.next();
                    agNameFmObjList = (String)agMap.get("id");
                    if(agNameFmObjList.equals(agName)) {
                        if(idOfConnCls.equals(clsid)){
                            href = "-";
                        } else {
                            if(!isprinterFriendly) {
                                href = "<a href=\"javascript:showModalDialog("
                                    + "'../common/emxTree.jsp?objectId=" + idOfConnCls + "',"
                                    + "'860','520');\"" + ">" + nameOfConnCls + "</a>";
                            } else {
                                href = nameOfConnCls;
                            }
                        }
                        break;
                    }
                }
                retVector.addElement(href);
            }
        } catch (Exception e) {
            throw new FrameworkException (e);
        }


        return retVector;
    }
/**
* Gets a StringList from a map list
*
* @param context the eMatrix <code>Context</code> object
* @param MapList list of Attribute Groups
* @ return StringList of Attribute Groups
* @exclude
*/
    static StringList toStringList(Context context, MapList agList) {
        StringList result = new StringList(agList.size());
        Iterator agIter = agList.iterator();
        while (agIter.hasNext()) {
            HashMap agInfo = (HashMap) agIter.next();
            String agName = (String)agInfo.get("id");
            result.addElement(agName);
        }
        return result;
    }

    /**
    * Checks if the Attribute Group is inherited for the list of Attribute Groups
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following list of arguments:
    *     0 - objectList the list of Attribute Group
    *     1 - objectId
    *@return Vector with true or false for each Attribute Group
    * @throws FrameworkException if the operation fails
    */
    public static Vector isAttributeGroupInherited(Context context, String[] args)
        throws FrameworkException {
        dbg_break();
        try {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objList = (MapList) programMap.get("objectList");
            HashMap paramMap = (HashMap) programMap.get("paramList");
            String clsid = (String) paramMap.get("objectId");
            Classification cls = (Classification)DomainObject.newInstance(context, clsid, CLASSIFICATION);
            StringList agToTestList = toStringList(context, objList);
            StringList agDirectList = cls.getAttributeGroups(context, false);
            Vector result = new Vector();


            Iterator agToTestIter = agToTestList.iterator();
            while (agToTestIter.hasNext()) {
                String element = (String) agToTestIter.next();
                if(!agDirectList.contains(element)) {
                    result.add("false");
                } else {
                    result.add("true");
                }
            }

            return result;
        } catch (Exception e) {
            throw new FrameworkException (e);
        }

    }

      /**
       * Finds the attributes from attribute group for particular classification object.
       *
       * @param context the eMatrix <code>Context</code> object
       * @param args holds the following list of arguments:
       *        0 -  strParentObjectId The id of the classification object
       * @return The list of attribute names
       * @throws Exception if the operation fails
       */
      public static Vector findAttributes(Context context, String[] args) throws Exception {

          if (args == null || args.length < 1) {
              throw (new IllegalArgumentException());
          }

          Map map = null;
          try {
              map = (Map) JPO.unpackArgs(args);
          } catch (Exception e) {
              throw (new FrameworkException(e));
          }
          String strParentObjectId = (String) map.get("strParentObjectId");
          return findAttributes(context, strParentObjectId);
      }

      /**
       * Finds the attributes from attribute group for particular classification object.
       *
       * @param context the eMatrix <code>Context</code> object
       * @param strParentObjectId The id of the classification object
       * @return The list of attribute names
       * @throws Exception if the operation fails
       */
      public static Vector findAttributes(Context context, String strParentObjectId) throws Exception
      {
    	ContextUtil.pushContext(context);
        DomainObject domainObject = new DomainObject(strParentObjectId);
        String strTypeOfTheObject     = domainObject.getType(context);
        BusinessType businessType = new BusinessType(strTypeOfTheObject, context.getVault());
        String strParentType = businessType.getParent(context);
        String  strTypeClassification =  PropertyUtil.getSchemaProperty(context, "type_Classification" );
        StringList strlistAttributeGroups = new StringList();
        if (strParentType != null && strTypeClassification!=null && strTypeClassification.equals(strParentType))
        {
         com.matrixone.apps.classification.Classification objClassification = (com.matrixone.apps.classification.Classification)DomainObject.newInstance (context, strParentObjectId, "Classification");
         strlistAttributeGroups = objClassification.getAttributeGroups(context, true);
        }//if
        ContextUtil.popContext(context);

        Vector vecAttributes = new Vector();

        // For each AG find the attributes and consolidate them into the Vector
        for (int i=0; i<strlistAttributeGroups.size(); i++)
        {
          String strAttributeGroupName = (String)strlistAttributeGroups.get(i);
          if ( strAttributeGroupName != null    &&
            !"".equals(strAttributeGroupName))
          {
            // For this attribute group find the Attributes
            com.matrixone.apps.classification.AttributeGroup attributeGroup =  com.matrixone.apps.classification.AttributeGroup.getInstance(context, strAttributeGroupName);

            StringList strlistAttributes = attributeGroup.getAttributes();
            if (strlistAttributes == null)
            {
              continue;
            }
            for (int j=0; j < strlistAttributes.size(); j++)
            {
              String strAttributeName = (String)strlistAttributes.get(j);
              if ( strAttributeName != null && !"".equals(strAttributeName) && !vecAttributes.contains(strAttributeName))
              {
                vecAttributes.add(strAttributeName);
              }
            }//for !
          }//if !
        }//for !

        return vecAttributes;
      }


      /**
       * Checks if the classified item will lose any of the attributes after removing it from the hierarchy
       *
       * @param context the eMatrix <code>Context</code> object
       * @param args holds the following list of arguments:
       *        0 -  strParentObjectId The id of the classification object
       *        1 -  vecCurrentAttributes Vector containing all the attributes of the classified item unde this classification
       *        2 - strChildId The id of the classified item object
       * @return true if the classified item will lose the attributes otherwise false
       * @throws Exception if the operation fails
       */
        public static boolean checkIfItLosesAttributeOnRemoval(Context context, String[] args)
                throws Exception {
            boolean bLoseAttribute = false;
            if (args == null || args.length < 1) {
                throw (new IllegalArgumentException());
            }

            Map map = null;
            try {
                map = (Map) JPO.unpackArgs(args);
            } catch (Exception e) {
                throw (new FrameworkException(e));
            }
            String strParentObjectId = (String) map.get("strParentObjectId");
            Vector vecCurrentAttributes = (Vector) map.get("vecCurrentAttributes");
            String strChildId = (String) map.get("strChildId");
            boolean isSearchAllSublevels = ((Boolean) map.get("isSearchAllSublevels")).booleanValue();
            bLoseAttribute = checkIfItLosesAttributeOnRemoval(context, strParentObjectId, vecCurrentAttributes, strChildId, isSearchAllSublevels);
            return bLoseAttribute;
        }

      /**
       * Checks if the classified item will lose any of the attributes after removing it from the hierarchy
       *
       * @param context the eMatrix <code>Context</code> object
       * @param strParentObjectId The id of the classification object
       * @param vecCurrentAttributes Vector containing all the attributes of the classified item unde this classification
       * @param strChildId The id of the classified item object
       * @return true if the classified item will lose the attributes otherwise false
       * @throws Exception if the operation fails
       */
      public static boolean checkIfItLosesAttributeOnRemoval(Context context,
                                                         String strParentObjectId,
                                                         Vector vecCurrentAttributes,
                                                         String strChildId, boolean isSearchAllSublevels) throws Exception
      {
        final boolean isGoingToLoseCurrentAttributes = true;

        if (!isSearchAllSublevels)
        {
          HashSet allAttrsOfAllParents = new HashSet();
          Vector vecAllParentIds = findAllParentClassifications(context, strChildId);
          vecAllParentIds.removeElement(strParentObjectId); // Remove the current parent classification from the list
          Iterator parentItr = vecAllParentIds.iterator();

          while (parentItr.hasNext()) {
            String strParentId = (String)parentItr.next();
            Vector thisParentsAttributes = findAttributes(context, strParentId);
            allAttrsOfAllParents.addAll(thisParentsAttributes);
          }
          boolean bReturn = allAttrsOfAllParents.containsAll(vecCurrentAttributes);
          return !bReturn;
        }
        else
        {
          HashSet allAttrsOfAllParentsCurentTaxonomy = new HashSet();
          Vector vecAllParentIdsInCurrentTaxonomy = findAllParentClassificationsInCurrentTaxonomy(context, strParentObjectId, strChildId);
          Iterator parentCurrentTaxonomyItr = vecAllParentIdsInCurrentTaxonomy.iterator();

          while (parentCurrentTaxonomyItr.hasNext()) {
            String strParentId = (String)parentCurrentTaxonomyItr.next();
            Vector vecAttributes = findAttributes(context, strParentId);
            if (vecAttributes != null && vecAttributes.size() > 0)
            {
              return isGoingToLoseCurrentAttributes;
            }
          }
        }
        return !isGoingToLoseCurrentAttributes;
      }

      /**
       * Finds all the parent classification for the classified item
       *
       * @param context the eMatrix <code>Context</code> object
       * @param args holds the following list of arguments:
       *        0 -  strChildId The id of the classified item object
       * @return a vector containing all the parent classification object ids
       * @throws FrameworkException if the operation fails
       */
      public static Vector findAllParentClassifications(Context context, String[] args)
              throws FrameworkException {

          if (args == null || args.length < 1) {
              throw (new IllegalArgumentException());
          }

          Map map = null;
          try {
              map = (Map) JPO.unpackArgs(args);
          } catch (Exception e) {
              throw (new FrameworkException(e));
          }
          String strChildId = (String) map.get("strChildId");
          return findAllParentClassifications(context, strChildId);
      }

      /**
       * Finds all the parent classification for the classified item
       *
       * @param context the eMatrix <code>Context</code> object
       * @param strChildId The id of the classified item object
       * @return a vector containing all the parent classification object ids
       * @throws FrameworkException if the operation fails
       */
      public static Vector findAllParentClassifications(Context context, String strChildId) throws FrameworkException
      {

        String strMQL  = "expand bus $1 to relationship $2 type $3 select bus $4 dump $5";
        String strResult = MqlUtil.mqlCommand(context, strMQL, true,
                                                strChildId,
                                                LibraryCentralConstants.RELATIONSHIP_CLASSIFIED_ITEM,
                                                "*",
                                                "id",
                                                "|"
                                              );
        Vector vecParentObjectIds = new Vector();

        if (strResult == null || "".equals(strResult))
        {
          return vecParentObjectIds;
        }

        StringTokenizer st1 = new StringTokenizer(strResult,"\n");
        while(st1.hasMoreTokens())
        {
          String strResultLine = st1.nextToken();
          if (strResultLine != null && !"".equals(strResultLine))
          {
            StringTokenizer st2 = new StringTokenizer(strResultLine, "|");
            String strParentObjectId = "";
            while(st2.hasMoreTokens())
            {
              strParentObjectId = st2.nextToken();
            }
            if ( strParentObjectId != null &&
               !"".equals(strParentObjectId) &&
               !vecParentObjectIds.contains(strParentObjectId))
            {
              vecParentObjectIds.add(strParentObjectId);
            }
          }
        }//while !

        return vecParentObjectIds;
      }

      /**
       * Finds all the parent classification for the classified item in current taxonomy
       *
       * @param context the eMatrix <code>Context</code> object
       * @param args holds the following list of arguments:
       *        0 - strParentId The id of the classified item's parent classification object
       *        1-  strChildId The id of the classified item object
       * @return a vector containing all the parent classification object ids in current taxonomy
       * @throws Exception if the operation fails
       */
        public static Vector findAllParentClassificationsInCurrentTaxonomy(Context context, String[] args)
                throws FrameworkException {

            if (args == null || args.length < 1) {
                throw (new IllegalArgumentException());
            }

            Map map = null;
            try {
                map = (Map) JPO.unpackArgs(args);
            } catch (Exception e) {
                throw (new FrameworkException(e));
            }
            String strParentId = (String) map.get("strParentId");
            String strChildId = (String) map.get("strChildId");
            return findAllParentClassificationsInCurrentTaxonomy(context, strParentId, strChildId);
        }

      /**
       * Finds all the parent classification for the classified item in current taxonomy
       *
       * @param context the eMatrix <code>Context</code> object
       * @param strParentId The id of the classified item's parent classification object
       * @param strChildId The id of the classified item object
       * @return a vector containing all the parent classification object ids in current taxonomy
       * @throws Exception if the operation fails
       */
      public static Vector findAllParentClassificationsInCurrentTaxonomy(Context context, String strParentId, String strChildId) throws FrameworkException
      {
        String strRelClassifiedItem = PropertyUtil.getSchemaProperty(context, "relationship_ClassifiedItem" );
        String strRelSubclass       = PropertyUtil.getSchemaProperty(context, "relationship_Subclass" );

        StringBuffer strMQLBuffer   = new StringBuffer();
        strMQLBuffer.append("expand bus $1");
        strMQLBuffer.append(" from relationship $2");
        strMQLBuffer.append(" recurse to all select bus $3 where $4 == $5 dump $6");
        String strResult            = MqlUtil.mqlCommand(context, strMQLBuffer.toString(), true,
                                                            strParentId,
                                                            strRelSubclass,
                                                            "id",
                                                            "from["+strRelClassifiedItem+"].to.id",
                                                            strChildId.trim(),
                                                            "|"
                                                        );

        Vector vecParentObjectIds = new Vector();
        vecParentObjectIds.add(strParentId);

        if (strResult == null || "".equals(strResult))
        {
          return vecParentObjectIds;
        }

        StringTokenizer st1 = new StringTokenizer(strResult,"\n");
        while(st1.hasMoreTokens())
        {
          String strResultLine = st1.nextToken();
          if (strResultLine != null && !"".equals(strResultLine))
          {
            StringTokenizer st2 = new StringTokenizer(strResultLine, "|");
            String strParentObjectId = "";
            while(st2.hasMoreTokens())
            {
              strParentObjectId = st2.nextToken();
            }
            if ( strParentObjectId != null &&
               !"".equals(strParentObjectId) &&
               !vecParentObjectIds.contains(strParentObjectId))
            {
              vecParentObjectIds.add(strParentObjectId);
            }
          }
        }//while !
        return vecParentObjectIds;
      }


    /**
     * This method returns true if the object losses any attributes
     * on removal of a given class from the object
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *        0 - objectId
     *        1 - selectedClassIds
     * @return true if the object is going to loose attributes
     * @throws Exception if the operation fails
     */
    public static boolean checkIfItLosesAttributeOnRemovalOfClassification(Context context,String[] args)
    throws Exception
    {

        Map map                                     = (Map) JPO.unpackArgs(args);
        String selectedClassIds                     = (String)map.get("selectedClassIds");
        StringList slSelectedClassIds               = FrameworkUtil.split(selectedClassIds,",");
        String objectId                             = (String)map.get("objectId");
        HashSet allAttrsOfSelectedClassifications   = new HashSet();
        HashSet allAttrsOfAllParents                = new HashSet();
        Vector vecAllParentIds                      = findAllParentClassifications(context, objectId);
        Iterator selectedClassificationsItr         = slSelectedClassIds.iterator();
        while (selectedClassificationsItr.hasNext())
        {
            String sSelectedClassificationID    = (String)selectedClassificationsItr.next();
            allAttrsOfSelectedClassifications.addAll(findAttributes(context,sSelectedClassificationID));
        }

        vecAllParentIds.removeAll(slSelectedClassIds); // Remove the current parent classification from the list
        Iterator parentItr              = vecAllParentIds.iterator();

        while (parentItr.hasNext())
        {
            String strParentId          = (String)parentItr.next();
            Vector thisParentsAttributes= findAttributes(context, strParentId);
            allAttrsOfAllParents.addAll(thisParentsAttributes);
        }
        return (!allAttrsOfAllParents.containsAll(allAttrsOfSelectedClassifications));
    }
    /***
     * Export the Library
     * @param context the ENOVIA <code>Context</code> object
     * @param args String array containing the Library Object Id
     * @return java.io.File contains the Exported Library information
     * @throws FrameworkException
     */

    public File exportLibrary(Context context, String args[]) throws FrameworkException{
        File gLibExportFile=null;
        Element libXML=null;
        try{
            HashMap programMap=(HashMap)JPO.unpackArgs(args);
            String objectId=(String)programMap.get("LibraryObjectId");
            String objectType=new DomainObject(objectId).getInfo(context, SELECT_TYPE);
            Map packMap=new HashMap();
            packMap.put("objectId",objectId);
            String[] constructor = { null };
            String packArgs[]=JPO.packArgs(packMap);
            MapList subClassesList = (MapList) JPO.invoke(context,
                    "emxLibraryCentralUtilBase", constructor, "getSubclasses",packArgs,MapList.class);
            com.matrixone.apps.classification.Classification classification=new com.matrixone.apps.classification.Classification();
            if(objectType.equals(TYPE_GENERAL_LIBRARY))
                libXML=classification.createTaxonomyXML(context,objectId,GENERAL_LIBRARY_ELEMENT_NAME,TYPE_GENERAL_LIBRARY);
            else if(objectType.equals(TYPE_PART_LIBRARY))
                libXML=classification.createTaxonomyXML(context, objectId, PART_LIBRARY_ELEMENT_NAME, TYPE_PART_LIBRARY);
            else if(objectType.equals(TYPE_DOCUMENT_LIBRARY))
                libXML=classification.createTaxonomyXML(context, objectId, DOCUMENT_LIBRARY_ELEMENT_NAME, TYPE_DOCUMENT_LIBRARY);
            else{
                String aliasName=FrameworkUtil.getAliasForAdmin(context, "type", objectType, true);
                if(aliasName == null || "".equals(aliasName))
                    aliasName=FrameworkUtil.getAliasForAdmin(context, "type", objectType, false);
                if(aliasName == null || "".equals(aliasName)){
                    Locale locale = MessageUtil.getLocale(context);
                    String message = MessageUtil.getMessage(context,
                            null,
                            "emxLibraryCentral.LoadAllowedTypes.SymbolicNameNotFound",
                            new String[]{i18nNow.getAdminI18NString("type", objectType, locale.getLanguage())},
                            null,
                            locale,
                    "emxLibraryCentralStringResource");
                    throw new Exception(message);
                }
                aliasName=aliasName.substring(aliasName.indexOf("_")+1, aliasName.length());
                libXML=classification.createTaxonomyXML(context, objectId, aliasName, objectType);
            }
            Iterator itr=subClassesList.iterator();
            while(itr.hasNext()){
                Map subClassMap=(Map)itr.next();
                String generalClassId=(String)subClassMap.get(SELECT_ID);
                Element generalclassXML =getAttributeGroupandSubClassesforGL(context, generalClassId);
                libXML.addContent(generalclassXML);
            }
            Calendar calen=Calendar.getInstance();
            int date=calen.get(Calendar.DAY_OF_MONTH);
            int month=calen.get(Calendar.MONTH)+1;
            int yr=calen.get(Calendar.YEAR);
			
			// Changes added by PSA11 start(IR-478456-3DEXPERIENCER2017x).
            String libraryNameForFile = new DomainObject(objectId).getInfo(context,SELECT_NAME);
            String modifiedXMLName = null;
            if(libraryNameForFile.contains(" ")){
            	modifiedXMLName = libraryNameForFile.replaceAll(" ", "_");
            } else {
            	modifiedXMLName = libraryNameForFile;
            }
            // Changes added by PSA11 end.	

            String fileName = modifiedXMLName + "_" + String.valueOf(date)+String.valueOf(month)+String.valueOf(yr) + ".xml";
            //to replace characters which cant be included in the name of the file
            /*if(fileName.indexOf("+")!= -1 || fileName.indexOf(";") != -1 || fileName.indexOf("/")!= -1){
            	fileName=fileName.replace("+", "_");
            	fileName=fileName.replace(";", "_");
            	fileName=fileName.replace("/", "_");
            }*/
            Pattern pat=Pattern.compile("^\\W");
            Matcher m=pat.matcher(fileName);
            //System.out.println("New File Name: matcher"+m.find());

            /*if(fileName.indexOf("<")!= -1 || fileName.indexOf(">")!= -1){
            	fileName=fileName.replaceAll("<", "_");
            	fileName=fileName.replaceAll(">", "_");
            }*/
            if(m.find())
            		fileName=Calendar.getInstance().getTimeInMillis()+"_"+String.valueOf(date)+String.valueOf(month)+String.valueOf(yr) + ".xml";
            //System.out.println("New File Name: "+fileName);
            // Create a workspace for the export file.
            String workspace = context.createWorkspace();
            // Open a file for write access.
            gLibExportFile = new java.io.File(workspace, fileName);
            FileWriter fos=new FileWriter(gLibExportFile);
            XMLOutputter compactOutputter=MxXMLUtils.getOutputter(true);
            //System.out.println("compactOutputter Encoding GL"+compactOutputter.getFormat().getEncoding());
            String GLXMLStr = compactOutputter.outputString(libXML);
            //compactOutputter.output(libXML, fos);
            //fos.write(new String(GLXMLStr.getBytes("UTF-8")));
            fos.write(GLXMLStr);
            fos.close();

        }catch(Exception err){
            err.printStackTrace();
        }
        return gLibExportFile;
    }
    /***
     * Retrieves the Sub Classes/Part Families and its Attribute Groups
     * @param context the ENOVIA <code>Context</code> object
     * @param classId, objectId of Classification Type
     * @return com.matrixone.apps.common.util.CommonElement
     * @throws FrameworkException
     */
    public Element getAttributeGroupandSubClassesforGL(Context context,String classId) throws FrameworkException{
        Element classXML=null;
        try{
            //com.matrixone.apps.classification.Classification classification=(com.matrixone.apps.classification.Classification)DomainObject.newInstance(context, classId,CLASSIFICATION);
            String typeName=new DomainObject(classId).getInfo(context, SELECT_TYPE);
            com.matrixone.apps.classification.Classification classification=new com.matrixone.apps.classification.Classification();
            if(typeName.equals(TYPE_GENERAL_CLASS))
                classXML=classification.createTaxonomyXML(context, classId,GENERAL_CLASS_ELEMENT_NAME,TYPE_GENERAL_CLASS);
            else if(typeName.equals(TYPE_PART_FAMILY))
                classXML=classification.createTaxonomyXML(context, classId,PART_FAMILY_ELEMENT_NAME,TYPE_PART_FAMILY);
            else if(typeName.equals(TYPE_DOCUMENT_FAMILY))
                classXML=classification.createTaxonomyXML(context, classId,DOCUMENT_FAMILY_ELEMENT_NAME,TYPE_DOCUMENT_FAMILY);
            else{
                String aliasName=FrameworkUtil.getAliasForAdmin(context, "type", typeName, true);
                if(aliasName == null || "".equals(aliasName))
                    aliasName=FrameworkUtil.getAliasForAdmin(context, "type", typeName, false);
                if(aliasName == null || "".equals(aliasName)){
                    Locale locale = MessageUtil.getLocale(context);
                    String message = MessageUtil.getMessage(context,
                            null,
                            "emxLibraryCentral.LoadAllowedTypes.SymbolicNameNotFound",
                            new String[]{i18nNow.getAdminI18NString("type", typeName, locale.getLanguage())},
                            null,
                            locale,
                    "emxLibraryCentralStringResource");
                    throw new Exception(message);
                }
                aliasName=aliasName.substring(aliasName.indexOf("_")+1, aliasName.length());
                classXML=classification.createTaxonomyXML(context, classId,aliasName,typeName);
            }

            Map packMap=new HashMap();
            packMap.put("objectId",classId);
            String[] constructor = { null };
            String packArgs[]=JPO.packArgs(packMap);
            MapList subClassesList = (MapList) JPO.invoke(context,
                    "emxLibraryCentralUtilBase", constructor, "getSubclasses",packArgs,MapList.class);
            Iterator itr=subClassesList.iterator();
            while(itr.hasNext()){
                Map subClassMap=(Map)itr.next();
                String generalClassId=(String)subClassMap.get(SELECT_ID);
                Element subClassesXML=getAttributeGroupandSubClassesforGL(context, generalClassId);
                classXML.addContent(subClassesXML);
            }

        }catch(Exception err){
            err.printStackTrace();
        }
        return classXML;
    }
    /** Invalidates VPLM Cache, API provided by VPLM Catalog Team
	 * @param context
	 * @throws Exception
	 * @throws MatrixException
	 */
	private void invalidateVPLMCatalogueCache(Context context)
			throws FrameworkException {
		try{
			String mqlString="list program $1";
			String output=MqlUtil.mqlCommand(context, mqlString,"emxPLMDictionaryProgram");
			if(UIUtil.isNotNullAndNotEmpty(output)){
	            Map argsHash = new HashMap();
	            String[] packArgs = JPO.packArgs(argsHash);
				JPO.invoke(context, "emxPLMDictionaryProgram",null,"invalidateCache",packArgs,Integer.class);
			}
        }catch(Exception e){
        	try {
				throw (new MatrixException (e));
			} catch (MatrixException e1) {
				throw new FrameworkException(e1);
			}
        }
	}
	
	/**
     * Checks whether object is VPLM Controlled
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following arguments:
     *       0 - from object id, 1 - to object id for subclass relationship
     * @return int 0 or 1 for Success or failure
	 * @throws Exception 
     */
	public int isVPMControlledLBCObject(Context context, String[] args) throws Exception
    {

        if(args.length > 0)
        {
            String strToObjectId = args[1];
            String strFromObjectId = args[0];
            try{
	            String selectQuery1 = "from[VPLMInteg-VPLMProjection].to.attribute[PLMReference.V_isVPLMControlled].value";
	            String selectQuery2 = "name";
	            String mqlCmd = "print bus $1 select $2 $3 dump $4";
	            String result = MqlUtil.mqlCommand(context, mqlCmd, strFromObjectId, selectQuery1, selectQuery2, "|");
	            String[] strCheck = result.split("\\|",2);
	            if((strCheck.length == 2) && (strCheck[0].equalsIgnoreCase("TRUE")))
	            {
	            	String errorMsg = EnoviaResourceBundle.getProperty(context,"emxLibraryCentralStringResource",new Locale(context.getSession().getLanguage()),"emxLibraryCentral.ErrorMsg.VPLMControlled");
	            	String errorMsgfinal = strCheck[1] + " " + errorMsg;
	            	FrameworkException newException = new FrameworkException(errorMsgfinal);
	            	throw newException;
	            }
            }catch(Exception e)
            {
            	throw e;
            }
        }
        return 0;
    }

	/**
     * Checks whether user is Approver for Document Library or Document Family
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following arguments:
     *       0 - object id
     * @return int 0 or 1 for Success or failure
	 * @throws Exception 
     */

    public int checkForApproval(Context context, String[] args) throws Exception
    {

        if(args.length > 0)
        {
            String strObjectId = args[0];
            try{
            	String attribute = PropertyUtil.getSchemaProperty(context, "attribute_Approver");
	            String selectQuery1 = "attribute["+attribute+"].value";
	            String mqlCmd = "print bus $1 select $2 dump $3";
	            String result = MqlUtil.mqlCommand(context, mqlCmd, strObjectId, selectQuery1, "|");
	            String loggedInUser = context.getUser();
	            String superuserName = PropertyUtil.getSchemaProperty(context, "person_UserAgent");
	            if(!(loggedInUser.equals(result) || loggedInUser.equals(superuserName)))
	            {
	            	String errorMsg = EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",new Locale(context.getSession().getLanguage()),"emxFramework.Alert.CannotApproveRejectUnassignedTasks");
	            	FrameworkException newException = new FrameworkException("CustomMessage"+errorMsg);
	            	throw newException;
	            	//return 1;
	            }
            }catch(Exception e)
            {
            	throw e;
            }
        }
        else
        {
        	return 1;
        }
        return 0;
    }

}
