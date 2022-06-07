package com.thehecklers.personservice;

import lombok.AllArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@SpringBootApplication
public class PersonServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PersonServiceApplication.class, args);
    }

}

@RestController
@AllArgsConstructor
class PersonController {
	private final PersonRepository repo;

	@GetMapping
	String status() { return "Person service is UP!"; }

	@GetMapping("/people")
	Iterable<Person> getPeople(@RequestParam(required = false) Long qty) {
		return repo.findSome((null == qty) ? 100 : qty);
	}

	@GetMapping("/findbyname")
	Iterable<Person> findByNameContaining(@RequestParam String name) {
		return repo.findByNameContaining(name);
	}

	@GetMapping("/characters")
	Iterable<Person> findCharacters(@RequestParam String id) {
		return repo.findCharactersForPersonId(id);
	}

	@GetMapping("/directed")
	Iterable<Person> findDirected(@RequestParam String id) {
		return repo.findProductionsForPersonId(id);
	}
}

interface PersonRepository extends Neo4jRepository<Person, String> {
	// findAll()

	Iterable<Person> findByNameContaining(String name);

	@Query("MATCH (p:Person {personId: $id})-[r:PLAYED]-(c:Character) RETURN p, collect(r), collect(c);")
	Iterable<Person> findCharactersForPersonId(String id);

	@Query("MATCH (p:Person {personId: $id})-[r:DIRECTED]->(pro:Production) RETURN p, collect(r), collect(pro);")
	Iterable<Person> findProductionsForPersonId(String id);

	@Query("MATCH (p:Person)-[r]-(other) RETURN p, collect(r), collect(other) LIMIT $qty;")
	Iterable<Person> findSome(Long qty);
}

@Node
record Person(@Id String personId,
			  String name,
			  @Relationship("PLAYED") List<Character> characters,
			  @Relationship("DIRECTED") List<Production> directs) {
}

@Node
record Character(@Id @GeneratedValue Long neoId,String name) {}

@Node
record Production(@Id String productionId, String title, String description,
				  LocalDate releaseYear, String rating, Integer runtime) {}