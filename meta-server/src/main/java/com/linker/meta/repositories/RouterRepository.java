package com.linker.meta.repositories;

import com.linker.common.router.Router;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RouterRepository extends CrudRepository<Router, String> {
}
