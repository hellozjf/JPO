/*
**  emxAEFCollectionBase
**
**  Copyright (c) 1992-2016 Dassault Systemes.
**  All Rights Reserved.
**  This program contains proprietary and trade secret information of MatrixOne,
**  Inc.  Copyright notice is precautionary only
**  and does not evidence any actual or intended publication of such program
**
**   This JPO contains the implementation of emxAEFCollectionBase
*/

import matrix.db.*;
import matrix.util.*;
import java.util.*;
import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.UIUtil;


/**
 * The <code>emxAEFCollectionBase</code> class contains methods for the "Collection" Common Component.
 *
 * @version AEF 10.0.Patch1.0 - Copyright (c) 2003, MatrixOne, Inc.
 */

public class emxAEFCollectionBase_mxJPO
{

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 10.0.Patch1.0
     */

    public emxAEFCollectionBase_mxJPO(Context context, String[] args)
      throws Exception
    {
        //super(context, args);
    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return int 0, status code.
     * @throws Exception if the operation fails
     * @since AEF 10.0.Patch1.0
     */

    public int mxMain(Context context, String[] args)
        throws Exception
    {
        if (!context.isConnected())
        {
            HashMap paramMap = (HashMap)JPO.unpackArgs(args);
            Map paramList = (Map)paramMap.get("paramList");
            String languageStr = (String)paramList.get("languageStr");
            String exMsg = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Message.DesktopClient", new Locale(languageStr));
            throw new Exception(exMsg);
        }
        return 0;
    }

    /**
     * This method gets the List of Collections depending on the context.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return MapList contains list of collections
     * @throws Exception if the operation fails
     * @since AEF 10.0.Patch1.0
     * @modified V6R2014x for refactoring
     */

    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getCollections (Context context,String[] args)
        throws Exception
    {
       	MapList returnMapList = new MapList();
       	HashMap pMap = (HashMap)JPO.unpackArgs(args);
       	String strLang = (String) pMap.get("languageStr");

       	String sName = (String)pMap.get("Name");
       	boolean isSearch = false;
       	ArrayList patternList = new ArrayList();
       	/* Check for null if Navigation is from My Desk--> Collection */
       	if(sName != null) {
			isSearch = true;
		}

       	/* Build the Tokens for Pattern Matching */
       	if(isSearch) {
       		sName = sName.trim();
       		if("".equals(sName)) {
	   			sName = "*";
	   		}

			if(sName.indexOf(",") != -1) {
				StringTokenizer token = new StringTokenizer(sName, ",");
				while(token.hasMoreTokens()) {
					patternList.add(token.nextToken().trim().toUpperCase());
				}
			} else {
				patternList.add(sName.toUpperCase());
			}
		}

       	return getCollectionList(context, strLang, isSearch, patternList);

    }

    /**
     * This method gets the List of Objects.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a Map with the following entries:
     *    treeLabel contains a String of the treeLabel name.
     * @return MapList contains list of Objects
     * @throws Exception if the operation fails
     * @since AEF 10.0.Patch1.0
     */

    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getObjects (Context context,String[] args)
        throws Exception
    {

        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        MapList totalresultList = new MapList();
        String sCharSet = (String)paramMap.get("charSet");
        String strSetId = (String)paramMap.get("relId");
        String strSetname = (String)paramMap.get("treeLabel");
        String strLang = (String) paramMap.get("languageStr");

        Map collectionMap = null;
        MapList collMapList = SetUtil.getCollections(context);
        for (int i=0; i < collMapList.size(); i++)
        {
            collectionMap = (Map) collMapList.get(i);
            if (strSetId.equals(collectionMap.get("id")))
            {
                break;
            }
        }
        strSetname = (String) collectionMap.get("name");

        /*
        if(strSetname != null && strSetname.indexOf("%") > -1){
        	strSetname = FrameworkUtil.decodeURL(strSetname,sCharSet);
        }
          // To get System Generated Collection Label
          String strSystemGeneratedCollectionLabel = UINavigatorUtil.getI18nString("emxFramework.ClipBoardCollection.NameLabel", "emxFrameworkStringResource", strLang);
          if(strSetname.equals(strSystemGeneratedCollectionLabel))
          {
			  //Modified for Bug 342586
			  strSetname = FrameworkProperties.getProperty(context, "emxFramework.ClipBoardCollection.Name");
         }
        */

        SelectList resultSelects = new SelectList(5);
        resultSelects.add(DomainConstants.SELECT_ID);
        resultSelects.add(DomainConstants.SELECT_NAME);
        resultSelects.add(DomainConstants.SELECT_TYPE);
        resultSelects.add("current.access[read]");
		resultSelects.add("current.access[show]");


        try {
            totalresultList = SetUtil.getMembers(context,
                                                 strSetname,
                                                 resultSelects);



			// If current user has the show+read access for the object (in its current state) then only show that object.
			//
			int intTotalResultSize = totalresultList.size();
			for (int i=0; i < intTotalResultSize; i++)
			{
				// Get object id
				HashMap mapObject	= (HashMap)totalresultList.get(i);
				if (mapObject == null)
				{
					totalresultList.remove(i);
					intTotalResultSize--;
					i--;
				}
				else
				{
					String strObjectId		= (String)mapObject.get(DomainObject.SELECT_ID);
					boolean isShowAccess	= "true".equalsIgnoreCase( (String)mapObject.get("current.access[show]") );
					boolean isReadAccess	= "true".equalsIgnoreCase( (String)mapObject.get("current.access[read]") );

					if (strObjectId == null || "".equals(strObjectId))
					{
						totalresultList.remove(i);
						intTotalResultSize--;
						i--;
					}
					else
					{
						if ( !(isReadAccess && isShowAccess) )
						{
							totalresultList.remove(i);
							intTotalResultSize--;
							i--;
						}
					}//else !
				}//else !
			}//for !


        }
        catch (Exception e) {
            throw new Exception(e.toString());
        }

        return totalresultList;

    }

    /**
     * This method gets the Object Names.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a Map with the following entries:
     *    objectList contains a MapList of Maps which contains object names
     * @return Vector contains list of Object names
     * @throws Exception if the operation fails
     * @since AEF 10.0.Patch1.0
     */

    public Vector getName(Context context,String[] args)
        throws Exception
    {

        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        //Added to support collections in structure browser.
        HashMap requestMap = (HashMap)programMap.get("paramList");
        String strLang	   = (String)requestMap.get("languageStr");


        MapList relBusObjPageList = (MapList)programMap.get("objectList");
        Vector vec = new Vector(relBusObjPageList.size());

        try{
            for (int i=0; i < relBusObjPageList.size(); i++)
            {
                HashMap collMap = (HashMap)relBusObjPageList.get(i);
                String StrName  = (String)collMap.get("name");

                // Modified for Clipboard Collections
				//Modified for Bug 342586
				String strSystemGenCollection = EnoviaResourceBundle.getProperty(context, "emxFramework.ClipBoardCollection.Name");
                if(StrName.equalsIgnoreCase(strSystemGenCollection))
                {
                    StrName = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", new Locale(strLang), "emxFramework.ClipBoardCollection.NameLabel");
                }

                vec.addElement(StrName);
            }
        }
        catch (Exception e) {
            throw new Exception(e.toString());
        }

        return vec;
    }

    /**
     * This method gets the Description of Objects.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a Map with the following entries:
     *    objectList contains a MapList of Maps which contains object names
     * @return Vector containing list of Objects description
     * @throws Exception if the operation fails
     * @since AEF 10.0.Patch1.0
     */

  public Vector getDescription(Context context,String[] args)
        throws Exception
    {

        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList relBusObjPageList = (MapList)programMap.get("objectList");
        Vector vec = new Vector(relBusObjPageList.size());

        try{
            for (int i=0; i < relBusObjPageList.size(); i++)
            {
                HashMap collMap = (HashMap)relBusObjPageList.get(i);
                String strName  = (String)collMap.get("name"); //list set test1 select property[description].value dump |; added for IR -168535V6R2013x
                String strDescProperty  =  MqlUtil.mqlCommand(context, "list set $1 select $2 dump $3 ",strName,"property[description].value","|");
                if(strDescProperty.equalsIgnoreCase("null")||  strDescProperty==null)
                	strDescProperty="";
                String strSystemGenCollection = EnoviaResourceBundle.getProperty(context, "emxFramework.ClipBoardCollection.Name");
                if(strName.equalsIgnoreCase(strSystemGenCollection)){
                	strDescProperty = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", new Locale((String)((HashMap)programMap.get("paramList")).get("languageStr")), "emxFramework.ClipBoardCollection.Description");
                }
                vec.addElement(strDescProperty);
            }
        }
        catch (Exception e)
        {
            throw new Exception(e.toString());
        }
        return vec;
    }

    /**
     * This method gets the Count of Objects.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a Map with the following entries:
     *    objectList contains a MapList of Maps which contains objects count.
     * @return Vector containing list of Objects Count
     * @throws Exception if the operation fails
     * @since AEF 10.0.Patch1.0
     */

    public Vector getCount (Context context,String[] args)
        throws Exception
    {

        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList relBusObjPageList = (MapList)programMap.get("objectList");
        Vector vec = new Vector(relBusObjPageList.size());

        try{
            for (int i=0; i < relBusObjPageList.size(); i++) {
                HashMap collMap = (HashMap)relBusObjPageList.get(i);
                String StrName=(String)collMap.get("count");
                vec.addElement(StrName);
            }
        }
        catch (Exception e) {
            throw new Exception(e.toString());
        }

        return vec;
    }
    /**
     * This method gets the collections Names.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a Map with the following entries:
     *    objectList contains a MapList of Maps which contains object names
     * @return Vector contains list of Object names
     * @throws Exception if the operation fails
     */
    public Vector getCollectionsName(Context context,String[] args)
    throws Exception
    {

        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap)programMap.get("paramList");
        String strLang     = (String)requestMap.get("languageStr");

        MapList relBusObjPageList = (MapList)programMap.get("objectList");
        Vector vec = new Vector(relBusObjPageList.size());

        try{
            for (int i=0; i < relBusObjPageList.size(); i++)
            {
                HashMap collMap = (HashMap)relBusObjPageList.get(i);
                String StrName  = (String)collMap.get("name");
                //Modified for Bug 342586
				String strSystemGenCollection = EnoviaResourceBundle.getProperty(context, "emxFramework.ClipBoardCollection.Name");
                if(StrName.equalsIgnoreCase(strSystemGenCollection))
                {
                    StrName = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", new Locale(strLang), "emxFramework.ClipBoardCollection.NameLabel");
                }

                String html = "<img height=\"16\" src=\"../common/images/iconSmallCollection.gif\" border=\"0\"/>&#160;";
                html += "<a href=\"javascript:try{parent.collectionItems('"+XSSUtil.encodeForJavaScript(context, StrName)+"');}catch(e){}\">"+XSSUtil.encodeForHTML(context, StrName) +"</a>";

                vec.addElement(html);
            }
        }
        catch (Exception e) {
            throw new Exception(e.toString());
       }

        return vec;
    }

