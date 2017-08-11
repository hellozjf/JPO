import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.MatrixException;
import matrix.util.StringList;

import com.matrixone.apps.domain.CurrencyConversion;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.domain.util.FrameworkUtil;


public class emxCurrencyConversionBase_mxJPO {

	 /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails      
     */
    public emxCurrencyConversionBase_mxJPO (Context context, String[] args)
        throws Exception
    {
        super();
    }
    
    /**
     * gets all Currency Exchange Rate
     *
     * @return MapList -  Currency Exchange Rate
     * @throws Exception if the operation fails
     * @since 10-7
     */
   @com.matrixone.apps.framework.ui.ProgramCallable
   public MapList getCurrencyExchangeRate(matrix.db.Context context, String[] args) throws Exception, MatrixException
   {
         Map programMap      = (Map)JPO.unpackArgs(args);
         String companyId    = (String)programMap.get("objectId");
         Map paramList       = (Map) programMap.get("paramList");

         StringList objectSelects = new StringList();
         String SELECT_RELATIONSHIP_RATE_PERIOD_ID = "from[" + CurrencyConversion.RELATIONSHIP_RATE_PERIOD + "].id";
         CurrencyConversion currConv = new CurrencyConversion();
        
         CurrencyConversion.MULTI_VALUE_LIST.add(SELECT_RELATIONSHIP_RATE_PERIOD_ID);
         objectSelects = new StringList(CurrencyConversion._currencyConversionSelects);
         objectSelects.add( SELECT_RELATIONSHIP_RATE_PERIOD_ID);
         
         MapList totalresultList = currConv.getCurrencyConversions(context,objectSelects, false,companyId);

         if ( totalresultList == null )
         {
           totalresultList = new MapList();
         }
         Iterator totalItr = totalresultList.iterator();
         MapList finalMapList = new MapList();
         
         Map mapCurrencyConversion1 = null;
         String fromCurrency        = null;
         String toCurrency          = null;
         StringList connectionIds   = null;
         StringList rates           = null;
         StringList dates           = null;
         String connectionID        = null;
         String rate                = null;
         String date                = null;
         HashMap rateMap            = null;
         while(totalItr.hasNext())
         {
           mapCurrencyConversion1 = (Map)totalItr.next();
           fromCurrency        = (String)mapCurrencyConversion1.get(CurrencyConversion.SELECT_NAME);
           toCurrency          = (String)mapCurrencyConversion1.get(CurrencyConversion.SELECT_REVISION);

           connectionIds = new StringList();
           rates         = new StringList();
           dates         = new StringList();
           try
           {
             connectionIds = (StringList)mapCurrencyConversion1.get(SELECT_RELATIONSHIP_RATE_PERIOD_ID);
             rates         = (StringList)mapCurrencyConversion1.get(CurrencyConversion.SELECT_RATE_PERIOD_RATE);
             dates         = (StringList)mapCurrencyConversion1.get(CurrencyConversion.SELECT_RATE_PERIOD_START_EFFECTIVITY);
           }
           catch (ClassCastException cex)
           {
               connectionIds.add((String)mapCurrencyConversion1.get(SELECT_RELATIONSHIP_RATE_PERIOD_ID));
               rates.add((String)mapCurrencyConversion1.get(CurrencyConversion.SELECT_RATE_PERIOD_RATE));
               dates.add((String)mapCurrencyConversion1.get(CurrencyConversion.SELECT_RATE_PERIOD_START_EFFECTIVITY));
           }
           
        
           for (int i=0; connectionIds != null && i<connectionIds.size(); i++) {
             connectionID = (String)connectionIds.elementAt(i);
             rate         = (String)rates.elementAt(i);
             date         = (String)dates.elementAt(i);
             rateMap      = new HashMap();
             rateMap.put(SELECT_RELATIONSHIP_RATE_PERIOD_ID,connectionID);
             rateMap.put(CurrencyConversion.SELECT_NAME,fromCurrency);
             rateMap.put(CurrencyConversion.SELECT_REVISION,toCurrency);
             rateMap.put(CurrencyConversion.SELECT_RATE_PERIOD_RATE,rate);
             rateMap.put(CurrencyConversion.SELECT_RATE_PERIOD_START_EFFECTIVITY,date);
             rateMap.put(CurrencyConversion.SELECT_ID,(String)mapCurrencyConversion1.get(CurrencyConversion.SELECT_ID));
             rateMap.put("id[connection]",connectionID);
             finalMapList.add(rateMap);
           }
         }
         return finalMapList;
   }
   
   /**
    * This method is written to get the FromCurrencyExchangeRateName for the table SCSCurrencyExchangeRateSummary
    *
    * @return Vector
    * @throws Exception if the operation fails
    * @since 10-7
    */

  public Vector getCurrencyExchangeRateName(matrix.db.Context context, String[] args) throws Exception, MatrixException
  {
      Vector ecrStates   = new Vector();
      HashMap programMap = (HashMap)JPO.unpackArgs(args);
      MapList objectList = (MapList)programMap.get("objectList");
      Iterator objIter   = objectList.iterator();

      Map paramList      = (Map)programMap.get("paramList");
      String languageStr = (String)paramList.get("languageStr");
      Map map            = null;
      String fromCurr    = null;
      while(objIter.hasNext())
      {
          map = (Map)objIter.next();
          fromCurr = (String)map.get(DomainConstants.SELECT_NAME);
          if(fromCurr == null)
              fromCurr = "";
          int index = -1;
          if((index=fromCurr.indexOf("~"))==-1){
              fromCurr = i18nNow.getRangeI18NString(CurrencyConversion.ATTRIBUTE_CURRENCY,fromCurr,languageStr);
              ecrStates.add(fromCurr);
          }
          else {
              String fromCurr1 = fromCurr.substring(index+1);
              fromCurr1 = i18nNow.getRangeI18NString(CurrencyConversion.ATTRIBUTE_CURRENCY,fromCurr1,languageStr);
              ecrStates.add(fromCurr1);
          }
      }
      return ecrStates;
   }
  
