package com.recipebox.App.repository;

import com.recipebox.App.entity.Favorites;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorites, Integer> {

    List<Favorites> findByUserUserId(int userId);

    Optional<Favorites> findByUserUserIdAndRecipeRecipeId(int userId, int recipeId); 
}