    /**
	 * This method matches the Name pattern given as search criteria
	 *
	 * @param pattern the name patterns (can be comma separated)
	 * @param collection the actual collection name
	 * @return true on success
	 * @since B&W V6 Upgrade
	 * @author Kaustav Banerjee
     */
    static boolean matches(String pattern, String collection) {
		// Add sentinel so don't need to worry about *'s at end of pattern
	  	collection    += '\0';
	  	pattern += '\0';

	  	int N = pattern.length();

	  	boolean[] states = new boolean[N+1];
	  	boolean[] old = new boolean[N+1];
	  	old[0] = true;

	  	for (int i = 0; i < collection.length(); i++) {
			char c = collection.charAt(i);
		 	states = new boolean[N+1];       // initialized to false
		 	for (int j = 0; j < N; j++) {
				char p = pattern.charAt(j);

				// Hack to handle *'s that match 0 characters
				if (old[j] && (p == '*')) old[j+1] = true;
				if (old[j] && (p ==  c )) states[j+1] = true;
				if (old[j] && (p == '.')) states[j+1] = true;
				if (old[j] && (p == '*')) states[j]   = true;
				if (old[j] && (p == '*')) states[j+1] = true;
		 	}
		 	old = states;
	  	}
	  	return states[N];
   	}


