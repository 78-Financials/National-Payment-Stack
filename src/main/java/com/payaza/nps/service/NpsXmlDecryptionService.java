package com.payaza.nps.service;

import org.apache.xml.security.Init;
import org.apache.xml.security.encryption.EncryptedData;
import org.apache.xml.security.encryption.EncryptedKey;
import org.apache.xml.security.encryption.XMLCipher;
import org.apache.xml.security.encryption.XMLEncryptionException;
import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.keys.content.X509Data;
import org.apache.xml.security.keys.keyresolver.KeyResolverSpi;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.signature.XMLSignatureException;
import org.apache.xml.security.utils.Constants;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
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
import java.util.Base64;

/**
 * NPS XML Decryption Service
 * 
 * Handles decryption and signature verification of incoming NIBSS messages:
 * 1. Decrypts AES-256-CBC encrypted content
 * 2. Verifies RSA-SHA256 signatures
 * 3. Supports both AES-256-CBC and AES-256-GCM modes
 * 4. Ignores unknown tags for future compatibility
 */
@Service
public class NpsXmlDecryptionService {

    private static final Logger logger = LoggerFactory.getLogger(NpsXmlDecryptionService.class);

    static {
        Security.addProvider(new BouncyCastleProvider());
        Init.init();
        logger.info("NPS XML Decryption Service initialized with BouncyCastle and Apache XML Security");
    }

    /**
     * Decrypts an encrypted XML document using the specified private key.
     * Supports both AES-256-CBC and AES-256-GCM modes.
     *
     * @param encryptedXml The encrypted XML content.
     * @param privateKey The private key for decryption.
     * @return The decrypted XML content.
     * @throws Exception if an error occurs during decryption.
     */
    public String decryptXmlDocument(String encryptedXml, PrivateKey privateKey) throws Exception {
        logger.info("Decrypting XML document from NIBSS");
        
        try {
            // Parse encrypted XML
            Document doc = parseXmlDocument(encryptedXml);
            
            // Find encrypted data elements
            NodeList encryptedDataList = doc.getElementsByTagNameNS("http://www.w3.org/2001/04/xmlenc#", "EncryptedData");
            
            if (encryptedDataList.getLength() == 0) {
                throw new Exception("No encrypted data found in XML document");
            }
            
            // Decrypt each encrypted data element
            for (int i = 0; i < encryptedDataList.getLength(); i++) {
                Element encryptedDataElement = (Element) encryptedDataList.item(i);
                decryptEncryptedData(doc, encryptedDataElement, privateKey);
            }
            
            // Convert back to string
            return documentToString(doc);
            
        } catch (Exception e) {
            logger.error("Error decrypting XML document: {}", e.getMessage(), e);
            throw new Exception("Failed to decrypt XML document: " + e.getMessage(), e);
        }
    }

    /**
     * Verifies the XML signature of a document using the specified public key.
     *
     * @param signedXml The signed XML content.
     * @param publicKey The public key for verification.
     * @return true if signature is valid, false otherwise.
     * @throws Exception if an error occurs during verification.
     */
    public boolean verifyXmlSignature(String signedXml, PublicKey publicKey) throws Exception {
        logger.info("Verifying XML signature from NIBSS");
        
        try {
            // Parse signed XML
            Document doc = parseXmlDocument(signedXml);
            
            // Find signature elements
            NodeList signatureList = doc.getElementsByTagNameNS("http://www.w3.org/2000/09/xmldsig#", "Signature");
            
            if (signatureList.getLength() == 0) {
                logger.warn("No signature found in XML document");
                return false;
            }
            
            // Verify each signature
            for (int i = 0; i < signatureList.getLength(); i++) {
                Element signatureElement = (Element) signatureList.item(i);
                if (!verifySignature(doc, signatureElement, publicKey)) {
                    logger.error("Signature verification failed for signature {}", i);
                    return false;
                }
            }
            
            logger.info("All signatures verified successfully");
            return true;
            
        } catch (Exception e) {
            logger.error("Error verifying XML signature: {}", e.getMessage(), e);
            throw new Exception("Failed to verify XML signature: " + e.getMessage(), e);
        }
    }

    /**
     * Decrypts and verifies a signed and encrypted XML document.
     *
     * @param signedEncryptedXml The signed and encrypted XML content.
     * @param privateKey The private key for decryption.
     * @param publicKey The public key for signature verification.
     * @return The decrypted and verified XML content.
     * @throws Exception if an error occurs during processing.
     */
    public String decryptAndVerifyXmlDocument(String signedEncryptedXml, PrivateKey privateKey, PublicKey publicKey) throws Exception {
        logger.info("Decrypting and verifying XML document from NIBSS");
        
        try {
            // First verify the signature
            if (!verifyXmlSignature(signedEncryptedXml, publicKey)) {
                throw new Exception("Signature verification failed");
            }
            
            // Then decrypt the content
            String decryptedXml = decryptXmlDocument(signedEncryptedXml, privateKey);
            
            logger.info("Successfully decrypted and verified XML document");
            return decryptedXml;
            
        } catch (Exception e) {
            logger.error("Error decrypting and verifying XML document: {}", e.getMessage(), e);
            throw new Exception("Failed to decrypt and verify XML document: " + e.getMessage(), e);
        }
    }

