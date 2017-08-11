/*
**   emxKeyUtil.java
**
**   Copyright (c) 1992-2016 Dassault Systemes.
**   All Rights Reserved.
**   This program contains proprietary and trade secret information of MatrixOne,
**   Inc.  Copyright notice is precautionary only
**   and does not evidence any actual or intended publication of such program
**
*/

import java.util.*;
import matrix.db.*;
import matrix.util.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.domain.*;
import com.matrixone.apps.common.util.*;
import com.matrixone.apps.common.Person;

public class emxKeyUtil_mxJPO
{
    private static int keySize = 20;

  /**
    * The default constructor.
    * @since AEF 9.5.2.0
    * @grade 0
    */
    public emxKeyUtil_mxJPO (Context context, String[] args) throws Exception
    {
    }

    /**
    * The default constructor.
    * @since AEF 9.5.2.0
    * @grade 0
    */
    public emxKeyUtil_mxJPO () throws Exception
    {
    }

    /**
     * This method is used to generate a unique key for Company
     * @param context the eMatrix <code>Context</code> object
     * @throws Exception if operation fails.
     * @since Common 10.0.0.0
     * @grade 0
     */
    private static synchronized String generateKey(Context context)
                        throws Exception
    {
        String uniqueNumber = (new Long(new Date().getTime())).toString();
        while( uniqueNumber.length() < keySize )
        {
            uniqueNumber = "0" + uniqueNumber;
        }
        return uniqueNumber;
    }


    /**
     * generate and set Primary key on Object Creation.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - attribute alias Name string ex:attribute_PrimaryKey
     * @returns nothing
     * @throws Exception if the operation fails
     * @since Common 10.0.0.0
     */
    public static void generateAndSetKey(Context context, String[] args) throws Exception
    {
        emxUtil_mxJPO utilityClass = new emxUtil_mxJPO(context, null);
        String arguments[] = new String[1];
        arguments[0]  = args[0];
        ArrayList adminNames = utilityClass.getAdminNameFromProperties(context, arguments);
        String attributeName = (String)adminNames.get(0);

        arguments = new String[4];
        arguments[0] = "get env TYPE";
        arguments[1] = "get env NAME";
        arguments[2] = "get env REVISION";
        ArrayList cmdResults = utilityClass.executeMQLCommands(context, arguments);
        String sType = (String)cmdResults.get(0);
        String sName = (String)cmdResults.get(1);
        String sRev = (String)cmdResults.get(2);
        arguments = new String[1];
        String key = generateKey(context);
        arguments[0] = "modify bus \"" + sType + "\" \"" + sName + "\" \"" + sRev + "\" \""+ attributeName +"\" \""+ key +"\"";
        utilityClass.executeMQLCommands(context, arguments);
    }

    /**
     * this method copy the primary key attribute from FromObject to ToObject
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - fromObjectId String ex:${FROMOBJECTID}
     *        1 - toObjectId String ex:${TOOBJECTID}
     * @returns nothing
     * @throws Exception if the operation fails
     * @since Common 10.0.0.0
     */
    public static void setToKey(Context context, String[] args) throws Exception
    {
        String fromObjectId = args[0];
        String toObjectId = args[1];
        String[] oids = new String[1];
        oids[0] = fromObjectId;
        emxDomainObject_mxJPO fromObject = new emxDomainObject_mxJPO(context,oids);
        oids[0] = toObjectId;
        emxDomainObject_mxJPO toObject = new emxDomainObject_mxJPO(context,oids);
        String key = fromObject.getInfo(context,DomainConstants.SELECT_PRIMARY_KEY);
        toObject.setAttributeValue(context,DomainConstants.ATTRIBUTE_PRIMARY_KEY,key);
    }

    /**
     * this method copy the primary key attribute from ToObject to FromObject
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - fromObjectId String ex:${FROMOBJECTID}
     *        1 - toObjectId String ex:${TOOBJECTID}
     * @returns nothing
     * @throws Exception if the operation fails
     * @since Common 10.0.0.0
     */
    public static void setFromKey(Context context, String[] args) throws Exception
    {
        String fromObjectId = args[0];
        String toObjectId = args[1];
        String[] oids = new String[1];
        oids[0] = fromObjectId;
        emxDomainObject_mxJPO fromObject = new emxDomainObject_mxJPO(context,oids);
        oids[0] = toObjectId;
        emxDomainObject_mxJPO toObject = new emxDomainObject_mxJPO(context,oids);
        String key = toObject.getInfo(context,DomainConstants.SELECT_PRIMARY_KEY);
        fromObject.setAttributeValue(context,DomainConstants.ATTRIBUTE_PRIMARY_KEY,key);
    }