      public static MapList getTitle(Context context, String[] args) throws Exception {
        // unpack args array to get input map
          HashMap programMap = (HashMap) JPO.unpackArgs(args);
        // get object list
          MapList objectList= (MapList) programMap.get("objectList");

          boolean showTitleFlag = false;

          if(objectList != null){
              for (int i=0; i < objectList.size(); i++){
                  HashMap collMap = (HashMap)objectList.get(i);
                  String objectId = (String) collMap.get("id");
                  DomainObject obj = new DomainObject(objectId);
                  String strAttribute = "";
                  try{
                      String SELECT_ATTRIBUTE__TITLE = "attribute[" + DomainConstants.ATTRIBUTE_TITLE + "]";
                      strAttribute = obj.getInfo(context, SELECT_ATTRIBUTE__TITLE);
                      if(strAttribute != null && !"".equals(strAttribute)){
                          showTitleFlag = true;
                          break;
                      }
                  }catch(Exception ex){
                  }
              }
           }

         //Define a new MapList to return.
          MapList columnMapList = new MapList();
          if(showTitleFlag){
             // create a column map to be returned.
              Map colMap = new HashMap();
              HashMap settingsMap = new HashMap();
            // Set information of Column Settings in settingsMap
              settingsMap.put("Registered Suite","Framework");
           // set column information
              colMap.put("name", "Title" );
              colMap.put("label", "emxFramework.Common.Title" );
              colMap.put("expression_businessobject","attribute[Title]");
              colMap.put("settings",settingsMap);
              columnMapList.add(colMap);
          }
        // return final list
        return columnMapList;
        }

