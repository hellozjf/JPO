/*
 ** emxQuantityRuleBase
 **
 **Copyright (c) 1993-2016 Dassault Systemes.
 ** All Rights Reserved.
 **This program contains proprietary and trade secret information of
 **Dassault Systemes.
 ** Copyright notice is precautionary only and does not evidence any actual
 **or intended publication of such program
 */

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

import com.matrixone.apps.configuration.ConfigurationConstants;
import com.matrixone.apps.configuration.RuleProcess;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.i18nNow;

/**
 * This JPO class has some methods pertaining to Quantity Rule.
 * 
 * @author 3D PLM
 * @version ProductCentral BX-3 -Copyright (c) 1993-2016 Dassault Systemes.
 */
public class emxQuantityRuleBase_mxJPO extends emxDomainObject_mxJPO {
    /** A string constant with the value *. */
    protected static final String SYMB_WILD = "*";
    /** A string constant with the value vaultOption. */
    protected static final String VAULT_OPTION = "vaultOption";
    /** A string constant with the value "null". */
    protected static final String SYMB_NULL = "null";
    /** A string constant with the value VaultDisplay. */
    protected static final String QUERY_LIMIT = "queryLimit";
    /** A string constant with the value objectId. */
    public static final String OBJECT_ID = "objectId";
    /** A string constant with the value Default. */
    protected static final String DEFAULT = "Default";
    /** A string constant with the value All. */
    /** A string constant with the value (. */
    protected static final String SYMB_OPEN_PARAN = "(";
    /** A string constant with the value ). */
    protected static final String SYMB_CLOSE_PARAN = ")";
    /** A string constant with the value ~~. */
    protected static final String SYMB_MATCH = " ~~ ";  // Short term fix for Bug #243366, was " ~~ "
    /** A string constant with the value Release. */
    protected static final String STATE_RELEASE = "Release";
    /** A string constant with the value last. */
    protected static final String REVISION_LAST = "last";
    protected static final String ALL = "All";
    /**
     * Alias used for comparision operator.
     */
     protected static final String EQUALS = " == ";
    /**
     * Alias used for Blank Space.
     */
     protected static final String SPACE = " ";
    /**
     * Alias used for Double Quote.
     */
     protected static final String DOUBLE_QUOTE = "\"";
     /**
     * Alias used for And operator.
     */
     protected static final String AMPERSAND = " && ";
     /**
     * Alias used for single quote operator.
     */
     protected static final String SINGLE_QUOTE = "'";
     /**
     * Alias used for comma operator.
     */
     protected static final String COMMA = ",";
     /**
     * Alias used for open brace.
     */
     protected static final String OPEN_BRACE = "[";
     /**
     * Alias used for cloase brace.
     */
     protected static final String CLOSE_BRACE = "]";
     /**
     * Alias used for dot.
     */
     protected static final String DOT = ".";
     /**
     * Alias used for to.
     */
     protected static final String TO = "to";
     /**
     * Alias used for from.
     */
     protected static final String FROM = "from";
     /**
      * Alias used for from.
      */
      protected static final String AND = " and ";
     /**
     * Alias used for false.
     */
     protected static final String FALSE = "False";
     
     private final static String STR_ATTRIBUTE = "attribute";
    
     // Added by Infosys for Bug # 311643 Date 09 Nov, 2005
     private final static String RELATIONSHIP_NAME = "relationship";
     /**
      * Alias used for OR operator.
      */
     protected static final String OR = "OR";

     /**
      * Alias used for NOT operator.
      */
     protected static final String NOT = "NOT";


    /**
     * Default Constructor.
     * 
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds no arguments
     * @return noting,constructor
     * @throws Exception
     *             if the operation fails
     * @since Product Central BX-3
     */
    public emxQuantityRuleBase_mxJPO(Context context, String[] args)
            throws Exception {
        super(context, args);
    }

    /**
     * Main entry point.
     * 
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds no arguments
     * @return an integer status code (0 = success)
     * @throws Exception
     *             if the operation fails
     * @since Product Central 10-0-0-0
     */
    public int mxMain(Context context, String[] args) throws Exception {
        if (!context.isConnected())
            throw new Exception("Not supported on desktop client");
        return 0;
    }

    /**
     * Method call to get all the Quantity Rules in Connected to the Feature.
     * 
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args -
     *            Holds the following arguments 0 - HashMap containing the
     *            following arguments
     * @return Object - MapList containing the id of Quantity Rule objects
     * @throws Exception
     *             if the operation fails
     * @since Product Central BX-3
     * @grade 0
     */

    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getAllQuantityRules(Context context, String[] args)
            throws Exception {
        MapList relBusObjPageList = new MapList();
        // Create Select list items
        StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
         // Modified For Bug No 374370  
        objectSelects.add(DomainConstants.SELECT_TYPE);
        objectSelects.addElement("attribute["+ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION+"]");
        StringList RelationSelects = new StringList("id[connection]");
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        // Retrive the Object ID of the feature
        String strObjectId = (String) programMap.get(OBJECT_ID);
        
        // Retrive the Object ID of the feature
        String strRelationship = ConfigurationConstants.RELATIONSHIP_QUANTITY_RULE;

        // set relationship pattern
        String strType = ConfigurationConstants.TYPE_QUANTITY_RULE;
        DomainObject dom = new DomainObject(strObjectId);

        relBusObjPageList = dom.getRelatedObjects(context, strRelationship,
                strType, objectSelects, RelationSelects, false, true, (short) 0, null,
                null, 0);
        
        return relBusObjPageList;

    }

