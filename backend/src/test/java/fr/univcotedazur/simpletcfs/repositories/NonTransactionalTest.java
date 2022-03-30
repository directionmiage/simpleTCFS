package fr.univcotedazur.simpletcfs.repositories;

import fr.univcotedazur.simpletcfs.entities.*;
import fr.univcotedazur.simpletcfs.exceptions.AlreadyExistingCustomerException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@SpringBootTest
public class NonTransactionalTest {

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    OrderRepository orderRepository;

    Long orderId;

    @BeforeEach
    void setup() {
        Customer john = new Customer("john", "1234567890");
        customerRepository.save(john);
        Order createdOrder = new Order(john, new HashSet<>(Arrays.asList(new Item(Cookies.CHOCOLALALA,2))));
        // The order is normally added to the customer in the Cashier component ->  is normal as it is a business
        // component, and linking the two objects is done only if everything goes well in the business process
        john.add(createdOrder);
        orderRepository.saveAndFlush(createdOrder);
        orderId = createdOrder.getId();
    }

    @AfterEach
    public void cleaningUp()  {
        customerRepository.deleteAll();
        orderRepository.deleteAll();
    }

    @Test // THIS METHOD IS NOT TRANSACTIONAL and we don't use a transactional controller method
    void testExceptionWithLazyFetching() {
        Assertions.assertNotNull(orderId);
        Optional<Order> orderToGet = orderRepository.findById(orderId);
        Assertions.assertTrue(orderToGet.isPresent());
        Order foundOrder = orderToGet.get();
        // the order is found by its repository,
        // the customer attribute is not a collection, it is loaded with the order
        Assertions.assertEquals("john",foundOrder.getCustomer().getName());
        // its items are loaded when we access them IN A TRANSACTION (@ElementCollection on items is lazy)
        // BUT WE ARE NOT INSIDE A TRANSACTION
        Assertions.assertThrows( org.hibernate.LazyInitializationException.class, () -> {
            Assertions.assertEquals(1, foundOrder.getItems().size());
        });
    }

}
