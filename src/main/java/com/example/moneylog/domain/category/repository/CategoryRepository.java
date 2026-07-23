package com.example.moneylog.domain.category.repository;

import com.example.moneylog.domain.category.entity.Category;
import com.example.moneylog.domain.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUser(User user);
    Optional<Category> findByIdAndUser(Long id, User user);
    boolean existsByUser(User user);
}