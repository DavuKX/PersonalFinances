package com.personalfinance.transactionservice.infrastructure.config;

import com.personalfinance.transactionservice.domain.model.Category;
import com.personalfinance.transactionservice.domain.model.TransactionType;
import com.personalfinance.transactionservice.domain.port.CategoryRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class DefaultCategorySeeder implements ApplicationRunner {

    private final CategoryRepository categoryRepository;

    public DefaultCategorySeeder(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (categoryRepository.countByUserIdIsNull() > 0) {
            return;
        }
        seedExpenseCategories();
        seedIncomeCategories();
    }

    private void seedExpenseCategories() {
        Map<String, List<String>> expenseCategories = new LinkedHashMap<>();
        expenseCategories.put("Food & Dining", List.of("Groceries", "Restaurants", "Fast Food", "Coffee Shops"));
        expenseCategories.put("Transportation", List.of("Gas", "Public Transit", "Parking", "Ride Sharing"));
        expenseCategories.put("Housing", List.of("Rent/Mortgage", "Utilities", "Insurance", "Maintenance"));
        expenseCategories.put("Shopping", List.of("Clothing", "Electronics", "Home & Garden"));
        expenseCategories.put("Entertainment", List.of("Movies", "Music", "Games", "Streaming Services"));
        expenseCategories.put("Health", List.of("Doctor", "Pharmacy", "Gym", "Insurance"));
        expenseCategories.put("Education", List.of("Tuition", "Books", "Online Courses"));
        expenseCategories.put("Bills & Utilities", List.of("Electricity", "Water", "Internet", "Phone"));
        expenseCategories.put("Personal Care", List.of("Haircut", "Skincare", "Spa"));
        expenseCategories.put("Travel", List.of("Flights", "Hotels", "Car Rental"));
        seedCategories(expenseCategories, TransactionType.EXPENSE);
    }

    private void seedIncomeCategories() {
        Map<String, List<String>> incomeCategories = new LinkedHashMap<>();
        incomeCategories.put("Salary", List.of("Main Job", "Side Job", "Bonus"));
        incomeCategories.put("Investment", List.of("Dividends", "Capital Gains", "Interest"));
        incomeCategories.put("Freelance", List.of("Consulting", "Contract Work"));
        incomeCategories.put("Gifts", List.of());
        incomeCategories.put("Rental Income", List.of());
        incomeCategories.put("Refunds", List.of());
        seedCategories(incomeCategories, TransactionType.INCOME);
    }

    private void seedCategories(Map<String, List<String>> categoriesMap, TransactionType type) {
        for (Map.Entry<String, List<String>> entry : categoriesMap.entrySet()) {
            Category parent = categoryRepository.save(Category.createDefault(entry.getKey(), null, type));
            for (String subName : entry.getValue()) {
                categoryRepository.save(Category.createDefault(subName, parent.getId(), type));
            }
        }
    }
}

