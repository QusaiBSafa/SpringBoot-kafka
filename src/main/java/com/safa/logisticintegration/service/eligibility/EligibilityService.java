package com.safa.logisticintegration.service.eligibility;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.safa.logisticintegration.client.OpenJetClient;
import com.safa.logisticintegration.data.dto.elgibility.IncomingEligibilityCheck;
import com.safa.logisticintegration.data.dto.elgibility.OutgoingEligibilityCheck;
import com.safa.logisticintegration.data.dto.elgibility.openJet.eligibilityCheck.EligibilityCheckRequest;
import com.safa.logisticintegration.data.dto.elgibility.openJet.eligibilityCheck.EligibilityCheckResponse;
import com.safa.logisticintegration.data.dto.elgibility.openJet.eligibilityStatus.EligibilityCheck;
import com.safa.logisticintegration.data.dto.elgibility.openJet.eligibilityStatus.EligibilityCheckAnswer;
import com.safa.logisticintegration.data.dto.elgibility.openJet.eligibilityStatus.EligibilityCheckAnswerMember;
import com.safa.logisticintegration.data.dto.elgibility.openJet.eligibilityStatus.EligibilityStatusResponse;
import com.safa.logisticintegration.data.dto.kafka.KafkaEventSource;
import com.safa.logisticintegration.data.entity.EligibilityStatus;
import com.safa.logisticintegration.data.entity.PendingEligibilityRequest;
import com.safa.logisticintegration.exception.ExceptionMessages;
import com.safa.logisticintegration.exception.IntegrationException;
import com.safa.logisticintegration.repository.PendingEligibilityRequestRepository;
import com.safa.logisticintegration.service.kafka.KafkaProducerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class EligibilityService {

    private final OpenJetClient openJetClient;
    private final KafkaProducerService kafkaProducerService;
    private final PendingEligibilityRequestRepository pendingEligibilityRequestRepository;
    private final Logger logger = LoggerFactory.getLogger(EligibilityService.class);
    @Value("${default-country}")
    private String defaultCountry;
    @Value("${openJet.apikey}")
    private String apiKey;

    @Autowired
    public EligibilityService(OpenJetClient openJetClient, KafkaProducerService kafkaProducerService, PendingEligibilityRequestRepository pendingEligibilityRequestRepository) {
        this.openJetClient = openJetClient;
        this.kafkaProducerService = kafkaProducerService;
        this.pendingEligibilityRequestRepository = pendingEligibilityRequestRepository;
    }

    private boolean isPendingRequest(IncomingEligibilityCheck incomingEligibilityCheck) {
        PendingEligibilityRequest pendingEligibilityRequest = pendingEligibilityRequestRepository.findByReferenceIdAndReferenceTypeAndIdentityNumber(incomingEligibilityCheck.getReferenceId(), incomingEligibilityCheck.getReferenceType(), incomingEligibilityCheck.getIdentityNumber());
        return pendingEligibilityRequest != null;
    }

    @Transactional
    public void createOpenJetEligibilityCheckRequest(IncomingEligibilityCheck incomingEligibilityCheck) throws Exception {
        // If the task already request and still pending, then do nothing because the schedule will poll the pending task and no need to create new request.
        if (isPendingRequest(incomingEligibilityCheck)) {
            return;
        }
        EligibilityCheckRequest eligibilityCheckRequest = new EligibilityCheckRequest();
        eligibilityCheckRequest.setVerified(false);
        eligibilityCheckRequest.setEmiratesId(incomingEligibilityCheck.getIdentityNumber().replaceAll("-", ""));
        eligibilityCheckRequest.setClinicianLicense(incomingEligibilityCheck.getDoctorLicense());
        String phoneWithCountryCode = incomingEligibilityCheck.getPhoneNumber();
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        Phonenumber.PhoneNumber phoneNumber = null;
        try {
            phoneNumber = phoneUtil.parse(phoneWithCountryCode, defaultCountry);
        } catch (NumberParseException e) {
            throw new IntegrationException(String.format(ExceptionMessages.PHONE_NUMBER_PARSING_ERROR, phoneWithCountryCode));
        }
        eligibilityCheckRequest.setCountryCode(String.format("+%d", phoneNumber.getCountryCode()));
        eligibilityCheckRequest.setMobileNumber(String.valueOf(phoneNumber.getNationalNumber()));

        int payerId = Integer.parseInt(incomingEligibilityCheck.getInsuranceId());
        if(payerId == 305){ // Enhanced insurance is supported to do new consultation check.
            eligibilityCheckRequest.setServiceCategoryId(1); // New consultation
            eligibilityCheckRequest.setConsultationCategoryId(4); // Elective is required if service is New consultation
        } else { // other insurances are supported to do pharmacy check.
            eligibilityCheckRequest.setServiceCategoryId(6); // pharmacy
        }
        eligibilityCheckRequest.setPayerId(payerId);
        eligibilityCheckRequest.setReferralLetterRefNo("");
        eligibilityCheckRequest.setPrescriptionReference("");
        eligibilityCheckRequest.setProxyEId(false);

        EligibilityCheckResponse eligibilityCheckResponse = this.openJetClient.createEligibilityCheckRequest(apiKey, eligibilityCheckRequest);

        if (eligibilityCheckResponse.getStatus() == 1) {
            PendingEligibilityRequest pendingEligibilityRequest = new PendingEligibilityRequest();
            long eligibilityId = eligibilityCheckResponse.getData().getEligibilityId();
            pendingEligibilityRequest.setExternalEligibilityId(eligibilityId);
            pendingEligibilityRequest.setReferenceId(incomingEligibilityCheck.getReferenceId());
            pendingEligibilityRequest.setReferenceType(incomingEligibilityCheck.getReferenceType());
            pendingEligibilityRequest.setIdentityNumber(incomingEligibilityCheck.getIdentityNumber());
            pendingEligibilityRequestRepository.save(pendingEligibilityRequest);
            // Send updates to BE service with the eligibility ID.
            OutgoingEligibilityCheck outgoingEligibilityCheck = new OutgoingEligibilityCheck();
            outgoingEligibilityCheck.setReferenceId(incomingEligibilityCheck.getReferenceId());
            outgoingEligibilityCheck.setReferenceType(incomingEligibilityCheck.getReferenceType());
            outgoingEligibilityCheck.setIdentityNumber(incomingEligibilityCheck.getIdentityNumber());
            outgoingEligibilityCheck.setExternalEligibilityId(eligibilityId);
            outgoingEligibilityCheck.setStatus(EligibilityStatus.PENDING.name());
            kafkaProducerService.sendEligibilityCheckUpdates(outgoingEligibilityCheck, KafkaEventSource.THIRD_PARTY_ELIGIBILITY_CHECK_UPDATE);
        } else if (eligibilityCheckResponse.getStatus() == -2) { // This status -2 means that the patient already has valid eligibility check and we can use it.
            // get eligibility id number
            String error = eligibilityCheckResponse.getErrors().get(0);
            String eligibilityIdStr = error.split("/eligibility/requestdetails/")[1];
            long eligibilityId = Long.parseLong(eligibilityIdStr);
            EligibilityStatusResponse eligibilityStatusResponse = openJetClient.getEligibilityStatusCheck(apiKey, eligibilityId);
            processEligibilityStatusResponse(eligibilityStatusResponse, eligibilityId, incomingEligibilityCheck.getReferenceId(), incomingEligibilityCheck.getReferenceType(), incomingEligibilityCheck.getIdentityNumber(), false); // cancel request is false because this request already cancelled
        } else if (eligibilityCheckResponse.getStatus() == -3) { // Rule violation
            processErrorResponse(eligibilityCheckResponse, incomingEligibilityCheck);
            cancelEligibilityRequest(eligibilityCheckResponse.getData().getEligibilityId());
        } else {
            processErrorResponse(eligibilityCheckResponse, incomingEligibilityCheck);

        }
    }

    /**
     * Scheduled job run every 1 minutes to pull the updates from openJet for pending eligibility check
     */
    @Scheduled(cron = "0 */1 * ? * *")
    public void pullEligibilityCheckUpdates() {
        pendingEligibilityRequestRepository.findAll().forEach((pendingEligibilityRequest) -> {
            EligibilityStatusResponse eligibilityStatusResponse = openJetClient.getEligibilityStatusCheck(apiKey, pendingEligibilityRequest.getExternalEligibilityId());
            try {
                processEligibilityStatusResponse(eligibilityStatusResponse, pendingEligibilityRequest.getExternalEligibilityId(), pendingEligibilityRequest.getReferenceId(), pendingEligibilityRequest.getReferenceType(), pendingEligibilityRequest.getIdentityNumber(), true);
            } catch (Exception e) {
                throw new IntegrationException(ExceptionMessages.SCHEDULED_JOB_FAILED, e);
            }
        });
    }

    /**
     * Handle eligibility status response, Map to OutgoingEligibilityCheck (send to BE service)
     * Delete pending eligibility request if its status changed
     */
    private void processEligibilityStatusResponse(EligibilityStatusResponse eligibilityStatusResponse, long eligibilityId, Long referenceId, String referenceType, String identityNumber, boolean cancelRequest) throws Exception {
        // If zero then it is still pending
        if (eligibilityStatusResponse.getStatus() == 0) {
            return;
        }
        // Success response this means the result is ready
        if (eligibilityStatusResponse.getStatus() == 1) {
            OutgoingEligibilityCheck outgoingEligibilityCheck = new OutgoingEligibilityCheck();
            outgoingEligibilityCheck.setExternalEligibilityId(eligibilityId);
            outgoingEligibilityCheck.setReferenceId(referenceId);
            outgoingEligibilityCheck.setReferenceType(referenceType);
            outgoingEligibilityCheck.setIdentityNumber(identityNumber);
            EligibilityCheck eligibilityCheck = eligibilityStatusResponse.getData().getEligibilityCheck();
            boolean result = eligibilityCheck.isResult();
            if (!result) {
                outgoingEligibilityCheck.setStatus(EligibilityStatus.NOT_ELIGIBLE.name());
                outgoingEligibilityCheck.setNotEligibleReason(Collections.singletonList(eligibilityCheck.getDenial().getDenialReason()));
            } else {
                List<String> eligibilitySummaries = new ArrayList<>();
                EligibilityCheckAnswer eligibilityCheckAnswer = eligibilityCheck.getEligibilityCheckAnswer();
                outgoingEligibilityCheck.setFirstName(eligibilityCheckAnswer.getFirstName());
                outgoingEligibilityCheck.setLastname(eligibilityCheckAnswer.getLastName());
                outgoingEligibilityCheck.setAuthorizationEndDate(eligibilityCheckAnswer.getAuthorizationEndDate());
                List<EligibilityCheckAnswerMember> eligibilityCheckAnswerMembers = eligibilityCheckAnswer.getEligibilityCheckAnswerMembers();
                eligibilityCheckAnswerMembers.forEach((eligibilityCheckAnswerMember) -> {
                    String eligibilitySummary = String.format(
                            """
                                    Card number: %s
                                    Package category: %s
                                    Package name: %s
                                    Card network: %s
                                    Policy name: %s
                                    Start date: %s
                                    Expiry date: %s
                                    """,
                            eligibilityCheckAnswerMember.getCardNumber(), eligibilityCheckAnswerMember.getPackageCategory(), eligibilityCheckAnswerMember.getPackageName(), eligibilityCheckAnswerMember.getCardNetwork(),
                            eligibilityCheckAnswerMember.getPolicyName(), eligibilityCheckAnswerMember.getStartDate(), eligibilityCheckAnswerMember.getExpiryDate());
                    eligibilitySummaries.add(eligibilitySummary);
                });
                outgoingEligibilityCheck.setEligibilitySummary(eligibilitySummaries);
                outgoingEligibilityCheck.setStatus(EligibilityStatus.ELIGIBLE.name());
            }
            kafkaProducerService.sendEligibilityCheckUpdates(outgoingEligibilityCheck, KafkaEventSource.THIRD_PARTY_ELIGIBILITY_CHECK_UPDATE);
            if (cancelRequest) {
                cancelEligibilityRequest(eligibilityId);
            }
        } else { // in case of error
            throw new IntegrationException(String.format(ExceptionMessages.OPENJET_RESPONSE_ERROR, eligibilityStatusResponse.getErrors().toString(), eligibilityId, referenceId, referenceType, identityNumber));
        }

    }

    @Transactional
    public void cancelEligibilityRequest(long eligibilityId) {
        pendingEligibilityRequestRepository.deleteByExternalEligibilityId(eligibilityId);
        openJetClient.cancelEligibilityCheck(apiKey, eligibilityId);
    }

    private void processErrorResponse(EligibilityCheckResponse eligibilityCheckResponse, IncomingEligibilityCheck incomingEligibilityCheck) throws Exception {
        OutgoingEligibilityCheck outgoingEligibilityCheck = new OutgoingEligibilityCheck();
        outgoingEligibilityCheck.setReferenceId(incomingEligibilityCheck.getReferenceId());
        outgoingEligibilityCheck.setReferenceType(incomingEligibilityCheck.getReferenceType());
        outgoingEligibilityCheck.setIdentityNumber(incomingEligibilityCheck.getIdentityNumber());
        outgoingEligibilityCheck.setStatus(EligibilityStatus.REQUEST_FAILED.name());
        List<String> errors = eligibilityCheckResponse.getErrors();
        if (eligibilityCheckResponse.getData() != null && eligibilityCheckResponse.getData().getRuleMessage() != null) {
            errors.add(eligibilityCheckResponse.getData().getRuleMessage());
        }
        outgoingEligibilityCheck.setNotEligibleReason(errors);
        kafkaProducerService.sendEligibilityCheckUpdates(outgoingEligibilityCheck, KafkaEventSource.THIRD_PARTY_ELIGIBILITY_CHECK_UPDATE);

    }

}
