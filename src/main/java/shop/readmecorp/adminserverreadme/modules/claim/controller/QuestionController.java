package shop.readmecorp.adminserverreadme.modules.claim.controller;

import org.springframework.web.bind.annotation.RestController;
import shop.readmecorp.adminserverreadme.modules.claim.service.QuestionService;

@RestController
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }
}
