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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author José
 */
@SuppressWarnings("unused")
public class Scrabble {
    
    private static final int [] scrabbleENScore = {
     // a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p,  q, r, s, t, u, v, w, x, y,  z
        1, 3, 3, 2, 1, 4, 2, 4, 1, 8, 5, 1, 3, 1, 1, 3, 10, 1, 1, 1, 1, 4, 4, 8, 4, 10} ;
    
	private static final int [] scrabbleENDistribution = {
     // a, b, c, d,  e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z
        9, 2, 2, 1, 12, 2, 3, 2, 9, 1, 1, 4, 2, 6, 8, 2, 1, 6, 4, 6, 4, 2, 2, 1, 2, 1} ;
    
    
    private static final int [] scrabbleFRScore = {
     // a,  b, c, d, e, f, g, h, i, j,  k, l, m, n, o, p, q, r, s, t, u, v,  w,  x,  y,  z
        1,  3, 3, 2, 1, 4, 2, 4, 1, 8, 10, 1, 2, 1, 1, 3, 8, 1, 1, 1, 1, 4, 10, 10, 10, 10} ;
 
    private static final int [] scrabbleFRDistribution = {
     // a, b, c, d,  e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z
        9, 2, 2, 3, 15, 2, 2, 2, 8, 1, 1, 5, 3, 6, 6, 2, 1, 6, 6, 6, 6, 2, 1, 1, 1, 1} ;
    
    private static final IntUnaryOperator scrabbleLetterValueEN =
            letter -> scrabbleENScore[letter - 'a'] ;
    
