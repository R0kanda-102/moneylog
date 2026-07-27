package com.example.moneylog.domain.category.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.moneylog.domain.category.dto.CategoryRequest;
import com.example.moneylog.domain.category.dto.CategoryResponse;
import com.example.moneylog.domain.category.entity.Category;
import com.example.moneylog.domain.category.entity.CategoryType;
import com.example.moneylog.domain.category.repository.CategoryRepository;
import com.example.moneylog.domain.user.entity.User;
import com.example.moneylog.global.exception.CustomException;
import com.example.moneylog.global.exception.ErrorCode;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    CategoryRepository categoryRepository;

    @InjectMocks
    CategoryService categoryService;

    private User user;

    private User newUser() {
        User u = User.builder().email("test@moneylog.com").password("encoded-pw").nickname("테스터").build();
        ReflectionTestUtils.setField(u, "id", 1L);
        return u;
    }

    @Test
    void getMyCategories_returnsOwnedCategoriesOnly() {
        user = newUser();
        Category food = Category.builder().user(user).name("식비").type(CategoryType.EXPENSE).build();
        when(categoryRepository.findByUser(user)).thenReturn(List.of(food));

        List<CategoryResponse> result = categoryService.getMyCategories(user);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("식비");
    }

    @Test
    void create_success_savesNewCategory() {
        user = newUser();
        CategoryRequest req = new CategoryRequest("취미", CategoryType.EXPENSE);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            ReflectionTestUtils.setField(category, "id", 10L);
            return category;
        });

        CategoryResponse response = categoryService.create(user, req);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.name()).isEqualTo("취미");
        assertThat(response.type()).isEqualTo(CategoryType.EXPENSE);
    }

    @Test
    void update_ownedCategory_updatesFields() {
        user = newUser();
        Category category = Category.builder().user(user).name("식비").type(CategoryType.EXPENSE).build();
        when(categoryRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(category));

        CategoryResponse response = categoryService.update(user, 1L, new CategoryRequest("외식", CategoryType.EXPENSE));

        assertThat(response.name()).isEqualTo("외식");
    }

    @Test
    void update_notOwnedCategory_throwsCategoryNotFound() {
        user = newUser();
        when(categoryRepository.findByIdAndUser(eq(99L), eq(user))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.update(user, 99L, new CategoryRequest("외식", CategoryType.EXPENSE)))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.CATEGORY_NOT_FOUND);
    }

    @Test
    void delete_ownedCategory_deletesIt() {
        user = newUser();
        Category category = Category.builder().user(user).name("식비").type(CategoryType.EXPENSE).build();
        when(categoryRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(category));

        categoryService.delete(user, 1L);

        verify(categoryRepository).delete(category);
    }

    @Test
    void delete_notOwnedCategory_throwsCategoryNotFound() {
        user = newUser();
        when(categoryRepository.findByIdAndUser(eq(99L), eq(user))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.delete(user, 99L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.CATEGORY_NOT_FOUND);

        verify(categoryRepository, never()).delete(any());
    }
}
