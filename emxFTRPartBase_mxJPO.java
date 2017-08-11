/*
**emxFTRPartBase
**Copyright (c) 1993-2016 Dassault Systemes.
**All Rights Reserved.
**This program contains proprietary and trade secret information of
**Dassault Systemes.
**Copyright notice is precautionary only and does not evidence any actual
**or intended publication of such program
*/

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

import com.matrixone.apps.configuration.ConfigurationConstants;
import com.matrixone.apps.configuration.ConfigurationUtil;
import com.matrixone.apps.configuration.LogicalFeature;
import com.matrixone.apps.configuration.PartFamily;
import com.matrixone.apps.configuration.ProductVariant;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.Job;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkLicenseUtil;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.productline.ProductLineCommon;
import com.matrixone.apps.productline.ProductLineConstants;

public class emxFTRPartBase_mxJPO extends emxPLCPart_mxJPO
{


 /** A string constant with the value COMMA:",". */
public static final String STR_COMMA = ",";
protected static final String PART_ACTIVE_STATUS = "emxProduct.Part.ActiveStatus";
protected static final String PART_INACTIVE_STATUS = "emxProduct.Part.InactiveStatus";
protected static final String SIMPLE_INCLUSION_RULE_KEY = "emxProduct.RuleCreation.SimpleInclusion";
protected static final String COMPLEX_INCLUSION_RULE_KEY = "emxProduct.RuleCreation.ComplexInclusion";
protected static final String SUITE_KEY = "Configuration";
public static final String DUPLICATE_PART_XML  = PropertyUtil.getSchemaProperty("attribute_DuplicatePartXML");
protected static final String DEFAULT_PART_POLICY_DEVELOPMENT_PART = "policy_DevelopmentPart";
protected static final String DEFAULT_PART_POLICY_EC_PART = "policy_ECPart";
// Added for Background Job
protected static Job _job = new Job();
protected String _jobId = null;

@Deprecated
public void setJob(Job job) {
    _job = job;
}

@Deprecated
public Job getJob() {
    return _job;
}

 /**
  * Default Constructor.
  * @param context the eMatrix <code>Context</code> object
  * @param args holds no arguments
  * @throws Exception if the operation fails
  * @since ProductCentral 10.0.0.0
  * @grade 0
  */
  public emxFTRPartBase_mxJPO (Context context, String[] args) throws Exception
  {
    super(context, args);
    // Added for BackGround Job

  }

/**
* This method is used to get the Filtered Object List of Parts
* @param context the eMatrix <code>Context</code> object
* @param args holds the following input arguments:
* @return Maplist
* @throws Exception if the operation fails
* @since version=V6R2008-1
*
*/

   public MapList getFilteredPartsList(Context context, String[] args) throws Exception
        {
            MapList relBusObjList = expandForParts(context,args,DomainConstants.EMPTY_STRING);
            MapList filteredObjPageList = new MapList();
            StringList busSelects = new StringList();
            boolean flag = false;

			try{
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            String sTextboxValue =  (String) programMap.get("TextboxValue");
            String strParentId = (String)programMap.get("objectId");
            try
          {
             if ( sTextboxValue != null && !sTextboxValue.equals("*"))
             {
                String objIdArray[] = new String[relBusObjList.size()];
                busSelects.add("name");

                for(int i = 0; i < relBusObjList.size(); i++)
                objIdArray[i] =(String)((Map)relBusObjList.get(i)).get("id");
                
                for (int i = 0; i < relBusObjList.size(); i++)
                {
                    Map obj = (Map) relBusObjList.get(i);
                    String strName = new DomainObject(obj.get("id").toString()).getInfo(context,"name");

                    //Check if '*' value in textbox
                    if(sTextboxValue.indexOf("*") == -1)
                        {
                           if(sTextboxValue.equalsIgnoreCase(strName))
                            filteredObjPageList.add(relBusObjList.get(i));
                        }
                     else
                       {
                           StringTokenizer st = new StringTokenizer(sTextboxValue,"*");
                           String strTokenns[] = new String[st.countTokens()];
                           int arrlength = 0;

                           while(st.hasMoreTokens())
                                {
                                  String strToken= st.nextToken();
                                  strTokenns[arrlength++] =  strToken;
                                }

                                 flag =true;
                                //position of "*"
                          if(sTextboxValue.indexOf("*") != 0)
                           {
                             if(strName.indexOf(strTokenns[0])== 0)
                            {
                                flag = true;
                                strName = strName.substring(strName.lastIndexOf(strTokenns[0])+2,strName.length());
                            }
                            else
                                flag = false;
                           }
                         else
                           {

                               if(strTokenns[0] != null && !strTokenns[0].equals(""))
                            {
                              if(strName.indexOf(strTokenns[0]) != -1 )
                                {
                                    flag = true;
                                    strName = strName.substring(strName.lastIndexOf(strTokenns[0])+2,strName.length());
                                }
                              else
                                flag = false;
                            }
                            else
                                flag = true;
                           }
                         for(int j =1; j < arrlength && flag==true ;j++)
                         {

                            if(strName.indexOf(strTokenns[j]) != -1 )
                            {
                                strName = strName.substring(strName.lastIndexOf(strTokenns[j])+2,strName.length());
                            }
                            else
                            {
                              flag = false;
                            }
                         }
                        if(flag)
                        filteredObjPageList.add(relBusObjList.get(i));
                      }
                }
                relBusObjList = filteredObjPageList;
            }
             }
            catch(Exception e)
            {
                e.printStackTrace();
            }

            //Modified to solve 352255 - Starts
            String strRowEditable = "RowEditable";
            String strReadOnly     = "readonly";
            Map tempMap           = null;
            Map tempObjMap       = null;
            boolean simple           = false;
            String ruleComplexity="";

			DomainObject domObj = new DomainObject(strParentId);

            String hasDvs = domObj.getInfo(context,"from["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"]");


            for(int i=0;i<relBusObjList.size();i++)
            {
				simple           = false;
                tempObjMap = (Map) relBusObjList.get(i);

                tempMap = new HashMap();
                tempMap.put("FeatureId",strParentId);
                tempMap.put("PartId",(String) tempObjMap.get (DomainConstants.SELECT_ID));
                tempMap.put("GBOMToId",(String) tempObjMap.get (DomainConstants.SELECT_RELATIONSHIP_ID));

                /*checking for rule complexity*/
				ruleComplexity =(String)tempObjMap.get ("ruleComplexity");


				if(ruleComplexity!=null && !ruleComplexity.equals("")){

					if(ruleComplexity.equalsIgnoreCase("Simple")){
						simple = true;
					}

				}else{
					 if(hasDvs.equalsIgnoreCase("TRUE")){
						 simple = true;
					 }


				}

               // simple = ${CLASS:emxConfigurableRulesBase}.isInclusionRuleSimple(context, tempArg);

                if (!simple)
                {
                   tempObjMap.put(strRowEditable,strReadOnly);
                }
            }
		}catch(Exception e){
				e.printStackTrace();
			}
           //Modified to solve 352255 - Ends
            return relBusObjList;
          }




         /* This method used get GBOM for all selected features in context
         * @param context
         * @param args
         * @return Maplist
         * @since V6R2008-1
         *
         */


        public MapList getPartsForSelectedFeatures(Context context,
                String args[]) throws Exception
        {
            HashMap programMap = (HashMap) JPO.unpackArgs (args) ;
            if (args.length == 0)
            {
                throw new IllegalArgumentException () ;
            }
            String strParentId = (String)programMap.get("objectId");
            MapList masterPartList = expandForParts (context, args,
                    DomainConstants.EMPTY_STRING) ;

            for(int i = 0; i<masterPartList.size(); i++ )
            {
                Map tempMap = (Map) masterPartList.get(i);
                tempMap.put("ParentFeaID",strParentId);
            }
            String prgType = (String) programMap.get ("editTableMode") ;
            if (prgType != null)
                return masterPartList ;

            int lengthArr = 0;
            if(programMap.get ("noOfTargetFeatures")!= null)
                lengthArr = Integer.parseInt ((String) programMap.get ("noOfTargetFeatures")) ;

            String objectIDs[] = new String [lengthArr] ;
            for (int i = 0; i < lengthArr; i++)
            {
                Map object = new HashMap () ;
                objectIDs[i] = (String) programMap.get ("objectIDs" + i) ;
                object.put ("objectId", objectIDs[i]) ;
                String [ ] arg = JPO.packArgs (object) ;
                MapList partList = expandForParts (context, arg,
                        DomainConstants.EMPTY_STRING) ;
                for(int j = 0; j<partList.size(); j++ )
                {
                    Map tempMap = (Map) partList.get(j);
                    tempMap.put("ParentFeaID",objectIDs[i]);
                }
                masterPartList.addAll (partList) ;
            }
            return masterPartList ;
        }



