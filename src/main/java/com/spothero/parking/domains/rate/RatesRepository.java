package com.spothero.parking.domains.rate;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RatesRepository extends JpaRepository<Rate, Long> {

    @Override
    @EntityGraph(value = "rate.default")
    public List<Rate> findAll();

}
