package com.app.Livetracker.repository;


import com.app.Livetracker.entity.products;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public interface productRepository extends JpaRepository<products,Long> {

}

