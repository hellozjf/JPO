/*
 ** emxModelBasedPUEMigrationBase.java
 **
 ** Copyright (c) 1993-2016 Dassault Systemes. All Rights Reserved.
 ** This program contains proprietary and trade secret information of
 ** Dassault Systemes.
 ** Copyright notice is precautionary only and does not evidence any actual
 ** or intended publication of such program
 */

import java.util.*;

import matrix.db.*;
import matrix.util.*;

import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.effectivitymigration.EffectivityFramework;

  public class emxModelBasedPUEMigrationBase_mxJPO	extends emxCommonMigration_mxJPO
  {
      private static final String CONTIGUOUS_DELIMITER = "-";
      private static final String DELIMITER = ",";
      private static final String _KEY_PREFIX = "@EF_UT(PHY@EF:";
      private static final String _KEY_SUFFIX = "])";
      private static final String SELECT_ATTRIBUTE_EFFECTIVITY_EXPRESSION = EffectivityFramework.SELECT_ATTRIBUTE_EFFECTIVITY_EXPRESSION;
      private static final String SELECT_ID = DomainConstants.SELECT_ID;
      
      private String _INF = FrameworkProperties.getProperty("emxUnresolvedEBOM.UnitRangeNotation.Subsequent.Value");
      private StringList _contiguousList = new StringList();
      private StringList _delimiterList  = new StringList();
      private StringBuffer _finalBuffer  = new StringBuffer();
	  	
	  String val;long lowerLimit;long higherLimit;String nextVal;
	  long nxtLowerLimit;long nxtHigherLimit;String currStr;long delimiterVal;long highestBuildVal;String highestVal="";
      HashMap<String,String> modelAppMap = new HashMap<String,String>();

	  Iterator itr;String modelPhyId;String applValue;String changeId;

      /**
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args holds no arguments
      * @throws Exception if the operation fails
      * @grade 0
      */
      public emxModelBasedPUEMigrationBase_mxJPO (Context context, String[] args)
              throws Exception
      {
       super(context, args);
      }

      /**
       * This method does the migration work.  All the assigned part relationships between Product
       * and the Top Level part gets replaced by corresponding Model and the Top Level part.       
       * @param context the eMatrix <code>Context</code> object
       * @param objectIdList StringList holds list objectids to migrate
       * @returns nothing
       * @throws Exception if the operation fails
       */
      @SuppressWarnings({ "rawtypes", "unchecked" })
	public void  migrateObjects(Context context, StringList objectIdList)
                                                          throws Exception 
      {
          // if scan variable is set, then do not migrate data
          // just write the problamatic ids to log else proceed with migration
          if(scan)
          {
              return;
          }
          
          String RELATIONSHIP_PRODUCTS   = PropertyUtil.getSchemaProperty(context,"relationship_Products");
          String SELECT_MODEL_ID = "to[" + RELATIONSHIP_PRODUCTS + "].from.physicalid";
          EffectivityFramework effectivity  = new EffectivityFramework();
          
          

          MapList changeExprList = new MapList();
          MapList changeObjList  = new MapList();
          MapList mlEffectivity  = new MapList();
          HashMap changeExprMap = new HashMap();
          Map mapEffectivity;
          Map prodAppMap;
          
          String actualExpr;
          String sProdObjId;
          String modelExpr;
          
          
          try
          {
        	  ContextUtil.startTransaction(context, true);
        	  changeObjList = getMapListOfIDs(objectIdList);        	  
        	  //Pass all the change ids once and get all the associated effectivity information at once
        	  mlEffectivity  = effectivity.getEffectivityOnChange(context, changeObjList);        	 
        	  mqlLogWriter ( "******************Effectivity Maplist with for the given StringList of PUE ECO object ids " + mlEffectivity + "\n");
        	  
        	  for (int i=0;i<mlEffectivity.size();i++) {
        		  mapEffectivity = (Map)mlEffectivity.get(i);
        		  //get actual effectivity expression of each change id and iterate through product ids involved
        		  actualExpr = (String)mapEffectivity.get(EffectivityFramework.ACTUAL_VALUE);
        		  if("".equals(actualExpr))continue;
        		  
        		  prodAppMap = effectivity.getExpressionSequence(context, actualExpr);
        		  
        		  //This Loop prepares Models and Effectivities as key/value pairs for a given expression
      			  itr        = prodAppMap.keySet().iterator();
      			  while (itr.hasNext()) {
      			    	sProdObjId 	= (String)itr.next();
      			    	modelPhyId	= (String)DomainObject.newInstance(context, sProdObjId).getInfo(context, SELECT_MODEL_ID);
      			    	applValue   = (String)prodAppMap.get(sProdObjId);
      			    	//if expression involves two product revisions from same model id then make union of those applicabilities
      			    	if(modelAppMap.containsKey(modelPhyId)) {
      			    		applValue = getUnionOfApplicabilities(context,new String[]{modelAppMap.get(modelPhyId), applValue});
      			    	}
      			    	modelAppMap.put(modelPhyId, applValue);

      			  }
      			  	changeId	= (String)objectIdList.get(i);
      			    //This Map used for calculating MOD Stacks
      			  	//changeAppMap.put(changeId, modelAppMap);
      			  	//Get the Required actual expression for a Model Effectivity Map
			    	modelExpr = getModelBasedExpression(modelAppMap);
			    	modelAppMap = new HashMap<String,String>();
			    	changeExprMap.put(SELECT_ID, changeId);
			    	changeExprMap.put(SELECT_ATTRIBUTE_EFFECTIVITY_EXPRESSION, modelExpr);
			    	//Write the Converted PUE ECO ids to log
			    	loadMigratedOids (changeId);
			    	//Prepare change id and Expression Maps for calling CFF api
  			    	changeExprList.add(changeExprMap);
			    	changeExprMap = new HashMap();
        	  }
        	  mqlLogWriter ( "*************************Change Expression Maplist which contains changeId and Model based expression undergoes CFF api " + changeExprList + "\n");
        	  effectivity.setEffectivityOnChange(context, changeExprList);
        	  ContextUtil.commitTransaction(context);
          }
          catch(Exception ex)
          {
        	  ContextUtil.abortTransaction(context);
              ex.printStackTrace();
              throw ex;
          }
      }
/**
 * Prepares a CFF based expression with Model physical id and with corresponding effectivity
 * @param modelAppMap contains Model physical id and effectivity  as key value pairs
 * @return CFF actual expression format contains Model id involved
 */
	private String getModelBasedExpression(HashMap modelAppMap) throws Exception {
		 mqlLogWriter ( "A Model Applicability Map " + modelAppMap + "Results in an Expression");
    	 StringBuffer exprBuffer = new StringBuffer();
    	 itr  = modelAppMap.keySet().iterator();
    	 while (itr.hasNext()) {
    		 modelPhyId = (String)itr.next();
    		 applValue = (String)modelAppMap.get(modelPhyId);
    		 exprBuffer = exprBuffer.length() > 0 ?exprBuffer.append(" ").append("OR").append(" "):exprBuffer;
    		 exprBuffer.append(_KEY_PREFIX)
    		 		   .append(modelPhyId)
    		 		   .append('[')
    		 		   .append(applValue)
    		 		   .append(_KEY_SUFFIX);
    	 }
    	mqlLogWriter (exprBuffer.toString()+"\n");
		return exprBuffer.toString();
		
	}

	/**
      * This method gives the merged/union of two applicabilites
      * eg: 1-50 ,51-100 ==>1-100
      * @param context the Ematrix code context object
      * @param args Strings of applicabilities
      * @return Union of the given applicabilities
      * @throws Exception if the operation fails.
      */	
	 public String getUnionOfApplicabilities(Context context,String[] args) throws Exception {
    	
		String oldApp = args[0];
		String newApp = args[1];
    	boolean subsequentNotation = false;
    	String totalAppString = oldApp + DELIMITER + newApp;
	  	StringList numberList = FrameworkUtil.splitString(totalAppString, ",");

	  	//Adding single digit values into Delimiter List, and adding range values into contigous list
	  	//and if any range value contains infinity, replace that range value with highestbuildNumber+1
	  	for (int i=0;i<numberList.size();i++) {
	  		 val = (String)numberList.get(i);
	  		 if(val.indexOf(CONTIGUOUS_DELIMITER) > -1) {
	  			if (val.indexOf(_INF) > -1) {
	  				highestBuildVal = getHighestBuildValue(context,totalAppString) + 1;
	  				highestVal = String.valueOf(highestBuildVal);
	  				val = FrameworkUtil.findAndReplace(val, _INF, String.valueOf(highestBuildVal));
	  				subsequentNotation = true;
	  			}
  				_contiguousList.addElement(val);
	  		 }else {
	  			_delimiterList.addElement(val);
	  		 }
	  		 
	  	}
	  		//Gives a sorted contiguous list like 1-20,1-5 gives 1-5,1-20
		  	_contiguousList	= getSortedList(_contiguousList);
		  	
		  	//Filters delimiter Values if those are in range of Contiguous list values (5,1-10)
		  	//5 is in range of 1-10 hence 5 is going to be removed from delimiter list
		  	_delimiterList 	= filterDelimiterList(_delimiterList,_contiguousList);
		  	
		  	//Prepares a resultant union list from like 1-5,1-10 ==>1-10
		  	StringList resultList 	  = getResultantStringList(_contiguousList);
		  	
		  	//Prepares a final applicability from delimiter values and resultant contiguous list
		  	//eg:10,11-20 ===>10-20
		  	
	  		String finalApplicability = getFinalApplicability(_delimiterList,resultList);
	  		
	  		//Replaces the highest build value with infinity at infinity case
	  		if (subsequentNotation) {
	  			finalApplicability = FrameworkUtil.findAndReplace(finalApplicability, highestVal, _INF);
	  		}
	  			
	  		System.out.println("THE RESULT STRING******"+finalApplicability);
    	
    	return finalApplicability;
    }
    /**
     * This method gives the final union applicability from _delimiterList and contiguous list
     * eg:[10,30] [11-20,50-100]==>10-20,30,50-100
     * @param _delimiterList StringList of delimiterValues
     * @param resultList StringList of contiguous range values
     * @return String of final Applicability
     */
	private String getFinalApplicability(StringList _delimiterList,StringList resultList) {
	
	_finalBuffer = new StringBuffer();
	if (_delimiterList.size() > 0) {
  		for (int y=0;y<_delimiterList.size();y++) {
  			val = (String)_delimiterList.get(y);
  			delimiterVal = Long.parseLong(val);
  			for (int z=0;z<resultList.size();z++) {
  				currStr = (String)resultList.get(z);
  		  		lowerLimit  = getLowerLimit(currStr);
  		  		higherLimit = getHigherLimit(currStr);
  		  		_finalBuffer = _finalBuffer.length() > 0?_finalBuffer.append(DELIMITER):_finalBuffer;
  		  		if (delimiterVal+1 == lowerLimit) {
  		  			_finalBuffer.append(delimiterVal).append(CONTIGUOUS_DELIMITER).append(higherLimit);
  		  			resultList.remove(currStr);
  		  			break;
  		  		}else if (delimiterVal == higherLimit+1) {
  		  			_finalBuffer.append(lowerLimit).append(CONTIGUOUS_DELIMITER).append(delimiterVal);
  		  			resultList.remove(currStr);
  		  			break;
  		  		}else if (delimiterVal < lowerLimit) {
  		  			_finalBuffer.append(delimiterVal).append(DELIMITER).append(currStr);
  		  			resultList.remove(currStr);
  		  			break;
  		  		}else {
  		  			_finalBuffer.append(currStr);
  		  			//condition of delmiter>UL, go to next value in result list
  		  			if (resultList.size() <= 1 || z+1 == resultList.size()) {
  		  				_finalBuffer.append(DELIMITER).append(delimiterVal);
  		  				break;
  		  			}
  		  			resultList.remove(currStr);
  		  		}	  			
  			}
  		}
	} else {
		for (int z=0;z<resultList.size();z++) {
				currStr = (String)resultList.get(z);
				_finalBuffer = (_finalBuffer.length() > 0 )?_finalBuffer.append(DELIMITER).append(currStr):_finalBuffer.append(currStr);
			}
		}
		return _finalBuffer.toString();
	}

	/**
	 * This method gives the Union of range values list from contiguous list of range values
	 * eg: [1-5,1-8,1-20]===>[1-20]
	 * @param _contiguousList StringList of range values
	 * @return Union of range values
	 * @throws Exception if the operation fails
	 */
	private StringList getResultantStringList(StringList _contiguousList) throws Exception {
		int index =0;
		StringList resultantList  = new StringList();
		String result ="";
	  	for (int x=0;x<_contiguousList.size();x++) {
	  		val = (resultantList.size() > 0)?(String)resultantList.get(index):(String)_contiguousList.get(x);
	  		lowerLimit  = getLowerLimit(val);
	  		higherLimit = getHigherLimit(val);
	  		nextVal = (resultantList.size() > 0)?(String)_contiguousList.get(x):_contiguousList.size()<=1?val:(String)_contiguousList.get(x+1);
	  		if("".equals(nextVal))continue;
	  		nxtLowerLimit  = getLowerLimit(nextVal);
	  		nxtHigherLimit = getHigherLimit(nextVal);
	  		
	  		if (isNumberIsInRange(lowerLimit,higherLimit,nextVal)) {	  			
	  			lowerLimit  = lowerLimit <= nxtLowerLimit?lowerLimit:nxtLowerLimit;
	  			higherLimit = higherLimit >= nxtHigherLimit?higherLimit:nxtHigherLimit;
	  			result = lowerLimit+CONTIGUOUS_DELIMITER+higherLimit;
	  				  			
	  			if (resultantList.size()>0) {
	  				resultantList.set(index, result);
	  			}else {
	  				resultantList.add(index, result);
	  			}
	  			if (x+1==_contiguousList.size()) {
	  				_contiguousList.set(x, "");
	  			}else {
	  				_contiguousList.set(x+1, "");
	  			}
	  			
	  			
	  		}else {
	  			if(!resultantList.contains(val))
	  				resultantList.add(index, val);
	  			resultantList.add(index+1, nextVal);
	  			index = index+1;
	  			_contiguousList.set(x, "");
	  		}
	  	}
		return resultantList;
	}
	/**
	 * Removes delimiter values if those are in contiguous range values
	 * eg: [5,30] [1-20,50-100]===>[30] removes 5 from list as 5 is in range of 1-20
	 * @param _delimiterList StringList of delimiter values
	 * @param _contiguousList StringList of range Values
	 * @return StringList of resultant delimiter values
	 * @throws Exception if the operation fails
	 */
	private StringList filterDelimiterList(StringList _delimiterList,StringList _contiguousList) throws Exception {

		for (int j=0;j<_delimiterList.size();j++) {
	  		val = (String)_delimiterList.get(j);
	  		for (int k=0;k<_contiguousList.size();k++) {
	  			if (checkNumberIsInRange(Long.parseLong(val), (String)_contiguousList.get(k))) {
	  				_delimiterList.remove(val);
	  				break;
	  			}
	  		}
	  	}
		return _delimiterList;
	}
	/**
	 * This method sorts the given range values 
	 * eg:[1-10,6-8]===>[6-8,1-10]
	 * @param _contiguousList StringList of unsorted rangeValues
	 * @return StringList of sorted rangeValues 
	 */
	private StringList getSortedList(StringList _contiguousList) {

		for (int index=0;index<_contiguousList.size();index++) {
  			val = (String)_contiguousList.get(index);
  			if (_contiguousList.size()<=1 || index+1 == _contiguousList.size())
  				break;
  			nextVal = (String)_contiguousList.get(index+1);
  			higherLimit  = getLowerLimit(val);
  			nxtHigherLimit  = getLowerLimit(nextVal);
  			if (higherLimit > nxtHigherLimit) {
  				_contiguousList.set(index, nextVal);
  				_contiguousList.set(index+1, val);
  			}
  		}
		return _contiguousList;
	}
	/**
	 * This method gives the highest build number value from total Applicability String
	 * @param totalAppString contains appended values of total applicability string
	 * @return long of highestBuildnumber
	 */
	private long getHighestBuildValue(Context context,String totalAppString)throws Exception {

  		//if any of the given applicabilites contains infinity value,then replace with highest build number + 1 value
  		if (totalAppString.indexOf(_INF) > -1) {
  			totalAppString = totalAppString.replace(_INF, "");
  		}
  		  		      		
  		//Preparing unique sorted array of numbers with given applicabilities
  		String appendedString 		= totalAppString.replaceAll(DELIMITER, "-");
  		StringList appSeperatedlist = FrameworkUtil.split(appendedString, CONTIGUOUS_DELIMITER);
  		
  		HashSet hSet = new HashSet(appSeperatedlist);
  		if (hSet.contains(""))
  			 hSet.remove("");

  		long []numArr = new long[hSet.size()+1];
  		Iterator itr = hSet.iterator();
  		int x = 0;
  		while (itr.hasNext()) {			
  			numArr[x] = Long.parseLong((String)itr.next());
  			x++;
  		}
  		Arrays.sort(numArr);
  		
		return numArr[numArr.length-1];
	}
	/**
	 * check the given digit is in range or not in 4 possible conditions.eg:(1,5,10)
	 * isInRange(1,5-10)||isInRange(1+1,5-10)||isInRange(5,5-10)||isInRange(5+1,5-10)
	 * @param lowerLimit the lower limit long value
	 * @param higherLimit the higher limit long value
	 * @param currVal range value to be checked
	 * @return true if any one condition gets succeeded
	 * @throws Exception if the operation fails
	 */
	private static Boolean isNumberIsInRange(long lowerLimit, long higherLimit, String currVal) throws Exception{
		
		return (checkNumberIsInRange(lowerLimit, currVal) || checkNumberIsInRange(lowerLimit+1, currVal) || checkNumberIsInRange(higherLimit, currVal) || checkNumberIsInRange(higherLimit+1, currVal));
	}

	/**
	 * Method used to return lower limit of particular string
	 * @param currStr contains any value like 1 or 1,1-5
	 * @return lower limit of any string
	 */
	private static long getLowerLimit(String currStr) {
		
		return Long.parseLong(currStr.replaceAll("(-|,).*", ""));
	}
	
	/**
	 * Method used to return higher limit of any string
	 * @param currStr contains any value like 1,1-5 or 1-5
	 * @return returns higher limit or any string
	 */
	private static long getHigherLimit(String currStr) {
		
		return Long.parseLong(currStr.replaceAll("^.*(-|,)", ""));
	}
	
	/**
	 * Method to check the given number is in the range or Not.	
	 * @param matchStr number to check
	 * @param app the range item being used
	 * @return true if number is in range else false.
	 * @throws Exception if the operation fails
	 */
		private static boolean checkNumberIsInRange(long match, String app) throws Exception {

			StringList splitList = FrameworkUtil.splitString(app, ",");
			Iterator<?>   splitItr  = splitList.iterator();
			while (splitItr.hasNext()) {
				String currStr  = (String)splitItr.next();
				
				if (currStr.indexOf('-') > -1) {
					long startVal = Long.parseLong(currStr.replaceAll("-.*", ""));		
					long endVal   = Long.parseLong(currStr.replaceAll("^.*-", ""));
					if (match >= startVal && match<= endVal) {
						return true;
					}
				} else {
					if (match == Long.parseLong(currStr)) {
						return true;
					}
				}
			}
			return false;
		}


/**
 * This method prepares a MapList of ID's with given stringlist of objects
 * @param objectIdList StringList of objects
 * @return MapList of objects
 */
	private MapList getMapListOfIDs(StringList objectIdList) {
        HashMap objMap;
        MapList mapList = new MapList ();
    	for (int i=0;i < objectIdList.size();i++) {
    		objMap = new HashMap();
    		objMap.put(DomainConstants.SELECT_ID, (String)objectIdList.get(i));
    		mapList.add(objMap);
    	}
    	return mapList;
    }
    
    public void help(Context context, String[] args) throws Exception
    {
        if(!context.isConnected())
        {
            throw new Exception("not supported on desktop client");
        }

        writer.write("================================================================================================\n");
        writer.write(" PUE Migration for Model Based is a two step process  \n");
        writer.write(" Step1: Find all PUE ECO objects except those in implemented and cancelled states and write them into flat files \n");
        writer.write(" Example: \n");
        writer.write(" execute program emxModelBasedPUEMigrationFindObjects 1000 PUE ECO C:/Temp/oids/; \n");
        writer.write(" First parameter  = indicates number of object per file \n");
        writer.write(" Second Parameter = the parent type to searh for \n");
        writer.write(" Third Parameter  = the directory where files should be written \n");
        writer.write(" \n");
        writer.write(" Step2: Migrate the objects \n");
        writer.write(" Example: \n");
        writer.write(" execute program emxModelBasedPUEMigration 'C:/Temp/oids/' 1 n ; \n");
        writer.write(" First parameter  = the directory to read the files from\n");
        writer.write(" Second Parameter = minimum range of file to start migrating  \n");
        writer.write(" Third Parameter  = maximum range of file to end migrating  \n");
        writer.write("        - value of 'n' means all the files starting from mimimum range\n");
        writer.write("================================================================================================\n");
        writer.close();
    }
}
