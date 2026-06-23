package com.example.smart_cinema_booking_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.smart_cinema_booking_system.entity.Movie;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
}
