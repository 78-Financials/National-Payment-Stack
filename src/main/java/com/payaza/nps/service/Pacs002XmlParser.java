package com.payaza.nps.service;

import com.payaza.nps.dto.Pacs002ResponseDto;
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
 * Specialized XML Parser for PACS.002 Payment Status Report
 * 
 * Parses the decrypted PACS.002 XML and extracts all relevant fields
 * according to the NIBSS specification. Handles all payment status scenarios:
 * - Case 1: Payment approved by creditor bank (ACSC)
 * - Case 2: Payment declined by creditor bank (RJCT)
 * - Case 3: Timeout Payment - late response received (RJCT)
 * - Case 4: Timeout Payment - response not received (RJCT)
 */
@Service
public class Pacs002XmlParser {

    private static final Logger logger = LoggerFactory.getLogger(Pacs002XmlParser.class);

    /**
     * Parse PACS.002 XML and extract all relevant information
     */
    public Pacs002ResponseDto parsePacs002Xml(String xmlContent) throws Exception {
        logger.info("Parsing PACS.002 XML content");
        
        try {
            Document doc = parseXmlDocument(xmlContent);
            
            Pacs002ResponseDto response = new Pacs002ResponseDto();
            
            // Parse Group Header section
            parseGroupHeaderSection(doc, response);
            
            // Parse Original Group Information and Status section
            parseOriginalGroupInfoAndStatusSection(doc, response);
            
            // Parse Transaction Information and Status section
            parseTransactionInfoAndStatusSection(doc, response);
            
            // Set default values and determine payment status
            setDefaultValues(response);
            
            logger.info("Successfully parsed PACS.002 XML");
            return response;
            
        } catch (Exception e) {
            logger.error("Error parsing PACS.002 XML: {}", e.getMessage(), e);
            throw new Exception("Failed to parse PACS.002 XML: " + e.getMessage(), e);
        }
    }

