package com.openbank.client.rest.v1;

import com.openbank.client.exception.RecordNotFoundException;
import com.openbank.client.model.TransactionDto;
import com.openbank.client.model.TransactionType;
import io.swagger.annotations.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Path("/")
@Api(value = "OpenBank Client", produces = "application/json")
public class OpenBankApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenBankApiClient.class);
    @Autowired
    RestTemplate restTemplate;

    @Value("${openbank.api.endpoint}")
    private String apiUrl;

    @GET
    @Path("v1/transaction-list/{bankId}/{accountId}")
    @ApiOperation(value = "Gets a list of all transactions from OpenBank based on bank identifier, account identifier ", response = TransactionDto.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "List of transactions found"),
            @ApiResponse(code = 404, message = "Unable to find list of transactions")
    })
    public ResponseEntity getTransactionList(@ApiParam(value = "Bank Identifier required") @PathParam("bankId") String bankId,
                                             @ApiParam(value = "Account Identifier required") @PathParam("accountId") String accountId) throws IOException {

        LOGGER.info("start getTransactionList() v1 ");
        List<TransactionDto> transactionDtoList = invokeExternalRestApi(bankId, accountId);
        if (transactionDtoList == null || transactionDtoList.isEmpty()) {
            throw new RecordNotFoundException("Record not found for Bank Id : " + bankId + " Account Id : " + accountId);
        }
        LOGGER.info("end getTransactionList() v1 " + transactionDtoList.size());
        return new ResponseEntity<>(transactionDtoList, HttpStatus.OK);
    }

    @GET
    @Path("v1/transaction-list/{bankId}/{accountId}/{transactionType}")
    @ApiOperation(value = "Gets a list of all transactions from OpenBank based on bank identifier, account identifier and transaction type", response = TransactionDto.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "List of transactions found"),
            @ApiResponse(code = 404, message = "Unable to find list of transactions")
    })
    public ResponseEntity getListForTransactionType(@ApiParam(value = "Bank Identifier required") @PathParam("bankId") String bankId,
                                                    @ApiParam(value = "Account Identifier required") @PathParam("accountId") String accountId,
                                                    @ApiParam(value = "TransactionType required") @PathParam("transactionType") String transactionType) throws IOException {
        LOGGER.info("start getListForTransactionType() v1 ");

        List<TransactionDto> updatedTransactionList = invokeExternalRestApi(bankId, accountId).stream()
                .filter(dto -> (dto.getTransactionType() != null && dto.getTransactionType().equalsIgnoreCase(transactionType)))
                .collect(Collectors.toList());

        if (updatedTransactionList == null || updatedTransactionList.isEmpty()) {
            throw new RecordNotFoundException("Record not found for Bank Id : " + bankId + " Account Id : " + accountId + " Transaction Type :" + transactionType);
        }
        LOGGER.info("end getListForTransactionType() v1 " + updatedTransactionList.size());
        return new ResponseEntity<>(updatedTransactionList, HttpStatus.OK);
    }


    @GET
    @Path("v1/total-amount/{bankId}/{accountId}/{transactionType}")
    @ApiOperation(value = "Gets total amount for transactions from OpenBank based on bank identifier, account identifier and transaction type", response = TransactionType.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "List of transactions found"),
            @ApiResponse(code = 404, message = "Unable to find list of transactions")
    })
    public ResponseEntity getTotalAmtForTransactionType(@ApiParam(value = "Bank Identifier required") @PathParam("bankId") String bankId,
                                                        @ApiParam(value = "Account ID required") @PathParam("accountId") String accountId,
                                                        @ApiParam(value = "Transaction Type Required") @PathParam("transactionType") String transactionType) throws IOException {
        LOGGER.info("start getTotalAmtForTransactionType() v1 ");
        TransactionType transactionType1 = new TransactionType();
        invokeExternalRestApi(bankId, accountId).stream()
                .filter(dto -> (dto.getTransactionType() != null && dto.getTransactionType().equalsIgnoreCase(transactionType)))
                .forEach(dto -> {
                    transactionType1.setTransactionCurrency(dto.getTransactionCurrency());
                    transactionType1.setTotalAmt(transactionType1.getTotalAmt() + Double.parseDouble(dto.getTransactionAmount()));
                    transactionType1.setTransactionType(dto.getTransactionType());
                });

        LOGGER.info("end getTotalAmtForTransactionType() v1 " + transactionType1);
        return new ResponseEntity<>(transactionType1, HttpStatus.OK);
    }


    /**
     * Method to invoke Public REST API endpoint of Open Bank Project
     **/
    private List<TransactionDto> invokeExternalRestApi(String bankId, String accountId) throws IOException {

        LOGGER.info("Invoke REST API call for Open Bank API ");
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(new MediaType[]{MediaType.APPLICATION_JSON}));
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> params = new HashMap<>();
        params.put("bankId", bankId);
        params.put("accountId", accountId);

        return convertToDto(restTemplate.exchange(this.apiUrl, HttpMethod.GET, new HttpEntity(headers), String.class, params).getBody());
    }


    /**
     * Convert REST response to Java POJO - TransactionDto
     **/
    private List<TransactionDto> convertToDto(String responseBody) throws IOException {

        LOGGER.info("Converting REST response to TransactionDto Class ");
        List<TransactionDto> transactionDtos = new ArrayList<>();
        if (responseBody != null) {
            JSONArray resultsArray = new JSONObject(responseBody).optJSONArray(RestConstants.TRANSACTIONS);
            if (resultsArray != null) {
                for (int i = 0; i < resultsArray.length(); i++) {
                    TransactionDto dto = new TransactionDto();
                    JSONObject resultObject = resultsArray.optJSONObject(i);
                    dto.setId(resultObject.optString(RestConstants.ID, ""));
                    dto.setAccountId(resultObject.optJSONObject(RestConstants.ACCOUNT).optString(RestConstants.ID, ""));
                    dto.setCounterPartyAccount(resultObject.optJSONObject(RestConstants.OTHER_ACCOUNT).optString(RestConstants.NUMBER, ""));
                    dto.setCounterPartyName(resultObject.optJSONObject(RestConstants.OTHER_ACCOUNT).optJSONObject(RestConstants.HOLDER).optString(RestConstants.NAME, ""));
                    dto.setCounterPartyLogoPath(resultObject.optJSONObject(RestConstants.OTHER_ACCOUNT).optJSONObject(RestConstants.METADATA).optString(RestConstants.IMAGE_URL, ""));
                    dto.setTransactionAmount(resultObject.optJSONObject(RestConstants.DETAILS).optJSONObject(RestConstants.VALUE).optString(RestConstants.AMOUNT, ""));
                    dto.setTransactionCurrency(resultObject.optJSONObject(RestConstants.DETAILS).optJSONObject(RestConstants.VALUE).optString(RestConstants.CURRENCY, ""));
                    dto.setTransactionType(resultObject.optJSONObject(RestConstants.DETAILS).optString(RestConstants.TYPE, ""));
                    dto.setDescription(resultObject.optJSONObject(RestConstants.DETAILS).optString(RestConstants.DESCRIPTION, ""));
                    transactionDtos.add(dto);
                }
            }
        }
        return transactionDtos;
    }
}