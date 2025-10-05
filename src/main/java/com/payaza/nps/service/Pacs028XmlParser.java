package com.payaza.nps.service;

import com.payaza.nps.dto.Pacs028ResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Specialized XML Parser for PACS.028 Payment Status Request
 * 
 * Parses the decrypted PACS.028 XML and extracts all relevant fields
 * according to the NIBSS specification. PACS.028 is used to request
 * the status of a previously sent payment instruction or related transaction.
 * 
 * Structure:
 * - Group Header: Message identification and creation details
 * - Original Group Information: Reference to original payment message
 * - Transaction Information: Specific transaction details for status request
 */
@Service
public class Pacs028XmlParser {

    private static final Logger logger = LoggerFactory.getLogger(Pacs028XmlParser.class);

    /**
     * Parse PACS.028 XML and extract all relevant information
     */
    public Pacs028ResponseDto parsePacs028Xml(String xmlContent) throws Exception {
        logger.info("Parsing PACS.028 XML content");
        
        try {
            Document doc = parseXmlDocument(xmlContent);
            
            Pacs028ResponseDto response = new Pacs028ResponseDto();
            
            // Parse Group Header section
            parseGroupHeaderSection(doc, response);
            
            // Parse Original Group Information section
            parseOriginalGroupInfoSection(doc, response);
            
            // Parse Transaction Information section
            parseTransactionInfoSection(doc, response);
            
            // Set default values and determine request status
            setDefaultValues(response);
            
            logger.info("Successfully parsed PACS.028 XML");
            return response;
            
        } catch (Exception e) {
            logger.error("Error parsing PACS.028 XML: {}", e.getMessage(), e);
            throw new Exception("Failed to parse PACS.028 XML: " + e.getMessage(), e);
        }
    }

    private void parseGroupHeaderSection(Document doc, Pacs028ResponseDto response) {
        try {
            // Get Group Header section
            NodeList groupHeaderList = doc.getElementsByTagName("GrpHdr");
            if (groupHeaderList.getLength() > 0) {
                Element groupHeader = (Element) groupHeaderList.item(0);
                
                // Parse Message ID
                String messageId = getElementTextContent(groupHeader, "MsgId");
                response.setMessageId(messageId);
                
                // Parse Creation Date Time
                String creationDateTime = getElementTextContent(groupHeader, "CreDtTm");
                
                // Parse Instructing Agent (Bank that sent the status request)
                NodeList instgAgtList = groupHeader.getElementsByTagName("InstgAgt");
                if (instgAgtList.getLength() > 0) {
                    Element instgAgt = (Element) instgAgtList.item(0);
                    parseFinancialInstitution(instgAgt, "instructing");
                }
            }
        } catch (Exception e) {
            logger.warn("Error parsing group header section: {}", e.getMessage());
        }
    }

    private void parseOriginalGroupInfoSection(Document doc, Pacs028ResponseDto response) {
        try {
            // Get Original Group Information section
            NodeList originalGroupInfoList = doc.getElementsByTagName("OrgnlGrpInf");
            if (originalGroupInfoList.getLength() > 0) {
                Element originalGroupInfo = (Element) originalGroupInfoList.item(0);
                
                // Parse Original Message ID (from the original PACS.008)
                String originalMessageId = getElementTextContent(originalGroupInfo, "OrgnlMsgId");
                response.setOriginalMessageId(originalMessageId);
                
                // Parse Original Message Name ID
                String originalMessageNameId = getElementTextContent(originalGroupInfo, "OrgnlMsgNmId");
                
                // Parse Original Creation Date Time
                String originalCreationDateTime = getElementTextContent(originalGroupInfo, "OrgnlCreDtTm");
                
                logger.debug("Original Message ID: {}", originalMessageId);
                logger.debug("Original Message Name ID: {}", originalMessageNameId);
                logger.debug("Original Creation Date Time: {}", originalCreationDateTime);
            }
        } catch (Exception e) {
            logger.warn("Error parsing original group info section: {}", e.getMessage());
        }
    }

