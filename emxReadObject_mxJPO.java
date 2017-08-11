 /*
 **  emxReadObject
 **
 **  Copyright (c) 1992-2016 Dassault Systemes.
 **
 **  All Rights Reserved.
 **  This program contains proprietary and trade secret information of
 **  MatrixOne, Inc.  Copyright notice is precautionary only and does
 **  not evidence any actual or intended publication of such program.
 **
 */

 import matrix.db.*;
 import matrix.util.*;
 import java.io.*;
 import java.util.*;

 import com.matrixone.apps.domain.*;
 import com.matrixone.apps.domain.util.*;

 /**
  * The <code>emxReadObject</code> class is to get Object Ids from a range of files and migrate them to 10 minor1 schema.
  *
  * @version AEF 10.0.1.0 - Copyright (c) 2003, MatrixOne, Inc.
  */

public class emxReadObject_mxJPO {


  BufferedWriter writer    = null;
  String documentDirectory = "";
  int minRange = 0;
  int maxRange = 0;
  DomainObject cloneOB = new DomainObject();
  DomainObject doLatest = null;
  DomainObject domainObject = new DomainObject();
  BusinessObject latestObj = new BusinessObject();
  StringList listFileName = null;
  StringList listFileFormat = null;
  String doType = null;
  String objectId = null;
  String ATTRIBUTE_ISVERSIONOBJECT = PropertyUtil.getSchemaProperty("attribute_IsVersionObject");
  final String TYPE_DRAWING_PRINT  = PropertyUtil.getSchemaProperty("type_DrawingPrint");
  Map mapVersion = new HashMap();
  Map mapObjAttr = null;
  MQLCommand mqlCommand = new MQLCommand();


  /**
   * Constructor.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds no arguments
   * @throws Exception if the operation fails
   * @since AEF 10.0.1.0
   */

  public emxReadObject_mxJPO (Context context, String[] args)
      throws Exception
  {
      writer = new BufferedWriter(new MatrixWriter(context));
  }

  /**
   * This method is executed if a specific method is not specified.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds the following input arguments:
   *        0 - String contains the directory of the document
   *        1 - int contains the minimum no of files
   *        2 - int contains the maximum no of files
   * @return an int 0, status code
   * @throws Exception if the operation fails
   * @since AEF 10.0.1.0
   */

  public int mxMain(Context context, String[] args) throws Exception
  {

    if(!context.isConnected()) 
    {
        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        Map paramList = (Map)paramMap.get("paramList");
        String languageStr = (String)paramList.get("languageStr");
        String exMsg = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Message.DesktopClient", new Locale(languageStr));        
        throw new Exception(exMsg);
    } 
       

    if (args.length < 3 ) throw new IllegalArgumentException();
    documentDirectory = args[0];
    minRange = Integer.parseInt(args[1]);

    if ("n".equalsIgnoreCase(args[2])){
      maxRange = getTotalFilesInDirectory();
    } else {
      maxRange = Integer.parseInt(args[2]);
    }

    for(int i = minRange;i <= maxRange; i++){
      ContextUtil.startTransaction(context,true);
      ArrayList objectList = readFiles(i);
      identifyModel(context,objectList);
      ContextUtil.commitTransaction(context);
    }
    writer.close();
    return 0;
    }

  /**
   * This method returns the total number of files in the directory.
   *
   * @return int representing the total no of files in directory
   * @throws Exception if the operation fails
   * @since AEF 10.0.1.0
   */

  public int getTotalFilesInDirectory() throws Exception
  {
    int totalFiles = 0;
    String[] fileNames = null;
    java.io.File file = new java.io.File(documentDirectory);
    if(file.isDirectory()){
    fileNames = file.list();
    } else {
    throw new IllegalArgumentException();
    }
    for (int i=0; i<fileNames.length; i++){
    if(fileNames[i].startsWith("documentobjectids_"))
      totalFiles = totalFiles + 1;
    }
    return totalFiles;
  }

  /**
   * This method reads the contents of the file and puts in Arraylist.
   *
   * @param i integer specifying the suffix of filename to identify the file.
   * @return ArrayList containing the document objectIds
   * @throws Exception if the operation fails
   * @since AEF 10.0.1.0
   */

  public ArrayList readFiles(int i) throws Exception
  {
    ArrayList objectIds = new ArrayList();
    java.io.File file = new java.io.File(documentDirectory + "documentobjectids_" + i + ".txt");
    BufferedReader fileReader = new BufferedReader(new FileReader(file));
    while((objectId = fileReader.readLine()) != null) {
    objectIds.add(objectId);
    }
    return objectIds;
  }


