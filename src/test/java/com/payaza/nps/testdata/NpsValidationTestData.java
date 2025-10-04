package com.payaza.nps.testdata;

/**
 * Test data for NPS validation phases
 * Contains the exact sample messages provided by NIBSS for validation
 */
public class NpsValidationTestData {

    /**
     * Phase 1: Valid Pacs.008 Message for Outbound Validation
     * This is the exact sample provided by NIBSS
     */
    public static final String VALID_PACS_008_MESSAGE = """
            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <ns2:Document xmlns:ns2="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.12">
                <FIToFICstmrCdtTrf>
                    <GrpHdr>
                        <MsgId>99905820250721095222930239203831889</MsgId>
                        <CreDtTm>2025-02-25T09:52:22.954Z</CreDtTm>
                        <BtchBookg>false</BtchBookg>
                        <NbOfTxs>1</NbOfTxs>
                        <SttlmInf>
                            <SttlmMtd>CLRG</SttlmMtd>
                        </SttlmInf>
                        <InstgAgt>
                            <FinInstnId>
                                <BICFI>999058</BICFI>
                                <ClrSysMmbId>
                                    <MmbId>999058</MmbId>
                                </ClrSysMmbId>
                            </FinInstnId>
                        </InstgAgt>
                        <InstdAgt>
                            <FinInstnId>
                                <BICFI/>
                                <ClrSysMmbId>
                                    <MmbId>999057</MmbId>
                                </ClrSysMmbId>
                            </FinInstnId>
                        </InstdAgt>
                    </GrpHdr>
                    <CdtTrfTxInf>
                        <PmtId>
                            <InstrId>99905899905720250721085722893090687</InstrId>
                            <EndToEndId>99905899905798653637383920281615142</EndToEndId>
                            <TxId>99905820250721095222930239203831889</TxId>
                        </PmtId>
                        <PmtTpInf>
                            <ClrChanl>RTNS</ClrChanl>
                            <SvcLvl>
                                <Prtry>0100</Prtry>
                            </SvcLvl>
                            <LclInstrm>
                                <Prtry>CTAA</Prtry>
                            </LclInstrm>
                            <CtgyPurp>
                                <Prtry>001</Prtry>
                            </CtgyPurp>
                        </PmtTpInf>
                        <IntrBkSttlmAmt Ccy="NGN">78000.00</IntrBkSttlmAmt>
                        <IntrBkSttlmDt>2025-02-25</IntrBkSttlmDt>
                        <ChrgBr>SLEV</ChrgBr>
                        <InstgAgt>
                            <FinInstnId>
                                <BICFI/>
                                <ClrSysMmbId>
                                    <MmbId>999058</MmbId>
                                </ClrSysMmbId>
                            </FinInstnId>
                        </InstgAgt>
                        <InstdAgt>
                            <FinInstnId>
                                <BICFI/>
                                <ClrSysMmbId>
                                    <MmbId>999057</MmbId>
                                </ClrSysMmbId>
                            </FinInstnId>
                        </InstdAgt>
                        <Dbtr>
                            <Nm>James</Nm>
                        </Dbtr>
                        <DbtrAcct>
                            <Id>
                                <IBAN>0177136558</IBAN>
                            </Id>
                            <Nm>James</Nm>
                        </DbtrAcct>
                        <DbtrAgt>
                            <FinInstnId>
                                <ClrSysMmbId>
                                    <MmbId>999058</MmbId>
                                </ClrSysMmbId>
                            </FinInstnId>
                        </DbtrAgt>
                        <CdtrAgt>
                            <FinInstnId>
                                <ClrSysMmbId>
                                    <MmbId>999057</MmbId>
                                </ClrSysMmbId>
                            </FinInstnId>
                        </CdtrAgt>
                        <Cdtr>
                            <Nm>Musa</Nm>
                        </Cdtr>
                        <CdtrAcct>
                            <Id>
                                <IBAN>0177136558</IBAN>
                            </Id>
                            <Nm>Musa</Nm>
                        </CdtrAcct>
                        <InstrForNxtAgt>
                            <InstrInf>/BNF/Beneficiary info</InstrInf>
                        </InstrForNxtAgt>
                        <InstrForNxtAgt>
                            <InstrInf>/SMPL/Sample data</InstrInf>
                        </InstrForNxtAgt>
                        <RmtInf>
                            <Ustrd>String of 140 chars</Ustrd>
                        </RmtInf>
                    </CdtTrfTxInf>
                    <SplmtryData>
                        <PlcAndNm>AdditionalVerificationDetails</PlcAndNm>
                            <Envlp>
                                <CustomData>
                                    <DebtorInfo>
                                        <AccountDesignation>1</AccountDesignation>
                                        <IdType>bvn</IdType> 
                                        <IdValue>2211232344</IdValue>
                                        <AccountTier>1</AccountTier>
                                    </DebtorInfo>
                                    <DebtorMetadata>
                                            <!-- <AnyOtherData>1</AnyOtherData > -->
                                    </DebtorMetadata>
                                    <CreditorInfo>
                                        <AccountDesignation>1</AccountDesignation >
                                        <IdType>bvn</IdType>
                                        <IdValue>2211232346</IdValue>
                                        <AccountTier>1</AccountTier>
                                    </CreditorInfo>
                                    <CreditorMetadata>
                                        <!-- <AnyOtherData>...</AnyOtherData> -->
                                    </CreditorMetadata>
                                    <TransactionInfo>
                                        <TransactionLocation>01080652440N020900337921E</TransactionLocation>
                                        <NameEnquiryMsgId></NameEnquiryMsgId>
                                        <ChannelCode>1</ChannelCode>
                                        <RiskRating>R000000000000000000B9</RiskRating>
                                    </TransactionInfo>
                                </CustomData>
                            </Envlp>
                    </SplmtryData>
                </FIToFICstmrCdtTrf>
            </ns2:Document>
            """;