    public Vector getRuleStatus(Context context,String args[])throws Exception{
     
        HashMap progrmaMap = (HashMap)JPO.unpackArgs(args);
        MapList objList = (MapList)progrmaMap.get("objectList");
        Vector columnVals = new Vector(objList.size());
        Iterator i = objList.iterator();
        while (i.hasNext())
        {
            Map m = (Map) i.next();

            String strRuleId = (String)m.get(SELECT_ID);
            DomainObject ObjFeature = new DomainObject(strRuleId);
            String strQurey = RELATIONSHIP_NAME+OPEN_BRACE+ConfigurationConstants.RELATIONSHIP_QUANTITY_RULE+CLOSE_BRACE+DOT+STR_ATTRIBUTE+OPEN_BRACE+ConfigurationConstants.ATTRIBUTE_RULE_STATUS+CLOSE_BRACE;

            
            String i18Value=i18nNow.getRangeI18NString(
            		ConfigurationConstants.ATTRIBUTE_RULE_STATUS,
                        (String) ObjFeature.getInfo(context,strQurey), context.getSession().getLanguage());
            
            columnVals.add(i18Value);
           
        }
        
        return columnVals;
        
    }
    /**
     * Method call to get the Edit Link for Quantity Rules Summary Page for the
     * feature
     * 
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args -
     *            Holds the following arguments 0 - HashMap containing the
     *            following arguments: 1:Relationship Id. 2:Feature Id.
     * @return Object - MapList containing the context name and ids.
     * @throws Exception
     *             if the operation fails
     * @since Product Central BX-3
     * @grade 0
     */
    public Vector getEditColumnDisplay(Context context,
            String[] args)throws Exception{
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        MapList objList = (MapList)programMap.get("objectList");

        HashMap paramList = (HashMap)programMap.get("paramList");
        
        String parentId = (String)paramList.get("objectId");
        String relId = (String)paramList.get("relId");
        String strProductID = (String)paramList.get("productID");
        Vector columnVals = new Vector(objList.size());
        Iterator i = objList.iterator();
        boolean isPrinterFriendly = false;
        String printerFriendly = (String)paramList.get("reportFormat");
        if ( printerFriendly != null )
        {
            isPrinterFriendly = true;
        }
          while (i.hasNext())
            {
                Map m = (Map) i.next();

                String id = (String)m.get(SELECT_ID);
              
                if(!isPrinterFriendly)
                {
                    columnVals.addElement("<a href=\"javascript:emxTableColumnLinkClick('../configuration/CreateQuantityRule.jsp?mode=edit%26emxSuiteDirectory=configuration%26relId="+relId+"%26suiteKey=configuration%26parentOID="+parentId+"%26objectId="+id+"%26productID="+strProductID+"', '780', '770', 'true', 'popup', '')\"><img border=\"0\" src=\"images/iconActionEdit.gif\" alt=\"\"/></a>");
             
                  }
                  else
                  {
                      columnVals.addElement("<img border=\"0\" src=\"images/iconNewWindow.gif\" alt=\"\"/>");
                  }
            }
        return columnVals;
        
    }
  
	



/**
 * Wrapper method to display the Right Expression value in In Quantity Rule Properties Page  
 * @param context
 * @param args
 * @return
 * @throws Exception
 * @since R212
 */

public List getRightExpressionToDisplayInQuantityRulePropertiesPage(Context context, String[] args) throws Exception {
    
	Map programMap2 = (HashMap) JPO.unpackArgs(args);
    Map relBusObjPageList = (HashMap) programMap2.get("paramMap");
   String strObjectId = (String)relBusObjPageList.get("objectId");
   DomainObject dom = new DomainObject(strObjectId);
   
   //getting the BCR attributes through the bean ..variables for fetching Expression
   Map mapTemp                 = new HashMap();
   MapList objectList          = new MapList();
   Map paramList               = new HashMap();
   HashMap programMap          = new HashMap();
   String[] arrJPOArguments    = new String[1];

   mapTemp.put("id", strObjectId);
   mapTemp.put("attribute["+ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION+"]",dom.getInfo(context,"attribute["+ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION+"]"));
   objectList.add(mapTemp);
   paramList.put("intermediate", "true");
   programMap.put("objectList", objectList);
   programMap.put("paramList", paramList);
   arrJPOArguments= JPO.packArgs(programMap);
	
	
	RuleProcess ruleProcess = new RuleProcess();
	return ruleProcess.getExpressionForRuleDisplay(context, arrJPOArguments,ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION);

}

/**
 * Wrapper method to display the Right Expression value in Quantity Rule List Page  
 * Called from Table Column settings
 * @param context
 * @param args
 * @return
 * @throws Exception
 * @since R212
 */
public List getRightExpressionToDisplayInQuantityRuleListPage(Context context, String[] args) throws Exception {
    RuleProcess ruleProcess = new RuleProcess();
	return ruleProcess.getExpressionForRuleDisplay(context, args,ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION);

}
}
