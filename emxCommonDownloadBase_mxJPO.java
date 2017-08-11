/*
 *  emxCommonDownloadBase.java
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 */
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.BusinessObject;
import matrix.db.ExpansionWithSelect;
import matrix.db.RelationshipType;
import matrix.util.*;
import com.matrixone.apps.common.util.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.domain.*;
import com.matrixone.apps.common.Person;
import com.matrixone.apps.common.Download;

import java.util.*;
import java.text.*;
import matrix.db.ExpansionIterator;


public class emxCommonDownloadBase_mxJPO extends emxDomainObject_mxJPO
{
  private final static String  USER_AGENT = "User Agent";

    public emxCommonDownloadBase_mxJPO (Context context, String[] args)
        throws Exception
    {
        super(context, args);
    }

  public static String getWhereExp(HashMap hmSearchElements)
  {
    StringBuffer sbWhereExp = new StringBuffer();
    Iterator iterator = hmSearchElements.keySet().iterator();
    while(iterator.hasNext())
    {
      String strSearchElementType = (String)iterator.next();
      String strSearchElementValue = (String)hmSearchElements.get(strSearchElementType);

      if (strSearchElementValue != null
        && (!strSearchElementValue.equals(DomainConstants.QUERY_WILDCARD))
        && (!strSearchElementValue.equals(""))
        && !("null".equalsIgnoreCase(strSearchElementValue))) {

        sbWhereExp.append("(");
        sbWhereExp.append(strSearchElementType);
        sbWhereExp.append(" ~~ '");
        sbWhereExp.append(strSearchElementValue);
        sbWhereExp.append("' ) && ");
      }
    }
    if(sbWhereExp.length() > 0)
    {
      sbWhereExp.setLength(sbWhereExp.length() - 4);
    }
    return sbWhereExp.toString();
  }


  @com.matrixone.apps.framework.ui.ProgramCallable
  public MapList getRelatedParts(Context context, String[] args)throws Exception
  {
    HashMap mapParam = (HashMap)JPO.unpackArgs(args);
        MapList mapList = null;

    Map programMap    = (Map) JPO.unpackArgs(args);

    String strType = (String) programMap.get("hdnType");

    if (strType == null
        || strType.equals("")
        || "null".equalsIgnoreCase(strType)) {
        strType = com.matrixone.apps.common.Part.TYPE_PART;
    }
    String strQueryLimit = (String) programMap.get("queryLimit");
    short sQueryLimit = 0;
    if(strQueryLimit != null)
    {
      try
      {
        sQueryLimit = Short.parseShort(strQueryLimit);
      }catch(Exception e)
      {
      }
    }
    String strVault = "";
    String strVaultOption = (String) programMap.get("vaultOption");

    if(PersonUtil.SEARCH_DEFAULT_VAULT.equalsIgnoreCase(strVaultOption) || PersonUtil.SEARCH_LOCAL_VAULTS.equalsIgnoreCase(strVaultOption) || PersonUtil.SEARCH_ALL_VAULTS.equalsIgnoreCase(strVaultOption)) {
      strVault = PersonUtil.getSearchVaults(context,false,strVaultOption);
    } else {
      strVault = (String)programMap.get("vaults");
    }
    if(strVault == null || "".equals(strVault) || "null".equals(strVault))
    {
      strVault = "*";
    }

    HashMap hmWhereElements = new HashMap(6);

    hmWhereElements.put(DomainConstants.SELECT_OWNER, (String) programMap.get("txtOwner"));
    hmWhereElements.put(DomainConstants.SELECT_NAME, (String) programMap.get("txtName"));
    hmWhereElements.put(DomainConstants.SELECT_REVISION, (String) programMap.get("txtRevision"));
    hmWhereElements.put(DomainConstants.SELECT_CURRENT, (String) programMap.get("txtState"));
    hmWhereElements.put(DomainConstants.SELECT_VAULT, strVault);
    hmWhereElements.put(DomainConstants.SELECT_DESCRIPTION, (String) programMap.get("txtDescription"));

    String strWhereExp = getWhereExp(hmWhereElements);
    if("true".equalsIgnoreCase((String) programMap.get("getClassifiedParts")))
    {
      if(strWhereExp.length() > 0)
      {
        strWhereExp += " && ";
      }
      strWhereExp += "to["+DomainConstants.RELATIONSHIP_CLASSIFIED_ITEM+"] == True ";
    }
    String strRelName = DomainConstants.RELATIONSHIP_REFERENCE_DOCUMENT+","+DomainConstants.RELATIONSHIP_PART_SPECIFICATION;

    DomainObject domDocument = new DomainObject((String) programMap.get("objectId"));

    StringList slSelect = new StringList(2);
    slSelect.addElement(DomainConstants.SELECT_ID);

    //ExpansionWithSelect expanSelList = domDocument.expandSelect(context, strRelName, strType, slSelect, new StringList(), true, true, (short)1, strWhereExp, "", sQueryLimit, true);
    // mapList = FrameworkUtil.toMapList(expanSelList);
    ContextUtil.startTransaction(context,false);
    ExpansionIterator expIter = domDocument.getExpansionIterator(context, strRelName, strType, slSelect, new StringList(0), true, true, (short)1, strWhereExp, "", sQueryLimit, true,false,(short)100, false);
    mapList = FrameworkUtil.toMapList(expIter,sQueryLimit,null,null,null,null);
    expIter.close();
    ContextUtil.commitTransaction(context);

    HashMap hm = null;
    for(int i=0; i<mapList.size(); i++)
    {
      Map map = (Map)mapList.get(i);
      Object obj = map.get(DomainConstants.SELECT_ID);
      String strId = (String) ( ( obj instanceof StringList ) ? ((StringList)obj).get(0) : obj );
      hm = new HashMap(1);
      hm.put(DomainConstants.SELECT_ID, strId);
      mapList.set(i, hm);
    }
    return mapList;
  }

  //creates the download object and connects to Document, and Part.
  public int create(Context context, String[] args)throws Exception
  {
    HashMap mapParam = (HashMap)JPO.unpackArgs(args);
    String[] strDocIds = (String[])mapParam.get("documentIds");
    int iSuccess = 0;
    HashMap hmParam = null;
    for(int i=0; i<strDocIds.length; i++)
    {
      hmParam = (HashMap)mapParam.get(strDocIds[i]);
      hmParam.put("documentId", strDocIds[i]);
      hmParam.put("trackUsagePartId", (String)mapParam.get("trackUsagePartId"));
      hmParam.put("createUsage", (String)mapParam.get("createUsage"));
      hmParam.put("createDownload", (String)mapParam.get("createDownload"));
      iSuccess = create(context, hmParam);
      if(iSuccess != 0)
      {
        return iSuccess;
      }
    }
    return iSuccess;
  }