  /**
   * This method identifies the model and invokes the relevant module for migration.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param objectIdList ArrayList object containing the list of document objectIds.
   * @throws Exception if the operation fails
   * @since AEF 10.0.1.0
   */

  public void identifyModel(Context context,ArrayList objectIdList) throws Exception
  {
    StringList objectSelect = new StringList();
    StringList select = new StringList();
    MapList mapList = new MapList();

    select.add(DomainConstants.SELECT_FILE_FORMAT);
    select.add(DomainConstants.SELECT_FILE_NAME);
    select.add(DomainConstants.SELECT_TYPE);
    select.add(DomainConstants.SELECT_FORMAT_HASFILE);

    objectSelect.add("to["+DomainConstants.RELATIONSHIP_VERSION+"].id");
    objectSelect.add(DomainConstants.SELECT_TYPE);
    objectSelect.add(DomainConstants.SELECT_NAME);
    objectSelect.add(DomainConstants.SELECT_ID);
    objectSelect.add("attribute["+DomainConstants.ATTRIBUTE_FILE_VERSION+"]");
    objectSelect.add(DomainConstants.SELECT_FILE_NAME);
    objectSelect.add(DomainConstants.SELECT_FILE_FORMAT);
    objectSelect.add(DomainConstants.SELECT_FORMAT_HASFILE);

   //Iterating the object Ids present in the file
    for(int i=0;i<objectIdList.size();i++)
    {
    objectId = (String)objectIdList.get(i);
    domainObject = DomainObject.newInstance(context);
    domainObject.setId(objectId);
    //getting the latest revision
    mapObjAttr = domainObject.getInfo(context,select);
    doType = (String)mapObjAttr.get(DomainConstants.SELECT_TYPE);

    //Check for the type of the object in file. if it is Document type then get the latest revision of that object.
    //if the objects in the file and latest revision is different skip the current object.
    if(doType.equals(DomainConstants.TYPE_DOCUMENT)){
      latestObj = domainObject.getLastRevision(context);
      doLatest = new DomainObject(latestObj);
      if (!(objectId.equals(doLatest.getId(context)))) continue;
      }
    listFileFormat = (StringList)mapObjAttr.get(DomainConstants.SELECT_FILE_FORMAT);
    listFileName = (StringList)mapObjAttr.get(DomainConstants.SELECT_FILE_NAME);

    //Get all the connected objects (id, type)
      mapList = domainObject.getRelatedObjects(context,DomainConstants.RELATIONSHIP_VERSION,DomainConstants.TYPE_VERSION_DOCUMENT,objectSelect,null,false,true,(short)1,null,null,0);

      if(doType.equals(DomainConstants.TYPE_DOCUMENT)){
      if(mapList.size()==0){
      // the model is TC and Sourcing.
        migrateTeamOrSourcing(context);
      }else{
         //for product central,spec central,program central
         migrateModel2(context, mapList, domainObject);
      }
      }else{
         if(doType.equals(DomainConstants.TYPE_MARKUP) || doType.equals(DomainConstants.TYPE_CAD_MODEL)||doType.equals(DomainConstants.TYPE_CAD_DRAWING)||
       doType.equals(TYPE_DRAWING_PRINT)){
          //for Model 3 (EC) migration
         migrateModel3 (context, domainObject);
         }else{
           //for generic document/document sheet/specification(document central)
         migrateModel2(context, mapList, domainObject);
         }
      }
    }
    }

  /**
   * This method migrates the Team, Sourcing Data to Common Document data model.
   *
   * @param context the eMatrix <code>Context</code> object
   * @throws Exception if the operation fails
   * @since AEF 10.0.1.0
   */

