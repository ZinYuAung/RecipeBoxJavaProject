package com.recipebox.App.service;

import java.util.List;

import com.recipebox.App.entity.Favorites;

public interface FavoriteService {

	List<Favorites> getFavoriteRecipesByUser(int userId);

	boolean toggleFavorite(int userId, int recipeId);
	
	List<Integer> getFavoriteRecipeIdsByUser(int userId);
}