  public int create(Context context, HashMap mapParam)throws Exception
  {
    DomainObject domDownloadHolder = null;
    String strDocumentId = (String)mapParam.get("documentId");
    String strPartId = (String)mapParam.get("trackUsagePartId");
    String strCreateUsage = (String)mapParam.get("createUsage");
    String strCreateDownload = (String)mapParam.get("createDownload");
    Map mapUsageAttrInfo = (Map)mapParam.get("usageAttributeInfo");
    Map mapDownloadAttrInfo = (Map)mapParam.get("downloadDocumentAttrInfo");
    if(strPartId == null || strDocumentId == null)
    {
      return 1;
    }
    //check if the Download Holder Exist.
    //create and connect the download holder to person.
    Person person = Person.getPerson(context);
    String strDownloadHolderId = person.getInfo(context, "from["+Download.RELATIONSHIP_DOWNLOAD_HOLDER+"].to.id");
    if(strDownloadHolderId == null || "".equals(strDownloadHolderId))
    {
      //There is no 'Download Holder' Object for this person. So
      //1) create 'Download Holder' and connect to Person
      //2) create Download and connect to 'Download Holder', Document, and Part.

      createObjects(context, null, null, strPartId, strDocumentId, "true".equalsIgnoreCase(strCreateUsage), "true".equalsIgnoreCase(strCreateDownload), mapUsageAttrInfo, mapDownloadAttrInfo);
    }else
    {
      domDownloadHolder = new DomainObject(strDownloadHolderId);

      String strObjectWhere = null;
      String strDownloadId = null;

      StringList slObjSelect = new StringList(1);
      slObjSelect.add(DomainObject.SELECT_ID);
      if(strPartId == null || "".equals(strPartId))
      {
        //get the Oraphan Download Object
        strObjectWhere = "from["+Download.RELATIONSHIP_DOWNLOAD_CONTEXT+"] == 'False'";
      }
      else
      {
        //get the Part object which is connected through the Download Object.
        strObjectWhere = "from["+Download.RELATIONSHIP_DOWNLOAD_CONTEXT+"].to.id == '"+strPartId+"'";
      }
      MapList mapList = domDownloadHolder.getRelatedObjects(context, Download.RELATIONSHIP_HAS_DOWNLOAD, Download.TYPE_DOWNLOAD,
                        slObjSelect, null, false, true, (short)1 , strObjectWhere, null);
      if(mapList.size() > 0)
      {
        strDownloadId = (String) ((Map)mapList.get(0)).get(DomainObject.SELECT_ID);
        //Part id should be make as null. So that the Download wont be connected again to Part.
        strPartId = null;
      }
      createObjects(context, strDownloadHolderId, strDownloadId, strPartId, strDocumentId, "true".equalsIgnoreCase(strCreateUsage), "true".equalsIgnoreCase(strCreateDownload), mapUsageAttrInfo, mapDownloadAttrInfo);
    }
    return 0;
  }
  private static String getTypeNameRevision(Context context, String objectId)throws Exception
  {
        DomainObject dom = new DomainObject(objectId);
        StringList sl = new StringList(3);
        sl.add(SELECT_TYPE);
        sl.add(SELECT_NAME);
        sl.add(SELECT_REVISION);
        Map map = dom.getInfo(context, sl);
        return (String)map.get(SELECT_TYPE) + " "+(String)map.get(SELECT_NAME) + " "+(String)map.get(SELECT_REVISION);

  }

  private DomainObject createObjects(Context context, String strDownloadHolderId, String strDownloadId, String strPartId, String strDocumentId, boolean createUsage, boolean createDownload, Map usageAttributeInfo, Map downloadAttrInfo)throws Exception
  {
    DomainObject domDownload = null;
    if(strDownloadId == null || "".equals(strDownloadId))
    {
      DomainObject domDownloadHolder = null;
      if(strDownloadHolderId == null)
      {

        //create and connect the download holder to person.
        domDownloadHolder = DomainObject.newInstance(context, Download.TYPE_DOWNLOAD_HOLDER);
        domDownloadHolder.createAndConnect(context, Download.TYPE_DOWNLOAD_HOLDER, null, null, Download.POLICY_DOWNLOAD, null, Download.RELATIONSHIP_DOWNLOAD_HOLDER, Person.getPerson(context), true);

      }
      else
      {
        domDownloadHolder = new DomainObject(strDownloadHolderId);
      }

      //create the Download Object and connect to 'Download Holder' object.
      domDownload = DomainObject.newInstance(context, Download.TYPE_DOWNLOAD);
      domDownload.createAndConnect(context, Download.TYPE_DOWNLOAD, null, null, Download.POLICY_DOWNLOAD, null, Download.RELATIONSHIP_HAS_DOWNLOAD, domDownloadHolder, true);

    }
    else
    {
      domDownload = new DomainObject(strDownloadId);
    }
    ContextUtil.pushContext(context, USER_AGENT, null, null);
    MqlUtil.mqlCommand(context,"history $1", true, "off");
    boolean isHistorOff =true;
    boolean isWriteDownloadDocHistory = false;
    boolean isWriteDocUsageHistory = false;
    boolean isWriteDownloadContextHistory = false;
    try
    {
      if(createDownload)
      {
        //connect Download object to Documnet Object
        DomainRelationship domRelDownload = domDownload.addToObject(context, new RelationshipType(Download.RELATIONSHIP_DOWNLOAD_DOCUMENT), strDocumentId);
        domRelDownload.setAttributeValues(context, downloadAttrInfo);
        isWriteDownloadDocHistory = true;
      }

      if(createUsage)
      {
        //connect to Downlaod Object to Document Object with Document Usage.
        DomainRelationship domRelUsage = domDownload.addToObject(context, new RelationshipType(Download.RELATIONSHIP_DOCUMENT_USAGE), strDocumentId);
        domRelUsage.setAttributeValues(context, usageAttributeInfo);
        isWriteDocUsageHistory = true;
      }
      if(strPartId != null)
      {
        //connect Download object to Part Object
        domDownload.addToObject(context, new RelationshipType(Download.RELATIONSHIP_DOWNLOAD_CONTEXT), strPartId);
        isWriteDownloadContextHistory = true;
      }
    }finally
    {
      if(isHistorOff)
      {
        MqlUtil.mqlCommand(context,"history $1", true, "on");
      }
      ContextUtil.popContext(context);
      String strDownloadObjectId = domDownload.getId();
      String strDocInfo = null;
      String strDownloadInfo = null;
      if(isWriteDownloadDocHistory || isWriteDocUsageHistory || isWriteDownloadContextHistory)
      {
        strDownloadInfo = getTypeNameRevision(context, domDownload.getId());
      }
      if(isWriteDownloadDocHistory || isWriteDocUsageHistory)
      {
        strDocInfo = getTypeNameRevision(context, strDocumentId);
      }
      if(isWriteDownloadDocHistory)
      {
        MqlUtil.mqlCommand(context, "modify bus $1 add history $2 comment $3",strDownloadObjectId, "Connect", "connect "+Download.RELATIONSHIP_DOWNLOAD_DOCUMENT+" to "+strDocInfo);
        MqlUtil.mqlCommand(context, "modify bus $1 add history $2 comment $3",strDocumentId, "Connect", "connect "+Download.RELATIONSHIP_DOWNLOAD_DOCUMENT+" from "+strDownloadInfo);

      }
      if(isWriteDocUsageHistory)
      {
        MqlUtil.mqlCommand(context, "modify bus $1 add history $2 comment $3",strDownloadObjectId, "Connect", "connect "+Download.RELATIONSHIP_DOCUMENT_USAGE+" to "+strDocInfo);
        MqlUtil.mqlCommand(context, "modify bus $1 add history $2 comment $3",strDocumentId, "Connect", "connect "+Download.RELATIONSHIP_DOCUMENT_USAGE+" from "+strDownloadInfo);
      }

      if(isWriteDownloadContextHistory)
      {
        String strPartInfo = getTypeNameRevision(context, strPartId);
        MqlUtil.mqlCommand(context, "modify bus $1 add history $2 comment $3",strDownloadObjectId, "Connect", "connect "+Download.RELATIONSHIP_DOWNLOAD_CONTEXT+" to "+strPartInfo);
        MqlUtil.mqlCommand(context, "modify bus $1 add history $2 comment $3",strPartId, "Connect", "connect "+Download.RELATIONSHIP_DOWNLOAD_CONTEXT+" from "+strDownloadInfo);
      }
    }
    return domDownload;
  }

