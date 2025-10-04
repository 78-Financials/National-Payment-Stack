package com.payaza.nps.service;

import org.apache.xml.security.Init;
import org.apache.xml.security.encryption.EncryptedData;
import org.apache.xml.security.encryption.EncryptedKey;
import org.apache.xml.security.encryption.XMLCipher;
import org.apache.xml.security.keys.KeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.security.*;
import java.util.ArrayList;
import java.util.List;

/**
 * NPS XML Encryption Service
 * 
 * Implements the XML Encryption process as specified by NIBSS:
 * 1. Select XML elements to encrypt
 * 2. Initialize XML encryption engine (AES-256-GCM for payload encryption)
 * 3. Generate AES-256 session key
 * 4. Encrypt session key using institution's RSA public key (RSA-OAEP)
 * 5. Encrypt the payload using the AES-256 session key
 * 6. Replace sensitive elements with <EncryptedData> in the XML
 * 
 * Based on the official NIBSS encryption implementation for Java using Apache Santuario + BouncyCastle
 */
@Service
public class NpsXmlEncryptionService {

    private static final Logger logger = LoggerFactory.getLogger(NpsXmlEncryptionService.class);

    static {
        try {
            // Initialize BouncyCastle provider
            Security.addProvider(new BouncyCastleProvider());
            
            // Initialize Apache XML Security
            Init.init();
            
            logger.info("NPS XML Encryption Service initialized with BouncyCastle and Apache XML Security");
        } catch (Exception e) {
            logger.error("Failed to initialize NPS XML Encryption Service", e);
            throw new RuntimeException("Failed to initialize XML encryption service", e);
        }
    }

    /**
     * Encrypt entire XML document using NIBSS XML encryption specification
     * This encrypts the entire Document element and replaces it with xenc:EncryptedData block
     * 
     * @param xmlContent The XML content to encrypt
     * @param publicKey The RSA public key for session key encryption
     * @return Encrypted XML content with xenc:EncryptedData block replacing the Document element
     */
    public String encryptXmlDocument(String xmlContent, PublicKey publicKey) throws Exception {
        logger.info("Encrypting entire XML document using NIBSS XML encryption specification");
        
        try {
            // Parse XML document
            Document doc = parseXmlDocument(xmlContent);
            
            // Find the Document element to encrypt (handle namespaced elements)
            NodeList documentNodes = doc.getElementsByTagName("Document");
            if (documentNodes.getLength() == 0) {
                // Try with namespace prefix
                documentNodes = doc.getElementsByTagNameNS("*", "Document");
                if (documentNodes.getLength() == 0) {
                    throw new Exception("Document element not found in XML");
                }
            }
            
            Element documentElement = (Element) documentNodes.item(0);
            
            // Debug: Log original XML structure
            logger.debug("Original XML has Document element: " + (documentElement != null));
            
            // Encrypt the entire Document element
            encryptDocumentElement(doc, publicKey, documentElement);
            
            // Debug: Check if EncryptedData was created in the final document
            NodeList finalEncryptedDataNodes = doc.getElementsByTagNameNS("http://www.w3.org/2001/04/xmlenc#", "EncryptedData");
            logger.debug("Final document has " + finalEncryptedDataNodes.getLength() + " EncryptedData elements");
            
            // Convert encrypted document to string
            String encryptedXml = documentToString(doc);
            
            // Debug: Log first 500 characters of encrypted XML
            logger.debug("Encrypted XML preview: " + encryptedXml.substring(0, Math.min(500, encryptedXml.length())));
            
            logger.info("Successfully encrypted entire XML document");
            return encryptedXml;
            
        } catch (Exception e) {
            logger.error("Failed to encrypt XML document", e);
            throw new Exception("XML encryption failed: " + e.getMessage(), e);
        }
    }

    /**
     * Encrypt XML document using NIBSS XML encryption specification (element-level encryption)
     * 
     * @param xmlContent The XML content to encrypt
     * @param publicKey The RSA public key for session key encryption
     * @param elementsToEncrypt Array of element names to encrypt (e.g., ["SensitiveData", "PmtId"])
     * @return Encrypted XML content with <EncryptedData> elements
     */
    public String encryptXmlDocument(String xmlContent, PublicKey publicKey, String[] elementsToEncrypt) throws Exception {
        logger.info("Encrypting XML document elements using NIBSS XML encryption specification");
        
        try {
            // Parse XML document
            Document doc = parseXmlDocument(xmlContent);
            
            // Encrypt specified elements
            for (String elementName : elementsToEncrypt) {
                encryptElement(doc, publicKey, elementName);
            }
            
            // Convert encrypted document to string
            String encryptedXml = documentToString(doc);
            
            logger.info("Successfully encrypted XML document with " + elementsToEncrypt.length + " elements");
            return encryptedXml;
            
        } catch (Exception e) {
            logger.error("Failed to encrypt XML document", e);
            throw new Exception("XML encryption failed: " + e.getMessage(), e);
        }
    }

