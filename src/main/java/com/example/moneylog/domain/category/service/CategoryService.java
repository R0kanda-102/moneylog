package com.example.moneylog.domain.category.service;

import com.example.moneylog.domain.category.dto.CategoryRequest;
import com.example.moneylog.domain.category.dto.CategoryResponse;
import com.example.moneylog.domain.category.entity.Category;
import com.example.moneylog.domain.category.entity.CategoryType;
import com.example.moneylog.domain.category.repository.CategoryRepository;
import com.example.moneylog.domain.user.entity.User;
import com.example.moneylog.global.exception.CustomException;
import com.example.moneylog.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // 회원가입 시 기본 카테고리 자동 생성 (3일차에서 회원가입 로직과 연결 예정)
    @Transactional
    public void seedDefaultCategories(User user) {
        if (categoryRepository.existsByUser(user)) return;

        List<Category> defaults = List.of(
                Category.builder().user(user).name("식비").type(CategoryType.EXPENSE).build(),
                Category.builder().user(user).name("교통").type(CategoryType.EXPENSE).build(),
                Category.builder().user(user).name("주거").type(CategoryType.EXPENSE).build(),
                Category.builder().user(user).name("문화").type(CategoryType.EXPENSE).build(),
                Category.builder().user(user).name("급여").type(CategoryType.INCOME).build(),
                Category.builder().user(user).name("용돈").type(CategoryType.INCOME).build()
        );
        categoryRepository.saveAll(defaults);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getMyCategories(User user) {
        return categoryRepository.findByUser(user).stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Transactional
    public CategoryResponse create(User user, CategoryRequest req) {
        Category category = Category.builder()
                .user(user)
                .name(req.name())
                .type(req.type())
                .build();
        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse update(User user, Long id, CategoryRequest req) {
        Category category = findOwned(user, id);
        category.update(req.name(), req.type());
        return CategoryResponse.from(category);
    }

    @Transactional
    public void delete(User user, Long id) {
        Category category = findOwned(user, id);
        categoryRepository.delete(category);
    }

    private Category findOwned(User user, Long id) {
        return categoryRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
    }
}