    private void parseGroupHeaderSection(Document doc, Pacs002ResponseDto response) {
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
                response.setCreationDateTime(creationDateTime);
                
                // Parse Instructing Agent (Bank that sent the report)
                NodeList instgAgtList = groupHeader.getElementsByTagName("InstgAgt");
                if (instgAgtList.getLength() > 0) {
                    Element instgAgt = (Element) instgAgtList.item(0);
                    parseGroupHeaderFinancialInstitution(instgAgt, response, "instructing");
                }
                
                // Parse Instructed Agent (Bank that receives the report)
                NodeList instdAgtList = groupHeader.getElementsByTagName("InstdAgt");
                if (instdAgtList.getLength() > 0) {
                    Element instdAgt = (Element) instdAgtList.item(0);
                    parseGroupHeaderFinancialInstitution(instdAgt, response, "instructed");
                }
            }
        } catch (Exception e) {
            logger.warn("Error parsing group header section: {}", e.getMessage());
        }
    }

    private void parseOriginalGroupInfoAndStatusSection(Document doc, Pacs002ResponseDto response) {
        try {
            // Get Original Group Information and Status section
            NodeList originalGroupInfoList = doc.getElementsByTagName("OrgnlGrpInfAndSts");
            if (originalGroupInfoList.getLength() > 0) {
                Element originalGroupInfo = (Element) originalGroupInfoList.item(0);
                
                // Parse Original Message ID (from the original PACS.008)
                String originalMessageId = getElementTextContent(originalGroupInfo, "OrgnlMsgId");
                response.setOriginalMessageId(originalMessageId);
                
                // Parse Original Message Name ID
                String originalMessageNameId = getElementTextContent(originalGroupInfo, "OrgnlMsgNmId");
                response.setOriginalMessageNameId(originalMessageNameId);
                
                // Parse Original Creation Date Time
                String originalCreationDateTime = getElementTextContent(originalGroupInfo, "OrgnlCreDtTm");
                response.setOriginalCreationDateTime(originalCreationDateTime);
                
                // Parse Group Status (this is the key field!)
                String groupStatus = getElementTextContent(originalGroupInfo, "GrpSts");
                response.setStatus(groupStatus);
                
                logger.debug("Group Status: {}", groupStatus);
            }
        } catch (Exception e) {
            logger.warn("Error parsing original group info and status section: {}", e.getMessage());
        }
    }

    private void parseTransactionInfoAndStatusSection(Document doc, Pacs002ResponseDto response) {
        try {
            // Get Transaction Information and Status section
            NodeList txInfAndStsList = doc.getElementsByTagName("TxInfAndSts");
            if (txInfAndStsList.getLength() > 0) {
                Element txInfAndSts = (Element) txInfAndStsList.item(0);
                
                // Parse Status ID
                String statusId = getElementTextContent(txInfAndSts, "StsId");
                response.setStatusId(statusId);
                
                // Parse Status Reason Information
                NodeList stsRsnInfList = txInfAndSts.getElementsByTagName("StsRsnInf");
                if (stsRsnInfList.getLength() > 0) {
                    Element stsRsnInf = (Element) stsRsnInfList.item(0);
                    
                    // Parse Reason
                    NodeList reasonList = stsRsnInf.getElementsByTagName("Rsn");
                    if (reasonList.getLength() > 0) {
                        Element reason = (Element) reasonList.item(0);
                        String reasonCode = getElementTextContent(reason, "Prtry");
                        response.setReasonCode(reasonCode);
                    }
                    
                    // Parse Additional Information
                    String additionalInfo = getElementTextContent(stsRsnInf, "AddtlInf");
                    response.setAdditionalInformation(additionalInfo);
                }
                
                // Parse Instructing Agent
                NodeList instgAgtList = txInfAndSts.getElementsByTagName("InstgAgt");
                if (instgAgtList.getLength() > 0) {
                    Element instgAgt = (Element) instgAgtList.item(0);
                    parseFinancialInstitution(instgAgt, "tx-instructing");
                }
                
                // Parse Instructed Agent
                NodeList instdAgtList = txInfAndSts.getElementsByTagName("InstdAgt");
                if (instdAgtList.getLength() > 0) {
                    Element instdAgt = (Element) instdAgtList.item(0);
                    parseFinancialInstitution(instdAgt, "tx-instructed");
                }
                
                // Parse Original Transaction Reference
                NodeList orgnlTxRefList = txInfAndSts.getElementsByTagName("OrgnlTxRef");
                if (orgnlTxRefList.getLength() > 0) {
                    Element orgnlTxRef = (Element) orgnlTxRefList.item(0);
                    String settlementDate = getElementTextContent(orgnlTxRef, "IntrBkSttlmDt");
                    response.setSettlementDate(settlementDate);
                }
            }
        } catch (Exception e) {
            logger.warn("Error parsing transaction info and status section: {}", e.getMessage());
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

    private void setDefaultValues(Pacs002ResponseDto response) {
        // Set response code and message based on group status
        String groupStatus = response.getStatus();
        
        if ("ACSC".equals(groupStatus)) {
            // Case 1: Payment approved by creditor bank
            response.setResponseCode("00");
            response.setResponseMessage("Payment approved and settled");
            response.setPaymentApproved(true);
            
        } else if ("RJCT".equals(groupStatus)) {
            // Cases 2, 3, 4: Payment declined, timeout with late response, or timeout with no response
            response.setResponseCode("99");
            response.setPaymentApproved(false);
            
            // Determine specific reason based on additional information
            String additionalInfo = response.getAdditionalInformation();
            String reasonCode = response.getReasonCode();
            
            if (additionalInfo != null) {
                if (additionalInfo.toLowerCase().contains("timeout")) {
                    if (additionalInfo.toLowerCase().contains("late")) {
                        // Case 3: Timeout Payment - late response received
                        response.setResponseMessage("Payment rejected due to late response from creditor bank");
                    } else {
                        // Case 4: Timeout Payment - response not received
                        response.setResponseMessage("Payment rejected due to no response from creditor bank");
                    }
                } else {
                    // Case 2: Payment declined by creditor bank
                    response.setResponseMessage("Payment declined by creditor bank: " + additionalInfo);
                }
            } else if (reasonCode != null) {
                // Use reason code to determine message
                response.setResponseMessage("Payment declined by creditor bank (Code: " + reasonCode + ")");
            } else {
                response.setResponseMessage("Payment declined by creditor bank");
            }
            
        } else {
            // Unknown status
            response.setResponseCode("01");
            response.setResponseMessage("Payment status unknown: " + groupStatus);
            response.setPaymentApproved(null); // Unknown
        }
        
        // Set timestamps
        response.setProcessedAt(java.time.LocalDateTime.now());
        response.setCreatedAt(java.time.LocalDateTime.now());
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

    private void parseGroupHeaderFinancialInstitution(Element agentElement, Pacs002ResponseDto response, String agentType) {
        try {
            NodeList finInstnIdList = agentElement.getElementsByTagName("FinInstnId");
            if (finInstnIdList.getLength() > 0) {
                Element finInstnId = (Element) finInstnIdList.item(0);
                
                // Parse BICFI
                String bicfi = getElementTextContent(finInstnId, "BICFI");
                
                // Parse Member ID
                NodeList clrSysMmbIdList = finInstnId.getElementsByTagName("ClrSysMmbId");
                String memberId = "";
                if (clrSysMmbIdList.getLength() > 0) {
                    Element clrSysMmbId = (Element) clrSysMmbIdList.item(0);
                    memberId = getElementTextContent(clrSysMmbId, "MmbId");
                }
                
                // Set values based on agent type
                if ("instructing".equals(agentType)) {
                    response.setInstgAgentBicfi(bicfi);
                    response.setInstgAgentMemberId(memberId);
                    logger.debug("Instructing Agent - BICFI: {}, Member ID: {}", bicfi, memberId);
                } else if ("instructed".equals(agentType)) {
                    response.setInstdAgentBicfi(bicfi);
                    response.setInstdAgentMemberId(memberId);
                    logger.debug("Instructed Agent - BICFI: {}, Member ID: {}", bicfi, memberId);
                }
            }
        } catch (Exception e) {
            logger.warn("Error parsing financial institution for {} agent: {}", agentType, e.getMessage());
        }
    }
}
