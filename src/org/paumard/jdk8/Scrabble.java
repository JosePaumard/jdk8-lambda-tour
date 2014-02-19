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

import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author José
 */
public class Scrabble {
    
    private static final int [] scrabbleENScore = {
     // a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p,  q, r, s, t, u, v, w, x, y,  z
        1, 3, 3, 2, 1, 4, 2, 4, 1, 8, 5, 1, 3, 1, 1, 3, 10, 1, 1, 1, 1, 4, 4, 8, 4, 10} ;
    
    
    
    private static final int [] scrabbleFRScore = {
     // a,  b, c, d, e, f, g, h, i, j,  k, l, m, n, o, p, q, r, s, t, u, v,  w,  x,  y,  z
        1,  3, 3, 2, 1, 4, 2, 4, 1, 8, 10, 1, 2, 1, 1, 3, 8, 1, 1, 1, 1, 4, 10, 10, 10, 10} ;
    
    private static final IntUnaryOperator scrabbleLetterValueEN =
            c -> scrabbleENScore[c - 'a'] ;
    
    private static final IntUnaryOperator scrabbleLetterValueFR =
            c -> scrabbleFRScore[c - 'a'] ;
    
    
    public static int getScrabbleScore(String word) {
        
        return word.toLowerCase().chars().map(scrabbleLetterValueFR).sum() ;
    }
    
    public static void main(String... args) throws Exception {
        
        LineNumberReader scrabbleWordsReader = new LineNumberReader(new FileReader("files/ospd.txt")) ;
        Stream<String> scrabbleWordsStream = scrabbleWordsReader.lines() ;
        Set<String> scrabbleWords = scrabbleWordsStream.map(String::toLowerCase).collect(Collectors.toSet()) ;
        
        LineNumberReader shakespearWordsReader = new LineNumberReader(new FileReader("files/words.shakespeare.txt")) ;
        Stream<String> shakespeareWordsStream = shakespearWordsReader.lines() ;
        Set<String> shakespeareWords = shakespeareWordsStream.map(String::toLowerCase).collect(Collectors.toSet()) ;
        
        System.out.println("# de mots autorisés au Scrabble : " + scrabbleWords.size()) ;
        
        // mots utilisés par Shakespeare
        Long nWords1 = shakespeareWords.stream().count() ;
        System.out.println("# de mots utilisés par Shakespeare  : " + nWords1) ;
        
        // nombre de mots utilisés par Shakespeare et autorisés au Scrabble
        long count = 
        shakespeareWords.stream()
                .map(String::toLowerCase)
                .filter(scrabbleWords::contains)
                .count() ;
        System.out.println("# mots utilisés par Shakespeare autorisés au scrabble = " + count);
        
        // répartition des mots de shakespeare en fonction de leur taille
        Map<Integer, Long> map1 = 
        shakespeareWords.stream()
                .collect(
                        Collectors.groupingBy(
                                String::length, 
                                Collectors.counting()
                        )
                ) ;
        System.out.println("Mots de shakespeare en fonction de leur taille = " + map1) ;
        
        // mots de shakespeare de 16 lettres et plus
        Map<Integer, List<String>> map2 = 
        shakespeareWords.stream()
                .filter(word -> word.length() > 15)
                .collect(
                        Collectors.groupingBy(
                                String::length
                        )
                ) ;
        System.out.println("Mots de Shakespeare de 16 lettres et plus = " + map2) ;
        
        // répartition des mots de Shakespeare en fonction de leur score au Scrabble
        // trié par score croissant
        Function<String, Integer> score = word -> word.chars().map(scrabbleLetterValueEN).sum() ;
        Map<Integer, Long> map3 =
        shakespeareWords.stream()
                .map(String::toLowerCase)
                .filter(scrabbleWords::contains)
                .collect(
                        Collectors.groupingBy(
                                score, 
                                TreeMap::new,
                                Collectors.counting()
                        )
                ) ;
        System.out.println("Répartition des mots de Shakespeare en fonction de leur score au Scrabble = " + map3) ;
        
        // mots de Shakespeare en fonction de leur score au Scrabble, pour les scores de 29 et plus
        // trié par score croissant
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
        System.out.println("Mots de Shakespeare en fonction de leur score au Scrabble = " + map4) ;
    }
}
