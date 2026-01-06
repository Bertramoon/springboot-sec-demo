package com.example.demo.utils;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;

public class ExcelSaxReader {
    /**
     * SAX方式读取Excel数据（内存安全）
     * @param path 文件路径
     */
    public static void read(String path) throws Exception {
        try (OPCPackage pkg = OPCPackage.open(path, PackageAccess.READ)) {
            XSSFReader reader = new XSSFReader(pkg);
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            saxParserFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            saxParserFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            saxParserFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
            XSSFReader.SheetIterator sheets = (XSSFReader.SheetIterator) reader.getSheetsData();
            while (sheets.hasNext()) {
                try (InputStream sheetStream = sheets.next()) {
                    saxParser.parse(sheetStream, new DefaultHandler());
                }
            }
        }
    }
}