  public Map getUsageIds(Context context, String[] args)throws Exception
    {
    HashMap programMap = (HashMap)JPO.unpackArgs(args);
    String strPartId = (String)programMap.get("trackUsagePartId");
    if(strPartId == null || "".equals(strPartId) || "null".equals(strPartId))
    {
      return null;
    }
    Map resMap = new HashMap();
    Person person = Person.getPerson(context);

    StringList slBusSelect = new StringList(2);
    slBusSelect.add("from["+Download.RELATIONSHIP_DOCUMENT_USAGE+"].to.id");
    slBusSelect.add("from["+Download.RELATIONSHIP_DOCUMENT_USAGE+"]."+DomainConstants.SELECT_RELATIONSHIP_ID);

    String strBusWhere = "to["+Download.RELATIONSHIP_HAS_DOWNLOAD+"].from.to["+Download.RELATIONSHIP_DOWNLOAD_HOLDER+"].from.id=="+person.getObjectId();

    DomainObject domPart = new DomainObject(strPartId);
    MapList mapList = domPart.getRelatedObjects(context, Download.RELATIONSHIP_DOWNLOAD_CONTEXT, Download.TYPE_DOWNLOAD,
                        slBusSelect, null, true, false, (short)1, strBusWhere, null);

    for(int i=0; i<mapList.size(); i++)
    {
      Map map = (Map)mapList.get(i);
      Object obj = map.get("from["+Download.RELATIONSHIP_DOCUMENT_USAGE+"].to.id");
      StringList slToIds = null;
      StringList slUsageIds = null;

      if(obj == null)
      {
        continue;
      }
      else if(obj instanceof StringList)
      {
        slToIds = (StringList)obj;
        slUsageIds = (StringList)map.get("from["+Download.RELATIONSHIP_DOCUMENT_USAGE+"]."+DomainConstants.SELECT_RELATIONSHIP_ID);
      }else
      {
        slToIds = new StringList(1);
        slToIds.add((String)obj);

        slUsageIds = new StringList(1);
        slUsageIds.add((String)map.get("from["+Download.RELATIONSHIP_DOCUMENT_USAGE+"]."+DomainConstants.SELECT_RELATIONSHIP_ID));
      }
      for(int j=0; j<slToIds.size(); j++)
      {
        resMap.put((String)slToIds.get(j),  (String)slUsageIds.get(j));
      }
    }
    return resMap;

  }


  public boolean displayUsageIcon(Context context, String[] args)throws Exception
  {

    if(Download.isTrackUsageOn(context))
    {

      HashMap programMap = (HashMap)JPO.unpackArgs(args);
      String parentRelName = (String) programMap.get("parentRelName");
      if(parentRelName  != null && (parentRelName.equals("relationship_PartSpecification") || parentRelName.equals("relationship_ReferenceDocument")) )
      {
        Map mapParamList = (Map) programMap.get("paramList");
        String strPartId = mapParamList == null ? (String)programMap.get("objectId") : (String)mapParamList.get("objectId") ;
        try
        {
          return Download.isClassifiedPart(context, strPartId);
        }catch(Exception e)
        {
          return false;
        }
      }
    }
    return false;
  }

  public Vector getUsageIcon(Context context, String[] args)throws Exception
  {
    HashMap programMap = (HashMap)JPO.unpackArgs(args);
    Map mapParamList = (Map) programMap.get("paramList");
    String strPartId = (String)mapParamList.get("objectId");
    if(strPartId == null || "".equals(strPartId) || "null".equals(strPartId))
    {
      return new Vector();
    }
    Person person = Person.getPerson(context);

    StringList slBusSelect = new StringList(1);
    slBusSelect.add("from["+Download.RELATIONSHIP_DOCUMENT_USAGE+"].id");
    slBusSelect.add("from["+Download.RELATIONSHIP_DOCUMENT_USAGE+"].to.id");

    String strBusWhere = "to["+Download.RELATIONSHIP_HAS_DOWNLOAD+"].from.to.from.id=="+person.getObjectId();
    DomainObject domPart = new DomainObject(strPartId);
    MapList mapList = domPart.getRelatedObjects(context, Download.RELATIONSHIP_DOWNLOAD_CONTEXT, Download.TYPE_DOWNLOAD,
                        slBusSelect, null, true, false, (short) 1, strBusWhere, null);
    HashMap hm = new HashMap();
    for(int i=0; i<mapList.size(); i++)
    {
      Map map = (Map)mapList.get(i);
      Object obj = map.get("from["+Download.RELATIONSHIP_DOCUMENT_USAGE+"].to.id");
      StringList slDocId = null;
      StringList slUsageId = null;
      if(obj == null)
      {
        continue;
      }
      else if(obj instanceof String)
      {
        slDocId = new StringList();
        slDocId.add((String)obj);

        slUsageId = new StringList();
        slUsageId.add((String)map.get("from["+Download.RELATIONSHIP_DOCUMENT_USAGE+"].id"));
      }else if(obj instanceof StringList)
      {
        slDocId = (StringList) obj;
        slUsageId = (StringList) map.get("from["+Download.RELATIONSHIP_DOCUMENT_USAGE+"].id");
      }
      for(int j=0; j<slDocId.size(); j++)
      {
        hm.put((String)slDocId.get(j), (String)slUsageId.get(j));
      }

    }
    mapList = ( MapList )programMap.get( "objectList" );
    Vector vec = new Vector(mapList.size());
    for(int i=0; i<mapList.size(); i++)
    {
      Map map = (Map)mapList.get(i);
      String strDocId = (String)map.get("id");
      String strUsageId = (String)hm.get(strDocId);
      if(strUsageId == null)
      {
        strUsageId = "";
      }else
      {
      strUsageId = "<a href=\"javascript:showModalDialog('../components/emxCommonUsageDialogFS.jsp?relId="+XSSUtil.encodeForJavaScript(context,strUsageId)+"&amp;documentId="+XSSUtil.encodeForJavaScript(context, strDocId)+"', 570, 520)\"><img border=\"0\" src=\"../common/images/iconSmallUsage.gif\"></img></a>";
      }
      vec.add(strUsageId);
    }
    return vec ;
  }

  public Vector getUsageOriginator(Context context, String[] args)throws Exception
  {
	  //XSSOK
    return getOwner(context, args);
  }

  public Vector getUsageOriginated(Context context, String[] args)throws Exception
  {
    return getList(args, SELECT_ORIGINATED);
  }

