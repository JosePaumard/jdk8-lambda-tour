package org.paumard.jdk8;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.paumard.model.Actor;
import org.paumard.model.Movie;

public class MovieV2 {
	
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
                        // Movies with a coma in their title are discarded
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
        
        // Actor that played in the greatest number of movies
        // Much faster version than the one in Movies, thanks to Celine !
        Map.Entry<Actor, Long> entry3 = 
        		movies.stream()
                .flatMap(movie -> movie.actors().stream())
                .collect(
                        Collectors.groupingBy(
                                Function.identity(), 
                                Collectors.counting()
                        )
                )
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .get() ;
        System.out.println("Actor that played in the greatest number of movies : " + entry3) ;
        
        // Actor that played in the greatest number of movies during a year
        // Much faster version than the one in Movies
        Map.Entry<Integer, Map.Entry<Actor, AtomicLong>> entry4 = 
        		movies.stream()
                .collect(
                        Collectors.groupingBy(
                                movie -> movie.releaseYear(), 
                                // Collector<? super T, A, D>
                                Collector.of(
                                        () -> new HashMap<Actor, AtomicLong>(), 
                                        (map, movie) -> {
                                            movie.actors().forEach(
                                                    actor -> map.computeIfAbsent(actor, a -> new AtomicLong()).incrementAndGet()
                                            ) ;
                                        },
                                        (map1, map2) -> {
                                            map2.entrySet().stream().forEach(
                                                    entry -> map1.computeIfAbsent(entry.getKey(), a -> new AtomicLong()).addAndGet(entry.getValue().get())
                                            ) ;
                                            return map1 ;
                                        }, 
                                        new Collector.Characteristics [] {
                                            Collector.Characteristics.CONCURRENT.CONCURRENT
                                        }
                                )
                        )
                )
                .entrySet()
                .stream()
                .collect(
                        Collectors.toMap(
                                entry5 -> entry5.getKey(),
                                entry5 -> entry5.getValue()
                                                    .entrySet()
                                                    .stream()
                                                    .max(Map.Entry.comparingByValue(Comparator.comparing(l -> l.get())))
                                                    .get()
                        )
                )
                .entrySet()
                .stream()
                .max(Comparator.comparing(entry -> entry.getValue().getValue().get()))
                .get() ;
        System.out.println("Actor that played in the greatest number of movies during a year : " + entry4) ;
    }
}
