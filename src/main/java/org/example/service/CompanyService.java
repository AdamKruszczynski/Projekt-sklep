package org.example.service;

import org.example.entity.Company;
import org.example.entity.User;
import org.example.repository.CompanyRepository;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Service
public class CompanyService {

    @Autowired
    UserRepository userRepository;

    private final CompanyRepository companyRepository;

    public List<Company> findAll() {
        return companyRepository.findAll();
    }

    public Optional<Company> findById(Long id) {
        return companyRepository.findById(id);
    }

    public Company save(Company company) {
        return companyRepository.save(company);
    }

    public void deleteById(Long id) {
        companyRepository.deleteById(id);
    }

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    public Optional<Company> getCompanyById(Long id) {
        return companyRepository.findById(id);
    }

    public void saveCompany(Company company) {
        companyRepository.save(company);
    }

    public Company createNewCompanyForUser(Company companyData, User user) {
        if (companyData.getId() != null && user.getCompany() == null) {
            companyData.setId(null);
        }

        companyData.setOwner(user);
        Company saved = companyRepository.save(companyData);

        user.setCompany(saved);
        userRepository.save(user);

        return saved;
    }

    public Company getCompanyByCurrentUser(Principal principal) {
        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Nie znaleziono użytkownika: " + username));

        return companyRepository.findByOwner(user)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono firmy dla użytkownika: " + username));
    }

    public void saveOrUpdateCompany(Company company, Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        company.setOwner(user);
        companyRepository.save(company);
    }

}