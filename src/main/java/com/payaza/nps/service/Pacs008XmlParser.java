package com.payaza.nps.service;

import com.payaza.nps.dto.Pacs008ResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;

/**
 * Specialized XML Parser for PACS.008 Payment Request
 * 
 * Parses the decrypted PACS.008 XML and extracts all relevant fields
 * according to the NIBSS specification. Handles complete payment information
 * including agents, parties, payment types, instructions, and supplementary data.
 */
@Service
public class Pacs008XmlParser {

    private static final Logger logger = LoggerFactory.getLogger(Pacs008XmlParser.class);

    /**
     * Parse PACS.008 XML and extract all relevant information
     */
    public Pacs008ResponseDto parsePacs008Xml(String xmlContent) throws Exception {
        logger.info("Parsing PACS.008 XML content");
        
        try {
            Document doc = parseXmlDocument(xmlContent);
            
            Pacs008ResponseDto response = new Pacs008ResponseDto();
            
            // Parse Group Header section
            parseGroupHeaderSection(doc, response);
            
            // Parse Credit Transfer Transaction Information section
            parseCreditTransferTransactionInfoSection(doc, response);
            
            // Parse Supplementary Data section
            parseSupplementaryDataSection(doc, response);
            
            // Set default values
            setDefaultValues(response);
            
            logger.info("Successfully parsed PACS.008 XML");
            return response;
            
        } catch (Exception e) {
            logger.error("Error parsing PACS.008 XML: {}", e.getMessage(), e);
            throw new Exception("Failed to parse PACS.008 XML: " + e.getMessage(), e);
        }
    }

