package org.paumard.jdk8;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IntSummaryStatistics;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.paumard.model.Actor;
import org.paumard.model.Movie;

public class MovieV3 {

	public static void main(String... args) throws Exception {

		Set<Movie> movies = new HashSet<>() ;

		Stream<String> lines =
			Files.lines(
					Paths.get("files", "movies-mpaa.txt"),
					Charset.forName("windows-1252")
					) ;

		lines.forEach(
			(String line) -> {
				String[] elements = line.split("/") ;
				String title = elements[0].substring(0, elements[0].toString().lastIndexOf("(")).trim() ;
				String releaseYear = elements[0].substring(elements[0].toString().lastIndexOf("(") + 1, elements[0].toString().lastIndexOf(")")) ;

				if (releaseYear.contains(",")) {
					// with skip movies with a coma in their title
					return ;
				}

				Movie movie = new Movie(title, Integer.valueOf(releaseYear)) ;

				for (int i = 1 ; i < elements.length ; i++) {
					String [] name = elements[i].split(", ") ;
					String lastName = name[0].trim() ;
					String firstName = "" ;
					if (name.length > 1) {
						firstName = name[1].trim() ;
					}

					Actor actor = new Actor(lastName, firstName) ;
					movie.addActor(actor) ;
				}

				movies.add(movie) ;
			}
		) ;

		Set<Actor> actors =
			movies.stream()
			.flatMap(movie -> movie.actors().stream())
			.collect(Collectors.toSet()) ;

		System.out.println("# actors = " + actors.size()) ;
		System.out.println("# movies = " + movies.size()) ;

		// number of production years
		int annees =
			movies.stream()
			.map(movie -> movie.releaseYear())
			.collect(Collectors.toSet())
			.size() ;
		System.out.println("# années = " + annees) ;

		// interval of production years
		IntSummaryStatistics stats =
			movies.stream()
			.mapToInt(movie -> movie.releaseYear())
			.summaryStatistics() ;
		System.out.println("From " + stats.getMin() + " to " + stats.getMax()) ;

		long debut = System.currentTimeMillis() ;

		// Duo actor who played in more movies
		NavigableSet<Actor> keyActors =
			new ConcurrentSkipListSet<>(
					// I love this new way of creating comparators !
					Comparator.comparing(Actor::lastName).thenComparing(Actor::firstName)
			) ;
		keyActors.addAll(actors) ;

		System.out.println("# Key actors = " + keyActors.size()) ;

		Supplier<Map<Actor, Map<Actor, AtomicLong>>> mapSupplier = () -> new ConcurrentHashMap<>() ;
		AtomicLong nMaps = new AtomicLong() ;

		Map<Actor, Map<Actor, AtomicLong>> map3 = 
			keyActors.stream().parallel()
			.collect(
				mapSupplier, 
				(Map<Actor, Map<Actor, AtomicLong>> map, Actor actor) -> {
					NavigableSet<Actor> valueActors = keyActors.tailSet(actor, false) ;
					movies.stream()
						.filter(movie -> movie.actors().contains(actor))
						.forEach(movie -> {movie.actors().stream()
							.filter(actor1 -> valueActors.contains(actor1))
							.forEach(actor2 -> {
								Map<Actor, AtomicLong> subMap = map.computeIfAbsent(
										actor, 
										a -> { nMaps.incrementAndGet() ; 
										return new ConcurrentHashMap<>() ; }
								) ;
								subMap.computeIfAbsent(
										actor2, 
										actor3 -> new AtomicLong(1L)
								)
								.incrementAndGet() ;
							}) ;
						}) ;
				}, 
				(Map<Actor, Map<Actor, AtomicLong>> map1, Map<Actor, Map<Actor, AtomicLong>> map2) -> {
					map2.entrySet().stream()
						.forEach(
							(Map.Entry<Actor, Map<Actor, AtomicLong>> entry) -> {
								Map<Actor, AtomicLong> map11 = 
										map1.computeIfAbsent(entry.getKey(), actor -> new ConcurrentHashMap<>()) ;
								Map<Actor, AtomicLong> map21 = 
										entry.getValue() ;
								map21.entrySet().stream()
									.forEach(
										(Map.Entry<Actor, AtomicLong> entry21) -> { 
											map11.merge(
													entry21.getKey(), entry21.getValue(), 
													(AtomicLong l1, AtomicLong l2) -> {
														l1.addAndGet(l2.get()) ;
														return l1 ;
													}
													) ;
									}) ;
							}
						) ;
				}
			) ;

		System.out.println("map 3 : " + map3.size()) ;
		System.out.println("n maps = " + nMaps) ;

		Map.Entry<Actor, Map.Entry<Actor, AtomicLong>> e = 
		map3.entrySet().stream().parallel()
		.filter(entry -> !entry.getValue().entrySet().isEmpty())
		.collect(
			Collectors.toMap(
				entry -> entry.getKey(), 
				entry -> entry.getValue().entrySet().stream()
				.max(
					Map.Entry.comparingByValue(
							Comparator.comparingLong(AtomicLong::get)
					)
				)
				.get()
			)
		)
		.entrySet()
		.stream()
		.max(
			Map.Entry.comparingByValue(
				Comparator.comparingLong(
					entry -> entry.getValue().get()
				)
			)
		)
		.get() ;
		System.out.println("Most seen actor duo = " + e) ;

		long fin = System.currentTimeMillis() ;
		System.out.println("T = " + (fin - debut) + "ms");
	}
}