      public static MapList getIcon(Context context, String[] args) throws Exception {
    	  HashMap programMap = (HashMap) JPO.unpackArgs(args);
    	  MapList iconMap = new MapList();
    	  Map<String, String> iconHm = new HashMap<String, String>();
    	  MapList objectList= (MapList) programMap.get("objectList");
    	  String objectInfo = "";
    	  if(objectList != null){
    		  for (int i=0; i < objectList.size(); i++){
    			  HashMap collMap = (HashMap)objectList.get(i);
    			  objectInfo = UIUtil.isNullOrEmpty((String) collMap.get("name"))?(String) collMap.get("id"):(String) collMap.get("name");
    			  iconHm.put(objectInfo, "iconSmallCollection.gif");
    			  iconMap.add(iconHm);
    		  }
    	  }
    	  return iconMap;
      }

	/**
	* This method gets the List of Collections depending on the context for Widgets.
	*
	* @param context the eMatrix <code>Context</code> object
	* @param args holds no arguments
	* @return MapList contains list of collections
	* @throws Exception if the operation fails
	* @since V6R2014x
	*/

	public MapList getCollectionsForWidgets (Context context,String[] args)
	throws Exception{
		HashMap paramMap = (HashMap)JPO.unpackArgs(args);
		HashMap widgetArgsMapList = (HashMap) paramMap.get("JPO_WIDGET_ARGS");
		String strLang = (String)widgetArgsMapList.get("arg_language");

		return getCollectionList(context, strLang, false, new ArrayList());
	}


	/**
	 * This is a private method which returns set of collections
	 * @param context
	 * @param strLang
	 * @param isSearch
	 * @param patternList
	 * @return MapList containing Collections
	 * @throws Exception
	 * @since V6R2014x
	 */
	private MapList getCollectionList(Context context, String strLang, boolean isSearch, ArrayList patternList)
	throws Exception{
		MapList collMapList = new MapList();
		MapList returnMapList = new MapList();
		try {
			collMapList = SetUtil.getCollections(context);
            String strSystemGeneratedCollection = EnoviaResourceBundle.getProperty(context, "emxFramework.ClipBoardCollection.Name");

			for (int i=0; i < collMapList.size(); i++) {
				HashMap collMap = (HashMap)collMapList.get(i);
				HashMap hm = new HashMap();
				String sCollectionName  = (String)collMap.get(DomainConstants.SELECT_NAME);
				String sCollectionCount = (String)collMap.get("count");
				hm.put("name",sCollectionName);
				hm.put("id[connection]", (String)collMap.get(DomainConstants.SELECT_ID));
				hm.put("description", (String)collMap.get(DomainConstants.SELECT_DESCRIPTION));

                String strDescription = null;
				if(sCollectionName.equalsIgnoreCase(strSystemGeneratedCollection)){
					sCollectionName = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", new Locale(strLang), "emxFramework.ClipBoardCollection.NameLabel");

                  	strDescription = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", new Locale(strLang), "emxFramework.ClipBoardCollection.Description");
				}

				if(isSearch){
					for(int pat = 0;  pat < patternList.size(); pat++) {
						String pattern = (String) patternList.get(pat);
						// Match the Name Pattern
						if (matches(pattern, sCollectionName.toUpperCase())) {
							hm.put("id",XSSUtil.encodeForURL(context,sCollectionName));
							hm.put("count",sCollectionCount);
							returnMapList.add(hm);
						}
					}
				}else{
                    hm.put("name",sCollectionName);
					hm.put("count",sCollectionCount);
                    if (strDescription != null) {
                        hm.put("description", strDescription);
                    }
					returnMapList.add(hm);
				}
			}
		}catch (Exception e){
			throw new Exception(e.toString());
		}

		return returnMapList;
	}

}
