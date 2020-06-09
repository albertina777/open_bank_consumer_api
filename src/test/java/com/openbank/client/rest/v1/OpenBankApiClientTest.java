package com.openbank.client.rest.v1;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.openbank.client.main.Application;
import com.openbank.client.model.UserProperties;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest("server.port:0")
public class OpenBankApiClientTest {

    private static final String API_PATH = "/api";
    private static final String RESPONSE = "{\"transactions\":[{\"id\":\"58aeed54-7042-456d-af86-f517bff5b7af\",\"this_account\":{\"id\":\"savings-tin\",\"holders\":[{\"name\":\"Savings - Kids John\",\"is_alias\":false}],\"number\":\"832425-00304050\",\"kind\":\"savings\",\"IBAN\":null,\"swift_bic\":null,\"bank\":{\"national_identifier\":\"rbs\",\"name\":\"The Royal Bank of Scotland\"}},\"other_account\":{\"id\":\"5780MRN4uBIgWYmWAhI3pdqbSpItvOw4culXP5FWHJA\",\"holder\":{\"name\":\"ALIAS_03C57D\",\"is_alias\":true},\"number\":\"savings-tin\",\"kind\":\"AC\",\"IBAN\":\"4930396\",\"swift_bic\":null,\"bank\":{\"national_identifier\":null,\"name\":\"rbs\"},\"metadata\":{\"public_alias\":null,\"private_alias\":null,\"more_info\":null,\"URL\":null,\"image_URL\":null,\"open_corporates_URL\":null,\"corporate_location\":null,\"physical_location\":null}},\"details\":{\"type\":\"SEPA\",\"description\":\"This is a SEPA Transaction Request\",\"posted\":\"2020-06-05T08:28:38Z\",\"completed\":\"2020-06-05T08:28:38Z\",\"new_balance\":{\"currency\":\"GBP\",\"amount\":null},\"value\":{\"currency\":\"GBP\",\"amount\":\"8.60\"}},\"metadata\":{\"narrative\":null,\"comments\":[],\"tags\":[],\"images\":[],\"where\":null}},{\"id\":\"e22b7066-d02f-41fa-a84f-5dbfcc39e307\",\"this_account\":{\"id\":\"savings-tin\",\"holders\":[{\"name\":\"Savings - Kids John\",\"is_alias\":false}],\"number\":\"832425-00304050\",\"kind\":\"savings\",\"IBAN\":null,\"swift_bic\":null,\"bank\":{\"national_identifier\":\"rbs\",\"name\":\"The Royal Bank of Scotland\"}},\"other_account\":{\"id\":\"5780MRN4uBIgWYmWAhI3pdqbSpItvOw4culXP5FWHJA\",\"holder\":{\"name\":\"ALIAS_03C57D\",\"is_alias\":true},\"number\":\"savings-tin\",\"kind\":\"AC\",\"IBAN\":\"4930396\",\"swift_bic\":null,\"bank\":{\"national_identifier\":null,\"name\":\"rbs\"},\"metadata\":{\"public_alias\":null,\"private_alias\":null,\"more_info\":null,\"URL\":null,\"image_URL\":null,\"open_corporates_URL\":null,\"corporate_location\":null,\"physical_location\":null}},\"details\":{\"type\":\"SEPA\",\"description\":\"This is a SEPA Transaction Request\",\"posted\":\"2020-06-05T08:15:58Z\",\"completed\":\"2020-06-05T08:15:58Z\",\"new_balance\":{\"currency\":\"GBP\",\"amount\":null},\"value\":{\"currency\":\"GBP\",\"amount\":\"8.60\"}},\"metadata\":{\"narrative\":null,\"comments\":[],\"tags\":[],\"images\":[],\"where\":null}}]}";
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    UserProperties userProperties;
    @Value("${local.server.port}")
    private int port;
    private MockRestServiceServer mockRestServiceServer;

    @Before
    public void setup() {
        RestAssured.port = this.port;
        mockRestServiceServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    public void retrieveResponseForTransactionsList() {
        String bank = "rbs";
        String account = "savings-tin";

        getOpenBankAPI();
        RestAssured.
                given().
                auth().basic(userProperties.getUser(), userProperties.getPassword()).
                accept(ContentType.JSON).
                when().
                get(String.format("%s/v1/transaction-list/{bankId}/{accountId}", API_PATH), bank, account).
                then().
                statusCode(HttpStatus.SC_OK).assertThat().body("size()", is(3));
    }

    @Test
    public void retrieveResponseForTransactionsListUsingTransactionType() {
        String bank = "rbs";
        String account = "savings-tin";
        String transType = "SEPA";

        getOpenBankAPI();
        RestAssured.
                given().
                auth().basic(userProperties.getUser(), userProperties.getPassword()).
                accept(ContentType.JSON).
                when().
                get(String.format("%s/v1/transaction-list/{bankId}/{accountId}/{transactionType}", API_PATH), bank, account, transType).
                then().
                statusCode(HttpStatus.SC_OK).assertThat().body("size()", is(3));
    }

    @Test
    public void fetchTotalAmountForTransactionType() {
        String bank = "rbs";
        String account = "savings-tin";
        String transType = "SEPA";

        getOpenBankAPI();
        RestAssured.
                given().
                auth().basic(userProperties.getUser(), userProperties.getPassword()).
                accept(ContentType.JSON).
                when().
                get(String.format("%s/v1/total-amount/{bankId}/{accountId}/{transactionType}", API_PATH), bank, account, transType).
                then().
                statusCode(HttpStatus.SC_OK).assertThat().body("size()", is(3));
    }

    @Test
    public void retrieveShouldResultIn401ForTransactionsList() {
        String bank = "abc";
        String account = "xyz";
        RestAssured.
                when().
                get(String.format("%s/v1/transaction-list/{bankId}/{accountId}", API_PATH), bank, account).
                then().
                statusCode(HttpStatus.SC_UNAUTHORIZED).
                contentType(ContentType.JSON);
    }

    @Test
    public void retrieveShouldResultIn401ForTransactionsType() {
        String bank = "abc";
        String account = "xyz";
        String transactionType = "tran";
        RestAssured.
                when().
                get(String.format("%s/v1/transaction-list/{bankId}/{accountId}/{transactionType}", API_PATH), bank, account, transactionType).
                then().
                statusCode(HttpStatus.SC_UNAUTHORIZED).
                contentType(ContentType.JSON);
    }

    private void getOpenBankAPI() {

        this.mockRestServiceServer.expect(requestTo("https://apisandbox.openbankproject.com/obp/v1.2.1/banks/rbs/accounts/savings-tin/public/transactions"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(RESPONSE, MediaType.APPLICATION_JSON));
    }
}