    private static final IntUnaryOperator scrabbleLetterValueFR =
            letter -> scrabbleFRScore[letter - 'a'] ;
    
    
	public static void main(String... args) {

		Set<String> scrabbleWords = null ;
		Set<String> shakespeareWords = null ;
        try (Stream<String> scrabbleWordsStream = Files.lines(Paths.get("files", "ospd.txt")) ;
             Stream<String> shakespeareWordsStream = Files.lines(Paths.get("files", "words.shakespeare.txt")) ;
        ) {
	        scrabbleWords = scrabbleWordsStream.map(String::toLowerCase).collect(Collectors.toSet()) ;
	        shakespeareWords = shakespeareWordsStream.map(String::toLowerCase).collect(Collectors.toSet()) ;
        } catch (IOException e) {
			e.printStackTrace();
		}
        
        System.out.println("# of words in the Scrabble dictionnary : " + scrabbleWords.size()) ;
        
        //  number of words used by Shakespeare
        Long nWords1 = shakespeareWords.stream().count() ;
        System.out.println("# of words used by Shakespeare  : " + nWords1) ;
        
        // number of words used by Shakespeare and allowed at Scrabble
        long count = 
        shakespeareWords.stream()
                .filter(scrabbleWords::contains)
                .count() ;
        System.out.println("# number of words used by Shakespeare and allowed at Scrabble = " + count);
        
        // words of Shakespeare grouped by their length
        Map<Integer, Long> map1 = 
        shakespeareWords.stream()
                .collect(
                    Collectors.groupingBy(
                		String::length, 
                        () -> new TreeMap<Integer, Long>(Comparator.<Integer>naturalOrder().reversed()),  
                        Collectors.counting()
                    )
                ) ;
        System.out.println("Words of Shakespeare grouped by their length = " + map1) ;
        
        // words of Shakespeare of 16 letters and more
        Map<Integer, List<String>> map2 = 
        shakespeareWords.stream()
                .filter(word -> word.length() > 15)
                .collect(
                    Collectors.groupingBy(
                        String::length, 
                        () -> new TreeMap<Integer, List<String>>(Comparator.<Integer>naturalOrder().reversed()), 
                        Collectors.toList()
                    )
                ) ;
        System.out.println("Words of Shakespeare of 16 letters and more = " + map2) ;
        
        // # of words of Shakespeare grouped by their Scrabble score
        // in descending order
        Function<String, Integer> score = word -> word.chars().map(scrabbleLetterValueEN).sum() ;
        Map<Integer, Long> map3 =
        shakespeareWords.stream()
                .filter(scrabbleWords::contains)
                .collect(
                    Collectors.groupingBy(
                        score, 
                        () -> new TreeMap<Integer, Long>(Comparator.<Integer>naturalOrder().reversed()),
                        Collectors.counting()
                    )
                ) ;
        System.out.println("# of words of Shakespeare grouped by their Scrabble score = " + map3) ;
        
        // words of Shakespeare grouped by their Scrabble score, with a score greater than 28
        // in ascending order
        Predicate<String> scoreGT28 = word -> score.apply(word) > 28 ;
        Map<Integer, List<String>> map4 =
        shakespeareWords.stream()
                .map(String::toLowerCase)
                .filter(scrabbleWords::contains)
                .filter(scoreGT28)
                .collect(
                    Collectors.groupingBy(
                        score, 
                        TreeMap::new,
                        Collectors.toList()
                    )
                ) ;
        System.out.println("Words of Shakespeare grouped by their Scrabble score = " + map4) ;
        
        // histogram of the letters in a given word
        Function<String, Map<Integer, Long>> lettersHisto = 
            word -> word.chars()
                        .boxed()
                        .collect(
                            Collectors.groupingBy(
                                Function.identity(),
                                Collectors.counting()
                            )
                        ) ;
            
        // Predicate to check if a word can be written without the use of blanks
        Predicate<String> noBlank = 
            word -> lettersHisto.apply(word)
                        .entrySet()
                        .stream() // Map.Entry<letters, # used>
                        .allMatch(
                            entry -> entry.getValue() <= 
                            		 scrabbleENDistribution[entry.getKey() - 'a']
                        ) ;
        System.out.println("Can we write buzzards without blanks? " + noBlank.test("buzzards")) ;
        System.out.println("Can we write whizzings without blanks? " + noBlank.test("whizzings")) ;
                        
        // score of a given word, taking into account that the given word
        // might contain blank letters
        Function<String, Integer> scoreWithBlanks = 
            word -> lettersHisto.apply(word)
                        .entrySet()
                        .stream() // Map.Entry<letters, # used>
                        .mapToInt(
                           entry -> scrabbleENScore[entry.getKey() - 'a']*
                                    (int)Long.min(entry.getValue(), scrabbleENDistribution[entry.getKey() - 'a'])
                        )
                        .sum() ;
            
        // number of blanks used for the given word
        Function<String, Integer> blanksUsed = 
                word -> lettersHisto.apply(word)
                            .entrySet()
                            .stream() // Map.Entry<letters, # used>
                            .mapToInt(
                               entry -> (int)Long.max(0L, entry.getValue() - scrabbleENDistribution[entry.getKey() - 'a'])
                            )
                            .sum() ;
                
        System.out.println("Number of blanks in [buzzards] = " + blanksUsed.apply("buzzards")) ;
        System.out.println("Real score of [buzzards] = " + scoreWithBlanks.apply("buzzards")) ;
        System.out.println("Number of blanks in [whizzing] = " + blanksUsed.apply("whizzing")) ;
        System.out.println("Real score of [whizzing] = " + scoreWithBlanks.apply("whizzing")) ;
                
        // best words of Shakespeare and their scores
        Map<Integer, List<String>> map = 
                shakespeareWords.stream()
                        .filter(scrabbleWords::contains)
                        .filter(word -> blanksUsed.apply(word) <= 2L)
                        .filter(word -> scoreWithBlanks.apply(word) >= 24)
                        .collect(
                            Collectors.groupingBy(
                        		scoreWithBlanks, 
                                Collectors.toList()
                            )
                        ) ;
        System.out.println("Best words of Shakespeare : " + map) ;
        
        // best word that Shakespeare could have played as a first move
        // scoring function
        Function<String, Integer> scoreOnBoard = 
        		word -> 2*( // the first word scores double at Scrabble 
	        				scoreWithBlanks.apply(word) + 
	        				Stream.of(
	        				    word.chars().skip(4), 
	        				    word.chars().limit(Integer.max(0, word.length() - 4))
	        				)
	        				.flatMapToInt(Function.identity())
	        				.map(scrabbleLetterValueEN)
	        				.max()
	        				.orElse(0)
	        			) +
	        			(word.length() == 7 ? 50 : 0) ; // there is a 50 pts bonus for a 7 letters word
        Map<Integer, List<String>> mapOnBoard = 
                shakespeareWords.stream()
                        .filter(scrabbleWords::contains)
                        .filter(word -> blanksUsed.apply(word) <= 2L)
                        .filter(word -> scoreOnBoard.apply(word ) >= 114)
                        .collect(
                            Collectors.groupingBy(
                        		scoreOnBoard, 
                        		() -> new TreeMap<Integer, List<String>>(Comparator.<Integer>naturalOrder().reversed()),
                                Collectors.toList()
                            )
                        ) ;
         System.out.println("Best words of Shakespeare played as first move : " + mapOnBoard) ;
        
    }
}
