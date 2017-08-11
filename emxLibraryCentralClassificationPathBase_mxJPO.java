/*
 *  emxLibraryCentralClassificationPathBase.java
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 *  static const RCSID [] = "$Id: emxLibraryCentralClassificationPathBase.java.rca 1.18.2.1 Thu Dec 18 04:36:55 2008 ds-arsingh Experimental $";
 */



// TODO: think aobut the case where the Part Family does not have an mxsysInterface associated

import matrix.db.*;
import matrix.util.*;

import java.io.*;
import java.util.*;
import java.util.List;

import com.matrixone.apps.domain.*;
import com.matrixone.apps.common.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.*;
import com.matrixone.apps.common.util.*;
import com.matrixone.apps.library.LibraryCentralCommon;
import com.matrixone.apps.library.LibraryCentralConstants;
import com.matrixone.apps.library.Classification;

/**
 * The <code>emxLibraryCentralClassificationPathBase</code> class.
 * This Class manages the Interface paths  and the Classification Paths and also provides API for Dispaly of the
 * Classification paths.
 *
 * @exclude
 */

public class emxLibraryCentralClassificationPathBase_mxJPO
implements LibraryCentralConstants {

    private int _ifPathCacheHits = 0;
    private int _ifPathCacheMisses = 0;
    private int _clsPathCacheHits = 0;
    private int _clsPathCacheMisses = 0;

    // key=interface name; value=InterfacePath
    public HashMap _ifName2ifPath          = new HashMap();

    // key=interface name; value=ClassificationPath
    public HashMap _ifName2clsPath         = new HashMap();

    // key=strClassId; value=interface name
    public HashMap _clsId2ifName           = new HashMap();

    /**
     * Creates the emxLibraryCentralClassificationPathBase Object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     */
    public emxLibraryCentralClassificationPathBase_mxJPO(Context context,
            String[] args) throws Exception {

    }

    /* Some of the following nested classes may appear to be
     * superfluous, but they make the main class code easier to
     * understand as compared to if it operated on more generic
     * classes like List.
     */

    /**
     * Each String in a List of this type is the name of a classification
     * interface.  The root is first in the least, the leaf last.  The
     * path is NOT meant to include the CLASSIFICATION_TAXONOMIES name, it
     * starts below that.
     */
    public class InterfacePath extends StringList {
    }

    /**
     *  Similar to InterfacePath, but each entry in the List
     *  is a ClassificationInfo
     */
    public class ClassificationPath extends Vector {
    }

    /**
     * Stores some basic info about a Classification.  Not a
     * full DomainObject.
     */
    public class ClassificationInfo {
        String name = null;
        String id = null;


    /**
     * Creates a ClassificationInfo Object given the Object Id and Object Name
     *
     * @param id ObjectId
     * @param Name the Name of the Object
     */
        public ClassificationInfo(String id, String name) {
            setId(id);
            setName(name);
        }
        /**
        *   Gets the ObjectId
        *
        * @return String the ObjectId
        */
        public String getId()              { return id; }

        /**
        * Sets the ObjectId
        *
        * @param id the ObjectId
        */
        public void   setId(String id)     { this.id = id; }

       /**
        * Gets the Object Name
        *
        * @return String the Object Name
        */
        public String getName()            { return name; }

        /**
        * Sets the Object name
        *
        * @param id the object Name
        */
        public void   setName(String name) { this.name = name; }
     }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return int
     * @throws Exception if the operation fails
     * @exclude
     */
    public int mxMain(Context context, String[] args) throws FrameworkException {
        if (!context.isConnected()) {
            throw new FrameworkException("not supported on desktop client");
        }
        return 0;
    }

    /**
     * This Comparator is used for sorting Vectors of ClassificationPaths
     */
    public Comparator _comparePaths = new Comparator() {
        // a and b are ClassificationPaths
        // Note that we are not sorting inside the
        // ClassificationPaths themselves, we are
        // comparing the paths to order them within the
        // list.

       /**
        * Compares two Objects
        *
        * @param _a object
        * @param _b object
        * @return int the Compare result
        */
        public int compare(Object _a, Object _b) {
            ClassificationPath a = (ClassificationPath)_a;
            ClassificationPath b = (ClassificationPath)_b;
            Iterator iterA = a.iterator();
            Iterator iterB = b.iterator();
            while (iterA.hasNext() && iterB.hasNext()) {
                ClassificationInfo elementA = (ClassificationInfo) iterA.next();
                ClassificationInfo elementB = (ClassificationInfo) iterB.next();
                String aName = elementA.getName();
                String bName = elementB.getName();
                int cmp = aName.compareTo(bName);
                if (cmp != 0) {
                    return cmp;
                }
            }
            return  b.size() - a.size();
        }
    };

    /**
     * Returns the InterfacePath representing all nodes from
     * CLASSIFICATION_TAXONOMIES down to the given interfaceName
     *
     * @param context the eMatrix <code>Context</code> object
     * @param interfaceName
     * @return InterfacePath
     * @throws FrameworkException if the operation fails
     */
    public InterfacePath getInterfacePath(Context context, String interfaceName)
            throws FrameworkException {
        InterfacePath result = new InterfacePath();

        // Build a path from CLASSIFICATION_TAXONOMIES (exclusive) to interfaceName
        // BUT keep a cache (HashMap) of interfaces for which the path has
        // already been looked up, to avoid redundant queries. This is important,
        // because within one pageful of data fed to a table,
        // the same paths tend to show up again and again.  The cache needs to
        // be a class instance member variable, to be reused across calls to
        // this method.

        // We could actually cache the parent interface of each individual
        // interface, and build up paths from previously stored elements,
        // but if you need to get even one node in the path, you might
        // as well ask for the whole path, as it's not that much more
        // expensive; interfaces are cached in the core, so require no trips to
        // Oracle. If we were to walk the Subclass relationship instead,
        // then that would be a different story, and caching at the path
        // element level would be a must, as relathioships DO require a trip
        // to the SQL DB every time.

        // First off, check for cached result
        InterfacePath cachedResult = (InterfacePath) _ifName2ifPath.get(interfaceName);
        if (cachedResult != null) {
            _ifPathCacheHits++;
            return cachedResult;
        }

        // Failing, that, query the core
        _ifPathCacheMisses++;

        //    Here's how the query works:
        //    % mql print interface {Wing Screws.1115318266179} select
        //      allparents.name allparents.kindof\[Classification Taxonomies] dump;
        //    Screws.1115318224024,Fasteners.1115318151854,Mechanical
        //    Parts.1115317777426,Classification Taxonomies,Physical Dimensions,
        //    Classification Attribute Groups,Thread,Wing,TRUE,TRUE,TRUE,TRUE,FALSE,
        //    FALSE,FALSE,FALSE
        //
        //    The parents come out in depth-first order, which we will exploit;
        //    if that were not the case, then the query would be more complex,
        //    something like
        //    "print interface Xyz select allparents.derivative"
        //    and would require some tedious parsing.
        //
        //      So, then:
        //    - split the list in half (maybe it could just have been two queries)
        //    - walk both halves in parallel
        //    - skip the FALSEs, keep the TRUEs, and you've built the path to the root
        //    - to build a path downwards from root, walk sublists backward instead

        String strParentsCmd    = "print interface $1 select $2 $3 dump $4";
        String strParentsResult = MqlUtil.mqlCommand(context, strParentsCmd, true,
                                                    interfaceName,
                                                    "allparents.name",
                                                    "allparents.kindof["+DomainConstants.INTERFACE_CLASSIFICATION_TAXONOMIES + "]",
                                                    ","
                                                    ).trim();

        StringList lstParentsAndBooleans = FrameworkUtil.split(strParentsResult, ",");
        int size = lstParentsAndBooleans.size();
        List lstParents = lstParentsAndBooleans.subList(0, size / 2);
        List lstBooleans = lstParentsAndBooleans.subList(size / 2, size);
        // Walk backwards (i--) so ancestors come out earlier than descendants
        // in path
        for (int i = size / 2 - 1; i >= 0; i--) {
            if (lstBooleans.get(i).toString().equalsIgnoreCase("TRUE")) {
                result.add(lstParents.get(i));
            }
        }

        if (!result.isEmpty() && result.get(0).toString().equals(INTERFACE_CLASSIFICATION_TAXONOMIES)) {
            // remove TAXONOMIES root interface
            result.remove(0);
        }

        // add interfaceName as the tail; it is not returned as part of
        // the mql query
        result.add(interfaceName);

        // cache the result
        _ifName2ifPath.put(interfaceName, result);

        //System.out.println("getInterfacePath(" + interfaceName + ") COMPUTED: " + FrameworkUtil.join(result, "->"));
        return result;
    }

    /**
     * Converts an InterfacePath to a ClassificationPath
     *
     * @param context the eMatrix <code>Context</code> object
     * @param interfacePath
     * @return ClassificationPath
     * @throws FrameworkException if the operation fails
     */
    public ClassificationPath convertInterfacePathToClassPath(Context context, InterfacePath interfacePath)
        throws FrameworkException
    {
        ClassificationPath result = new ClassificationPath();

        String tailIf = (String)interfacePath.get(interfacePath.size() - 1);
        // First off, check for cached result
        ClassificationPath cachedResult =
            (ClassificationPath) _ifName2clsPath.get(tailIf);
        if (cachedResult != null) {
            _clsPathCacheHits++;
            return cachedResult;
        }
        _clsPathCacheMisses++;

        // Again, keep a cache (another HashMap) of interface names for which
        // the corresponding class name has already been looked up.
        Map ifName2clsInfo          = new HashMap();
        String getClsCmd        = "temp query bus '$1' $2 $3 where \"$4\" select $5 $6 name dump $7 recordsep $8";
        StringBuffer sbtypes = new StringBuffer(TYPE_LIBRARIES).append(",").append(TYPE_CLASSIFICATION);
        emxLibraryCentralUtil_mxJPO.
            commaPipeQueryToMapSkipTNR(context, getClsCmd, true, ifName2clsInfo,
                                        sbtypes.toString(),
                                        "*",
                                        "*",
                                        "attribute[" + ATTRIBUTE_MXSYS_INTERFACE + "] matchlist '" + FrameworkUtil.join(interfacePath, ",") + "' ','",
                                        "attribute[" + ATTRIBUTE_MXSYS_INTERFACE + "]",
                                        "id",
                                        ",",
                                        "|"
                                        );

        Iterator ifNameIter = interfacePath.iterator();
        while (ifNameIter.hasNext()) {
            String strIfName = (String) ifNameIter.next();
            StringList lstClsFields = (StringList) ifName2clsInfo.get(strIfName);
            String strId = (String) lstClsFields.get(0);
            String strName = (String) lstClsFields.get(1);
            ClassificationInfo cls = new ClassificationInfo(strId, strName);
            result.add(cls);
        }

        for (int i = 0; i < result.size(); i++) {
            ClassificationInfo cls = (ClassificationInfo) result.get(i);
            String clsId = cls.getId();
            String ifName = (String) interfacePath.get(i);
            _clsId2ifName.put(clsId, ifName);
        }

        _ifName2clsPath.put(tailIf, result);
         return result;
    }


    /**
     * Returns the depth of a classificatoin object within its library;
     * e.g. for MechanicalParts=>Fasterners, the depth would be 2
     *
     * @param context the eMatrix <code>Context</code> object
     * @param strClsId
     * @return int the depth of the object
     * @throws Exception if the operation fails
     */
    public int getDepth(Context context, String strClsId)
            throws Exception {
        // once again, must make use of cacheing
        String strIfName = (String) _clsId2ifName.get(strClsId);
        if (strIfName == null) {
            Classification cls = new Classification(strClsId);
            strIfName = cls.getAttributeValue(context, LibraryCentralConstants.ATTRIBUTE_MXSYS_INTERFACE);
        }
        InterfacePath path = getInterfacePath(context, strIfName);
        return path.size();
    }

    /**
     * Returns Vector of ClassificationPath's.  Each ClassificationPath in
     * the Vector represents one classification path for the given endItemId.
     * Each ClassificationInfo in each ClassificatinoPath in the Vector
     * represents an element in the path (a Classification or Libaries)
     * and stores name and id of the classification or lib object. The entire
     * Vector is sorted by the Comparator above.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param endItemId the objectId
     * @return a Vector of ClassificationPath's
     * @throws Exception
     */
     public Vector getEndItemClassificationPaths(Context context, String endItemId)
            throws Exception {

        // get obj's classification interfaces
        StringList lstInterfaces = emxLibraryCentralUtil_mxJPO.
            getClassificationInterfaces(context, endItemId);

        // For each interface, get a path up to CLASSIFICATION_TAXONOMIES (exclusive).
        // Each path is a StringList of interface names, starting with the library's
        // interface, and ending with the interface containing the end item
        Vector vecClsInfoPaths = new Vector(lstInterfaces.size());
        Iterator ifIter = lstInterfaces.iterator();
        while (ifIter.hasNext()) {
            String strInterfaceName = (String) ifIter.next();
            InterfacePath interfacePath = getInterfacePath(context, strInterfaceName);
            ClassificationPath clsInfoPath = convertInterfacePathToClassPath(
                    context, interfacePath);
            vecClsInfoPaths.add(clsInfoPath);
        }

        // Sort the result
        Collections.sort(vecClsInfoPaths, _comparePaths);

        return vecClsInfoPaths;
    }

      /**
      * Returns Vector of ClassificationPath's using relationship 'RELATIONSHIP_SUBCLASS'.
      * Each ClassificationPath in the Vector represents one classification path for the given endItemId.
      * Each ClassificationInfo in each ClassificatinoPath in the Vector 
      * represents an element in the path (a Classification or Libaries)
      * and stores name and id of the classification or lib object. The entire
      * Vector is sorted by the Comparator above.
      *
      * @param context the eMatrix <code>Context</code> object
      * @param endItemId the objectId
      * @return a Vector of ClassificationPath's
      * @throws Exception
      */
      public Vector getEndItemClassificationPathsFromRelationship(Context context, String endItemId)
             throws Exception {
       
         Vector vecClsPaths = new Vector();
         
         try {    
             
             //Get Classification Id(Immediate Parent Id) from selected Classified Item
             HashMap argsHash = new HashMap();
             argsHash.put("objectId", endItemId);
             MapList result = (MapList)JPO.invoke(context, "emxClassification", null, "getClassificationIds", JPO.packArgs(argsHash), MapList.class);
             
             SelectList selectStmts      = new SelectList(3);
             selectStmts.addElement(DomainConstants.SELECT_ID);
             selectStmts.addElement(DomainConstants.SELECT_NAME);
             
             Iterator classListItr      = result.iterator();
             while(classListItr.hasNext())
             {
                 Map objectMap           = (Map) classListItr.next();
                 String ObjectId         = (String)objectMap.get(DomainConstants.SELECT_ID);
                 String className        = (String)objectMap.get(DomainConstants.SELECT_NAME);
                 ClassificationPath classPath = new ClassificationPath();
                 DomainObject domObj     = new DomainObject(ObjectId);
                 if (UIUtil.isNullOrEmpty(className)) {
                     className           = (String)domObj.getInfo(context, DomainConstants.SELECT_NAME);
                     }
                 //Get parent classes/libraries from class id using relationship "RELATIONSHIP_SUBCLASS"
                 MapList parentClasses   = (MapList)domObj.getRelatedObjects(context,
                                         LibraryCentralConstants.RELATIONSHIP_SUBCLASS,  // relationship pattern
                                         LibraryCentralConstants.QUERY_WILDCARD,         // type pattern
                                         selectStmts,        // Object selects
                                         null,               // relationship selects
                                         true,               // from
                                         false,              // to
                                         (short)0,           // expand level
                                         null,               // object where
                                         null,               // relationship where
                                         0);                 // limit                 
                 parentClasses           = parentClasses.sortStructure(context,DomainConstants.SELECT_LEVEL,"descending","emxSortNumericAlphaSmallerBase");
                 //Build Classification Path and append in vector
                 Iterator itr            = parentClasses.iterator();
                 while (itr.hasNext())
                 {
                     Map parentClassMap      = (Map) itr.next();
                     String parentclassName  = (String)parentClassMap.get(DomainConstants.SELECT_NAME);
                     String parentClassId    = (String)parentClassMap.get(DomainConstants.SELECT_ID);
                     ClassificationInfo clsParent = new ClassificationInfo(parentClassId, parentclassName);
                     classPath.add(clsParent);
                 }
                 
                 ClassificationInfo clsObject = new ClassificationInfo(ObjectId, className);
                 classPath.add(clsObject);
                 vecClsPaths.add(classPath);
             }
       } catch (Exception e) {
           throw new Exception (e);
       }
         // Sort the result
        Collections.sort(vecClsPaths, _comparePaths);
        
        return vecClsPaths;
     }


     /**
      * This method differs from getEndItemClassificationPaths in that the former
      * applies to end items, and this one applies to classifications.  Classification
      * objects may have only one path, whereas end items may have any number of them.
      *
      * @param context the eMatrix <code>Context</code> object
      * @param clsObjId the Classification object Id
      * @return ClassificationPath
      */
     public ClassificationPath getClassificationPath(Context context, String strClsObjId)
     throws FrameworkException
     {
         Classification cls = (Classification) DomainObject.newInstance(context, strClsObjId, LIBRARY );
         String strInterfaceName = cls.getInterfaceName(context);
         if (strInterfaceName.trim().equals("")) {
             return new ClassificationPath();
         }
         InterfacePath interfacePath = getInterfacePath(context, strInterfaceName);
         ClassificationPath clsInfoPath = convertInterfacePathToClassPath(context, interfacePath);
         return clsInfoPath;
     }

   /**
     * This method fills in two Vectors of MapLists similar to the one from
     * getClassificationPaths(); the difference is that this one splits the
     * information into two Vectors, the first one is of paths that lead to
     * relevantParentId or below, the second is all other paths. Each Vector is
     * sorted.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param endItemId the end Item OjectId
     * @param relevantParentId parrent OjectId
     * @param relevantPathsOut the path list
     * @param otherPathsOut other paths list
     * @throws Exception if the exception fails.
     */
    public void getPrioritizedClassificationPaths(Context context,
            String endItemId, String relevantParentId, Vector relevantPathsOut,
            Vector otherPathsOut) throws Exception {

        Vector vecAllPaths = getEndItemClassificationPaths(context, endItemId);
        // A path is "relevant" if it passes through (or terminates at)
        // relevantParentId because there is only one path to any class object,
        // the object's position in any path that passes through it is fixed,
        // and we only need to look in that one positionto determine if the path
        // is relevant (passes through that node) or not.
        int depth = getDepth(context, relevantParentId);
        Iterator pathIter = vecAllPaths.iterator();
        while (pathIter.hasNext()) {
            ClassificationPath vecPath = (ClassificationPath) pathIter.next();
            if (vecPath.size() < depth)
                continue; // prevents out-of-bounds
            ClassificationInfo elem = (ClassificationInfo) vecPath.get(depth - 1);
            String strClsId = elem.getId();
            if (strClsId.equals(relevantParentId)) {
                relevantPathsOut.add(vecPath);
            } else {
                otherPathsOut.add(vecPath);
            }
        }
        // because vecAllPaths is already sorted, and we don't disturb the
        // order, each sub-Vector is also already sorted.
    }



    /*************************************************************************/
    /** Members above this point are clean business logic                   **/
    /** Members below are HTML generating for use in programHtmlOutput      **/
    /** (presentation)                                                      **/
    /*************************************************************************/

    // separator between path elements. prefers breaking before arrow, rather
    // than after
    public String  _elemSeparatorHTML      = "<img style=\"padding-left:2px; padding-right:2px;\" src=\"../common/images/iconTreeToArrow.gif\"/>";

    // separator between one path and the next
    public String  _pathSeparatorHTML      = "<br/>";

    //  separator between relevant paths and others
    public String  _relevanceSeparatorHTML = "<br/>";

    // some HTML rendering building blocks


    /**
     * Renders the path element
     *
     * @param strName the Object name
     * @param strObjectId the objectId
     * @param reportFormat the report format
     * @return String the rendered element
     */
    public String renderPathElem(Context context, String strName, String strObjectId, String reportFormat) {
        if ("HTML".equals(reportFormat) || "CSV".equals(reportFormat)) {
            return strName;
        } else {
           //Modified for Bug Id -363882 replace # by javascript:void(0) which was causing error in IE
          return "<a href=\"javascript:void(0)\" " + "onClick=\"javascript:showModalDialog("
            + "'../common/emxTree.jsp?objectId=" + XSSUtil.encodeForURL(context,strObjectId) + "',"
            + "'860','520');\"" + ">" + XSSUtil.encodeForXML(context, strName) + "</a>";
        }
    }


    /**
     * Renders an entire path by rendering its constituent elements
     * separated by the separator HTML
     *
     * @param pathElems the list of elements
     * @param reportFormat the report format
     * @return the rendered path
     */
    public String renderPath(Context context, ClassificationPath pathElems, String reportFormat) {
        StringList elemHtmlList = new StringList();
        Iterator i = pathElems.iterator();
        while (i.hasNext()) {
            ClassificationInfo elem = (ClassificationInfo) i.next();
            elemHtmlList.add(renderPathElem(context, elem.getName(), elem.getId(), reportFormat));
        }
        String elemSeperator = _elemSeparatorHTML;
        if("CSV".equalsIgnoreCase(reportFormat)){
            elemSeperator = "->";
        }
        return FrameworkUtil.join(elemHtmlList, elemSeperator);
    }

    /**
     * Given a vector of ClassificationPath's, render each one with HTML using all appropriate separators
     * @param vecPaths the paths
     * @param forUseInForm not used; was previously needed to allow different rendering in UIForm vs. UITable,
     *  and could still be used for that purpose in a customization
     * @param reportFormat the report format
     * @return String the rendered paths
     */
    public String renderPaths(Context context, Vector vecPaths, boolean forUseInForm, String reportFormat) {
        StringList lstPathHtml = new StringList();
        Iterator pathIter = vecPaths.iterator();
        while (pathIter.hasNext()) {
            ClassificationPath lstPath = (ClassificationPath) pathIter.next();
            lstPathHtml.add(renderPath(context, lstPath, reportFormat));
        }
        String pathSeperator = _pathSeparatorHTML;
        if("CSV".equalsIgnoreCase(reportFormat)){
            pathSeperator = "\n";
        }
        
        String strPathHtml = FrameworkUtil.join(lstPathHtml, pathSeperator);
        return strPathHtml;
    }

    // The methods below are the JPO entry points used by UI components

    /**
     * This method operates on a single object, whose oid is given in args.
     * If that object is a Classification, this method calls getClassificationPath
     * for that object, and then renders the resulting path as HTML.  If the
     * object is not a Classification, then it is treated as an end-item;
     * in that case, this method calls getEndItemClassificationPaths and
     * renders the resulting paths as HTML.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following arguments
     *      0 - objectId the objectId
     *      1 - pfMode printer friendly mode\
     *      2 -
     * @return String the Classification path HTML
     * @throws FrameworkException
     */
    public String getClassificationPathsHTML(Context context, String[] args)
        throws FrameworkException
    {
        try {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            HashMap paramMap = (HashMap) programMap.get("paramMap");
            String objectId = (String) paramMap.get("objectId");
            HashMap requestMap = (HashMap)programMap.get("requestMap");
            String pfMode = (String)requestMap.get("PFmode");
            String reportFormat = pfMode == null ? null : "HTML";
            if (pfMode != null && pfMode.equalsIgnoreCase("true")) {
                reportFormat = "HTML";
            } else {
                reportFormat = null;
            }

            DomainObject domainObject = new DomainObject(objectId);
            if (domainObject.isKindOf(context, TYPE_CLASSIFICATION)) {
                ClassificationPath path = getClassificationPath(context, objectId);
                return renderPath(context, path, reportFormat);
            } else {
                //Vector vecPaths = getEndItemClassificationPaths(context, objectId);
                //Get classification path for classified item using relationship.
                Vector vecPaths = getEndItemClassificationPathsFromRelationship(context, objectId);
                return renderPaths(context, vecPaths, true, reportFormat);
            }
        } catch (Exception e) {
            throw new FrameworkException(e);
        }
    }


    /**
     * This method operates on a list of end-items, with reference to
     * a single classification. It returns classification path info for
     * each end item, but returns it as rendered HTML, one HTML string
     * per end-item, where each HTML lists the "relevant" paths first,
     * then other paths below.  In args, objectId is the Classification
     * object, in reference to which path relevance is judged.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following arguments
     *      0 - objectList the list of Object details
     *      1 - reportFormat the report format
     * @return Vector the with list of paths for each End item with respect to the Classification
     * @throws FrameworkException
     */
    public Vector getPrioritizedClassificationPathsHTMLs(Context context, String[] args)
        throws FrameworkException
    {
        try {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);

            MapList relBusObjPageList = (MapList) programMap.get("objectList");
            HashMap paramMap = (HashMap) programMap.get("paramList");
            String strClsId = (String) paramMap.get("objectId");
            String reportFormat  = (String)paramMap.get("reportFormat");

            Vector vecResult = new Vector();
            Iterator mapItr = relBusObjPageList.iterator();

            while (mapItr.hasNext()) {
                Map map = (Map) mapItr.next();
                String strEndItemId = (String) map.get(DomainConstants.SELECT_ID);
                Vector vecRelevantPaths = new Vector();
                Vector vecOtherPaths = new Vector();
                getPrioritizedClassificationPaths(context, strEndItemId, strClsId,
                        vecRelevantPaths, vecOtherPaths);

                String strRelevantHtml = renderPaths(context, vecRelevantPaths, false, reportFormat);
                String strOtherHtml = renderPaths(context, vecOtherPaths, false, reportFormat);
                String strCombinedHtml = null;

                if (!vecRelevantPaths.isEmpty() && !vecOtherPaths.isEmpty()) {
                    //strCombinedHtml = strRelevantHtml + _relevanceSeparatorHTML + strOtherHtml;
                    strCombinedHtml = strRelevantHtml + _relevanceSeparatorHTML + "<i>" + strOtherHtml + "</i>";
                } else {
                    // Don't render the separator if there's nothing on the other side of it
                    strCombinedHtml = strRelevantHtml + strOtherHtml;
                }

                vecResult.add(strCombinedHtml);
            }
            return vecResult;
        } catch (Exception e) {
            throw new FrameworkException(e);
        }
    }
}