  @com.matrixone.apps.framework.ui.ProgramCallable
  public MapList getUsage(Context context, String[] args)throws Exception
  {
    MapList mapListResult = null;

      MapList mapList = null;
      short sQueryLimit = (short)0;
      HashMap mapParam = (HashMap)JPO.unpackArgs(args);

      String strOriginatorId = (String)mapParam.get("txtPersonOID");
      String strOriginator = (String)mapParam.get("txtPerson");
      String strStateOfUsage = (String)mapParam.get("selStateOfUsage");
      String strDocumentName = (String)mapParam.get("txtDocument");
      String strDocumentId = (String)mapParam.get("txtDocumentOID");
      String strPurpose = (String)mapParam.get("txtPurpose");
      String strQueryLimit = (String)mapParam.get("QueryLimit");
      try
      {
        sQueryLimit = Short.parseShort(strQueryLimit);
      }catch(Exception e)
      {
          //do nothing.
      }

      boolean processMapList = false;

      String strMatchCase = null;
      boolean isDocSelected = false;

      if(strOriginator != null && !QUERY_WILDCARD.equals(strOriginator) && !"".equals(strOriginator))
      {
        processMapList = true;
        StringBuffer sbBusWhere = new StringBuffer();
        StringBuffer sbSelWhere = new StringBuffer();

        if(strDocumentId != null && !"".equals(strDocumentId))
        {
          isDocSelected = true;
          sbBusWhere.append(" from["+Download.RELATIONSHIP_DOCUMENT_USAGE+"].to.id matchlist \""+strDocumentId+"\" \",\"  && ");
        }

        if(strPurpose != null && !QUERY_WILDCARD.equals(strPurpose) && !"".equals(strPurpose))
        {
          strMatchCase = strPurpose.indexOf("*") == -1 ? " == " :  " ~~ ";

          sbBusWhere.append(" from["+Download.RELATIONSHIP_DOCUMENT_USAGE+"].attribute["+Download.ATTRIBUTE_DOWNLOAD_PURPOSE+"] "+strMatchCase+" \""+strPurpose+"\"  && ");
          sbSelWhere.append(" attribute["+Download.ATTRIBUTE_DOWNLOAD_PURPOSE+"] "+strMatchCase+" \""+strPurpose+"\"  && ");
        }

        if(strStateOfUsage != null && !QUERY_WILDCARD.equals(strStateOfUsage) && !"".equals(strStateOfUsage))
        {
          sbBusWhere.append(" from["+Download.RELATIONSHIP_DOCUMENT_USAGE+"].attribute["+Download.ATTRIBUTE_STATE_OF_USAGE+"] == \""+strStateOfUsage+"\"  && ");
          sbSelWhere.append(" attribute["+Download.ATTRIBUTE_STATE_OF_USAGE+"] == \""+strStateOfUsage+"\"  && ");
        }
        String strSelWhere = null;
        if(sbBusWhere.length()>0)
        {
          sbBusWhere.setLength(sbBusWhere.length() - 4);
        }
        if(sbSelWhere.length() > 0)
        {
          sbSelWhere.setLength(sbSelWhere.length() - 4);
          sbSelWhere.insert(0, Download.RELATIONSHIP_DOCUMENT_USAGE+"|(");
          strSelWhere = sbSelWhere.append(")").toString();
        }
        else
        {
          strSelWhere = Download.RELATIONSHIP_DOCUMENT_USAGE;
        }
        Person person = null;
        if(strOriginatorId != null) {
            person = new Person(strOriginatorId);
        }
        String strDownloadHolderId = person.getInfo(context, "from["+Download.RELATIONSHIP_DOWNLOAD_HOLDER+"].to.id");
        if(strDownloadHolderId == null || "".equals(strDownloadHolderId))
        {
          return new MapList();
        }

        DomainObject domDownloadHolder = new DomainObject(strDownloadHolderId);
        StringList slBusSelect = new StringList(6);
        slBusSelect.add(SELECT_OWNER);
        slBusSelect.add("from["+strSelWhere+"].to.id");
        slBusSelect.add("from["+strSelWhere+"].to.name");
        slBusSelect.add("from["+strSelWhere+"]."+SELECT_ID);
        slBusSelect.add("from["+strSelWhere+"]."+SELECT_ORIGINATED);

        /*ExpansionWithSelect expanSelList = domDownloadHolder.expandSelect(context, Download.RELATIONSHIP_HAS_DOWNLOAD, Download.TYPE_DOWNLOAD, slBusSelect, new StringList(), false, true, (short)1, sbBusWhere.toString(), "", sQueryLimit, true);
        mapList = FrameworkUtil.toMapList(expanSelList);*/
        ContextUtil.startTransaction(context,false);
        ExpansionIterator expIter = domDownloadHolder.getExpansionIterator(context, Download.RELATIONSHIP_HAS_DOWNLOAD, Download.TYPE_DOWNLOAD, slBusSelect, new StringList(0), false, true, (short)1, sbBusWhere.toString(), null, sQueryLimit, true,false,(short)100, false);
        mapList = FrameworkUtil.toMapList(expIter,sQueryLimit,null,null,null,null);
        expIter.close();
        ContextUtil.commitTransaction(context);

      }
      else if(strDocumentName != null && !QUERY_WILDCARD.equals(strDocumentName) && !"".equals(strDocumentName))
      {

        StringBuffer sbRelWhere = new StringBuffer();
        if(strPurpose != null && !QUERY_WILDCARD.equals(strPurpose) && !"".equals(strPurpose))
        {
          strMatchCase = strPurpose.indexOf("*") == -1 ?" == " :  " ~~ ";
          sbRelWhere.append(" attribute["+Download.ATTRIBUTE_DOWNLOAD_PURPOSE+"] "+strMatchCase+" \""+strPurpose+"\"  && ");
        }
        if(strStateOfUsage != null && !QUERY_WILDCARD.equals(strStateOfUsage) && !"".equals(strStateOfUsage))
        {
          sbRelWhere.append(" attribute["+Download.ATTRIBUTE_STATE_OF_USAGE+"] == \""+strStateOfUsage+"\"  && ");
        }
        if(sbRelWhere.length() > 0)
        {
          sbRelWhere.setLength(sbRelWhere.length() -4);
        }
        StringList slRelSelect = new StringList(1);
        //select owner, downloaded files, download status, and originated.
        slRelSelect.add(SELECT_ID);


        StringList slBusSelect = new StringList(2);
        slBusSelect.add(SELECT_OWNER);
        slBusSelect.add(SELECT_ORIGINATED);
        //expand the Document object and get the Download information & Part name & id.
        DomainObject domDocument = new DomainObject(strDocumentId);
        /*ExpansionWithSelect expanSelList = domDocument.expandSelect(context, Download.RELATIONSHIP_DOCUMENT_USAGE, Download.TYPE_DOWNLOAD, slBusSelect, slRelSelect, true, false, (short)1, "", sbRelWhere.toString(), sQueryLimit, true);
        mapList = FrameworkUtil.toMapList(expanSelList);*/

        ContextUtil.startTransaction(context,false);
        ExpansionIterator expIter = domDocument.getExpansionIterator(context, Download.RELATIONSHIP_DOCUMENT_USAGE, Download.TYPE_DOWNLOAD, slBusSelect, slRelSelect, true, false, (short)1, null, sbRelWhere.toString(), sQueryLimit, true,false,(short)100, false);
        mapList = FrameworkUtil.toMapList(expIter,sQueryLimit,null,null,null,null);
        expIter.close();
        ContextUtil.commitTransaction(context);
        
        mapListResult = new MapList(mapList.size());
        for(int i=0; i<mapList.size(); i++)
        {
          Map map = (Map)mapList.get(i);

          HashMap hashMap = new HashMap(7);
          hashMap.put(Download.ATTRIBUTE_DOWNLOAD_PURPOSE, (String)((StringList)map.get("attribute["+Download.ATTRIBUTE_DOWNLOAD_PURPOSE+"]")).get(0));
          hashMap.put(Download.ATTRIBUTE_STATE_OF_USAGE, (String)((StringList)map.get("attribute["+Download.ATTRIBUTE_STATE_OF_USAGE+"]")).get(0));
          hashMap.put(SELECT_ORIGINATED, (String)((StringList)map.get(SELECT_ORIGINATED)).get(0));
          hashMap.put(SELECT_OWNER, (String)((StringList)map.get(SELECT_OWNER)).get(0));
          hashMap.put(SELECT_ID, (String)((StringList)map.get(SELECT_ID)).get(0));
          hashMap.put("DocumentId", strDocumentId);
          hashMap.put("DocumentName", strDocumentName);
          mapListResult.add(hashMap);


        }

      }
      else
      {
        StringBuffer sbWhere = new StringBuffer();
        StringBuffer sbSelectWhere = new StringBuffer();

        processMapList = true;
        if(strPurpose != null && !QUERY_WILDCARD.equals(strPurpose) && !"".equals(strPurpose))
        {
          strMatchCase = strPurpose.indexOf("*") == -1 ?" == " :  " ~~ ";
          sbWhere.append(" from["+Download.RELATIONSHIP_DOCUMENT_USAGE+"].attribute["+Download.ATTRIBUTE_DOWNLOAD_PURPOSE+"] "+strMatchCase+"  \""+strPurpose+"\"  && ");
          sbSelectWhere.append(" attribute["+Download.ATTRIBUTE_DOWNLOAD_PURPOSE+"] "+strMatchCase+"  \""+strPurpose+"\"  && ");
        }
        if(strStateOfUsage != null && !QUERY_WILDCARD.equals(strStateOfUsage) && !"".equals(strStateOfUsage))
        {
          sbWhere.append(" from["+Download.RELATIONSHIP_DOCUMENT_USAGE+"].attribute["+Download.ATTRIBUTE_STATE_OF_USAGE+"] == \""+strStateOfUsage+"\"  && ");
          sbSelectWhere.append(" attribute["+Download.ATTRIBUTE_STATE_OF_USAGE+"] == \""+strStateOfUsage+"\"  && ");
        }
        String strSelWhere = null;

        if(sbWhere.length()>0)
        {
          //remove the last ' && ' from the command.
          sbWhere.setLength(sbWhere.length() - 4);

          sbSelectWhere.insert(0,  Download.RELATIONSHIP_DOCUMENT_USAGE+"|(");
          sbSelectWhere.setLength(sbSelectWhere.length() - 4);
          sbSelectWhere.append(")]");
          strSelWhere = sbSelectWhere.toString();

        }else
        {
          strSelWhere = Download.RELATIONSHIP_DOCUMENT_USAGE;
        }


        if(!MULTI_VALUE_LIST.contains("from["+Download.RELATIONSHIP_DOCUMENT_USAGE+"].to.name"))
        {
          MULTI_VALUE_LIST.add("from["+Download.RELATIONSHIP_DOCUMENT_USAGE+"].to.name");
        }
        if(!MULTI_VALUE_LIST.contains("from["+Download.RELATIONSHIP_DOCUMENT_USAGE+"].to.id"))
        {
          MULTI_VALUE_LIST.add("from["+Download.RELATIONSHIP_DOCUMENT_USAGE+"].to.id");
        }
        if(!MULTI_VALUE_LIST.contains("from["+Download.RELATIONSHIP_DOCUMENT_USAGE+"]."+SELECT_ORIGINATED))
        {
          MULTI_VALUE_LIST.add("from["+Download.RELATIONSHIP_DOCUMENT_USAGE+"]."+SELECT_ORIGINATED);
        }
        if(!MULTI_VALUE_LIST.contains("from["+Download.RELATIONSHIP_DOCUMENT_USAGE+"]."+SELECT_ID))
        {
          MULTI_VALUE_LIST.add("from["+Download.RELATIONSHIP_DOCUMENT_USAGE+"]."+SELECT_ID);
        }

        StringList slBusSelect = new StringList(9);
        slBusSelect.add(SELECT_OWNER);
        slBusSelect.add("from["+strSelWhere+"].to.id");
        slBusSelect.add("from["+strSelWhere+"].to.name");
        slBusSelect.add("from["+strSelWhere+"]."+SELECT_ID);
        slBusSelect.add("from["+strSelWhere+"].attribute["+Download.ATTRIBUTE_DOWNLOAD_PURPOSE+"]");
        slBusSelect.add("from["+strSelWhere+"].attribute["+Download.ATTRIBUTE_STATE_OF_USAGE+"]");
        slBusSelect.add("from["+strSelWhere+"]."+SELECT_ORIGINATED);
        slBusSelect.add("from["+strSelWhere+"]."+SELECT_ID);
        mapList = DomainObject.findObjects(context, Download.TYPE_DOWNLOAD, QUERY_WILDCARD, QUERY_WILDCARD, QUERY_WILDCARD, QUERY_WILDCARD, sbWhere.toString(), "", false, slBusSelect, sQueryLimit);

      }

      if(processMapList)
      {
        mapListResult = new MapList(mapList.size());
        for(int i=0; i<mapList.size(); i++)
        {
          Map map = (Map)mapList.get(i);
          StringList slUsageOriginated = null;
          StringList slUsageRelId = null;
          StringList slDocumentNames = null;
          StringList slDocumentIds = null;
          StringList slOwner = null;
          Object obj = map.get("from["+Download.RELATIONSHIP_DOCUMENT_USAGE+"]."+SELECT_ID);

          if(obj == null)
          {
            continue;
          }
          Object objOwner = map.get(SELECT_OWNER);

          if(objOwner  instanceof StringList)
          {

            slOwner = (StringList) objOwner;
          }else
          {
            slOwner = new StringList(1);
            slOwner.add((String)map.get(SELECT_OWNER));
          }

          if(obj instanceof StringList)
          {

            slUsageOriginated = (StringList)map.get("from["+Download.RELATIONSHIP_DOCUMENT_USAGE+"]."+SELECT_ORIGINATED);
            //StringList slOwner = (StringList)map.get(SELECT_OWNER);

            slUsageRelId = (StringList)obj;


            slDocumentNames = (StringList)map.get("from["+Download.RELATIONSHIP_DOCUMENT_USAGE+"].to.name");
            slDocumentIds = (StringList)map.get("from["+Download.RELATIONSHIP_DOCUMENT_USAGE+"].to.id");

          }else
          {
            slUsageOriginated = new StringList(1);
            slUsageOriginated.add((String)map.get("from["+Download.RELATIONSHIP_DOCUMENT_USAGE+"]."+SELECT_ORIGINATED));
            //StringList slOwner = (StringList)map.get(SELECT_OWNER);
            slUsageRelId = new StringList(1);
            slUsageRelId.add((String)obj);

            slDocumentNames = new StringList(1);
            slDocumentNames.add((String)map.get("from["+Download.RELATIONSHIP_DOCUMENT_USAGE+"].to.name"));
            slDocumentIds = new StringList(1);
            slDocumentIds.add((String)map.get("from["+Download.RELATIONSHIP_DOCUMENT_USAGE+"].to.id"));

          }

          for(int j=0; j<slUsageRelId.size(); j++)
          {
            if(isDocSelected && strDocumentId.indexOf((String)slDocumentIds.get(0)) == -1)
            {
              continue;
            }

            HashMap hashMap = new HashMap(7);
            hashMap.put(SELECT_ORIGINATED, (String)slUsageOriginated.get(j));
            hashMap.put(SELECT_OWNER, (String)slOwner.get(0));
            hashMap.put(SELECT_RELATIONSHIP_ID, (String)slUsageRelId.get(j));

            hashMap.put("DocumentId", slDocumentIds.get( isDocSelected ? 0 : j ));
            hashMap.put("DocumentName", slDocumentNames.get(isDocSelected ? 0 : j));
            mapListResult.add(hashMap);

          }
        }
      }
    return mapListResult;
  }

