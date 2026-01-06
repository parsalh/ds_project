package gr.hua.dit.project.web;

import gr.hua.dit.project.core.model.Person;
import gr.hua.dit.project.core.model.PersonType;
import gr.hua.dit.project.core.repository.PersonRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.awt.*;
import java.time.Instant;

/**
 * Controller for <strong>Testing</strong>.
 */
@Controller
public class TestController {

    public TestController() {}

    /*
    @GetMapping(value = "/test/error/404")
    public String test(){
        return "error/404";
    }

    @GetMapping(value = "/test/error/error")
    public String testErrorError(){
        return "error/error";
    }

    @GetMapping(value = "/test/error/NullPointerException")
    public String testErrorNullPointerException(){
        final Integer a = null;
        final int b = 0;
        final int c=a+b;
        return null;
    }
     */

}