    /**
     * Encrypt Document element using NIBSS specification
     * This encrypts the content inside FIToFICstmrCdtTrf element with xenc:EncryptedData block
     * 
     * @param doc The XML document
     * @param publicKey The RSA public key for session key encryption
     * @param documentElement The Document element to encrypt
     */
    private void encryptDocumentElement(Document doc, PublicKey publicKey, Element documentElement) throws Exception {
        logger.debug("Encrypting Document element using NIBSS specification");
        
        // Determine message type and find the appropriate element to encrypt
        Element elementToEncrypt = null;
        String messageType = "unknown";
        
        // Check for FIToFICstmrCdtTrf (pacs.008 payment messages)
        NodeList fitToFiElements = documentElement.getElementsByTagName("FIToFICstmrCdtTrf");
        if (fitToFiElements.getLength() == 0) {
            // Try with namespace prefix
            fitToFiElements = documentElement.getElementsByTagNameNS("*", "FIToFICstmrCdtTrf");
        }
        if (fitToFiElements.getLength() > 0) {
            elementToEncrypt = (Element) fitToFiElements.item(0);
            messageType = "pacs.008";
            logger.debug("Detected pacs.008 payment message, encrypting FIToFICstmrCdtTrf element");
        } else {
            // Check for IdVrfctnReq (acmt.023 identification verification messages)
            NodeList idVrfctnReqElements = documentElement.getElementsByTagName("IdVrfctnReq");
            if (idVrfctnReqElements.getLength() == 0) {
                // Try with namespace prefix
                idVrfctnReqElements = documentElement.getElementsByTagNameNS("*", "IdVrfctnReq");
            }
            if (idVrfctnReqElements.getLength() > 0) {
                elementToEncrypt = (Element) idVrfctnReqElements.item(0);
                messageType = "acmt.023";
                logger.debug("Detected acmt.023 identification verification message, encrypting IdVrfctnReq element");
            } else {
        // Check for IdVrfctnRpt (acmt.024 identification verification report messages)
        NodeList idVrfctnRptElements = documentElement.getElementsByTagName("IdVrfctnRpt");
        if (idVrfctnRptElements.getLength() == 0) {
            // Try with namespace prefix
            idVrfctnRptElements = documentElement.getElementsByTagNameNS("*", "IdVrfctnRpt");
        }
        if (idVrfctnRptElements.getLength() > 0) {
            elementToEncrypt = (Element) idVrfctnRptElements.item(0);
            messageType = "acmt.024";
            logger.debug("Detected acmt.024 identification verification report message, encrypting IdVrfctnRpt element");
        } else {
            // Check for FIToFIPmtStsRpt (pacs.002 payment status report messages)
            NodeList fitToFiPmtStsRptElements = documentElement.getElementsByTagName("FIToFIPmtStsRpt");
            if (fitToFiPmtStsRptElements.getLength() == 0) {
                // Try with namespace prefix
                fitToFiPmtStsRptElements = documentElement.getElementsByTagNameNS("*", "FIToFIPmtStsRpt");
            }
                    if (fitToFiPmtStsRptElements.getLength() > 0) {
                        elementToEncrypt = (Element) fitToFiPmtStsRptElements.item(0);
                        messageType = "pacs.002";
                        logger.debug("Detected pacs.002 payment status report message, encrypting FIToFIPmtStsRpt element");
                    }
                }
            }
        }
        
        // Check for FIToFIPmtStsReq (pacs.028 payment status request messages)
        if (elementToEncrypt == null) {
            NodeList fitToFiPmtStsReqElements = documentElement.getElementsByTagName("FIToFIPmtStsReq");
            if (fitToFiPmtStsReqElements.getLength() == 0) {
                // Try with namespace prefix
                fitToFiPmtStsReqElements = documentElement.getElementsByTagNameNS("*", "FIToFIPmtStsReq");
            }
            if (fitToFiPmtStsReqElements.getLength() > 0) {
                elementToEncrypt = (Element) fitToFiPmtStsReqElements.item(0);
                messageType = "pacs.028";
                logger.debug("Detected pacs.028 payment status request message, encrypting FIToFIPmtStsReq element");
            }
        }
        
        if (elementToEncrypt == null) {
            throw new Exception("No supported message element found in Document. Expected FIToFICstmrCdtTrf (pacs.008), IdVrfctnReq (acmt.023), IdVrfctnRpt (acmt.024), FIToFIPmtStsRpt (pacs.002), or FIToFIPmtStsReq (pacs.028)");
        }
        
        // Step 1: Generate AES-256 session key
        KeyGenerator keyGen = KeyGenerator.getInstance("AES", "BC");
        keyGen.init(256);
        SecretKey sessionKey = keyGen.generateKey();
        
        // Step 2: Wrap session key with RSA public key (RSA-OAEP-MGF1P)
        XMLCipher keyCipher = XMLCipher.getInstance("http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p");
        keyCipher.init(XMLCipher.WRAP_MODE, publicKey);
        EncryptedKey encryptedKey = keyCipher.encryptKey(doc, sessionKey);
        
        // Step 3: Create XMLCipher for AES-256-CBC content encryption
        XMLCipher xmlCipher = XMLCipher.getInstance("http://www.w3.org/2001/04/xmlenc#aes256-cbc");
        xmlCipher.init(XMLCipher.ENCRYPT_MODE, sessionKey);
        
        // Step 4: Set up the EncryptedData structure
        EncryptedData encryptedData = xmlCipher.getEncryptedData();
        encryptedData.setType("http://www.w3.org/2001/04/xmlenc#Content");
        
        // Set up KeyInfo with the encrypted session key
        KeyInfo keyInfo = new KeyInfo(doc);
        keyInfo.add(encryptedKey);
        encryptedData.setKeyInfo(keyInfo);
        
        // Step 5: Collect the content to encrypt
        String contentToEncrypt = "";
        NodeList childNodes = elementToEncrypt.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                contentToEncrypt += nodeToString(child);
            }
        }
        logger.debug("Content to encrypt length: " + contentToEncrypt.length());
        logger.debug("Content to encrypt preview: " + contentToEncrypt.substring(0, Math.min(200, contentToEncrypt.length())));
        
        // Step 6: Encrypt the content string using AES directly
        javax.crypto.Cipher aesCipher = javax.crypto.Cipher.getInstance("AES/CBC/PKCS5Padding", "BC");
        aesCipher.init(javax.crypto.Cipher.ENCRYPT_MODE, sessionKey);
        byte[] iv = aesCipher.getIV();
        byte[] contentBytes = contentToEncrypt.getBytes("UTF-8");
        logger.debug("Content bytes length: " + contentBytes.length);
        byte[] encryptedBytes = aesCipher.doFinal(contentBytes);
        logger.debug("Encrypted bytes length: " + encryptedBytes.length);
        String encryptedContent = java.util.Base64.getEncoder().encodeToString(encryptedBytes);
        logger.debug("Base64 encoded encrypted content length: " + encryptedContent.length());
        
        // Step 7: Create EncryptedData element manually
        Element encryptedDataElement = doc.createElementNS("http://www.w3.org/2001/04/xmlenc#", "EncryptedData");
        encryptedDataElement.setAttribute("Type", "http://www.w3.org/2001/04/xmlenc#Content");
        
        // Add EncryptionMethod with IV
        Element encryptionMethod = doc.createElementNS("http://www.w3.org/2001/04/xmlenc#", "EncryptionMethod");
        encryptionMethod.setAttribute("Algorithm", "http://www.w3.org/2001/04/xmlenc#aes256-cbc");
        
        // Add IV element
        Element ivElement = doc.createElementNS("http://www.w3.org/2001/04/xmlenc#", "InitializationVector");
        ivElement.setTextContent(java.util.Base64.getEncoder().encodeToString(iv));
        encryptionMethod.appendChild(ivElement);
        
        encryptedDataElement.appendChild(encryptionMethod);
        
        // Add KeyInfo
        Element keyInfoElement = doc.createElementNS("http://www.w3.org/2000/09/xmldsig#", "KeyInfo");
        Element encryptedKeyElement = doc.createElementNS("http://www.w3.org/2001/04/xmlenc#", "EncryptedKey");
        
        // Add key encryption method
        Element keyEncryptionMethod = doc.createElementNS("http://www.w3.org/2001/04/xmlenc#", "EncryptionMethod");
        keyEncryptionMethod.setAttribute("Algorithm", "http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p");
        encryptedKeyElement.appendChild(keyEncryptionMethod);
        
        // Add CipherData for key
        Element keyCipherData = doc.createElementNS("http://www.w3.org/2001/04/xmlenc#", "CipherData");
        Element keyCipherValue = doc.createElementNS("http://www.w3.org/2001/04/xmlenc#", "CipherValue");
        
        // Encrypt the session key manually using RSA-OAEP-MGF1P
        String encryptedKeyData = "";
        try {
            javax.crypto.Cipher rsaCipher = javax.crypto.Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding", "BC");
            rsaCipher.init(javax.crypto.Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedKeyBytes = rsaCipher.doFinal(sessionKey.getEncoded());
            encryptedKeyData = java.util.Base64.getEncoder().encodeToString(encryptedKeyBytes);
            logger.debug("Successfully encrypted session key using RSA-OAEP-MGF1P");
        } catch (Exception e) {
            logger.error("Failed to encrypt session key", e);
            encryptedKeyData = "ENCRYPTION_FAILED";
        }
        
        keyCipherValue.setTextContent(encryptedKeyData);
        keyCipherData.appendChild(keyCipherValue);
        encryptedKeyElement.appendChild(keyCipherData);
        
        keyInfoElement.appendChild(encryptedKeyElement);
        encryptedDataElement.appendChild(keyInfoElement);
        
        // Add CipherData for content
        Element cipherData = doc.createElementNS("http://www.w3.org/2001/04/xmlenc#", "CipherData");
        Element cipherValue = doc.createElementNS("http://www.w3.org/2001/04/xmlenc#", "CipherValue");
        cipherValue.setTextContent(encryptedContent);
        cipherData.appendChild(cipherValue);
        encryptedDataElement.appendChild(cipherData);
        
        // Step 8: Clear original content and add EncryptedData
        while (elementToEncrypt.hasChildNodes()) {
            elementToEncrypt.removeChild(elementToEncrypt.getFirstChild());
        }
        elementToEncrypt.appendChild(encryptedDataElement);
        
        // Debug: Check if EncryptedData was created
        NodeList finalEncryptedDataNodes = doc.getElementsByTagNameNS("http://www.w3.org/2001/04/xmlenc#", "EncryptedData");
        logger.debug("Number of EncryptedData elements created: " + finalEncryptedDataNodes.getLength());
        
        // Debug: Log the encrypted XML structure
        String encryptedXml = documentToString(doc);
        logger.debug("Generated encrypted XML structure:\n" + encryptedXml);

        logger.debug("Successfully encrypted Document element using NIBSS specification");
    }

    /**
     * Helper method to convert a DOM node to string
     */
    private String nodeToString(Node node) {
        try {
            StringWriter sw = new StringWriter();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty("omit-xml-declaration", "yes");
            transformer.transform(new DOMSource(node), new StreamResult(sw));
            return sw.toString();
        } catch (Exception e) {
            logger.error("Failed to convert node to string", e);
            return "";
        }
    }

    /**
     * Encrypt specific XML element using NIBSS specification
     * 
     * @param doc The XML document
     * @param publicKey The RSA public key for session key encryption
     * @param elementName The name of the element to encrypt
     */
    private void encryptElement(Document doc, PublicKey publicKey, String elementName) throws Exception {
        logger.debug("Encrypting XML element: " + elementName);
        
        // Step 1: Generate AES-256 session key
        KeyGenerator keyGen = KeyGenerator.getInstance("AES", "BC");
        keyGen.init(256);
        SecretKey sessionKey = keyGen.generateKey();
        
        // Step 2: Wrap session key with RSA public key (RSA-OAEP)
        XMLCipher keyCipher = XMLCipher.getInstance(XMLCipher.RSA_OAEP);
        keyCipher.init(XMLCipher.WRAP_MODE, publicKey);
        EncryptedKey encryptedKey = keyCipher.encryptKey(doc, sessionKey);
        
        // Step 3: Encrypt sensitive XML data using AES-256-GCM
        XMLCipher xmlCipher = XMLCipher.getInstance("http://www.w3.org/2001/04/xmlenc#aes256-gcm");
        xmlCipher.init(XMLCipher.ENCRYPT_MODE, sessionKey);
        
        // Step 4: Attach encrypted session key info inside <EncryptedData>
        EncryptedData encryptedData = xmlCipher.getEncryptedData();
        KeyInfo keyInfo = new KeyInfo(doc);
        keyInfo.add(encryptedKey);
        encryptedData.setKeyInfo(keyInfo);
        
        // Step 5: Locate the XML element to encrypt
        NodeList elements = doc.getElementsByTagName(elementName);
        if (elements.getLength() == 0) {
            logger.warn("Element '" + elementName + "' not found in XML document");
            return;
        }
        
        Element elementToEncrypt = (Element) elements.item(0);
        
        // Step 6: Perform encryption and replace the element with <EncryptedData>
        xmlCipher.doFinal(doc, elementToEncrypt, true);
        
        logger.debug("Successfully encrypted element: " + elementName);
    }

    /**
     * Decrypt XML document using NIBSS XML encryption specification
     * 
     * @param encryptedXmlContent The encrypted XML content
     * @param privateKey The RSA private key for session key decryption
     * @return Decrypted XML content
     */
    public String decryptXmlDocument(String encryptedXmlContent, PrivateKey privateKey) throws Exception {
        logger.info("Decrypting XML document using NIBSS XML encryption specification");
        
        try {
            // Parse encrypted XML document
            Document doc = parseXmlDocument(encryptedXmlContent);
            
            // Find all EncryptedData elements
            NodeList encryptedDataNodes = doc.getElementsByTagNameNS(
                    "http://www.w3.org/2001/04/xmlenc#", "EncryptedData");
            
            // Decrypt each EncryptedData element
            for (int i = 0; i < encryptedDataNodes.getLength(); i++) {
                Element encryptedDataElement = (Element) encryptedDataNodes.item(i);
                decryptElement(doc, privateKey, encryptedDataElement);
            }
            
            // Convert decrypted document to string
            String decryptedXml = documentToString(doc);
            
            logger.info("Successfully decrypted XML document with " + encryptedDataNodes.getLength() + " encrypted elements");
            return decryptedXml;
            
        } catch (Exception e) {
            logger.error("Failed to decrypt XML document", e);
            throw new Exception("XML decryption failed: " + e.getMessage(), e);
        }
    }

    /**
     * Decrypt specific EncryptedData element
     * 
     * @param doc The XML document
     * @param privateKey The RSA private key for session key decryption
     * @param encryptedDataElement The EncryptedData element to decrypt
     */
    private void decryptElement(Document doc, PrivateKey privateKey, Element encryptedDataElement) throws Exception {
        logger.debug("Decrypting EncryptedData element using manual approach");
        
        try {
            // Get the encrypted session key from KeyInfo
            String encryptedSessionKey = getEncryptedSessionKey(encryptedDataElement);
            logger.debug("Encrypted session key length: " + encryptedSessionKey.length());
            
            // Decrypt the session key using RSA private key
            javax.crypto.Cipher rsaCipher = javax.crypto.Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding", "BC");
            rsaCipher.init(javax.crypto.Cipher.DECRYPT_MODE, privateKey);
            byte[] encryptedKeyBytes = java.util.Base64.getDecoder().decode(encryptedSessionKey);
            logger.debug("Encrypted key bytes length: " + encryptedKeyBytes.length);
            byte[] sessionKeyBytes = rsaCipher.doFinal(encryptedKeyBytes);
            logger.debug("Decrypted session key bytes length: " + sessionKeyBytes.length);
            javax.crypto.SecretKey sessionKey = new javax.crypto.spec.SecretKeySpec(sessionKeyBytes, "AES");
            
            // Get the encrypted content
            String encryptedContent = getEncryptedContent(encryptedDataElement);
            logger.debug("Encrypted content length: " + encryptedContent.length());
            
            // Get the IV from EncryptionMethod
            byte[] iv = getInitializationVector(encryptedDataElement);
            logger.debug("IV length: " + iv.length);
            
            // Decrypt the content using AES
            javax.crypto.Cipher aesCipher = javax.crypto.Cipher.getInstance("AES/CBC/PKCS5Padding", "BC");
            javax.crypto.spec.IvParameterSpec ivSpec = new javax.crypto.spec.IvParameterSpec(iv);
            aesCipher.init(javax.crypto.Cipher.DECRYPT_MODE, sessionKey, ivSpec);
            byte[] encryptedContentBytes = java.util.Base64.getDecoder().decode(encryptedContent);
            logger.debug("Encrypted content bytes length: " + encryptedContentBytes.length);
            byte[] decryptedBytes = aesCipher.doFinal(encryptedContentBytes);
            logger.debug("Decrypted content bytes length: " + decryptedBytes.length);
            String decryptedContent = new String(decryptedBytes, "UTF-8");
            logger.debug("Decrypted content preview: " + decryptedContent.substring(0, Math.min(100, decryptedContent.length())));
            
            // Parse the decrypted content back to XML and replace the EncryptedData element
            try {
                // Wrap the decrypted content in a temporary root element to make it valid XML
                String wrappedContent = "<root>" + decryptedContent + "</root>";
                Document decryptedDoc = parseXmlDocument(wrappedContent);
                Element rootElement = decryptedDoc.getDocumentElement();
                
                // Replace the EncryptedData element with the decrypted content
                Element parent = (Element) encryptedDataElement.getParentNode();
                parent.removeChild(encryptedDataElement);
                
                // Import all child elements from the decrypted content
                NodeList childNodes = rootElement.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); i++) {
                    Node child = childNodes.item(i);
                    if (child.getNodeType() == Node.ELEMENT_NODE) {
                        Element importedElement = (Element) doc.importNode(child, true);
                        parent.appendChild(importedElement);
                    }
                }
                
            } catch (Exception parseException) {
                logger.error("Failed to parse decrypted content as XML, treating as text content", parseException);
                // If parsing fails, replace with text content
                Element parent = (Element) encryptedDataElement.getParentNode();
                parent.removeChild(encryptedDataElement);
                parent.setTextContent(decryptedContent);
            }
            
            logger.debug("Successfully decrypted EncryptedData element using manual approach");
            
        } catch (Exception e) {
            logger.error("Manual decryption failed, trying XMLCipher fallback", e);
            // Fallback to XMLCipher if manual decryption fails
            XMLCipher xmlCipher = XMLCipher.getInstance();
            xmlCipher.init(XMLCipher.DECRYPT_MODE, null);
            xmlCipher.setKEK(privateKey);
            xmlCipher.doFinal(doc, encryptedDataElement, false);
        }
    }
    
    /**
     * Extract encrypted session key from EncryptedData element
     */
    private String getEncryptedSessionKey(Element encryptedDataElement) throws Exception {
        NodeList keyInfoNodes = encryptedDataElement.getElementsByTagNameNS("http://www.w3.org/2000/09/xmldsig#", "KeyInfo");
        if (keyInfoNodes.getLength() == 0) {
            throw new Exception("KeyInfo element not found");
        }
        
        NodeList encryptedKeyNodes = ((Element) keyInfoNodes.item(0)).getElementsByTagNameNS("http://www.w3.org/2001/04/xmlenc#", "EncryptedKey");
        if (encryptedKeyNodes.getLength() == 0) {
            throw new Exception("EncryptedKey element not found");
        }
        
        NodeList cipherValueNodes = ((Element) encryptedKeyNodes.item(0)).getElementsByTagNameNS("http://www.w3.org/2001/04/xmlenc#", "CipherValue");
        if (cipherValueNodes.getLength() == 0) {
            throw new Exception("CipherValue element not found in EncryptedKey");
        }
        
        return cipherValueNodes.item(0).getTextContent();
    }
    
    /**
     * Extract encrypted content from EncryptedData element
     */
    private String getEncryptedContent(Element encryptedDataElement) throws Exception {
        // Find CipherData elements - there should be two: one for the session key and one for the content
        NodeList cipherDataNodes = encryptedDataElement.getElementsByTagNameNS("http://www.w3.org/2001/04/xmlenc#", "CipherData");
        if (cipherDataNodes.getLength() < 2) {
            throw new Exception("Expected 2 CipherData elements (session key + content), found: " + cipherDataNodes.getLength());
        }
        
        // The content CipherData is the second one (after the session key CipherData)
        Element contentCipherData = (Element) cipherDataNodes.item(1);
        NodeList cipherValueNodes = contentCipherData.getElementsByTagNameNS("http://www.w3.org/2001/04/xmlenc#", "CipherValue");
        if (cipherValueNodes.getLength() == 0) {
            throw new Exception("CipherValue element not found in content CipherData");
        }
        
        return cipherValueNodes.item(0).getTextContent();
    }
    
    /**
     * Extract initialization vector from EncryptionMethod element
     */
    private byte[] getInitializationVector(Element encryptedDataElement) throws Exception {
        NodeList encryptionMethodNodes = encryptedDataElement.getElementsByTagNameNS("http://www.w3.org/2001/04/xmlenc#", "EncryptionMethod");
        if (encryptionMethodNodes.getLength() == 0) {
            throw new Exception("EncryptionMethod element not found");
        }
        
        NodeList ivNodes = ((Element) encryptionMethodNodes.item(0)).getElementsByTagNameNS("http://www.w3.org/2001/04/xmlenc#", "InitializationVector");
        if (ivNodes.getLength() == 0) {
            throw new Exception("InitializationVector element not found");
        }
        
        String ivString = ivNodes.item(0).getTextContent();
        return java.util.Base64.getDecoder().decode(ivString);
    }

    /**
     * Encrypt entire ISO 20022 payment message using NIBSS specification
     * This encrypts the entire Document element and replaces it with xenc:EncryptedData block
     * 
     * @param xmlContent The ISO 20022 XML content
     * @param publicKey The RSA public key for session key encryption
     * @return Encrypted XML content with xenc:EncryptedData block
     */
    public String encryptPaymentData(String xmlContent, PublicKey publicKey) throws Exception {
        logger.info("Encrypting entire ISO 20022 payment message using NIBSS specification");
        
        // Encrypt the entire Document element (NIBSS approach)
        return encryptXmlDocument(xmlContent, publicKey);
    }

    /**
     * Encrypt specific payment data elements in ISO 20022 messages (element-level encryption)
     * This method identifies and encrypts sensitive payment information
     * 
     * @param xmlContent The ISO 20022 XML content
     * @param publicKey The RSA public key for session key encryption
     * @return Encrypted XML content
     */
    public String encryptPaymentDataElements(String xmlContent, PublicKey publicKey) throws Exception {
        logger.info("Encrypting payment data elements in ISO 20022 message");
        
        // Define sensitive payment elements to encrypt
        String[] sensitiveElements = {
            "PmtId",           // Payment identification
            "IntrBkSttlmAmt",  // Interbank settlement amount
            "Dbtr",            // Debtor information
            "Cdtr",            // Creditor information
            "DbtrAcct",        // Debtor account
            "CdtrAcct",        // Creditor account
            "RmtInf"           // Remittance information
        };
        
        return encryptXmlDocument(xmlContent, publicKey, sensitiveElements);
    }

    /**
     * Encrypt entire ISO 20022 identification verification message using NIBSS specification
     * This encrypts the entire Document element and replaces it with xenc:EncryptedData block
     * 
     * @param xmlContent The ISO 20022 XML content
     * @param publicKey The RSA public key for session key encryption
     * @return Encrypted XML content with xenc:EncryptedData block
     */
    public String encryptIdentificationData(String xmlContent, PublicKey publicKey) throws Exception {
        logger.info("Encrypting entire ISO 20022 identification verification message using NIBSS specification");
        
        // Encrypt the entire Document element (NIBSS approach)
        return encryptXmlDocument(xmlContent, publicKey);
    }

    /**
     * Encrypt identification verification data elements (element-level encryption)
     * 
     * @param xmlContent The ISO 20022 XML content
     * @param publicKey The RSA public key for session key encryption
     * @return Encrypted XML content
     */
    public String encryptIdentificationDataElements(String xmlContent, PublicKey publicKey) throws Exception {
        logger.info("Encrypting identification verification data elements in ISO 20022 message");
        
        // Define sensitive identification elements to encrypt
        String[] sensitiveElements = {
            "AcctId",          // Account identification
            "AcctOwnr",        // Account owner
            "IdVrfctn",        // Identification verification
            "Assgnmt"          // Assignment information
        };
        
        return encryptXmlDocument(xmlContent, publicKey, sensitiveElements);
    }

    /**
     * Generate test RSA key pair for encryption validation
     * 
     * @return RSA key pair
     */
    public KeyPair generateTestKeyPair() throws Exception {
        logger.info("Generating test RSA key pair for XML encryption validation");
        
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            
            logger.info("Successfully generated RSA key pair (2048 bits) for encryption");
            return keyPair;
            
        } catch (Exception e) {
            logger.error("Failed to generate RSA key pair for encryption", e);
            throw new Exception("Key pair generation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Extract encrypted data information from XML document
     * 
     * @param encryptedXmlContent The encrypted XML content
     * @return Information about encrypted elements
     */
    public String getEncryptionInfo(String encryptedXmlContent) throws Exception {
        logger.debug("Extracting encryption information from XML document");
        
        try {
            Document doc = parseXmlDocument(encryptedXmlContent);
            
            NodeList encryptedDataNodes = doc.getElementsByTagNameNS(
                    "http://www.w3.org/2001/04/xmlenc#", "EncryptedData");
            
            StringBuilder info = new StringBuilder();
            info.append("Encryption Information:\n");
            info.append("Number of encrypted elements: ").append(encryptedDataNodes.getLength()).append("\n");
            
            for (int i = 0; i < encryptedDataNodes.getLength(); i++) {
                Element encryptedDataElement = (Element) encryptedDataNodes.item(i);
                
                // Extract encryption method
                NodeList encryptionMethodNodes = encryptedDataElement.getElementsByTagNameNS(
                        "http://www.w3.org/2001/04/xmlenc#", "EncryptionMethod");
                if (encryptionMethodNodes.getLength() > 0) {
                    Element encryptionMethodElement = (Element) encryptionMethodNodes.item(0);
                    String algorithm = encryptionMethodElement.getAttribute("Algorithm");
                    info.append("Element ").append(i + 1).append(": ").append(algorithm).append("\n");
                }
                
                // Extract key info
                NodeList keyInfoNodes = encryptedDataElement.getElementsByTagNameNS(
                        "http://www.w3.org/2000/09/xmldsig#", "KeyInfo");
                if (keyInfoNodes.getLength() > 0) {
                    info.append("  - Contains encrypted session key\n");
                }
            }
            
            return info.toString();
            
        } catch (Exception e) {
            logger.error("Failed to extract encryption information", e);
            throw new Exception("Encryption info extraction failed: " + e.getMessage(), e);
        }
    }

    /**
     * Test XML encryption functionality
     */
    public void testXmlEncryption() throws Exception {
        logger.info("Testing XML encryption functionality");
        
        try {
            // Generate test key pair
            KeyPair keyPair = generateTestKeyPair();
            
            // Test XML content
            String testXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <ns2:Document xmlns:ns2="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.12">
                    <FIToFICstmrCdtTrf>
                        <GrpHdr>
                            <MsgId>TEST-MSG-ID-001</MsgId>
                            <CreDtTm>2025-02-25T09:52:22.954Z</CreDtTm>
                            <NbOfTxs>1</NbOfTxs>
                        </GrpHdr>
                        <CdtTrfTxInf>
                            <PmtId>
                                <EndToEndId>E2E-TEST-001</EndToEndId>
                            </PmtId>
                            <IntrBkSttlmAmt Ccy="NGN">100000</IntrBkSttlmAmt>
                            <Dbtr>
                                <Nm>Test Debtor</Nm>
                            </Dbtr>
                            <Cdtr>
                                <Nm>Test Creditor</Nm>
                            </Cdtr>
                        </CdtTrfTxInf>
                    </FIToFICstmrCdtTrf>
                </ns2:Document>
                """;
            
            // Test encryption
            String[] elementsToEncrypt = {"PmtId", "IntrBkSttlmAmt", "Dbtr", "Cdtr"};
            String encryptedXml = encryptXmlDocument(testXml, keyPair.getPublic(), elementsToEncrypt);
            
            // Validate encryption
            assert encryptedXml.contains("<EncryptedData xmlns=\"http://www.w3.org/2001/04/xmlenc#\">");
            assert encryptedXml.contains("<EncryptionMethod Algorithm=\"http://www.w3.org/2009/xmlenc11#aes256-gcm\">");
            assert encryptedXml.contains("<KeyInfo xmlns=\"http://www.w3.org/2000/09/xmldsig#\">");
            logger.info("XML encryption test passed");
            
            // Test decryption
            String decryptedXml = decryptXmlDocument(encryptedXml, keyPair.getPrivate());
            
            // Validate decryption
            assert decryptedXml.contains("E2E-TEST-001");
            assert decryptedXml.contains("100000");
            assert decryptedXml.contains("Test Debtor");
            assert decryptedXml.contains("Test Creditor");
            assert !decryptedXml.contains("<EncryptedData");
            logger.info("XML decryption test passed");
            
            // Test payment data encryption (document-level)
            String encryptedPaymentData = encryptPaymentData(testXml, keyPair.getPublic());
            assert encryptedPaymentData.contains("<EncryptedData xmlns=\"http://www.w3.org/2001/04/xmlenc#\">");
            logger.info("Payment data encryption test passed");
            
            // Test payment data element-level encryption
            String encryptedPaymentDataElements = encryptPaymentDataElements(testXml, keyPair.getPublic());
            assert encryptedPaymentDataElements.contains("<EncryptedData");
            logger.info("Payment data elements encryption test passed");
            
            // Test encryption info extraction
            String encryptionInfo = getEncryptionInfo(encryptedXml);
            assert encryptionInfo.contains("Number of encrypted elements: 4");
            logger.info("Encryption info extraction test passed");
            
            logger.info("✅ All XML encryption tests passed successfully");
            
        } catch (Exception e) {
            logger.error("❌ XML encryption test failed", e);
            throw new Exception("XML encryption test failed: " + e.getMessage(), e);
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
}