    private void parseGroupHeaderSection(Document doc, Pacs008ResponseDto response) {
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
                
                // Parse Batch Booking
                String batchBooking = getElementTextContent(groupHeader, "BtchBookg");
                response.setBatchBooking("true".equalsIgnoreCase(batchBooking));
                
                // Parse Number of Transactions
                String numberOfTransactions = getElementTextContent(groupHeader, "NbOfTxs");
                response.setNumberOfTransactions(numberOfTransactions);
                
                // Parse Settlement Information
                NodeList sttlmInfList = groupHeader.getElementsByTagName("SttlmInf");
                if (sttlmInfList.getLength() > 0) {
                    Element sttlmInf = (Element) sttlmInfList.item(0);
                    String settlementMethod = getElementTextContent(sttlmInf, "SttlmMtd");
                    response.setSettlementMethod(settlementMethod);
                }
                
                // Parse Instructing Agent
                NodeList instgAgtList = groupHeader.getElementsByTagName("InstgAgt");
                if (instgAgtList.getLength() > 0) {
                    Element instgAgt = (Element) instgAgtList.item(0);
                    parseGroupHeaderFinancialInstitution(instgAgt, response, "instructing");
                }
                
                // Parse Instructed Agent
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

    private void parseCreditTransferTransactionInfoSection(Document doc, Pacs008ResponseDto response) {
        try {
            // Get Credit Transfer Transaction Information section
            NodeList cdtTrfTxInfList = doc.getElementsByTagName("CdtTrfTxInf");
            if (cdtTrfTxInfList.getLength() > 0) {
                Element cdtTrfTxInf = (Element) cdtTrfTxInfList.item(0);
                
                // Parse Payment ID
                NodeList pmtIdList = cdtTrfTxInf.getElementsByTagName("PmtId");
                if (pmtIdList.getLength() > 0) {
                    Element pmtId = (Element) pmtIdList.item(0);
                    String instructionId = getElementTextContent(pmtId, "InstrId");
                    response.setInstructionId(instructionId);
                    
                    String endToEndId = getElementTextContent(pmtId, "EndToEndId");
                    response.setEndToEndId(endToEndId);
                    
                    String transactionId = getElementTextContent(pmtId, "TxId");
                    response.setTransactionId(transactionId);
                }
                
                // Parse Payment Type Information
                NodeList pmtTpInfList = cdtTrfTxInf.getElementsByTagName("PmtTpInf");
                if (pmtTpInfList.getLength() > 0) {
                    Element pmtTpInf = (Element) pmtTpInfList.item(0);
                    
                    String clearingChannel = getElementTextContent(pmtTpInf, "ClrChanl");
                    response.setClearingChannel(clearingChannel);
                    
                    NodeList svcLvlList = pmtTpInf.getElementsByTagName("SvcLvl");
                    if (svcLvlList.getLength() > 0) {
                        Element svcLvl = (Element) svcLvlList.item(0);
                        String serviceLevel = getElementTextContent(svcLvl, "Prtry");
                        response.setServiceLevel(serviceLevel);
                    }
                    
                    NodeList lclInstrmList = pmtTpInf.getElementsByTagName("LclInstrm");
                    if (lclInstrmList.getLength() > 0) {
                        Element lclInstrm = (Element) lclInstrmList.item(0);
                        String localInstrument = getElementTextContent(lclInstrm, "Prtry");
                        response.setLocalInstrument(localInstrument);
                    }
                    
                    NodeList ctgyPurpList = pmtTpInf.getElementsByTagName("CtgyPurp");
                    if (ctgyPurpList.getLength() > 0) {
                        Element ctgyPurp = (Element) ctgyPurpList.item(0);
                        String categoryPurpose = getElementTextContent(ctgyPurp, "Prtry");
                        response.setCategoryPurpose(categoryPurpose);
                    }
                }
                
                // Parse Interbank Settlement Amount and Date
                NodeList intrBkSttlmAmtList = cdtTrfTxInf.getElementsByTagName("IntrBkSttlmAmt");
                if (intrBkSttlmAmtList.getLength() > 0) {
                    Element intrBkSttlmAmt = (Element) intrBkSttlmAmtList.item(0);
                    String settlementAmount = intrBkSttlmAmt.getTextContent();
                    String currency = intrBkSttlmAmt.getAttribute("Ccy");
                    
                    if (settlementAmount != null && !settlementAmount.isEmpty()) {
                        response.setAmount(new BigDecimal(settlementAmount));
                    }
                    if (currency != null && !currency.isEmpty()) {
                        response.setCurrency(currency);
                    }
                }
                
                String settlementDate = getElementTextContent(cdtTrfTxInf, "IntrBkSttlmDt");
                response.setSettlementDate(settlementDate);
                
                // Parse Charge Bearer
                String chargeBearer = getElementTextContent(cdtTrfTxInf, "ChrgBr");
                response.setChargeBearer(chargeBearer);
                
                // Parse Debtor Information
                NodeList dbtrList = cdtTrfTxInf.getElementsByTagName("Dbtr");
                if (dbtrList.getLength() > 0) {
                    Element dbtr = (Element) dbtrList.item(0);
                    String debtorName = getElementTextContent(dbtr, "Nm");
                    response.setSenderAccountName(debtorName);
                }
                
                // Parse Debtor Account
                NodeList dbtrAcctList = cdtTrfTxInf.getElementsByTagName("DbtrAcct");
                if (dbtrAcctList.getLength() > 0) {
                    Element dbtrAcct = (Element) dbtrAcctList.item(0);
                    NodeList idList = dbtrAcct.getElementsByTagName("Id");
                    if (idList.getLength() > 0) {
                        Element id = (Element) idList.item(0);
                        String debtorAccount = getElementTextContent(id, "IBAN");
                        response.setSenderAccountNumber(debtorAccount);
                    }
                }
                
                // Parse Debtor Agent
                NodeList dbtrAgtList = cdtTrfTxInf.getElementsByTagName("DbtrAgt");
                if (dbtrAgtList.getLength() > 0) {
                    Element dbtrAgt = (Element) dbtrAgtList.item(0);
                    parseTransactionFinancialInstitution(dbtrAgt, response, "debtor");
                }
                
                // Parse Creditor Information
                NodeList cdtrList = cdtTrfTxInf.getElementsByTagName("Cdtr");
                if (cdtrList.getLength() > 0) {
                    Element cdtr = (Element) cdtrList.item(0);
                    String creditorName = getElementTextContent(cdtr, "Nm");
                    response.setReceiverAccountName(creditorName);
                }
                
                // Parse Creditor Account
                NodeList cdtrAcctList = cdtTrfTxInf.getElementsByTagName("CdtrAcct");
                if (cdtrAcctList.getLength() > 0) {
                    Element cdtrAcct = (Element) cdtrAcctList.item(0);
                    NodeList idList = cdtrAcct.getElementsByTagName("Id");
                    if (idList.getLength() > 0) {
                        Element id = (Element) idList.item(0);
                        String creditorAccount = getElementTextContent(id, "IBAN");
                        response.setReceiverAccountNumber(creditorAccount);
                    }
                }
                
                // Parse Creditor Agent
                NodeList cdtrAgtList = cdtTrfTxInf.getElementsByTagName("CdtrAgt");
                if (cdtrAgtList.getLength() > 0) {
                    Element cdtrAgt = (Element) cdtrAgtList.item(0);
                    parseTransactionFinancialInstitution(cdtrAgt, response, "creditor");
                }
                
                // Parse Instructions for Next Agent
                NodeList instrForNxtAgtList = cdtTrfTxInf.getElementsByTagName("InstrForNxtAgt");
                if (instrForNxtAgtList.getLength() > 0) {
                    StringBuilder instructions = new StringBuilder();
                    for (int i = 0; i < instrForNxtAgtList.getLength(); i++) {
                        Element instrForNxtAgt = (Element) instrForNxtAgtList.item(i);
                        String instruction = getElementTextContent(instrForNxtAgt, "InstrInf");
                        if (instruction != null && !instruction.isEmpty()) {
                            if (instructions.length() > 0) {
                                instructions.append("; ");
                            }
                            instructions.append(instruction);
                        }
                    }
                    response.setInstructionsForNextAgent(instructions.toString());
                }
                
                // Parse Remittance Information
                NodeList rmtInfList = cdtTrfTxInf.getElementsByTagName("RmtInf");
                if (rmtInfList.getLength() > 0) {
                    Element rmtInf = (Element) rmtInfList.item(0);
                    String remittanceInfo = getElementTextContent(rmtInf, "Ustrd");
                    response.setRemittanceInformation(remittanceInfo);
                }
            }
        } catch (Exception e) {
            logger.warn("Error parsing credit transfer transaction info section: {}", e.getMessage());
        }
    }

    private void parseSupplementaryDataSection(Document doc, Pacs008ResponseDto response) {
        try {
            // Get Supplementary Data section
            NodeList supplementaryDataList = doc.getElementsByTagName("SplmtryData");
            if (supplementaryDataList.getLength() > 0) {
                Element supplementaryData = (Element) supplementaryDataList.item(0);
                
                // Parse Custom Data
                NodeList customDataList = supplementaryData.getElementsByTagName("CustomData");
                if (customDataList.getLength() > 0) {
                    Element customData = (Element) customDataList.item(0);
                    
                    // Parse Debtor Info
                    NodeList debtorInfoList = customData.getElementsByTagName("DebtorInfo");
                    if (debtorInfoList.getLength() > 0) {
                        Element debtorInfo = (Element) debtorInfoList.item(0);
                        
                        String debtorAccountDesignation = getElementTextContent(debtorInfo, "AccountDesignation");
                        response.setDebtorAccountDesignation(debtorAccountDesignation);
                        
                        String debtorAccountTier = getElementTextContent(debtorInfo, "AccountTier");
                        response.setDebtorAccountTier(debtorAccountTier);
                        
                        String debtorBvn = getElementTextContent(debtorInfo, "IdValue");
                        response.setDebtorBvn(debtorBvn);
                        logger.debug("Debtor BVN found: {}", debtorBvn);
                    }
                    
                    // Parse Creditor Info
                    NodeList creditorInfoList = customData.getElementsByTagName("CreditorInfo");
                    if (creditorInfoList.getLength() > 0) {
                        Element creditorInfo = (Element) creditorInfoList.item(0);
                        
                        String creditorAccountDesignation = getElementTextContent(creditorInfo, "AccountDesignation");
                        response.setCreditorAccountDesignation(creditorAccountDesignation);
                        
                        String creditorAccountTier = getElementTextContent(creditorInfo, "AccountTier");
                        response.setCreditorAccountTier(creditorAccountTier);
                        
                        String creditorBvn = getElementTextContent(creditorInfo, "IdValue");
                        response.setCreditorBvn(creditorBvn);
                        logger.debug("Creditor BVN found: {}", creditorBvn);
                    }
                    
                    // Parse Transaction Info
                    NodeList transactionInfoList = customData.getElementsByTagName("TransactionInfo");
                    if (transactionInfoList.getLength() > 0) {
                        Element transactionInfo = (Element) transactionInfoList.item(0);
                        
                        String transactionLocation = getElementTextContent(transactionInfo, "TransactionLocation");
                        response.setTransactionLocation(transactionLocation);
                        
                        String nameEnquiryMsgId = getElementTextContent(transactionInfo, "NameEnquiryMsgId");
                        response.setNameEnquiryMsgId(nameEnquiryMsgId);
                        
                        String channelCode = getElementTextContent(transactionInfo, "ChannelCode");
                        response.setChannelCode(channelCode);
                        
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

    private void parseGroupHeaderFinancialInstitution(Element agentElement, Pacs008ResponseDto response, String agentType) {
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
                    logger.debug("Group Header Instructing Agent - BICFI: {}, Member ID: {}", bicfi, memberId);
                } else if ("instructed".equals(agentType)) {
                    response.setInstdAgentBicfi(bicfi);
                    response.setInstdAgentMemberId(memberId);
                    logger.debug("Group Header Instructed Agent - BICFI: {}, Member ID: {}", bicfi, memberId);
                }
            }
        } catch (Exception e) {
            logger.warn("Error parsing financial institution for {} agent: {}", agentType, e.getMessage());
        }
    }

    private void parseTransactionFinancialInstitution(Element agentElement, Pacs008ResponseDto response, String agentType) {
        try {
            NodeList finInstnIdList = agentElement.getElementsByTagName("FinInstnId");
            if (finInstnIdList.getLength() > 0) {
                Element finInstnId = (Element) finInstnIdList.item(0);
                
                // Parse Member ID
                NodeList clrSysMmbIdList = finInstnId.getElementsByTagName("ClrSysMmbId");
                String memberId = "";
                if (clrSysMmbIdList.getLength() > 0) {
                    Element clrSysMmbId = (Element) clrSysMmbIdList.item(0);
                    memberId = getElementTextContent(clrSysMmbId, "MmbId");
                }
                
                // Set values based on agent type
                if ("debtor".equals(agentType)) {
                    response.setDbtrAgentMemberId(memberId);
                    logger.debug("Debtor Agent Member ID: {}", memberId);
                } else if ("creditor".equals(agentType)) {
                    response.setCdtrAgentMemberId(memberId);
                    logger.debug("Creditor Agent Member ID: {}", memberId);
                }
            }
        } catch (Exception e) {
            logger.warn("Error parsing financial institution for {} agent: {}", agentType, e.getMessage());
        }
    }

    private void setDefaultValues(Pacs008ResponseDto response) {
        // Set default status for payment request
        response.setStatus("PENDING");
        response.setResponseCode("00");
        response.setResponseMessage("Payment request received");
        
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

    private String getElementTextContent(Element element, String tagName) {
        try {
            NodeList nodeList = element.getElementsByTagName(tagName);
            if (nodeList.getLength() > 0) {
                return nodeList.item(0).getTextContent();
            }
        } catch (Exception e) {
            logger.warn("Error getting element text content for tag {}: {}", tagName, e.getMessage());
        }
        return "";
    }
}
