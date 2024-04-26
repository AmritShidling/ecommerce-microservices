package com.microservice.orderservice.service;

import com.microservice.orderservice.dto.InventoryResponse;
import com.microservice.orderservice.dto.OrderLineItemsDto;
import com.microservice.orderservice.dto.OrderRequest;
import com.microservice.orderservice.event.OrderPlacedEvent;
import com.microservice.orderservice.model.Order;
import com.microservice.orderservice.model.OrderLineItems;
import com.microservice.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    @Autowired
    private final OrderRepository orderRepository;

    private final WebClient.Builder webClientBuilder;

    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;
    public String placeOrder(OrderRequest orderRequest){
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList().stream().map(orderLineItemsDto -> mapToDto(orderLineItemsDto)).toList();
        order.setOrderLineItemsList(orderLineItems);
        //check if item present in inventory

        List<String> skuCodes = order.getOrderLineItemsList()
                .stream()
                .map(orderLineItem -> orderLineItem.getSkuCode())
                .toList();
        InventoryResponse[] inventoryResponses = webClientBuilder.build().get()
                        .uri("http://inventory-service/inventory", uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
                        .retrieve()
                        .bodyToMono(InventoryResponse[].class)
                        .block();
        boolean allInStock = Arrays.stream(inventoryResponses).allMatch(InventoryResponse::isInStock);
        if(allInStock) {
            kafkaTemplate.send("notificationTopic", new OrderPlacedEvent(order.getOrderNumber()));
            orderRepository.save(order);
            return "Order Placed Successfully";
        }
        else
            throw new IllegalArgumentException("Product is not in stock. Please try again later...!");
    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        return orderLineItems;
    }
}
