/*
 *  emxCommonDocumentUIBase.java
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 */
import matrix.db.*;
import matrix.util.*;
import java.util.*;
import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.UINavigatorUtil;
import com.matrixone.apps.common.*;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.framework.ui.UIForm;

/**
 * The <code>emxDocumentUtilBase</code> class contains utility methods for
 * getting data using configurable table APPDocumentSummary
 *
 * @version Common 10-5 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxCommonDocumentUIBase_mxJPO
{


    static public Map typeMapping = new HashMap();

   /**
     * Constructor
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since Common 10.5
     * @grade 0
     */
    public emxCommonDocumentUIBase_mxJPO (Context context, String[] args)
        throws Exception
    {
    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @returns nothing
     * @throws Exception if the operation fails
     * @since Common 10.5
     * @grade 0
     */
    public int mxMain(Context context, String[] args)
        throws Exception
    {
        if (!context.isConnected())
            throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.Generic.NotSupportedOnDesktopClient", context.getLocale().getLanguage()));
        return 0;
    }

    /**
     * gets the list of connected DOCUMENTS to the master Object
     * Used for APPDocumentSummary table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectId - parent object OID
     * @returns Object
     * @throws Exception if the operation fails
     * @since Common 10.5
     * @grade 0
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public Object getDocuments(Context context, String[] args)
        throws Exception
    {
        try
        {
            HashMap programMap        = (HashMap) JPO.unpackArgs(args);
            String  parentId          = (String) programMap.get("objectId");
            String  parentRel         = (String) programMap.get("parentRelName");
            Pattern relPattern        = new Pattern("");

            // If parent relation ship is passed separated by comma
            // Tokenize and add it rel pattern

            if(parentRel != null)
            {
                StringTokenizer relString = new StringTokenizer(parentRel,",");
                while (relString.hasMoreTokens())
                {
                    String relStr = relString.nextToken().trim();
                    if(relStr != null && !"null".equals(relStr) && !"".equals(relStr))
                    {
                        String actRelName = PropertyUtil.getSchemaProperty(context, relStr);
                        if(actRelName != null && !"null".equals(actRelName) && !"".equals(actRelName))
                        {
                           relPattern.addPattern(actRelName);
                        }
                    }
                }
            }

            // if not passed, or non-existing relationship passed then default to "Reference Document" relationship
            if("".equals(relPattern.getPattern()))
            {
                relPattern.addPattern(PropertyUtil.getSchemaProperty(context, CommonDocument.SYMBOLIC_relationship_ReferenceDocument));
            }

            String objectWhere = "";//CommonDocument.SELECT_IS_VERSION_OBJECT + "==\"False\"";

            DomainObject masterObject = DomainObject.newInstance(context, parentId);

            StringList typeSelects = new StringList(1);
            typeSelects.add(CommonDocument.SELECT_ID);
            StringList relSelects = new StringList(1);
            relSelects.add(CommonDocument.SELECT_RELATIONSHIP_ID);
            MapList documentList = masterObject.getRelatedObjects(context,
                                                          relPattern.getPattern(),
                                                          "*",
                                                          typeSelects,
                                                          relSelects,
                                                          false,
                                                          true,
                                                          (short)1,
                                                          objectWhere,
                                                          null,
                                                          null,
                                                          null,
                                                          null);

            return documentList;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
    }

   /**
    *  Get Maplist containing Revisions Info for Id passed In
    *  Used for Revision Summary Page in APPDocumentSummary table
    *  revision column
    *
    *  @param context the eMatrix <code>Context</code> object
    *  @param args an array of String arguments for this method
    *  @return MapList containing Revisions Info
    *  @throws Exception if the operation fails
    *
    * @since Common 10.5
    */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public static MapList getRevisions(Context context, String[] args)
        throws Exception
    {

        HashMap map = (HashMap) JPO.unpackArgs(args);

        String       objectId = (String) map.get("objectId");
        DomainObject busObj   = DomainObject.newInstance(context, objectId);

        StringList busSelects = new StringList(1);
        busSelects.add(DomainObject.SELECT_ID);

        // for the Id passed, get revisions Info
        MapList revisionsList = busObj.getRevisionsInfo(context,busSelects,
                                                          new StringList(0));

        return revisionsList;
    }

   /**
    *  This method is to be called from a UI component Table. The method
    *  suppress the revision value for the type type_ProjectVault.
    *  The type type_ProjectVault uses a auto generated value and users don't
    *  want to see the value in the UI interface.  The method takes the
    *  objectList from the UI table and parses through the revision values.
    *  Objects of type type_ProjectVault have the revision value replaced with
    *  a blank value.  For all other types, the revision values is returned.
    *
    *  @param context the eMatrix <code>Context</code> object
    *  @param args an array of String arguments for this method
    *  @return Vector object that contains a vector of revision values
    *          for the column.
    *  @throws Exception if the operation fails
    *
    *  @since Common 10.5
    *  @grade 0
    */
    public Object getRevisionLevel ( Context context, String[] args )
          throws Exception
    {

        // unpack and get parameter
        HashMap programmap  = (HashMap)JPO.unpackArgs(args);
        MapList objectList = (MapList)programmap.get("objectList");

        Vector revisionVector = new Vector(objectList.size());
        Map objectMap = null;

        // loop through objects that are in the UI table.  populate Vector
        // with the appropriate revision value.
        for ( int i = 0; i < objectList.size(); i++ )
        {

           objectMap       = (Map) objectList.get(i);
           String objectId = (String)objectMap.get(CommonDocument.SELECT_ID);

           // get object type, revision
           DomainObject domainObject = DomainObject.newInstance(context, objectId );

           StringList busSelects = new StringList(1);
           busSelects.add(CommonDocument.SELECT_ID);
           busSelects.add(CommonDocument.SELECT_TYPE);
           busSelects.add(CommonDocument.SELECT_REVISION);

           Map objectDataMap = domainObject.getInfo(context,busSelects);

           String typeName = (String)objectDataMap.get (CommonDocument.SELECT_TYPE);


           // initialize to be blank, will be returned for type_ProjectVault
           // objects.
           String revLevel = "";

           // if the type isn't a type_ProjectVault, set gather revision level.
           if ( ! typeName.equals (
				   PropertyUtil.getSchemaProperty ( context, "type_ProjectVault" ) ) )
           {
             revLevel = (String)objectDataMap.get (CommonDocument.SELECT_REVISION);
           }

           // set a revision level for the object.
           revisionVector.add(revLevel);
        }

        return revisionVector;
    }

    protected static String removeSpace(String formatStr) {

       int flag = 1;
       int strLength = 0;
       int index = 0;

       while  (flag != 0)  {
         strLength = formatStr.length();
         index = 0;
         index = formatStr.indexOf(' ');
         if (index == -1) {
           flag = 0;
           break;
         }

         String tempStr1 = formatStr.substring(0,index);
         String tempStr2 = formatStr.substring(index+1,strLength);
         formatStr = tempStr1 + tempStr2;
       }
       return formatStr;
    }
    /**
     * getNameOrTitle - Will get the Name/Title for Content Summary Table
     *       Will be called in the Name Column
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Object of type Vector
     * @throws Exception if the operation fails
     * @since V10 Patch1
     */

    public Vector getNameOrTitle(Context context, String[] args)
      throws Exception
    {
        Vector vName = new Vector();
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);

            MapList objectList = (MapList)programMap.get("objectList");
            Map paramList = (Map)programMap.get("paramList");
            Map objectMap = null;
            boolean isprinterFriendly = false;
            if(paramList.get("reportFormat") != null)
            {
               isprinterFriendly = true;
            }

            String jsTreeID = (String)paramList.get("jsTreeID");
            String name = "";
            String title = "";
            String nameOrTitle = "";
            String objectType = "";
            String parentType = "";

            StringBuffer sBuff= new StringBuffer(256);
            StringBuffer sbNextURL= new StringBuffer(128);
            String objectId = "";
            String objectIcon = "";

            for(int i = 0 ; i < objectList.size()  ; i++)
            {
                sBuff= new StringBuffer(256);
                sbNextURL= new StringBuffer(128);
                objectMap = (Hashtable)objectList.get(i);
                objectId = (String) objectMap.get(CommonDocument.SELECT_ID);
                name = (String) objectMap.get(CommonDocument.SELECT_NAME);
                title = (String) objectMap.get(CommonDocument.SELECT_TITLE);
                objectType = (String) objectMap.get(CommonDocument.SELECT_TYPE);
                objectIcon = "../common/images/iconSmall"+removeSpace(objectType)+".gif";
                parentType = CommonDocument.getParentType(context, objectType);
                // Get the image of the Type. If a specific naming convention
                // of the image is followed , the following code can be generalized.
                if (title != null && !"".equals(title) && !"null".equals(title))
                {
                  // For a Document the Name will be in the Title object
                  nameOrTitle = title;
                }
                else
                {
                   nameOrTitle = name;
                }
                sbNextURL.append("../common/emxTree.jsp?objectId=");
                sbNextURL.append(objectId);
                sbNextURL.append("&mode=insert&jsTreeID=");
                sbNextURL.append(jsTreeID);
                sbNextURL.append("&DefaultCategory=APPDocumentFiles&AppendParameters=true");
                if(!isprinterFriendly)
                {
                    sBuff.append("<a href ='");
                    sBuff.append(sbNextURL.toString());
                    sBuff.append(" ' class='object' target=\"content\">");
                }
                sBuff.append("<img src='");
                sBuff.append(objectIcon);
                sBuff.append("' border=0 />");
                sBuff.append(nameOrTitle);

                if(!isprinterFriendly)
                {
                    sBuff.append("</a>");
                }
                vName.add(sBuff.toString());
            }
           return vName;
        }
        catch(Exception e)
        {
           e.printStackTrace();
           throw e;
        }
    }

    /**
     * getLockStatus- This method is used to show the Lock image.
     *                This method is called from the Column Lock Image.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Object of type Vector
     * @throws Exception if the operation fails
     * @since V10 Patch1
     */
    public Vector getLockStatus(Context context, String[] args)
                                  throws Exception
    {
        Vector showLock= new Vector();
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            if(objectList.size() <= 0){
				return showLock;
			}
            Map objectMap = null;
            Map paramList = (Map)programMap.get("paramList");
            boolean isprinterFriendly = false;
            if(paramList.get("reportFormat") != null)
            {
               isprinterFriendly = true;
            }

            StringBuffer baseURLBuf = new StringBuffer(256);
            baseURLBuf.append("emxTable.jsp?program=emxCommonFileUI:getFiles&amp;table=APPFileSummary&amp;sortColumnName=Name&amp;");
            baseURLBuf.append("sortDirection=ascending&amp;popup=true&amp;header=emxComponents.Menu.Files&amp;subHeader=emxComponents.Menu.SubHeaderDocuments&amp;");
            baseURLBuf.append("HelpMarker=emxhelpcommondocuments&amp;suiteKey=Components&amp;FilterFramePage=../components/emxCommonDocumentCheckoutUtil.jsp&amp;FilterFrameSize=1");

            StringBuffer nonVersionableBaseURLBuf = new StringBuffer(256);
            nonVersionableBaseURLBuf.append("emxTable.jsp?program=emxCommonFileUI:getNonVersionableFiles&amp;table=APPNonVersionableFileSummary&amp;sortColumnName=Name&amp;");
            nonVersionableBaseURLBuf.append("popup=true&amp;sortDirection=ascending&amp;popup=true&amp;header=emxComponents.Menu.Files&amp;subHeader=emxComponents.Menu.SubHeaderDocuments&amp;");
            nonVersionableBaseURLBuf.append("HelpMarker=emxhelpcommondocuments&amp;suiteKey=Components&amp;FilterFramePage=../components/emxCommonDocumentCheckoutUtil.jsp&amp;FilterFrameSize=1");

            String statusImageString ="";
            StringList files = new StringList();
            StringList thumbnailFileList = new StringList();
            StringList locked = new StringList();
            String  objectType = "";
            boolean isOfICType = false;
            String parentType = "";
            String lock = "";
            int lockCount = 0;
            int fileCount = 0;
            String objectId = "";
            String file ="";
            StringBuffer urlBuf = new StringBuffer(256);
            boolean moveFilesToVersion = false;
            //Start for Bug 363529
            boolean vcDocumentType=false;
            //End for Bug 363529



			if(objectList != null && objectList.size() > 0)
            {
				objectMap = (Map)objectList.get(0);
		    }

		    Iterator mItr = null;
            boolean bActivateDSFA= FrameworkUtil.isSuiteRegistered(context,"ActivateDSFA",false,null,null);
		    /*boolean getfromMapList = false;
		    if ( objectMap != null
		    	&& objectMap.containsKey(CommonDocument.SELECT_TYPE)
		    	&& objectMap.containsKey(CommonDocument.SELECT_MOVE_FILES_TO_VERSION)
		    	&& objectMap.containsKey(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION)
		    	&& objectMap.containsKey(CommonDocument.SELECT_FILE_NAME)
		    	&& objectMap.containsKey(CommonDocument.SELECT_ACTIVE_FILE_LOCKED)
		    	&& objectMap.containsKey(CommonDocument.SELECT_LOCKED)
		    	&& objectMap.containsKey(CommonDocument.SELECT_TYPE_OF_IC_DOCUMENT)
		    	&& objectMap.containsKey(CommonDocument.SELECT_FILE_FORMAT)) {
				if (bActivateDSFA ){
					if ( objectMap.containsKey(CommonDocument.SELECT_IS_KIND_OF_VC_DOCUMENT)){
						getfromMapList = true;
					}
				} else {
					getfromMapList = true;
				}
			}


		    if (getfromMapList) {
				mItr = objectList.iterator();
			} else {*/
            String oidsArray[] = new String[objectList.size()];
            for (int i = 0; i < objectList.size(); i++)
            {
               try
               {
                   oidsArray[i] = (String)((HashMap)objectList.get(i)).get("id");
               } catch (Exception ex)
               {
                   oidsArray[i] = (String)((Hashtable)objectList.get(i)).get("id");
               }
            }

            StringList selects = new StringList(10);
            selects.add(CommonDocument.SELECT_TYPE);
            selects.add(CommonDocument.SELECT_ID);
            selects.add(CommonDocument.SELECT_MOVE_FILES_TO_VERSION);
            selects.add(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION);
            selects.add(CommonDocument.SELECT_FILE_NAME);
            selects.add(CommonDocument.SELECT_ACTIVE_FILE_LOCKED);
            selects.add(CommonDocument.SELECT_LOCKED);
            selects.add(CommonDocument.SELECT_TYPE_OF_IC_DOCUMENT);
            selects.add(CommonDocument.SELECT_FILE_FORMAT);
            //Start for Bug 363529

            if (bActivateDSFA ){
            selects.add(CommonDocument.SELECT_IS_KIND_OF_VC_DOCUMENT);
            }
            //End for Bug 363529


            MapList mlist = DomainObject.getInfo(context, oidsArray, selects);
				mItr = mlist.iterator();
			//}

			HashMap versionMap = new HashMap();

            while( mItr.hasNext() )
            {
                urlBuf = new StringBuffer(256);
                lockCount = 0;
                fileCount = 0;
                files = new StringList();
                thumbnailFileList = new StringList();
                locked = new StringList();
                statusImageString = "";
                objectMap = (Map)mItr.next();
                objectId = (String) objectMap.get(CommonDocument.SELECT_ID);
                objectType = (String) objectMap.get(CommonDocument.SELECT_TYPE);
                if(!versionMap.containsKey(objectType)){
					  versionMap.put(objectType, CommonDocument.checkVersionableType(context, objectType));
				}
                isOfICType = "TRUE".equalsIgnoreCase((String)objectMap.get(CommonDocument.SELECT_TYPE_OF_IC_DOCUMENT));
                moveFilesToVersion = (Boolean.valueOf((String) objectMap.get(CommonDocument.SELECT_MOVE_FILES_TO_VERSION))).booleanValue();
                thumbnailFileList = (StringList)objectMap.get(CommonDocument.SELECT_FILE_FORMAT);
                int thumnailsfilecount= 0;

                for(int j =0; j <thumbnailFileList.size();j++)
                {
                	String format = (String)thumbnailFileList.get(j);
                	if(DomainObject.FORMAT_MX_MEDIUM_IMAGE.equalsIgnoreCase(format)){
                		thumnailsfilecount++;
                	}

                }

                parentType = CommonDocument.getParentType(context, objectType);
                boolean isVersionableType = ((Boolean) versionMap.get(objectType)).booleanValue();
                //Start for Bug 363529
                if((String) objectMap.get(CommonDocument.SELECT_IS_KIND_OF_VC_DOCUMENT)!= null)
                {
                    vcDocumentType = true;
                }
                //End for Bug 363529

                //Start for Bug 363529
                if(!vcDocumentType){
                //End for Bug 363529
                if ( !isVersionableType || (parentType.equals(CommonDocument.TYPE_DOCUMENTS) && !isOfICType)){
                    if ( moveFilesToVersion )
                    {
                        try
                        {
                            files = (StringList)objectMap.get(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION);
                        }
                        catch(ClassCastException cex )
                        {
                            files.add((String)objectMap.get(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION));
                        }
                    } else {
                        try
                        {
                            files = (StringList)objectMap.get(CommonDocument.SELECT_FILE_NAME);
                        }
                        catch(ClassCastException cex )
                        {
                            files.add((String)objectMap.get(CommonDocument.SELECT_FILE_NAME));
                        }
                    }
                    if ( files != null )
                    {
                        fileCount = files.size();
                        fileCount = fileCount-thumnailsfilecount;
                        if ( fileCount == 1 )
                        {
                            file = (String)files.get(0);
                            if ( file == null || "".equals(file) || "null".equals(file) )
                            {
                                fileCount = 0;
                            }
                        }
                    }

                    try
                    {
                        locked = (StringList)objectMap.get(CommonDocument.SELECT_ACTIVE_FILE_LOCKED);
                    } catch(ClassCastException cex)
                    {
                        locked.add((String)objectMap.get(CommonDocument.SELECT_ACTIVE_FILE_LOCKED));
                    }
                    if ( locked != null )
                    {
                        Iterator itr = locked.iterator();
                        while (itr.hasNext())
                        {
                            lock = (String)itr.next();
                            if(lock.equalsIgnoreCase("true"))
                            {
                                lockCount ++;
                            }
                        }
                    }
                    if ( !isVersionableType )
                    {
                        lock = (String)objectMap.get(CommonDocument.SELECT_LOCKED);
                        if(lock.equalsIgnoreCase("true"))
                        {
                            lockCount = fileCount;
                        }
                    }

                    if(!isprinterFriendly)
                    {
                        urlBuf.append("<a href =\"javascript:showModalDialog('");
                        if ( !isVersionableType )
                        {
                            urlBuf.append(nonVersionableBaseURLBuf.toString());
                        } else {
                        urlBuf.append(baseURLBuf.toString());
                        }

                        urlBuf.append("&amp;objectId=");
                        urlBuf.append(XSSUtil.encodeForJavaScript(context, objectId));
                        urlBuf.append("',730,450)\">");
                    }
                    urlBuf.append(lockCount + "/" + fileCount);
                    if(!isprinterFriendly)
                    {
                        urlBuf.append("</a>");
                    }
                      if (CommonDocument.TYPE_DOCUMENTS.equals(parentType) )
                    showLock.add(urlBuf.toString());
                else
                      showLock.add("&#160;");
                } else {
                    showLock.add("&#160;");
                    }
                //Start for Bug 363529
                }else {
                    showLock.add("");
                    vcDocumentType = false;
                }
                //End for Bug 363529
            }
            return  showLock;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
     * getRouteStatus- This method is used to show the Lock image.
     *                This method is called from the Column Lock Image.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Object of type Vector
     * @throws Exception if the operation fails
     * @since V10 Patch1
     */
    public Vector getRouteStatus(Context context, String[] args)
                                  throws Exception
    {
        Vector showRoute = new Vector();
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            Map objectMap = null;
            String routeId = "";
            StringList routeIds = new StringList();
  			if(objectList != null && objectList.size() > 0)
            {
				objectMap = (Map)objectList.get(0);
            }

		    Iterator itr = null;
		    if ( objectMap != null && objectMap.containsKey(CommonDocument.SELECT_HAS_ROUTE)) {
				itr = objectList.iterator();
			} else {
            String oidsArray[] = new String[objectList.size()];
            for (int i = 0; i < objectList.size(); i++)
            {
               try
               {
                   oidsArray[i] = (String)((HashMap)objectList.get(i)).get("id");
               } catch (Exception ex)
               {
                   oidsArray[i] = (String)((Hashtable)objectList.get(i)).get("id");
               }
            }
            StringList selects = new StringList(1);
            selects.add(CommonDocument.SELECT_HAS_ROUTE);
            ContextUtil.pushContext(context); //Addded for IR-054419V6R2011x
            MapList mlist = DomainObject.getInfo(context, oidsArray, selects);
            ContextUtil.popContext(context); //Addded for IR-054419V6R2011x
				itr = mlist.iterator();
			}

            while( itr.hasNext() )
            {
                objectMap = (Map)itr.next();
                try
                {
                    routeId = (String) objectMap.get(CommonDocument.SELECT_HAS_ROUTE);
                } catch (ClassCastException cex)
                {
                    routeIds = (StringList) objectMap.get(CommonDocument.SELECT_HAS_ROUTE);
                    routeId = (String)routeIds.get(0);
                }
                if ( routeId != null)
                {
                    showRoute.add("<img border='0' src='../common/images/iconSmallRoute.gif'/>");
                } else {
                    showRoute.add("&#160;");
                }
            }
            return  showRoute;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
     * getRevisionStatus- This method is used to show the Lock image.
     *                This method is called from the Column Lock Image.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Object of type Vector
     * @throws Exception if the operation fails
     * @since V10 Patch1
     */
    public Vector getRevisionStatus(Context context, String[] args)
                                  throws Exception
    {
        Vector showRev = new Vector();
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            if(objectList.size() <= 0){
				return showRev;
			}
            Map objectMap = null;
            Map paramList = (Map)programMap.get("paramList");
            boolean isprinterFriendly = false;
            if(paramList.get("reportFormat") != null)
            {
               isprinterFriendly = true;
            }

            String objectType ="";
            String parentType ="";
            String objectId = "";

            StringBuffer baseURLBuf = new StringBuffer(250);
            baseURLBuf.append("emxTable.jsp?program=emxCommonDocumentUI:getRevisions&amp;popup=true&amp;table=APPDocumentRevisions&amp;header=emxComponents.Common.RevisionsPageHeading&amp;HelpMarker=emxhelpdocumentfilerevisions&amp;subHeader=emxComponents.Menu.SubHeaderDocuments&amp;suiteKey=Components");
            String revHref = "";
            StringBuffer urlBuf = new StringBuffer(250);

			boolean isOfICType = false;


			if(objectList != null && objectList.size() > 0)
            {
				objectMap = (Map)objectList.get(0);
		    }

		    Iterator itr = null;
		    if ( objectMap != null
		    	&& objectMap.containsKey(CommonDocument.SELECT_TYPE_OF_IC_DOCUMENT)
		    	&& objectMap.containsKey(CommonDocument.SELECT_REVISION)
		    	&& objectMap.containsKey(CommonDocument.SELECT_TYPE)) {
					itr = objectList.iterator();
			} else {

            String oidsArray[] = new String[objectList.size()];
            for (int i = 0; i < objectList.size(); i++)
            {
               try
               {
                   oidsArray[i] = (String)((HashMap)objectList.get(i)).get("id");
               } catch (Exception ex)
               {
                   oidsArray[i] = (String)((Hashtable)objectList.get(i)).get("id");
               }
            }

            StringList selects = new StringList(3);
            selects.add(CommonDocument.SELECT_TYPE);
            selects.add(CommonDocument.SELECT_ID);
            selects.add(CommonDocument.SELECT_REVISION);
            selects.add(CommonDocument.SELECT_TYPE_OF_IC_DOCUMENT);


            MapList mlist = DomainObject.getInfo(context, oidsArray, selects);
				itr = mlist.iterator();
			}

            while( itr.hasNext() )
            {
                urlBuf = new StringBuffer(250);
                revHref = "";
                objectMap = (Map) itr.next();
                objectType = (String) objectMap.get(CommonDocument.SELECT_TYPE);
                objectId = (String) objectMap.get(CommonDocument.SELECT_ID);
                parentType = CommonDocument.getParentType(context, objectType);
                isOfICType = "TRUE".equalsIgnoreCase((String)objectMap.get(CommonDocument.SELECT_TYPE_OF_IC_DOCUMENT));


                    if ( parentType.equals(CommonDocument.TYPE_DOCUMENTS) && !isOfICType){
                        revHref = XSSUtil.encodeForHTML(context, (String)objectMap.get(CommonDocument.SELECT_REVISION));
                        if(!isprinterFriendly)
                        {
                            urlBuf.append("<a ");
                            urlBuf.append(" href =\"javascript:showModalDialog('");
                            urlBuf.append(baseURLBuf.toString());
                            urlBuf.append("&amp;objectId=");
                            urlBuf.append(XSSUtil.encodeForJavaScript(context, objectId));
                            urlBuf.append("',730,450)\">");
                        }
                        urlBuf.append(revHref);
                        if(!isprinterFriendly)
                        {
                            urlBuf.append("</a>");
                        }
                    revHref = urlBuf.toString();
                    } else {
                    revHref = XSSUtil.encodeForHTML(context, (String)objectMap.get(CommonDocument.SELECT_REVISION));
                    }

                showRev.add(revHref);
            }
            //XSSOK
            return  showRev;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
    }
    /**
     * getVersionStatus- This method is used to show the Lock image.
     *                This method is called from the Column Lock Image.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Object of type Vector
     * @throws Exception if the operation fails
     * @since V10 Patch1
     */
    public Vector getVersionStatus(Context context, String[] args)
                                  throws Exception
    {
        Vector showVer = new Vector();
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            if(objectList.size() <= 0){
				return showVer;
			}
            Map objectMap = null;
            Map paramList = (Map)programMap.get("paramList");
            boolean isprinterFriendly = false;
            //Start for Bug 363529
            boolean vcDocumentType=false;
            //End for Bug 363529
            //Added for IR-061343V6R2011x
            boolean isCSVExport = false;
            if(paramList.get("reportFormat") != null && "CSV".equalsIgnoreCase((String)paramList.get("reportFormat") ))
            {
              isCSVExport = true;
            }

            if(paramList.get("reportFormat") != null)
            {
               isprinterFriendly = true;
            }

            String languageStr = (String)paramList.get("languageStr");
            Locale strLocale = context.getLocale();
            String sTipFileVersion = EnoviaResourceBundle.getProperty(context,  "emxComponentsStringResource", strLocale,"emxComponents.Common.Alt.FileVersions");
            String objectType ="";
            String parentType ="";
            StringList versions = new StringList();
            String version = "";
            String objectId = "";
            String vcFileType="";
            String vcFolderType="";
            String vcModuleType="";
            StringBuffer baseURLBuf = new StringBuffer(250);
            baseURLBuf.append("emxTable.jsp?program=emxCommonFileUI:getFileVersions&amp;popup=true&amp;table=APPFileVersions&amp;header=emxComponents.Common.DocumentVersionsPageHeading&amp;subHeader=emxComponents.Menu.SubHeaderDocuments&amp;HelpMarker=emxhelpdocumentfileversions&amp;suiteKey=Components&amp;FilterFramePage=../components/emxCommonDocumentCheckoutUtil.jsp&amp;FilterFrameSize=1");
            StringBuffer urlBuf = new StringBuffer(250);

			if(objectList != null && objectList.size() > 0)
            {
				objectMap = (Map)objectList.get(0);
		    }

		    Iterator itr = null;
            boolean bActivateDSFA= FrameworkUtil.isSuiteRegistered(context,"ActivateDSFA",false,null,null);
		    /*boolean getFromMapList = false;
			if ( objectMap != null
				&& objectMap.containsKey(CommonDocument.SELECT_TYPE)
				&& objectMap.containsKey(CommonDocument.SELECT_ACTIVE_FILE_VERSION)) {
					if (bActivateDSFA ){
						if (objectMap.containsKey(CommonDocument.SELECT_VCFILE_EXISTS)
							&& objectMap.containsKey(CommonDocument.SELECT_VCFOLDER_EXISTS)
							&& objectMap.containsKey(CommonDocument.SELECT_VCMODULE_EXISTS)) {
								getFromMapList = true;
						}
					} else {
							getFromMapList = true;
					}
			}

		    if ( getFromMapList) {
				itr = objectList.iterator();
			} else {*/
            String oidsArray[] = new String[objectList.size()];
            for (int i = 0; i < objectList.size(); i++)
            {
               try
               {
                   oidsArray[i] = (String)((HashMap)objectList.get(i)).get("id");
               } catch (Exception ex)
               {
                   oidsArray[i] = (String)((Hashtable)objectList.get(i)).get("id");
               }
            }
            StringList selects = new StringList(6);
            selects.add(CommonDocument.SELECT_TYPE);
            selects.add(CommonDocument.SELECT_ID);
            selects.add(CommonDocument.SELECT_ACTIVE_FILE_VERSION);
            //Start for Bug 363529

            if (bActivateDSFA ){
            selects.add(CommonDocument.SELECT_VCFILE_EXISTS);
            selects.add(CommonDocument.SELECT_VCFOLDER_EXISTS);
            selects.add(CommonDocument.SELECT_VCMODULE_EXISTS);
            }
            //End for Bug 363529

            MapList mlist = DomainObject.getInfo(context, oidsArray, selects);
				itr = mlist.iterator();
			//}

            while( itr.hasNext() )
            {
                urlBuf = new StringBuffer(250);
                objectMap = (Map) itr.next();
                objectType = (String) objectMap.get(CommonDocument.SELECT_TYPE);
                objectId = (String) objectMap.get(CommonDocument.SELECT_ID);
                parentType = CommonDocument.getParentType(context, objectType);
                //Start for Bug 363529
                vcFileType=(String) objectMap.get(CommonDocument.SELECT_VCFILE_EXISTS);
                vcFolderType=(String) objectMap.get(CommonDocument.SELECT_VCFOLDER_EXISTS);
                vcModuleType=(String) objectMap.get(CommonDocument.SELECT_VCMODULE_EXISTS);
                if((vcFileType!=null && vcFileType.equalsIgnoreCase("true")) ||
                   (vcFolderType!=null && vcFolderType.equalsIgnoreCase("true")) ||
                   (vcModuleType!=null && vcModuleType.equalsIgnoreCase("true"))){
                    vcDocumentType = true;
                }

                if(!vcDocumentType){
                //End for Bug 363529
                if ( parentType.equals(CommonDocument.TYPE_DOCUMENTS) ){
                    versions = (StringList)objectMap.get(CommonDocument.SELECT_ACTIVE_FILE_VERSION);
                    if ( versions == null || versions.size() == 0)
                    {
                        version = "";
                    } else if ( versions.size() == 1 ) {
                        version = (String)versions.get(0);
                    } else {
                //     Added for IR-061343V6R2011x
                        if(isCSVExport)
                            version = sTipFileVersion;
                        else
                            version = "<img border='0' src='../common/images/iconSmallFiles.gif' alt=\"" + sTipFileVersion +"\" title=\"" + sTipFileVersion +"\"></img>";
                    }
                    if(!isprinterFriendly && !"".equals(version) )
                    {
                        urlBuf.append("<a href =\"javascript:showModalDialog('");
                        urlBuf.append(baseURLBuf.toString());
                        urlBuf.append("&amp;objectId=");
                        urlBuf.append(XSSUtil.encodeForJavaScript(context, objectId));
                        urlBuf.append("',730,450)\">");
                    }
                    urlBuf.append(version);
                    if(!isprinterFriendly && !"".equals(version))
                    {
                        urlBuf.append("</a>");
                    }
                    if (CommonDocument.TYPE_DOCUMENTS.equals(parentType) )
                      showVer.add(urlBuf.toString());
                else
                      showVer.add("");
                } else {
                    showVer.add("");
                }
                // Start for Bug 363529
              } else{

                    if(vcFileType!=null && vcFileType.equalsIgnoreCase("true")){
                        showVer.add(EnoviaResourceBundle.getProperty(context,  "emxComponentsStringResource", strLocale,"emxComponents.VCDocument.DesignSync")+
                                " "+EnoviaResourceBundle.getProperty(context,  "emxComponentsStringResource", strLocale,"emxComponents.CommonDocument.File"));
                        vcDocumentType = false;
                    }

                    if(vcFolderType!=null && vcFolderType.equalsIgnoreCase("true")){
                        showVer.add(EnoviaResourceBundle.getProperty(context,  "emxComponentsStringResource", strLocale,"emxComponents.VCDocument.DesignSync")+
                                " "+EnoviaResourceBundle.getProperty(context,  "emxComponentsStringResource", strLocale,"emxComponents.CommonDocument.Folder"));
                        vcDocumentType = false;
                    }

                    if(vcModuleType!=null && vcModuleType.equalsIgnoreCase("true")){
                        showVer.add(EnoviaResourceBundle.getProperty(context,  "emxComponentsStringResource", strLocale,"emxComponents.VCDocument.DesignSync")+
                                " "+EnoviaResourceBundle.getProperty(context,  "emxComponentsStringResource", strLocale,"emxComponents.CommonDocument.Module"));
                        vcDocumentType = false;
                    }
                }
                //End for Bug 363529
            }
            return  showVer;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
    }

   /**
    *  Get Vector of Strings for Document Action Icons
    *
    *  @param context the eMatrix <code>Context</code> object
    *  @param args an array of String arguments for this method
    *  @return Vector object that contains a vector of html code to
    *        construct the Actions Column.
    *  @throws Exception if the operation fails
    *
    *  @since Common 10.5
    *  @grade 0
    */
    public static Vector getDocumentActions(Context context, String[] args)
        throws Exception
    {
        Vector vActions = new Vector();
        try
        {
			// Start MSF
			String msfRequestData = "";
			String msfFileFormatDetails = "";
			boolean isAdded = false;
			// End MSF
            HashMap programMap = (HashMap) JPO.unpackArgs(args);

            MapList objectList = (MapList)programMap.get("objectList");
            if(objectList.size() <= 0){
				return vActions;
			}
            Map paramList      = (Map)programMap.get("paramList");
            String uiType = (String)paramList.get("uiType");
            String parentOID = (String)paramList.get("parentOID");
            String customSortColumns = (String)paramList.get("customSortColumns");
            String customSortDirections = (String)paramList.get("customSortDirections");
            String table = (String)paramList.get("table");
            if(objectList == null || objectList.size() <= 0)
            {
               return vActions;
            }

            boolean isprinterFriendly = false;
            if(paramList.get("reportFormat") != null)
            {
            isprinterFriendly = true;
            }

            String languageStr = (String)context.getSession().getLanguage();
            Locale strLocale = context.getLocale();
            String sTipDownload = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", strLocale,"emxComponents.DocumentSummary.ToolTipDownload");
            String sTipCheckout = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", strLocale,"emxComponents.DocumentSummary.ToolTipCheckout");
            String sTipCheckin  = EnoviaResourceBundle.getProperty(context,  "emxComponentsStringResource", strLocale,"emxComponents.DocumentSummary.ToolTipCheckin");
            String sTipUpdate   = EnoviaResourceBundle.getProperty(context,   "emxComponentsStringResource", strLocale,"emxComponents.DocumentSummary.ToolTipUpdate");
            String sTipSubscriptions   = EnoviaResourceBundle.getProperty(context,   "emxComponentsStringResource", strLocale,"emxComponents.Command.Subscriptions");

			Map objectMap = null;
			if(objectList != null && objectList.size() > 0)
            {
				objectMap = (Map)objectList.get(0);
		    }

		    Iterator objectListItr = null;
		    if ( objectMap != null
		    	&& objectMap.containsKey(CommonDocument.SELECT_TYPE)
		    	&& objectMap.containsKey(CommonDocument.SELECT_SUSPEND_VERSIONING)
		    	&& objectMap.containsKey(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS)
		    	&& objectMap.containsKey(CommonDocument.SELECT_HAS_CHECKIN_ACCESS)
		    	&& objectMap.containsKey(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION)
		    	&& objectMap.containsKey(CommonDocument.SELECT_FILE_NAME)
		    	&& objectMap.containsKey(CommonDocument.SELECT_MOVE_FILES_TO_VERSION)
		    	&& objectMap.containsKey(CommonDocument.SELECT_IS_KIND_OF_VC_DOCUMENT)
		    	&& objectMap.containsKey("vcfile")
		    	&& objectMap.containsKey("vcmodule")
		    	&& objectMap.containsKey(CommonDocument.SELECT_ACTIVE_FILE_LOCKED)
		    	&& objectMap.containsKey(CommonDocument.SELECT_ACTIVE_FILE_LOCKER)
		    	&& objectMap.containsKey(CommonDocument.SELECT_HAS_TOCONNECT_ACCESS)
		    	&& objectMap.containsKey(CommonDocument.SELECT_ACTIVE_FILE_VERSION_ID)
		    	&& objectMap.containsKey(CommonDocument.SELECT_OWNER)
		    	&& objectMap.containsKey(CommonDocument.SELECT_LOCKED)
		    	&& objectMap.containsKey(CommonDocument.SELECT_LOCKER)) {

					objectListItr = objectList.iterator();
			} else {
            StringList selectTypeStmts = new StringList(1);
            selectTypeStmts.add(DomainConstants.SELECT_ID);
            selectTypeStmts.add(DomainConstants.SELECT_TYPE);
            selectTypeStmts.add(CommonDocument.SELECT_SUSPEND_VERSIONING);
            selectTypeStmts.add(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS);
            selectTypeStmts.add(CommonDocument.SELECT_HAS_CHECKIN_ACCESS);
            selectTypeStmts.add(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION);
            selectTypeStmts.add(CommonDocument.SELECT_FILE_NAME);
            selectTypeStmts.add(CommonDocument.SELECT_MOVE_FILES_TO_VERSION);
            selectTypeStmts.add(CommonDocument.SELECT_IS_KIND_OF_VC_DOCUMENT);
            selectTypeStmts.add("vcfile");
            selectTypeStmts.add("vcmodule");
            selectTypeStmts.add(CommonDocument.SELECT_ACTIVE_FILE_LOCKED);
            selectTypeStmts.add(CommonDocument.SELECT_ACTIVE_FILE_LOCKER);
            selectTypeStmts.add(CommonDocument.SELECT_HAS_TOCONNECT_ACCESS);
            selectTypeStmts.add(CommonDocument.SELECT_ACTIVE_FILE_VERSION_ID);
            selectTypeStmts.add(CommonDocument.SELECT_OWNER);
            selectTypeStmts.add(CommonDocument.SELECT_LOCKED);
            selectTypeStmts.add(CommonDocument.SELECT_LOCKER);

            //Getting all the content ids
            String oidsArray[] = new String[objectList.size()];
            for (int i = 0; i < objectList.size(); i++)
            {
               try
               {
                   oidsArray[i] = (String)((HashMap)objectList.get(i)).get("id");
               } catch (Exception ex)
               {
                   oidsArray[i] = (String)((Hashtable)objectList.get(i)).get("id");
               }
            }

            MapList objList = DomainObject.getInfo(context, oidsArray, selectTypeStmts);

				objectListItr = objList.iterator();
			}


		    HashMap versionMap = new HashMap();
		    String linkAttrName = PropertyUtil.getSchemaProperty(context,"attribute_MxCCIsObjectLinked");
            while(objectListItr.hasNext())
            {
                // Start MSF
                msfFileFormatDetails = "MSFFileFormatDetails:[";
                // End MSF
                Map contentObjectMap = (Map)objectListItr.next();
                int fileCount = 0;
                String vcInterface = "";
                boolean vcDocument = false;
                boolean vcFile = false;
                String docType = "";
                StringBuffer strBuf = new StringBuffer(1256);
                boolean moveFilesToVersion = (Boolean.valueOf((String) contentObjectMap.get(CommonDocument.SELECT_MOVE_FILES_TO_VERSION))).booleanValue();
                String documentId = (String)contentObjectMap.get(DomainConstants.SELECT_ID);

                DomainObject docObject = DomainObject.newInstance(context,documentId);

                //For getting the count of files
                HashMap filemap = new HashMap();
                filemap.put(CommonDocument.SELECT_MOVE_FILES_TO_VERSION, contentObjectMap.get(CommonDocument.SELECT_MOVE_FILES_TO_VERSION));
                // Start MSF
                StringList activeVersionFileList = (StringList) contentObjectMap.get(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION);
                if (null == activeVersionFileList) {
                    activeVersionFileList = new StringList();
                }
                filemap.put(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION, activeVersionFileList);
                // End MSF
                filemap.put(CommonDocument.SELECT_FILE_NAME, contentObjectMap.get(CommonDocument.SELECT_FILE_NAME));
                // Start MSF
                StringList activeVersionIDList = (StringList) contentObjectMap.get(CommonDocument.SELECT_ACTIVE_FILE_VERSION_ID);
                if (null == activeVersionIDList) {
                    activeVersionIDList = new StringList();
                }
                // End MSF
                fileCount = CommonDocument.getFileCount(context,filemap);

                vcInterface = (String)contentObjectMap.get(CommonDocument.SELECT_IS_KIND_OF_VC_DOCUMENT);
                vcDocument = "TRUE".equalsIgnoreCase(vcInterface)?true:false;

                docType    = (String)contentObjectMap.get(DomainConstants.SELECT_TYPE);

                if(!versionMap.containsKey(docType)){
                	versionMap.put(docType, CommonDocument.checkVersionableType(context, docType));
                }

                String parentType = CommonDocument.getParentType(context, docType);
                if (CommonDocument.TYPE_DOCUMENTS.equals(parentType))
                {
                    // show subscription link
                    if (FrameworkLicenseUtil.isCPFUser(context))
                    {
                        if(!isprinterFriendly)
                        {
                            strBuf.append("<a href=\"javascript:showModalDialog('../components/emxSubscriptionDialog.jsp?objectId=");
                            strBuf.append(XSSUtil.encodeForJavaScript(context, documentId));
                            strBuf.append("', '730', '450')\"><img border='0' src='../common/images/iconSmallSubscription.gif' alt=\""+sTipSubscriptions+"\" title=\""+sTipSubscriptions+"\"></img></a>&#160;");
                        } else {
                            strBuf.append("<img border='0' src='../common/images/iconSmallSubscription.gif' alt=\""+sTipSubscriptions+"\" title=\""+sTipSubscriptions+"\"></img>&#160;");
                        }
                    }
                    //Can Download
                    if(CommonDocument.canDownload(context, contentObjectMap))
                    {
                        if (!isprinterFriendly)
                        {
                            strBuf.append("<a href='javascript:callCheckout(\"");
                            strBuf.append(XSSUtil.encodeForJavaScript(context, documentId));
                            strBuf.append("\",\"download\", \"\", \"\",\"");
                            strBuf.append(XSSUtil.encodeForJavaScript(context, customSortColumns));
                            strBuf.append("\", \"");
                            strBuf.append(XSSUtil.encodeForJavaScript(context, customSortDirections));
                            strBuf.append("\", \"");
                            strBuf.append(XSSUtil.encodeForJavaScript(context, uiType));
                            strBuf.append("\", \"");
                            strBuf.append(XSSUtil.encodeForJavaScript(context, table));
                            strBuf.append("\", \"");
                            strBuf.append(XSSUtil.encodeForJavaScript(context, parentOID));
                            strBuf.append("\"");
                            strBuf.append(")'>");
                            strBuf.append("<img border='0' src='../common/images/iconActionDownload.gif' alt=\"");
                            strBuf.append(sTipDownload);
                            strBuf.append("\" title=\"");
                            strBuf.append(sTipDownload);
                            strBuf.append("\"></img></a>&#160;");
                        } else {
                            strBuf.append("<img border='0' src='../common/images/iconActionDownload.gif' alt=\"");
                            strBuf.append(sTipDownload);
                            strBuf.append("\"></img>&#160;");
                        }
                        // Changes for CLC start here..
                        //Show Download Icon for ClearCase Linked Objects
                        //DomainObject ccLinkedObject  = DomainObject.newInstance(context, documentId);

                        String isObjLinked = null;
                        if(linkAttrName!=null && !linkAttrName.equals(""))
                        {
                            isObjLinked = docObject.getAttributeValue(context,linkAttrName);
                        }

                        if(isObjLinked!=null && !isObjLinked.equals(""))
                        {
                            if(isObjLinked.equalsIgnoreCase("True"))
                            {
                                //show download icon for Linked Objects
                                strBuf.append("<a href='../servlet/MxCCCS/MxCCCommandsServlet.java?commandName=downloadallfiles&amp;objectId=");
                                strBuf.append(XSSUtil.encodeForURL(context, documentId));
                                strBuf.append("'>");
                                strBuf.append("<img border='0' src='../common/images/iconActionDownload.gif' alt=\"");
                                strBuf.append(sTipDownload);
                                strBuf.append("\" title=\"");
                                strBuf.append(sTipDownload);
                                strBuf.append("\"></img></a>&#160;");
                            }
                        }
                    }
                    // Can Checkout
                    if(CommonDocument.canCheckout(context, contentObjectMap,false, ((Boolean) versionMap.get(docType)).booleanValue()))
                    {
                        if(!isprinterFriendly)
                        {

                            strBuf.append("<a href='javascript:callCheckout(\"");
                            strBuf.append(XSSUtil.encodeForJavaScript(context, documentId));
                            strBuf.append("\",\"checkout\", \"\", \"\",\"");
                            strBuf.append(XSSUtil.encodeForJavaScript(context, customSortColumns));
                            strBuf.append("\", \"");
                            strBuf.append(XSSUtil.encodeForJavaScript(context, customSortDirections));
                            strBuf.append("\", \"");
                            strBuf.append(XSSUtil.encodeForJavaScript(context, uiType));
                            strBuf.append("\", \"");
                            strBuf.append(XSSUtil.encodeForJavaScript(context, table));
                            strBuf.append("\", \"");
                            strBuf.append(parentOID);
                            strBuf.append("\"");
                            strBuf.append(")'>");
                            strBuf.append("<img border='0' src='../common/images/iconActionCheckOut.gif' alt=\"");
                            strBuf.append(sTipCheckout);
                            strBuf.append("\" title=\"");
                            strBuf.append(sTipCheckout);
                            strBuf.append("\"></img></a>&#160;");
                        } else {
                            strBuf.append("<img border='0' src='../common/images/iconActionCheckOut.gif' alt=\"");
                            strBuf.append(sTipCheckout);
                            strBuf.append("\"></img>&#160;");
                       }
                    } else {
                        strBuf.append("&#160;");
                    }
                    // Can Checkin
                    if((CommonDocument.canCheckin(context, contentObjectMap) || VCDocument.canVCCheckin(context, contentObjectMap)))
                    {
                        // Start MSF
                        isAdded = false;
                        for(int ii =0; ii< activeVersionFileList.size(); ii++){
                            if(!isAdded) {
                                isAdded = true;
                                msfFileFormatDetails += "{FileName: '" + XSSUtil.encodeForJavaScript(context, (String)activeVersionFileList.get(ii)) + 
                                "', VersionId: '" + XSSUtil.encodeForJavaScript(context, (String)activeVersionIDList.get(ii)) + "'}";
                            }
                            else {
                                msfFileFormatDetails += ", {FileName: '" + XSSUtil.encodeForJavaScript(context, (String)activeVersionFileList.get(ii)) + 
                                "', VersionId: '" + XSSUtil.encodeForJavaScript(context, (String)activeVersionIDList.get(ii)) + "'}";
                            }
                        }
                        msfFileFormatDetails += "]";
                        // End MSF

                        // MSF
                        msfRequestData = "{RequestType: 'CheckIn', DocumentID: '" + documentId + "', " + msfFileFormatDetails + "}";
                        // MSF
                        vcFile =(Boolean.valueOf((String) contentObjectMap.get("vcfile"))).booleanValue();
                        if(!isprinterFriendly)
                        {
                            if( !vcDocument )
                            {
                                strBuf.append("<a href=\"javascript:processModalDialog(" + msfRequestData + "," + "'../components/emxCommonDocumentPreCheckin.jsp?objectId=");
                                strBuf.append(XSSUtil.encodeForJavaScript(context, documentId));
                                strBuf.append("&amp;customSortColumns="); //Added for Bug #371651 starts
                                strBuf.append(XSSUtil.encodeForJavaScript(context, customSortColumns));
                                strBuf.append("&amp;customSortDirections=");
                                strBuf.append(XSSUtil.encodeForJavaScript(context, customSortDirections));
                                strBuf.append("&amp;uiType=");
                                strBuf.append(XSSUtil.encodeForJavaScript(context, uiType));
                                strBuf.append("&amp;table=");
                                strBuf.append(XSSUtil.encodeForJavaScript(context, table));                 //Added for Bug #371651 ends
                                strBuf.append("&amp;showFormat=true&amp;showComments=required&amp;objectAction=update&amp;JPOName=emxTeamDocumentBase&amp;appDir=teamcentral&amp;appProcessPage=emxTeamPostCheckinProcess.jsp&amp;refreshTableContent=true',730,450);\">");
                                strBuf.append("<img border='0' src='../common/images/iconActionCheckIn.gif' alt=\"");
                                strBuf.append(sTipCheckin);
                                strBuf.append("\" title=\"");
                                strBuf.append(sTipCheckin);
                                strBuf.append("\"></img></a>&#160;");
                            } else {
                                if(vcFile)
                                {
                                    strBuf.append("<a href=\"javascript:processModalDialog(" + msfRequestData + "," + "'../components/emxCommonDocumentPreCheckin.jsp?objectId=");
                                    strBuf.append(XSSUtil.encodeForJavaScript(context, documentId));
                                    strBuf.append("&amp;customSortColumns=");     //Added for Bug #371651 starts
                                    strBuf.append(XSSUtil.encodeForJavaScript(context, customSortColumns));
                                    strBuf.append("&amp;customSortDirections=");
                                    strBuf.append(XSSUtil.encodeForJavaScript(context, customSortDirections));
                                    strBuf.append("&amp;uiType=");
                                    strBuf.append(XSSUtil.encodeForJavaScript(context, uiType));
                                    strBuf.append("&amp;table=");
                                    strBuf.append(XSSUtil.encodeForJavaScript(context, table));                 //Added for Bug #371651 ends
                                    strBuf.append("&amp;showFormat=false&amp;showComments=required&amp;objectAction=checkinVCFile&amp;allowFileNameChange=false&amp;noOfFiles=1&amp;JPOName=emxVCDocument&amp;methodName=checkinUpdate&amp;refreshTableContent=true', '730', '450');\">");
                                    strBuf.append("<img border='0' src='../common/images/iconActionCheckIn.gif' alt=\"");
                                    strBuf.append(sTipCheckin);
                                    strBuf.append("\" title=\"");
                                    strBuf.append(sTipCheckin);
                                    strBuf.append("\"></img></a>&#160;");
                                } else {
                                    strBuf.append("<a href=\"javascript:processModalDialog(" + msfRequestData + "," + "'../components/emxCommonDocumentPreCheckin.jsp?objectId=");
                                    strBuf.append(XSSUtil.encodeForJavaScript(context, documentId));
                                    strBuf.append("&amp;customSortColumns=");         //Added for Bug #371651 starts
                                    strBuf.append(XSSUtil.encodeForJavaScript(context, customSortColumns));
                                    strBuf.append("&amp;customSortDirections=");
                                    strBuf.append(XSSUtil.encodeForJavaScript(context, customSortDirections));
                                    strBuf.append("&amp;uiType=");
                                    strBuf.append(XSSUtil.encodeForJavaScript(context, uiType));
                                    strBuf.append("&amp;table=");
                                    strBuf.append(XSSUtil.encodeForJavaScript(context, table));                 //Added for Bug #371651 ends
                                    strBuf.append("&amp;override=false&amp;showFormat=false&amp;showComments=required&amp;objectAction=checkinVCFile&amp;allowFileNameChange=true&amp;noOfFiles=1&amp;JPOName=emxVCDocument&amp;methodName=checkinUpdate&amp;refreshTableContent=true', '730', '450');\">");
                                    strBuf.append("<img border='0' src='../common/images/iconActionCheckIn.gif' alt=\"");
                                    strBuf.append(sTipCheckin);
                                    strBuf.append("\" title=\"");
                                    strBuf.append(sTipCheckin);
                                    strBuf.append("\"></img></a>&#160;");
                                }
                            }
                        } else {
                            strBuf.append("<img border='0' src='../common/images/iconActionCheckIn.gif' alt=\"");
                            strBuf.append(sTipCheckin);
                            strBuf.append("\" title=\"");
                            strBuf.append(sTipCheckin);
                            strBuf.append("\"></img>&#160;");
                        }
                    }
                    // Can Add Files
                    if(CommonDocument.canAddFiles(context, contentObjectMap))
                    {
                        // MSF
                        msfRequestData = "{RequestType: 'AddFiles', DocumentID: '" + documentId + "'}";
                        // MSF
                        if(!isprinterFriendly)
                        {
                            strBuf.append("<a href=\"javascript:processModalDialog(" + msfRequestData + "," + "'../components/emxCommonDocumentPreCheckin.jsp?objectId=");
                            strBuf.append(XSSUtil.encodeForJavaScript(context, documentId));
                            strBuf.append("&amp;customSortColumns=");       //Added for Bug #371651 starts
                            strBuf.append(XSSUtil.encodeForJavaScript(context, customSortColumns));
                            strBuf.append("&amp;customSortDirections=");
                            strBuf.append(XSSUtil.encodeForJavaScript(context, customSortDirections));
                            strBuf.append("&amp;uiType=");
                            strBuf.append(XSSUtil.encodeForJavaScript(context, uiType));
                            strBuf.append("&amp;table=");
                            strBuf.append(XSSUtil.encodeForJavaScript(context, table));                   //Added for Bug #371651 ends
                            strBuf.append("&amp;showFormat=true&amp;showDescription=required&amp;objectAction=checkin&amp;showTitle=true&amp;JPOName=emxTeamDocumentBase&amp;appDir=teamcentral&amp;appProcessPage=emxTeamPostCheckinProcess.jsp&amp;refreshTableContent=true', '730', '450')\">");
                            strBuf.append("<img border='0' src='../common/images/iconActionAppend.gif' alt=\"");
                            strBuf.append(sTipUpdate);
                            strBuf.append("\" title =\"");
                            strBuf.append(sTipUpdate);
                            strBuf.append("\"></img></a>&#160;");
                        } else {
                            strBuf.append("<img border='0' src='../common/images/iconActionAppend.gif' alt=\"");
                            strBuf.append(sTipUpdate);
                            strBuf.append("\" title=\"");
                            strBuf.append(sTipUpdate);
                            strBuf.append("\"></img>&#160;");
                        }
                    }
                    if (strBuf.length() == 0)
                        strBuf.append("&#160;");
                } else {
                    strBuf.append("&#160;");
                }
                vActions.add(strBuf.toString());
            }
        } catch(Exception ex){
            ex.printStackTrace();
            throw ex;
        }
        finally
        {
            return vActions;
        }
    }

   /**
    *  Get Vector of Strings for Document Revision Action Icons
    *  @param context the eMatrix <code>Context</code> object
    *  @param args an array of String arguments for this method
    *  @return Vector object that contains a vector of html code to
    *        construct the Actions Column.
    *  @throws Exception if the operation fails
    *
    *  @since Common 10.5
    *  @grade 0
    */
    public static Vector getDocumentRevisionActions(Context context, String[] args)
        throws Exception
    {

        Vector vActions = new Vector();
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            Map paramList      = (Map)programMap.get("paramList");

            boolean isprinterFriendly = false;
            if(paramList.get("reportFormat") != null)
            {
                isprinterFriendly = true;
            }

            String languageStr = (String)paramList.get("languageStr");

            StringBuffer strActionURL = null;

            String objectId    = null;
            Map objectMap      = null;

            String sTipDownload = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(),"emxComponents.DocumentSummary.ToolTipDownload");

            for(int i=0; i< objectList.size(); i++)
            {
                objectMap      = (Map) objectList.get(i);
                objectId       = (String)objectMap.get(CommonDocument.SELECT_ID);

                StringBuffer strBuf = new StringBuffer();

                // Show download, checkout for all type of files.
                if(!UINavigatorUtil.isMobile(context)){
					if(!isprinterFriendly) {
						strBuf.append("<a href='javascript:callCheckout(\"");
						strBuf.append(XSSUtil.encodeForJavaScript(context, objectId));
						strBuf.append("\",\"download\", \"\", \"\")'>");
						strBuf.append("<img border='0' src='../common/images/iconActionDownload.gif' alt=\""+sTipDownload+"\" title=\""+sTipDownload+"\"></img></a>&#160;");
					} else {
						strBuf.append("<img border='0' src='../common/images/iconActionDownload.gif' alt=\""+sTipDownload+"\" title=\""+sTipDownload+"\">&#160;");
	                }
                }
                vActions.add(strBuf.toString());
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
        finally
        {
            return vActions;
        }
    }

    public static String getParentType(Context context, String type) throws Exception
    {
        return CommonDocument.getParentType(context, type);
    }

    public String getUploadFilesUIBlock(matrix.db.Context context, String[] args) throws Exception {
    	Locale strLocale = context.getLocale();
    	String dropFilesHere = EnoviaResourceBundle.getProperty(context,  "emxComponentsStringResource", strLocale,"emxComponents.CommonDocument.UploadFiles.DropFiles"); // "Drop files here or click to select"
    	String addComment = EnoviaResourceBundle.getProperty(context,  "emxComponentsStringResource", strLocale,"emxComponents.CommonDocument.UploadFiles.AddComment");
    	StringBuffer strBuf = new StringBuffer();
    	strBuf.append("<script>var MSG_ADD_COMMENT = \'" + addComment + "\'; emxCreateRemoveExtraRowAboveType(); </script>"); // "Click to add comment"
    	strBuf.append("<div class=\"dropArea\" style=\"position:relative\" onmouseout=\"mouseoutDropArea(this)\" onmouseover=\"mouseoverDropArea(this)\" ondragover=\"filesDragOver(this)\" ondragleave=\"filesDragLeave(this)\" ondrop=\"filesDrop(this, event)\" >");
    	strBuf.append("<img id=\"dropAreaIcon\" src=\"images/iconActionAdd.png\"/>");
    	strBuf.append(" " + dropFilesHere);
    	strBuf.append("<input id=\"hiddenFileInputElement\" type=\"file\" style=\"opacity:0;position:absolute;width:100%;height:100%;left:0px;top:0px\" onchange=\"fileInputChanged(this, event)\" multiple=\"true\"></input>");
    	strBuf.append("</div>");
        String strHTML = strBuf.toString();
        return strHTML;
    }

    public HashMap getPolicies ( Context context , String[] args ) throws Exception {
        HashMap hmPolicyMap = new HashMap();
        HashMap programMap  = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap  = (HashMap) programMap.get("requestMap");
        String typeString   = (String) requestMap.get("type");
        if (typeString != null && typeString.indexOf(",") > 0) {
            typeString = ((String[])typeString.split(","))[0];
        }
        if(typeString.startsWith("type_")){
            typeString = PropertyUtil.getSchemaProperty(context, typeString);
        }else if(typeString.startsWith("_selectedType")){
            typeString = typeString.substring(typeString.indexOf(':')+1);
        }

        BusinessType boType     = new BusinessType(typeString, context.getVault());
        PolicyList allPolicyList= boType.getPoliciesForPerson(context, false);
        Map policyInfo          = mxType.getDefaultPolicy(context, typeString, false);
        String defaultPolicy    = "";
        if (policyInfo != null) {
            defaultPolicy       = (String) policyInfo.get("name");
        }
        PolicyItr policyItr     = new PolicyItr(allPolicyList);

        String languageStr      = context.getSession().getLanguage();
        Policy policyValue      = null;
        String policyName       = "";
        StringList display      = new StringList();
        StringList actualVal    = new StringList();
        while (policyItr.next()) {
            policyValue         = (Policy) policyItr.obj();
            policyName          = policyValue.getName();
            display.addElement(i18nNow.getAdminI18NString("Policy",policyName, languageStr));
            actualVal.addElement(policyName);
        }
        int position    = actualVal.indexOf(defaultPolicy);
        if (position > 0) {
            String positionDisplay  = (String) display.get(position);
            String positionActual   = (String) actualVal.get(position);
            display.setElementAt(display.get(0), position);
            actualVal.setElementAt(actualVal.get(0), position);
            display.setElementAt(positionDisplay, 0);
            actualVal.setElementAt(positionActual, 0);
        }

        hmPolicyMap.put("field_choices", actualVal);
        hmPolicyMap.put("field_display_choices", display);
        return hmPolicyMap;
    }


    /**
     * To create a Document object
	 * API called from web UI
	 * Appends a random number to the passed name, before creating the Document object
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *      0 - requestMap
     * @return Map contains created objectId
     * @throws Exception
     */
    @com.matrixone.apps.framework.ui.CreateProcessCallable
    public Map createDocument(Context context, String[] args)
    throws Exception {

        HashMap requestMap  = (HashMap) JPO.unpackArgs(args);
        Map returnMap       = new HashMap();

        try {
			UIForm uiForm       = new UIForm();
			String name         = (String)requestMap.get("Name");
			// name will be null if AutoName is selected
		    if (name != null && !"".equals(name)) {
				DomainObject object = DomainObject.newInstance(context, CommonDocument.TYPE_DOCUMENTS);
                String documentNameDelimiter = EnoviaResourceBundle.getProperty(context,"emxComponents.DocumentNameDelimiter");
                name = name + documentNameDelimiter + object.getShortUniqueName(CommonDocument.EMPTY_STRING);
                requestMap.put("Name",name) ;
            }
            String objectId = uiForm.createObject(context, requestMap);

            returnMap.put("id", objectId);

        } catch (Exception e) {
            throw new FrameworkException(e);
        }
        return returnMap;
    }
}