  private void migrateTeamOrSourcing(Context context) throws Exception{

    StringList relVaultedObj = new StringList();
    BusinessObjectItr boItr = null;
    DomainObject objRev = null;
    StringList relSelect = new StringList();
    String strRelId = null;
    Map mapRel = null;
    relSelect.add(DomainConstants.SELECT_RELATIONSHIP_ID);
  //Cloning the current object to create MAster object
    cloneOB = new DomainObject(domainObject.cloneObject(context,null));
  //moving the file from latest revision to Clone object.
    moveFile(context, (String)mapObjAttr.get(DomainConstants.SELECT_FORMAT_HASFILE),cloneOB.getId(context), objectId,(String)listFileFormat.get(0),(String)listFileName.get(0) );

  //Getting the all the relationship id between Parent object and latest revision in both direction

    MapList listToObj = doLatest.getRelatedObjects(context,"*","*",null,relSelect,false,true,(short)1,null,null,0);
    Iterator itrToObj = listToObj.iterator();

    while(itrToObj.hasNext()){
    mapRel = (Map)itrToObj.next();
    strRelId =  (String) mapRel.get(DomainConstants.SELECT_RELATIONSHIP_ID);
    DomainRelationship.setFromObject(context,strRelId,cloneOB);
    }

    MapList listFromObj = doLatest.getRelatedObjects(context,"*","*",null,relSelect,true,false,(short)1,null,null,0);
    Iterator itrFromObj = listFromObj.iterator();

    while(itrFromObj.hasNext()){
    mapRel = (Map)itrFromObj.next();
    strRelId =  (String) mapRel.get(DomainConstants.SELECT_RELATIONSHIP_ID);
    DomainRelationship.setToObject(context,strRelId,cloneOB);
    }
  //Connecting the cloned object to latest revision using Active Version and Latest Version relationship
    cloneOB.addToObject(context,new RelationshipType("Active Version"),doLatest.getId(context));
    cloneOB.addToObject(context,new RelationshipType("Latest Version"),doLatest.getId(context));
  // Updating all revisions with Is Version Object attribute as True.
    boItr = new BusinessObjectItr(latestObj.getRevisions(context));
    while(boItr.next()){
    objRev = new DomainObject(boItr.obj());
    objRev.setAttributeValue(context, ATTRIBUTE_ISVERSIONOBJECT, "True");
    }
  }


  /**
   * This method migrates model 2 data to common document data model.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param list MapList contains list of document objects connected to Current Object
   * @param masterObj DomainObject represents Current Object from ArrayList loaded from file
   * @throws Exception if the operation fails
   * @since AEF 10.0.1.0
   */


  private void migrateModel2 (Context context, MapList list, DomainObject masterObj)throws Exception{

    Map revMap = new HashMap();
    Map highRevMap = new HashMap();
    final String POLICY_VERSION = PropertyUtil.getSchemaProperty(context,"policy_Version");
    DomainObject versionObj = new DomainObject();
    StringBuffer strCommand = null;
    String strId = null;
    String strRev = null;
    String strHighRev = null;
    String strHighVerId = null;
    Iterator itr = null;
    String strName = null;
    StringList listIds = new StringList();
    StringList listVerFileName= new StringList();
    String strVerFileName = null;
    String strRelId = null;
    boolean bSuccess;
    String languageStr = context.getSession().getLanguage();
    //Sorting the version document objects on file version attribute to get the files in sequence
    list.sortStructure("attribute["+DomainConstants.ATTRIBUTE_FILE_VERSION+"]","ascending","string");
    int size = list.size();


    if(size > 0){
    for(int i=0;i<list.size();i++){
      revMap = (Map)list.get(i);
      strId = (String)revMap.get(DomainConstants.SELECT_ID);
      strName = (String)revMap.get(DomainConstants.SELECT_NAME);
      strRev = (String)revMap.get("attribute["+DomainConstants.ATTRIBUTE_FILE_VERSION+"]");
      strRelId = (String)revMap.get("to["+DomainConstants.RELATIONSHIP_VERSION+"].id");
      listIds.add(strId);
      try{
    	  MqlUtil.mqlCommand(context, "modify bus $1 type $2 policy $3 name $4 revision $5",strId,doType,POLICY_VERSION,strName,strRev);   	  
      }catch(Exception ex){
          ContextUtil.abortTransaction(context);
          String exMsg = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.ReadObject.ErrorMessage_Versioning", new Locale(languageStr));          
          throw new FrameworkException(exMsg);  	  
      }
      
      versionObj.setId(strId);
      versionObj.setAttributeValue(context,ATTRIBUTE_ISVERSIONOBJECT, "True");
      DomainRelationship.disconnect(context, strRelId);
      if(i<list.size()-1){
      try{
        listVerFileName=(StringList)revMap.get(DomainConstants.SELECT_FILE_NAME);
        strVerFileName = (String)listFileName.get(0);
      }catch(Exception e){
        strVerFileName = (String)revMap.get(DomainConstants.SELECT_FILE_NAME);
      }
      }
    }
    versionObj.setAttributeValue(context,DomainConstants.ATTRIBUTE_TITLE, strVerFileName);
    for(int i=0;i<listIds.size()-1;i++){
      try{
    	      MqlUtil.mqlCommand(context, "revise bus $1 bus $2",(String)listIds.get(i),(String)listIds.get(i+1));   	  
      }catch(Exception ex){
    	     ContextUtil.abortTransaction(context);
    	     String exMsg = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.ReadObject.ErrorMessage_Revising", new Locale(languageStr));    	     
    	     throw new FrameworkException(exMsg);
     	  
      }
    }
    masterObj.addToObject(context,new RelationshipType("Active Version"),versionObj.getId(context));
    masterObj.addToObject(context,new RelationshipType("Latest Version"),versionObj.getId(context));
    }
    else{
     //Incase if there is no version object connected to Master object(that has file in it)
     //clone the master object and connect using latest/active version.
    cloneObject(context, masterObj);
    }
  }


