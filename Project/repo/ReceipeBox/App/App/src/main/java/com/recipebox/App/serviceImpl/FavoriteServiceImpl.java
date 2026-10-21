package com.recipebox.App.serviceImpl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recipebox.App.entity.Favorites;
import com.recipebox.App.entity.Recipe;
import com.recipebox.App.entity.User;
import com.recipebox.App.repository.FavoriteRepository;
import com.recipebox.App.repository.RecipeRepository;
import com.recipebox.App.repository.UserRepository;
import com.recipebox.App.service.FavoriteService;

@Service
public class FavoriteServiceImpl implements FavoriteService {

	@Autowired
	private FavoriteRepository favoriteRepository;

	@Autowired
	private RecipeRepository recipeRepository;

	@Autowired
	private UserRepository userRepository;

	@Override
	@Transactional(readOnly = true)
	public List<Favorites> getFavoriteRecipesByUser(int userId) {
		return favoriteRepository.findByUserUserId(userId);
	}

	@Override
	@Transactional
	public boolean toggleFavorite(int userId, int recipeId) {
		Optional<Favorites> existingFav = favoriteRepository.findByUserUserIdAndRecipeRecipeId(userId, recipeId);
		if (existingFav.isPresent()) {
			favoriteRepository.delete(existingFav.get());
			return false;
		}
		Recipe recipe = recipeRepository.findById(recipeId)
				.orElseThrow(() -> new IllegalArgumentException("Recipe not found with ID: " + recipeId));
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
		Favorites newFavorite = new Favorites();
		newFavorite.setUser(user);
		newFavorite.setRecipe(recipe);
		favoriteRepository.save(newFavorite);
		return true;
	}

	@Override
	public List<Integer> getFavoriteRecipeIdsByUser(int userId) {
	    List<Favorites> favorites = favoriteRepository.findByUserUserId(userId);
	    return favorites.stream()
	            .map(fav -> fav.getRecipe().getRecipeId())
	            .toList();
	}

}