    private void parseTransactionInfoSection(Document doc, Pacs028ResponseDto response) {
        try {
            // Get Transaction Information section
            NodeList txInfList = doc.getElementsByTagName("TxInf");
            if (txInfList.getLength() > 0) {
                Element txInf = (Element) txInfList.item(0);
                
                // Parse Status Request ID
                String statusRequestId = getElementTextContent(txInf, "StsReqId");
                response.setStatusRequestId(statusRequestId);
                
                // Parse Original Transaction ID
                String originalTransactionId = getElementTextContent(txInf, "OrgnlTxId");
                response.setOriginalTransactionId(originalTransactionId);
                
                // Parse Instructing Agent
                NodeList instgAgtList = txInf.getElementsByTagName("InstgAgt");
                if (instgAgtList.getLength() > 0) {
                    Element instgAgt = (Element) instgAgtList.item(0);
                    parseFinancialInstitution(instgAgt, "tx-instructing");
                }
                
                // Parse Instructed Agent
                NodeList instdAgtList = txInf.getElementsByTagName("InstdAgt");
                if (instdAgtList.getLength() > 0) {
                    Element instdAgt = (Element) instdAgtList.item(0);
                    parseFinancialInstitution(instdAgt, "tx-instructed");
                }
                
                // Parse Original Transaction Reference
                NodeList orgnlTxRefList = txInf.getElementsByTagName("OrgnlTxRef");
                if (orgnlTxRefList.getLength() > 0) {
                    Element orgnlTxRef = (Element) orgnlTxRefList.item(0);
                    String settlementDate = getElementTextContent(orgnlTxRef, "IntrBkSttlmDt");
                    response.setSettlementDate(settlementDate);
                }
            }
        } catch (Exception e) {
            logger.warn("Error parsing transaction info section: {}", e.getMessage());
        }
    }

    private void parseFinancialInstitution(Element parentElement, String type) {
        try {
            NodeList finInstnIdList = parentElement.getElementsByTagName("FinInstnId");
            if (finInstnIdList.getLength() > 0) {
                Element finInstnId = (Element) finInstnIdList.item(0);
                
                // Parse BICFI
                String bicfi = getElementTextContent(finInstnId, "BICFI");
                if (bicfi != null && !bicfi.isEmpty()) {
                    logger.debug("{} BICFI: {}", type, bicfi);
                }
                
                // Parse Clearing System Member ID
                NodeList clrSysMmbIdList = finInstnId.getElementsByTagName("ClrSysMmbId");
                if (clrSysMmbIdList.getLength() > 0) {
                    Element clrSysMmbId = (Element) clrSysMmbIdList.item(0);
                    String memberId = getElementTextContent(clrSysMmbId, "MmbId");
                    if (memberId != null && !memberId.isEmpty()) {
                        logger.debug("{} Member ID: {}", type, memberId);
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Error parsing financial institution for {}: {}", type, e.getMessage());
        }
    }

    private String getElementTextContent(Element parent, String tagName) {
        try {
            NodeList nodeList = parent.getElementsByTagName(tagName);
            if (nodeList.getLength() > 0) {
                Element element = (Element) nodeList.item(0);
                return element.getTextContent().trim();
            }
        } catch (Exception e) {
            logger.debug("Error getting text content for tag {}: {}", tagName, e.getMessage());
        }
        return null;
    }

    private void setDefaultValues(Pacs028ResponseDto response) {
        // Set response code and message for status request
        response.setResponseCode("00");
        response.setResponseMessage("Payment status request received and processed successfully");
        response.setStatus("PENDING"); // Status request is pending until response is sent
        
        // Set timestamps
        response.setProcessedAt(java.time.LocalDateTime.now());
        response.setCreatedAt(java.time.LocalDateTime.now());
        
        logger.info("PACS.028 status request parsed - Message ID: {}, Original Message ID: {}", 
            response.getMessageId(), response.getOriginalMessageId());
    }

    private Document parseXmlDocument(String xmlContent) throws Exception {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new ByteArrayInputStream(xmlContent.getBytes("UTF-8")));
        } catch (Exception e) {
            logger.error("Error parsing XML document: {}", e.getMessage(), e);
            throw new Exception("Failed to parse XML document: " + e.getMessage(), e);
        }
    }
}
