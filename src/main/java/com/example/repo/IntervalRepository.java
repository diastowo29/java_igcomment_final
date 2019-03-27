package com.example.repo;

import org.springframework.data.repository.CrudRepository;

import com.example.model.Interval;

public interface IntervalRepository extends CrudRepository<Interval, Long> {
	Interval findByCifAccountId(String cifAccountId);
}
