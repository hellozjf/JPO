import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.MapList;

import matrix.db.Context;
import matrix.util.StringList;
import net.sf.jxls.transformer.XLSTransformer;

public class Bmbim_excels_mxJPO extends emxDomainObject_mxJPO {

    
    public Bmbim_excels_mxJPO(Context arg0, String[] arg1) throws Exception {
        super(arg0, arg1);
        // TODO Auto-generated constructor stub
    }

    public static final String inputFolder = "/home/webapp/template";
    public static final String outputFolder = "/tmp";
    
    public String createContractBasicInformationTable(Context context, String[] args) {
        String fileName = "??????????";
        FileInputStream in = null;
        FileOutputStream out = null;
        
        try {
            // ?????Bmbim_contracts
            StringList objectSelects = new StringList();
            objectSelects.add(DomainConstants.SELECT_ID);
            MapList mapList = DomainObject.findObjects(context, "Bmbim_contract", "*", "", objectSelects);
            List<Bmbim_contract_mxJPO> contracts = new ArrayList<Bmbim_contract_mxJPO>();
            int index = 0;
            for (Iterator iter = mapList.iterator(); iter.hasNext();) {
                Map map = (Map) iter.next();
                String oid = (String) map.get(DomainConstants.SELECT_ID);
                DomainObject domainObject = DomainObject.newInstance(context, oid);
                Bmbim_contract_mxJPO contract = new Bmbim_contract_mxJPO(context, args);
                index++;
                String id = domainObject.getInfo(context, "name");
                String name = domainObject.getInfo(context, "attribute[Bmbim_contract_name]");
                String state = domainObject.getInfo(context, "state");         
                String signDate = domainObject.getInfo(context, "attribute[Bmbim_contract_signdate]");      
                String totalMoney = domainObject.getInfo(context, "attribute[Bmbim_contract_totalamount]");    
                String scale = domainObject.getInfo(context, "attribute[Bmbim_contract_projectscale]");         
                String clientName = domainObject.getInfo(context, "attribute[Bmbim_contract_clientname]");    
                String measure = domainObject.getInfo(context, "attribute[Bmbim_contract_measure]");       
                String geology = domainObject.getInfo(context, "attribute[Bmbim_contract_geology]");       
                String other = domainObject.getInfo(context, "attribute[Bmbim_contract_other]");         
                String hasMeetingFee = Double.parseDouble(domainObject.getInfo(context, "attribute[Bmbim_contract_meetingcharge]")) > 0 ? "true" : "false"; 
                contract.setParameters(String.valueOf(index), id, name, state, signDate, totalMoney, scale, clientName, measure, geology, other, hasMeetingFee);
                contracts.add(contract);
            }

            // ????
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("contracts", contracts);
            
            // ???????
            in = new FileInputStream(inputFolder + "/" + fileName);
            File folder = new File(outputFolder + "/" + new Date().getTime()); 
            folder.mkdirs();
            File outputPathFile = new File(folder + "/" + fileName);
            out = new FileOutputStream(outputPathFile);
            XLSTransformer xls = new XLSTransformer();
            Workbook workbook = xls.transformXLS(in, data);
            workbook.write(out);
            
            return outputPathFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}
