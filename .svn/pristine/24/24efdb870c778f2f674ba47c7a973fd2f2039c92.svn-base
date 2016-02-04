package com.apicloud.commons.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;


@SuppressWarnings("unchecked")
public final class XMLUtil {

    private XMLUtil() {
    }

    /**
     * 读取XML文件
     * @param filePath 文件路径
     * @return
     * @throws DocumentException
     */
    public static Document loadXmlFile(String filePath) throws DocumentException {
        return loadXmlFile(new File(filePath));
    }

    /**
     * 读取XML文件
     * @param file 文件
     * @return
     * @throws DocumentException
     */
    public static Document loadXmlFile(File file) throws DocumentException {
        SAXReader reader = new SAXReader();
        return reader.read(file);
    }

    /**
     * 读取XML文件
     * @param in 输入流
     * @return
     * @throws DocumentException
     */
    public static Document loadXmlFile(InputStream in) throws DocumentException {
        SAXReader reader = new SAXReader();
        reader.setEncoding("UTF-8");
        return reader.read(in);
    }

    /**
     * 写XML文件
     * @param filePath 文件路径
     * @param document XML对象
     * @return
     * @throws IOException
     */
    public static boolean saveXml(String filePath, Document document) throws IOException {
        return saveXml(new File(filePath), document);
    }

    /**
     * 写XML文件
     * @param file 文件
     * @param document XML对象
     * @return
     * @throws IOException
     */
    public static boolean saveXml(File file, Document document) throws IOException {
        OutputFormat format = OutputFormat.createPrettyPrint(); // 紧缩
        format.setEncoding("UTF-8"); // 设置UTF-8编码
        return saveXml(file, document, format);
    }

    /**
     * 写XML文件
     * @param filePath 文件路径
     * @param document XML对象
     * @param format XML输出格式
     * @return
     * @throws IOException
     */
    public static boolean saveXml(String filePath, Document document, OutputFormat format) throws IOException {
        return saveXml(new File(filePath), document, format);
    }

    /**
     * 写XML文件
     * @param file 文件
     * @param document XML对象
     * @param format XML输出格式
     * @return
     * @throws IOException
     */
    public static boolean saveXml(File file, Document document, OutputFormat format) throws IOException {
        FileOutputStream fos = null;
        XMLWriter xmlWriter = null;
        try {
            file.getParentFile().mkdirs();
            fos = new FileOutputStream(file);
            xmlWriter = new XMLWriter(fos, format);
            xmlWriter.write(document);
            return true;
        } finally {
        	if(xmlWriter != null) {
        		xmlWriter.close();
        	}
        	if(fos != null) {
        		fos.close();
        	}
        }
    }

    /**
     * 创建一个空的XML文档
     * @return
     */
    public static Document createDocument() {
        // 使用DocumentHelper.createDocument方法建立一个文档实例
        return DocumentHelper.createDocument();
    }

    /**
     * 设置或添加属性
     * @param element
     * @param name
     * @param value
     */
    public static void setAttribute(Element element, String name, String value) {
        Attribute attribute = element.attribute(name);
        if (attribute != null) {
            attribute.setValue(value);
        } else {
            element.addAttribute(name, value);
        }
    }
    /**
     * 设置元素的内容
     * @param element
     * @param value
     */
    public static void setText(Element element, String value) {
        element.setText(value);
    }

    /**
     * 设置或添加子元素
     * @param element
     * @param childName
     */
    public static Element setElementChild(Element element, String childName) {
        Element child = element.element(childName);
        if (child == null) {
            child = element.addElement(childName);
        }
        return child;
    }

    /**
     * 设置或添加子元素
     * @param element
     * @param childName
     * @param childValue
     */
    public static Element setElementChild(Element element, String childName, String childValue) {
        Element child = element.element(childName);
        if (child != null) {
            child.setText(childValue);
        } else {
            child = element.addElement(childName, childValue);
        }
        return child;
    }

