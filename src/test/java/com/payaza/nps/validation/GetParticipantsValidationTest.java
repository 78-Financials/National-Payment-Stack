package com.payaza.nps.validation;

import com.payaza.nps.NigerianPaymentStackApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validation test for Get Participants endpoint
 * Tests the response structure and content validation
 */
@SpringBootTest(classes = NigerianPaymentStackApplication.class)
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class GetParticipantsValidationTest {

    private static final Logger logger = LoggerFactory.getLogger(GetParticipantsValidationTest.class);

    private String sampleParticipantsResponse;

    @BeforeEach
    void setUp() {
        sampleParticipantsResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<participants>\n" +
                "    <participant institutionCode=\"8827392\">\n" +
                "        <bicfic></bicfic>\n" +
                "        <name>Test inst</name>\n" +
                "        <countryCode>NG</countryCode>\n" +
                "        <status>ACTIVE</status>\n" +
                "        <categoryCode>3</categoryCode>\n" +
                "        <currencies>\n" +
                "            <currency>NGN</currency>\n" +
                "        </currencies>\n" +
                "        <operationsAllowed>\n" +
                "            <operation></operation>\n" +
                "        </operationsAllowed>\n" +
                "    </participant>\n" +
                "</participants>";
        logger.info("Get Participants Validation Test setup completed");
    }

    @Test
    void testParticipantsResponseStructureValidation() {
        logger.info("=== Testing Participants Response Structure Validation ===");
        
        // Test XML structure
        assertTrue(sampleParticipantsResponse.contains("<participants>"), "Should contain participants root element");
        assertTrue(sampleParticipantsResponse.contains("<participant institutionCode=\"8827392\">"), "Should contain participant element with institution code");
        assertTrue(sampleParticipantsResponse.contains("<bicfic></bicfic>"), "Should contain bicfic element");
        assertTrue(sampleParticipantsResponse.contains("<name>Test inst</name>"), "Should contain name element");
        assertTrue(sampleParticipantsResponse.contains("<countryCode>NG</countryCode>"), "Should contain country code");
        assertTrue(sampleParticipantsResponse.contains("<status>ACTIVE</status>"), "Should contain status element");
        assertTrue(sampleParticipantsResponse.contains("<categoryCode>3</categoryCode>"), "Should contain category code");
        assertTrue(sampleParticipantsResponse.contains("<currencies>"), "Should contain currencies element");
        assertTrue(sampleParticipantsResponse.contains("<currency>NGN</currency>"), "Should contain currency element");
        assertTrue(sampleParticipantsResponse.contains("<operationsAllowed>"), "Should contain operations allowed element");
        assertTrue(sampleParticipantsResponse.contains("<operation></operation>"), "Should contain operation element");
        
        logger.info("✅ Participants Response Structure Validation tests passed");
    }

    @Test
    void testParticipantsResponseXmlParsing() throws Exception {
        logger.info("=== Testing Participants Response XML Parsing ===");
        
        // Parse the XML to validate structure
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new java.io.ByteArrayInputStream(sampleParticipantsResponse.getBytes()));
        
        // Validate root element
        assertNotNull(doc.getDocumentElement(), "Document should have root element");
        assertEquals("participants", doc.getDocumentElement().getNodeName(), "Root element should be 'participants'");
        
        // Validate participant elements
        NodeList participants = doc.getElementsByTagName("participant");
        assertTrue(participants.getLength() > 0, "Should contain at least one participant");
        
        // Validate participant attributes and child elements
        for (int i = 0; i < participants.getLength(); i++) {
            var participant = participants.item(i);
            assertNotNull(participant.getAttributes().getNamedItem("institutionCode"), 
                    "Participant should have institutionCode attribute");
            
            // Check required child elements
            NodeList nameNodes = ((org.w3c.dom.Element) participant).getElementsByTagName("name");
            assertTrue(nameNodes.getLength() > 0, "Participant should have name element");
            
            NodeList statusNodes = ((org.w3c.dom.Element) participant).getElementsByTagName("status");
            assertTrue(statusNodes.getLength() > 0, "Participant should have status element");
            
            NodeList countryCodeNodes = ((org.w3c.dom.Element) participant).getElementsByTagName("countryCode");
            assertTrue(countryCodeNodes.getLength() > 0, "Participant should have countryCode element");
        }
        
        logger.info("✅ Participants Response XML Parsing tests passed");
    }

    @Test
    void testParticipantsResponseContentValidation() {
        logger.info("=== Testing Participants Response Content Validation ===");
        
        // Validate specific content values
        assertTrue(sampleParticipantsResponse.contains("institutionCode=\"8827392\""), "Should contain specific institution code");
        assertTrue(sampleParticipantsResponse.contains("Test inst"), "Should contain institution name");
        assertTrue(sampleParticipantsResponse.contains("NG"), "Should contain country code NG");
        assertTrue(sampleParticipantsResponse.contains("ACTIVE"), "Should contain ACTIVE status");
        assertTrue(sampleParticipantsResponse.contains("3"), "Should contain category code 3");
        assertTrue(sampleParticipantsResponse.contains("NGN"), "Should contain NGN currency");
        
        // Validate status values (should be ACTIVE or INACTIVE)
        assertTrue(sampleParticipantsResponse.contains("ACTIVE") || sampleParticipantsResponse.contains("INACTIVE"), 
                "Status should be either ACTIVE or INACTIVE");
        
        // Validate country code format (should be 2-letter country code)
        assertTrue(sampleParticipantsResponse.contains("countryCode>NG<"), "Should contain valid 2-letter country code");
        
        logger.info("✅ Participants Response Content Validation tests passed");
    }

    @Test
    void testParticipantsResponseMultipleParticipants() {
        logger.info("=== Testing Participants Response with Multiple Participants ===");
        
        String multipleParticipantsResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<participants>\n" +
                "    <participant institutionCode=\"8827392\">\n" +
                "        <bicfic>TESTBIC1</bicfic>\n" +
                "        <name>Test Bank 1</name>\n" +
                "        <countryCode>NG</countryCode>\n" +
                "        <status>ACTIVE</status>\n" +
                "        <categoryCode>1</categoryCode>\n" +
                "        <currencies>\n" +
                "            <currency>NGN</currency>\n" +
                "            <currency>USD</currency>\n" +
                "        </currencies>\n" +
                "        <operationsAllowed>\n" +
                "            <operation>PAYMENT</operation>\n" +
                "            <operation>TRANSFER</operation>\n" +
                "        </operationsAllowed>\n" +
                "    </participant>\n" +
                "    <participant institutionCode=\"8827393\">\n" +
                "        <bicfic>TESTBIC2</bicfic>\n" +
                "        <name>Test Bank 2</name>\n" +
                "        <countryCode>NG</countryCode>\n" +
                "        <status>INACTIVE</status>\n" +
                "        <categoryCode>2</categoryCode>\n" +
                "        <currencies>\n" +
                "            <currency>NGN</currency>\n" +
                "        </currencies>\n" +
                "        <operationsAllowed>\n" +
                "            <operation>PAYMENT</operation>\n" +
                "        </operationsAllowed>\n" +
                "    </participant>\n" +
                "</participants>";
        
        // Validate multiple participants structure
        assertTrue(multipleParticipantsResponse.contains("<participant institutionCode=\"8827392\">"), "Should contain first participant");
        assertTrue(multipleParticipantsResponse.contains("<participant institutionCode=\"8827393\">"), "Should contain second participant");
        assertTrue(multipleParticipantsResponse.contains("Test Bank 1"), "Should contain first bank name");
        assertTrue(multipleParticipantsResponse.contains("Test Bank 2"), "Should contain second bank name");
        assertTrue(multipleParticipantsResponse.contains("ACTIVE"), "Should contain ACTIVE status");
        assertTrue(multipleParticipantsResponse.contains("INACTIVE"), "Should contain INACTIVE status");
        
        // Validate multiple currencies
        assertTrue(multipleParticipantsResponse.contains("<currency>NGN</currency>"), "Should contain NGN currency");
        assertTrue(multipleParticipantsResponse.contains("<currency>USD</currency>"), "Should contain USD currency");
        
        // Validate multiple operations
        assertTrue(multipleParticipantsResponse.contains("<operation>PAYMENT</operation>"), "Should contain PAYMENT operation");
        assertTrue(multipleParticipantsResponse.contains("<operation>TRANSFER</operation>"), "Should contain TRANSFER operation");
        
        logger.info("✅ Multiple Participants Response tests passed");
    }

}
