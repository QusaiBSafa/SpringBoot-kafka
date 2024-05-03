package com.safa.logisticintegration.exception;

public class ExceptionMessages {
    public final static String OPENJET_RESPONSE_ERROR = "OpenJet Error: %s, eligibility external id: %s, reference id: %s, reference type: %s, identity number: %s";
    public final static String PHONE_NUMBER_PARSING_ERROR = "Failed while parsing phone number, %s";
    public final static String KAFKA_PROCESSING_ERROR = "Failed processing eligibility check request event: %s";
    public final static String SCHEDULED_JOB_FAILED = "Job failed while processing pending eligibility check request";


}