    /**
     * 创建XML文件
     * @return
     */
    private static Document testCreateXMLFile() {
        // 使用DocumentHelper.createDocument方法建立一个文档实例
        Document document = DocumentHelper.createDocument();
        // 使用addElement方法方法创建根元素
        Element catalogElement = document.addElement("catalog");
        // 使用addComment方法方法向catalog元素添加注释
        catalogElement.addComment("使用addComment方法方法向catalog元素添加注释");
        // 使用addProcessInstruction向catalog元素增加处理指令
        catalogElement.addProcessingInstruction("target", "text");

        // 使用addElement方法向catalog元素添加journal子元素
        Element journalElement = catalogElement.addElement("journal");
        // 使用addAttribute方法向journal元素添加title和publisher属性
        journalElement.addAttribute("title", "XML Zone");
        journalElement.addAttribute("publisher", "Willpower Co");

        // 使用addElement方法向journal元素添加article子元素
        Element articleElement = journalElement.addElement("article");
        // 使用addAttribute方法向article元素添加level和date属性
        articleElement.addAttribute("level", "Intermediate");
        articleElement.addAttribute("date", "July-2006");

        // 使用addElement方法向article元素添加title子元素
        Element titleElement = articleElement.addElement("title");
        // 使用setText方法设置title子元素的值
        titleElement.setText("Dom4j Create XML Schema");

        // 使用addElement方法向article元素添加authorElement子元素
        Element authorElement = articleElement.addElement("author");

        // 使用addElement方法向author元素添加firstName子元素
        Element firstName = authorElement.addElement("fistname");
        // 使用setText方法设置firstName子元素的值
        firstName.setText("Yi");

        // 使用addElement方法向author元素添加lastname子元素
        Element lastName = authorElement.addElement("lastname");
        // 使用setText方法设置lastName子元素的值
        lastName.setText("Qiao");

        return document;
    }

    /**
     * 修改XML文件
     * @param fileName
     * @param newFileName
     * @return
     */
    private static Document testModifyXMLFile(String fileName) {
        SAXReader reader = new SAXReader();
        Document document = null;
        try {
            document = reader.read(new File(fileName));
            // 用xpath查找对象
            List<?> list = document.selectNodes("/catalog/journal/@title");
            Iterator<?> itr = list.iterator();
            while (itr.hasNext()) {
                Attribute attribute = (Attribute) itr.next();
                if (attribute.getValue().equals("XML Zone")) {
                    attribute.setText("Modi XML");
                }
            }
            // 在journal元素中增加date元素
            list = document.selectNodes("/catalog/journal");
            itr = list.iterator();
            if (itr.hasNext()) {
                Element journalElement = (Element) itr.next();
                Element dateElement = journalElement.addElement("date");
                dateElement.setText("2006-07-10");
                dateElement.addAttribute("type", "Gregorian calendar");
            }
            // 删除title接点
            list = document.selectNodes("/catalog/journal/article");
            itr = list.iterator();
            while (itr.hasNext()) {
                Element articleElement = (Element) itr.next();
                Iterator<Element> iter = articleElement.elementIterator("title");
                while (iter.hasNext()) {
                    Element titleElement = (Element) iter.next();
                    if (titleElement.getText().equals("Dom4j Create XML Schema")) {
                        articleElement.remove(titleElement);
                    }
                }
            }

        } catch (DocumentException e) {
            e.printStackTrace();
        }

        return document;

    }

    /**
     * 递归显示文档内容
     * @param els elements数组
     */
    private static void print(List<Element> els) {
        for (Element el : els) {
            if (el.hasContent()) {
                print(el.elements());
            }
        }
    }

	 public static String readFileByLines(File file) {
		 String string = ""; //$NON-NLS-1$
		 BufferedReader reader = null;
         try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-8")); //$NON-NLS-1$
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
               string += tempString;
            }
            reader.close();
         } catch (IOException e) {
        		e.printStackTrace();
         } finally {
        	 if (reader != null) {
        		 try {
                    reader.close();
                 } catch (IOException e1) {
                }
            }
        }
        return string;
    }
	 
	 public static String formatXml(String str) throws Exception {
		  Document document = null;
		  document = DocumentHelper.parseText(str.trim());
		  // 格式化输出格式
		  OutputFormat format = OutputFormat.createPrettyPrint();
		  format.setEncoding("UTF-8");
		  StringWriter writer = new StringWriter();
		  // 格式化输出流
		  XMLWriter xmlWriter = new XMLWriter(writer, format);
		  // 将document写入到输出流
		  xmlWriter.write(document);
		  xmlWriter.close();

		  return writer.toString();
		 }
    public static void main(String[] args) throws IOException {
        Document document = testCreateXMLFile();
        String filePath = "d:/temp/test_create.xml";
        saveXml(filePath, document);

        document = testModifyXMLFile(filePath);
        String modifyPath = "d:/temp/test_modify.xml";
        saveXml(modifyPath, document);

        // 读取xml文档
        Document doc;
        try {
            doc = loadXmlFile(filePath);
            List<Element> listEl = doc.getRootElement().elements();

            print(listEl);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }
}
