package gr.hua.dit.project.web.ui;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import gr.hua.dit.project.core.model.PersonType;
import gr.hua.dit.project.core.service.PersonService;
import gr.hua.dit.project.core.service.model.CreatePersonRequest;
import gr.hua.dit.project.core.service.model.CreatePersonResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

@Controller
public class RegistrationController {

    private final PersonService personService;

    public RegistrationController(PersonService personService) {
        if (personService == null) throw new NullPointerException();
        this.personService = personService;
    }

    public record CountryCodeOption(String code, String label, String isoCode) {}

    private List<CountryCodeOption> getCountryCodes(){
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        Set<String> regions = phoneNumberUtil.getSupportedRegions();
        List<CountryCodeOption> options = new ArrayList<>();
        for (String region : regions) {
            int countryCode = phoneNumberUtil.getCountryCodeForRegion(region);
            String countryName = new Locale("", region).getDisplayCountry(Locale.ENGLISH);
            options.add(new CountryCodeOption("+" + countryCode, countryName + " (+" + countryCode + ")", region.toLowerCase()));
        }
        options.sort(Comparator.comparing(CountryCodeOption::label));
        return options;
    }

    @GetMapping("/register")
    public String showRegistrationForm(final Authentication authentication, Model model){
        if (AuthController.isAuthenticated(authentication)) return "redirect:/";

        model.addAttribute("countryCodes", getCountryCodes());
        model.addAttribute("createPersonRequest", new CreatePersonRequest(
                PersonType.CUSTOMER, "", "", "", "", "+30", "", "", "", "", "", null, null
        ));
        return "register";
    }

    @PostMapping("/register")
    public String handleRegistrationFormSubmission(
            final Authentication authentication,
            @Valid @ModelAttribute("createPersonRequest") CreatePersonRequest createPersonRequest,
            final BindingResult bindingResult,
            final Model model
    ) {
        if (AuthController.isAuthenticated(authentication)) return "redirect:/";

        if (bindingResult.hasErrors()) {
            model.addAttribute("countryCodes", getCountryCodes());
            return "register";
        }

        final CreatePersonResult createPersonResult = this.personService.createPerson(createPersonRequest);

        if (createPersonResult.created()) {
            return "redirect:/login";
        }

        model.addAttribute("countryCodes", getCountryCodes());
        model.addAttribute("createPersonRequest", createPersonRequest);
        model.addAttribute("errorMessage", createPersonResult.reason());
        return "register";
    }
}