    /**
     * this method copy the primary key attribute from FromObject to
     *  secondary Keys attribute of ToObject
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - fromObjectId String ex:${FROMOBJECTID}
     *        1 - toObjectId String ex:${TOOBJECTID}
     * @returns nothing
     * @throws Exception if the operation fails
     * @since Common 10.0.0.0
     */
    public static void setFromSecondaryKey(Context context, String[] args) throws Exception
    {
        String fromObjectId = args[0];
        String toObjectId = args[1];
        String[] oids = new String[1];
        oids[0] = fromObjectId;
        emxDomainObject_mxJPO fromObject = new emxDomainObject_mxJPO(context,oids);
        oids[0] = toObjectId;
        emxDomainObject_mxJPO toObject = new emxDomainObject_mxJPO(context,oids);
        String key = toObject.getInfo(context,DomainConstants.SELECT_PRIMARY_KEY);
        StringList keys = new StringList(1);
        keys.addElement(key);
        AttributeUtil.setAttributeList(context, ((DomainObject)fromObject), DomainConstants.ATTRIBUTE_SECONDARY_KEYS, keys, true, null);
    }

    /**
     * this method copy the primary key attribute of ToObject to
     *  secondary Keys attribute of FromObject
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - fromObjectId String ex:${FROMOBJECTID}
     *        1 - toObjectId String ex:${TOOBJECTID}
     * @returns nothing
     * @throws Exception if the operation fails
     * @since Common 10.0.0.0
     */
    public static void setToSecondaryKey(Context context, String[] args) throws Exception
    {

        String fromObjectId = args[0];
        String toObjectId = args[1];
        String[] oids = new String[1];
        oids[0] = fromObjectId;
        emxDomainObject_mxJPO fromObject = new emxDomainObject_mxJPO(context,oids);
        oids[0] = toObjectId;
        emxDomainObject_mxJPO toObject = new emxDomainObject_mxJPO(context,oids);
        String key = fromObject.getInfo(context,DomainConstants.SELECT_PRIMARY_KEY);
        StringList keys = new StringList(1);
        keys.addElement(key);
        AttributeUtil.setAttributeList(context, ((DomainObject)toObject), DomainConstants.ATTRIBUTE_SECONDARY_KEYS, keys, true, null);
    }
    /**
     * this method copy the primary key attribute of ToObject to
     *  secondary Keys attribute of FromObject
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - fromObjectId String ex:${FROMOBJECTID}
     *        1 - toObjectId String ex:${TOOBJECTID}
     * @returns nothing
     * @throws Exception if the operation fails
     * @since Common 10.0.0.0
     */
    public static void setSecondaryKeys(Context context, String[] args) throws Exception
    {

        String fromObjectId = args[0];
        String toObjectId = args[1];
        String[] oids = new String[1];
        oids[0] = fromObjectId;
        emxDomainObject_mxJPO fromObject = new emxDomainObject_mxJPO(context,oids);
        oids[0] = toObjectId;
        emxDomainObject_mxJPO toObject = new emxDomainObject_mxJPO(context,oids);
        String fromKey = fromObject.getInfo(context,DomainConstants.SELECT_PRIMARY_KEY);
        String toKey = toObject.getInfo(context,DomainConstants.SELECT_PRIMARY_KEY);

        StringList keys = new StringList(1);
        keys.addElement(fromKey);
        AttributeUtil.setAttributeList(context, ((DomainObject)toObject), DomainConstants.ATTRIBUTE_SECONDARY_KEYS, keys, true, null);
        keys = new StringList(1);
        keys.addElement(toKey);
        AttributeUtil.setAttributeList(context, ((DomainObject)fromObject), DomainConstants.ATTRIBUTE_SECONDARY_KEYS, keys, true, null);
    }

    /**
     * this method remove the primary key attribute of FromObject from
     *  secondary Keys attribute of ToObject
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - fromObjectId String ex:${FROMOBJECTID}
     *        1 - toObjectId String ex:${TOOBJECTID}
     * @returns nothing
     * @throws Exception if the operation fails
     * @since Common 10.0.0.0
     */
    public static void removeFromSecondaryKey(Context context, String[] args) throws Exception
    {
        String fromObjectId = args[0];
        String toObjectId = args[1];
        String[] oids = new String[1];
        oids[0] = fromObjectId;
        emxDomainObject_mxJPO fromObject = new emxDomainObject_mxJPO(context,oids);
        oids[0] = toObjectId;
        emxDomainObject_mxJPO toObject = new emxDomainObject_mxJPO(context,oids);
        String key = toObject.getInfo(context,DomainConstants.SELECT_PRIMARY_KEY);
        StringList keys = new StringList(1);
        keys.addElement(key);
        AttributeUtil.removeAttributeList(context, ((DomainObject)fromObject), DomainConstants.ATTRIBUTE_SECONDARY_KEYS, keys, null);
    }

