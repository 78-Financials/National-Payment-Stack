package com.payaza.nps.service;

import com.payaza.nps.dto.Acmt024ResponseDto;
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
 * Specialized XML Parser for ACMT.024 Identification Verification Report
 * 
 * Parses the decrypted ACMT.024 XML and extracts all relevant fields
 * according to the NIBSS specification.
 */
@Service
public class Acmt024XmlParser {

    private static final Logger logger = LoggerFactory.getLogger(Acmt024XmlParser.class);

    /**
     * Parse ACMT.024 XML and extract all relevant information
     */
    public Acmt024ResponseDto parseAcmt024Xml(String xmlContent) throws Exception {
        logger.info("Parsing ACMT.024 XML content");
        
        try {
            Document doc = parseXmlDocument(xmlContent);
            
            Acmt024ResponseDto response = new Acmt024ResponseDto();
            
            // Parse Assignment section
            parseAssignmentSection(doc, response);
            
            // Parse Original Assignment section
            parseOriginalAssignmentSection(doc, response);
            
            // Parse Report section
            parseReportSection(doc, response);
            
            // Parse Supplementary Data section
            parseSupplementaryDataSection(doc, response);
            
            // Set default values
            setDefaultValues(response);
            
            logger.info("Successfully parsed ACMT.024 XML");
            return response;
            
        } catch (Exception e) {
            logger.error("Error parsing ACMT.024 XML: {}", e.getMessage(), e);
            throw new Exception("Failed to parse ACMT.024 XML: " + e.getMessage(), e);
        }
    }