  /**
   * This method moves the file from one object to another and updates the title attribute.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param hasFile String has value (True/False) whether current object has file
   * @param toId String has document object Id to which file has to be moved
   * @param fromId String has document object Id from which file has to be moved
   * @param format String has format of the file to be moved
   * @param fileName String has name of the file to be moved
   * @throws Exception if the operation fails
   * @since AEF 10.0.1.0
   */

  private void moveFile(Context context, String hasFile, String toId, String fromId, String format, String fileName) throws Exception{
    String languageStr = context.getSession().getLanguage();
    if("True".equalsIgnoreCase(hasFile)){
    try {
    	MqlUtil.mqlCommand(context, "modify bus $1 Title $2 move from $3 format $4 file $5",toId,fileName,fromId,format,fileName);    	
    }catch(Exception ex){
        ContextUtil.abortTransaction(context);
        String exMsg = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.ReadObject.ErrorMessage_Moving", new Locale(languageStr));        
        throw new FrameworkException(exMsg);
    }
    }
  }

  /**
   * This method migrates model 3 data to common document data model.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param masterObj current object from the ArrayList loaded from file
   * @throws Exception if the operation fails
   * @since AEF 10.0.1.0
   */

   private void migrateModel3 (Context context, DomainObject masterObj) throws Exception{
    cloneObject(context, masterObj);
   }

  /**
   * This method clones masterobject based on the no of files in master object and connects
   * the master to clone using Active and Latest Version relationship and also updates the Title and is Version Object
   * attribute in cloned object.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param masterObj DomainObject represents Current Object from ArrayList loaded from file
   * @throws Exception if the operation fails
   * @since AEF 10.0.1.0
   */

  private void cloneObject(Context context, DomainObject masterObj) throws Exception{
	String sSuccess = "";
    StringBuffer strCommand = new StringBuffer(150);
    StringBuffer fileName = new StringBuffer(100);
    boolean bSuccess;
    String hasFile = (String)mapObjAttr.get(DomainConstants.SELECT_FORMAT_HASFILE);
    DomainObject cloneMaster = null;
    String languageStr = context.getSession().getLanguage();

    if("True".equalsIgnoreCase(hasFile)){

    for(int i=0;i<listFileName.size();i++){
    if(i==0){
    fileName.append("'");
    fileName.append((String)listFileName.get(i));
    fileName.append("'");
    }else{
      fileName.append(fileName.toString());
      fileName.append(" ");
      fileName.append("'");
      fileName.append((String)listFileName.get(i));
      fileName.append("'");
    }
    }
   //depending on no. of file Master object is cloned and connected using Active/Latest
    for(int j=0;j < listFileName.size();j++){
      cloneMaster = new DomainObject(masterObj.cloneObject(context,null));
       try{
    	  MqlUtil.mqlCommand(context, "delete bus $1 format $2 file $3",cloneMaster.getId(context),cloneMaster.getInfo(context,"format"),fileName.toString());    	  
      }catch(Exception ex){
    	  ContextUtil.abortTransaction(context);
          String exMsg = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.ReadObject.ErrorMessage_Deleting", new Locale(languageStr));          
          throw new FrameworkException(exMsg);
      }

      cloneMaster.setAttributeValue(context,ATTRIBUTE_ISVERSIONOBJECT, "True");
      cloneMaster.setAttributeValue(context,DomainConstants.ATTRIBUTE_TITLE, (String)listFileName.get(j));
      masterObj.addToObject(context,new RelationshipType("Active Version"),cloneMaster.getId(context));
      masterObj.addToObject(context,new RelationshipType("Latest Version"),cloneMaster.getId(context));
    }
    }
   }

}
