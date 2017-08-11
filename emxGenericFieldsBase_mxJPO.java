
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.FrameworkStringResource;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.framework.ui.UINavigatorUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.fcs.common.ImageRequestData;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.Vector;
import matrix.db.BusinessObjectProxy;
import matrix.db.Command;
import matrix.db.Context;
import matrix.db.FileList;
import matrix.db.JPO;
import matrix.dbutil.SelectSetting;
import matrix.util.MatrixException;
import matrix.util.StringList;


public class emxGenericFieldsBase_mxJPO {
   
    public emxGenericFieldsBase_mxJPO(Context context, String[] args) throws Exception {}


    // Lifecycle Field
    public static String fieldLifecycle(Context context, String[] args) throws Exception {

        StringBuilder sbResult  = new StringBuilder();
        HashMap programMap      = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap        = (HashMap) programMap.get("paramMap");
        HashMap fieldMap        = (HashMap) programMap.get("fieldMap");
        HashMap settingsMap     = (HashMap) fieldMap.get("settings");
        String sOID             = (String) paramMap.get("objectId");
        String sHeight          = (String) settingsMap.get("Height");
        
        if (null == sHeight || "".equals(sHeight)) { sHeight = "85"; }
        
        sbResult.append("<object id='gnvLifecycle' type='text/html'");
        sbResult.append("data='../common/emxLifecycleDialog.jsp?export=false&toolbar=AEFLifecycleMenuToolBar&objectId=").append(sOID).append("&header=emxFramework.Lifecycle.LifeCyclePageHeading&mode=basic'");
        sbResult.append("width='100%' height='").append(sHeight).append("' style='overflow:hidden;padding:0px;margin:0px'></object>");

        return sbResult.toString();

    }    
    
    
    // Images Field
    public  String fieldImages(Context context, String[] args) throws Exception {
        
        StringBuilder sbResult  = new StringBuilder();
        HashMap programMap      = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap        = (HashMap) programMap.get("paramMap");
        HashMap fieldMap        = (HashMap) programMap.get("fieldMap");
        HashMap settings        = (HashMap) fieldMap.get("settings");
        String fieldName 			= (String)fieldMap.get("name");
        fieldName = FrameworkUtil.Replace(fieldName, " ", "_");
        Map requestMap          = (Map) programMap.get("requestMap");
        Map imageData           = (Map) requestMap.get("ImageData"); 
        String sLang            = (String)requestMap.get("languageStr"); 
        String sOID             = (String) paramMap.get("objectId");
        String sMCSURL          = (String)imageData.get("MCSURL");
        DomainObject dObject    = new DomainObject(sOID); 
        StringBuilder sbZoom    = new StringBuilder();
        int iCounter            = 0;
        int iCounterMax         = 0;
        
        String sGallery     = (String) settings.get("Gallery");
        String sGalleryZoom = (String) settings.get("Gallery Zoom");
        String sHeight      = (String) settings.get("Height");
        String sFormat      = (String) settings.get("Image Size");
        String sPrimary     = (String) settings.get("Include Primary");
        String sMaxItems    = (String) settings.get("Max Items");
        String sStyle       = (String) settings.get("Style");
        String sZooming     = (String) settings.get("Zoom");        
        String sZoomWidth   = (String) settings.get("Zoom Window Width");
        String sZoomHeight  = (String) settings.get("Zoom Window Height");
                 
        String sTitle = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.String.ClickToOpenGalleryView", new Locale(sLang));
        if(null == sPrimary)    { sPrimary = "true"; }
        if(null == sGallery)    { sGallery = "true"; }
        if((null == sGalleryZoom) || (sGalleryZoom.equalsIgnoreCase("TRUE"))) { sGalleryZoom = "onComplete:function(){ $('.cboxPhoto').wheelzoom(); },"; } else { sGalleryZoom = ""; }
        if(null == sZooming)    { sZooming = "false"; }
        if(null == sZoomWidth)  { sZoomWidth  = "var widthZoom   = jQuery( window ).width()  * 0.5;"; } else { sZoomWidth  = "var widthZoom  = " + sZoomWidth  + ";"; }
        if(null == sZoomHeight) { sZoomHeight = "var heightZoom  = jQuery( window ).height() * 0.6;"; } else { sZoomHeight = "var heightZoom = " + sZoomHeight + ";"; }
        if(null == sFormat)     { sFormat = "mxSmall Image"; }
        if(null == sHeight)     { 
            if(sFormat.equals("mxThumbnail Image"))     { sHeight =  "42"; }
            else if(sFormat.equals("mxSmall Image"))    { sHeight =  "64"; }
            else if(sFormat.equals("mxMedium Image"))   { sHeight = "102"; }
            else if(sFormat.equals("mxLarge Image"))    { sHeight = "480"; }
            else { sHeight = "64"; } 
        }
        if(null == sStyle)      { sStyle  = "margin:5px;border:1px solid #bababa;box-shadow:1px 1px 2px #ccc;height:" + sHeight + "px;"; }
        if(null == sMaxItems)   { iCounterMax = 10; } else  { iCounterMax = Integer.parseInt(sMaxItems); }

        if(sGallery.equalsIgnoreCase("TRUE")) {
        	sbResult.append("<script>if(!jQuery.colorbox){");
        	sbResult.append("function loadJS(fileName){	var fileref=document.createElement('script');fileref.setAttribute(\"type\",\"text/javascript\");");
        	sbResult.append("fileref.setAttribute(\"src\", fileName);$('#pageHeadDiv').append(fileref);}");	
        	sbResult.append("function loadCSS(fileName){var fileref=document.createElement(\"link\");fileref.setAttribute(\"rel\", \"stylesheet\");");
        	sbResult.append("fileref.setAttribute(\"type\", \"text/css\");fileref.setAttribute(\"href\", fileName);	jQuery('#pageHeadDiv').append(fileref);}");
        	sbResult.append("loadJS(\"../plugins/colorbox/js/jquery.colorbox.js\");");
        	sbResult.append("loadCSS(\"../plugins/colorbox/css/colorbox.css\");");
        	sbResult.append("loadJS(\"../plugins/wheelzoom/js/jquery.wheelzoom.js\");");
            sbResult.append("}</script>");
        }
        if(sZooming.equalsIgnoreCase("TRUE")) {
        	sbResult.append("<script>if(!jQuery.elevateZoom){");
        	sbResult.append("function loadJS(fileName){	var fileref=document.createElement('script');fileref.setAttribute(\"type\",\"text/javascript\");");
        	sbResult.append("fileref.setAttribute(\"src\", fileName);$('#pageHeadDiv').append(fileref);}");
        	sbResult.append("loadJS(\"../plugins/elevatezoom/js/jquery.elevatezoom.js\");");
            sbResult.append("}</script>");
        }

        StringList busSelects = new StringList();        
        busSelects.add(DomainConstants.SELECT_ID);
        busSelects.add(DomainConstants.SELECT_ORIGINATED);

        // Get related Product Central images
        
        MapList mlImages = dObject.getRelatedObjects(context, DomainConstants.RELATIONSHIP_IMAGES, DomainConstants.TYPE_IMAGE, busSelects, null, false, true, (short)1, "", "", 0);                
        String sOIDPrimary = dObject.getInfo(context, "from["+ DomainConstants.RELATIONSHIP_PRIMARY_IMAGE +"].to.id");
        if(null != sOIDPrimary ) {
            if(!"".equals(sOIDPrimary) ) {
                for(int i = mlImages.size() - 1; i >= 0; i--) {
                    Map mImage              = (Map)mlImages.get(i);
                    String OIDImage         = (String)mImage.get("id");
                    if(sOIDPrimary.equals(OIDImage)) {
                        if(sPrimary.equalsIgnoreCase("FALSE")) {
                        mlImages.remove(i);
                        } else {
                            mImage.put("primary", "yes");
                        }
                    } else {
                        mImage.put("primary", "no");
                    }
                }
            }        
        }
        mlImages.sort("originated", "ascending", "date");
        mlImages.sort("primary", "descending", "String");
        
                
        for(int i = 0; i < mlImages.size(); i++) {
            
            Map mImage              = (Map)mlImages.get(i);
            String OIDImage         = (String)mImage.get("id");
            DomainObject doImage    = new DomainObject(OIDImage);
            FileList fileList       = doImage.getFiles(context, sFormat);
            
            if (fileList.size() > 0) {                
                for (int k = 0; k < fileList.size(); k++) {
                    iCounter++;                    
                    matrix.db.File fTemp    = (matrix.db.File) fileList.get(k);
                    String sFileName        = fTemp.getName();
                    ArrayList bopArrayList  = new ArrayList();
                    BusinessObjectProxy bop = new BusinessObjectProxy(OIDImage, sFormat, sFileName, false, false);
                    bopArrayList.add(bop);	        
                    String[] tmpImageUrls   = ImageRequestData.getImageURLS(context, sMCSURL, bopArrayList);
                    String sURLImage        = tmpImageUrls[0];                     
                    String sURLImageGeneric = retrievePluginURLs(context, doImage, OIDImage, fieldName, k, sFileName, sGallery, sZooming, sbResult, sbZoom, sMCSURL);                    
                    if(iCounter <= iCounterMax) {
                        sbResult.append("<img id='").append(fieldName+k).append("' title='").append(sTitle).append("' style='").append(sStyle).append("' src='").append(sURLImage).append("' data-zoom-image='").append(sURLImageGeneric).append("' />");                                            
                    }
                    if(sGallery.equalsIgnoreCase("TRUE")) {
                        sbResult.append("</a>");    
                    }
                }            
            }
        }
        
        // Get related Image Holders
        String attrPrimaryImage = PropertyUtil.getSchemaProperty(context,DomainObject.SYMBOLIC_attribute_PrimaryImage);
        String relImageHolder = PropertyUtil.getSchemaProperty(context,DomainObject.SYMBOLIC_relationship_ImageHolder);
        String typeImageHolder = PropertyUtil.getSchemaProperty(context,DomainObject.SYMBOLIC_type_ImageHolder);
        busSelects.add("attribute["+ attrPrimaryImage +"]");
        
        MapList mlImageHolders = dObject.getRelatedObjects(context, relImageHolder, typeImageHolder, busSelects, null, true, false, (short)1, "", "", 0);       
        MapList mlSketches = dObject.getRelatedObjects(context, DomainObject.RELATIONSHIP_REFERENCE_DOCUMENT, DomainObject.TYPE_SKETCH, busSelects, null, false, true, (short)1, "", "", 0);
        if(mlSketches.size() > 0) {
            for (int i = 0; i < mlSketches.size(); i++) {
                Map mSketch =(Map)mlSketches.get(i);
                String sOIDSketch = (String)mSketch.get("id");
                DomainObject doSketch = new DomainObject(sOIDSketch);
                MapList mlSketchImages = doSketch.getRelatedObjects(context, relImageHolder, typeImageHolder, busSelects, null, true, false, (short)1, "", "", 0);
                if(mlSketchImages.size() > 0) {
                    for (int j = 0; j < mlSketchImages.size(); j++) {
                        Map mSketchImage = (Map)mlSketchImages.get(j);
                        Boolean bAdd = true;
                        String sOIDImageSketch = (String)mSketchImage.get("id");
                        for (int k = 0; k < mlImageHolders.size(); k++) {    
                            Map mImageHolder = (Map)mlImageHolders.get(k);
                            String sOIDImageHolder = (String)mImageHolder.get("id");                        
                            if(sOIDImageSketch.equals(sOIDImageHolder)) {
                                bAdd = false;
                                continue;
                            } 
                        }
                        if(bAdd) {
                            mlImageHolders.add(mSketchImage);
                        }
                    }
                }
            }
        }        
              
        
        for(int i = 0; i < mlImageHolders.size(); i++) {
            
            mlImageHolders.sort("originated", "ascending", "date");
            mlImageHolders.sort("attribute["+ attrPrimaryImage +"]", "descending", "date");
                        
            Map mImageHolder            = (Map)mlImageHolders.get(i);
            String sOIDImageHolder      = (String)mImageHolder.get(DomainConstants.SELECT_ID);
            String sPrimaryImage        = (String)mImageHolder.get("attribute["+ attrPrimaryImage +"]");
            DomainObject doImageHolder  = new DomainObject(sOIDImageHolder);
            FileList fileList           = doImageHolder.getFiles(context, sFormat);
            
            if (fileList.size() > 0) {
                
                for (int k = 0; k < fileList.size(); k++) {
                    matrix.db.File fTemp    = (matrix.db.File) fileList.get(k);
                    String sFileName        = fTemp.getName();                    
                    
                    if(sPrimary.equalsIgnoreCase("FALSE") && (sFileName.equals(sPrimaryImage))) {
                    } else {
                        iCounter++;
                        ArrayList bopArrayList  = new ArrayList();
                        BusinessObjectProxy bop = new BusinessObjectProxy(sOIDImageHolder, sFormat, sFileName, false, false);
                        bopArrayList.add(bop);	        
                        String[] tmpImageUrls   = ImageRequestData.getImageURLS(context, sMCSURL, bopArrayList);
                        String sURLImage        = tmpImageUrls[0];                        
                        String sURLImageGeneric = retrievePluginURLs(context, doImageHolder, sOIDImageHolder, fieldName, k, sFileName, sGallery, sZooming, sbResult, sbZoom, sMCSURL);                        
                        if(iCounter <= iCounterMax) {
                            sbResult.append("<img id='").append(fieldName+k).append("' title='Click to open gallery view' style='").append(sStyle).append("' src='").append(sURLImage).append("' data-zoom-image='").append(sURLImageGeneric).append("' />");
                        }
                        if(sGallery.equalsIgnoreCase("TRUE")) {
                            sbResult.append("</a>");    
                        }    
                    }
                }                
            }
        }
        
        if(iCounter > iCounterMax) {
            sbResult.append("<div style='line-height:").append(sHeight).append("px;display:inline-block;vertical-align:top;margin-top:6px;'> (").append(iCounter - iCounterMax).append(" more)</div>");
        }
        
        if(sGallery.equalsIgnoreCase("TRUE")) {        	
        	sbResult.append("<script>jQuery(\"a[rel='gallery"+fieldName+"']\").colorbox({").append(sGalleryZoom).append("photo:'true', maxWidth:'100%', maxHeight:'100%', opacity: 1.0});</script>");
        }
   
        // Output if there are no images
        if(mlImages.size() == 0) {
            String sMessage = EnoviaResourceBundle.getProperty(context, "Components", "emxComponents.Image.NoImageAssociated",  sLang); 
            if(mlImageHolders.size() == 0) {
                sbResult.append("<i>").append(sMessage).append("</i>");
            }
        }
        
        sbResult.append("<script>");
        sbResult.append("var widthWindow = jQuery( window ).width() - 20;");
        sbResult.append(sZoomHeight);
        sbResult.append(sZoomWidth);
        sbResult.append(sbZoom.toString());
        sbResult.append("</script>");   
        
        return sbResult.toString();        
    }  
    
