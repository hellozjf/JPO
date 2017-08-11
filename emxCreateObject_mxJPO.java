/*
 * emxCreateObject
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
import matrix.db.MatrixWriter;
import matrix.util.StringList;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;

import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.MapList;

/**
 * The <code>emxCreateObject</code> class contains methods to get all document type Object Ids.
 *
 * @version AEF 9.5.0.0 - Copyright (c) 2003, MatrixOne, Inc.
 */

public class emxCreateObject_mxJPO {

 BufferedWriter writer = null;
 String documentDirectory = "";

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 9.5.0.0
     */

    public emxCreateObject_mxJPO (Context context, String[] args)
        throws Exception
    {
      writer = new BufferedWriter(new MatrixWriter(context));
    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - chunksize an int that has the no. of objects to be stored in file.
     *        1 - String that holds the documentDirectory.
     * @return an int 0, status code
     * @throws Exception if the operation fails
     * @since AEF 9.5.0.0
     */

    public int mxMain(Context context, String[] args)
        throws Exception
    {
      if(!context.isConnected())
      {
          HashMap paramMap = (HashMap)JPO.unpackArgs(args);
          Map paramList = (Map)paramMap.get("paramList");
          String languageStr = (String)paramList.get("languageStr");
          String exMsg = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Message.DesktopClient", new Locale(languageStr));          
          throw new Exception(exMsg);
      }

      if (args.length < 2 ) throw new IllegalArgumentException();

      int chunkSize = Integer.parseInt(args[0]);
      documentDirectory = args[1];

      getIds(context, chunkSize);
      writer.close();
      return 0;
    }

    /**
     * This method is used to retrieve Documents connected to an object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param chunkSize int that has the no. of objects to be stored in file.
     * @throws Exception if the operation fails.
     * @since AEF 9.5.0.0
     */

  public void getIds(Context context, int chunkSize) throws Exception
    {
      String TYPE_DOCUMENTS  = PropertyUtil.getSchemaProperty(context,"type_DOCUMENTS");
    String objectId = null;
    int counter = 0;
    int fileSequence = 1;
    ArrayList arrayList = new ArrayList();

      if (chunkSize == 0 || chunkSize < 1 ) throw new IllegalArgumentException();

      StringList objectSelect = new StringList();
      objectSelect.add(DomainConstants.SELECT_ID);

      MapList mapList = DomainObject.findObjects(context, TYPE_DOCUMENTS, DomainConstants.QUERY_WILDCARD, null, objectSelect);
      Iterator iterator = mapList.iterator();
      Map map = new HashMap();

      while ( iterator.hasNext()) {
      map = (Map)iterator.next();
      counter = counter + 1;
        arrayList.add((String)map.get(DomainConstants.SELECT_ID));

        if (counter == chunkSize){
        writeFile(arrayList,fileSequence);
        fileSequence = fileSequence + 1;
        counter = 0;
        arrayList.clear();
      }
    }
    if(arrayList.size() > 0)
      writeFile(arrayList,fileSequence);
    }

    /**
     * This method is used to write objectids in a Flatfile.
     *
     * @param objectIds ArrayList that contains list of id's
     * @param sequence int that contains the suffix value of the file.
     * @throws Exception if the operation fails.
     * @since AEF 9.5.0.0
     */


    public void writeFile(ArrayList objectIds, int sequence) throws Exception
    {
    java.io.File file = new java.io.File(documentDirectory + "documentobjectids_" + sequence + ".txt");
    BufferedWriter fileWriter = new BufferedWriter(new FileWriter(file));
    for (int i=0; i < objectIds.size(); i++) {
      fileWriter.write((String)objectIds.get(i));
      fileWriter.newLine();
    }
    fileWriter.close();
    }

}
