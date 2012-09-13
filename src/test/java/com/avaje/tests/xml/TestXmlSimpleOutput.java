package com.avaje.tests.xml;

import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import junit.framework.TestCase;

import com.avaje.ebean.Ebean;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;
import com.avaje.tests.xml.build.XbContextBuilder;
import com.avaje.tests.xml.oxm.OxmContext;
import com.avaje.tests.xml.oxm.OxmContextBuilder;
import com.avaje.tests.xml.oxm.OxmNode;

public class TestXmlSimpleOutput extends TestCase {

    public void test() throws Exception {
        
        ResetBasicData.reset();
        
        SpiEbeanServer server = (SpiEbeanServer)Ebean.getServer(null);
                
        OxmContextBuilder builder = new XbContextBuilder(server, null);
        
        OxmNode node = builder.addRootElement("header", XHeader.class);
        node.addElement("requestedBy");
        //node.addElement("requestedSystem");
        OxmNode horders = node.addElement("orders");
        horders.addElement("id");
        horders.addElement("cretime");
        horders.addElement("orderDate");
        
        
//        OxmNode orderRoot = builder.addRootElement("order", Order.class);
//        
//        orderRoot.addElement("id").addAttribute("status");
//        orderRoot.addElement("cretime","created-ts");
//        orderRoot.addElement("orderDate","ship-date");
//        OxmNode cust = orderRoot.addElement("customer","cust");
//        cust.addAttribute("id");
//        cust.addElement("name");
//        
//        OxmNode orderDetails = orderRoot.addElement("details","order-details");
//        OxmNode line = orderDetails.addWrapperElement("line");
//        line.addElement("id");
//        line.addElement("orderQty","order-quantity");
//        line.addElement("unitPrice","unit-price");
//        
//        OxmNode product = line.addElement("product");
//        product.addElement("name","prodname");
//        product.addElement("sku","sku");

        
        OxmContext oxmContext = builder.createContext();
        

        List<Order> list = Ebean.find(Order.class)
            .fetch("details")
            .fetch("details.product","sku,name")
            .findList();
        
        XHeader header = new XHeader();
        header.setRequestedBy("blah");
        header.setRequestedSystem("twoSystem");
        header.setOrders(list);
//        
//        Order bean = list.get(0);
//        
//        Writer writer = new StringWriter();
//        oxmContext.writeBean(bean, writer);
//        System.out.println(writer);
//        
        Writer writer = new StringWriter();
        oxmContext.writeBean(header, writer);
        System.out.println(writer);
//        
//        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
//        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
//        Document doc = docBuilder.newDocument();
//
//
//        
//        oxmContext.writeBean(bean, doc);
//        
//        System.out.println(doc.getDocumentElement());
//        
//        NodeList childNodes = doc.getChildNodes();
//        int len = childNodes.getLength();
//        for (int i = 0; i < len; i++) {
//            
//            Node childNode = childNodes.item(i);
//            Order o = (Order)oxmContext.readBean(childNode);
//            System.out.println(o);
//            System.out.println(o.getDetails());
//                        
//        }
        
        
    }
    
}