    private String retrievePluginURLs(Context context, DomainObject dObject, String OIDImage, String fieldName, int k,
    		String sFileName, String sGallery, String sZooming, StringBuilder sbResult, StringBuilder sbZoom, String sMCSURL)
    		throws MatrixException, Exception {
        
        String sURLImageGeneric = "";
        String idItem = fieldName+k;
        
        if(sGallery.equalsIgnoreCase("TRUE")|| sZooming.equalsIgnoreCase("TRUE")) {
            FileList fileListGeneric        = dObject.getFiles(context, "generic");
            matrix.db.File fTempGeneric     = (matrix.db.File) fileListGeneric.get(k);
            String sFileNameGeneric         = fTempGeneric.getName();
            ArrayList bopArrayListGeneric   = new ArrayList();
            BusinessObjectProxy bopGeneric  = new BusinessObjectProxy(OIDImage, "generic", sFileNameGeneric, false, false);
            bopArrayListGeneric.add(bopGeneric);	        
            String[] tmpImageUrlsGeneric    = ImageRequestData.getImageURLS(context, sMCSURL, bopArrayListGeneric);
            sURLImageGeneric                = tmpImageUrlsGeneric[0];
        }
        if(sGallery.equalsIgnoreCase("TRUE")) {
            sbResult.append("<a rel='gallery" + fieldName + "' class='gallery' href='").append(sURLImageGeneric).append("' title=''>");
        }
        if(sZooming.equalsIgnoreCase("TRUE")) {
            sbZoom.append("var pos").append(idItem).append(" = 7; ");
            sbZoom.append("var left").append(idItem).append(" = jQuery('#").append(idItem).append("').position().left; ");
            sbZoom.append("var right").append(idItem).append(" = widthWindow - left").append(idItem).append("; ");
            sbZoom.append("var midRight").append(idItem).append(" = right").append(idItem).append(" - (jQuery('#").append(idItem).append("').width() / 2); ");
            sbZoom.append("if (right").append(idItem).append(" < widthZoom) { ");
            sbZoom.append("     if( midRight").append(idItem).append(" > (widthZoom / 2)) {");
            sbZoom.append("         pos").append(idItem).append(" = 6; ");
            sbZoom.append("     } else {");
            sbZoom.append("         pos").append(idItem).append(" = 5; ");
            sbZoom.append("     }");
            sbZoom.append("} ");
            sbZoom.append("jQuery('#").append(idItem).append("').elevateZoom({zoomWindowOffety: 8, borderSize : 1, borderColour: '#bababa', scrollZoom : true, zoomWindowWidth:widthZoom, zoomWindowHeight:heightZoom, zoomWindowPosition: pos").append(idItem).append("});");
        }    
        
        return sURLImageGeneric;
        
    }
       
    
    // Field Visibility
    public static Boolean setAccess(Context context, String[] args) throws Exception {

        Boolean bResult     = false;
        HashMap programMap  = (HashMap) JPO.unpackArgs(args);
        HashMap settingsMap = (HashMap) programMap.get("SETTINGS");
        String sShowView    = (String) settingsMap.get("Show View");
        String sShowAll     = (String) settingsMap.get("Show All View");
        String sShowEdit    = (String) settingsMap.get("Show Edit");
        String sShowPrint   = (String) settingsMap.get("Show Printer Friendly");
        String sShowExport  = (String) settingsMap.get("Show Export");
        String sMode        = (String) programMap.get("mode");
        String sPFMode      = (String) programMap.get("PFmode");
        
        if(sShowView    == null) { sShowView    = "false"; }
        if(sShowEdit    == null) { sShowEdit    = "false"; }
        if(sShowPrint   == null) { sShowPrint   = "false"; }
        if(sShowExport  == null) { sShowExport  = "false"; }
        if(sShowAll     != null) { if(sShowAll.equalsIgnoreCase("true")) { sShowView = "true"; sShowPrint = "true"; sShowExport = "true"; } }        
        
        if(null == sMode) {
            if(sShowExport.equalsIgnoreCase("TRUE")) { bResult = true; }
            else if(sShowView.equalsIgnoreCase("TRUE")) { bResult = true; }
        } else if(sMode.equalsIgnoreCase("edit")) {
            if(sShowEdit.equalsIgnoreCase("TRUE")) { bResult = true; }
        } else if(sMode.equalsIgnoreCase("view")) {
            if(null == sPFMode) {
                if(sShowView.equalsIgnoreCase("TRUE")) { bResult = true; }       
            } else {
                if(sShowPrint.equalsIgnoreCase("TRUE")) { bResult = true; }                       
            }
        }

        return bResult;
    }    
    
    
    // Set Format of form field
    public String fieldFormat(Context context, String[] args) throws Exception {        
        

        SimpleDateFormat sdf    = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aaa");
        HashMap programMap      = (HashMap) JPO.unpackArgs(args);    
        HashMap paramMap        = (HashMap) programMap.get("paramMap");        
        HashMap fieldMap        = (HashMap) programMap.get("fieldMap");
        HashMap requestMap      = (HashMap) programMap.get("requestMap");
        String sLang            = (String)requestMap.get("languageStr");        
        String sHref            = (String) fieldMap.get("href");        
        HashMap settings        = (HashMap) fieldMap.get("settings");        
        String sOID             = (String) paramMap.get("objectId");    
        String sExpression      = (String) fieldMap.get("expression_businessobject");   
        String sBold            = (String) settings.get("Bold");
        String sColor           = (String) settings.get("Color");
        String sBGColor         = (String) settings.get("Background Color");
        String sItalic          = (String) settings.get("Italic");
        String sImage           = (String) settings.get("Image");
        String sImages          = (String) settings.get("Images");
        String sStyle           = (String) settings.get("Style");
        String sStyles          = (String) settings.get("Styles");
        String sValues          = (String) settings.get("Values");
        String sFilterOwned     = (String) settings.get("Filter Owned");
        String sFilterStatus    = (String) settings.get("Filter Status");
        String sAttributeName   = (String) settings.get("Filter Attribute Name");
        String sAttributeValue  = (String) settings.get("Filter Attribute Value");
        String sFilterFrom      = (String) settings.get("Filter From");
        String sFilterTo        = (String) settings.get("Filter To");
        String sValuesFrom      = (String) settings.get("Values From");
        String sValuesTo        = (String) settings.get("Values To");
        String sFilterAfter     = (String) settings.get("Filter After");
        String sFilterBefore    = (String) settings.get("Filter Before");
        String sValuesAfter     = (String) settings.get("Values After");
        String sValuesBefore    = (String) settings.get("Values Before");
//        String sFormat          = (String) settings.get("Display Format");
        String sFormat          = (String) settings.get("format");
        String sTarget          = (String) settings.get("Target Location");
        String sDisplayFormat   = (String) settings.get("Display Format");
        String sAdminType       = (String) settings.get("Admin Type");
        Boolean bApplyStyle     = false;
        StringBuilder sbResult  = new StringBuilder();
        StringBuilder sbStyle   = new StringBuilder();
        StringBuilder sbImage   = new StringBuilder();
        String sImagePrefix     = "<img style='vertical-align:middle;margin-right:8px;' src='images/";
        String sImageSuffix     = "' />";
        String sOutputPrefix    = "<div ";
        String sOutputSuffix    = "</div>";   
           
        if(null != sStyle)      { bApplyStyle = true; sbStyle.append(sStyle); if(!sStyle.endsWith(";")) { sbStyle.append(";"); } }
        if(null != sBold)       { bApplyStyle = true; if(sBold.equalsIgnoreCase("TRUE")) { sbStyle.append("font-weight:bold;"); }  }      
        if(null != sItalic)     { bApplyStyle = true; if(sItalic.equalsIgnoreCase("TRUE")) { sbStyle.append("font-style:italic;"); } }
        if(null != sColor)      { bApplyStyle = true; sbStyle.append("color:").append(sColor).append(";");  }
        if(null != sBGColor)    { bApplyStyle = true; sbStyle.append("background-color:").append(sBGColor).append(";"); }
        if(null != sImage)      { sbImage.append(sImagePrefix).append(sImage).append(sImageSuffix); }
        if(null == sFormat)     { sFormat = ""; }
        if(null == sDisplayFormat){ sDisplayFormat = "2"; }
        if(null == sTarget)     { sTarget = ""; }
        if(null != sHref)       { 
            if(!sHref.contains("?")) { sHref += "?"; }
            if(!sHref.endsWith("?")) { sHref += "&"; }
            sHref += "objectId=" + sOID;
            StringBuilder sbOutputPrefix = new StringBuilder();
            sbOutputPrefix.append("<a class=\"object\" ");
            sbOutputPrefix.append("href=\"JavaScript:emxFormLinkClick(&quot;");
            sbOutputPrefix.append(sHref);
            sbOutputPrefix.append("&amp;relId=null&quot;, &quot;").append(sTarget).append("&quot;, &quot;&quot;, &quot;&quot;, &quot;&quot;, &quot;M-0000004&quot;)\" ");
            sOutputPrefix = sbOutputPrefix.toString();
            sOutputSuffix = "</a>";   
        }
        
        
        DomainObject dObject    = new DomainObject(sOID);
        String sValue           = dObject.getInfo(context, sExpression);
               
        String[] aValues        = new String[0];
        String[] aStyles        = new String[0];
        String[] aImages        = new String[0];
        String[] aValuesAfter   = new String[0];
        String[] aValuesBefore  = new String[0];
        
        if(null != sValues)         { aValues       = sValues.split(",");       }
        if(null != sStyles)         { aStyles       = sStyles.split(",");       }
        if(null != sImages)         { aImages       = sImages.split(",");       }        
        if(null != sValuesAfter)    { aValuesAfter  = sValuesAfter.split(",");  }
        if(null != sValuesBefore)   { aValuesBefore = sValuesBefore.split(","); }
        if(null == sAdminType)      { sAdminType    = "";                       }
        
                   
        if((aValues.length == aStyles.length) || (aValues.length == aImages.length)) {
            for(int i = 0; i < aValues.length; i++) {
                if(sValue.equals(aValues[i])) {                    
                    if(aStyles.length == aValues.length) { sbStyle.append(aStyles[i]); }
                    if(aImages.length == aValues.length) { 
                        sbImage = new StringBuilder(); 
                        if(!aImages[i].equals("")) {
                            sbImage.append(sImagePrefix).append(aImages[i]).append(sImageSuffix);
                        }
                    }                
                    bApplyStyle = true; 
                }
            }
        }
        
        
        if(!sValue.equals("")) {            
            
            if(null != sValuesFrom) { 
                String[] aValuesFrom = sValuesFrom.split(",");
                if((aValuesFrom.length == aStyles.length) || (aValuesFrom.length == aImages.length)) {
                    BigDecimal bdValue = new BigDecimal(sValue);    
                    for(int i = aValuesFrom.length - 1; i >= 0; i--) {
                        BigDecimal bdValueFrom = new BigDecimal(aValuesFrom[i]);
                        if(bdValue.compareTo(bdValueFrom) != -1) {
                            if(aStyles.length == aValuesFrom.length) { sbStyle.append(aStyles[i]); }
                            if(aImages.length == aValuesFrom.length) { 
                                sbImage = new StringBuilder(); 
                                if(!aImages[i].equals("")) {
                                    sbImage.append(sImagePrefix).append(aImages[i]).append(sImageSuffix);
                                }
                            }                          
                            bApplyStyle = true;
                            break;
                        }
                    }
                }   
            }
            
            if(null != sValuesTo) { 
                String[] aValuesTo = sValuesTo.split(",");
                if((aValuesTo.length == aStyles.length) || (aValuesTo.length == aImages.length)) {
                    BigDecimal bdValue = new BigDecimal(sValue);    
                    for(int i = 0; i < aValuesTo.length; i++) {
                        BigDecimal bdValueFrom = new BigDecimal(aValuesTo[i]);
                        if(bdValue.compareTo(bdValueFrom) != 1) {
                            if(aStyles.length == aValuesTo.length) { sbStyle.append(aStyles[i]); }
                            if(aImages.length == aValuesTo.length) { 
                                sbImage = new StringBuilder(); 
                                if(!aImages[i].equals("")) {
                                    sbImage.append(sImagePrefix).append(aImages[i]).append(sImageSuffix);
                                }
                            }                          
                            bApplyStyle = true;
                            break;
                        }
                    }
                }   
            }            
            

            if((aValuesAfter.length == aStyles.length) || (aValuesAfter.length == aImages.length)) {
                for(int i = aValuesAfter.length - 1; i >= 0; i--) {
                    
                    String sDiff    = aValuesAfter[i].replace("+", "");
                    sDiff           = sDiff.replace("-", "");
                    sDiff           = sDiff.trim();
                    int iDays       = Integer.parseInt(sDiff);                
                    Calendar cAfter = Calendar.getInstance(TimeZone.getDefault());
                    
                    if(aValuesAfter[i].contains("-")) {
                        cAfter.add(java.util.GregorianCalendar.DAY_OF_YEAR,  - iDays);
                    } else {
                        cAfter.add(java.util.GregorianCalendar.DAY_OF_YEAR,  + iDays);
                    }                   

                    Calendar cActual = Calendar.getInstance();
                    cActual.setTime(sdf.parse(sValue));
                    if(cActual.after(cAfter)) {                  
                        if(aStyles.length == aValuesAfter.length) { sbStyle.append(aStyles[i]); }
                        if(aImages.length == aValuesAfter.length) { 
                            sbImage = new StringBuilder(); 
                            if(!aImages[i].equals("")) {
                                sbImage.append(sImagePrefix).append(aImages[i]).append(sImageSuffix);
                            }
                        }                
                        bApplyStyle = true; 
                        break;                    
                    }
                }
            } else {

            }
        
 
            
            if((aValuesBefore.length == aStyles.length) || (aValuesBefore.length == aImages.length)) {
                for(int i = 0; i < aValuesBefore.length; i++) {

                    String sDiff    = aValuesBefore[i].replace("+", "");
                    sDiff           = sDiff.replace("-", "");
                    sDiff           = sDiff.trim();
                    int iDays       = Integer.parseInt(sDiff);                
                    Calendar cAfter = Calendar.getInstance(TimeZone.getDefault());

                    if(aValuesBefore[i].contains("-")) {
                        cAfter.add(java.util.GregorianCalendar.DAY_OF_YEAR,  - iDays);
                    } else {
                        cAfter.add(java.util.GregorianCalendar.DAY_OF_YEAR,  + iDays);
                    }

                    Calendar cActual = Calendar.getInstance();
                    cActual.setTime(sdf.parse(sValue));
                    if(cActual.before(cAfter)) {                  
                        if(aStyles.length == aValuesBefore.length) { sbStyle.append(aStyles[i]); }
                        if(aImages.length == aValuesBefore.length) { 
                            sbImage = new StringBuilder(); 
                            if(!aImages[i].equals("")) {
                                sbImage.append(sImagePrefix).append(aImages[i]).append(sImageSuffix);
                            }
                        }                
                        bApplyStyle = true; 

                        break;

                    }
                }
            }
            
        }
        
     
      if(bApplyStyle) {
            if(null != sFilterAfter) {

                String sDiff    = sFilterAfter.replace("+", "");
                sDiff           = sDiff.replace("-", "");
                sDiff           = sDiff.trim();
                int iDays       = Integer.parseInt(sDiff);                
                Calendar cAfter = Calendar.getInstance(TimeZone.getDefault());
                
                if(sFilterAfter.contains("-")) {
                    cAfter.add(java.util.GregorianCalendar.DAY_OF_YEAR,  - iDays);
                } else {
                    cAfter.add(java.util.GregorianCalendar.DAY_OF_YEAR,  + iDays);
                }
                
		Calendar cActual = Calendar.getInstance();
                cActual.setTime(sdf.parse(sValue));
                if(cActual.before(cAfter)) {  bApplyStyle = false; }
                
            }
        }
        
       if(bApplyStyle) {
            if(null != sFilterBefore) {

                String sDiff    = sFilterBefore.replace("+", "");
                sDiff           = sDiff.replace("-", "");
                sDiff           = sDiff.trim();
                int iDays       = Integer.parseInt(sDiff);                
                Calendar cAfter = Calendar.getInstance(TimeZone.getDefault());
                
                if(sFilterBefore.contains("-")) {
                    cAfter.add(java.util.GregorianCalendar.DAY_OF_YEAR,  - iDays);
                } else {
                    cAfter.add(java.util.GregorianCalendar.DAY_OF_YEAR,  + iDays);
                }
                
		Calendar cActual = Calendar.getInstance();
                cActual.setTime(sdf.parse(sValue));
                if(cActual.after(cAfter)) {  bApplyStyle = false; }
                
            }
        }        
        if(bApplyStyle) {
            if(null != sFilterOwned) {
                if(sFilterOwned.equalsIgnoreCase("TRUE")) {
                    String sOwner = dObject.getInfo(context, DomainConstants.SELECT_OWNER);
                    String sUser = context.getUser();
                    if(!sOwner.equals(sUser)) {
                        bApplyStyle = false;
                    }
                }
            }
        }
     
        if(bApplyStyle) {
            if(null != sFilterStatus) {
                String[] aFilterStatus = sFilterStatus.split(",");
                String sCurrent = dObject.getInfo(context, DomainConstants.SELECT_CURRENT);
                bApplyStyle = false;
                for(int i = 0; i < aFilterStatus.length; i++) {
                    if(aFilterStatus[i].equals(sCurrent)) {
                        bApplyStyle = true; 
                        continue;
                    }
                }
            }        
        }
        if(bApplyStyle) {
            if(null != sAttributeName) {
                if(null != sAttributeValue) {
                    String sValueData = dObject.getInfo(context, "attribute[" + sAttributeName + "]");
                    if(sValueData.equals(sAttributeName)) {
                        bApplyStyle = true; 
                    }
                }
            }        
        }       
        
        if(!sValue.equals("")) {
        
            
            // Filter by Value (range)
            if(bApplyStyle) {
                if(null != sFilterFrom) {                
                    BigDecimal bdFilterFrom = new BigDecimal(sFilterFrom);
                    BigDecimal bdValue = new BigDecimal(sValue);                
                    if(bdValue.compareTo(bdFilterFrom) != -1) { bApplyStyle = true;
                    } else { bApplyStyle = false; }
                }
            }
            if(bApplyStyle) {    
                if(null != sFilterTo) {                
                    BigDecimal bdFilterTo = new BigDecimal(sFilterTo);
                    BigDecimal bdValue = new BigDecimal(sValue);                
                    if(bdValue.compareTo(bdFilterTo) != 1) { bApplyStyle = true;
                    } else { bApplyStyle = false; }                                
                }
            }
        }
            
        if(sFormat.equalsIgnoreCase("date")) {

            if(!"".equals(sValue)) {
                sLang = sLang.substring(0, 2);        
                Locale locale = new Locale(sLang);                    
                DateFormat df = DateFormat.getDateInstance(Integer.parseInt(sDisplayFormat), locale);   
                Date date = (Date)sdf.parse(sValue);                                      
                sValue = df.format(date);
            }

        } else if(sFormat.equalsIgnoreCase("user")) {
            com.matrixone.apps.common.Person person = com.matrixone.apps.common.Person.getPerson(context, sValue);
            String sLastName = person.getInfo(context, "attribute["+ DomainConstants.ATTRIBUTE_LAST_NAME +"]");
            String sFirstName = person.getInfo(context, "attribute["+ DomainConstants.ATTRIBUTE_FIRST_NAME +"]");
            sValue = sLastName + ", " + sFirstName;
        }
               
        if(bApplyStyle) {
            sbResult.append(sOutputPrefix); 
            sbResult.append(" style='"); 
            sbResult.append(sbStyle.toString()); 
            sbResult.append("' >");             
            sbResult.append(sbImage.toString());
        }   
              
        if(!sAdminType.equals("")) {    
            String sResult = MqlUtil.mqlCommand(context, "print program $1 select $2 dump", true, "eServiceSchemaVariableMapping.tcl", "property[" + sAdminType + "].to");                
            if(sResult.startsWith(("att "))) { sResult = sResult.substring(4); }
            sValue = i18nNow.getRangeI18NString(sResult, sValue, sLang);            
        }
               
        sbResult.append(sValue);
        if(bApplyStyle) {
            sbResult.append(sOutputSuffix);
        }
        return sbResult.toString();        

    }    
    