    /**
     * Phase 3: Valid Pacs.002 Message for Response Validation
     * This is the exact sample provided by NIBSS
     */
    public static final String VALID_PACS_002_MESSAGE = """
            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <ns2:Document xmlns:ns2="urn:iso:std:iso:20022:tech:xsd:pacs.002.001.12">
                <FIToFIPmtStsRpt>
                    <GrpHdr>
                        <!--Unique message identifier-->
                        <MsgId>99905820250718112702293023920383111</MsgId>
                        <!--Message creation local date-time-->
                        <CreDtTm>2025-02-25T00:02:35.954Z</CreDtTm>
                        <InstgAgt>
                            <FinInstnId>
                                <BICFI>999058</BICFI>
                                <ClrSysMmbId>
                                    <MmbId>ABCBANK</MmbId>
                                </ClrSysMmbId>
                            </FinInstnId>
                        </InstgAgt>
                        <InstdAgt>
                            <FinInstnId>
                                <BICFI>999057</BICFI>
                                <ClrSysMmbId>
                                    <MmbId>XYZBANK</MmbId>
                                </ClrSysMmbId>
                            </FinInstnId>
                        </InstdAgt>
                    </GrpHdr>
                    <OrgnlGrpInfAndSts>
                        <!--As taken from pacs.008 FIToFICstmrCdtTrf/GrpHdr/MsgId-->
                        <OrgnlMsgId>99905820250718141133359235380416169</OrgnlMsgId>
                        <!--Type of original message to refer-->
                        <OrgnlMsgNmId>pacs.008.001.12</OrgnlMsgNmId>
                        <!--As taken from pacs.008 FIToFICstmrCdtTrf/GrpHdr/CreDtTm-->
                        <OrgnlCreDtTm>2025-02-25T00:02:35.954Z</OrgnlCreDtTm>
                        <GrpSts>ACSC</GrpSts>
                    </OrgnlGrpInfAndSts>
                    <TxInfAndSts>
                        <!--Instructing agent.Participant who sent original payment to the system.-->
                        <InstgAgt>
                            <FinInstnId>
                                <BICFI>999057</BICFI>
                                <ClrSysMmbId>
                                    <MmbId>ABCBANK</MmbId>
                                </ClrSysMmbId>
                            </FinInstnId>
                        </InstgAgt>
                        <InstdAgt>
                            <FinInstnId>
                                <BICFI>999058</BICFI>
                                <ClrSysMmbId>
                                    <MmbId>XYZBANK</MmbId>
                                </ClrSysMmbId>
                            </FinInstnId>
                        </InstdAgt>
                        <OrgnlTxRef>
                            <!--Settlement date of original payment-->
                            <IntrBkSttlmDt>2025-02-25</IntrBkSttlmDt>
                        </OrgnlTxRef>
                    </TxInfAndSts>
                </FIToFIPmtStsRpt>
            </ns2:Document>
            """;

    /**
     * Sample JSON request that should convert to the valid Pacs.008 message
     */
    public static final String VALID_PAYMENT_REQUEST_JSON = """
            {
              "endToEndId": "99905899905798653637383920281615142",
              "debtorAccount": "0177136558",
              "creditorAccount": "0177136558",
              "amount": 78000.00,
              "currency": "NGN",
              "debtorName": "James",
              "creditorName": "Musa",
              "remittanceInfo": "String of 140 chars",
              "debtorBankId": "999058",
              "creditorBankId": "999057",
              "instructionId": "99905899905720250721085722893090687",
              "transactionId": "99905820250721095222930239203831889"
            }
            """;

    /**
     * Sample JSON request for payment status inquiry
     */
    public static final String VALID_PAYMENT_STATUS_REQUEST_JSON = """
            {
              "originalMessageId": "99905820250718141133359235380416169",
              "originalEndToEndId": "99905899905798653637383920281615142",
              "originalInstructionId": "99905899905720250721085722893090687",
              "originalTransactionId": "99905820250721095222930239203831889"
            }
            """;

    /**
     * Test endpoints for validation
     */
    public static final String NPS_PACS_ENDPOINT = "https://<nps-ip-you-are-calling>:8022/nps/pacs";
    public static final String NPS_ACMT_ENDPOINT = "https://<nps-ip-you-are-calling>:8022/nps/acmt";

    /**
     * Test credentials and configuration
     */
    public static final String TEST_BANK_ID = "999058";
    public static final String TEST_BENEFICIARY_BANK_ID = "999057";
    public static final String TEST_BICFI = "999058";
    public static final String TEST_MESSAGE_ID_PREFIX = "999058";

    /**
     * Validation response codes
     */
    public static final int HTTP_SUCCESS = 200;
    public static final int HTTP_BAD_REQUEST = 400;
    public static final String SUCCESS_MESSAGE = "Message processed successfully";
    public static final String ENCRYPTION_ERROR_MESSAGE = "Could not decrypt message";
}
