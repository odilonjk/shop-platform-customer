package com.odilonjk.shopplatformcustomer.resources;

import com.google.gson.Gson;
import com.odilonjk.shopplatformcustomer.entities.Customer;
import com.odilonjk.shopplatformcustomer.models.RestResponsePage;
import com.odilonjk.shopplatformcustomer.repositories.CustomerRepository;
import com.odilonjk.shopplatformcustomer.repositories.CustomerSearchPageRepository;
import com.odilonjk.shopplatformcustomer.repositories.CustomerSearchSliceRepository;
import com.odilonjk.shopplatformcustomer.services.CustomerService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CustomerResourceIT {

    @LocalServerPort
    int port;

    @MockBean
    private CustomerRepository customerRepository;

    @MockBean
    private CustomerSearchPageRepository customerSearchPageRepository;

    @MockBean
    private CustomerSearchSliceRepository customerSearchSliceRepository;

    @Autowired
    private CustomerService customerService;

    @Test
    public void shouldFindCustomerById() {
        var id = "a1b2c3";
        var customer = new Customer(id, "Tester", "(55) 1234-5678");

        when(customerRepository.findById(id))
                .thenReturn(Optional.of(customer));

        var receivedCustomer = given()
                .when().port(port).get("/customer/" + id)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(Customer.class);

        assertEquals(customer, receivedCustomer);
    }

    @Test
    public void shouldFindCustomerByName() {
        var customer1 = new Customer("a1b2c3", "Tester A", "(55) 1234-5678");
        var customer2 = new Customer("a2b2c3", "Tester B", "(55) 2234-5678");
        var customer3 = new Customer("a3b2c3", "Tester C", "(55) 3234-5678");

        when(customerRepository.findByNameIgnoreCaseContaining("Tester"))
                .thenReturn(List.of(customer1, customer2, customer3));

        Customer[] receivedCustomers = given()
                .when().port(port).get("/customer/name/" + "Tester")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(Customer[].class);

        assertArrayEquals(new Customer[]{customer1, customer2, customer3}, receivedCustomers);
    }

    Gson gson = new Gson();
    @Test
    public void shouldFindCustomerByNameSliceResponse() {
        var customer1 = new Customer("a1b2c3", "Tester A", "(55) 1234-5678");
        var customer2 = new Customer("a2b2c3", "Tester B", "(55) 2234-5678");
        var customer3 = new Customer("a3b2c3", "Tester C", "(55) 3234-5678");
        List<Customer> content = new ArrayList<>();
        content.add(customer1);
        content.add(customer2);
        content.add(customer3);

        Slice<Customer> page = new RestResponsePage<>(content,3,3,3l,null,true,1,null,true,3);

        when(customerSearchSliceRepository.findByName(anyString(),any(Pageable.class)))
                .thenReturn(page);

        RestResponsePage<List<Customer>> json  = given()
                .when().port(port).get("/customerSlice?name=" + "Tester")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(RestResponsePage.class);
        assertEquals(3, 3);
    }

    @Test
    public void shouldFindCustomerByNamePageResponse() {
        var customer1 = new Customer("a1b2c3", "Tester A", "(55) 1234-5678");
        var customer2 = new Customer("a2b2c3", "Tester B", "(55) 2234-5678");
        var customer3 = new Customer("a3b2c3", "Tester C", "(55) 3234-5678");
        List<Customer> content = new ArrayList<>();
        content.add(customer1);
        content.add(customer2);
        content.add(customer3);

        Page<Customer> page = new RestResponsePage<>(content,3,3,3l,null,true,1,null,true,3);

        when(customerSearchPageRepository.findByName(anyString(),any(Pageable.class)))
                .thenReturn(page);

        RestResponsePage<List<Customer>> json = given()
                .when().port(port).get("/customerPage?name=" + "Tester")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(RestResponsePage.class);

        assertEquals(3, json.getNumberOfElements());
    }


}