    private void decryptEncryptedData(Document doc, Element encryptedDataElement, PrivateKey privateKey) throws Exception {
        try {
            // Get encryption method
            String encryptionMethod = getEncryptionMethod(encryptedDataElement);
            logger.debug("Encryption method: {}", encryptionMethod);
            
            // Get encrypted key
            Element encryptedKeyElement = getEncryptedKeyElement(encryptedDataElement);
            if (encryptedKeyElement == null) {
                // Log the XML structure for debugging
                logger.error("No encrypted key found. XML structure:");
                logger.error("EncryptedData element: {}", encryptedDataElement.getNodeName());
                logger.error("Child elements: {}", getChildElementNames(encryptedDataElement));
                throw new Exception("No encrypted key found");
            }
            
            // Decrypt the session key
            byte[] sessionKey = decryptSessionKey(encryptedKeyElement, privateKey);
            
            // Get encrypted content
            Element cipherDataElement = (Element) encryptedDataElement.getElementsByTagNameNS("http://www.w3.org/2001/04/xmlenc#", "CipherData").item(0);
            Element cipherValueElement = (Element) cipherDataElement.getElementsByTagNameNS("http://www.w3.org/2001/04/xmlenc#", "CipherValue").item(0);
            String encryptedContent = cipherValueElement.getTextContent();
            
            // Decrypt the content
            byte[] decryptedContent = decryptContent(encryptedContent, sessionKey, encryptionMethod);
            
            // Replace encrypted data with decrypted content
            Element parentElement = (Element) encryptedDataElement.getParentNode();
            String decryptedText = new String(decryptedContent, "UTF-8");
            
            // Create a new document from the decrypted content
            Document decryptedDoc = parseXmlDocument(decryptedText);
            Element decryptedRoot = decryptedDoc.getDocumentElement();
            
            // Import and replace the encrypted data
            Element importedElement = (Element) doc.importNode(decryptedRoot, true);
            parentElement.replaceChild(importedElement, encryptedDataElement);
            
        } catch (Exception e) {
            logger.error("Error decrypting encrypted data: {}", e.getMessage(), e);
            throw new Exception("Failed to decrypt encrypted data: " + e.getMessage(), e);
        }
    }

    private String getEncryptionMethod(Element encryptedDataElement) {
        Element encryptionMethodElement = (Element) encryptedDataElement.getElementsByTagNameNS("http://www.w3.org/2001/04/xmlenc#", "EncryptionMethod").item(0);
        return encryptionMethodElement.getAttribute("Algorithm");
    }

    private Element getEncryptedKeyElement(Element encryptedDataElement) {
        // Look for KeyInfo elements - they can be in either xmlenc or xmldsig namespace
        NodeList keyInfoList = encryptedDataElement.getElementsByTagNameNS("http://www.w3.org/2000/09/xmldsig#", "KeyInfo");
        if (keyInfoList.getLength() == 0) {
            // Try xmlenc namespace as fallback
            keyInfoList = encryptedDataElement.getElementsByTagNameNS("http://www.w3.org/2001/04/xmlenc#", "KeyInfo");
        }
        
        if (keyInfoList.getLength() == 0) {
            logger.debug("No KeyInfo element found in encrypted data");
            return null;
        }
        
        Element keyInfoElement = (Element) keyInfoList.item(0);
        
        // Look for EncryptedKey elements - they should be in xmlenc namespace
        NodeList encryptedKeyList = keyInfoElement.getElementsByTagNameNS("http://www.w3.org/2001/04/xmlenc#", "EncryptedKey");
        
        if (encryptedKeyList.getLength() == 0) {
            logger.debug("No EncryptedKey element found in KeyInfo");
            return null;
        }
        
        return (Element) encryptedKeyList.item(0);
    }