  /**
   * This method is written to get the ToCurrencyExchangeRateName for the table SCSCurrencyExchangeRateSummary
   *
   * @return Vector
   * @throws Exception if the operation fails
   * @since v6r2009
*/
public Vector getToCurrencyExchangeRateName(matrix.db.Context context, String[] args) throws Exception, MatrixException
 {

     Vector ecrStates   = new Vector();
     HashMap programMap = (HashMap)JPO.unpackArgs(args);
     MapList objectList = (MapList)programMap.get("objectList");
     Iterator objIter   = objectList.iterator();

     Map paramList      = (Map)programMap.get("paramList");
     String languageStr = (String)paramList.get("languageStr");
     Map map            = null;
     String toCurr      = null;
     while(objIter.hasNext())
     {
         map       = (Map)objIter.next();
         toCurr = (String)map.get(DomainConstants.SELECT_REVISION);
         if(toCurr == null)
             toCurr = "";
         int index = -1;
         if((index=toCurr.indexOf("~"))==-1){
             toCurr=i18nNow.getRangeI18NString(CurrencyConversion.ATTRIBUTE_CURRENCY,toCurr,languageStr);
             ecrStates.add(toCurr);
         }
         else {
             String toCurr1 = toCurr.substring(index+1);
             toCurr1 = i18nNow.getRangeI18NString(CurrencyConversion.ATTRIBUTE_CURRENCY,toCurr1,languageStr);
             ecrStates.add(toCurr1);
         }
     }
     return ecrStates;
}

   /**
    * This method is to get the currency choices for add new currency conversion 
    * @param context
    * @param args
    * @return
    * @throws Exception
    */
    public HashMap getCurrencyChoices(matrix.db.Context context, String[] args) throws MatrixException
    {
       //Modified:23-Feb-2011:hp5:R211:PRG:IR-093750V6R2012
    	try 
    	{
    	   Map programMap = (Map)JPO.unpackArgs(args);
    	   Map paramMap   = (Map)programMap.get("paramMap");
    	   String sLanguage = (String) paramMap.get("languageStr");
		   StringList currencyRangeValues  = FrameworkUtil.getRanges(context, CurrencyConversion.	ATTRIBUTE_CURRENCY);
		   StringList displayValue = new StringList(currencyRangeValues.size());
			
           for (int i = 0; i < currencyRangeValues.size(); i++) {
                displayValue.add(i18nNow.getRangeI18NString(CurrencyConversion.ATTRIBUTE_CURRENCY, (String)currencyRangeValues.get(i), sLanguage));
            }
		   HashMap resultMap = new HashMap();
		   resultMap.put("field_choices", currencyRangeValues);
		   resultMap.put("field_display_choices", displayValue);
		   return  resultMap;
    	} 
    	catch (Exception e) {
    		throw new MatrixException(e);  
		}
    	//End:23-Feb-2011:hp5:R211:PRG:IR-093750V6R2012
    }
    
    /**
     * This method is to update the existing currency conversion rate
     * @param context
     * @param args
     * @throws Exception
     */
    public void updateCurrencyRate(Context context, String[] args)throws Exception, MatrixException {
  	  Map programMap = (Map)JPO.unpackArgs(args);
  	  Map paramMap   = (Map)programMap.get("paramMap");
  	  String relId       = (String)paramMap.get("relId");
      String strNewValue = (String)paramMap.get("New Value");

	  	try
	    {
	  		DomainRelationship domainRelationship = new DomainRelationship(relId);	  			
	        domainRelationship.setAttributeValue(context,CurrencyConversion.ATTRIBUTE_RATE, strNewValue);	      
	    }
	    catch(Exception e)
	    {
	    	throw new MatrixException(e.toString());
	    }
     }
   
 
    /**
     * This method is to update the existing currency conversion effectivity date
     * @param context
     * @param args
     * @throws Exception
     */
    public void updateCurrencyConvEffectivityDate(Context context, String[] args) throws Exception, MatrixException {
	      Map programMap = (Map)JPO.unpackArgs(args);
	      Map paramMap   = (Map)programMap.get("paramMap");
	      Map requestMap = (Map) programMap.get("requestMap");
	      Locale locale = (Locale)requestMap.get("locale");
    	  String relId       = (String)paramMap.get("relId");
          String strNewValue = (String)paramMap.get("New Value");
	  	  	try
	  	    {
	  	       double clientTZOffset  = Double.parseDouble((String)requestMap.get("timeZone"));
	  	       String strDate = eMatrixDateFormat.getFormattedInputDate(context, strNewValue, clientTZOffset, locale);	  	  	
	  	  	   DomainRelationship domainRelationship = new DomainRelationship(relId);
	  	       domainRelationship.setAttributeValue(context,CurrencyConversion.ATTRIBUTE_START_EFFECTIVITY, strDate);
	  	    }
	  	    catch(Exception e)
	  	    {
	  	    	throw new MatrixException(e.toString());
	  	    }
       }
    
}
