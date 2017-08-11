/*
 **
 **   Copyright (c) 2002-2016 Dassault Systemes.
 **   All Rights Reserved.
 **
 **   This JPO contains the implementation of emxFullSearch.
 **
 */
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import matrix.db.BusinessObject;
import matrix.db.BusinessType;
import matrix.db.BusinessTypeList;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.Policy;
import matrix.db.StateRequirement;
import matrix.util.MatrixException;
import matrix.util.StringList;

import com.matrixone.apps.cache.CacheManager;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkProperties;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.UOMUtil;
import com.matrixone.apps.domain.util.VaultUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.framework.ui.UICache;
import com.matrixone.apps.framework.ui.UIComponent;
import com.matrixone.apps.framework.ui.UINavigatorUtil;
import com.matrixone.apps.framework.ui.UISearch;
import com.matrixone.apps.framework.ui.UISearchUtil;
import com.matrixone.apps.framework.ui.UITableCommon;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.json.JSONArray;
import com.matrixone.json.JSONObject;
import com.matrixone.jsystem.util.MxMatcher;
import com.matrixone.jsystem.util.MxPattern;
import com.matrixone.search.AccessRefinement;
import com.matrixone.search.AttributeDimension;
import com.matrixone.search.AttributeRefinement;
import com.matrixone.search.AutonomySearch;
import com.matrixone.search.CaseSensitiveAttributeRefinement;
import com.matrixone.search.CaseSensitiveMatrixSearch;
import com.matrixone.search.ComplexRefinement;
import com.matrixone.search.MatrixSearch;
import com.matrixone.search.RecordCount;
import com.matrixone.search.ReferenceRefinement;
import com.matrixone.search.Search;
import com.matrixone.search.SearchRecord;
import com.matrixone.search.SearchRefinement;
import com.matrixone.search.SearchResult;
import com.matrixone.search.SortRefinement;
import com.matrixone.search.Suggestion;
import com.matrixone.search.TagDimension;
import com.matrixone.search.TagRefinement;
import com.matrixone.search.Taxonomy;
import com.matrixone.search.TaxonomyDimension;
import com.matrixone.search.TaxonomyRefinement;
import com.matrixone.search.TextRefinement;
import com.matrixone.search.XLSearch;
import com.matrixone.search.index.Config;
import com.matrixone.search.index.Config.Field;

/**
 * The <code>emxFullSearchBase</code> class contains implementation code for
 * emxFullSearch.
 *
 * @version AEF X+3
 */
public class emxAEFFullSearchBase_mxJPO {
    protected static final String LANG_RESOURCE_FILE = "emxFrameworkStringResource";

    // Prefix used in BOTH language file and emxSystem.properties
    protected static final String FTS_RES_KEY_PREFIX = "emxFramework.FullTextSearch.";

    // NOTE: SEARCH_INCL_OIDS_MAP and SEARCH_EXCL_OIDS_MAP are caches
    // indexed by timestamp. After entries are made in this map, they
    // are never cleared or modified over the life of one FST window.
    // They are cleared when the window is closed.
    protected static final HashMap SEARCH_INCL_OIDS_MAP = new HashMap();

    protected static final HashMap SEARCH_EXCL_OIDS_MAP = new HashMap();

    protected static final HashMap SEARCH_RESULTS_MAP = new HashMap();

    protected static final HashMap STATE_TRANSLATIONS_MAP = new HashMap();

    protected static final HashMap STATE_SCHEMA_MAP = new HashMap();

    protected static final HashMap TYPE_SCHEMA_MAP = new HashMap();

    protected static final HashMap TYPE_CHOOSER_TRANSLATIONS_MAP = new HashMap();

    protected static final HashMap STATE_CHOOSER_TRANSLATIONS_MAP = new HashMap();

    protected static final HashMap VAULT_CHOOSER_TRANSLATIONS_MAP = new HashMap();

    protected static final HashMap TAXONOMIES_MAP = new HashMap();

    protected static final int JSON_INDENT_FACTOR = 2;

    private static final Map<String, Set<String>> TYPES_TAXONOMY_MAP =  new HashMap<String, Set<String>>();

    private static final Map<String, MapList> TYPES_POLICIES_MAP =  new HashMap<String, MapList>(5);

    protected static boolean noObjectsFound = false;

	private static Map<String,Object> _searchRecordsMap = Collections.synchronizedMap(new HashMap<String,Object>(1));

    private static final org.htmlcleaner.HtmlCleaner _localHtmlCleaner;
    static{
    	org.htmlcleaner.CleanerProperties localCleanerProperties = new org.htmlcleaner.CleanerProperties();
    	_localHtmlCleaner = new org.htmlcleaner.HtmlCleaner(localCleanerProperties);
    }

    // control some debug output
    protected int MAX_DIMS_OUTPUT = 0;
    protected int TAIL_DIMS_OUTPUT = 5;

    //Tag Type
    public static final String TAG_TYPE_IMPLICIT = "implicit";
    public static final String TAG_TYPE_EXPLICIT = "explicit";

  //protected String _excludeoids = null;
  //protected String _includeoids = null;
    protected Config _config = null;

    protected JSONObject buildJsonForError(Throwable e, String languageStr) throws Exception {
        StringBuffer msgBuf = new StringBuffer();
        if (isMalformedTextSearchError(e, msgBuf, languageStr)) {
            JSONObject res = new JSONObject();
            res.put("ERROR", msgBuf.toString());
            res.put("CLASS", "MALFORMED_TEXT_SEARCH");
            return res;
        } else {
            return UISearchUtil.buildJsonForException(e);
        }
    }

    protected boolean isMalformedTextSearchError(Throwable e, StringBuffer msgBuf, String languageStr) throws Exception {
        StringList codes = FrameworkUtil.split("502,504,505,508,509,512", ",");
        String eStr = e.toString();
        eStr = FrameworkUtil.findAndReplace(eStr,"\n","");
        eStr = FrameworkUtil.findAndReplace(eStr,"\t","");
        //MxPattern p = MxPattern.compile(".*<errorid>AXE[A-Z]+([0-9]+)</errorid>.*", MxPattern.DOTALL);
        MxPattern p = MxPattern.compile(".*<errorid>AXE[A-Z]+([0-9]+)</errorid>.*");
        MxMatcher m = p.matcher(eStr);
        if (m.matches()) {
            //MatchResult mr = m.toMatchResult();
            String errNum = m.group(1);
            if (codes.contains(errNum)) {
                if (msgBuf != null) {
                    String msg1 = i18nNow.getI18nString(FTS_RES_KEY_PREFIX + "InvalidTextSearch", LANG_RESOURCE_FILE, languageStr);
                    msgBuf.append(msg1);
                    msgBuf.append(":\n");
                    String msg2 = i18nNow.getI18nString(FTS_RES_KEY_PREFIX + "AXEErrorCode." + errNum, LANG_RESOURCE_FILE, languageStr);
                    msgBuf.append(msg2);
                }
                return true;
            }
        }
        return false;
    }