        /**
         * Get values for column group number depending upon duplicate condition.
         * @param context
         * @param args
         * @return
         * @throws Exception
         */
        public List getGroupNumber(Context context, String args[])
                throws Exception
        {
            if (args.length == 0)
            {
                throw new IllegalArgumentException () ;
            }
            StringList data = new StringList () ;
            try
            {
            HashMap programMap = (HashMap) JPO.unpackArgs (args) ;
            MapList objectList = (MapList) programMap.get ("objectList") ;
            Map paramMap = (Map) programMap.get("paramList");
            String strStep = (String) paramMap.get("Step");

            for (int i = 0; i < objectList.size(); i++)
                {
                    Map mapPart = (Map) objectList.get(i);
                    String strGrpNumber = "";

                    if(strStep.equals("EditGBOM") || strStep.equals("ViewDuplicate"))
                        strGrpNumber = (String) mapPart.get("GroupNumber");
                    else
                    {
                        if(mapPart.get("PNGroupNumber")!=null)
                            strGrpNumber = (String) mapPart.get("PNGroupNumber");
                        if(mapPart.get("IRGroupNumber")!=null)
                            strGrpNumber = (String) mapPart.get("IRGroupNumber");
                    }
                    if(strGrpNumber != null)
                        data.add(strGrpNumber);
                    else
                        data.add("");
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            return data ;
        }

        /**
         *
         * @param context
         * @param args
         * @return
         * @throws Exception
         *
         */
        public List getGBOMCheckBox(Context context, String [ ] args)
        throws Exception
        {
            HashMap programMap = (HashMap) JPO.unpackArgs (args) ;
            List lstObjectIdsList = (MapList) programMap.get ("objectList") ;

            List chkBoxes = new StringList () ;

            for (int i = 0; i < lstObjectIdsList.size (); i++)
            {
                Map partObj = (Map) lstObjectIdsList.get (i) ;
                String part = (String) partObj.get ("id") ;
                String relID = (String) partObj.get ("id[connection]") ;

                StringBuffer strTypeChooser = new StringBuffer (150) ;
                strTypeChooser
                        .append ("<input type=\"checkbox\" id=\""
                                + part
                                + "\" value=\""
                                + relID
                                + "|"
                                + part
                                + "\" onclick=\"doCheckboxClick(this); doSelectAllCheck(this)\" name=\"emxTableRowIds\"  >") ;
                chkBoxes.add (strTypeChooser.toString ()) ;
            }

            return chkBoxes ;
        }




        /**
         * This method is used to Evaluate the arithmatic equation in duplicate part check.
         * @param context The ematrix context object.
         * @param strExpression
         * @param listExpToken
         * @return int
         * @since V6R2008-1
         *
         */

         protected Object getEvaluatedResultForExpression (Context context,String strExpression,StringList listExpToken) throws Exception
         {
            Stack expPostFixExp= new Stack();
            boolean notOperator = false;
            if(strExpression.indexOf("NOT")!= -1)
                notOperator = true;
            StringTokenizer expressionTokenZ = new StringTokenizer(strExpression,"\"");
            String strToken = "";
            String strSubTokenOp = "";
            Stack stackOperator = new Stack();
            try{
            while(expressionTokenZ.hasMoreTokens())
            {
                strToken = expressionTokenZ.nextToken();
                if(strToken.trim().equals("("))
                {
                    stackOperator.push("(");
                }
                else if(!stackOperator.isEmpty() && strToken.trim().equals("AND"))
                {
                    if(stackOperator.get(stackOperator.size()).toString().equals("!"))
                    {
                        while(!stackOperator.isEmpty() && !(stackOperator.get(stackOperator.size()).toString().equals("(")))
                        {
                            strSubTokenOp = (String)stackOperator.pop();
                            expPostFixExp.push(strSubTokenOp);
                        }
                        stackOperator.push("*");
                    }
                    if(stackOperator.get(stackOperator.size()).toString().equals("+"))
                        expPostFixExp.push("*");
                }
                else if(!stackOperator.isEmpty() && strToken.trim().equals("OR"))
                {
                    if(stackOperator.get(stackOperator.size()).toString().equals("!") || stackOperator.get(stackOperator.size()).toString().equals("*") || stackOperator.get(stackOperator.size()).toString().equals("+"))
                    {
                        while(!stackOperator.isEmpty() && !(stackOperator.get(stackOperator.size()).toString().equals("(")))
                        {
                            strSubTokenOp = (String)stackOperator.pop();
                            expPostFixExp.push(strSubTokenOp);
                        }
                        stackOperator.push("+");
                    }
                }
                else if(!stackOperator.isEmpty() && strToken.trim().equals("NOT"))
                {
                    stackOperator.push("!");
                }
                else if(!stackOperator.isEmpty() && strToken.trim().equals(")"))
                {
                    while(!stackOperator.isEmpty() && !(stackOperator.get(stackOperator.size()).toString().equals("(")))
                    {
                        strSubTokenOp = (String)stackOperator.pop();
                        expPostFixExp.push(strSubTokenOp);
                    }
                    stackOperator.pop();
                }
                else if(stackOperator.isEmpty() && ( strToken.trim().equals("AND") || strToken.trim().equals("OR") || strToken.trim().equals("NOT")))
                {
                    if(strToken.trim().equals("AND"))
                        stackOperator.push("*");
                    else if(strToken.trim().equals("OR") )
                        stackOperator.push("+");
                    else if(strToken.trim().equals("NOT") )
                        stackOperator.push("!");
                }
                else if(!strToken.trim().equals(""))
                    expPostFixExp.push(strToken);
            }
            while(!stackOperator.isEmpty())
                expPostFixExp.push((String)stackOperator.pop());
            Stack stckResult = new Stack();
            if(!notOperator)
            {
                while(!expPostFixExp.isEmpty())
                {
                    String strElement = (String) expPostFixExp.pop();
                    if(!strElement.equals("+") && !strElement.equals("*"))
                        stckResult.push(strElement);
                    else
                    {
                        Object strRtElement =  stckResult.pop();
                        Object strLftElement = stckResult.pop();
                        int rightOperant = 0;
                        int leftOperant = 0;

                        rightOperant = listExpToken.indexOf((String) stckResult.pop())+2;
                        leftOperant = listExpToken.indexOf((String) stckResult.pop())+2;
                        int result = 0;

                        if(!strRtElement.getClass().toString().equals("java.lang.Integer"))
                            rightOperant = listExpToken.indexOf((String)strRtElement)+2;
                        else
                            rightOperant = ((Integer)strRtElement).intValue();

                        if(!strLftElement.getClass().toString().equals("java.lang.Integer"))
                            leftOperant = listExpToken.indexOf((String)strLftElement)+2;
                        else
                            leftOperant = ((Integer)strLftElement).intValue();

                        if(strElement.equals("+"))
                                result = rightOperant + leftOperant;
                        if(strElement.equals("*"))
                                result = rightOperant * leftOperant;
                        stckResult.push(Integer.valueOf(result));
                    }
                }
            }
            else
            {
                    Stack tempPostFixStack = new Stack();
                    MapList valueMapList = getValueMapList(context,listExpToken);
                    MapList resultList = new MapList();
                    for(int i =0 ;i < valueMapList.size();i++)
                    {
                        Map valMap = (Map) valueMapList.get(i);
                        tempPostFixStack.addAll(expPostFixExp);
                        while(!tempPostFixStack.isEmpty())
                        {
                            String strElement = (String) tempPostFixStack.get(0);
                            if(!strElement.equals("+") && !strElement.equals("*") && !strElement.equals("!"))
                                stckResult.push(strElement);
                            else if(!strElement.equals("!"))
                            {
                                Object strRtElement =  stckResult.pop();
                                Object strLftElement = stckResult.pop();

                                boolean rightOperant = false;
                                boolean leftOperant = false;

                                if(!strRtElement.getClass().equals(Boolean.class))
                                    rightOperant = Boolean.valueOf((String)valMap.get(strRtElement)).booleanValue();
                                else
                                    rightOperant = ((Boolean)strRtElement).booleanValue();

                                if(!strLftElement.getClass().equals(Boolean.class))
                                    leftOperant = Boolean.valueOf((String)valMap.get(strLftElement)).booleanValue();
                                else
                                    leftOperant = ((Boolean)strLftElement).booleanValue();

                                boolean result = false;
                                if(strElement.equals("+"))
                                    result = (rightOperant ||leftOperant);
                                if(strElement.equals("*"))
                                    result = (rightOperant && leftOperant);

                                stckResult.push(Boolean.valueOf(result));
                            }
                            else
                            {
                                Object strRtElement =  stckResult.pop();
                                boolean rightOperant = false;
                                if(!strRtElement.getClass().equals(Boolean.class))
                                    rightOperant = !((Boolean)valMap.get(strRtElement)).booleanValue();
                                else
                                    rightOperant = !((Boolean)strRtElement).booleanValue();
                                stckResult.push(Boolean.valueOf(rightOperant));
                            }
                            tempPostFixStack.remove(0);
                        }
                        if(!stckResult.isEmpty())
                            resultList.add((Boolean) stckResult.pop());
                    }
                    stckResult.clear();
                    stckResult.push(resultList);
            }
            if(!stckResult.isEmpty())
                return stckResult.pop();
            else
                return Integer.valueOf(0);
            }catch(Exception e){e.printStackTrace(); return Integer.valueOf(0);}

         }
         /**
          * This method is used to retrieve the  Status of Part.
          * @param context The ematrix context object.
          * @param strExpression
          * @param listExpToken
          * @return int
          * @since V6R2008-1
          *
          */
          public MapList getValueMapList (Context context,StringList slTokens) throws Exception
          {
              if (slTokens == null) {
                  slTokens = new StringList();
              }

              int nSize = slTokens.size();
              MapList mlResult = new MapList();

              if (nSize == 0) {
                  // Do nothing
              }
              else if (nSize == 1) {
                  String strToken = (String)slTokens.get(0);

                  Map mapValue = new HashMap();
                  mapValue.put(strToken, Boolean.TRUE);
                  mlResult.add(mapValue);

                  mapValue = new HashMap();
                  mapValue.put(strToken, Boolean.FALSE);
                  mlResult.add(mapValue);
              }
              else {
                  String strToken = (String)slTokens.remove(0);

                  MapList mlPartial = getValueMapList(context, slTokens);

                  nSize = mlPartial.size();
                  for (int i = 0; i < nSize; i++) {
                      Map mapValue = (Map)mlPartial.get(i);

                      Map mapValue1 = new HashMap(mapValue);
                      mapValue1.put(strToken, Boolean.FALSE);
                      mlResult.add(mapValue1);

                      Map mapValue2 = new HashMap(mapValue);
                      mapValue2.put(strToken, Boolean.TRUE);
                      mlResult.add(mapValue2);
                  }//~for
              }

              return mlResult;

          }





    /**This method will compare the Part Name.
    * @param context
    * @param args
    * @return boolean true if both parts Name match otherwise false.
    * @throws Exception
    * @since R212
    */

    private boolean isPartDuplicate(Map mapOutter, Map mapInner){

        boolean isDuplicate = false;
        if(mapOutter!=null && mapInner!=null){
            if (mapOutter.get(DomainObject.SELECT_NAME) != null && mapInner.get(DomainObject.SELECT_NAME)!=null ) {

                String strOutterPartName = (String) mapOutter.get(DomainObject.SELECT_NAME);
                String strInnerPartName = (String) mapInner.get(DomainObject.SELECT_NAME) ;

                return(strOutterPartName.equalsIgnoreCase(strInnerPartName));

            }

        }
        return isDuplicate;
    }


    /**
     * This method checks if the map has Incluision Rule or not.
     * @param context
     * @param args
     * @return boolean true if InclusionRule is the otherwise false.
     * @throws Exception
     * @since V6R2008-1 HF0.6
     */

    private boolean hasInclusionRule(Map map){
        if ((map.get("IncRuleId")!=null) && (!map.get("IncRuleId").equals("")))
            return true;
        else
           return false;
    }

    //
    /** This method will check the Rule complexity.
     * @param context
     * @param args
     * @return boolean true if both Parts Rule Complexity match otherwise false.
     * @throws Exception
     * @since R212
     */
    private boolean isRuleSimpleOrComplex(Map map){
        if ((map.get("RuleComplexity")!=null) && (!map.get("RuleComplexity").equals(""))){
            String strRuleComplexity = (String) map.get("RuleComplexity");
            return(strRuleComplexity.equalsIgnoreCase("Simple"));
        }
        return false;

    }

    /**
     * This method will compare if bot the maps has matching Design Variants.
     * @param context
     * @param args
     * @return boolean true if Design Variants match otherwise false.
     * @throws Exception
     * @since R212
     */

    private boolean hasMatchingDesignVariants(Map mapOutter, Map mapInner){

        if (((mapOutter.get("DesignVariantsAttr")!=null) && (!mapOutter.get("DesignVariantsAttr").equals("")))
                && ((mapInner.get("DesignVariantsAttr")!=null) && (!mapInner.get("DesignVariantsAttr").equals(""))) ){

            String strOutterDesignVariants = (String) mapOutter.get("DesignVariantsAttr");
            String strInnerDesignVariants = (String) mapInner.get("DesignVariantsAttr");
            StringTokenizer strOutterDVs = new StringTokenizer(strOutterDesignVariants, ",");
            StringTokenizer strInnerDVs = new StringTokenizer(strInnerDesignVariants, ",");
            // check if the length of the design variants in both Maps
            if(strOutterDVs.countTokens() != strInnerDVs.countTokens()){
                return  false;
            }

            // otherwise compare the design variants
            HashMap innerDVs = new HashMap();
            while(strInnerDVs.hasMoreElements()){
                String strInnerValue = strInnerDVs.nextElement().toString();
                innerDVs.put(strInnerValue,strInnerValue);
            }


            while(strOutterDVs.hasMoreElements()){
               String strInnerDesignVariant = (String)strOutterDVs.nextToken();
               // if the Design Variant value is not found then return false
               if(innerDVs.get(strInnerDesignVariant)==null){
                   return false;
               }
            }
            // assuming the design variants match for both the Outter and the inner maps.
            return true;
        }
        return false;
    }


    /**
     * This method will compare if bot the maps has matching Right Expression attribute.
     * @param context
     * @param args
     * @return boolean true if Right Expression match otherwise false.
     * @throws Exception
     * @since R212
     */

    private boolean hasMatchingRightExpressions(Map mapOutter, Map mapInner){

        if (((mapOutter.get("RightExpressionAttr")!=null) && (!mapOutter.get("RightExpressionAttr").equals("")))
                && ((mapInner.get("RightExpressionAttr")!=null) && (!mapInner.get("RightExpressionAttr").equals(""))) ){

            String strOutterRightExpression = (String) mapOutter.get("RightExpressionAttr");
            String strInnerRightExpression = (String) mapInner.get("RightExpressionAttr");
            StringTokenizer strOutterRE = new StringTokenizer(strOutterRightExpression, "AND");
            StringTokenizer strInnerRE = new StringTokenizer(strInnerRightExpression, "AND");


            HashMap innerRE = new HashMap();
            while(strInnerRE.hasMoreElements()){
            	// Modified for IR-040114V6R2011
                String strInnerValue = strInnerRE.nextElement().toString().trim();
                innerRE.put(strInnerValue,strInnerValue);
            }


            while(strOutterRE.hasMoreElements()){
            	//Modified for IR-040114V6R2011
               String strInnerDesignVariant = (String)strOutterRE.nextToken().trim();
               // if the Right Expression value is not found then return false
               if(innerRE.get(strInnerDesignVariant)==null){
                   return false;
               }
            }
            // assuming the Right Expression match for both the Outter and the inner maps.
            return true;
        }
        return false;
    }

    /**
     * This method will compare the Part Expression.
     * @param context
     * @param args
     * @return boolean true if both parts expression match otherwise false.
     * @throws Exception
     * @since R212
     */
    private boolean isExpressionDuplicate(Map mapOutter, Map mapInner){

        boolean isExpresionDuplicate = false;
        if(mapOutter!=null && mapInner!=null){
            // to check if both the maps has Inclusion Rule
            boolean hasInclusionRuleForOuter = hasInclusionRule(mapOutter);
            boolean hasInclusionRuleForInner = hasInclusionRule(mapInner);
            if(hasInclusionRuleForOuter && hasInclusionRuleForInner){
                // to check if both the maps match either Simple Rule or the Complex.
                // returns true if it is simple; false if complex.
                if((isRuleSimpleOrComplex(mapOutter) && isRuleSimpleOrComplex(mapOutter))
                        || (!isRuleSimpleOrComplex(mapOutter) && !isRuleSimpleOrComplex(mapOutter))){
                    // This method will return if the Design Variants match
                    if(hasMatchingDesignVariants(mapOutter, mapInner) ){
                        if (hasMatchingRightExpressions(mapOutter, mapInner)){
                            isExpresionDuplicate = true;
                        }
                    }
                    // else if not hasMatchingDesignVariants
                    else{
                        isExpresionDuplicate = false;
                    }
                }
                // else if not isRuleSimpleOrComplex
                else{
                    isExpresionDuplicate = false;
                }
            }
            // if both map do not have Inclusion rules they are  duplicate
            else if(!hasInclusionRuleForOuter && !hasInclusionRuleForInner){
                isExpresionDuplicate = true;
            }
            else{
                isExpresionDuplicate = false;
            }
         }
        return isExpresionDuplicate;
    }









    /**
     * This method is used to retrieve the Group Numbers Status of Part.
     * @param context The ematrix context object.
     * @param strExpression
     * @param listExpToken
     * @return MapList
     * @since R212
     */
    public MapList getDuplicatePartsForSelectedFeature(Context context, String [ ] args)
    throws Exception
    {

        Map programMap = (Map)JPO.unpackArgs(args);
        MapList listParts = (MapList)programMap.get("FinalPartList");


        try{

        // set the group number
        int iMasterGroupNumber = 1;

        MapList mapListMaster = new MapList();
        HashMap masterHashMap = new HashMap();


        String outterKey = "";
        String innerKey = "";
        
        // This for loop is to process outter Map
        for (int i = 0; i < listParts.size()-1; i++)
        {

            Map mapPart = (Map) listParts.get(i);
            int iPNGroupNumber = 0;
            int iIRGroupNumber = 0;

            // This for loop is to process Inner Map
            for (int j = i+1; j < listParts.size(); j++)
            {
                Map mapInnerPart = (Map) listParts.get(j);
                
                // Call method to check if the Part Names are duplicate
                if (isPartDuplicate(mapPart, mapInnerPart)){
                    outterKey = mapPart.toString() + "PNGroupNumber";

                    if (!masterHashMap.containsKey(outterKey)) {
                        masterHashMap.put(outterKey, "");
                        // This condition is to keep track of the group numbers
                        if(iPNGroupNumber==0){
                            iPNGroupNumber = iMasterGroupNumber;
                            iMasterGroupNumber ++;
                        }
                        // Make copy
                        Map hmOuter = new HashMap(mapPart);
                        // Add copy
                        hmOuter.put("PNGroupNumber",""+iPNGroupNumber);
                        hmOuter.put("GroupNumber",""+iPNGroupNumber);
                        // Add to Master
                        mapListMaster.add(hmOuter);
                    }

                    innerKey = mapInnerPart.toString() + "PNGroupNumber";
                    if (!masterHashMap.containsKey(innerKey)) {
                        masterHashMap.put(innerKey, "");
                        // This condition is to keep track of the group numbers
                        if(iPNGroupNumber==0){
                            iPNGroupNumber = iMasterGroupNumber;
                            iMasterGroupNumber ++;
                        }

                        // Make copy
                        Map hmInner = new HashMap(mapInnerPart);
                        // Add copy
                        hmInner.put("PNGroupNumber",""+iPNGroupNumber);
                        hmInner.put("GroupNumber",""+iPNGroupNumber);
                        // Add to Master
                        mapListMaster.add(hmInner);
                    }
                }
                // call method to check if the Expression dulicate
                if (isExpressionDuplicate(mapPart, mapInnerPart)){

                    // creating the new map for the duplicate for the inner and the outer maps.
                     outterKey = mapPart.toString() + "IRGroupNumber";
                    if (!masterHashMap.containsKey(outterKey)) {
                        masterHashMap.put(outterKey, "");
                        // This condition is to keep track of the group numbers
                        if(iIRGroupNumber==0){
                            iIRGroupNumber = iMasterGroupNumber;
                            iMasterGroupNumber ++;
                        }

                        // Make copy
                        Map hmOuter = new HashMap(mapPart);
                        // Add copy
                        hmOuter.put("IRGroupNumber",""+iIRGroupNumber);
                        hmOuter.put("GroupNumber",""+iIRGroupNumber);
                        // Add to Master
                        mapListMaster.add(hmOuter);
                    }

                    innerKey = mapInnerPart.toString() + "IRGroupNumber";
                    if (!masterHashMap.containsKey(innerKey)) {
                        masterHashMap.put(innerKey, "");
                        // This condition is to keep track of the group numbers
                        if(iIRGroupNumber==0){
                            iIRGroupNumber = iMasterGroupNumber;
                            iMasterGroupNumber ++;
                        }

                        // Make copy
                        Map hmInner = new HashMap(mapInnerPart);
                        // Add copy
                        hmInner.put("IRGroupNumber",""+iIRGroupNumber);
                        hmInner.put("GroupNumber",""+iIRGroupNumber);
                        // Add to Master
                        mapListMaster.add(hmInner);
                    }
                }
            } // end of Inner for loop

        } // end of outter for loop
        return mapListMaster;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw new Exception(e);
        }
}



   /**
    * Determine if the Replace command for the GBOM table is visible.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args contains the feature object ID
    * @returns Boolean
    * @throws Exception if the operation fails
    * @since FTR R207
    * @deprecated in R418
    */
   public static Boolean showReplaceGBOMActionCommand(Context context, String[] args) throws Exception
   {
      HashMap paramMap = (HashMap) JPO.unpackArgs(args);
      String strFeatureId = (String)paramMap.get("objectId");

      //get the current state for the feature object
      StringList slSelectable = new StringList(DomainConstants.SELECT_CURRENT);
      slSelectable.add(DomainConstants.SELECT_POLICY);

      Map mpSelectable = new DomainObject(strFeatureId).getInfo(context,slSelectable);

      String strCurrent = (String)mpSelectable.get(DomainConstants.SELECT_CURRENT);
      String strPolicy = (String)mpSelectable.get(DomainConstants.SELECT_POLICY);

      String stateRelease = PropertyUtil.getSchemaProperty(context,"policy", strPolicy, "state_Release");
      String stateObsolete = PropertyUtil.getSchemaProperty(context,"policy", strPolicy, "state_Obsolete");

      boolean isReleased = stateRelease.equals(strCurrent);
      boolean isObsolete = stateObsolete.equals(strCurrent);

      //get the flag value
      String updateReleasedFeatureEnabled = EnoviaResourceBundle.getProperty(context,"emxConfiguration.GBOMPartUpdateAllowedForReleasedFeature");

      //(1) The command is always visible if the feature is neither released nor obsolete,
      //    regardless of the value for the flag - same existing behavior
      //(2) If the feature is released, the command is visible only if the flag has value true
      //(3) If the feature is obsolete, the command is invisible - the same existing behavior
      boolean showCommand = false;
      if (!isReleased && !isObsolete)
         showCommand = true;
      else if (isReleased && "true".equalsIgnoreCase(updateReleasedFeatureEnabled))
         showCommand = true;

      return Boolean.valueOf(showCommand);
   }

   /**
    * Get all open Reported Against ECs owned by the user for all feature objects except for the given one.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args contains the given feature object ID
    * @returns StringList
    * @throws Exception if the operation fails
    * @since FTR R207
    */
   @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
   public static StringList getSelfOwnedOpenReportedAgainstECs(Context context, String[] args) throws Exception
   {
      HashMap paramMap = (HashMap)JPO.unpackArgs(args);
      String strFeatureId = (String)paramMap.get("objectId");

      StringList objectSelects = new StringList(1);
      objectSelects.addElement(DomainConstants.SELECT_ID);
      DomainObject featureObj = DomainObject.newInstance(context, strFeatureId);
      short sRecurseLevel = 1;
      boolean getTo = true, getFrom = false;
      MapList relatedReportedAgainstECsML = featureObj.getRelatedObjects(
          context, DomainConstants.RELATIONSHIP_REPORTED_AGAINST_EC, DomainConstants.TYPE_ENGINEERING_CHANGE,
          objectSelects, null, getTo, getFrom, sRecurseLevel, "", "", 0);

      StringList relatedReportedAgainstECs = new StringList();
      Iterator iterator = relatedReportedAgainstECsML.iterator();
      Map listMap = null;
      while (iterator.hasNext())
      {
         listMap = (Map)iterator.next();
         relatedReportedAgainstECs.add((String)listMap.get(SELECT_ID));
      }

      StringList allOpenReportedAgainstECs = new StringList();

      StringBuffer whereExpSB = new StringBuffer(128);
      whereExpSB.append("current != Complete && current != Close && current != Reject && current != Cancelled");
      whereExpSB.append(" && ");
      whereExpSB.append("from[" + DomainConstants.RELATIONSHIP_REPORTED_AGAINST_EC + "] == true");
      MapList allOpenReportedAgainstECsML = DomainObject.findObjects(
         context, DomainConstants.TYPE_ENGINEERING_CHANGE, DomainConstants.QUERY_WILDCARD, DomainConstants.QUERY_WILDCARD,
         context.getUser(), DomainConstants.QUERY_WILDCARD, whereExpSB.toString(), true, objectSelects);
      iterator = allOpenReportedAgainstECsML.iterator();
      while (iterator.hasNext())
      {
         listMap = (Map)iterator.next();
         allOpenReportedAgainstECs.add((String)listMap.get(SELECT_ID));
      }
      //System.out.println("   all open Reported Against ECs: " + allOpenReportedAgainstECs.toString());

      //now try to remove the related Reported Against ECs
      allOpenReportedAgainstECs.removeAll(relatedReportedAgainstECs);
      //System.out.println("   all other open Reported Against ECs: " + allOpenReportedAgainstECs.toString());
      return allOpenReportedAgainstECs;
   }
   //R207 GBOM Part Updates end

	/**
	 * This method will be used to get the Inactive GBOM connected with the
	 * context Object inactive due to Part.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            - Holds the parameters passed from the calling method
	 * @return MapList - MapList containing the id of Part objects
	 * @throws Exception
	 *             if the operation fails
	 * @since R211
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getInactiveGBOMStructure(Context context, String[] args)
			throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String strParentId = (String) programMap.get("objectId");
		String strProdId = (String) programMap.get("prodId");

		LogicalFeature compFtr = new LogicalFeature(strParentId);
		compFtr.setGBOMContext(context, strProdId);


		// Where Condition to retrieve GBOMs with Inactive From Variant as No
		String relWhere = "attribute["
				+ ConfigurationConstants.ATTRIBUTE_INACTIVE_FROM_VARIANT
				+ "] == No";
		String objWhere = DomainObject.EMPTY_STRING;

		// Obj and Rel pattern
		String typePattern = DomainObject.EMPTY_STRING;
		String relPattern = DomainObject.EMPTY_STRING;
		relPattern=ConfigurationConstants.RELATIONSHIP_INACTIVE_GBOM;

		String strCustomPartMode = EnoviaResourceBundle.getProperty(context,
 		"emxConfiguration.PreviewBOM.EnableCustomPartMode");
         if (ProductLineCommon.isNotNull(strCustomPartMode)
 				&& strCustomPartMode.equalsIgnoreCase("true")) {

        	 relPattern = relPattern + ","+ ConfigurationConstants.RELATIONSHIP_INACTIVE_CUSTOM_GBOM;
         }

		// Obj and Rel Selects
		StringList objSelects = getGBOMObjectSelects();
		StringList relSelects = getGBOMRelationshipSelects(REL_SELECT_PART_INACTIVE);

		int iLevel = ConfigurationUtil.getLevelfromSB(context, args);
		String filterExpression = (String) programMap
				.get("CFFExpressionFilterInput_OID");

		// retrieve Inactive GBOM with where condition satisfied
		MapList objectList = compFtr.getInactiveGBOMStrucure(context,
				typePattern, relPattern, objSelects, relSelects, false, true,
				iLevel, 0, objWhere, relWhere, DomainObject.FILTER_STR_AND_ITEM,
				filterExpression);
		return objectList;
	}

	/**
	 * Returns the inactive parts due to inactive Design Variant.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds Hashmap containing the object id.
	 * @return Maplist containing the ids of Parts.
	 * @throws Exception
	 *             if the operation fails
	 * @since R211
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getVariantInactiveGBOMStrucure(Context context, String[] args)
			throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String strParentId = (String) programMap.get("objectId");
		LogicalFeature compFtr = new LogicalFeature(strParentId);
		// Where Condition to retrieve GBOMs with Inactive From Variant as Yes
		String relWhere = "attribute["
				+ ConfigurationConstants.ATTRIBUTE_INACTIVE_FROM_VARIANT
				+ "] == Yes";
		String objWhere = DomainObject.EMPTY_STRING;

		// Obj and Rel pattern
		String typePattern = DomainObject.EMPTY_STRING;
		String relPattern = DomainObject.EMPTY_STRING;
		relPattern=ConfigurationConstants.RELATIONSHIP_INACTIVE_GBOM;
		String strCustomPartMode = EnoviaResourceBundle.getProperty(context,
 		"emxConfiguration.PreviewBOM.EnableCustomPartMode");
         if (ProductLineCommon.isNotNull(strCustomPartMode)
 				&& strCustomPartMode.equalsIgnoreCase("true")) {

        	 relPattern = relPattern + ","+ ConfigurationConstants.RELATIONSHIP_INACTIVE_CUSTOM_GBOM;
         }

		// Obj and Rel Selects
		StringList objSelects = getGBOMObjectSelects();
		StringList relSelects = getGBOMRelationshipSelects(REL_SELECT_DV_INACTIVE);

		int iLevel = ConfigurationUtil.getLevelfromSB(context, args);
		String filterExpression = (String) programMap
				.get("CFFExpressionFilterInput_OID");

		// retrieve Inactive GBOM with where condition satisfied
		MapList objectList = compFtr.getInactiveGBOMStrucure(context,
				typePattern, relPattern, objSelects, relSelects, false, true,
				iLevel, 0, objWhere, relWhere, DomainObject.FILTER_STR_AND_ITEM,
				filterExpression);
		return objectList;
	}

	/**
	 * This will return all GBOM connected to the context i.e.Active and
	 * Inactive.
	 *
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 * @since R211
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getCompleteGBOMStructure(Context context, String[] args)
			throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String strParentId = (String) programMap.get("objectId");

		LogicalFeature compFtr = new LogicalFeature(strParentId);

		String relWhere = DomainObject.EMPTY_STRING;
		String objWhere = DomainObject.EMPTY_STRING;
		// Obj and Rel pattern
		String typePattern = DomainObject.EMPTY_STRING;
		String relPattern = ConfigurationConstants.RELATIONSHIP_GBOM;

		String strCustomPartMode = EnoviaResourceBundle.getProperty(context,
 		"emxConfiguration.PreviewBOM.EnableCustomPartMode");
		StringBuffer sbRelPattern = new StringBuffer(relPattern);
         if (ProductLineCommon.isNotNull(strCustomPartMode)
 				&& strCustomPartMode.equalsIgnoreCase("true")) {
        	 sbRelPattern.append(",");
        	 sbRelPattern.append(ConfigurationConstants.RELATIONSHIP_CUSTOM_GBOM);
         }

		// Obj and Rel Selects
		StringList objSelects = getGBOMObjectSelects();
		StringList relSelects = getGBOMRelationshipSelects(REL_SELECT_GBOM_SUMMARY);

		int iLevel = ConfigurationUtil.getLevelfromSB(context, args);
		String filterExpression = (String) programMap
				.get("CFFExpressionFilterInput_OID");

		// retrieve Active Inactive GBOM
		MapList objectList = compFtr.getGBOMStructure(context, typePattern,
				sbRelPattern.toString(), objSelects, relSelects, false, true, iLevel, 0,
				objWhere, relWhere, DomainObject.FILTER_STR_AND_ITEM, filterExpression);
		return objectList;
	}

	/**
	 * This will return Active GBOM connected to the context
	 *
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 * @since R211
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getActiveGBOMStructure(Context context, String[] args)
			throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String strParentId = (String) programMap.get("objectId");
		String strProdId = (String) programMap.get("prodId");

		LogicalFeature logicalFTR= new LogicalFeature(strParentId);
		logicalFTR.setGBOMContext(context, strProdId);

		String relWhere = DomainObject.EMPTY_STRING;
		// name and limit filter related Object Where condition
		String sNameFilterValue = (String) programMap
				.get("FTRLogicalFeatureGBOMNameFilterCommand");
		String sLimitFilterValue = (String) programMap
				.get("FTRLogicalFeatureGBOMLimitFilterCommand");
		String objWhere = DomainObject.EMPTY_STRING;
		if (sNameFilterValue != null
				&& !(sNameFilterValue.equalsIgnoreCase("*"))) {
			objWhere = DomainConstants.SELECT_NAME + " ~~ '" + sNameFilterValue
					+ "'";
		}

		String strLimit = EnoviaResourceBundle.getProperty(context,
				"emxConfiguration.Search.QueryLimit");
		int limit = Integer.parseInt(strLimit);
		// TODO -emxConfiguration.Search.QueryLimit
		if (sLimitFilterValue != null
				&& !(sLimitFilterValue.equalsIgnoreCase("*"))) {
			if (sLimitFilterValue.length() > 0) {
				limit = (short) Integer.parseInt(sLimitFilterValue);
				if (limit < 0) {
					limit = 32767;
				}
			}
		}

		// Obj and Rel pattern
		String typePattern = "";
		String relPattern = ConfigurationConstants.RELATIONSHIP_GBOM;
		String strCustomPartMode = EnoviaResourceBundle.getProperty(context,
 		"emxConfiguration.PreviewBOM.EnableCustomPartMode");
         if (ProductLineCommon.isNotNull(strCustomPartMode)
 				&& strCustomPartMode.equalsIgnoreCase("true")) {

        	 relPattern = relPattern + ","+ ConfigurationConstants.RELATIONSHIP_CUSTOM_GBOM;
         }
		// Obj and Rel Selects
		StringList objSelects = getGBOMObjectSelects();
		StringList relSelects = getGBOMRelationshipSelects(REL_SELECT_ACTIVE);

		String filterExpression = (String) programMap
				.get("CFFExpressionFilterInput_OID");

		// retrieve Active Inactive GBOM
		MapList objectList = logicalFTR.getActiveGBOMStructure(context,
				typePattern, relPattern, objSelects, relSelects, false, true,
				1, limit, objWhere, relWhere, DomainObject.FILTER_STR_AND_ITEM,
				filterExpression);

		return objectList;
	}

	/**
	 * return Object Selects
	 *
	 * @return
	 * @since R211
	 */
	private StringList getGBOMObjectSelects() {
		StringList objSelects = new StringList();
		objSelects.add(DomainConstants.SELECT_OWNER);
		objSelects.add("from[" + ConfigurationConstants.RELATIONSHIP_GBOM
				+ "].to.name");
		return objSelects;
	}
    public static short REL_SELECT_ACTIVE = 0;
    public static short REL_SELECT_PART_INACTIVE = 1;
    public static short REL_SELECT_DV_INACTIVE = 2;
    public static short REL_SELECT_GBOM_SUMMARY = 3;
    public static short REL_SELECT_ALL = 4;

    /**
	 * return Relationship Selects
	 *
	 * @return
	 * @since R211
	 * TODO-- Need to revisit for performance
	 */
    private StringList getGBOMRelationshipSelects(int relSelect) {
    	StringList relSelects = new StringList(DomainRelationship.SELECT_ID);
		String dvselectable = "from.from["
			+ ConfigurationConstants.RELATIONSHIP_VARIES_BY + "].to.id";
		String dvnameselectable = "from.from["
			+ ConfigurationConstants.RELATIONSHIP_VARIES_BY + "].to.name";
		String inactivedvselectable = "from.from["
			+ ConfigurationConstants.RELATIONSHIP_INACTIVE_VARIES_BY + "].to.id";
		String inactivedvselectable2 = "tomid["
			+ ConfigurationConstants.RELATIONSHIP_INACTIVE_VARIES_BY_GBOM + "].fromrel.to.name";

    	if(REL_SELECT_ACTIVE== relSelect){
    		relSelects.add(DomainRelationship.SELECT_FROM_TYPE);
    		relSelects.add("tomid["
    				+ ProductLineConstants.RELATIONSHIP_LEFT_EXPRESSION
    				+ "].from.id");
    		relSelects.add(dvselectable);
    		relSelects.add(dvnameselectable);
    		relSelects.add(DomainRelationship.SELECT_FROM_ID);
    		relSelects.add("attribute["
    				+ ConfigurationConstants.ATTRIBUTE_RULE_TYPE + "]");
    		relSelects.add("tomid["
    				+ ProductLineConstants.RELATIONSHIP_LEFT_EXPRESSION
    				+ "].from.attribute["
    				+ ConfigurationConstants.ATTRIBUTE_RULE_COMPLEXITY + "]");
    		String dvList= "tomid["
    			+ ProductLineConstants.RELATIONSHIP_LEFT_EXPRESSION
    			+ "].from.attribute["
    			+ ConfigurationConstants.ATTRIBUTE_DESIGNVARIANTS + "]";
    		relSelects.add(dvList);
    		relSelects.add("tomid["
    			+ ProductLineConstants.RELATIONSHIP_LEFT_EXPRESSION
    			+ "].from.attribute["
    			+ ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION + "]");
    		relSelects.add(ConfigurationConstants.SELECT_ATTRIBUTE_RULE_TYPE);



    	}else if(REL_SELECT_PART_INACTIVE== relSelect){
    		relSelects.add(inactivedvselectable);
    		relSelects.add("tomid["
    				+ ProductLineConstants.RELATIONSHIP_LEFT_EXPRESSION
    				+ "].from.attribute["
    				+ ConfigurationConstants.ATTRIBUTE_RULE_COMPLEXITY + "]");
    		relSelects.add("frommid[" + ConfigurationConstants.RELATIONSHIP_REPLACED_BY + "].torel.to.name");
    		relSelects.add("frommid[" + ConfigurationConstants.RELATIONSHIP_REPLACED_BY + "].frommid[" + ConfigurationConstants.RELATIONSHIP_AUTHORIZING_EC + "].to.name");
    		relSelects.add("attribute["
    				+ ConfigurationConstants.ATTRIBUTE_RULE_TYPE + "]");
    		relSelects.add("tomid["
    				+ ProductLineConstants.RELATIONSHIP_LEFT_EXPRESSION
    				+ "].from.id");
    		relSelects.add(DomainRelationship.SELECT_FROM_ID);
    		relSelects.add(DomainRelationship.SELECT_FROM_TYPE);
    	}else if(REL_SELECT_DV_INACTIVE== relSelect){
    		relSelects.add(inactivedvselectable);
    		DomainRelationship.MULTI_VALUE_LIST.add(inactivedvselectable2);
    		relSelects.add(inactivedvselectable2);
    		relSelects.add("tomid["
    				+ ProductLineConstants.RELATIONSHIP_LEFT_EXPRESSION
    				+ "].from.attribute["
    				+ ConfigurationConstants.ATTRIBUTE_RULE_COMPLEXITY + "]");
    		relSelects.add("attribute["
    				+ ConfigurationConstants.ATTRIBUTE_RULE_TYPE + "]");
    		relSelects.add("tomid["
    				+ ProductLineConstants.RELATIONSHIP_LEFT_EXPRESSION
    				+ "].from.id");
    		relSelects.add(DomainRelationship.SELECT_FROM_ID);
    		relSelects.add(DomainRelationship.SELECT_FROM_TYPE);
    	}else if(REL_SELECT_GBOM_SUMMARY== relSelect){
    		relSelects.add(DomainRelationship.SELECT_FROM_ID);
    		relSelects.add(DomainRelationship.SELECT_FROM_TYPE);
    		relSelects.add("tomid["
    				+ ProductLineConstants.RELATIONSHIP_LEFT_EXPRESSION
    				+ "].from.id");
    		relSelects.add("tomid["
    				+ ProductLineConstants.RELATIONSHIP_LEFT_EXPRESSION
    				+ "].from.attribute["
    				+ ConfigurationConstants.ATTRIBUTE_LEFT_EXPRESSION + "]");
    		relSelects.add("tomid["
    				+ ProductLineConstants.RELATIONSHIP_LEFT_EXPRESSION
    				+ "].from.attribute["
    				+ ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION + "]");
    		relSelects.add("tomid["
    				+ ProductLineConstants.RELATIONSHIP_LEFT_EXPRESSION
    				+ "].from.attribute["
    				+ ConfigurationConstants.ATTRIBUTE_RULE_COMPLEXITY + "]");
    		relSelects.add("attribute["
    				+ ConfigurationConstants.ATTRIBUTE_RULE_TYPE + "]");
    		relSelects.add("tomid["
    				+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION
    				+ "].from.type");
    		relSelects.add(dvselectable);

    	}else if(REL_SELECT_ALL== relSelect || relSelect>REL_SELECT_GBOM_SUMMARY){
    		relSelects.add("attribute["
    				+ ConfigurationConstants.ATTRIBUTE_RULE_TYPE + "]");
    		relSelects.add("tomid["
    				+ ProductLineConstants.RELATIONSHIP_LEFT_EXPRESSION
    				+ "].from.attribute["
    				+ ConfigurationConstants.ATTRIBUTE_RULE_COMPLEXITY + "]");
    		String dvList= "tomid["
    			+ ProductLineConstants.RELATIONSHIP_LEFT_EXPRESSION
    			+ "].from.attribute["
    			+ ConfigurationConstants.ATTRIBUTE_DESIGNVARIANTS + "]";
    		relSelects.add(dvList);
    		relSelects.add("tomid["
    				+ ProductLineConstants.RELATIONSHIP_LEFT_EXPRESSION
    				+ "].from.id");
    		relSelects.add("tomid["
    				+ ProductLineConstants.RELATIONSHIP_LEFT_EXPRESSION
    				+ "].from.attribute["
    				+ ConfigurationConstants.ATTRIBUTE_LEFT_EXPRESSION + "]");
    		relSelects.add("tomid["
    				+ ProductLineConstants.RELATIONSHIP_LEFT_EXPRESSION
    				+ "].from.attribute["
    				+ ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION + "]");
    		relSelects.add(DomainRelationship.SELECT_FROM_ID);
    		relSelects.add(dvselectable);
    		relSelects.add(inactivedvselectable);
    		//
    		DomainRelationship.MULTI_VALUE_LIST.add("from.from["
    				+ ConfigurationConstants.RELATIONSHIP_INACTIVE_VARIES_BY + "].to.name");
    		relSelects.add("from.from["
    				+ ConfigurationConstants.RELATIONSHIP_INACTIVE_VARIES_BY + "].to.name");
    		relSelects.add("frommid[" + ConfigurationConstants.RELATIONSHIP_REPLACED_BY + "].torel.to.name");
    		relSelects.add("frommid[" + ConfigurationConstants.RELATIONSHIP_REPLACED_BY + "].frommid[" + ConfigurationConstants.RELATIONSHIP_AUTHORIZING_EC + "].to.name");

    	}

    	return relSelects;
    }

	/**
	 * This will be column JPO used to render the GBOM status on GBOM page.
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 * @since R211
	 */
	public List getGBOMStatus(Context context, String args[]) throws Exception {
		List statuslist = new StringList();
		String status = DomainConstants.EMPTY_STRING;
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList lstObjectIdsList = (MapList) programMap.get("objectList");
		String strRelationship = DomainConstants.EMPTY_STRING;
		String strLocale = context.getSession().getLanguage();

		for (int i = 0; i < lstObjectIdsList.size(); i++) {
			Map tempMap = (Map) lstObjectIdsList.get(i);
			strRelationship = (String) tempMap.get("relationship");
			//IF relationship is GBOM means its Active/ for Inactive GBOM relationship it's Inactive
			if (strRelationship
					.equalsIgnoreCase(ConfigurationConstants.RELATIONSHIP_GBOM))

				status = EnoviaResourceBundle.getProperty(context,SUITE_KEY,
						 PART_ACTIVE_STATUS,strLocale);
			else
				status = EnoviaResourceBundle.getProperty(context,SUITE_KEY,
						 PART_INACTIVE_STATUS,strLocale);
			statuslist.add(status);
		}
		return statuslist;
	}

	/**
	 * This is the coloumn JPO method, used to render the Inactive Variants Name in Inactive variants Tab
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 * @since R211
	 */
	public List getInactiveVariantsList(Context context, String[] args)throws Exception

	{
		List variantList = new StringList();
		Map tempMap = null;
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList lstObjectIdsList = (MapList) programMap.get("objectList");

		for(int i=0; i<lstObjectIdsList.size();i++)
		{
			tempMap = (Map)lstObjectIdsList.get(i);
			StringList strINVariesByName = new StringList();
			StringBuffer returnSB = new StringBuffer();
			String inactivedvselectable = "tomid["
			                        			+ ConfigurationConstants.RELATIONSHIP_INACTIVE_VARIES_BY_GBOM + "].fromrel.to.name";
			if(tempMap.containsKey(inactivedvselectable)){
				if (tempMap.get(inactivedvselectable) instanceof StringList) {
					strINVariesByName = (StringList) tempMap.get(inactivedvselectable);
				} else if (tempMap.get(inactivedvselectable) instanceof String) {
					String strConfOptionsOLDRelId = (String) tempMap.get(inactivedvselectable);
					strINVariesByName.add(strConfOptionsOLDRelId);
				}
				int j = 0;
				for (Iterator itrIVariesByItr = strINVariesByName.iterator(); itrIVariesByItr.hasNext();) {
					String strDVName = (String) itrIVariesByItr.next();
					if(strDVName != null){
						strDVName = strDVName.trim();
						if(returnSB.indexOf(strDVName) == -1){
							returnSB = returnSB.append(strDVName);
							if (strINVariesByName.size() > 1 && j < strINVariesByName.size() ){
								returnSB = returnSB.append(STR_COMMA); //to avoid comma after last entry
							}
						}
					}
					j++;
				}
				String strInactiveDV= returnSB.toString();
				if(strInactiveDV.endsWith(STR_COMMA)){
					strInactiveDV = strInactiveDV.substring(0, strInactiveDV.length() - 1);
				}
				variantList.add(strInactiveDV);
			}else{
				variantList.add(DomainConstants.EMPTY_STRING);
			}
		}
		return variantList;
	}
    /**
     * This is the coloumn JPO method, used to render the Replaced By Part's name in Inactive Parts Tab
     * @param context
     * @param args
     * @return
     * @throws Exception
     * @since R211
     * @deprecated in R418
     */
    public List getReplacedByPartName(Context context, String[] args)throws Exception
    {
    	HashMap programMap = (HashMap) JPO.unpackArgs(args);
    	MapList lstObjectIdsList = (MapList) programMap.get("objectList");
    	String strRBPartName="";

    	int size = lstObjectIdsList.size();
    	List resultList = new StringList(size);
    	for (int i = 0; i < size; i++)
    	{
    		Map tempMap = (Map)lstObjectIdsList.get(i);
    		strRBPartName= (String)tempMap.get("frommid[" + ConfigurationConstants.RELATIONSHIP_REPLACED_BY + "].torel.to.name");
    		if(!nullOrBlank(strRBPartName)){
    			resultList.add(strRBPartName);
    		}else{
    			resultList.add("");
    		}
    	}
    	return resultList;
    }

    private boolean nullOrBlank(String str)
    {
       return (str == null || str.trim().length() == 0 || str.equalsIgnoreCase("null"));
    }

    /**
     * This is the coloumn JPO method, used to render the Authorizing EC's name in Inactive Parts Tab
     * @param context
     * @param args
     * @return
     * @throws Exception
     * @since R211
     * @deprecated in R418
     */
    public List getAuthorizingECName(Context context, String[] args)throws Exception
    {
    	HashMap programMap = (HashMap) JPO.unpackArgs(args);
    	MapList lstObjectIdsList = (MapList) programMap.get("objectList");
    	String authorizingECName= "";
    	int size = lstObjectIdsList.size();
    	List resultList = new StringList(size);
    	for (int i = 0; i < size; i++)
    	{
    		Map tempMap = (Map)lstObjectIdsList.get(i);
    		authorizingECName= (String)tempMap.get("frommid[" + ConfigurationConstants.RELATIONSHIP_REPLACED_BY + "].frommid[" + ConfigurationConstants.RELATIONSHIP_AUTHORIZING_EC + "].to.name");
    		if(!nullOrBlank(authorizingECName)){
    			resultList.add(authorizingECName);
    		}else{
    			resultList.add("");
    		}
    	}
    	return resultList;
    }
    /**
     * This method is used to control the access levels for Part family commands in the GBOM Part Table
     * @param context The ematrix context object
     * @param String The args
     * @return Boolean
     * @since R211
     */
    public static Boolean isLogicalFeatureConnectedToVariants(Context context, String[] args )throws Exception
    {
    	boolean isConnectedToDV= false;
    	HashMap requestMap = (HashMap) JPO.unpackArgs(args);
    	String objectId = (String) requestMap.get("objectId");
    	LogicalFeature logicalFTR= new LogicalFeature(objectId);
    	DomainObject domObject= new DomainObject(objectId);
    	String strLeftFeatureType = domObject.getInfo(context,
    			DomainObject.SELECT_TYPE);
    	//in case if called from Product context- do not need to create IR if it has DV
    	if (mxType.isOfParentType(context, strLeftFeatureType,
    			ConfigurationConstants.TYPE_PRODUCTS)) {
    		return true;
    	}
    	isConnectedToDV= logicalFTR.isConnectedToDesignVariants(context);
    	return isConnectedToDV;
    }
    /**
     * To obtain the list of Object IDs to be excluded from the search for Add Existing Actions
     * for Part Family
     *
     * @param context- the eMatrix <code>Context</code> object
     * @param args- holds the HashMap containing the following arguments
     * @return  StringList- consisting of the object ids to be excluded from the Search Results
     * @throws Exception if the operation fails
     */
    @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
    public StringList excludeConnectedPartfamilies(Context context, String[] args) throws Exception
    {
        Map programMap = (Map) JPO.unpackArgs(args);
        String strObjectIds = (String)programMap.get("objectId");
        StringList excludeList= new StringList();
    	String typePattern = DomainConstants.TYPE_PART_FAMILY;
    	String relPattern = ConfigurationConstants.RELATIONSHIP_GBOM;

    	LogicalFeature logicalFTR= new LogicalFeature(strObjectIds);
    	StringList relSelects= new StringList();
    	StringList objSelects= new StringList();
    	MapList objectList = logicalFTR.getActiveGBOMStructure(context,
    			typePattern, relPattern, objSelects, relSelects, false, true,
    			1, 0, DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING, DomainObject.FILTER_STR_AND_ITEM,
    	"");

    	for(int i=0;i<objectList.size();i++){
            Map tempMap=(Map)objectList.get(i);
            excludeList.add(tempMap.get(ConfigurationConstants.SELECT_ID));
        }
        excludeList.add(strObjectIds);

        return excludeList;
    }


    /**
     * Determine if the Replace command for the GBOM table is visible.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args contains the feature object ID
     * @returns Boolean
     * @throws Exception if the operation fails
     * @since FTR R212
     * @deprecated in R418
     */
    public static Boolean showGBOMReplaceCommand(Context context, String[] args) throws Exception
    {
       HashMap paramMap = (HashMap) JPO.unpackArgs(args);
       String strContextOId = (String)paramMap.get("objectId");

       //get the current state for the context object
       String strCurrent = new DomainObject(strContextOId).getInfo(context,DomainConstants.SELECT_CURRENT);

       String stateRelease = PropertyUtil.getSchemaProperty(context,"policy",
    		   ConfigurationConstants.POLICY_LOGICAL_FEATURE,
       "state_Release");
       String stateObsolete = com.matrixone.apps.domain.util.PropertyUtil.getSchemaProperty(context,"policy",
    		   ConfigurationConstants.POLICY_LOGICAL_FEATURE,
       "state_Obsolete");

       boolean isReleased = stateRelease.equals(strCurrent);
       boolean isObsolete = stateObsolete.equals(strCurrent);

       //get the flag value
       String updateReleasedFeatureEnabled = com.matrixone.apps.domain.util.EnoviaResourceBundle.getProperty(context,"emxConfiguration.GBOMPartUpdateAllowedForReleasedFeature");

       //(1) The command is always visible if the feature is neither released nor obsolete,
       //    regardless of the value for the flag - same existing behavior
       //(2) If the feature is released, the command is visible only if the flag has value true
       //(3) If the feature is obsolete, the command is invisible - the same existing behavior
       boolean showCommand = false;
       if (!isReleased && !isObsolete)
          showCommand = true;
       else if (isReleased && "true".equalsIgnoreCase(updateReleasedFeatureEnabled))
          showCommand = true;

       return Boolean.valueOf(showCommand);
    }
    /**
     * Access Function used while rendering Authorizing EC field
     * @param context
     * @param args
     * @return
     * @throws Exception
     * @deprecated in R418
     */
    public  boolean isAuthorizingECRequired(Context context ,String [] args) throws Exception{
    	boolean isAuthorizingECRequired= false;
    	try {
    		HashMap paramMap = (HashMap) JPO.unpackArgs(args);
    		String strContextOId = (String)paramMap.get("parentOID");
    		DomainObject domContextObj = new DomainObject(strContextOId);
    		String curState = domContextObj.getInfo(context, DomainConstants.SELECT_CURRENT);
    		String stateRelease = com.matrixone.apps.domain.util.PropertyUtil.getSchemaProperty(context,"policy",
    				ConfigurationConstants.POLICY_LOGICAL_FEATURE,
    		"state_Release");
    		String updateReleasedFeatureEnabled = EnoviaResourceBundle.getProperty(context,"emxConfiguration.GBOMPartUpdateAllowedForReleasedFeature");
    		//Authorizing EC is required if and only if the feature is released and the flag is true.
    		isAuthorizingECRequired = stateRelease.equals(curState) && "true".equalsIgnoreCase(updateReleasedFeatureEnabled);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return isAuthorizingECRequired;
    }

  	/**
	 * This Method is used to get the range HREF for GBOM replace ,it will be for Part
	 *
     */
    public String getGBOMReplaceRangeHref(Context context, String[] args) throws Exception{
    	Map programMap = (HashMap) JPO.unpackArgs(args);
    	Map fieldMap = (HashMap) programMap.get("fieldMap");
    	String strFieldName = (String)fieldMap.get("name");
    	Map paramMap = (HashMap) programMap.get("paramMap");
    	String objectId = (String) paramMap.get("objectId");
    	HashMap requestMap = (HashMap) programMap.get("requestMap");
    	String contextObjectId = (String) requestMap.get("parentOID");
    	String [] strOIDs= new String[]{objectId};
    	StringList slObjSel= new StringList(DomainConstants.SELECT_TYPE);
    	slObjSel.add(DomainConstants.SELECT_NAME);
    	MapList mlSourceGBOMInfo=DomainObject.getInfo(context, strOIDs, slObjSel);
    	String defaultDestValue="";
    	String sourceType="";
    	if(mlSourceGBOMInfo.size()>0){
    		Map mpSourceGBOM= (Map)mlSourceGBOMInfo.get(0);
    		defaultDestValue=mpSourceGBOM.get(DomainConstants.SELECT_NAME).toString();
    		sourceType=mpSourceGBOM.get(DomainConstants.SELECT_TYPE).toString();
    	}
    	String strTypes="";
    	if (mxType.isOfParentType(context, sourceType,
    			ConfigurationConstants.TYPE_PART_FAMILY)) {
    		strTypes = "type_Part,type_Products,type_PartFamily";
    	}else{
    		strTypes = "type_Part,type_Products";
    	}
    	StringBuffer sbBuffer  = new StringBuffer();
    	sbBuffer.append("<input type=\"text\" READONLY ");
    	sbBuffer.append("name=\"");
    	sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context, strFieldName));
    	sbBuffer.append("Display\" id=\"\" value=\"");
    	sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context, defaultDestValue));
    	sbBuffer.append("\">");
    	sbBuffer.append("<input type=\"hidden\" name=\"");
    	sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context, strFieldName));
    	sbBuffer.append("\" value=\"");
    	sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context, defaultDestValue));
    	sbBuffer.append("\">");
    	sbBuffer.append("<input type=\"hidden\" name=\"");
    	sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context, strFieldName));
    	sbBuffer.append("OID\" value=\"");
    	sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context, objectId));
    	sbBuffer.append("\">");
    	sbBuffer.append("<input ");
    	sbBuffer.append("type=\"button\" name=\"btnObject2\" ");
    	sbBuffer.append("size=\"200\" value=\"...\" alt=\"\" enabled=\"true\" ");
    	sbBuffer.append("onClick=\"javascript:showChooser('../common/emxFullSearch.jsp?objectId="+XSSUtil.encodeForHTMLAttribute(context, objectId)+"&excludeOID="+XSSUtil.encodeForHTMLAttribute(context, objectId)+"&contextObjectId="+XSSUtil.encodeForHTMLAttribute(context, contextObjectId)+"&suiteKey=Configuration");
    	sbBuffer.append("&field=TYPES="+strTypes);
    	sbBuffer.append(":POLICY!=policy_ConfiguredPart:CURRENT!=policy_DevelopmentPart.state_Obsolete,policy_StandardPart.state_Obsolete,policy_Classification.state_Obsolete");
    	sbBuffer.append("&table=PLCSearchPartsTable");
    	sbBuffer.append("&Registered Suite=Configuration");
    	sbBuffer.append("&selection=single");
    	sbBuffer.append("&hideHeader=true");
    	sbBuffer.append("&submitURL=../configuration/SearchUtil.jsp?mode=Chooser");
    	sbBuffer.append("&chooserType=FormChooser");
    	//TODO- do we need this
    	if (mxType.isOfParentType(context, sourceType,
    			ConfigurationConstants.TYPE_PART_FAMILY)) {
    		sbBuffer.append("&fieldNameActual=ReplaceWithOID");
    		sbBuffer.append("&fieldNameDisplay=ReplaceWithDisplay");
    	}else{
    		sbBuffer.append("&fieldNameActual=ReplaceWith2OID");
    		sbBuffer.append("&fieldNameDisplay=ReplaceWith2Display");
    	}
    	sbBuffer.append("&HelpMarker=emxhelpfullsearch','850','630')\">");
    	return sbBuffer.toString();
    }
  /**
   * Check Trigger used check if Part is used in BOM
   * @param context
   * @param args
   * @return
   * @throws Exception
   * @sience R212
   */
    public int isPartUsedInBOMCheck(Context context, String[] args) throws Exception{
    	int iResult = 0;
    	String selectedPartID= args[0];
    	String featureID = args[1];
    	String strRelPattern = "";
    	StringList objSelects = new StringList();
    	StringList lstPCs = new StringList();
    	short sRecurse = 1;
    	DomainObject domObjFeature = new DomainObject(featureID);
    	String strLanguage = context.getSession().getLanguage();
    	String strSubjectKey = EnoviaResourceBundle.getProperty(context,
    			SUITE_KEY,"emxProduct.Alert.UsedInBOMMakeInactive", strLanguage);
    	//getting all the Product Configurations connected to the Feature
    	objSelects.add(DomainConstants.SELECT_ID);

    	// need to change the relationship to Product Configuration to handle the PCs created directly under Logical Feature,
    	// Product, Product Variants.
    	MapList mapListPC1 = domObjFeature.getRelatedObjects(context,
    			ProductLineConstants.RELATIONSHIP_PRODUCT_CONFIGURATION,
    			ProductLineConstants.TYPE_PRODUCT_CONFIGURATION,
    			objSelects,
    			null,
    			true,
    			false,
    			sRecurse,
    			DomainConstants.EMPTY_STRING,
    			DomainConstants.EMPTY_STRING, 0);
    	//return all the PC connected with context Object
    	for(int i=0;i<mapListPC1.size();i++)
    	{
    		String strPC1 = (String)((Map)mapListPC1.get(i)).get(DomainConstants.SELECT_ID);
    		lstPCs.add(strPC1);
    	}
    	//getting all the Product Configurations connected to the Logical Features and configuration Feature
    	strRelPattern = ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES;
    	String strObjPattern = ConfigurationConstants.TYPE_LOGICAL_STRUCTURES+","+ConfigurationConstants.TYPE_PRODUCTS;
    	StringList relSelect= new StringList(DomainRelationship.SELECT_ID);
    	relSelect.addElement("tomid["+ProductLineConstants.RELATIONSHIP_SELECTED_OPTIONS+"].from.id");

    	MapList mapListLogicalFtrs = domObjFeature.getRelatedObjects(context,
    			strRelPattern,
    			strObjPattern,
    			objSelects,
    			relSelect,
    			true,
    			false,
    			sRecurse,
    			DomainConstants.EMPTY_STRING,
    			DomainConstants.EMPTY_STRING, 0);
    	for(int j=0;j<mapListLogicalFtrs.size();j++)
    	{
    		String strPC2 = (String)((Map)mapListLogicalFtrs.get(j)).get(relSelect.get(1));
    		if(strPC2!=null && ! lstPCs.contains(strPC2))
    			lstPCs.add(strPC2);
    	}
    	//getting all the Parts referred by Product Configuration and EBOM
    	for(int i=0;i<lstPCs.size();i++)
    	{
    		String strPC = (String)lstPCs.get(i);
    		String strObjWhere = "("+DomainConstants.SELECT_ID + "==" + selectedPartID + ")";
    		// add the part id in object where to retrieve only the part being removed.
    		MapList mapListTopLevelParts = new DomainObject(strPC).getRelatedObjects(context,
    				ProductLineConstants.RELATIONSHIP_TOP_LEVEL_PART,
    				ProductLineConstants.TYPE_PART,
    				objSelects,
    				null,
    				false,
    				true,
    				sRecurse,
    				strObjWhere,
    				DomainConstants.EMPTY_STRING,0);
    		if(!mapListTopLevelParts.isEmpty()){
    			StringList slTopLevelParts= new StringList(mapListTopLevelParts.size());
    			for(int j=0; j<mapListTopLevelParts.size();j++){
    				String strTopLevelPart = (String)((Map)mapListTopLevelParts.get(j)).get(DomainConstants.SELECT_ID);
    				slTopLevelParts.add(strTopLevelPart);
    			}
    			if(!slTopLevelParts.isEmpty() && slTopLevelParts.contains(selectedPartID)){
    				throw new FrameworkException(strSubjectKey);
    			}
    			for(int j=0; j<slTopLevelParts.size();j++){
    				MapList mapListParts = new DomainObject((String)slTopLevelParts.get(j)).getRelatedObjects(context,
    						ProductLineConstants.RELATIONSHIP_EBOM,
    						ProductLineConstants.TYPE_PART,
    						objSelects,
    						null,
    						false,
    						true,
    						(short)0,
    						strObjWhere,
    						DomainConstants.EMPTY_STRING,0);
    				if(!mapListParts.isEmpty()){
    					StringList slEBOMParts= new StringList(mapListParts.size());
    					for(int k=0; k<mapListParts.size();k++){
    						String strEBOMPart = (String)((Map)slEBOMParts.get(j)).get(DomainConstants.SELECT_ID);
    						slEBOMParts.add(strEBOMPart);
    					}
    					if(!slEBOMParts.isEmpty() && slEBOMParts.contains(selectedPartID)){
    						throw new FrameworkException(strSubjectKey);
    					}
    				}
    			}
    		}
    	}
    	return iResult;
    }

    /**
     * Method to search Parts in Replace GBOM in Feature context
     * @param context
     * @throws Exception
     * @sience R212
     * Not Used- Can remove this exclude prg
     */

   public StringList excludePartforReplace(Context context, String [] args)
   throws Exception
   {
	   Map programMap = (Map) JPO.unpackArgs(args);
	   //Logical Feature ID
	   String strSourceObjectId = (String) programMap.get("contextObjectId");
	   LogicalFeature compFtr = new LogicalFeature(strSourceObjectId);
	   String relWhere = DomainObject.EMPTY_STRING;
	   String objWhere = DomainObject.EMPTY_STRING;
	   // Obj and Rel pattern
	   String typePattern = DomainObject.EMPTY_STRING;
	   String relPattern = DomainObject.EMPTY_STRING;

	   // Obj and Rel Selects
	   StringList objSelects = new StringList();
	   StringList relSelects = new StringList();

	   int iLevel = ConfigurationUtil.getLevelfromSB(context, args);
	   String filterExpression = DomainObject.EMPTY_STRING;

	   // retrieve Active Inactive GBOM
	   MapList objectList = compFtr.getGBOMStructure(context, typePattern,
			   relPattern, objSelects, relSelects, false, true, iLevel, 0,
			   objWhere, relWhere, DomainObject.FILTER_STR_AND_ITEM, filterExpression);

	   StringList gbomToExclude = new StringList();

	   for(int i=0;i<objectList.size();i++)
	   {
		   Map mapPartObj = (Map) objectList.get(i);
		   if(mapPartObj.containsKey(DomainObject.SELECT_ID))
		   {
			   String partIDToExclude = (String)mapPartObj.get(DomainObject.SELECT_ID);
			   gbomToExclude.add(partIDToExclude);
		   }
	   }

	   return gbomToExclude;
   }
   /**
    * This create range href for Authorizing EC chooser in GBOM replace
    * @param context
    * @param args
    * @return
    * @throws Exception
    * @sience R212
    */
   public String getGBOMAuthorizingECRangeHref(Context context, String[] args) throws Exception{
   	Map programMap = (HashMap) JPO.unpackArgs(args);
   	Map fieldMap = (HashMap) programMap.get("fieldMap");
   	String strFieldName = (String)fieldMap.get("name");
   	Map paramMap = (HashMap) programMap.get("paramMap");
   	String objectId = (String) paramMap.get("objectId");
   	HashMap requestMap = (HashMap) programMap.get("requestMap");
   	String contextObjectId = (String) requestMap.get("parentOID");
   	String userID = context.getUser();
   	String strTypes="type_EngineeringChange";
   	StringBuffer sbBuffer  = new StringBuffer();
   	sbBuffer.append("<input type=\"text\" READONLY ");
   	sbBuffer.append("name=\"");
   	sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context, strFieldName));
   	sbBuffer.append("Display\" id=\"\" value=\"");
   	sbBuffer.append("");
   	sbBuffer.append("\">");
   	sbBuffer.append("<input type=\"hidden\" name=\"");
   	sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context, strFieldName));
   	sbBuffer.append("\" value=\"");
   	sbBuffer.append("");
   	sbBuffer.append("\">");
   	sbBuffer.append("<input type=\"hidden\" name=\"");
   	sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context, strFieldName));
   	sbBuffer.append("OID\" value=\"");
   	sbBuffer.append("");
   	sbBuffer.append("\">");
   	sbBuffer.append("<input ");
   	sbBuffer.append("type=\"button\" name=\"btnObject2\" ");
   	sbBuffer.append("size=\"200\" value=\"...\" alt=\"\" enabled=\"true\" ");
   	sbBuffer.append("onClick=\"javascript:showChooser('../common/emxFullSearch.jsp?objectId="+XSSUtil.encodeForHTMLAttribute(context, objectId)+"&excludeOID="+XSSUtil.encodeForHTMLAttribute(context, objectId)+"&contextObjectId="+XSSUtil.encodeForHTMLAttribute(context, contextObjectId)+"&suiteKey=Configuration");
   	sbBuffer.append("&field=TYPES="+strTypes);
   	sbBuffer.append(":OWNER="+XSSUtil.encodeForHTMLAttribute(context, userID));
   	sbBuffer.append(":CURRENT!=policy_EngineeringChangeStandard.state_Complete,policy_EngineeringChangeStandard.state_Close,policy_EngineeringChangeStandard.state_Reject,policy_SoftwareEC.state_Complete,policy_SoftwareEC.state_Close,policy_SoftwareEC.state_Reject,policy_HardwareEC.state_Cancelled");
   	sbBuffer.append("&table=PLCSearchPartsTable");
   	sbBuffer.append("&Registered Suite=Configuration");
   	sbBuffer.append("&selection=single");
   	sbBuffer.append("&excludeOIDprogram=emxFTRPart:getSelfOwnedOpenReportedAgainstECs");
   	sbBuffer.append("&hideHeader=true");
   	sbBuffer.append("&submitURL=../configuration/SearchUtil.jsp?mode=Chooser");
   	sbBuffer.append("&chooserType=FormChooser");
   	sbBuffer.append("&fieldNameActual=AuthorizingECOID");
   	sbBuffer.append("&fieldNameDisplay=AuthorizingECDisplay");
   	sbBuffer.append("&HelpMarker=emxhelpfullsearch','850','630')\">");
   	sbBuffer.append("&nbsp;&nbsp;");
   	sbBuffer.append("<a href=\"javascript:basicClear('");
   	sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context, strFieldName));
   	sbBuffer.append("')\">");
   	String strClear =EnoviaResourceBundle.getProperty(context, SUITE_KEY,
   				"emxProduct.Button.Clear",
   				context.getSession().getLanguage());
   	sbBuffer.append(strClear);
   	sbBuffer.append("</a>");
   	return sbBuffer.toString();
   }
   /**
    * Column JPO Used to render group number
    * @param context
    * @param args
    * @return
    * @throws Exception
    * @sience R212
    */
   public List getGroupNumberList(Context context, String args[])
   throws Exception
   {
	   if (args.length == 0) {
		   throw new IllegalArgumentException();
	   }
	   StringList data = new StringList();
	   try {
		   HashMap programMap = (HashMap) JPO.unpackArgs(args);
		   MapList objectList = (MapList) programMap.get("objectList");
		   
		   for (int i = 0; i < objectList.size(); i++) {
			   Map mapPart = (Map) objectList.get(i);
			   String strGrpNumber = "";
			   strGrpNumber = (String) mapPart.get("GroupNumber");
			   if (strGrpNumber != null)
				   data.add(strGrpNumber);
			   else
				   data.add("");
		   }
	   } catch (Exception e) {
		   e.printStackTrace();
	   }
	   return data;
}

   /**
    * Method used to get the Part Number for the Equipment reporte.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args contains the feature object ID
    * @returns vector
    * @throws Exception if the operation fails
    * @since FTR R207- Used as it is in FTR 212
    */

   public Vector displayGBOMPartNumber(Context context, String args[])throws Exception{
       HashMap parametersMap = (HashMap) JPO.unpackArgs(args);
       MapList objectList = (MapList) parametersMap.get("objectList");
       Vector valueList = new Vector();
       Map map = null;
       for(int i =0; i<objectList.size(); i++){
           map = (Map)objectList.get(i);
           String columnValue = (String)map.get("GBOMPartNumber");
           valueList.add(columnValue);
       }
       return valueList;
   }


   /**
    * Method used to get the Part Description for the Equipment reporte.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args contains the feature object ID
    * @returns vector
    * @throws Exception if the operation fails
    * @since FTR R207-Used as it is in FTR 212
    */

   public Vector displayPartDescription(Context context, String args[])throws Exception{
       HashMap parametersMap = (HashMap) JPO.unpackArgs(args);
       MapList objectList = (MapList) parametersMap.get("objectList");
       Vector valueList = new Vector();
       Map map = null;
       for(int i =0; i<objectList.size(); i++){
           map = (Map)objectList.get(i);
           String columnValue = (String)map.get("PartDescription");
           valueList.add(columnValue);
       }
       return valueList;
   }

   /**
    * Method used to get the Part Revision for the Equipment reporte.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args contains the feature object ID
    * @returns vector
    * @throws Exception if the operation fails
    * @since FTR R207-Used as it is in FTR 212
    */


   public Vector displayPartRevision(Context context, String args[])throws Exception{
       HashMap parametersMap = (HashMap) JPO.unpackArgs(args);
       MapList objectList = (MapList) parametersMap.get("objectList");
       Vector valueList = new Vector();
       Map map = null;
       for(int i =0; i<objectList.size(); i++){
           map = (Map)objectList.get(i);
           String columnValue = (String)map.get("PartRevision");
           valueList.add(columnValue);
       }
       return valueList;
   }

   /**
    * Method used to get the Part State for the Equipment reporte.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args contains the feature object ID
    * @returns vector
    * @throws Exception if the operation fails
    * @since FTR R207-Used as it is in FTR 212
    */


   public Vector displayPartState(Context context, String args[])throws Exception{
	   HashMap parametersMap = (HashMap) JPO.unpackArgs(args);
	   MapList objectList = (MapList) parametersMap.get("objectList");
	   Vector valueList = new Vector();
	   Map map = null;
	   String strLanguage = context.getSession().getLanguage();
	   String strState ="";
	   String stateKey = "";
	   for(int i =0; i<objectList.size(); i++){
		   map = (Map)objectList.get(i);
		   strState = (String)map.get("PartState");
		   stateKey = "emxFramework.State."+strState;
		   valueList.add(EnoviaResourceBundle.getProperty(context, "Framework",stateKey, strLanguage));
	   }
	   return valueList;
   }

   /**
    * Method used to get the Engineering Change column for the Equipment reporte.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args contains the feature object ID
    * @returns vector
    * @throws Exception if the operation fails
    * @since FTR R207-will be used in R212-
    */


   public Vector displayEngineeringChange(Context context, String args[])throws Exception{
       HashMap parametersMap = (HashMap) JPO.unpackArgs(args);
       MapList objectList = (MapList) parametersMap.get("objectList");
       Vector valueList = new Vector();
       Map map = null;
       for(int i =0; i<objectList.size(); i++){
           map = (Map)objectList.get(i);
           String columnValue = (String)map.get("Engchg");
           valueList.add(columnValue);
       }
       return valueList;
   }

   /**
    * Method used to get the Librart Path of the Equipment Feature for the Equipment report.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args contains the feature object ID
    * @returns List
    * @throws Exception if the operation fails
    * @since FTR R207- will be used in R212-
    */

   public List showLibraryPath(Context context, String[] args) throws Exception
   {

       HashMap programMap = (HashMap) JPO.unpackArgs(args);
       List lstObjectIdsList = (MapList) programMap.get("objectList");
       Map tempMap;

       List lstLibrartPath = new StringList();
       try {

           String strObjId = DomainConstants.EMPTY_STRING;
           for(int i=0; i<lstObjectIdsList.size();i++)
           {
               tempMap = (Map)lstObjectIdsList.get(i);
               strObjId = (String)tempMap.get(DomainConstants.SELECT_ID);

               if(strObjId!=null)
               {
                   DomainObject domObj = new DomainObject(strObjId);
                   StringList slTempId = new StringList(DomainConstants.SELECT_ID);
                   slTempId.addElement(DomainConstants.SELECT_NAME);

                   StringList slTempRel = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);

                   MapList mlRelObj = new MapList();
                   StringBuffer sbRelPattern = new StringBuffer(ConfigurationConstants.RELATIONSHIP_CLASSIFIED_ITEM);


                   String strRelSubclass= PropertyUtil.getSchemaProperty(context,"relationship_Subclass");

                   String strRelPattern = sbRelPattern.toString();
                   StringBuffer sbTypePattern = new StringBuffer(ConfigurationConstants.TYPE_CLASSIFICATION);

                   String strTypePattern = sbTypePattern.toString();

                   mlRelObj = domObj.getRelatedObjects(context,
                                                       strRelPattern,
                                                       strTypePattern,
                                                       slTempId,
                                                       slTempRel,
                                                       true,
                                                       false,
                                                       (short)1,
                                                       DomainConstants.EMPTY_STRING,
                                                       DomainConstants.EMPTY_STRING, 0);
                   if(mlRelObj.size()>0){
                       //sbHref Added before loop for Bug :373767
                       StringBuffer sbHref  = new StringBuffer();
                       for (int k=0; k<mlRelObj.size(); k++)
                       {

                           Map mapListObj = (Map) mlRelObj.get(k);
                           String strTempClassificationName = (String)mapListObj.get(DomainConstants.SELECT_NAME);
                           String strTempClassificationId = (String)mapListObj.get(DomainConstants.SELECT_ID);

                           // create link for library if any

                           if(strTempClassificationId!=null){
                               DomainObject domClass = new DomainObject(strTempClassificationId);
                               String strLibraryId = domClass.getInfo(context,"to["+strRelSubclass+"].from.id");
                               String strLibraryName = domClass.getInfo(context,"to["+strRelSubclass+"].from.name");

                               if(strLibraryId!=null){
                                   sbHref.append("<A class=\"object\" HREF=\"javascript:showDetailsPopup('../common/emxTree.jsp?objectId=");
                                   sbHref.append(XSSUtil.encodeForHTMLAttribute(context,strLibraryId));
                                   sbHref.append("&amp;mode=replace");
                                   sbHref.append("')\">");
                                   sbHref.append(XSSUtil.encodeForXML(context,strLibraryName));
                                   sbHref.append("</A>");
                                   sbHref.append("<img border=\"0\" src=\"");
                                   sbHref.append("images/iconTreeToArrow.gif");
                                   sbHref.append("\"></img>");
                               }
                               sbHref.append("<A class=\"object\" HREF=\"javascript:showDetailsPopup('../common/emxTree.jsp?objectId=");
                               sbHref.append(XSSUtil.encodeForHTMLAttribute(context,strTempClassificationId));
                               sbHref.append("&amp;mode=replace");
                               sbHref.append("')\">");
                               sbHref.append(XSSUtil.encodeForXML(context,strTempClassificationName));
                               sbHref.append("</A>");
                               //Added for Bug :373767
                               if( k != mlRelObj.size()-1)
                               {
                                   sbHref.append(",");
                               }//End of block for Bug :373767
                            }
                        }
                       lstLibrartPath.add(sbHref.toString());
                   }else
                   {
                       lstLibrartPath.add("");
                   }

                }
             }
       }
       catch (Exception e) {
          lstLibrartPath.add("");
          e.printStackTrace();
       }
       return lstLibrartPath;
   }
    /**
    * It is used  for updating of Group Number in GBOM table of Merge replace
    * @param context
    * @param args
    * @return Map List contains Part Data also Group Numbers
    * @throws Exception
    */
   @com.matrixone.apps.framework.ui.ProgramCallable
   public MapList getDuplicatePartsForMergeReplace(Context context, String [ ] args)
   throws Exception
   {
       HashMap programMap = (HashMap) JPO.unpackArgs (args) ;

       String strMode = (String) programMap.get("Mode");
       MapList listParts = new MapList(getPartsForSelectedLogicalFeatures(context,args));

       //StringList remPartList = new StringList();
       StringList remPartRelList = new StringList();
       int noOfRemParts = 0;

       try{
       if(programMap.get("NoOfRemoved") != null )
       {
           noOfRemParts = Integer.parseInt((String)programMap.get("NoOfRemoved"));
           for(int i =0; i<noOfRemParts;i++){
               //remPartList.add((String)programMap.get("RemPart"+i));
               remPartRelList.add((String)programMap.get("RemPartRelId"+i));
           }
       }
       
       // code for removing the parts from the map
       for (int i = 0; i < listParts.size (); i++)
       {
           Map mapPart = (Map) listParts.get(i);
           String strPartRelId = (String)mapPart.get(DomainConstants.SELECT_RELATIONSHIP_ID);
           if(remPartRelList.contains(strPartRelId))
           {
               listParts.remove(i);
               i--;
           }
       }
       MapList mpViewDups = (MapList)getDuplicatePartsForMergeReplace(context, args, listParts);

       if(listParts!=null && listParts.size()>0){
           for (Iterator itrOrigPartList = listParts.iterator(); itrOrigPartList.hasNext();) {
               Map mapOrigPart = (Map) itrOrigPartList.next();
               String strPartId = (String) mapOrigPart.get(DomainConstants.SELECT_ID);
               String strPartRelId = (String)mapOrigPart.get(DomainConstants.SELECT_RELATIONSHIP_ID);
               String strGrpNo = (String)mapOrigPart.get("GroupNumber");
               if(mpViewDups!=null && mpViewDups.size()>0){
                   for (Iterator itrDupPartList = mpViewDups.iterator(); itrDupPartList.hasNext();) {
                       Map mapDupPart = (Map) itrDupPartList.next();
                       String strDupPartId = (String) mapDupPart.get(DomainConstants.SELECT_ID);
                       String strDupPartRelId = (String)mapDupPart.get(DomainConstants.SELECT_RELATIONSHIP_ID);
                       String strDupGrpNo = (String)mapDupPart.get("GroupNumber");
                       if(strPartId.equals(strDupPartId) && strPartRelId.equals(strDupPartRelId))
                       {
                           if(strGrpNo != null && !strGrpNo.equals(""))
                           {
                               strGrpNo = strGrpNo + "," +strDupGrpNo;
                           }
                           else
                           {
                               strGrpNo = strDupGrpNo;
                           }
                       }
                   }
               }
               if(strGrpNo == null)
               {
                   strGrpNo = "";
               }
               mapOrigPart.put("GroupNumber",strGrpNo);
           }
        }


       if(strMode!= null && strMode.equals("ViewDuplicate")){
           return mpViewDups;
       }else{
            return listParts;
       }


     }
       catch(Exception e)
       {e.printStackTrace(); return null;}
   }
   /** This method used get GBOM for all selected features in context
    * @param context
    * @param args
    * @return Maplist
    * @since R212
    * @author WKU
    */

   public MapList getPartsForSelectedLogicalFeatures(Context context,
           String args[]) throws Exception
   {
       HashMap programMap = (HashMap) JPO.unpackArgs (args) ;
       MapList masterPartList= new MapList();
       if (args.length == 0)
       {
           throw new IllegalArgumentException () ;
       }
       String strParentId = (String)programMap.get("objectId");
       masterPartList = getActiveGBOMStructure(context, args);
       for(int i = 0; i<masterPartList.size(); i++ )
       {
           Map tempMap = (Map) masterPartList.get(i);
           tempMap.put("ParentFeaID",strParentId);
       }
       String prgType = (String) programMap.get ("editTableMode") ;
       if (prgType != null)
       {

       	return masterPartList ;
       }


       int lengthArr = 0;
       if(programMap.get ("noOfTargetFeatures")!= null)
           lengthArr = Integer.parseInt ((String) programMap.get ("noOfTargetFeatures")) ;

       String objectIDs[] = new String [lengthArr] ;
       for (int i = 0; i < lengthArr; i++)
       {
           Map object = new HashMap () ;
           objectIDs[i] = (String) programMap.get ("objectIDs" + i) ;
           object.put ("objectId", objectIDs[i]) ;
           String [ ] arg = JPO.packArgs (object) ;
           MapList partList = getActiveGBOMStructure(context, arg);
           for(int j = 0; j<partList.size(); j++ )
           {
               Map tempMap = (Map) partList.get(j);
               tempMap.put("ParentFeaID",objectIDs[i]);
           }
           masterPartList.addAll (partList) ;
       }
       return masterPartList ;
   }

   /**
    * This method gets all groups of duplicate parts
    * @param context
    * @param args
    * @return MapList Duplicate Parts
    * @throws Exception
    */
   public MapList getDuplicatePartsForMergeReplace(Context context, String [ ] args, MapList mapParts)
           throws Exception
   {
       try{
           //MapList allParts = new MapList () ;
           String strREList=null;
           String strRuleComplexity =null;
           String strDVList =null;

           MapList finalMap = new MapList();
           for (int i = 0; i < mapParts.size (); i++)
           {

              Map mapPart = (Map) mapParts.get(i);
              if(mapPart.containsKey("tomid["
       					+ ProductLineConstants.RELATIONSHIP_LEFT_EXPRESSION
       					+ "].from.attribute["
       					+ ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION + "]"))
              {
            	  strREList  = (String)mapPart.get("tomid["
       					+ ProductLineConstants.RELATIONSHIP_LEFT_EXPRESSION
       					+ "].from.attribute["
       					+ ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION + "]");
            	  mapPart.put("RightExpressionAttr",strREList.toString());

              }
              else mapPart.put("RightExpressionAttr",DomainConstants.EMPTY_STRING);

               String strPrtName = (String)mapPart.get("name");
               if(mapPart.containsKey("tomid["
   					+ ProductLineConstants.RELATIONSHIP_LEFT_EXPRESSION
   					+ "].from.attribute["
   					+ ConfigurationConstants.ATTRIBUTE_RULE_COMPLEXITY + "]"))
               {
               	strRuleComplexity = (String)mapPart.get("tomid["
       					+ ProductLineConstants.RELATIONSHIP_LEFT_EXPRESSION
       					+ "].from.attribute["
       					+ ConfigurationConstants.ATTRIBUTE_RULE_COMPLEXITY + "]");
               	mapPart.put("RuleComplexity",strRuleComplexity);


               }
               else mapPart.put("RuleComplexity",DomainConstants.EMPTY_STRING);
               if(mapPart.containsKey("tomid["
      					+ ProductLineConstants.RELATIONSHIP_LEFT_EXPRESSION
      					+ "].from.attribute["
      					+ ConfigurationConstants.ATTRIBUTE_DESIGNVARIANTS + "]"))
                  {
            	   strDVList = (String)mapPart.get("tomid["
          					+ ProductLineConstants.RELATIONSHIP_LEFT_EXPRESSION
          					+ "].from.attribute["
          					+ ConfigurationConstants.ATTRIBUTE_DESIGNVARIANTS + "]");
            	   mapPart.put("DesignVariantsAttr",strDVList.toString());

                  }
               else mapPart.put("DesignVariantsAttr",DomainConstants.EMPTY_STRING);




               mapPart.put(DomainConstants.SELECT_NAME,strPrtName);


               String strIncRuleId = (String)mapPart.get("tomid["
                       + ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION
                       + "].from.id");
               if(strIncRuleId!=null && !strIncRuleId.equals("")){
                   mapPart.put("IncRuleId",strIncRuleId);
               }
               finalMap.add(mapPart);
           }

           Map programMap = new HashMap();
           MapList mapPartFamily = new MapList();
           MapList mapUpdatedPartsActual = new MapList();

         	  //Removing PartFamily
             for(int j=0;j<finalMap.size();j++)
             {
             	Map mapUpdatedParts = (Map)finalMap.get(j);
             	String strPartFamily = (String)mapUpdatedParts.get("type");
             	if(strPartFamily.equalsIgnoreCase(ConfigurationConstants.TYPE_PART_FAMILY))
             	{
             		mapPartFamily.add(mapUpdatedParts);
             	}
             	else
             	{
             		mapUpdatedPartsActual.add(mapUpdatedParts);
             	}

             }

             programMap.put("FinalPartList",mapUpdatedPartsActual);


           String[] arrJPOArgs = (String[])JPO.packArgs(programMap);


           MapList mplist = (MapList)getDuplicatePartsForSelectedFeature(context, arrJPOArgs);
           //Adding
           if(mapPartFamily.size()>0)
       	{
       		for(int k=0;k<mapPartFamily.size();k++)
           	{
           		Map mptemp = (Map)mapPartFamily.get(k);
           		mplist.add(mptemp);
           	}

       	}

           return mplist;
      }
      catch(Exception e)
       {e.printStackTrace(); return null;}
   }

   /**
    * Get values for column rule in GBOM table for Merge Replace
    * @param context
    * @param args Parts data
    * @return Rules
    * @throws Exception
    */

   public Object getGBOMInclusionRuleLink(Context context, String[] args)
   throws Exception
   {
       HashMap parametersMap = (HashMap) JPO.unpackArgs(args);
       MapList objectList = (MapList) parametersMap.get("objectList");
       String strRuleType = "";
       Map objectMap;
       Vector vGBOMInclusionRuleLink = new Vector();
       String strParentOID = null;
       StringList objSelects = new StringList();
       StringList relSelects = new StringList();
       for (int i = 0; i < objectList.size(); i++)
       {
       objectMap = (Map) objectList.get(i);
       strParentOID = (String) objectMap.get("ParentFeaID");
       ConfigurationUtil confUtil = new ConfigurationUtil(strParentOID);
       MapList mapListDV = confUtil.getObjectStructure(context,ConfigurationConstants.TYPE_CONFIGURATION_FEATURES,
	   ConfigurationConstants.RELATIONSHIP_VARIES_BY,objSelects,relSelects,false,true,1,0,null,null,(short)0,null);
       String strLocale = context.getSession().getLanguage();
       String strType = (String)objectMap.get("type");

       	   if(strType.equalsIgnoreCase(ConfigurationConstants.TYPE_PART_FAMILY))
       		{
       			strRuleType = EnoviaResourceBundle.getProperty(context,SUITE_KEY, "emxProduct.Rule_Type.ComplexInclusionRule",strLocale );
       		}

       	   else if(mapListDV.size()>0)
           {
               strRuleType = EnoviaResourceBundle.getProperty(context,SUITE_KEY, SIMPLE_INCLUSION_RULE_KEY,strLocale);
           }

    	   else
           {
               strRuleType = EnoviaResourceBundle.getProperty(context,SUITE_KEY, "emxProduct.Rule_Type.ComplexInclusionRule",strLocale);
           }
       vGBOMInclusionRuleLink.add(strRuleType);
       }
       return vGBOMInclusionRuleLink;

   }

   /**
    * Get values for column group number depending upon duplicate condition.
    * @param context
    * @param args Parts Data
    * @return Group Numbers
    * @throws Exception
    */
   public List getGroupNumberGBOMtable(Context context, String args[])
           throws Exception
   {
       if (args.length == 0)
       {
           throw new IllegalArgumentException () ;
       }
       StringList data = new StringList () ;
       try
       {
       HashMap programMap = (HashMap) JPO.unpackArgs (args) ;
       MapList objectList = (MapList) programMap.get ("objectList") ;
       Map paramMap = (Map) programMap.get("paramList");
       String strStep = (String) paramMap.get("Step");

       for (int i = 0; i < objectList.size(); i++)
           {
               Map mapPart = (Map) objectList.get(i);
               String strGrpNumber = "";

               if(strStep.equals("EditGBOM") || strStep.equals("ViewDuplicate"))
                   strGrpNumber = (String) mapPart.get("GroupNumber");
               else
               {
                   if(mapPart.get("PNGroupNumber")!=null)
                       strGrpNumber = (String) mapPart.get("PNGroupNumber");
                   if(mapPart.get("IRGroupNumber")!=null)
                       strGrpNumber = (String) mapPart.get("IRGroupNumber");
               }
               if(strGrpNumber != null)
                   data.add(strGrpNumber);
               else
                   data.add("");
           }
       }
       catch(Exception e)
       {
           e.printStackTrace();
       }
       return data ;
   }

   /**
    * This method is used to retrieve the  Status of Part.
    * @param context The ematrix context object.
    * @param strExpression
    * @param listExpToken
    * @return int
    * @since R212
    */

    @com.matrixone.apps.framework.ui.PostProcessCallable
    public Map postJPOMergeReplaceEdit (Context context,String []args) throws Exception
    {
        Map programMap = (Map) JPO.unpackArgs(args);
        Map returnPorgMap = new HashMap();
        Map tableData = (Map) programMap.get("tableData");
        Map requestMap = (Map) tableData.get("RequestMap");
        MapList listUpdatedParts = (MapList) requestMap.get("UpdatedCells");
        MapList mapPartFamily = new MapList();
        MapList mapUpdatedPartsActual = new MapList();
        try{
      	  //Removing PartFamily
          for(int j=0;j<listUpdatedParts.size();j++)
          {
          	Map mapUpdatedParts = (Map)listUpdatedParts.get(j);
          	String partId = (String)mapUpdatedParts.get("PartId");
          	if(partId== null)
          		partId = (String)mapUpdatedParts.get("id");

          	String strPartFamily =new DomainObject(partId).getInfo(context, ConfigurationConstants.SELECT_TYPE);
          	if(strPartFamily.equalsIgnoreCase(ConfigurationConstants.TYPE_PART_FAMILY))
          	{
          		mapPartFamily.add(mapUpdatedParts);
          	}
          	else
          	{
          		mapUpdatedPartsActual.add(mapUpdatedParts);
          	}

          }
      	    programMap.put("FinalPartList",mapUpdatedPartsActual);

            String[] arrJPOArgs = (String[])JPO.packArgs(programMap);

            MapList mpViewDups = (MapList)getDuplicatePartsForSelectedFeature(context, arrJPOArgs);
          //Adding PartFamily
        	if(mapPartFamily.size()>0)
        	{
        		for(int k=0;k<mapPartFamily.size();k++)
            	{
            		Map mptemp = (Map)mapPartFamily.get(k);
            		mpViewDups.add(mptemp);
            	}

        	}

            StringList lstGrpNumber = new StringList();
            StringList lstLevelId = new StringList();
            StringList strLevelList = new StringList();
            for (int i = 0; i < mpViewDups.size (); i++)
            {
                Map mapPart = (Map) mpViewDups.get(i);
                String strlevelID =(String)mapPart.get("id[level]");

                if(lstLevelId.contains("'"+strlevelID+"'")){
                    String strGrpNo = ((String) lstGrpNumber.get(lstLevelId.indexOf("'"+strlevelID+"'"))).replaceAll("\'","");
                    strGrpNo = "'"+strGrpNo+","+(String)mapPart.get("GroupNumber")+"'";
                    lstLevelId.add("'"+strlevelID+"'");
                    lstGrpNumber.add(strGrpNo);
                }else{
                    lstLevelId.add("'"+strlevelID+"'");
                    lstGrpNumber.add("'"+(String)mapPart.get("GroupNumber")+"'");
                    strLevelList.add("'"+(String)mapPart.get("id[level]")+"'");
                }
            }
            for (int i = 0; i < listUpdatedParts.size (); i++){
                    Map mapPart = (Map) listUpdatedParts.get(i);
                    String strLevelID = (String)mapPart.get("id[level]");
                    if(!strLevelList.contains("'"+strLevelID+"'")){
                        lstLevelId.add("'"+(String)mapPart.get("id[level]")+"'");
                        lstGrpNumber.add("''");
                    }
            }
            returnPorgMap.put("Action","execScript");
            returnPorgMap.put("Message","{main:function __main(){submitUpdatedData("+lstLevelId+","+lstGrpNumber+")}}");
            returnPorgMap.put("LevelIds",lstLevelId);
            returnPorgMap.put("GrpNumbers",lstGrpNumber);
            returnPorgMap.put("DuplicateParts",mpViewDups);
        }
        catch (Exception e) {
          e.printStackTrace();
      }
        return returnPorgMap;
    }
    /**
     *  The method to update the maplist for post process with Design Variants
     * @param context
     * @param args
     * @return Updated MapList with New DesignVariants
     * @throws Exception
     */
    public MapList getUpdatedGBOMListPostMerge(Context context, String args[]) throws Exception{

        Map programMap = (Map) JPO.unpackArgs(args);
        Map tableData = (Map) programMap.get("tableData");
        Map requestMap = (Map) tableData.get("RequestMap");
        MapList listUpdatedParts = (MapList) requestMap.get("UpdatedCells");
        MapList removedPartList = (MapList) requestMap.get("removedPartList");
        StringList lstGroupNumber = new  StringList();
        StringList lstComplxPartIds = new  StringList();
        Map mpGBOM = (Map)listUpdatedParts.get(0);
    	Object allDesignVariants = (Object)mpGBOM.get("AllDesignVariantList");
    	ProductVariant productvariant = new ProductVariant();
    	StringList allDVlist=productvariant.stringtoStringList(context,allDesignVariants);


        for(int j=0;j<listUpdatedParts.size();j++)
        {
        	StringList addDVListId = new StringList();
        	StringList removeDVListId = new StringList();
        	StringList addPhysListId = new StringList();
        	Map dvTONewVal = new HashMap();
        	StringList removePhysListId = new StringList();
        	Map mpPart = (Map)listUpdatedParts.get(j);
        	for(int k=0;k<allDVlist.size();k++)
        	{
        		String configurationFeatureId=(String)allDVlist.get(k);
        		if(configurationFeatureId!=null)
        		{
        			Map mpUpdateDV = (Map)mpPart.get(configurationFeatureId);
        			String strNewFeatureId = (String)mpUpdateDV.get("NewFeatureId");
        			String strNewFeatureRelId = (String)mpUpdateDV.get("NewFeatureRelId");

        			if(strNewFeatureId!=null)
        			{
        				if(!"-".equals(strNewFeatureId))
        				{
        					addDVListId.add(configurationFeatureId);
        					Map tempPhysicalId = (Map)mpUpdateDV.get("physicalIdMap");
        					addPhysListId.add((String)tempPhysicalId.get(strNewFeatureId));
        					if((String)tempPhysicalId.get(strNewFeatureRelId)!=null ||(String)tempPhysicalId.get(strNewFeatureId)!=null)
        						dvTONewVal.put(configurationFeatureId, (String)tempPhysicalId.get(strNewFeatureId));
        					else{
        						DomainRelationship domRelation = new DomainRelationship();
        						StringList sl = new StringList("physicalid");
        						String nsl[] = new String[]{strNewFeatureId};
        						String strphysicalNewFeatureId = null;

        						MapList hmCGPhysicalID = domRelation.getInfo(context, nsl, sl);
        						for(int count=0; count<hmCGPhysicalID.size();count++)
        						{
        							Map mCGPhysicalID = (Map)hmCGPhysicalID.get(count);
        							strphysicalNewFeatureId = (String)mCGPhysicalID.get("physicalid");

        						}
        						dvTONewVal.put(configurationFeatureId, strphysicalNewFeatureId);
        					}

        					tempPhysicalId.remove(strNewFeatureId);
        					Collection collection = tempPhysicalId.values();
        					Iterator iterator = collection.iterator();
        					while(iterator.hasNext())
        						removePhysListId.add((String)iterator.next());

        				}
        				else
        				{
        					removeDVListId.add(configurationFeatureId);
        					Map tempPhysicalId = (Map)mpUpdateDV.get("physicalIdMap");
        					addPhysListId.add("-");
        					dvTONewVal.put(configurationFeatureId, "-");
        					//for(int count=0;count<tempPhysicalId.size();count++)
        					Collection collection = tempPhysicalId.values();
        					Iterator iterator = collection.iterator();
        					while(iterator.hasNext())
        						removePhysListId.add((String)iterator.next());

        				}
        			}


        		}
        	}
        	//Get DesignVariantsAttr
        	//Adding
        	String oldDesignVariantsAttr = (String)mpPart.get("DesignVariantsAttr");
        	StringList oldDesignVariantsList = new StringList();
        	StringList newDesignVariantsList = new StringList();
        	if(oldDesignVariantsAttr!=null && !oldDesignVariantsAttr.trim().isEmpty()){
        		StringTokenizer stroldDesignVariantsAttrs = new StringTokenizer(oldDesignVariantsAttr, ",");
        		while(stroldDesignVariantsAttrs.hasMoreElements()){
        			String strDesignVariant = (String)stroldDesignVariantsAttrs.nextToken();
        			oldDesignVariantsList.add(strDesignVariant);
        		}
        	}
        	for(int i=0;i<oldDesignVariantsList.size();i++)
        	{
        		String strDesignVariant = (String)oldDesignVariantsList.get(i);
        		newDesignVariantsList.add(strDesignVariant);
        		if(addDVListId.contains(strDesignVariant))
        		{
        			addDVListId.remove(strDesignVariant);
        		}

        	}
        	for(int i=0;i<addDVListId.size();i++)
        		newDesignVariantsList.add(addDVListId.get(i));
        	//Removing
        	for(int i=0;i<newDesignVariantsList.size();i++)
        	{
        		String strDesignVariant = (String)newDesignVariantsList.get(i);
        		if(removeDVListId.contains(strDesignVariant))
        		{
        			newDesignVariantsList.remove(strDesignVariant);
        			//chk need i decrement?????
        		}

        	}
        	StringBuffer dvattr = new StringBuffer();
        	int tmpsize = newDesignVariantsList.size()-1;
        	for(int i=0;i<newDesignVariantsList.size();i++)
        	{
        		dvattr.append(newDesignVariantsList.get(i));
        		if(tmpsize>i)//Removing the last element
        		{
        			dvattr.append(",");
        		}

        	}
        	if(newDesignVariantsList.size()>0)
        	{
        		mpPart.put("DesignVariantsAttr", dvattr.toString());
        	}
        	//Updating New RE List
        	Map mlDVwithOldValue= new HashMap();
        	Map mlDVwithNewValue= new HashMap();

        	//for all DVs  will create Map which will hold key as DV ID and Value as "" string,
        	//excluding DVs for which selected value is changed also  create another Map which will hold "" string for all DVs
        	for(int k=0;k<allDVlist.size();k++)
        	{
        		String configurationFeatureId=(String)allDVlist.get(k);
        		mlDVwithOldValue.put(configurationFeatureId, "");
        		if(dvTONewVal.containsKey(configurationFeatureId))
        			mlDVwithNewValue.put(configurationFeatureId, dvTONewVal.get(configurationFeatureId).toString());
        		else
        		    mlDVwithNewValue.put(configurationFeatureId, "");
        	}
        	//Right Expression
        	String oldRightExpressionAttr = (String)mpPart.get("RightExpressionAttr");
        	String strCommittedREAttrVal = (String)mpPart.get("tomid["
					+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION
					+ "].from.attribute["
					+ ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION
					+ "]");


        	if(strCommittedREAttrVal!=null && !strCommittedREAttrVal.equalsIgnoreCase(oldRightExpressionAttr)){
        		//this usecase exits when user changes cell value twice before clicking on Done button
        		oldRightExpressionAttr = strCommittedREAttrVal;
        	}

        	//get DV- OLD COs TODO can we avoid this
        	//will iterate RE, will get DV ID
        	if(oldRightExpressionAttr!=null){
        		StringTokenizer tokenizeRTExp = new StringTokenizer(oldRightExpressionAttr, " ");
        		while(tokenizeRTExp.hasMoreTokens()){
        			String rightExpressionOldPhyID= tokenizeRTExp.nextToken().trim();
        			if(!rightExpressionOldPhyID.equalsIgnoreCase("AND")){
        				rightExpressionOldPhyID = rightExpressionOldPhyID.substring(1);
        				String[] relIdList = new String[] { rightExpressionOldPhyID };
        				StringList selectableList = new StringList(
        						DomainRelationship.SELECT_TYPE);
        				selectableList.add("from.id");
        				selectableList.add("torel.to.id");
        				MapList resultList = DomainRelationship.getInfo(context,
        						relIdList, selectableList);
        				Map mpREToDetails=(Map)resultList.get(0);
        				String strRelName=mpREToDetails.get(DomainRelationship.SELECT_TYPE).toString();
        				if(strRelName
        						.equals(ConfigurationConstants.RELATIONSHIP_CONFIGURATION_OPTIONS) || strRelName
        						.equals(ConfigurationConstants.RELATIONSHIP_CONFIGURATION_FEATURES)){
        					String dvID=mpREToDetails.get("from.id").toString();
        					mlDVwithOldValue.put(dvID, rightExpressionOldPhyID);
        				}else{
        					//TODO CG Case
        					String dvID = mpREToDetails.get(
        					"torel.to.id")
        					.toString();
        					mlDVwithOldValue.put(dvID, rightExpressionOldPhyID);
        				}
        			}
        		}
        	}
        	//create Final list which will hold all new options selcted for current GBOM
    		StringList finalRTExp= new StringList();
			if (!mlDVwithNewValue.isEmpty()) {
				Iterator newRE= mlDVwithNewValue.keySet().iterator();
				//iterate on all the Cos in RE
				while (newRE.hasNext()) {
					String dvID = (String)newRE.next();
					String newExp=(String)mlDVwithNewValue.get(dvID);
					String oldExp=(String)mlDVwithOldValue.get(dvID);
					//if we have new expression empty, and old is not empty
					if(newExp.trim().isEmpty() && !oldExp.trim().isEmpty()){
						finalRTExp.add(oldExp);
					}
					if(newExp.trim().isEmpty() || newExp.equalsIgnoreCase(oldExp)|| newExp.equalsIgnoreCase("-"))
						continue;
					else
						finalRTExp.add(newExp);

				}
			}
			//create RE with AND in between
        	StringBuffer reattr = new StringBuffer();
        	int size=finalRTExp.size()-1;
        	for(int i=0;i<finalRTExp.size();i++)
        	{
        		reattr.append(finalRTExp.get(i));
        		if(size>i)//Removing the last element
        		{
        		reattr.append(" ");
        		reattr.append("AND");
        		reattr.append(" ");
        		}
        	}
        	if(finalRTExp.size()>0)
            {
            	mpPart.put("RightExpressionAttr", reattr.toString());
        	}
        }

        for(int i= 0;i<listUpdatedParts.size();i++)
        {

            Map objMap = (Map) listUpdatedParts.get(i);
            String strObjId = (String)objMap.get(DomainConstants.SELECT_ID);
            String strRuleComplexity = (String)objMap.get("ruleComplexity");
            String strdesignVariant = (String)objMap.get("DesignVariantsAttr");
            String strRightExpression = (String)objMap.get("RightExpressionAttr").toString();
            boolean booOutIncRulSim = false;

            if(strRuleComplexity!=null && !strRuleComplexity.equals("")){
                   if(strRuleComplexity.equalsIgnoreCase("Simple")){
                       booOutIncRulSim = true;
                   }
            }

            if(removedPartList!= null && removedPartList.contains(strObjId))
                listUpdatedParts.remove(i);
            else if(booOutIncRulSim)
            {
                objMap.remove("IRGroupNumber");
                objMap.remove("PNGroupNumber");
                objMap.remove("PNDuplicates");
                objMap.remove("IRDuplicates");
            }
            else
            {
                objMap.remove("PNGroupNumber");
                objMap.remove("PNDuplicates");
                if(!lstComplxPartIds.contains(strObjId))
                {
                    lstComplxPartIds.add(strObjId);
                    lstGroupNumber.add((String)objMap.get("IRGroupNumber"));
                }

            }

            if(strRuleComplexity!=null)
            	objMap.put("RuleComplexity",strRuleComplexity);
            else
            	objMap.put("RuleComplexity",DomainConstants.EMPTY_STRING);
            objMap.put("IncRuleId","true");
            if(strdesignVariant!=null)
            objMap.put("DesignVariantsAttr",strdesignVariant);
            else
            	objMap.put("DesignVariantsAttr",DomainConstants.EMPTY_STRING);
            if(strRightExpression!=null&&!strRightExpression.trim().isEmpty() )
            objMap.put("RightExpressionAttr",strRightExpression);
            else
            	objMap.put("RightExpressionAttr",DomainConstants.EMPTY_STRING);


        }
    return listUpdatedParts;

    }

    /**
     * This method is used as chooser JPO method for part family field where ever applicable.
     * @param context
     * @param args
     * @return
     * @throws Exception
     */

	public HashMap getPartFamilyChooser(Context context, String[] args) throws Exception{
		HashMap cacheMap = new HashMap();
    	StringList strPFSelects = new StringList(2);
    	strPFSelects.add(ConfigurationConstants.SELECT_ID);
    	strPFSelects.add(ConfigurationConstants.SELECT_NAME);

    	MapList partFamilyList = DomainObject.findObjects(context,
    			ConfigurationConstants.TYPE_PART_FAMILY,
    			ConfigurationConstants.QUERY_WILDCARD,
    			ConfigurationConstants.EMPTY_STRING,strPFSelects );

    	for (Iterator pfIttr = partFamilyList.iterator(); pfIttr
				.hasNext();) {
			Map pfMap = (Map) pfIttr.next();
			String strName = (String) pfMap.get(ConfigurationConstants.SELECT_NAME);
			cacheMap.put(strName,strName);
		}

		return cacheMap;
	}


    /**
     * This method displays Cutom Action Icons column.
     *
     * @param  context the eMatrix Context object
     * @param  args holds the following input arguments: paramMap a HashMap
     *             requestMap a HashMap
     * @return Vector which holds following parameter html code to display icons
     * @throws Exception if the operation fails
     * @since  R212
     */
     public Vector getPartUsageForDisplay (Context context, String args[])throws Exception {
         HashMap parametersMap = (HashMap) JPO.unpackArgs(args);
         MapList objectList = (MapList) parametersMap.get("objectList");
         Vector vectPartUsage = new Vector();
         String strLanguage = context.getSession().getLanguage();
         String strStandard = EnoviaResourceBundle.getProperty(context, SUITE_KEY,
                 "emxConfiguration.Range.Part_Usage.Standard",strLanguage);
         String strCustom = EnoviaResourceBundle.getProperty(context, SUITE_KEY,
                 "emxConfiguration.Range.Part_Usage.Custom",strLanguage);

         for (int i = 0; i < objectList.size(); i++)
         {
        	 Map mapPart = (Map) objectList.get(i);
        	 String strRelType = (String)mapPart.get(ConfigurationConstants.KEY_RELATIONSHIP);
        	 if(strRelType.equalsIgnoreCase(ConfigurationConstants.RELATIONSHIP_GBOM) || ProductLineCommon.isOfParentRel(context, strRelType, ConfigurationConstants.RELATIONSHIP_GBOM) ||
        			 strRelType.equalsIgnoreCase(ConfigurationConstants.RELATIONSHIP_INACTIVE_GBOM) || ProductLineCommon.isOfParentRel(context, strRelType, ConfigurationConstants.RELATIONSHIP_INACTIVE_GBOM)){
        		 vectPartUsage.add(strStandard);
        	 }else{
        		 vectPartUsage.add(strCustom);
        	 }
         }
         return vectPartUsage;
     }


     /**
  	 * It is a check trigger method. Fires when a relationship GBOM is getting created
  	 * It is used to check the cyclic condition
  	 * @param context
  	 *            The ematrix context object.
  	 * @param String[]
  	 *            The args .
  	 * @return zero when cyclic condition is false, throws exception if it is true
  	 * @author g98
  	 * @since R212
  	 */
  	public int multiLevelRecursionCheck(Context context, String[] args)throws Exception
  	{
 		 String strRemovedLFId = PropertyUtil.getGlobalRPEValue(context,"CyclicCheckRequired");
 		 int iResult = 0;
 	 	 if(strRemovedLFId==null || strRemovedLFId.equalsIgnoreCase("")) {
 	    	 String fromObjectId = args[0];
 	    	 String toObjectId = args[1];
 	    	 String relType = args[2];
 	    	 try {
 	    		 boolean recursionCheck =  ConfigurationUtil.multiLevelRecursionCheck(context,fromObjectId,toObjectId,relType);
 	    		 if (recursionCheck) {
 					String language = context.getSession().getLanguage();
 					String strAlertMessage = EnoviaResourceBundle.getProperty(context, SUITE_KEY,
 							"emxConfiguration.Add.CyclicCheck.Error",language);
 					throw new FrameworkException(strAlertMessage);
 	    		 } else {
 	    			 iResult = 0;
 	    		 }
 	    	 }catch (Exception e) {
 					throw new FrameworkException(e.getMessage());
 	    	 }
 	 	}else if(strRemovedLFId.equalsIgnoreCase("False")){
 			iResult = 0;
 		}
 	 	return iResult;


  	}
  	/**
  	 * It is a programHTML method, used to display GBOM Action Icons in GBOM PArt Table
  	 * @param context
  	 *            The ematrix context object.
  	 * @param String[]
  	 *            The args .
  	 * @return InActive GBOM, Repalce GBOM and Remove GBOM icons will return
  	 * @author wku
  	 * @since R214
  	 */
  	public Vector renderActionIconsOnGBOM(Context context, String[] args) throws FrameworkException {
  		try{
  			HashMap programMap = (HashMap)JPO.unpackArgs(args);

  			HashMap paramMap = (HashMap)programMap.get("paramList");
  	    	String parentOID = (String)paramMap.get("parentOID");
  			MapList objectList = (MapList) programMap.get("objectList");
  			String strTimeStamp  = (String) paramMap.get("timeStamp");
  			DomainObject domParentOID = new DomainObject(parentOID);
  	    	String contextState = domParentOID.getInfo(context, DomainConstants.SELECT_CURRENT);
  	    	DomainObject domItem = DomainObject.newInstance(context, domParentOID);
  	    	String strStateRelease = FrameworkUtil.lookupStateName(context,domItem.getPolicy(context).getName(),"state_Release");
  	    	String strStateObsolete = FrameworkUtil.lookupStateName(context,domItem.getPolicy(context).getName(),"state_Obsolete");
  	    	Vector actionIcons = new Vector();
  	       //For Replace GBOM
  	    	String[] arrJPOArguments = new String[4];
	            Map programtmpMap = new HashMap();
	            programtmpMap.put("objectId", parentOID);
	            arrJPOArguments = JPO.packArgs(programtmpMap);
	            boolean repalce = Boolean.parseBoolean(JPO.invoke(context,"emxFTRPart",null,"showReplaceGBOMActionCommand",arrJPOArguments,Boolean.class).toString()); ;
  			for (int i = 0; i < objectList.size(); i++)
  			{
  				String strobjectId = "";
  				String strConnectionId = "";
  				String strTypes="";
  				String sourceType ="";
  				String policyType ="policy_DevelopmentPart,policy_StandardPart,policy_ConfiguredPart";
  		    	String partState= "policy_Classification.state_Obsolete,policy_ECPart.state_Obsolete,policy_Product.state_Obsolete";

  				Map objectMap = (Map) objectList.get(i);
  				if(objectMap.containsKey(DomainConstants.SELECT_ID))
  					strobjectId = (String) objectMap.get(DomainConstants.SELECT_ID);
  				if(objectMap.containsKey(DomainConstants.SELECT_RELATIONSHIP_ID))
  					strConnectionId = (String)objectMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);
  				
  				if(objectMap.containsKey(DomainConstants.SELECT_TYPE))
  					sourceType =(String)objectMap.get(DomainConstants.SELECT_TYPE);

  				if (mxType.isOfParentType(context, sourceType,
  		    			ConfigurationConstants.TYPE_PART_FAMILY)) {
  		    		strTypes = "type_Part,type_Products,type_PartFamily";
  		    	}else{
  		    		strTypes = "type_Part,type_Products";
  		    	}

  				StringBuffer strBufferIcons = new StringBuffer();
  				if(!contextState.equalsIgnoreCase(strStateObsolete))
  				{
  				//For GBOM Inactive
  				String strCustomInactiveProcessJsp = EnoviaResourceBundle.getProperty(context, "SummaryActions.ProcessPage.LogicalFeature_GBOM_Inactive");
  				String strCustomInactiveIconImage = EnoviaResourceBundle.getProperty(context,"SummaryActions.ActionIcon.LogicalFeature_GBOM_Inactive");
  				String strCustomInactiveTargetLoc = EnoviaResourceBundle.getProperty(context,"SummaryActions.TargetLocation.LogicalFeature_GBOM_Inactive");
  				strBufferIcons.append("<a href=\"javascript:removeActionIcon('../configuration/");
  				strBufferIcons.append(XSSUtil.encodeForXML(context, strCustomInactiveProcessJsp)+"?mode=CustomGBOMInactive");
  				strBufferIcons.append("%26objectId=");
  				strBufferIcons.append(XSSUtil.encodeForXML(context, strobjectId));
  				strBufferIcons.append("%26parentOID=");
  				strBufferIcons.append(XSSUtil.encodeForXML(context, parentOID));
  				strBufferIcons.append("%26relId=");
  				strBufferIcons.append(XSSUtil.encodeForXML(context, strConnectionId));
  					strBufferIcons.append("%26timestamp=");
  					strBufferIcons.append(XSSUtil.encodeForXML(context, strTimeStamp));
  				strBufferIcons.append("%26targetLocation=");
  				strBufferIcons.append(XSSUtil.encodeForXML(context, strCustomInactiveTargetLoc)+"' ,"+ "'InactiveGBOM'" + " ,'"+XSSUtil.encodeForXML(context, strConnectionId)+"'");
  				strBufferIcons.append(")\"><img border=\"0\" src=\"images/"+strCustomInactiveIconImage+"\"");
  				strBufferIcons.append(" TITLE=\"");
  				strBufferIcons.append( XSSUtil.encodeForXML(context, EnoviaResourceBundle.getProperty(context, SUITE_KEY,"emxConfiguration.ToolTip.LogicalFeature_GBOM_Inactive",context.getSession().getLanguage())));
  				strBufferIcons.append("\"");
  				strBufferIcons.append("/></a>");
  				//For Remove GBOM
  					String strCustomProcessJsp = EnoviaResourceBundle.getProperty(context, "SummaryActions.ProcessPage.LogicalFeature_GBOM_Remove");
  					String strCustomIconImage = EnoviaResourceBundle.getProperty(context,"SummaryActions.ActionIcon.LogicalFeature_GBOM_Remove");
  					String strCustomTargetLoc = EnoviaResourceBundle.getProperty(context,"SummaryActions.TargetLocation.LogicalFeature_GBOM_Remove");
  					strBufferIcons.append("<a href=\"javascript:removeActionIcon('../configuration/");
  					strBufferIcons.append(XSSUtil.encodeForXML(context, strCustomProcessJsp)+"?mode=CustomGBOMDisconnect");
  					strBufferIcons.append("%26objectId=");
  					strBufferIcons.append(XSSUtil.encodeForXML(context, strobjectId));
  					strBufferIcons.append("%26parentOID=");
  					strBufferIcons.append(XSSUtil.encodeForXML(context, parentOID));
  					strBufferIcons.append("%26relId=");
  					strBufferIcons.append(XSSUtil.encodeForXML(context, strConnectionId));
  					strBufferIcons.append("%26timestamp=");
  					strBufferIcons.append(XSSUtil.encodeForXML(context, strTimeStamp));
  					strBufferIcons.append("%26targetLocation=");
  					strBufferIcons.append(strCustomTargetLoc+"' ,"+ "'RemoveGBOM'" + " ,'"+XSSUtil.encodeForXML(context,strConnectionId)+"'");
  					strBufferIcons.append(")\"><img border=\"0\" src=\"images/"+strCustomIconImage+"\"");
  					strBufferIcons.append(" TITLE=\"");
  					strBufferIcons.append(EnoviaResourceBundle.getProperty(context, SUITE_KEY,"emxConfiguration.ToolTip.LogicalFeature_GBOM_Remove",context.getSession().getLanguage()));
  					strBufferIcons.append("\"");
  					strBufferIcons.append("/></a>");
  				}
  				//For Replace GBOM
  				if(repalce)
  				{
  					String strCustomIconImage = EnoviaResourceBundle.getProperty(context,"SummaryActions.ActionIcon.LogicalFeature_GBOM_Replace");
  					
  					strBufferIcons.append("<a href=\"javascript:showModalDialog('../common/emxFullSearch.jsp?objectId="+strobjectId+"%26excludeOID="+strobjectId+"%26contextObjectId="+parentOID+"%26suiteKey=Configuration");
  					strBufferIcons.append("%26field=TYPES="+strTypes+":POLICY!="+policyType+":CURRENT!="+partState);
  					strBufferIcons.append("%26table=PLCSearchPartsTable");
  					strBufferIcons.append("%26Suite=Configuration");
  					strBufferIcons.append("%26selection=single");
  					strBufferIcons.append("%26hideHeader=true");
  					strBufferIcons.append("%26HelpMarker=emxhelpfullsearch");
  					strBufferIcons.append("%26submitURL=../configuration/GBOMReplacePostProcess.jsp%26submitAction=doNothing%26mode1=ActionIcon%26relId=");
  	  			    strBufferIcons.append(XSSUtil.encodeForXML(context,strConnectionId));
	  				strBufferIcons.append("%26suiteKey=configuration%26SuiteDirectory=configuration%26parentOID=");
	  				strBufferIcons.append(XSSUtil.encodeForXML(context,parentOID));
	  	  			strBufferIcons.append("%26timestamp=");
	  	  			strBufferIcons.append(XSSUtil.encodeForXML(context,strTimeStamp)); 
	  				strBufferIcons.append("%26objectId=");
	  				strBufferIcons.append(XSSUtil.encodeForXML(context,strobjectId));
	  				strBufferIcons.append("', '850', '630', 'true', 'popup', '')\"><img border=\"0\" src=\"images/"+strCustomIconImage+"\"");
	  				strBufferIcons.append(" TITLE=\"");
	  				strBufferIcons.append(EnoviaResourceBundle.getProperty(context, SUITE_KEY,
	  				"emxConfiguration.ToolTip.LogicalFeature_GBOM_Replace",context.getSession().getLanguage()));
	  				strBufferIcons.append("\"");
	  				strBufferIcons.append("/>");
	  				strBufferIcons.append("</a>");	
               }
  				actionIcons.add(strBufferIcons.toString());
  			}
  			return  actionIcons;
  		}
  		catch (Exception e) {
  			throw new FrameworkException(e.toString());
  		}
  	}
  	/**
  	 * It is a programHTML method, used to display GBOM Action Icons in GBOM PArt Table
  	 * @param context
  	 *            The ematrix context object.
  	 * @param String[]
  	 *            The args .
  	 * @return Active GBOM  icon will return
  	 * @author wku
  	 * @since R214
  	 */
  		public Vector renderActionIconsOnInactiveGBOM(Context context, String[] args) throws FrameworkException {
  			//XSSOK- Deprecated
  			try{
  				HashMap programMap = (HashMap)JPO.unpackArgs(args);
  				HashMap paramMap = (HashMap)programMap.get("paramList");
  		    	String parentOID = (String)paramMap.get("parentOID");
  				MapList objectList = (MapList) programMap.get("objectList");
  				String strTimeStamp  = (String) paramMap.get("timeStamp");
  		    	Vector actionIcons = new Vector();
  				for (int i = 0; i < objectList.size(); i++)
  				{
  					String strobjectId = "";
  					String strConnectionId = "";
  					Map objectMap = (Map) objectList.get(i);
  					if(objectMap.containsKey(DomainConstants.SELECT_ID))
  						strobjectId = (String) objectMap.get(DomainConstants.SELECT_ID);
  					if(objectMap.containsKey(DomainConstants.SELECT_RELATIONSHIP_ID))
  						strConnectionId = (String)objectMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);
  					StringBuffer strBufferIcons = new StringBuffer();
  					//For GBOM Inactive
  					String strCustomActiveProcessJsp = EnoviaResourceBundle.getProperty(context, "SummaryActions.ProcessPage.LogicalFeature_GBOM_Active");
  					String strCustomActiveIconImage = EnoviaResourceBundle.getProperty(context,"SummaryActions.ActionIcon.LogicalFeature_GBOM_Active");
  					String strCustomActiveTargetLoc = EnoviaResourceBundle.getProperty(context,"SummaryActions.TargetLocation.LogicalFeature_GBOM_Active");
  					strBufferIcons.append("<a href=\"javascript:removeActionIcon('../configuration/");
  					strBufferIcons.append(strCustomActiveProcessJsp+"?mode=CustomGBOMActive");
  					strBufferIcons.append("%26objectId=");
  					strBufferIcons.append(strobjectId);
  	  				strBufferIcons.append("%26parentOID=");
  	  				strBufferIcons.append(parentOID);
  					strBufferIcons.append("%26relId=");
  					strBufferIcons.append(strConnectionId);
  	  				strBufferIcons.append("%26timestamp=");
  	  				strBufferIcons.append(strTimeStamp);
  					strBufferIcons.append("%26targetLocation=");
  					strBufferIcons.append(strCustomActiveTargetLoc+"' ,"+ "'ActiveGBOM'" + " ,'"+strConnectionId+"'");
  					strBufferIcons.append(")\"><img border=\"0\" src=\"images/"+strCustomActiveIconImage+"\" alt=\"\"");
  					strBufferIcons.append(" TITLE=\"");
  					strBufferIcons.append(EnoviaResourceBundle.getProperty(context, SUITE_KEY,
  					"emxConfiguration.ToolTip.LogicalFeature_GBOM_Active",context.getSession().getLanguage()));
  					strBufferIcons.append("\"");
  					strBufferIcons.append("/></a>");
  					actionIcons.add(strBufferIcons.toString());
  				}
  				return  actionIcons;
  			}
  			catch (Exception e) {
  				throw new FrameworkException(e.toString());
  			}

  	}
  		/**
   	    * Determine if the Actions Icon Column for the GBOM table is visible.
   	    * @param context the eMatrix <code>Context</code> object
   	    * @param args contains the feature object ID
   	    * @returns Boolean
   	    * @throws Exception if the operation fails
   	    */
   	   public static Boolean actionCoumnDisplay(Context context, String[] args) throws Exception
   	   {
     		boolean isFTRUser=false;
     		String Licenses[] = {"ENO_FTR_TP","ENO_CFE_TP"};
      		try {
      			FrameworkLicenseUtil.checkLicenseReserved(context,Licenses);
    		    isFTRUser = true;
    		}catch (Exception e){
    			isFTRUser = false;
    		}
      	   if(!isFTRUser)
      		 return Boolean.valueOf(isFTRUser);
      	   
   		   HashMap paramMap = (HashMap) JPO.unpackArgs(args);
   		   String strFeatureId = (String)paramMap.get("objectId");
   		   StringList slSelectable = new StringList(DomainConstants.SELECT_CURRENT);
   		   slSelectable.add(DomainConstants.SELECT_POLICY);
   		   Map mpSelectable = new DomainObject(strFeatureId).getInfo(context,slSelectable);
   		   String strCurrent = (String)mpSelectable.get(DomainConstants.SELECT_CURRENT);
   		   String strPolicy = (String)mpSelectable.get(DomainConstants.SELECT_POLICY);
   		   String stateObsolete = PropertyUtil.getSchemaProperty(context,"policy", strPolicy, "state_Obsolete");
   		   boolean isObsolete = stateObsolete.equals(strCurrent);
   		   boolean showColumn = false;
   		   if (!isObsolete)
   			 showColumn = true;
   		   return Boolean.valueOf(showColumn);
   	   }
   	 /**
   	    * This method is used to include the appropriate ProductConfigurations from the Product and Logical Feature context.
   	    *
   	    * @param context
   	    * @param args
   	    * @return
   	    * @throws Exception
   	    */
   		@com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
   		public StringList includePCBasedOn(Context context, String[] args) throws Exception
   		{
   		    HashMap programMap = (HashMap) JPO.unpackArgs(args);
   		    String strProductId = (String)  programMap.get("objectId");
   		    String strLogFeatureId = (String)  programMap.get("lfeatureId");

   		    if(strProductId==null && strLogFeatureId!=null){
   		    	strProductId = strLogFeatureId;
   		    }

   		    DomainObject domObjFeature =new DomainObject(strProductId);
   		    StringList tempStrList = new StringList();
   		    StringList objSelects = new StringList();
   		    objSelects.add(DomainConstants.SELECT_ID);

   		    MapList mapProductObjsList = new MapList();
   		    mapProductObjsList = domObjFeature.getRelatedObjects(context,
   		    		ConfigurationConstants.RELATIONSHIP_PRODUCT_CONFIGURATION+","+ConfigurationConstants.RELATIONSHIP_FEATURE_PRODUCT_CONFIGURATION,
   		    		ConfigurationConstants.TYPE_PRODUCT_CONFIGURATION,
   	    			objSelects,
   	    			null,
   	    			false,
   	    			true,
   	    			(short)1,
   	    			DomainConstants.EMPTY_STRING,
   	    			DomainConstants.EMPTY_STRING, 0);
   		    for(int i=0;i<mapProductObjsList.size();i++){
   		        Map temp = new HashMap();
   		        temp=(Map)mapProductObjsList.get(i);
   		        String mstPCIds = (String)temp.get("id");
   		     tempStrList.add(mstPCIds);
   		    }
   		    return tempStrList;
   		}
   		/**
   		 * 	This Methods is used to create PF -Create JPO
   		 * @param context
   		 * @param args
   		 * @return
   		 * @throws Exception
   		 * @since R418 HF2
   		 */
   		@com.matrixone.apps.framework.ui.CreateProcessCallable
   		public Map createPartFamilyJPO(Context context, String[] args) throws Exception {
   			HashMap mapReturn = new HashMap(1);
   			try {
   				HashMap programMap = (HashMap) JPO.unpackArgs(args);
   				String sType =(String)	programMap.get("TypeActual");
   				String sName = (String) programMap.get("Name");
   				String sPolicy =(String) programMap.get("Policy");
   				String sVault = (String) programMap.get("Vault");
   				String sDefaultPartType = (String) programMap.get("DefaultPartType");

   				String sDefaultPartPolicy = "";
   				String sReleasePhase = "";
   				if(programMap.containsKey("ReleaseProcess")){
   					sReleasePhase = (String) programMap.get("ReleaseProcess");
   				}
   				if(sReleasePhase.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_RELEASE_PHASE_DEVELOPMENT)){
   					sDefaultPartPolicy=DEFAULT_PART_POLICY_DEVELOPMENT_PART;
   				}else if(sReleasePhase.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_RELEASE_PHASE_PRODUCTION)){
   					sDefaultPartPolicy=DEFAULT_PART_POLICY_EC_PART;
   				}else{
   					sDefaultPartPolicy=DEFAULT_PART_POLICY_DEVELOPMENT_PART;
   				}
   				Map objAttributeMap = new HashMap(2);
   				objAttributeMap.put(DomainConstants.ATTRIBUTE_DEFAULT_PART_TYPE, sDefaultPartType);
   				objAttributeMap.put(DomainConstants.ATTRIBUTE_DEFAULT_PART_POLICY, sDefaultPartPolicy);

   				PartFamily partFamily = new PartFamily();
   				partFamily.create(context, sType, sName, null, sPolicy, sVault,"",objAttributeMap);

   				String partfamilyId = partFamily.getId(context);
   				mapReturn.put("id", partfamilyId);
   			} catch (Exception e) {
   				throw (new FrameworkException(e.getMessage()));
   			}
   			return mapReturn;
   		}
   		/**
   		 * This Methods is used to connect PF object with GBOM relationship to context Object
   		 * @param context
   		 *            the eMatrix <code>Context</code> object
   		 * @param args -
   		 *            Holds the following arguments 0 - HashMap containing the
   		 *            following arguments
   		 * @return Object - MapList containing the id of Archived Manufacturing Plans objects
   		 * @throws Exception
   		 *             if the operation fails
   		 * @since R418 HF2
   		 */
   		@com.matrixone.apps.framework.ui.PostProcessCallable
   		public String connectPartFamily(Context context, String[] args)
   				throws Exception {
   			try {
   				HashMap programMap = (HashMap) JPO.unpackArgs(args);
   				Map paramMap = (Map) programMap.get("paramMap");
   				String newObjectId = (String) paramMap.get("newObjectId");

   				Map requestMap = (Map) programMap.get("requestMap");
   				String objectId = (String) requestMap.get("objectId");

   				//Connect Part Family API
   				LogicalFeature logicalFTR= new LogicalFeature(objectId);
   				logicalFTR.addPartFamily(context, newObjectId);

   				return newObjectId;
   			} catch (Exception e) {
   				throw (new FrameworkException(e.getMessage()));
   			}
   		}  	  

   		/**
   		 * Update Function for Release Process Field- will be empty as Release Phase is not PF
   		 * 
   		 * @param context
   		 * @param args
   		 * @throws FrameworkException
   		 */
   		public void emptyProgram(Context context, String[] args)
   				throws FrameworkException {

   		}
   		/**
   		 * Initial function for Release Process Field Value, 
   		 * @param context
   		 * @param args
   		 * @return
   		 * @throws Exception
   		 */
   		public Map getReleasePhase(Context context, String[] args) throws Exception {
   			HashMap programMap = (HashMap) JPO.unpackArgs(args);
   			Map returnMap = new HashMap();
   			try {
   				StringList slPhaseList = new StringList();
   				slPhaseList.add(ConfigurationConstants.RANGE_VALUE_RELEASE_PHASE_DEVELOPMENT);
   				slPhaseList.add(ConfigurationConstants.RANGE_VALUE_RELEASE_PHASE_PRODUCTION);
   				HashMap hmPhaseDetails;
   				String sLabel;
   				String displayValue;
   				StringList slDisplayValueList = new StringList();
   				for (int i=0; i<slPhaseList.size();i++)
   				{
   					sLabel = (String)slPhaseList.get(i);
   					displayValue = EnoviaResourceBundle.getProperty(context, SUITE_KEY, "emxConfiguration.ReleasePhase."+sLabel, context.getSession().getLanguage());
   					slDisplayValueList.add(displayValue);
   				}
   				returnMap.put("field_choices", slPhaseList);
   				returnMap.put("field_display_choices", slDisplayValueList);
   			} catch (Exception e) {
   				throw new FrameworkException(e.getMessage());
   			}
   			return returnMap;
   		}

   		/**
   		 * Default Part Type, which will be rendered in "Default Part Type" Field.
   		 * @param context
   		 * @param args
   		 * @return
   		 * @throws Exception
   		 */
   		public String getDefaultPartType(Context context, String[] args) throws Exception {
   			return ConfigurationConstants.TYPE_PART;
   		}
   		/**
   		 * Update Function for "Default Part Type" Field - which will set value as per "New Value" in Request 
   		 * @param context
   		 * @param args
   		 * @throws Exception
   		 */
   		public void updateDefaultPartType(Context context, String[] args)throws Exception{
   			try{
   				HashMap programMap = (HashMap)JPO.unpackArgs(args);
   				HashMap requestMap = (HashMap)programMap.get("requestMap");
   				HashMap fieldMap = (HashMap)programMap.get("fieldMap");
   				HashMap paramMap = (HashMap)programMap.get("paramMap");

   				String objectId = (String)paramMap.get("objectId");
   				String sType = (String)paramMap.get("New Value");

   				DomainObject dmObj = DomainObject.newInstance(context);
   				dmObj.setId(objectId);
   				dmObj.setAttributeValue(context, DomainConstants.ATTRIBUTE_DEFAULT_PART_TYPE, sType);

   			}
   			catch(Exception e){
   				throw new FrameworkException(e.getMessage());
   			}
   		}
   		
	    /** This function is used to show Release Phase and Change Controlled Column. 
	     * Depending upon Types.
	     * @param context
	     * @param args
	     * @return
	     *        true  - if attribute "field=TYPES=" contains type Part.
	     *        false - if attribute "field=TYPES=" does not contains type Part. 
	     * @throws Exception 
	     *         if the operation fails
   		 * @since R418 HF2
	     */
	     
	     public static boolean showReleasePhaseAndChangeControlledColumn(Context context, String[] args) throws Exception
	     {
	      try
	    	{
	    	 HashMap programMap     = (HashMap)JPO.unpackArgs(args);
			 String sTypeString     = (String)programMap.get("field");
			 boolean strResult      = false;
			 if(sTypeString != null){
			 String strTypes        = sTypeString.substring(sTypeString.indexOf("=")+1, sTypeString.indexOf(":"));
			 String[] strTypeArr    = strTypes.split(",");
			 StringList strTypeList = new StringList(strTypeArr);
			 
			 
			 if(strTypeList.contains(ConfigurationConstants.TYPE_PART))
			 {
				 strResult = true;
			 }
			 }
			 return strResult;
	    	}
	      catch(Exception e)
	      {
	    	  throw (new FrameworkException(e.getMessage()));
	      }
	    }
}
