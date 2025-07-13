package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.entity.*;
import org.example.repository.CategoryRepository;
import org.example.service.CompanyService;
import org.example.service.ReviewService;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.*;

@Controller
@RequestMapping("/companies")
public class CompanyController {

    private final CompanyService companyService;
    private final ReviewService reviewService;
    private final UserService userService;

    @Autowired
    private CategoryRepository categoryRepository;

    public CompanyController(CompanyService companyService, ReviewService reviewService, UserService userService) {
        this.companyService = companyService;
        this.reviewService = reviewService;
        this.userService = userService;
    }

    @GetMapping
    public String showCompanies(Model model) {
        model.addAttribute("companies", companyService.getAllCompanies());
        return "companies";
    }

    @GetMapping("/{id}")
    public String showCompany(@PathVariable Long id,
                              @RequestParam(required = false) Double priceMin,
                              @RequestParam(required = false) Double priceMax,
                              @RequestParam(required = false) String sortBy,
                              @RequestParam(required = false) List<String> excludedAllergens,
                              Model model,
                              Principal principal) {
        Company company = companyService.getCompanyById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono firmy o ID: " + id));

        List<Category> categories = categoryRepository.findByCompany(company);
        categories.sort(Comparator.comparing(Category::getName, String.CASE_INSENSITIVE_ORDER));

        Map<Category, List<Product>> categoryProducts = new LinkedHashMap<>();

        for (Category category : categories) {
            List<Product> filteredProducts = category.getProducts().stream()
                    .filter(p -> p.getCompany() != null && p.getCompany().getId().equals(company.getId()))
                    .filter(p -> priceMin == null || p.getPrice() >= priceMin)
                    .filter(p -> priceMax == null || p.getPrice() <= priceMax)
                    .filter(p -> isAllowedByAllergens(p.getAllergens(), excludedAllergens))
                    .sorted(getProductComparator(sortBy))
                    .toList();

            categoryProducts.put(category, filteredProducts);
        }

        List<Review> reviews = reviewService.getReviewsForCompany(id);
        model.addAttribute("reviews", reviews);
        model.addAttribute("newReview", new Review());
        model.addAttribute("isLoggedIn", principal != null);
        model.addAttribute("company", company);
        model.addAttribute("categoryProducts", categoryProducts);

        model.addAttribute("priceMin", priceMin);
        model.addAttribute("priceMax", priceMax);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("excludedAllergens", excludedAllergens != null ? excludedAllergens : new ArrayList<>());

        return "company";
    }

    private Comparator<Product> getProductComparator(String sortBy) {
        if ("priceAsc".equals(sortBy)) {
            return Comparator.comparing(Product::getPrice);
        } else if ("priceDesc".equals(sortBy)) {
            return Comparator.comparing(Product::getPrice).reversed();
        } else {
            return Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER);
        }
    }

    private boolean isAllowedByAllergens(String productAllergens, List<String> excludedAllergens) {
        if (excludedAllergens == null || excludedAllergens.isEmpty()) {
            return true;
        }

        if (productAllergens == null || productAllergens.isBlank()) {
            return true;
        }

        List<String> productAllergenList = Arrays.stream(productAllergens.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .toList();

        for (String excluded : excludedAllergens) {
            if (productAllergenList.contains(excluded.toLowerCase())) {
                return false;
            }
        }
        return true;
    }

    @PostMapping("/{id}/add-review")
    public String addReview(@PathVariable Long id,
                            @ModelAttribute("newReview") Review review,
                            Principal principal,
                            RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }

        boolean alreadyReviewed = reviewService.hasUserReviewedCompany(principal.getName(), id);
        if (alreadyReviewed) {
            redirectAttributes.addAttribute("reviewExists", "");
        } else {
            reviewService.addReview(principal.getName(), id, review.getContent(), review.getRating());
            redirectAttributes.addAttribute("reviewAdded", "");
        }

        return "redirect:/companies/" + id;
    }


    @PostMapping("/{companyId}/delete-review")
    public String deleteReview(@PathVariable Long companyId, Principal principal) {
        String username = principal.getName();
        reviewService.deleteReviewByUsernameAndCompany(username, companyId);
        return "redirect:/companies/" + companyId;
    }

    @PostMapping("/{id}/vote")
    public String vote(@PathVariable Long id,
                       @RequestParam("voteValue") int voteValue,
                       Principal principal,
                       HttpServletRequest request) {

        Optional<User> optionalUser = userService.getByEmail(principal.getName());
        Optional<Review> optionalReview = reviewService.getById(id);


        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }

}