  public Vector getOriginator(Context context, String[] args)throws Exception
  {
    return getList(args, SELECT_ORIGINATOR);
  }

  public Vector getDocument(Context context, String[] args)throws Exception
  {
    return getList(context, args, "DocumentId", "DocumentName");
  }

  public Vector getUsageNewWindow(Context context, String[] args)throws Exception
  {
    HashMap programMap = ( HashMap ) JPO.unpackArgs( args );

    MapList mapList = ( MapList )programMap.get( "objectList" );

    HashMap paramMap = (HashMap) programMap.get("paramList");
    boolean bPrintMode = false;
    boolean bExport = false;
    String reportFormat = (String) paramMap.get("reportFormat");
    if("ExcelHTML".equals(reportFormat)  || "CSV".equals(reportFormat) )
    {
      bExport = true;
    }else if("HTML".equals(reportFormat) || "true".equalsIgnoreCase((String)paramMap.get("editTableMode")))
    {
      bPrintMode = true;
    }

    Vector vec = new Vector(mapList.size());
    for(int i=0; i<mapList.size(); i++)
    {
      Map map = (Map)mapList.get(i);
      if(bExport)
      {
        vec.add("");
      }
      else if(bPrintMode)
      {
        vec.add("<img border=\"0\" src=\"images/iconActionEdit.gif\">");
      }else
      {
        vec.add("<a href=\"javascript:emxTableColumnLinkClick('../components/emxCommonUsageDialogFS.jsp?refreshContent=true&relId="+(String)map.get(SELECT_RELATIONSHIP_ID)+"&documentName="+XSSUtil.encodeForURL(context,XSSUtil.encodeForURL(context,(String)map.get("DocumentName")))+"',%20'570',%20'520',%20'false',%20'popup',%20'')\"  class=\"object\"><img border=\"0\" src=\"images/iconActionEdit.gif\"></a>");
      }
    }
    return vec;
  }