    /**
     * this method remove the primary key attribute of ToObject from
     *  secondary Keys attribute of FromObject
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - fromObjectId String ex:${FROMOBJECTID}
     *        1 - toObjectId String ex:${TOOBJECTID}
     * @returns nothing
     * @throws Exception if the operation fails
     * @since Common 10.0.0.0
     */
    public static void removeToSecondaryKey(Context context, String[] args) throws Exception
    {
        String fromObjectId = args[0];
        String toObjectId = args[1];
        String[] oids = new String[1];
        oids[0] = fromObjectId;
        emxDomainObject_mxJPO fromObject = new emxDomainObject_mxJPO(context,oids);
        oids[0] = toObjectId;
        emxDomainObject_mxJPO toObject = new emxDomainObject_mxJPO(context,oids);
        String key = fromObject.getInfo(context,DomainConstants.SELECT_PRIMARY_KEY);
        StringList keys = new StringList(1);
        keys.addElement(key);
        AttributeUtil.removeAttributeList(context, ((DomainObject)toObject), DomainConstants.ATTRIBUTE_SECONDARY_KEYS, keys,null);
    }

    /**
     * this method remove the primary key attribute of ToObject from
     *  secondary Keys attribute of FromObject
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - fromObjectId String ex:${FROMOBJECTID}
     *        1 - toObjectId String ex:${TOOBJECTID}
     * @returns nothing
     * @throws Exception if the operation fails
     * @since Common 10.0.0.0
     */
    public static void removeSecondaryKeys(Context context, String[] args) throws Exception
    {
        String fromObjectId = args[0];
        String toObjectId = args[1];
        String[] oids = new String[1];
        oids[0] = fromObjectId;
        emxDomainObject_mxJPO fromObject = new emxDomainObject_mxJPO(context,oids);
        oids[0] = toObjectId;
        emxDomainObject_mxJPO toObject = new emxDomainObject_mxJPO(context,oids);
        String fromKey = fromObject.getInfo(context,DomainConstants.SELECT_PRIMARY_KEY);
        String toKey = toObject.getInfo(context,DomainConstants.SELECT_PRIMARY_KEY);
        StringList keys = new StringList(1);
        keys.addElement(fromKey);
        AttributeUtil.removeAttributeList(context, ((DomainObject)toObject), DomainConstants.ATTRIBUTE_SECONDARY_KEYS, keys,null);
        keys.addElement(toKey);
        AttributeUtil.removeAttributeList(context, ((DomainObject)fromObject), DomainConstants.ATTRIBUTE_SECONDARY_KEYS, keys,null);
    }

    /**
     * this method set the primary key attribute of creating object
     * from
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - fromObjectId String ex:${FROMOBJECTID}
     *        1 - toObjectId String ex:${TOOBJECTID}
     * @returns nothing
     * @throws Exception if the operation fails
     * @since Common 10.0.0.0
     */
    public static void setCompanyKeyFromRPE(Context context, String[] args) throws Exception
    {
        emxUtil_mxJPO utilityClass = new emxUtil_mxJPO(context, null);
        String arguments[] = new String[1];
        arguments[0]  = args[0];
        ArrayList adminNames = utilityClass.getAdminNameFromProperties(context, arguments);
        String attributeName = (String)adminNames.get(0);

        arguments = new String[4];
        arguments[0] = "get env TYPE";
        arguments[1] = "get env NAME";
        arguments[2] = "get env REVISION";
        ArrayList cmdResults = utilityClass.executeMQLCommands(context, arguments);
        String sType = (String)cmdResults.get(0);
        String sName = (String)cmdResults.get(1);
        String sRev = (String)cmdResults.get(2);
        arguments = new String[1];
        String key = PropertyUtil.getGlobalRPEValue(context, Person.RPE_COMPANY_KEY);
        arguments[0] = "modify bus \"" + sType + "\" \"" + sName + "\" \"" + sRev + "\" \""+ attributeName +"\" \""+ key +"\"";
        utilityClass.executeMQLCommands(context, arguments);
    }

    /**
     * This method set the primary key of the Company as Admin property of person
     * on connect with employee relationship
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - companyId String
     *        1 - personId String
     * @returns nothing
     * @throws Exception if the operation fails
     * @since Common 10.0.0.0
     */
    public static void setCompanyKey(Context context, String[] args) throws Exception
    {
        String companyId = args[0];
        String personId = args[1];
        String[] oids = new String[1];
        oids[0] = companyId;
        emxDomainObject_mxJPO companyObject = new emxDomainObject_mxJPO(context,oids);
        oids[0] = personId;
        emxDomainObject_mxJPO personObject = new emxDomainObject_mxJPO(context,oids);
        String key = companyObject.getInfo(context,DomainConstants.SELECT_PRIMARY_KEY);
        String user = personObject.getInfo(context, "name");
        PropertyUtil.setAdminProperty(context, Person.personAdminType, user, Person.COMPANY_KEY_ADMIN_PROPERTY, key);
    }

}
