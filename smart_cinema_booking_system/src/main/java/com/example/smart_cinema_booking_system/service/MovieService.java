package com.example.smart_cinema_booking_system.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.smart_cinema_booking_system.dto.request.MovieRequestDTO;
import com.example.smart_cinema_booking_system.dto.response.MovieResponseDTO;
import com.example.smart_cinema_booking_system.entity.Genre;
import com.example.smart_cinema_booking_system.entity.Movie;
import com.example.smart_cinema_booking_system.exception.BusinessException;
import com.example.smart_cinema_booking_system.repository.GenreRepository;
import com.example.smart_cinema_booking_system.repository.MovieRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final CloudinaryService cloudinaryService;

    public List<MovieResponseDTO> getAllMovies() {
        return movieRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public MovieRequestDTO getMovieForEdit(Long id) {
        Movie movie = findMovieById(id);
        MovieRequestDTO dto = new MovieRequestDTO();
        dto.setTitle(movie.getTitle());
        dto.setDescription(movie.getDescription());
        dto.setDuration(movie.getDuration());
        dto.setReleaseDate(movie.getReleaseDate());
        dto.setLanguage(movie.getLanguage());
        dto.setTrailerUrl(movie.getTrailerUrl());
        dto.setAgeRating(movie.getAgeRating());
        dto.setStatus(movie.getStatus());
        dto.setGenreIds(movie.getGenres().stream().map(Genre::getGenreId).collect(Collectors.toList()));
        return dto;
    }
    
    public MovieResponseDTO getMovieById(Long id) {
    	return mapToResponseDTO(findMovieById(id));
    }

    @Transactional
    public void createMovie(MovieRequestDTO dto) {
        Movie movie = new Movie();
        updateMovieEntity(movie, dto);
        movieRepository.save(movie);
    }

    @Transactional
    public void updateMovie(Long id, MovieRequestDTO dto) {
        Movie movie = findMovieById(id);
        updateMovieEntity(movie, dto);
        movieRepository.save(movie);
    }

    @Transactional
    public void deleteMovie(Long id) {
        Movie movie = findMovieById(id);
        // Có thể đổi status sang INACTIVE thay vì xóa cứng nếu muốn
        movieRepository.delete(movie);
    }

    private Movie findMovieById(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy phim!"));
    }

    private void updateMovieEntity(Movie movie, MovieRequestDTO dto) {
        movie.setTitle(dto.getTitle());
        movie.setDescription(dto.getDescription());
        movie.setDuration(dto.getDuration());
        movie.setReleaseDate(dto.getReleaseDate());
        movie.setLanguage(dto.getLanguage());
        movie.setTrailerUrl(dto.getTrailerUrl());
        movie.setAgeRating(dto.getAgeRating());
        movie.setStatus(dto.getStatus());

        if (dto.getGenreIds() != null && !dto.getGenreIds().isEmpty()) {
            List<Genre> genres = genreRepository.findAllById(dto.getGenreIds());
            if (genres.size() != dto.getGenreIds().size()) {
                throw new BusinessException("Một số thể loại không tồn tại!");
            }
            movie.setGenres(genres);
        } else {
            movie.setGenres(null);
        }

        if (dto.getPosterFile() != null && !dto.getPosterFile().isEmpty()) {
            String posterUrl = cloudinaryService.uploadAvatar(dto.getPosterFile(), "movie_poster_" + System.currentTimeMillis());
            movie.setPosterUrl(posterUrl);
        }

        if (dto.getBackdropFile() != null && !dto.getBackdropFile().isEmpty()) {
            String backdropUrl = cloudinaryService.uploadAvatar(dto.getBackdropFile(), "movie_backdrop_" + System.currentTimeMillis());
            movie.setBackdropUrl(backdropUrl);
        }
    }

    private MovieResponseDTO mapToResponseDTO(Movie movie) {
        MovieResponseDTO dto = new MovieResponseDTO();
        dto.setMovieId(movie.getMovieId());
        dto.setTitle(movie.getTitle());
        dto.setDescription(movie.getDescription());
        dto.setDuration(movie.getDuration());
        dto.setReleaseDate(movie.getReleaseDate());
        dto.setLanguage(movie.getLanguage());
        dto.setPosterUrl(movie.getPosterUrl());
        dto.setBackdropUrl(movie.getBackdropUrl());
        dto.setTrailerUrl(movie.getTrailerUrl());
        dto.setAgeRating(movie.getAgeRating());
        dto.setStatus(movie.getStatus());
        dto.setGenres(movie.getGenres());
        return dto;
    }
}