    protected String getSearchClassificationRefinmenent(Context context, SearchRefinement[] refinements) {
        //367327
        try {
            StringList slTaxonomy = FrameworkProperties.getTokenizedProperty(context,"emxFramework.FullTextSearch.Libraries", ",");
        for (int i = 0; i < refinements.length; i++) {
            if (refinements[i] instanceof TaxonomyRefinement) {
                TaxonomyRefinement tr = (TaxonomyRefinement) refinements[i];
                    if (slTaxonomy.contains(tr.getName())) {
                    return tr.getValue();
                }
            }
        }
        } catch (FrameworkException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected String getSearchTypeRefinement(SearchRefinement[] refinements) {
//      System.out.println("START getSearchTypeRefinement");
        // Precedence goes to TYPES refinement
        for (int i = 0; i < refinements.length; i++) {
            if (refinements[i] instanceof TaxonomyRefinement) {
                TaxonomyRefinement tr = (TaxonomyRefinement) refinements[i];
                if (tr.getName().equals("TYPES")) {
                        return tr.getValue();
            }
        }
    }
//  System.out.println("SECOND CHANCE");

    // Second chance: is TYPE refinement used?
    for (int i = 0; i < refinements.length; i++) {
        if (refinements[i] instanceof AttributeRefinement) {
            AttributeRefinement tr = (AttributeRefinement) refinements[i];
            if (tr.getName().equals("TYPE")) {
                    return tr.getValue();
            }
        }
    }
//  System.out.println("BUMMER");

        // No TYPES or TYPE refinement found
        return null;
    }


    /**
     * Performs a generic search.
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            contains a packed HashMap with the following entries: field -
     *            String of the fields to search. txtTextSearch - Strng of the
     *            Text to search. txtExcludeOIds - Comma separated string of
     *            object ids to exclude from the search results
     *            excludeOIDProgram - program:function JPO that should return
     *            the StringList of objectids to exclude
     * @return a String array of object ids.
     * @throws Exception
     *             if the operation fails.
     * @since AEF 10.7.3
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList search(Context context, String[] args) throws Exception {
        try {
            long b = System.currentTimeMillis();
            HashMap params = (HashMap) JPO.unpackArgs(args);
            // LQA : Fix for bug --375532
            String languageStr = (String)params.get("languageStr");
            int limit = UISearchUtil.getQueryLimit(context,params);
            StringBuffer sbObjLimitWarning = new StringBuffer();
            sbObjLimitWarning.append(EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",new Locale(languageStr),"emxComponents.Warning.ObjectFindLimit"));
            sbObjLimitWarning.append(limit);
            sbObjLimitWarning.append(EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",new Locale(languageStr),"emxComponents.Warning.Reached"));
            //
            MapList returnMapList = new MapList();
            String fullTextSearchTimestamp = (String)params.get("fullTextSearchTimestamp");
			String sbTimestamp = (String)params.get("timeStamp");
            String fromFormSearch = (String)params.get("fromFormSearch");
            String checkStoredResult = (String)params.get("checkStoredResult");
			boolean canShowSnippets = UISearchUtil.canShowSnippets(context, params);
            if("true".equalsIgnoreCase(checkStoredResult)){
                returnMapList = (MapList)SEARCH_RESULTS_MAP.get(fullTextSearchTimestamp);
                if (UISearchUtil.isDebug(params)){
                    System.out.println("--- search() returning cached result ---");
                }
                if("true".equalsIgnoreCase(fromFormSearch) && returnMapList!= null){
                    //If user closes the search window before the results are loaded, then SEARCH_RESULTS_MAP will not cleared
                    //in this scenario we may need to initialise it again
                    //SEARCH_RESULTS_MAP = new HashMap();
                    SEARCH_RESULTS_MAP.remove(fullTextSearchTimestamp);
                    return returnMapList;
                }else if(returnMapList != null){
                    MapList clonedResultMapList = new MapList(returnMapList);
                    return clonedResultMapList;
                }
                //in case of empty results, if user reloads the page again
                if(returnMapList == null){
                    returnMapList = new MapList();
                }
            }

            if ("true".equals(params.get("txtNoSearch"))) {
                MapList res = new MapList();
                res.add(new Integer(0));
                if (UISearchUtil.isDebug(params)){
                    System.out.println("--- search() txtNoSearch ---");
                }

                return res;
            }

            SearchRefinement refinements[] = GetSearchRefinements(context, params);

	        /* Added for IR-017381 */
	        /* if no vault refinement is passed in autonomy search,*/
	        /* search within vaults as set in user prefereces */

	        ArrayList tempRefinementList = new ArrayList();
	        boolean hasVaultRefinement = false;

			for (int i = 0; i < refinements.length; i++) {
				tempRefinementList.add(refinements[i]);
				if(refinements[i].toString().indexOf("BOVAULT") > -1) {
					hasVaultRefinement = true;
					break;
				}
			}
			if(!hasVaultRefinement) {
				// get user preference vault
				String vaultDefaultSelection = PersonUtil.getSearchDefaultVaults(context);

                if(vaultDefaultSelection == null || "".equals(vaultDefaultSelection)) {
                   vaultDefaultSelection = EnoviaResourceBundle.getProperty(context, "emxFramework.DefaultSearchVaults");
                }

                AttributeRefinement vaultRefinement = new AttributeRefinement("BOVAULT", vaultDefaultSelection, AttributeRefinement.OPERATOR_EQUAL);

                tempRefinementList.add(vaultRefinement);
                refinements = (SearchRefinement[]) tempRefinementList.toArray(new SearchRefinement[tempRefinementList.size()]);

                if (UISearchUtil.isDebug(params)) {
                    System.out.println("------------------\nREFINEMENTS");
                    for (int i = 0; i < refinements.length; i++) {
                        System.out.println("  " + i + ": " + refinements[i].toString().trim());
                    }
                }
			}

	        /* end IR-017381 fix */

            Search search = newSearchInstance(context, params);
            //LQA: declaring the variable above
            //int limit = UISearchUtil.getQueryLimit(params);
            if (!UISearchUtil.isAutonomySearch(context,params)) {
                MatrixSearch mxSearch = (MatrixSearch) search;
                mxSearch.setFindLimit(limit);
            search.setPageSize(limit);
            } else {
                String showPagination = EnoviaResourceBundle.getProperty(context, "emxFramework.FullTextSearch.ShowPagination");
                String pgAction = (String) params.get("pgAction");
                if(pgAction == null || "".equalsIgnoreCase(pgAction)) {
                    pgAction = "On";
                }
                if(showPagination !=null && "true".equalsIgnoreCase(showPagination) && !"Off".equalsIgnoreCase(pgAction)) {
                    int ipageSize = UISearchUtil.getPageSize(context);
                    //For Bug 366863
                    int pageSize = ipageSize;
                    try {
                        String pgRange = (String)params.get("paginationRange");
                        pageSize = Integer.parseInt(pgRange);
                    } catch (Exception e) {
                        pageSize = pageSize;
                    }
                    String spageSize = (String)params.get("pageSize");
                    if(UIUtil.isNotNullAndNotEmpty(spageSize)){
                    	pageSize = Integer.parseInt(spageSize);
                    }
                    search.setPageSize(pageSize);
                } else {
                    search.setPageSize(limit);
                }
            }

            String includeOIDprogram = (String) params.get(UISearchUtil.INCLUDEOIDPROGRAM);
          //Added for Autonomy search fill pages feature
            boolean isFillPages = UISearchUtil.isFillPagesMode(context, params);

              if ((includeOIDprogram != null && !"".equals(includeOIDprogram) && !"null".equals(includeOIDprogram))
                      && "".equals(getIncludeList(context, params))) {
                  MapList emptyMapList = new MapList();
                  emptyMapList.add(new Integer(0));
                   if(isFillPages){
                	   emptyMapList.add(new Integer(0));
                   }
                  return emptyMapList;
              }



            String currentPage = (String) params.get("currentPage");
            if(currentPage == null || "".equals(currentPage) || "0".equals(currentPage)) {
                currentPage = "1";
            }
            int intcurpage = Integer.parseInt(currentPage);
            SearchResult result = null;


            try {
            	if(UISearchUtil.isAutonomySearch(context,params) && isFillPages) {
            		String curIndex = (String)params.get("curFTSIndex");
            		if(curIndex == null || "".equals(curIndex) || "0".equals(curIndex)) {
            			curIndex = "0";
                    }
            		int curFTSIndex = Integer.parseInt(curIndex);
            		result = ((AutonomySearch) search).searchEx(context, refinements, false, curFTSIndex+1);
            	} else {
				    if(canShowSnippets){
            			result = ((XLSearch)search).search(context, refinements, intcurpage-1);
            		}else{
                	    result = search.search(context, refinements, intcurpage-1);
				    }
            	}
            } catch (Exception e) {
                // Bug nnnnnn  : exceptions thrown if malformed search expression
                // This is not the right way to do it; api should make this more accessible
            	//IR-058044V6R2011x
            	StringBuffer strBuf = new StringBuffer();
                if (isMalformedTextSearchError(e, strBuf, (String)params.get("languageStr").toString())) {
                    // Treat this as simple no-results
                    System.out.println(e.toString());
                    returnMapList.add(new Integer(0));
                    if (UISearchUtil.isDebug(params)){
                        System.out.println("--- search() " + e.toString() + "---");
                    }

                    //IR-058044V6R2011x
                    //return returnMapList;
                    throw new FrameworkException(strBuf.toString());
                } else {
                    throw e;
                }
            }

            SearchRecord records[] = result.getRecords();
            //FIX for IR-029760V6R2011
            /*int recordCount = 0;
            if(records != null) recordCount = records.length;
            returnMapList.add(new Integer(recordCount));*/
            //FIX for IR-029760V6R2011
            returnMapList.add(new Integer(result.getTotalCount()));

            //Added for Autonomy search fill pages feature
            if(UISearchUtil.isAutonomySearch(context,params) && isFillPages && !"false".equalsIgnoreCase(UIUtil.getValue(params, "showWarning"))) {
            	// Add Correct Search index to be used in next search into returnMapList.
            	returnMapList.add(new Integer(result.getFTSIndex()));

            	// Below code is added for Internationlized Message when result count is less than expected
            	int ftsIndex = result.getFTSIndex();
            	int totCount = result.getTotalCount();
                if (records != null && records.length == 0) {
            		if(ftsIndex != totCount) {
            			StringBuffer strWarningMessage = new StringBuffer(64);
            			strWarningMessage.append (EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", new Locale(languageStr), "emxFramework.FullTextSearch.FillPages.NoResultFound"));
            			MqlUtil.mqlCommand(context, "WARNING $1",strWarningMessage.toString());
            		} else {
            			// End of results reached
            			StringBuffer strWarningMessage = new StringBuffer(64);
            			strWarningMessage.append (EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", new Locale(languageStr), "emxFramework.Range.Regression.No"));
            			strWarningMessage.append(" ");
            			strWarningMessage.append (EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", new Locale(languageStr), "emxFramework.FullTextSearch.FillPages.Warning.EndOfResult"));
            			MqlUtil.mqlCommand(context, "WARNING $1",strWarningMessage.toString());
            		}
            	}
            	else if(records != null && records.length < search.getPageSize())
            	{
            		String curIndex = (String)params.get("curFTSIndex");
            		if(curIndex == null || "".equals(curIndex) || "0".equals(curIndex)) {
            			curIndex = "0";
            		}
            		int curFTSIndex = Integer.parseInt(curIndex);
            		if(ftsIndex != totCount) {
            			StringBuffer strWarningMessage = new StringBuffer(64);
            			strWarningMessage.append(records.length);
            			strWarningMessage.append(" ");
            			strWarningMessage.append (EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", new Locale(languageStr), "emxFramework.FullTextSearch.FillPages.Warning"));
            			MqlUtil.mqlCommand(context, "WARNING $1",strWarningMessage.toString());
            		} else if(curFTSIndex+records.length < totCount) {
            			StringBuffer strWarningMessage = new StringBuffer(64);
            			strWarningMessage.append(records.length);
            			strWarningMessage.append(" ");
            			strWarningMessage.append (EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", new Locale(languageStr), "emxFramework.FullTextSearch.FillPages.Warning.EndOfResult"));
            			MqlUtil.mqlCommand(context, "WARNING $1",strWarningMessage.toString());
                 }
            }
            }

            HashMap objInfo = null;
            if(records != null){
                updateSearchRecordsMap(context, sbTimestamp, records, returnMapList, canShowSnippets);
            }

            // workaround for Bug 352765

            if (records != null && records.length == limit && !UISearchUtil.isAutonomySearch(context,params)
            		&& !"false".equalsIgnoreCase(UIUtil.getValue(params, "showWarning"))) {
               // MqlUtil.mqlCommand(context, "WARNING 'Object Find Limit (" + limit +  ") Reached'");
               // Fix for bug 375532
                //MqlUtil.mqlCommand(context, "WARNING \""+sbObjLimitWarning.toString()+"\"");
            	MqlUtil.mqlCommand(context, "WARNING $1",sbObjLimitWarning.toString());
                // Fix ends.
            }
            ContextUtil.commitTransaction(context);

            if (UISearchUtil.isTimingMode(params)) {
                System.out.println("search(): " + (System.currentTimeMillis() - b) + "ms");
            }

            return returnMapList;
        } catch (Exception e) {
            // This particular method does not build JSON, so exceptions
            // are just printed out to stdout
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Performs  generic search when user clicks on a chooser in Autonomy search, where chooserURL is configured.
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            contains a packed HashMap.
     * @returns a HashMap
     * @throws Exception
     *             if the operation fails.
     */
    public HashMap getSearchResults(Context context, String[] args) throws Exception {
        try {
            long b = System.currentTimeMillis();
            HashMap params = (HashMap) JPO.unpackArgs(args);
            HashMap returnHashMap = new HashMap();
            String fullTextSearchTimestamp = (String)params.get("fullTextSearchTimestamp");
            MapList resultMapList = new MapList();
            resultMapList = search(context,args);
            Integer objectCount = (Integer)resultMapList.get(0);
            returnHashMap.put("objectCount",objectCount);
            returnHashMap.put("fullTextSearchTimestamp",fullTextSearchTimestamp);
            if(objectCount.intValue()>1){
                SEARCH_RESULTS_MAP.put(fullTextSearchTimestamp,resultMapList);
                returnHashMap.put("searchResult",new MapList());
            }else if(objectCount.intValue()==1){
                HashMap object = (HashMap)resultMapList.get(1);
                String OID = (String)object.get("id");
                String dispName = "";
                if(!UIUtil.isNullOrEmpty(OID)){
                 dispName = MqlUtil.mqlCommand(context,"print bus $1 select $2 dump ",OID,"name");
                }
                object.put("displayName",dispName);
                resultMapList.add(1,object);
                returnHashMap.put("searchResult",resultMapList);

            }else{
                MapList emptyMapList = new MapList();
                emptyMapList.add(new Integer(0));
                SEARCH_RESULTS_MAP.put(fullTextSearchTimestamp,emptyMapList);
                returnHashMap.put("searchResult",new MapList());
            }
            if (UISearchUtil.isTimingMode(params)) {
                System.out.println("search(): " + (System.currentTimeMillis() - b) + "ms");
            }
            return returnHashMap;
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            throw e;
        }
    }

    protected Search newSearchInstance(Context context, HashMap params) throws Exception {
        if (UISearchUtil.isAutonomySearch(context, params)) {
            Search asearch;
        	if(UISearchUtil.canShowSnippets(context, params)){
        		asearch = new XLSearch(context);
        		((XLSearch)asearch).setLanguage(getLocale(params));
        	}else{
        		asearch = new AutonomySearch(context);
        		((AutonomySearch)asearch).setLanguage(getLocale(params));
        	}
            return asearch;
        }else if(UISearchUtil.isCaseSensitiveSearch(context, params)){
            CaseSensitiveMatrixSearch msearch = new CaseSensitiveMatrixSearch();
            return msearch;
        }else{
            MatrixSearch msearch = new MatrixSearch(context);
            return msearch;
        }
    }
    /**
     * Returns the type taxonmy
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            contains a packed HashMap of request parameters
     * @throws Exception
     *             if the operation fails.
     * @since AEF 10.7.3
     */
    public String getDynaTaxonomyCounts(Context context, String[] args) throws Exception {
        HashMap params = (HashMap) JPO.unpackArgs(args);
        JSONObject finalResultobj = new JSONObject();
        JSONArray finArr = new JSONArray();
        try {
            ContextUtil.startTransaction(context, false);
            String languageStr = (String) params.get("languageStr");
            SearchRefinement refinements[] = GetSearchRefinements(context, params);

            HashMap returnMap = new HashMap();
            String fullTextSearchTimestamp = (String)params.get("fullTextSearchTimestamp");
            HashMap cacheMap = (HashMap)TAXONOMIES_MAP.get(fullTextSearchTimestamp);
            if(cacheMap == null){
                cacheMap = new HashMap();
            }

            Search search = newSearchInstance(context, params);
            long b = System.currentTimeMillis();
            boolean includeCounts  = (UISearchUtil.isAutonomySearch(context,params) && UISearchUtil.includeCounts(params));
            String collectionName = (String) params.get("COLLECTION");
            if (canUse(collectionName)) {
            	includeCounts = true;
            }
            Taxonomy taxonomies[] = null;
            if(params.get("parentTaxonomy") != null && cacheMap.get("TAXONOMIES") != null){
                taxonomies = (Taxonomy[])cacheMap.get("TAXONOMIES");
            }else{
                taxonomies = search.getTaxonomies(context, refinements, includeCounts);
                cacheMap.put("TAXONOMIES",taxonomies);
            }

            if (UISearchUtil.isTimingMode(params)) {
                System.out.println(" getTaxonomies(): " + (System.currentTimeMillis() - b) + "ms");
            }
            boolean canHaveSuggestions = false;
            String isDidYouMean = EnoviaResourceBundle.getProperty(context, "emxFramework.FullTextSearch.DidYouMean");
            JSONObject jsonFilters = extractFilters(params);
            if(filterExists(jsonFilters, UISearchUtil.TEXTSEARCH) && UISearchUtil.isAutonomySearch(context,params) && !isDidYouMean.matches("false")){
                if(includeCounts == true && taxonomies.length == 0){
                    canHaveSuggestions = true;
                }else if(includeCounts == false){
                    canHaveSuggestions = true;
                }
            }
            //added to fix Did You mean
			if (canHaveSuggestions) {
                JSONObject jsonSugg = getSuggestions(context, refinements, params);
                if (jsonSugg != null) {
                    return jsonSugg.toString();
                }
            }
            for (int i = 0; i < taxonomies.length; i++) {
                Taxonomy t = taxonomies[i];
                String singleTaxonomy = (String)params.get("singleTaxonomy");
                if (canUse(singleTaxonomy)) {
                    if (!singleTaxonomy.equalsIgnoreCase(t.getName())) {
                        continue;
                    }
                }
                if (UISearchUtil.isDebug(params)) {
                    dumpTaxonomy(context, t, -1, 0);
                }
                String lineage = t.getName();
                TaxonomyDimension[] tds = t.getDimensions();
				//for Interface linege, refinement should not be created
                if(isTaxonomyHidden(context, lineage)){
                    continue;
                }
				//add as a new Taxonomy is added in Search crawler
				if(lineage!=null && lineage.equals("bo.RDFClasses"))
                {
                	continue;
                }
                if (tds != null) {
                    UISearchUtil searchUtil = new UISearchUtil();
                    String paramFields = (String) params.get("field");
                    Hashtable fieldsMap = searchUtil.getFields(paramFields,UISearchUtil.getFieldSeperator(context,params));
                    StringList fieldList = new StringList();
                    if(UISearchUtil.FIELD_TYPES.equals(lineage)){
                        String defaultFields = (String) params.get("default");
    					if (canUse(defaultFields)) {
                            defaultFields = com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(defaultFields);
                        }
    					/*Finding param types Start*/
                        if (canUse(paramFields)) {
                            paramFields = com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(paramFields);
                        }
                        Hashtable defaultsMap = searchUtil.getFields(defaultFields,UISearchUtil.getFieldSeperator(context,params));

    					String txtType = (String) defaultsMap.get(UISearchUtil.FIELD_TYPES);
                        if(!canUse(txtType)){
    						txtType = (String) fieldsMap.get(UISearchUtil.FIELD_TYPES);
    					}

                        StringList defaultTypes = new StringList();
                        if (canUse(txtType)) {
                            StringList tempTypeList = FrameworkUtil.split(txtType, "^");
                            for (int itr = 0; itr < tempTypeList.size(); itr++) {
                                String tempType = (String)tempTypeList.get(itr);
                                StringList typeList = FrameworkUtil.split(tempType, "|");
                                String operator = (String) typeList.get(0);
                                boolean bEqOpr = (getOperator(operator) == AttributeRefinement.OPERATOR_EQUAL);
                                if (bEqOpr) {
                                    txtType = (String) typeList.get(1);
                                }
                            }
                            defaultTypes = FrameworkUtil.split(txtType, ",");
                        }
    			        if(defaultTypes.size() > 0) {
                            for (int p = 0; p < defaultTypes.size(); p++) {
                                String strTypeName = (String) defaultTypes.get(p);
                                if(strTypeName.startsWith("type_")){
                                    strTypeName = PropertyUtil.getSchemaProperty(context, strTypeName);
                                }
                                fieldList.add(strTypeName);
                            }
                        }
                    }else{
                    	String txtTaxValue = (String) fieldsMap.get(lineage);
    		        	if(txtTaxValue != null){
    		        		StringList taxList = FrameworkUtil.split(txtTaxValue, "|");
    		        		txtTaxValue = (String)taxList.get(1);
    		        		fieldList = FrameworkUtil.split(txtTaxValue, ",");
    		        	}
                    }
                    	    JSONArray resultArr  = new JSONArray();
                            for (int p = 0; p < tds.length; p++) {
                            	JSONObject resultobj = new JSONObject();
                            	String typeName = tds[p].getTaxonomy().getName();
                            	resultobj.put("type", typeName);
                             	if(fieldList.size() > 0 && !fieldList.contains(typeName)){
                                     resultobj.put("expand", true);
                                     resultobj.put("select", false);
                            	}else{
                                	boolean isSelected = Boolean.valueOf(filterExists(jsonFilters, lineage, typeName, "string", false));
                                    resultobj.put("select", isSelected);
                                    resultobj.put("expand", isSelected);
                            	}
                            	if (lineage.equals(UISearchUtil.FIELD_TYPES)) {
                            		resultobj.put("title", i18nNow.getTypeI18NString(typeName,languageStr)+"("+tds[p].getCount()+")");
									if(canUse((String)fieldsMap.get(UISearchUtil.FIELD_TYPES))){
										resultobj.put("hideCheckbox", true);
									}
                            	}else{
                            		try {
                            			resultobj.put("title", MqlUtil.mqlCommand(context,"print bus $1 select $2 dump ",typeName,"name")+"("+tds[p].getCount()+")");
                            		}catch(MatrixException me){
                            			//context is not having the access or object does not exist
                            			continue;
                            		}
                            	}
                            	resultobj.put("taxonomyType",lineage);
                            	resultobj.put("children", new JSONArray());
                            	resultArr.put(resultobj);
                            	buildDynaTaxonomyTree(context, tds[p], refinements, t.getId(), lineage, params, resultobj, resultArr,fieldList);
                            }
                            if(resultArr.length() > 0){
                            JSONObject finObj = new JSONObject();
                            String noSpaceLineage = FrameworkUtil.findAndReplace(lineage," ","_");
                            String strLineageHeading = EnoviaResourceBundle.getProperty(context, LANG_RESOURCE_FILE, new Locale(languageStr), FTS_RES_KEY_PREFIX+noSpaceLineage);
                            finObj.put("title", strLineageHeading);
                            finObj.put("taxonomy",lineage);
                            finObj.put("children",resultArr);
                            finArr.put(finObj);
                            }
                }
            }

            finalResultobj.put("children",finArr);
            JSONObject attributeTax = new JSONObject(getAttributeCounts(context,args));
            finalResultobj.put("attributes",attributeTax);
            TAXONOMIES_MAP.put(fullTextSearchTimestamp,cacheMap);

            return finalResultobj.toString(JSON_INDENT_FACTOR);
        } catch (Throwable e) {
            e.printStackTrace();
            return buildJsonForError(e, (String)params.get("languageStr")).toString();
        } finally {
            ContextUtil.commitTransaction(context);
        }
    }

    /**
     * Returns the type taxonmy
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            contains a packed HashMap of request parameters
     * @throws Exception
     *             if the operation fails.
     * @since AEF 10.7.3
     */
    public String getTaxonomyCounts(Context context, String[] args) throws Exception {
        HashMap params = (HashMap) JPO.unpackArgs(args);

        // This block simply prevents errors if this method
        // is called when in Matrix search mode, since MatrixSearch
        // does not (currently) implement getTaxonomies() etc..
        if (!UISearchUtil.isAutonomySearch(context,params)) {
            JSONObject t = new JSONObject();
            t.put("type", "Part");
            t.put("field", "TYPES");
            t.put("displayValue", "Part");
            JSONArray a = new JSONArray();
            a.put(t);
            return new JSONObject().put("objs", a).toString();
        }
        JSONArray resultArr = new JSONArray();
        try {
            ContextUtil.startTransaction(context, false);

            SearchRefinement refinements[] = GetSearchRefinements(context, params);
            HashMap allTaxonomiesData = generateTaxonomyCounts(context, refinements, params);
            //if (allTaxonomiesData.size() == 0 && UISearchUtil.isAutonomySearch(params)) {
            //added to fix Did You mean
            boolean includeCounts  = (UISearchUtil.isAutonomySearch(context,params) && UISearchUtil.includeCounts(params));
            boolean canHaveSuggestions = false;
            String isDidYouMean = EnoviaResourceBundle.getProperty(context, "emxFramework.FullTextSearch.DidYouMean");
            JSONObject jsonFilters = extractFilters(params);
            if(filterExists(jsonFilters, UISearchUtil.TEXTSEARCH) && UISearchUtil.isAutonomySearch(context,params) && !isDidYouMean.matches("false")){
                if(includeCounts == true && allTaxonomiesData.size() == 0){
                    canHaveSuggestions = true;
                }else if(includeCounts == false){
                    canHaveSuggestions = true;
                }
            }
            //added to fix Did You mean
			if (canHaveSuggestions) {
                JSONObject jsonSugg = getSuggestions(context, refinements, params);
                if (jsonSugg != null) {
                    return jsonSugg.toString();
                }
            }
            Iterator taxonomyIter = allTaxonomiesData.keySet().iterator();
            while (taxonomyIter.hasNext()) {
                String taxonomyName = (String) taxonomyIter.next();
                HashMap taxonomyData = (HashMap) allTaxonomiesData.get(taxonomyName);
                //ContextUtil.pushContext(context);
                if(isTaxonomyHidden(context, taxonomyName)){
                    continue;
                }
                JSONArray arr = buildJSONTaxonomies(context, taxonomyName, taxonomyData, params);
                //ContextUtil.popContext(context);
                if(arr.length() > 0) {
                JSONObject obj = new JSONObject();
                obj.put(taxonomyName, arr);
                String noSpaceTaxonomyName = FrameworkUtil.findAndReplace(taxonomyName," ","_");
                String strLeftNavTypesHeading = EnoviaResourceBundle.getProperty(context, LANG_RESOURCE_FILE, new Locale((String)params.get("languageStr")), FTS_RES_KEY_PREFIX+noSpaceTaxonomyName);
                obj.put("field", strLeftNavTypesHeading);
                resultArr.put(obj);
            }
            }
            JSONObject o = new JSONObject();
            o.put("objs", resultArr);
            return o.toString(JSON_INDENT_FACTOR);
        } catch (Throwable e) {
            e.printStackTrace();
            return buildJsonForError(e, (String)params.get("languageStr")).toString();
        } finally {
            ContextUtil.commitTransaction(context);
        }
    }

    /* Sorts a JSONArray containing JSONObjects by sortKey */
    /* Analogous to MapList.sort() */
    public void sortJsonArrayOfObjects(JSONArray jsonObjArray, String sortKey) throws Exception {
        sortJsonArrayOfObjects(jsonObjArray, sortKey, "ascending", "string");
    }

    public void sortJsonArrayOfObjects(JSONArray jsonObjArray, String sortKey, String direction, String dataType)
        throws Exception
    {
        MapList tmp = new MapList();
        for (int i = 0; i < jsonObjArray.length(); i++) {
            JSONObject jsonObj = jsonObjArray.getJSONObject(i);
            HashMap m = new HashMap();
            try {
                String key = jsonObj.getString(sortKey);
                m.put("key", key);
            } catch (Exception e) {
                m.put("key", "");
            }
            m.put("data", jsonObj);
            tmp.add(m);
        }
        tmp.sort("key", direction, dataType);
        for (int i = 0; i < tmp.size(); i++) {
            HashMap m = (HashMap) tmp.get(i);
            jsonObjArray.put(i, m.get("data"));
        }
    }

    /**
     * Returns the JSON string with type taxonmy details
     *
     * @param HashMap
     *            the type taxonomky details
     * @param args
     *            contains a packed HashMap of request parameters
     * @throws Exception,
     *             Exception if the operation fails.
     * @since AEF 10.7.3
     */
    protected JSONArray buildJSONTaxonomies(Context context, String lineage, HashMap countsTree, HashMap params)
        throws Exception, Exception
    {
        JSONArray jsonTaxonomiesArr = new JSONArray();
        JSONObject jsonNode = null;
        Integer count;
        ;
        HashMap children;
        String name = null;

        JSONObject jsonFilters = extractFilters(params);

        String languageStr = (String) params.get("languageStr");
        StringList mandatoryList = getManadatoryList(params);

        Iterator itr = countsTree.keySet().iterator();
        while (itr.hasNext()) {
            if (name == null || !name.startsWith("@")) {
                jsonNode = new JSONObject();
                //jsonTaxonomiesArr.put(jsonNode);
            }

            name = (String) itr.next();
            if (name.startsWith("@") == false) {
                jsonNode.put("type", name);
                jsonNode.put("field", lineage);
				String strLoading = EnoviaResourceBundle.getProperty(context, LANG_RESOURCE_FILE, new Locale(languageStr), "emxNavigator.UIMenuBar.Loading");
                if (lineage.equals(UISearchUtil.FIELD_TYPES)) {
					String dispValue = "";
					if("hasChild".equals(name)){
						dispValue = strLoading;
					}else{
						dispValue = i18nNow.getTypeI18NString(name, languageStr);
					}

					jsonNode.put("displayValue", dispValue);
                } else {
                    String dispName;
                    try {
						if("hasChild".equals(name)){
							dispName = strLoading;
						}else{
                        dispName = MqlUtil.mqlCommand(context,"print bus $1 select $2 dump ",name,"name");

						}
                    } catch (MatrixException me) {
                        // object is either deleted, or more likely, access restricted
                        // TODO: use some i18N'ized string meaning "not available"
                        dispName = "...";
                        continue;
                    }
                    jsonNode.put("displayValue", dispName);
                }

                jsonNode.put("selected", Boolean.valueOf(filterExists(jsonFilters, lineage, name, "string", false)));

                if (mandatoryList.contains(lineage)) {
                    jsonNode.put("mandatorySearch", Boolean.valueOf(true));
                    jsonNode.put("selected", Boolean.valueOf(true));
                }

                children = (HashMap) countsTree.get(name);
                count = (Integer) children.get("@COUNT");
                if (count.intValue() != -1) {
                    jsonNode.put("count", count);
                }

                if (children.size() > 2) { // one "child" is the @COUNT and
                                            // another @ID
                    JSONArray arr = buildJSONTaxonomies(context, lineage, children, params);
                    //ContextUtil.popContext(context);
                    if(arr.length() > 0) {
                        jsonNode.put("children", arr);
                    }else {
                        continue;
                    }
                }
                if (jsonNode != null) {
                    jsonTaxonomiesArr.put(jsonNode);
                }
            }
        }

        sortJsonArrayOfObjects(jsonTaxonomiesArr, "displayValue");
        return (jsonTaxonomiesArr);
    }

    /**
     * Returns the attribute taxonmy
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            contains a packed HashMap of request parameters
     * @throws Exception
     *             if the operation fails.
     * @since AEF 10.7.3
     */
    public String getAttributeCounts(Context context, String[] args) throws Exception {
        HashMap params = (HashMap) JPO.unpackArgs(args);
        try {
            SearchRefinement refinements[] = GetSearchRefinements(context, params);
            StringList includeFields = getFieldInclusionList(context,params);

            HashMap attributeCounts = generateAttributeCounts(context, refinements, includeFields, params);


            JSONObject jsonNestedObj = new JSONObject();

            JSONArray jsonAttributes = buildJSONAttributes(context, attributeCounts, includeFields, params);
            String strLeftNavAttrHeading = EnoviaResourceBundle.getProperty(context, LANG_RESOURCE_FILE, new Locale((String)params.get("languageStr")), FTS_RES_KEY_PREFIX+"attributes");
            jsonNestedObj.put("attributes", jsonAttributes);

			if(!UISearchUtil.isFormMode(params)){
	            jsonNestedObj.put("field", strLeftNavAttrHeading);

	            if(!canUse((String)params.get("singleField"))){
	                JSONArray jsonBreadcrumbs = buildJSONAttributeBreadcrumbs(context, params);
	                jsonNestedObj.put("breadcrumbs", jsonBreadcrumbs);
	            }
            }

            JSONObject res = new JSONObject().put("obj", jsonNestedObj);
            return res.toString(JSON_INDENT_FACTOR);
        } catch (Throwable e) {
            e.printStackTrace();
            return buildJsonForError(e, (String)params.get("languageStr")).toString();
        } finally {
            ContextUtil.commitTransaction(context);
        }
    }

    protected JSONObject getSuggestions(Context context, SearchRefinement[] refinements, HashMap params) throws Exception, MatrixException {
        Search search = newSearchInstance(context, params);
        //search.setPageSize(1);
        SearchResult result = search.search(context, refinements, 0);

        Suggestion[] suggestions = result.getSuggestions();
        if (suggestions != null && suggestions.length > 0) {
            JSONArray jsonSuggestions = new JSONArray();
            if (suggestions != null && suggestions.length > 0) {
                for (int itr = 0; itr < suggestions.length; itr++) {
                    JSONObject jsonSuggestion = new JSONObject();
                    jsonSuggestion.put("suggestions", suggestions[itr].getText());
                    jsonSuggestions.put(jsonSuggestion);
                }
            }
            JSONObject res = new JSONObject();
            res.put("suggestions", jsonSuggestions);
            return (res);

        }
        return null;
    }

    /**
     * Returns the JSONObject of filter parameter
     *
     * @param args
     *            contains a packed HashMap with the following entries: filters:
     *            contains the navigation/form refinements
     * @throws Exception
     *             if the operation fails.
     * @since AEF 10.7.3
     */
    protected JSONObject extractFilters(HashMap params) throws Exception {
        String ftsFilters = (String) params.get("ftsFilters");
        if (canUse(ftsFilters)) {
// Bug 351815 filters = com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(filters);
            JSONObject jsonFilters = null;
            try{
                String locfilters = FrameworkUtil.findAndReplace(ftsFilters,"+","%2B");
                jsonFilters = new JSONObject(com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(locfilters));
            }catch(Exception ex){
            jsonFilters = new JSONObject(ftsFilters);
        }
            return jsonFilters;
        } else {
            return new JSONObject();
        }
    }

    /*
     * In form view mode, exactly these fields will be displayed, no more no
     * less: fields specified in
     * emxFramework.FullTextSearch.FormView.RealTime.FixedFields or emxFramework.FullTextSearch.FormView.Indexed.FixedFields fields specified in url
     * param (which must be found) both of which are comma separated lists
     * (which could be missing or empty) This method just combines the two lists
     *
     * In nav mode, it's fully dynamic, but fields named in
     * emxFramework.FullTextSearch.NavigationView.IncludeFields
     * are always included.  Typically these are non-parametric fields
     * like MODIFIED.
     */
    protected StringList getFieldInclusionList(Context context,HashMap params) throws Exception {
        StringList includeFields = new StringList();
		// 466582 - This change is done not translate all the choices of the field
        if (UISearchUtil.isFormMode(params)) {
            if(!UISearchUtil.isAutonomySearch(context,params)){
            	MapList fields = getFormFields(context,params);
            	for (int i = 0; i < fields.size(); i++) {
					HashMap field = (HashMap)fields.get(i);
					String fieldName = UIComponent.getName(field);
					Config.Field configfield = _config.indexedBOField(fieldName);
					if(configfield != null){
						includeFields.add(fieldName);
					}
            	}
        	}

            if(includeFields != null && includeFields.size() <= 0){
            String fixedFields = UISearchUtil.isAutonomySearch(context,params) ? "emxFramework.FullTextSearch.FormView.Indexed.FixedFields"
                                 : "emxFramework.FullTextSearch.FormView.RealTime.FixedFields";

            includeFields = FrameworkProperties.getTokenizedProperty(context,fixedFields, ",");
            }
            String sFormInclusionCsl = (String) params.get(UISearchUtil.FORMINCLUSIONLIST);
            if(canUse(sFormInclusionCsl)){
            StringList formInclusionList = FrameworkUtil.split(sFormInclusionCsl.toUpperCase(), ",");
            for (int i = 0; i < formInclusionList.size(); i++) {
                String currentFormField = (String)formInclusionList.get(i);
                if(includeFields.contains(currentFormField)){
                    formInclusionList.remove(currentFormField);
                    i--;
                }
            }
            if(formInclusionList.size() > 0){
                String formInclString = formInclusionList.toString().substring(1,formInclusionList.toString().length()-1);
                if (canUse(formInclString)) {
                    includeFields.addAll(formInclusionList);
                }
            }
            }

        } else {
            includeFields = FrameworkProperties.getTokenizedProperty(context,
                "emxFramework.FullTextSearch.NavigationView.IncludeFields", ",");
        }
        return includeFields;
    }


    protected MapList getFormFields(Context context, HashMap params) throws FrameworkException {
    	Vector userRoleList = PersonUtil.getAssignments(context);
    	String formName = (String)params.get("form");
    	MapList fields = new MapList();
    	MapList finalFields = new MapList();
    	BusinessObject busObject = null;
    	if (PersonUtil.isPersonObjectSchemaExist(context))
        {
            busObject = (BusinessObject)PersonUtil.getPersonObject(context);
        }
    	if(canUse(formName)){
    		fields = getCacheFormFields(context, formName, userRoleList);
    		if(fields != null){
    			for (int i = 0; i < fields.size(); i++) {
    				HashMap field = (HashMap) fields.get(i);
    				if(UINavigatorUtil.checkAccessForSettings(context,busObject,params,UIComponent.getSettings(field)))
    	            {
    					finalFields.add(field);
    	            }
				}
    		}
    	}
        return finalFields;
	}

    private MapList getCacheFormFields(Context context, String formName,Vector userRoleList) throws FrameworkException {
    	return UICache.getFields(context, formName, userRoleList);
    }

    private static String getValue(Map map, String key)
    {
        String value = (String) map.get(key);
        return ((value == null) ? "" : value.trim());
    }

    protected boolean filterExists(JSONObject jsonFilters, String attribute) throws Exception {
        return filterExists(jsonFilters, attribute, null, null, false);
    }

    /**
     * Checkes a specified config field exists in the filters object
     *
     * @param JSONObject
     *            contains a packed filters objects that will come from
     *            emxUIFullSearch.js
     * @param attribute
     *            contains config field name
     * @param value
     *            contains the value that user selects from the navigation/form
     *            refinement
     * @param dataType
     *            contains the type of field configured in config.xml
     * @param isUOM
     *            specifies the UOM attribute or not
     * @throws Exception
     *             if the operation fails.
     * @since AEF 10.7.3
     */
    protected boolean filterExists(JSONObject jsonFilters, String attribute, String value, String dataType,
        boolean isUOM) throws Exception
    {
        if (jsonFilters != null) {
            JSONArray jsonValues;
            String fieldName;

            Iterator itr = jsonFilters.keys();
            String jsonValue = "";
            while (itr.hasNext()) {
                fieldName = (String) itr.next();
                if (UOMUtil.isAttributeExpression(attribute)) {
                    attribute = UOMUtil.getAttrNameFromSelect(attribute);
                }
                if (fieldName.equals(attribute)) {
                    jsonValues = jsonFilters.getJSONArray(fieldName);

                    if (value == null) {
                        return true;
                    }

                    if (jsonValues.length() > 0) {
                        for (int j = 0; j < jsonValues.length(); j++) {
                            jsonValue = jsonValues.getString(j);
                            if (jsonValue.indexOf("|") != -1) {
                                jsonValue = jsonValue.substring(jsonValue.indexOf("|") + 1, jsonValue.length());
                                if (isUOM && jsonValue.indexOf(" ") > 0) {
                                    jsonValue = jsonValue.substring(0, jsonValue.indexOf(" "));
                                }
                            }
                            if (("real".equals(dataType) || "integer".equals(dataType))) {
                                try {
                                    double doubleValue = new Double(value).doubleValue();
                                    double doubleJsonValue = new Double(jsonValue).doubleValue();
                                    if (doubleValue == doubleJsonValue) {
                                        return true;
                                    }
                                } catch (NumberFormatException nf) {
                                    // this is normal; user typed garbage
                                    // instead of number
                                }
                            } else {
                                //376973
                                /*StringList jsonValueList = FrameworkUtil.split(jsonValue, ",");
                                for (int i = 0; i < jsonValueList.size(); i++) {
                                    if(jsonValueList.get(i).equals(value)){
                                return true;
                            }
                                }*/
                                if(jsonValue.equals(value)){
                                    return true;
                        }
                    }
                }
            }
        }
            }
        }

        return false;
    }

    /**
     * Checkes type exists in the specified field TYPES field
     *
     * @param txtType
     *            contains TYPES value of field parameter
     * @param type
     *            contains type name that user selects
     * @throws Exception
     *             if the operation fails.
     * @since AEF 10.7.3
     */
    protected boolean typeExists(String txtType, String type) throws Exception {
        return FrameworkUtil.split(txtType, ",").contains(type);
    }

    protected String getBasicFieldI18nName(String basic, String languageStr) throws Exception {
        String keyPrefix = "emxFramework.Basic.";
        char[] chars = basic.toLowerCase().toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        String key = keyPrefix + new String(chars);
        return i18nNow.getI18nString(key, LANG_RESOURCE_FILE, languageStr);
    }

    protected JSONArray buildJSONAttributeBreadcrumbs(Context context, HashMap params) throws Exception {
        JSONArray breadcrumbs = new JSONArray();
        String languageStr = (String) params.get("languageStr");
        String fieldParam = (String) params.get("field");
        StringList fieldList = FrameworkUtil.split(fieldParam,":");
        boolean ignoreLastOrLatest = false;
        boolean ignoreSystemProperty = false;
        JSONObject jsonFilters = extractFilters(params);
        String TagsView = (String) params.get("TagsView");

        String STR_REFINEMENT_SEPARATOR = EnoviaResourceBundle.getProperty(context, "emxFramework.FullTextSearch.RefinementSeparator");
        if(STR_REFINEMENT_SEPARATOR == null || "".equals(STR_REFINEMENT_SEPARATOR)){
            STR_REFINEMENT_SEPARATOR = ",";
        }

        Iterator keyIter = jsonFilters.keys();
        while (keyIter.hasNext()) {
        	JSONObject bdObj = new JSONObject();
        	JSONArray bdArray = new JSONArray();
        	StringList bdValueList = new StringList();
            String fName = (String) keyIter.next();
            if(("LATESTREVISION".equalsIgnoreCase(fName) || "LASTREVISION".equalsIgnoreCase(fName))){
                ignoreSystemProperty = true;
                if(ignoreLastOrLatest){
                    continue;
                }
            }
            Config.Field field = _config.indexedBOField(fName);
            if (field == null) {
                continue; // happens for taxonomies
            }
            String isHidden = (String) field.attributes.get("hidden");
            if("true".equalsIgnoreCase(isHidden)){
                continue;
            }
            String selectable = field.selectable;

            String kind;
            String attributeActualName = null;
            String format = (String) field.attributes.get("format");
            String fieldSep = (String) field.attributes.get("fieldSeparator");

            String translatedName = getTranslatedFieldLabel(context,field,params);

            JSONArray jsonOpValArr = jsonFilters.getJSONArray(fName);
            boolean isMandatory = false;
            for (int i = 0; i < fieldList.size(); i++) {
                String fieldURL = (String)fieldList.get(i);
                String fieldNameURL = (String)FrameworkUtil.split(fieldURL,"=").get(0);
                if(fieldNameURL != null && fieldNameURL.equals(fName)){
                    isMandatory = true;
                }
            }

            bdObj.put("attribute", fName);
            bdObj.put("displayName", translatedName);
            bdObj.put("isMandatory", Boolean.valueOf(isMandatory));
            for (int i = 0; i < jsonOpValArr.length(); i++) {
                String opAndVal = jsonOpValArr.getString(i);
                String operatorString = opAndVal.substring(0, opAndVal.indexOf("|"));
                String value = opAndVal.substring(opAndVal.indexOf("|") + 1);
                bdValueList.add(value);
                if(("LATESTREVISION".equalsIgnoreCase(fName) || "LASTREVISION".equalsIgnoreCase(fName))){
                       if("true".equalsIgnoreCase(value)){
                           ignoreLastOrLatest = true;
                       }else{
                           continue;
                       }
                }
				//if(field!= null && field.selectable.equalsIgnoreCase("current") && value.indexOf(".") != -1){
            		//value = (String)FrameworkUtil.split(value, ".").get(1);
            	//}
				String translatedValue = getTranslatedValue(context,params,field,value,languageStr);
                value = FrameworkUtil.findAndReplace(value,"%","%25");
                if(fieldSep != null && !"".equals(fieldSep) && !"true".equals(TagsView)){
                    JSONObject jsonFilterValue = new JSONObject(value);
                    JSONArray jsonFilterArr = jsonFilterValue.getJSONArray(fName);
                    for (int j = 0; j < jsonFilterArr.length(); j++) {
                        JSONArray jsonArr = jsonFilterArr.getJSONArray(j);
                        String tmpVal = "";
                        for (int k = 0; k < jsonArr.length(); k++) {
                            String filterValue = jsonArr.getString(k);
                            JSONObject crumb = new JSONObject();
                            crumb.put("attribute", fName);
                            crumb.put("displayName", translatedName);
                            crumb.put("operator", operatorString);
                            crumb.put("value", filterValue);
                            crumb.put("displayValue", filterValue);
                            crumb.put("fieldSeparator", fieldSep);
                            crumb.put("isMandatory", Boolean.valueOf(isMandatory));
                            crumb.put("index", j+","+k);
                            bdArray.put(crumb);
                            bdObj.put("operator", operatorString);
                        }
                    }
                }else{
                    JSONObject crumb = new JSONObject();
                    crumb.put("attribute", fName);
                    crumb.put("displayName", translatedName);
                    crumb.put("operator", operatorString);
                    crumb.put("value", value);
                    crumb.put("displayValue", translatedValue);
                    bdArray.put(crumb);
                    bdObj.put("operator", operatorString);
                }
            }
            if(bdArray.length() > 0){
                bdObj.put("value", FrameworkUtil.join(bdValueList, STR_REFINEMENT_SEPARATOR));
                bdObj.put("children", bdArray);
                breadcrumbs.put(bdObj);
            }

        }

        if(!ignoreSystemProperty){
            String lastRevision = EnoviaResourceBundle.getProperty(context, "emxFramework.FullTextSearch.LASTREVISION");
            String latestRevision = EnoviaResourceBundle.getProperty(context, "emxFramework.FullTextSearch.LATESTREVISION");
            String refinementName = "";
            if(!("true".equalsIgnoreCase(lastRevision) && "true".equalsIgnoreCase(latestRevision))
                    && !("false".equalsIgnoreCase(lastRevision) && "false".equalsIgnoreCase(latestRevision))){
                if("true".equalsIgnoreCase(lastRevision)){
                    refinementName = "LASTREVISION";
                }else if("true".equalsIgnoreCase(latestRevision)){
                    refinementName = "LATESTREVISION";
                }
                String translatedName = EnoviaResourceBundle.getProperty(context,LANG_RESOURCE_FILE,new Locale(languageStr),FTS_RES_KEY_PREFIX.concat(refinementName));
                String translatedValue = UISearchUtil.getActualNames(context,"TRUE");
                JSONObject crumb = new JSONObject();
                JSONObject bdObj = new JSONObject();
            	JSONArray bdArray = new JSONArray();
                bdObj.put("attribute", refinementName);
                bdObj.put("displayName", translatedName);
                bdObj.put("isMandatory", false);
                bdObj.put("value", "true");
                bdObj.put("operator", "Equals");
                crumb.put("attribute", refinementName);
                crumb.put("displayName", translatedName);
                crumb.put("operator", "Equals");
                crumb.put("value", "true");
                crumb.put("displayValue", translatedValue);
                bdArray.put(crumb);
                bdObj.put("children", bdArray);;
                breadcrumbs.put(bdObj);

            }
        }
        return breadcrumbs;
    }

    public String getStateI18NString(String state, String languageStr) throws Exception {
        /*371102 - Removed the spaces in state names*/
        state = FrameworkUtil.findAndReplace(state," ","_");
        String translatedValue = i18nNow.getI18nString("emxFramework.FullTextSearch.State." + state, LANG_RESOURCE_FILE,
            languageStr);
        if (translatedValue.startsWith("emxFramework.")) {
            return state;
        } else {
            return translatedValue;
        }
    }

    /**
     * Returns the array of JSONObjects
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param allFieldCounts
     *            contains HashMap of attibutes
     * @param type
     *            contains packed HashMap of request parameters
     * @throws Exception
     *             if the operation fails.
     * @since AEF 10.7.3
     */
    protected JSONArray buildJSONAttributes(Context context, HashMap allFieldCounts, StringList lstIncludeFields,
        HashMap params) throws Exception
    {

        JSONObject jsonFilters = extractFilters(params);
        UISearchUtil searchUtil = new UISearchUtil();
        String languageStr = (String) params.get("languageStr");
        String TagsView = (String)params.get("TagsView");
        // read mandatoryListParam from param map
        StringList mandatoryList = getManadatoryList(params);
        String sIncludeFieldsFirst =  EnoviaResourceBundle.getProperty(context,
                "emxFramework.FullTextSearch.NavigationView.IncludeFieldsShownFirst");
        boolean bInclFirst = sIncludeFieldsFirst.equalsIgnoreCase("true");

        String minRequiredChars = (String)params.get("minRequiredChars");
        if (canUse(minRequiredChars)) {
            minRequiredChars = com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(minRequiredChars);
        }
        Hashtable fieldsReqMap = searchUtil.getMinReqCharsFields(minRequiredChars,UISearchUtil.getFieldSeperator(context,params));
        String STR_REFINEMENT_SEPARATOR = EnoviaResourceBundle.getProperty(context, "emxFramework.FullTextSearch.RefinementSeparator");
        if(STR_REFINEMENT_SEPARATOR == null || "".equals(STR_REFINEMENT_SEPARATOR)){
            STR_REFINEMENT_SEPARATOR = ",";
        }
        boolean uiAutomation = false;
        if(UIComponent.getUIAutomationStatus(context)){
        	uiAutomation = true;
        }
        JSONArray jsonAttributes = new JSONArray();
        Iterator fieldIter = allFieldCounts.keySet().iterator();
        while (fieldIter.hasNext()) {
            String fName = (String) fieldIter.next();
            boolean isUOM = false;
            String dataType = "string";

            JSONObject jsonAttribute = new JSONObject();
            String attributeActualName = null;
            String selectable = "";
            String fieldType = "";
            String fieldSep = "";
            String isHidden = "";
            String viewOnlyIn = "";
            String kind;
            String format = null;
            //Read the field type
            Config.Field field = _config.indexedAllBOField(fName);
            boolean treatTYPEasTYPES = false;

            if(uiAutomation){
            	jsonAttribute.put("UIAutomation", "true");
            }
            if(!UISearchUtil.isAutonomySearch(context,params) && field != null && ("TYPE".equalsIgnoreCase(field.name) && jsonFilters.contains("TYPES"))){
                treatTYPEasTYPES = true;
            }
            if (getTaxonomyNames(context).contains(fName)) {
                jsonAttribute.put("attribute", fName);
                jsonAttribute.put("displayValue", EnoviaResourceBundle.getProperty(context,LANG_RESOURCE_FILE,new Locale(languageStr),FTS_RES_KEY_PREFIX.concat(fName)));
                jsonAttribute.put("dataType", "string");
                kind = "other";
            } else {

                if (field == null) {
					//IR-047484V6R2011
					//throw new Exception("Invalid field: " + fName);

                	if(TagsView!= null && "true".equals(TagsView) && fName!= null){
	                	JSONObject jsonAttributeTag = new JSONObject();
	                	HashMap tempp =  (HashMap) allFieldCounts.get(fName);
	              	  	Iterator itr = tempp.keySet().iterator();
	              	    JSONArray jsonValues = new JSONArray();
	              	    JSONArray jsonValues11 = new JSONArray();
	              	    
	              	    JSONObject jsonAttribute13 = new JSONObject();
	              	    jsonValues11.put(jsonAttribute13);
	              	    jsonValues11.put(jsonAttribute13);
						String sixwValue = null;
	                    while(itr.hasNext()) {
	                    	JSONObject jsonAttribute11 = new JSONObject();
	                    	String key = (String) itr.next();
							if("sixw".equals(key)){
	                    		sixwValue = (String)tempp.get(key);
							}
	                    	 if(("sixwDisplay".equals(key) ||"sixw".equals(key)|| "dataTypetag".equals(key) ||  "fieldName".equals(key))  ){
	                    		 if("sixwDisplay".equals(key) ||"sixw".equals(key)){
	                    			 String displayValue = (String)tempp.get(key);
	                    			 jsonAttributeTag.put(key, displayValue);
	                    		 }
	                         }else{
	                        	 jsonAttributeTag.put(key, Integer.toString((Integer)tempp.get(key)));
	                        	 jsonAttributeTag.put("attribute"," ");
	                        	 jsonAttribute11.put("value", key);
	                        	 jsonAttribute11.put("count",  Integer.toString((Integer)tempp.get(key)));
	                        	 String tagsDisplayValue = null;
	     	                	 if(getAdminValue(sixwValue).equalsIgnoreCase("type") || getAdminValue(sixwValue).equalsIgnoreCase("policy")){
	     	                		tagsDisplayValue = getDisplayValue(context, key, null, (String)params.get("languageStr"), getAdminValue(sixwValue));
	     	                	 }
	     	                	 else{
	     	                		if(getAdminValue(sixwValue).equalsIgnoreCase("status")){
	     	                			 tagsDisplayValue = getStateI18NString(context, params, key, (String)params.get("languageStr"));
	     	                	    }else{
	     	                			 tagsDisplayValue = key;
	     	                		 }
	     	                	 }
	                        	 jsonAttribute11.put("displayValue", tagsDisplayValue);
	                        	 if(UISearchUtil.isValidDate(tagsDisplayValue)){
	                        		 jsonAttribute11.put("dataType", "date");
	                        	 }
	                        	 else{
	                        	 jsonAttribute11.put("dataType", "string");
	                        	 }
	                        	 if(fName.indexOf("ExplicitTags")>=0){
	                        		 jsonAttribute11.put("field", TAG_TYPE_EXPLICIT);
	                        	 }
	                        	 if(fName.indexOf("ImplicitTags")>=0){
		                        	 jsonAttribute11.put("field", TAG_TYPE_IMPLICIT);
	                        	 }
	                        	 jsonValues11.put(jsonAttribute11);
	                         }
	                    	 jsonValues.put(jsonAttributeTag);
	              		}
	                    jsonAttributeTag.put("values",jsonValues11 );
	              		jsonAttributes.put(jsonAttributeTag);
                	}
					continue;
                }

                format = (String) field.attributes.get("format");
                selectable = field.selectable;
                fieldType = (String)field.attributes.get("fieldType");
                if(fieldType == null){
                    fieldType = "";
                }
                fieldSep = (String) field.attributes.get("fieldSeparator");
                if(fieldSep != null){
                    jsonAttribute.put("fieldSeparator", fieldSep);
                }
                isHidden = (String) field.attributes.get("hidden");
                viewOnlyIn = (String) field.attributes.get("viewInOnly");
                boolean hideInRealTime = false;
                boolean hideInIndexed = false;
                if(!"true".equalsIgnoreCase(isHidden))
                {
                    hideInRealTime = ((!UISearchUtil.isAutonomySearch(context,params)) && "indexed".equalsIgnoreCase(viewOnlyIn));
                    hideInIndexed = ((UISearchUtil.isAutonomySearch(context,params)) && "realTime".equalsIgnoreCase(viewOnlyIn));
                }
                if("true".equalsIgnoreCase(isHidden) || hideInRealTime || hideInIndexed){
                    continue;
                }
                int attrType = field.type;

                String translatedName = getTranslatedFieldLabel(context,field,params);

                jsonAttribute.put("displayValue", translatedName);
                jsonAttribute.put("attribute", fName);

                if(field.parametric){
                    jsonAttribute.put("parametric", Boolean.valueOf(true));
                }
                jsonAttribute.put("refinementSeparator", STR_REFINEMENT_SEPARATOR);

                if (!UISearchUtil.isAutonomySearch(context,params)) {
                String chooser = (String) field.attributes.get("chooserURL");
                String chooserJPO = (String) field.attributes.get("chooserJPO");
                    if (chooser != null && !"".equals(chooser)) {
                    jsonAttribute.put("chooser", UINavigatorUtil.parseHREF(context,chooser, null));
                    } else if(format != null && format.equalsIgnoreCase("user")) {
                        jsonAttribute.put("UserChooser", UISearch.USER_SEARCH_CHOOSER);
                    }
                    if (chooserJPO != null && !"".equals(chooserJPO)) {
						jsonAttribute.put("chooserJPO", chooserJPO);
					}
                }else{
                    String chooser = (String) field.attributes.get("chooserURL");
                    if(!field.parametric && UISearchUtil.isAutonomySearch(context,params)&& UISearchUtil.isFormMode(params)&& chooser!= null && chooser.length() != 0){
                        jsonAttribute.put("customChooser", UINavigatorUtil.parseHREF(context,chooser, null));
                        jsonAttribute.put("disabled","false");
                        if(chooser.startsWith("${COMMON_DIR}/emxFullSearch.jsp?")){
                            jsonAttribute.put("isFullSearch","true");
                        }else{
                            jsonAttribute.put("isFullSearch","false");
                        }
                    }
                }


                //if (UISearchUtil.isAutonomySearch(params)) {
                    String minReqChars = (String)fieldsReqMap.get(field.name);
                    if (minReqChars == null || "".equals(minReqChars)) {
                        minReqChars = (String) field.attributes.get("minRequiredChars");
                    }
                    if (minReqChars != null && !"".equals(minReqChars)) {
                        try {
                            if(Integer.parseInt(minReqChars) > 0)
                            {
                                jsonAttribute.put("minReqChars", minReqChars);
                            }
                        }catch(Exception e){
                            // do nothing
                        }
                    }
                //}

                //353833 - start
                String paramFields = (String) params.get("field");
                if (canUse(paramFields)) {
                	paramFields =FrameworkUtil.findAndReplace(paramFields,"+",com.matrixone.apps.domain.util.XSSUtil.encodeForURL("+"));
                    paramFields = com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(paramFields);
                }
                String defaultFields = (String) params.get("default");
                if (canUse(defaultFields)) {
                	defaultFields =FrameworkUtil.findAndReplace(defaultFields,"+",com.matrixone.apps.domain.util.XSSUtil.encodeForURL("+"));
                    defaultFields = com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(defaultFields);
                }
                if(defaultFields!= null && defaultFields.contains("LASTREVISION=true")){
                	jsonAttribute.put("defaultLastRev","true");
                }
                if(defaultFields!= null && defaultFields.contains("LATESTREVISION=true")){
                	jsonAttribute.put("defaultLatestRev","true");
                }
                Hashtable fieldsMap = searchUtil.getFields(paramFields,UISearchUtil.getFieldSeperator(context,params));
                String txtSelect = (String) fieldsMap.get(field.name);
                if(!canUse(txtSelect) && "type".equalsIgnoreCase(field.selectable) && canUse((String)fieldsMap.get("TYPES")) && !filterExists(jsonFilters, "TYPE")){
                    txtSelect = (String) fieldsMap.get("TYPES");
                }
                if (canUse(txtSelect) && UISearchUtil.isFormMode(params)) {
                    StringList txtSelectList = FrameworkUtil.split(txtSelect, "|");
                    String txtMxSelect = (String) txtSelectList.get(1);
                    StringList txtMxSelectList = FrameworkUtil.split(txtMxSelect, ",");
                    StringList txtDispMxSelectList = new StringList();
                    for (int it = 0; it < txtMxSelectList.size(); it++) {
                        String trnsValue = getTranslatedValue(context,params,field,(String)txtMxSelectList.get(it),languageStr);
                        txtDispMxSelectList.add(trnsValue);
                        }
                    jsonAttribute.put("selectedValue",FrameworkUtil.join(txtDispMxSelectList,STR_REFINEMENT_SEPARATOR));
                    if(txtMxSelect.indexOf(",") == -1){
                    	if("type".equalsIgnoreCase(field.selectable) && hasSubTypes(context,txtMxSelect)){
                    		jsonAttribute.put("disabled","false");
                    	}else if(field.selectable.equalsIgnoreCase("current")){
                            jsonAttribute.put("disabled","false");
                        }else{
                    jsonAttribute.put("disabled","true");
                    }
                	}
                }else if (jsonFilters.contains(field.name)  || treatTYPEasTYPES){
                    //376973
                    /*String txtJsonVal = (String)jsonFilters.getString(field.name);
                    StringList sltxtFilterSelect = FrameworkUtil.split(txtJsonVal, "|");
                    String txtDispSelect = "";
                    for (int i = 0; i < sltxtFilterSelect.size(); i++) {
                        String txtFilterSelect = (String)sltxtFilterSelect.get(i);
                        StringList slTempList = FrameworkUtil.split(txtFilterSelect, ",");
                        for (int j = 0; j < slTempList.size(); j++) {
                                txtFilterSelect = (String)slTempList.get(j);
                        if(txtFilterSelect.lastIndexOf("\"]") > -1){
                            txtFilterSelect = txtFilterSelect.substring(0,txtFilterSelect.lastIndexOf("\"]"));
                        }
                        if(txtFilterSelect.lastIndexOf("\"") > -1){
                            txtFilterSelect = txtFilterSelect.substring(0,txtFilterSelect.lastIndexOf("\""));
                        }
                        if(filterExists(jsonFilters, fName, txtFilterSelect, dataType, isUOM)){
                                String transValue = getTranslatedValue(context,field,txtFilterSelect,languageStr);
                            if("".equals(txtDispSelect)){
                                    txtDispSelect = transValue;
                            }else{
                                    txtDispSelect += STR_REFINEMENT_SEPARATOR + transValue;
                            }
                        }
                    }
                    }*/
                    String txtDispSelect = "";
                    String txtFName = "";
                    JSONArray txtJsonArr = new JSONArray();
                    if(treatTYPEasTYPES){
                        txtFName = "TYPES";
                    }else{
                        txtFName = field.name;
                    }
                    txtJsonArr = (JSONArray)jsonFilters.getJSONArray(txtFName);
                    for (int i = 0; i < txtJsonArr.length(); i++) {
                        String txtJsonVal = txtJsonArr.getString(i);
                        String txtFilterSelect = (String)FrameworkUtil.split(txtJsonVal, "|").get(1);

                        if(filterExists(jsonFilters, txtFName, txtFilterSelect, dataType, isUOM)){
                            String transValue = getTranslatedValue(context,params,field,txtFilterSelect,languageStr);
                            if("".equals(txtDispSelect)){
                                txtDispSelect = transValue;
                            }else{
                                txtDispSelect += STR_REFINEMENT_SEPARATOR + transValue;
                            }
                        }
                    }
                    jsonAttribute.put("selectedValue",txtDispSelect);
                    jsonAttribute.put("disabled","false");
                }

                //353833 - end
        if(!UISearchUtil.isAutonomySearch(context,params)){
                    if (selectable.equalsIgnoreCase("type") && (fieldsMap.get("TYPES") != null)) {
                        String types = (String) fieldsMap.get(UISearchUtil.FIELD_TYPES);
                        StringList slTypes = FrameworkUtil.split(types, "^");

                        for (int i = 0; i < slTypes.size(); i++) {
                            String txtType = (String) slTypes.get(i);
                            StringList typeList = FrameworkUtil.split(txtType, "|");
                            int operator = getOperator((String) typeList.get(0));
                            txtType = (String) typeList.get(1);
                            boolean bEqualsOpr = (operator == AttributeRefinement.OPERATOR_EQUAL);
                            if (bEqualsOpr) {
                                jsonAttribute.put("inclusionList", txtType);
                            }else{
                                jsonAttribute.put("exclusionList", txtType);
                            }
                        }
                    }
                }
                // Verify that attribute is attached to any dimension
                if (field.selectable.equalsIgnoreCase("originated") || field.selectable.equalsIgnoreCase("modified")) {
                    dataType = "timestamp";
                } else if (UOMUtil.isAttributeExpression(selectable)) {
                    if (attrType == Config.PARAMETRIC_NUM || attrType == Config.NUM || attrType == Config.DOUBLE) {
                        dataType = "real";
                    } else if (attrType == Config.PARAMETRIC_DATE || attrType == Config.DATE) {
                        dataType = "timestamp";
                    } else {
                        dataType = "string";
                    }

                    if (UOMUtil.isSimpleAttributeExpression(selectable) || UOMUtil.isAttributeExpression(selectable)) {
                        attributeActualName = UOMUtil.getAttrNameFromSelect(selectable);
                    }

                    if (attributeActualName != null) {
                      	if(UIUtil.isNullOrEmpty(PropertyUtil.getAliasForAdmin(context,"attribute",attributeActualName,true))){
                    		continue;
                    	}
                        if (UOMUtil.isAssociatedWithDimension(context, attributeActualName)) {
                            isUOM = true;
                            StringList uomList = UOMUtil.getDimensionUnits(context, attributeActualName);
                            Collections.sort(uomList);
                            JSONArray jsonUoms = new JSONArray();
                            for (int i = 0; i < uomList.size(); i++) {
                                jsonUoms.put(uomList.get(i));
                            }
                            jsonAttribute.put("uomList", jsonUoms);
                            String defaultUnit = UOMUtil.getDBunit(context, attributeActualName);
                            jsonAttribute.put("defaultunit", defaultUnit);

                        }
                    }
				}else{
                    if (attrType == Config.PARAMETRIC_NUM || attrType == Config.NUM || attrType == Config.DOUBLE) {
                        dataType = "real";
                    } else if (attrType == Config.PARAMETRIC_DATE || attrType == Config.DATE) {
                        dataType = "timestamp";
                    } else {
                        dataType = "string";
                    }
                }
                if("timestamp".equals(dataType)){
                    jsonAttribute.put("label-between", EnoviaResourceBundle.getProperty(context,LANG_RESOURCE_FILE,new Locale(languageStr),FTS_RES_KEY_PREFIX.concat("Between")));
                    jsonAttribute.put("label-on", EnoviaResourceBundle.getProperty(context,LANG_RESOURCE_FILE,new Locale(languageStr),FTS_RES_KEY_PREFIX.concat("DateField.On")));
                    jsonAttribute.put("label-on-or-before", EnoviaResourceBundle.getProperty(context,LANG_RESOURCE_FILE,new Locale(languageStr),FTS_RES_KEY_PREFIX.concat("DateField.OnOrBefore")));
                    jsonAttribute.put("label-on-or-after", EnoviaResourceBundle.getProperty(context,LANG_RESOURCE_FILE,new Locale(languageStr),FTS_RES_KEY_PREFIX.concat("DateField.OnOrAfter")));
                }else if("real".equals(dataType)){
                    jsonAttribute.put("label-between", EnoviaResourceBundle.getProperty(context,LANG_RESOURCE_FILE,new Locale(languageStr),FTS_RES_KEY_PREFIX.concat("Between")));
                }

                jsonAttribute.put("dataType", dataType);

                Hashtable defaultsMap = searchUtil.getFields(defaultFields,UISearchUtil.getFieldSeperator(context,params));
                String defaultSelect = (String) defaultsMap.get(field.name);
                String defvalue = null;

                boolean hasdefault = (canUse(defaultSelect) && !canUse(txtSelect));
                boolean hasFound   = !(field.parametric && "true".equals(params.get("firstRequest")));
                if(hasdefault && field.parametric && "true".equals(params.get("firstRequest"))) {
                    StringList txtSelectList = FrameworkUtil.split(defaultSelect, "|");
                    String opr      = (String) txtSelectList.get(0);
                    defvalue = (String) txtSelectList.get(1);
                    StringList allvalues = FrameworkUtil.split(defvalue, ",");
                    HashMap singleFieldCounts = (HashMap) allFieldCounts.get(field.name);
                    if(singleFieldCounts != null && singleFieldCounts.size() > 0) {
                        Iterator valuesIter = singleFieldCounts.entrySet().iterator();

                        while (valuesIter.hasNext()) {
                            Entry e      = (Entry) valuesIter.next();
                            int count    = ((Integer) e.getValue()).intValue();
                            if(allvalues != null && ("NOTEQUALS".equalsIgnoreCase(opr) || allvalues.contains(e.getKey()) && count > 0)){
                                hasFound = true;
                                break;
                            }
                        }
                    }
                }

                boolean hasSelected = hasdefault ? hasFound : true;//!_noObjectsFound
                if ((canUse(txtSelect) && UISearchUtil.isFormMode(params)) || hasdefault && !noObjectsFound) {
                    StringList txtSelectList = FrameworkUtil.split(hasdefault ? defaultSelect : txtSelect, "|");
                    String opr = (String) txtSelectList.get(0);
                    String txtMxSelect = (String) txtSelectList.get(1);
                    txtMxSelect = UISearchUtil.getActualNames(context,txtMxSelect);
                    StringList txtMxSelectList = FrameworkUtil.split(txtMxSelect, ",");
                    StringList txtDispMxSelectList = new StringList();
                    for (int it = 0; it < txtMxSelectList.size(); it++) {
                        String trnsValue = getTranslatedValue(context,params,field,(String)txtMxSelectList.get(it),languageStr);
                        txtDispMxSelectList.add(trnsValue);
                        }
                    String defaultFieldValue = FrameworkUtil.findAndReplace(txtMxSelect,",",STR_REFINEMENT_SEPARATOR);
                    String defaultDisplayValue = FrameworkUtil.join(txtDispMxSelectList,STR_REFINEMENT_SEPARATOR);
                    String defaultDisplayjsonValue = FrameworkUtil.findAndReplace(defaultDisplayValue, "'", "\\\'");

                    if("NOTEQUALS".equalsIgnoreCase(opr)) {
                        defaultFieldValue = "!" + txtMxSelect;
                        // Default display value is not translated: Added translated default value for display.
                        defaultDisplayjsonValue = "!" + defaultDisplayjsonValue;
                    }

                    if(hasSelected){
                        jsonAttribute.put("defaultFieldValue",defaultFieldValue);
                        jsonAttribute.put("defaultDisplayValue",defaultDisplayjsonValue);
                    }

                    String filterSelect = "";
                    String filterOpr = opr;
                    StringBuffer filterMxSelect = new StringBuffer(txtMxSelect.length());
                    boolean isDateField = "timestamp".equalsIgnoreCase(jsonAttribute.getString("dataType"));
                    if("EQUALS".equalsIgnoreCase(opr) && txtMxSelect.indexOf(",") < 0 && !hasdefault)
                    {
                        if("type".equalsIgnoreCase(field.selectable) && hasSubTypes(context,txtMxSelect)){
                    		jsonAttribute.put("disabled","false");
                    	}else{
                        jsonAttribute.put("disabled","true");
                    	}
                        txtMxSelect = getTranslatedValue(context,params,field,txtMxSelect,languageStr);
                	} else if ((jsonFilters.contains(field.name) && "TYPE".equals(field.name)) || "type".equalsIgnoreCase(field.selectable)){
                        if(jsonFilters.contains("TYPES")){
                            filterSelect = (String)jsonFilters.getString("TYPES");
                    } else if (jsonFilters.contains(field.name)){
                        filterSelect = (String)jsonFilters.getString(field.name);
                        }else{
                        	txtMxSelect = defaultDisplayjsonValue.toString();
                        }
                        if(filterSelect.length()>2 && !isDateField)
                        {
                             //["NotEquals|AEFQE_SBOrderPolicy","NotEquals|AEFQE_SBInvoicePolicy"]
                             StringList filterSelectList = FrameworkUtil.split(filterSelect.substring(1,filterSelect.length()-1), ",");
                             for(int i=0; i < filterSelectList.size(); i++){
                                 String tempSelect = (String)filterSelectList.get(i);
                                 tempSelect = tempSelect.substring(1, tempSelect.length()-1);
                                 StringList tempList = FrameworkUtil.split(tempSelect, "|");
                                 if(i == 0) {
                                     filterOpr = (String)tempList.get(0);
                                 } else {
                                     filterMxSelect.append(STR_REFINEMENT_SEPARATOR);
                                 }
                                 filterMxSelect.append(getTranslatedValue(context,params,field,(String)tempList.get(1),languageStr));
                                 /*if(format != null && format.equalsIgnoreCase("user")) {
                                     String txtMxUser = "";
                                     try{
                                         txtMxUser = PersonUtil.getFullName(context,(String)tempList.get(1));
                                     }catch(Exception ex) {
                                         txtMxUser = txtMxSelect;
                                     }
                                     txtMxSelect = txtMxUser;
                                     filterMxSelect.append(txtMxSelect);
                                 }else{
                                     filterMxSelect.append((String)tempList.get(1));
                                 }*/
                             }
                        }else if(isDateField){
                            JSONArray jsonDateValArr = jsonFilters.getJSONArray(field.name);
                            txtMxSelect = "";
                            //StringList filterSelectList = FrameworkUtil.split(filterSelect.substring(1,filterSelect.length()-1), ",");
                            for (int i = 0; i < jsonDateValArr.length(); i++) {
                                String tempSelect = jsonDateValArr.get(i).toString();
                                StringList tempList = FrameworkUtil.split(tempSelect, "|");
                                String operator = (String)tempList.get(0);
                                if("EQUALS".equalsIgnoreCase(operator)) {
                                    txtMxSelect = (String)tempList.get(1);
                                }else if("GREATER".equalsIgnoreCase(operator)){
                                    if(i==0){
                                        txtMxSelect += "> " + tempList.get(1);
                                    }else{
                                        txtMxSelect += ", > " + tempList.get(1);
                                    }

                                }else if("LESS".equalsIgnoreCase(operator)){
                                    if(i==0){
                                        txtMxSelect += "< " + tempList.get(1);
                                    }else{
                                        txtMxSelect += ", < " + tempList.get(1);
                                    }

                                }
                            }
                            //jsonAttribute.put("defaultFieldValue",txtMxSelect);
                        }
                        if(filterMxSelect.length() > 0) {
                            opr = filterOpr;
                            txtMxSelect = filterMxSelect.toString();
                        }
                    }
                    if(!isDateField){
                        if("NOTEQUALS".equalsIgnoreCase(opr)) {
                        	// On reset comparator is lost. Add it. Also, add the translated default display value.
                        	//txtMxSelect = "!" + txtMxSelect;
                        	txtMxSelect = "!" + defaultDisplayValue;
                        }else if("GREATER".equalsIgnoreCase(opr)){
                            txtMxSelect = "> " + txtMxSelect;
                        }else if("LESS".equalsIgnoreCase(opr)){
                            txtMxSelect = "< " + txtMxSelect;
                        }
                    }
                    if(hasSelected){
                        if("EQUALS".equalsIgnoreCase(opr)) {
                            jsonAttribute.put("selectedValue",defaultDisplayValue);
                        }else{
                            jsonAttribute.put("selectedValue",txtMxSelect);
                        }
                    }


                    if(hasSelected)
                        jsonAttribute.put("hasDefault","true");
                }

                else if (jsonFilters.contains(field.name) || treatTYPEasTYPES){
                    String txtJsonVal = "";
                    String txtDispSelect = "";
                    String txtFieldName = "";
                    if(treatTYPEasTYPES){
                        txtFieldName = "TYPES";
                    }else{
                        txtFieldName = field.name;
                    }
                    txtJsonVal = (String)jsonFilters.getString(txtFieldName);
                    if(dataType.equalsIgnoreCase("timestamp") && txtJsonVal.length() > 2){
                        txtJsonVal = txtJsonVal.substring(2);
                        if(txtJsonVal.startsWith("Equals")) {
                            txtDispSelect = txtJsonVal.substring(txtJsonVal.indexOf("|") + 1, txtJsonVal.length()-2);
                        } else if(txtJsonVal.startsWith("Less")) {
                            txtDispSelect = "< " + txtJsonVal.substring(txtJsonVal.indexOf("|") + 1, txtJsonVal.length()-2);
                        } else {
                            if(txtJsonVal.indexOf("Less") > 0) {
                                txtDispSelect = "> " + txtJsonVal.substring(txtJsonVal.indexOf("|") + 1, txtJsonVal.indexOf("Less")-3) + ", < " + txtJsonVal.substring(txtJsonVal.lastIndexOf("|") + 1, txtJsonVal.length()-2);;
                            } else {
                                txtDispSelect = "> " + txtJsonVal.substring(txtJsonVal.indexOf("|") + 1, txtJsonVal.length()-2);
                            }
                        }
                    } else {
                        //376973
                        /*StringList sltxtFilterSelect = FrameworkUtil.split(txtJsonVal, ",");
                        for (int i = 0; i < sltxtFilterSelect.size(); i++) {
                            String txtFilterSelect = (String)sltxtFilterSelect.get(i);
                            StringList slTempList = FrameworkUtil.split(txtFilterSelect, "|");
                            if (slTempList.size() > 1) {
                                txtFilterSelect = (String)slTempList.get(1);
                            }else{
                                txtFilterSelect = (String)slTempList.get(0);
                            }

                            if(txtFilterSelect.lastIndexOf("\"]") > -1){
                                txtFilterSelect = txtFilterSelect.substring(0,txtFilterSelect.lastIndexOf("\"]"));
                            }
                            if(txtFilterSelect.lastIndexOf("\"") > -1){
                                txtFilterSelect = txtFilterSelect.substring(0,txtFilterSelect.lastIndexOf("\""));
                            }
                            if(filterExists(jsonFilters, fName, txtFilterSelect, dataType, isUOM)){
                                String transValue = getTranslatedValue(context,field,txtFilterSelect,languageStr);
                                if("".equals(txtDispSelect)){
                                    txtDispSelect = transValue;
                                }else{
                                    txtDispSelect += STR_REFINEMENT_SEPARATOR + transValue;
                                }
                            }
                        }*/
                        JSONArray txtJsonArr = (JSONArray)jsonFilters.getJSONArray(txtFieldName);
                        for (int i = 0; i < txtJsonArr.length(); i++) {
                            String tmpJsonVal = txtJsonArr.getString(i);
                            String txtFilterSelect = (String)FrameworkUtil.split(tmpJsonVal, "|").get(1);

                            if(filterExists(jsonFilters, txtFieldName, txtFilterSelect, dataType, isUOM)){
                                String transValue = getTranslatedValue(context,params,field,txtFilterSelect,languageStr);
                                if("".equals(txtDispSelect)){
                                    txtDispSelect = transValue;
                                }else{
                                    txtDispSelect += STR_REFINEMENT_SEPARATOR + transValue;
                                }
                            }
                        }
                    }
                    if(!noObjectsFound)
                    jsonAttribute.put("selectedValue",txtDispSelect);
                    jsonAttribute.put("disabled","false");
                }
                //353833 - end
                String txtJsonLatestRevision = "";
                String txtJsonLastRevision = "";
                String isSelected = "";
                if(isRevisionField(field.name,selectable) && jsonFilters.contains("LATESTREVISION")){
                    txtJsonLatestRevision = (String)jsonFilters.getString("LATESTREVISION");
                    StringList txtJsonLatestRevisionList = FrameworkUtil.split(txtJsonLatestRevision, "|");

                    if(txtJsonLatestRevisionList.size()>1)
                    isSelected = (String)txtJsonLatestRevisionList.get(1);
                    if(isSelected.startsWith("true")|| isSelected.startsWith("TRUE")){
                        jsonAttribute.put("latest","true");
                    }
                    if(canUse(paramFields)){
                    	jsonAttribute.put("disabled","true");
                    }
                }
                if(isRevisionField(field.name,selectable) && jsonFilters.contains("LASTREVISION") && !isSelected.startsWith("true")){
                    txtJsonLastRevision = (String)jsonFilters.getString("LASTREVISION");
                    StringList txtJsonLastRevisionList = FrameworkUtil.split(txtJsonLastRevision, "|");

                    if(txtJsonLastRevisionList.size()>1)
                    isSelected = (String)txtJsonLastRevisionList.get(1);
                    if(isSelected.startsWith("true") || isSelected.startsWith("TRUE")){
                        jsonAttribute.put("last","true");
                    }
                    if(canUse(paramFields)){
                    	jsonAttribute.put("disabled","true");
                    }
                }
                if (isRevisionField(field.name,selectable)) {
                    String highest = EnoviaResourceBundle.getProperty(context, LANG_RESOURCE_FILE, new Locale(languageStr), FTS_RES_KEY_PREFIX + "Highest");
                    String bystate = EnoviaResourceBundle.getProperty(context, LANG_RESOURCE_FILE, new Locale(languageStr), FTS_RES_KEY_PREFIX + "ByState");
                    String highestTitle =EnoviaResourceBundle.getProperty(context, LANG_RESOURCE_FILE, new Locale(languageStr), FTS_RES_KEY_PREFIX + "HighestTitle");
                    String bystateTitle = EnoviaResourceBundle.getProperty(context, LANG_RESOURCE_FILE, new Locale(languageStr), FTS_RES_KEY_PREFIX + "ByStateTitle");
                    String displayType = "";
                    if( !jsonFilters.contains("LASTREVISION") && !jsonFilters.contains("LATESTREVISION")){
                        String lastRevision = EnoviaResourceBundle.getProperty(context, "emxFramework.FullTextSearch.LASTREVISION");
                        String latestRevision = EnoviaResourceBundle.getProperty(context, "emxFramework.FullTextSearch.LATESTREVISION");
                        if(!("true".equalsIgnoreCase(lastRevision) && "true".equalsIgnoreCase(latestRevision))){
                            jsonAttribute.put("last", lastRevision);
                            jsonAttribute.put("latest", latestRevision);
                        }else{
                            jsonAttribute.put("latest", latestRevision);
                        }
                    }

                    if (!UISearchUtil.isAutonomySearch(context,params)) {
                        displayType = "TextBox";
                    }else if(field.parametric){
                        displayType = "Chooser";
                    }
                    jsonAttribute.put("DisplayType", displayType);
                    jsonAttribute.put("Highest", highest);
                    jsonAttribute.put("ByState", bystate);
                    jsonAttribute.put("HighestTitle", highestTitle);
                    jsonAttribute.put("ByStateTitle", bystateTitle);
            }
      }
            if (UISearchUtil.isFormMode(params)) {
                jsonAttribute.put("includePos", new Integer(lstIncludeFields.indexOf(fName)));
            } else if (bInclFirst) {
                int pos = lstIncludeFields.indexOf(fName);
                jsonAttribute.put("includePos", new Integer(pos >= 0 ? pos : 9999));
            }

            JSONArray jsonValues = new JSONArray();
            HashMap singleFieldCounts = (HashMap) allFieldCounts.get(fName);


			/* Mx361263 - added for i18n of vault strings (Local,All,Selected) */
			if("VAULT".equals(fName)){
				StringList vaultOptionsValue = new StringList();
				vaultOptionsValue.addElement(PersonUtil.SEARCH_DEFAULT_VAULT);
				vaultOptionsValue.addElement(PersonUtil.SEARCH_LOCAL_VAULTS);
				vaultOptionsValue.addElement(PersonUtil.SEARCH_ALL_VAULTS);
				vaultOptionsValue.addElement(PersonUtil.SEARCH_SELECTED_VAULTS);

				StringList vaultOptionsDisplayValue = new StringList();

				vaultOptionsDisplayValue.addElement(EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", new Locale(languageStr), "emxFramework.Preferences.UserDefaultVault"));
				vaultOptionsDisplayValue.addElement(EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", new Locale(languageStr), "emxFramework.Preferences.LocalVaults"));
				vaultOptionsDisplayValue.addElement(EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", new Locale(languageStr), "emxFramework.Preferences.AllVaults"));
				vaultOptionsDisplayValue.addElement(EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", new Locale(languageStr), "emxFramework.Preferences.SelectedVaults"));


				JSONArray vaultArray = new JSONArray();
				for(int i = 0; i < vaultOptionsValue.size(); i++) {
					JSONObject vaultSelectionAttribute = new JSONObject();
					String value = (String)vaultOptionsValue.get(i);
					String displayValue = (String)vaultOptionsDisplayValue.get(i);
					vaultSelectionAttribute.put("value", value);
					vaultSelectionAttribute.put("displayValue", displayValue);
					vaultArray.put(vaultSelectionAttribute);
				}

				jsonAttribute.put("vaultOptions", vaultArray);

				if(UISearchUtil.isAutonomySearch(context,params)){
					jsonAttribute.put("isAutonomySearch", "true");
				}else {
					jsonAttribute.put("isAutonomySearch", "false");
				}
			}

            String resetFieldTitle = EnoviaResourceBundle.getProperty(context, LANG_RESOURCE_FILE, new Locale(languageStr), FTS_RES_KEY_PREFIX + "ResetFieldTitle");
            jsonAttribute.put("ResetFieldTitle", resetFieldTitle);

			/* end Mx361263 */

            // In Nav mode, if only one value is available, don't show the attribute
            // EXCEPT if it one explicitly selected by the user,
            // or the one being currently queried,
            // or forced by inclusionList+showAtTop in which case we keep it
             if ((!UISearchUtil.isFormMode(params)
                && singleFieldCounts.size() <= 1
                && field.parametric == true
                && !fName.equalsIgnoreCase((String)params.get("currentField"))
                && !fName.equalsIgnoreCase((String)params.get("singleField"))
                    && !(lstIncludeFields.contains(fName) && bInclFirst))
                    && (!UISearchUtil.isFormMode(params)&& !filterExists(jsonFilters, fName)))

              {
                        continue;
              }
             jsonAttributes.put(jsonAttribute);


            // This check is made to not to include date values in to the array
			if((UISearchUtil.isAutonomySearch(context,params)
					|| fName.equalsIgnoreCase((String)params.get("singleField")))) {
            Iterator valuesIter = singleFieldCounts.entrySet().iterator();
            int maxLevels = 0;
            if("true".equals(TagsView) || allFieldCounts.size() >= 1) {
            while (valuesIter.hasNext()) {
                Entry e = (Entry) valuesIter.next();
                String value = (String) e.getKey();

                int count = -1;//((Integer)e.getValue()).intValue();

                JSONObject jsonValue = new JSONObject();
                if("sixwDisplay".equals(value) ||"sixw".equals(value)|| "dataTypetag".equals(value) ||  "fieldName".equals(value)  ){
                if("sixwDisplay".equals(value) ||"sixw".equals(value)){
                	jsonAttribute.put(value,e.getValue());
                	}
                }else{
                	count= ((Integer)e.getValue()).intValue();
                jsonValue.put("value", value);
                }
				String translatedValue = "";
				if(!UISearchUtil.isAutonomySearch(context,params) && field.attributes.get("chooserJPO") != null){
                    translatedValue = value;
				}else if("sixwDisplay".equals(value) ||"sixw".equals(value)|| "dataTypetag".equals(value) ||  "fieldName".equals(value)  ){
					 translatedValue = value;
				}else{
					translatedValue = getTranslatedValue(context,params,field,value,languageStr);
				}
				 if("sixwDisplay".equals(value) ||"sixw".equals(value)|| "dataTypetag".equals(value) ||  "fieldName".equals(value)){
					 //DO nothing
				 }else{
                jsonValue.put("displayValue", translatedValue);
				 }


                if (count != -1) {
                    jsonValue.put("count", new Integer(count));
                }
                if (mandatoryList.contains(fName)) {
                    jsonValue.put("mandatorySearch", Boolean.valueOf(true));
                    // jsonValue.put("selected", true);
                } else {
                    // jsonValue.put("mandatorySearch", false);
                    // jsonValue.put("selected", filterExists(jsonFilters,
                    // fName, value, dataType, isUOM));
                }
                jsonValues.put(jsonValue);
                if (fieldSep != null) {
                    StringList slvalueList = FrameworkUtil.split(value.toString(),fieldSep);
                    if(maxLevels < slvalueList.size()){
                        maxLevels = slvalueList.size();
                    }
                }
            }
			}
            //Commented for bug  IR-168379V6R2014
           /* if(UISearchUtil.isAutonomySearch(params) && field.selectable.equalsIgnoreCase("current")){
                jsonValues = new JSONArray();
                jsonValues = getStateList(context,params,field,singleFieldCounts);
            }*/

            if (!UISearchUtil.isAutonomySearch(context,params)) {
                String currentField = (String) params.get("currentField");
                if(currentField != null && !"".equalsIgnoreCase(currentField)) {
                    if(fName.equalsIgnoreCase(currentField)) {
                        //Config.Field field = _config.indexedBOField(currentField);
                        String chooserJPO = (String) field.attributes.get("chooserJPO");
                        if(chooserJPO != null && !"".equals(chooserJPO))
                        {
                            StringList progMethList = FrameworkUtil.split(chooserJPO, ":");
                            String program = (String)progMethList.get(0);
                            String method = (String)progMethList.get(1);
                            HashMap programMap = new HashMap();
                            programMap.put("requestMap", params);
                            programMap.put("currentField", currentField);
                            //programMap.put("fieldValues", params);
                            HashMap valuesMap = (HashMap) JPO.invoke(context, program, null, method, JPO.packArgs(programMap),HashMap.class);
                            jsonValues = new JSONArray();
                            if(valuesMap != null && valuesMap.size() > 0) {
                                Iterator itr = valuesMap.keySet().iterator();
                                while(itr.hasNext()) {
                                    String key = (String) itr.next();
									String displayValue = (String)valuesMap.get(key);
									StringList values   = FrameworkUtil.split(displayValue, "~");
									for(int i = 0; i < values.size(); i++)
									{
                                    JSONObject jsonValue = new JSONObject();
                                    jsonValue.put("value", key);
										jsonValue.put("displayValue", values.get(i));

										if (mandatoryList.contains(fName)) {
											jsonValue.put("mandatorySearch", Boolean.valueOf(true));
										}
										jsonValues.put(jsonValue);
									}
                                    if (fieldSep != null) {
                                        StringList slvalueList = FrameworkUtil.split(key,fieldSep);
                                        if(maxLevels < slvalueList.size()){
                                            maxLevels = slvalueList.size();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            jsonAttribute.put("levels", new Integer(maxLevels));
            sortJsonArrayOfObjects(jsonValues, "displayValue", "ascending", dataType);
            jsonAttribute.put("values", jsonValues);
            }
        }

        if (UISearchUtil.isFormMode(params)) {
            sortJsonArrayOfObjects(jsonAttributes, "includePos", "ascending", "integer");
        } else {
            sortJsonArrayOfObjects(jsonAttributes, "displayValue");
            if (bInclFirst) { // two level sort in this case
                sortJsonArrayOfObjects(jsonAttributes, "includePos", "ascending", "integer");
            }
        }
        return (jsonAttributes);
    }

    private String getTranslatedFieldLabel(Context context, Field field,
			HashMap params) throws Exception {
    	String languageStr = (String) params.get("languageStr");
    	String translatedName = "";
    	String fName = field.name;
    	String attributeActualName = "";
        String kind;
        String selectable = field.selectable;
        if (UOMUtil.isSimpleAttributeExpression(selectable)) {
            attributeActualName = UOMUtil.getAttrNameFromSelect(selectable);
            kind = "attribute";
        } else if (UOMUtil.isAttributeExpression(selectable)) {
            attributeActualName = UOMUtil.getAttrNameFromSelect(selectable);
            kind = "attribute";
        } else if (UISearchUtil.getBasics(context).contains(selectable.toLowerCase())) {
            kind = "basic";
        } else {
            kind = "other";
        }

        if(!UISearchUtil.isAutonomySearch(context,params)){
        	MapList fields = getFormFields(context,params);
        	for (int i = 0; i < fields.size(); i++) {
				HashMap formfield = (HashMap)fields.get(i);
				String fieldName = UIComponent.getName(formfield);
				String fieldLabel = UIComponent.getLabel(formfield);
				if(fieldName.equals(fName)){
					String fieldSuite = UIComponent.getSetting(formfield, "Registered Suite");
					String stringResourceFile = "";
					if(!"".equals(fieldSuite)){
						try{
							stringResourceFile = UINavigatorUtil.getStringResourceFileId(context,fieldSuite);
						}catch(Exception ex){
							stringResourceFile = LANG_RESOURCE_FILE;
						}
					}else{
						stringResourceFile = LANG_RESOURCE_FILE;
					}
					translatedName = EnoviaResourceBundle.getProperty(context, stringResourceFile, new Locale(languageStr), fieldLabel);
					break;
				}
        	}
    	}
        if (translatedName == null || "".equals(translatedName) || translatedName.startsWith("emxFramework.")) {
        	translatedName = EnoviaResourceBundle.getProperty(context,LANG_RESOURCE_FILE,new Locale(languageStr),FTS_RES_KEY_PREFIX.concat(fName));
        }
        if (translatedName != null && translatedName.startsWith("emxFramework.")) {
            translatedName = null;
            }
        if (UOMUtil.isSimpleAttributeExpression(selectable)) {
            attributeActualName = UOMUtil.getAttrNameFromSelect(selectable);
            if (translatedName == null) {
            translatedName = i18nNow.getAttributeI18NString(attributeActualName, languageStr);
            }
            kind = "attribute";
        } else if (UOMUtil.isAttributeExpression(selectable)) {
            attributeActualName = UOMUtil.getAttrNameFromSelect(selectable);
            if (translatedName == null) {
            translatedName = EnoviaResourceBundle.getProperty(context,LANG_RESOURCE_FILE,new Locale(languageStr),FTS_RES_KEY_PREFIX.concat(fName));
            }
            kind = "attribute";
        } else if (UISearchUtil.getBasics(context).contains(selectable.toLowerCase())) {
            if (translatedName == null) {
            translatedName = getBasicFieldI18nName(field.selectable, languageStr);
            }
            kind = "basic";
        } else {
            kind = "other";
        } if (translatedName == null || translatedName.startsWith("emxFramework.")) {
            translatedName = field.name;
        }
        String paramFieldLabels = (String) params.get("fieldLabels");
        if (canUse(paramFieldLabels)) {
            StringList fieldLabelList = FrameworkUtil.split(paramFieldLabels, ",");
            Iterator itr = fieldLabelList.iterator();
            while(itr.hasNext()){
                String fl = (String)itr.next();
                int fnlindex = fl.indexOf(fName+":");
                if(fnlindex == 0) {
                    translatedName = EnoviaResourceBundle.getProperty(context,LANG_RESOURCE_FILE,new Locale(languageStr),fl.substring(fName.length() + 1));
                    break;
                }
            }
        }

        if (translatedName == null) {
            translatedName = fName;
        }
        return translatedName;
	}

    private boolean hasSubTypes(Context context, String txtType) throws MatrixException {
		BusinessType btType = new BusinessType(txtType, context.getVault());
		return btType.getChildren(context).size() > 0;
	}

    private boolean isStateExist(Context context, String policyName, String value) throws Exception {
        // TODO Auto-generated method stub
        boolean retVal = false;
        String result = MqlUtil.mqlCommand(context, "print policy $1 select $2 dump",policyName,"state["+value+"].name");
        if(value.equals(result)){
            retVal = true;
        }
        return retVal;
    }

    private String getStateI18NString(Context context, HashMap params, String state, String languageStr) throws Exception {
    	String fullTextSearchTimestamp = (String)params.get("fullTextSearchTimestamp");
    	HashMap cacheMap = (HashMap)STATE_TRANSLATIONS_MAP.get(fullTextSearchTimestamp);
    	boolean isIndexSearch = UISearchUtil.isAutonomySearch(context,params);
    	if(cacheMap == null){
    		cacheMap = new HashMap();
    	}else if(cacheMap.get(state) != null){
    		return (String)cacheMap.get(state);
    	}

    	if(canUse(state) && state.indexOf(".") > -1){
            if(isIndexSearch)
            {
                String txtpolicy = (String)FrameworkUtil.split(state,".").get(0);
                String txtstate  = (String)FrameworkUtil.split(state,".").get(1);
                String stateKey  = "emxFramework.State." + txtpolicy + "." + txtstate;
                return getI18nString(context, stateKey,LANG_RESOURCE_FILE,languageStr);
            }else
            {
                return (String)FrameworkUtil.split(state,".").get(0);
            }
    	}
        boolean isTranslationComplete = false;

        String translatedStateValue = "";
        String fieldActual = (String)params.get("field_actual");
        UISearchUtil searchUtil = new UISearchUtil();
        Hashtable fieldActualMap = searchUtil.getFields(fieldActual,UISearchUtil.getFieldSeperator(context,params));
        String defaultActual = (String)params.get("default_actual");
        Hashtable defaultActualMap = searchUtil.getFields(defaultActual,UISearchUtil.getFieldSeperator(context,params));

        String stateParam = (String) defaultActualMap.get("CURRENT");
        if (!canUse(stateParam)) {
            stateParam = (String) fieldActualMap.get("CURRENT");
        }
        if(canUse(stateParam) && stateParam.indexOf(".") > -1){
            String tmpStr = (String)FrameworkUtil.split(stateParam,"|").get(1);
            StringList tempList = FrameworkUtil.split(tmpStr,",");
            for (int i = 0; i < tempList.size(); i++) {
                String tmpState = (String)tempList.get(i);
                String symbPolicyName = (String)FrameworkUtil.split(tmpState,".").get(0);
                String symbStateName = (String)FrameworkUtil.split(tmpState,".").get(1);
                String policyName = UISearchUtil.getActualNames(context, symbPolicyName);
                String stateName = PropertyUtil.getSchemaProperty(context, "policy", policyName, symbStateName);
                if(canUse(policyName) && canUse(stateName) && stateName.equals(state)){
                    String stateKey = "emxFramework.State." + policyName + "." + state;
                    String translatedValue = getI18nString(context, stateKey,LANG_RESOURCE_FILE,languageStr);
                    if (!translatedValue.startsWith("emxFramework.")) {
                        translatedStateValue = translatedValue;
                        isTranslationComplete = true;
                    }
                }
            }
        }

        JSONObject jsonFilters = extractFilters(params);
        StringList policyList = getFilterValues(jsonFilters , "POLICY");
        StringList typesList = getFilterValues(jsonFilters , "TYPES");
        if(typesList == null || typesList.size() <= 0){
        	typesList = getFilterValues(jsonFilters , "TYPE");
        }

        //Translate with the policy
        if(!isTranslationComplete){
            if(policyList != null && policyList.size() > 0){
                for (int i = 0; i < policyList.size(); i++) {
                    String strPolicy = (String)policyList.get(i);
                    String stateKey = "emxFramework.State." + strPolicy + "." + state;
                    String translatedValue = getI18nString(context, stateKey,LANG_RESOURCE_FILE,languageStr);
                    if (!translatedValue.startsWith("emxFramework.")) {
                        translatedStateValue = translatedValue;
                        isTranslationComplete = true;
                    }
                }
            }
        }


        //Translate with the type
        if(!isTranslationComplete){
    		if(isIndexSearch && (typesList == null || typesList.size() <= 0)){
    			Set TYPES_TAXONOMY_SET = TYPES_TAXONOMY_MAP.get(fullTextSearchTimestamp);
				if(TYPES_TAXONOMY_SET != null){
    			typesList = new StringList();
    			Iterator itr = TYPES_TAXONOMY_SET.iterator();
    			while(itr.hasNext()){
    				typesList.add(itr.next());
    			}
    		}
    		}
        	if(typesList == null || typesList.size() <= 0){
        		String txtType = EnoviaResourceBundle.getProperty(context, "emxFramework.GenericSearch.Types");
        		typesList = FrameworkUtil.split(UISearchUtil.getActualNames(context,txtType), ",");
        	}

        	MapList policyMapList = new MapList();

            for (int i = 0; i < typesList.size(); i++) {
            	StringBuffer sbPolicy = new StringBuffer();
            	String strType = (String) typesList.get(i);
            	try{
                    policyMapList = TYPES_POLICIES_MAP.get(strType);
                    if (policyMapList == null) {
                        policyMapList = mxType.getPolicies(context,strType,false);
                        TYPES_POLICIES_MAP.put(strType, policyMapList);
                    }
                }catch(Exception e){
                    //When business type does not exist then code will come here...
                    //Do Nothing
                }
                if(policyMapList != null){
                    java.util.Iterator policyListItr = policyMapList.iterator();
                    while(policyListItr.hasNext()){
                        HashMap policyMap=(HashMap)policyListItr.next();
                        String policyName = (String)policyMap.get("name");
                        String stateKey = "emxFramework.State." + policyName + "." + state;
                        String translatedValue = getI18nString(context, stateKey,LANG_RESOURCE_FILE,languageStr);
                        if (!translatedValue.startsWith("emxFramework.")) {
                        	translatedStateValue = translatedValue;
                            isTranslationComplete = true;
                            break;
                        }
                    }
                }
            }
        }


        if (translatedStateValue.startsWith("emxFramework.") || translatedStateValue.length() <= 0) {
            translatedStateValue = state;
        }
        if (cacheMap.get(state) == null) {
        	cacheMap.put(state, translatedStateValue);
		}
        STATE_TRANSLATIONS_MAP.put(fullTextSearchTimestamp,cacheMap);

        return translatedStateValue;
    }

	private String getI18nString(Context context, String propKey, String string,
			String languageStr) throws Exception {
		propKey = FrameworkUtil.findAndReplace(propKey, " ", "_");
		return EnoviaResourceBundle.getProperty(context, string, new Locale(languageStr), propKey);
	}

	protected StringList getFilterValues(JSONObject jsonFilters, String string) throws Exception {
        StringList returnList = new StringList();
        if (filterExists(jsonFilters,string)) {
        	JSONArray txtJsonArr = (JSONArray)jsonFilters.getJSONArray(string);
            for (int i = 0; i < txtJsonArr.length(); i++) {
                String txtJsonVal = txtJsonArr.getString(i);
                String txtFilterSelect = (String)FrameworkUtil.split(txtJsonVal, "|").get(1);

                returnList.add(txtFilterSelect);
            }
		}
        return returnList;
    }

    private String getFilterOperator(JSONObject jsonFilters, String string) throws MatrixException, Exception {
        String opr = "";
        if (filterExists(jsonFilters,string)) {
            JSONArray txtJsonArr = (JSONArray)jsonFilters.getJSONArray(string);
            for (int i = 0; i < txtJsonArr.length(); i++) {
                String txtJsonVal = txtJsonArr.getString(i);
                opr = (String)FrameworkUtil.split(txtJsonVal, "|").get(0);
                break;
            }
        }
        return opr;
    }

	private String getTranslatedValue(Context context,HashMap params, Config.Field field, String value,String languageStr) throws Exception {
        String attributeActualName = "";
        String kind;
        String selectable = field.selectable;
        String fieldName = field.name;
        if (UOMUtil.isSimpleAttributeExpression(selectable)) {
            attributeActualName = UOMUtil.getAttrNameFromSelect(selectable);
            kind = "attribute";
        } else if (UOMUtil.isAttributeExpression(selectable)) {
            attributeActualName = UOMUtil.getAttrNameFromSelect(selectable);
            kind = "attribute";
        } else if (UISearchUtil.getBasics(context).contains(selectable.toLowerCase())) {
            kind = "basic";
        } else {
            kind = "other";
        }
        String translatedValue;
        String format = (String)field.attributes.get("format");;
        if (selectable.equalsIgnoreCase("current") || selectable.toLowerCase().endsWith(".current")) {
            translatedValue = getStateI18NString(context, params, value, languageStr);
        } else if (selectable.equalsIgnoreCase("policy") || selectable.toLowerCase().endsWith(".policy")) {
            translatedValue = UISearchUtil.getActualNames(context,value);
            translatedValue=UINavigatorUtil.getAdminI18NString(context,"Policy", value, languageStr);
            
        } else if (selectable.equalsIgnoreCase("type")) {
            translatedValue=EnoviaResourceBundle.getAdminI18NString(context,"Type", value, languageStr);
        } else if (selectable.equalsIgnoreCase("vault")) {
            translatedValue=UINavigatorUtil.getAdminI18NString("Vault", value, languageStr);
        } else if (format  != null && format.equalsIgnoreCase("user")) {
            try {
                translatedValue=PersonUtil.getFullName(context,value);
            } catch (MatrixException me) {
                System.out.println("PersonUtil.getFullName(" + value + "): " + me.getMessage());
                translatedValue = "{" + value + "}";
            }
        } else if (kind.equals("attribute")) {
            translatedValue = i18nNow.getRangeI18NString(attributeActualName, value, languageStr);
        } else if (fieldName.equalsIgnoreCase("USERROLE") && UIUtil.isNotNullAndNotEmpty(value)){
        	translatedValue = i18nNow.getRoleI18NString(value, context.getSession().getLanguage());
        } else {
            translatedValue = UISearchUtil.getActualNames(context,value);
        }
        translatedValue = FrameworkUtil.findAndReplace(translatedValue,"\\,",",");
        return translatedValue;
    }

	protected void buildDynaTaxonomyTree(Context context, TaxonomyDimension td, SearchRefinement[] refinements,
	        String currLevelId, String lineage, HashMap params, JSONObject taxMap, JSONArray taxList, StringList fieldList) throws Exception
	    {
	        TaxonomyDimension tds[] = td.getTaxonomy().getDimensions();
	        String languageStr = (String) params.get("languageStr");
			String paramFields = (String) params.get("field");
			UISearchUtil searchUtil = new UISearchUtil();
            Hashtable fieldsMap = searchUtil.getFields(paramFields,UISearchUtil.getFieldSeperator(context,params));
	        JSONObject jsonFilters = extractFilters(params);
					for (int i = 0; tds != null && i < tds.length; i++) {
						JSONObject tempMap = new JSONObject();
						String typeName = tds[i].getTaxonomy().getName();
						tempMap.put("type", typeName);
    					   boolean isSelected = Boolean.valueOf(filterExists(jsonFilters, lineage, typeName, "string", false));
    					   tempMap.put("select", (Boolean)taxMap.get("select") || isSelected);
   						   tempMap.put("expand", isSelected);
						if (lineage.equals(UISearchUtil.FIELD_TYPES)) {
							tempMap.put("title", i18nNow.getTypeI18NString(typeName,languageStr)+"("+tds[i].getCount()+")");
							if(canUse((String)fieldsMap.get(UISearchUtil.FIELD_TYPES))){
								tempMap.put("hideCheckbox", true);
							}
                    	}else{
                            try {
                                tempMap.put("title", MqlUtil.mqlCommand(context,"print bus $1 select $2 dump ",typeName,"name")+"("+tds[i].getCount()+")");
                            }catch(MatrixException me){
                                //context is not having the access or object does not exist
                                continue;
                            }
                    	}
						 tempMap.put("taxonomyType",lineage);
						 tempMap.put("children", new JSONArray());
						 TaxonomyDimension temptds[] = tds[i].getTaxonomy().getDimensions();
						 if(temptds != null && temptds.length > 0){
							 JSONArray tempList1 = (JSONArray)taxMap.get("children");
							 tempList1.put(tempMap);
							 buildDynaTaxonomyTree(context, tds[i], refinements, td.getTaxonomy().getId(), lineage, params,tempMap,taxList,fieldList);
						 }else{
							 JSONArray tempList1 = (JSONArray)taxMap.get("children");
							 tempList1.put(tempMap);
						 }
					}
				}

    protected HashMap BuildTaxonomyTree(Context context, TaxonomyDimension td, SearchRefinement[] refinements,
        String currLevelId, String stopLevelId, HashMap params) throws Exception
    {
        HashMap taxMap = new HashMap();
        taxMap.put("@COUNT", new Integer(td.getCount()));
        taxMap.put("@ID", td.getTaxonomy().getId());
        // System.out.println(td.getTaxonomy().getName() + " = " +
        // td.getCount());
        TaxonomyDimension tds[] = td.getTaxonomy().getDimensions();

		/*if (tds == null) {
            return taxMap;
		}*/

        /* tds will be null only if getTaxomomies(..., false) was used */
		HashMap dummyMap = new HashMap();
		dummyMap.put("@COUNT", new Integer(-1));
		dummyMap.put("@ID",  "");

		JSONObject jsonFilters = extractFilters(params);
		String rootTaxonomy = "";
		if(tds != null && tds.length > 0){
			rootTaxonomy = tds[0].getTaxonomy().getRoot().getName();
        }

        if (tds == null || tds.length == 0) {
            //Search search = newSearchInstance(context, params);
            //tds = search.getTaxonomyDimensions(context, refinements, td.getTaxonomy().getId());
			tds = td.getTaxonomy().getDimensions();
			if(tds != null && tds.length > 0){
				taxMap.put("hasChild", dummyMap);
        }
			return taxMap;
		}else if(filterExists(jsonFilters,rootTaxonomy)){
            for (int i = 0; tds != null && i < tds.length; i++) {
                // System.out.println(tds[i].getTaxonomy().getName() + " = " +
                // tds[i].getCount());
                taxMap.put(tds[i].getTaxonomy().getName(), BuildTaxonomyTree(context, tds[i], refinements, td
                    .getTaxonomy().getId(), stopLevelId, params));
            }
			return taxMap;
		}else{
			if(params.get("parentTaxonomy") != null){
				for (int i = 0; tds != null && i < tds.length; i++) {
					 HashMap tempMap = new HashMap();
					//FIX for IR-029760V6R2011
					 tempMap.put("@COUNT", new Integer(tds[i].getCount()));
					 tempMap.put("@ID",  tds[i].getTaxonomy().getId());
					//FIX for IR-029760V6R2011
					 TaxonomyDimension temptds[] = tds[i].getTaxonomy().getDimensions();
					 if(temptds != null && temptds.length > 0){
						 tempMap.put("hasChild", dummyMap);
						 taxMap.put(tds[i].getTaxonomy().getName(), tempMap);
					 }else{
						 taxMap.put(tds[i].getTaxonomy().getName(), tempMap);
					 }
				}
			}else{
				taxMap.put("hasChild", dummyMap);
        }
        return taxMap;
    }
	}


    /** Added for Bug 352576
     * @param context
     * @param dimension
     * @param params
     * @param tempMap
     * @throws Exception
     */
    protected void PreBuildTaxTree(Context context, TaxonomyDimension[] tds, HashMap returnMap) throws Exception {
        if (tds != null) {
            for (int i = 0; i < tds.length; i++) {
                TaxonomyDimension dimension = tds[i];
                returnMap.put(dimension.getTaxonomy().getName(), dimension);
                TaxonomyDimension temptds[] = dimension.getTaxonomy().getDimensions();
                if(temptds != null ) {
                    PreBuildTaxTree(context, temptds, returnMap);
                }
            }
        }
    }

    Locale getLocale(HashMap params) {
        String languageStr = (String) params.get("languageStr");
        return i18nNow.getLocale(languageStr);
//      return new Locale(languageStr);
    }

    /**
     * Returns HashMap of taxonomy details
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            contains the packed HashMap of request paremeters
     * @throws Exception
     *             if the operation fails.
     * @since AEF 10.7.3
     */
    protected HashMap generateTaxonomyCounts(Context context, SearchRefinement refinements[], HashMap params)
        throws Exception
    {
        HashMap returnMap = new HashMap();
        String fullTextSearchTimestamp = (String)params.get("fullTextSearchTimestamp");
        HashMap cacheMap = (HashMap)TAXONOMIES_MAP.get(fullTextSearchTimestamp);
        if(cacheMap == null){
            cacheMap = new HashMap();
        }

        Set TYPES_TAXONOMY_SET = TYPES_TAXONOMY_MAP.get(fullTextSearchTimestamp);

        String stopNode = "";

        Search search = newSearchInstance(context, params);

        long b = System.currentTimeMillis();
        /*
         * NOTE: here we do getTaxonomies(..., ..., true) which recurses to
         * leaves. We have the option of passing false for no recursion. Then
         * recursion is done by BuildTaxonomyTree(..., stopNode) to limit the
         * recursion. Currently, that does not seem to be necessary. TODO: At
         * some point, if we decide we're not going to go that route, we should
         * strip out the stopNode stuff.
         */
        boolean includeCounts  = (UISearchUtil.isAutonomySearch(context,params) && UISearchUtil.includeCounts(params));
        String collectionName = (String) params.get("COLLECTION");
        if (canUse(collectionName)) {
        	includeCounts = true;
        }
        Taxonomy taxonomies[] = null;
        if(params.get("parentTaxonomy") != null && cacheMap.get("TAXONOMIES") != null){
            taxonomies = (Taxonomy[])cacheMap.get("TAXONOMIES");
        }else{
            taxonomies = search.getTaxonomies(context, refinements, includeCounts);
            cacheMap.put("TAXONOMIES",taxonomies);
        }

        if (UISearchUtil.isTimingMode(params)) {
            System.out.println(" getTaxonomies(): " + (System.currentTimeMillis() - b) + "ms");
        }
        for (int i = 0; i < taxonomies.length; i++) {
            Taxonomy t = taxonomies[i];
            String singleTaxonomy = (String)params.get("singleTaxonomy");
            if (canUse(singleTaxonomy)) {
                if (!singleTaxonomy.equalsIgnoreCase(t.getName())) {
                    continue;
                }
            }
            if (UISearchUtil.isDebug(params)) {
                dumpTaxonomy(context, t, -1, 0);
            }
            String lineage = t.getName();
            TaxonomyDimension[] tds = t.getDimensions();

            if (tds != null) {
                HashMap lineageInfo = new HashMap();
                UISearchUtil searchUtil = new UISearchUtil();
                String paramFields = (String) params.get("field");
                Hashtable fieldsMap = searchUtil.getFields(paramFields,UISearchUtil.getFieldSeperator(context,params));
                if(UISearchUtil.FIELD_TYPES.equals(lineage)){
                    String defaultFields = (String) params.get("default");
					if (canUse(defaultFields)) {
                        defaultFields = com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(defaultFields);
                    }
					/*Finding param types Start*/
                    if (canUse(paramFields)) {
                        paramFields = com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(paramFields);
                    }
                    Hashtable defaultsMap = searchUtil.getFields(defaultFields,UISearchUtil.getFieldSeperator(context,params));

					String txtType = (String) defaultsMap.get(UISearchUtil.FIELD_TYPES);
                    if(!canUse(txtType)){
						txtType = (String) fieldsMap.get(UISearchUtil.FIELD_TYPES);
					}

                    StringList defaultTypes = new StringList();
                    if (canUse(txtType)) {
                        StringList tempTypeList = FrameworkUtil.split(txtType, "^");
                        for (int itr = 0; itr < tempTypeList.size(); itr++) {
                            String tempType = (String)tempTypeList.get(itr);
                            StringList typeList = FrameworkUtil.split(tempType, "|");
                            String operator = (String) typeList.get(0);
                            boolean bEqOpr = (getOperator(operator) == AttributeRefinement.OPERATOR_EQUAL);
                            if (bEqOpr) {
                                txtType = (String) typeList.get(1);
                            }
                        }
                        defaultTypes = FrameworkUtil.split(txtType, ",");
                    }else{
                    	defaultTypes = UISearchUtil.getTypes(context, null, false);
                    }
			        if(defaultTypes.size() > 0) {
                    HashMap dimentionMap = new HashMap();
                    PreBuildTaxTree(context, tds, dimentionMap);
                        for (int p = 0; p < defaultTypes.size(); p++) {
                            String strTypeName = (String) defaultTypes.get(p);
                            if(strTypeName.startsWith("type_")){
                                strTypeName = PropertyUtil.getSchemaProperty(context, strTypeName);
                            }
                            TaxonomyDimension typeDim = (TaxonomyDimension) dimentionMap.get(strTypeName);
                            if(typeDim != null) {
                                lineageInfo.put(typeDim.getTaxonomy().getName(), BuildTaxonomyTree(context, typeDim, refinements, t.getId(), stopNode, params));
                            }
                        }
                    } else {
                        for (int p = 0; p < tds.length; p++) {
                            lineageInfo.put(tds[p].getTaxonomy().getName(), BuildTaxonomyTree(context, tds[p], refinements, t.getId(), stopNode, params));
                        }
                    }
			        if(TYPES_TAXONOMY_SET == null) {
			            TYPES_TAXONOMY_MAP.put(fullTextSearchTimestamp, lineageInfo.keySet());
			        }

                    /* Finding param types end*/
                } else {

					String txtTaxValue = (String) fieldsMap.get(lineage);
		        	if(params.get("parentTaxonomy") != null && txtTaxValue != null){
		        		StringList taxList = FrameworkUtil.split(txtTaxValue, "|");
		        		txtTaxValue = (String)taxList.get(1);
		        		HashMap dimentionMap = new HashMap();
				        PreBuildTaxTree(context, tds, dimentionMap);
				        TaxonomyDimension taxDim = (TaxonomyDimension) dimentionMap.get(txtTaxValue);
				        if(taxDim != null) {
			            	lineageInfo.put(taxDim.getTaxonomy().getName(), BuildTaxonomyTree(context, taxDim, refinements, t.getId(), stopNode, params));
			            }
		        	}else{
		        		for (int p = 0; p < tds.length; p++) {
			        		TaxonomyDimension loctds[] = tds[p].getTaxonomy().getDimensions();
			            	lineageInfo.put(tds[p].getTaxonomy().getName(), BuildTaxonomyTree(context, tds[p], refinements, t.getId(), stopNode, params));
			        	}
                    }
                }
                returnMap.put(lineage, lineageInfo);
            }
        }

        TAXONOMIES_MAP.put(fullTextSearchTimestamp,cacheMap);
        return returnMap;
    }

    protected String repeat(String str, int n) {
        StringBuffer b = new StringBuffer(str.length() * n);
        for (int i=0;i<n;i++) {
            b.append(str);
        }
        return b.toString();
    }
    protected String indent(int level) {
        return repeat(" ", level);
    }
    protected String indent(String str, int level) {
        return indent(level) + str;
    }

    protected void dumpTaxonomy(Context context, Taxonomy t, int count, int level) {
        String name = t.getName();
        if (level == 0) {
            System.out.println("-----------------\nTAXONOMY " + t.getName());
        } else {
            if (name.matches("[0-9]+[.][0-9]+[.][0-9]+[.][0-9]+")) {
                try {
                    name += " "
                        + MqlUtil.mqlCommand(context, "print bus $1 select $2 dump",true,t.getName(),"name").trim();
                } catch (Exception e) {
                    name = e.toString();
                }
            }
            System.out.println(indent(level) + name + " (" + count + ")");
        }
        TaxonomyDimension[] tds = t.getDimensions();
        if (tds == null)
            return;
        for (int i = 0; i < tds.length; i++) {
            TaxonomyDimension td = tds[i];
            dumpTaxonomy(context, td.getTaxonomy(), td.getCount(), level + 1);
        }
    }

    protected void dumpAttributes(Context context, AttributeDimension ads[]) throws Exception {
        System.out.println("----------------\nATTRIBUTES [" + ads.length + "]");
        for (int i = 0; i < ads.length; i++) {
            if (ads[i] == null) {
                System.out.println("  " + "<<NULL>>");
            } else {
                RecordCount counts[] = ads[i].getRecordCount();
                System.out.println("  " + ads[i].getName() + " [" + counts.length + " dimensions]");
                if (MAX_DIMS_OUTPUT != 0) {
                for (int j = 0; j < counts.length; j++) {
                        // limit output to some number of values per attribute
                        if (j == MAX_DIMS_OUTPUT - TAIL_DIMS_OUTPUT && counts.length > MAX_DIMS_OUTPUT + 1) {
                            int skip = counts.length - j - TAIL_DIMS_OUTPUT;
                            System.out.print("    ");
                            if (skip/100>0) {
                                System.out.print(repeat("#", skip/100));
                            } else {
                                System.out.print(repeat(".", skip%100));
                            }
                            System.out.println(" " + skip + " dimensions omitted ...");
                            j += skip;
                        }
                    System.out.println("    " + counts[j].getValue() + " (" + counts[j].getCount() + ")");
                }
            }
        }
    }
    }

    /**
     *
     */
    protected HashMap generateAttributeCounts(Context context, SearchRefinement refinements[],
        StringList lstIncludeFields, HashMap params) throws Exception
    {
        HashSet includeSet = new HashSet(lstIncludeFields);
        String clsOid = getSearchClassificationRefinmenent(context,refinements);
        String type = getSearchTypeRefinement(refinements);
        String singleField = (String)params.get("singleField");
        String TagsView = (String)params.get("TagsView");

        StringList  typeList    = FrameworkUtil.split(type, ",");

        if (!UISearchUtil.isFormMode(params)) {
        	for(Iterator typeItr	= typeList.iterator(); typeItr.hasNext();) {
        		type	= (String)typeItr.next();
	            if (type != null) {
	                // add non-parametric (not parametric, and not classification) fields for this type
	                StringList typeAttrFields = UISearchUtil.getFieldsForTypeAttributes(context, type, false, false, true);
	                includeSet.addAll(typeAttrFields);

	                if (UISearchUtil.isDebug(params)) {
	                    System.out.println("------------------------\nNON-PARAMETRIC TYPE FIELDS FOR " + type + ": [" + typeAttrFields.size() + "]");
	                    System.out.println("    " + FrameworkUtil.join(typeAttrFields, "\n    "));
	                }
	            }
        	}
            if (clsOid != null) {
                // add non-parametric (not parametric, and not classification) fields for this classification
                StringList clsAttrFields = UISearchUtil.getFieldsForClassificationAttributes(context, clsOid, false, false, true);
                includeSet.addAll(clsAttrFields);

                if (UISearchUtil.isDebug(params)) {
                    System.out.println("------------------------\nNON-PARAMETRIC CLASSIFICATION FIELDS FOR " + clsOid + ": [" + clsAttrFields.size() + "]");
                    System.out.println("    " + FrameworkUtil.join(clsAttrFields, "\n    "));
                }
            }
        }
        String fieldParam = (String) params.get("field");
        StringList fieldList = FrameworkUtil.split(fieldParam,":");
        HashMap returnMap = new HashMap();
        Search search = newSearchInstance(context, params);
        boolean includeCounts  = (UISearchUtil.isAutonomySearch(context,params) && UISearchUtil.includeCounts(params));
       String collectionName = (String) params.get("COLLECTION");
        if (canUse(collectionName)) {
        	includeCounts = true;
        }
        AttributeDimension dimensions[];
        TagDimension  Tdimensions[] = new TagDimension[0];
        if (UISearchUtil.isAutonomySearch(context,params)) {
            StringList classificationFields = new StringList();
            if (clsOid != null && !UISearchUtil.isFormMode(params)) {
                // get Get ,CLASSIFICATION fields for this classification
                StringList clsAttrFields = UISearchUtil.getFieldsForClassificationAttributes(context, clsOid, true, false, false);
                includeSet.addAll(clsAttrFields);

                if (UISearchUtil.isDebug(params)) {
                    System.out.println("------------------------\nCLASSIFICATION FIELDS FOR CLASSIFICATION " + clsOid + ": [" + clsAttrFields.size() + "]");
                    System.out.println("    " + FrameworkUtil.join(clsAttrFields, "\n    "));
                }
                classificationFields.addAll(clsAttrFields);
            }
            if (canUse(singleField)) {
                Config.Field field = _config.indexedBOField(singleField);
                if (field.parametric) {
                    if (UISearchUtil.isDebug(params)) {
                        System.out.println(
                                "getAttributeDimensions CASE 1 ("
                                + "AUTONOMY"
                                + "/SINGLE=" + singleField
                                + "/PARAMETRIC"
                                + (includeCounts ? "/COUNTS" : "/NOCOUNTS")
                                + "):");
                        System.out.println("    " + FrameworkUtil.join(classificationFields, "\n    "));
            }
            dimensions = search.getAttributeDimensions(context, refinements,
                    (String[]) classificationFields.toArray(new String[classificationFields.size()]),
                            new String[] {singleField},
                    includeCounts);
        } else {
                    if (UISearchUtil.isDebug(params)) {
                        System.out.println(
                                "getAttributeDimensions CASE 2 ("
                                + "AUTONOMY"
                                + "/SINGLE=" + singleField
                                + "/NON-PARAMETRIC"
                                + (includeCounts ? "/COUNTS" : "/NOCOUNTS")
                                + "):");
                    }
                    // Unfortunately client side may call us for non-parametric singleField...
                    dimensions = new AttributeDimension[0];
                }
            } else {
                if (UISearchUtil.isDebug(params)) {
                    System.out.println(
                            "getAttributeDimensions CASE 3 ("
                            + "AUTONOMY"
                            + "/ALL"
                            + (includeCounts ? "/COUNTS" : "/NOCOUNTS")
                            + "):");
                    System.out.println("    " + FrameworkUtil.join(classificationFields, "\n    "));
                }
                if(!"true".equals(TagsView)){
				    if(search instanceof XLSearch){
                	    dimensions = ((XLSearch)search).getAttributeDimensions(context, refinements,
                                          (String[]) classificationFields.toArray(new String[classificationFields.size()]),
                                          null,
                                          includeCounts,
        					              2);
                    }else{
                        dimensions = ((AutonomySearch)search).getAttributeDimensions(context, refinements,
                                          (String[]) classificationFields.toArray(new String[classificationFields.size()]),
                                          null,
                                          includeCounts,
					                      2);
                    }
                }else{
                	dimensions = new AttributeDimension[0];
			    }


                    XLSearch asearch = new XLSearch(context);//juk-added
                    asearch.setLanguage(getLocale(params));
                    if("true".equals(TagsView)){
                    	Tdimensions = ((XLSearch)asearch).getTagDimensions(context, refinements,true);
                    }


                /*  To replace below invocation with this c
                 * ommented line once core changes appear in V6R2010x
                dimensions = ((AutonomySearch)search).getAttributeDimensions(context, refinements,
                    (String[]) classificationFields.toArray(new String[classificationFields.size()]),
                    null,
                    includeCounts); // We filter out attrs with one value only, so get two to detect >1
                */
            }
        } else {
            if (UISearchUtil.isDebug(params)) {
                System.out.println("getAttributeDimensions CASE 4 (MATRIX)");
            }
            dimensions = search.getAttributeDimensions(context, refinements, includeCounts);
        }

        if (UISearchUtil.isDebug(params)) {
            dumpAttributes(context, dimensions);
        }

        StringList roleList = getRolesList(context, params);

        //juk-added
        for (int i = 0; i < Tdimensions.length; i++) {

            String fieldName = Tdimensions[i].getName();
            String sixw = Tdimensions[i].get_sixw();
            String sixwDisplay = Tdimensions[i].get_sixwDisplay();
            int data_type = Tdimensions[i].getType();

            RecordCount rc[] = Tdimensions[i].getRecordCount();
	                HashMap valueMap = new HashMap();
	                for (int j = 0; j < rc.length; j++) {
	                    valueMap.put(rc[j].getValue(), new Integer(rc[j].getCount()));
	                }
	                valueMap.put("sixw", sixw);
	                valueMap.put("sixwDisplay", sixwDisplay);
	                //valueMap.put("dataTypetag", Integer.toString(data_type));
	                //valueMap.put("fieldName", fieldName);
	                if(!fieldName.equalsIgnoreCase(TAG_TYPE_IMPLICIT)){
						fieldName = "ExplicitTags"+i;
	            }
					else
						fieldName = "ImplicitTags"+i;
	                returnMap.put(fieldName, valueMap);
            }


        for (int i = 0; i < dimensions.length && !"true".equals(TagsView); i++) {
            if (dimensions[i] == null) {
                continue;
            }
            String fieldName = dimensions[i].getName();
            /*#372792 I am not sure why I am getting some attributes...*/
            Config.Field field = _config.indexedBOField(fieldName);
            if (/*!fieldName.equals(singleField) &&*/ // need to distinguish between 1 and not 1
                ( "OBJECTID".equalsIgnoreCase(fieldName) ||
                  "LASTREVISION".equalsIgnoreCase(fieldName) ||
                  "LATESTREVISION".equalsIgnoreCase(fieldName)))
            {
                continue;
            }

            /*
             * In form mode given include field list exactly must be returned,
             * it's not a mimimum subset it's the complete set.
             */
            if (UISearchUtil.isFormMode(params) && !includeSet.contains(fieldName)) {
                includeSet.remove(fieldName); // mark as already processed
                continue;
            } else {
                includeSet.remove(fieldName); // mark as already processed
            }

            RecordCount rc[] = dimensions[i].getRecordCount();
            if (fieldName.equals(singleField)) {
            HashMap valueMap = new HashMap();
                if ("USERROLE".equalsIgnoreCase(fieldName)) {
                    for (int j = 0; j < rc.length; j++) {
                        if (roleList != null && roleList.contains(rc[j].getValue())) {
                            valueMap.put(rc[j].getValue(), new Integer(rc[j].getCount()));
                        }
                    }
                }
                if (valueMap.size() == 0) {
                    for (int j = 0; j < rc.length; j++) {
                        valueMap.put(rc[j].getValue(), new Integer(rc[j].getCount()));
                    }
                }
                //IR-153451V6R2013 IR-153446V6R2013.. parametric not showing as list.
                if (valueMap.size() > 0) {
                         returnMap.put(fieldName, valueMap);
                    }
            } else if(field != null && field.parametric) {
                /*#372792 I am not sure why I am getting some attributes...*/
                HashMap valueMap = new HashMap();
                for (int j = 0; j < rc.length; j++) {
                    valueMap.put(rc[j].getValue(), new Integer(rc[j].getCount()));
                }
                returnMap.put(fieldName, valueMap);
            }else{
                returnMap.put(fieldName, new HashMap());
            }
        }

        /*
         * Inclusion list items that have not already been added must now be
         * added
         */
        if (includeSet != null && !canUse(singleField) && !"true".equals(TagsView)) {
            Iterator it = includeSet.iterator();
            while (it.hasNext()) {
                String fName = (String) it.next();
                returnMap.put(fName, new HashMap());
            }
        }
        // if singleField is not parametric, this will return something
        if (canUse(singleField) && !returnMap.containsKey(singleField) && !"true".equals(TagsView)) {
			/* Mx361263 */
			HashMap vaultMap = new HashMap();
			String languageStr = (String)params.get("languageStr");
			if("VAULT".equals(singleField) && !UISearchUtil.isAutonomySearch(context,params)){
				MapList vaultsList = new MapList();
				vaultsList = VaultUtil.getVaults(context);
				Iterator itr = vaultsList.iterator();
				while(itr.hasNext())
				{
					Map vaultMap2 = (Map) itr.next();
					String vault = (String) vaultMap2.get(DomainConstants.SELECT_NAME);
					Integer tempInt = new Integer(-1);
					vaultMap.put(vault, tempInt);
				}
			}
			//returnMap.put(singleField, new HashMap());
			returnMap.put(singleField, vaultMap);
			/* end Mx361263 */
        }

        if (!UISearchUtil.isFormMode(params) && !"true".equals(TagsView) && !canUse(singleField)) {
            //Non Parametric attributes of the type passed and its parent type[s]

            Iterator typeItr = typeList.iterator();

            while(typeItr.hasNext()){
                String[] nonParaAttrToBeAdded = Search.getFieldNames(context,(String)typeItr.next());
            for (int i = 0; i < nonParaAttrToBeAdded.length; i++) {
                if(!returnMap.containsKey(nonParaAttrToBeAdded[i])){
                    Config.Field field = _config.indexedBOField(nonParaAttrToBeAdded[i]);
					if(field != null){
                        String val = (String)field.attributes.get("classification");
                        if(val != null && "true".equals(val)){
                            continue;
                        }
                    }
					//IR-041776V6R2011 & IR-044376V6R2011
					if ("attribute[".indexOf(field.selectable) != 0) {
                        returnMap.put(nonParaAttrToBeAdded[i], new HashMap());
                    }
                }
            }
        }
        }
        return returnMap;
    }
    
    private String getDisplayValue(Context context, String valueToBeTranslated, String policyStr, String lang, String adminType){
    	String text = "";
		StringBuilder strBuilder=new StringBuilder("");
		strBuilder.append("emxFramework.");
		if(adminType.equals("type")){
			adminType = "Type";
		}else if(adminType.equals("policy")){
			adminType = "Policy";
		}
		strBuilder.append(adminType);
		if (UIUtil.isNotNullAndNotEmpty(policyStr)) 
		{
			 
			 strBuilder.append(".");
			 strBuilder.append(policyStr.replace(' ', '_'));
		} 
		strBuilder.append(".");
	    strBuilder.append(valueToBeTranslated.replace(' ', '_'));
	    text=strBuilder.toString();
	    String returnString = valueToBeTranslated;
	    try {
	    	 String I18NreturnString=EnoviaResourceBundle.getFrameworkStringResourceProperty(context, text, new Locale(lang));
	    	 if (UIUtil.isNotNullAndNotEmpty(I18NreturnString)) 
	    	 {
	 	        if (I18NreturnString.equals(text))
	 	            returnString = valueToBeTranslated;
	 	        else
	 	            returnString = I18NreturnString;
	 		}
	    } catch (Exception e) {
	        //Do Nothing Value Already Set
	        //String must not have been in Property File or another Exception
	    }
	    return returnString;
    }
    
    private String getAdminValue(String sixw){
    	String retVal = null;
		if(sixw != null){
			String [] splittedSixw = sixw.split("/");
			retVal = (splittedSixw[splittedSixw.length - 1].split(":"))[1];
		  }else{
			retVal = "";
		}
    	return retVal;
    }

    /**
     * Constructor.
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            holds no arguments.
     * @throws Exception
     *             if the operation fails.
     * @since EC 9.5.JCI.0.
     */
    public emxAEFFullSearchBase_mxJPO(Context context, String[] args) throws Exception {
        super();
        _config = Config.getInstance(context);

    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            holds no arguments.
     * @return int.
     * @throws Exception
     *             if the operation fails.
     */
    public int mxMain(Context context, String[] args) throws Exception {
        if (true) {
            String languageStr = context.getSession().getLanguage();
            String exMsg = EnoviaResourceBundle.getProperty(context, LANG_RESOURCE_FILE, new Locale(languageStr), "emxFramework.Message.Invocation");
            exMsg += EnoviaResourceBundle.getProperty(context, LANG_RESOURCE_FILE, new Locale(languageStr), "emxFramework.AEFFullSearch");
            throw new Exception(exMsg);
        }
        return 0;
    }

    /**
     * Returns string "true" if the given object is the most current revision
     * among all the revisions of that object that that are in the same
     * particular state that the object is in. (This may or may not be the
     * latest revision overall for the object.) Returns "false" otherwise.
     *
     * @param context
     * @param args
     *            args[0] is an OID
     * @return true if object is most recent rev in its state, false otherwise.
     * @throws Exception
     */
    public String getLatest(Context context, String[] args) throws Exception {
        DomainObject domObj = new DomainObject(args[0]);
        String id = domObj.getInfo(context, "id");
        String state = domObj.getInfo(context, "current");
        String data = "";
        if(!UIUtil.isNullOrEmpty(id)){
          data = MqlUtil.mqlCommand(context,"PRINT BUS $1 SELECT $2 $3 dump $4",id,"revisions.id","revisions.current","~");
        }
        StringList rows = FrameworkUtil.split(data, "~");
        int numPairs = rows.size() / 2;
        for (int i = numPairs - 1; i >= 0; i--) {
            String revId = (String) rows.get(i);
            String revState = (String) rows.get(i + numPairs);
            if (revState.equals(state)) {
                return "" + revId.equals(id);
            }
        }
        throw new FrameworkException("Did not find state " + state + " for oid " + id);
    }

    /**
     * Returns string "true" if the given object is the latest revision of that
     * object, "false" otherwise
     *
     * @param context
     * @param args
     *            args[0] is the oid
     * @return
     * @throws Exception
     */
    public String lastRevision(Context context, String[] args) throws Exception {
        DomainObject domObj = new DomainObject(args[0]);
        // note: getInfo() doesn't work for this; use getInfoList()
        StringList stRevList = domObj.getInfoList(context, "evaluate[revision==last]");
        return stRevList.get(0).toString().trim().toLowerCase();
    }

    /**
     * Returns true if the field is a revision field
     * @param fieldName
     * @param selectable
     * @return isRevision
     * @throws Exception
     */
    public boolean isRevisionField(String fieldName, String selectable) throws Exception {
        boolean isRevision = false;
        if("revision".equalsIgnoreCase(selectable) || selectable.toLowerCase().endsWith(".revision")
                || "REVISION".equalsIgnoreCase(fieldName)){
            isRevision = true;
        }
        return isRevision;
    }

    public StringList getTaxonomyNames(Context context) throws Exception {
        StringList res = new StringList();
        Config.Taxonomy taxonomies[] = _config.taxonomies();
        for (int i = 0; i < taxonomies.length; i++) {
            res.add(taxonomies[i].name);
        }
        return res;
    }

    /**
     * Returns true if the taxonomy field has hidden attribute set to
     * true in config.xml .Returns "false" otherwise.
     * @param context
     * @param taxonomyname
     * @throws Exception
     */
    protected boolean isTaxonomyHidden(Context context,String taxonomyname) throws Exception {
        String sHidden ="";
        Config.Taxonomy taxonomies[] = _config.taxonomies();
        for (int i = 0; i < taxonomies.length; i++) {
            if(taxonomyname.equals(taxonomies[i].name)){
            	sHidden = (String) taxonomies[i].attributes.get("hidden");
            }
        }
		boolean isHidden = "true".equalsIgnoreCase(sHidden)? true: false;
        return isHidden;
    }

    protected SearchRefinement[] GetSearchRefinements(Context context, HashMap params) throws Exception {
        String fullTextSearchTimestamp = (String) params.get("fullTextSearchTimestamp");
        String refreshRefinements = (String) params.get("refreshRefinements");

        boolean isIndexSearch = UISearchUtil.isAutonomySearch(context,params);
        StringList taxonomyNames = getTaxonomyNames(context);
        ArrayList refinementList = new ArrayList();

        UISearchUtil searchUtil = new UISearchUtil();
        String paramFields = (String) params.get("field");
//      if (canUse(paramFields)) {
// Bug 351815 paramFields = com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(paramFields);
//      }

        StringList filterKeys = new StringList();
        String ftsFilters = (String) params.get("ftsFilters");

        String typeAheadMapping = (String)params.get("Type Ahead Mapping");
        if(typeAheadMapping == null) typeAheadMapping = "";
        StringList typeAheadMappingList = FrameworkUtil.split(typeAheadMapping, ",");


        boolean isTypeDefined = false;
        boolean ignoreSysProp = false;

        // Modified for IR-205371V6R2014-With this we send exclusion refinement to core only in Real-Time.
        if (!UISearchUtil.isAutonomySearch(context,params)) {
        String exclusionTypes = EnoviaResourceBundle.getProperty(context, "emxFramework.Types.ExclusionList");
        exclusionTypes = UISearchUtil.getActualNames(context, exclusionTypes);
        TaxonomyRefinement excludeRefinement = new TaxonomyRefinement(UISearchUtil.FIELD_TYPES, exclusionTypes, false);
        refinementList.add(excludeRefinement);
        }

        if (UIUtil.isNotNullAndNotEmpty(ftsFilters)) {
// Bug 351815   filters = com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(filters);
            JSONObject jsonFilters = extractFilters(params);
            JSONArray jsonValues;
            String fieldName;

            //  in Matrix search mode, must default VAULT to user preferred or system default
            //if (!UISearchUtil.isAutonomySearch(params)) {
                if (!jsonFilters.contains("VAULT")) {
                   /* modified for 361263 */
                   //String vaultDefaultSelection = PersonUtil.getSearchDefaultSelection(context);
                	if(!"VAULT".equalsIgnoreCase((String)params.get("currentField"))){
                		String vaultDefaultSelection = PersonUtil.getSearchDefaultVaults(context);

                		if(vaultDefaultSelection == null || "".equals(vaultDefaultSelection)) {
                			vaultDefaultSelection = EnoviaResourceBundle.getProperty(context, "emxFramework.DefaultSearchVaults");
                		}

                		/* modified for 361263 */
                		//SearchRefinement vaultRefinement = createRefinement("BOVAULT", vaultDefaultSelection, AttributeRefinement.OPERATOR_EQUAL);
                		AttributeRefinement vaultRefinement;
                		if(UISearchUtil.isCaseSensitiveSearch(context, params)){
                			vaultRefinement = new CaseSensitiveAttributeRefinement("BOVAULT", vaultDefaultSelection, AttributeRefinement.OPERATOR_EQUAL,UISearchUtil.isCaseSensitive(params));
                		}else{
                			vaultRefinement = new AttributeRefinement("BOVAULT", vaultDefaultSelection, AttributeRefinement.OPERATOR_EQUAL);
                		}
                		refinementList.add(vaultRefinement);
                	}
                }

        	ComplexRefinement typeMappingCompRefinement = new ComplexRefinement(ComplexRefinement.LOGICAL_OPERATOR_OR);


			String TagsView = (String) params.get("TagsView");
			ComplexRefinement compRefinementORJUK = new ComplexRefinement(ComplexRefinement.LOGICAL_OPERATOR_OR);
            Iterator filterKeyIter = jsonFilters.keys();
            while (filterKeyIter.hasNext()) {
                fieldName = (String) filterKeyIter.next();
                //changed for bug 358479
                StringBuffer fieldValue = new StringBuffer("");
                int opr = 0;
                jsonValues = jsonFilters.getJSONArray(fieldName);

                /*
                 * Special case: in matrix search mode, TYPE field is treated
                 * like TYPES which generates a TaxonomyRefinement, which is
                 * hierarchical (expand type), which is what we want; not so
                 * with TYPE. So why not just put TYPES as field on the UI to
                 * begin with instead of TYPE? The answer is that we
                 * wanted the ability to use configurable chooserURL for the UI,
                 * and that can only be done with FIELD TYPE, not with TAXONOMY
                 * TYPES.
                 */
                if (fieldName.equals("TYPE") &&
                    UISearchUtil.isFormMode(params))
                {
                    fieldName = "TYPES";
                }

                Config.Field field = _config.indexedBOField(fieldName);
                boolean isTimeStampField = false;
                String fieldOpr = "";
                String fieldSep = "";
                int attrType = 0;
                if (field != null && field.selectable != null) { // is null for taxonomies
					attrType = field.type;
                    if (field.selectable.equalsIgnoreCase("originated") || field.selectable.equalsIgnoreCase("modified") || attrType == Config.PARAMETRIC_DATE || attrType == Config.DATE) {
                        isTimeStampField = true;
                    }
                    fieldOpr = (String) field.attributes.get("fieldOperator");
                    fieldSep = (String) field.attributes.get("fieldSeparator");
                }
				StringBuffer policyRefinemnt = new StringBuffer();
				boolean addPolicyRefinement = false;

                if (jsonValues.length() > 0) {
                    filterKeys.addElement(fieldName);
                    boolean isAttributeRefinement = false;
                    boolean isTaxonomyRefinement = false;
                    boolean isReferenceRefinement = false;
                    StringList taxonomyValueList = new StringList();
                    ComplexRefinement compRefinement = new ComplexRefinement(ComplexRefinement.LOGICAL_OPERATOR_AND);
                    /* This logic needs to be beefed up */
                    /*
                     * currently if there is any Equals operator, entire
                     * complexrefinement is OR
                     */
                    /* otherwise, it is AND */
                    for (int k = 0; k < jsonValues.length(); k++) {
                        String jsonValue = jsonValues.getString(k);
                        String operatorString = jsonValue.substring(0, jsonValue.indexOf("|"));
                        if (operatorString.equalsIgnoreCase("Equals") && "OR".equals(fieldOpr) && fieldSep != null && !"".equals(fieldSep)) {
                            compRefinement = new ComplexRefinement(ComplexRefinement.LOGICAL_OPERATOR_OR);
                        }
                    }

					Map stateFieldValue = new HashMap();
                    for (int j = 0; j < jsonValues.length(); j++) {
                        String jsonValue = jsonValues.getString(j);
                        String operatorString = jsonValue.substring(0, jsonValue.indexOf("|"));
                        String value = jsonValue.substring(jsonValue.indexOf("|") + 1, jsonValue.length());

						if(TagsView!= null && "true".equals(TagsView) && value!=null && value.indexOf("TagsAttr_Explicit____")>=0){
							value =  value.replace("TagsAttr_Explicit____", "");
							refinementList.add(new TagRefinement(value, "xsd:string", TAG_TYPE_EXPLICIT, fieldName));
	                        continue;
                        }
						if(TagsView!= null && "true".equals(TagsView) && value!=null && value.indexOf("TagsAttr_Implicit____")>=0){
                        	String[] paramArray =value.split("____");
                        	compRefinement.addRefinement(new TagRefinement(paramArray[2], "xsd:"+paramArray[1], TAG_TYPE_IMPLICIT, fieldName));
	                        continue;
                        }
						value =  value.replace("TagsAttr_Implicit____date____", "");
						value =  value.replace("TagsAttr_Implicit____string____", "");
						value =  value.replace("TagsAttr_Explicit____", "");
                        if (field != null && field.selectable != null) { // is null for taxonomies
                            if (UOMUtil.isAttributeExpression(field.selectable)) {
                                String attrName = UOMUtil.getAttrNameFromSelect(field.selectable);
                                if (UOMUtil.isAssociatedWithDimension(context, attrName)) {
                                        StringList valueSplit = FrameworkUtil.split(value, " ");
                                        if (valueSplit.size() > 1) { // allow for multiple spaces
                                            String num = (String)valueSplit.get(0);
                                            String unit = (String)valueSplit.get(valueSplit.size() -1);
                                            value = UOMUtil.convertToDefaultUnit(context, attrName, num, unit);
                                        }
                                }
                            }
                        }
                        int op = getOperator(operatorString);
                        //changed for bug 358479
                        opr = op;
                        if (fieldName.equals(UISearchUtil.FIELD_TYPES)) {
                            isTypeDefined = true;
                            isTaxonomyRefinement = true;
              //Bug:361452
              Hashtable fieldsMap = searchUtil.getFields(paramFields,UISearchUtil.getFieldSeperator(context,params));
              String txtType = (String) fieldsMap.get(UISearchUtil.FIELD_TYPES);
              StringList tmpTypeList = FrameworkUtil.split(txtType, "^");
              TaxonomyRefinement refinementFromField = null;
              for (int i = 0; i < tmpTypeList.size(); i++) {
                  txtType = (String) tmpTypeList.get(i);
                  StringList typeList = FrameworkUtil.split(txtType, "|");
                  int operator = getOperator((String) typeList.get(0));
                  txtType = (String) typeList.get(1);
                  boolean bEqualsOpr = (operator == AttributeRefinement.OPERATOR_EQUAL);
                  if (!bEqualsOpr) {
                      StringList searchTypes = UISearchUtil.getTypes(context, txtType, false);
                      String csl = FrameworkUtil.join(searchTypes, ",");
                      refinementFromField = new TaxonomyRefinement( UISearchUtil.FIELD_TYPES, csl, bEqualsOpr);
                      break;
                  }
              }
                            StringList searchTypes = UISearchUtil.getTypes(context, value, false);
                            String csl = FrameworkUtil.join(searchTypes, ",");
                            TaxonomyRefinement refinement = new TaxonomyRefinement(fieldName, csl);
              if (refinementFromField != null) {
                  ComplexRefinement typeRefinement = new ComplexRefinement(ComplexRefinement.LOGICAL_OPERATOR_AND);
                  typeRefinement.addRefinement(refinement);
                  typeRefinement.addRefinement(refinementFromField);
        						//refinementList.add(typeRefinement);
                				if(!typeAheadMappingList.contains(fieldName))
                					refinementList.add(refinement);
                				else
                					typeMappingCompRefinement.addRefinement(refinement);
              } else {
                            taxonomyValueList.add(csl);
                            //TaxonomyRefinement refinement = new TaxonomyRefinement(fieldName, csl);
                            //refinementList.add(refinement);
              }

                            compRefinement = null;
						//BUG:: IR-019952V6R2011 START sl9
						} else if (fieldName.equals(UISearchUtil.FIELD_TYPE)) {
							isTypeDefined = true;
							Hashtable fieldsMap = searchUtil.getFields(paramFields,UISearchUtil.getFieldSeperator(context,params));
							String txtType = (String) fieldsMap.get(UISearchUtil.FIELD_TYPE);
							StringList tmpTypeList = FrameworkUtil.split(txtType, "^");
							for (int i = 0; i < tmpTypeList.size(); i++) {
								txtType = (String) tmpTypeList.get(i);
								StringList typeList = FrameworkUtil.split(txtType, "|");
								int operator = getOperator((String) typeList.get(0));
								txtType = (String) typeList.get(1);
								boolean bEqualsOpr = (operator == AttributeRefinement.OPERATOR_EQUAL);
								if (!bEqualsOpr) {
									StringList searchTypes = UISearchUtil.getTypes(context, txtType, false);
									String csl = FrameworkUtil.join(searchTypes, ",");
									TaxonomyRefinement refinement = new TaxonomyRefinement(UISearchUtil.FIELD_TYPES, csl, bEqualsOpr);
									refinementList.add(refinement);
								}
							}

							compRefinement = null;
						}
						//BUG:: IR-019952V6R2011 START sl9
						    else if (taxonomyNames.contains(fieldName)) {
                            isTaxonomyRefinement = true;
                            //TaxonomyRefinement refinement = new TaxonomyRefinement(fieldName, value);
                            taxonomyValueList.add(value);
                            //refinementList.add(refinement);
                            compRefinement = null;
                        } else if (fieldName.equals(UISearchUtil.TEXTSEARCH)) {
                            String v = value.trim();
                            if (!v.equals("") && !v.equals("*")) {
                                TextRefinement refinement = new TextRefinement(value);
                                refinementList.add(refinement);
                            }
                            compRefinement = null;
                        } else if(isTimeStampField && (TagsView == null || !"true".equals(TagsView))){
                                double clientTZOffset    = (new Double((String)params.get("timeZone"))).doubleValue();
                                Locale loc = getLocale(params);
                                AttributeRefinement refinement = null;
                            	ComplexRefinement compRefinementOR = null;
                                switch(op)
                                {
                                    case AttributeRefinement.OPERATOR_EQUAL:
                                    	ComplexRefinement compRefinementAND = new ComplexRefinement(ComplexRefinement.LOGICAL_OPERATOR_AND);
                                        refinement = new AttributeRefinement(fieldName, eMatrixDateFormat.getFormattedInputDateTime(context,value,"11:59:59 PM",clientTZOffset,loc), getOperator("LESS"));
                                    	compRefinementAND.addRefinement(refinement);
                                        refinement = new AttributeRefinement(fieldName, eMatrixDateFormat.getFormattedInputDateTime(context,value,"12:00:00 AM",clientTZOffset, loc), getOperator("GREATER"));
                                    	compRefinementAND.addRefinement(refinement);

                                    	compRefinementOR = new ComplexRefinement(ComplexRefinement.LOGICAL_OPERATOR_OR);
                                    	refinement = new AttributeRefinement(fieldName, eMatrixDateFormat.getFormattedInputDateTime(context,value,"12:00:00 AM",clientTZOffset, loc), op);
                                    	compRefinementOR.addRefinement(refinement);
                                    	refinement = new AttributeRefinement(fieldName, eMatrixDateFormat.getFormattedInputDateTime(context,value,"11:59:59 PM",clientTZOffset, loc), op);
                                    	compRefinementOR.addRefinement(refinement);

                                    	compRefinementOR.addRefinement(compRefinementAND);
                                    	compRefinement.addRefinement(compRefinementOR);
                                        break;
                                    case AttributeRefinement.OPERATOR_GREATER_THAN:
                                    	compRefinementOR = new ComplexRefinement(ComplexRefinement.LOGICAL_OPERATOR_OR);
                                        refinement = new AttributeRefinement(fieldName, eMatrixDateFormat.getFormattedInputDateTime(context,value,"12:00:00 AM",clientTZOffset, loc), op);
                                        compRefinementOR.addRefinement(refinement);
                                		refinement = new AttributeRefinement(fieldName, eMatrixDateFormat.getFormattedInputDateTime(context,value,"12:00:00 AM",clientTZOffset, loc), getOperator("EQUALS"));
                                		compRefinementOR.addRefinement(refinement);
                                		compRefinement.addRefinement(compRefinementOR);
                                        break;
                                    case AttributeRefinement.OPERATOR_LESS_THAN:
                                    	compRefinementOR = new ComplexRefinement(ComplexRefinement.LOGICAL_OPERATOR_OR);
                                        refinement = new AttributeRefinement(fieldName, eMatrixDateFormat.getFormattedInputDateTime(context,value,"11:59:59 PM",clientTZOffset,loc), op);
                                        compRefinementOR.addRefinement(refinement);
                                    	refinement = new AttributeRefinement(fieldName, eMatrixDateFormat.getFormattedInputDateTime(context,value,"11:59:59 PM",clientTZOffset,loc), getOperator("EQUALS"));
                                    	compRefinementOR.addRefinement(refinement);
                                    	compRefinement.addRefinement(compRefinementOR);
                                        break;
                                }
						}else if((attrType == Config.NUM || attrType == Config.PARAMETRIC_NUM || attrType == Config.DOUBLE) && AttributeRefinement.OPERATOR_EQUAL != op){
							AttributeRefinement refinement = new AttributeRefinement(fieldName, value, op);
							compRefinement.addRefinement(refinement);
                        } else {
                            //changed for bug 358479
                            value = UISearchUtil.getActualNames(context,value);

                            if (!isIndexSearch && field!= null && field.selectable.equalsIgnoreCase("current") && value.indexOf(".") != -1) {
                            	HashMap stateChooerTranslationsMap = (HashMap)STATE_CHOOSER_TRANSLATIONS_MAP.get(fullTextSearchTimestamp);
								//IR-046342V6R2011 & IR-046956V6R2011
                            	if(stateChooerTranslationsMap != null && stateChooerTranslationsMap.get(value) != null && !jsonFilters.contains("POLICY") && !jsonFilters.contains("TYPE")){
                            		addPolicyRefinement = true;
                                    String txtPolicy = (String)stateChooerTranslationsMap.get(value);
                                    if("".equals(policyRefinemnt.toString())){
                                    	policyRefinemnt.append(txtPolicy);
                                    }else{
                                    	policyRefinemnt.append(",").append(txtPolicy);
                                    }
                            	}
                            	value = (String)FrameworkUtil.split(value, ".").get(1);
                            	if(stateFieldValue.get(value) == null){
                            		stateFieldValue.put(value, value);
                            		if("".equals(fieldValue.toString())){
                                        fieldValue.append(value);
                                    }else{
                                        fieldValue.append(",").append(value);
                                    }
                            	}
                            }else{
                            if (field != null && field.selectable.equalsIgnoreCase("current") && value.indexOf(".") != -1) {
                            	value = (String)FrameworkUtil.split(value, ".").get(1);
                            }
                            }
                            if("".equals(fieldValue.toString())){
								/* Mx361263 */
								if("ALL_VAULTS".equals(value)||
										"DEFAULT_VAULT".equals(value) ||
										"LOCAL_VAULTS".equals(value))
								{
									value = PersonUtil.getSearchVaults(context, true, value);
								}
								/* end Mx361263 */
                                fieldValue.append(value);
                            }else{
                                fieldValue.append(",").append(value);
                            }
                            if(fieldSep != null && !"".equals(fieldSep)&& !"true".equals(TagsView) ){
                                boolean valueFound = false;
                                JSONObject jsonFilterValue = new JSONObject(value);
                                JSONArray jsonFilterArr = jsonFilterValue.getJSONArray(fieldName);
                                for (int i = 0; i < jsonFilterArr.length(); i++) {
                                    JSONArray jsonArr = jsonFilterArr.getJSONArray(i);
                                    String tmpVal = "";
                                    for (int k = 0; k < jsonArr.length(); k++) {
                                        String filterValue = jsonArr.getString(k);
                                        filterValue = FrameworkUtil.findAndReplace(filterValue, ",", "\\,");
                                        if(!"".equals(filterValue)){
                                            valueFound = true;
                                        }
                                        if(k == 0){
                                            tmpVal = filterValue;
                                        }else{
                                            tmpVal += "," + filterValue;
                                        }
                                    }
                                    if(!"".equals(tmpVal)){
                                        AttributeRefinement refinement = new AttributeRefinement(fieldName, tmpVal.toString(), opr);
                                        compRefinement.addRefinement(refinement);
                                    }
                                }
                                if(!valueFound){
                                    compRefinement = null;
                                }
                            }else if("ID".equalsIgnoreCase(fieldName)){
                            	compRefinement = null;
                            	isReferenceRefinement = true;
                            }
                            else{
                            compRefinement = null;
                            isAttributeRefinement = true;
                        }
                    }
                    }
                    if (compRefinement != null) {
                        // Simplify refinements slightly; avoid Complex refinements
                        // where a simple one will do
                        if (compRefinement.getRefinements().length == 1) {
                            refinementList.add(compRefinement.getRefinements()[0]);
                        } else {
                            refinementList.add(compRefinement);
                        }
                    }else if(isReferenceRefinement){
                    	StringList idList = FrameworkUtil.split(fieldValue.toString(), ",");
                    	if(idList.size() >= 1){
                        	String idsCsl = "B" + FrameworkUtil.join(idList, ",B");
                        	ReferenceRefinement ref = new ReferenceRefinement(idsCsl,true);
                        	refinementList.add(ref);
                    	}
                    }
                    else if(isAttributeRefinement){
                      //changed for bug 358479
                      if("LATESTREVISION".equalsIgnoreCase(fieldName)){
                          ignoreSysProp = true;
                          if("true".equalsIgnoreCase(fieldValue.toString())){
                              filterKeys.add("LASTREVISION");
                          }else{
                              continue;
                          }
                      }else if("LASTREVISION".equalsIgnoreCase(fieldName)){
                          ignoreSysProp = true;
                          if("true".equalsIgnoreCase(fieldValue.toString())){
                              filterKeys.add("LATESTREVISION");
                          }else{
                              continue;
                          }
                      }else if("REVISION".equalsIgnoreCase(fieldName) && "last".equalsIgnoreCase(fieldValue.toString())){
                              ignoreSysProp = true;
                        	  fieldName = "LASTREVISION";fieldValue.setLength(0); fieldValue.append("true");
                              filterKeys.add("LASTREVISION");
                      }
                      AttributeRefinement refinement = null;
					  ComplexRefinement compRefinementVPMNameTitle = null;
                      Config.Field VPMNamefield = _config.indexedBOField("bo.PLMEntity.PLM_ExternalID");
                      Config.Field VPMTitlefield = _config.indexedBOField("bo.plmentity.v_name");
                      if(UISearchUtil.isCaseSensitiveSearch(context, params) && !(field != null && field.type == Config.PARAMETRIC_BOOLEAN)){
                          refinement = new CaseSensitiveAttributeRefinement(fieldName, fieldValue.toString(), opr,UISearchUtil.isCaseSensitive(params));
                      }else{
                          if(fieldName.equalsIgnoreCase("Name")){
							refinement = new AttributeRefinement(fieldName, fieldValue.toString(), opr);
							if(!typeAheadMappingList.contains(fieldName)){
                      			refinementList.add(refinement);	
        					}
        					else
        						typeMappingCompRefinement.addRefinement(refinement);
                    		  if(VPMNamefield != null){
                    		  compRefinementVPMNameTitle = new ComplexRefinement(ComplexRefinement.LOGICAL_OPERATOR_OR);
                          refinement = new AttributeRefinement(fieldName, fieldValue.toString(), opr);
                              compRefinementVPMNameTitle.addRefinement(refinement);
	                          refinement = new AttributeRefinement(VPMNamefield.name, fieldValue.toString(), opr);
	                          compRefinementVPMNameTitle.addRefinement(refinement);
                        	  }else{
								compRefinementVPMNameTitle = new ComplexRefinement(ComplexRefinement.LOGICAL_OPERATOR_OR);
                        	  refinement = new AttributeRefinement(fieldName, fieldValue.toString(), opr);
                        	  compRefinementVPMNameTitle.addRefinement(refinement);
				  			  }
                    	  }else if(fieldName.equalsIgnoreCase("TITLE") && VPMTitlefield != null){
                    		  compRefinementVPMNameTitle = new ComplexRefinement(ComplexRefinement.LOGICAL_OPERATOR_OR);
                    		  refinement = new AttributeRefinement(fieldName, fieldValue.toString(), opr);
                              compRefinementVPMNameTitle.addRefinement(refinement);
	                          refinement = new AttributeRefinement(VPMTitlefield.name, fieldValue.toString(), opr);
	                          compRefinementVPMNameTitle.addRefinement(refinement);
                    	  }else{
                    		  refinement = new AttributeRefinement(fieldName, fieldValue.toString(), opr);  
                    	  }
                      }

                      if(!isIndexSearch && "LASTREVISION".equalsIgnoreCase(fieldName)){
                    	  AttributeRefinement majorRefinement = new AttributeRefinement("MAJORLATEST", "majorid.lastmajorid");
                    	  ComplexRefinement cref = new ComplexRefinement(ComplexRefinement.LOGICAL_OPERATOR_AND);
                    	  cref.addRefinement(refinement);
                    	  cref.addRefinement(majorRefinement);
                    		
                    	  if(!typeAheadMappingList.contains(fieldName))
                    		  refinementList.add(cref);
                          else
                        	  typeMappingCompRefinement.addRefinement(cref);
                      }else{
        				if(!typeAheadMappingList.contains(fieldName)){
        					if(compRefinementVPMNameTitle != null){
        						refinementList.add(compRefinementVPMNameTitle);
        					}else{
                      		refinementList.add(refinement);
        					}
        				}
        				else
        					typeMappingCompRefinement.addRefinement(refinement);
                      }

                    }else if(isTaxonomyRefinement){
                        String csl = FrameworkUtil.join(taxonomyValueList,",");
                        TaxonomyRefinement refinement = new TaxonomyRefinement(fieldName, csl);
        				//refinementList.add(refinement);
        				if(!typeAheadMappingList.contains(fieldName))
                        refinementList.add(refinement);
        				else
        					typeMappingCompRefinement.addRefinement(refinement);
                    }
                }
				if(!isIndexSearch && addPolicyRefinement){
					AttributeRefinement refinement = new AttributeRefinement("POLICY", policyRefinemnt.toString() , AttributeRefinement.OPERATOR_EQUAL);
        			//refinementList.add(refinement);
    				if(!typeAheadMappingList.contains(fieldName))
			        refinementList.add(refinement);
    				else
    					typeMappingCompRefinement.addRefinement(refinement);
				}
            }


			if(TagsView!= null && "true".equals(TagsView)){
				refinementList.add(compRefinementORJUK);
			}

        	if (typeMappingCompRefinement.getRefinements().length == 1) {
				refinementList.add(typeMappingCompRefinement.getRefinements()[0]);
			} else if (typeMappingCompRefinement.getRefinements().length > 0) {
				refinementList.add(typeMappingCompRefinement);
			}
        }

        // Code for search within collection
        // Start
        String collectionName = (String) params.get("COLLECTION");
        if (canUse(collectionName)) {
            StringList setObjList = getCollectionObjects(context, collectionName);
            if (!setObjList.isEmpty()) {
                String formattedOidsCsl = "B" + FrameworkUtil.join(setObjList, ",B");
                ReferenceRefinement includeRef = new ReferenceRefinement(formattedOidsCsl, true);
                refinementList.add(includeRef);
            }
        }
        // End

        Hashtable fieldsMap = searchUtil.getFields(paramFields,UISearchUtil.getFieldSeperator(context,params));
        Enumeration enuField = fieldsMap.keys();

		if(params.get("parentTaxonomy") != null){
			while (enuField.hasMoreElements()) {
				String field = (String) enuField.nextElement();
				if (filterKeys.contains(field)) {
					continue;
				}
				if (UISearchUtil.FIELD_TYPES.equalsIgnoreCase(field)){
					isTypeDefined = true;
				}
				String value = (String) fieldsMap.get(field);
				StringList valueList = FrameworkUtil.split(value, "|");
				value = (String) valueList.get(1);
				TaxonomyRefinement refinement = new TaxonomyRefinement(field, value, true);
				refinementList.add(refinement);
			}

		}else{
        while (enuField.hasMoreElements()) {
            String field = (String) enuField.nextElement();
            if (filterKeys.contains(field)) {
                continue;
            }
            if("LATESTREVISION".equalsIgnoreCase(field) || "LASTREVISION".equalsIgnoreCase(field)){
                ignoreSysProp = true;
            }

            if (UISearchUtil.FIELD_TYPES.equalsIgnoreCase(field) || UISearchUtil.FIELD_TYPE.equalsIgnoreCase(field)) {
                isTypeDefined = true;
                String txtType = (String) fieldsMap.get(field);
                StringList typeList = FrameworkUtil.split(txtType, "|");

                int operator = getOperator((String) typeList.get(0));
                txtType = (String) typeList.get(1);

                boolean bEqualsOpr = (operator == AttributeRefinement.OPERATOR_EQUAL);
                StringList searchTypes = UISearchUtil.getTypes(context, txtType, false);
                String csl = FrameworkUtil.join(searchTypes, ",");
                TaxonomyRefinement refinement = new TaxonomyRefinement(UISearchUtil.FIELD_TYPES, csl, bEqualsOpr);
                refinementList.add(refinement);
            } else

            if (UISearchUtil.FIELD_REVISION.equalsIgnoreCase(field)) {
                String txtRev = (String) fieldsMap.get(UISearchUtil.FIELD_REVISION);
                StringList revList = FrameworkUtil.split(txtRev, "|");

                int operator = getOperator((String) revList.get(0));
                txtRev = (String) revList.get(1);

                if ("last".equalsIgnoreCase(txtRev)) {
                    AttributeRefinement refinement = new AttributeRefinement("LASTREVISION", "TRUE", operator);
                    refinementList.add(refinement);
                } else if ("latest".equalsIgnoreCase(txtRev)) {
                    AttributeRefinement refinement = new AttributeRefinement("LATESTREVISION", "TRUE", operator);
                    refinementList.add(refinement);
                } else if (txtRev != null && !txtRev.equalsIgnoreCase("null") && txtRev.length() > 0) {
                    //changed for bug 358479
                    //SearchRefinement refinement = createRefinement("REVISION", txtRev, operator);
                    AttributeRefinement refinement = new AttributeRefinement("REVISION", txtRev, operator);
                    refinementList.add(refinement);
                }
            } else if (UISearchUtil.FIELD_CURRENT.equalsIgnoreCase(field)) {
                String txtState = (String) fieldsMap.get(UISearchUtil.FIELD_CURRENT);
                StringList currentList = FrameworkUtil.split(txtState, "|");

                int operator = getOperator((String) currentList.get(0));
                txtState = (String) currentList.get(1);
                if (txtState != null && !txtState.equalsIgnoreCase("null") && txtState.length() > 0) {
                    String txtType = (String) fieldsMap.get(UISearchUtil.FIELD_TYPES);
                    if (canUse(txtType)) {
                        StringList typeList = FrameworkUtil.split(txtType, "|");
                        txtType = (String) typeList.get(1);
                    }
                    txtState = searchUtil.getStateNames(context, txtState, txtType);
                    //changed for bug 358479
                    //SearchRefinement refinement = createRefinement(UISearchUtil.FIELD_CURRENT, txtState, operator);
                    AttributeRefinement refinement = new AttributeRefinement(UISearchUtil.FIELD_CURRENT, txtState, operator);
                    refinementList.add(refinement);
                }
            } else {
                String txtField = (String) fieldsMap.get(field);
                StringList fieldList = FrameworkUtil.split(txtField, "|");

                int operator = getOperator((String) fieldList.get(0));
                txtField = (String) fieldList.get(1);
                txtField = UISearchUtil.getActualNames(context, txtField);
                //changed for bug 358479
                //refinementList.add(createRefinement(field, txtField, operator));
                if(!(("LATESTREVISION".equalsIgnoreCase(field) || "LASTREVISION".equalsIgnoreCase(field))
                        && "false".equalsIgnoreCase(txtField))){
                    AttributeRefinement refinement = new AttributeRefinement(field, txtField, operator);
                    refinementList.add(refinement);
                }
            }
        }
        }
        if(!ignoreSysProp){
            String lastRevision = EnoviaResourceBundle.getProperty(context, "emxFramework.FullTextSearch.LASTREVISION");
            String latestRevision = EnoviaResourceBundle.getProperty(context, "emxFramework.FullTextSearch.LATESTREVISION");
            String refinementString = "";
            if(!("true".equalsIgnoreCase(lastRevision) && "true".equalsIgnoreCase(latestRevision))
                    && !("false".equalsIgnoreCase(lastRevision) && "false".equalsIgnoreCase(latestRevision))){
                if("true".equalsIgnoreCase(lastRevision)){
                    refinementString = "LASTREVISION";
                }else if("true".equalsIgnoreCase(latestRevision)){
                    refinementString = "LATESTREVISION";
                }
                AttributeRefinement refinement = new AttributeRefinement(refinementString, "true", AttributeRefinement.OPERATOR_EQUAL);
                refinementList.add(refinement);
            }
        }

        

        // To exclude objects
        String txtExcludeOIDs = getExcludeList(context, params);
        if (!txtExcludeOIDs.equals("")) {
            ReferenceRefinement ref = new ReferenceRefinement(txtExcludeOIDs);
            refinementList.add(ref);
        }

        // To include objects
        String txtIncludeOIDs = getIncludeList(context, params);

        // AND condition added for IR-051630V6R2011x
        // When searching within a collection,
        // don't add include object to refinement list
        if (!txtIncludeOIDs.equals("") && !canUse(collectionName)) {
            ReferenceRefinement ref = new ReferenceRefinement(txtIncludeOIDs, true);
            refinementList.add(ref);
        }


        String sortColumnName = (String)params.get("sortColumnName");
        String sortDirection = (String)params.get("sortDirection");

        if(sortColumnName == null || "".equalsIgnoreCase(sortColumnName)) {
            sortColumnName = (String)params.get("firstColumnName");
            sortDirection = "ascending";
        }
        if(sortDirection == null || "".equalsIgnoreCase(sortDirection)) {
            sortDirection = "ascending";
        }
        boolean isAscending = true;
        if(!"ascending".equalsIgnoreCase(sortDirection))
        {
            isAscending = false;
        }

        StringList sortColumnNameList = FrameworkUtil.split(sortColumnName, ",");
        StringList sortColumnDirList = FrameworkUtil.split(sortDirection, ",");

        String sortName = "";
        String sortDir = "";
        for(int i =0; i < sortColumnNameList.size(); i++){
            sortName = (String)sortColumnNameList.get(i);
            try {
                sortDir = (String)sortColumnDirList.get(i);
            } catch(Exception e){
                sortDir = "ascending";
            }
            if(sortName != null && !"".equalsIgnoreCase(sortName)) {
                Config.Field field   = _config.indexedBOField(sortName);
                if(field != null) {
                    String fastsort = (String) field.attributes.get("fastsort");
                    if(fastsort != null && "true".equalsIgnoreCase(fastsort)) {
                        SortRefinement sortRefinement = new SortRefinement(sortName, "ascending".equalsIgnoreCase(sortDir));
                        refinementList.add(sortRefinement);
                    }
                }
            }
        }
        
        if (!isTypeDefined) {
        	String typeValues = null;
        	Set TYPES_TAXONOMY_SET = TYPES_TAXONOMY_MAP.get(fullTextSearchTimestamp);
        	if(isIndexSearch? TYPES_TAXONOMY_SET == null : false){
        		generateTaxonomyCounts(context, (SearchRefinement[]) refinementList.toArray(new SearchRefinement[refinementList.size()]), params);
        		TYPES_TAXONOMY_SET = TYPES_TAXONOMY_MAP.get(fullTextSearchTimestamp);
        	}
        	if(isIndexSearch && TYPES_TAXONOMY_SET != null){
        		String[] typeValuesArray = (String[])TYPES_TAXONOMY_SET.toArray(new String[TYPES_TAXONOMY_SET.size()]);
        		typeValues = FrameworkUtil.join( typeValuesArray, ",");
        	}
            StringList searchTypes = UISearchUtil.getTypes(context, typeValues, false);
            String csl = FrameworkUtil.join(searchTypes, ",");
            TaxonomyRefinement refinement = new TaxonomyRefinement(UISearchUtil.FIELD_TYPES, csl);
            refinementList.add(refinement);
        }


        SearchRefinement[] refinements = (SearchRefinement[]) refinementList.toArray(new SearchRefinement[refinementList.size()]);

        if (UISearchUtil.isDebug(params)) {
            System.out.println("------------------\nREFINEMENTS");
            for (int i = 0; i < refinements.length; i++) {
                System.out.println("  " + i + ": " + refinements[i].toString().trim());
            }
        }

        return refinements;
    }

//Bug:361452
  private SearchRefinement getTypeRefinement(Context context,Hashtable fieldsMap)throws Exception
  {
      String txtType = (String) fieldsMap.get(UISearchUtil.FIELD_TYPES);
      //Bug:361452
      StringList tmpTypeList = FrameworkUtil.split(txtType, "^");
      if(tmpTypeList.size()>1){
          ComplexRefinement typeRefinement = new ComplexRefinement(ComplexRefinement.LOGICAL_OPERATOR_AND);
          for (int i = 0; i < tmpTypeList.size(); i++) {
              txtType = (String) tmpTypeList.get(i);
              StringList typeList = FrameworkUtil.split(txtType, "|");
              int operator = getOperator((String) typeList.get(0));
              txtType = (String) typeList.get(1);
              boolean bEqualsOpr = (operator == AttributeRefinement.OPERATOR_EQUAL);
              StringList searchTypes = UISearchUtil.getTypes(context, txtType, false);
              String csl = FrameworkUtil.join(searchTypes, ",");
              TaxonomyRefinement refinement = new TaxonomyRefinement( UISearchUtil.FIELD_TYPES, csl, bEqualsOpr);
              typeRefinement.addRefinement(refinement);
          }
          return typeRefinement;
      }else{
          StringList typeList = FrameworkUtil.split(txtType, "|");
          int operator = getOperator((String) typeList.get(0));
          txtType = (String) typeList.get(1);

          boolean bEqualsOpr = (operator == AttributeRefinement.OPERATOR_EQUAL);
          StringList searchTypes = UISearchUtil.getTypes(context, txtType, false);
          String csl = FrameworkUtil.join(searchTypes, ",");
          TaxonomyRefinement typeRefinement = new TaxonomyRefinement(UISearchUtil.FIELD_TYPES, csl, bEqualsOpr);
          return typeRefinement;
      }
  }

    protected String getExcludeList(Context context, HashMap params) throws Exception {
        String fullTextSearchTimestamp = (String)params.get("fullTextSearchTimestamp");
        fullTextSearchTimestamp = fullTextSearchTimestamp == null || "".equals(fullTextSearchTimestamp) ? (String)params.get("searchTimeStamp") : fullTextSearchTimestamp;
        String cachedExcl = (String)SEARCH_EXCL_OIDS_MAP.get(fullTextSearchTimestamp);
        if (cachedExcl != null) {
            return cachedExcl;
        }

        StringList excludeList = new StringList();
        String txtExcludeOIDs = (String) params.get(UISearchUtil.EXLUDEOID);

        if (txtExcludeOIDs != null && !"".equals(txtExcludeOIDs) && !"null".equals(txtExcludeOIDs)) {
            txtExcludeOIDs = txtExcludeOIDs.replaceAll(" ", "");
            StringList oidsList = FrameworkUtil.split(txtExcludeOIDs, ",");
            excludeList.addAll(oidsList);
        }

        String excludeOIDprogram = (String) params.get(UISearchUtil.EXCLUDEOIDPROGRAM);
        if (excludeOIDprogram != null && !"".equals(excludeOIDprogram) && !"null".equals(excludeOIDprogram)) {
            StringList tmp = FrameworkUtil.split(excludeOIDprogram, ":");
            String exOidProg = "";
            String exOidFunc = "";
            if (tmp.size() > 0) {
                exOidProg = (String) tmp.elementAt(0);
                exOidFunc = (String) tmp.elementAt(1);
            }
            FrameworkUtil.validateMethodBeforeInvoke(context, exOidProg, exOidFunc,"excludeOIDProgram");
            if (exOidProg.length() > 0 && exOidFunc.length() > 0) {
                StringList oidList = (StringList) JPO.invoke(context, exOidProg, null, exOidFunc, JPO.packArgs(params),
                    StringList.class);
                excludeList.addAll(oidList);
            }
        }
        String ret = "";
        if (!excludeList.isEmpty()) {
            ret = "B" + FrameworkUtil.join(excludeList, ",B");
        }
        SEARCH_EXCL_OIDS_MAP.put(fullTextSearchTimestamp, ret);
        return ret;
    }

    protected String getIncludeList(Context context, HashMap params) throws Exception {
        String fullTextSearchTimestamp = (String)params.get("fullTextSearchTimestamp");

        String cachedIncl = (String)SEARCH_INCL_OIDS_MAP.get(fullTextSearchTimestamp);
        if (cachedIncl != null) {
            return cachedIncl;
        }

        StringList includeList = new StringList();

        String includeOIDprogram = (String) params.get(UISearchUtil.INCLUDEOIDPROGRAM);
        if (includeOIDprogram != null && !"".equals(includeOIDprogram) && !"null".equals(includeOIDprogram)) {
            StringList tmp = FrameworkUtil.split(includeOIDprogram, ":");
            String inOidProg = "";
            String inOidFunc = "";
            if (tmp.size() > 0) {
                inOidProg = (String) tmp.elementAt(0);
                inOidFunc = (String) tmp.elementAt(1);
            }
            FrameworkUtil.validateMethodBeforeInvoke(context, inOidProg, inOidFunc,"includeOIDProgram");
            if (inOidProg.length() > 0 && inOidFunc.length() > 0) {
                StringList oidList = (StringList) JPO.invoke(context, inOidProg, null, inOidFunc, JPO.packArgs(params),
                    StringList.class);
                includeList.addAll(oidList);
            }
        }
        String ret = "";
        if (!includeList.isEmpty()) {
            ret = "B" + FrameworkUtil.join(includeList, ",B");
        }
        SEARCH_INCL_OIDS_MAP.put(fullTextSearchTimestamp, ret);
        return ret;
    }

    protected int getOperator(String operatorString) throws Exception {
        if ("EQUALS".equalsIgnoreCase(operatorString)) {
            return AttributeRefinement.OPERATOR_EQUAL;
        } else if ("GREATER".equalsIgnoreCase(operatorString)) {
            return AttributeRefinement.OPERATOR_GREATER_THAN;
        } else if ("LESS".equalsIgnoreCase(operatorString)) {
            return AttributeRefinement.OPERATOR_LESS_THAN;
        } else if ("NOTEQUALS".equalsIgnoreCase(operatorString)) {
            return AttributeRefinement.OPERATOR_NOT_EQUAL;
        } else {
            throw new Exception("Unsupported operator: " + operatorString);
        }
    }

    protected boolean canUse(String paramvalue) {
        if (paramvalue != null && !"".equals(paramvalue.trim()) && !"null".equals(paramvalue) && !"undefined".equalsIgnoreCase(paramvalue)) {
            return true;
        } else {
            return false;
        }
    }

    protected SearchRefinement createRefinement(String field, String txtValueCSL, int operator) throws Exception {
        txtValueCSL = txtValueCSL.replaceAll(" *, *", ",");
        StringList values = FrameworkUtil.split(txtValueCSL, ",");
        int logic;
        if (operator == AttributeRefinement.OPERATOR_EQUAL) {
            logic = ComplexRefinement.LOGICAL_OPERATOR_OR;
        } else {
            logic = ComplexRefinement.LOGICAL_OPERATOR_AND;
        }

        if (values.size() > 1) {
            ComplexRefinement compRefinement = new ComplexRefinement(logic);
            for (int k = 0; k < values.size(); k++) {
                String value = (String) values.get(k);
                if (value != null && !(value.equalsIgnoreCase("null")) && (value.length() > 0)) {
                    AttributeRefinement refinement = new AttributeRefinement(field, value, operator);
                    compRefinement.addRefinement(refinement);
                }
            }
            return compRefinement;
        } else {
            return new AttributeRefinement(field, txtValueCSL, operator);
        }
    }

    protected StringList getCollectionObjects(Context context, String collectionsCsl) throws Exception {
        StringList allOids = new StringList();
        StringList setList = new StringList(1);
        setList.addElement(DomainObject.SELECT_ID);
        StringTokenizer strTokens = new StringTokenizer(collectionsCsl, ",");
        while (strTokens.hasMoreTokens()) {
            String colName = strTokens.nextToken();
            if (canUse(colName)) {
                String oids = MqlUtil.mqlCommand(context, "print set $1 select $2 dump recordsep $3",colName,"id","|");
                allOids.addAll(FrameworkUtil.split(oids, "|"));
            }
        }
        return allOids;
    }

    protected StringList getManadatoryList(HashMap params) {
        String mandatorySearchParam = (String) params.get("mandatorySearchParam");
        StringList res = FrameworkUtil.split(mandatorySearchParam, ",");
        return res;
    }

    protected StringList getRolesList(Context context, HashMap params) throws Exception {
        StringList roleList = null;
        String ftsFilters = (String) params.get("ftsFilters");
        if (UIUtil.isNotNullAndNotEmpty(ftsFilters)) {
// Bug 351815 filters = com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(filters);
            JSONObject jsonFilters = extractFilters(params);
            JSONArray jsonValues;
            String fieldName;
            Iterator itr = jsonFilters.keys();
            while (itr.hasNext()) {
                fieldName = (String) itr.next();
                jsonValues = jsonFilters.getJSONArray(fieldName);
                if ("USERROLE".equals(fieldName) && jsonValues.length() > 0) {
                    roleList = new StringList();
                    for (int j = 0; j < jsonValues.length(); j++) {
                        String jsonValue = jsonValues.getString(j);
                        // String
                        // operatorString=jsonValue.substring(0,jsonValue.indexOf("|"));
                        String value = jsonValue.substring(jsonValue.indexOf("|") + 1, jsonValue.length());
                        roleList.addElement(value);
                    }
                }
            }
        }
        if (roleList == null) {
            UISearchUtil searchUtil = new UISearchUtil();
            String paramFields = (String) params.get("field");
            Hashtable fieldsMap = searchUtil.getFields(paramFields,UISearchUtil.getFieldSeperator(context,params));
            String userRole = (String) fieldsMap.get("USERROLE");
            if (userRole != null && !"".equals(userRole)) {
                roleList = FrameworkUtil.split(userRole, "|");
                // String operator = (String)roleList.get(0);
                userRole = (String) roleList.get(1);
                userRole = UISearchUtil.getActualNames(context, userRole);
                roleList = FrameworkUtil.split(userRole, ",");
            }
        }

        return roleList;
    }

    public String clearCache(Context context, String[] args) throws Exception {
        HashMap params = (HashMap) JPO.unpackArgs(args);
        String fullTextSearchTimestamp = (String)params.get("fullTextSearchTimestamp");

        SEARCH_EXCL_OIDS_MAP.remove(fullTextSearchTimestamp);
        SEARCH_INCL_OIDS_MAP.remove(fullTextSearchTimestamp);
        SEARCH_RESULTS_MAP.remove(fullTextSearchTimestamp);
        TAXONOMIES_MAP.remove(fullTextSearchTimestamp);
        STATE_TRANSLATIONS_MAP.remove(fullTextSearchTimestamp);
        TYPE_CHOOSER_TRANSLATIONS_MAP.remove(fullTextSearchTimestamp);
        STATE_CHOOSER_TRANSLATIONS_MAP.remove(fullTextSearchTimestamp);
        VAULT_CHOOSER_TRANSLATIONS_MAP.remove(fullTextSearchTimestamp);
        STATE_SCHEMA_MAP.remove(fullTextSearchTimestamp);
        TYPE_SCHEMA_MAP.remove(fullTextSearchTimestamp);
    	TYPES_TAXONOMY_MAP.remove(fullTextSearchTimestamp);

        return new JSONObject().put("status", "ok").toString();
    }

    // TODO: review this
    public String resolveSymbolicName(Context context, String[] args) throws Exception {
        String symName = args[0];
        if (symName.startsWith("program[")) {
            return symName;
        }
        int startIndex = 0;
        int endIndex = symName.lastIndexOf("]");
        int middleIndex = 0;
        while (startIndex < endIndex) {
            startIndex = symName.indexOf("[", startIndex);
            if (startIndex < 0) {
                startIndex = endIndex;
            } else {
                middleIndex = symName.indexOf("]", startIndex);
                String temp = symName.substring(startIndex + 1, middleIndex);
                if (canUse(temp)) {
                    String temp1 = UISearchUtil.getActualNames(context, temp);
                    symName = FrameworkUtil.findAndReplace(symName, temp, temp1);
                    middleIndex = symName.indexOf("]", startIndex);
                }
            }
            if (startIndex < middleIndex) {
                startIndex = middleIndex;
            }
            endIndex = symName.lastIndexOf("]");
        }
        return symName;
    }

	public HashMap getTypeChooserValues(Context context, String[] args) throws Exception{
		HashMap progMap = (HashMap)JPO.unpackArgs(args);
		HashMap params = (HashMap)progMap.get("requestMap");
		String fullTextSearchTimestamp = (String)params.get("fullTextSearchTimestamp");
    	
		HashMap cacheMap=new HashMap();
		String languageStr = (String) params.get("languageStr");

		JSONObject jsonFilters = extractFilters(params);
		StringList typesList = getFilterValues(jsonFilters, "TYPES");
		if(typesList == null || typesList.size() <= 0){
        	typesList = getFilterValues(jsonFilters , "TYPE");
        }
		if(typesList == null || typesList.size() <= 0){
			String fieldActual = (String)params.get("field_actual");
	        UISearchUtil searchUtil = new UISearchUtil();
	        Hashtable fieldActualMap = searchUtil.getFields(fieldActual,UISearchUtil.getFieldSeperator(context,params));
	        String typesParam = (String) fieldActualMap.get("TYPES");
	        if (canUse(typesParam)) {
	        	typesParam = (String)FrameworkUtil.split(typesParam,"|").get(1);
	        	typesList = FrameworkUtil.split(UISearchUtil.getActualNames(context,typesParam), ",");
	        }
		}
		if(typesList == null || typesList.size() <= 0){
    		String txtType = EnoviaResourceBundle.getProperty(context,"emxFramework.GenericSearch.Types");
    		typesList = FrameworkUtil.split(txtType, ",");
    		StringList newTypeList = new StringList();
    		Iterator<String> itr = typesList.iterator();
    		while(itr.hasNext()){
    			String typeName = itr.next();
    			String typeActualName = PropertyUtil.getSchemaProperty(context,typeName.trim());
    			if(UIUtil.isNullOrEmpty(typeActualName)){
    				itr.remove();
    			}else{
    				newTypeList.add(typeActualName);
    			}
    		}
    		typesList = newTypeList;
            typesList = (StringList) UICache.removeLicensedTypes(context, typesList);
    	}
		for (int i = 0; i < typesList.size(); i++) {
			String txtType = (String)typesList.get(i);
			if(UINavigatorUtil.isHidden(txtType)){
				continue;
			}
			StringList typeList2 = UISearchUtil.getTypes(context, txtType, true);
			for (int j = 0; j < typeList2.size(); j++) {
				String txtType2 = (String)typeList2.get(j);
				if(UINavigatorUtil.isHidden(txtType2)){
					continue;
				}
				String mxTransType = UINavigatorUtil.getAdminI18NString("Type",txtType2, languageStr);
				//retMap.put(txtType2, mxTransType);
				if (cacheMap.get(txtType2) == null) {
		        	cacheMap.put(txtType2, mxTransType);
			}
		}
		}

       
		return cacheMap;
	}

	public HashMap getStateChooserValues(Context context, String[] args) throws Exception{
		HashMap progMap = (HashMap)JPO.unpackArgs(args);
		HashMap params = (HashMap)progMap.get("requestMap");
		String fullTextSearchTimestamp = (String)params.get("fullTextSearchTimestamp");
		HashMap stateSchemaMap = (HashMap)STATE_SCHEMA_MAP.get(fullTextSearchTimestamp);
		HashMap typeSchemaMap = (HashMap)TYPE_SCHEMA_MAP.get(fullTextSearchTimestamp);
		HashMap stateTranslationsMap = (HashMap)STATE_TRANSLATIONS_MAP.get(fullTextSearchTimestamp);
		HashMap stateChooerTranslationsMap = (HashMap)STATE_CHOOSER_TRANSLATIONS_MAP.get(fullTextSearchTimestamp);

    	if(stateSchemaMap == null){
    		stateSchemaMap = new HashMap();
    	}
    	if(typeSchemaMap == null){
    		typeSchemaMap = new HashMap();
    	}
    	if(stateTranslationsMap == null){
    		stateTranslationsMap = new HashMap();
    	}
    	if(stateChooerTranslationsMap == null){
    		stateChooerTranslationsMap = new HashMap();
    	}
		HashMap retMap = new HashMap();
		String languageStr = (String) params.get("languageStr");

		JSONObject jsonFilters = extractFilters(params);
		String fieldActual = (String)params.get("field_actual");
        UISearchUtil searchUtil = new UISearchUtil();
        Hashtable fieldActualMap = searchUtil.getFields(fieldActual,UISearchUtil.getFieldSeperator(context,params));

		//get policy from filters
		StringList policyList = getFilterValues(jsonFilters, "POLICY");
		StringList policyListDup = policyList;
		int op = 0;
		if( !(policyList== null || policyList.size() <= 0) ){
		String policyOpr = getFilterOperator(jsonFilters, "POLICY");
			op = getOperator(policyOpr);
		}

		if(policyList== null || policyList.size() <= 0 ||op==1){
			//get types
        String typesParam = (String) fieldActualMap.get("TYPES");
	        StringList completeTypeList = new StringList();
			StringList typesFilterList = getFilterValues(jsonFilters, "TYPES");
	        StringList typesList = new StringList();
	        if(filterExists(jsonFilters,"TYPES")) {
	        	typesList = typesFilterList;
		        }else if(canUse(typesParam)){
		        	typesParam = (String)FrameworkUtil.split(typesParam,"|").get(1);
		        	typesList = FrameworkUtil.split(UISearchUtil.getActualNames(context,typesParam), ",");
	        }


		if(typesList == null || typesList.size() <= 0){
    		String txtType = EnoviaResourceBundle.getProperty(context, "emxFramework.GenericSearch.Types");
    		typesList = FrameworkUtil.split(UISearchUtil.getActualNames(context,txtType), ",");
            typesList = (StringList) UICache.removeLicensedTypes(context, typesList);
			}
			completeTypeList.addAll(typesList);
	        for (int i = 0; i < typesList.size(); i++) {
	        	String txtType = (String)typesList.get(i);
	        	//add child types also
	        	StringList typeList2 = UISearchUtil.getTypes(context, txtType, true);
	        	completeTypeList.addAll(typeList2);
	        }
	        for (int j = 0; j < completeTypeList.size(); j++) {
	        	String txtType = (String)completeTypeList.get(j);
	        	if(typeSchemaMap.get(txtType) != null){
	        		policyList.addAll((StringList)typeSchemaMap.get(txtType));
	        	}else{
	        		StringList tempList = getPolicyList(context, txtType);
	        		policyList.addAll(tempList);
	        		typeSchemaMap.put(txtType, tempList);
	        	}
	        }
		}
		if(op!=1){
			policyListDup= new StringList();
		}
		//remove duplicate policies
		StringList filteredPolicyList = new StringList();
		HashMap checkDuplicate = new HashMap();
		for (int i = 0; i < policyList.size(); i++) {
			String sPolicy = (String)policyList.get(i);
			if(checkDuplicate.get(sPolicy) == null && !(policyListDup.contains(sPolicy))){
				checkDuplicate.put(sPolicy, sPolicy);
				filteredPolicyList.add(sPolicy);
			}
		}
		StringList stateList = new StringList();
		//IR-044856V6R2011
        StringList notStateList = new StringList();
		String statesFieldParam = (String) fieldActualMap.get("CURRENT");
        StringList statesParamList = FrameworkUtil.split(statesFieldParam,"^");

        String statesParam = "";
        String notstatesParam = "";

        for (int i = 0; i < statesParamList.size(); i++) {
            String oprVal = (String)statesParamList.get(i);
            String opr = (String)FrameworkUtil.split(oprVal,"|").get(0);
            int operator = getOperator(opr);
            boolean bEqualsOpr = (operator == AttributeRefinement.OPERATOR_EQUAL);
            boolean bNotEqualsOpr = (operator == AttributeRefinement.OPERATOR_NOT_EQUAL);
            if(bEqualsOpr){
                statesParam = oprVal;
            }else if(bNotEqualsOpr){
                notstatesParam = oprVal;
            }
        }
        if (canUse(notstatesParam)) {
            notstatesParam = (String)FrameworkUtil.split(notstatesParam,"|").get(1);
            StringList tempNotStateList = FrameworkUtil.split(notstatesParam, ",");
            for (int i3 = 0; i3 < tempNotStateList.size(); i3++) {
                String stateandpolicy = (String)tempNotStateList.get(i3);
                String policyName = "";
                String stateName = "";
                if(stateandpolicy.indexOf(".") > -1){
                    policyName = UISearchUtil.getActualNames(context,(String)FrameworkUtil.split(stateandpolicy, ".").get(0));
                    stateName = UISearchUtil.getActualNames(context,stateandpolicy);
                    notStateList.add(policyName + "." + stateName);
                }else{
                    notStateList.add(UISearchUtil.getActualNames(context,stateandpolicy));
                }
            }
        }
		if (canUse(statesParam)) {
			statesParam = (String)FrameworkUtil.split(statesParam,"|").get(1);
        	StringList tempStateList = FrameworkUtil.split(statesParam, ",");
			for (int i3 = 0; i3 < tempStateList.size(); i3++) {
				String stateandpolicy = (String)tempStateList.get(i3);
				String policyName = "";
				String stateName = "";
				if(stateandpolicy.indexOf(".") > -1){
					policyName = UISearchUtil.getActualNames(context,(String)FrameworkUtil.split(stateandpolicy, ".").get(0));
					stateName = UISearchUtil.getActualNames(context,stateandpolicy);
					stateList.add(policyName + "." + stateName);
				}else{
					stateList.add(UISearchUtil.getActualNames(context,stateandpolicy));
				}
			}
        }

		if(stateList != null && stateList.size() <= 0){
			for (int i = 0; i < filteredPolicyList.size(); i++) {
				String policyName = (String)filteredPolicyList.get(i);
				List<String> hiddenStates = CacheManager.getInstance().getValue(context, CacheManager._entityNames.HIDDEN_STATES, policyName);
				Policy mxPolicy = new Policy(policyName);
				for (Iterator stateItr = mxPolicy.getStateRequirements(context).iterator(); stateItr.hasNext();) {
					StateRequirement stateReq = (StateRequirement) stateItr.next();
					String stateName = stateReq.getName();
					if (!hiddenStates.contains(stateName)) {
						stateList.add(policyName + "." + stateName);
					}
				}
			}
		}

		for (int i2 = 0; i2 < stateList.size(); i2++) {
			String stateandpolicy = (String)stateList.get(i2);
			String stateName = "";
			String policyName = "";
			if(stateandpolicy.indexOf(".") > -1){
				policyName = (String)FrameworkUtil.split(stateandpolicy, ".").get(0);
				stateName = (String)FrameworkUtil.split(stateandpolicy, ".").get(1);
			}else{
				stateName = stateandpolicy;
			}

            if(notStateList.contains(policyName + "." + stateName) || notStateList.contains(stateName)){
                continue;
            }
				String translatedValue = "";
				if(stateTranslationsMap.get(stateName + "."+policyName) != null){
                   	if ("".equals(policyName) || policyName == null){
                        translatedValue = (String)stateTranslationsMap.get(stateName);
                    } else {
                    translatedValue = (String)stateTranslationsMap.get(stateName + "."+policyName);
                	}
                }else{
                    if ("".equals(policyName) || policyName == null){
                    translatedValue = (String)stateTranslationsMap.get(stateName);
                     }
                    else {
	        		String stateKey = "emxFramework.State." + policyName + "." + stateName;
					translatedValue = getI18nString(context, stateKey,LANG_RESOURCE_FILE,languageStr);
					    if (translatedValue.startsWith("emxFramework.")) {
						translatedValue = stateName.toString();
					    }
                    }
					stateTranslationsMap.put(stateName + "."+policyName,translatedValue);
	        	}

				retMap.put(translatedValue +"."+stateName, translatedValue);
				if(stateChooerTranslationsMap.get(translatedValue +"."+stateName) == null){
					stateChooerTranslationsMap.put(translatedValue +"."+stateName, policyName);
				}else{
					String tmpStr = (String)stateChooerTranslationsMap.get(translatedValue +"."+stateName);
					stateChooerTranslationsMap.put(translatedValue +"."+stateName, tmpStr+","+policyName);
				}
			}

        STATE_SCHEMA_MAP.put(fullTextSearchTimestamp,stateSchemaMap);
        TYPE_SCHEMA_MAP.put(fullTextSearchTimestamp,typeSchemaMap);
        STATE_TRANSLATIONS_MAP.put(fullTextSearchTimestamp,stateTranslationsMap);
        STATE_CHOOSER_TRANSLATIONS_MAP.put(fullTextSearchTimestamp,stateChooerTranslationsMap);
        
        Iterator<Map.Entry> it=retMap.entrySet().iterator();
        HashMap<String,String>tempMap= new HashMap<String,String>();
        HashMap<String,String> returnMap= new HashMap<String,String>();
        returnMap.putAll(retMap);
       
        while(it.hasNext()){
        	Entry e=it.next();
        	String key=(String) e.getKey();
        	String value=(String) e.getValue();
            String tempvalue=value.substring(0,1).toUpperCase()+value.substring(1,value.length()).toLowerCase();
        	
        	  	if(!tempMap.containsKey(tempvalue)){
        	  		tempMap.put(tempvalue, key);
        	  	}else {
    	  			String presentkey=tempMap.get(tempvalue);
					value=returnMap.get(presentkey);
    	  			returnMap.put(presentkey+"||"+key,value);
    	  			tempMap.put(tempvalue, presentkey+"||"+key);    	  			
    	  			returnMap.remove(presentkey);
    	  			returnMap.remove(key);        	  			
        	  	}
        }
		return returnMap;
	}

	protected StringList getPolicyList(Context context,String txtType) throws MatrixException{
        	matrix.db.FindLikeInfo flLikeObj = null;
        	BusinessTypeList btList = new BusinessTypeList();
    	StringList policyList = new StringList();
        	try{
    		BusinessType btType = new BusinessType(txtType, context.getVault());
        		btType.open(context, false);
        		//To get the Find Like information of the business type selected
        		if(btType.isAbstract(context) && btType.hasChildren(context)){
        			btList = btType.getChildren(context);
        		}else{
                flLikeObj = btType.getFindLikeInfo(context);
        		}
                btType.close(context);
            }catch(Exception e){
                //When business type does not exist then code will come here...
                //Do Nothing
            }

        if(flLikeObj != null && flLikeObj.getPolicies() != null){
        	policyList.addAll(flLikeObj.getPolicies());
            }else{
            	for (Iterator iterator = btList.iterator(); iterator
						.hasNext();) {
            		BusinessType object = (BusinessType) iterator.next();
            		matrix.db.FindLikeInfo flObj = object.getFindLikeInfo(context);
            		StringList locPolicyList = flObj.getPolicies();
                		if(locPolicyList != null){
            		policyList.addAll(locPolicyList);
				}
		}
		}
        return policyList;
        	}

	public String validatePolicyAndState(Context context, String[] args) throws Exception {
        HashMap params = (HashMap) JPO.unpackArgs(args);
        String fullTextSearchTimestamp = (String)params.get("fullTextSearchTimestamp");
        String currField = (String)params.get("currField");
        String languageStr = (String) params.get("languageStr");

        HashMap typeSchemaMap = (HashMap)TYPE_SCHEMA_MAP.get(fullTextSearchTimestamp);

        HashMap stateChooerTranslationsMap = (HashMap)STATE_CHOOSER_TRANSLATIONS_MAP.get(fullTextSearchTimestamp);
        String fieldActual = (String)params.get("field_actual");
        UISearchUtil searchUtil = new UISearchUtil();
        Hashtable fieldActualMap = searchUtil.getFields(fieldActual,UISearchUtil.getFieldSeperator(context,params));

        JSONObject retJsonObj = new JSONObject();

        JSONObject jsonFilters = extractFilters(params);
        StringList typeList = null;
        StringList policyList = null;
        StringList stateList = null;
        if(filterExists(jsonFilters,"TYPES") && "EQUALS".equalsIgnoreCase(getFilterOperator(jsonFilters,"TYPES"))){
        	typeList = new StringList();
        	typeList = getFilterValues(jsonFilters, "TYPES");
        }

        if(filterExists(jsonFilters,"POLICY") && "EQUALS".equalsIgnoreCase(getFilterOperator(jsonFilters,"POLICY"))){
        	policyList = new StringList();
        	policyList = getFilterValues(jsonFilters, "POLICY");
        }

        if(filterExists(jsonFilters,"CURRENT") && "EQUALS".equalsIgnoreCase(getFilterOperator(jsonFilters,"CURRENT"))){
        	stateList = new StringList();
        	stateList = getFilterValues(jsonFilters, "CURRENT");
        }

        StringList validPolicies = new StringList();
        StringList validStates = new StringList();
        if(("TYPE".equals(currField) || "TYPES".equals(currField)) && filterExists(jsonFilters,"TYPES")){
        	//validate policy and state based on selected types
        	for (int i = 0; i < typeList.size(); i++) {
        		String txtType = (String)typeList.get(i);
                if(typeSchemaMap == null){
            		typeSchemaMap = new HashMap();
            		StringList tmpPolicyList = getPolicyList(context, txtType);
	        		typeSchemaMap.put(txtType, tmpPolicyList);
						}
        		if(policyList != null){
            		//validate txtPolicy
        			for (int j = 0; j < policyList.size(); j++) {
            			String txtPolicy = (String)policyList.get(j);
            			boolean isValidPolicy = validatePolicy(context,txtPolicy,typeList,typeSchemaMap);
            			if(isValidPolicy && !validPolicies.contains(txtPolicy)){
            				validPolicies.add(txtPolicy);
    						}
			        	}
							}
        		StringList policies = (StringList)typeSchemaMap.get(txtType);
        		if(stateList != null){
            		//validate txtState
    				for (int j1 = 0; j1 < stateList.size(); j1++) {
            			String txtState = (String)stateList.get(j1);
            			boolean isValidState = validateState(context,txtState,policies,stateChooerTranslationsMap);
            			if(isValidState){
            				validStates.add(txtState);
            			}
    				}
						}
					}
        }else if("POLICY".equals(currField) && filterExists(jsonFilters,"POLICY")){
        	//validate state based on selected policies
        	if(stateList != null){
        		//validate txtState
        		for (int j = 0; j < stateList.size(); j++) {
        			String txtState = (String)stateList.get(j);
        			boolean isValidState = validateState(context,txtState,policyList,stateChooerTranslationsMap);
        			if(isValidState){
        				validStates.add(txtState);
						}
					}
				}
        }else if("CURRENT".equals(currField) && filterExists(jsonFilters,"CURRENT")){
        	//validate policy based on selected states
        	if(policyList != null){
        		//validate policy list
        		for (int j = 0; j < policyList.size(); j++) {
        			String txtPolicy = (String)policyList.get(j);
            		boolean isValidPolicy = validatePolicywithState(context,txtPolicy,stateList,stateChooerTranslationsMap);
            		if(isValidPolicy){
        				validPolicies.add(txtPolicy);
        			}
				}
        	}
        }

        TYPE_SCHEMA_MAP.put(fullTextSearchTimestamp,typeSchemaMap);

        String STR_REFINEMENT_SEPARATOR = EnoviaResourceBundle.getProperty(context, "emxFramework.FullTextSearch.RefinementSeparator");
        if(STR_REFINEMENT_SEPARATOR == null || "".equals(STR_REFINEMENT_SEPARATOR)){
            STR_REFINEMENT_SEPARATOR = ",";
        }
        String mxValidPolicies = "";
        String mxValidStates = "";
        JSONObject policyJsonObj = new JSONObject();
        JSONObject stateJsonObj = new JSONObject();

        String strValidPolicies = FrameworkUtil.join(validPolicies,STR_REFINEMENT_SEPARATOR);
        if("".equals(strValidPolicies)){
        	strValidPolicies = "*";
        }
        StringList mxPolicyList = new StringList();
        for (int i1 = 0; i1 < validPolicies.size(); i1++) {
        	String translatedValue = UISearchUtil.getActualNames(context,(String)validPolicies.get(i1));
            translatedValue=UINavigatorUtil.getAdminI18NString("Policy", translatedValue, languageStr);
            mxPolicyList.add(translatedValue);
		}
        mxValidPolicies = FrameworkUtil.join(mxPolicyList,STR_REFINEMENT_SEPARATOR);
        if("".equals(mxValidPolicies)){
        	mxValidPolicies = " ";
        }

        String strValidStates = FrameworkUtil.join(validStates,STR_REFINEMENT_SEPARATOR);
        if("".equals(strValidStates)){
        	strValidStates = "*";
        }
        StringList mxStateList = new StringList();
        for (int i2 = 0; i2 < validStates.size(); i2++) {
        	String translatedValue = getStateI18NString(context, params, (String)validStates.get(i2), languageStr);
        	mxStateList.add(translatedValue);
		}
        mxValidStates = FrameworkUtil.join(mxStateList,STR_REFINEMENT_SEPARATOR);
        if("".equals(mxValidStates)){
        	mxValidStates = " ";
        }

        policyJsonObj.put("Actual", strValidPolicies);
        policyJsonObj.put("Display", mxValidPolicies);
        stateJsonObj.put("Actual", strValidStates);
        stateJsonObj.put("Display", mxValidStates);
        retJsonObj.put("POLICY", policyJsonObj);
        retJsonObj.put("CURRENT", stateJsonObj);
		return retJsonObj.toString();
	}

	private boolean validatePolicywithState(Context context,
			String txtPolicy, StringList stateList, HashMap stateChooerTranslationsMap) throws MatrixException {
		boolean isValidPolicy = false;
		if(stateChooerTranslationsMap != null){
		for (int i = 0; i < stateList.size(); i++) {
			String txtState = (String)stateList.get(i);
				String strPolicyForState = (String) stateChooerTranslationsMap.get(txtState);
				StringList policyForStateList = FrameworkUtil.split(strPolicyForState, ",");
				if(policyForStateList.contains(txtPolicy)){
				isValidPolicy = true;
				break;
			}
		}
		}else{
			Policy mxPolicy = new Policy(txtPolicy);
			Iterator stateItr = mxPolicy.getStateRequirements(context).iterator();
			while (stateItr.hasNext()) {
				StateRequirement stateReq = (StateRequirement) stateItr.next();
				String stateName = stateReq.getName();
                for (int i = 0; i < stateList.size(); i++) {
                    String txtState = (String)stateList.get(i);
                    String actualState = "";
                    if(txtState.indexOf(".") != -1){
                        actualState = (String)FrameworkUtil.split(txtState, ".").get(1);
                    }
                    if(stateName.equals(actualState)){
                        isValidPolicy = true;
                        break;
                    }
                }

			}
	}
		return isValidPolicy;
	}

	private boolean validateState(Context context, String txtState,
			StringList policyList, HashMap stateChooserTranslationsMap) throws MatrixException {
		boolean isValidState = false;
		//String currPolicy = (String)FrameworkUtil.split(txtState, ".").get(0);
		if(stateChooserTranslationsMap != null){
			String strPolicyForState = (String) stateChooserTranslationsMap.get(txtState);
			if(strPolicyForState != null){
				StringList policyForStateList = FrameworkUtil.split(strPolicyForState, ",");
		for (int i = 0; i < policyList.size(); i++) {
			String txtPolicy = (String)policyList.get(i);
					if(policyForStateList.contains(txtPolicy)){
				isValidState = true;
				break;
					}
				}
			}
		}else{
			String actualState = "";
			if(txtState.indexOf(".") != -1){
				actualState = (String)FrameworkUtil.split(txtState, ".").get(1);
				for (int i = 0; i < policyList.size(); i++) {
					String txtPolicy = (String)policyList.get(i);
					Policy mxPolicy = new Policy(txtPolicy);
					Iterator stateItr = mxPolicy.getStateRequirements(context).iterator();
					while (stateItr.hasNext()) {
						StateRequirement stateReq = (StateRequirement) stateItr.next();
						String stateName = stateReq.getName();
						if(stateName.equals(actualState)){
							isValidState = true;
							break;
						}
					}

				}
			}
		}
		return isValidState;
	}

	private boolean validatePolicy(Context context, String txtPolicy,
			StringList typeList, HashMap typeSchemaMap) throws MatrixException {
		boolean isValidPolicy = false;

		for (int i = 0; i < typeList.size(); i++) {
			String txtType = (String)typeList.get(i);
			StringList polices = (StringList)typeSchemaMap.get(txtType);
			if(polices == null){
				StringList tmpPolicyList = getPolicyList(context, txtType);
				polices = tmpPolicyList;
        		typeSchemaMap.put(txtType, tmpPolicyList);
			}
			if(polices.contains(txtPolicy)){
				isValidPolicy = true;
				break;
			}
		}
		return isValidPolicy;
	}

		/**
	 * Updates the search records static map with search records returned by search engine
	 * Updates returnMapList with objectId maps
	 * @param context the eMatrix <code>Context</code> object
	 * @param sbTimestamp indented table timeStamp for the current request
	 * @param searchRecords search records returned by search engine
	 * @param returnMapList to update object Ids
	 * @param canShowSnippets to decide on whether to update search records map or not
	 * @since BPS R214.HF7
	 */
	private final void updateSearchRecordsMap(Context context, String sbTimestamp, SearchRecord[] searchRecords, MapList returnMapList, boolean canShowSnippets){
		HashMap objectInfo;
		String objId = "";
		if(canShowSnippets){
			HashMap<String, SearchRecord> recordsMap = new HashMap<String, SearchRecord>(searchRecords.length);
	    	for(SearchRecord record : searchRecords){
	    		objectInfo = new HashMap();
	    		objId = record.getMXId();
	    		objectInfo.put("id", objId);
	    		try {
	    			String type = MqlUtil.mqlCommand(context, "print bus $1 select type dump", objId);
					objectInfo.put("type", type);
				} catch (Exception e) {
					e.printStackTrace();
				}
	    		returnMapList.add(objectInfo);
	    		recordsMap.put(objId, record);
	    	}
	    	_searchRecordsMap.put(sbTimestamp, recordsMap);
		}else{
			for(SearchRecord record : searchRecords){
				objectInfo = new HashMap();
				String objectId = record.getMXId();
	    		objectInfo.put("id", objectId);
	    		try {
	    			String type = MqlUtil.mqlCommand(context, "print bus $1 select type dump", objectId);
					objectInfo.put("type", type);
				} catch (Exception e) {
					e.printStackTrace();
				}

	    		returnMapList.add(objectInfo);
	    	}
		}
	}

	/**
	 * to remove saved search records for the previous search. invoked when freeze pane data is cleaned up
	 * @param context the eMatrix <code>Context</code> object
	 * @param args program map with timestamp
	 * @throws Exception
	 * @since BPS R214.HF7
	 */
	public final void removeSearchRecords(Context context, String[] args) throws Exception{
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		_searchRecordsMap.remove((String) programMap.get("sbTimestamp"));
	}

	/**
	 * method to return column values for snippet column
	 * Based on the snippet source setting, gets summary from record object using kernel APIs (getSummaryFromFile, getSummaryFromMeta)
	 * If snippetSource is "MetadataAndFiles"
	 *       first get snippet from file, if a matching term is found, then format it
	 *       get snippet from metadata, if a matching term is found, then format it
	 *       If formatted snippets are available for both the sources, then combine them. Else, add the one available to snippetList.
	 *       If there is no formatted snippet from both the sources, then just add an empty string to snippet list
	 * If snippet source is "Files" or "Metadata", check if there is no match for input search text term, add empty string.
	 * In any case if there is a match, then format the snippet
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args program map
	 * @return stringlist of snippets for all objects in search result
	 * @throws Exception
	 * @since BPS R214.HF7
	 */
    public StringList getSearchSnippets(Context context, String[] args) throws Exception{
    	StringList snippetList = new StringList();
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		HashMap paramMap = (HashMap)programMap.get("paramList");
		boolean isExport = false;
		String exportFormat = (String) paramMap.get("exportFormat");
		if(exportFormat != null){
			isExport = true;
		}
		String sbTimestamp = (String) paramMap.get("timeStamp");
		String objId = "";
		HashMap searchRecords = (HashMap) _searchRecordsMap.get(sbTimestamp);
		String snippetsSource = UISearchUtil.getSnippetsSource(context, paramMap);
		boolean isMetaAndFiles = UISearchUtil.METADATA_AND_FILES.equals(snippetsSource);
		boolean isMeta = UISearchUtil.METADATA.equals(snippetsSource);
		boolean isFiles = UISearchUtil.FILES.equals(snippetsSource);
		boolean hasMatchInFile;
		boolean isHTMLExport = "HTML".equals(exportFormat);
		String summary = "";
		String summaryFiles = "";
		String searchTextTerm = UISearchUtil.getSearchTextTerm(context, paramMap);
		if(searchTextTerm.endsWith("*")){
			searchTextTerm = searchTextTerm.substring(0, (searchTextTerm.length()-1));
		}
		StringTokenizer st = new StringTokenizer(searchTextTerm, "*");
		String searchTermRegEx = "";
		while(st.hasMoreElements()){
			searchTermRegEx += Pattern.quote((String)st.nextElement());
			if(st.hasMoreElements()){
				searchTermRegEx += "(.*?)";
			}
		}
		Pattern pattern = Pattern.compile(searchTermRegEx, Pattern.CASE_INSENSITIVE);
		Locale locale = new Locale((String)paramMap.get("languageStr"));
		String sourceLabelData = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", locale, "emxFramework.FullTextSearch.SnippetsSourceData");
		String sourceLabelFiles = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", locale, "emxFramework.FullTextSearch.SnippetsSourceFiles");
		MapList relBusObjList = (MapList)programMap.get("objectList");
		Iterator relBusObjListItr = relBusObjList.iterator();
		org.htmlcleaner.TagNode tagNode;
		while(relBusObjListItr.hasNext()){
			hasMatchInFile = false;
			objId = (String)((HashMap)relBusObjListItr.next()).get("id");
			SearchRecord record = (SearchRecord) searchRecords.get(objId);
			if(isMetaAndFiles || isFiles){
				summaryFiles = record.getSummaryFromFile();
				if(pattern.matcher(summaryFiles).find()){
					if(isHTMLExport){
						tagNode = _localHtmlCleaner.clean(summaryFiles);
						summaryFiles = tagNode.getText().toString();
					}
					if(isFiles){
						snippetList.add(formatSnippet(context, pattern, summaryFiles.trim(), sourceLabelFiles, isExport, isMetaAndFiles));
						continue;
					}else{
						summaryFiles = formatSnippet(context, pattern, summaryFiles.trim(), sourceLabelFiles, isExport, isMetaAndFiles);
						hasMatchInFile = true;
					}
				}else if(isFiles){
			        snippetList.add("");
				    continue;
				}
			}
			if(isMeta || isMetaAndFiles){
				summary = record.getSummaryFromMeta();
				if(pattern.matcher(summary).find()){
					if(isHTMLExport){
						tagNode = _localHtmlCleaner.clean(summary);
						summary = tagNode.getText().toString();
					}
					summary = formatSnippet(context, pattern, summary.trim(), sourceLabelData, isExport, isMetaAndFiles);
					if(hasMatchInFile){
						summary = isExport ?  (summary +  "     " + summaryFiles) : ("<p>" + summary + "<br /><br />" + summaryFiles + "</p>");
						snippetList.add(summary);
					}else{
						summary = isExport ? summary : ("<p>" + summary + "</p>");
						snippetList.add(summary);
					}
				}else{
					if(hasMatchInFile){
						summaryFiles = isExport ? summaryFiles : ("<p>" + summaryFiles + "</p>");
						snippetList.add(summaryFiles);
					}else{
						snippetList.add("");
					}
				}
			}
		}
		return snippetList;
	}

    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param pattern pattern generated for the input text term
     * @param summary snippet obtained
     * @param sourceLabel will be used if @param isMetaAndFiles is true
     * @param isExport if true, formats snippet for export data
     * @param isMetaAndFiles
     * @return formatted snippet
     * @since BPS R214.HF7
     */
    private String formatSnippet(Context context, Pattern pattern, String summary, String sourceLabel, boolean isExport, boolean isMetaAndFiles){
		if(isExport){
    		return (isMetaAndFiles ? (summary + " " + sourceLabel) : summary);
		}else{
			Matcher matcher = pattern.matcher(summary);
			StringBuffer sbSnippet = new StringBuffer();
		    while (matcher.find()){
		        matcher.appendReplacement(sbSnippet, ":B:_:L_:D:_:S:" + matcher.group() + ":B:_:L_:D:_:E:");
		    }
		    String snippet = matcher.appendTail(sbSnippet).toString();
		    snippet = snippet.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("/", "&#x2f;");
		    snippet = snippet.replaceAll(":B:_:L_:D:_:S:", "<b>").replaceAll(":B:_:L_:D:_:E:", "</b>");
		    return (isMetaAndFiles ? (snippet + " " + sourceLabel) : ("<p>" + snippet+ "</p>"));
		}
    }

    public SearchRefinement[] getRefinmentsforTags(Context context, String[] args) throws Exception {
    		
           	HashMap params = (HashMap) JPO.unpackArgs(args);         
           	
           	String table = (String)params.get("table");
			HashMap tableInfo = UICache.getTable(context, table);
			String firstColumnName = UITableCommon.getColumnNameByIndex(context, tableInfo, 0);
			params.put("firstColumnName", firstColumnName);
           	
        	SearchRefinement refinements[] = GetSearchRefinements(context, params);
        	return refinements;
    }
}

