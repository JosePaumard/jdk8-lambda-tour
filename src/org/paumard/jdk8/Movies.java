/*
 * Copyright (C) 2014 José Paumard
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.paumard.jdk8;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IntSummaryStatistics;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.paumard.model.Actor;
import org.paumard.model.Movie;

/**
 *
 * @author José
 */
public class Movies {
    
    
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
                        // On ne prend pas en compte les films qui ont une
                        // virgule dans le titre
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
        
        // nombre d'années de production
        int annees = 
        movies.stream()
                .map(movie -> movie.releaseYear())
                .collect(Collectors.toSet())
                .size() ;
        System.err.println("Nombre d'années de production = " + annees) ;
        
        // intervalle des années de production
        IntSummaryStatistics stats = 
        movies.stream()
                .mapToInt(movie -> movie.releaseYear())
                .summaryStatistics() ;
        System.err.println("De " + stats.getMin() + " à " + stats.getMax()) ;
        
        // Film dans lequel le plus d'acteurs ont joué
        Map.Entry<Integer, Long> entry1 = 
        movies.stream()
                .collect(
                        Collectors.groupingBy(
                                movie -> movie.actors().size(), 
                                Collectors.counting()
                        )
                )
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .get() ;
        System.out.println("Plus grand nombre d'acteurs et nombre de films : " + entry1) ;
        
        // Annee qui a vu le plus de films produits
        Map.Entry<Integer, Long> entry2 = 
        movies.stream()
                .collect(
                        Collectors.groupingBy(
                                movie -> movie.releaseYear(), 
                                Collectors.counting()
                        )
                )
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .get() ;
        
        long debut = System.currentTimeMillis() ;
        System.out.println("Année qui a vu le plus de films produits : " + entry2) ;
        
        // Acteur qui a joué dans le plus de films
        Map.Entry<Actor, Long> entry3 = 
        actors.stream().parallel()
                .collect(
                        Collectors.toMap(
                                Function.identity(), 
                                actor -> movies.stream().filter(movie -> movie.actors().contains(actor)).count()
                        )
                )
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .get() ;
        System.out.println("Acteur qui a joué dans le plus de films : " + entry3) ;
        
        // Acteur qui a joué dans le plus de films durant une année
        Map.Entry<Actor, Map.Entry<Integer, Long>> entry4 = 
        actors.stream().parallel()
                .collect(
                        Collectors.toMap(
                                Function.identity(), 
                                actor -> movies.stream()
                                            .filter(movie -> movie.actors().contains(actor))
                                            .collect(
                                                    Collectors.groupingBy(
                                                            Movie::releaseYear,
                                                            Collectors.counting()
                                                    )
                                            )
                                            .entrySet()
                                            .stream()
                                            .max(Map.Entry.comparingByValue())
                                            .get()
                        )
                )
                .entrySet()
                .stream()
                .max(Comparator.comparing(entry -> entry.getValue().getValue()))
                .get() ;
        System.out.println("L'acteur qui a le plus joué durant une année : " + entry4) ;
        
        
        long fin = System.currentTimeMillis() ;
        System.err.println((fin - debut) + "ms");
    }
}