    private byte[] decryptSessionKey(Element encryptedKeyElement, PrivateKey privateKey) throws Exception {
        try {
            // Get key encryption method
            Element encryptionMethodElement = (Element) encryptedKeyElement.getElementsByTagNameNS("http://www.w3.org/2001/04/xmlenc#", "EncryptionMethod").item(0);
            String keyEncryptionMethod = encryptionMethodElement.getAttribute("Algorithm");
            
            // Get encrypted key data
            Element cipherDataElement = (Element) encryptedKeyElement.getElementsByTagNameNS("http://www.w3.org/2001/04/xmlenc#", "CipherData").item(0);
            Element cipherValueElement = (Element) cipherDataElement.getElementsByTagNameNS("http://www.w3.org/2001/04/xmlenc#", "CipherValue").item(0);
            String encryptedKeyData = cipherValueElement.getTextContent();
            
            // Decode and decrypt
            byte[] encryptedKeyBytes = Base64.getDecoder().decode(encryptedKeyData);
            
            if (keyEncryptionMethod.equals("http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p")) {
                // RSA-OAEP-MGF1P decryption
                Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding", "BC");
                cipher.init(Cipher.DECRYPT_MODE, privateKey);
                return cipher.doFinal(encryptedKeyBytes);
            } else {
                throw new Exception("Unsupported key encryption method: " + keyEncryptionMethod);
            }
            
        } catch (Exception e) {
            logger.error("Error decrypting session key: {}", e.getMessage(), e);
            throw new Exception("Failed to decrypt session key: " + e.getMessage(), e);
        }
    }

    private byte[] decryptContent(String encryptedContent, byte[] sessionKey, String encryptionMethod) throws Exception {
        try {
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedContent);
            
            if (encryptionMethod.equals("http://www.w3.org/2001/04/xmlenc#aes256-cbc")) {
                // AES-256-CBC decryption
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding", "BC");
                SecretKeySpec keySpec = new SecretKeySpec(sessionKey, "AES");
                
                // Extract IV from first 16 bytes
                byte[] iv = new byte[16];
                System.arraycopy(encryptedBytes, 0, iv, 0, 16);
                IvParameterSpec ivSpec = new IvParameterSpec(iv);
                
                // Decrypt content (skip IV)
                byte[] contentToDecrypt = new byte[encryptedBytes.length - 16];
                System.arraycopy(encryptedBytes, 16, contentToDecrypt, 0, contentToDecrypt.length);
                
                cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
                return cipher.doFinal(contentToDecrypt);
                
            } else if (encryptionMethod.equals("http://www.w3.org/2001/04/xmlenc#aes256-gcm") || 
                       encryptionMethod.equals("http://www.w3.org/2009/xmlenc11#aes256-gcm")) {
                // AES-256-GCM decryption (NIBSS uses xmlenc11 namespace)
                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", "BC");
                SecretKeySpec keySpec = new SecretKeySpec(sessionKey, "AES");
                
                // Extract IV from first 12 bytes
                byte[] iv = new byte[12];
                System.arraycopy(encryptedBytes, 0, iv, 0, 12);
                IvParameterSpec ivSpec = new IvParameterSpec(iv);
                
                // Decrypt content (skip IV)
                byte[] contentToDecrypt = new byte[encryptedBytes.length - 12];
                System.arraycopy(encryptedBytes, 12, contentToDecrypt, 0, contentToDecrypt.length);
                
                cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
                return cipher.doFinal(contentToDecrypt);
                
            } else {
                throw new Exception("Unsupported encryption method: " + encryptionMethod);
            }
            
        } catch (Exception e) {
            logger.error("Error decrypting content: {}", e.getMessage(), e);
            throw new Exception("Failed to decrypt content: " + e.getMessage(), e);
        }
    }

    private boolean verifySignature(Document doc, Element signatureElement, PublicKey publicKey) throws Exception {
        try {
            // Create XML signature factory
            XMLSignatureFactory sigFactory = XMLSignatureFactory.getInstance("DOM");
            
            // Create validate context
            DOMValidateContext validateContext = new DOMValidateContext(publicKey, signatureElement);
            
            // Unmarshal and validate signature
            javax.xml.crypto.dsig.XMLSignature signature = sigFactory.unmarshalXMLSignature(validateContext);
            
            boolean isValid = signature.validate(validateContext);
            logger.debug("Signature validation result: {}", isValid);
            
            return isValid;
            
        } catch (Exception e) {
            logger.error("Error verifying signature: {}", e.getMessage(), e);
            throw new Exception("Failed to verify signature: " + e.getMessage(), e);
        }
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

    private String documentToString(Document doc) throws Exception {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty("omit-xml-declaration", "no");
            transformer.setOutputProperty("encoding", "UTF-8");
            transformer.setOutputProperty("indent", "yes");
            
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            return writer.toString();
        } catch (Exception e) {
            logger.error("Error converting document to string: {}", e.getMessage(), e);
            throw new Exception("Failed to convert document to string: " + e.getMessage(), e);
        }
    }

    private String getChildElementNames(Element element) {
        StringBuilder sb = new StringBuilder();
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                Element child = (Element) children.item(i);
                sb.append(child.getNodeName()).append(" ");
            }
        }
        return sb.toString().trim();
    }
}