    private void parseAssignmentSection(Document doc, Acmt024ResponseDto response) {
        try {
            // Get Assignment section
            NodeList assignmentList = doc.getElementsByTagName("Assgnmt");
            if (assignmentList.getLength() > 0) {
                Element assignment = (Element) assignmentList.item(0);
                
                // Parse Message ID
                String messageId = getElementTextContent(assignment, "MsgId");
                response.setMessageId(messageId);
                
                // Parse Creation Date Time
                String creationDateTime = getElementTextContent(assignment, "CreDtTm");
                
                // Parse Assignor (Bank that sent the report)
                NodeList assignorList = assignment.getElementsByTagName("Assgnr");
                if (assignorList.getLength() > 0) {
                    Element assignor = (Element) assignorList.item(0);
                    parseFinancialInstitution(assignor, "assignor");
                }
                
                // Parse Assignee (Bank that receives the report)
                NodeList assigneeList = assignment.getElementsByTagName("Assgne");
                if (assigneeList.getLength() > 0) {
                    Element assignee = (Element) assigneeList.item(0);
                    parseFinancialInstitution(assignee, "assignee");
                    
                    // Parse Party Name
                    String partyName = getElementTextContent(assignee, "Nm");
                    if (partyName != null && !partyName.isEmpty()) {
                        response.setAccountName(partyName);
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Error parsing assignment section: {}", e.getMessage());
        }
    }

    private void parseOriginalAssignmentSection(Document doc, Acmt024ResponseDto response) {
        try {
            // Get Original Assignment section
            NodeList originalAssignmentList = doc.getElementsByTagName("OrgnlAssgnmt");
            if (originalAssignmentList.getLength() > 0) {
                Element originalAssignment = (Element) originalAssignmentList.item(0);
                
                // Parse Original Message ID
                String originalMessageId = getElementTextContent(originalAssignment, "MsgId");
                response.setOriginalMessageId(originalMessageId);
                
                // Parse Original Creation Date Time
                String originalCreationDateTime = getElementTextContent(originalAssignment, "CreDtTm");
            }
        } catch (Exception e) {
            logger.warn("Error parsing original assignment section: {}", e.getMessage());
        }
    }

    private void parseReportSection(Document doc, Acmt024ResponseDto response) {
        try {
            // Get Report section
            NodeList reportList = doc.getElementsByTagName("Rpt");
            if (reportList.getLength() > 0) {
                Element report = (Element) reportList.item(0);
                
                // Parse Original ID
                String originalId = getElementTextContent(report, "OrgnlId");
                
                // Parse Verification Result (this is the key field!)
                String verificationResult = getElementTextContent(report, "Vrfctn");
                if (verificationResult != null) {
                    boolean isVerified = "true".equalsIgnoreCase(verificationResult);
                    response.setVerificationResult(isVerified);
                    response.setAccountVerified(isVerified);
                }
                
                // Parse Original Party and Account ID
                NodeList originalPtyAndAcctIdList = report.getElementsByTagName("OrgnlPtyAndAcctId");
                if (originalPtyAndAcctIdList.getLength() > 0) {
                    Element originalPtyAndAcctId = (Element) originalPtyAndAcctIdList.item(0);
                    parseAccountInfo(originalPtyAndAcctId, response);
                }
                
                // Parse Updated Party and Account ID
                NodeList updatedPtyAndAcctIdList = report.getElementsByTagName("UpdtdPtyAndAcctId");
                if (updatedPtyAndAcctIdList.getLength() > 0) {
                    Element updatedPtyAndAcctId = (Element) updatedPtyAndAcctIdList.item(0);
                    parseAccountInfo(updatedPtyAndAcctId, response);
                }
            }
        } catch (Exception e) {
            logger.warn("Error parsing report section: {}", e.getMessage());
        }
    }

    private void parseSupplementaryDataSection(Document doc, Acmt024ResponseDto response) {
        try {
            // Get Supplementary Data section
            NodeList supplementaryDataList = doc.getElementsByTagName("SplmtryData");
            if (supplementaryDataList.getLength() > 0) {
                Element supplementaryData = (Element) supplementaryDataList.item(0);
                
                // Parse Custom Data
                NodeList customDataList = supplementaryData.getElementsByTagName("CustomData");
                if (customDataList.getLength() > 0) {
                    Element customData = (Element) customDataList.item(0);
                    
                    // Parse Creditor Info
                    NodeList creditorInfoList = customData.getElementsByTagName("CreditorInfo");
                    if (creditorInfoList.getLength() > 0) {
                        Element creditorInfo = (Element) creditorInfoList.item(0);
                        
                        // Parse Account Designation
                        String accountDesignation = getElementTextContent(creditorInfo, "AccountDesignation");
                        response.setAccountDesignation(accountDesignation);
                        
                        // Parse ID Type
                        String idType = getElementTextContent(creditorInfo, "IdType");
                        response.setIdType(idType);
                        
                        // Parse BVN
                        String bvn = getElementTextContent(creditorInfo, "IdValue");
                        response.setBvn(bvn);
                        logger.debug("BVN found: {}", bvn);
                        
                        // Parse Account Tier
                        String accountTier = getElementTextContent(creditorInfo, "AccountTier");
                        response.setAccountTier(accountTier);
                    }
                    
                    // Parse Transaction Info
                    NodeList transactionInfoList = customData.getElementsByTagName("TransactionInfo");
                    if (transactionInfoList.getLength() > 0) {
                        Element transactionInfo = (Element) transactionInfoList.item(0);
                        
                        // Parse Risk Rating
                        String riskRating = getElementTextContent(transactionInfo, "RiskRating");
                        response.setRiskRating(riskRating);
                        logger.debug("Risk Rating found: {}", riskRating);
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Error parsing supplementary data section: {}", e.getMessage());
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

    private void parseAccountInfo(Element parentElement, Acmt024ResponseDto response) {
        try {
            // Parse Party Name
            String partyName = getElementTextContent(parentElement, "Nm");
            if (partyName != null && !partyName.isEmpty()) {
                response.setAccountName(partyName);
            }
            
            // Parse Account ID
            NodeList acctList = parentElement.getElementsByTagName("Acct");
            if (acctList.getLength() > 0) {
                Element acct = (Element) acctList.item(0);
                NodeList idList = acct.getElementsByTagName("Id");
                if (idList.getLength() > 0) {
                    Element id = (Element) idList.item(0);
                    String iban = getElementTextContent(id, "IBAN");
                    if (iban != null && !iban.isEmpty()) {
                        response.setAccountNumber(iban);
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Error parsing account info: {}", e.getMessage());
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

    private void setDefaultValues(Acmt024ResponseDto response) {
        // Set status based on verification result
        if (response.getVerificationResult() != null) {
            if (response.getVerificationResult()) {
                response.setStatus("SUCCESS");
                response.setResponseCode("00");
                response.setResponseMessage("Identification verification successful");
            } else {
                response.setStatus("FAILED");
                response.setResponseCode("99");
                response.setResponseMessage("Identification verification failed");
            }
        } else {
            response.setStatus("UNKNOWN");
            response.setResponseCode("01");
            response.setResponseMessage("Identification verification status unknown");
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
}
