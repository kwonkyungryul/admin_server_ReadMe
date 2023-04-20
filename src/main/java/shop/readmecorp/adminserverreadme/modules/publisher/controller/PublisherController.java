package shop.readmecorp.adminserverreadme.modules.publisher.controller;

import org.springframework.web.bind.annotation.RestController;
import shop.readmecorp.adminserverreadme.modules.publisher.service.PublisherService;

@RestController
public class PublisherController {

    private final PublisherService publisherService;

    public PublisherController(PublisherService publisherService) {
        this.publisherService = publisherService;
    }
}