    // Related Items Field
    public String fieldRelatedItems(Context context, String[] args) throws Exception {        
        
        HashMap programMap  = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap  = (HashMap) programMap.get("requestMap");
        HashMap paramMap    = (HashMap) programMap.get("paramMap");
        String sOID         = (String) paramMap.get("objectId");        
        HashMap fieldMap    = (HashMap) programMap.get("fieldMap");
        HashMap settings    = (HashMap) fieldMap.get("settings");                
        MapList mlObjects   = new MapList();
        Map mObject         = new HashMap();                
        String sLang        = (String)requestMap.get("languageStr");
        
        mObject.put(DomainConstants.SELECT_ID, sOID);
        mObject.put("id[level]", "form");
        mlObjects.add(mObject);
        
        Vector vResult = emxGenericColumnsBase_mxJPO.controlRelatedItems(context, args, (String) requestMap.get("StringResourceFileId"), settings, mlObjects, "form", "formViewHidden", sLang); 
        
        return (String)vResult.get(0);
    }    
    
    
 // URL Field
    public  String fieldURL(Context context, String[] args) throws Exception {        
       
        StringBuilder sbResult  = new StringBuilder();  
        HashMap programMap      = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap        = (HashMap) programMap.get("paramMap");
        HashMap requestMap      = (HashMap) programMap.get("requestMap");
        HashMap fieldMap        = (HashMap) programMap.get("fieldMap");
        HashMap settings        = (HashMap) fieldMap.get("settings");
        String sOID             = (String) paramMap.get("objectId");        
        String sName            = (String)fieldMap.get("name");
        String sLabel           = (String)fieldMap.get("label");
        String sURL             = (String)settings.get("Url");
        String sCommand         = (String)settings.get("Command");
        String sHeight          = (String)settings.get("Height");        
        String sHideLabel       = (String)settings.get("Hide Label");        
        String sNoFrame         = (String)settings.get("No Frame");        
//        String sCollapsed       = (String)settings.get("Collapsed");
        String sLanguage        = (String)requestMap.get("languageStr");
        String sSuite           = (String)settings.get("Registered Suite");
        String sBorderBottom    = (String)settings.get("Border Bottom");        
        
        if(null == sHeight)         { sHeight       = "300"; }
        if(null == sHideLabel)      { sHideLabel    = "FALSE"; }
        if(null == sNoFrame)        { sNoFrame      = "FALSE"; }
        if(null == sBorderBottom)   { sBorderBottom = "FALSE"; }
        sName = sName.replace(" ", "");
        
        sLabel = EnoviaResourceBundle.getProperty(context, sSuite, sLabel, sLanguage);       
        
        StringBuilder sbStyleImage = new StringBuilder();
        sbStyleImage.append("style='");
        sbStyleImage.append("background: url(\"../plugins/Compass/images/open_close.png\") no-repeat scroll 0 -24px rgba(0, 0, 0, 0);");
        sbStyleImage.append("height: 22px;");
        sbStyleImage.append("width: 22px;");
        
        if(UIUtil.isNotNullAndNotEmpty(sURL)){
        	sURL = UINavigatorUtil.parseHREF(context, sURL, sSuite);
        } else {
            sURL = "";
        }   
        
        if(UIUtil.isNullOrEmpty(sURL) && UIUtil.isNotNullAndNotEmpty(sCommand)){
                
            StringBuilder sbURL     = new StringBuilder();
            Command command         = new Command(context, sCommand);
            String sHref            = command.getHref();
            SelectSetting setting   = command.getSettings();
            String sSuiteCommand    = setting.getValue("Registered Suite");
            
            sHref = UINavigatorUtil.parseHREF(context, sHref, sSuite);             
            sbURL.append(sHref);
            if(!sHref.contains("?")) { sbURL.append("?"); }
            sbURL.append("&objectId=").append(sOID);
            sbURL.append("&parentOID=").append(sOID);
            
            if(UIUtil.isNotNullAndNotEmpty(sSuiteCommand)){             
                    String sSuiteDirCommand = EnoviaResourceBundle.getProperty(context, "eServiceSuite" + sSuiteCommand + ".Directory"); 
                    String sResourceFileCommand = EnoviaResourceBundle.getProperty(context, "eServiceSuite" + sSuiteCommand + ".StringResourceFileId");                         
                    sbURL.append("&suiteKey=").append(sSuiteCommand);
                    sbURL.append("&SuiteDirectory=").append(sSuiteDirCommand);
                    sbURL.append("&StringResourceFileId=").append(sResourceFileCommand);
            }

            sURL = sbURL.toString();
        }         
        
        if(!sURL.contains("?")) { sURL += "?"; }       
        
        if(sNoFrame.equalsIgnoreCase("FALSE")) {
            sbResult.append("<script type='text/javascript'>");
            sbResult.append("  $(document).ready(function() {");
            sbResult.append("   $('#header").append(sName).append("').parent().css(\"padding\", \"0px\");");
            if(sHideLabel.equalsIgnoreCase("TRUE")) {
                if(sBorderBottom.equalsIgnoreCase("FALSE")) {
                    sbResult.append("   $('#header").append(sName).append("').parent().css(\"border-bottom\", \"none\");");
                }
            }
            sbResult.append("});");
                                 
            sbResult.append("function toggle").append(sName).append(" () { ");
            sbResult.append("   var visible = $('#").append(sName).append("').is(':visible');");
            sbResult.append("	if(visible) {");
            sbResult.append("           $('#").append(sName).append("').fadeOut('200', function() { $(header").append(sName).append(").toggleClass('extendedHeader extendedHeaderCollapsed'); });");
            sbResult.append("           $('#imgExpand").append(sName).append("').show();");
            sbResult.append("           $('#imgCollapse").append(sName).append("').hide();");
            sbResult.append("	} else {");
            sbResult.append("           $('#header").append(sName).append("').toggleClass('extendedHeaderCollapsed extendedHeader');");        
            sbResult.append("           $('#imgExpand").append(sName).append("').hide();");
            sbResult.append("           $('#imgCollapse").append(sName).append("').show();");
            sbResult.append("           $('#").append(sName).append("').fadeIn();");
            sbResult.append("	}");        
            sbResult.append("}");
            sbResult.append("</script>");
                   
            sbResult.append("<div class='extendedHeader' id='header").append(sName).append("' onclick='javascript:toggle").append(sName).append("();'>");
            sbResult.append("<span id='imgExpand").append(sName).append("' ").append(sbStyleImage.toString()).append("display:none;").append("'></span>");
            sbResult.append("<span id='imgCollapse").append(sName).append("' ").append(sbStyleImage.toString()).append("background-position: 0 0;").append("'></span>");
            sbResult.append(sLabel);
            sbResult.append("</div>");
            sbResult.append("<div class='extendedHeaderFieldPanel' id='").append(sName).append("'>");       
            
        } else {        
            sbResult.append("<script type='text/javascript'>");
            sbResult.append("$(document).ready(function() {");
            sbResult.append("   $('#frameField").append(sName).append("').parent().css(\"padding\", \"0px\");");
            if(sHideLabel.equalsIgnoreCase("TRUE")) {
                if(sBorderBottom.equalsIgnoreCase("FALSE")) {
                    sbResult.append("   $('#frameField").append(sName).append("').parent().css(\"border-bottom\", \"none\");");
                }
            }
            sbResult.append("});");
            sbResult.append("</script>");
        }     
        
        sbResult.append("<div style='height:").append(sHeight).append("px;position:relative'>");
        sbResult.append("<iframe type='text/html' id='frameField").append(sName).append("' name='frameField").append(sName).append("' src='");
        sbResult.append(sURL);
        sbResult.append("&hideAllHeader=true&portalMode=true&objectId=").append(sOID);
        sbResult.append("' width='100%' frameBorder='0' style='padding:0px;margin:0px;'></iframe></div>");                         
               

        if(sNoFrame.equalsIgnoreCase("FALSE")) {
            sbResult.append("</div>");      
        }
        
        return sbResult.toString();
        
    }          
    
    
    // Route Tasks Field
    public  String fieldRouteTasks(Context context, String[] args) throws Exception {
          

        HashMap programMap      = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap        = (HashMap) programMap.get("paramMap");
        HashMap requestMap      = (HashMap) programMap.get("requestMap");
        String sLang            = (String)requestMap.get("languageStr");
        HashMap fieldMap        = (HashMap) programMap.get("fieldMap");
        HashMap settings        = (HashMap) fieldMap.get("settings");
        String sRoutes          = (String)settings.get("Routes");
        String sHideLabel       = (String)settings.get("Hide Label");
        String sHeight          = (String)settings.get("Height");
        String sIncludeStart    = (String)settings.get("Include Start");
        String sOID             = (String) paramMap.get("objectId");        
        DomainObject dObject    = new DomainObject(sOID);
        MapList mlTasks         = new MapList();
        String sCurrent         = dObject.getInfo(context, "current");        
        String sWhere           = "";
        
        if(null == sRoutes) { sRoutes = "all"; }
        if(null == sIncludeStart) { sIncludeStart = "false"; }
        
        
        StringList busSelects = new StringList();
        busSelects.add(DomainConstants.SELECT_ID);
        
        
        sCurrent = sCurrent.replace(" ", "");
        if(sRoutes.equalsIgnoreCase("blocking")) { sWhere = "attribute[Route Base State] == 'state_" + sCurrent + "'"; }
	
	MapList mlRoutes = dObject.getRelatedObjects(context, DomainConstants.RELATIONSHIP_OBJECT_ROUTE, DomainConstants.TYPE_ROUTE, busSelects, null, false, true, (short)1, "(current != 'Archive')", sWhere, 0);
	for(int i = 0; i < mlRoutes.size(); i++) {
            Map mRoute 			= (Map)mlRoutes.get(i);
            String sOIDRoute 		= (String)mRoute.get(DomainConstants.SELECT_ID);
            DomainObject doRoute 	= new DomainObject(sOIDRoute);
            MapList mlRouteTasks 	= doRoute.getRelatedObjects(context, DomainConstants.RELATIONSHIP_ROUTE_NODE, DomainConstants.TYPE_PERSON, busSelects, null, false, true, (short)1, "", "", 0);	
            mlTasks.addAll(mlRouteTasks);
	}        
        

        if(mlTasks.size() > 0) {
        
            int iHeight     = 40 + (mlTasks.size() * 40);
            if(iHeight < 120) { iHeight = 120; }
            
            if( (null == sHeight) || (sHeight.equals("")) ) {            
                sHeight  = String.valueOf(iHeight);
            }

            HashMap programMapNew   = new HashMap();
            HashMap paramMapNew     = new HashMap();
            HashMap requestMapNew   = new HashMap();
            HashMap fieldMapNew     = new HashMap();
            HashMap settingsNew     = new HashMap();
            
            settingsNew.put("Hide Label", sHideLabel);
            settingsNew.put("Height", sHeight);
            settingsNew.put("Url", "../common/GNVChartRouteTasks.jsp?routes=" + sRoutes + "&includeStart=" + sIncludeStart);
            settingsNew.put("Registered Suite", "Components");
            fieldMapNew.put("settings", settingsNew);
            fieldMapNew.put("name", "fieldRouteTasks");
            fieldMapNew.put("label", "emxComponents.TaskSummary.Tasks");
            requestMapNew.put("StringResourceFileId", "emxComponentsStringResource");
            requestMapNew.put("languageStr", sLang);
            paramMapNew.put("objectId", sOID);
            programMapNew.put("fieldMap",   fieldMapNew);
            programMapNew.put("paramMap",   paramMapNew);
            programMapNew.put("requestMap", requestMapNew);
            
            return fieldURL(context, JPO.packArgs (programMapNew));
            
        } else {
            return "";
        }
        
    }  
    
    
    // Field Graphic Workflow
    public  String fieldWorkflowGraphic(Context context, String[] args) throws Exception {

        HashMap programMap  = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap    = (HashMap) programMap.get("paramMap");
        HashMap fieldMap    = (HashMap) programMap.get("fieldMap");
        HashMap settingsMap = (HashMap) fieldMap.get("settings");
        String sOID         = (String) paramMap.get("objectId");
        String sHeight      = (String) settingsMap.get("height");
      
        if (null == sHeight || "".equals(sHeight)) {

            DomainObject dObject = new DomainObject(sOID);
            String sIsRoute = dObject.getInfo(context, "type.kindof["+ DomainConstants.TYPE_ROUTE +"]");
            String attrRouteSequene = DomainConstants.ATTRIBUTE_ROUTE_SEQUENCE;
            	
            if(sIsRoute.equalsIgnoreCase("FALSE")) {
                sOID = dObject.getInfo(context, "from["+DomainConstants.RELATIONSHIP_ROUTE_TASK +"].to.id");
            }            
            
            StringList busSelects = new StringList();
            StringList relSelects = new StringList();
            relSelects.add("attribute["+ attrRouteSequene +"]");

            DomainObject doRoute = new DomainObject(sOID);
            MapList mlTasks = doRoute.getRelatedObjects(context, DomainConstants.RELATIONSHIP_ROUTE_NODE, DomainConstants.TYPE_PERSON, busSelects, relSelects, false, true, (short)1, "", "", 0);           
            mlTasks.sort("attribute["+ attrRouteSequene +"]", "ascending", "String");
            
            if(mlTasks.size() > 0) {
            
                int iMax = 0;
                int iCount = 0;
                Map mTask = (Map)mlTasks.get(0);
                String sSeqPrev = (String)mTask.get("attribute["+ attrRouteSequene +"]");
                for (int i = 0; i < mlTasks.size(); i++) {
                    mTask = (Map)mlTasks.get(i);
                    String sSeq = (String)mTask.get("attribute["+ attrRouteSequene +"]");
                    if(sSeqPrev.equals(sSeq)) {
                        iCount++;
                        if(iCount > iMax) { iMax = iCount; }                        
                    } else {
                        iCount = 1;
                        sSeqPrev = sSeq;
                    }
                }
                
                if(iMax == 1) { 
                    sHeight = "100";
                } else {
//                    int iHeight = 20 + (iMax * 96) + 20;
                    //int iHeight = 10 + ((iMax - 1) * 96) + 75 + 15;
                    int iHeight = 24 + 72 + ((iMax - 1) * 97) + 24;
                    sHeight = String.valueOf(iHeight);
                }

            } else {            
                sHeight = "60";            
            }
        }
        
        StringBuilder sbResult = new StringBuilder();        
        
        sbResult.append("<script type='text/javascript'>");
        sbResult.append("$(document).ready(function() {");
        sbResult.append("   $('#frameGNVWorkflow').parent().css(\"padding\", \"0px\");");
        sbResult.append("   $('#frameGNVWorkflow').parent().css(\"border-bottom\", \"none\");");
        sbResult.append("});");
        sbResult.append("</script>");
        
        sbResult.append("<object id='frameGNVWorkflow' type='text/html'");
        sbResult.append("data='../common/GNVWorkflowGraphic.jsp?objectId=").append(sOID).append("'");
        sbResult.append("width='100%' height='").append(sHeight).append("px' style='overflow:none;padding:0px;margin:0px'></object>");

        return sbResult.toString();

    }  
 
    
    // Keywords attribute field
    public static String fieldKeywords(Context context, String[] args) throws Exception {

        StringBuilder sbResult  = new StringBuilder();
        HashMap programMap      = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap        = (HashMap) programMap.get("paramMap");
        HashMap fieldMap        = (HashMap) programMap.get("fieldMap");
        HashMap settingsMap     = (HashMap) fieldMap.get("settings");
        String sOID             = (String) paramMap.get("objectId");
        String sTable          = (String) settingsMap.get("Table");
        
        if(sTable == null) { sTable = "AEFGeneralSearchResults"; }
        DomainObject dObject = new DomainObject(sOID);
        
        String sKeywords = dObject.getInfo(context, "attribute["+ DomainConstants.ATTRIBUTE_KEYWORDS +"]");
        String sRootType = dObject.getInfo(context, "type.kindof");
        
        sRootType = FrameworkUtil.getAliasForAdmin(context, "Type", sRootType, true);

        
        if(null != sKeywords) {
            if (!"".equals(sKeywords)) {
                String[] aKeywords = sKeywords.split(";");
                for(int i = 0; i < aKeywords.length; i++) {
                    String sKeyword = aKeywords[i];
                    sKeyword = sKeyword.trim();
                    sbResult.append("<a style='margin-right:20px;vertical-align:middle;' onclick=\"");
                    sbResult.append("   var posLeft=(screen.width/2)-(490);");
                    sbResult.append("   var posTop = (screen.height/2)-(330);");
                    sbResult.append("   window.open('../common/emxFullSearch.jsp?field=TYPES=").append(sRootType).append(":KEYWORDS=").append(sKeyword).append("&amp;table=").append(sTable).append("', ");
                    sbResult.append("      '', 'height=650,width=980,top=' + posTop + ',left=' + posLeft + ',toolbar=no,directories=no,status=no,menubar=no;return false;');");
                    sbResult.append("\" >");
                    sbResult.append("<img style='vertical-align:middle;' src='../common/images/iconSmallTag.png' /> ");
                    sbResult.append(sKeyword);
                    sbResult.append("</a> ");
                }
            }
        }
        
        return sbResult.toString();

    }        

    
}