  public Vector getDownloadedDocument(Context context, String[] args)throws Exception
  {
//XSSOK
    return getList(context, args, "DocumentId", "DocumentName");
  }

  public Vector getOwner(Context context, String[] args)throws Exception
  {

    HashMap programMap = ( HashMap ) JPO.unpackArgs( args );
    HashMap paramMap = (HashMap) programMap.get("paramList");
    String reportFormat = (String) paramMap.get("reportFormat");
    boolean bExportPrint = false;
    if("ExcelHTML".equals(reportFormat)  || "CSV".equals(reportFormat) || "HTML".equals(reportFormat) || "true".equalsIgnoreCase((String)paramMap.get("editTableMode")) )
    {
      bExportPrint = true;
    }

    Vector vecPersonNames = getList(args, SELECT_OWNER);
    StringBuffer sbPerNames = new StringBuffer();

    for(int i=0; i<vecPersonNames.size(); i++)
    {
      sbPerNames.append(vecPersonNames.get(i)+",");
    }
    StringList slObjectSelects = new StringList(4);
    slObjectSelects.add(SELECT_ID);
    slObjectSelects.add(SELECT_NAME);
    slObjectSelects.add("attribute["+ATTRIBUTE_FIRST_NAME+"]");
    slObjectSelects.add("attribute["+ATTRIBUTE_LAST_NAME+"]");

    MapList mlPersonInfo = DomainObject.findObjects(context, TYPE_PERSON, sbPerNames.toString(), "*", null, null,
                "revision == last", false, slObjectSelects);
    String strLink = "<a href=\"javascript:emxTableColumnLinkClick('../common/emxTree.jsp?objectId=";

    HashMap hmPersonInfo = new HashMap(vecPersonNames.size());
    String strURL = "";


    for(int i=0; i<mlPersonInfo.size(); i++)
    {
      Map map = (Map)mlPersonInfo.get(i);
      if(bExportPrint)
      {
        strURL =  XSSUtil.encodeForHTML(context, (String)map.get("attribute["+ATTRIBUTE_LAST_NAME+"]")) + ", " + XSSUtil.encodeForHTML(context, (String)map.get("attribute["+ATTRIBUTE_FIRST_NAME+"]"));
      }
      else
      {
        strURL = strLink+XSSUtil.encodeForJavaScript(context, (String)map.get(SELECT_ID)) + "','570','520','false','popup','')\">" + XSSUtil.encodeForHTML(context, (String)map.get("attribute["+ATTRIBUTE_LAST_NAME+"]")) + ", " + XSSUtil.encodeForHTML(context, (String)map.get("attribute["+ATTRIBUTE_FIRST_NAME+"]")) + "</a>";
      }
      hmPersonInfo.put((String)map.get(SELECT_NAME), strURL);
    }


    for(int i=0; i<vecPersonNames.size(); i++)
    {
      vecPersonNames.set(i, (String)hmPersonInfo.get(vecPersonNames.get(i)));
    }
    return vecPersonNames;
  }

  public Vector getDownloadedOriginator(Context context, String[] args)throws Throwable
  {
	//XSSOK
    return getOwner(context, args);
  }

  public Vector getParentComponent(Context context, String[] args)throws Exception
  {
//XSSOK
      return getList(context, args, "PartId", "PartName");
  }

  private Vector getList(Context context, String[] args, String id, String name)throws Exception
  {
    HashMap programMap = ( HashMap ) JPO.unpackArgs( args );
    MapList mapList = ( MapList )programMap.get( "objectList" );

    HashMap paramMap = (HashMap) programMap.get("paramList");
    boolean reportMode = false;
    if(paramMap != null)
    {
      String reportFormat = (String) paramMap.get("reportFormat");
      reportMode = ("HTML".equals(reportFormat) || "ExcelHTML".equals(reportFormat)  || "CSV".equals(reportFormat) || "true".equalsIgnoreCase((String)paramMap.get("editTableMode")) );
    }

    Vector vec = new Vector(mapList.size());
    String strLink = "<a href=\"javascript:emxTableColumnLinkClick('../common/emxTree.jsp?emxSuiteDirectory=components&treeMenu=&objectId=";
    for(int i=0; i<mapList.size(); i++)
    {
      Map map = (Map)mapList.get(i);
      String strPartId = (String)map.get(id);
      String strPartName = (String)map.get(name);
      if(reportMode)
      {
        vec.add(strPartName);
      }else
      {
        StringBuffer sb = new StringBuffer(strLink);
        sb.append(strPartId);
        sb.append("', '860', '520', 'false', 'popup')\">");
        sb.append(XSSUtil.encodeForHTML(context,strPartName));
        sb.append("</a>");
        vec.add(sb.toString());
      }
    }
    return vec;
  }

  public static Vector getList(String[] args, String key)throws Exception
  {
    HashMap programMap = ( HashMap ) JPO.unpackArgs( args );
    MapList mapList = ( MapList )programMap.get( "objectList" );

    Vector vec = new Vector(mapList.size());
    for(int i=0; i<mapList.size(); i++)
    {
      Map map = (Map)mapList.get(i);
      vec.add((String)map.get(key));
    }
    return vec;
  }

  public Vector getDownloadNewWindow(Context context, String[] args)throws Exception
  {
    HashMap programMap = ( HashMap ) JPO.unpackArgs( args );
    MapList mapList = ( MapList )programMap.get( "objectList" );

    HashMap paramMap = (HashMap) programMap.get("paramList");
    boolean bPrintMode = false;
    boolean bExport = false;

    String reportFormat = (String) paramMap.get("reportFormat");
    if("ExcelHTML".equals(reportFormat)  || "CSV".equals(reportFormat) )
    {
      bExport = true;
    }else if("HTML".equals(reportFormat) || "true".equalsIgnoreCase((String)paramMap.get("editTableMode")) )
    {
      bPrintMode = true;
    }

    Vector vec = new Vector(mapList.size());
    for(int i=0; i<mapList.size(); i++)
    {
      Map map = (Map)mapList.get(i);
      if(bExport)
      {
        vec.add("");
      }
      else if(bPrintMode)
      {
        vec.add("<img border=\"0\" src=\"images/iconActionEdit.gif\">");
      }else
      {
        vec.add("<a href=\"javascript:emxTableColumnLinkClick('../components/emxCommonDownloadEditDialogFS.jsp?relId="+(String)map.get(SELECT_RELATIONSHIP_ID)+"&partName="+XSSUtil.encodeForURL(context,XSSUtil.encodeForURL(context,(String)map.get("PartName")))+"&documentName="+XSSUtil.encodeForURL(context,XSSUtil.encodeForURL(context,(String)map.get("DocumentName")))+"&originator="+XSSUtil.encodeForURL(context,XSSUtil.encodeForURL(context,(String)map.get(SELECT_OWNER)))+"',%20'570',%20'520',%20'false',%20'popup',%20'')\"  class=\"object\"><img border=\"0\" src=\"images/iconActionEdit.gif\"></a>");
      }
    }
    return vec;
  }


