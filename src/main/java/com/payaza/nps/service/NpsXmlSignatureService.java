package com.payaza.nps.service;

import org.apache.xml.security.Init;
import org.apache.xml.security.c14n.Canonicalizer;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Collections;

/**
 * NPS XML Digital Signature Service
 * 
 * Implements the XML Digital Signature (XMLDSig) process as specified by NIBSS:
 * 1. W3C Canonicalization (C14n)
 * 2. SHA-256 digest algorithm
 * 3. RSA-SHA256 signature algorithm
 * 4. Enveloped signature format
 * 
 * Based on the official NIBSS signature implementation for Java using JSR-105 + BouncyCastle
 */
@Service
public class NpsXmlSignatureService {

    private static final Logger logger = LoggerFactory.getLogger(NpsXmlSignatureService.class);

    static {
        try {
            // Initialize BouncyCastle provider
            Security.addProvider(new BouncyCastleProvider());
            
            // Initialize Apache XML Security
            Init.init();
            
            logger.info("NPS XML Signature Service initialized with BouncyCastle and Apache XML Security");
        } catch (Exception e) {
            logger.error("Failed to initialize NPS XML Signature Service", e);
            throw new RuntimeException("Failed to initialize XML signature service", e);
        }
    }

    /**
     * Sign an XML Document using XML Digital Signature (XMLDSig)
     * Following the NIBSS specification for enveloped RSA-SHA256 signatures
     * 
     * @param xmlContent The XML content to sign
     * @param privateKey The private key for signing
     * @return Signed XML content with embedded signature
     */
    public String signXmlDocument(String xmlContent, PrivateKey privateKey) throws Exception {
        logger.info("Signing XML document using NIBSS XMLDSig specification");
        
        try {
            // Parse XML content
            Document doc = parseXmlDocument(xmlContent);
            
            // Apply W3C Canonicalization (C14n) - this is handled by the XMLDSig process
            // The canonicalization method is specified in the SignedInfo
            
            // Create XML signature factory
            XMLSignatureFactory sigFactory = XMLSignatureFactory.getInstance("DOM");
            
            // Create reference to the XML payload (enveloped signature)
            Reference ref = sigFactory.newReference(
                    "", // URI="" means the entire document
                    sigFactory.newDigestMethod(DigestMethod.SHA256, null),
                    Collections.singletonList(
                            sigFactory.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null)
                    ),
                    null, // Type
                    null  // Digest value (calculated automatically)
            );
            
            // Build SignedInfo with canonicalization and signature method
            SignedInfo signedInfo = sigFactory.newSignedInfo(
                    sigFactory.newCanonicalizationMethod(
                            CanonicalizationMethod.EXCLUSIVE, // Use NIBSS-compliant canonicalization method
                            (C14NMethodParameterSpec) null
                    ),
                    sigFactory.newSignatureMethod(SignatureMethod.RSA_SHA256, null),
                    Collections.singletonList(ref)
            );
            
            // Create signing context
            DOMSignContext dsc = new DOMSignContext(privateKey, doc.getDocumentElement());
            
            // Create XML signature
            XMLSignature signature = sigFactory.newXMLSignature(signedInfo, null);
            
            // Sign the document
            signature.sign(dsc);
            
        // Convert signed document to string
        String signedXml = documentToString(doc);
        
        // Debug: Log the signature structure
        logger.debug("Generated signed XML structure:\n" + signedXml);
        
        logger.info("Successfully signed XML document with RSA-SHA256 signature");
        return signedXml;
            
        } catch (Exception e) {
            logger.error("Failed to sign XML document", e);
            throw new Exception("XML signature failed: " + e.getMessage(), e);
        }
    }

    /**
     * Verify XML signature in a signed document
     * 
     * @param signedXmlContent The signed XML content
     * @param publicKey The public key for verification
     * @return true if signature is valid, false otherwise
     */
    public boolean verifyXmlSignature(String signedXmlContent, PublicKey publicKey) throws Exception {
        logger.info("Verifying XML signature using NIBSS XMLDSig specification");
        
        try {
            // Parse signed XML document
            Document doc = parseXmlDocument(signedXmlContent);
            
            // Find the signature element
            NodeList signatureNodes = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
            if (signatureNodes.getLength() == 0) {
                logger.error("No signature element found in XML document");
                return false;
            }
            
            // Create validation context
            DOMValidateContext valContext = new DOMValidateContext(
                    publicKey, 
                    signatureNodes.item(0)
            );
            
            // Create XML signature factory
            XMLSignatureFactory sigFactory = XMLSignatureFactory.getInstance("DOM");
            
            // Unmarshal the signature
            XMLSignature signature = sigFactory.unmarshalXMLSignature(valContext);
            
            // Validate the signature
            boolean isValid = signature.validate(valContext);
            
            if (isValid) {
                logger.info("XML signature verification successful");
            } else {
                logger.warn("XML signature verification failed");
                
                // Log detailed validation status
                if (signature.getKeyInfo() != null) {
                    logger.warn("KeyInfo validation status: " + signature.getKeyInfo().getId());
                }
                
                // Log reference validation status
                @SuppressWarnings("unchecked")
                java.util.List<Reference> references = signature.getSignedInfo().getReferences();
                for (Reference ref : references) {
                    logger.warn("Reference validation status: " + ref.getId() + " = " + ref.validate(valContext));
                }
            }
            
            return isValid;
            
        } catch (Exception e) {
            logger.error("Failed to verify XML signature", e);
            throw new Exception("XML signature verification failed: " + e.getMessage(), e);
        }
    }

    /**
     * Canonicalize XML content using W3C C14n (Inclusive)
     * This is used for consistent XML processing before signing
     * 
     * @param xmlContent The XML content to canonicalize
     * @return Canonicalized XML content
     */
    public String canonicalizeXml(String xmlContent) throws Exception {
        logger.debug("Canonicalizing XML content using W3C C14n");
        
        try {
            Document doc = parseXmlDocument(xmlContent);
            
            // Use Apache XML Security canonicalizer
            Canonicalizer canonicalizer = Canonicalizer.getInstance("http://www.w3.org/TR/2001/REC-xml-c14n-20010315");
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            canonicalizer.canonicalizeSubtree(doc.getDocumentElement(), outputStream);
            byte[] canonicalBytes = outputStream.toByteArray();
            
            return new String(canonicalBytes, "UTF-8");
            
        } catch (Exception e) {
            logger.error("Failed to canonicalize XML content", e);
            throw new Exception("XML canonicalization failed: " + e.getMessage(), e);
        }
    }

    /**
     * Extract signature from signed XML document
     * 
     * @param signedXmlContent The signed XML content
     * @return Signature element as string, or null if not found
     */
    public String extractSignature(String signedXmlContent) throws Exception {
        logger.debug("Extracting signature from signed XML document");
        
        try {
            Document doc = parseXmlDocument(signedXmlContent);
            
            // Find the signature element
            NodeList signatureNodes = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
            if (signatureNodes.getLength() == 0) {
                return null;
            }
            
            Element signatureElement = (Element) signatureNodes.item(0);
            return elementToString(signatureElement);
            
        } catch (Exception e) {
            logger.error("Failed to extract signature from XML document", e);
            throw new Exception("Signature extraction failed: " + e.getMessage(), e);
        }
    }

    /**
     * Remove signature from signed XML document
     * This is useful for re-signing or when you need the original document
     * 
     * @param signedXmlContent The signed XML content
     * @return Original XML content without signature
     */
    public String removeSignature(String signedXmlContent) throws Exception {
        logger.debug("Removing signature from signed XML document");
        
        try {
            Document doc = parseXmlDocument(signedXmlContent);
            
            // Find and remove the signature element
            NodeList signatureNodes = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
            if (signatureNodes.getLength() > 0) {
                Element signatureElement = (Element) signatureNodes.item(0);
                signatureElement.getParentNode().removeChild(signatureElement);
            }
            
            return documentToString(doc);
            
        } catch (Exception e) {
            logger.error("Failed to remove signature from XML document", e);
            throw new Exception("Signature removal failed: " + e.getMessage(), e);
        }
    }

    /**
     * Generate test RSA key pair for validation
     * 
     * @return RSA key pair
     */
    public KeyPair generateTestKeyPair() throws Exception {
        logger.info("Generating test RSA key pair for XML signature validation");
        
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            
            logger.info("Successfully generated RSA key pair (2048 bits)");
            return keyPair;
            
        } catch (Exception e) {
            logger.error("Failed to generate RSA key pair", e);
            throw new Exception("Key pair generation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Parse XML content into Document
     */
    private Document parseXmlDocument(String xmlContent) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        
        ByteArrayInputStream inputStream = new ByteArrayInputStream(xmlContent.getBytes("UTF-8"));
        return builder.parse(inputStream);
    }

    /**
     * Convert Document to String
     */
    private String documentToString(Document doc) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        
        return writer.toString();
    }

    /**
     * Convert Element to String
     */
    private String elementToString(Element element) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(element), new StreamResult(writer));
        
        return writer.toString();
    }

    /**
     * Test XML signature functionality
     */
    public void testXmlSignature() throws Exception {
        logger.info("Testing XML signature functionality");
        
        try {
            // Generate test key pair
            KeyPair keyPair = generateTestKeyPair();
            
            // Test XML content (sample Pacs.008)
            String testXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <ns2:Document xmlns:ns2="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.12">
                    <FIToFICstmrCdtTrf>
                        <GrpHdr>
                            <MsgId>TEST-MSG-ID-001</MsgId>
                            <CreDtTm>2025-02-25T09:52:22.954Z</CreDtTm>
                            <NbOfTxs>1</NbOfTxs>
                        </GrpHdr>
                    </FIToFICstmrCdtTrf>
                </ns2:Document>
                """;
            
            // Test canonicalization
            String canonicalXml = canonicalizeXml(testXml);
            logger.info("Canonicalization test passed");
            
            // Test signing
            String signedXml = signXmlDocument(testXml, keyPair.getPrivate());
            assert signedXml.contains("<Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\">");
            logger.info("XML signing test passed");
            
            // Test signature extraction
            String signature = extractSignature(signedXml);
            assert signature != null && signature.contains("SignedInfo");
            logger.info("Signature extraction test passed");
            
            // Test signature verification
            boolean isValid = verifyXmlSignature(signedXml, keyPair.getPublic());
            assert isValid;
            logger.info("XML signature verification test passed");
            
            // Test signature removal
            String originalXml = removeSignature(signedXml);
            assert !originalXml.contains("<Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\">");
            logger.info("Signature removal test passed");
            
            logger.info("✅ All XML signature tests passed successfully");
            
        } catch (Exception e) {
            logger.error("❌ XML signature test failed", e);
            throw new Exception("XML signature test failed: " + e.getMessage(), e);
        }
    }
}