  @com.matrixone.apps.framework.ui.ProgramCallable
  public MapList getDownloads(Context context, String[] args)throws Exception
  {
    MapList mapList = null;
    MapList mapListResult = null;
    short sQueryLimit = (short)0;

      HashMap mapParam = (HashMap)JPO.unpackArgs(args);
      String strOriginator = (String)mapParam.get("txtPerson");
      String strDocumentId = (String)mapParam.get("txtDocumentOID");
      String strStartDate = (String)mapParam.get("txtStartDate");
      String strEndDate = (String)mapParam.get("txtEndDate");
      String strStatus = (String)mapParam.get("status");
      String strQueryLimit = (String)mapParam.get("QueryLimit");
      String strOriginatorId = (String)mapParam.get("txtPersonOID");
      String strDocumentName = (String)mapParam.get("txtDocument");
      Locale locale = (Locale)mapParam.get("localeObj");
      SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");      
      double iClientTimeOffset = (new Double((String)mapParam.get("timeZone"))).doubleValue();
      
      if(strStartDate != null && !"".equals(strStartDate))
      {
    	strStartDate = eMatrixDateFormat.getFormattedInputDate(strStartDate, iClientTimeOffset, locale);
    	Date startDate = eMatrixDateFormat.getJavaDate(strStartDate, locale);
    	matrix.util.MxCalendar start = new matrix.util.MxCalendar();
    	start.setTime(startDate);
    	strStartDate = sdf.format(start.getTime());
      }
      if(strEndDate != null && !"".equals(strEndDate))
      {
    	  String temp = eMatrixDateFormat.getFormattedInputDate(strEndDate, iClientTimeOffset, locale);
    	  Date enDate = eMatrixDateFormat.getJavaDate(temp, locale);
    	  matrix.util.MxCalendar endDate = new matrix.util.MxCalendar();
    	  endDate.setTime(enDate);
    	  //added one day to make sure it gives
    	  endDate.add(Calendar.DATE, 1);
    	  strEndDate = sdf.format(endDate.getTime());
      }

      try
      {
        sQueryLimit = Short.parseShort(strQueryLimit);
      }catch(Exception e)
      {
          //do nothing.
      }
      boolean processMapList = false;

      StringBuffer sbBusWhere = new StringBuffer();
      if(strOriginator != null && !QUERY_WILDCARD.equals(strOriginator) && !"".equals(strOriginator))
      {
        processMapList = true;

        StringBuffer sbDocWhereClause  = new StringBuffer();
        String strDocWhereClause = null;

        if(strDocumentId != null && !"".equals(strDocumentId))
        {
          sbBusWhere.append(" from["+Download.RELATIONSHIP_DOWNLOAD_DOCUMENT+"].to.id matchlist \""+strDocumentId+"\" \",\"  && ");
          sbDocWhereClause.append("to.id matchlist \""+strDocumentId+"\" \",\" && ");
        }

        if(strStartDate != null && !"".equals(strStartDate))
        {
          sbBusWhere.append(" from["+Download.RELATIONSHIP_DOWNLOAD_DOCUMENT+"].attribute["+Download.ATTRIBUTE_DOWNLOAD_TIME+"] >= \""+strStartDate+"\"  && ");
          sbDocWhereClause.append("attribute["+Download.ATTRIBUTE_DOWNLOAD_TIME+"] >= \""+strStartDate+"\"  && ");
        }
        if(strEndDate != null && !"".equals(strEndDate))
        {
          sbBusWhere.append(" from["+Download.RELATIONSHIP_DOWNLOAD_DOCUMENT+"].attribute["+Download.ATTRIBUTE_DOWNLOAD_TIME+"] < \""+strEndDate+"\"  && ");
          sbDocWhereClause.append("attribute["+Download.ATTRIBUTE_DOWNLOAD_TIME+"] < \""+strEndDate+"\"  && ");
        }
        if(strStatus != null && !QUERY_WILDCARD.equals(strStatus) && !"".equals(strStatus))
        {
          sbBusWhere.append(" from["+Download.RELATIONSHIP_DOWNLOAD_DOCUMENT+"].attribute["+Download.ATTRIBUTE_DOWNLOAD_STATUS+"] == \""+strStatus+"\"  && ");
          sbDocWhereClause.append("attribute["+Download.ATTRIBUTE_DOWNLOAD_STATUS+"] == \""+strStatus+"\"  && ");
        }
        if(sbBusWhere.length()>0)
        {
          sbBusWhere.setLength(sbBusWhere.length()- 4);
          sbDocWhereClause.insert(0, Download.RELATIONSHIP_DOWNLOAD_DOCUMENT+"|(");
          sbDocWhereClause.setLength(sbDocWhereClause.length()- 4);
          strDocWhereClause = sbDocWhereClause.append(")").toString();
        }else
        {
          strDocWhereClause = Download.RELATIONSHIP_DOWNLOAD_DOCUMENT;
        }
        Person person = null;
        if(strOriginatorId != null) {
            person = new Person(strOriginatorId);
        }
        String strDownloadHolderId = person.getInfo(context, "from["+Download.RELATIONSHIP_DOWNLOAD_HOLDER+"].to.id");
        if(strDownloadHolderId == null || "".equals(strDownloadHolderId))
        {
          return new MapList();
        }

        DomainObject domDownloadHolder = new DomainObject(strDownloadHolderId);

        StringList slBusSelect = new StringList(6);
        slBusSelect.add(SELECT_OWNER);
        slBusSelect.add("from["+Download.RELATIONSHIP_DOWNLOAD_CONTEXT+"].to.id");
        slBusSelect.add("from["+Download.RELATIONSHIP_DOWNLOAD_CONTEXT+"].to.name");

        slBusSelect.add("from["+strDocWhereClause+"].to.id");
        slBusSelect.add("from["+strDocWhereClause+"].to.name");
        slBusSelect.add("from["+strDocWhereClause+"]."+SELECT_ID);

        mapList = domDownloadHolder.getRelatedObjects(context, Download.RELATIONSHIP_HAS_DOWNLOAD, Download.TYPE_DOWNLOAD,
                        slBusSelect, null, false, true, (short) 1, sbBusWhere.toString(), null);

      }
      else if(strDocumentId != null && !QUERY_WILDCARD.equals(strDocumentId) && !"".equals(strDocumentId))
      {

        if(strStartDate != null && !"".equals(strStartDate))
        {
          sbBusWhere.append("attribute["+Download.ATTRIBUTE_DOWNLOAD_TIME+"] >= \""+strStartDate+"\" && ");
        }
        if(strEndDate != null && !"".equals(strEndDate))
        {
          sbBusWhere.append("attribute["+Download.ATTRIBUTE_DOWNLOAD_TIME+"] < \""+strEndDate+"\" && ");
        }
        if(strStatus != null && !QUERY_WILDCARD.equals(strStatus) && !"".equals(strStatus))
        {
          sbBusWhere.append("attribute["+Download.ATTRIBUTE_DOWNLOAD_STATUS+"] == \""+strStatus+"\" && ");
        }
        //remove the && from the end of the line.
        if(sbBusWhere.length() > 0)
        {
          sbBusWhere.setLength(sbBusWhere.length() - 3);
        }

        StringList slRelSelect = new StringList(1);
        //select owner, downloaded files, download status, and originated.

        slRelSelect.add(SELECT_ID);

        StringList slBusSelect = new StringList(3);
        //select the Part id and its name.
        slBusSelect.add(SELECT_OWNER);
        slBusSelect.add("from["+Download.RELATIONSHIP_DOWNLOAD_CONTEXT+"].to.id");
        slBusSelect.add("from["+Download.RELATIONSHIP_DOWNLOAD_CONTEXT+"].to.name");
        //expand the Document object and get the Download information & Part name & id.
        DomainObject domDocument = new DomainObject(strDocumentId);
       /* ExpansionWithSelect expanSelList = domDocument.expandSelect(context, Download.RELATIONSHIP_DOWNLOAD_DOCUMENT, Download.TYPE_DOWNLOAD, slBusSelect, slRelSelect, true, false, (short)1, "", sbBusWhere.toString(), sQueryLimit, true);
        mapList = FrameworkUtil.toMapList(expanSelList);*/
        
        ContextUtil.startTransaction(context,false);
        ExpansionIterator expIter = domDocument.getExpansionIterator(context,Download.RELATIONSHIP_DOWNLOAD_DOCUMENT, Download.TYPE_DOWNLOAD, slBusSelect, slRelSelect, true, false, (short)1, null, sbBusWhere.toString(), sQueryLimit, true, false, (short) 100, false);
        mapList = FrameworkUtil.toMapList(expIter,sQueryLimit,null,null,null,null);
        expIter.close();
        ContextUtil.commitTransaction(context);
        mapListResult = new MapList(mapList.size());

        String strDocumentLink = "<a href=\"javascript:emxTableColumnLinkClick('../common/emxTree.jsp?emxSuiteDirectory=components&treeMenu=&objectId="+strDocumentId+"', '860', '520', 'false', 'popup')\">"+strDocumentName+"</a>";

        for(int i=0; i<mapList.size(); i++)
        {
          Map map = (Map)mapList.get(i);
          Map resMap = new HashMap(7);

          StringList slPartId = (StringList)map.get("from["+Download.RELATIONSHIP_DOWNLOAD_CONTEXT+"].to.id");
          StringList slPartName = (StringList)map.get("from["+Download.RELATIONSHIP_DOWNLOAD_CONTEXT+"].to.name");
          StringList slId = (StringList)map.get(SELECT_ID);
          StringList slOwner = (StringList)map.get(SELECT_OWNER);
          String strPartId = "";
          String strPartName = "";
          if(slPartId != null)
          {
            strPartName = (String)slPartName.get(0);
          }

          resMap.put(SELECT_ID, (String)slId.get(0));
          resMap.put(SELECT_OWNER, (String)slOwner.get(0));
          resMap.put("PartId", strPartId);
          resMap.put("PartName", strPartName);
          resMap.put("DocumentId", strDocumentId);
          resMap.put("DocumentName", strDocumentName);
          mapListResult.add(resMap);
        }

      }else
      {
        processMapList = true;
        if(strStartDate != null  && !"".equals(strStartDate))
        {
          sbBusWhere.append("attribute["+Download.ATTRIBUTE_DOWNLOAD_TIME+"] >= \""+strStartDate+"\" && ");
        }
        if(strEndDate != null && !"".equals(strEndDate))
        {
          sbBusWhere.append("attribute["+Download.ATTRIBUTE_DOWNLOAD_TIME+"] < \""+strEndDate+"\" && ");
        }
        if(strStatus != null && !QUERY_WILDCARD.equals(strStatus) && !"".equals(strStatus))
        {
          sbBusWhere.append("attribute["+Download.ATTRIBUTE_DOWNLOAD_STATUS+"] == \""+strStatus+"\" && ");
        }
        //remove the && from the end of the line.
        if(sbBusWhere.length() > 0)
        {
          sbBusWhere.setLength(sbBusWhere.length() - 3);
          sbBusWhere.insert(0, "from["+Download.RELATIONSHIP_DOWNLOAD_DOCUMENT+"|(");
          sbBusWhere.append(")]");
        }else
        {
          sbBusWhere.append("from["+Download.RELATIONSHIP_DOWNLOAD_DOCUMENT+"]");
        }


        StringList slBusSelect = new StringList(7);
        //select downloaded files, download status, and originated.
        if(!MULTI_VALUE_LIST.contains("from["+Download.RELATIONSHIP_DOWNLOAD_DOCUMENT+"]."+SELECT_ID))
        {
          MULTI_VALUE_LIST.add("from["+Download.RELATIONSHIP_DOWNLOAD_DOCUMENT+"]."+SELECT_ID);
        }
        if(!MULTI_VALUE_LIST.contains("from["+Download.RELATIONSHIP_DOWNLOAD_DOCUMENT+"].to.name"))
        {
          MULTI_VALUE_LIST.add("from["+Download.RELATIONSHIP_DOWNLOAD_DOCUMENT+"].to.name");
        }
        if(!MULTI_VALUE_LIST.contains("from["+Download.RELATIONSHIP_DOWNLOAD_DOCUMENT+"].to.id"))
        {
          MULTI_VALUE_LIST.add("from["+Download.RELATIONSHIP_DOWNLOAD_DOCUMENT+"].to.id");
        }

        //Note: Explicity added the Owner after adding slBusSelect to MULTI_VALUE_LIST.
        //this Owner no need to be added to MULTI_VALUE_LIST.
        slBusSelect.add(SELECT_OWNER);

        //select the Part and Document id and its name.
        slBusSelect.add("from["+Download.RELATIONSHIP_DOWNLOAD_CONTEXT+"].to.id");
        slBusSelect.add("from["+Download.RELATIONSHIP_DOWNLOAD_CONTEXT+"].to.name");
        slBusSelect.add(sbBusWhere.toString()+".id");
        slBusSelect.add(sbBusWhere.toString()+".to.id");
        slBusSelect.add(sbBusWhere.toString()+".to.name");
        mapList = DomainObject.findObjects(context, Download.TYPE_DOWNLOAD, QUERY_WILDCARD, QUERY_WILDCARD, QUERY_WILDCARD, QUERY_WILDCARD, "", false, slBusSelect);
      }

      if(processMapList)
      {

        mapListResult = new MapList(mapList.size());
        for(int i=0; i<mapList.size(); i++)
        {

          Map map = (Map)mapList.get(i);
          String strOwner = (String)map.get(SELECT_OWNER);
          Object objRelIds = map.get("from["+Download.RELATIONSHIP_DOWNLOAD_DOCUMENT+"]."+SELECT_ID);
          if(objRelIds == null)
          {
            continue;
          }
          StringList slId = null;
          StringList slDocumentNames = null;
          StringList slDocumentIds = null;

          if(objRelIds instanceof StringList)
          {
            slDocumentNames = (StringList)map.get("from["+Download.RELATIONSHIP_DOWNLOAD_DOCUMENT+"].to.name");
            slDocumentIds = (StringList)map.get("from["+Download.RELATIONSHIP_DOWNLOAD_DOCUMENT+"].to.id");
            slId = (StringList)objRelIds;
          }else
          {
            slId = new StringList(1);
            slDocumentNames = new StringList(1);
            slDocumentIds = new StringList(1);

            slId.add((String)map.get("from["+Download.RELATIONSHIP_DOWNLOAD_DOCUMENT+"]."+SELECT_ID));
            slDocumentNames.add((String)map.get("from["+Download.RELATIONSHIP_DOWNLOAD_DOCUMENT+"].to.name"));
            slDocumentIds.add((String)map.get("from["+Download.RELATIONSHIP_DOWNLOAD_DOCUMENT+"].to.id"));
          }

          Object objPartId = map.get("from["+Download.RELATIONSHIP_DOWNLOAD_CONTEXT+"].to.id");
          String strPartLink = "";
          String strPartName = "";
          if(objPartId != null)
          {
            strPartName = (String) map.get("from["+Download.RELATIONSHIP_DOWNLOAD_CONTEXT+"].to.name");

            strPartLink = "<a href=\"javascript:emxTableColumnLinkClick('../common/emxTree.jsp?emxSuiteDirectory=components&treeMenu=&objectId="+(String)objPartId+"', '860', '520', 'false', 'popup')\">"+strPartName+"</a>";
          }

          for(int j=0; j<slId.size(); j++)
          {
            HashMap hashMap = new HashMap(7);
            hashMap.put(SELECT_RELATIONSHIP_ID, (String)slId.get(j));
            hashMap.put("PartId", (String)objPartId);
            hashMap.put("PartName", strPartName);
            String strDocLink =  "<a href=\"javascript:emxTableColumnLinkClick('../common/emxTree.jsp?emxSuiteDirectory=components&treeMenu=&objectId="+slDocumentIds.get(0)+"', '860', '520', 'false', 'popup')\">"+slDocumentNames.get(0)+"</a>";

            hashMap.put("DocumentId", slDocumentIds.get(j));
            hashMap.put("DocumentName", slDocumentNames.get(j));
            hashMap.put(SELECT_OWNER, strOwner);

            mapListResult.add(hashMap);
          }
        }
    }
    //this sets the result map size to requested limit.
    for(int i=mapListResult.size()-1; i>= (int)sQueryLimit; i--)
    {
      mapListResult.remove(i);
    }
    return mapListResult;

  }

}
